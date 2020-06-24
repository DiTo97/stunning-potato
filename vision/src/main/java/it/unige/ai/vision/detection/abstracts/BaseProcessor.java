package it.unige.ai.vision.detection.abstracts;

import android.graphics.Bitmap;
import android.media.Image;

import androidx.annotation.NonNull;

import it.unige.ai.vision.detection.interfaces.BaseProcessorInterface;
import it.unige.ai.vision.detection.metadata.FrameMetadata;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to do with
 * the detection results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> Type of the detected feature.
 */
public abstract class BaseProcessor<T> implements BaseProcessorInterface {

    // Thread-safe flag to control whether calls to `process()` should be ignored or not. This may happen when
    // the input data is ingested faster than what the model can handle.
    private final AtomicBoolean shouldFire = new AtomicBoolean(false);

    public BaseProcessor() {

    }

    @Override
    public void process(ByteBuffer data, final FrameMetadata frameMetadata,
                        final GraphicOverlay graphicOverlay) {
        if (shouldFire.get()) {
            return;
        }

        FirebaseVisionImageMetadata firebaseMetadata = new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, firebaseMetadata),
                frameMetadata, graphicOverlay);
    }

    @Override
    public void process(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        if (shouldFire.get()) {
            return;
        }

        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap),
                null, graphicOverlay);
    }

    @Override
    public void process(Image image, int rotation, final GraphicOverlay graphicOverlay) {
        if (shouldFire.get()) {
            return;
        }

        // Overlay usage display
        FrameMetadata frameMetadata = new FrameMetadata.Builder()
                .setWidth(image.getWidth())
                .setHeight(image.getHeight())
                .build();

        detectInVisionImage(FirebaseVisionImage.fromMediaImage(image, rotation),
                frameMetadata, graphicOverlay);
    }

    @Override
    public void stop() {
        // Do nothing here.
    }

    private void detectInVisionImage(FirebaseVisionImage image, final FrameMetadata frameMetadata,
                                     final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<T>() {
                            @Override
                            public void onSuccess(T results) {
                                shouldFire.set(false);
                                BaseProcessor.this.onSuccess(results, frameMetadata,
                                        graphicOverlay);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            shouldFire.set(false);
                            BaseProcessor.this.onFailure(e);
                        });

        // Begin throttling (i.e., enqueuing frames) until the current input frame has been processed,
        // either within `onSuccess` or `onFailure`.
        shouldFire.set(true);
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    protected abstract void onSuccess(@NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);

}
