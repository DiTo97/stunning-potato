package it.unige.ai.speech.synthesis.executors;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Background thread executor to execute runnable on background thread
 */
public class BackgroundExecutor implements Executor {

    private ExecutorService executorService =
            Executors.newFixedThreadPool(2);

    @Override public void execute(@NonNull Runnable command) {
        executorService.execute(command);
    }

}
