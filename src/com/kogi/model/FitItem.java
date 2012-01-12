package com.kogi.model;

import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;

public class FitItem {

	private int id;
	private String titlePlain;
	private String excerpt;
	private String content;
	private String urlContent;
	private Date date;
	// initial image (fits item list)
	private String urlInitImage;
	private Bitmap initImage;
	// carousel images
	private ArrayList<String> urlsImages;
	private ArrayList<Bitmap> images;
	private ArrayList<String> tags;

	public FitItem() {
		tags = new ArrayList<String>();
		urlsImages = new ArrayList<String>(0);
		images = new ArrayList<Bitmap>(0);
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrlContent() {
		return urlContent;
	}

	public void setUrlContent(String urlContent) {
		this.urlContent = urlContent;
	}

	public String getUrlInitImage() {
		return urlInitImage;
	}

	public void setUrlInitImage(String urlImage) {
		this.urlInitImage = urlImage;
	}

	public ArrayList<Bitmap> getImages() {
		return images;
	}

	public void setImages(ArrayList<Bitmap> images) {
		this.images = images;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ArrayList<String> getUrlsImages() {
		return urlsImages;
	}

	public void setUrlsImages(ArrayList<String> urlsImages) {
		this.urlsImages = urlsImages;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public Bitmap getInitImage() {
		return initImage;
	}

	public void setInitImage(Bitmap imageFull) {
		this.initImage = imageFull;
	}

}
