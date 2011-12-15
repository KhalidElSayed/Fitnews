package com.kogi.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kogi.model.FitItem;

public class ConsumerWebServices {

	public static String URL_FIT_NEWS = "http://66.228.57.165/wordpress/?json=get_recent_post&";

	public static ArrayList<FitItem> getNewsData(String count, String page)
			throws JSONException {

		// Building the request
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet();
		URI uri;
		ArrayList<FitItem> fitItems = new ArrayList<FitItem>();
		try {
			uri = new URI(URL_FIT_NEWS + "count=20" + "&" + "page=" + page);
			getRequest.setURI(uri);

			// getting the response
			HttpResponse httpResponse = httpClient.execute(getRequest);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				// TODO search another way to return the answer with the error
				// code (some costum exception)
				return fitItems;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					httpResponse.getEntity().getContent()));

			// getting data from response
			String line;
			while ((line = in.readLine()) != null) {

				JSONObject jsonObject = new JSONObject(line);
				JSONArray postItems = jsonObject.getJSONArray("posts");

				for (int i = 0; i < postItems.length(); i++) {

					try {
						JSONObject postItem = postItems.getJSONObject(i);
						FitItem fitItem = new FitItem();
						fitItem.setId(postItem.getInt("id"));
						fitItem.setTitlePlain(postItem.getString("title_plain"));
						fitItem.setExcerpt(postItem.getString("excerpt"));

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
							JSONObject attachment = attachmentItems
									.getJSONObject(0);
							JSONObject images = attachment
									.getJSONObject("images");
							JSONObject full = images.getJSONObject("full");
							String urlImageFull = full.getString("url");

							if (!urlImageFull.equals("")) {
								fitItem.setUrlImage(urlImageFull);
							}
						}

						// add fitItem to the list of fitItems
						fitItems.add(fitItem);

					} catch (JSONException e) {
						// skip one iteration
					}

				}

			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new JSONException("Error process the URI");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new JSONException("Error to connect to internet");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new JSONException("Error in the vm");
		} catch (IOException e) {
			e.printStackTrace();
			throw new JSONException("Error to connect to internet");
		}
		return fitItems;
	}
}
