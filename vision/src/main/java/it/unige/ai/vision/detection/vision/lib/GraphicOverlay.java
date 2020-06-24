package it.unige.ai.vision.detection.vision.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview). You can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 * <p>Supports scaling and mirroring of the graphics relative to the preview properties. It's based on
 * the idea that detected items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.
 *
 * <p>Associated {@link Graphic} items should use the following methods to convert to view
 * coordinates for the graphics that are drawn:
 *
 * <ol>
 *   <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
 *       supplied value from the preview scale to the view scale.
 *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
 *       coordinate from the preview's coordinate system to the view coordinate system.
 * </ol>
 */
public class GraphicOverlay extends View {
    // Shared resource lock
    private final Object syncLock;

    private int previewWidth;
    private float widthScaleFactor = 1.0f;

    private int previewHeight;
    private float heightScaleFactor = 1.0f;

    private Set<Graphic> graphicObjs;

    // Back camera default
    private int cameraFacing = CameraSource.CAMERA_facingBack;

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
     * this and implement the {@link Graphic#draw(Canvas)} method to define the graphics element. Add
     * instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
     */
    public abstract static class Graphic {

        private GraphicOverlay graphicOverlay;

        public Graphic(GraphicOverlay graphicOverlay) {
            this.graphicOverlay = graphicOverlay;
        }

        /**
         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
         * to view coordinates for the graphics that are being drawn:
         *
         * <ol>
         *   <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
         *       supplied value from the preview scale to the view scale.
         *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
         *       coordinate from the preview's coordinate system to the view coordinate system.
         * </ol>
         *
         * @param canvas Drawing canvas
         */
        public abstract void draw(Canvas canvas);

        /**
         * Adjusts a value on the X-axis from the preview scale to the view scale.
         */
        public float scaleX(float horizontal) {
            return horizontal * graphicOverlay.widthScaleFactor;
        }

        /**
         * Adjusts a value on the Y-axis from the preview scale to the view scale.
         */
        public float scaleY(float vertical) {
            return vertical * graphicOverlay.heightScaleFactor;
        }

        /**
         * Returns the application context of the app.
         */
        public Context getApplicationContext() {
            return graphicOverlay.getContext().getApplicationContext();
        }

        /**
         * Adjusts the X coordinate from the preview's coordinate system to the view coordinate system.
         */
        public float translateX(float x) {
            if (graphicOverlay.cameraFacing == CameraSource.CAMERA_facingFront) {
                return graphicOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        /**
         * Adjusts the Y coordinate from the preview's coordinate system to the view coordinate system.
         */
        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            graphicOverlay.postInvalidate();
        }

    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        syncLock = new Object();
        graphicObjs = new HashSet<>();
    }

    /**
     * Removes all graphics from the overlay.
     */
    public void clear() {
        synchronized (syncLock) {
            graphicObjs.clear();
        }
        postInvalidate();
    }

    /**
     * Adds a graphic to the overlay.
     */
    public void add(Graphic graphic) {
        synchronized (syncLock) {
            graphicObjs.add(graphic);
        }
        postInvalidate();
    }

    /**
     * Removes a graphic from the overlay.
     */
    public void remove(Graphic graphic) {
        synchronized (syncLock) {
            graphicObjs.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * Sets the camera attributes for size and facing direction, which are kept until later when
     * they will inform how to transform image coordinates accordingly.
     */
    public void setCameraInfo(int previewWidth, int previewHeight, int cameraFacing) {
        synchronized (syncLock) {
            this.cameraFacing = cameraFacing;
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
        }
        postInvalidate();
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (syncLock) {
            if ((previewWidth != 0) && (previewHeight != 0)) {
                widthScaleFactor  = (float) canvas.getWidth() / (float) previewWidth;
                heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
            }

            for (Graphic graphic : graphicObjs) {
                graphic.draw(canvas);
            }
        }
    }

}
