package com.suprema.biominisample.suprema.biominisample;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.suprema.biominisample.suprema.biominisample.Utility.appendLog;
import static org.hamcrest.core.StringContains.containsString;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.core.deps.guava.net.InetAddresses;
import android.support.test.espresso.matcher.ViewMatchers;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.suprema.biominisample.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Created by ihkim_win10_x64 on 2017-08-31.
 */

class Utility{
  public  static final String TAG = "BioMiniSDK[ESPRESSO] ";
  public static void appendLog(String string){
    Log.d(TAG , string);
  }


  public static void containsText(String compareText){
    onView(withId(R.id.textStatus)).check(matches(withText(containsString(compareText))));
  }

  public static ViewAction setProgress(final int progress) {
    return new ViewAction() {
      @Override
      public void perform(UiController uiController, View view) {
        ((SeekBar) view).setProgress(progress);
      }

      @Override
      public String getDescription() {
        return "Set a progress";
      }

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(SeekBar.class);
      }
    };
  }
  public static Matcher<View> detectChangeWithImageView (final Drawable new_one){
    return new DrawableMatcher(new_one);
  }

}

class NotifyImageViewChange{
  final int MAX_RETRY = 40;
  Drawable _origin_drawable;
  Bitmap _origin_bitmap ;
  ImageView _view;
  public void setView(ImageView view){
    this._view = view;
  }
  public void keepState(){
    //_origin_bitmap = ((BitmapDrawable) _view.getDrawable()).getBitmap();
    _origin_drawable = _view.getDrawable();
  }
  public boolean waitFor(){
    boolean nRet = true;
    int nCount =0 ;
    while( nRet == true || nCount <MAX_RETRY){
      nRet = isEquals(nCount);
      nCount++;
      Log.e(Utility.TAG , "nRet : " + nRet + "  / nCount : " + nCount);
      SystemClock.sleep(100);
    }
    if( nCount == MAX_RETRY ){
      Log.e(Utility.TAG , "retry count is full..");
      return false;
    }
    _origin_drawable = null;
    return true;
  }
  public boolean isEquals(int n){
    Bitmap _origin = getBitmap(_origin_drawable);
    Bitmap _new =getBitmap(_view.getDrawable());

    if( _origin_drawable ==null && _view.getDrawable() != null){
      Log.e(Utility.TAG , "original_drawable is null and _new view is not null");
      return true;
    }
    //
    DumpBitmap(_origin , String.format(Locale.ENGLISH , "%03d_origin.jpg"));
    DumpBitmap(_new, String.format(Locale.ENGLISH , "%03d_new.jpg"));
    //
    return _new.sameAs(_origin);
  }

  private void DumpBitmap(Bitmap bitmap , String filename){
    if( bitmap == null){
      Log.e(Utility.TAG, " bitmap is null");
      return ;
    }
    Log.e(Utility.TAG, " Dump bitmap filename " + filename);
    String strFilePath = "/sdcard/Pictures/";
    File file = new File(strFilePath);

    if (!file.exists())
      file.mkdirs();

    File fileCacheItem = new File(strFilePath + filename);
    OutputStream out = null;

    try {
      fileCacheItem.createNewFile();
      out = new FileOutputStream(fileCacheItem);

      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private Bitmap getBitmap(Drawable drawable) {
    if (drawable == null){
      appendLog("Function GetBitmap :  drawable is null");
      return null;
    }else if (drawable.getIntrinsicWidth() <0 ||drawable.getIntrinsicHeight() <0  ){
      appendLog("Function GetBitmap :  width or height < 0");
      return null;
    }
    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }
}


class DrawableMatcher extends TypeSafeMatcher<View>{
  Drawable _origin;
  public DrawableMatcher(Drawable origin ) {
    this._origin = origin;
  }
  @Override
  protected boolean matchesSafely(View target) {

    Drawable img_origin = null;
    ImageView img_new = null;
    if( target instanceof ImageView && _origin instanceof  Drawable){
      img_origin = (Drawable)_origin;
      img_new =(ImageView)target;
    }else {
      Log.e(Utility.TAG ,"View is not a instance of ImageView ");
    }
    Bitmap bitmap = getBitmap(img_origin);
    Bitmap otherBitmap = getBitmap(img_new.getDrawable());
    return bitmap.sameAs(otherBitmap);
  }

  private Bitmap getBitmap(Drawable drawable){
    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  @Override
  public void describeTo(Description description) {

  }
}