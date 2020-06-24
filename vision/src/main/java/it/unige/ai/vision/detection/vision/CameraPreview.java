package it.unige.ai.vision.detection.vision;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import it.unige.ai.vision.detection.vision.lib.CameraSource;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.android.gms.common.images.Size;

import java.io.IOException;

/**
 * Widget to preview the captured image onto the screen.
 */
public class CameraPreview extends ViewGroup {
    private static final String TAG = CameraPreview.class.getCanonicalName();

    private final int DEFAULT_width  = 320;
    private final int DEFAULT_height = 240;

    private Context mContext;
    private boolean startRequested;

    private boolean surfaceAvailable;
    private SurfaceView surfaceView;

    private CameraSource cameraSource;
    private GraphicOverlay graphicOverlay;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;
        startRequested = false;
        surfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        // Add view to the tree
        addView(surfaceView);
    }

    public void start(CameraSource cameraSource)
            throws IOException {
        if (cameraSource == null) { // Might be throttling
            stop();
        }

        this.cameraSource = cameraSource;

        if (this.cameraSource != null) {
            startRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay graphicOverlay)
            throws IOException {
        this.graphicOverlay = graphicOverlay;
        start(cameraSource);
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private void startIfReady() throws IOException {
        if (startRequested && surfaceAvailable) {
            cameraSource.start(surfaceView.getHolder()); // The UX flow reaches this point only if the user has granted all the permissions
                                                         // and blocks him in case he reverts them back; hence, there's no potential `SecurityException`.
            if (graphicOverlay != null) {
                Size size = cameraSource.getPreviewSize();

                int minHW = Math.min(size.getWidth(), size.getHeight());
                int maxHW = Math.max(size.getWidth(), size.getHeight());

                // Swap width and height when in portrait mode, since the camera source
                // will be rotated by 90 degrees.
                if (isPortraitMode()) { // Responsive on orientation
                    graphicOverlay.setCameraInfo(minHW, maxHW, cameraSource.getCameraFacing());
                } else {
                    graphicOverlay.setCameraInfo(maxHW, minHW, cameraSource.getCameraFacing());
                }
                graphicOverlay.clear();
            }
            startRequested = false;
        }
    }

    /*
     * Whether the device is in portrait or landscape mode.
     */
    private boolean isPortraitMode() {
        int orientation = mContext.getResources()
                .getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode() returning false by default");
        return false;
    }

    @Override
    protected void onLayout(boolean isChanged, int left, int top,
                            int right, int bottom) {
        int previewWidth  = DEFAULT_width;
        int previewHeight = DEFAULT_height;

        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();

            if (size != null) {
                previewWidth  = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height when in portrait mode, since the camera source
        // will be rotated by 90 degrees.
        if (isPortraitMode()) {
            int tmp = previewWidth;

            previewWidth  = previewHeight;
            previewHeight = tmp;
        }

        final int layoutWidth  = right - left;
        final int layoutHeight = bottom - top;

        int childWidth  = layoutWidth;
        int childHeight = layoutHeight;

        int childXOffset = 0;
        int childYOffset = 0;

        float wRatio = (float) layoutWidth  / (float) previewWidth;
        float hRatio = (float) layoutHeight / (float) previewHeight;

        // In order to fill the view, while also preserving the correct aspect ratio, it is usually
        // necessary to slightly oversize the child and to crop off portions along one of the dimensions,
        // otherwise it may be a little bit off. We scale up based on the dimension requiring
        // the most correction, and compute a crop offset for the other dimension.
        if (wRatio > hRatio) {
            childHeight = (int) ((float) previewHeight * wRatio);
            childYOffset = (childHeight - layoutHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * hRatio);
            childXOffset = (childWidth - layoutWidth) / 2;
        }

        // One dimension will be "cropped" (i.e., actually just moved outside of the view port). We shift every child over
        // or up by an offset on the X- and the Y-axis and adjust the size to maintain the proper aspect ratio.
        for (int i = 0; i < getChildCount(); ++i) {
            Log.d(TAG, "Child #" + i + " assigned to preview");

            // l: Left, t: Top, r: Right and b: Bottom
            getChildAt(i).layout(-1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Camera source not started", e);
        }
    }

    /*
     * Stateful handler of the surface showing the preview; it keeps track of the state
     * through the outer flag `surfaceAvailable`.
     */
    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;

            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Camera source not started", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {

        }

    }

}
