import { WebPlugin } from '@capacitor/core';
export class AdMobAdsWeb extends WebPlugin {
    setOptions(options) {
        console.log('parameter received:', options);
        throw new Error('Method not implemented.');
    }
    createBanner(options) {
        console.log('parameter received:', options);
        throw new Error('Method not implemented.');
    }
    showBanner(options) {
        console.log('parameter received:', options);
        throw new Error('Method not implemented.');
    }
    constructor() {
        super({
            name: 'AdMobAds',
            platforms: ['web']
        });
    }
}
const AdMobAds = new AdMobAdsWeb();
export { AdMobAds };
import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AdMobAds);
//# sourceMappingURL=web.js.map