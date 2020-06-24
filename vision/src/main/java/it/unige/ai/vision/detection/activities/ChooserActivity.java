package it.unige.ai.vision.detection.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import it.unige.ai.vision.R;
import com.google.android.material.button.MaterialButton;

import static it.unige.ai.base.utils.PermissionUtils.allPermissionsGranted;
import static it.unige.ai.base.utils.PermissionUtils.getRuntimePermissions;
import static it.unige.ai.base.utils.PermissionUtils.showDeniedSnackbar;
import static it.unige.ai.base.utils.PermissionUtils.showNeverAskAgainSnackbar;

public class ChooserActivity extends AppCompatActivity {

    private static final String TAG = ChooserActivity.class.getCanonicalName();
    private static final int RC = 1; // Request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_chooser);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialButton btnStartDetection = findViewById(R.id.btn_start_detection);
        btnStartDetection.setOnClickListener(view -> {
                    if (!allPermissionsGranted(this)) {
                        getRuntimePermissions(this, RC);
                    } else {
                        startActivity(new Intent(getApplicationContext(),
                                DetectorActivity.class));
                    }
        });
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
                startActivity(new Intent(getApplicationContext(),
                        DetectorActivity.class));
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
