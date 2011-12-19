package com.kogi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class DecoderImages {

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Bitmap getBitmapReSize(Bitmap bm1, int max) {
		final int IMAGE_MAX_SIZE = max;
		int heigth = bm1.getHeight();
		int width = bm1.getWidth();
		int newScale = 1;
		if (heigth > IMAGE_MAX_SIZE || width > IMAGE_MAX_SIZE) {
			newScale = (int) Math.pow(
					2,
					(int) Math.round(Math.log(IMAGE_MAX_SIZE
							/ (double) Math.max(heigth, width))
							/ Math.log(0.5)));
		}
		Matrix matrix = new Matrix();
		// resize the bit map
		//Log.e("new scale", " " + newScale);
		matrix.postScale((float) 1 / newScale, (float) 1 / newScale);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bm1, 0, 0, width, heigth, matrix, true);
	}
}
