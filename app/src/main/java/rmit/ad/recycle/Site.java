package rmit.ad.recycle;

import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Site implements Serializable, ClusterItem {
    private String title;
    private List<String> types = new ArrayList<>();
    private String address;
    private GeoPoint geoPoint;
    public Site() {}

    public Site(GeoPoint geoPoint) {
        this.geoPoint= geoPoint;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
