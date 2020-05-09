package com.example.myapplication;

public class LAB {
    protected double L, A, B;

    /**
     * Flag set to true if the coordinates are valid.
     */
    protected boolean valid;			//


    public LAB( double L, double A, double B) {
        this.L = L;
        this.A = A;
        this.B = B;
        valid = true;
    }

    public LAB( ) {
    }

    public double getL() {
        return L;
    }

    public double getA() {
        return A;
    }

    public double getB() {
        return B;
    }

    /**
     * Returns true if the coordinates are valid.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the RGB coordinates to specified values. The values
     * must be 0 to 255.
     */
    public void setLAB( double L, double A, double B ) {
        this.L = L;
        this.A = A;
        this.B = B;
        this.valid = true;		// of course, it's true
    }

}