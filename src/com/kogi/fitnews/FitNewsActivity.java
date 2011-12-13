package com.kogi.fitnews;

import java.util.ArrayList;

import org.json.JSONException;

import com.kogi.model.FitItem;
import com.kogi.util.DecoderImages;
import com.kogi.ws.ConsumerWebServices;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FitNewsActivity extends ListActivity {

	private static class EfficientAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context mContext;
		private ArrayList<FitItem> mFitItems;
		private Display mDisplay;

		public EfficientAdapter(Context context, ArrayList<FitItem> fitItems) {
			mInflater = LayoutInflater.from(context);
			mContext = context;
			mFitItems = fitItems;
			mDisplay = ((WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
		}

		public int getCount() {
			return mFitItems.size();
		}

		public Object getItem(int position) {
			return mFitItems.get(position);
		}

		public long getItemId(int position) {
			return mFitItems.get(position).getId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.fit_news_item_list,
						null);
				holder = new ViewHolder();
				holder.imgNews = (ImageView) convertView
						.findViewById(R.id.img_new_item_list_fit);
				holder.labTitle = (TextView) convertView
						.findViewById(R.id.lab_title_item_list_fit);
				holder.tagsPanel = (LinearLayout) convertView
						.findViewById(R.id.tags_panel);
				holder.labDetail = (TextView) convertView
						.findViewById(R.id.lab_detail_item_list_fit);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				// clean all tags added to tagspanel
				holder.tagsPanel.removeAllViews();
			}

			// set the list fit item data
			// TODO manage the images in cache
			FitItem fitItem = mFitItems.get(position);
			if (fitItem.getUrlImage() != null
					&& !fitItem.getUrlImage().equals("")) {

				Bitmap bitmap = DecoderImages.getBitmapFromURL(fitItem
						.getUrlImage());

				if (bitmap != null) {
					/*
					 * Bitmap bitmapResized; int orientation =
					 * getScreenOrientation(); switch (orientation) { case
					 * Configuration.ORIENTATION_SQUARE: bitmapResized =
					 * DecoderImages.getBitmapReSize(bitmap, 165);
					 * Log.v("scren orientation", "square"); break; case
					 * Configuration.ORIENTATION_PORTRAIT: bitmapResized =
					 * DecoderImages.getBitmapReSize(bitmap, 165);
					 * Log.v("scren orientation", "portrait"); break; case
					 * Configuration.ORIENTATION_LANDSCAPE: bitmapResized =
					 * DecoderImages.getBitmapReSize(bitmap, 200);
					 * Log.v("scren orientation", "landscape"); default:
					 * bitmapResized = DecoderImages.getBitmapReSize(bitmap,
					 * 165); Log.v("scren orientation", "default"); break; }
					 */

					Bitmap bitmapResized = DecoderImages.getBitmapReSize(
							bitmap, 165);
					holder.imgNews.setImageBitmap(bitmapResized);
					holder.imgNews.setAdjustViewBounds(true);
				}
			} else {
				holder.imgNews.setImageResource(R.drawable.no_data);
			}

			holder.labTitle.setText(fitItem.getTitlePlain());
			holder.labDetail.setText(Html.fromHtml(fitItem.getExcerpt()));

			ArrayList<String> tags = fitItem.getTags();
			boolean continuar = true;

			for (int i = 0; continuar && i < tags.size(); i++) {
				Button butTag = new Button(mContext);
				butTag.setTextAppearance(mContext, R.style.lab_tags_fit_news);

				if (i < 2) {
					butTag.setText(tags.get(i));
				} else {
					butTag.setText("others");
					continuar = false;

				}
				/*
				 * holder.tagsPanel.addView(butTag, i, new LayoutParams(
				 * LayoutParams.WRAP_CONTENT, 40));
				 */
				// TODO set up to 20% the tags panel according screen size
				Log.v("screen size", "x= " + mDisplay.getWidth() + " y= " + mDisplay.getHeight());
				holder.tagsPanel.addView(butTag, i);

			}
			return convertView;
		}

		/*
		 * protected int getScreenOrientation() {
		 * 
		 * Display display = ((WindowManager) mContext
		 * .getSystemService(WINDOW_SERVICE)).getDefaultDisplay(); int
		 * orientation = display.getOrientation();
		 * 
		 * // Sometimes you may get undefined orientation Value is 0 // simple
		 * logic solves the problem compare the screen // X,Y Co-ordinates and
		 * determine the Orientation in such cases if (orientation ==
		 * Configuration.ORIENTATION_UNDEFINED) {
		 * 
		 * Configuration config = mContext.getResources() .getConfiguration();
		 * orientation = config.orientation;
		 * 
		 * if (orientation == Configuration.ORIENTATION_UNDEFINED) { // if
		 * height and widht of screen are equal then // it is square orientation
		 * if (display.getWidth() == display.getHeight()) { orientation =
		 * Configuration.ORIENTATION_SQUARE; } else { // if widht is less than
		 * height than it is portrait if (display.getWidth() <
		 * display.getHeight()) { orientation =
		 * Configuration.ORIENTATION_PORTRAIT; } else { // if it is not any of
		 * the above it will // defineitly // be landscape orientation =
		 * Configuration.ORIENTATION_LANDSCAPE; } } } } return orientation; //
		 * return value 1 is portrait and 2 is Landscape // Mode
		 * 
		 * return mContext.getResources().getConfiguration().orientation; }
		 */
	}

	static class ViewHolder {
		ImageView imgNews;
		TextView labTitle;
		TextView labDetail;
		LinearLayout tagsPanel;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fit_news_list);

		mTextSearch = (TextView) findViewById(R.id.txt_search);
		mTextSearch.clearFocus();
		mImgSearchAction = (ImageView) findViewById(R.id.img_search_action);
		mImgSearchAction.requestFocus();

		mProgressDialog = new ProgressDialog(FitNewsActivity.this);
		mProgressDialog.setMessage("Searching news on the web...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();

		// TODO manage the request service by time
		new Thread(new Runnable() {
			// TODO build a service component
			@Override
			public void run() {
				try {
					Looper.prepare();
					fitItems = ConsumerWebServices.getNewsData("20", "1");
					mGetDataNewsHandler.sendEmptyMessage(0);
					Looper.loop();
				} catch (JSONException e) {

					if (mProgressDialog.isShowing())
						mProgressDialog.hide();

					Toast.makeText(getApplicationContext(),
							R.string.error_rest_full_service, Toast.LENGTH_LONG)
							.show();

					e.printStackTrace();
				}

			}
		}).start();
	}

	private ProgressDialog mProgressDialog;
	private TextView mTextSearch;
	private ImageView mImgSearchAction;
	private ArrayList<FitItem> fitItems;

	private Handler mGetDataNewsHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (fitItems.isEmpty()) {
				Toast.makeText(getApplicationContext(),
						R.string.message_to_empty_fit_list, Toast.LENGTH_LONG);
			} else {
				setListAdapter(new EfficientAdapter(getApplicationContext(),
						fitItems));
			}
			if (mProgressDialog.isShowing())
				mProgressDialog.hide();
		};
	};

}