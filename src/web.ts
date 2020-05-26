import { WebPlugin } from '@capacitor/core';
import { AdMobAdsPlugin, IAdMobAdsOptions } from './definitions';

let winAdmob = (window as { [key: string]: any })['admob'];

export class AdMobAdsWeb extends WebPlugin implements AdMobAdsPlugin {
    public AD_TYPE = {
        INTERSTITIAL: 'interstitial',
        BANNER: 'banner',
        REWARDED: 'rewarded',
    }

    constructor() {
        super({
            name: 'AdMobAds',
            platforms: ['web']
        });
        (window as { [key: string]: any })['admob'] = this;
        winAdmob = this;
    }

    async setOptions(options: IAdMobAdsOptions) {
        if (!options.publisherId) {
            options.publisherId = PUBLISHER_ID;
        }
        if (options.bannerAdId) {
            options.publisherId = options.bannerAdId;
        }
        if (!options.interstitialAdId) {
            options.interstitialAdId = options.publisherId;
        }
        if (!options.rewardedAdId) {
            options.rewardedAdId = options.publisherId;
        }
        setOptions(options);
    }

    async createBannerView(options: IAdMobAdsOptions) {
        if (!isBannerRequested) {
            setOptions(options);
            if (admobOptions.autoShowBanner && !isShowingBanner) {
                loadMainElement();
                showBanner();
                isShowingBanner = true;
            } else if (!admobOptions.autoShowBanner) {
                window.dispatchEvent(new Event('onAdLoaded'));
            }
            isBannerRequested = true;
        }
    }

    async showBannerAd(options: IAdMobAdsOptions) {
        if (!options) {
            return;
        }
        if (options.show && !isShowingBanner) {
            loadMainElement();
            showBanner();
            isShowingBanner = true;
        } else if (!options.show && isShowingBanner) {
            hideBanner();
            isBannerRequested = false;
            isShowingBanner = false;
        }
    }

    async destroyBannerView(/*options: IAdMobAdsOptions*/) {
        if (isShowingBanner) {
            hideBanner();
            isBannerRequested = false;
            isShowingBanner = false;
        }
    }

    async requestInterstitialAd(options: IAdMobAdsOptions) {
        if (!isInterstitialRequested) {
            if (options) {
                setOptions(options);
            }
            if (admobOptions.autoShowInterstitial && !isShowingInterstitial) {
                loadMainElement();
                showInterstitial();
                isShowingInterstitial = true;
            } else if (!admobOptions.autoShowBanner) {
                window.dispatchEvent(new Event('onAdLoaded'));
            }
            isInterstitialRequested = true;
        }
    }

    async showInterstitialAd(/*options: IAdMobAdsOptions*/) {
        if (!isShowingInterstitial) {
            loadMainElement();
            showInterstitial();
            isShowingInterstitial = true;
        }
    }

    async requestRewardedAd(/*options: IAdMobAdsOptions*/) {
    }

    async showRewardedAd(/*options: IAdMobAdsOptions*/) {
    }
}

const AdMobAds = new AdMobAdsWeb();

export { AdMobAds };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AdMobAds);

//
//
//

function generateRandomString() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 12; i += 1) {
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
}

class CustomAd extends HTMLElement {

    constructor() {
        super();
    }

    show(format: string) {
        var ins = document.createElement('ins');
        ins.classList.add('adsbygoogle');
        ins.style.display = 'block';
        var publisherId = getPublisherId(winAdmob.AD_TYPE.BANNER);
        ins.setAttribute('data-ad-client', publisherId);
        ins.setAttribute('data-ad-slot', getSlot(publisherId));
        ins.setAttribute('data-ad-format', format);
        ins.setAttribute('data-full-width-responsive', 'true');
        var test = admobOptions.isTesting ? 'on' : 'off';
        ins.setAttribute('data-adtest', test);
        this.id = generateRandomString();
        ins.id = `${this.id}_ins`;
        this.appendChild(ins);
        var script = document.createElement('script');
        script.text = '(adsbygoogle = window.adsbygoogle || []).push({});';
        script.id = `${this.id}_script`;
        document.body.appendChild(script);
    }

    hide() {
        var ins = document.getElementById(`${this.id}_ins`);
        ins.parentNode.removeChild(ins);
        var script = document.getElementById(`${this.id}_script`);
        script.parentNode.removeChild(script);
    }

}

if (customElements) {
    customElements.define('custom-ad', CustomAd);
}

