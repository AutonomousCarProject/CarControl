package com.apw.steering.steeringclasses;

import java.util.ArrayList;

public class Equation {

    private ArrayList<Double> coefficients;
    private double result;

    public Equation(ArrayList<Double> coefficients, double result) {
        this.coefficients = coefficients;
        this.result = result;
    }

    public void makeValue1(long valueNum) {
        double scalar = 1 / coefficients.get((int) valueNum);
        scale(scalar);
    }

    public void scale(double scalar) {
        for (int idx = 0; idx < coefficients.size(); idx++) {
            coefficients.set(idx, coefficients.get(idx) * scalar);
        }
        result = result * scalar;
    }

    public int size() {
        return coefficients.size();
    }

    public ArrayList<Double> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(ArrayList<Double> coefficients) {
        this.coefficients = coefficients;
    }

    public double getResult() {
        return result;
    }

    public void combineEquations(Equation equation, long offset) {
        for (int idx = (int) offset; idx < equation.size() + offset; idx++) {
            coefficients.set(idx, equation.getCoefficients().get(idx - ((int) offset)));
        }
    }

    public void subtract(Equation equation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx) - equation.getCoefficients().get(coefficientsIdx));
        }
        result -= equation.getResult();
    }

    public void add(Equation equation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx) + equation.getCoefficients().get(coefficientsIdx));
        }
        result += equation.getResult();
    }
}
