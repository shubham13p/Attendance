package com.example.shubhampatel.studentattendance.Main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shubhampatel.studentattendance.R;
import com.example.shubhampatel.studentattendance.Utils.Method;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private Context mContext;
    private static final String TAG = "Profile";
    private TextView id, email;
    private EditText name;
    private DatabaseReference studentinfo;
    private Button update;
    private String userId, displayid, displayemail, displayname, displayimg;
    private ImageView mProfileImage;
    private Uri resultUri;
    private final int PICK_IMAGE_REQUEST = 71;
    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileimage").child(userId + ".png");
    private Method method;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mContext = Profile.this;
        method = new Method(mContext);

        method.setupFirebaseAuth();


        id = findViewById(R.id.id);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        update = findViewById(R.id.update_button);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        studentinfo = FirebaseDatabase.getInstance().getReference("Student");
        progressBar = findViewById(R.id.progressBar);
        mProfileImage = findViewById(R.id.profileImage);

        getSupportActionBar().setTitle("Your Profile");

        Log.d(TAG, "onCreate: user id is" + userId);

        Intent intent = getIntent();
        displayid = intent.getStringExtra("fillid");
        displayemail = intent.getStringExtra("fillemail");
        displayname = intent.getStringExtra("fillname");
        displayimg = intent.getStringExtra("profileimg");

        Log.d(TAG, " id: " + displayid + " email: " + displayemail + " name: " + displayname + " img: " + displayimg);

        resultUri = Uri.parse(displayimg.toString());

        id.setText(displayid);
        email.setText(displayemail);
        name.setText(displayname);
        if (displayimg != null) {
            Glide.with(getApplication()).load(displayimg).into(mProfileImage);
        }

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploaddata();
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                Bitmap bmResized = Method.getResizedBitmap(bitmap, 250, 250);
                mProfileImage.setImageBitmap(bmResized);
                Log.d(TAG, resultUri.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploaddata() {
        final String username = name.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(mContext, "Fields Empty", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if ((resultUri.toString().equals(displayimg)) && (username.equals(displayname))) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Profile.this, " No Data To Change", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Profile.this, MainActivity.class));
        } else {
            if (resultUri != null) {
                if (resultUri.toString() != displayimg) {
                    Log.e("resulturi", resultUri.toString());
                    Log.e("displayimg", displayimg);

                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
                    final byte[] data = baos.toByteArray();
                    UploadTask uploadTask = filepath.putBytes(data);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return filepath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.VISIBLE);
                                Uri downloadUri = task.getResult();
                                Map userInfo = new HashMap<>();
                                userInfo.put("profileimage", downloadUri.toString());
                                userInfo.put("name", username.toUpperCase());
                                studentinfo.child(displayid).updateChildren(userInfo);
                                Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(Profile.this, MainActivity.class));
                            }
                        }
                    });
                } else {
                    Map userInfo = new HashMap<>();
                    userInfo.put("name", username.toUpperCase());
                    studentinfo.child(displayid).updateChildren(userInfo);
                    Toast.makeText(Profile.this, "Name Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Profile.this, MainActivity.class));

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Profile.this, MainActivity.class));
        super.onBackPressed();
    }
}
