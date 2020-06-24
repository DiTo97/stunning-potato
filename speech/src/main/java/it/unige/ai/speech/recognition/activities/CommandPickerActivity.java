package it.unige.ai.speech.recognition.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import it.unige.ai.speech.R;
import it.unige.ai.speech.recognition.interfaces.OnCommandListener;
import it.unige.ai.speech.recognition.recognizers.CommandRecognizer;
import it.unige.ai.speech.recognition.utils.CommandUtils;

import static it.unige.ai.base.utils.HierarchyUtils.findViewsByTag;
import static it.unige.ai.base.utils.PermissionUtils.allPermissionsGranted;
import static it.unige.ai.base.utils.PermissionUtils.getRuntimePermissions;
import static it.unige.ai.base.utils.PermissionUtils.showDeniedSnackbar;
import static it.unige.ai.base.utils.PermissionUtils.showNeverAskAgainSnackbar;

public class CommandPickerActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private final String TAG = CommandPickerActivity.class.getSimpleName();

    /* Keyword we are looking for to activate menu */
    private final String ACTIVATION_search = "wakeup";
    private final String ACTIVATION_keyphrase = "ok potato";

    private final int STATE_inSetup = 0;
    private final int STATE_setupFailed = 1;
    private final int STATE_setupOk = 2;
    private final int STATE_ttsEngine = 3;

    private final int RC = 2;
    private final int RC_ttsInstalled = 3;

    private HashMap<String, Button> mCmdButtons;

    private Group mGroupSetup;
    private Group mGroupFrames;

    private CommandRecognizer mCommandRecognizer; // Google's recognizer used to detect commands
                                                  // after the activation through "Ok potato" *
    private SpeechRecognizer mSphinxRecognizer;

    private TextToSpeech mTts;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_picker);

        mGroupSetup  = findViewById(R.id.group_setup);
        mGroupFrames = findViewById(R.id.group_frames);

        if (!allPermissionsGranted(this)) {
            getRuntimePermissions(this, RC);
        } else {
            checkTTSEngine();
        }
    }

    private void checkTTSEngine() {
        // Wait for Google's TTS setup
        setState(STATE_ttsEngine);

        // An intent is an abstract description of an operation to be performed.
        Intent checkTTSIntent = new Intent();

        // Activity action: Starts the activity from the platform TTS engine to verify
        // the proper installation and availability of the resource files on the system.
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        // This code creates a new Intent purely for the purposes of checking the user data whose
        // completion will call the "onActivityResult" method.
        startActivityForResult(checkTTSIntent, RC_ttsInstalled);
    }

    private void initViews(CommandPickerActivity activity) {
        mCmdButtons = new HashMap<>();

        ArrayList<View> cmdButtons = findViewsByTag(activity.findViewById(
                android.R.id.content), getString(R.string.tag_btn_command));

        for (View btn : cmdButtons) {
            setOnClickBehaviour((Button) btn);
        }
    }

    private void setOnClickBehaviour(Button btn) {
        // Get ConstraintLayout container
        ViewGroup container = (ViewGroup) btn.getParent();

        // Get command as text of the TextView sibling
        String cmd = ((TextView) container.getChildAt(1)).getText()
                .toString().trim().toLowerCase();

        mCmdButtons.put(cmd, btn); // Store the Button with its corresponding command
                                   // to later retrieve with OnCommandListener

        Class<?> c = CommandUtils.getCommandsNav().get(cmd);

        if (c == null) { // Command not implemented yet
            btn.setOnClickListener((v) -> {
                if (mCommandRecognizer != null) {
                    mCommandRecognizer.cancel();
                }

                speakAndShowCmdSnackbar(v.getRootView().findViewById(android.R.id.content),
                        StringUtils.capitalize(cmd) + " is not available yet");
            });
        } else { // Command available
            btn.setOnClickListener((v) -> this.startActivity(
                    new Intent(this, c)));
        }
    }

    private void speakAndShowCmdSnackbar(View v, String text) {
        speakCmd(text);
        showCmdSnackbar(v, text);
    }

    private void speakCmd(String text) {
        Bundle dataMap = new Bundle();

        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0f);    // Goes from -1 to 1
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f); // Goes from 0 to 1

        dataMap.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM,
                AudioManager.STREAM_VOICE_CALL); // Stream type to be used while speaking

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, dataMap,
                UUID.randomUUID().toString());
    }

    private void showCmdSnackbar(View v, String text) {
        mSnackbar = Snackbar.make(v, text, Snackbar.LENGTH_INDEFINITE)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBar, int event) {
                        super.onDismissed(transientBar, event);
                        startListeningSphinx();
                    }});
        mSnackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mTts != null
                && mTts.isSpeaking()) {
            mTts.stop();
        }

        if (mCommandRecognizer != null) {
            mCommandRecognizer.cancel();
        }

        stopSphinx();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!allPermissionsGranted(this)) {
            getRuntimePermissions(this, RC);
        } else {
            startListeningSphinx();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.shutdown();
        }

        if (mCommandRecognizer != null) {
            mCommandRecognizer.destroy();
        }

        destroySphinx();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permissions request done!");
        if(permissions.length == 0)
            return;

        if (requestCode == RC) {
            // TODO: Check permission grant for the whole array
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) { // Permission granted
                checkTTSEngine();
            } else if (!shouldShowRequestPermissionRationale(permissions[0])) { // `Never ask again` option selected
                try {
                    showNeverAskAgainSnackbar(this, permissions[0],
                            "capture sound from mic and enable Sphinx recognition's functionality");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {  // Permission denied
                try {
                    showDeniedSnackbar(this, permissions[0]);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setState(int state) {
        TextView tvSetup = findViewById(R.id.tv_setup);

        switch (state) {
            case STATE_inSetup:
                tvSetup.setText(R.string.setup_rec_in_setup);

                mGroupSetup.setVisibility(View.VISIBLE);
                mGroupFrames.setVisibility(View.GONE);
                break;

            case STATE_ttsEngine:
                tvSetup.setText(R.string.setup_tts_engine);

                mGroupSetup.setVisibility(View.VISIBLE);
                mGroupFrames.setVisibility(View.GONE);
                break;

            case STATE_setupOk:
                mGroupSetup.setVisibility(View.GONE);
                mGroupFrames.setVisibility(View.VISIBLE);
                break;

            default: // STATE_setupFailed
                tvSetup.setText(R.string.setup_rec_failed);

                mGroupSetup.setVisibility(View.VISIBLE);
                mGroupFrames.setVisibility(View.GONE);

                findViewById(R.id.pb)
                        .setVisibility(View.INVISIBLE);
                break;
        }

    }

    // Sphinx recognizer can be configured to perform multiple searches
    // of different kind and switch between them
    private void setupRecognizer(File assetsDir) throws IOException {
        mSphinxRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();

        mSphinxRecognizer.addListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onPartialResult(Hypothesis hypothesis) {
                if (hypothesis == null)
                    return;

                // Get prediction
                String hypPhrase = hypothesis.getHypstr();

                if (hypPhrase.equals(ACTIVATION_keyphrase)) {
                    Log.i(TAG, "Keyphrase recognized: " + hypPhrase);

                    Snackbar.make(findViewById(android.R.id.content), "Keyphrase recognized: "
                            + StringUtils.capitalize(hypPhrase), Snackbar.LENGTH_SHORT).show();

                    stopSphinx();
                    mCommandRecognizer.startListening();
                }
            }

            @Override
            public void onResult(Hypothesis hypothesis) {
                Log.d(TAG, "Hypothesis: " + hypothesis);
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onTimeout() {

            }
        });

        // Create keyword-activation search
        mSphinxRecognizer.addKeyphraseSearch(ACTIVATION_search,
                ACTIVATION_keyphrase);
    }

    /*
     * Sphinx recognizer handlers
     */
    private void startListeningSphinx() {
        if (mSphinxRecognizer != null) {
            mSphinxRecognizer.startListening(ACTIVATION_search);
        }
    }

    private void stopSphinx() {
        if (mSphinxRecognizer != null) {
            mSphinxRecognizer.stop();
        }
    }

    private void destroySphinx() {
        if (mSphinxRecognizer != null) {
            mSphinxRecognizer.cancel();
            mSphinxRecognizer.shutdown();
        }
    }

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
    private void initComponents() {
        mTts = new TextToSpeech(this, this,
                "com.google.android.tts"); // Use Google TTS engine

        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    if (mSnackbar != null
                            && mSnackbar.isShown())
                        mSnackbar.dismiss();
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> {
                    if (mSnackbar != null
                            && mSnackbar.isShown())
                        mSnackbar.dismiss();
                });
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> {
                    if (mSnackbar != null
                            && mSnackbar.isShown())
                        mSnackbar.dismiss();
                });
            }});

        // Configure the TTS engine
        speechEngineConfigurations();

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }

    /*
     * Setup asynchronous task
     */
    private class SetupTask extends AsyncTask<Void, Void, Boolean> {

        WeakReference<CommandPickerActivity> activityRef;

        SetupTask(CommandPickerActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CommandPickerActivity activity = activityRef.get();

            if (activity != null) {
                activity.setState(STATE_inSetup);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                CommandPickerActivity activity = activityRef.get();

                if (activity != null) {
                    Assets assets = new Assets(activityRef.get());
                    File assetsDir = assets.syncAssets();

                    activity.setupRecognizer(assetsDir);
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }

            // Assets are perfectly synced
            return true;
        }

        @Override
        protected void onPostExecute(Boolean synced) {
            CommandPickerActivity activity = activityRef.get();

            // Check if activity has been
            // collected by the GC
            if (activity != null) {
                if (synced) {
                    initViews(activity); // Init only if setup successful

                    activity.mCommandRecognizer = new CommandRecognizer(activity,
                            new OnCommandListener() {
                        @Override
                        public void onTimeout() {
                            if (activity.mCommandRecognizer != null) {
                                activity.mCommandRecognizer.cancel();
                            }

                            activity.startListeningSphinx();
                        }

                        @Override
                        public void onNotRecognized() {
                            if (activity.mCommandRecognizer != null) {
                                activity.mCommandRecognizer.cancel();
                            }

                            speakAndShowCmdSnackbar(activity.findViewById(android.R.id.content),
                                    "Sorry, I did not understand!");
                        }

                        @Override
                        public void onSupported(String cmd) {
                            activity.mCmdButtons.get(cmd).performClick();
                        }
                    });

                    activity.startListeningSphinx();
                    activity.setState(STATE_setupOk);
                } else {
                    activity.setState(STATE_setupFailed);
                }
            }
        }

    }

}