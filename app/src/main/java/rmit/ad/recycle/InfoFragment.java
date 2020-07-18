package rmit.ad.recycle;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class InfoFragment extends Fragment {
    ViewPager viewPager;
    Adapter adapter;
    List<Item> items;
    LinearLayout sliderDots;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        items = new ArrayList<>();
        items.add(new Item(TrashType.VOCO.image, TrashType.VOCO.name, TrashType.VOCO.info));
        items.add(new Item(TrashType.HUUCO.image, TrashType.HUUCO.name, TrashType.HUUCO.info));
        items.add(new Item(TrashType.TAICHE.image, TrashType.TAICHE.name, TrashType.TAICHE.info));
        items.add(new Item(TrashType.DIENTU.image, TrashType.DIENTU.name, TrashType.DIENTU.info));
        items.add(new Item(TrashType.PIN.image, TrashType.PIN.name, TrashType.PIN.info));

        adapter = new Adapter(items, view.getContext());
        sliderDots = view.findViewById(R.id.sliderDots);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        imageSlideShow(view, viewPager);
        return view;
    }
    private void imageSlideShow(final View view, ViewPager viewPager) {

        sliderDots.setBackgroundColor(Color.TRANSPARENT);
        final int dotCounter = adapter.getCount();
        final ImageView[] dots = new ImageView[dotCounter];
        for (int i = 0; i < dotCounter; i++) {
            dots[i] = new ImageView(view.getContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            if (sliderDots.getChildCount() < dotCounter) {
                sliderDots.addView(dots[i], params);
            }
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.active_dot));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotCounter; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.nonactive_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
