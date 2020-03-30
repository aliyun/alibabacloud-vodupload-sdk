#
# Be sure to run `pod lib lint AlivcCore.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'VODUpload'
  s.version          = '1.6.0'
  s.summary          = 'A short description of VODUpload.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
  VODUpload
                       DESC

  s.homepage         = 'https://.www.aliyun.com'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'Apache License, Version 2.0', :file => 'LICENSE' }
  s.author           = { 'aliyun' => 'aliyuncloudcomputing' }
  s.source           = { :git => 'https://github.com/aliyun/alibabacloud-vodupload-sdk.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '8.0'

  s.source_files = 'VODUpload/src/*','VODUpload/src/utils/*'

   # s.resource_bundles = {
   #   'AliyunVideoSDKPro' => ['AlivcCore/Assets/ShortVideoResource/**/*','AlivcCore/Assets/Images/**/*','AlivcCore/Classes/**/*.xib']
   # }
  # s.resource = "Src/Resources/AliyunVideoSDKPro.bundle"
   

  # s.prefix_header_contents = ''


  s.public_header_files = 'VODUpload/src/*.h'
  
   s.frameworks = "SystemConfiguration", "MobileCoreServices", "CoreMedia", "AVFoundation", "CoreTelephony"
   s.library   = "resolv"
   s.dependency 'AliyunOSSiOS'
end
