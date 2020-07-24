package rmit.ad.recycle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ClusterManager<Site> mClusterManager;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private boolean allowAccessLocation;
    private Location lastKnownLocation;


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private static final LatLng mDefaultLocation = new LatLng(10.730918, 106.693402);
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private List<Site> allSites = new ArrayList<>();
    private boolean isBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!allowAccessLocation) {
            lastKnownLocation = null;
            getLocationPermission();
        }
        mMap.setPadding(0, 30, 0, 0);

        mClusterManager = new ClusterManager<>(this, mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new CustomMarker(this, mMap, mClusterManager));
        setClusterListener();

        fetchAllSites();
    }


    private void fetchAllSites() {
        Query query = FirebaseFirestore.getInstance().collection("sites");
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Site site = document.toObject(Site.class);
                    allSites.add(site);
                }
                addCluster();
            }
        });
    }



    private void setAnimateCamera(List<LatLng> list) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (LatLng latLng : list) {
            builder.include(latLng);
        }
        if (lastKnownLocation != null) builder.include(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()));
        final LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
    }


    private void setClusterListener() {
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Site>() {
            @Override
            public boolean onClusterClick(Cluster<Site> cluster) {
                clearView();
                List<LatLng> list = new ArrayList<>();
                for (ClusterItem item : cluster.getItems()) {
                    list.add(item.getPosition());
                }
                setAnimateCamera(list);
                return true;
            }
        });


        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Site>() {
            @Override
            public boolean onClusterItemClick(Site site) {
                MapDetailFragment mapDetailFragment = new MapDetailFragment(site, onDirectionClick);
                getSupportFragmentManager().beginTransaction().replace(R.id.map_detail_layout, mapDetailFragment).commit();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    for (Fragment fragment: getSupportFragmentManager().getFragments()) {
                        if (fragment instanceof MapDetailFragment) {
                            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        }
                    }
                } catch (Exception e) {}
            }
        });

    }
    private FetchUrl fetchUrl;

    private void clearCluster() {
        for (Site site: allSites) {
            mClusterManager.removeItem(site);
            mClusterManager.cluster();
        }
    }

    private void addCluster() {
        List<LatLng> list = new ArrayList<>();
        String str = isBattery ? "pin" : "dientu";
        for (Site site : allSites) {
            if (site.getTypes().contains(str)) {
                mClusterManager.addItem(site);
                mClusterManager.cluster();
                list.add(site.getPosition());
            }
        }
        setAnimateCamera(list);
    }

    public MapDetailFragment.DirectionOnClickListener onDirectionClick = new MapDetailFragment.DirectionOnClickListener() {
        @Override
        public void directionClick(Site site, MapDetailFragment fragment) {

            if (lastKnownLocation != null) {

                clearCluster();
                // add site
                mClusterManager.addItem(site);

                // Hide bottom detail
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                // Fetch direction data
                fetchUrl = new FetchUrl();
                Object[] dataTransfer = new Object[4];
                dataTransfer[0] = mMap;
                LatLng position = new LatLng(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());
                dataTransfer[1] = position;
                dataTransfer[2] = site.getPosition();
                dataTransfer[3] = getString(R.string.google_maps_key);


                fetchUrl.execute(dataTransfer);

                List<LatLng> list = new ArrayList<>();
                list.add(position);
                list.add(site.getPosition());
                mMap.setPadding(20,220,20,20);
                setAnimateCamera(list);

                // Show top detail
                DirectionFragment directionFragment = new DirectionFragment(onHideDirection, site.getTitle());
                getSupportFragmentManager().beginTransaction().replace(R.id.direction_detail_layout, directionFragment).commit();

            } else {
                Toast.makeText(getApplicationContext(), "Please allow to access location", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private DirectionFragment.OnBackButtonClick onHideDirection = new DirectionFragment.OnBackButtonClick() {
        @Override
        public void onHideDirection(DirectionFragment fragment) {
            // Hide top fragment
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

            // Clear poly line
            fetchUrl.clearPolyline();

            // add clusters
            addCluster();
        }
    };


    private void clearView () {
        try {
            for (Fragment fragment: getSupportFragmentManager().getFragments()) {
                if (fragment instanceof DirectionFragment || fragment instanceof MapDetailFragment) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        } catch (Exception e) {}
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ) {
            allowAccessLocation = true;
            updateLocationUI();
        } else  {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d("Nane", "onRequestPermissionsResult: update UI");

        allowAccessLocation = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    allowAccessLocation = true;
                }
            }
        }

        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(allowAccessLocation);
            mMap.getUiSettings().setMyLocationButtonEnabled(allowAccessLocation);

            if (allowAccessLocation) {
                getDeviceLocation();
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() instanceof Location) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = (Location) task.getResult();
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });

        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private class CustomMarker extends DefaultClusterRenderer<Site> {

        private final IconGenerator iconGenerator = new IconGenerator(getApplicationContext());

        CustomMarker(Context context, GoogleMap map, ClusterManager<Site> clusterManager) {
            super(context, map, clusterManager);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(R.drawable.ic_location_on_red_24dp);
            iconGenerator.setContentView(imageView);
            Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);
            iconGenerator.setBackground(TRANSPARENT_DRAWABLE);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<Site> cluster) {
            return cluster.getSize() >= 2;
        }

        @Override
        protected void onBeforeClusterItemRendered(Site item, MarkerOptions markerOptions) {
            Bitmap icon = iconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public static class MapDetailFragment extends Fragment {

        ImageView avatar;
        TextView title, time, date, address, user_name;
        Button direction, showDetail;

        private Site site;

        public MapDetailFragment(Site site, DirectionOnClickListener directionOnClickListener) {
            this.site = site;
            this.directionOnClickListener = directionOnClickListener;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_map_detail, container,false);
            mappingView(view);
            displayData();
            return view;
        }

        public void mappingView(View view) {

            title = view.findViewById(R.id.title_detail);


            address = view.findViewById(R.id.address_detail);
            direction = view.findViewById(R.id.directionBtn);

        }

        public void displayData() {
            title.setText(site.getTitle());

            direction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    directionOnClickListener.directionClick(site, MapDetailFragment.this);
                }
            });
        }

        private DirectionOnClickListener directionOnClickListener;

        public interface DirectionOnClickListener {
            void directionClick(Site site, MapDetailFragment fragment);
        }
    }
}
