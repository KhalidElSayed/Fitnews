package com.kogi.fitnews;

import java.util.ArrayList;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.kogi.model.FitItem;

import android.app.Application;

public class MyApplication extends Application {

	// Your Facebook Application ID must be set before running this example
	// See http://www.facebook.com/developers/createapp.php
	public static final String APP_ID = "324233464274304";

	private static MyApplication mMyApplication;
	private ArrayList<FitItem> mFitItems = null;
	//facebook
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private String accessToken = null;
	private String expiresIn = null;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
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

	public Facebook getFacebook() {
		return mFacebook;
	}

	public AsyncFacebookRunner getAsyncRunner() {
		return mAsyncRunner;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}
	
}
