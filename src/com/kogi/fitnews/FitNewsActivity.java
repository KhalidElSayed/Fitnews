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
							final String items[] = new String[tags.size()];
							tags.toArray(items);
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

												mTagProgressDialog = buildProgressDialog(
														"Searching posts by "
																+ items[itemSelected.arg1],
														true, true);
												mTagProgressDialog.show();

												new ProcessTagButtonPressedThread(
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
							mTagProgressDialog = buildProgressDialog(
									"Searching posts by "
											+ butTag.getText().toString(),
									false, true);
							mTagProgressDialog.show();
							new ProcessTagButtonPressedThread(butTag.getText()
									.toString(), "20", "1").start();
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

		mTextSearch = (TextView) findViewById(R.id.txt_search);
		mImgSearchAction = (ImageView) findViewById(R.id.img_search_action);

		mFItProgressDialog = buildProgressDialog(
				"Searching news on the web...", false, true);
		mFItProgressDialog.show();

		// Set a listener to be invoked when the list should be refreshed.
		((PullToRefreshListView) getListView())
				.setOnRefreshListener(new OnRefreshListener() {

					@Override
					public void onRefresh() {
						// Do work to refresh the list here.
						new GetFitItemsWhenPullingToRefreshTask().execute();

					}
				});

		// TODO uses internet manager to detect the network state
		new Thread(new Runnable() {

			@Override
			public void run() {
				final Message msg = Message.obtain();
				msg.obj = mFItProgressDialog;
				try {
					mFitItems = ConsumerWebServices.getInstance()
							.getFitNewsData("20", "1");
					if (mFitItems.isEmpty()) {
						mHideProgressDialogHandler.sendMessage(msg);
						Toast.makeText(getApplicationContext(),
								R.string.message_to_empty_fit_list,
								Toast.LENGTH_LONG).show();
					} else {
						downloadImagesFitItems(mFitItems);
						getListView().post(new Runnable() {

							@Override
							public void run() {
								setListAdapter(new EfficientAdapter(
										FitNewsActivity.this, mFitItems));
								mHideProgressDialogHandler.sendMessage(msg);
							}
						});
					}

				} catch (JSONException e) {
					mHideProgressDialogHandler.sendMessage(msg);
					Toast.makeText(getApplicationContext(),
							R.string.error_rest_full_service, Toast.LENGTH_LONG)
							.show();
					e.printStackTrace();
				}
			}
		}).start();

	}

	private Handler mSetDataFitNewsHandler = new Handler() {
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
			mHideProgressDialogHandler.sendEmptyMessage(0);
		};
	};

	final private Handler mHideProgressDialogHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ProgressDialog progressDialog = (ProgressDialog) msg.obj;
			if (progressDialog.isShowing())
				progressDialog.dismiss();
		};
	};

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

	class ProcessTagButtonPressedThread extends Thread {
		String tag;
		String count;
		String page;

		public ProcessTagButtonPressedThread(String tag, String count,
				String page) {
			this.tag = tag;
			this.count = count;
			this.page = page;
		}

		private void processTagButtonPressed() {

			try {
				final ArrayList<FitItem> newFits = ConsumerWebServices
						.getInstance().getFitNewsDataByTag(tag, count, page);
				if (!newFits.isEmpty()) {
					mFitItems = newFits;
					downloadImagesFitItems(mFitItems);
					getListView().post(new Runnable() {

						@Override
						public void run() {
							((BaseAdapter) getListAdapter())
									.notifyDataSetChanged();
						}
					});

				} else {
					Toast.makeText(FitNewsActivity.this, "No posts by " + tag,
							Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				Toast.makeText(FitNewsActivity.this, e.getMessage(),
						Toast.LENGTH_LONG);
			}
			Message msg = Message.obtain();
			msg.obj = mTagProgressDialog;
			mHideProgressDialogHandler.sendMessage(msg);

		}

		@Override
		public void run() {
			processTagButtonPressed();
		}

	}

	private ProgressDialog buildProgressDialog(String msg,
			boolean isCancelable, boolean isIndeterminate) {
		ProgressDialog progressDialog = new ProgressDialog(FitNewsActivity.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(msg);

		return progressDialog;
	}

	private ProgressDialog mFItProgressDialog;
	private ProgressDialog mTagProgressDialog;
	private TextView mTextSearch;
	private ImageView mImgSearchAction;
	private ArrayList<FitItem> mFitItems;
}