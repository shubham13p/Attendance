package com.example.shubhampatel.studentattendance.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shubhampatel.studentattendance.R;
import com.example.shubhampatel.studentattendance.Utils.Method;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;


public class Register extends AppCompatActivity implements Serializable {

    private Context mContext;
    private Button register;
    private ProgressBar progressBar;
    private EditText scuid, scuemail, scuname, scupassword;
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "gmail.com"; // "scu.edu";
    public static final String PASSWORD_PATTERN = "^.*(?=.{6,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$";
    public static final String ID_PATTERN = "^[A-Z]{1}" + "\\d{7}$";       //"^[A-Z\\d{7}]";
    private DatabaseReference myRef;
    private String sscuid, sscuemail, sscuname, sscupassword, userMac, sscuimage;
    private Method method;
    private static final String TAG = "Register";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Registration");

        mContext = Register.this;
        method = new Method(mContext);
        userMac = method.getMacc();
        mAuth = FirebaseAuth.getInstance();

        register = findViewById(R.id.sign_up_button);
        progressBar = findViewById(R.id.progressBar);
        scuid = findViewById(R.id.id);
        scuemail = findViewById(R.id.email);
        scuname = findViewById(R.id.name);
        scupassword = findViewById(R.id.password);
        myRef = FirebaseDatabase.getInstance().getReference("Student");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sscuid = scuid.getText().toString().trim().toUpperCase();
                sscuemail = scuemail.getText().toString().trim();
                sscuname = scuname.getText().toString().trim().toUpperCase();
                sscupassword = scupassword.getText().toString().trim();

                if (TextUtils.isEmpty(sscuemail)) {
                    Toast.makeText(getApplicationContext(), "Enter SCU EMAIL!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(sscuid)) {
                    Toast.makeText(getApplicationContext(), "Enter SCU ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(sscuname)) {
                    Toast.makeText(getApplicationContext(), "Enter SCU Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(sscupassword)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!sscuemail.matches(EMAIL_PATTERN)) {
                    Toast.makeText(getApplicationContext(), "Please Check The Format Of Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sscuid.length() != 8) {
                    Toast.makeText(getApplicationContext(), "Incorrect SCU ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sscupassword.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!sscupassword.matches(PASSWORD_PATTERN)) {
                    Toast.makeText(getApplicationContext(), "Password Error Must Contain Capital, Small, Number, Special Character Each", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!sscuid.matches(ID_PATTERN)) {
                    Toast.makeText(getApplicationContext(), "Incorrect SCU ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                setupfirebase();
            }
        });
    }

    private void setupfirebase() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getChildrenCount() != 0) {

                        Log.e(TAG, sscuemail + " " + sscuid + " " + userMac);

                        if (ds.child("email").getValue(String.class).equals(sscuemail)) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "email present", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (ds.child("id").getValue(String.class).equals(sscuid)) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "id present", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (ds.child("name").getValue(String.class).equals(sscuname)) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "name present", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (ds.child("mac").getValue(String.class).equals(userMac)) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "This device is already register", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                // by default image or can enter blank into firebase
                sscuimage = "https://firebasestorage.googleapis.com/v0/b/studentattendance-3f232.appspot.com/o/profileimage%2Fdefault.png?alt=media&token=7748abd3-1663-42f5-b932-45cbda7b204d";
                method.registerNewEmail(sscuemail, sscuname, sscuid, sscupassword, sscuimage);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
