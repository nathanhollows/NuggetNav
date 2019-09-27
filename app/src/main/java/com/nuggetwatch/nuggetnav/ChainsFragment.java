package com.nuggetwatch.nuggetnav;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChainsFragment extends Fragment {


    public ChainsFragment() {
        // Required empty public constructor
    }


    @Override
    public ListView onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());
        return listView;
    }

}
