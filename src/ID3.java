import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class has static methods that implement the ID3 decision tree algorithm
 */
public class ID3 {

    /* Multiply this term by any log_10 calculations to convert it into a log_2 result. */
    private static final double LOG_2_RATE = 1/Math.log10(2);


    /**
     * Constructs a binary decision tree using the provided data set
     *
     * @param samples  a list of samples to learn the tree on
     * @return         a reference to the root node of the tree
     */
    public static DecisionTree buildTree(List<Sample> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Empty data set");
        }

        // Base case 1: All samples have same result
        int totalDiag = (int)samples.stream().filter(s->s.diagnosis).count();

        if (totalDiag == samples.size())
            return new DecisionTree(true);
        if (totalDiag == 0)
            return new DecisionTree(false);

        // Base case 2: All samples have the same data
        // No need to check--there are no duplicates.

        // We want to calculate the minimum conditional entropy for splitting

        double pd           = (double) totalDiag / samples.size();
        double currEntropy  = entropy_10(pd) * LOG_2_RATE;

        double minEntropy   = currEntropy;  // Minimum entropy (should be bounded above by 1)
        int    bestAttr     = -1;           // The attribute associated with minEntropy
        double bestSplit    = -1.0;         // Threshold value at which attr is split to produce minEntropy\

        for (int attr = 0; attr < 30; attr++) {
            // Sort the data so we can check thresholds in a linear scan
            sortByAttr(samples, attr);

            /***********************************
             *  GUIDE: The different kinds
             *         of thresholds
             *
             *  1) Simple (x < 1.2)
             *
             *                    |
             *  ******************|************
             *  Val.  | 1.1 | 1.1 | 1.2 | 1.2 |
             *  Diag. |  B  |  B  |  M  |  M  |
             *  ******************|************
             *                    |
             *
             *  2) Before mixed value (x < 1.2)
             *
             *              |     $
             *  ************|*****$************
             *  Val.  | 1.1 | 1.2 $ 1.2 | 1.3 |
             *  Diag. |  B  |  B  $  M  |  M  |
             *  ************|*****$************
             *              |     $
             *
             *  3) After mixed value (x < 1.3)
             *
             *                    $     |
             *  ************|*****$*****|******
             *  Val.  | 1.1 | 1.2 $ 1.2 | 1.3 |
             *  Diag. |  B  |  B  $  M  |  M  |
             *  ************|*****$*****|******
             *                    $     |
             *
             * There will be occasional overlap
             * where (1) and (2) are the same.
             * It's okay if we double-test it.
             ***********************************/

            // Keep track of the previous diagnosis to track threshold values
            boolean prevDiag = samples.get(0).diagnosis;

            // A running count of positive diagnoses encountered so far
            int diagCount = 0;

            // Track the previous attribute value
            double prevValue = samples.get(0).data[attr];

            // If the same value repeats, we need to keep track of how many (edge case)
            int valueStreak = 0;

            // Indicates whether the previous sample belonged to a mixed value
            boolean prevMixedValue = false;

            for (int i = 1; i < samples.size(); i++) {  // Note we skip index 0 b/c it can't be a threshold
                Sample sample = samples.get(i);

                // Check for repeated value
                boolean repeatValue = sample.data[attr] == prevValue;

                if (repeatValue)
                    valueStreak++;
                else
                    valueStreak = 0;

                // Check for threshold value
                if (sample.diagnosis != prevDiag || (!repeatValue && prevMixedValue)) {
                    if (repeatValue)
                        prevMixedValue = true;

                    // If we have a streak, we need to place the threshold before our streak began
                    // Hence the offset in total_1.

                    // Calculate the entropy
                    int total   = samples.size();       // # of samples in un-split set
                    int total_1 = i - valueStreak;      // # of samples left of the split
                    int total_2 = total - total_1;      // # of samples right of the split
                    int diag_1  = diagCount;            // # of pos. diagnoses left of split
                    int diag_2  = totalDiag - diag_1;   // # of pos. diagnoses right of split

                    // Note that diag_1 is not affected by repeated values as
                    // total_1 is, because we sorted to place negatives first.

                    double p_1  = (double) total_1 / total;     // Proportion of set 1 out of total set
                    double p_2  = (double) total_2 / total;     // Proportion of set 1 out of total set
                    double pd_1 = (double) diag_1 / total_1;    // Proportion of pos. diagnoses in set 1
                    double pd_2 = (double) diag_2 / total_2;    // Proportion of pos. diagnoses in set 2

                    // We find the entropy of each set in base 10 (since there's no base 2 method)
                    double entropy_1 = entropy_10(pd_1);
                    double entropy_2 = entropy_10(pd_2);

                    // The total conditional entropy is the expected entropy
                    double entropy = (p_1 * entropy_1) + (p_2 * entropy_2);

                    // Convert to base 2
                    entropy *= LOG_2_RATE;

//                    System.out.printf("i: %d, attr: %d, value: %.4f%n", i, attr, sample.data[attr]);
//                    System.out.printf("p_1: %.4f, p_2: %.4f, pd_1: %.4f, pd_2: %.4f%n", p_1, p_2, pd_1, pd_2);
//                    System.out.printf("H_1: %.4f, H_2: %.4f, H: %.4f%n", entropy_1, entropy_2, entropy);

                    if (entropy < minEntropy) {
                        minEntropy = entropy;
                        bestAttr = attr;
                        bestSplit = sample.data[attr];
                    }
                }

                // Update iteration information
                if (sample.diagnosis) {
                    diagCount++;
                }
                prevDiag = sample.diagnosis;
                prevValue = sample.data[attr];

                if (!repeatValue) {
                    prevMixedValue = false;
                }
            }
        }
//        System.out.printf("H_min: %.4f, attr: %d, split: %.4f%n", minEntropy, bestAttr, bestSplit);
//        System.out.printf("total: %d, left: %d, right %d%n", samples.size(), samples_1.size(), samples_2.size());

