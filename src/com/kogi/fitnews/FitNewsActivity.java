package com.kogi.fitnews;

import java.util.ArrayList;

import org.json.JSONException;

import com.kogi.fitnews.PullToRefreshListView.OnRefreshListener;
import com.kogi.model.FitItem;
import com.kogi.util.DecoderImages;
import com.kogi.ws.ConsumerWebServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FitNewsActivity extends ListActivity {

	private class EfficientAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context mContext;
		private DisplayMetrics mDisplayMetrics;

		protected int screenWidth;

		private EfficientAdapter(Context context,
				ArrayList<FitItem> fitItemsList) {

			mInflater = LayoutInflater.from(context);
			mContext = context;
			mFitItems = fitItemsList;
			mDisplayMetrics = new DisplayMetrics();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getMetrics(mDisplayMetrics);

			screenWidth = (int) (mDisplayMetrics.widthPixels * 0.3);
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
				holder.imgNews.setMinimumWidth(screenWidth);
				holder.imgNews.setMaxWidth(screenWidth);

				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
						screenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				layoutParams.addRule(RelativeLayout.ALIGN_TOP,
						R.id.lab_detail_item_list_fit);
				holder.imgNews.setLayoutParams(layoutParams);

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
				// clean the image fit
				holder.imgNews.setImageDrawable(null);
			}

			// set the list fit item data
			// TODO manage the images in cache
			FitItem fitItem = mFitItems.get(position);

			if (fitItem.getImageFull() != null) {
				holder.imgNews.setImageBitmap(fitItem.getImageFull());
			} else {
				holder.imgNews.setImageResource(R.drawable.no_data);
			}

			holder.labTitle.setText(fitItem.getTitlePlain());
			holder.labDetail.setText(Html.fromHtml(fitItem.getExcerpt()));

			final ArrayList<String> tags = fitItem.getTags();
			boolean continuar = true;

			for (int i = 0; continuar && i < tags.size(); i++) {
				Button butTag = new Button(mContext);
				butTag.setTextColor(R.color.red);
				butTag.setTextAppearance(mContext, R.style.lab_tags_fit_news);

				if (i == 2 && tags.size() >= 4) {
					butTag.setText("others");
					// Listener para lanzar dialogo de tags
					butTag.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// create the dialog
							AlertDialog.Builder builder = new AlertDialog.Builder(
									mContext);
							builder.setTitle("Pick a tag");
							builder.setPositiveButton("Aceptar",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});

							// items list
							final boolean checkedTags[] = new boolean[tags
									.size()];
							String items[] = new String[tags.size()];
							tags.toArray(items);

							builder.setMultiChoiceItems(
									items,
									checkedTags,
									new DialogInterface.OnMultiChoiceClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int wich, boolean isChecked) {
											checkedTags[wich] = isChecked;

										}
									});

							builder.create().show();
						}
					});
					continuar = false;
				} else {

					butTag.setText(tags.get(i));
					butTag.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

						}
					});
				}

				// TODO set up to 20% the tags panel according screen size
				holder.tagsPanel.addView(butTag, i, new LayoutParams(
						LayoutParams.WRAP_CONTENT,
						(int) (28 * mDisplayMetrics.density)));

			}
			return convertView;
		}
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

		mFitNewsActivity = this;

		mTextSearch = (TextView) findViewById(R.id.txt_search);
		mImgSearchAction = (ImageView) findViewById(R.id.img_search_action);

		mProgressDialog = new ProgressDialog(FitNewsActivity.this);
		mProgressDialog.setMessage("Searching news on the web...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();

		// Set a listener to be invoked when the list should be refreshed.
		((PullToRefreshListView) getListView())
				.setOnRefreshListener(new OnRefreshListener() {

					@Override
					public void onRefresh() {
						// Do work to refresh the list here.
						new GetFitItemsWhenPullingToRefreshTask().execute();

					}
				});

		// TODO manage the request service by time
		// TODO uses internet manager to detect the network state
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Looper.prepare();
					mFitItems = ConsumerWebServices.getNewsData("20", "1");
					downloadImagesFitItems(mFitItems);
					mSetDataNewsHandler.sendEmptyMessage(0);
					Looper.loop();
				} catch (JSONException e) {

					// TODO hide mProgressDialog in the UI thread
					mFitNewsActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (mProgressDialog.isShowing())
								mProgressDialog.hide();
							Toast.makeText(getApplicationContext(),
									R.string.error_rest_full_service,
									Toast.LENGTH_LONG).show();
						}
					});

					e.printStackTrace();
				}

			}
		}).start();

	}

	private Handler mSetDataNewsHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (mFitItems.isEmpty()) {
				Toast.makeText(getApplicationContext(),
						R.string.message_to_empty_fit_list, Toast.LENGTH_LONG)
						.show();
				// TODO set empty the list
			} else {
				setListAdapter(new EfficientAdapter(FitNewsActivity.this,
						mFitItems));
			}
			if (mProgressDialog.isShowing())
				mProgressDialog.hide();
		};
	};

	public void downloadImagesFitItems(ArrayList<FitItem> fitItems) {
		int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.3);

		for (FitItem fitItem : fitItems) {
			String urlImage = fitItem.getUrlImage();
			if (urlImage != null && !urlImage.equals("")) {

				boolean finished = false;
				int intentos = 1;
				while (!finished && intentos <= 2) {
					Bitmap bmp = DecoderImages.getBitmapFromURL(fitItem
							.getUrlImage());
					if (bmp != null) {
						Bitmap bmpResized = DecoderImages.getBitmapReSize(bmp,
								width);
						fitItem.setImageFull(bmpResized);
						finished = true;
					} else {
						intentos++;
					}

				}
			}
		}
	}

	private class GetFitItemsWhenPullingToRefreshTask extends
			AsyncTask<Void, Void, Void> {

		private ArrayList<FitItem> newFitItemsList = null;

		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

			try {
				newFitItemsList = ConsumerWebServices.getNewsData("20", "1");
				if (!newFitItemsList.isEmpty()) {
					downloadImagesFitItems(newFitItemsList);
				}
			} catch (JSONException e) {
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			if (newFitItemsList != null && !newFitItemsList.isEmpty()) {
				mFitItems = newFitItemsList;
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
			}

			// Call onRefreshComplete when the list has been refreshed.
			((PullToRefreshListView) getListView()).onRefreshComplete();

			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			// reset the UI
			((PullToRefreshListView) getListView()).onRefreshComplete();
		}
	}

	private ProgressDialog mProgressDialog;
	private TextView mTextSearch;
	private ImageView mImgSearchAction;
	private ArrayList<FitItem> mFitItems;
	private Activity mFitNewsActivity;

}