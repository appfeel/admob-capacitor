
  Pod::Spec.new do |s|
    s.name = 'AdMobAds'
    s.version = '0.0.1'
    s.summary = 'easy way to integrate Google AdMob'
    s.license = 'MIT'
    s.homepage = '-'
    s.author = ''
    s.source = { :git => '-', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end