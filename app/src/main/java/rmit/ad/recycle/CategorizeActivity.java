package rmit.ad.recycle;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class CategorizeActivity extends AppCompatActivity {

    private ImageView type;
    private TextView name;
    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorize);
        ImageView imageView = findViewById(R.id.image);
        type = findViewById(R.id.type);
        name = findViewById(R.id.name);
        info = findViewById(R.id.info);

        mappingInfo("cardboard");

        try {
            String path = getIntent().getStringExtra("photo");
            File imgFile = new File(path);

            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);

            }
        } catch (Exception e) {}
    }



    public TrashType wasteSorting(String type) {
        if (type.equals("cardboard") || type.equals("plastic") || type.equals("glass") || type.equals("metal")) {
            return TrashType.TAICHE;
        }
        return TrashType.HUUCO;
    }

    public void mappingInfo(String type) {
        TrashType trashType = wasteSorting(type);
        Bitmap myBitmap = BitmapFactory.decodeFile(trashType.image);
        this.type.setImageBitmap(myBitmap);
        name.setText(trashType.name);
        info.setText(trashType.info);
    }
}
