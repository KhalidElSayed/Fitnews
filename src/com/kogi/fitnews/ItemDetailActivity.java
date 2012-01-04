package com.kogi.fitnews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.kogi.model.FitItem;
import com.kogi.util.AppStatus;
import com.kogi.util.DecoderImages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetailActivity extends Activity implements OnClickListener {

	private TextView mLabTitle;
	private TextView mLabDate;
	private ImageView mImageView;
	private TextView mLabContent;
	private ProgressDialog mProgressDialog;
	private Dialog mGalleryImagesDialog;

	// action bar
	private ImageView mPrevAction;
	private ImageView mSavePicAction;
	private ImageView mShareFaceAction;
	private ImageView mShareTwetterAction;
	private ImageView mNextAction;

	private FitItem mFItItem;
	private int mIndex;
	// synchronize the actions on the background
	private boolean mFinishedAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// validate internet connection before do the task
		if (!AppStatus.isOnline(ItemDetailActivity.this)) {

			Button back = new Button(ItemDetailActivity.this);
			back.setText("Back");
			back.setGravity(Gravity.CENTER);
			back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();

				}
			});

			setContentView(back);

			showToastMessage(ItemDetailActivity.this,
					R.string.message_to_network_problem, Toast.LENGTH_LONG);
			return;
		}

		setContentView(R.layout.detail_item_fit);

		mLabTitle = (TextView) findViewById(R.id.lab_title_item_detail);
		mLabDate = (TextView) findViewById(R.id.lab_date);
		
		mImageView = (ImageView) findViewById(R.id.img_item_detail);
		int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.8);
		int height = (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.4);
		mImageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		mImageView.setOnClickListener(this);
		
		mLabContent = (TextView) findViewById(R.id.lab_content);

		mPrevAction = (ImageView) findViewById(R.id.prev_action);
		mPrevAction.setOnClickListener(this);
		mSavePicAction = (ImageView) findViewById(R.id.save_pic_action);
		mSavePicAction.setOnClickListener(this);
		mShareFaceAction = (ImageView) findViewById(R.id.share_facebook_action);
		mShareFaceAction.setOnClickListener(this);
		mShareTwetterAction = (ImageView) findViewById(R.id.share_twetter_action);
		mShareTwetterAction.setOnClickListener(this);
		mNextAction = (ImageView) findViewById(R.id.next_action);
		mNextAction.setOnClickListener(this);

		mFinishedAction = true;

		Bundle extras = getIntent().getExtras();
		mIndex = (extras != null) ? extras
				.getInt(FitNewsActivity.ITEM_SELECTED) : -1;

		if (mIndex != -1) {
			mFItItem = MyApplication.getInstance().getFitItemsList()
					.get(mIndex);

			if (mFItItem != null) {
				if (mFItItem.getUrlsImages().size() >= 1) {
					if (mFItItem.getImages().isEmpty()) {

						new DownloadImagesToFitItemTask().execute(mFItItem);
						setFitItemBaseData();

					} else {
						setFitItemBaseData();
						mImageView.setImageBitmap(mFItItem.getImages().get(0));
					}
				} else {
					setFitItemBaseData();
					setFitItemImageWhenItNoHasImages();
				}

			}
		} else {
			// TODO send a messagge indicating mIndex = -1
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
			final int index = i;
			String urlImage = urls.get(index);
			if (urlImage != null && !urlImage.equals("")) {
				boolean finished = false;
				int intentos = 1;

				while (!finished && intentos <= 2) {
					Bitmap bmp = DecoderImages.getBitmapFromURL(urlImage);
					if (bmp != null) {
						Bitmap bmpResized = DecoderImages.getBitmapReSize(bmp,
								width);
						fitItem.getImages().add(index, bmpResized);
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
		ProgressDialog progressDialog = new ProgressDialog(
				ItemDetailActivity.this);
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

	private class DownloadImagesToFitItemTask extends
			AsyncTask<FitItem, Void, Void> {

		@Override
		protected void onPreExecute() {
			mFinishedAction = false;
			mProgressDialog = buildProgressDialog("Loading data...", false,
					true);
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
			// set the first image
			mImageView.setImageBitmap(mFItItem.getImages().get(0));
			mProgressDialog.hide();
			// mHideProgressDialogHandler.sendEmptyMessage(0);

			mFinishedAction = true;
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mHideProgressDialogHandler.sendEmptyMessage(0);
		}
	}

	private void setFitItemBaseData() {
		mLabTitle.setText(mFItItem.getTitlePlain());
		SimpleDateFormat format = new SimpleDateFormat("hh:mm a E, d'th' MMMMM");
		String stringDate = format.format(mFItItem.getDate());
		mLabDate.setText(stringDate);

		mLabContent.setText(Html.fromHtml(mFItItem.getContent()));
	}

	private void setFitItemImageWhenItNoHasImages() {
		int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.8);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.no_images);
		Bitmap bmpResized = DecoderImages.getBitmapReSize(bmp, width);
		mImageView.setImageBitmap(bmpResized);
	}

	private void doPreviousAction() {

		if (!mFinishedAction)
			return;

		if (!decrementIndex())
			return;

		FitItem item = MyApplication.getInstance().getFitItemsList()
				.get(mIndex);

		if (item != null)
			mFItItem = item;

		if (mFItItem.getUrlsImages().size() >= 1) {
			if (mFItItem.getImages().isEmpty()) {

				new DownloadImagesToFitItemTask().execute(mFItItem);
				setFitItemBaseData();

			} else {
				setFitItemBaseData();
				mImageView.setImageBitmap(mFItItem.getImages().get(0));
			}
		} else {
			setFitItemBaseData();
			setFitItemImageWhenItNoHasImages();
		}

	}

	private void doNextAction() {
		if (!mFinishedAction)
			return;

		if (!incrementIndex())
			return;

		FitItem item = MyApplication.getInstance().getFitItemsList()
				.get(mIndex);

		if (item != null)
			mFItItem = item;

		if (mFItItem.getUrlsImages().size() >= 1) {
			if (mFItItem.getImages().isEmpty()) {

				new DownloadImagesToFitItemTask().execute(mFItItem);
				setFitItemBaseData();

			} else {
				setFitItemBaseData();
				mImageView.setImageBitmap(mFItItem.getImages().get(0));
			}
		} else {
			setFitItemBaseData();
			setFitItemImageWhenItNoHasImages();
		}
	}

	private void showGalleryImagesPopUp() {
		if (mFItItem.getUrlsImages().size() > 1) {
			mGalleryImagesDialog = buildPopUpGalleryImages();
			mGalleryImagesDialog.show();
		}
	}

	private boolean decrementIndex() {
		if (mIndex == 0)
			return false;

		mIndex--;
		return true;
	}

	private boolean incrementIndex() {
		if (mIndex == MyApplication.getInstance().getFitItemsList().size() - 1)
			return false;

		mIndex++;
		return true;
	}

	private void showToastMessage(Context ctx, int idMsg, int duration) {
		Toast.makeText(ctx, idMsg, duration).show();
	}

	private Dialog buildPopUpGalleryImages() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		Gallery gallery = new Gallery(ItemDetailActivity.this);
		gallery.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		gallery.setPadding(3, 3, 3, 3);
		gallery.setAdapter(new ImageGalleryAdapter(ItemDetailActivity.this,
				mFItItem.getImages().toArray(new Bitmap[0])));

		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				mImageView.setImageBitmap(mFItItem.getImages().get(position));

				if (mGalleryImagesDialog != null)
					mGalleryImagesDialog.dismiss();
			}
		});

		builder = new AlertDialog.Builder(ItemDetailActivity.this);
		builder.setView(gallery);
		alertDialog = builder.create();

		return alertDialog;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prev_action:
			doPreviousAction();
			break;
		case R.id.save_pic_action:
			break;

		case R.id.share_facebook_action:
			break;

		case R.id.share_twetter_action:
			break;

		case R.id.next_action:
			doNextAction();
			break;

		case R.id.img_item_detail:
			showGalleryImagesPopUp();
			break;

		default:
			break;
		}

	}

	public class ImageGalleryAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;
		private Bitmap[] mImages;

		public ImageGalleryAdapter(Context c, Bitmap[] images) {
			mContext = c;
			mImages = images;
			TypedArray attr = mContext
					.obtainStyledAttributes(R.styleable.HelloGallery);
			mGalleryItemBackground = attr.getResourceId(
					R.styleable.HelloGallery_android_galleryItemBackground, 0);
			attr.recycle();
		}

		public int getCount() {
			return mImages.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);

			imageView.setImageBitmap(mImages[position]);
			imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setBackgroundResource(mGalleryItemBackground);

			return imageView;
		}

	}
}