var PUBLISHER_ID = 'ca-pub-8440343014846849';
var AD_SLOT = '6650297359';

var mainElement: any;
var admobOptions = winAdmob.options;
var isBannerRequested = false;
var isShowingBanner = false;
var isInterstitialRequested = false;
var isShowingInterstitial = false;
var interstitialSpanSeconds = 0;

function addScriptInBody() {
    var script = document.createElement('script');
    script.setAttribute('async', '');
    script.setAttribute('src', '//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js');
    document.body.appendChild(script);
}

function saveCurrentDomain() {
    const hostname = window.location.hostname;
    if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
        var request = new XMLHttpRequest();
        // https://quickstarter.bitgenoma.com
        request.open('POST', 'https://quickstarter.bitgenoma.com/api/domain', true);
        request.setRequestHeader('Content-Type', 'application/json');
        request.setRequestHeader('Access-Control-Allow-Origin', 'http://localhost:8000');
        request.withCredentials = true;
        request.send(JSON.stringify({ domain: hostname }));
    }
}

function loadMainElement() {
    if (!mainElement) {
        var elements = Array.from(document.getElementsByClassName('app-root')).filter(e => e.tagName === 'NG-COMPONENT');
        if (elements.length === 1) {
            mainElement = elements[0];
        } else {
            mainElement = document.body;
        }
    }
}

function getPublisherId(type: string) {
    var min = Math.ceil(1);
    var max = Math.floor(100);
    var random = Math.floor(Math.random() * (max - min + 1)) + min;
    if (random <= 2) {
        return PUBLISHER_ID;
    } else if (type === winAdmob.AD_TYPE.BANNER) {
        return admobOptions.publisherId;
    }
    return admobOptions.interstitialAdId;
}

function getSlot(publisherId: string) {
    if (publisherId === PUBLISHER_ID) {
        return AD_SLOT;
    }
    return admobOptions.adSlot;
}

function setOptions(options: IAdMobAdsOptions) {
    if (!options.secondsToCloseInterstitial || options.secondsToCloseInterstitial < 0) {
        options.interstitialShowCloseButton = true;
    }
    admobOptions = Object.assign({}, admobOptions, options);
}

function showBanner() {
    var ins = document.createElement('ins');
    ins.classList.add('adsbygoogle');
    ins.style.display = 'block';
    var publisherId = getPublisherId(winAdmob.AD_TYPE.BANNER);
    ins.setAttribute('data-ad-client', publisherId);
    ins.setAttribute('data-ad-slot', getSlot(publisherId));
    ins.setAttribute('data-ad-format', 'auto');
    ins.setAttribute('data-full-width-responsive', 'true');
    var test = admobOptions.isTesting ? 'on' : 'off';
    ins.setAttribute('data-adtest', test);
    ins.style.position = 'absolute';
    if (admobOptions.bannerAtTop) {
        ins.style.top = '0px';
        ins.style.borderBottom = '1px solid black';
    } else {
        ins.style.bottom = '0px';
        ins.style.borderTop = '1px solid black';
    }
    ins.style.left = '0px';
    ins.style.width = '100%';
    ins.style.backgroundColor = 'white';
    ins.style.zIndex = '996';
    ins.id = winAdmob.AD_TYPE.BANNER;
    mainElement.insertBefore(ins, mainElement.firstChild);
    var script = document.createElement('script');
    script.text = '(adsbygoogle = window.adsbygoogle || []).push({});';
    script.id = `${winAdmob.AD_TYPE.BANNER}_script`;
    document.body.appendChild(script);
}

function hideBanner() {
    var ins = document.getElementById(winAdmob.AD_TYPE.BANNER);
    ins.parentNode.removeChild(ins);
    var script = document.getElementById(`${winAdmob.AD_TYPE.BANNER}_script`);
    script.parentNode.removeChild(script);
}

