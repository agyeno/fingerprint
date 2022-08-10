package com.suprema.biominisample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;
import com.suprema.android.BioMiniJni;
import com.suprema.util.Logger;
import com.telpo.tps550.api.fingerprint.FingerPrint;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

public class DefaultActivity extends AppCompatActivity implements View.OnClickListener,BasicFuncFragment.OnFragmentInteractionListener,
                                                                    SettingFragment.OnFragmentInteractionListener {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG ="BioMini";
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private static final int FILE_SELECT_CODE = 511;
    String cardOwner_,cardNumber_,clockinType_;


    //Object
    PowerManager.WakeLock mWakeLock = null;
    private Context mContext;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private boolean mIsCapturing;
//    private PagerAdapter mAdapter;
    private BasicFuncFragment mBasicFuncFragment;
    private SettingFragment mSettingFragment;
    private int currentViewPosition = 0;
    private boolean mCurrentNightMode = false;
    private UsbManager mUsbManager;
    public UsbDevice mUsbDevice;
    private PendingIntent mPermissionIntent;
    private TextView mLogView;
    private BioMiniFactory mBioMiniFactory;
    public IBioMiniDevice mCurrentDevice;
    private IBioMiniDevice.CaptureOption mCaptureOption = new IBioMiniDevice.CaptureOption();
    private ArrayList<UserData> mUsers = new ArrayList<UserData>();
    private int mDetect_core = 0;
    private int mTemplateQualityEx = 0;
    private String mUserName = "";
    private String mEncyptKey = "";
    private IBioMiniDevice.TemplateData mTemplateData;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private ViewPagerFragmentAdapter mAdapter;
    private TabLayoutMediator mTabLayoutMediator;
    private Bitmap  mCaptureImage;
    private IBioMiniDevice.TransferMode mTransferMode;
    private long mCaptureStartTime = 0;
    private boolean isAbortCapturing = false;

    //UI Component
    ImageView   mImageView;
    TextView    mDeviceTextView;
    TextView    mSerialNumTextView;
    TextView    mFwVersionTextViewMenu;
    TextView    mFwVersionTextView;
    TextView    mSdkInfoTextView;
    Button      mCaptureSingleButton;
    Button      mPreviewButton;
    Button      mAutoCaptureButton;
    Button      mAbortCaptureButton;
    Button      mEnrollmentButton;
    Button      mVerifyButton;
    Button      mEnrollDummyButton;
    Button      mDeleteAllUserButton;
    Button      mExportBmpButton;
    Button      mExportWsqButton;
    Button      mExport19794Button;
    Button      mExportRawButton;
    Button      mExportTemplateButton;

    //basic event
    private static final int BASE_EVENT = 3000;
    private static final int ACTIVATE_USB_DEVICE = BASE_EVENT + 1;
    private static final int REMOVE_USB_DEVICE = BASE_EVENT + 2;
    private static final int UPDATE_DEVICE_INFO = BASE_EVENT + 3;
    private static final int REQUEST_USB_PERMISSION = BASE_EVENT+4;
    private static final int MAKE_DELAY_1SEC = BASE_EVENT+5;
    private static final int ADD_DEVICE = BASE_EVENT+6;
    private static final int CLEAR_VIEW_FOR_CAPTURE = BASE_EVENT+8;
    private static final int SET_TEXT_LOGVIEW = BASE_EVENT+10;
    private static final int MAKE_TOAST = BASE_EVENT+11;
    private static final int SHOW_CAPTURE_IMAGE_DEVICE = BASE_EVENT+12;

    //menu event
    private static final int BASE_MENU_EVENT = 4000;
    private static final int FAST_MODE_MENU = BASE_MENU_EVENT + 1;
    private static final int CROP_MODE_MENU = BASE_MENU_EVENT + 2;
    private static final int SENSITIVITY_MENU = BASE_MENU_EVENT + 3;
    private static final int SECURITY_MENU = BASE_MENU_EVENT + 4;
    private static final int TIMEOUT_MENU = BASE_MENU_EVENT + 5;
    private static final int DEVICE_LFD_MENU = BASE_MENU_EVENT + 6;
    private static final int SW_LFD_MENU = BASE_MENU_EVENT + 7;
    private static final int Ext_Trigger_MENU = BASE_MENU_EVENT + 8;
    private static final int Auto_Sleep_MENU = BASE_MENU_EVENT + 9;
    private static final int Auto_Rotate_MENU = BASE_MENU_EVENT + 10;
    private static final int Detect_Core_MENU = BASE_MENU_EVENT + 11;
    private static final int Template_Quality_Ex_MENU = BASE_MENU_EVENT + 12;
    private static final int Firmware_Update_MENU = BASE_MENU_EVENT + 13;

    //File Browser Event
    private static final int BASE_FILE_BROWSER_EVENT = 5000;
    private static final int FIRMWARE_UPDATE_EVENT = BASE_FILE_BROWSER_EVENT+1;
    private static final int LFD_FROM_IMAGE_FILE_EVENT = BASE_FILE_BROWSER_EVENT+2;


    //Device ID
    private static final int BioMiniOC4 = 0x0406;
    private static final int BioMiniSlim = 0x0407;
    private static final int BioMiniSlim2 = 0x0408;
    private static final int BioMiniPlus2 = 0x0409;
    private static final int BioMiniSlimS = 0x0420;
    private static final int BioMiniSlim2S = 0x0421;
    private static final int BioMiniSlim3 = 0x0460;
    private static final int BioMiniSlim3HID = 0x0423;

    class UserData {
        String name;
        byte[] template;
        public UserData(String name, byte[] data, int len) {
            this.name = name;
            this.template = Arrays.copyOf(data, len);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("START!");

        setContentView(R.layout.default_main);

        Intent intent = getIntent();
        cardOwner_ = intent.getStringExtra("_FullName");
        cardNumber_ = intent.getStringExtra("_CardNumber");
        clockinType_ = intent.getStringExtra("_ClockinType");


        if(mContext == null)
            mContext = this;

        requestWakeLock();

        setDefaultToolBar();

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager2) findViewById(R.id.pager);

        setupTablayout();
        setupViewPager(mViewPager);

        checkCurrentSystemTheme();

        if(mUsbManager == null)
            mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        initUsbListener();
//        addDeviceToUsbDeviceList();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Logger.d("START!");
        if(!mWakeLock.isHeld())
            mWakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d("START!");
        if(mWakeLock.isHeld())
            mWakeLock.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d("START!");
        if(mWakeLock.isHeld())
            mWakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("START!");
        int result = 0;
        if(mCurrentDevice != null)
        {
            if(mCurrentDevice.isCapturing())
            {
                doAbortCapture();
                while (mCurrentDevice.isCapturing())
                {
                    SystemClock.sleep(10);
                }
            }
        }
        if(mBioMiniFactory != null)
        {
            if(mUsbDevice != null)
                result = mBioMiniFactory.removeDevice(mUsbDevice);

            if(result == IBioMiniDevice.ErrorCode.OK.value() || result == IBioMiniDevice.ErrorCode.ERR_NO_DEVICE.value())
            {
                mBioMiniFactory.close();
                mContext.unregisterReceiver(mUsbReceiver);
                mUsbDevice = null;
                mCurrentDevice = null;
            }
        }

        FingerPrint.fingerPrintPower(0);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    /**
     * 1. Processing the result of permission request.
     * @param requestCode request code in result
     * @param permissions permission type in result
     * @param grantResults result of permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setLogInTextView("write permission granted");
            requestBatteryOptimization();
        }
    }

    /**
     * 1. Handles UI button click events.
     * @param v view object
     */
    @Override
    public void onClick(View v) {
        if(mCurrentDevice == null)
        {
            setLogInTextView(mContext.getResources().getString(R.string.error_device_not_conneted));
            return;
        }
        String err_other_capture_running = mContext.getResources().getString(R.string.error_other_capture_runnging);
        switch (v.getId())
        {
            case R.id.bt_capturesingle:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_capturesingle clicked!");
                doSinlgeCapture();
                break;
            case R.id.bt_startpreview:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_startpreview clicked!");
                doCapturePreview();
                break;
            case R.id.bt_autocapture:
                if(mCurrentDevice.isCapturing() == true)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_autocapture clicked!");
                doAutoCapture();
                break;
            case R.id.bt_abortcapture:
                Logger.d("bt_abortcapture clicked!");
                if(isAbortCapturing && mCurrentDevice.isCapturing())
                {
                    Logger.d("abortCapturing already started.");
                    setUiClickable(false);
                    return;
                }
                doAbortCapture();
                break;
            case R.id.bt_enrollment:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_enrollment clicked!");
                makeEditTextDialog(IBioMiniDevice.CaptureFuntion.ENROLLMENT.toString());
                break;
            case R.id.bt_verify:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_verify clicked!");
                doVerify();
                break;
            case R.id.bt_enrolldummy:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_enrolldummy clicked!");
                doEnrollDummy();
                break;
            case R.id.bt_deleteallusers:
                if(mCurrentDevice.isCapturing() == true )
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_deleteallusers clicked!");
                mUsers.clear();
                setLogInTextView(getResources().getString(R.string.delete_user));
                break;
            case R.id.bt_exportBmp:
                if(mCurrentDevice.isCapturing() == true && mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_exportBmp clicked!");
                doExportBmp();
                break;
            case R.id.bt_exportWsq:
                if(mCurrentDevice.isCapturing() == true && mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_exportWsq clicked!");
                doExportWsq();
                break;
            case R.id.bt_export19794:
                if(mCurrentDevice.isCapturing() == true && mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_export19794 clicked!");
                doExport19794_4();
                break;
            case R.id.bt_exportRaw:
                if(mCurrentDevice.isCapturing() == true && mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_exportRaw clicked!");
                doExportRaw();
                break;
            case R.id.bt_exporttemplate:
                if(mCurrentDevice.isCapturing() == true && mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                {
                    setLogInTextView(err_other_capture_running);
                    setUiClickable(false);
                    return;
                }
                Logger.d("bt_exporttemplate clicked!");
                makeEditTextDialog("EXPORT_TEMPLATE");
                break;
            case R.id._ivCapture:
                Logger.d("_ivCapture is clicked!");
                showImageInDialog(mCaptureImage);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Logger.d("START!");
    }

    /**
     * 1. Create view page with fragment.
     * 2. Monitoring whether the view page has been changed.
     * @param viewPager
     */
    private void setupViewPager(ViewPager2 viewPager) {
        Logger.d("setupViewPager");
        mAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle());

        mAdapter.createFragment(0);
        mAdapter.createFragment(1);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(0);
        currentViewPosition =0;
        viewPager.setOffscreenPageLimit(1);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            //tab.setCustomView(adapter.getTabView(i));
            new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    //Configure your tabs...
                    if(position == 0 )
                        tab.setText("HOME");
                    /*if(position == 1)
                        tab.setText("SETTING");*/
                }
            }).attach();

        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TabLayout.Tab tab =  mTabLayout.getTabAt(position);
                tab.select();
                if(position == 0)
                {
                    Logger.d("onPageSelected : " + position + " This is Basic Function Page.");
                    currentViewPosition = 0;
                    //    loadResource();
                }
               /* if(position == 1)
                {
                    Logger.d("onPageSelected : " + position + " This is Setting Function Page.");
                    currentViewPosition = 1;
                    //    loadResourceEnrollment();
                }*/
            }
        });
    }

    private void setupTablayout() {

        Logger.d("setupTablayout");

        LinearLayout tabStrip = ((LinearLayout)mTabLayout.getChildAt(0));
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            //tab.setCustomView(adapter.getTabView(i));
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                int count = 0;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Logger.d("mIsCapturing : " + mIsCapturing + " count : " +count);
                    if(mCurrentDevice != null && mCurrentDevice.isCapturing())
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });
        }
    }

    private void setDefaultToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("VTOM Attendance");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(mToolbar);
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> myFragments = new ArrayList<>();
        private final List<String> myFragmentTitles = new ArrayList<>();
        private Context context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        public void addFragment(Fragment fragment, String title) {
            myFragments.add(fragment);
            myFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return myFragments.get(position);
        }

        @Override
        public int getCount() {
            return myFragments.size();
        }

        public View getTabView(int position) {
            // Given you have a custom layout in `res/layout/custom_tab_item.xml` with a TextView and ImageView
            View view = null;
            if(position == 0)
                view = LayoutInflater.from(context).inflate(R.layout.fragment_basic_func, null);

            if(position == 1)
                view = LayoutInflater.from(context).inflate(R.layout.fragment_setting, null);

            return view;
        }
    }

    private void checkCurrentSystemTheme() {
        int currentNightMode = mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                Logger.d("current theme is UI_MODE_NIGHT_NO");
                mCurrentNightMode = false;
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                Logger.d("current theme is UI_MODE_NIGHT_YES");
                mCurrentNightMode = true;
                break;
        }
    }

    private void initUsbListener() {
        Logger.d("start!");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mContext.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        IntentFilter attachfilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(mUsbReceiver , attachfilter);
        IntentFilter detachfilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver , detachfilter);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action)
            {
                case ACTION_USB_PERMISSION:
                    Logger.d("ACTION_USB_PERMISSION");
                    boolean hasUsbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false);
                    if(hasUsbPermission && mUsbDevice != null)
                    {
                        Logger.d(mUsbDevice.getDeviceName() + " is acquire the usb permission. activate this device.");
                        mHandler.sendEmptyMessage(ACTIVATE_USB_DEVICE);
                    }
                    else
                    {
                        Logger.d("USB permission is not granted!");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Logger.d("ACTION_USB_DEVICE_ATTACHED");
                    addDeviceToUsbDeviceList();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Logger.d("ACTION_USB_DEVICE_DETACHED");
                    setLogInTextView(getResources().getString(R.string.usb_detached));
                    mViewPager.setCurrentItem(0);
                    removeDevice();
                    break;
                default:
                    break;
            }
        }
    };

    Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case ACTIVATE_USB_DEVICE:
                    if(mUsbDevice != null)
                        Logger.d("ACTIVATE_USB_DEVICE : " + mUsbDevice.getDeviceName());
                    createBioMiniDevice();
                    break;
                case REMOVE_USB_DEVICE:
                    Logger.d("REMOVE_USB_DEVICE");
                    //_rsApi.terminate();
                    removeDevice();
                    break;
                case UPDATE_DEVICE_INFO:
                    Logger.d("UPDATE_DEVICE_INFO");

                    break;
                case REQUEST_USB_PERMISSION:
                    mPermissionIntent = PendingIntent.getBroadcast(mContext,0,new Intent(ACTION_USB_PERMISSION),0);
                    mUsbManager.requestPermission(mUsbDevice , mPermissionIntent);
                    break;
                case MAKE_DELAY_1SEC:
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ADD_DEVICE:
                    addDeviceToUsbDeviceList();
                    break;
                case CLEAR_VIEW_FOR_CAPTURE:
                    cleareViewForCapture();
                    break;
                case SET_TEXT_LOGVIEW:
                    String _log = (String) msg.obj;
                    // append the new string
                    scrollBottom(_log);
                    break;
                case MAKE_TOAST:
                    Logger.d("MAKE_TOAST : " + (String)msg.obj);
                    Toast.makeText(mContext,(String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_CAPTURE_IMAGE_DEVICE:
                    Logger.d("SHOW_CAPTURE_IMAGE_DEVICE");
                    Bitmap _captureImgDev = (Bitmap)msg.obj;
                    mImageView.setImageBitmap(_captureImgDev);
                    break;
            }
        }
    };

    public void addDeviceToUsbDeviceList()
    {
        Logger.d("start!");
        if(mUsbManager == null)
        {
            Logger.d("mUsbManager is null");
            return;
        }
        if(mUsbDevice !=null)
        {
            Logger.d("usbdevice is not null!");
            return;
        }

        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if( _device.getVendorId() ==0x16d1 ){
                Logger.d("found suprema usb device");
                mUsbDevice = _device;
                if(mUsbManager.hasPermission(mUsbDevice) == false)
                {
                    Logger.d("This device need to Usb Permission!");
                    mHandler.sendEmptyMessage(REQUEST_USB_PERMISSION);
                }
                else
                {
                    Logger.d("This device alread have USB permission! please activate this device.");
//                    _rsApi.deviceAttached(mUsbDevice);
                    mHandler.sendEmptyMessage(ACTIVATE_USB_DEVICE);
                }
            }
            else
            {
                Logger.d("This device is not suprema device!  : " + _device.getVendorId());
            }
        }
    }


    private void removeDevice() {
        Logger.d("ACTION_USB_DEVICE_DETACHED");
        if(mBioMiniFactory != null)
        {
            mBioMiniFactory.removeDevice(mUsbDevice);
            mBioMiniFactory.close();
        }
        mUsbDevice = null;
        mCurrentDevice = null;
        clearDeviceInfoView();
        cleareViewForCapture();
        resetSettingMenu();
        clearSharedPreferenceData();
    }

    public void loadResource(View _view) {
        Logger.d("START!");



        mImageView = _view.findViewById(R.id._ivCapture);
        mImageView.setOnClickListener(this);


        mDeviceTextView =   _view.findViewById(R.id.tvDevice);
        mSerialNumTextView = _view.findViewById(R.id.tv_serialnumbervalue);
        mSerialNumTextView.setSelected(true);
        mSdkInfoTextView =   _view.findViewById(R.id.tv_sdkinfovalue);
        mFwVersionTextView = _view.findViewById(R.id.tv_fwversionvalue);
        mFwVersionTextViewMenu = _view.findViewById(R.id.tv_fwversion);



        mCaptureSingleButton = _view.findViewById(R.id.bt_capturesingle);
        mCaptureSingleButton.setOnClickListener(this);

        mPreviewButton = _view.findViewById(R.id.bt_startpreview);
        mPreviewButton.setOnClickListener(this);

        mAutoCaptureButton = _view.findViewById(R.id.bt_autocapture);
        mAutoCaptureButton.setOnClickListener(this);

        mAbortCaptureButton = _view.findViewById(R.id.bt_abortcapture);
        mAbortCaptureButton.setOnClickListener(this);

        mEnrollmentButton = _view.findViewById(R.id.bt_enrollment);
        mEnrollmentButton.setOnClickListener(this);

        mVerifyButton = _view.findViewById(R.id.bt_verify);
        mVerifyButton.setOnClickListener(this);

        mEnrollDummyButton = _view.findViewById(R.id.bt_enrolldummy);
        mEnrollDummyButton.setOnClickListener(this);

        mDeleteAllUserButton = _view.findViewById(R.id.bt_deleteallusers);
        mDeleteAllUserButton.setOnClickListener(this);

        mExportBmpButton = _view.findViewById(R.id.bt_exportBmp);
        mExportBmpButton.setOnClickListener(this);

        mExportWsqButton = _view.findViewById(R.id.bt_exportWsq);
        mExportWsqButton.setOnClickListener(this);

        mExport19794Button = _view.findViewById(R.id.bt_export19794);
        mExport19794Button.setOnClickListener(this);

        mExportRawButton = _view.findViewById(R.id.bt_exportRaw);
        mExportRawButton.setOnClickListener(this);

        mExportTemplateButton = _view.findViewById(R.id.bt_exporttemplate);
        mExportTemplateButton.setOnClickListener(this);

        mLogView = _view.findViewById(R.id._tvCaptureLog);
        mLogView.setMovementMethod(new ScrollingMovementMethod());

        if(clockinType_.equalsIgnoreCase("Finger")){
            mEnrollmentButton.setVisibility(View.INVISIBLE);
            mDeleteAllUserButton.setVisibility(View.INVISIBLE);
        }


    }
    synchronized public void setLogInTextView(final String msg)
    {
        sendMsgToHandler(SET_TEXT_LOGVIEW,msg);
    }

    private void scrollBottom(String _log) {
        Logger.d(_log);
        if(mLogView == null)
            return;
        if(mLogView.getLayout() == null)
            return;
        mLogView.append(_log + "\n");
        final int scrollAmount = mLogView.getLayout().getLineTop(mLogView.getLineCount()) - mLogView.getHeight();
        if (scrollAmount > 0)
            mLogView.scrollTo(0, scrollAmount);
        else
            mLogView.scrollTo(0, 0);
    }

    public Context getContext() {
        return mContext;
    }

    CaptureResponder mCaptureCallBack = new CaptureResponder() {
        @Override
        public void onCapture(Object context, IBioMiniDevice.FingerState fingerState) {
            super.onCapture(context, fingerState);
        }

        @Override
        public boolean onCaptureEx(Object context, IBioMiniDevice.CaptureOption option, final Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, IBioMiniDevice.FingerState fingerState) {

            Logger.d("START! : " + mCaptureOption.captureFuntion.toString());

            if(capturedTemplate != null)
            {
                Logger.d("TemplateData is not null!");
                mTemplateData = capturedTemplate;
            }

            if(option.captureFuntion == IBioMiniDevice.CaptureFuntion.ENROLLMENT && mTemplateData != null)
            {
                Logger.d("register user template data.");
                boolean result = mUsers.add(new UserData(mUserName, mTemplateData.data, mTemplateData.data.length));
                if(result == true)
                {
                    setLogInTextView(getResources().getString(R.string.enroll_ok) + " for User : " + mUserName);
                }
                else
                    setLogInTextView(mUserName + " "  + getResources().getString(R.string.enroll_fail) + " for ID : " + mUserName);
            }

            if(option.captureFuntion == IBioMiniDevice.CaptureFuntion.VERIFY && mTemplateData != null)
            {
                if (capturedTemplate != null) {
                    boolean isMatched = false;
                    String matchedName = "";
                    for (DefaultActivity.UserData ud : mUsers) {
                        if (mCurrentDevice.verify(
                                mTemplateData.data, mTemplateData.data.length,
                                ud.template, ud.template.length)) {
                            isMatched = true;
                            matchedName = ud.name;
                            break;
                        }
                    }
                    if (isMatched) {
                        Logger.d("Match found : " + matchedName);
                        setLogInTextView(getResources().getString(R.string.verify_ok));
                        setLogInTextView(getResources().getString(R.string.verify_id) + " " +matchedName);
                    } else {
                        if(mCurrentDevice.getLastError().value() == IBioMiniDevice.ErrorCode.ERR_TEMPLATE_TYPE.value())
                        {
                            setLogInTextView(getResources().getString(R.string.verify_not_match_by_templateType));
                        }
                        else
                        {
                            setLogInTextView(getResources().getString(R.string.verify_not_match_by_tempateData));
                        }
                    }
                } else {
                    Logger.d("<<ERROR>> Template is not extracted...");
                    setLogInTextView(getResources().getString(R.string.verify_fail));
                }
            }
//            if((option.captureFuntion != IBioMiniDevice.CaptureFuntion.START_CAPTURING && option.captureFuntion != IBioMiniDevice.CaptureFuntion.NONE))
            if(capturedTemplate != null)
            {
                Logger.d("check additional capture result.");
                if(mCurrentDevice!= null && mCurrentDevice.getLfdLevel() > 0 )
                {
                    setLogInTextView("LFD SCORE : " + mCurrentDevice.getLfdScoreFromCapture());
                }
                if(mDetect_core == 1)
                {
                    int[] _coord = mCurrentDevice.getCoreCoordinate();
                    setLogInTextView("Core Coordinate X : " + _coord[0] + " Y : " + _coord[1]);
                }
                if(mTemplateQualityEx == 1)
                {
                    int _templateQualityExValue = mCurrentDevice.getTemplateQualityExValue();
                    setLogInTextView("template Quality : " + _templateQualityExValue);
                }
            }

            //fpquality example
            if(mCurrentDevice != null)
            {
                byte[] imageData = mCurrentDevice.getCaptureImageAsRAW_8();
                if(imageData != null)
                {
                    IBioMiniDevice.FpQualityMode mode = IBioMiniDevice.FpQualityMode.NQS_MODE_DEFAULT;
                    int _fpquality = mCurrentDevice.getFPQuality(imageData,mCurrentDevice.getImageWidth(),mCurrentDevice.getImageHeight(),mode.value());
                    Logger.d("_fpquality : " + _fpquality);
                }
            }

            if(option.captureFuntion == IBioMiniDevice.CaptureFuntion.CAPTURE_SINGLE)
                setLogInTextView(getResources().getString(R.string.capture_single_ok));

            if(option.captureFuntion == IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO)
                setLogInTextView(getResources().getString(R.string.capture_auto_ok));
            
            Logger.i("capture time = " + (System.currentTimeMillis() - mCaptureStartTime));
            sendMsgToHandler(SHOW_CAPTURE_IMAGE_DEVICE,capturedImage);

            if(option.captureFuntion == IBioMiniDevice.CaptureFuntion.CAPTURE_SINGLE ||
                option.captureFuntion == IBioMiniDevice.CaptureFuntion.ENROLLMENT ||
                option.captureFuntion == IBioMiniDevice.CaptureFuntion.VERIFY)
            {
                Logger.d("set ui event is available.");
                mViewPager.setUserInputEnabled(true);
                setUiClickable(true);
            }
            return true;

        }

        @Override
        public void onCaptureError(Object context, int errorCode, String error) {
            if (errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_IS_CAPTURING.value())
            {
                setLogInTextView("Other capture function is running. abort capture function first!");
            }
            else if(errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_CAPTURE_ABORTED.value())
            {
                Logger.d("CTRL_ERR_CAPTURE_ABORTED occured.");
            }
            else if(errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_FAKE_FINGER.value())
            {
                setLogInTextView("Fake Finger Detected");
                if(mCurrentDevice!= null && mCurrentDevice.getLfdLevel() > 0 )
                {
                    setLogInTextView("LFD SCORE : " + mCurrentDevice.getLfdScoreFromCapture());
                }
            }
            else
            {
                setLogInTextView(mCaptureOption.captureFuntion.name() + " is fail by " + error);
                setLogInTextView("Please try again.");
            }
            mViewPager.setUserInputEnabled(true);
            setUiClickable(true);
        }
    };

    private void createBioMiniDevice() {
        Logger.d("START!");
        if(mUsbDevice == null)
        {
            setLogInTextView(getResources().getString(R.string.error_device_not_conneted));
            return;
        }
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }

        Logger.d("new BioMiniFactory( )");
        mBioMiniFactory = new BioMiniFactory(mContext,mUsbManager) { //for android sample
            @Override
            public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                Logger.d("onDeviceChange : " + event);
                handleDevChange(event, dev);
            }
        };
        Logger.d("new BioMiniFactory( ) : " + mBioMiniFactory);
        boolean _transferMode = mSettingFragment.mDataStorage.getUseNativeUsbModeParam();
        Logger.d("_transferMode : " + _transferMode);
        setTransferMode(_transferMode,false);

        boolean _result = mBioMiniFactory.addDevice(mUsbDevice);

        if(_result == true)
        {
            mCurrentDevice = mBioMiniFactory.getDevice(0);
            if(mCurrentDevice != null)
            {
                setLogInTextView(getResources().getString(R.string.device_attached));
                Logger.d("mCurrentDevice attached : " + mCurrentDevice);
                mViewPager.setCurrentItem(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mCurrentDevice != null /*&& mCurrentDevice.getDeviceInfo() != null*/) {
                            mDeviceTextView.setText(mCurrentDevice.getDeviceInfo().deviceName);
                            mToolbar.setTitle(mCurrentDevice.getDeviceInfo().deviceName);
                            mSerialNumTextView.setText(mCurrentDevice.getDeviceInfo().deviceSN);
                            mFwVersionTextView.setText(mCurrentDevice.getDeviceInfo().versionFW);
                            mSdkInfoTextView.setText(mBioMiniFactory.getSdkVersionInfo());
                            if(mCurrentDevice.getDeviceInfo().scannerType.getDeviceClass() == IBioMiniDevice.ScannerClass.HID_DEVICE)
                            {
                                mFwVersionTextView.setVisibility(View.VISIBLE);
                                mFwVersionTextViewMenu.setVisibility(View.VISIBLE);
                                mFwVersionTextView.setText(mCurrentDevice.getDeviceInfo().versionFW);
                            }
                            else
                            {
                                mFwVersionTextView.setVisibility(View.GONE);
                                mFwVersionTextViewMenu.setVisibility(View.GONE);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestWritePermission();
                            }
                            setEnableMenuForDevice();
                            getDefaultParameterFromDevice();
                            cleareImageView();
                            cleareLogView();
                        }
                    }
                });
            }
            else
            {
                Logger.d("mCurrentDevice is null");
            }

        }
        else
        {
            Logger.d("addDevice is fail!");
        }
        //mBioMiniFactory.setTransferMode(IBioMiniDevice.TransferMode.MODE2);
    }

    private void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        Logger.d("START!");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestWritePermission() {
        Logger.d("start!");
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},  REQUEST_WRITE_PERMISSION);
        }
        else
        {
            Logger.d("WRITE_EXTERNAL_STORAGE permission already granted!");
            requestBatteryOptimization();
        }
    }

    private void requestBatteryOptimization() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void checkEnrollDummyFunction() {
        Logger.d("START!");
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if (mCurrentDevice != null) {
                                  if (mUsbDevice.getProductId() == 0x423) {
                                      mEnrollDummyButton.setEnabled(true);
                                  } else {
                                      mEnrollDummyButton.setEnabled(false);
                                  }
                              }
                              else
                              {
                                  mEnrollDummyButton.setEnabled(true);
                              }
                          }
                      }
        );
    }

    public void resetSettingMenu() {
        mToolbar.setTitle("VTOM Attendance");
        if (mCurrentDevice == null)
        {
            mCaptureSingleButton.setEnabled(false);
            mPreviewButton.setEnabled(false);
            mAutoCaptureButton.setEnabled(false);
            mAbortCaptureButton.setEnabled(false);
            mEnrollmentButton.setEnabled(false);
            mVerifyButton.setEnabled(false);
            mEnrollDummyButton.setEnabled(false);
            mDeleteAllUserButton.setEnabled(false);
            mExportBmpButton.setEnabled(false);
            mExportWsqButton.setEnabled(false);
            mExport19794Button.setEnabled(false);
            mExportRawButton.setEnabled(false);
            mExportTemplateButton.setEnabled(false);
            mSettingFragment.resetPreference();
        }
    }


    private void clearDeviceInfoView() {
        mDeviceTextView.setText("");
        mSerialNumTextView.setText("");
        mFwVersionTextView.setText("");
        mSdkInfoTextView.setText("");
    }
    private void cleareLogView()
    {
        if(mLogView != null)
            mLogView.setText(null);
    }
    private void cleareImageView()
    {
        if(mImageView != null)
            mImageView.setImageBitmap(null);
    }
    private void cleareViewForCapture()
    {
        cleareImageView();
        cleareLogView();
    }
    private void doSinlgeCapture() {
        Logger.d("START!");
        mCaptureStartTime = System.currentTimeMillis();
        mTemplateData = null;
        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.CAPTURE_SINGLE;
        mCaptureOption.extractParam.captureTemplate = true;
        cleareViewForCapture();
        mViewPager.setUserInputEnabled(false);
        setLogInTextView(getResources().getString(R.string.toast_viewpager_capturing));
        if(mCurrentDevice != null) {
            boolean result = mCurrentDevice.captureSingle(
                    mCaptureOption,
                    mCaptureCallBack,
                    true);
        }
    }

    private void doCapturePreview() {
        int result = 0;
        mTemplateData = null;
        mCaptureOption.extractParam.captureTemplate = false;
        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.START_CAPTURING;
        cleareViewForCapture();
        mViewPager.setUserInputEnabled(false);
        setLogInTextView(getResources().getString(R.string.toast_viewpager_capturing));
        if(mCurrentDevice != null) {
            result = mCurrentDevice.startCapturing(
                    mCaptureOption,
                    mCaptureCallBack);
            if(result == IBioMiniDevice.ErrorCode.ERR_NOT_SUPPORTED.value())
            {
                setLogInTextView("This device is not support preview!");
            }
        }
    }

    private void doAutoCapture() {
        Logger.d("buttonCaptureAuto clicked");
        mTemplateData = null;
        mCaptureOption.extractParam.captureTemplate = true;
        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.CAPTURE_AUTO;
        cleareViewForCapture();
        mViewPager.setUserInputEnabled(false);
        mCaptureOption.frameRate = IBioMiniDevice.FrameRate.LOW;
        setLogInTextView(getResources().getString(R.string.toast_viewpager_capturing));
        if(mCurrentDevice != null) {
            int result = mCurrentDevice.captureAuto(mCaptureOption,mCaptureCallBack);
            if(result == IBioMiniDevice.ErrorCode.ERR_NOT_SUPPORTED.value())
            {
                setLogInTextView("This device is not support auto Capture!");
            }
        }
    }

    private void doAbortCapture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentDevice != null) {
//                    mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE;
                    if(mCurrentDevice.isCapturing() == false)
                    {
                        setLogInTextView("Capture Function is already aborted.");
                        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE;
                        mViewPager.setUserInputEnabled(true);
                        setUiClickable(true);
                        isAbortCapturing = false;
                        return;
                    }
                    int result = mCurrentDevice.abortCapturing();
                    int nRetryCount = 0;
//                    while (mCurrentDevice != null && mCurrentDevice.isCapturing()) {
//                        SystemClock.sleep(10);
//                        nRetryCount++;
//                    }
                    Logger.d("run: abortCapturing : " + result);
                    if (result == 0) {

                        if(mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.NONE)
                            setLogInTextView(mCaptureOption.captureFuntion.name() + " is aborted.");

                        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE;
                        mViewPager.setUserInputEnabled(true);
                        setUiClickable(true);
                        isAbortCapturing = false;
                    }
                    else {
//                        if(mCurrentDevice.isCapturing() == false && IBioMiniDevice.ErrorCode.CTRL_ERR_CAPTURE_IS_NOT_RUNNING.value() == result)
//                        {
//                            setLogInTextView("Capture Function is already aborted.");
//                            mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE;
//                            mViewPager.setUserInputEnabled(true);
//                            isAbortCapturing = false;
//                        }
//                        else
                        if(result == IBioMiniDevice.ErrorCode.ERR_CAPTURE_ABORTING.value())
                        {

                            setLogInTextView("abortCapture is still running.");
                        }
                        else
                            setLogInTextView("abort capture fail!");
                    }
                }
            }
        }).start();
    }

    private void setUiClickable(boolean isClickable) {
        Logger.d("isClickable = " + isClickable);
        mCaptureSingleButton.setClickable(isClickable);
        mPreviewButton.setClickable(isClickable);
        mAutoCaptureButton.setClickable(isClickable);
        mEnrollmentButton.setClickable(isClickable);
        mVerifyButton.setClickable(isClickable);
        mEnrollDummyButton.setClickable(isClickable);
        mExportBmpButton.setClickable(isClickable);
        mExportWsqButton.setClickable(isClickable);
        mExport19794Button.setClickable(isClickable);
        mExportRawButton.setClickable(isClickable);
        mExportTemplateButton.setClickable(isClickable);
    }
    private void doEnrollment() {
        mTemplateData = null;
        if (mCurrentDevice != null) {
            Logger.d("mUserName : " + mUserName);
            if (mUserName.equals("")) {
                return;
            }
            mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.ENROLLMENT;
            mCaptureOption.extractParam.captureTemplate = true;
            cleareViewForCapture();
            mViewPager.setUserInputEnabled(false);
            setLogInTextView(getResources().getString(R.string.toast_viewpager_capturing));
            if(mCurrentDevice != null) {
                boolean result = mCurrentDevice.captureSingle(
                        mCaptureOption,
                        mCaptureCallBack,
                        true);
            }
        }
    }

    private void doVerify() {
        if (mUsers.size() == 0) {
            setLogInTextView("There is no enrolled data");
            return;
        }
        mTemplateData = null;
        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.VERIFY;
        mCaptureOption.extractParam.captureTemplate = true;
        cleareViewForCapture();
        mViewPager.setUserInputEnabled(false);
        setLogInTextView(getResources().getString(R.string.toast_viewpager_capturing));
        if(mCurrentDevice != null) {
            boolean result = mCurrentDevice.captureSingle(
                    mCaptureOption,
                    mCaptureCallBack,
                    true);
        }

        if(clockinType_.equalsIgnoreCase("Finger")){
            Intent myIntent = new Intent(getApplicationContext(), Clockin_Card.class);
            myIntent.putExtra("_CardNumber", "718222");
            myIntent.putExtra("_FullName", "718222");
            myIntent.putExtra("_ClockinType", clockinType_);
            startActivity(myIntent);
        }

    }

    public void makeEditTextDialog(final String functionName)
    {
        Logger.d("START : " + functionName);
        mUserName = "";
        AlertDialog.Builder ad = new AlertDialog.Builder(DefaultActivity.this);

        ad.setTitle(functionName);
        if(functionName.equals("EXPORT_TEMPLATE"))
        {
            ad.setMessage("Enter Encrypt Key.");   // Encrypt Key
        }
        else
            ad.setMessage("Enter User Name.");   // User Name

        final EditText et = new EditText(DefaultActivity.this);
        ad.setView(et);

        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                Logger.d("Positive Button Clicked : " + mUserName);

                dialog.dismiss();
                // Event
                Logger.d("Function name is " + functionName);
                if(functionName.equals(IBioMiniDevice.CaptureFuntion.ENROLLMENT.toString()))
                {
                    mUserName = et.getText().toString();
                    doEnrollment();
                }
                if(functionName.equals("EXPORT_TEMPLATE"))
                {
                    mEncyptKey = et.getText().toString();
                    doExportTemplate();
                }
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Logger.d("No Btn Click");
                dialog.dismiss();
                // Event
            }
        });
        ad.setCancelable(false);
        ad.show();
    }
    private void doEnrollDummy() {
        if (mCurrentDevice != null) {
            IBioMiniDevice.TemplateData [] data = (IBioMiniDevice.TemplateData [])
                    mCurrentDevice.test("createDummyTemplates", 100);
            for(IBioMiniDevice.TemplateData item : data) {
                mUsers.add(new UserData(item.id, item.data, item.data.length));
            }
            setLogInTextView(data.length + " items added :" + mUsers.size() + " in total.");
        }
    }

    private void doExportBmp() {
        if (mCurrentDevice != null) {
            byte[] bmp = mCurrentDevice.getCaptureImageAsBmp();
            if (bmp == null) {
                Logger.d("<<ERROR>> Cannot get BMP buffer");
                setLogInTextView(getResources().getString(R.string.export_bmp_fail));
                return;
            }
            try {
                String strCurrentTime = getCurrentTime();
                String lfdScore = "";
                if(mCurrentDevice.getLfdLevel() > 0)
                    lfdScore = "LFD_" + String.valueOf(mCurrentDevice.getLfdScoreFromCapture());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/"+ strCurrentTime +"_" +lfdScore+"_capturedImage.bmp");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bmp);
                Logger.d("buttonExportBmp : " + file.length() + "  bmp.length : " + bmp.length);
                fos.close();

                setLogInTextView(getResources().getString(R.string.export_bmp_ok));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String getCurrentTime() {
        Logger.d("START");
        String result = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        Date time = new Date();
        result = dateFormat.format(time);
        Logger.d("result : " + result);

        return result;
    }

    private void doExportWsq() {
        if (mCurrentDevice != null) {
            int _width = mCurrentDevice.getImageWidth();
            int _height = mCurrentDevice.getImageHeight();
            byte[] wsq = mCurrentDevice.getCaptureImageAsWsq(_width, _height, 3.5f, 0/* 0 or 180 */);
            if (wsq == null) {
                Logger.d("<<ERROR>> Cannot get WSQ buffer");
                setLogInTextView(getResources().getString(R.string.export_wsq_fail));
                return;
            }
            try {
                String strCurrentTime = getCurrentTime();
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + strCurrentTime + "_capturedImage.wsq");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(wsq);
                fos.close();
                setLogInTextView(getResources().getString(R.string.export_wsq_ok));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doExport19794_4() {
        if (mCurrentDevice != null) {
            byte[] format19794_4 = mCurrentDevice.getCaptureImageAs19794_4();
            if (format19794_4 == null) {
                Logger.d("<<ERROR>> Cannot get 19794_4 buffer");
                setLogInTextView(getResources().getString(R.string.export_19794_4_fail));
                return;
            }
            try {
                String strCurrentTime = getCurrentTime();
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + strCurrentTime + "_capturedImage.dat");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(format19794_4);
                fos.close();
                setLogInTextView(getResources().getString(R.string.export_19794_4_ok));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doExportRaw() {
        if (mCurrentDevice != null) {
            byte[] formatRaw = mCurrentDevice.getCaptureImageAsRAW_8();
            if (formatRaw == null) {
                Logger.d("<<ERROR>> Cannot get Raw buffer");
                setLogInTextView(getResources().getString(R.string.export_Raw_fail));
                return;
            }
            try {
                String strCurrentTime = getCurrentTime();
                String lfdScore = "";
                if(mCurrentDevice.getLfdLevel() > 0)
                    lfdScore = "LFD_" + String.valueOf(mCurrentDevice.getLfdScoreFromCapture());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/"+ strCurrentTime +"_" +lfdScore+"_capturedImage.raw");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(formatRaw);
                fos.close();
                setLogInTextView(getResources().getString(R.string.export_Raw_ok));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void doExportTemplate() {
        if (mCurrentDevice != null) {

            if(mTemplateData == null)
            {
                setLogInTextView(getResources().getString(R.string.error_export_template_no_template));
                return;
            }

            try {
                if(mEncyptKey.equals(""))
                    mEncyptKey = null;
                else
                {
                    Logger.d("mEncyptkey : "+ mEncyptKey.getBytes("UTF-8").length);
                    mCurrentDevice.setEncryptionKey(mEncyptKey.getBytes("UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                mCurrentDevice.setEncryptionKey(null);
                Logger.d("<<ERROR>> cannot set encryption key unsupported character sets assigned...");
                setLogInTextView(getResources().getString(R.string.export_template_fail));
                e.printStackTrace();
            }

            int tmp_type = 0;
            IBioMiniDevice.TemplateType _type = IBioMiniDevice.TemplateType.SUPREMA;

            int tmp_type_idx = mSettingFragment.mDataStorage.getExportTemplateTypeParam();
            Logger.d("tmp_type_idx" + tmp_type_idx);

            switch (tmp_type_idx) {
                case 2001:
                    tmp_type = IBioMiniDevice.TemplateType.SUPREMA.value();
                    _type = IBioMiniDevice.TemplateType.SUPREMA;
                    break;
                case 2002:
                    tmp_type = IBioMiniDevice.TemplateType.ISO19794_2.value();
                    _type = IBioMiniDevice.TemplateType.ISO19794_2;
                    break;
                case 2003:
                    tmp_type = IBioMiniDevice.TemplateType.ANSI378.value();
                    _type = IBioMiniDevice.TemplateType.ANSI378;
                    break;
            }
            mCurrentDevice.setParameter(
                    new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE,
                            tmp_type));
            IBioMiniDevice.TemplateData tmp = mCurrentDevice.extractTemplate();
            if (tmp == null) {
                Logger.d("<<ERROR>> Cannot get Template buffer");
                setLogInTextView(getResources().getString(R.string.export_template_fail));
                return;
            }
            String strCurrentTime = getCurrentTime();
            if (tmp.data != null) {
                try {
                    if(mEncyptKey == null)
                        mEncyptKey="";

                    File file = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" +
                                    strCurrentTime + "_(" + _type.toString() + ")_"+mEncyptKey+"_capturedTemplate.dat");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(tmp.data);
                    fos.close();
                    setLogInTextView(getResources().getString(R.string.export_template_ok));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (!mEncyptKey.equals("")) {
                            File file = new File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" +
                                            strCurrentTime + "_(" + _type.toString() + ")_capturedTemplate_dec.dat");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(mCurrentDevice.decrypt(tmp.data));
                            fos.close();
                            setLogInTextView(getResources().getString(R.string.export_decrypt_template_ok));

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private void getDefaultParameterFromDevice() {
        Logger.d("START!");
        if(mCurrentDevice != null) {
            DataStorage _dataStorage = mSettingFragment.mDataStorage;
            int hw_lfd_level = 0;
            int sw_lfd_level = 0;
            int result= -1;

            if(_dataStorage.containKey(mContext,_dataStorage.getSecurityKey()) == false)
            {
                if(mUsbDevice.getProductId() == BioMiniSlim2S)
                {
                    int security_level = (int) mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SECURITY_LEVEL).value;
                    _dataStorage.setSecurityParam(security_level);
                    result = setParameterToDevice(_dataStorage.getSecurityKey(),_dataStorage.getSecurityParam(),true);
                }
                else
                {
                    result = setParameterToDevice(_dataStorage.getSecurityKey(),_dataStorage.DEFAULT_SECURITY_VALUE,true);
                }
                if(result != 0)
                {
                    mSettingFragment.setSensitivityPref(false);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getSecurityKey(),_dataStorage.getSecurityParam(),true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getSensitivityKey()) == false)
            {
                if(mUsbDevice.getProductId() == BioMiniSlim2S) {
                    int sensitivity_level = (int) mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SENSITIVITY).value;
                    _dataStorage.setSensitivityPram(sensitivity_level);
                    setParameterToDevice(_dataStorage.getSensitivityKey(),_dataStorage.getSensitivityPram(),true);
                }
                else
                {
                    setParameterToDevice(_dataStorage.getSensitivityKey(),_dataStorage.DEFAULT_SENSITIVITY_VALUE,true);
                }
                if(result != 0)
                {
                    mSettingFragment.setSecurityPref(false);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getSensitivityKey(),_dataStorage.getSensitivityPram(),true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getTimeoutKey()) == false)
            {
                if(mUsbDevice.getProductId() == BioMiniSlim2S)
                {
                    int timeout = (int) mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
                    _dataStorage.setTimeoutParam(timeout/1000);
                    setParameterToDevice(_dataStorage.getTimeoutKey(),_dataStorage.getTimeoutParam(),true);
                }
                else
                    setParameterToDevice(_dataStorage.getTimeoutKey(),_dataStorage.DEFAULT_TIMEOUT_VALUE,true);

                if(result != 0)
                {
                    mSettingFragment.setTimeOutPref(false);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getTimeoutKey(),_dataStorage.getTimeoutParam(),true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getLfdWithDeviceKey()) == false)
            {
                if(mUsbDevice.getProductId() == BioMiniSlim2S)
                {
                    hw_lfd_level = (int) mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.DETECT_FAKE_HW).value;
                    _dataStorage.setLfdWithDeviceParam(hw_lfd_level);
                    setParameterToDevice(_dataStorage.getLfdWithDeviceKey(),_dataStorage.getLfdWithDeviceParam(),true);
                }
                else
                    result = setParameterToDevice(_dataStorage.getLfdWithDeviceKey(),_dataStorage.DEFAULT_LFD_WITH_DEVICE_VALUE,true);

                if(result != 0)
                {
                    mSettingFragment.setHwLfdPref(false);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getLfdWithDeviceKey(),_dataStorage.getLfdWithDeviceParam(),true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getLfdWithSdkKey()) == false)
            {
                if(mUsbDevice.getProductId() == BioMiniSlim2S)
                {
                    sw_lfd_level = (int) mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.DETECT_FAKE_SW).value;
                    _dataStorage.setLfdWithSdkParam(sw_lfd_level);
                    setParameterToDevice(_dataStorage.getLfdWithSdkKey(),_dataStorage.getLfdWithSdkParam(),true);
                }
                else
                {
                    result = setParameterToDevice(_dataStorage.getLfdWithSdkKey(),_dataStorage.DEFAULT_LFD_WITH_SDK_VALUE,true);
                }

                if(result != 0)
                {
                    mSettingFragment.setSwLfdPref(false);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getLfdWithSdkKey(),_dataStorage.getLfdWithSdkParam(),true);
            }
            if(_dataStorage.containKey(mContext,_dataStorage.getFastModeKey()) == false)
            {
                boolean fast_mode = mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.FAST_MODE).value == 1;
                _dataStorage.setFastModeParam(fast_mode);
            }
            else
            {
                boolean fast_mode = _dataStorage.getFastModeParam();
                setParameterToDevice(_dataStorage.getFastModeKey() ,fast_mode == true ? 1: 0, true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getCropModeKey()) == false)
            {
                Logger.d("this key is not used.");
                boolean crop_mode = false;
                crop_mode = mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SCANNING_MODE).value == 1;
                result = setParameterToDevice(_dataStorage.getCropModeKey() ,crop_mode == true ? 1: 0, true);
                if(result != 0)
                {
                    mSettingFragment.setCropModePref(false);
                }
                else
                {
                    _dataStorage.setCropModeParam(crop_mode);
                }
                Logger.d("crop_mode: "+ crop_mode);
            }
            else
            {
                Logger.d("this key is used.");
                boolean crop_mode = _dataStorage.getCropModeParam();
                setParameterToDevice(_dataStorage.getCropModeKey() ,crop_mode == true ? 1: 0, true);
            }

            if(_dataStorage.containKey(mContext,_dataStorage.getExtTriggerKey()) == false)
            {
                boolean ext_trigger = false;
                if(mUsbDevice.getProductId() == BioMiniSlim2S)
                {
                    ext_trigger  = mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.EXT_TRIGGER).value == 1;
                    _dataStorage.setExtTriggerParam(ext_trigger);
                    setParameterToDevice(_dataStorage.getExtTriggerKey() ,ext_trigger == true ? 1: 0, true);
                }
                else
                {
                    setParameterToDevice(_dataStorage.getExtTriggerKey() ,_dataStorage.DEFAULT_EXT_TRIGGER_VALUE == true ? 1: 0, true);
                }
            }
            else
            {
                setParameterToDevice(_dataStorage.getExtTriggerKey() ,_dataStorage.getExtTriggerParam() == true ? 1: 0, true);
            }

            boolean auto_sleep = mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.ENABLE_AUTOSLEEP).value == 1;
            _dataStorage.setAutoSleepParam(auto_sleep);
            result = setParameterToDevice(_dataStorage.getAutoSleepKey(),_dataStorage.getAutoSleepParam() == true? 1: 0 , true);
            if(result != 0)
            {
                mSettingFragment.setAutoSleepPref(false);
            }

            boolean image_flip_180d = _dataStorage.getImageFlip180dParam();
            result = setParameterToDevice(_dataStorage.getImageFlip180dKey() ,image_flip_180d == true ? 1: 0, true);
            if(result != 0)
            {
                mSettingFragment.setImageFlip180dPref(false);
            }

            boolean omnidir_verify = _dataStorage.getOmniDirVerifyParam();
            result = setParameterToDevice(_dataStorage.getOmniDirVerifyKey() ,omnidir_verify == true ? 1: 0, true);
            if(result != 0)
            {
                mSettingFragment.setOmniDirVerifyPref(false);
            }

            boolean detect_core = _dataStorage.getDetectCoreParam();
            result = setParameterToDevice(_dataStorage.getDetectCoreKey() ,detect_core == true ? 1: 0, true);
            if(result != 0)
            {
                mSettingFragment.setDetectCorePref(false);
            }

            boolean template_qualityEx = _dataStorage.getTemplateQualityExParam();
            result = setParameterToDevice(_dataStorage.getTemplateQualityExKey() ,template_qualityEx == true ? 1: 0, true);
            if(result != 0)
            {
                mSettingFragment.setTemplateQualityExPref(false);
            }
            setTransferMode(_dataStorage.getUseNativeUsbModeParam(),false);
        }
        mSettingFragment.mIsParamInitiated = true;
        mSettingFragment.mHandler.sendEmptyMessage(mSettingFragment.EVENT_SCREEN_UPDATE);
    }
    public int setParameterToDevice(String paramName, int value, boolean bInit)
    {
        int result = -1;
        Logger.d("paramName : " + paramName + " value : " + value);
        switch(paramName)
        {
            case "pref_sensitivity":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SENSITIVITY, value));
                break;
            case "pref_security":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SECURITY_LEVEL, value));
                break;
            case "pref_timeout":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TIMEOUT, value*1000));
                break;
            case "pref_hwlfd":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.DETECT_FAKE_HW, value));
                break;
            case "pref_swlfd":
                    result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.DETECT_FAKE_SW, value));
                break;
            case "pref_fastmode":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.FAST_MODE, value));
                if(result != IBioMiniDevice.ErrorCode.OK.value())
                {
                    mSettingFragment.setFastModePref(false);
                }
                break;
            case "pref_cropmode":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SCANNING_MODE, value));
                if(result != IBioMiniDevice.ErrorCode.OK.value())
                {
                    mSettingFragment.setCropModePref(false);
                }
                break;
            case "pref_exttriger":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.EXT_TRIGGER, value));
                break;
            case "pref_autosleep":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.ENABLE_AUTOSLEEP, value));
                break;
            case "pref_manualsleep":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.ENABLE_MANUAL_SLEEP, value));
                break;
            case "pref_imageflip180d":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.IMAGE_FLIP, value));
                break;
            case "pref_omniDirVerify":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.AUTO_ROTATE, value));
                break;
            case "pref_detectcore":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.DETECT_CORE, value));
                mDetect_core = value;
                break;
            case "pref_templatequalityex":
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_QUALITY_EX, value));
                mTemplateQualityEx = value;
                break;
            case "pref_exporttemplatetype":
                Logger.d("setTemplateType : " + value);
                result = mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE, value));
                break;
        }
        Logger.d("result : " + result);
        if(bInit == false)
        {
            if(result == 0 )
            {
                setLogInTextView(paramName.substring(5) + " parameter was successfully set.");
            }
            else
            {
                setLogInTextView(paramName.substring(5)  + " parameter was not set due to " + IBioMiniDevice.ErrorCode.fromInt(result));
            }
        }
        return result;
    }

    private void setEnableMenuForDevice() {
        Logger.d("START!");
        //capture single
        mCaptureSingleButton.setEnabled(true);

        //preview
        mPreviewButton.setEnabled(true);
        if(mUsbDevice.getProductId() == BioMiniSlimS ||mUsbDevice.getProductId() == BioMiniSlim2S)
        {
            mPreviewButton.setEnabled(false);
        }

        //check Auto Capture
        mAutoCaptureButton.setEnabled(true);
        if(mUsbDevice.getProductId() == BioMiniSlim || mUsbDevice.getProductId() == BioMiniOC4 || mUsbDevice.getProductId() == BioMiniPlus2)
        {
           mAutoCaptureButton.setEnabled(false);
        }
        else
        {
            mAutoCaptureButton.setEnabled(true);
        }
        //abort capture
        mAbortCaptureButton.setEnabled(true);

        //Enrollment
        if(mUsbDevice.getProductId() == BioMiniSlimS)
        {
            mEnrollmentButton.setEnabled(false);
        }
        else
            mEnrollmentButton.setEnabled(true);

        //verify
        if(mUsbDevice.getProductId() == BioMiniSlimS)
            mVerifyButton.setEnabled(false);
        else
            mVerifyButton.setEnabled(true);

        //Enroll Dummy
        if (mUsbDevice.getProductId() == BioMiniSlim3HID) {
            mEnrollDummyButton.setEnabled(true);
        } else {
            mEnrollDummyButton.setEnabled(false);
        }

        //Delete user
        if(mUsbDevice.getProductId() == BioMiniSlimS)
            mDeleteAllUserButton.setEnabled(false);
        else
            mDeleteAllUserButton.setEnabled(true);

        mExportBmpButton.setEnabled(true);

        if(mUsbDevice.getProductId() == BioMiniSlimS)
            mExportWsqButton.setEnabled(false);
        else
            mExportWsqButton.setEnabled(true);

        mExport19794Button.setEnabled(true);

        mExportRawButton.setEnabled(true);

        mExportTemplateButton.setEnabled(true);

        //setting preference
        mSettingFragment.setInitPreference();
    }
    public void setThemeMode(String mode)
    {
        Logger.d("START : " + mode);
        if(mode.equals(getResources().getString(R.string.Theme_light)))
        {
            Logger.d("set light mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }
        if(mode.equals(getResources().getString(R.string.Theme_dark)))
        {
            Logger.d("set dark mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        }
    }

    private void clearSharedPreferenceData()
    {
        mSettingFragment.mDataStorage.clear(mContext);
    }

    private void requestWakeLock() {
        Logger.d("START!");
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,":BioMini WakeLock");
        mWakeLock.acquire();
    }

    private void sendMsgToHandler(int what, String msgToSend)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj = (String)msgToSend;
        mHandler.sendMessage(msg);
    }
    private void sendMsgToHandler(int what, Object objToSend)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj = objToSend;
        mHandler.sendMessage(msg);
    }
    public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        private ArrayList<Fragment> arrayList = new ArrayList<>();


        public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {

                case 0:
                    mBasicFuncFragment =  new BasicFuncFragment();
                    return mBasicFuncFragment;

                case 1:
                    mSettingFragment = new SettingFragment();
                    return mSettingFragment;

            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    @SuppressLint("ResourceType")
    public void showImageInDialog(final Bitmap bitmap) {

        if(bitmap == null)
        {
            Logger.d("Capture Image is null!");
            return;
        }

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        imageWidth = imageWidth*3;
        imageHeight = imageHeight*3;

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        builder.addContentView(imageView, new LinearLayout.LayoutParams(
                imageWidth,
                imageHeight));
        builder.show();
    }
    public int setTransferMode(boolean _value, boolean _prefchanged)
    {
        Logger.d("setTransferMode : " + _value + " _prefchanged : " + _prefchanged);
        int result = 0;
        if(_value == true)
        {
            mTransferMode = IBioMiniDevice.TransferMode.MODE2;
        }
        else
        {
            mTransferMode = IBioMiniDevice.TransferMode.MODE1;
        }
        result = mBioMiniFactory.setTransferMode(mTransferMode);
        if(result != IBioMiniDevice.ErrorCode.OK.value())
        {
            if(result == IBioMiniDevice.ErrorCode.ERR_NOT_SUPPORTED.value())
            {
                mSettingFragment.setUseNativeUsbModePref(false);
            }
            return IBioMiniDevice.ErrorCode.ERR_NOT_SUPPORTED.value();
        }

        if(_prefchanged == true)
            recreate();

        return result;
    }






    private void LOGOUT(){


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("adminId");
        editor.remove("fullName");
        editor.remove("email");
        editor.remove("phoneNumber");
        editor.remove("role");
        editor.remove("organizationId");

        editor.remove("organizationName");
        editor.remove("organizationCategory");
        editor.remove("departmentId");
        editor.remove("departmentName");
        editor.remove("manageCard");
        editor.remove("manageClockIn");

        editor.remove("isUserLoggedIn");
        editor.remove("actor");
        editor.commit();

        startActivity(new Intent(this, Clockin_Login.class));
        finish();
    }

}
