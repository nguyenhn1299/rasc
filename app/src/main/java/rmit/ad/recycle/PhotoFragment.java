package rmit.ad.recycle;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;


public class PhotoFragment extends Fragment {

    private CameraKitView cameraKitView;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        ImagePicker.Companion.with(this)
//                .compress(1024)         //Final image size will be less than 1 MB(Optional)
//                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
//                .start();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        cameraKitView = view.findViewById(R.id.camera);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        takePhotoBtn = view.findViewById(R.id.takePhotoBtn);
        takePhotoBtn.setOnClickListener(photoOnClickListener);
        progressBar = view.findViewById(R.id.progressBar);
        viewHide = view.findViewById(R.id.viewHide);
        // From button OnClickListener
        //checkPermission();
        checkCameraPermission();

        return view;
    }

    ProgressBar progressBar;
    Intent intent;
    Button takePhotoBtn;

    View viewHide;
    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                    takePhotoBtn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    viewHide.setVisibility(View.VISIBLE);
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                ALLOW_WRITE_EXTERNAL);
                        return;
                    }
                    Bitmap bmp=BitmapFactory.decodeByteArray(capturedImage,0,capturedImage.length);
                    Bitmap scaleBmp = Bitmap.createScaledBitmap(bmp, 224, 224, false);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    scaleBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    String str = tempFileImage(getContext(), bmp, "photo");
                    intent = new Intent(getActivity(), CategorizeActivity.class);
                    intent.putExtra("photo", str);
                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    Uri tempUri = getImageUri(getContext(), scaleBmp);
                    Log.d("jjj", "onImage: " + tempUri.toString());
                    uploadImage(tempUri);

                }
            });
        }
    };

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
        }

        return imageFile.getAbsolutePath();
    }

    private StorageReference storageReference;
    private StorageTask storageTask;
    public void uploadImage(Uri imageUri) {
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        storageTask = fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                JsonPostRequest jsonPostRequest = new JsonPostRequest(url);
                                jsonPostRequest.execute();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        viewHide.setVisibility(View.INVISIBLE);
                        takePhotoBtn.setEnabled(true);
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    public void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {

        cameraKitView.onStop();
        super.onStop();
    }

    private final static int ALLOW_READ_EXTERNAL = 1;
    private final static int ALLOW_CAMERA = 2;
    private final static int ALLOW_WRITE_EXTERNAL = 1;

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ALLOW_WRITE_EXTERNAL);
        } else {

        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    ALLOW_READ_EXTERNAL);
        } else {
            openFileChooser();
        }
    }

    private void openFileChooser() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(i, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class JsonPostRequest extends AsyncTask<Void, Void, String> {

        String uri;
        JsonPostRequest(String uri) {
            this.uri = uri;
        }
        String TAG = "jjj";

        @Override
        protected String doInBackground(Void... voids) {
            String status = "";
            StringBuilder result = new StringBuilder();

            try {
                String address = "https://a96558b064b9.ngrok.io";
                JSONObject json = new JSONObject();

                json.put("photo", uri);

                String requestBody = json.toString();
                Log.d(TAG, "doInBackground: " + requestBody);
                URL url = new URL(address);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                try(OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    status = urlConnection.getResponseMessage();
                    Log.d(TAG, "mess: " + urlConnection.getResponseCode() + urlConnection.getResponseMessage());
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    os.flush();
                }



            } catch (MalformedURLException e) {
                return e.toString();
            } catch (ProtocolException e){return e.toString();}
            catch (IOException e) { return  e.toString();}
            catch ( JSONException e) {
                return e.toString();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            viewHide.setVisibility(View.INVISIBLE);
            takePhotoBtn.setEnabled(true);
            intent.putExtra("type", s);
            startActivity(intent);
        }
    }

}