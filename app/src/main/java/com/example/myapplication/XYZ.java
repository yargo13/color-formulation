package com.example.myapplication;

public class XYZ {
    protected double X, Y, Z;

    protected int illuminant;

    public XYZ( double X, double Y, double Z){
        new XYZ(X, Y, Z, ColorTransformation.ILLUMINANT_D65_10_DEGREES);
    }

    public XYZ( double X, double Y, double Z, int illuminant) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.illuminant = illuminant;
    }

    public double getX() { return X; }

    public double getY() {
        return Y;
    }

    public double getZ() {
        return Z;
    }

    public int getIlluminant() { return illuminant; }
}
