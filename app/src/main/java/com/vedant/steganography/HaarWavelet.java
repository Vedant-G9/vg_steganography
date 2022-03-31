package com.vedant.steganography;

public class HaarWavelet extends Wavelet{

    private double h0;
    private double h1;
    private double g0;
    private double g1;

    private double Ih0;
    private double Ih1;
    private double Ig0;
    private double Ig1;

    public HaarWavelet(){
        super(WaveletType.HaarWavelet);

        h0 = h1 = g0 = 0.5;
        g1 = -0.5;

        Ih0 = Ih1 = Ig0 = 1.0;
        Ig1 = -1.0;
    }

    @Override
    public void forwardTransform(double[] buffer, int n)
    {
        h0 = h1 = g0 = 0.5;
        g1 = -0.5;

        int i, j;
        int half = n >> 1;

        double tmp[] = new double[n];

        i = 0;
        for (j = 0; j < n; j = j + 2) {
            tmp[i]      = buffer[j]*h0 + buffer[j+1]*h1;
            tmp[i+half] = buffer[j]*g0 + buffer[j+1]*g1;
            i++;
        }

        for (i = 0; i < n; i++) {
            buffer[i] = tmp[i];
        }
    }

    @Override
    public void inverseTransform(double[] buffer, int n)
    {
        Ih0 = Ih1 = Ig0 = 1.0;
        Ig1 = -1.0;

        int j = 0;
        int half = n >> 1;

        double tmp[] = new double[n];

        for (int i = 0; i < half-1; i++) {
            tmp[j++] = buffer[i]*Ih0 + buffer[i+half]*Ih1;
            tmp[j++] = buffer[i]*Ig0 + buffer[i+half]*Ig1;
        }

        for (int i = 0; i < n; i++) {
            buffer[i] = tmp[i];
        }

    }
}
