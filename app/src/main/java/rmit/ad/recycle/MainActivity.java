package rmit.ad.recycle;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;

    private Fragment photoFragment;
    private Fragment mapFragment;
    private Fragment infoFragment;
    private Fragment active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initFragment();
    }


    private final FragmentManager fm = getSupportFragmentManager();
    private void initFragment() {
        // Initialize all fragments
        photoFragment = new PhotoFragment();
        mapFragment = new MapFragment();
        infoFragment = new InfoFragment();
        active = photoFragment;

        // Add all into fragmentManager
        fm.beginTransaction().add(R.id.container, infoFragment).hide(infoFragment)
                .add(R.id.container, mapFragment).hide(mapFragment)
                .add(R.id.container, photoFragment).commit();
    }


    private void switchFragment(Fragment fragment) {
        fm.beginTransaction().hide(active).show(fragment).commit();
        active = fragment;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_photo:
                    switchFragment(photoFragment);
                    return true;
                case R.id.nav_map:
                    switchFragment(mapFragment);
                    return true;
                case R.id.nav_info:
                    switchFragment(infoFragment);
                    return true;
            }
            return false;
        }
    };


    public void createSite() {
        Site site = new Site(new GeoPoint(10.782359, 106.682129));
        site.setTitle("UBND Phường 9, Q3");
        site.getTypes().add("pin");
        site.getTypes().add("dientu");


        Site site1 = new Site(new GeoPoint(10.754351, 106.708079));
        site1.setTitle("UBND Phường 15, Q4");
        site1.getTypes().add("pin");

        Site site2 = new Site(new GeoPoint(10.793020, 106.680954));
        site2.setTitle("UBND Phường 17, Q Phú Nhuận");
        site2.getTypes().add("dientu");

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("sites").add(site);
    }

}


//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
