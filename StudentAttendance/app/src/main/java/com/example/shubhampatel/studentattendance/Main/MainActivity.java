package com.example.shubhampatel.studentattendance.Main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shubhampatel.studentattendance.Barcode.BarcodeCaptureActivity;
import com.example.shubhampatel.studentattendance.Login.Login;
import com.example.shubhampatel.studentattendance.R;
import com.example.shubhampatel.studentattendance.Utils.Method;
import com.example.shubhampatel.studentattendance.Utils.User;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context mContext;
    private static final String TAG = "MainActivity";
    private TextView username, studentemail, barcodescan, totalclassno, totalclasspresent;
    private String fillid, fillemail, fillname, profileImageUri, fillmac, userscanbar, noofdayspresent, formattedDate;
    private ImageView imagePerson;
    private int BARCODE_READER_REQUEST_CODE = 1;
    private Barcode barcode;
    private int totaldays = 0;
    private DatabaseReference myref, myRef = FirebaseDatabase.getInstance().getReference("Student");
    private DatabaseReference attendance = FirebaseDatabase.getInstance().getReference("Attendance");

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Method method;

    Date date;
    SimpleDateFormat dateformat;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }
            }
        };


        mContext = MainActivity.this;
        method = new Method(mContext);

//        method.setupFirebaseAuth();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home Page");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        imagePerson = headerView.findViewById(R.id.imageView);
        username = headerView.findViewById(R.id.namesharp);


        studentemail = findViewById(R.id.student_email);
        barcodescan = findViewById(R.id.barcodescan);

        totalclassno = findViewById(R.id.mytotalclass);
        totalclasspresent = findViewById(R.id.mycount);


        date = Calendar.getInstance().getTime();
        dateformat = new SimpleDateFormat("ddMMMyyyy");
        formattedDate = dateformat.format(date);


        findUser();
        counttotalnoofclass();

        Login.clientusername = Login.pref.getString("clientusername", null);
        Log.d(TAG, fillmac + "<- MAC EMAIL->" + Login.clientusername);
        username.setText(Login.clientusername);
        studentemail.setText(Login.clientusername);


    }

    private void findUser() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("email").getValue(String.class).equals(Login.clientusername)) {
                        fillid = snapshot.child("id").getValue(String.class);
//                      or fillid = snapshot.getKey();
                        fillname = snapshot.child("name").getValue(String.class);
                        profileImageUri = snapshot.child("profileimage").getValue(String.class);
                        fillemail = Login.clientusername.toString();
                        fillmac = snapshot.child("mac").getValue(String.class);
                        noofdayspresent = snapshot.child("noofpresent").getValue(String.class);
                        totalclasspresent.setText("YOUR TOTAL PRESENT :- " + noofdayspresent);
                        if (profileImageUri != null) {
                            Log.d(TAG, fillemail + " " + fillname + " " + fillid + " " + profileImageUri + " " + fillmac + " " + noofdayspresent);
                            Glide.with(getApplication()).load(profileImageUri).into(imagePerson);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.update) {
            Intent i = new Intent(MainActivity.this, Profile.class);
            i.putExtra("fillid", fillid);
            i.putExtra("fillname", fillname);
            i.putExtra("fillemail", fillemail);
            i.putExtra("profileimg", profileImageUri);
            startActivity(i);

        } else if (id == R.id.scanpresent) {
            Toast.makeText(MainActivity.this, "Scan Attandence", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, BarcodeCaptureActivity.class);
            startActivityForResult(i, BARCODE_READER_REQUEST_CODE);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Toast.makeText(MainActivity.this, "Barcode read successfully", Toast.LENGTH_SHORT).show();
                    userscanbar = barcode.displayValue.toString();
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    recordmypresent();
                } else {
                    barcodescan.setText("no barcode scan");
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void counttotalnoofclass() {
        attendance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totaldays = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    totaldays++; // total class days
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "");
                }
                Log.e(TAG, totaldays + "");
                totalclassno.setText("TOTAL CLASS :- " + totaldays);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void recordmypresent() {

        final Query userQuery = FirebaseDatabase.getInstance().getReference().child("Attendance").child(formattedDate).orderByChild("email");
        userQuery.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot.getChildrenCount() != 0) {

                                    // key enter by professor
                                    String key = dataSnapshot1.getKey();

                                    // mac during registration
                                    String usermac = fillmac;

                                    // mac during login, Mainactivity
                                    String usermac1 = method.getMacc();
                                    Log.d(TAG, key + " " + usermac + " " + usermac1);

                                    // check the barcode scan by student
                                    // if it is different from the admin provided
                                    if (key.equals(userscanbar)) {

                                        // check if the user uses the device same as used during registration time
                                        if (usermac.equals(usermac1)) {

                                            // device is same
                                            //barcodescan.setText(barcode.displayValue);
                                            barcodescan.setText("You Are Present");
                                            method.showalert("Attendance Success", "Your Present Is Marked, ThankYou");

                                            // increasing the no of days uer is present
                                            int present = Integer.parseInt(noofdayspresent) + 1;
                                            myref = FirebaseDatabase.getInstance().getReference("Attendance").child(formattedDate).child(key.toString());
                                            User user1 = new User(fillemail, "true");
                                            myref.child(fillid).setValue(user1);

                                            User user2 = new User(fillemail, fillname, fillid, fillmac, profileImageUri, present + "");
                                            myRef.child(fillid).setValue(user2);

                                        } else {
//                                            Toast.makeText(MainActivity.this, "Please use your register Mobile to mark your present", Toast.LENGTH_SHORT).show();
                                            method.showalert("Attendance Error", "Please use your register Mobile to mark your present");

                                        }
                                    } else {
                                        Log.d(TAG, key.toString() + " " + usermac.toString() + " " + usermac1.toString());
//                                        Toast.makeText(MainActivity.this, "Please scan the barcode provided by Admin", Toast.LENGTH_SHORT).show();
                                        method.showalert("Attendance Error", "Please scan the barcode provided by Admin");

                                    }
                                }
                            }
                        } else {
                            method.showalert("Attendance Error", "The Attendance Has Not Yet Started");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signOut() {

        mAuth.signOut();
    }


}
