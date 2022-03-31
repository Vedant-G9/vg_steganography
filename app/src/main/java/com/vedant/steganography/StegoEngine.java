package com.vedant.steganography;

public class StegoEngine {

    private int coefficientDivisionFactor = 10000;

    private void changeWaveletCoefficientsToStegoAscii(double[] signal, double[] asciiStegoArray) {

        for (int i = 0; i < asciiStegoArray.length; i++) {
            signal[i + 1] = asciiStegoArray[i];
        }
    }

    private void extractStegoAsciiFromWaveletCoefficients(double[] signal, double[] asciiStegoArray) {

        for (int i = 0; i < asciiStegoArray.length; i++) {
            asciiStegoArray[i] = signal[i + 1];
        }
    }

    private void translateDoubleArrayToCharAsciiArray(double[] doubleArray, char[] charAsciiArray) {

        for (int i = 0; i < charAsciiArray.length; i++) {
            charAsciiArray[i] = (char) ((int) Math.round((doubleArray[i] * coefficientDivisionFactor)));
        }
    }

    private String extractTextFromDecomposedSignal(double[] signal) {

        int lengthOfMessage = (int) (Math.round(signal[0] * coefficientDivisionFactor));
        double[] asciiDoubleArray = new double[lengthOfMessage];
        char[] asciiCharArray = new char[lengthOfMessage];

        extractStegoAsciiFromWaveletCoefficients(signal, asciiDoubleArray);

        translateDoubleArrayToCharAsciiArray(asciiDoubleArray, asciiCharArray);

        String decryptedText = new String(asciiCharArray);

        return decryptedText;
    }

    public void embedStegoMessageInSignal(double[] signal, int levelsOfDecomposition, String stegoText, Wavelet wavelet) {

        char[] charArray = stegoText.toCharArray();
        double[] asciiStegoArray = new double[charArray.length];
        int length = signal.length;
        double decompFactor = Math.pow(2, (levelsOfDecomposition - 1));

        for (int i = 0; i < charArray.length; i++) {
            asciiStegoArray[i] = (double) (((int) charArray[i])) / coefficientDivisionFactor;
        }

        for (int j = 0; j < levelsOfDecomposition; j++) {
            wavelet.forwardTransform(signal, length);
            length /= 2;
        }

        length = (signal.length / (int) decompFactor);

        signal[0] = (double) (charArray.length) / coefficientDivisionFactor;

        changeWaveletCoefficientsToStegoAscii(signal, asciiStegoArray);

        for (int j = 0; j < levelsOfDecomposition; j++) {
            wavelet.inverseTransform(signal, length);
            length *= 2;
        }
    }

    public String extractStegoMessageFromSignal(double[] signal, int levelsOfDecomposition, Wavelet wavelet) {

        String hiddenText;

        int length = signal.length;

        for (int j = 0; j < levelsOfDecomposition; j++) {
            wavelet.forwardTransform(signal, length);
            length /= 2;
        }

        hiddenText = extractTextFromDecomposedSignal(signal);

        double decompFactor = Math.pow(2, (levelsOfDecomposition - 1));
        length = (signal.length / (int) decompFactor);

        for (int j = 0; j < levelsOfDecomposition; j++) {
            wavelet.inverseTransform(signal, length);
            length *= 2;
        }

        return hiddenText;
    }
}
