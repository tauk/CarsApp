package com.tauk.carsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

public class ViewAllCarsActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private TextView tvCarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_cars);

        FirebaseApp.initializeApp(this);
        tvCarList = findViewById(R.id.tvCarList);

        displayCars();
    }

    public void displayCars() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference carsReference = firebaseDatabase.getReference("/cars");

        StringBuilder stringBuilder = new StringBuilder();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("View all Cars", "onChildAdded:" + dataSnapshot.getKey());

                // A new car has been added, add it to the displayed list
                Car car = dataSnapshot.getValue(Car.class);
                tvCarList.append(car.toString());
                // ...
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("View all Cars", "onChildChanged:" + dataSnapshot.getKey());
                Toast.makeText(ViewAllCarsActivity.this, dataSnapshot.getKey() + " changed!", Toast.LENGTH_SHORT).show();

                displayCars();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("removed", "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();
                Toast.makeText(ViewAllCarsActivity.this,
                        commentKey + " removed - RELOADING.........", Toast.LENGTH_SHORT).show();
                //reload and display again              Toast.LENGTH_SHORT).show();
                displayCars();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("moved", "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Car car = dataSnapshot.getValue(Car.class);
                String commentKey = dataSnapshot.getKey();
                Toast.makeText(ViewAllCarsActivity.this, commentKey + " moved", Toast.LENGTH_SHORT).show();
                // ...
                displayCars();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("cancelled", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(ViewAllCarsActivity.this, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        carsReference.addChildEventListener(childEventListener); //handle CRUD event on /cars
        tvCarList.setText(stringBuilder.toString());
    }
}
