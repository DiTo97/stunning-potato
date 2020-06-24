package it.unige.ai.speech.recognition.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import it.unige.ai.speech.R;

import static it.unige.ai.speech.recognition.utils.RecognitionUtils.filterData;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechIntent;

    private TextView tvSynthesis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);

        // Instantiate Google's recognizer
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeechRecognizer.setRecognitionListener(
                new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {

                    }

                    @Override
                    public void onBeginningOfSpeech() {

                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {

                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {

                    }

                    @Override
                    public void onEndOfSpeech() {

                    }

                    @Override
                    public void onError(int error) {
                        mSpeechRecognizer.cancel();
                    }

                    @RequiresApi(api = 24)
                    @Override
                    public void onResults(Bundle results) {
                        String phrase = filterData(results);
                        appendToScreen(phrase);
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        // Google's guidelines have changed to push people towards usage of their
                        // paid cloud speech API. Hence, they do not allow continuous recognition
                        // within the offline API but rather send everything to onResults.
                    }

                    private void appendToScreen(String data) {
                        tvSynthesis = findViewById(R.id.tv_synthesis);
                        tvSynthesis.setText(data);
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {

                    }
                }
        );

        // Instantiate intent to launch the recognition service in background
        // with the desired parameters (i.e., language, model, etc...)
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Configure intent
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // Amount of silence before stopping
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "it.unige.ai.speech.recognition.recognizer");
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        Button belButton = findViewById(R.id.btn_speak);
        tvSynthesis = findViewById(R.id.tv_synthesis);

        belButton.setOnClickListener((v) -> {
            mSpeechRecognizer.startListening(mSpeechIntent);
            tvSynthesis.setText("");
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

}
