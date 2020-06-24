package it.unige.ai.vision.detection.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import it.unige.ai.vision.R;
import it.unige.ai.vision.detection.models.faceDetection.FaceDetectionProcessor;
import it.unige.ai.vision.detection.vision.lib.CameraSource;
import it.unige.ai.vision.detection.vision.CameraPreview;
import it.unige.ai.vision.detection.vision.lib.GraphicOverlay;
import com.google.android.gms.common.annotation.KeepName;

import java.io.IOException;

import static it.unige.ai.base.utils.PermissionUtils.allPermissionsGranted;
import static it.unige.ai.base.utils.PermissionUtils.getRuntimePermissions;
import static it.unige.ai.base.utils.PermissionUtils.showDeniedSnackbar;
import static it.unige.ai.base.utils.PermissionUtils.showNeverAskAgainSnackbar;

/**
 * Continuous frame-by-frame ML processing on frames captured by a camera source.
 */
@KeepName
public final class DetectorActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = DetectorActivity.class.getCanonicalName();
    private static final int RC = 2;

    private CameraSource cameraSource;
    private CameraPreview cameraPreview;
    private GraphicOverlay graphicOverlay;

    // TODO: Allow for more ML Kit models
    private String selectedModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_detector);

        // Camera display elements
        cameraSource = null;

        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.w(TAG, "Graphic overlay is null");
        }

        cameraPreview = findViewById(R.id.camera_preview);
        if (cameraPreview == null) {
            Log.w(TAG, "Camera preview is null");
        }

        ToggleButton facingSwitch = findViewById(R.id.ic_camera_facing);
        facingSwitch.setOnCheckedChangeListener(this);

        // Model initialization
        selectedModel = "Face detection";

        if (allPermissionsGranted(this)) {
            createCameraSource();
        } else {
            getRuntimePermissions(this, RC);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Camera facing switch");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource
                        .CAMERA_facingFront);
            } else {
                cameraSource.setFacing(CameraSource
                        .CAMERA_facingBack);
            }
        }
        cameraPreview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
        // If there's no cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        switch (selectedModel) {
            case "Face detection":
                Log.i(TAG, "Processor updated: " + selectedModel);
                cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
                break;
            default:
                Log.e(TAG, "Processor unknown: " + selectedModel);
        }
    }

    /**
     * Controller of the camera source runtime:
     *      - Start it, if brand new;
     *      - Restart it, if it already exists.
     *
     * Please note: If the camera source doesn't exist yet (e.g., because onResume was called before
     * the camera source was created), this will be called again as soon as it is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (graphicOverlay == null) {
                    Log.d(TAG, "onResume > Graphic overlay is null");
                }

                if (cameraPreview == null) {
                    Log.d(TAG, "onResume > Camera preview is null");
                }
                cameraPreview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Camera source not started", e);

                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permissions request done!");
        if(permissions.length == 0)
            return;

        if (requestCode == RC) {
            // TODO: Check permission grant for the whole array
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) { // Permission granted
                createCameraSource();
            } else if (!shouldShowRequestPermissionRationale(permissions[0])) { // `Never ask again` option selected
                try {
                    showNeverAskAgainSnackbar(this, permissions[0],
                            "detect features from real-time image captures");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {  // Permission denied
                try {
                    showDeniedSnackbar(this, permissions[0]);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
