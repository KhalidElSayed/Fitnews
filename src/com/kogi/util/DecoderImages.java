package com.kogi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class DecoderImages {

	private Context ctx;

	public DecoderImages(Context ctx) {
		this.ctx = ctx;
	}

	public Bitmap getBitmapFromURL(String src) {
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

	public Bitmap getBitmapReSize(Bitmap bm1, int max) {
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
		// Log.e("new scale", " " + newScale);
		matrix.postScale((float) 1 / newScale, (float) 1 / newScale);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bm1, 0, 0, width, heigth, matrix, true);
	}

	public boolean saveBitmap(String dir, String nameBmp, Bitmap bitmap) {
		File dirFile = ctx.getDir(dir, Context.MODE_PRIVATE);
		String newFilePath = String.format("%s%s%s", dirFile.getAbsolutePath(),
				File.separator, nameBmp);
		Log.d("===== savebitmap >>>>", newFilePath);
		FileOutputStream outStream = null;
		boolean savedImage = false;
		try {
			outStream = new FileOutputStream(newFilePath);
			savedImage = bitmap.compress(Bitmap.CompressFormat.PNG, 100,
					outStream);
			outStream.flush();
		} catch (IOException e) {
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}

		return savedImage;
	}

	public boolean saveImageToExternalStorage(String dir, String bmpName, Bitmap bmp ) {

		OutputStream fOut = null;
		boolean savedImage = false;
		try {			
			File file = new File(dir, bmpName);
			fOut = new FileOutputStream(file);
			savedImage = bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			//MediaStore.Images.Media.insertImage(ctx.getContentResolver(),
					//file.getAbsolutePath(), file.getName(), file.getName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return savedImage;
	}

}
