package com.apw.steering.steeringclasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PolynomialEquation {

    private ArrayList<BigDecimal> coefficients;
    private BigDecimal result;
    private long degree;

    public PolynomialEquation(ArrayList<BigDecimal> coefficients, BigDecimal result) {
        this.coefficients = coefficients;
        this.result = result;
    }

    public PolynomialEquation(ArrayList<BigDecimal> coefficients) {
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

    public void subtract(PolynomialEquation polynomialEquation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx).subtract(polynomialEquation.getCoefficients().get(coefficientsIdx)));
        }
        result = result.subtract(polynomialEquation.getResult());
    }

    public void setDegree(long degree) {
        this.degree = degree;
    }

    public long getDegree() {
        return degree;
    }

    public BigDecimal getYValueAtX(long x) {
        BigDecimal y = new BigDecimal(0);
        for (int idx = 0; this.degree - idx >= 0; idx++) {
            y = y.add(getCoefficients().get(idx).multiply(new BigDecimal(Math.pow(x, (int) this.degree - idx))));
        }
        return y;
    }

    public void add(PolynomialEquation polynomialEquation) {
        for (int coefficientsIdx = 0; coefficientsIdx < this.coefficients.size(); coefficientsIdx++) {
            this.coefficients.set(coefficientsIdx,
                    this.coefficients.get(coefficientsIdx).add(polynomialEquation.getCoefficients().get(coefficientsIdx)));
        }
        result = result.add(polynomialEquation.getResult());
    }

}
