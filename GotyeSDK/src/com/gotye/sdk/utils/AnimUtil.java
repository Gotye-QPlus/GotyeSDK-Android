package com.gotye.sdk.utils;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.gotye.sdk.R;

public class AnimUtil {

	public static AnimationDrawable getSpeakBgAnim(Resources resources) {
		AnimationDrawable speakBg = new AnimationDrawable();
		BitmapDrawable item = new BitmapDrawable(BitmapFactory.decodeResource(
				resources, R.drawable.gotye_talk_ring));
		speakBg.addFrame(item, 400);
		item = new BitmapDrawable(BitmapFactory.decodeResource(resources,
				R.drawable.gotye_talk_ring1));
		speakBg.addFrame(item, 400);
		item = new BitmapDrawable(BitmapFactory.decodeResource(resources,
				R.drawable.gotye_talk_ring2));
		speakBg.addFrame(item, 400);
		speakBg.setOneShot(false);
		return speakBg;
	}

	public static Animation createAnimationFromXml(Context c,
			Resources resources, int resid) {
		XmlResourceParser parser = null;
		try {
			parser = resources.getAnimation(resid);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

		return createAnimationFromXml(c, parser, null,
				Xml.asAttributeSet(parser));
	}

	public static Animation createAnimationFromXml(Context c,
			XmlPullParser parser, AnimationSet parent, AttributeSet attrs) {
		Animation anim = null;

		// Make sure we are on a start tag.
		int type;
		int depth = parser.getDepth();

		try {
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser
					.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				String name = parser.getName();

				if (name.equals("set")) {
					anim = new AnimationSet(c, attrs);
					createAnimationFromXml(c, parser, (AnimationSet) anim,
							attrs);
				} else if (name.equals("alpha")) {
					anim = new AlphaAnimation(c, attrs);
				} else if (name.equals("scale")) {
					anim = new ScaleAnimation(c, attrs);
				} else if (name.equals("rotate")) {
					anim = new RotateAnimation(c, attrs);
				} else if (name.equals("translate")) {
					anim = new TranslateAnimation(c, attrs);
				} else {
					throw new RuntimeException("Unknown animation name: "
							+ parser.getName());
				}

				if (parent != null) {
					parent.addAnimation(anim);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return anim;

	}
}
