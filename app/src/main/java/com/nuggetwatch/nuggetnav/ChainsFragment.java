package com.nuggetwatch.nuggetnav;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChainsFragment extends Fragment {


    public ChainsFragment() {
        // Required empty public constructor
    }


    @Override
    public ListView onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        return new ListView(getActivity());
    }

}
