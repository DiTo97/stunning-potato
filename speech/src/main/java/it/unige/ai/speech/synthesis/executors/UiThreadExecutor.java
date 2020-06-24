package it.unige.ai.speech.synthesis.executors;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

/*
 * Ui thread executor to execute runnable on UI thread
 */
public class UiThreadExecutor implements Executor {

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override public void execute(@NonNull Runnable command) {
        uiHandler.post(command);
    }

}
