package com.gotye.sdk.ui.view.imageview;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.gotye.api.GotyeProgressListener;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.utils.ImageUtils;
import com.gotye.api.utils.Log;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.R;
import com.gotye.sdk.utils.GotyeFileCache;
import com.gotye.sdk.utils.GotyeFileCache.CachedData;
import com.gotye.sdk.utils.ImageCache;


public class AsycImageView extends ImageView {

	private String downloadUrl;
	private static ImageCache memCache = new ImageCache();
	private int defaultImage = R.drawable.gotye_default_icon;
	private Bitmap defaultImageBitmap;
	private int backId = R.drawable.gotye_bg_icon_nn;
	private Bitmap backIDBitmap;
	private boolean isRound = false;
	private int srcID;
	
	public AsycImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AsycImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AsycImageView(Context context) {
		super(context);
		init();
		
	}
	
	private void init(){
		setScaleType(ScaleType.FIT_XY);
		defaultImageBitmap = BitmapFactory.decodeResource(getResources(), defaultImage);
		backIDBitmap = BitmapFactory.decodeResource(getResources(), backId);
	}
	
	
	public void setImageBitmap(Bitmap bm, int id) {
		setBackID(id);
		if(bm == null){
			setImageBitmap(ImageUtils.toRoundCorner(getContext(), defaultImageBitmap, backIDBitmap));
			return;
		}
		super.setImageBitmap(bm);
	}
	
	private void setBackID(int id){
		if(backId == id){
			return;
		}
		backId = id;
		backIDBitmap = BitmapFactory.decodeResource(getResources(), id);
	}

	public void setImageBitmap(String downloadUrl, int id){
		setBackID(id);
		this.downloadUrl = downloadUrl;
		this.srcID = id;
		Bitmap bitmap = memCache.getImage(downloadUrl + "==" + id);
		if(bitmap != null){
			setImageBitmap(bitmap);
			return ;
		}
		bitmap = getBitmapFromFileCache(downloadUrl);
		if(bitmap != null){
			bitmap = ImageUtils.toRoundCorner(getContext(), bitmap, backIDBitmap);
			memCache.putImage(downloadUrl + "==" + id, bitmap);
			
			setImageBitmap(bitmap);
			return ;
		}
		setImageBitmap(ImageUtils.toRoundCorner(getContext(), defaultImageBitmap, backIDBitmap));
		downloadImage(downloadUrl);
	}
	
	protected Bitmap getBitmapFromFileCache(final String downlaodUrl){
		CachedData data = GotyeFileCache.getFileCacheByUser().get(getContext(), GotyeFileCache.CACHE_IMAGE, downlaodUrl);
		if(data == null){
			return null;
		}
		return BitmapFactory.decodeByteArray(data.mData, data.mOffset, data.mData.length - data.mOffset);
		
	}
	
	protected boolean downloadImage(final String downlaodUrl){
		return imageListener.startDownload(getContext(), downlaodUrl, this);
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	public int getDefaultImage() {
		return defaultImage;
	}

	public void setDefaultImage(int defaultImage) {
		if(this.defaultImage == defaultImage){
			return;
		}
		this.defaultImage = defaultImage;
		defaultImageBitmap = BitmapFactory.decodeResource(getResources(), defaultImage);
	}

	public int getSrcID() {
		return backId;
	}

	private static class ImageDownloadListener implements GotyeProgressListener{
		private HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String,Object>>();
		private HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		private Context context;
		
		public synchronized boolean startDownload(Context context, String resID, AsycImageView imageView){
			this.context = context;
			HashMap<String, Object> m = map.get(resID);
			if(m == null){
				Integer count = countMap.get(resID);
				if(count == null){
					count = 0;
				}else {
					count++;
				}
				if(count > 10){
					return false;
				}
				countMap.put(resID, count);
				m = new HashMap<String, Object>();
				map.put(resID, m);
				m.put("stream", new ByteArrayOutputStream());
				Set<AsycImageView> listeners = new HashSet<AsycImageView>();
				m.put("listeners", listeners);
				synchronized (listeners) {
					listeners.add(imageView);
				}
				try {
					if(!StringUtil.isEmpty(resID)){
						InnerConstants.getAPI(context).downloadRes(resID, null,this);
					}else {
						m.clear();
						map.remove(resID);
						return false;
					}
				} catch (Exception e) {
//					e.printStackTrace();
					m.clear();
					map.remove(resID);
					return false;
				}
			}else {
				Set<AsycImageView> listeners = (Set<AsycImageView>) m.get("listeners");
				synchronized (listeners) {
					listeners.add(imageView);
				}
			}
			return true;
			
		}
		
		@Override
		public void onDownloadRes(String appKey, String username,
				final String resID, String path, int code) {
			synchronized (this) {
				ByteArrayOutputStream bout = (ByteArrayOutputStream) map.get(resID).get("stream");
				if(code == GotyeStatusCode.STATUS_OK){
					byte[] downloadData = bout.toByteArray();
					GotyeFileCache.getFileCacheByUser().put(context, GotyeFileCache.CACHE_IMAGE, resID, ImageUtils.getResizedImageData(downloadData, 400, 400, 400, 400, 1024 * 100));
				
					Set<AsycImageView> listeners = (Set<AsycImageView>) map.get(resID).get("listeners");
					synchronized (listeners) {
						for(final AsycImageView imageView : listeners){
							
								imageView.post(new Runnable() {
									
									@Override
									public void run() {
										if(("" + imageView.getDownloadUrl()).equals(resID)){
											Log.d("", "notify image change ");
											imageView.setImageBitmap(resID, imageView.getSrcID());
											Animation anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
											anim.setDuration(1500);
											imageView.startAnimation(anim);
										}
									}
								});
						}
					}
				}else {
					//下载失败
				}
				
				
				map.remove(resID);
			}
		
			
		}

		@Override
		public void onProgressUpdate(String appKey, String username,
				String resID, String path, long totalByte,
				long downloadByte, byte[] data, int offset, int len) {
			synchronized (map) {
				ByteArrayOutputStream bout = (ByteArrayOutputStream) map.get(resID).get("stream");
				bout.write(data, offset, len);
			}
		}
	}

	private static ImageDownloadListener imageListener =  new ImageDownloadListener();
}
