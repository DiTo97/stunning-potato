package it.unige.ai.vision.detection.interfaces;

import android.graphics.Bitmap;
import android.media.Image;

import it.unige.ai.vision.detection.metadata.FrameMetadata;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.firebase.ml.common.FirebaseMLException;

import java.nio.ByteBuffer;

/**
 * Interface to process images with different ML Kit detectors or custom ML models.
 */
public interface BaseProcessorInterface {

    /**
     * Process images with the underlying ML models.
     */
    void process(ByteBuffer data, FrameMetadata frameMetadata,
                 GraphicOverlay graphicOverlay) throws FirebaseMLException;

    /**
     * Process bitmap (BMP) images.
     */
    void process(Bitmap bitmap, GraphicOverlay graphicOverlay);

    /**
     * Process images in different formats (PNG, JPG, etc...), also taking into
     * account the rotation of the camera source.
     */
    void process(Image image, int rotation, GraphicOverlay graphicOverlay);

    /**
     * Stop the underlying ML model and release resources.
     */
    void stop();

}
