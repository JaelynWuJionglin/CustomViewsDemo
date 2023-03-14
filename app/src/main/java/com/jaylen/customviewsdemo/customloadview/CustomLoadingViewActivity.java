package com.jaylen.customviewsdemo.customloadview;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import com.custom.loading.CustomLoadingView;
import com.jaylen.customviewsdemo.R;
import com.jaylen.customviewsdemo.ui.BaseActivity;

/**
 * Created by Jack Wang on 2016/8/5.
 */

public class CustomLoadingViewActivity extends BaseActivity {

    private CustomLoadingView avi;

    public CustomLoadingViewActivity() {
        super(R.layout.activity_custom_loading_view);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String indicator = getIntent().getStringExtra("indicator");
        avi = findViewById(R.id.avi);
        avi.setIndicator(indicator);
    }

    public void hideClick(View view) {
        avi.hide();
        // or avi.smoothToHide();
    }

    public void showClick(View view) {
        avi.show();
        // or avi.smoothToShow();
    }
}
