package it.unige.ai.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import it.unige.ai.base.R;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.unige.ai.base.utils.HierarchyUtils.getActivity;

/*
 * Static-methods-only class (private constructor) that wraps the pipeline to check in-app permissions grant,
 * following the requirements to enable ML Kit functionalities.
 */
public class PermissionUtils {

    private PermissionUtils() {

    }

    private static String[] getRequiredPermissions(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_PERMISSIONS);

            String[] requestedPermissions = pInfo.requestedPermissions;
            if (requestedPermissions != null && requestedPermissions.length > 0) {
                return requestedPermissions;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static void openSettings(Context context) {
        Intent intent = new Intent();

        Uri uriPackage = Uri.fromParts("package",
                context.getPackageName(),null);

        intent.setAction(android.provider.Settings
                .ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(uriPackage);

        // Open the Settings app
        context.startActivity(intent);
    }

    public static boolean allPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static String getPermissionSimpleName(Context context, String permission)
            throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);

        assert permissionInfo.group != null;

        PermissionGroupInfo permissionGroupInfo = packageManager
                .getPermissionGroupInfo(permissionInfo.group, 0);

        return StringUtils.capitalize(permissionGroupInfo.loadLabel(
                packageManager).toString());
    }

    public static void getRuntimePermissions(Context context, int requestCode) {
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                missingPermissions.add(permission);
            }
        }

        Activity activity = getActivity(context);

        if (!missingPermissions.isEmpty()) {
            Log.i(activity.getClass().getCanonicalName(), "Missing permissions: "
                    + TextUtils.join(" ", missingPermissions));
            ActivityCompat.requestPermissions(activity, missingPermissions
                    .toArray(new String[0]), requestCode);
        }
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        String tag = getActivity(context).getClass()
                .getCanonicalName();

        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(tag, "Permission granted: " + permission);
            return true;
        }
        Log.i(tag, "Permission not granted: " + permission);
        return false;
    }

    public static void showDeniedSnackbar(Context context, String permission)
            throws PackageManager.NameNotFoundException {
        Activity activity = getActivity(context);

        if (activity != null) {
            Snackbar.make(activity.findViewById(android.R.id.content), // android.R.id.content is the ID of the root view
                    context.getString(R.string.snack_permission_denied,
                            getPermissionSimpleName(context, permission)),
                    Snackbar.LENGTH_SHORT).show();

            activity.finish();
        }
    }

    public static void showNeverAskAgainSnackbar(Context context, String permission, String reason)
            throws PackageManager.NameNotFoundException {
        Activity activity = getActivity(context);

        if (activity != null) {
            AtomicBoolean actionTaken = new AtomicBoolean(
                    false);

            Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                    context.getString(R.string.snack_permission_never_ask_again,
                            getPermissionSimpleName(context, permission), reason),
                    Snackbar.LENGTH_LONG);

            View snackbarView = snackbar.getView();
            TextView snackTextView = snackbarView.findViewById(com.google.android
                    .material.R.id.snackbar_text);

            snackTextView.setMaxLines(5); // Avoid text truncation
            snackbar.setAction("SET", view -> {
                    actionTaken.set(true);
                    openSettings(context);}).show();

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBar, int event) {
                    super.onDismissed(transientBar, event);
                    if (!actionTaken.get())
                        activity.finish();
                }
            });
        }
    }

}
