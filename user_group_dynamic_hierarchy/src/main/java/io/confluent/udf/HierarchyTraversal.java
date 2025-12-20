package io.confluent.udf;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * A Table Function that recursively traverses a hierarchy and returns all
 * persons under a given node (department or group).
 * 
 * Input: Takes the hierarchy as a nested ARRAY of ROWs and a starting node name
 * Output: Emits each person found under that node (at any depth)
 * 
 * The function maintains an internal cache to track the previous state of each
 * group's user list. It only emits (collects) a row when the users for a group
 * have changed compared to the previous invocation.
 */
@FunctionHint(output = @DataTypeHint("ROW<group_name STRING, users ARRAY<STRING>>"))
public class HierarchyTraversal extends TableFunction<Row> {
    private static final Logger logger = LogManager.getLogger();
    
    // Cache to store the previous user set for each group should be stored in a persistent store (not this implementation)
    private transient Map<String, Set<String>> cachedGroupUsers = new HashMap<>();

    /**
     * Traverse hierarchy starting from a given node
     * 
     * @param hierarchyData ARRAY of ROW<group_name STRING, item_name STRING,
     *                      item_type STRING>
     */
    public void eval(
            @DataTypeHint("ARRAY<ROW<group_name STRING, item_name STRING, item_type STRING>>") Row[] hierarchyData) throws Exception {

        logger.info("Starting hierarchy traversal from node: {}", hierarchyData);

        try {
            if (hierarchyData == null) {
                logger.error("Hierarchy data or start node is null");
                return;
            }
            
            // Build adjacency map: parent -> list of children with their types
            Map<String, List<String>> subGroupsMap = new HashMap<>();
            Map<String, List<String>> membersMap = new HashMap<>();
            Set<String> allGroups = new HashSet<>();

            for (Row row : hierarchyData) {

                String group_name = (String) row.getField(0);
                String itemName = (String) row.getField(1);
                String itemType = (String) row.getField(2);
                logger.info("Processing row: {}, {}, {}", group_name, itemName, itemType);
                if (group_name != null && ! allGroups.contains(group_name)) {
                    allGroups.add(group_name);
                }
                if (group_name != null && (itemName != null && ! itemName.equals("NULL"))) {
                    if (itemType.equals("GROUP")) {
                        /* parent_group, sub_group, GROUP -> then subgroup is a group*/ 
                        logger.info("Adding group: {} to allGroups", itemName);
                        allGroups.add(itemName);
                        subGroupsMap.computeIfAbsent(group_name, k -> new ArrayList<>())
                            .add(itemName);
                    } else if (itemType.equals("PERSON")) {
                        logger.info("Adding person: {} to membersMap", itemName);
                        membersMap.computeIfAbsent(group_name, k -> new ArrayList<>())
                            .add(itemName);
                    } else {
                        logger.error("Unknown item type: {}", itemType);
                    }
                } else {
                    logger.info("item_name is null or equals to NULL");
                }
            }
        
            // Track which groups are present in current data
            Set<String> currentGroups = new HashSet<>();
            
            for (String groupName : allGroups) {
                currentGroups.add(groupName);
                if (!membersMap.isEmpty()) {
                    List<String> usersList = findUsers(groupName, membersMap, subGroupsMap);
                    
                    Set<String> currentUsers = new HashSet<>(usersList);
                    
                    // Check if users have changed compared to cache
                    Set<String> previousUsers = cachedGroupUsers.computeIfAbsent(groupName, k -> new HashSet<>());
                    if ( !previousUsers.equals(currentUsers)) {
                        logger.info("Group {} users changed. Previous: {}, Current: {}", 
                            groupName, previousUsers, currentUsers);
                        // Update cache and emit the change
                        cachedGroupUsers.put(groupName, currentUsers);
                        // change type to primitive for Row.of to work
                        String[] users = usersList.toArray(new String[0]);
                        Row outRow = new Row(2);
                        outRow.setField(0,groupName);
                        outRow.setField(1, users);
                        collect(outRow);
                    } else {
                        logger.debug("Group {} users unchanged, skipping collect", groupName);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during hierarchy traversal", e);
            throw e;
        }
    }

    private List<String> findUsers(String groupName, Map<String, List<String>> membersMap, Map<String, List<String>> subGroupsMap) {
        /**
         * Find all users in a group and its children
         * @param groupName
         * @param allGroups
         * @param membersMap <group_name, List<HierarchyItem>> the persons of the group
         * @param childrenMap <group_name, List<HierarchyItem>> the subgroups of the group 
         * @return
         */
        logger.info("Finding users for group: {}", groupName);
        Set<String> users = new HashSet<String>();
        try {
            if (subGroupsMap.containsKey(groupName)) {
                for (String item : subGroupsMap.get(groupName)) {
                    users.addAll(findUsers(item, membersMap, subGroupsMap));
                }
            } else {
                logger.warn("Group: {} not found in subGroupsMap", groupName);
            }
            if (membersMap.containsKey(groupName)) {
                users.addAll(membersMap.get(groupName).stream().toList());
            } else {
                logger.warn("Group: {} not found in membersMap", groupName);
            }
        } catch (Exception e) {
            logger.error("Error finding users for group: {}", groupName, e);
            throw e;
        }
        return users.stream().toList();
    }

    /**
     * Returns a string describing the function.
     */
        @Override
        public String toString() {
            return "USERS_IN_GROUPS";
        }

}

