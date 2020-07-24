package rmit.ad.recycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment  implements OnMapReadyCallback {

    private MapView mapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 10;
    private static final LatLng mDefaultLocation = new LatLng(10.730918, 106.693402);

    private GoogleMap mMap;
    //
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // If user allow to access location
    private boolean allowAccessLocation;

    private Location lastKnownLocation;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container,false);
        //Button button = view.findViewById(R.id.pin);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), MapsActivity.class);
//                intent.putExtra("type", "pin");
//                startActivity(intent);
//            }
//        });

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mapView = view.findViewById(R.id.map_view);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        initGoogleMap(savedInstanceState);
        return view;
    }
    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private ClusterManager<Site> mClusterManager;

    private List<Site> allSites = new ArrayList<>();
    //private boolean isBattery;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!allowAccessLocation) {
            lastKnownLocation = null;
            getLocationPermission();
        }
        mMap.setPadding(0, 30, 0, 0);

        mClusterManager = new ClusterManager<>(getActivity(), mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new CustomMarker(getContext(), mMap, mClusterManager));
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
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {}
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
                getFragmentManager().beginTransaction().replace(R.id.map_detail_layout, mapDetailFragment).commit();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    for (Fragment fragment: getFragmentManager().getFragments()) {
                        if (fragment instanceof MapDetailFragment) {
                            getFragmentManager().beginTransaction().remove(fragment).commit();
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
//        String str = isBattery ? "pin" : "dientu";
        for (Site site : allSites) {
            //if (site.getTypes().contains(str)) {
                mClusterManager.addItem(site);
                mClusterManager.cluster();
                list.add(site.getPosition());
            //}
        }
        setAnimateCamera(list);
    }

    private void moveMyLocationButton() {
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 220, 30, 0);

            View toolBar = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("4"));

            // and next place it, for example, on bottom right (as Google Maps app)
            rlp = (RelativeLayout.LayoutParams) toolBar.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 370, 30, 0);
        }

    }

    public MapDetailFragment.DirectionOnClickListener onDirectionClick = new MapDetailFragment.DirectionOnClickListener() {
        @Override
        public void directionClick(Site site, MapDetailFragment fragment) {

            if (lastKnownLocation != null) {

                clearCluster();
                // add site
                mClusterManager.addItem(site);

                // Hide bottom detail
                getFragmentManager().beginTransaction().remove(fragment).commit();

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
                getFragmentManager().beginTransaction().replace(R.id.direction_detail_layout, directionFragment).commit();

            } else {
                Toast.makeText(getActivity(), "Please allow to access location", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private DirectionFragment.OnBackButtonClick onHideDirection = new DirectionFragment.OnBackButtonClick() {
        @Override
        public void onHideDirection(DirectionFragment fragment) {
            // Hide top fragment
            getFragmentManager().beginTransaction().remove(fragment).commit();

            // Clear poly line
            fetchUrl.clearPolyline();

            // add clusters
            addCluster();
        }
    };


    private void clearView () {
        try {
            for (Fragment fragment: getFragmentManager().getFragments()) {
                if (fragment instanceof DirectionFragment || fragment instanceof MapDetailFragment) {
                    getFragmentManager().beginTransaction().remove(fragment).commit();
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


        if (ContextCompat.checkSelfPermission(getContext(),
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

            moveMyLocationButton();
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
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
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

        private final IconGenerator iconGenerator = new IconGenerator(getContext());

        CustomMarker(Context context, GoogleMap map, ClusterManager<Site> clusterManager) {
            super(context, map, clusterManager);
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.ic_plant);
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
        TextView title, address;
        Button direction;

        private Site site;

        public MapDetailFragment(Site site, MapDetailFragment.DirectionOnClickListener directionOnClickListener) {
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
            StringBuilder str = new StringBuilder("Thu nhập  ");
            for (String s : site.getTypes()) {
                if(s.equals("dientu")) str.append("điện tử"). append(" ");
                else str.append(s).append(" ");
            }
            address.setText(str.toString());
            direction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { directionOnClickListener.directionClick(site, MapDetailFragment.this);
                }
            });
        }

        private MapDetailFragment.DirectionOnClickListener directionOnClickListener;

        public interface DirectionOnClickListener {
            void directionClick(Site site, MapDetailFragment fragment);
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