        // Now split the data to achieve the minimum entropy

        int splitAttr = bestAttr;
        double splitVal = bestSplit;

        Predicate<Sample> condition_1 = (s -> s.data[splitAttr] < splitVal);

        DecisionTree root = new DecisionTree(condition_1);

        List<Sample> samples_1 = samples.stream()
                .filter(condition_1)
                .collect(Collectors.toList());
        List<Sample> samples_2 = samples.stream()
                .filter(condition_1.negate())
                .collect(Collectors.toList());

        // If no minimum entropy is found below the current entropy, we stop splitting
        if (bestAttr < 0) {
            // Just return a node that predicts the majority
//            System.err.println("No split acceptable: " + pd);
            return new DecisionTree(pd >= 0.5);
        }

        // Recursively call build tree with the split data
        root.left = buildTree(samples_1);
        root.right = buildTree(samples_2);

        return root;
    }

    /**
     * Calculates the base-10 entropy of a random variable that takes one value
     * at probability p, and another value at probability (1-p).
     *
     * @param p probability (or proportion)
     * @return  the entropy in base-10. Multiply to LOG_2_RATE to convert to base 2
     */
    private static double entropy_10(double p) {
        if (p == 0 || p == 1)
            return 0;
        return -(p * Math.log10(p) + (1-p) * Math.log10(1-p));
    }

    /**
     * Sorts a list of samples by a given attribute.
     * In case of tie, places negative diagnoses first.
     *
     * @param samples   list of samples
     * @param attr      index of the attribute to use a sorting key
     */
    private static void sortByAttr(List<Sample> samples, int attr) {
        Comparator<Sample> attrComp = Comparator.comparingDouble(s -> s.data[attr]);
        samples.sort(attrComp.thenComparingInt(s -> s.diagnosis ? 1 : 0));
    }
}
