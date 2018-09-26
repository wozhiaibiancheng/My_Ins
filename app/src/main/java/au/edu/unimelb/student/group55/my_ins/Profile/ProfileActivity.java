package au.edu.unimelb.student.group55.my_ins.Profile;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import au.edu.unimelb.student.group55.my_ins.LoginNRegister.LoginActivity;
import au.edu.unimelb.student.group55.my_ins.LoginNRegister.RegisterActivity;
import au.edu.unimelb.student.group55.my_ins.R;
import au.edu.unimelb.student.group55.my_ins.Utils.ImageAdapter;
import au.edu.unimelb.student.group55.my_ins.Utils.UniversalImageLoader;
import au.edu.unimelb.student.group55.my_ins.Utils.bottomNavTool;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "Profile Activity";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView editProfile;
    private ProgressBar progressBar;
    private ImageView profilePic;
    private Context context = ProfileActivity.this;
    private static final int numColumns = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d("INFO","onCreate started!");

        setUpToolbar();
        setBottom();
        setUpEditProfile();
        setupActivityWidgets();
        setProfilePic();
        temGridSetup();
    }


//    set up toolbar
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        FirebaseAuth();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG,"clicked menu icon"+ item);
                switch (item.getItemId()){
                    case R.id.profile_menu:
                        Log.d(TAG,"on menuItem click");
                        auth.signOut();
                        finish();
                }
                return false;
            }
        });
    }


    //    set up bottom view
    private void setBottom(){
        Log.d(TAG,"bottom view setting");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom);
        bottomNavTool.setBottomNav(bottomNavigationViewEx);
        bottomNavTool.enableNav(ProfileActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }



    /**
     * Setup the firebase auth object
     */
    private void FirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());}
                else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                // ...
            }
        };
    }

    //  set up edit_profile button
    private void setUpEditProfile() {
        editProfile = (TextView) findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View item) {
                Log.d(TAG,"clicked edit profile");
                Intent intent = new Intent(ProfileActivity.this,EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }


    private void setupActivityWidgets(){
        progressBar = (ProgressBar)findViewById(R.id.profileProgressBar);
//        progressBar.setVisibility(View.GONE);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
    }

    private void setProfilePic() {
        Log.d(TAG, "set profile pic");
        String imgURL = "https://artinsights.com/wp-content/uploads/2013/11/20120919143022.jpg";
        UniversalImageLoader.setImage(imgURL, profilePic, null, "");
    }

    private void setImageGrid(ArrayList<String> imgURLs){
        GridView imgGrid = (GridView)findViewById(R.id.image_grid);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgWidth = screenWidth/numColumns;
        imgGrid.setColumnWidth(imgWidth);


        ImageAdapter imageAdapter = new ImageAdapter(context,R.layout.image_grid,"",imgURLs);
        imgGrid.setAdapter(imageAdapter);

    }

    private void temGridSetup(){
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz1WudxjK_akg8ZwryyxpzLzDNodquERTqGmPFqFNRcu5pNA-EVw");
        imgURLs.add("https://frontiersinblog.files.wordpress.com/2018/02/psychology-influence-behavior-with-images.jpg?w=940");
        imgURLs.add("https://secure.i.telegraph.co.uk/multimedia/archive/03290/kitten_potd_3290498k.jpg");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz1WudxjK_akg8ZwryyxpzLzDNodquERTqGmPFqFNRcu5pNA-EVw");
        imgURLs.add("https://vignette.wikia.nocookie.net/parody/images/e/ef/Alice-PNG-alice-in-wonderland-33923432-585-800.png/revision/latest?cb=20141029225915");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz1WudxjK_akg8ZwryyxpzLzDNodquERTqGmPFqFNRcu5pNA-EVw");

        setImageGrid(imgURLs);
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


