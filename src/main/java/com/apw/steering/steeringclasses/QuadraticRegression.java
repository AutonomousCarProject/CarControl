package com.apw.steering.steeringclasses;

import java.util.ArrayList;

public class QuadraticRegression {

    private ArrayList<Point> points;
    private ArrayList<Double> aValues;
    private ArrayList<Equation> equations;

    public QuadraticRegression(ArrayList<Point> points) {
        this.points = points;
        aValues = new ArrayList<>();
        equations = new ArrayList<>();
        regressPoints(points, 2);
    }

    private void regressPoints(ArrayList<Point> points, int degree) {
        ArrayList<Integer> values = calculateValues(points, degree);
        ArrayList<Integer> results = calculateResults(points, degree);
        values.add(0, points.size());

        // Adds all points to equations
        assignEquations(values, results, degree);

        Equation A = equations.get(0);
        Equation B = equations.get(1);
        Equation C = equations.get(2);

        // Make the first Coefficient 1 in all equations
        for (Equation equation : equations) {
            equation.makeValue1(0);
        }

        // Subtract first equation from all other equations.
        for (int idx = 1; idx < degree + 1; idx++) {
            equations.get(idx).subtract(equations.get(0));
        }

        //
        equations.get(1).scale(equations.get(2).getCoefficients().get(1) / equations.get(1).getCoefficients().get(1));

        equations.get(2).subtract(equations.get(1));

        equations.get(1).scale(1 / equations.get(1).getCoefficients().get(1));
        equations.get(2).scale(1 / equations.get(2).getCoefficients().get(2));

        double ar = A.getResult();
        double ab = A.getCoefficients().get(1);
        double ac = A.getCoefficients().get(2);
        double br = B.getResult();
        double bc = B.getCoefficients().get(2);
        double cr = C.getResult();

        aValues.add(cr);
        aValues.add(br - (bc * cr));
        aValues.add(ar - (ab * (br - (bc * cr))) - (ac * cr));
    }


    private void assignEquations(ArrayList<Integer> values, ArrayList<Integer> results, int degree) {
        for (int numEquations = 0; numEquations < degree + 1; numEquations++) {
            ArrayList<Double> valuesInEquation = new ArrayList<>();
            for (int numValues = 0; numValues < degree + 1; numValues++) {
                valuesInEquation.add((double) values.get(numEquations + numValues));
            }
            equations.add(new Equation(valuesInEquation, results.get(numEquations)));
        }
    }

    private static ArrayList<Integer> calculateValues(ArrayList<Point> points, int degree) {
        ArrayList<Integer> values = new ArrayList<>();
        for (int numValues = 1; numValues <= degree * 2; numValues++) {
            values.add(addXValues(points, numValues));
        }
        return values;
    }

    private static ArrayList<Integer> calculateResults(ArrayList<Point> points, int degree) {
        ArrayList<Integer> results = new ArrayList<>();
        for (int numResults = 0; numResults <= degree; numResults++) {
            results.add(addXYValues(points, numResults));
        }
        return results;
    }

    private static int addXValues(ArrayList<Point> points, int degree) {
        int sum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum += Math.pow(point.getX(), degree);
            }
        }
        return sum;
    }

    private static int addXYValues(ArrayList<Point> points, int degree) {
        int sum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                sum += point.getY() * Math.pow(point.getX(), degree);
            }
        }
        return sum;
    }
}
