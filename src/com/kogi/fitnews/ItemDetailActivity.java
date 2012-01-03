package com.kogi.fitnews;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemDetailActivity extends Activity {

	private TextView mLabTitle;
	private TextView mLabDate;
	private ImageView mImageView;
	private TextView mLabContent;

	// action bar
	private ImageView mPrevAction;
	private ImageView mSavePicAction;
	private ImageView mShareFaceAction;
	private ImageView mShareTwetterAction;
	private ImageView mNextAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detail_item_fit);

		mLabTitle = (TextView) findViewById(R.id.lab_title_item_detail);
		mLabDate = (TextView) findViewById(R.id.lab_date);
		mImageView = (ImageView) findViewById(R.id.img_item_detail);
		mLabContent = (TextView) findViewById(R.id.lab_content);

		mPrevAction = (ImageView) findViewById(R.id.prev_action);
		mSavePicAction = (ImageView) findViewById(R.id.save_pic_action);
		mShareFaceAction = (ImageView) findViewById(R.id.share_facebook_action);
		mShareTwetterAction = (ImageView) findViewById(R.id.share_twetter_action);
		mNextAction = (ImageView) findViewById(R.id.next_action);
	}
}
