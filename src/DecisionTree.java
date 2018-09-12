import java.util.function.Predicate;

/**
 * This class represents a node in a binary decision tree
 */
class DecisionTree {

    /* The left and right children, respectively */
    DecisionTree left;
    DecisionTree right;

    /**
     * This will be set as True or False on a leaf node, to indicate the diagnosis
     * If null, this is not a leaf node.
     */
    Boolean result;

    /**
     * A predicate that tests a given sample. If true, follow the left branch.
     * Otherwise, follow the right branch.
     */
    Predicate<Sample> leftCondition;

    /**
     * Use this constructor for a non-leaf node
     */
    DecisionTree(Predicate<Sample> condition) {
        leftCondition = condition;
    }

    /**
     * Use this constructor for a leaf node
     */
    DecisionTree(boolean result) {
        this.result = result;
    }

    /**
     * Counts the total number of nodes in a tree
     *
     * @param tree  pointer to the root of a tree
     * @return      number of nodes in total
     */
    public static int nodeCount(DecisionTree tree) {
        if (tree == null)
            return 0;
        return 1 + nodeCount(tree.left) + nodeCount(tree.right);
    }

    /**
     * Finds the depth of a tree
     *
     * @param tree  pointer to the root of a tree
     * @return      the maximum distance from the root to a leaf node
     */
    public static int depth(DecisionTree tree) {
        if (tree == null)
            return -1;
        return 1 + Math.max(depth(tree.left), depth(tree.right));
    }



}
