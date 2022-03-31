package com.vedant.steganography;

import static java.lang.Math.sqrt;

public class DaubechiesD4Wavelet extends Wavelet {

    private double h0, h1, h2, h3, g0, g1, g2, g3;

    private double Ih0, Ih1, Ih2, Ih3, Ig0, Ig1, Ig2, Ig3;

    public DaubechiesD4Wavelet() {
        super(WaveletType.DaubechiesD4Wavelet);
        double commonDenominator = 4 * sqrt(2);

        h0 = (1 + sqrt(3)) / commonDenominator;
        h1 = (3 + sqrt(3)) / commonDenominator;
        h2 = (3 - sqrt(3)) / commonDenominator;
        h3 = (1 - sqrt(3)) / commonDenominator;

        g0 = h3;
        g1 = -h2;
        g2 = h1;
        g3 = -h0;

        Ih0 = h2;
        Ih1 = g2;
        Ih2 = h0;
        Ih3 = g0;

        Ig0 = h3;
        Ig1 = g3;
        Ig2 = h1;
        Ig3 = g1;
    }

    @Override
    public void forwardTransform(double buffer[], int n){
        if (n >= 4) {
            int i, j;
            int half = n >> 1;

            double tmp[] = new double[n];

            i = 0;
            for (j = 0; j < n - 2; j = j + 2) {
                tmp[i] = buffer[j] * h0 + buffer[j + 1] * h1 + buffer[j + 2] * h2 + buffer[j + 3] * h3;
                tmp[i + half] = buffer[j] * g0 + buffer[j + 1] * g1 + buffer[j + 2] * g2 + buffer[j + 3] * g3;
                i++;
            }

            tmp[i] = buffer[n - 2] * h0 + buffer[n - 1] * h1 + buffer[0] * h2 + buffer[1] * h3;
            tmp[i + half] = buffer[n - 2] * g0 + buffer[n - 1] * g1 + buffer[0] * g2 + buffer[1] * g3;

            for (i = 0; i < n; i++) {
                buffer[i] = tmp[i];
            }
        }
    }

    @Override
    public void inverseTransform(double buffer[], int n){
        if (n >= 4) {
            int i, j;
            int half = n >> 1;
            int halfPls1 = half + 1;

            double tmp[] = new double[n];

            tmp[0] = buffer[half - 1] * Ih0 + buffer[n - 1] * Ih1 + buffer[0] * Ih2 + buffer[half] * Ih3;
            tmp[1] = buffer[half - 1] * Ig0 + buffer[n - 1] * Ig1 + buffer[0] * Ig2 + buffer[half] * Ig3;

            j = 2;
            for (i = 0; i < half - 1; i++) {
                tmp[j++] = buffer[i] * Ih0 + buffer[i + half] * Ih1 + buffer[i + 1] * Ih2 + buffer[i + halfPls1] * Ih3;
                tmp[j++] = buffer[i] * Ig0 + buffer[i + half] * Ig1 + buffer[i + 1] * Ig2 + buffer[i + halfPls1] * Ig3;
            }
            for (i = 0; i < n; i++) {
                buffer[i] = tmp[i];
            }
        }
    }

}
