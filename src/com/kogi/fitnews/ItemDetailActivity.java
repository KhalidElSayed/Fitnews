package com.kogi.fitnews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.kogi.model.FitItem;
import com.kogi.util.DecoderImages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemDetailActivity extends Activity {

	private TextView mLabTitle;
	private TextView mLabDate;
	private ImageView mImageView;
	private TextView mLabContent;
	private ProgressDialog mProgressDialog;
	
	// action bar
	private ImageView mPrevAction;
	private ImageView mSavePicAction;
	private ImageView mShareFaceAction;
	private ImageView mShareTwetterAction;
	private ImageView mNextAction;

	private FitItem mFItItem;
	private int mIndex;

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

		Bundle extras = getIntent().getExtras();
		mIndex = (extras != null) ? extras
				.getInt(FitNewsActivity.ITEM_SELECTED) : -1;

		if (mIndex != -1) {
			mFItItem = MyApplication.getInstance().getFitItemsList()
					.get(mIndex);

			if (mFItItem != null) {								
				new BuildingItemFitUITask().execute(mFItItem);			
			}
		} else {

		}
	}

	/**
	 * Download the carousel images from chosen Fit item from its images urls
	 * list
	 */
	protected void downloadImagesFitItem(FitItem fitItem) {
		int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.8);
		ArrayList<String> urls = fitItem.getUrlsImages();

		for (int i = 0; i < urls.size(); i++) {
			String urlImage = urls.get(i);
			if (urlImage != null && !urlImage.equals("")) {
				boolean finished = false;
				int intentos = 1;

				while (!finished && intentos <= 2) {
					Bitmap bmp = DecoderImages.getBitmapFromURL(urlImage);
					if (bmp != null) {
						Bitmap bmpResized = DecoderImages.getBitmapReSize(bmp,
								width);
						fitItem.getImages().add(i, bmpResized);
						finished = true;
					} else {
						intentos++;
					}

				}
			}
		}
	}
	
	private ProgressDialog buildProgressDialog(String msg,
			boolean isCancelable, boolean isIndeterminate) {
		ProgressDialog progressDialog = new ProgressDialog(ItemDetailActivity.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(msg);

		return progressDialog;
	}
	
	final private Handler mHideProgressDialogHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		};

	};
	
	private class BuildingItemFitUITask extends AsyncTask<FitItem, Void, Void>{

		@Override
		protected void onPreExecute() {
			mProgressDialog = buildProgressDialog("Loading data..."
					, false, true);
			mProgressDialog.show();
		}
		
		@Override
		protected Void doInBackground(FitItem... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			downloadImagesFitItem(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			mLabTitle.setText(mFItItem.getTitlePlain());
			SimpleDateFormat format = new SimpleDateFormat(
					"hh:mm a E, d'th' MMMMM");
			String stringDate = format.format(mFItItem.getDate());
			mLabDate.setText(stringDate);
			
			if(!mFItItem.getImages().isEmpty()){
				mImageView.setImageBitmap(mFItItem.getImages().get(0));
			}else{
				int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.9);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_images);
				Bitmap bmpResized = DecoderImages.getBitmapReSize(bmp, width);
				mImageView.setImageBitmap(bmpResized);
			}
						
			mLabContent.setText(Html.fromHtml(mFItItem.getContent()));	
			
			mHideProgressDialogHandler.sendEmptyMessage(0);			
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			mHideProgressDialogHandler.sendEmptyMessage(0);
		}
	}
}
