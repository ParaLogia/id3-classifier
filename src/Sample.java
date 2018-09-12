import java.util.Arrays;

/**
 * This class represents an individual sample of breast tissue
 */
public class Sample {
    /**
     * True if malignant, false if benign
     */
    boolean diagnosis;

    /**
     * An array of attributes for this Sample instance:
     *
     * a) radius (mean of distances from center to points on the perimeter)
     * b) texture (standard deviation of gray-scale values)
     * c) perimeter
     * d) area
     * e) smoothness (local variation in radius lengths)
     * f) compactness (perimeter^2 / area - 1.0)
     * g) concavity (severity of concave portions of the contour)
     * h) concave points (number of concave portions of the contour)
     * i) symmetry
     * j) fractal dimension ("coastline approximation" - 1)
     *
     * Indices 0-9 contain the means of these attributes (0:a, 1:b, 2:c, etc.)
     * Indices 10-19 contain the standard errors (10:a, 11:b, etc.)
     * Indices 20-29 contain the largest (20:a, 21:b, etc.)
     */
    final double[] data = new double[30];

    @Override
    public String toString() {
        return "Diagnosis: " + diagnosis + ", " + Arrays.toString(data);
    }
}
