package com.tauk.carsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Main2Activity extends Activity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    DatabaseReference newCarDbRef = null;

    //Firebase Storage reference
    FirebaseStorage firebaseStorage;
    //image URL for the uploaded image
    String uploadedImageURL;

    private EditText etCarRegNo;
    private EditText etCarModelName;
    private EditText etCarModelYear;
    private EditText etCarPrice;

    private TextView tvStatus;

    //camera - image capture
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//get the user who is logged in

        //init firebaseStorage
        firebaseStorage = FirebaseStorage.getInstance();

        etCarRegNo = findViewById(R.id.etCarRegNumber);
        etCarModelName = findViewById(R.id.etCarModelName);
        etCarModelYear = findViewById(R.id.etModelYear);
        etCarPrice = findViewById(R.id.etPrice);

        tvStatus = findViewById(R.id.tvStatus);

        //init imageView
        imageView = findViewById(R.id.imageView);

        String user = firebaseUser.getDisplayName() + firebaseUser.getEmail() + firebaseUser.getPhoneNumber();
        Toast.makeText(this, user, Toast.LENGTH_SHORT).show();
    }

    public void doAddNewCar(View view) {
        final String carRegNo = etCarRegNo.getText().toString();
        String carModelName = etCarModelName.getText().toString();
        int carModelYear = Integer.parseInt(etCarModelYear.getText().toString());
        double carPrice = Double.parseDouble(etCarPrice.getText().toString());

        String postedByUser = firebaseUser.getDisplayName();
        String posterEmail = firebaseUser.getEmail();
        String posterMobile = firebaseUser.getPhoneNumber();

        Toast.makeText(this, "User " + postedByUser + "mobile" + posterMobile, Toast.LENGTH_LONG).show();

        Car car = new Car(carRegNo, carModelName, carModelYear, carPrice, postedByUser, posterEmail,
                                posterMobile, "");
        Toast.makeText(this, "Saving " + car, Toast.LENGTH_LONG).show();

        //get a reference to the Firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if(databaseReference != null)
            Toast.makeText(this, databaseReference.toString(), Toast.LENGTH_LONG).show();

        //create a new node under which you will add the new car data
        Task task = databaseReference.child("cars").child(carRegNo).setValue("");

                task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            tvStatus.setText(carRegNo + "added!");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("doRegister", "createUserWithEmail:failure", task.getException());
                            tvStatus.setText("Add failed!!!" +task.getException());
                        }

                        // ...
                    }
                });

        //get the reference to the regNumber under /cars node
        newCarDbRef = databaseReference.child("/cars").child(carRegNo);

        //add the new car data
        newCarDbRef.setValue(car) //adds the data under carRegNo child node
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        Toast.makeText(Main2Activity.this, carRegNo + " as added!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Toast.makeText(Main2Activity.this, carRegNo + " db error!", Toast.LENGTH_SHORT).show();
                    }
                });

        //now upload the image and update the image URL in the newCarDbRef
        uploadImageToFirebaseStorageFor(newCarDbRef, car);


    }

    public void deleteByCarRegNumber(View view) {
        final String carRegNo = etCarRegNo.getText().toString();

        //get a reference to the Firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference carRef = databaseReference.child("/cars").child(carRegNo);

        if (carRef != null) {
            //a car exists with this registration number - delete
            Task removeTask = carRef.removeValue();
            removeTask.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        tvStatus.setText(carRegNo + " removed!");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("doRegister", "remove car:failure", task.getException());
                        tvStatus.setText("remove failed!!!" +task.getException());
                    }
                    // ...
                }
            });
        }
        else {
            Toast.makeText(Main2Activity.this, carRegNo + " not found!", Toast.LENGTH_LONG).show();
        }
    }

    //search a car by registration number
    public void doSearch(View view) {
        final String carRegNo = etCarRegNo.getText().toString();

        //get a reference to the Firebase database
        //STEP 1 - get the reference to your firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //STEP 2 - get a reference to the child under your database ref
        DatabaseReference carRef = databaseReference.child("/cars").child(carRegNo);

        //see https://firebase.google.com/docs/database/android/read-and-write?authuser=0

        //STEP 3 - create a ValueEventListener
        ValueEventListener carQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //data from firebase into DataSnapshot
                if (dataSnapshot.exists()) { //if snapshot exists it means a car exists at carRef
                    // Get the Car object and use the values to update the UI
                    Car car = dataSnapshot.getValue(Car.class);
                    // ...
                    etCarModelName.setText(car.carModelName);
                    etCarModelYear.setText(car.year + "");
                    etCarPrice.setText(car.price + "");
                    tvStatus.setText(carRegNo + " found!");
                }
                else {
                    Toast.makeText(Main2Activity.this, carRegNo + " NOT found!", Toast.LENGTH_SHORT).show();
                    tvStatus.setText(carRegNo + " NOT found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("SEARCH", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        //STEP 4 - add the value event lister to your car reference
        carRef.addListenerForSingleValueEvent(carQueryListener);
    }

    //Update a car by regNo
    public void doUpdateCar(View view) {
        final String carRegNo = etCarRegNo.getText().toString();

        //get a reference to the Firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference carRef = databaseReference.child("/cars").child(carRegNo);

        //see https://firebase.google.com/docs/database/android/read-and-write?authuser=0

        ValueEventListener carQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if snapshot exists it means a car exists at carRef
                    // Get the Car object and get the posterEmail from it to check authorization
                    Car car = dataSnapshot.getValue(Car.class);
                    String posterEmail = car.posterEmail;

                    //Check if the current user the same user who posted the car ad?
                    if (posterEmail.equals(firebaseUser.getEmail())) {
                        // the current user is the same user who posted this car ad - so update
                        //get the new values from edit text and update them in the car object
                        car.carModelName = etCarModelName.getText().toString();
                        car.year = Integer.parseInt(etCarModelYear.getText().toString());
                        car.price = Double.parseDouble(etCarPrice.getText().toString());
                        //poster email is already in car object
                        //update the car ref with the new values from the editTexts
                        carRef.setValue(car);
                        tvStatus.setText( "Car "+carRegNo+ " updated!");
                    }
                    else {
                        Toast.makeText(Main2Activity.this, "Update NOT Authorized ",
                                                                        Toast.LENGTH_SHORT).show();
                        tvStatus.setText( "Update NOT Authorized");
                    }
                }
                else {
                    Toast.makeText(Main2Activity.this, carRegNo + " NOT found!", Toast.LENGTH_SHORT).show();
                    tvStatus.setText(carRegNo + " NOT found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("UPDATE", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        carRef.addListenerForSingleValueEvent(carQueryListener);
    }

    public void doShowViewAllCarsActivity(View view) {
        Intent intent = new Intent(this, ViewAllCarsActivity.class);
        startActivity(intent);
    }

    public void doLogout(View view) {
        firebaseAuth.signOut(); //logout
        finish(); //close current activity and go to previous activity
    }

    public void doTakePic(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check if there is a camera on this device
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    public void uploadImageToFirebaseStorageFor(DatabaseReference databaseReference, Car car) {
        // Create a storage reference from our app
        StorageReference storageRef = firebaseStorage.getReference();

        //get the car reg no so we can make a file name for the uploaded image
        final String carRegNo = etCarRegNo.getText().toString();

        // Create a reference to 'cars-images/<carRegNo>.jpg'
        final StorageReference carImageRef = storageRef.child("cars-images/"+carRegNo+".jpg");

        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Upload the bytes data from image to the carImageRef in Firebase Storage
        UploadTask uploadTask = carImageRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return carImageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String downloadURL = downloadUri.toString();
                    Toast.makeText(Main2Activity.this, "URL:"+downloadURL, Toast.LENGTH_LONG).show();
                    updateCarRefWithImageURL(carRegNo, downloadURL);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    //update the car ref with the upload image URL for it
    public void updateCarRefWithImageURL(final String carRegNo, final String uploadedImageURL) {

        //get a reference to the Firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference carRef = databaseReference.child("/cars").child(carRegNo);

        //see https://firebase.google.com/docs/database/android/read-and-write?authuser=0

        ValueEventListener carQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //if snapshot exists it means a car exists at carRef
                    // Get the Car object and get the posterEmail from it to check authorization
                    Car car = dataSnapshot.getValue(Car.class);
                    String posterEmail = car.posterEmail;

                    //Check if the current user the same user who posted the car ad?
                    if (posterEmail.equals(firebaseUser.getEmail())) {
                        // the current user is the same user who posted this car ad - so update
                        //get the new values from edit text and update them in the car object
                        car.carModelName = etCarModelName.getText().toString();
                        car.year = Integer.parseInt(etCarModelYear.getText().toString());
                        car.price = Double.parseDouble(etCarPrice.getText().toString());
                        //poster email is already in car object
                        //set the storageImageURL
                        car.storageImageURL = uploadedImageURL;
                        //update the car ref with the new values from the editTexts
                        carRef.setValue(car);
                        tvStatus.setText( "Car "+carRegNo+ " updated!");
                    }
                    else {
                        Toast.makeText(Main2Activity.this, "Update NOT Authorized ",
                                Toast.LENGTH_SHORT).show();
                        tvStatus.setText( "Update NOT Authorized");
                    }
                }
                else {
                    Toast.makeText(Main2Activity.this, carRegNo + " NOT found!", Toast.LENGTH_SHORT).show();
                    tvStatus.setText(carRegNo + " NOT found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("UPDATE", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        carRef.addListenerForSingleValueEvent(carQueryListener);
    }
}
