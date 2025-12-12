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
@FunctionHint(output = @DataTypeHint("ROW<person_name STRING, depth INT, path STRING>"))
public class HierarchyTraversal extends TableFunction {
    private static final Logger logger = LogManager.getLogger(HierarchyTraversal.class);

    /**
     * Traverse hierarchy starting from a given node
     * 
     * @param hierarchyData ARRAY of ROW<department_name STRING, item_name STRING,
     *                      item_type STRING>
     * @param startNode     The department or group name to start traversal from
     * @param maxDepth      Maximum depth to traverse (safety limit)
     */
    public void eval(
            @DataTypeHint("ARRAY<ROW<department_name STRING, item_name STRING, item_type STRING>>") Row[] hierarchyData,
            String startNode,
            Integer maxDepth) {
        if (hierarchyData == null || startNode == null) {
            return;
        }

        // Build adjacency map: parent -> list of children with their types
        Map<String, List<HierarchyItem>> childrenMap = new HashMap<>();

        for (Row row : hierarchyData) {
            String departmentName = (String) row.getField(0);
            String itemName = (String) row.getField(1);
            String itemType = (String) row.getField(2);

            if (departmentName != null && itemName != null) {
                childrenMap
                        .computeIfAbsent(departmentName, k -> new ArrayList<>())
                        .add(new HierarchyItem(itemName, itemType));
            }
        }

        // Recursive traversal using a stack (to avoid stack overflow)
        Deque<TraversalState> stack = new ArrayDeque<>();
        stack.push(new TraversalState(startNode, 0, startNode));

        Set<String> visited = new HashSet<>(); // Prevent cycles

        while (!stack.isEmpty()) {
            TraversalState current = stack.pop();

            if (current.depth > maxDepth || visited.contains(current.nodeName)) {
                continue;
            }
            visited.add(current.nodeName);

            List<HierarchyItem> children = childrenMap.get(current.nodeName);
            if (children == null) {
                continue;
            }

            for (HierarchyItem child : children) {
                String newPath = current.path + " -> " + child.name;

                if ("PERSON".equals(child.type)) {
                    // Emit person found
                    collect(Row.of(child.name, current.depth + 1, newPath));
                } else if ("GROUP".equals(child.type)) {
                    // Continue traversal into subgroup
                    stack.push(new TraversalState(child.name, current.depth + 1, newPath));
                }
            }
        }
    }

    // Overload with default max depth
    public void eval(Row[] hierarchyData, String startNode) {
        eval(hierarchyData, startNode, 10); // Default max depth of 10
    }

    private static class HierarchyItem {
        final String name;
        final String type;

        HierarchyItem(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    private static class TraversalState {
        final String nodeName;
        final int depth;
        final String path;

        TraversalState(String nodeName, int depth, String path) {
            this.nodeName = nodeName;
            this.depth = depth;
            this.path = path;
        }
    }

    /**
     * Returns a string describing the function.
     */
        @Override
        public String toString() {
            return "DYNAMIC_GROUP_HIERARCHY_TRAVERSAL";
        }

}

