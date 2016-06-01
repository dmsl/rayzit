// angular.module is a global place for creating, registering and retrieving Angular modules
// 'ionicApp' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'ionicApp.services' is found in services.js
// 'ionicApp.controllers' is found in controllers.js
angular.module('ionicApp', ['ionic', 'ionicApp.controllers', 'ionicApp.services', 'ngCordova'])

  .run(function ($ionicPlatform, $ionicPopup) {
    $ionicPlatform.ready(function () {

      // if we have the keyboard plugin, let use it
      if (window.cordova && window.cordova.plugins.Keyboard) {
        //Lets hide the accessory bar fo the keyboard (ios)
        cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
        // also, lets disable the native overflow scroll
        cordova.plugins.Keyboard.disableScroll(true);
      }

      var background = false;
      document.addEventListener("pause", onPause, false);
      function onPause() {
        background = true;
      }

      document.addEventListener("resume", onResume, false);
      function onResume() {
        background = false;
      }
$ionicPopup.alert({
            cssClass: 'error',
            title: "The internet is disconnected on your device and some features may not work correctly.",
            okText: '<b>OK</b>'
          });

      var flag = false;
      var stillOffline = false;

      document.addEventListener("offline", onOffline, false);
      function onOffline() {
        if (flag && !stillOffline && !background) {
          $ionicPopup.alert({
            cssClass: 'error',
            title: "The internet is disconnected on your device and some features may not work correctly.",
            okText: '<b>OK</b>'
          });
          stillOffline = true;
        }
      }

      document.addEventListener("online", onOnline, false);
       function onOnline() {
         stillOffline = false;
       }

      if (window.StatusBar) {
        if (ionic.Platform.isAndroid()) {
          StatusBar.backgroundColorByHexString("#BA3209")
        } else {
          StatusBar.overlaysWebView(true);
          StatusBar.styleLightContent();
        }
      }

      if (window.Connection) {
        if (navigator.connection.type == Connection.NONE) {
          $ionicPopup.alert({
            cssClass: 'error',
            title: "The internet is disconnected on your device. Make sure your device is" +
            " connected to a Wi-Fi or mobile network and try again.",
            okText: '<b>Ok</b>'
          }).then(function (result) {
            ionic.Platform.exitApp();
          });
        }else{
          flag = true;
        }
      }

      // then override any default you want
      window.plugins.nativepagetransitions.globalOptions.duration = 100;
      window.plugins.nativepagetransitions.globalOptions.iosdelay = 350;
      window.plugins.nativepagetransitions.globalOptions.androiddelay = 300; // 300
      window.plugins.nativepagetransitions.globalOptions.winphonedelay = 350;
      window.plugins.nativepagetransitions.globalOptions.slowdownfactor = 1;
      // these are used for slide left/right only currently
      window.plugins.nativepagetransitions.globalOptions.fixedPixelsTop = 92;
      window.plugins.nativepagetransitions.globalOptions.fixedPixelsBottom = 0;

    });
  })

  .
  config(function ($stateProvider, $urlRouterProvider, $ionicConfigProvider) {
    $ionicConfigProvider.backButton.text('').icon('ion-android-arrow-back').previousTitleText(true);
    $ionicConfigProvider.navBar.alignTitle("center"); //Places them at the bottom for all OS
    //$ionicConfigProvider.tabs.position("bottom"); //Places them at the bottom for all OS
    $ionicConfigProvider.tabs.style("standard"); //Makes them all look the same across all OS
    /*$ionicConfigProvider.scrolling.jsScrolling(false);*/
    $ionicConfigProvider.scrolling.jsScrolling(false);

    $stateProvider

    // setup an abstract state for the tabs directive
      .state('tab', {
        url: '/tab',
        abstract: true,
        templateUrl: 'templates/tabs.html',
        controller: 'MainCtrl'
      })

      // Each tab has its own nav history stack:

      .state('tab.livefeed', {
        url: '/livefeed',
        cache: true,
        views: {
          'tab-livefeed': {
            templateUrl: 'templates/tab-livefeed.html',
            controller: 'LivefeedCtrl'
          }
        }
      })

      .state('tab.nearby', {
        url: '/nearby',
        cache: true,
        views: {
          'tab-nearby': {
            templateUrl: 'templates/tab-nearby.html',
            controller: 'NearbyCtrl'
          }
        }
      })

      .state('tab.starred', {
        url: '/starred',
        cache: true,
        views: {
          'tab-starred': {
            templateUrl: 'templates/tab-starred.html',
            controller: 'StarredCtrl'
          }
        }
      })


      .state('replies', {
        url: "/replies",
        cache: true,
        templateUrl: "templates/replies.html"
      })

      .state('new-rayz', {
        url: "/new-rayz",
        cache: false,
        templateUrl: "templates/new-rayz.html"
      })

      .state('new-reply', {
        url: "/new-reply",
        cache: true,
        templateUrl: "templates/new-reply.html"
      })


      .state('settings', {
        url: "/settings",
        cache: true,
        templateUrl: "templates/settings.html",
        controller: 'SettingsCtrl'
      })

      .state('distance-metric', {
        url: "/distance-metric",
        cache: true,
        templateUrl: "templates/distance-metric.html",
        controller: 'SettingsCtrl'
      })

      .state('select-distance', {
        url: "/select-distance",
        cache: true,
        templateUrl: "templates/select-distance.html",
        controller: 'SettingsCtrl'
      })

      .state('about', {
        url: "/about",
        cache: true,
        templateUrl: "templates/about.html",
        controller: 'AboutCtrl'
      });

    // if none of the above states are matched, use this as the fallback
    $urlRouterProvider.otherwise('/tab/livefeed');

  });
