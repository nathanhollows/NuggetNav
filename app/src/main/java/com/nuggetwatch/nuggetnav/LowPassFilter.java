package com.nuggetwatch.nuggetnav;

public class LowPassFilter {

    private static final float ALPHA = 0.1f;

    private LowPassFilter() {}

    // https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
    public static float[] filter(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }
}
