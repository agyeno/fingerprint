package com.suprema.biominisample;

import android.content.Context;
import android.content.SharedPreferences;

import com.suprema.util.Logger;

import java.util.HashSet;
import java.util.Set;

public class DataStorage {
    public static final String PREFERENCES_NAME = "biomini_data";
    private static final String DEFAULT_VALUE_STRING = "-1";
    private static final Set<String> DEFAULT_VALUE_STRINGSET = null;
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;

    //data
    public static final String SENSITIVITY_KEY = "pref_sensitivity";
    public static   int DEFAULT_SENSITIVITY_VALUE;

    public static final String SECURITY_KEY = "pref_security";
    public static   int DEFAULT_SECURITY_VALUE;

    public static final String TIMEOUT_KEY = "pref_timeout";
    public static   int DEFAULT_TIMEOUT_VALUE;

    public static final String LFD_WITH_DEVICE_KEY = "pref_hwlfd";
    public static   int DEFAULT_LFD_WITH_DEVICE_VALUE;

    public static final String LFD_WITH_SDK_KEY = "pref_swlfd";
    public static   int DEFAULT_LFD_WITH_SDK_VALUE;

    public static final String FASTMODE_KEY = "pref_fastmode";
    public static boolean   DEFAULT_FASTMODE_VALUE;

    public static final String CROPMODE_KEY = "pref_cropmode";
    public static boolean   DEFAULT_CROPMODE_VALUE;

    public static final String EXT_TRIGGER_KEY = "pref_exttriger";
    public static boolean   DEFAULT_EXT_TRIGGER_VALUE;

    public static final String AUTO_SLEEP_KEY = "pref_autosleep";
    public static boolean   DEFAULT_AUTO_SLEEP_VALUE;

    public static final String IMAGE_FLIP_180d_KEY = "pref_imageflip180d";
    public static boolean   DEFAULT_IMAGE_FLIP_180d_VALUE;

    public static final String OMNI_DIR_VERIFY_KEY = "pref_omniDirVerify";
    public static boolean   DEFAULT_OMNIDIR_VERIFY_VALUE;

    public static final String DETECT_CORE_KEY = "pref_detectcore";
    public static boolean   DEFAULT_DETECT_CORE_VALUE;

    public static final String TEMPLATE_QUALITY_EX_KEY = "pref_templatequalityex";
    public static boolean   DEFAULT_TEMPLATE_QUALITY_EX_VALUE;

    public static final String EXPORT_TEMPLATE_TYPE_KEY = "pref_exporttemplatetype";
    public static int    DEFAULT_EXPORT_TEMPLATE_TYPE_VALUE;

    public static final String USB_LIB_USB_MODE_KEY = "pref_usbNativeUsbMode";
    public static boolean   DEFAULT_USB_LIB_USB_MODE_VALUE;

    private Context mContext;
    private SharedPreferences   mPreference;

    public DataStorage() { }

    public void init(Context context)
    {
        mContext = context;
        mPreference =   mContext.getSharedPreferences(PREFERENCES_NAME,Context.MODE_PRIVATE);
        setDefaultPrefValue();
    }

    private void setDefaultPrefValue() {
        DEFAULT_SENSITIVITY_VALUE = 7;
        DEFAULT_SECURITY_VALUE = 4;
        DEFAULT_TIMEOUT_VALUE = 10;
        DEFAULT_LFD_WITH_DEVICE_VALUE = 0;
        DEFAULT_LFD_WITH_SDK_VALUE = 0;
        DEFAULT_EXPORT_TEMPLATE_TYPE_VALUE = 0;
        DEFAULT_FASTMODE_VALUE = false;
        DEFAULT_CROPMODE_VALUE = false;
        DEFAULT_EXT_TRIGGER_VALUE = false;
        DEFAULT_AUTO_SLEEP_VALUE = false;
        DEFAULT_IMAGE_FLIP_180d_VALUE = false;
        DEFAULT_OMNIDIR_VERIFY_VALUE = false;
        DEFAULT_DETECT_CORE_VALUE = false;
//        DEFAULT_USB_LIB_USB_MODE_VALUE = false;
    }

