package au.edu.unimelb.student.group55.my_ins.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

import au.edu.unimelb.student.group55.my_ins.Firebase.FirebaseMethods;
import au.edu.unimelb.student.group55.my_ins.Firebase.UserAccountSetting;
import au.edu.unimelb.student.group55.my_ins.PhotoNGallery.ApplyFilters;
import au.edu.unimelb.student.group55.my_ins.Home.HomeActivity;
import au.edu.unimelb.student.group55.my_ins.LoginNRegister.LoginActivity;
import au.edu.unimelb.student.group55.my_ins.PhotoNGallery.ApplyFilters;
import au.edu.unimelb.student.group55.my_ins.PhotoNGallery.PhotoUploadService;
import au.edu.unimelb.student.group55.my_ins.R;
import au.edu.unimelb.student.group55.my_ins.Utils.ImageManager;
import au.edu.unimelb.student.group55.my_ins.Utils.UniversalImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfile Activity";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseMethods firebaseMethods;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;


    private Context context;
    private String uid;

    private EditText description,displayName, username,phoneNum;
    private CircleImageView profilePic;
    private TextView changeProfilePic;
    private TextView cancel;
    private  TextView done;


    private UserAccountSetting userAccountSetting;

    private StorageReference storageReference;
    private FirebaseStorage storage;
    private FirebaseUser user;

    private Task<Uri> downloadUri;
    private String downloadLink;
    private double mPhotoUploadProgress = 0;

    private String IMAGE_PATH;

    private Bitmap resultImageBitmap;
    private ByteArrayOutputStream baos;
    private byte[] imageData;







    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        Log.d(TAG, "OnCreate: Started");

        profilePic = (CircleImageView) findViewById(R.id.profile_pic);
        description = (EditText) findViewById(R.id.description);
        username = (EditText)findViewById(R.id.username);
        displayName = (EditText)findViewById(R.id.display_name);
        phoneNum = (EditText)findViewById(R.id.phone_num);
        changeProfilePic = (TextView)findViewById(R.id.change_profile_pic);
        context = EditProfileActivity.this;
        firebaseMethods = new FirebaseMethods(context);
        changeProfilePic = (TextView)findViewById(R.id.change_profile_pic);
        baos = new ByteArrayOutputStream();
        storage = FirebaseStorage.getInstance();

        FirebaseAuth();


//        click cancel to go back to profile page
        cancel = (TextView) findViewById(R.id.edit_profile_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked cancel");
                EditProfileActivity.this.finish();
            }
        });

        done = (TextView) findViewById(R.id.edit_profile_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"profile settings changed: clicked done!");
                saveProfileSettings();
            }
        });

        getIncomingIntent();



    }

    private void setUpSettingList() {
        Log.d(TAG, "set up edit profile setting list");

    }


    private void setProfile(UserAccountSetting userAccountSetting) {
        UniversalImageLoader.setImage(userAccountSetting.getProfile_pic(), profilePic, null, "");
        displayName.setText(userAccountSetting.getDisplay_name());
        username.setText(userAccountSetting.getUsername());
        description.setText(userAccountSetting.getDescription());
        phoneNum.setText(String.valueOf(userAccountSetting.getPhone_number()));
        System.out.println(String.valueOf(userAccountSetting.getPhone_number()));
        this.userAccountSetting = userAccountSetting;

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"changing profile picture");
                Intent intent = new Intent(context, ApplyFilters.class);
//                set a none-zero flag to differentiate post and chang profile methods
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }


    private void saveProfileSettings(){
        final String displayName = this.displayName.getText().toString();
        final String username = this.username.getText().toString();
        final String description = this.description.getText().toString();
        final long phoneNum = Long.parseLong(this.phoneNum.getText().toString());


        if(!userAccountSetting.getUsername().equals(username)){
//            only if new username unique, we can update all information
            checkUsername(username,displayName,description,phoneNum);
        }else{
//           username is not changed, so we can update other information
            if(!userAccountSetting.getDisplay_name().equals(displayName)){
                firebaseMethods.updateDisplayName(displayName);
            }
            if(!userAccountSetting.getDescription().equals(description)){
                firebaseMethods.updateDescription(description);
            }
            if(userAccountSetting.getPhone_number()!=(phoneNum)){
                firebaseMethods.updatePhoneNum(phoneNum);
            }
            Toast.makeText(context, "saved", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }


//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = new User();
//                for(DataSnapshot ds:dataSnapshot.child("users").getChildren()){
//                    if(ds.getKey().equals(uid)){
//
//                    }
//
//                };
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }


//    check id username unique, if yes, update
    private void checkUsername(final String username,final String displayName, final String description, final long phoneNum) {
        Log.d(TAG, "checkIfUsernameExists: Checking if  " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .orderByChild("username")
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    //add the username
                    firebaseMethods.updateUsername(username);
                    if(!userAccountSetting.getDisplay_name().equals(displayName)){
                        firebaseMethods.updateDisplayName(displayName);
                    }

                    if(!userAccountSetting.getDescription().equals(description)){
                        firebaseMethods.updateDescription(description);
                    }

                    if(userAccountSetting.getPhone_number()!=(phoneNum)){
                        firebaseMethods.updatePhoneNum(phoneNum);
                    }

                    Toast.makeText(context, "saved", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
//                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(context, "This username is not available, please try again.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void uploadProfilePic(final String imgUrl,
                                  Bitmap bm){
        Log.d(TAG, "uploadProfilePic: attempting to uplaod new profile pic.");

        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

//        FilePaths filePaths = new FilePaths();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = storage.getReference().
                child( uid ).child(currentDate + ".jpg");

        //convert image url to bitmap
        if(bm == null){
            bm = ImageManager.getBitmap(imgUrl);
        }
        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

        UploadTask uploadTask = null;
        uploadTask = storageReference.putBytes(bytes);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUri = storageReference.getDownloadUrl();
                downloadLink = downloadUri.toString();

                Toast.makeText(context, "photo upload success", Toast.LENGTH_SHORT).show();

                //insert into 'user_account_settings' node
                setProfilePhoto(downloadLink);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Photo upload failed.");
                Toast.makeText(context, "Photo upload failed ", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                if(progress - 15 > mPhotoUploadProgress){
                    Toast.makeText(context, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    mPhotoUploadProgress = progress;
                }

                Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
            }
        });
    }


    private void getIncomingIntent(){
        Intent intent = getIntent();

        if(intent.hasExtra("profilePicPath")){
            //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl");
            //set the new profile picture
            IMAGE_PATH = intent.getStringExtra( "profilePicPath" );
            resultImageBitmap = readImage( IMAGE_PATH );
            resultImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);


            uploadProfilePic(null,resultImageBitmap);
        }
            }

//            update profile pic url to db
    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        databaseReference.child("user_account_settings")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile_photo")
                .setValue(url);
    }

    public Bitmap readImage(String imagePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        System.out.println("11111111111111111: " + imagePath);

        return bitmap;
    }









    /**
     * Setup the firebase auth object
     */
    private void FirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        uid = auth.getCurrentUser().getUid();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setProfile(firebaseMethods.getUserSetting(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


}


