package it.unige.ai.speech.synthesis.listeners;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import it.unige.ai.speech.synthesis.dialogs.CaptioningDialogFragment;
import it.unige.ai.speech.synthesis.metadata.ImageMetadata;

/*
 * When the listener is fired an utterance progress listener is associated to the TTS object,
 * in order to better monitor the flow of the speech audio.
 */
public class CaptioningViewListener implements View.OnClickListener {

    private final String TAG = CaptioningDialogFragment.class.getSimpleName();

    private ImageMetadata mImageMetadata;
    private TextToSpeech mTts;
    private FragmentManager mFragmentManager;

    public CaptioningViewListener(ImageMetadata imageMetadata, TextToSpeech tts,
                                  Context callingActivity) {
        this.mImageMetadata = imageMetadata;
        this.mTts = tts;
        this.mFragmentManager = ((AppCompatActivity) callingActivity)
                .getSupportFragmentManager();
    }

    private void createDialog() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        // Specify a polish transition animation
        fragmentTransaction.setTransition(FragmentTransaction
                .TRANSIT_FRAGMENT_OPEN);

        Fragment oldInstance = mFragmentManager.findFragmentByTag(TAG);

        if (oldInstance != null) {
            fragmentTransaction.remove(oldInstance);
        }

        CaptioningDialogFragment captioningDialogFragment =
                new CaptioningDialogFragment(mImageMetadata, mTts);

        // To make it fullscreen use the 'content' root view as the container,
        // which is always the root view for the activity
        fragmentTransaction.add(android.R.id.content, captioningDialogFragment,
                TAG).addToBackStack(null).commit();
    }

    @Override
    public void onClick(View v) {
        createDialog();
    }

}
