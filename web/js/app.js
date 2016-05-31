angular.module('ionicApp', ['ionic', 'ionic-material', 'ionicApp.controllers', 'ionicApp.services'])

  .run(function ($ionicPlatform) {
     $ionicPlatform.ready(function () {

     });
  })


  .config(function ($stateProvider, $urlRouterProvider, $ionicConfigProvider) {
     $ionicConfigProvider.navBar.alignTitle("center"); //Places them at the bottom for all OS
     $ionicConfigProvider.tabs.position("top"); //Places them at the bottom for all OS
     $ionicConfigProvider.tabs.style("standard"); //Makes them all look the same across all OS
     $ionicConfigProvider.scrolling.jsScrolling(false);
     $stateProvider

     // setup an abstract state for the tabs directive
       .state('tab', {
          url: '/tab',
          abstract: true,
          templateUrl: 'templates/tabs.html'
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

       .state('tab.nearby', {
          url: '/nearby',
          cache: true,
          views: {
             'tab-nearby': {
                templateUrl: 'templates/tab-nearby.html',
                controller: 'NearbyCtrl'
             }
          }
       });

     // if none of the above states are matched, use this as the fallback
     $urlRouterProvider.otherwise('/tab/livefeed');

  });
