package it.unige.ai.speech.recognition.listeners;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.util.Log;

import androidx.annotation.RequiresApi;

import it.unige.ai.speech.recognition.interfaces.OnCommandListener;
import it.unige.ai.speech.recognition.utils.CommandUtils;

import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
import static it.unige.ai.speech.recognition.utils.RecognitionUtils.filterData;

public class CommandListener implements RecognitionListener  {

    // Debugging variables
    private final String TAG;

    private final OnCommandListener mCommandListener;

    public CommandListener(String TAG, OnCommandListener commandListener) {
        this.TAG = TAG;
        this.mCommandListener = commandListener;
    }

    public void onReadyForSpeech(Bundle params) {

    }

    public void onBeginningOfSpeech() {

    }

    public void onRmsChanged(float rmsdB) {

    }

    public void onBufferReceived(byte[] buffer) {

    }

    public void onEndOfSpeech() {

    }

    public void onError(int error) {
        if (error == ERROR_SPEECH_TIMEOUT) {
            Log.d(TAG, "onError > Timeout reached");

            // Recognizer went on timeout
            mCommandListener.onTimeout();
        } else {
            mCommandListener.onNotRecognized();
        }
    }

    @RequiresApi(api = 24)
    public void onResults(Bundle results) {
        String cmd = filterData(results);
        parseCommand(cmd);
    }

    @RequiresApi(api = 24)
    private void parseCommand(String cmd) {
        if (CommandUtils.getCommands().contains(cmd)) { // Command supported
            mCommandListener.onSupported(cmd);
        } else { // Command not recognized
            mCommandListener.onNotRecognized();
        }
    }

    public void onPartialResults(Bundle partialResults) {

    }

    public void onEvent(int eventType, Bundle params) {

    }

}
