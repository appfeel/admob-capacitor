import { WebPlugin } from '@capacitor/core';
import { AdMobAdsPlugin } from './definitions';
export declare class AdMobAdsWeb extends WebPlugin implements AdMobAdsPlugin {
    setOptions(options: any): Promise<void>;
    createBanner(options: any): Promise<void>;
    showBanner(options: {
        show: boolean;
    }): Promise<void>;
    constructor();
}
declare const AdMobAds: AdMobAdsWeb;
export { AdMobAds };
