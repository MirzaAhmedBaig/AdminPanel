package com.mab.adminpanel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseDatabase by lazy {
        FirebaseDatabase.getInstance().reference
    }

    private val approvedRef by lazy {
        firebaseDatabase.child("ApprovedInfo")
    }

    private val firebaseStorage by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val documentRef by lazy {
        firebaseStorage.child("documents")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
    }

    public override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser == null)
            loginUser()
    }

    private fun setListeners() {
        scan_button.setOnClickListener {
            if (performValidation())
                gotoQRScanActivity()
        }
    }

    private fun performValidation(): Boolean {
        if (file_name.text.toString().isBlank()) {
            file_name.requestFocus()
            file_name.error = "Enter file name"
            return false
        }
        return true
    }

    private fun gotoQRScanActivity() {
        val intent = Intent(this, QRScannerActivity::class.java)
        intent.putExtra("format", 1)
        startActivityForResult(intent, MConstants.QR_RESULT_INTENT_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled ", Toast.LENGTH_SHORT).show()
            return
        }

        loader.visibility = View.VISIBLE
        loader.bringToFront()
        val barCode = data?.getStringExtra(MConstants.QR_RESULT_INTENT_FILTER)!!
        Log.d(TAG, "BAR CODE : $barCode")

        firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "onCancelled : ${p0.message}")
                loader.visibility = View.GONE
                showAlert("Please Try Again")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange ")
                if (snapshot.hasChild("documentsKeys/$barCode")) {
                    onValidFile()
                } else {
                    showAlert("File Not approved")
                    loader.visibility = View.GONE
                }
            }
        })

    }


    private fun onValidFile() {

        documentRef.child(file_name.text.toString()).downloadUrl.addOnSuccessListener { uri ->
            val email = file_name.text.toString().split("__")[0]
            val url = uri.toString()

            val key = approvedRef.push().key
            approvedRef.child(key!!).child("User").setValue(email)
            approvedRef.child(key!!).child("Approved Document").setValue(url)

            showAlert("Document Approved")
            loader.visibility = View.GONE
        }.addOnFailureListener {
            showAlert("Invalid File Name")
            loader.visibility = View.GONE
        }

    }


    private fun loginUser() {
        loader.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword("admin@gmail.com", "123456")
            .addOnCompleteListener(this) { task ->
                loader.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showAlert(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Status")
            .setMessage(msg)
            .setPositiveButton("Ok", null)
            .show()
    }

}
