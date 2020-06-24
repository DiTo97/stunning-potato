package it.unige.ai.base.utils;

public class MathUtils {

    private MathUtils() {

    }

    public static int argMax(float[] x){
        float max  = x[0];
        int argMax = 0;

        for (int i = 1; i < x.length; i++) {
            if (x[i] > max) {
                max    = x[i];
                argMax = i;
            }
        }
        return argMax;
    }

}
