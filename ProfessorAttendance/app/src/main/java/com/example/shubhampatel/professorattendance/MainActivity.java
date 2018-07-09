package com.example.shubhampatel.professorattendance;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button push;
    EditText profstring;
    DatabaseReference reference, databaseReference;
    String formattedDate, pattern;
    Spinner show;
    TextView count;
    long totaldays = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profstring = findViewById(R.id.profstring);
        push = findViewById(R.id.button);
        show = findViewById(R.id.spinner);
        count = findViewById(R.id.cnt);

        // to get current date.
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyy");
        formattedDate = df.format(c);

        // get the date for spinner that show total no of student present at that date.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Attendance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> dates = new ArrayList<String>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    dates.add(snapshot.getKey());
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "");
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dates);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                show.setAdapter(dataAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // to enter the qrcode pattern name from admin side to firebase
        // the variable pattern should be same i.e. when the admin insert it into firebase and when admin generate qr code
        // with the help of website or online qr code generator.
        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern = profstring.getText().toString().trim();
                if (!pattern.equals("")) {
                    reference = FirebaseDatabase.getInstance().getReference("Attendance/" + formattedDate.toString() + "/" + pattern);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = new User("profemail", "true");
                            reference.child("Prof").setValue(user);
                            profstring.setText("");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Field Is Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // this will display the total number of student present at that particular date.
        show.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String root = show.getSelectedItem().toString();
                counttotalstudent(root);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    // for counting the no of student present
    // will have to substract 1 from it due to presents of "Prof" child in firebase.
    private void counttotalstudent(String root) {
        final DatabaseReference attendance = FirebaseDatabase.getInstance().getReference("Attendance/"+root);
        attendance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totaldays = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    totaldays = snapshot.getChildrenCount();
                    Log.e(snapshot.getKey(), snapshot.getChildrenCount() + "");
                }
                count.setText((totaldays-1)+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