    /**
     * String 값 저장
     * @param key
     * @param value
     */
    public void setString( String key, String value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }
    /**
     * StringSet 저장
     * @param key
     * @param dataSet
     */
    public void setStringSet( String key, Set<String> dataSet) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putStringSet(key, dataSet);
        editor.commit();
    }
    /**
     * boolean 값 저장
     * @param key
     * @param value
     */
    public void setBoolean( String key, boolean value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    /**
     * int 값 저장
     * @param key
     * @param value
     */
    public void setInt( String key, int value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    /**
     * long 값 저장
     * @param key
     * @param value
     */
    public void setLong( String key, long value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    /**
     * float 값 저장
     * @param key
     * @param value
     */
    public void setFloat( String key, float value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    /**
     * String 값 로드
     * @param key
     * @return
     */
    public String getString( String key) {
        String value = mPreference.getString(key, DEFAULT_VALUE_STRING);
        return value;
    }
    /**
     * String 값 로드
     * @param key
     * @return
     */
    public Set<String> getStringSet( String key) {
        Set<String> dataSet = mPreference.getStringSet(key, DEFAULT_VALUE_STRINGSET);
        return dataSet;
    }

    /**
     * boolean 값 로드
     * @param key
     * @return
     */
    public boolean getBoolean( String key) {
        boolean value = mPreference.getBoolean(key, DEFAULT_VALUE_BOOLEAN);
        return value;
    }
    /**
     * int 값 로드
     * @param key
     * @return
     */
    public int getInt( String key) {
        int value = mPreference.getInt(key, DEFAULT_VALUE_INT);
        return value;
    }
    /**
     * long 값 로드
     * @param key
     * @return
     */
    public long getLong( String key) {
        long value = mPreference.getLong(key, DEFAULT_VALUE_LONG);
        return value;
    }
    /**
     * float 값 로드
     * @param key
     * @return
     */
    public float getFloat( String key) {
        float value = mPreference.getFloat(key, DEFAULT_VALUE_FLOAT);
        return value;
    }
    /**
     * 키 값 삭제
     * @param key
     */
    public void removeKey( String key) {
        SharedPreferences.Editor edit = mPreference.edit();
        edit.remove(key);
        edit.commit();
    }
    /**
     * 모든 저장 데이터 삭제
     * @param context
     */
    public void clear(Context context) {
        SharedPreferences.Editor edit = mPreference.edit();
        edit.clear();
        edit.commit();
    }
    /**
     * 키 값 체크
     * @param key
     */
    public boolean containKey(Context context, String key) {
        return mPreference.contains(key);
    }
    /**
     * 키 값 삭제
     * @param context
     * @param key
     */
    public void removeKey(Context context, String key) {
        SharedPreferences.Editor edit = mPreference.edit();
        edit.remove(key);
        edit.commit();
    }


    public void setSensitivityPram(int _value)
    {
        setInt(SENSITIVITY_KEY,_value);
    }
    public int getSensitivityPram()
    {
        return mPreference.getInt(SENSITIVITY_KEY,DEFAULT_SENSITIVITY_VALUE);
    }

    public void setSecurityParam(int _value)
    {
        setInt(SECURITY_KEY,_value);
    }
    public int getSecurityParam()
    {
        return mPreference.getInt(SECURITY_KEY,DEFAULT_SECURITY_VALUE);
    }

    public void setTimeoutParam(int _value)
    {
        setInt(TIMEOUT_KEY,_value);
    }
    public int getTimeoutParam()
    {
        return mPreference.getInt(TIMEOUT_KEY,DEFAULT_TIMEOUT_VALUE);
    }

    public void setLfdWithDeviceParam(int _value)
    {
        setInt(LFD_WITH_DEVICE_KEY,_value);
    }
    public int getLfdWithDeviceParam()
    {
        return mPreference.getInt(LFD_WITH_DEVICE_KEY,DEFAULT_LFD_WITH_DEVICE_VALUE);
    }

    public void setLfdWithSdkParam(int _value)
    {
        setInt(LFD_WITH_SDK_KEY,_value);
    }
    public int getLfdWithSdkParam()
    {
        return mPreference.getInt(LFD_WITH_SDK_KEY,DEFAULT_LFD_WITH_SDK_VALUE);
    }

    public void setFastModeParam(Boolean _value)
    {
        setBoolean(FASTMODE_KEY,_value);
    }
    public boolean getFastModeParam()
    {
        return mPreference.getBoolean(FASTMODE_KEY,false);
    }

    public void setCropModeParam(Boolean _value)
    {
        setBoolean(CROPMODE_KEY,_value);
    }
    public boolean getCropModeParam()
    {
        return mPreference.getBoolean(CROPMODE_KEY,false);
    }

    public void setExtTriggerParam(Boolean _value)
    {
        setBoolean(EXT_TRIGGER_KEY,_value);
    }
    public boolean getExtTriggerParam()
    {
        return mPreference.getBoolean(EXT_TRIGGER_KEY,false);
    }

    public void setAutoSleepParam(Boolean _value)
    {
        setBoolean(AUTO_SLEEP_KEY,_value);
    }
    public boolean getAutoSleepParam()
    {
        return mPreference.getBoolean(AUTO_SLEEP_KEY,false);
    }

    public void setImageFlip180dParam(Boolean _value)
    {
        setBoolean(IMAGE_FLIP_180d_KEY,_value);
    }
    public boolean getImageFlip180dParam()
    {
        return mPreference.getBoolean(IMAGE_FLIP_180d_KEY,false);
    }

    public void setOmniDirVerifyParam(Boolean _value)
    {
        setBoolean(OMNI_DIR_VERIFY_KEY,_value);
    }
    public boolean getOmniDirVerifyParam()
    {
        return mPreference.getBoolean(OMNI_DIR_VERIFY_KEY,false);
    }

    public void setDetectCoreParam(Boolean _value)
    {
        setBoolean(DETECT_CORE_KEY,_value);
    }
    public boolean getDetectCoreParam()
    {
        return mPreference.getBoolean(DETECT_CORE_KEY,false);
    }
    public void setTemplateQualityExParam(Boolean _value)
    {
        setBoolean(TEMPLATE_QUALITY_EX_KEY,_value);
    }
    public boolean getTemplateQualityExParam()
    {
        return mPreference.getBoolean(TEMPLATE_QUALITY_EX_KEY,false);
    }

    public void setExportTemplateTypeParam(String _value)
    {
        int _valueToSet = Integer.parseInt(_value);
        setInt(EXPORT_TEMPLATE_TYPE_KEY,_valueToSet);
    }
    public int getExportTemplateTypeParam()
    {
        return mPreference.getInt(EXPORT_TEMPLATE_TYPE_KEY,DEFAULT_EXPORT_TEMPLATE_TYPE_VALUE);
    }

    public void setUseNativeUsbModeParam(boolean _value) {
        setBoolean(USB_LIB_USB_MODE_KEY,_value);
    }
    public boolean getUseNativeUsbModeParam()
    {
        Logger.d("getUseNativeUsbModeParam : " + mPreference.getBoolean(USB_LIB_USB_MODE_KEY,false));
        return mPreference.getBoolean(USB_LIB_USB_MODE_KEY,false);
    }

    public String getSensitivityKey()
    {
        return SENSITIVITY_KEY;
    }
    public String getSecurityKey()
    {
        return SECURITY_KEY;
    }
    public String getTimeoutKey()
    {
        return TIMEOUT_KEY;
    }
    public String getLfdWithDeviceKey()
    {
        return LFD_WITH_DEVICE_KEY;
    }
    public String getLfdWithSdkKey()
    {
        return LFD_WITH_SDK_KEY;
    }
    public String getFastModeKey()
    {
        return FASTMODE_KEY;
    }
    public String getCropModeKey()
    {
        return CROPMODE_KEY;
    }
    public String getExtTriggerKey()
    {
        return EXT_TRIGGER_KEY;
    }
    public String getAutoSleepKey()
    {
        return AUTO_SLEEP_KEY;
    }
    public String getImageFlip180dKey()
    {
        return IMAGE_FLIP_180d_KEY;
    }
    public String getOmniDirVerifyKey()
    {
        return OMNI_DIR_VERIFY_KEY;
    }
    public String getDetectCoreKey()
    {
        return DETECT_CORE_KEY;
    }
    public String getTemplateQualityExKey()
    {
        return TEMPLATE_QUALITY_EX_KEY;
    }
    public String getExportTemplateTypeKey()
    {
        return EXPORT_TEMPLATE_TYPE_KEY;
    }
}
