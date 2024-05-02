package com.example.puredrop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class home_fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        // Find the LottieAnimationView by its ID
        LottieAnimationView anim = view.findViewById(R.id.capAnimation);

        // Set loop mode to LOOP
        anim.setRepeatCount(LottieDrawable.INFINITE);

        // Start the animation
        anim.playAnimation();

        return view;
    }
}
