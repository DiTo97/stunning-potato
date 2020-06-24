package it.unige.ai.vision.detection.models.faceDetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import it.unige.ai.vision.detection.vision.lib.CameraSource;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

/**
 * Graphic instance to render face position (i.e., box), orientation, and landmarks within
 * an associated graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_positionRadius = 10.0f;
    private static final float FACE_boxStrokeWidth = 5.0f;

    private static final float ID_textSize = 35.0f;
    private static final float ID_offsetY = 50.0f;
    private static final float ID_offsetX = -50.0f;

    // Just one color because at every frame, as the graphic is recreated,
    // it would blink through colors over and over
    private static final int[] graphicColors = {
            Color.BLUE // Color.RED, Color.GREEN
    };

    private static int currentColorIdx = 0;

    private int cameraFacing;

    // TODO: Add more landmarks
    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;

    public FaceGraphic(GraphicOverlay graphicOverlay) {
        super(graphicOverlay);

        currentColorIdx = (currentColorIdx + 1) % graphicColors.length;
        final int selectedColor = graphicColors[currentColorIdx];

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_textSize);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(FACE_boxStrokeWidth);
    }

    /**
     * Updates the face instance from the detection of the most recent frame and invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateFace(FirebaseVisionFace visionFace, int cameraFacing) {
        firebaseVisionFace = visionFace;
        this.cameraFacing = cameraFacing;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace visionFace = firebaseVisionFace;
        if (visionFace == null) {
            return;
        }

        // Draws a dot at the position of the detected face,
        // with the face's track ID below.
        float x = translateX(visionFace.getBoundingBox().centerX());
        float y = translateY(visionFace.getBoundingBox().centerY());

        canvas.drawText("ID: " + visionFace.getTrackingId(), x + ID_offsetX,
                y + ID_offsetY, idPaint);
        canvas.drawCircle(x, y, FACE_positionRadius, facePositionPaint);

        // Draws an estimate for happiness
        canvas.drawText("Happiness: " + String.format("%.2f",
                        visionFace.getSmilingProbability()),
                    x + ID_offsetX * 3,
                    y - ID_offsetY,
                    idPaint);

        // Draws an estimate for right/left eye being open, picking the right one depending on
        // the camera facing and mirroring. For some reason, the probabilities seem to be inverted
        // and associated to the opposite eye; hence, I just reverted them.
        if (cameraFacing == CameraSource.CAMERA_facingFront) {
            canvas.drawText("Right eye: " + String.format("%.2f", visionFace
                            .getLeftEyeOpenProbability()),
                    x - ID_offsetX,
                    y, idPaint);

            canvas.drawText("Left eye: " + String.format("%.2f", visionFace
                            .getRightEyeOpenProbability()),
                    x + ID_offsetX * 6,
                    y, idPaint);
        } else {
            canvas.drawText("Left eye: " + String.format("%.2f", visionFace
                            .getRightEyeOpenProbability()),
                    x - ID_offsetX,
                    y, idPaint);

            canvas.drawText("Right eye: " + String.format("%.2f", visionFace
                            .getLeftEyeOpenProbability()),
                    x + ID_offsetX * 6,
                    y, idPaint);
        }

        // Draws a bounding box around the face.
        float xOffset = scaleX(visionFace.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(visionFace.getBoundingBox().height() / 2.0f);

        canvas.drawRect(x - xOffset, y - yOffset,
                x + xOffset, y + yOffset, boxPaint);
    }

}
