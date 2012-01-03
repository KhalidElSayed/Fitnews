package com.kogi.fitnews;

import java.util.ArrayList;

import com.kogi.model.FitItem;

import android.app.Application;

public class MyApplication extends Application {

	private static MyApplication mMyApplication;
	private ArrayList<FitItem> mFitItems = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
	}

	public static MyApplication getInstance() {
		return mMyApplication;
	}

	public ArrayList<FitItem> getFitItemsList() {
		return mFitItems;
	}

	public void setFitItemsList(ArrayList<FitItem> mFitItems) {
		this.mFitItems = mFitItems;
	}

}
