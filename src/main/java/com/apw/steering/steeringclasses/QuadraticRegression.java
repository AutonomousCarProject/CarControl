package com.apw.steering.steeringclasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class QuadraticRegression {

    private ArrayList<Point> points;
    private ArrayList<Double> aValues;
    private ArrayList<Equation> equations;
    private long degree;

    public QuadraticRegression(ArrayList<Point> points, long degree) {


        this.points = points;
        aValues = new ArrayList<>();
        equations = new ArrayList<>();
        this.degree = degree;

        calculateRegression(points, degree);
    }

    private void calculateRegression(ArrayList<Point> points, long degree) {
        ArrayList<Long> values = calculateValues(points, degree);
        ArrayList<Long> results = calculateResults(points, degree);
        values.add(0, (long) points.size());

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

    private String toScientificNotation(double value) {
        long power = 0;
        while (Math.abs(value) < 1) {
            power++;
            value = value * 10;
        }
        while (Math.abs(value) >= 10) {
            power--;
            value = value / 10;
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


    private static ArrayList<Double> calculateAValues(ArrayList<Equation> equations) {
        ArrayList<Double> aValues = new ArrayList<>();

        for (int idx = 1; idx <= equations.size(); idx++) {
            Equation equation = equations.get(equations.size() - idx);
            ArrayList<Double> coefficients = equation.getCoefficients();
            Double aValue = equation.getResult();

            for (int numCoefficients = 0; numCoefficients < idx - 1; numCoefficients++) {
                aValue -= coefficients.get(equation.size() - 1 - numCoefficients) * aValues.get(numCoefficients);
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


    private ArrayList<Equation> assignEquations(ArrayList<Long> values, ArrayList<Long> results, long degree) {
        ArrayList<Equation> equations = new ArrayList<>();
        for (int numEquations = 0; numEquations < degree + 1; numEquations++) {
            ArrayList<Double> valuesInEquation = new ArrayList<>();
            for (int numValues = 0; numValues < degree + 1; numValues++) {
                valuesInEquation.add((double) values.get(numEquations + numValues));
            }
            equations.add(new Equation(valuesInEquation, results.get(numEquations)));
        }
        return equations;
    }

    public long getYValueAtX(long x) {
        long y = 0;
        for (int idx = 0; degree - idx >= 0; idx++) {
            y += aValues.get(idx) * Math.pow(x, degree - idx);
        }
        return y;
    }

    private static ArrayList<Long> calculateValues(ArrayList<Point> points, long degree) {
        ArrayList<Long> values = new ArrayList<>();
        for (int numValues = 1; numValues <= degree * 2; numValues++) {
            values.add(addXValues(points, numValues));
        }
        return values;
    }

    private static ArrayList<Long> calculateResults(ArrayList<Point> points, long degree) {
        ArrayList<Long> results = new ArrayList<>();
        for (int numResults = 0; numResults <= degree; numResults++) {
            results.add(addXYValues(points, numResults));
        }
        return results;
    }

    private static long addXValues(ArrayList<Point> points, long degree) {
        long sum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum += Math.pow(point.getX(), degree);
            }
        }
        return sum;
    }

    private static long addXYValues(ArrayList<Point> points, long degree) {
        long sum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum += point.getY() * Math.pow(point.getX(), degree);
            }
        }
        return sum;
    }
}
