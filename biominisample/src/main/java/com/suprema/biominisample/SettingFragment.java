package com.suprema.biominisample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.suprema.IBioMiniDevice;
import com.suprema.util.Logger;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int EVENT_SCREEN_UPDATE = 100;


    static Context mContext = null;
    static DefaultActivity mActivity = null;
    public static DataStorage mDataStorage = null;

    //UI Component
    static EditTextPreference  mSensitivityPref;
    static EditTextPreference  mSecurityPref;
    static EditTextPreference  mTimeOutPref;
    static EditTextPreference  mHwLfdPref;
    static EditTextPreference  mSwLfdPref;
    static SwitchPreference    mFastModePref;
    static SwitchPreference    mCropModePref;
    static SwitchPreference    mExtTrigerPref;
    static SwitchPreference    mAutoSleepPref;
    static SwitchPreference    mImageFlip180dPref;
    static SwitchPreference    mOmniDirVerifyPref;
    static SwitchPreference    mDetectCorePref;
    static SwitchPreference    mTemplateQualityExPref;
    static SwitchPreference    mUseNativeUsbModePref;
    static ListPreference      mExportTemplateTypePref;
    static Preference    mLicensePref;

    public boolean          mIsRunning = false;
    public boolean          mIsPrefRefreshed = false;
    public boolean          mIsParamInitiated = false;
    public boolean          bHwLfdSupport = false;

    //Device ID
    private static final int BioMiniOC4 = 0x0406;
    private static final int BioMiniSlim = 0x0407;
    private static final int BioMiniSlim2 = 0x0408;
    private static final int BioMiniPlus2 = 0x0409;
    private static final int BioMiniSlimS = 0x0420;
    private static final int BioMiniSlim2S = 0x0421;
    private static final int BioMiniSlim3 = 0x0460;
    private static final int BioMiniSlim3HID = 0x0423;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public AlertDialog.Builder mBuilder = null;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Logger.d("START!");
        //addPreferencesFromResource(R.xml.setting_pref);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Logger.d("START!");
        setPreferencesFromResource(R.xml.setting_pref,rootKey);
        loadPreference();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d("onAttach : " + context);
        mContext = context;
        mActivity = (DefaultActivity) context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d("onDetach!");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("start!");
        initDataStorage();
        mHandler.sendEmptyMessage(EVENT_SCREEN_UPDATE);
        mActivity.resetSettingMenu();
        mActivity.addDeviceToUsbDeviceList();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Logger.d("START! : " + preference.getKey());
        switch (preference.getKey())
        {

            case "pref_license":
                Logger.d("pref_license is clicked.");
                showLicenseInfo();
                break;				
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Logger.d("START : " + preference.getKey() + " value : " + newValue);
        mIsPrefRefreshed = false;
        int result = 0;
        switch (preference.getKey())
        {
            case "pref_sensitivity":
                Logger.d("pref_sensitivity is changed! : " + newValue);
                int _sensitivity = Integer.parseInt((String)newValue);
                if(_sensitivity < 0 || _sensitivity > 7)
                {
                    Toast.makeText(mContext,getResources().getString(R.string.pref_error_wrong_range_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = mActivity.setParameterToDevice(preference.getKey(), _sensitivity, false);
                if(result == 0)
                {
                    mDataStorage.setSensitivityPram(_sensitivity);
                    Logger.d("data check : " + mDataStorage.getSensitivityPram());
                    mSensitivityPref.setSummary(String.valueOf(_sensitivity));
                }
                break;
            case "pref_security":
                Logger.d("pref_security is changed! : " + newValue );
                int _sercurity = Integer.parseInt((String)newValue);
                if(_sercurity < 0 || _sercurity > 7)
                {
                    Toast.makeText(mContext,getResources().getString(R.string.pref_error_wrong_range_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = mActivity.setParameterToDevice(preference.getKey(), _sercurity, false);
                if(result == 0)
                {
                    mDataStorage.setSecurityParam(_sercurity);
                    Logger.d("data check : " + mDataStorage.getSecurityParam());
                    mSecurityPref.setSummary(String.valueOf(_sercurity));
                }
                break;
            case "pref_timeout":
                Logger.d("pref_timeout is changed! : "+ newValue);
                int _timeout =  Integer.parseInt((String)newValue);
                if(_timeout < 0 || _timeout > 60)
                {
                    Toast.makeText(mContext,getResources().getString(R.string.pref_error_wrong_range_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = mActivity.setParameterToDevice(preference.getKey(), _timeout, false);
                if(result == 0 )
                {
                    mDataStorage.setTimeoutParam(_timeout);
                    Logger.d("data check : " + mDataStorage.getTimeoutParam());
                    mTimeOutPref.setSummary(String.valueOf(_timeout));
                }
                break;
            case "pref_hwlfd":
                Logger.d("pref_hwlfd is changed! : "+ newValue);
                int _hwlfd = Integer.parseInt((String)newValue);
                if(_hwlfd < 0 || _hwlfd > 5)
                {
                    Toast.makeText(mContext,getResources().getString(R.string.pref_error_wrong_range_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = mActivity.setParameterToDevice(preference.getKey(), _hwlfd, false);
                if(result == 0)
                {
                    mDataStorage.setLfdWithDeviceParam(_hwlfd);
                    Logger.d("data check : " + mDataStorage.getLfdWithDeviceParam());
                    mHwLfdPref.setSummary(String.valueOf(_hwlfd));
                }
                break;
            case "pref_swlfd":
                Logger.d("pref_swlfd is changed! : "+ newValue);
                int _swlfd = Integer.parseInt((String)newValue);
                if(_swlfd < 0 || _swlfd > 5)
                {
                    Toast.makeText(mContext,getResources().getString(R.string.pref_error_wrong_range_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = mActivity.setParameterToDevice(preference.getKey(),_swlfd, false);
                if(result == 0 )
                {
                    mDataStorage.setLfdWithSdkParam(_swlfd);
                    Logger.d("data check : " + mDataStorage.getLfdWithSdkParam());
                    mSwLfdPref.setSummary(String.valueOf(_swlfd));
                }
                break;
            case "pref_fastmode":
                Logger.d("pref_fastmode is changed! : "+ newValue);
                mDataStorage.setFastModeParam((boolean)newValue);
                Logger.d("data check : " + mDataStorage.getFastModeParam());
                mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                break;
            case "pref_cropmode":
                Logger.d("pref_cropmode is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setCropModeParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getCropModeParam());
                }
                break;
            case "pref_exttriger":
                Logger.d("pref_exttriger is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setExtTriggerParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getExtTriggerParam());
                }
                break;
            case "pref_autosleep":
                Logger.d("pref_autosleep is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setAutoSleepParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getAutoSleepParam());
                }
                break;
            case "pref_imageflip180d":
                Logger.d("pref_imageflip180d is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setImageFlip180dParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getImageFlip180dParam());
                }
                break;
            case "pref_omniDirVerify":
                Logger.d("pref_omniDirVerify is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setOmniDirVerifyParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getOmniDirVerifyParam());
                }
                break;
            case "pref_detectcore":
                Logger.d("pref_detectcore is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setDetectCoreParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getDetectCoreParam());
                }
                break;
            case "pref_templatequalityex":
                Logger.d("pref_templatequalityex is changed! : "+ newValue);
                result = mActivity.setParameterToDevice(preference.getKey(), ((boolean)newValue == true ? 1:0), false);
                if(result == 0)
                {
                    mDataStorage.setTemplateQualityExParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getTemplateQualityExParam());
                }
                break;
            case "pref_exporttemplatetype":
                Logger.d("pref_exporttemplatetype is changed! : "+ newValue);
                mDataStorage.setExportTemplateTypeParam((String)newValue);
                result = mActivity.setParameterToDevice(preference.getKey(),mDataStorage.getExportTemplateTypeParam(),false);
                if(result == 0)
                {
                    Logger.d("data check : " + mDataStorage.getExportTemplateTypeParam());
                    int currentTemplateTypeValueIndex = mExportTemplateTypePref.findIndexOfValue((String)newValue);
                    mExportTemplateTypePref.setSummary(mExportTemplateTypePref.getEntries()[currentTemplateTypeValueIndex]);
                    mExportTemplateTypePref.setValueIndex(currentTemplateTypeValueIndex);
                }
                break;
            case "pref_usbNativeUsbMode":
                Logger.d("pref_usbNativeUsbMode is changed!");
                result = mActivity.setTransferMode(mDataStorage.getUseNativeUsbModeParam(),true);
                if(result == 0)
                {
                    mDataStorage.setUseNativeUsbModeParam((boolean)newValue);
                    Logger.d("data check : " + mDataStorage.getUseNativeUsbModeParam());
                }
                break;
        }
        mHandler.sendEmptyMessage(EVENT_SCREEN_UPDATE);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d("START!");
        mIsRunning = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d("START!");
        mIsRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SCREEN_UPDATE:
                    Logger.d("EVENT_SCREEN_UPDATE");
                    pref();
                    break;

                default:
                    break;
            }
        }
    };

    void pref()
    {
        Logger.d("START!");
        if(mActivity.mCurrentDevice != null)
        {
            updateSensitivityPref();

            updateSecurityPref();

            updateTimeOutPref();

            updateLfdWithDevicePref();

            updateLfdWithSdkPref();

            updateFastModePref();

            updateCropModePref();

            updateExtTriggerPref();

            updateAutoSleepPref();

            updateImageFlip180dPref();

            updateDetectCorePref();

            updateTemplateQualityExPref();

            updateExportTemplateTypePref();

            updateUseNativeUsbModePref();
        }
        mIsPrefRefreshed = true;
    }

    private void updateUseNativeUsbModePref() {
        boolean _usbNativeUsbMode = mDataStorage.getUseNativeUsbModeParam();
        Logger.d("_usbNativeUsbMode : " + _usbNativeUsbMode);
        mUseNativeUsbModePref.setChecked(_usbNativeUsbMode);
        if(_usbNativeUsbMode)
        {
            mUseNativeUsbModePref.setSummary("");
        }
        else
        {
            mUseNativeUsbModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_useNativeUsbMode));
        }
    }

    private void updateExportTemplateTypePref()
    {
        int currentTemplateTypeValue = 0;

        if(mDataStorage.getExportTemplateTypeParam() == 2001)
        {
            currentTemplateTypeValue = 0;
        }
        if(mDataStorage.getExportTemplateTypeParam() == 2002)
        {
            currentTemplateTypeValue = 1;
        }
        if(mDataStorage.getExportTemplateTypeParam() == 2003)
        {
            currentTemplateTypeValue = 2;
        }
        mExportTemplateTypePref.setSummary(mExportTemplateTypePref.getEntries()[currentTemplateTypeValue]);
        mExportTemplateTypePref.setValueIndex(currentTemplateTypeValue);
    }

    private void updateTemplateQualityExPref()
    {
        boolean templatequalityexVal = mDataStorage.getTemplateQualityExParam();
        Logger.d("templatequalityexVal : " + templatequalityexVal);
        mTemplateQualityExPref.setChecked(templatequalityexVal);
        if(templatequalityexVal)
        {
            mTemplateQualityExPref.setSummary("");
        }
        else
        {
            mTemplateQualityExPref.setSummary(mContext.getResources().getString(R.string.pref_summery_templatequalityex));
        }
    }

    private void updateDetectCorePref()
    {
        boolean detectcoreVal = mDataStorage.getDetectCoreParam();
        Logger.d("detectcoreVal : " + detectcoreVal);
        mDetectCorePref.setChecked(detectcoreVal);
        if(detectcoreVal)
        {
            mDetectCorePref.setSummary("");
        }
        else
        {
            mDetectCorePref.setSummary(mContext.getResources().getString(R.string.pref_summery_detectcore));
        }
    }

    private void updateImageFlip180dPref()
    {
        boolean autorotateVal = mDataStorage.getImageFlip180dParam();
        Logger.d("autorotateVal : " + autorotateVal);
        mImageFlip180dPref.setChecked(autorotateVal);
        if(autorotateVal)
        {
            mImageFlip180dPref.setSummary("");
        }
        else
        {
            mImageFlip180dPref.setSummary(mContext.getResources().getString(R.string.pref_summery_imageflip180d));
        }
    }
    private void updateOmniDirVerifyPref()
    {
        boolean omniDirVerifyParam = mDataStorage.getOmniDirVerifyParam();
        Logger.d("omniDirVerifyParam : " + omniDirVerifyParam);
        mOmniDirVerifyPref.setChecked(omniDirVerifyParam);
        if(omniDirVerifyParam)
        {
            mOmniDirVerifyPref.setSummary("");
        }
        else
        {
            mOmniDirVerifyPref.setSummary(mContext.getResources().getString(R.string.pref_summery_imageflip180d));
        }
    }

    private void updateAutoSleepPref()
    {
        boolean autosleepVal = mDataStorage.getAutoSleepParam();
        Logger.d("autosleepVal : " + autosleepVal);
        mAutoSleepPref.setChecked(autosleepVal);
        if(autosleepVal)
        {
            mAutoSleepPref.setSummary("");
        }
        else
        {
            mAutoSleepPref.setSummary(mContext.getResources().getString(R.string.pref_summery_autosleep));
        }
    }
    private void updateExtTriggerPref()
    {
        boolean exttrigerVal = mDataStorage.getExtTriggerParam();
        Logger.d("exttrigerVal : " + exttrigerVal);
        mExtTrigerPref.setChecked(exttrigerVal);
        if(exttrigerVal)
        {
            mExtTrigerPref.setSummary("");
        }
        else
        {
            mExtTrigerPref.setSummary(mContext.getResources().getString(R.string.pref_summery_exttriger));
        }
    }

    private void updateCropModePref()
    {
        boolean cropmodeVal = mDataStorage.getCropModeParam();
        Logger.d("cropmodeVal : " + cropmodeVal);
        mCropModePref.setChecked(cropmodeVal);
        if(cropmodeVal)
        {
            mCropModePref.setSummary("");
        }
        else
        {
            mCropModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_cropmode));
        }
    }

    private void updateFastModePref()
    {
        boolean fastmodeVal = mDataStorage.getFastModeParam();
        Logger.d("fastmodeVal : " + fastmodeVal);
        mFastModePref.setChecked(fastmodeVal);
        if(fastmodeVal)
        {
            mFastModePref.setSummary("");
        }
        else
        {
            mFastModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_fastmode));
        }
    }

    private void updateLfdWithSdkPref()
    {
        int swlfdVal = mDataStorage.getLfdWithSdkParam();
        if(swlfdVal == 0)
        {
            mSwLfdPref.setSummary(mContext.getResources().getString(R.string.pref_summery_swlfd));
        }
        else if(swlfdVal == -111)
        {
            mSwLfdPref.setSummary(mContext.getResources().getString(R.string.pref_summery_unsupport_swlfd));
        }
        else
        {
            mSwLfdPref.setSummary(String.valueOf(swlfdVal));
        }
    }

    private void updateLfdWithDevicePref()
    {
        int hwlfdValue = mDataStorage.getLfdWithDeviceParam();
        if(bHwLfdSupport)
        {
            if(hwlfdValue == 0)
            {
                mHwLfdPref.setSummary(mContext.getResources().getString(R.string.pref_summery_hwlfd));
            }
            else
            {
                mHwLfdPref.setSummary(String.valueOf(hwlfdValue));
            }
        }
        else
            mHwLfdPref.setSummary(mContext.getResources().getString(R.string.pref_summery_unsupport_hwlfd));

    }

    private void updateTimeOutPref()
    {
        int timeoutVal = mDataStorage.getTimeoutParam();
        if(timeoutVal == 0)
        {
            mTimeOutPref.setSummary(mContext.getResources().getString(R.string.pref_summery_timeout));
        }
        else
        {
            mTimeOutPref.setSummary(String.valueOf(timeoutVal));
        }
    }

    private void updateSecurityPref()
    {
        int securityPrefVal = mDataStorage.getSecurityParam();
        if(securityPrefVal == 0)
        {
            mSecurityPref.setSummary(mContext.getResources().getString(R.string.pref_summery_security));
        }
        else
        {
            mSecurityPref.setSummary(String.valueOf(securityPrefVal));
        }

    }

    private void updateSensitivityPref()
    {
        Logger.d("sensitivityVal : " + mDataStorage.getSensitivityPram());
        int sensitivityVal = mDataStorage.getSensitivityPram();
        if(sensitivityVal == 0)
        {
            mSensitivityPref.setSummary(mContext.getResources().getString(R.string.pref_summery_sensitivity));
        }
        else
        {
            mSensitivityPref.setSummary(String.valueOf(sensitivityVal));
        }
    }

    private void loadPreference() {
        Logger.d("START!");
        mSensitivityPref = findPreference("pref_sensitivity");
        mSensitivityPref.setOnPreferenceChangeListener(this);
        mSensitivityPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getSensitivityPram()));
                editText.setSelection(editText.getText().length());
            }
        });

        mSecurityPref = findPreference("pref_security");
        mSecurityPref.setOnPreferenceChangeListener(this);
        mSecurityPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getSecurityParam()));
                editText.setSelection(editText.getText().length());
            }
        });

        mTimeOutPref = findPreference("pref_timeout");
        mTimeOutPref.setOnPreferenceChangeListener(this);
        mTimeOutPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getTimeoutParam()));
                editText.setSelection(editText.getText().length());
            }
        });

        mHwLfdPref = findPreference("pref_hwlfd");
        mHwLfdPref.setOnPreferenceChangeListener(this);
        mHwLfdPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getLfdWithDeviceParam()));
                editText.setSelection(editText.getText().length());
            }
        });


        mSwLfdPref = findPreference("pref_swlfd");
        mSwLfdPref.setOnPreferenceChangeListener(this);
        mSwLfdPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getLfdWithSdkParam()));
                editText.setSelection(editText.getText().length());
            }
        });

        mFastModePref = findPreference("pref_fastmode");
        mFastModePref.setOnPreferenceChangeListener(this);

        mCropModePref = findPreference("pref_cropmode");
        mCropModePref.setOnPreferenceChangeListener(this);

        mExtTrigerPref = findPreference("pref_exttriger");
        mExtTrigerPref.setOnPreferenceChangeListener(this);

        mAutoSleepPref = findPreference("pref_autosleep");
        mAutoSleepPref.setOnPreferenceChangeListener(this);

        mImageFlip180dPref = findPreference("pref_imageflip180d");
        mImageFlip180dPref.setOnPreferenceChangeListener(this);

        mOmniDirVerifyPref = findPreference("pref_omniDirVerify");
        mOmniDirVerifyPref.setOnPreferenceChangeListener(this);

        mDetectCorePref = findPreference("pref_detectcore");
        mDetectCorePref.setOnPreferenceChangeListener(this);

        mTemplateQualityExPref = findPreference("pref_templatequalityex");
        mTemplateQualityExPref.setOnPreferenceChangeListener(this);

        mExportTemplateTypePref = findPreference("pref_exporttemplatetype");
        mExportTemplateTypePref.setOnPreferenceChangeListener(this);
        mUseNativeUsbModePref = (SwitchPreference) findPreference("pref_usbNativeUsbMode");
        mUseNativeUsbModePref.setOnPreferenceChangeListener(this);

        mLicensePref = (Preference) findPreference("pref_license");
        mLicensePref.setOnPreferenceClickListener(this);
    }
    private void initDataStorage() {
        Logger.d("START!");
        mDataStorage = new DataStorage();
        mDataStorage.init(mContext);
    }

    public void resetPreference()
    {
        setSensitivityPref(false);
        setSecurityPref(false);
        setTimeOutPref(false);
        setHwLfdPref(false);
        setSwLfdPref(false);
        setFastModePref(false);
        setCropModePref(false);
        setExtTrigerPref(false);
        setAutoSleepPref(false);
        setImageFlip180dPref(false);
        setOmniDirVerifyPref(false);
        setDetectCorePref(false);
        setTemplateQualityExPref(false);
        setExportTemplateTypePref(false);
        setUseNativeUsbModePref(false);
    }
    public void setInitPreference()
    {
        //HW LFD
        UsbDevice _device = mActivity.mUsbDevice;
        if(_device == null)
        {
            Logger.e("No Usb Device. Return.");
            return;
        }
        IBioMiniDevice _bioMiniDevice = mActivity.mCurrentDevice;
        if(_bioMiniDevice == null)
        {
            Logger.e("No BioMini Device. Return.");
            return;
        }
        //Sensitivity
        setSensitivityPref(true);

        //Security
        setSecurityPref(true);

        //Timeout
        setTimeOutPref(true);


        if(_device.getProductId() == BioMiniSlimS ||_device.getProductId() == BioMiniSlim2S)
        {
            setHwLfdPref(true);
            bHwLfdSupport = true;
        }
        else
        {
            setHwLfdPref(false);
            bHwLfdSupport = false;
        }

        //SW LFD
        setSwLfdPref(true);

        //FAST MODE
        setFastModePref(true);

        if(mActivity.mUsbDevice.getProductId() == BioMiniPlus2)
            setAutoSleepPref(false);
        else
            setAutoSleepPref(true);
        //Crop Mode
        if(_device.getProductId() == BioMiniPlus2)
        {
            setCropModePref(true);
        }
        else
        {
            setCropModePref(false);
        }
        //DETECT CORE
//        if(_device.getProductId() == BioMiniSlim2)
//        {
//            mDetectCorePref.setEnabled(true);
//        }
//        else
//        {
//            mDetectCorePref.setEnabled(false);
//        }
        setDetectCorePref(true);

        //Ext Trigger
        if(mActivity.mUsbDevice.getProductId() == BioMiniOC4
            || mActivity.mUsbDevice.getProductId() == BioMiniPlus2
            || (mActivity.mCurrentDevice.getDeviceInfo().hasTouchSenor == false && mActivity.mUsbDevice.getProductId() == BioMiniSlim))
            setExtTrigerPref(false);
        else
            setExtTrigerPref(true);

        //Image Flip 180d
        setImageFlip180dPref(true);

        //OmniDir Verify
        setOmniDirVerifyPref(true);

        //Template Quality Ex
        setTemplateQualityExPref(true);

        //Tempate Type
        setExportTemplateTypePref(true);
        setUseNativeUsbModePref(true);
    }
    public void setSensitivityPref (boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mSensitivityPref.setVisible(true);
        else
        {
            if(_enable == true)
                mSensitivityPref.setVisible(true);
            else
                mSensitivityPref.setVisible(false);
        }
        mSensitivityPref.setEnabled(_enable);
    }
    public void setSecurityPref (boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mSecurityPref.setVisible(true);
        else
        {
            if(_enable == true)
                mSecurityPref.setVisible(true);
            else
                mSecurityPref.setVisible(false);
        }
        mSecurityPref.setEnabled(_enable);
    }
    public void setTimeOutPref (boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mTimeOutPref.setVisible(true);
        else
        {
            if(_enable == true)
                mTimeOutPref.setVisible(true);
            else
                mTimeOutPref.setVisible(false);
        }
        mTimeOutPref.setEnabled(_enable);
    }
    public void setHwLfdPref (boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mHwLfdPref.setVisible(true);
        else
        {
            if(_enable == true)
                mHwLfdPref.setVisible(true);
            else
                mHwLfdPref.setVisible(false);
        }
        mHwLfdPref.setEnabled(_enable);
    }
    public void setSwLfdPref (boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mSwLfdPref.setVisible(true);
        else
        {
            if(_enable == true)
                mSwLfdPref.setVisible(true);
            else
                mSwLfdPref.setVisible(false);
        }
        mSwLfdPref.setEnabled(_enable);
    }
    public void setFastModePref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mFastModePref.setVisible(true);
        else
        {
            if(_enable == true)
                mFastModePref.setVisible(true);
            else
                mFastModePref.setVisible(false);
        }
        mFastModePref.setEnabled(_enable);
    }
    public void setCropModePref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mCropModePref.setVisible(true);
        else
        {
            if(_enable == true)
                mCropModePref.setVisible(true);
            else
                mCropModePref.setVisible(false);
        }
        mCropModePref.setEnabled(_enable);
    }
    public void setExtTrigerPref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mExtTrigerPref.setVisible(true);
        else
        {
            if(_enable == true)
                mExtTrigerPref.setVisible(true);
            else
                mExtTrigerPref.setVisible(false);
        }
        mExtTrigerPref.setEnabled(_enable);
    }
    public void setAutoSleepPref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mAutoSleepPref.setVisible(true);
        else
        {
            if(_enable == true)
                mAutoSleepPref.setVisible(true);
            else
                mAutoSleepPref.setVisible(false);
        }
        mAutoSleepPref.setEnabled(_enable);
    }
    public void setImageFlip180dPref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mImageFlip180dPref.setVisible(true);
        else
        {
            if(_enable == true)
                mImageFlip180dPref.setVisible(true);
            else
                mImageFlip180dPref.setVisible(false);
        }
        mImageFlip180dPref.setEnabled(_enable);
    }
    public void setOmniDirVerifyPref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mOmniDirVerifyPref.setVisible(true);
        else
        {
            if(_enable == true)
                mOmniDirVerifyPref.setVisible(true);
            else
                mOmniDirVerifyPref.setVisible(false);
        }
        mOmniDirVerifyPref.setEnabled(_enable);
    }
    public void setDetectCorePref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mDetectCorePref.setVisible(true);
        else
        {
            if(_enable == true)
                mDetectCorePref.setVisible(true);
            else
                mDetectCorePref.setVisible(false);
        }
        mDetectCorePref.setEnabled(_enable);
    }
    public void setTemplateQualityExPref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mTemplateQualityExPref.setVisible(true);
        else
        {
            if(_enable == true)
                mTemplateQualityExPref.setVisible(true);
            else
                mTemplateQualityExPref.setVisible(false);
        }
        mTemplateQualityExPref.setEnabled(_enable);
    }
    public void setExportTemplateTypePref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mExportTemplateTypePref.setVisible(true);
        else
        {
            if(_enable == true)
                mExportTemplateTypePref.setVisible(true);
            else
                mExportTemplateTypePref.setVisible(false);
        }
        mExportTemplateTypePref.setEnabled(_enable);
    }
    public void setUseNativeUsbModePref(boolean _enable)
    {
        if(mActivity.mCurrentDevice == null)
            mUseNativeUsbModePref.setVisible(true);
        else
        {
            if(_enable == true)
                mUseNativeUsbModePref.setVisible(true);
            else
                mUseNativeUsbModePref.setVisible(false);
        }
        mUseNativeUsbModePref.setEnabled(_enable);
    }
    private void showLicenseInfo() {
        Logger.start();
        try {
            mBuilder = new AlertDialog.Builder(mContext);

            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setPositiveButton clicked");
                    dialog.dismiss();
                }
            });
            CharSequence cs = "[Copyright notice]\n\n"+
                    "The copyright of this document is vested in Suprema ID.\n"+
                    "The rights of other product names, trademarks and registered\ntrademarks are vested in each individual or organization that owns such rights.\n\n"+
                    "[Open Source License]\n\n"+
                    "This product uses the libUSB1.0 library, which is licensed under LGPL 2.1.\n" +
                    "This product uses the NFIQ, which is licensed under NIST License. \n"+
                    "This product uses the BiomDI, which is licensed under NIST license.\n\n"+
                    "For more details, refer to chapter 3. Appendix of SW SDK Manual.";

            mBuilder.setMessage(cs);
            mBuilder.setCancelable(false);
            mBuilder.show();
        }catch (Exception e)
        {
            Logger.d("e: " +e.getMessage());
        }
    }
}
