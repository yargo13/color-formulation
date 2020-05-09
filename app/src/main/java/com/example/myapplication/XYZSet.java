package com.example.myapplication;

/**
 * This class implements conversions between RGB, CIE XYZ and CIELAB color
 * spaces.
 */
public class XYZSet {

    double L, aa, bb;	// current CIE Lab coords
    LAB cor;
    double X, Y, Z;		// CIE XYZ coords
    int r, g, b;		// RGB coords
    /**
     * White Point Reference
     */
    static double Xr = 0.9673;
    static double Yr = 1;
    static double Zr = 0.814073;

    /**
     * x color matching function tabulated at 10-nm intervals.
     * (10-degree 1964 CIE suppl. std. observer)
     * de 400 a 700 de 10 em 10 = 31
     */
    static double Px[] = {
            0.01911d, 0.08474d, 0.20449d, 0.31468d,
            0.38373d, 0.37070d, 0.30227d, 0.19562d,
            0.08051d, 0.01617d, 0.00382d, 0.03747d,
            0.11775d, 0.23649d, 0.37677d, 0.52983d,
            0.70522d, 0.87866d, 1.01416d, 1.11852d,
            1.12399d, 1.03048d, 0.85630d, 0.64747d,
            0.43157d, 0.26833d, 0.15257d, 0.08126d,
            0.04085d, 0.01994d, 0.00958d
    };

    /**
     * y color matching function tabulated at 10-nm intervals.
     * (10-degree 1964 CIE suppl. std. observer)
     */
    static double Py[] = {
            0.00200d, 0.00876d, 0.02139d, 0.03868d,
            0.06208d, 0.08946d, 0.12820d, 0.18519d,
            0.25359d, 0.33913d, 0.46078d, 0.60674d,
            0.76176d, 0.87521d, 0.96199d, 0.99176d,
            0.99734d, 0.95555d, 0.86893d, 0.77741d,
            0.65834d, 0.52796d, 0.39806d, 0.28349d,
            0.17983d, 0.10763d, 0.06028d, 0.03180d,
            0.01591d, 0.00775d, 0.00372d
    };

    /**
     * z color matching function tabulated at 10-nm intervals.
     * (10-degree 1964 CIE suppl. std. observer)
     */
    static double Pz[] = {
            0.08601d, 0.38937d, 0.97254d, 1.55348d,
            1.96728d, 1.99480d, 1.74537d, 1.31756d,
            0.77213d, 0.41525d, 0.21850d, 0.11204d,
            0.06071d, 0.03045d, 0.01368d, 0.00399d,
            0.00000d, 0.00000d, 0.00000d, 0.00000d,
            0.00000d, 0.00000d, 0.00000d, 0.00000d,
            0.00000d, 0.00000d, 0.00000d, 0.00000d,
            0.00000d, 0.00000d, 0.00000d
    };

    /**
     * Espectral density for the D50 illuminant
     */
    static double D50[] = {
            49.25, 56.45, 59.97, 57.76,
            74.77, 87.19, 90.56, 91.32,
            95.07, 91.93, 95.70, 96.59,
            97.11, 102.09, 100.75, 102.31,
            100.00, 97.74, 98.92, 93.51,
            97.71, 99.29, 99.07, 95.75,
            98.90, 95.71, 98.24, 103.06,
            99.19, 87.43, 91.66
    };

    /**
     * Constructs a converter with no arguments.
     */
    public XYZSet() {
        cor = new LAB();
    }


    /**
     * Computes XYZ coordinates for a spectrum.
     * @param data an array of 31 spectrum data points.
     */
    public void spectrumToXYZ( double data[] ) {

        X = 0.f;
        Y = 0.f;
        Z = 0.f;
        // Reference for D50 10º
        double N = 1140.0685;

        // integrate
        for( int wl = 0; wl < 31; wl++) {
            X += data[wl] * Px[wl] * D50 [wl];
            Y += data[wl] * Py[wl] * D50 [wl];
            Z += data[wl] * Pz[wl] * D50 [wl];
        }
        X /= N;
        Y /= N;
        Z /= N;
    }



    /**
     * Converts from LAB to XYZ. Uses internal L-value.
     * @param a the a-value
     * @param b the b-value
     */
    public void LABtoXYZ( double a, double b) {

        double frac = (L + 16) / 116;

        if( L < 7.9996f ) {
            Y = L / 903.3f;
            X = a / 3893.5f + Y;
            Z = Y - b / 1557.4f;
        }
        else {
            double tmp;
            tmp = frac + a / 500;
            X = tmp * tmp * tmp;
            Y = frac * frac * frac;
            tmp = frac - b / 200;
            Z = tmp * tmp * tmp;
        }
    }

    public LAB getLAB(){
        return cor;
    }

    /*
    *
     */
    public double f(double x){
        if(x > 0.008856){
            return Math.pow(x, 0.333333);
        }
        else {
            return 7.787*x + 0.1379;
        }
    }

    /**
     * Converts from XYZ to LAB internally.
     */
    public void XYZtoLAB() {
        double fx, fy, fz;

        fx = f(X/Xr);
        fy = f(Y/Yr);
        fz = f(Z/Zr);

        L = 116*fy - 16;

        aa = 500*(fx-fy);

        bb = 200*(fy-fz);

        cor.setLAB(L,aa,bb);
    }
}