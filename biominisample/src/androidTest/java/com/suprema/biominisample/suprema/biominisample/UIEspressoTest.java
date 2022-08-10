package com.suprema.biominisample.suprema.biominisample;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.suprema.biominisample.suprema.biominisample.Utility.appendLog;
import static com.suprema.biominisample.suprema.biominisample.Utility.setProgress;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.view.ViewPager;
import android.widget.SeekBar;

import com.suprema.BioMiniFactory;
import com.suprema.biominisample.MainActivity;
import com.suprema.biominisample.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ihkim_win10_x64 on 2017-08-22.
 */
public class UIEspressoTest {
  @Rule
  public ActivityTestRule<MainActivity> mActivity = new ActivityTestRule<MainActivity>(MainActivity.class);
  IBioMiniCyDevice _device;
  int nPagerRes[]= { R.layout.log_view , R.layout.setting_capture , R.layout.enrollment , R.layout.export};
  enum ePages{ LOG , SETTING , ENROLL, EXPORT};
  BioMiniFactory _bmFactory;
  private Context mainContext ;

  private static final int ENROLL_COUNT = 3;

  @Before
  public void TestInitialize(){
    mainContext = mActivity.getActivity().getApplicationContext();
    do {
      _device = mActivity.getActivity().mCurrentDevice;
    }while ( _device == null);
    IBioMiniCyDevice.Parameter param = new IBioMiniCyDevice.Parameter();
    param.type = IBioMiniCyDevice.ParameterType.TIMEOUT ;
    param.value= 5000;
    _device.setParameter(param);
  }

@Test
  public void startCapture() {
    SystemClock.sleep(1000);
    clearNotice();
    onView(withId(R.id.buttonStartCapturing)).perform(click());
    long _nTimeout = _device.getParameter(IBioMiniCyDevice.ParameterType.TIMEOUT).value;
    SystemClock.sleep(_nTimeout-1000);
    IsSuccess(R.string.capture_ok);
    onView(withId(R.id.buttonAbortCapturing)).perform(click());
    while( !_device.isCapturing() ){
      SystemClock.sleep(100);
    }
  }
  @Test
  public void capture(){
    SystemClock.sleep(1000);
    clearNotice();
    onView(withId(R.id.buttonCaptureSingle)).perform(click());
    long _nTimeout = _device.getParameter(IBioMiniCyDevice.ParameterType.TIMEOUT).value;
    SystemClock.sleep(_nTimeout);
    IsSuccess(R.string.capture_ok);
  }
  //@Test
  public void captureSetting(){
    SystemClock.sleep(1000);
    changePage(ePages.SETTING);
    clearNotice();
    onView(withId(R.id.layout_viewpager)).perform(swipeUp());
    onView(withId(R.id.buttonReadCaptureParam)).perform(click());
    IsSuccess(R.string.read_params_ok);
    SystemClock.sleep(1000);
    onView(withId(R.id.layout_viewpager)).perform(swipeDown());

    CaptureParams _oldParam = new CaptureParams(
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarSensitivity)).getProgress(),
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarSecurityLevel)).getProgress(),
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarTimeout)).getProgress()
    );
    appendLog("Old captureParams :" + _oldParam);

    CaptureParams param = new CaptureParams();
    param.generateRandomWithout(_oldParam);
    appendLog("Generated captureParams :" + param);

    onView(withId(R.id.seekBarSensitivity)).perform(setProgress(param.Sensitivity));
    onView(withId(R.id.seekBarSecurityLevel)).perform(setProgress(param.Security_level));
    onView(withId(R.id.seekBarTimeout)).perform(setProgress(param.Timeout));

    onView(withId(R.id.layout_viewpager)).perform(swipeUp());
    onView(withId(R.id.buttonWriteCaptureParam)).perform(click());
    IsSuccess(R.string.write_params_ok);
    onView(withId(R.id.layout_viewpager)).perform(swipeDown());
    appendLog("write Capture param");
    SystemClock.sleep(1000);

    // setting UI using psudo value for parameter check
    CaptureParams psudoParam = new CaptureParams();
    psudoParam.generateRandomWithout(param);

    onView(withId(R.id.seekBarSensitivity)).perform(setProgress(psudoParam.Sensitivity));
    onView(withId(R.id.seekBarSecurityLevel)).perform(setProgress(psudoParam.Security_level));
    onView(withId(R.id.seekBarTimeout)).perform(setProgress(psudoParam.Timeout));
    appendLog("setup psudo params." + psudoParam);
    SystemClock.sleep(1000);


    // read saved data.
    onView(withId(R.id.layout_viewpager)).perform(swipeUp());
    onView(withId(R.id.buttonReadCaptureParam)).perform(click());
    SystemClock.sleep(1000);
    onView(withId(R.id.layout_viewpager)).perform(swipeDown());

    CaptureParams readParam = new CaptureParams(
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarSensitivity)).getProgress(),
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarSecurityLevel)).getProgress(),
        ((SeekBar)mActivity.getActivity().findViewById(R.id.seekBarTimeout)).getProgress()
    );
    if( readParam.isDifferent(_oldParam)){
      appendLog("Capture Parameter setting OK ");
    }else{
      appendLog("Capture Parameter setting fail");
    }
  }
  //@Test
  public void enroll(){
    SystemClock.sleep(1000);
    changePage(ePages.ENROLL);
    clearNotice();
    // for Delete
    onView(withId(R.id.editUsername)).perform(clearText());
    onView(withId(R.id.editUsername)).perform(typeText("for_removeUser_test"));
    closeSoftKeyboard();
    SystemClock.sleep(1000);


    long _nTimeout = _device.getParameter(IBioMiniCyDevice.ParameterType.TIMEOUT).value;
    onView(withId(R.id.buttonEnroll)).perform(click());
    SystemClock.sleep(_nTimeout);
    IsSuccess(R.string.enroll_ok);
    // Delete
    onView(withId(R.id.buttonDeleteAll)).perform(click());


    //enroll
    for( int i =0 ; i< ENROLL_COUNT ; i++){
      onView(withId(R.id.editUsername)).perform(clearText());
      onView(withId(R.id.editUsername)).perform(typeText("Number"+i));
      closeSoftKeyboard();
      SystemClock.sleep(400);

      _nTimeout = _device.getParameter(IBioMiniCyDevice.ParameterType.TIMEOUT).value;
      onView(withId(R.id.buttonEnroll)).perform(click());
      SystemClock.sleep(_nTimeout);
    }

    //verify
    int nVerifyCount = 2;
    for( int i=0 ; i< nVerifyCount; i++) {
      onView(withId(R.id.buttonVerify)).perform(click());
      SystemClock.sleep(_nTimeout);
    }

  }

  @Test
  public void export(){
    SystemClock.sleep(1000);

    changePage(ePages.ENROLL);
    clearNotice();

    int nSaveCount=2;
    //for( int a=0 ; a< nSaveCount ; a++){
      long time = System.currentTimeMillis();
      SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss");
      String str = dayTime.format(new Date(time));
      onView(withId(R.id.editUsername)).perform(clearText());
      onView(withId(R.id.editUsername)).perform(typeText("USER_"+str));
      closeSoftKeyboard();

      onView(withId(R.id.buttonEnroll)).perform(click());
      long _nTimeout = _device.getParameter(IBioMiniCyDevice.ParameterType.TIMEOUT).value;
      SystemClock.sleep(_nTimeout);

      changePage(ePages.EXPORT);
      clearNotice();
      //bmp
      onView(withId(R.id.buttonExportBmp)).perform(click());
      SystemClock.sleep(400);
      IsSuccess(R.string.export_bmp_ok);
      //wsq
      onView(withId(R.id.buttonExportWsq)).perform(click());
      SystemClock.sleep(400);
      IsSuccess(R.string.export_wsq_ok);
      for( int i=0 ; i< 3; i++){
        onView(withId(R.id.spinnerTemplateType)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(i).perform(click());
        onView(withId(R.id.buttonTemplate)).perform(click());
        SystemClock.sleep(400);
        IsSuccess(R.string.export_template_ok);
      }
    SystemClock.sleep(200);
    //}
    changePage(ePages.LOG);
  }

  public void clearNotice(){
    mActivity.getActivity().clearState();
  }

  public void changePage(ePages pageName){
    clearNotice();
    ViewPager pager = (ViewPager)mActivity.getActivity().findViewById(R.id.viewpager);
    int _nCurrentPage = pager.getCurrentItem();
    int _nSetPage = pageName.ordinal();
    while( _nCurrentPage != _nSetPage ){
      if( _nCurrentPage < _nSetPage ){
        onView(withId(R.id.viewpager)).perform(swipeLeft());
      }else if( _nCurrentPage > _nSetPage ){
        onView(withId(R.id.viewpager)).perform(swipeRight());
      }
      _nCurrentPage = pager.getCurrentItem();
    }
    appendLog("Page Change :" + pageName.toString());

  }
  public void IsSuccess(int nStringResourceID){

    onView(withId(R.id.textStatus)).check(   matches(withText(nStringResourceID)));

    //onView(withId(R.id.textStatus)).check(matches(withText(containsString(mActivity.getActivity().getResources().getString(nStringResourceID)))));
  }
}


