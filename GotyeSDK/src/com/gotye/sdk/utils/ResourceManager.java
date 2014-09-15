/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gotye.sdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import android.content.Context;
import android.net.Uri;

import com.gotye.api.GotyeProgressListener;
import com.gotye.api.net.GotyeRequestFuture;
import com.gotye.api.utils.StringUtil;
import com.gotye.sdk.InnerConstants;
import com.gotye.sdk.utils.ImageCacheService.ImageData;

/**
 * Primary {@link ResourceManager} implementation used by {@link MessagingApplication}.
 * <p>
 * Public methods should only be used from a single thread (typically the UI
 * thread). Callbacks will be invoked on the thread where the ThumbnailManager
 * was instantiated.
 * <p>
 * Uses a thread-pool ExecutorService instead of AsyncTasks since clients may
 * request lots of pdus around the same time, and AsyncTask may reject tasks
 * in that case and has no way of bounding the number of threads used by those
 * tasks.
 * <p>
 * ThumbnailManager is used to asynchronously load pictures and create thumbnails. The thumbnails
 * are stored in a local cache with SoftReferences. Once a thumbnail is loaded, it will call the
 * passed in callback with the result. If a thumbnail is immediately available in the cache,
 * the callback will be called immediately as well.
 *
 * Based on BooksImageManager by Virgil King.
 */
public class ResourceManager extends BackgroundLoaderManager {
    private static final String TAG = "ResManager";


    private final Context mContext;
    private ImageCacheService mImageCacheService;
    private static ResourceManager instance;
    
    public static synchronized ResourceManager getInstance(Context context){
    	if(instance == null){
    		instance = new ResourceManager(context.getApplicationContext());
    	}
    	return instance;
    }

    public ResourceManager(final Context context) {
        super(context);
        mContext = context;
        mImageCacheService = new ImageCacheService(context.getApplicationContext());
    }

    /**
     * getThumbnail must be called on the same thread that created ThumbnailManager. This is
     * normally the UI thread.
     * @param uri the uri of the image
     * @param width the original full width of the image
     * @param height the original full height of the image
     * @param callback the callback to call when the thumbnail is fully loaded
     * @return
     */
    public ItemLoadedFuture getThumbnail(String resid, Uri uri,
            final ItemLoadedCallback<ResLoaded> callback, Object tag) {
        return getThumbnail(ImageCacheService.TYPE_RES, resid, uri, false, callback, tag);
    }
    
    public ItemLoadedFuture getThumbnail(String resid, int type, Uri uri,
            final ItemLoadedCallback<ResLoaded> callback, Object tag) {
        return getThumbnail(type, resid, uri, false, callback, tag);
    }

    private ItemLoadedFuture getThumbnail(int type, String resid, Uri uri, boolean isVideo,
            final ItemLoadedCallback<ResLoaded> callback, Object tag) {
        if (uri == null) {
            throw new NullPointerException();
        }
        NullItemLoadedFuture fu = new NullItemLoadedFuture();
        if(StringUtil.isEmpty(resid)){
            ResLoaded imageLoaded = new ResLoaded(fu, tag, resid, null);
            callback.onItemLoaded(imageLoaded, null);
            return fu;
        }

        final ImageData thumbnail = mImageCacheService.getImageData(resid, type);
        
        boolean thumbnailExists = (thumbnail != null);
        
        if(thumbnailExists && (thumbnail.mData == null || 0 == (thumbnail.mData.length - thumbnail.mOffset))){
        	thumbnailExists = false;
        }
        
        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean newTaskRequired = !thumbnailExists && !taskExists;
        final boolean callbackRequired = (callback != null);

        if (thumbnailExists) {
        	
            if (callbackRequired) {
                if (callbackRequired) {
                    addCallback(uri, callback);
                }
                final Set<ItemLoadedCallback> callbacks = mCallbacks.get(uri);
                ResLoaded imageLoaded = new ResLoaded(fu, tag, resid, thumbnail);
                if (callbacks != null) {

                    // Make a copy so that the callback can unregister itself
                    for (final ItemLoadedCallback<ResLoaded> c : asList(callbacks)) {
                        c.onItemLoaded(imageLoaded, null);
                    }
                } 
//                callback.onItemLoaded(imageLoaded, null);
            }
            return fu;
        }

        if (callbackRequired) {
            addCallback(uri, callback);
        }

        ItemLoadedFuture f= new ItemLoadedFuture() {
            private boolean mIsDone;

            @Override
            public void cancel(Uri uri) {
                cancelCallback(callback);
//                removeThumbnail(uri);   // if the thumbnail is half loaded, force a reload next time
            }

            @Override
            public void setIsDone(boolean done) {
                mIsDone = done;
            }

            @Override
            public boolean isDone() {
                return mIsDone;
            }
        };
        
        if (newTaskRequired) {
            mPendingTaskUris.add(uri);
            Runnable task = new LoadResTask(f, tag, type, resid, uri, isVideo);
            mExecutor.execute(task);
        }
        return f;
    }

