package rmit.ad.recycle;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchML extends AsyncTask<Object, String, String> {

    private static final String URL = "";
    HttpURLConnection httpURLConnection = null;
    String data = "";
    InputStream inputStream = null;
    Intent intent;
    PhotoFragment photoFragment;
    @Override
    protected String doInBackground(Object... objects) {
        byte[] capturedImage = (byte[]) objects[0];

        intent = (Intent) objects[1];
        photoFragment = (PhotoFragment) objects[2];
        try {
            URL myUrl = new URL(URL + capturedImage);

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

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject jsonObject = new JSONObject(s);
            String result = jsonObject.getString("result");
            intent.putExtra("type", result);
            photoFragment.startActivity(intent);
        } catch (JSONException e) {
            Toast.makeText(photoFragment.getActivity(), "Can not connect to server", Toast.LENGTH_SHORT).show();
        }
    }
}
