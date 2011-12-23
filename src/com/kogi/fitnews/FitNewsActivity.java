package com.kogi.fitnews;

import java.security.KeyStore.LoadStoreParameter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.kogi.fitnews.PullToRefreshListView.OnLoadMoreListener;
import com.kogi.fitnews.PullToRefreshListView.OnRefreshListener;
import com.kogi.model.FitItem;
import com.kogi.util.DecoderImages;
import com.kogi.ws.ConsumerWebServices;

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
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
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
				final Button butTag = new Button(mContext);
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

							// items dialog
							List<String> others = tags.subList(2, tags.size());
							final String items[] = new String[others.size()];
							others.toArray(items);
							final Message itemSelected = Message.obtain();
							itemSelected.arg1 = -1;
							builder.setSingleChoiceItems(items, -1,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int pos) {
											itemSelected.arg1 = pos;
										}
									});

							// accept but
							builder.setPositiveButton("Aceptar",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
											if (itemSelected.arg1 != -1) {

												mProgressDialog = buildProgressDialog(
														"Searching posts by "
																+ items[itemSelected.arg1],
														true, true);
												mProgressDialog.show();

												new ProcessSearchingPostsByTag(
														items[itemSelected.arg1],
														"20", "1").start();
											}
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
							mProgressDialog = buildProgressDialog(
									"Searching posts by "
											+ butTag.getText().toString(),
									false, true);
							mProgressDialog.show();
							new ProcessSearchingPostsByTag(butTag.getText()
									.toString(), "20", "1").start();
						}
					});
				}

				// TODO set up to 20% the tags panel according screen size
				holder.tagsPanel.addView(butTag, i, new LayoutParams(
						LayoutParams.WRAP_CONTENT,
						(int) (30 * mDisplayMetrics.density)));

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

		mTextSearch = (TextView) findViewById(R.id.txt_search);
		mImgSearchAction = (ImageView) findViewById(R.id.img_search_action);
		mImgSearchAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String query = mTextSearch.getText().toString();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						mTextSearch.getApplicationWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				if (!query.equals("")) {
					mProgressDialog = buildProgressDialog("Searching posts by "
							+ query, false, true);
					mProgressDialog.show();
					new ProcessSearchingPostsByQuery(query, "20", "1").start();
				}
			}
		});

		mProgressDialog = buildProgressDialog("Searching news on the web...",
				false, true);
		mProgressDialog.show();

		mFitItems = new ArrayList<FitItem>();
		setListAdapter(new EfficientAdapter(FitNewsActivity.this, mFitItems));

		// Set a listener to be invoked when the list should be refreshed.
		((PullToRefreshListView) getListView())
				.setOnRefreshListener(new OnRefreshListener() {

					@Override
					public void onRefresh() {
						// Do work to refresh the list here.
						new GetFitItemsWhenPullingToRefreshTask().execute();

					}
				});
		// set a listener to be invoged when the list reaches the end
		((PullToRefreshListView) getListView())
				.setOnLoadMoreListener(mOnLoadMoreListener);

		// TODO uses internet manager to detect the network state
		// load initial posts
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mFitItems = ConsumerWebServices.getInstance()
							.getFitNewsData("20", "1");
					if (mFitItems.isEmpty()) {
						Toast.makeText(getApplicationContext(),
								R.string.message_to_empty_fit_list,
								Toast.LENGTH_LONG).show();
					} else {
						downloadImagesFitItems(mFitItems);
						runOnUiThread(mNotifyAdapterListTask);
					}
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(),
							R.string.error_rest_full_service, Toast.LENGTH_LONG)
							.show();
					e.printStackTrace();
				}

				mHideProgressDialogHandler.sendEmptyMessage(0);
			}
		}).start();

		// initialize the footer view list
		View footerView = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.fit_list_footer, null, false);
		this.getListView().addFooterView(footerView);
		getListView().setFooterDividersEnabled(false);
	}

	final private Handler mHideProgressDialogHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		};

	};

	/** Download images from Fit items list from their urls */
	protected void downloadImagesFitItems(ArrayList<FitItem> fitItems) {
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

	/**
	 * AsynckTask to does the job of update the list when the user makes pulling
	 * to refresh
	 */
	private class GetFitItemsWhenPullingToRefreshTask extends
			AsyncTask<Void, Void, Void> {

		private ArrayList<FitItem> newFitItemsList = null;

		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

			try {
				newFitItemsList = ConsumerWebServices.getInstance()
						.getFitNewsData("20", "1");
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

	/** Thread to does the job of searches by tag */
	class ProcessSearchingPostsByTag extends Thread {
		String tag;
		String count;
		String page;

		public ProcessSearchingPostsByTag(String tag, String count, String page) {
			this.tag = tag;
			this.count = count;
			this.page = page;
		}

		private void processSearching() {

			try {
				final ArrayList<FitItem> newFits = ConsumerWebServices
						.getInstance().getFitNewsDataByTag(tag, count, page);
				if (!newFits.isEmpty()) {
					mFitItems = newFits;
					downloadImagesFitItems(mFitItems);
					runOnUiThread(mNotifyAdapterListTask);
				} else {
					Toast.makeText(FitNewsActivity.this, "No posts by " + tag,
							Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				Toast.makeText(FitNewsActivity.this, e.getMessage(),
						Toast.LENGTH_LONG);
			}
			mHideProgressDialogHandler.sendEmptyMessage(0);

		}

		@Override
		public void run() {
			processSearching();
		}

	}

	/** Thread to does the job of searches by query */
	class ProcessSearchingPostsByQuery extends Thread {

		String query;
		String count;
		String page;

		public ProcessSearchingPostsByQuery(String query, String count,
				String page) {
			this.query = query;
			this.count = count;
			this.page = page;
		}

		private void processSearching() {
			try {
				final ArrayList<FitItem> newFits = ConsumerWebServices
						.getInstance()
						.getFitNewsDataByQuery(query, count, page);
				if (!newFits.isEmpty()) {
					mFitItems = newFits;
					downloadImagesFitItems(mFitItems);
					runOnUiThread(mNotifyAdapterListTask);
				} else {
					Toast.makeText(FitNewsActivity.this,
							"No posts by " + query, Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				Toast.makeText(FitNewsActivity.this, e.getMessage(),
						Toast.LENGTH_LONG);

			}
			mHideProgressDialogHandler.sendEmptyMessage(0);
			Looper.loop();
		}

		@Override
		public void run() {
			Looper.prepare();
			processSearching();
			mHideProgressDialogHandler.getLooper().quit();
		}

	}

	private OnLoadMoreListener mOnLoadMoreListener = new OnLoadMoreListener() {

		@Override
		public void onLoadMore() {
			new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						final ArrayList<FitItem> newFits = ConsumerWebServices
								.getInstance().getFitNewsData("20", "1");
						if (!newFits.isEmpty()) {
							downloadImagesFitItems(newFits);
							mFitItems.addAll(newFits);
							runOnUiThread(mNotifyAdapterListTask);
						} else {
							Toast.makeText(FitNewsActivity.this,
									"No more fit news to load",
									Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						Toast.makeText(FitNewsActivity.this, e.getMessage(),
								Toast.LENGTH_LONG);
					}

				}
			}).start();
		}
	};

	private ProgressDialog buildProgressDialog(String msg,
			boolean isCancelable, boolean isIndeterminate) {
		ProgressDialog progressDialog = new ProgressDialog(FitNewsActivity.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(msg);

		return progressDialog;
	}

	private Runnable mNotifyAdapterListTask = new Runnable() {

		@Override
		public void run() {
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		}
	};

	private ProgressDialog mProgressDialog;
	private TextView mTextSearch;
	private ImageView mImgSearchAction;
	private ArrayList<FitItem> mFitItems;
}