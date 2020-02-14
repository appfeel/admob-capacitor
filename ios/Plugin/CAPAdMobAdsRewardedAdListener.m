
/*
 CAPAdMobAdsRewardedAdListener.m
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
#include "CAPAdMobAds.h"
#include "CAPAdMobAdsRewardedAdListener.h"

@interface CAPAdMobAdsRewardedAdListener()
- (NSString *) __getErrorReason:(NSInteger) errorCode;
@end


@implementation CAPAdMobAdsRewardedAdListener

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

- (void)rewardBasedVideoAdDidFailedToShow:(GADRewardBasedVideoAd *)rewarded {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded", @"error", 0, @"reason", @"Advertising tracking may be disabled. To get test ads on this device, enable advertising tracking."];
        [adMobAds notifyListeners:@"onAdFailedToLoad", data];
    }];
}

- (void)rewardBasedVideoAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd didRewardUserWithReward:(GADAdReward *)reward {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onRewardedAd", data];
    }];
}

- (void)rewardBasedVideoAdDidReceiveAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onAdLoaded", data];
    }];
    [adMobAds onRewardedAd:rewardBasedVideoAd adListener:self];
}

- (void)rewardBasedVideoAdDidOpen:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onAdOpened", data];
    }];
}

- (void)rewardBasedVideoAdDidStartPlaying:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onRewardedAdVideoStarted", data];
    }];
}

- (void)rewardBasedVideoAdDidCompletePlaying:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onRewardedAdVideoCompleted", data];
    }];
}

- (void)rewardBasedVideoAdDidClose:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onAdClosed", data];
    }];
}

- (void)rewardBasedVideoAdWillLeaveApplication:(GADRewardBasedVideoAd *)rewardBasedVideoAd {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded"];
        [adMobAds notifyListeners:@"onAdLeftApplication", data];
    }];
}

- (void)rewardBasedVideoAd:(GADRewardBasedVideoAd *)rewardBasedVideoAd didFailToLoadWithError:(NSError *)error {
    if (isBackFill) {
        adMobAds.isRewardedAvailable = false;
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:@"adType", @"rewarded", @"error", (long)error.code, @"reason", [self __getErrorReason:error.code]];
            [adMobAds notifyListeners:@"onAdFailedToLoad", data];
        }];
    } else {
        [adMobAds tryToBackfillRewardedAd];
    }
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