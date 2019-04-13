package com.mab.adminpanel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private String TAG = QRScannerActivity.class.getSimpleName();
    private int permsRequestCode = 200;
    private String[] perms = new String[]{Manifest.permission.CAMERA};

    private ZXingScannerView qr_view_id;

    @Override
    public void handleResult(Result result) {
        Log.d(TAG, "handleResult" + result.getText());
        Intent intent = new Intent();
        intent.putExtra(MConstants.QR_RESULT_INTENT_FILTER, result.getText());
        setResult(MConstants.QR_RESULT_INTENT_RESULT, intent);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }
        qr_view_id = findViewById(R.id.qr_view_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                Toast.makeText(this, "Need permission", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        gotPermissionOfCameraLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qr_view_id.stopCamera();
    }

    private void gotPermissionOfCameraLocation() {
        qr_view_id.setResultHandler(this);
        qr_view_id.startCamera();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case 200: {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    gotPermissionOfCameraLocation();
                } else {
                    Toast.makeText(this, "Need permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
