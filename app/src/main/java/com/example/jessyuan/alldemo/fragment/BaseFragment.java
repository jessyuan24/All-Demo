package com.example.jessyuan.alldemo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Jess Yuan on 23/09/2016.
 */

public abstract class BaseFragment extends Fragment {

    protected Activity mActivity;
    protected FragmentManager mFragmentManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setLayoutViewId(), container, false);

        ButterKnife.bind(this, view);

        mActivity = getActivity();
        mFragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();

        return view;
    }

    public abstract int setLayoutViewId();

}