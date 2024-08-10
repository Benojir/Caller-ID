package zorro.dimyon.calleridentity.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;

import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.adapters.CallLogsAdapter;
import zorro.dimyon.calleridentity.databinding.ActivityMainBinding;
import zorro.dimyon.calleridentity.helpers.CallLogUtils;
import zorro.dimyon.calleridentity.helpers.CustomMethods;
import zorro.dimyon.calleridentity.helpers.LoginSaverPrefHelper;

public class MainActivity extends AppCompatActivity {

    private static final int CALL_SCREENING_REQUEST_ID = 153;
    private static final int PERMISSIONS_REQUEST_CODE = 4556;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 9786;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
    };
    private static final String TAG = "MADARA";

    private boolean isUserLoggedIn = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkAndRequestPermissions();
//      --------------------------------------------------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
//      --------------------------------------------------------------------------------------------

        LoginSaverPrefHelper loginPrefHelper = new LoginSaverPrefHelper(this);

        if (loginPrefHelper.getApiKey().isEmpty()) {
            binding.callLogsViewContainer.setVisibility(View.GONE);
            binding.loginWithOtpBtn.setVisibility(View.VISIBLE);

            binding.loginWithOtpBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            isUserLoggedIn = true;
            binding.loginWithOtpBtn.setVisibility(View.GONE);
            binding.callLogsViewContainer.setVisibility(View.VISIBLE);
        }
    }

    /**************************************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();

        if (isUserLoggedIn && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

            CallLogUtils.getTodaysCallLogs(this, new CallLogUtils.CallLogsCallback() {
                @Override
                public void onCallLogsRetrieved(JSONArray callLogs) {
                    if (callLogs.length() > 0) {
                        binding.todayLogsContainer.setVisibility(View.VISIBLE);
                        setRecyclerView(callLogs, binding.todayLogsRV);
                    } else {
                        binding.todayLogsContainer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onCallLogsRetrieved: ", e);
                    binding.todayLogsContainer.setVisibility(View.GONE);
                    CustomMethods.errorAlert(MainActivity.this, "Error", e.getMessage(), "OK", false);
                }
            });

            CallLogUtils.getYesterdaysCallLogs(this, new CallLogUtils.CallLogsCallback() {
                @Override
                public void onCallLogsRetrieved(JSONArray callLogs) {
                    if (callLogs.length() > 0) {
                        binding.yesterdayLogsContainer.setVisibility(View.VISIBLE);
                        setRecyclerView(callLogs, binding.yesterdayLogsRV);
                    } else {
                        binding.yesterdayLogsContainer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onCallLogsRetrieved: ", e);
                    binding.yesterdayLogsContainer.setVisibility(View.GONE);
                    CustomMethods.errorAlert(MainActivity.this, "Error", e.getMessage(), "OK", false);
                }
            });

            CallLogUtils.getOlderCallLogs(this, new CallLogUtils.CallLogsCallback() {
                @Override
                public void onCallLogsRetrieved(JSONArray callLogs) {
                    if (callLogs.length() > 0) {
                        binding.olderLogsContainer.setVisibility(View.VISIBLE);
                        setRecyclerView(callLogs, binding.olderLogsRV);
                    } else {
                        binding.olderLogsContainer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onCallLogsRetrieved: ", e);
                    binding.olderLogsContainer.setVisibility(View.GONE);
                    CustomMethods.errorAlert(MainActivity.this, "Error", e.getMessage(), "OK", false);
                }
            });
        }
    }

    /**************************************************************************************************/

    private void setRecyclerView(JSONArray callLogs, RecyclerView recyclerView) {
        CallLogsAdapter callLogsAdapter = new CallLogsAdapter(this, callLogs);
        recyclerView.setAdapter(callLogsAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**************************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        MenuItem searchBtn = menu.findItem(R.id.menu_search_action);
        MenuItem settingsBtn = menu.findItem(R.id.menu_settings_action);

        if (!isUserLoggedIn) {
            searchBtn.setVisible(false);
            settingsBtn.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings_action) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.menu_search_action) {
            startActivity(new Intent(this, SearchActivity.class));
        }
        return true;
    }

    /**************************************************************************************************/

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
        } else {
            requestBatteryOptimization();
        }
    }

    @SuppressLint("BatteryLife")
    private void requestBatteryOptimization() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
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