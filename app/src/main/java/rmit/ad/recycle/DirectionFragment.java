package rmit.ad.recycle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DirectionFragment extends Fragment {

    ImageButton backBtn;
    TextView destination;
    String title;

    public DirectionFragment(OnBackButtonClick onBackButtonClick, String title) {
        this.onBackButtonClick = onBackButtonClick;
        this.title = title;
    }

    private OnBackButtonClick onBackButtonClick;

    public interface OnBackButtonClick {
        void onHideDirection(DirectionFragment fragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direction_detail, container,false);
        backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonClick.onHideDirection(DirectionFragment.this);
            }
        });

        destination = view.findViewById(R.id.dest);
        destination.setText(title);
        return view;
    }
}
