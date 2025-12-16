package io.confluent.udf;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Deque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;

/**
 * A Table Function that recursively traverses a hierarchy and returns all
 * persons
 * under a given node (department or group).
 * 
 * Input: Takes the hierarchy as a nested ARRAY of ROWs and a starting node name
 * Output: Emits each person found under that node (at any depth)
 */
@FunctionHint(output = @DataTypeHint("ROW<group_name STRING, users ARRAY<STRING>>"))
public class HierarchyTraversal extends TableFunction {
    private static final Logger logger = LogManager.getLogger(HierarchyTraversal.class);

    /**
     * Traverse hierarchy starting from a given node
     * 
     * @param hierarchyData ARRAY of ROW<group_name STRING, item_name STRING,
     *                      item_type STRING>
     * @param startNode     The group name to start traversal from
     */
    public void eval(
            @DataTypeHint("ARRAY<ROW<group_name STRING, item_name STRING, item_type STRING>>") Row[] hierarchyData,
            String startNode) {
        if (hierarchyData == null || startNode == null) {
            return;
        }

        // Build adjacency map: parent -> list of children with their types
        Map<String, List<HierarchyItem>> subGroupsMap = new HashMap<>();
        Map<String, List<HierarchyItem>> membersMap = new HashMap<>();
        Set<String> allGroups = new HashSet<>();

        for (Row row : hierarchyData) {
            String group_name = (String) row.getField(0);
            String itemName = (String) row.getField(1);
            String itemType = (String) row.getField(2);

            if (group_name != null) {
                allGroups.add(group_name);
            }
            if (group_name != null && (itemName != null && ! itemName.equals("NULL"))) {
                if (itemType.equals("GROUP")) {
                     /* parent_group, sub_group, GROUP -> then subgroup is a group*/ 
                    allGroups.add(itemName);
                    subGroupsMap.computeIfAbsent(group_name, k -> new ArrayList<>())
                        .add(new HierarchyItem(itemName, itemType));
                } else if (itemType.equals("PERSON")) {
                    membersMap.computeIfAbsent(group_name, k -> new ArrayList<>())
                        .add(new HierarchyItem(itemName, itemType));
                }
            }
        }
       
        for (String groupName : allGroups) {
            List<String> users= findUsers(groupName, membersMap, subGroupsMap);
            collect(Row.of(groupName, users));
        }
    }

    private List<String> findUsers(String groupName, Map<String, List<HierarchyItem>> membersMap, Map<String, List<HierarchyItem>> subGroupsMap) {
        /**
         * Find all users in a group and its children
         * @param groupName
         * @param allGroups
         * @param membersMap <group_name, List<HierarchyItem>> the persons of the group
         * @param childrenMap <group_name, List<HierarchyItem>> the subgroups of the group 
         * @return
         */
        Set<String> users = new HashSet<String>();
        if (subGroupsMap.containsKey(groupName)) {
            for (HierarchyItem item : subGroupsMap.get(groupName)) {
                users.addAll(findUsers(item.name, membersMap, subGroupsMap));
            }
        }
        if (membersMap.containsKey(groupName)) {
            users.addAll(membersMap.get(groupName).stream().map(item -> item.name).toList());
        }
        return users.stream().toList();
    }


    private static class HierarchyItem {
        final String name;
        final String type;

        HierarchyItem(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * Returns a string describing the function.
     */
        @Override
        public String toString() {
            return "USERS_IN_GROUPS";
        }

}

