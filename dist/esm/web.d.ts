import { WebPlugin } from '@capacitor/core';
import { AdMobAdsPlugin, IAdMobAdsOptions } from './definitions';
export declare class AdMobAdsWeb extends WebPlugin implements AdMobAdsPlugin {
    setOptions(options: IAdMobAdsOptions): Promise<void>;
    createBannerView(options: IAdMobAdsOptions): Promise<void>;
    showBannerAd(options: IAdMobAdsOptions): Promise<void>;
    destroyBannerView(): Promise<void>;
    requestInterstitialAd(options: IAdMobAdsOptions): Promise<void>;
    showInterstitialAd(): Promise<void>;
    requestRewardedAd(): Promise<void>;
    showRewardedAd(): Promise<void>;
    constructor();
}
declare const AdMobAds: AdMobAdsWeb;
export { AdMobAds };
