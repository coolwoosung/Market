package kr.co.mishmash.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.webkit.JavascriptInterface;

public class WebInterface {
    private static final String TAG = "WebInterface";

    private Context mContext;
    private MainActivity mMainActivity;
    public final Handler mHandler = new Handler();

    public WebInterface(Context context, MainActivity mainActivity) {
        mContext = context;
        mMainActivity = mainActivity;
    }

    @JavascriptInterface
    public String getToken() {
        SharedPreferences spfs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        String token = spfs.getString(PrefsConstans.fcmDeviceToken, "");

        return token;
    }

    @JavascriptInterface
    public boolean getPushSound() {
        SharedPreferences spfs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        boolean pushSound = spfs.getBoolean(PrefsConstans.pushSound, true);

        return pushSound;
    }

    @JavascriptInterface
    public boolean getPushVibrate() {
        SharedPreferences spfs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        boolean pushVibrate = spfs.getBoolean(PrefsConstans.pushVibrate, true);

        return pushVibrate;
    }

    @JavascriptInterface
    public void setPushSound(boolean val) {
        SharedPreferences spfs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor spfsEditor = spfs.edit();
        spfsEditor.putBoolean(PrefsConstans.pushSound, val);
        spfsEditor.apply();
    }

    @JavascriptInterface
    public void setPushVibrate(boolean val) {
        SharedPreferences spfs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor spfsEditor = spfs.edit();
        spfsEditor.putBoolean(PrefsConstans.pushVibrate, val);
        spfsEditor.apply();
    }
}