    @Override
    public synchronized void clear() {
        super.clear();

        clearBackingStore();        // clear on-disk cache
    }

    // Delete the on-disk cache, but leave the in-memory cache intact
    public synchronized void clearBackingStore() {
        if (mImageCacheService == null) {
            // No need to call getImageCacheService() to renew the instance if it's null.
            // It's enough to only delete the image cache files for the sake of safety.
            CacheManager.clear(mContext);
        } else {
            getImageCacheService().clear();

            // force a re-init the next time getImageCacheService requested
            mImageCacheService = null;
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public synchronized ImageCacheService getImageCacheService() {
        return mImageCacheService;
    }

    public class LoadResTask implements Runnable {
        private final Uri mUri;
        private final boolean mIsVideo;
        private final String downloadUrl;
        private int type;
        private Object tag;
        public ItemLoadedFuture fu;

        public LoadResTask(ItemLoadedFuture fu, Object tag, int type, String downloadUrl, Uri uri, boolean isVideo) {
            if (uri == null) {
                throw new NullPointerException();
            }
            this.type = type;
            this.downloadUrl = downloadUrl;
            mUri = uri;
            mIsVideo = isVideo;
            this.tag = tag;
            this.fu = fu;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {

        	Exception exception = null;
        	
           //download res
        	final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        	FileInputStream fin = null;
			try {
				GotyeRequestFuture fu = InnerConstants.getAPI(mContext).downloadRes(downloadUrl, null, new GotyeProgressListener() {
					
					@Override
					public void onProgressUpdate(String appKey, String username, String resID,
							String path, long totalByte, long downloadByte, byte[] data,
							int offset, int len) {
						bout.write(data, offset, len);
					}
					
					@Override
					public void onDownloadRes(String appKey, String username, String resID,
							String path, int code) {
						
					}
				});
				boolean success = (Boolean) fu.get();
				if(success){
					mImageCacheService.putImageData(downloadUrl, type, bout.toByteArray());
				}
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			} finally {
				if(fin != null){
					try {
						fin.close();
					} catch (IOException e) {
					}
				}
			}
        	
        	byte[] bytes = bout.toByteArray();
        	final ImageData result = new ImageData(bytes, 0);

        	final Exception e = exception;
            mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                    if (callbacks != null) {

                        // Make a copy so that the callback can unregister itself
                        for (final ItemLoadedCallback<ResLoaded> callback : asList(callbacks)) {
                        	ResLoaded imageLoaded = new ResLoaded(fu, tag, downloadUrl, result);
                             callback.onItemLoaded(imageLoaded, e);
                        }
                    } 

                    // Add the bitmap to the soft cache if the load succeeded. Don't cache the
                    // stand-ins for empty bitmaps.

                    mCallbacks.remove(mUri);
                    mPendingTaskUris.remove(mUri);

                }
            });
        }

 

    }

    public static class ResLoaded {
        public final String downloadUrl;
        public final ImageData data;
        public final Object tag;
        public final ItemLoadedFuture fu;

        public ResLoaded(ItemLoadedFuture fu, Object tag, String downloadUrl, ImageData data) {
            this.downloadUrl = downloadUrl;
            this.data = data;
            this.tag = tag;
            this.fu = fu;
        }
    }
}
