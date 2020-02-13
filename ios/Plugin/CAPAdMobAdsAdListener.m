/*
 CAPAdMobAdsAdListener.m
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

#import <Foundation/Foundation.h>
#include "CAPAdMobAds.h"
#include "CAPAdMobAdsAdListener.h"

@interface CAPAdMobAdsAdListener()
- (NSString *) __getErrorReason:(NSInteger) errorCode;
@end


@implementation CAPAdMobAdsAdListener

@synthesize adMobAds;
@synthesize isBackFill;

- (instancetype)initWithAdMobAds: (CAPAdMobAds *)originalAdMobAds andIsBackFill: (BOOL)andIsBackFill {
    self = [super init];
    if (self) {
        adMobAds = originalAdMobAds;
        isBackFill = andIsBackFill;
    }
    return self;
}

#pragma mark -
#pragma mark GADBannerViewDelegate implementation

// onAdLoaded
- (void)adViewDidReceiveAd:(GADBannerView *)adView {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner"];   
        [adMobAds notifyListeners:@"onAdLoaded", data];
    }];
    [adMobAds onBannerAd:adView adListener:self];
}

// onAdFailedToLoad
- (void)adView:(GADBannerView *)view didFailToReceiveAdWithError:(GADRequestError *)error {
    NSLog(@"%s: Failed to receive ad with error: %@",
          __PRETTY_FUNCTION__, [error localizedFailureReason]);
    
    if (isBackFill) {
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            // Since we're passing error back through Cordova, we need to set this up.
            NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner", @"error", (long)error.code, @"reason", [self __getErrorReason:error.code]];
            [adMobAds notifyListeners:@"onAdFailedToLoad", data];
        }];
    } else {
        [adMobAds tryToBackfillBannerAd];
    }
}

- (void)adViewDidFailedToShow:(GADBannerView *)view {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner", @"error", 0, @"reason", @"Advertising tracking may be disabled. To get test ads on this device, enable advertising tracking."];
        [adMobAds notifyListeners:@"onAdFailedToLoad", data];
    }];   
}

// onAdOpened
- (void)adViewWillPresentScreen:(GADBannerView *)adView {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner"];
        [adMobAds notifyListeners:@"onAdOpened", data];
    }];
}

// onAdLeftApplication
- (void)adViewWillLeaveApplication:(GADBannerView *)adView {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner"];
        [adMobAds notifyListeners:@"onAdLeftApplication", data];
    }];
}

// onAdClosed
- (void)adViewDidDismissScreen:(GADBannerView *)adView {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"banner"];
        [adMobAds notifyListeners:@"onAdClosed", data];
    }];
}

#pragma mark -
#pragma mark GADInterstitialDelegate implementation

// Sent when an interstitial ad request succeeded.  Show it at the next
// transition point in your application such as when transitioning between view
// controllers.
// onAdLoaded
- (void)interstitialDidReceiveAd:(GADInterstitial *)interstitial {
    if (adMobAds.interstitialView) {
        [adMobAds onInterstitialAd:interstitial adListener:self];
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"interstitial"];
            [adMobAds notifyListeners:@"onAdLoaded", data];
        }];
    }
}

- (void)interstitialDidFailedToShow:(GADInterstitial *) interstitial {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"interstitial", @"error", 0, @"reason", @"Advertising tracking may be disabled. To get test ads on this device, enable advertising tracking."];
        [adMobAds notifyListeners:@"onAdFailedToLoad", data];
    }];
    
}

// Sent when an interstitial ad request completed without an interstitial to
// show.  This is common since interstitials are shown sparingly to users.
// onAdFailedToLoad
- (void)interstitial:(GADInterstitial *)ad didFailToReceiveAdWithError:(GADRequestError *)error {
    NSLog(@"%s: Failed to receive ad with error: %@",
          __PRETTY_FUNCTION__, [error localizedFailureReason]);

    if (isBackFill) {
        adMobAds.isInterstitialAvailable = false;
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"interstitial", @"error", (long)error.code, @"reason", [self __getErrorReason:error.code]];
            [adMobAds notifyListeners:@"onAdFailedToLoad", data];
        }];
        
    } else {
        [adMobAds tryToBackfillInterstitialAd];
    }
}

// Sent just before presenting an interstitial.  After this method finishes the
// interstitial will animate onto the screen.  Use this opportunity to stop
// animations and save the state of your application in case the user leaves
// while the interstitial is on screen (e.g. to visit the App Store from a link
// on the interstitial).
// onAdOpened
- (void)interstitialWillPresentScreen:(GADInterstitial *)interstitial {
    if (adMobAds.isInterstitialAvailable) {
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"interstitial"];
            [adMobAds notifyListeners:@"onAdOpened", data];
        }];
        adMobAds.isInterstitialAvailable = false;
    }
}

// Sent just after dismissing an interstitial and it has animated off the
// screen.
// onAdClosed
- (void)interstitialDidDismissScreen:(GADInterstitial *)interstitial {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"interstitial"];
        [adMobAds notifyListeners:@"onAdClosed", data];
    }];
    adMobAds.isInterstitialAvailable = false;
}

- (NSString *) __getErrorReason:(NSInteger) errorCode {
    switch (errorCode) {
        case kGADErrorServerError:
        case kGADErrorOSVersionTooLow:
        case kGADErrorTimeout:
            return @"Internal error";
            break;
            
        case kGADErrorInvalidRequest:
            return @"Invalid request";
            break;
            
        case kGADErrorNetworkError:
            return @"Network Error";
            break;
            
        case kGADErrorNoFill:
            return @"No fill";
            break;
            
        default:
            return @"Unknown";
            break;
    }
}

#pragma mark -
#pragma mark Cleanup

- (void)dealloc {
    adMobAds = nil;
}

@end