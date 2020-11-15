package com.example.myapplication;

import java.util.HashMap;

/**
 * This class implements conversions between RGB, CIE XYZ and CIELAB color
 * spaces.
 */
public class ColorTransformation {

    static int ILLUMINANT_D65_10_DEGREES = 1;
    static int ILLUMINANT_A_10_DEGREES = 2;

    static HashMap<Integer,double[]> whitePointReference = new HashMap<>();
    static {
        double[] d65_10degrees_WhiteReference = {0.948242, 1, 1.073955};
        double[] a_10degrees_WhiteReference = {1.111493, 1, 0.35207};
        whitePointReference.put(ILLUMINANT_D65_10_DEGREES, d65_10degrees_WhiteReference);
        whitePointReference.put(ILLUMINANT_A_10_DEGREES, a_10degrees_WhiteReference);
    }

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


    static HashMap<Integer,double[]> spectralDensity = new HashMap<>();
    static {
        double[] d65_10degrees_SpectralDensity = {
                82.78, 91.51, 93.45, 86.70,
                104.88, 117.03, 117.83, 114.87,
                115.94, 108.82, 109.36, 107.81,
                104.79, 107.69, 104.41, 104.05,
                100.00, 96.33, 95.79, 88.68, 90.00,
                89.59, 87.69, 83.28, 83.69, 80.02,
                80.21, 82.27, 78.27, 69.71,	71.60
        };
        double[] a_10degrees_SpectralDensity = {
                14.72, 17.69, 21.01, 24.68,
                28.71, 33.10, 37.82, 42.88,
                48.25, 53.92, 59.87, 66.07,
                72.50, 79.14, 85.95, 92.91,
                100.00, 107.18, 114.43, 121.72,
                129.03, 136.33, 143.60, 150.81,
                157.95, 164.99, 171.92, 178.72,
                185.38, 191.88, 198.20
        };
        whitePointReference.put(ILLUMINANT_D65_10_DEGREES, d65_10degrees_SpectralDensity);
        whitePointReference.put(ILLUMINANT_A_10_DEGREES, a_10degrees_SpectralDensity);
    }

    static HashMap<Integer, Double> spectralReference = new HashMap<>();
    static {
        spectralReference.put(ILLUMINANT_D65_10_DEGREES, 1161.9469);
        spectralReference.put(ILLUMINANT_A_10_DEGREES, 1137.80110);
    }

    /**
     * Computes XYZ coordinates for a spectrum.
     * @param data an array of 31 spectrum data points.
     */
    static XYZ spectrumToXYZ(double data[], int illuminant) {

        double X = 0.f;
        double Y = 0.f;
        double Z = 0.f;
        double N = spectralReference.get(illuminant);
        double[] spectralDensityIlluminant = spectralDensity.get(illuminant);

        // integrate
        for( int wl = 0; wl < 31; wl++) {
            X += data[wl] * Px[wl] * spectralDensityIlluminant[wl];
            Y += data[wl] * Py[wl] * spectralDensityIlluminant[wl];
            Z += data[wl] * Pz[wl] * spectralDensityIlluminant[wl];
        }
        X /= N;
        Y /= N;
        Z /= N;

        return new XYZ(X, Y, Z);
    }

    static LAB spectrumToLAB(double data[], int illuminant) {
        return XYZtoLAB(spectrumToXYZ(data, illuminant));
    }

    /**
     * Converts RGB to XYZ data using the default illuminant (D65)
     */
    static XYZ RGBtoXYZ(int r_int, int g_int, int b_int) {
        double r = r_int/255.0;
        double g = g_int/255.0;
        double b = b_int/255.0;

        /* Linearização do sRGB */
        if (r>0.04045) r = Math.pow(((r+0.055)/1.055),2.4);
        else r = r/12.92;
        if (g>0.04045) g = Math.pow(((g+0.055)/1.055),2.4);
        else g = g/12.92;
        if (b>0.04045) b = Math.pow(((b+0.055)/1.055),2.4);
        else b = b/12.92;

        double X = (r*0.4124 + g*0.3576 + b*0.1805);
        double Y = (r*0.2126 + g*0.7152 + b*0.0722);
        double Z = (r*0.0193 + g*0.1192 + b*0.9505);

        // Currently only converts to the default D65 illuminant
        return new XYZ(X, Y, Z, ILLUMINANT_D65_10_DEGREES);
    }


    static LAB RGBtoLAB(int r_int, int g_int, int b_int) {
        return XYZtoLAB(RGBtoXYZ(r_int, g_int, b_int));
    }

    static double f(double x){
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
    static LAB XYZtoLAB(XYZ xyz) {
        double fx, fy, fz;

        int illuminant = xyz.getIlluminant();
        double[] reference = whitePointReference.get(illuminant);
        fx = f(xyz.getX()/reference[0]);
        fy = f(xyz.getY()/reference[1]);
        fz = f(xyz.getZ()/reference[2]);

        double L = 116*fy - 16;
        double a = 500*(fx-fy);
        double b = 200*(fy-fz);

        return new LAB(L, a, b, illuminant);

    }
}