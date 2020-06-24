package it.unige.ai.vision.detection.models.faceDetection;

import android.util.Log;

import androidx.annotation.NonNull;

import it.unige.ai.vision.detection.abstracts.BaseProcessor;
import it.unige.ai.vision.detection.metadata.FrameMetadata;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

/**
 * Face detection processor.
 */
public class FaceDetectionProcessor extends BaseProcessor<List<FirebaseVisionFace>> {

    private static final String TAG = FaceDetectionProcessor.class.getCanonicalName();

    private final FirebaseVisionFaceDetector faceDetector;

    /*
     * Firebase detection options may vary greatly depending on the task:
     *
     *      Static images detection:
     *          - All contours
     *          - All classifications
     *          - Accuracy mode
     *
     *      Real-time stream detection:
     *          - All landmarks
     *          - All classifications
     *          - Tracking enabled
     *          - Fast mode
     */
    public FaceDetectionProcessor() { // Real-time setup for now
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .enableTracking()
                .build();

        faceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            faceDetector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close "
                    + "Firebase's face detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return faceDetector.detectInImage(image);
    }

    @Override
    protected void onSuccess(@NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();

        for (FirebaseVisionFace face : faces) {
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
            graphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face, frameMetadata.getCameraFacing());
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed: " + e);
    }

}
