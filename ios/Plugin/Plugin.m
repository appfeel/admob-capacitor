#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(AdMobAds, "CAPAdMobAds",
    CAP_PLUGIN_METHOD(setOptions, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(createBannerView, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showBannerAd, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(destroyBannerView, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(createInterstitialView, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(requestInterstitialAd, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showInterstitialAd, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(createRewardedView, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(requestRewardedAd, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showRewardedAd, CAPPluginReturnPromise);
)