package rmit.ad.recycle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

public class CategorizeActivity extends AppCompatActivity {

    private ImageView image;
    private TextView name;
    private TextView desc;
    private Button info;
    private EditText search;
    private TextView sub;
    private RelativeLayout layout;
    private String subResult;
    private String result;
    private int resultInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorize);

        image = findViewById(R.id.image);
        desc = findViewById(R.id.desc);
        name = findViewById(R.id.name);
        info = findViewById(R.id.button);
        sub = findViewById(R.id.sub);
        search = findViewById(R.id.inputSearch);
        layout = findViewById(R.id.searchLayout);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategorizeActivity.this, SearchActivity.class));
                finish();
            }
        });



        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        ImageButton finish = findViewById(R.id.closeBtn);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        try {
            subResult = (String) intent.getExtras().get("item");
        } catch (Exception e) {}

        if (subResult == null){
            //FOR TESTING
            result = (String) intent.getExtras().get("type");

            String str;
            if (result.equals("plastic")) str = "Nhựa";
            else if (result.equals("metal")) str = "Kim loại";
            else if (result.equals("cardboard")) str = "Bìa ni lông";
            else if (result.equals("trash")) str = "Rác không phân hủy";
            else if (result.equals("glass")) str = "Thủy tinh";
            else str = "Giấy";
            name.setText(str);
            if(result.equals("none")){
                name.setText("Chưa thể nhận dạng");
                desc.setText("Hãy dùng công cụ tìm kiếm bên dưới nhé.");
                info.setVisibility(View.GONE);
                image.setImageResource(R.drawable.not);
            }else wasteSorting(result);
        }else initResult();

    }

    public void initResult(){
        name.setText(subResult);
        if (subResult.equals("Chai bia") || subResult.equals("Bao nilon")){
            wasteSorting("glass");
        }else wasteSorting("plastic");
    }

    public void wasteSorting(String type) {
        sub.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
        if (type.equals("cardboard") || type.equals("plastic") || type.equals("paper") || type.equals("metal")) {
            desc.setText("Hãy bỏ vào thùng tái chế");
            info.setText("Thông tin thêm");
            image.setImageResource(R.drawable.taiche);
            resultInt = 1;
        }else {
            desc.setText("Hãy bỏ vào thùng vô cơ");
            info.setText("Thông tin thêm");
            image.setImageResource(R.drawable.voco);
            resultInt = 0;
        }
    }

    public void openDialog(){
        final AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.confirm_logout_dialog, null);
        Button yes = mView.findViewById(R.id.yes_button);
        TextView info = mView.findViewById(R.id.info);
        if (resultInt == 1){
            info.setText(TrashType.TAICHE.info);
        } else info.setText(TrashType.VOCO.info);
        confirm.setView(mView);
        final AlertDialog alertDialog = confirm.create();
        alertDialog.setCanceledOnTouchOutside(false);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

}
