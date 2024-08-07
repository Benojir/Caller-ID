package zorro.dimyon.calleridentity.activities;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.helpers.GetPhoneNumberInfo;

public class MainActivity extends AppCompatActivity {

    private static final int CALL_SCREENING_REQUEST_ID = 153;
    private static final int PERMISSIONS_REQUEST_CODE = 4556;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 9786;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

//        TextView textView = findViewById(R.id.textView);
//
//        GetPhoneNumberInfo numberInfo = new GetPhoneNumberInfo("9735749430");
//        numberInfo.getNumberInfo(new GetPhoneNumberInfo.OnFetchedInfoListener() {
//            @Override
//            public void onSuccess(String numberInfo) {
//                textView.setText(numberInfo);
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//                textView.setText(errorMessage);
//            }
//        });
    }

    private void checkAndRequestPermissions() {
        if (areAllPermissionsGranted()) {
            requestRole();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean areAllPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestRole() {
        RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
        if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            startActivityForResult(intent, CALL_SCREENING_REQUEST_ID);
        } else {
            checkOverlayPermission();
        }
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CALL_SCREENING_REQUEST_ID:
                handleCallScreeningResult(resultCode);
                break;
            case OVERLAY_PERMISSION_REQUEST_CODE:
                if (!Settings.canDrawOverlays(this)) {
                    showToastAndFinish("Overlay permission is required.");
                }
                break;
        }
    }

    private void handleCallScreeningResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "App is now the call screening app.", Toast.LENGTH_SHORT).show();
            checkOverlayPermission();
        } else {
            showToastAndFinish("This app needs call screening permission.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (areAllPermissionsGranted()) {
                requestRole();
            } else {
                showToastAndFinish("All permissions are required.");
            }
        }
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(this::finish, 2000);
    }
}