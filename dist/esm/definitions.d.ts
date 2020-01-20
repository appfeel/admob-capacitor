declare module "@capacitor/core" {
    interface PluginRegistry {
        AdMobAds: AdMobAdsPlugin;
    }
}
export interface AdMobAdsPlugin {
    setOptions(options: any): Promise<void>;
    createBanner(options: any): Promise<void>;
    showBanner(options: {
        show: boolean;
    }): Promise<void>;
}
