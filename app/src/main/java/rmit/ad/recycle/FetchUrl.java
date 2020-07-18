package rmit.ad.recycle;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchUrl extends AsyncTask<Object, String, String> {

    HttpURLConnection httpURLConnection = null;
    String data = "";
    InputStream inputStream = null;


    private GoogleMap mMap;
    private LatLng origin;
    private LatLng dest;
    private String apiKey;

    @Override
    protected String doInBackground(Object... objects) {

        mMap = (GoogleMap) objects[0];
        origin = (LatLng) objects[1];
        dest = (LatLng) objects[2];
        apiKey = (String) objects[3];

        try {
            URL myUrl = new URL(getUrl());

            httpURLConnection = (HttpURLConnection) myUrl.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            Log.d("nane", "onPostExecute: " + jsonObject.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0)
                    .getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

            int count = jsonArray.length();
            String[] polyline_array = new String[count];

            JSONObject jsonObject1;
            for (int i = 0; i < count; i++) {
                jsonObject1 = jsonArray.getJSONObject(i);

                String polygone = jsonObject1.getJSONObject("polyline").getString("points");

                polyline_array[i] = polygone;
            }

            int count1 = polyline_array.length;

            for (int i = 0; i < count1; i++) {
                PolylineOptions options = new PolylineOptions();
                options.color(Color.BLUE);
                options.width(10);
                options.addAll(PolyUtil.decode(polyline_array[i]));

                Polyline polyline = mMap.addPolyline(options);
                polylineList.add(polyline);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<Polyline> polylineList = new ArrayList<>();

    private String getUrl() {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," +dest.longitude;

        String str_apiKey = "key=" + apiKey;

        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + str_origin + "&" + str_dest + "&" + "mode=driving" + "&" + str_apiKey;
        Log.d("NENE", "getUrl: " + str_apiKey);

        Log.d("Nane", "getUrl: " + url);
        return url;
    }

    public void clearPolyline() {
        for (Polyline polyline: polylineList) {
            polyline.remove();
        }
        polylineList.clear();
        mMap.setPadding(0,30,0,0);
    }
}