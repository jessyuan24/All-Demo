package com.example.jessyuan.alldemo.githubapi;

import android.os.Handler;
import android.os.Looper;

import com.example.jessyuan.alldemo.Scoped.GithubScoped;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by JessYuan on 13/12/2016.
 */

@Module
public class GithubModule {

    private GithubContract.GithubView mView;

    public GithubModule(GithubContract.GithubView view) {
        mView = view;
    }

    @Provides
    @GithubScoped
    GithubContract.GithubView provideView() {
        return mView;
    }

    @Provides
    @GithubScoped
    Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
