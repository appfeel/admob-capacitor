declare module "@capacitor/core" {
  interface PluginRegistry {
    AdMobAds: AdMobAdsPlugin;
  }
}

export interface IAdMobAdsOptions {
  bannerAdId: string;
  publisherId?: string;
  interstitialAdId?: string;
  rewardedAdId?: string;
  adSize?: string;
  isBannerAtTop?: boolean;
  isBannerOverlap?: boolean;
  isOffsetStatusBar?: boolean;
  isTesting?: boolean;
  adExtras?: any;
  isBannerAutoShow?: boolean;
  isInterstitialAutoShow?: boolean;
  isRewardedAutoShow?: boolean;
  tappxId?: string;
  tappxShare?: number;
  secondsToShowCloseButton: number;
  secondsToCloseInterstitial: number;
  adSlot: string;
  interstitialShowCloseButton: boolean;
  overlap: boolean;
  show: boolean;
}

export interface AdMobAdsPlugin {
  setOptions(options: any): Promise<void>;
  createBannerView(options?: any): Promise<void>;
  showBannerAd(options?: any): Promise<void>;
  destroyBannerView(options?: any): Promise<void>;
  createInterstitialView?(options?: any): Promise<void>;
  requestInterstitialAd(options?: any): Promise<void>;
  showInterstitialAd(options?: any): Promise<void>;
  createRewardedView?(options?: any): Promise<void>;
  requestRewardedAd(options?: any): Promise<void>;
  showRewardedAd(options?: any): Promise<void>;
}
