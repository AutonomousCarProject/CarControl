package com.apw.steering.steeringclasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Equation {

    private ArrayList<BigDecimal> coefficients;
    private BigDecimal result;

    public Equation(ArrayList<BigDecimal> coefficients, BigDecimal result) {
        this.coefficients = coefficients;
        this.result = result;
    }

    public Equation(ArrayList<BigDecimal> coefficients) {
        this.coefficients = coefficients;
        result = new BigDecimal(-1);
    }

    public void makeValue1(long valueNum) {
        BigDecimal scalar = new BigDecimal(1 / coefficients.get((int) valueNum).doubleValue());
        scale(scalar);
    }

    public void scale(BigDecimal scalar) {
        for (int idx = 0; idx < coefficients.size(); idx++) {
            coefficients.set(idx, coefficients.get(idx).multiply(scalar));
        }
        result = result.multiply(scalar);
    }

    public int size() {
        return coefficients.size();
    }

    public ArrayList<BigDecimal> getCoefficients() {
        return coefficients;
    }

    public BigDecimal getResult() {
        return result;
    }

    public void subtract(Equation equation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx).subtract(equation.getCoefficients().get(coefficientsIdx)));
        }
        result = result.subtract(equation.getResult());
    }

    public void add(Equation equation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx).add(equation.getCoefficients().get(coefficientsIdx)));
        }
        result = result.add(equation.getResult());
    }

}
