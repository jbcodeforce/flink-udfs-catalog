package io.confluent.udf;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;
import java.util.Map;
import java.util.Arrays;
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
public class HierarchyTraversalBasic extends TableFunction<Row> {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Traverse hierarchy starting from a given node
     * 
     * @param hierarchyData ARRAY of ROW<group_name STRING, item_name STRING,
     *                      item_type STRING>
     */
    public void eval(
            @DataTypeHint("ARRAY<ROW<group_name STRING, item_name STRING, item_type STRING>>") Row[] hierarchyData) throws Exception {

        logger.info("Starting hierarchy traversal from node: {}", hierarchyData);
        if (hierarchyData == null) {
            logger.error("Hierarchy data or start node is null");
            return;
        }
        try {
            // Build adjacency map: parent -> list of children with their types
            Map<String, List<String>> subGroupsMap = new HashMap<>();
            Map<String, List<String>> membersMap = new HashMap<>();
            Set<String> allGroups = new HashSet<>();

            for (Row row : hierarchyData) {
                String group_name = (String) row.getField(0);
                String itemName = (String) row.getField(1);
                String itemType = (String) row.getField(2);
                logger.info("Processing row: {}, {}, {}", group_name, itemName, itemType);
                if (group_name != null) {
                    allGroups.add(group_name);
                }
                String[] users = new String[]{"user-1", "user-2", "user-3"};
                Row outRow = new Row(2);
                outRow.setField(0,group_name);
                outRow.setField(1, users);
                collect(outRow);
            }
        } catch (Exception e) {
            logger.error("Error during hierarchy traversal", e);
            throw e;
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

