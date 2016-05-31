/**
 *
 The MIT License (MIT)
 Copyright (c) 2016, Dimosthenis Stefanidis, Data Management Systems Laboratory (DMSL)
 Department of Computer Science, University of Cyprus, Nicosia, CYPRUS,
 dmsl@cs.ucy.ac.cy, http://dmsl.cs.ucy.ac.cy/
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
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
