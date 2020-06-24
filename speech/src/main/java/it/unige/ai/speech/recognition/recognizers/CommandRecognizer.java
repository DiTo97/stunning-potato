package it.unige.ai.speech.recognition.recognizers;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import it.unige.ai.speech.recognition.interfaces.OnCommandListener;
import it.unige.ai.speech.recognition.listeners.CommandListener;

public class CommandRecognizer {

    private final String TAG = CommandRecognizer.class.getSimpleName();

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;

    public CommandRecognizer(Context from, OnCommandListener timeoutListener) {
        // Instantiate recognizer to capture commands once CMU Sphinx's continuous recognizer
        // has detected an incoming command for listening
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(from);

        // Configure recognizer
        speechRecognizer.setRecognitionListener(
                new CommandListener(
                        TAG, timeoutListener
                )
        );

        // Instantiate intent to launch the recognition service in background
        // with the desired parameters (i.e., language, model, etc...)
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Configure intent
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // Amount of silence before stopping
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "it.unige.ai.speech.recognition.recognizer");
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
    }

    public void startListening() {
        speechRecognizer.startListening(speechIntent);
    }

    public void cancel() {
        speechRecognizer.cancel();
    }

    public void destroy() {
        speechRecognizer.destroy();
    }

}
