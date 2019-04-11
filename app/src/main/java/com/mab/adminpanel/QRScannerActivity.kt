package com.mab.adminpanel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_qrscanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val TAG = QRScannerActivity::class.java.simpleName
    private val permsRequestCode = 200
    private val perms = arrayOf(Manifest.permission.CAMERA)


    override fun handleResult(result: Result?) {
        Log.d(TAG, "handleResult ${result?.text}")
        val intent = Intent()
        intent.putExtra(MConstants.QR_RESULT_INTENT_FILTER, result?.text)
        setResult(MConstants.QR_RESULT_INTENT_RESULT, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            enable_button.setOnClickListener {
                requestPermissions(perms, permsRequestCode)
            }
        }
        initListeners()
    }

    private fun initListeners() {
        cancel_button.setOnClickListener {
            finish()
        }

    }

    public override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                notHavePermissionForCameraLocation()
                return
            }
        }
        gotPermissionOfCameraLocation()

    }

    private fun notHavePermissionForCameraLocation() {
        info_view_id.visibility = VISIBLE
        qr_layout.visibility = GONE
    }

    private fun gotPermissionOfCameraLocation() {
        info_view_id.visibility = GONE
        qr_layout.visibility = VISIBLE
        qr_view_id.setResultHandler(this)
        qr_view_id.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        qr_view_id.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        when (requestCode) {
            200 -> {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted) {
                    gotPermissionOfCameraLocation()
                } else {
                    notHavePermissionForCameraLocation()
                }
            }
        }
    }
}

