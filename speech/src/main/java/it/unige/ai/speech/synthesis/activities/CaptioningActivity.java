package it.unige.ai.speech.synthesis.activities;

import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.HashSet;
import java.util.Locale;

import it.unige.ai.speech.R;
import it.unige.ai.speech.synthesis.adapters.CaptioningAdapter;
import it.unige.ai.speech.synthesis.viewModels.CaptioningViewModel;

/*
 * We use the same activity to implement the interface OnInitListener, in order to pass it both as a context and
 * a listener to the method that is in charge of creating the TextToSpeech instance.
 */
public class CaptioningActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private static final String TAG = CaptioningActivity.class.getName();
    private final int RC_ttsInstalled = 0;

    private SwipeRefreshLayout containerImgsGallery;

    private RecyclerView rvImgsGallery;
    private CaptioningAdapter galleryAdapter;

    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        rvImgsGallery = findViewById(R.id.rv_imgs_gallery);
        rvImgsGallery.setHasFixedSize(true);

        // Layout manager for a given RecyclerView object
        // (A RecyclerView handles a collection of items on screen)
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvImgsGallery.setLayoutManager(layoutManager);

        checkTTSEngine();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (containerImgsGallery != null) {
            containerImgsGallery.setRefreshing(false);
        }

        if (mTts != null) {
            mTts.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTts != null) {
            mTts.shutdown();
        }
    }

    private void checkTTSEngine() {
        // An intent is an abstract description of an operation to be performed.
        Intent checkTTSIntent = new Intent();

        // Activity action: Starts the activity from the platform TTS engine to verify
        // the proper installation and availability of the resource files on the system.
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        // This code creates a new Intent purely for the purposes of checking the user data whose
        // completion will call the "onActivityResult" method.
        startActivityForResult(checkTTSIntent, RC_ttsInstalled);
    }

    /*
     onInit method: Called to signal the completion of the TTS engine initialization.
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int isLanguageAvailable = mTts.isLanguageAvailable(Locale.US);
            if (isLanguageAvailable != TextToSpeech.LANG_MISSING_DATA
                    && isLanguageAvailable != TextToSpeech.LANG_NOT_SUPPORTED) {
                mTts.setLanguage(Locale.US);
                mTts.setVoice(new Voice("en-us-x-sfg#female_2-local", Locale.US, Voice.QUALITY_VERY_HIGH,
                        Voice.LATENCY_NORMAL, false, new HashSet<>()));
            } else {
                isLanguageAvailable = mTts.isLanguageAvailable(Locale.UK);
                if (isLanguageAvailable != TextToSpeech.LANG_MISSING_DATA
                        && isLanguageAvailable != TextToSpeech.LANG_NOT_SUPPORTED) {
                    mTts.setLanguage(Locale.UK);
                    mTts.setVoice(new Voice("en-gb-x-sfg#female_2-local", Locale.UK, Voice.QUALITY_VERY_HIGH,
                            Voice.LATENCY_NORMAL, false, new HashSet<>()));
                } else {
                    Toast.makeText(this, "Failed to set the main language for the TTS engine!"
                            + " Using default locale instead...", Toast.LENGTH_SHORT).show();

                    mTts.setLanguage(Locale.getDefault());
                    mTts.setVoice(new Voice("Default voice", Locale.getDefault(), Voice.QUALITY_VERY_HIGH,
                            Voice.LATENCY_NORMAL, false, new HashSet<>()));
                }
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! TTS engine failed...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = 26)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
         * When the data checking Intent completes, the app calls this method, passing it the "RC_ttsInstalled" variable
         * indicating whether or not the user has the TTS data installed. If the data is present, the code goes ahead
         * and creates an instance of the TTS class, otherwise the app will prompt the user to install it.
         */
        if (requestCode == RC_ttsInstalled) {
            /*
             * Engine.CHECK_VOICE_DATA_PASS:
             * Indicates success when checking the installation status of the resources used by the
             * TTS engine with the ACTION_CHECK_TTS_DATA intent.
             */
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Language pack available - create the TTS
                initComponents();
            } else {
                // No language pack - install English language
                Toast.makeText(this, "No TTS engine available!"
                        + " Please, install it from the Play Store...", Toast.LENGTH_LONG).show();

                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @RequiresApi(api = 26)
    private void speechEngineConfigurations() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        mTts.setAudioAttributes(audioAttributes); // Used to modify some characteristics of speech

        // Set default rates
        mTts.setPitch(1f);
        mTts.setSpeechRate(0.9f);
    }

    @RequiresApi(api = 26)
    private void initComponents() {
        mTts = new TextToSpeech(this, this,
                "com.google.android.tts"); // Use Google TTS engine

        // Configure the TTS engine
        speechEngineConfigurations();

        CaptioningViewModel viewModel = new ViewModelProvider(this)
                .get(CaptioningViewModel.class).initContext(this);

        viewModel.initImagesMetadata();

        // Configure the adapter
        galleryAdapter = new CaptioningAdapter(mTts);
        rvImgsGallery.setAdapter(galleryAdapter);

        // Notify the adapter of changes
        viewModel.imagesMetadata.observe(this,
                galleryAdapter::submitList);

        containerImgsGallery = findViewById(R.id.container_imgs_gallery);
        containerImgsGallery.setOnRefreshListener(() -> {
            rvImgsGallery.setVisibility(View.INVISIBLE);
            viewModel.refresh();

            // TODO: Stop refreshing after LiveData notifies
            new Handler().postDelayed(() -> {
                containerImgsGallery.setRefreshing(false);

                rvImgsGallery.getLayoutManager().smoothScrollToPosition(rvImgsGallery,
                        null, 0);
                rvImgsGallery.setVisibility(View.VISIBLE);
            }, 2000);
        });
    }

}
