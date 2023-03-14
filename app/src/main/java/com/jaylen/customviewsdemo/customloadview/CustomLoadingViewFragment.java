package com.jaylen.customviewsdemo.customloadview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.custom.loading.CustomLoadingView;
import com.jaylen.customviewsdemo.R;
import com.jaylen.customviewsdemo.ui.BaseFragment;

/**
 * Created by Jack Wang on 2016/8/5.
 */

public class CustomLoadingViewFragment extends BaseFragment {

    public CustomLoadingViewFragment() {
        super(R.layout.fragment_custom_loading_view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecycler = view.findViewById(R.id.recycler);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.setAdapter(new RecyclerView.Adapter<IndicatorHolder>() {
            @NonNull
            @Override
            public IndicatorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = getLayoutInflater().inflate(R.layout.item_indicator, parent, false);
                return new IndicatorHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull IndicatorHolder holder, @SuppressLint("RecyclerView") final int position) {
                holder.indicatorView.setIndicator(INDICATORS[position]);
                holder.itemLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), CustomLoadingViewActivity.class);
                    intent.putExtra("indicator", INDICATORS[position]);
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return INDICATORS.length;
            }
        });
    }

    final static class IndicatorHolder extends RecyclerView.ViewHolder {

        public CustomLoadingView indicatorView;
        public View itemLayout;

        public IndicatorHolder(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            indicatorView = itemView.findViewById(R.id.indicator);
        }
    }


    private static final String[] INDICATORS = new String[]{
            "BallPulseIndicator",
            "BallGridPulseIndicator",
            "BallClipRotateIndicator",
            "BallClipRotatePulseIndicator",
            "SquareSpinIndicator",
            "BallClipRotateMultipleIndicator",
            "BallPulseRiseIndicator",
            "BallRotateIndicator",
            "CubeTransitionIndicator",
            "BallZigZagIndicator",
            "BallZigZagDeflectIndicator",
            "BallTrianglePathIndicator",
            "BallScaleIndicator",
            "LineScaleIndicator",
            "LineScalePartyIndicator",
            "BallScaleMultipleIndicator",
            "BallPulseSyncIndicator",
            "BallBeatIndicator",
            "LineScalePulseOutIndicator",
            "LineScalePulseOutRapidIndicator",
            "BallScaleRippleIndicator",
            "BallScaleRippleMultipleIndicator",
            "BallSpinFadeLoaderIndicator",
            "LineSpinFadeLoaderIndicator",
            "TriangleSkewSpinIndicator",
            "PacmanIndicator",
            "BallGridBeatIndicator",
            "SemiCircleSpinIndicator",
            "com.wang.avi.sample.MyCustomIndicator"
    };

}
