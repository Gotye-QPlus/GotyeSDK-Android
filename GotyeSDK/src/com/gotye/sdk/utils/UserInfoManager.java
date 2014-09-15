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

import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.gotye.api.GotyeStatusCode;
import com.gotye.api.bean.GotyeUser;
import com.gotye.api.net.GotyeRequestFuture;
import com.gotye.sdk.InnerConstants;

/**
 * Primary {@link UserInfoManager} implementation used by {@link MessagingApplication}.
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
public class UserInfoManager extends BackgroundLoaderManager {
    private static final String TAG = "ResManager";

    private final static int TIMEOUT = 120000;
    private final Context mContext;
    private static UserInfoManager instance;
    private HashMap<String, TimeUser> mCache;
    
    public static synchronized UserInfoManager getInstance(Context context){
    	if(instance == null){
    		instance = new UserInfoManager(context.getApplicationContext());
    	}
    	return instance;
    }

    public UserInfoManager(final Context context) {
        super(context);
        mContext = context;
        mCache = new HashMap<String, TimeUser>();        
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
    public ItemLoadedFuture getThumbnail(GotyeUser user,
            final ItemLoadedCallback<UserLoaded> callback, Object tag) {
    	
        return getThumbnail(user, Uri.parse(user.getUsername()), false, callback, tag);
    }

    private ItemLoadedFuture getThumbnail(GotyeUser user, Uri uri, boolean isVideo,
            final ItemLoadedCallback<UserLoaded> callback, Object tag) {
        if (uri == null) {
            throw new NullPointerException();
        }
        
        TimeUser item = mCache.get(user.getUsername());
        boolean thumbnailExists = (item != null);
        
        final boolean taskExists = mPendingTaskUris.contains(uri);
        final boolean newTaskRequired = !thumbnailExists && !taskExists;
        final boolean callbackRequired = (callback != null);
        
        if (callbackRequired) {
            addCallback(uri, callback);
        }

        if (thumbnailExists) {
        	NullItemLoadedFuture f = new NullItemLoadedFuture();;
        	boolean timeout = ((System.currentTimeMillis() - item.putTime) > TIMEOUT) ? true : false;
        	final GotyeUser thumbnail = item.user;
            if (callbackRequired) {
            	UserLoaded imageLoaded = new UserLoaded(tag, thumbnail, f);
//                callback.onItemLoaded(imageLoaded, null);
                final Set<ItemLoadedCallback> callbacks = mCallbacks.get(uri);
                if (callbacks != null) {

                    // Make a copy so that the callback can unregister itself
                    for (final ItemLoadedCallback<UserLoaded> c : asList(callbacks)) {
                    	Log.d("", "call back " + imageLoaded.user.getUsername() + " " + imageLoaded.user.getNickName());
                        c.onItemLoaded(imageLoaded, null);
                    }
                } 
            }
            if(timeout){
       		 	mCache.remove(user.getUsername());
            }
            if(!timeout){
            	mCallbacks.remove(uri);
            	Log.d(TAG, "find userinfo");
            	return f;
            }else {
            	Log.d(TAG, "find userinfo timeout");
            }
        }

        ItemLoadedFuture f =  new ItemLoadedFuture() {
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
            Runnable task = new LoadResTask(tag, user, uri, f);
            mExecutor.execute(task);
        }
        return f;
    }

    @Override
    public synchronized void clear() {
        super.clear();

        mCache.clear();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public class LoadResTask implements Runnable {
        private final Uri mUri;
        private final ItemLoadedFuture mIsVideo;
        private final GotyeUser user;
        private final Object tag;

        public LoadResTask(Object tag, GotyeUser user, Uri uri, ItemLoadedFuture isVideo) {
            if (uri == null) {
                throw new NullPointerException();
            }
            this.user = user;
            mUri = uri;
            mIsVideo = isVideo;
            this.tag = tag;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {

        	Exception exception = null;
        	
           //download res
        	try {
        		GotyeRequestFuture fu = InnerConstants.getAPI(mContext).getUserInfo(user);
				fu.get();
				if(fu.getCode() == GotyeStatusCode.STATUS_OK){
	        		
	        		TimeUser item = new TimeUser();
	        		item.putTime = System.currentTimeMillis();
	        		item.user = user;
	        		mCache.put(user.getUsername(), item);
	        	}
			} catch (Exception e1) {
				e1.printStackTrace();
				exception = e1;
			}

        	final Exception e = exception;
            mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Set<ItemLoadedCallback> callbacks = mCallbacks.get(mUri);
                    if (callbacks != null) {

                        // Make a copy so that the callback can unregister itself
                        for (final ItemLoadedCallback<UserLoaded> callback : asList(callbacks)) {
                        	 UserLoaded imageLoaded = new UserLoaded(tag, user, mIsVideo);
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

    public static class UserLoaded {
        public final GotyeUser user;
        public final ItemLoadedFuture fu;
        public final Object tag;

		public UserLoaded(Object tag, GotyeUser user, ItemLoadedFuture fu) {
			super();
			this.user = user;
			this.fu = fu;
			this.tag = tag;
		}

    }
    
    private static class TimeUser {
    	long putTime;
    	GotyeUser user;
    }
}
