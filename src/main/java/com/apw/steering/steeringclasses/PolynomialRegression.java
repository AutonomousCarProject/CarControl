package com.apw.steering.steeringclasses;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * This class is an equation that is created by approximating a line though a given data set.
 * Given any amount of points, this class will return a polynomial of any degree that best
 * approximates the data.
 *
 * @see PolynomialEquation
 * @author kevin
 */
public class PolynomialRegression extends PolynomialEquation {

    /**
     * Constructor that calculates the coefficients of the
     * polynomial, then sets this.degree equal to degree value passed in.
     *
     * @param points List of points that will be regressed.
     * @param degree Degree of polynomial to regress the data through.
     */
    public PolynomialRegression(ArrayList<Point> points, long degree) {
        super(calculateRegression(removeEmptyPoints(points), degree));
        setDegree(degree);
    }

    /**
     * Regress a polynomial to the degree-th power.
     *
     * @param points List of points that will be regressed.
     * @param degree Degree of the polynomial.
     * @return The Coefficients for the polynomial.
     */
    private static ArrayList<BigDecimal> calculateRegression(ArrayList<Point> points, long degree) {
        // Calculate the values of left Matrix
        ArrayList<BigDecimal> values = calculateValues(points, degree);
        // Calculate the results of Right Matrix
        ArrayList<BigDecimal> results = calculateResults(points, degree);
        values.add(0, new BigDecimal(points.size()));

        // Return the coefficients of the polynomial
        return calculateCoefficients(formatEquations(assignEquations(values, results, degree)));
    }

    /**
     * Creates a string containing each coefficient in scientific notation.
     * @return a string containing each coefficient in scientific notation.
     */
    public String toString() {
        String results = "";
        // Loop through each coefficient
        for (int idx = getCoefficients().size() - 1; idx >= 0; idx--) {
            // Format the string.
            results = String.format("%s%15e * x ^ %d\n", results, getCoefficients().get(idx), idx);
        }
        return results;
    }

    /**
     * This method takes in a list of equations, where the size of the list is equal to
     * the amount of coefficients in each equation, and returns a formatted list of
     * equations. The format has a diagonal series of 1's, and 0's underneath the 1's.
     *
     *  1    c1  ...   c2  c3
     *
     *  0    1   ...   c4  c5
     *
     *  ...  ... ...   c6  c7
     *
     *  0    0    0   ...  c9
     *
     *  0    0    0    0   1
     *
     * @param equations List of equations, who's length is equal to the number of coefficients in each equation.
     * @return Formatted List of equations.
     */
    private static ArrayList<PolynomialEquation> formatEquations(ArrayList<PolynomialEquation> equations) {

        ArrayList<PolynomialEquation> finalEquations = new ArrayList<>();

        long column = equations.get(0).size() - equations.size();

        // Make the first Coefficient 1 in all equations
        for (PolynomialEquation polynomialEquation : equations) {
            polynomialEquation.makeValue1(column);
        }

        // Subtract first equation from all other equations.
        for (int idx = 1; idx < equations.size(); idx++) {
            equations.get(idx).subtract(equations.get(0));
        }

        // Add first equation to final equations, then remove it from equations.
        finalEquations.add(equations.get(0));
        equations.remove(0);

        // If there is at least one equation left in equations, recursively call formatEquations.
        if (equations.size() > 0) {
            finalEquations.addAll(formatEquations(equations));
        }

        return finalEquations;
    }

    /**
     * This method calculates all of the coefficents to best approximate a line
     * through all of the points given.
     *
     * @param equations List of formatted equations.
     * @return List of aValues for the polynomial.
     */
    private static ArrayList<BigDecimal> calculateCoefficients(ArrayList<PolynomialEquation> equations) {
        ArrayList<BigDecimal> aValues = new ArrayList<>();

        // Iterate equations.size() times.
        for (int idx = 1; idx <= equations.size(); idx++) {
            PolynomialEquation polynomialEquation = equations.get(equations.size() - idx);
            ArrayList<BigDecimal> coefficients = polynomialEquation.getCoefficients();
            BigDecimal aValue = polynomialEquation.getResult();

            // Iterate over each non-1 and non-0 coefficient in polynomialEquation.
            for (int numCoefficients = 0; numCoefficients < idx - 1; numCoefficients++) {
                aValue = aValue.subtract(coefficients.get(polynomialEquation.size() - 1 - numCoefficients)
                        .multiply(aValues.get(numCoefficients)));
            }
            aValues.add(aValue);
        }
        return aValues;
    }


    private static ArrayList<PolynomialEquation> assignEquations(ArrayList<BigDecimal> values, ArrayList<BigDecimal> results, long degree) {
        ArrayList<PolynomialEquation> equations = new ArrayList<>();

        for (int numEquations = 0; numEquations < degree + 1; numEquations++) {
            ArrayList<BigDecimal> valuesInEquation = new ArrayList<>();

            for (int numValues = 0; numValues < degree + 1; numValues++) {
                valuesInEquation.add(values.get(numEquations + numValues));
            }
            equations.add(new PolynomialEquation(valuesInEquation, results.get(numEquations)));
        }
        return equations;
    }


    private static ArrayList<BigDecimal> calculateValues(ArrayList<Point> points, long degree) {
        ArrayList<BigDecimal> values = new ArrayList<>();
        for (int numValues = 1; numValues <= degree * 2; numValues++) {
            values.add(addXValues(points, numValues));
        }
        return values;
    }

    private static ArrayList<BigDecimal> calculateResults(ArrayList<Point> points, long degree) {
        ArrayList<BigDecimal> results = new ArrayList<>();
        for (int numResults = 0; numResults <= degree; numResults++) {
            results.add(addXYValues(points, numResults));
        }
        return results;
    }

    private static BigDecimal addXValues(ArrayList<Point> points, long degree) {
        BigDecimal sum = new BigDecimal(0);
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum = sum.add(new BigDecimal(Math.pow(point.getX(), degree)));
            }
        }
        return sum;
    }

    private static BigDecimal addXYValues(ArrayList<Point> points, long degree) {
        BigDecimal sum = new BigDecimal(0);
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum = sum.add(new BigDecimal(point.getY() * Math.pow(point.getX(), degree)));
            }
        }
        return sum;
    }

    private static ArrayList<Point> removeEmptyPoints(ArrayList<Point> list) {
        ArrayList<Point> nonEmptyPoints = new ArrayList<>(list);
        for (int idx = 0; idx < list.size(); idx++) {
            if (nonEmptyPoints.get(idx).isEmpty()) {
                nonEmptyPoints.remove(idx);
            }
        }
        return nonEmptyPoints;
    }

}
