package rmit.ad.recycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        checkCameraPermission();
        Button takePhotoBtn = view.findViewById(R.id.takePhotoBtn);
        takePhotoBtn.setOnClickListener(photoOnClickListener);
        // From button OnClickListener

        return view;
    }

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("NANA", "onClick: ");
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                    Bitmap bmp=BitmapFactory.decodeByteArray(capturedImage,0,capturedImage.length);
                    String str = tempFileImage(getContext(), bmp, "photo");
                    classifyObject(str);
                }
            });
        }
    };

    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {;
        }

        return imageFile.getAbsolutePath();
    }


    public void classifyObject(String photo) {
        Intent intent = new Intent(getActivity(), CategorizeActivity.class);
        intent.putExtra("photo", photo);
        startActivity(intent);
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

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ALLOW_READ_EXTERNAL);
        } else {
            openFileChooser();
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
        if (requestCode == ALLOW_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
            } else {
                Toast.makeText(getActivity(), "Please allow access camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

}