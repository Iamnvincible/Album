package com.jude.album.app;

import android.app.Activity;
import android.os.Bundle;

import com.android.debug.hv.ViewServer;
import com.jude.album.BuildConfig;
import com.jude.beam.bijection.ActivityLifeCycleDelegate;
import com.jude.swipbackhelper.SwipeBackHelper;
import com.jude.utils.JActivityManager;
import com.jude.utils.JUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Mr.Jude on 2015/9/9.
 */
public class ActivityDelegate extends ActivityLifeCycleDelegate {

    public ActivityDelegate(Activity act) {
        super(act);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.onCreate(getActivity());
        if (BuildConfig.DEBUG) ViewServer.get(getActivity()).addWindow(getActivity());
        JUtils.Log(getActivity().getClass().getSimpleName()+" onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(getActivity());
        if (BuildConfig.DEBUG) ViewServer.get(getActivity()).setFocusedWindow(getActivity());

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(getActivity());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SwipeBackHelper.onDestroy(getActivity());
        if (BuildConfig.DEBUG) ViewServer.get(getActivity()).removeWindow(getActivity());
        JUtils.Log(getActivity().getClass().getSimpleName()+" onDestroy");

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackHelper.onPostCreate(getActivity());
    }
}