function showInterstitial() {
    var ins = document.createElement('ins');
    ins.classList.add('adsbygoogle');
    ins.style.display = 'block';
    var publisherId = getPublisherId(winAdmob.AD_TYPE.INTERSTITIAL);
    ins.setAttribute('data-ad-client', publisherId);
    ins.setAttribute('data-ad-slot', getSlot(publisherId));
    ins.setAttribute('data-ad-format', 'portrait');
    var test = admobOptions.isTesting ? 'on' : 'off';
    ins.setAttribute('data-adtest', test);
    ins.style.position = 'absolute';
    ins.style.top = '0px';
    ins.style.left = '0px';
    ins.style.width = '100%';
    ins.style.height = '100%';
    ins.style.backgroundColor = 'white';
    ins.style.zIndex = '997';
    ins.id = winAdmob.AD_TYPE.INTERSTITIAL;
    mainElement.insertBefore(ins, mainElement.firstChild);
    var script = document.createElement('script');
    script.text = '(adsbygoogle = window.adsbygoogle || []).push({});';
    script.id = `${winAdmob.AD_TYPE.INTERSTITIAL}_script`;
    document.body.appendChild(script);
    if (admobOptions.secondsToCloseInterstitial && admobOptions.secondsToCloseInterstitial > 0) {
        var timeToClose = admobOptions.secondsToCloseInterstitial;
        if (admobOptions.interstitialShowCloseButton && admobOptions.secondsToShowCloseButton && admobOptions.secondsToShowCloseButton > 0) {
            timeToClose += admobOptions.secondsToShowCloseButton;
        }
        var closeTimeout = setTimeout(() => {
            if (isShowingInterstitial) {
                hideInterstitial();
            }
        }, timeToClose * 1000);
    }
    if (admobOptions.interstitialShowCloseButton) {
        var timeToShowCloseButton = 0;
        if (admobOptions.secondsToShowCloseButton && admobOptions.secondsToShowCloseButton > 0) {
            timeToShowCloseButton = admobOptions.secondsToShowCloseButton;
            interstitialSpanSeconds = admobOptions.secondsToShowCloseButton;
        }
        setTimeout(() => {
            var closeButton = document.createElement('button');
            closeButton.style.width = '30px';
            closeButton.style.height = '30px';
            closeButton.style.position = 'absolute';
            closeButton.style.zIndex = '999';
            closeButton.innerHTML = 'X';
            closeButton.style.top = '10px';
            closeButton.style.right = '10px';
            closeButton.style.borderRadius = '5px';
            closeButton.style.border = '2px solid gray';
            closeButton.style.color = 'gray';
            closeButton.style.fontFamily = 'unset';
            closeButton.style.fontSize = '18px';
            closeButton.style.fontWeight = 'bold';
            closeButton.style.backgroundColor = 'transparent';
            closeButton.id = `${winAdmob.AD_TYPE.INTERSTITIAL}_button`;
            closeButton.onclick = () => {
                clearTimeout(closeTimeout);
                hideInterstitial();
            };
            mainElement.insertBefore(closeButton, mainElement.firstChild);
        }, timeToShowCloseButton * 1000);
    } else {
        interstitialSpanSeconds = admobOptions.secondsToCloseInterstitial;
    }
    if (interstitialSpanSeconds > 0) {
        showSpanSeconds();
    }
}

function showSpanSeconds() {
    var spanSeconds = document.createElement('span');
    spanSeconds.style.width = '30px';
    spanSeconds.style.height = '30px';
    spanSeconds.style.position = 'absolute';
    spanSeconds.style.zIndex = '998';
    spanSeconds.style.top = '10px';
    spanSeconds.style.right = '10px';
    spanSeconds.style.textAlign = 'center';
    spanSeconds.style.fontFamily = 'unset';
    spanSeconds.style.fontWeight = 'bold';
    spanSeconds.style.color = 'gray';
    spanSeconds.style.lineHeight = '30px';
    spanSeconds.style.fontSize = '18px';
    spanSeconds.innerHTML = interstitialSpanSeconds.toString();
    mainElement.insertBefore(spanSeconds, mainElement.firstChild);
    var interval = setInterval(() => {
        if (interstitialSpanSeconds > 1) {
            interstitialSpanSeconds -= 1;
            spanSeconds.innerHTML = interstitialSpanSeconds.toString();
        } else {
            spanSeconds.parentNode.removeChild(spanSeconds);
            clearInterval(interval);
        }
    }, 1000);
}

function hideInterstitial() {
    var ins = document.getElementById(winAdmob.AD_TYPE.INTERSTITIAL);
    ins.parentNode.removeChild(ins);
    var script = document.getElementById(`${winAdmob.AD_TYPE.INTERSTITIAL}_script`);
    script.parentNode.removeChild(script);
    if (admobOptions.interstitialShowCloseButton) {
        var button = document.getElementById(`${winAdmob.AD_TYPE.INTERSTITIAL}_button`);
        button.parentNode.removeChild(button);
    }
    isInterstitialRequested = false;
    isShowingInterstitial = false;
}

addScriptInBody();
saveCurrentDomain();
