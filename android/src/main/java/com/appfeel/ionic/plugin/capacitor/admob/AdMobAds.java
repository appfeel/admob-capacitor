/*
 AdMobAds.java
 Copyright 2015 AppFeel. All rights reserved.
 http://www.appfeel.com

 AdMobAds Cordova Plugin (cordova-admob)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to
 deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 sell copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package com.appfeel.ionic.plugin.capacitor.admob;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.Bridge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.appfeel.ionic.plugin.capacitor.admob.Connectivity;
import com.appfeel.ionic.plugin.capacitor.admob.Connectivity.IConnectivityChange;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.AdLoader;

@NativePlugin()
public class AdMobAds extends Plugin implements IConnectivityChange {

    public static final String ADMOBADS_LOGTAG = "AdmMobAds";
    public static final String INTERSTITIAL = "interstitial";
    public static final String BANNER = "banner";
    public static final String REWARDED = "rewarded";

    private static final String DEFAULT_AD_PUBLISHER_ID = "ca-app-pub-8440343014846849/3119840614";
    private static final String DEFAULT_INTERSTITIAL_PUBLISHER_ID = "ca-app-pub-8440343014846849/4596573817";
    private static final String DEFAULT_REWARDED_PUBLISHER_ID = "ca-app-pub-8440343014846849/4854611361";
    private static final String DEFAULT_TAPPX_ID = "/120940746/Pub-2700-Android-8171";

    /* options */
    private static final String OPT_PUBLISHER_ID = "publisherId";
    private static final String OPT_BANNER_AD_ID = "bannerAdId";
    private static final String OPT_INTERSTITIAL_AD_ID = "interstitialAdId";
    private static final String OPT_REWARDED_AD_ID = "rewardedAdId";
    private static final String OPT_AD_SIZE = "adSize";
    private static final String OPT_BANNER_AT_TOP = "bannerAtTop";
    private static final String OPT_OVERLAP = "overlap";
    private static final String OPT_OFFSET_STATUSBAR = "offsetStatusBar";
    private static final String OPT_IS_TESTING = "isTesting";
    private static final String OPT_AD_EXTRAS = "adExtras";
    private static final String OPT_AUTO_SHOW_BANNER = "autoShowBanner";
    private static final String OPT_AUTO_SHOW_INTERSTITIAL = "autoShowInterstitial";
    private static final String OPT_AUTO_SHOW_REWARDED = "autoShowRewarded";
    private static final String OPT_TAPPX_ID_ANDROID = "tappxIdAndroid";
    private static final String OPT_TAPPX_SHARE = "tappxShare";
    protected boolean isBannerAutoShow = true;
    protected boolean isInterstitialAutoShow = true;
    protected boolean isRewardedAutoShow = true;
    private Connectivity connectivity;
    private AdMobAdsAdListener bannerListener = new AdMobAdsAdListener(BANNER, this, false);
    private AdMobAdsAdListener interstitialListener = new AdMobAdsAdListener(INTERSTITIAL, this, false);
    private AdMobAdsRewardedAdListener rewardedListener = new AdMobAdsRewardedAdListener(REWARDED, this, false);
    private AdMobAdsAdListener backFillBannerListener = new AdMobAdsAdListener(BANNER, this, true);
    private AdMobAdsAdListener backFillInterstitialListener = new AdMobAdsAdListener(INTERSTITIAL, this, true);
    private AdMobAdsRewardedAdListener backfillRewardedListener = new AdMobAdsRewardedAdListener(REWARDED, this, true);
    private boolean isInterstitialAvailable = false;
    private boolean isRewardedAvailable = false;
    private boolean isNetworkActive = false;
    private boolean isBannerRequested = false;
    private boolean isInterstitialRequested = false;
    private boolean isRewardedRequested = false;
    // private View adView;
    // private SearchAdView sadView;
    private ViewGroup parentView;
    /**
     * The adView to display to the user.
     */
    private AdView adView;
    /**
     * if want banner view overlap webview, we will need this layout
     */
    private RelativeLayout adViewLayout = null;
    /**
     * The interstitial ad to display to the user.
     */
    private InterstitialAd interstitialAd;
    private String publisherId = DEFAULT_AD_PUBLISHER_ID;
    private String interstitialAdId = DEFAULT_INTERSTITIAL_PUBLISHER_ID;
    private String rewardedAdId = DEFAULT_REWARDED_PUBLISHER_ID;
    private String tappxId = DEFAULT_TAPPX_ID;
    private AdSize adSize = AdSize.SMART_BANNER;
    /**
     * The rewarded ad to display to the user.
     */
    private RewardedVideoAd rewardedAd;
    /**
     * Whether or not the ad should be positioned at top or bottom of screen.
     */
    private boolean isBannerAtTop = false;
    /**
     * Whether or not the banner will overlap the webview instead of push it up or
     * down
     */
    private boolean isBannerOverlap = false;
    private boolean isOffsetStatusBar = false;
    private boolean isTesting = false;
    private JSONObject adExtras = null;
    private boolean isBannerVisible = false;
    private double tappxShare = 0.5;
    private boolean isGo2TappxInBannerBackfill = false;
    private boolean isGo2TappxInIntesrtitialBackfill = false;
    private boolean hasTappx = false;

    @Override
    public void load() {
        Activity activity = this.bridge.getActivity();
        connectivity = Connectivity.GetInstance(activity, this);
        connectivity.observeInternetConnection();
    }

    @Override
    public void handleOnPause() {
        super.handleOnPause();
        if (adView != null) {
            adView.pause();
        }
        connectivity.stopAllObservers(true);
    }

    @Override
    public void handleOnResume() {
        super.handleOnResume();
        if (adView != null) {
            adView.resume();
        }
        connectivity.observeInternetConnection();
    }

    @Override
    public void handleOnStop() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (adViewLayout != null) {
            ViewGroup parentView = (ViewGroup) adViewLayout.getParent();
            if (parentView != null) {
                parentView.removeView(adViewLayout);
            }
            adViewLayout = null;
        }
        connectivity.stopAllObservers(true);
        super.handleOnStop();
    }

    @Override
    public void notifyListeners(String eventName, JSObject data) {
        super.notifyListeners(eventName, data);
    }

    public void handleOnAdLoaded(String adType) {
        if (INTERSTITIAL.equalsIgnoreCase(adType)) {
            isInterstitialAvailable = true;
            if (isInterstitialAutoShow) {
                try {
                    showInterstitialAd(null);
                } catch (Error err) {
                    Log.w("Show interstitial error", err.getMessage());
                }
            }
        } else if (BANNER.equalsIgnoreCase(adType)) {
            if (isBannerAutoShow) {
                try {
                    showBannerAd(true, null);
                    bannerListener.onAdOpened();
                } catch (Error err) {
                    Log.w("Show banner error", err.getMessage());
                }
            }
        } else if (REWARDED.equalsIgnoreCase(adType)) {
            isRewardedAvailable = true;
            if (isRewardedAutoShow) {
                try {
                    showRewardedAd(null);
                } catch (Error err) {
                    Log.w("Show rewarded error", err.getMessage());
                }
            }
        }
    }

    public void handleOnAdOpened(String adType) {
        if (INTERSTITIAL.equalsIgnoreCase(adType)) {
            isInterstitialAvailable = false;
        } else if (REWARDED.equalsIgnoreCase(adType)) {
            isRewardedAvailable = false;
        }
    }

    @PluginMethod()
    public void setOptions(PluginCall call) {
        if (call.hasOption(OPT_PUBLISHER_ID)) {
            this.publisherId = call.getString(OPT_PUBLISHER_ID);
        }
        if (call.hasOption(OPT_BANNER_AD_ID)) {
            this.publisherId = call.getString(OPT_BANNER_AD_ID);
        }
        if (call.hasOption(OPT_INTERSTITIAL_AD_ID)) {
            this.interstitialAdId = call.getString(OPT_INTERSTITIAL_AD_ID);
        }
        if (call.hasOption(OPT_REWARDED_AD_ID)) {
            this.rewardedAdId = call.getString(OPT_REWARDED_AD_ID);
        }
        if (call.hasOption(OPT_AD_SIZE)) {
            this.adSize = adSizeFromString(call.getString(OPT_AD_SIZE));
        }
        if (call.hasOption(OPT_BANNER_AT_TOP)) {
            this.isBannerAtTop = call.getBoolean(OPT_BANNER_AT_TOP);
        }
        if (call.hasOption(OPT_OVERLAP)) {
            this.isBannerOverlap = call.getBoolean(OPT_OVERLAP);
        }
        if (call.hasOption(OPT_OFFSET_STATUSBAR)) {
            this.isOffsetStatusBar = call.getBoolean(OPT_OFFSET_STATUSBAR);
        }
        if (call.hasOption(OPT_IS_TESTING)) {
            this.isTesting = call.getBoolean(OPT_IS_TESTING);
        }
        if (call.hasOption(OPT_AD_EXTRAS)) {
            this.adExtras = call.getObject(OPT_AD_EXTRAS);
        }
        if (call.hasOption(OPT_AUTO_SHOW_BANNER)) {
            this.isBannerAutoShow = call.getBoolean(OPT_AUTO_SHOW_BANNER);
        }
        if (call.hasOption(OPT_AUTO_SHOW_INTERSTITIAL)) {
            this.isInterstitialAutoShow = call.getBoolean(OPT_AUTO_SHOW_INTERSTITIAL);
        }
        if (call.hasOption(OPT_AUTO_SHOW_REWARDED)) {
            this.isRewardedAutoShow = call.getBoolean(OPT_AUTO_SHOW_REWARDED);
        }
        if (call.hasOption(OPT_TAPPX_ID_ANDROID)) {
            this.tappxId = call.getString(OPT_TAPPX_ID_ANDROID);
            hasTappx = true;
        }
        if (call.hasOption(OPT_TAPPX_SHARE)) {
            this.tappxShare = call.getDouble(OPT_TAPPX_SHARE);
            hasTappx = true;
        }
        call.success();
    }

    @SuppressLint("DefaultLocale")
    private AdRequest buildAdRequest() {
        AdRequest.Builder request_builder = new AdRequest.Builder();
        if (isTesting) {
            Activity activity = this.bridge.getActivity();
            // This will request test ads on the emulator and deviceby passing this hashed
            // device ID.
            String ANDROID_ID = Settings.Secure.getString(activity.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            String deviceId = md5(ANDROID_ID).toUpperCase();
            request_builder = request_builder.addTestDevice(deviceId).addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        Bundle bundle = new Bundle();
        if (adExtras != null) {
            Iterator<String> it = adExtras.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    bundle.putString(key, adExtras.get(key).toString());
                } catch (JSONException exception) {
                    Log.w(ADMOBADS_LOGTAG, String.format("Caught JSON Exception: %s", exception.getMessage()));
                }
            }
        }
        AdMobExtras adextras = new AdMobExtras(bundle);
        request_builder = request_builder.addNetworkExtras(adextras);
        AdRequest request = request_builder.build();
        return request;
    }

    private String getPublisherId(boolean isBackFill) {
        return getPublisherId(isBackFill, hasTappx);
    }

    private String getPublisherId(boolean isBackFill, boolean hasTappx) {
        String _publisherId = publisherId;

        if (!isBackFill && hasTappx && (new Random()).nextInt(100) <= (int) (tappxShare * 100)) {
            if (tappxId != null && tappxId.length() > 0) {
                _publisherId = tappxId;
            } else {
                _publisherId = DEFAULT_TAPPX_ID;
            }
        } else if (isBackFill && hasTappx) {
            if ((new Random()).nextInt(100) > 2) {
                if (tappxId != null && tappxId.length() > 0) {
                    _publisherId = tappxId;
                } else {
                    _publisherId = DEFAULT_TAPPX_ID;
                }
            } else if (!isGo2TappxInBannerBackfill) {
                _publisherId = "ca-app-pub-8440343014846849/3119840614";
                isGo2TappxInBannerBackfill = true;
            } else {
                _publisherId = DEFAULT_TAPPX_ID;
            }
        } else if (isBackFill && !isGo2TappxInBannerBackfill) {
            _publisherId = "ca-app-pub-8440343014846849/3119840614";
            isGo2TappxInBannerBackfill = true;
        } else if (isBackFill) {
            _publisherId = DEFAULT_TAPPX_ID;
        }

        return _publisherId;
    }

    private void createBannerView(String _pid, AdMobAdsAdListener adListener, boolean isBackFill) {
        boolean isTappx = _pid.equals(tappxId);

        if (adView != null && !adView.getAdUnitId().equals(_pid)) {
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            adView.destroy();
            adView = null;
        }
        if (adView == null) {
            Activity activity = this.bridge.getActivity();
            adView = new AdView(activity);
            if (isTappx) {
                if (adSize == AdSize.BANNER) { // 320x50
                    adView.setAdSize(adSize);
                } else if (adSize == AdSize.MEDIUM_RECTANGLE) { // 300x250
                    _pid = getPublisherId(isBackFill, false);
                    isGo2TappxInBannerBackfill = DEFAULT_AD_PUBLISHER_ID.equals(_pid);
                    adView.setAdSize(adSize);
                } else if (adSize == AdSize.FULL_BANNER) { // 468x60
                    adView.setAdSize(AdSize.BANNER);
                } else if (adSize == AdSize.LEADERBOARD) { // 728x90
                    adView.setAdSize(AdSize.BANNER);
                } else if (adSize == AdSize.SMART_BANNER) { // Screen width x 32|50|90
                    Activity adMobAdsActivity = (Activity) AdMobAds.this.getContext();
                    DisplayMetrics metrics = DisplayInfo(adMobAdsActivity);
                    if (metrics.widthPixels >= 768) {
                        adView.setAdSize(new AdSize(768, 90));
                    } else {
                        adView.setAdSize(AdSize.BANNER);
                    }
                }

            } else {
                adView.setAdSize(adSize);
            }
            adView.setAdUnitId(_pid);
            adView.setAdListener(adListener);
            adView.setVisibility(View.GONE);
        }

        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }
        isBannerVisible = false;
        adView.loadAd(buildAdRequest());
    }

    @PluginMethod()
    public void createBannerView(final PluginCall call) {
        this.setOptions(call);
        String __pid = publisherId;
        Activity activity = this.bridge.getActivity();
        try {
            __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                    : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                            : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                    activity.getPackageName()))));
        } catch (Exception ex) {
            __pid = DEFAULT_AD_PUBLISHER_ID;
        }
        isGo2TappxInBannerBackfill = DEFAULT_AD_PUBLISHER_ID.equals(__pid);
        final String _pid = __pid;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isBannerRequested = true;
                createBannerView(_pid, bannerListener, false);
                call.success();
            }
        });
    }

    @PluginMethod()
    public void showBannerAd(final PluginCall call) {
        if (call.hasOption("show")) {
            Boolean show = call.getBoolean("show");
            try {
                this.showBannerAd(show, call);
            } catch (Error err) {
                Log.w("Show banner error", err.getMessage());
            }
        } else {
            try {
                this.showBannerAd(true, call);
            } catch (Error err) {
                Log.w("Show banner error", err.getMessage());
            }
        }
    }

    /**
     * Parses the show ad input parameters and runs the show ad action on the UI
     * thread.
     *
     * @param show            indicates if the banner should be shown or not.
     * @param callbackContext Callback to be called when thread finishes.
     * @return A PluginResult representing whether or not an ad was requested
     *         succcessfully. Listen for onReceiveAd() and onFailedToReceiveAd()
     *         callbacks to see if an ad was successfully retrieved.
     */
    private void showBannerAd(final Boolean show, final PluginCall call) {
        if (adView == null) {
            String errorMessage = "adView is null, call createBannerView first.";
            if (call != null) {
                call.error(errorMessage);
            }
            throw new Error(errorMessage);
        }

        final Activity activity = this.bridge.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show == isBannerVisible) {
                    // no change
                } else if (show) {
                    if (adView != null && adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }

                    if (isBannerOverlap) {
                        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                        if (isOffsetStatusBar) {
                            int titleBarHeight = 0;
                            Rect rectangle = new Rect();
                            Activity adMobAdsActivity = (Activity) AdMobAds.this.getContext();
                            Window window = adMobAdsActivity.getWindow();
                            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

                            if (isBannerAtTop) {
                                if (rectangle.top == 0) {
                                    int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                                    titleBarHeight = contentViewTop - rectangle.top;
                                }
                                params2.topMargin = titleBarHeight;

                            } else {
                                if (rectangle.top > 0) {
                                    int contentViewBottom = window.findViewById(Window.ID_ANDROID_CONTENT).getBottom();
                                    titleBarHeight = contentViewBottom - rectangle.bottom;
                                }
                                params2.bottomMargin = titleBarHeight;
                            }

                        } else if (isBannerAtTop) {
                            params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                        } else {
                            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        }

                        if (adViewLayout == null) {
                            adViewLayout = new RelativeLayout(activity);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                            // TODO: test if it works like cordova 4
                            // if (CORDOVA_4) {
                            ((ViewGroup) bridge.getWebView().getParent()).addView(adViewLayout, params);
                            // } else {
                            // ((ViewGroup) bridge.getWebView()).addView(adViewLayout, params);
                            // }
                        }
                        adViewLayout.addView(adView, params2);
                        adViewLayout.bringToFront();

                    } else {
                        ViewGroup wvParentView = (ViewGroup) ((ViewGroup) bridge.getWebView()).getParent();

                        if (parentView == null) {
                            parentView = new LinearLayout(bridge.getWebView().getContext());
                        }

                        if (wvParentView != null && wvParentView != parentView) {
                            wvParentView.removeView(bridge.getWebView());
                            ((LinearLayout) parentView).setOrientation(LinearLayout.VERTICAL);
                            parentView.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
                            bridge.getWebView().setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
                            parentView.addView(bridge.getWebView());
                            activity.setContentView(parentView);
                        }
                        if (isBannerAtTop) {
                            parentView.addView(adView, 0);
                        } else {
                            parentView.addView(adView);
                        }
                        parentView.bringToFront();
                        parentView.requestLayout();

                    }

                    adView.setVisibility(View.VISIBLE);
                    isBannerVisible = true;

                } else {
                    adView.setVisibility(View.GONE);
                    isBannerVisible = false;
                }

                if (call != null) {
                    call.success();
                }
            }
        });
    }

    @PluginMethod()
    public void destroyBannerView(final PluginCall call) {
        Log.w(ADMOBADS_LOGTAG, "destroyBannerView");
        Activity activity = this.bridge.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView != null) {
                    ViewGroup parentView = (ViewGroup) adView.getParent();
                    if (parentView != null) {
                        parentView.removeView(adView);
                    }
                    adView.destroy();
                    adView = null;
                }
                isBannerVisible = false;
                isBannerRequested = false;
                call.success();
            }
        });
    }

    private String getInterstitialId(boolean isBackFill) {
        String _interstitialAdId = interstitialAdId;

        if (!isBackFill && hasTappx && (new Random()).nextInt(100) <= (int) (tappxShare * 100)) {
            if (tappxId != null && tappxId.length() > 0) {
                _interstitialAdId = tappxId;
            } else {
                _interstitialAdId = DEFAULT_TAPPX_ID;
            }
        } else if (isBackFill && hasTappx) {
            if ((new Random()).nextInt(100) > 2) {
                if (tappxId != null && tappxId.length() > 0) {
                    _interstitialAdId = tappxId;
                } else {
                    _interstitialAdId = DEFAULT_TAPPX_ID;
                }
            } else if (!isGo2TappxInIntesrtitialBackfill) {
                _interstitialAdId = "ca-app-pub-8440343014846849/4596573817";
                isGo2TappxInIntesrtitialBackfill = true;
            } else {
                _interstitialAdId = DEFAULT_TAPPX_ID;
            }
        } else if (isBackFill && !isGo2TappxInIntesrtitialBackfill) {
            _interstitialAdId = "ca-app-pub-8440343014846849/4596573817";
            isGo2TappxInIntesrtitialBackfill = true;
        } else if (isBackFill) {
            _interstitialAdId = DEFAULT_TAPPX_ID;
        }

        return _interstitialAdId;
    }

    private void createInterstitialView(String _iid, AdMobAdsAdListener adListener) {
        Activity activity = this.bridge.getActivity();
        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(_iid);
        interstitialAd.setAdListener(adListener);
        interstitialAd.loadAd(buildAdRequest());
    }

    @PluginMethod()
    public void createInterstitialView(final PluginCall call) {
        this.setOptions(call);
        String __pid = publisherId;
        String __iid = interstitialAdId;
        Activity activity = this.bridge.getActivity();
        try {
            __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                    : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                            : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                    activity.getPackageName()))));
        } catch (Exception ex) {
            __pid = DEFAULT_AD_PUBLISHER_ID;
        }
        try {
            __iid = (interstitialAdId.length() == 0 ? __pid
                    : (new Random()).nextInt(100) > 2 ? getInterstitialId(false)
                            : activity.getString(
                                    activity.getResources().getIdentifier("iid", "string", activity.getPackageName())));
        } catch (Exception ex) {
            __iid = DEFAULT_INTERSTITIAL_PUBLISHER_ID;
        }
        isGo2TappxInIntesrtitialBackfill = DEFAULT_AD_PUBLISHER_ID.equals(__iid)
                || DEFAULT_INTERSTITIAL_PUBLISHER_ID.equals(__iid);
        final String _iid = __iid;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isInterstitialRequested = true;
                createInterstitialView(_iid, interstitialListener);
                call.success();
            }
        });
    }

    @PluginMethod()
    public void requestInterstitialAd(final PluginCall call) {
        if (isInterstitialAvailable) {
            interstitialListener.onAdLoaded();
            call.success();

        } else {
            this.setOptions(call);
            if (interstitialAd == null) {
                createInterstitialView(call);

            } else {
                Activity activity = this.bridge.getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        interstitialAd.loadAd(buildAdRequest());
                        call.success();
                    }
                });
            }
        }
    }

    @PluginMethod()
    public void showInterstitialAd(final PluginCall call) {
        if (interstitialAd == null) {
            String errorMessage = "interstitialAd is null, call createInterstitialView first.";
            call.error(errorMessage);
            throw new Error(errorMessage);
        }
        Activity activity = this.bridge.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd.isLoaded()) {
                    isInterstitialRequested = false;
                    interstitialAd.show();
                }
                if (call != null) {
                    call.success();
                }
            }
        });
    }

    private String getRewardedId(boolean isBackfill) {
        String _rewardedAdId = rewardedAdId;
        if (isBackfill) {
            _rewardedAdId = "ca-app-pub-8440343014846849/4854611361";
        }
        return _rewardedAdId;
    }

    private void createRewardedView(String _rid, AdMobAdsRewardedAdListener rewardedListener) {
        Activity activity = this.bridge.getActivity();
        rewardedAd = MobileAds.getRewardedVideoAdInstance(activity);
        rewardedAd.setRewardedVideoAdListener(rewardedListener);
        rewardedAd.loadAd(_rid, buildAdRequest());
    }

    @PluginMethod()
    public void createRewardedView(final PluginCall call) {
        this.setOptions(call);
        String __pid = publisherId;
        String __rid = rewardedAdId;
        Activity activity = this.bridge.getActivity();
        try {
            __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                    : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                            : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                    activity.getPackageName()))));
        } catch (Exception ex) {
            __pid = DEFAULT_AD_PUBLISHER_ID;
        }
        try {
            __rid = (rewardedAdId.length() == 0 ? __pid
                    : (new Random()).nextInt(100) > 2 ? getRewardedId(false)
                            : activity.getString(
                                    activity.getResources().getIdentifier("rid", "string", activity.getPackageName())));
        } catch (Exception ex) {
            __rid = DEFAULT_REWARDED_PUBLISHER_ID;
        }
        final String _rid = __rid;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRewardedRequested = true;
                createRewardedView(_rid, rewardedListener);
                call.success();
            }
        });
    }

    @PluginMethod()
    public void requestRewardedAd(final PluginCall call) {
        this.setOptions(call);
        if (rewardedAd == null) {
            createRewardedView(call);
        } else {
            Activity activity = this.bridge.getActivity();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rewardedAd.loadAd(rewardedAdId, buildAdRequest());
                    call.success();
                }
            });
        }
    }

    @PluginMethod()
    public void showRewardedAd(final PluginCall call) {
        if (rewardedAd == null) {
            String errorMessage = "rewardedAd is null, call requestRewardedAd first.";
            call.error(errorMessage);
            throw new Error(errorMessage);
        }
        Activity activity = this.bridge.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rewardedAd.isLoaded()) {
                    isRewardedRequested = false;
                    rewardedAd.show();
                }
                if (call != null) {
                    call.success();
                }
            }
        });
    }

    public void tryBackfill(String adType) {
        Activity activity = this.bridge.getActivity();
        if (BANNER.equals(adType)) {
            String __pid = publisherId;
            try {
                __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                        : ((new Random()).nextInt(100) > 2 ? getPublisherId(true)
                                : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                        activity.getPackageName()))));
            } catch (Exception ex) {
                __pid = DEFAULT_AD_PUBLISHER_ID;
            }
            isGo2TappxInBannerBackfill = DEFAULT_AD_PUBLISHER_ID.equals(__pid);
            final String _pid = __pid;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isGo2TappxInBannerBackfill) {
                        createBannerView(_pid, backFillBannerListener, true);
                    } else {
                        createBannerView(_pid, bannerListener, true);
                    }
                }
            });
        } else if (INTERSTITIAL.equals(adType)) {
            String __pid = publisherId;
            String __iid = interstitialAdId;
            try {
                __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                        : ((new Random()).nextInt(100) > 2 ? getPublisherId(true)
                                : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                        activity.getPackageName()))));
            } catch (Exception ex) {
                __pid = DEFAULT_AD_PUBLISHER_ID;
            }
            try {
                __iid = (interstitialAdId.length() == 0 ? __pid
                        : (new Random()).nextInt(100) > 2 ? getInterstitialId(true)
                                : activity.getString(activity.getResources().getIdentifier("iid", "string",
                                        activity.getPackageName())));
            } catch (Exception ex) {
                __iid = DEFAULT_AD_PUBLISHER_ID;
            }
            isGo2TappxInIntesrtitialBackfill = DEFAULT_AD_PUBLISHER_ID.equals(__iid)
                    || DEFAULT_INTERSTITIAL_PUBLISHER_ID.equals(__iid);
            final String _iid = __iid;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isGo2TappxInIntesrtitialBackfill) {
                        createInterstitialView(_iid, backFillInterstitialListener);
                    } else {
                        createInterstitialView(_iid, interstitialListener);
                    }
                }
            });
        } else if (REWARDED.equals(adType)) {
            String __pid = publisherId;
            String __rid = rewardedAdId;
            try {
                __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                        : ((new Random()).nextInt(100) > 2 ? getPublisherId(true)
                                : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                        activity.getPackageName()))));
            } catch (Exception ex) {
                __pid = DEFAULT_AD_PUBLISHER_ID;
            }
            try {
                __rid = (rewardedAdId.length() == 0 ? __pid
                        : (new Random()).nextInt(100) > 2 ? getRewardedId(true)
                                : activity.getString(activity.getResources().getIdentifier("rid", "string",
                                        activity.getPackageName())));
            } catch (Exception ex) {
                __rid = DEFAULT_REWARDED_PUBLISHER_ID;
            }
            final String _rid = __rid;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createRewardedView(_rid, backfillRewardedListener);
                }
            });
        }
    }

    @Override
    public void onConnectivityChanged(String interfaceType, boolean isConnected, String observer) {
        if (!isConnected) {
            isNetworkActive = false;
        } else if (!isNetworkActive) {
            isNetworkActive = true;
            Activity activity = this.bridge.getActivity();

            if (isBannerRequested) {
                String __pid = publisherId;
                try {
                    __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                            : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                                    : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                            activity.getPackageName()))));
                } catch (Exception ex) {
                    __pid = DEFAULT_AD_PUBLISHER_ID;
                }
                final String _pid = __pid;
                createBannerView(_pid, bannerListener, false);
            }

            if (isInterstitialRequested) {
                if (isInterstitialAvailable) {
                    interstitialListener.onAdLoaded();

                } else {
                    if (interstitialAd == null) {
                        String __pid = publisherId;
                        String __iid = interstitialAdId;
                        try {
                            __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                                    : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                                            : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                                    activity.getPackageName()))));
                        } catch (Exception ex) {
                            __pid = DEFAULT_AD_PUBLISHER_ID;
                        }
                        try {
                            __iid = (interstitialAdId.length() == 0 ? __pid
                                    : (new Random()).nextInt(100) > 2 ? getInterstitialId(false)
                                            : activity.getString(activity.getResources().getIdentifier("iid", "string",
                                                    activity.getPackageName())));
                        } catch (Exception ex) {
                            __iid = DEFAULT_AD_PUBLISHER_ID;
                        }
                        final String _iid = __iid;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isInterstitialRequested = true;
                                createInterstitialView(_iid, interstitialListener);
                            }
                        });

                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                interstitialAd.loadAd(buildAdRequest());
                            }
                        });
                    }
                }
            }

            if (isRewardedRequested) {
                if (isRewardedAvailable) {
                    rewardedListener.onRewardedVideoAdLoaded();
                } else {
                    String __pid = publisherId;
                    String __rid = rewardedAdId;
                    try {
                        __pid = (publisherId.length() == 0 ? DEFAULT_AD_PUBLISHER_ID
                                : ((new Random()).nextInt(100) > 2 ? getPublisherId(false)
                                        : activity.getString(activity.getResources().getIdentifier("bid", "string",
                                                activity.getPackageName()))));
                    } catch (Exception ex) {
                        __pid = DEFAULT_AD_PUBLISHER_ID;
                    }
                    try {
                        __rid = (rewardedAdId.length() == 0 ? __pid
                                : (new Random()).nextInt(100) > 2 ? getRewardedId(false)
                                        : activity.getString(activity.getResources().getIdentifier("rid", "string",
                                                activity.getPackageName())));
                    } catch (Exception ex) {
                        __rid = DEFAULT_AD_PUBLISHER_ID;
                    }
                    final String _rid = __rid;
                    if (rewardedAd == null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isRewardedRequested = true;
                                createRewardedView(_rid, rewardedListener);
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rewardedAd.loadAd(_rid, buildAdRequest());
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Gets an AdSize object from the string size passed in from JavaScript. Returns
     * null if an improper string is provided.
     *
     * @param size The string size representing an ad format constant.
     * @return An AdSize object used to create a banner.
     */
    public static AdSize adSizeFromString(String size) {
        if ("BANNER".equals(size)) {
            return AdSize.BANNER;
        } else if ("IAB_MRECT".equals(size)) {
            return AdSize.MEDIUM_RECTANGLE;
        } else if ("IAB_BANNER".equals(size)) {
            return AdSize.FULL_BANNER;
        } else if ("IAB_LEADERBOARD".equals(size)) {
            return AdSize.LEADERBOARD;
        } else if ("SMART_BANNER".equals(size)) {
            return AdSize.SMART_BANNER;
        } else {
            return AdSize.SMART_BANNER;
        }
    }

    public static final String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte i : messageDigest) {
                String h = Integer.toHexString(0xFF & i);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    public static DisplayMetrics DisplayInfo(Context p_context) {
        DisplayMetrics metrics = null;
        try {
            metrics = new DisplayMetrics();
            ((android.view.WindowManager) p_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                    .getMetrics(metrics);
            // p_activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        } catch (Exception e) {
        }
        return metrics;
    }

    public static double DeviceInches(Context p_context) {
        double default_value = 4.0f;
        if (p_context == null)
            return default_value;
        try {
            DisplayMetrics metrics = DisplayInfo(p_context);
            return Math.sqrt(Math.pow(metrics.widthPixels / metrics.xdpi, 2.0)
                    + Math.pow(metrics.heightPixels / metrics.ydpi, 2.0));
        } catch (Exception e) {
            return default_value;
        }
    }
}