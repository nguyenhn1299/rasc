package rmit.ad.recycle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener{
    private static final String[] items = {"chai nuoc","vo lon","giay","bia","chai bia","bao nilon"};
    private RecyclerView recyclerView;
    private EditText inputSearch;
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        recyclerView = findViewById(R.id.recyclerView);

        initButtons();
        initSearchView();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(Arrays.asList(items), this);
        recyclerView.setAdapter(searchAdapter);
    }

    private void initSearchView() {
        inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(SearchActivity.this, v);
                    handled = true;
                }
                return handled;
            }
        });

        inputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(SearchActivity.this, v);
                }
            }
        });
    }

    private void initButtons() {
        final ImageButton finish = findViewById(R.id.backBtn);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button clearText = findViewById(R.id.clearText);
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputSearch.setText("");
                hideKeyboard(SearchActivity.this, v);
            }
        });

    }
    public static void hideKeyboard(Activity activity, View view) {
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

        } catch (NullPointerException e) {}
    }


    @Override
    public void onItemClick(String item) {
        Intent intent = new Intent(SearchActivity.this, CategorizeActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
        finish();
    }

}