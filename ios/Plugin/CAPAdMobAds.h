/*
 CAPAdMobAds.h
 Copyright 2015 AppFeel. All rights reserved.
 http://www.appfeel.com
 
 AdMobAds Capacitor Plugin (cordova-admob)
 
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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <GoogleMobileAds/GADAdSize.h>
#import <GoogleMobileAds/GADBannerView.h>
#import <GoogleMobileAds/GADInterstitial.h>
#import <GoogleMobileAds/GADRewardBasedVideoAd.h>
#import <GoogleMobileAds/GADBannerViewDelegate.h>
#import <GoogleMobileAds/GADInterstitialDelegate.h>
#import <GoogleMobileAds/GADRewardBasedVideoAdDelegate.h>
#import "CAPAdMobAdsAdListener.h"
#import "CAPAdMobAdsRewardedAdListener.h"
#import "AppFeelReachability.h"

#pragma mark - JS requestAd options

@class GADBannerView;
@class GADInterstitial;
@class GADRewardBasedVideoAd;
@class CAPAdMobAdsAdListener;
@class CAPAdMobAdsRewardedAdListener;

#pragma mark AdMobAds Plugin

@interface CAPAdMobAds : CAPPlugin {
}

@property (assign) BOOL isInterstitialAvailable;
@property (assign) BOOL isRewardedAvailable;

@property (nonatomic, retain) GADBannerView *bannerView;
@property (nonatomic, retain) GADInterstitial *interstitialView;
@property (nonatomic, retain) CAPAdMobAdsAdListener *adsListener;
@property (nonatomic, retain) CAPAdMobAdsAdListener *backFillAdsListener;
@property (nonatomic, retain) CAPAdMobAdsRewardedAdListener *rewardedAdsListener;
@property (nonatomic, retain) CAPAdMobAdsRewardedAdListener *backfillRewardedAdsListener;

@property (nonatomic, retain) NSString* publisherId;
@property (nonatomic, retain) NSString* bannerAdId;
@property (nonatomic, retain) NSString* interstitialAdId;
@property (nonatomic, retain) NSString* rewardedAdId;
@property (nonatomic, retain) NSString* tappxId;

@property (assign) GADAdSize adSize;
@property (assign) BOOL isBannerAtTop;
@property (assign) BOOL isBannerOverlap;
@property (assign) BOOL isOffsetStatusBar;

@property (assign) BOOL isTesting;
@property (nonatomic, retain) NSDictionary* adExtras;

@property (assign) BOOL isBannerVisible;
@property (assign) BOOL isBannerInitialized;
@property (assign) BOOL isBannerShow;
@property (assign) BOOL isBannerAutoShow;
@property (assign) BOOL isInterstitialAutoShow;
@property (assign) BOOL isRewardedAutoShow;
@property (assign) BOOL isGo2TappxInInterstitialBackfill;
@property (assign) BOOL isGo2TappxInBannerBackfill;
@property (assign) BOOL hasTappx;
@property (assign) double tappxShare;

- (void)setOptions:(CAPPluginCall *)command;

- (void)createBannerView:(CAPPluginCall *)command;
- (void)showBannerAd:(CAPPluginCall *)command;
- (void)destroyBannerView:(CAPPluginCall *)command;

- (void)requestInterstitialAd:(CAPPluginCall *)command;
- (void)showInterstitialAd:(CAPPluginCall *)command;

- (void)requestRewardedAd:(CAPPluginCall *)command;
- (void)showRewardedAd:(CAPPluginCall *)command;

- (void)onBannerAd:(GADBannerView *)adView adListener:(CAPAdMobAdsAdListener *)adListener ;
- (void)onInterstitialAd:(GADInterstitial *)interstitial adListener:(CAPAdMobAdsAdListener *)adListener;
- (void)onRewardedAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd adListener:(CAPAdMobAdsRewardedAdListener *)adListener;

- (void)tryToBackfillBannerAd;
- (void)tryToBackfillInterstitialAd;
- (void)tryToBackfillRewardedAd;

@end
