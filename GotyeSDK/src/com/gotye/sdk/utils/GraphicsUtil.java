package com.gotye.sdk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 像素与dip转换，drawable与bitmap转换
 * 
 * @author lhxia
 * 
 */
public class GraphicsUtil {
	final static float scale = Resources.getSystem().getDisplayMetrics().density;

	/**
	 * Converts DIP to pixels.
	 * 
	 * @param dip
	 *            the value of the dip.
	 * @return the value of the pixels.
	 */
	public static int dipToPixel(int dip) {
		return (int) (dip * scale + 0.5f);
	}

	public static int pixelToDip(int px) {
		return (int) ((px - 0.5f) / scale);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		return bitmap;
	}

	public static Drawable BitmapToDrawable(final Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		return bd;
	}

	/**
	 * 通过图片url返回图片Bitmap
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	public static Bitmap returnBitMap(String path) throws IOException {
		URL url = null;
		Bitmap bitmap = null;
		url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 利用HttpURLConnection对象,我们可以从网络中获取网页数据.
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream(); // 得到网络返回的输入流
		bitmap = BitmapFactory.decodeStream(is);
		is.close();
		return bitmap;
	}
}
