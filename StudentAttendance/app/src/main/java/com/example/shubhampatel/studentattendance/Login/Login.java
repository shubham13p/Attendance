package com.example.shubhampatel.studentattendance.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.shubhampatel.studentattendance.Main.MainActivity;
import com.example.shubhampatel.studentattendance.R;
import com.example.shubhampatel.studentattendance.Utils.Method;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity {

    private Context mContext;
    private Button register, login, forgot;
    private EditText email, password;
    private static final String TAG = "Login";

    private String macduringregister, macduringlogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    String scuemail, scupassword;
    private DatabaseReference myref = FirebaseDatabase.getInstance().getReference("Student");
    private Method method;

    public static String clientusername;
    public static String mypref = "mypref";
    public static SharedPreferences pref;
    public SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Log.e(TAG, String.valueOf(mAuth.getCurrentUser()));
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }

        method = new Method(mContext);
        mContext = Login.this;

        register = findViewById(R.id.btn_signup);
        login = findViewById(R.id.btn_login);
        forgot = findViewById(R.id.btn_reset_password);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();


        macduringlogin = method.getMacc();
        Log.e(TAG, "MAC during login :- " + macduringlogin);
        Log.e(TAG, String.valueOf(mAuth.getCurrentUser()));


        register.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });


        forgot.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scuemail = email.getText().toString();
                scupassword = password.getText().toString();

                if ((TextUtils.isEmpty(scuemail)) || !scuemail.matches(Register.EMAIL_PATTERN) || TextUtils.isEmpty(scupassword)) {
                    Toast.makeText(mContext, "Fields Empty Or Email Format Different", Toast.LENGTH_SHORT).show();

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(scuemail, scupassword)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {

                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (task.isSuccessful()) {
                                        try {
                                            if (user.isEmailVerified()) {
                                                Log.e(TAG, "onComplete: success, email is verified.");

                                                clientusername = email.getText().toString();
                                                edit.putString("clientusername", clientusername);
                                                edit.commit();
                                                Log.e(TAG, clientusername);

                                                myref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.child("email").getValue(String.class).equals(scuemail)) {

                                                                macduringregister = snapshot.child("mac").getValue(String.class);
                                                                Log.e(TAG, "MAC at register " + macduringregister);

                                                                if (macduringlogin.equals(macduringregister)) {
                                                                    // client username for display on main page using shared preference

                                                                    edit.putString("clientusermac", macduringregister);
                                                                    edit.commit();
                                                                    Log.e(TAG, clientusername);


                                                                    email.getText().clear();
                                                                    password.getText().clear();
                                                                    progressBar.setVisibility(View.GONE);

                                                                    Toast.makeText(Login.this, "Welcome", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                                                    startActivity(intent);
                                                                } else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(Login.this, "Please Use Your Register Mobile.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(Login.this, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                        } catch (NullPointerException e) {
                                            Log.e(TAG, "signInWithEmail: onComplete: " + task.isSuccessful());
                                            progressBar.setVisibility(View.GONE);
                                        }

                                    } else {
                                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                }
                            });
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
