package it.unige.ai.speech.recognition.utils;

import android.os.Bundle;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

import static it.unige.ai.base.utils.MathUtils.argMax;

public class RecognitionUtils {

    private RecognitionUtils() {

    }

    public static String filterData(Bundle results) {
        ArrayList<String> data = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);

        if (data != null) {
            if (data.size() > 1) { // When there is more than one prediction we have a look
                                   // at the associated confidence array and keep the most fidel
                float[] confidence = results.getFloatArray(
                        SpeechRecognizer.CONFIDENCE_SCORES);

                if (confidence != null) {
                    return data.get(argMax(confidence));
                }
            }
            return data.get(0);
        }
        return null;
    }

}
