package com.kogi.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kogi.model.FitItem;

public class ConsumerWebServices {

	public static String URL_GET_FIT_NEWS = "http://66.228.57.165/wordpress/?json=get_recent_post&";
	public static String URL_GET_FIT_NEWS_BY_TAG = "http://66.228.57.165/wordpress/?json=get_tag_posts&tag_slug=";

	// http://66.228.57.165/wordpress/?json=get_tag_posts&tag_slug=jagger&count=20&page=1

	private static ConsumerWebServices instance = null;

	private ConsumerWebServices() {
	}

	public static ConsumerWebServices getInstance() {
		if (instance == null) {
			instance = new ConsumerWebServices();
		}
		return instance;
	}

	public ArrayList<FitItem> getFitNewsData(String count, String page)
			throws JSONException {

		// make the web service�s url
		String url = URL_GET_FIT_NEWS + "count=" + count + "&page=" + page;
		ArrayList<FitItem> fitItems;
		try {
			String json = getJSONFromURL(url);
			fitItems = getPostsFromJSON(json);
		} catch (IOException e) {
			throw new JSONException("Error process the JSON");
		} catch (JSONException e) {
			throw (e);
		}
		return fitItems;
	}

	public ArrayList<FitItem> getFitNewsDataByTag(String tag, String count,
			String page) throws JSONException {
		String url = URL_GET_FIT_NEWS_BY_TAG + tag + "&count=" + count
				+ "&page=" + page;
		ArrayList<FitItem> fitItems;
		String json;
		try {
			json = getJSONFromURL(url);
			fitItems = getPostsFromJSON(json);
		} catch (IOException e) {
			throw new JSONException("Error process the JSON");
		} catch (JSONException e) {
			throw (e);
		}
		return fitItems;
	}

	private String getJSONFromURL(String url) throws IOException {
		try {
			// Building the request
			HttpClient httpClient = new DefaultHttpClient();
			URI uri = new URI(url);
			HttpGet getRequest = new HttpGet();
			getRequest.setURI(uri);

			// getting the response
			HttpResponse httpResponse = httpClient.execute(getRequest);

			final int statusCode = httpResponse.getStatusLine().getStatusCode();

			// check the response if it's ok
			if (statusCode != HttpStatus.SC_OK) {
				// TODO search another way to return the answer with the error
				// code (some costum exception)
				return null;
			}

			final HttpEntity entity = httpResponse.getEntity();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					entity.getContent()));

			String data = in.readLine();
			return data;
		} catch (URISyntaxException e) {
			throw new IOException("Error internet connection");
		} catch (ClientProtocolException e) {
			throw new IOException("Error internet connection");
		} catch (IOException e) {
			throw new IOException("Error internet connection");
		}

	}

	private ArrayList<FitItem> getPostsFromJSON(String json)
			throws JSONException {

		JSONObject jsonObject;
		JSONArray postItems;
		try {
			jsonObject = new JSONObject(json);
			postItems = jsonObject.getJSONArray("posts");
		} catch (JSONException e1) {
			throw new JSONException("Error process the JSON");
		}

		ArrayList<FitItem> fitItems = new ArrayList<FitItem>();

		for (int i = 0; i < postItems.length(); i++) {

			try {
				JSONObject postItem = postItems.getJSONObject(i);

				FitItem fitItem = new FitItem();
				fitItem.setId(postItem.getInt("id"));

				String titlePlain = postItem.getString("title_plain");
				if (!titlePlain.equals(""))
					fitItem.setTitlePlain(titlePlain);

				String excerpt = postItem.getString("excerpt");
				if (!excerpt.equals(""))
					fitItem.setExcerpt(excerpt);

				// get tags from a post
				JSONArray tagsItems = postItem.getJSONArray("tags");
				for (int j = 0; j < tagsItems.length(); j++) {
					JSONObject tagItem = tagsItems.getJSONObject(j);
					fitItem.getTags().add(tagItem.getString("slug"));
				}

				// get an attachment from a post
				JSONArray attachmentItems = postItem
						.getJSONArray("attachments");
				if (attachmentItems.length() != 0) {
					JSONObject attachment = attachmentItems.getJSONObject(0);
					JSONObject images = attachment.getJSONObject("images");
					JSONObject full = images.getJSONObject("full");
					String urlImageFull = full.getString("url");

					if (!urlImageFull.equals("")) {
						fitItem.setUrlImage(urlImageFull);
					}
				}

				// add fitItem to the list of fitItems
				fitItems.add(fitItem);

			} catch (JSONException e) {
				// skip the current (i) post
			}

		}

		return fitItems;
	}
}
