package com.mab.adminpanel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private EditText email;
    private Button scan_button;
    private FirebaseAuth firebaseAuth;
    private View loader;

    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setIds();
        setListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null)
            loginUser();
    }

    private void setIds() {
        email = findViewById(R.id.file_name);
        scan_button = findViewById(R.id.scan_button);
        loader = findViewById(R.id.loader);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

    }

    private void setListener() {
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (performValidation())
                    gotoQRScanActivity();
            }
        });
    }

    private boolean performValidation() {
        if (email.getText().toString().isEmpty()) {
            email.requestFocus();
            email.setError("Enter file name");
            return false;
        }
        return true;
    }

    private void gotoQRScanActivity() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        intent.putExtra("format", 1);
        startActivityForResult(intent, MConstants.QR_RESULT_INTENT_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled ", Toast.LENGTH_SHORT).show();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        loader.bringToFront();
        String barCode = data.getStringExtra(MConstants.QR_RESULT_INTENT_FILTER);
        Log.d(TAG, "BAR CODE : $barCode");
        checkIsFileWithGivenNameInFirebase(barCode);


    }

    private void checkIsFileWithGivenNameInFirebase(String filename) {
        //here file name will be qrcode or barcode
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("validDocuments/" + filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //it comes here if file is there so show valid document and generate random key here
                showUserId(email.getText().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //it comes here if file is not there so show invalid document and you can get error message from firebase with exception.getMessage() i guess
                showAlert("Document validation failed");
            }
        });
    }


    private void showUserId(String email) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("User");
        ref.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot datas : dataSnapshot.getChildren()) {
                    String keys = datas.getKey();
                    Log.d(TAG, "Key " + keys);
                    showAlert("Document validation done successfully,\nGenerated user ID : " + keys);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled " + databaseError.getMessage());
                showAlert("User with given email not found");
            }
        });
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Status")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .show();
    }


    //need for validation purpose firebase me
    private void loginUser() {
        loader.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword("admin@gmail.com", "123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loader.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    }
                });

    }
}
