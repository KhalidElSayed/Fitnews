package com.kogi.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class FitItem {

	private int id;
	private String titlePlain;
	private String excerpt;
	private String urlImage;
	private Bitmap imageFull;
	private ArrayList<String> tags;

	public FitItem() {
		tags = new ArrayList<String>();
	}

	public FitItem(int id, String titlePlain, String excerpt, String urlImage,
			ArrayList<String> tags) {
		super();
		this.id = id;
		this.titlePlain = titlePlain;
		this.excerpt = excerpt;
		this.urlImage = urlImage;
		this.tags = tags;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitlePlain() {
		return titlePlain;
	}

	public void setTitlePlain(String titlePlain) {
		this.titlePlain = titlePlain;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public String getUrlImage() {
		return urlImage;
	}

	public void setUrlImage(String urlImage) {
		this.urlImage = urlImage;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public Bitmap getImageFull() {
		return imageFull;
	}

	public void setImageFull(Bitmap imageFull) {
		this.imageFull = imageFull;
	}

}
