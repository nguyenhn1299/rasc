package rmit.ad.recycle;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.MenuItem;

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
        Site site = new Site(new GeoPoint(10.804103, 106.700381));
        site.setTitle(" UBND Phường 2, Quận Bình Thạnh");
        site.getTypes().add("pin");
        site.getTypes().add("dientu");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sites").add(site);
    }

}