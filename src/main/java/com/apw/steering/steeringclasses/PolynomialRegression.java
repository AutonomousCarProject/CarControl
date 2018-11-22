package com.apw.steering.steeringclasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PolynomialRegression {

    private ArrayList<Point> points;
    private ArrayList<BigDecimal> aValues;
    private ArrayList<Equation> equations;
    private long degree;

    public PolynomialRegression(ArrayList<Point> points, long degree) {


        this.points = points;
        aValues = new ArrayList<>();
        equations = new ArrayList<>();
        this.degree = degree;

        calculateRegression(points, degree);
    }

    private void calculateRegression(ArrayList<Point> points, long degree) {
        ArrayList<BigDecimal> values = calculateValues(points, degree);
        ArrayList<BigDecimal> results = calculateResults(points, degree);
        values.add(0, new BigDecimal(points.size()));

        ArrayList<Equation> originalEquations = assignEquations(values, results, degree);

        this.equations = regressPoints(originalEquations);

        this.aValues = calculateAValues(equations);

    }

    public String toString() {
        String results = "";
        for (int idx = aValues.size() - 1; idx >= 0; idx--) {
            results += "aValue: " + toScientificNotation(aValues.get(idx)) + " Pow: " + idx + "\n";
        }
        return results;
    }

    private String toScientificNotation(BigDecimal value) {
        long power = 0;
        while (value.abs().doubleValue() < 1) {
            power++;
            value = value.multiply(new BigDecimal(10));
        }
        while (value.abs().doubleValue() >= 10) {
            power--;
            value = new BigDecimal(value.doubleValue() / 10);
        }
        return value + " * 10^" + power;
    }

    private static ArrayList<Equation> regressPoints(ArrayList<Equation> equations) {

        ArrayList<Equation> finalEquations = new ArrayList<>();

        long column = equations.get(0).size() - equations.size();

        // Make the first Coefficient 1 in all equations
        for (Equation equation : equations) {
            equation.makeValue1(column);
        }

        // Subtract first equation from all other equations.
        for (int idx = 1; idx < equations.size(); idx++) {
            equations.get(idx).subtract(equations.get(0));
        }

        finalEquations.add(equations.get(0));
        equations.remove(0);
        if (equations.size() > 0) {
            finalEquations.addAll(regressPoints(equations));
        }

        return finalEquations;
    }


    private static ArrayList<BigDecimal> calculateAValues(ArrayList<Equation> equations) {
        ArrayList<BigDecimal> aValues = new ArrayList<>();

        for (int idx = 1; idx <= equations.size(); idx++) {
            Equation equation = equations.get(equations.size() - idx);
            ArrayList<BigDecimal> coefficients = equation.getCoefficients();
            BigDecimal aValue = equation.getResult();

            for (int numCoefficients = 0; numCoefficients < idx - 1; numCoefficients++) {
                aValue = aValue.subtract(coefficients.get(equation.size() - 1 - numCoefficients)
                        .multiply(aValues.get(numCoefficients)));
            }
            aValues.add(aValue);
        }
        return aValues;

        /*Equation equation0 = equations.get(equations.size() - 1);
        aValues.add(equation0.getResult());

        Equation equation1 = equations.get(equations.size() - 2);
        ArrayList<Double> coefficients1 = equation1.getCoefficients();
        aValues.add(equation1.getResult() - coefficients1.get(equation1.size() - 1) * aValues.get(0));

        Equation equation2 = equations.get(equations.size() - 3);
        ArrayList<Double> coefficients2 = equation2.getCoefficients();
        aValues.add(equation2.getResult() - coefficients2.get(equation2.size() - 2) * aValues.get(1) - coefficients2.get(equation2.size() - 1) * aValues.get(0));

        Equation equation3 = equations.get(equations.size() - 4);
        ArrayList<Double> coefficients3 = equation3.getCoefficients();
        aValues.add(equation3.getResult() - coefficients3.get(equation3.size() - 3) * aValues.get(2) - coefficients3.get(equation3.size() - 2) * aValues.get(1) - coefficients3.get(equation3.size() - 1) * aValues.get(0));
        //*/
    }


    private ArrayList<Equation> assignEquations(ArrayList<BigDecimal> values, ArrayList<BigDecimal> results, long degree) {
        ArrayList<Equation> equations = new ArrayList<>();
        for (int numEquations = 0; numEquations < degree + 1; numEquations++) {
            ArrayList<BigDecimal> valuesInEquation = new ArrayList<>();
            for (int numValues = 0; numValues < degree + 1; numValues++) {
                valuesInEquation.add(values.get(numEquations + numValues));
            }
            equations.add(new Equation(valuesInEquation, results.get(numEquations)));
        }
        return equations;
    }

    public BigDecimal getYValueAtX(long x) {
        BigDecimal y = new BigDecimal(0);
        for (int idx = 0; degree - idx >= 0; idx++) {
            y = y.add(aValues.get(idx).multiply(new BigDecimal(Math.pow(x, (int) degree - idx))));
        }
        return y;
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
}
