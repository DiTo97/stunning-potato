package it.unige.ai.speech.synthesis.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.squareup.picasso.Picasso;

import java.util.UUID;

import it.unige.ai.speech.R;
import it.unige.ai.speech.synthesis.metadata.ImageMetadata;

/**
 * The system calls this to get the DialogFragment's layout, regardless
 * of whether it's being displayed as a dialog or an embedded fragment.
 */
public class CaptioningDialogFragment extends DialogFragment {

    private static final String TAG = CaptioningDialogFragment.class.getSimpleName();

    private boolean mFirstListen;

    private String mDescription;
    private String mUrl;

    private ImageMetadata mImageMetadata;
    private TextToSpeech mTts;

    private Activity mActivity;
    private TextView tvDescription;

    public CaptioningDialogFragment(ImageMetadata imageMetadata, TextToSpeech tts) {
        mImageMetadata = imageMetadata;
        mFirstListen = true;
        mTts = tts;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        super.onCreateView(inflater, container, savedInstanceState);

        View dialogView = inflater.inflate(R.layout.card_img_gallery_dialog,
                container, false);

        mDescription = mImageMetadata.getDescription();
        mUrl = mImageMetadata.getUrl();

        ImageView ivDialog = dialogView.findViewById(R.id.iv_dialog);
        Picasso.get().load(mUrl).fit().centerCrop()
                .into(ivDialog);

        tvDescription = dialogView.findViewById(
                R.id.tv_description);
        tvDescription.setText(mDescription);

        return dialogView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Do stuff only if attached
        if (getActivity() != null) {
            mActivity = getActivity();

            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    mActivity.runOnUiThread(() -> {
                        if (mFirstListen) {
                            mFirstListen = false;
                            mActivity.invalidateOptionsMenu();
                        }

                        // No-highlights description reset
                        tvDescription.setText(mDescription);
                    });
                }

                @Override
                public void onStop(String utteranceId, boolean interrupted) {
                    mActivity.runOnUiThread(() -> {
                        if (mFirstListen) {
                            mFirstListen = false;
                            mActivity.invalidateOptionsMenu();
                        }

                        // No-highlights description reset
                        tvDescription.setText(mDescription);
                    });
                }

                @Override
                public void onError(String utteranceId) {
                    mActivity.onBackPressed();
                }

                @Override
                public void onRangeStart(String utteranceId, final int start,
                                         final int end, int frame) {
                    Log.i(TAG, "onRangeStart > utteranceId: " + utteranceId + ", start: " + start
                            + ", end: " + end + ", frame: " + frame);

                    // onRangeStart (and all UtteranceProgressListener callbacks) do not run on main thread
                    // ... so we explicitly manipulate views on the main thread:
                    mActivity.runOnUiThread(() -> {
                        Spannable textWithHighlights = new SpannableString(mDescription);

                        textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLACK),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        textWithHighlights.setSpan(new BackgroundColorSpan(Color.YELLOW),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                        tvDescription.setText(textWithHighlights);
                    });
                }
            });

            readDescription();
        }
    }

    private void readDescription() {
        Bundle dataMap = new Bundle();

        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0f);    // Goes from -1 to 1
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f); // Goes from 0 to 1

        dataMap.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM,
                AudioManager.STREAM_VOICE_CALL); // Stream type to be used while speaking

        mTts.speak(mDescription, TextToSpeech.QUEUE_FLUSH, dataMap,
                UUID.randomUUID().toString());
    }

    @Override
    public void onStop() {
        if (mTts != null
                && mTts.isSpeaking()) {
            mTts.stop();
        }

        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.dialog_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_repeat:
                readDescription();
                return true;

            case R.id.ic_close:
                mActivity.onBackPressed();
                return true;

            default:
                break;
        }

        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.ic_repeat).setEnabled(!mFirstListen);
        menu.findItem(R.id.ic_repeat).setVisible(!mFirstListen);

        super.onPrepareOptionsMenu(menu);
    }

    /**
     * The system calls this only when creating the layout in a dialog. Override it to build
     * your own custom Dialog container, except when showing an AlertDialog instead of a generic Dialog.
     *
     * When doing so, onCreateView(LayoutInflater, ViewGroup, Bundle) does not need to be implemented
     * since AlertDialog takes care of its own content.
     */
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it.
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        return dialog;
    }

}
