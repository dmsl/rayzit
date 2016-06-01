angular.module('ionicApp.controllers', ['ngCordova'])

  .controller('MainCtrl', function ($ionicPopover, $ionicActionSheet, $cordovaClipboard, $rootScope, $scope, $state, $ionicSideMenuDelegate, $ionicPopup, $timeout, sharedProperties) {

    $rootScope.livefeed = [];
    $rootScope.starredRayzs = [];
    $rootScope.nearbyRayzs = [];
    $rootScope.rayz = {};
    $rootScope.power;
    $rootScope.spinner2 = true;
    $rootScope.spinner3 = true;
    $rootScope.hideValN = true;
    $rootScope.hideValL = true;
    $rootScope.livefeedRefresher = false;
    $rootScope.nearbyRefresher = false;
    $rootScope.starredRefresher = false;
    $rootScope.data = {selectDistance: null};

    $scope.getCurrentPower = function (destination) {
      /*sharedProperties.getPower("max-requests")
       .then(function (responseP) {
       sharedProperties.setCurPower(responseP.power);
       $rootScope.power = responseP.power;
       if(destination === "new-reply"){
       sharedProperties.setRayzId_Reply($rootScope.rayz.rayzId);
       }
       $state.go(destination);
       });*/
      if (destination === "new-reply") {
        sharedProperties.setRayzId_Reply($rootScope.rayz.rayzId);
      }
      $state.go(destination);
    };

    var a = window.outerWidth;
    var b = window.outerHeight;
    if (a < b) {

      $scope.dev_width = a - 47;
      if ($scope.dev_width < 150) {
        $scope.dev_width = 150;
      }
      $scope.side_menu_chevron = $scope.dev_width - 30;
    } else {
      $scope.dev_width = b - 47;
      if ($scope.dev_width < 150) {
        $scope.dev_width = 150;
      }
      $scope.side_menu_chevron = $scope.dev_width - 30;
    }

    $scope.active = '';

    $scope.fun = function () {
      if ($scope.active === '')
        $scope.active = 'is-active';
      else
        $scope.active = '';
    };

    $timeout(function () {
      // Watch for changes to the openRatio which is a value between 0 and 1 that says how "open" the side menu is
      $scope.$watch(function () {
          return $ionicSideMenuDelegate.getOpenRatio();
        },
        function (ratio) {
          $scope.data = ratio;
          if (ratio == 0) {
            $scope.active = '';
          }
        });
    });

    $scope.showPopup = function () {
      $scope.data = {};

      var confirmPopup = $ionicPopup.confirm({
        title: '<b>Play Intro Online</b>',
        scope: $scope,
        cancelText: '<div class="cancel">Cancel</div>',
        okText: '<b>Ok</b>',
        okType: 'button-positive',
      });
      confirmPopup.then(function (res) {
        if (res) {
          window.open('http://rayzit.com', '_blank', 'location=no');
        }
      });
    };

    $scope.changeTab = function (to) {
      var str = document.URL;
      var fromL = str.indexOf("livefeed") > -1;
      var fromN = str.indexOf("nearby") > -1;

      if (fromL) {
        if (to === "nearby") {
          $scope.goRight('tab.nearby');
        } else if (to === "starred") {
          $scope.goRight('tab.starred');
        }
      } else if (fromN) {
        if (to === "livefeed") {
          $scope.goLeft('tab.livefeed');
        } else if (to === "starred") {
          $scope.goRight('tab.starred');
        }
      } else {
        if (to === "livefeed") {
          $scope.goLeft('tab.livefeed');
        } else if (to === "nearby") {
          $scope.goLeft('tab.nearby');
        }
      }
    };

    $scope.goLeft = function (destination) {
      $state.go(destination);
      window.plugins.nativepagetransitions.slide(
        {"direction": "right", "fixedPixelsTop": 92},
        function (msg) {
          console.log("success: " + msg)
        },
        function (msg) {
          alert("error: " + msg)
        }
      );
    };

    $scope.goRight = function (destination) {
      $state.go(destination);
      window.plugins.nativepagetransitions.slide(
        {
          "direction": "left",
          "fixedPixelsBottom": 92
        },
        function (msg) {
          console.log("success: " + msg)
        },
        function (msg) {
          alert("error: " + msg)
        }
      );
    };

    $scope.closeMenu = function () {
      $ionicSideMenuDelegate.toggleLeft();
    };


    $scope.showActionsheetRayzs = function (rayzId, str) {
      $ionicActionSheet.show({
        buttons: [
          {text: '<i class="icon ion-clipboard"></i> <span class="actionsheet_letters"> Copy </span>'},
          {text: '<i class="icon report-blue-icon"></i> <span class="actionsheet_letters"> Report </span>'}
        ],
        cancelText: '<span class="actionsheet_cancel_letters"> Cancel </span>',

        cancel: function () {
          console.log('CANCELLED');
        },
        buttonClicked: function (index) {
          if (index === 0) {
            $cordovaClipboard.copy(str).then(function () {
              console.log("Copied text");
            }, function () {
              console.error("There was an error copying");
            });
          } else {
            sharedProperties.reportRayz(rayzId, "max-requests");
            $scope.changeRayzs_Report(rayzId);
          }
          return true;
        }
      });
    };


    $scope.changeRayzs_Report = function (rayzId) {
      for (var i = 0; i < $rootScope.livefeed.length; i++) {
        if ($rootScope.livefeed[i].rayzId === rayzId) {
          $rootScope.livefeed[i].report++;
          break;
        }
      }
      for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
        if ($rootScope.starredRayzs[i].rayzId === rayzId) {
          $rootScope.starredRayzs[i].report++;
          break;
        }
      }
      for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
        if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
          $rootScope.nearbyRayzs[i].report++;
          break;
        }
      }

      $rootScope.rayz.report++;
    };


    $scope.showActionsheetReplies = function (replyId, str) {
      $ionicActionSheet.show({
        buttons: [
          {text: '<i class="icon ion-clipboard"></i> <span class="actionsheet_letters"> Copy </span>'},
          {text: '<i class="icon report-blue-icon"></i> <span class="actionsheet_letters"> Report </span>'}
        ],
        cancelText: '<span class="actionsheet_cancel_letters"> Cancel </span>',

        cancel: function () {
          console.log('CANCELLED');
        },
        buttonClicked: function (index) {
          if (index === 0) {
            $cordovaClipboard.copy(str).then(function () {
              console.log("Copied text");
            }, function () {
              console.error("There was an error copying");
            });
          } else {
            sharedProperties.reportReply(replyId, "max-requests");
            $scope.changeReplies_Report(replyId);
          }
          return true;
        }
      });
    };

    $scope.changeReplies_Report = function (replyId) {
      var rayzId = $rootScope.rayz.rayzId;
      for (var i = 0; i < $rootScope.rayz.replies.length; i++) {
        if ($rootScope.rayz.replies[i].rayzReplyId === replyId) {
          $rootScope.rayz.replies[i].report++;
          break;
        }
      }


      for (var i = 0; i < $rootScope.livefeed.length; i++) {
        if ($rootScope.livefeed[i].rayzId === rayzId) {
          for (var j = 0; j < $rootScope.livefeed[i].replies.length; j++) {
            if ($rootScope.livefeed[i].replies[j].rayzReplyId === replyId) {
              $rootScope.livefeed[i].replies[j].report++;
              i = $rootScope.livefeed.length;
              break;
            }
          }
        }
      }
      for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
        if ($rootScope.starredRayzs[i].rayzId === rayzId) {
          for (var j = 0; j < $rootScope.starredRayzs[i].replies.length; j++) {
            if ($rootScope.starredRayzs[i].replies[j].rayzReplyId === replyId) {
              $rootScope.starredRayzs[i].replies[j].report++;
              i = $rootScope.starredRayzs.length;
              break;
            }
          }
        }
      }
      for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
        if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
          for (var j = 0; j < $rootScope.nearbyRayzs[i].replies.length; j++) {
            if ($rootScope.nearbyRayzs[i].replies[j].rayzReplyId === replyId) {
              $rootScope.nearbyRayzs[i].replies[j].report++;
              i = $rootScope.nearbyRayzs.length;
              break;
            }
          }
        }
      }

    };

    $scope.starToggle = function (rayzId, tabs) {
      var rayz;

      for (var i = 0; i < $rootScope.livefeed.length; i++) {
        if ($rootScope.livefeed[i].rayzId === rayzId) {
          if ($rootScope.livefeed[i].starByMe === "Star") {
            $rootScope.livefeed[i].follow++;
            $rootScope.livefeed[i].starByMe = "Unstar";
            rayz = angular.copy($rootScope.livefeed[i]);
          } else {
            $rootScope.livefeed[i].follow--;
            $rootScope.livefeed[i].starByMe = "Star";
          }
          break;
        }
      }
      for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
        if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
          if ($rootScope.nearbyRayzs[i].starByMe === "Star") {
            $rootScope.nearbyRayzs[i].follow++;
            $rootScope.nearbyRayzs[i].starByMe = "Unstar";
            rayz = angular.copy($rootScope.nearbyRayzs[i]);
          } else {
            $rootScope.nearbyRayzs[i].follow--;
            $rootScope.nearbyRayzs[i].starByMe = "Star";
          }
          break;
        }
      }

      var starred = false;
      for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
        if ($rootScope.starredRayzs[i].rayzId === rayzId) {
          if ($rootScope.starredRayzs[i].starByMe === "Unstar") {
            starred = true;
            $rootScope.starredRayzs.splice(i, 1);
            i = $rootScope.starredRayzs.length + 1;
          }
        }
      }

      if (!starred) {
        $rootScope.starredRayzs.unshift(rayz);
      }

      if (tabs === "other-tabs") {
        if ($rootScope.rayz.starByMe === "Star") {
          $rootScope.rayz.follow++;
          $rootScope.rayz.starByMe = "Unstar";
        } else {
          $rootScope.rayz.follow--;
          $rootScope.rayz.starByMe = "Star";
        }
      }

      if (!starred) {// Star a rayz
        sharedProperties.starRayz(rayzId, "max-requests").then(function () {
        })
      } else {// Untar a rayz
        sharedProperties.unstarRayz(rayzId, "max-requests").then(function () {
        })
      }

    };

    $scope.rerayzItem = function (rayzId, tabs) {
      if (sharedProperties.getCurPower() >= 2) {
        for (var i = 0; i < $rootScope.livefeed.length; i++) {
          if ($rootScope.livefeed[i].rayzId === rayzId) {
            $rootScope.livefeed[i].rerayz++;
            break;
          }
        }
        for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
          if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
            $rootScope.nearbyRayzs[i].rerayz++;
            break;
          }
        }

        for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
          if ($rootScope.starredRayzs[i].rayzId === rayzId) {
            $rootScope.starredRayzs[i].rerayz++;
            break;
          }
        }

        if (tabs === "other-tabs") {
          $rootScope.rayz.rerayz++;
        }

        sharedProperties.rerayz(rayzId, "max-requests")
      } else {
        $ionicPopup.alert({
          title: "<b>Rerayz Failed</b>",
          content: "You don't have enough power!",
          okText: '<b>Ok</b>',
          okType: 'button-royal'
        })
      }

    };


    $scope.setRayzId_Replies = function (rayz) {
      $rootScope.rayz = angular.copy(rayz);
      sharedProperties.setRayzId_Replies(rayz);
      $state.go("replies");
    };

    var template = '<div style="position:relative; top:30px;">' +
      '            <ion-popover-view style="height:40px!important; width: 125px;!important;border-radius: 10px; background-color:rgba(0, 0, 0, 0.7);">' +
      '                <ion-content scroll="false" style="border-radius: 10px;"> ' +
      '                   <div class="list" style="position:relative; top:8px;left:15px;"> ' +
      '                         <span class="actionsheet_letters" style="position:relative;bottom:4px; right:10px; color:white">Power: {{$root.power}}%</span>' +
      '                   </div>' +
      '                </ion-content>' +
      '            </ion-popover-view>' +
      '          </div>';

    $scope.popover = $ionicPopover.fromTemplate(template, {
      scope: $scope
    });

    $scope.openPopover = function ($event) {
      $scope.popover.show($event);
    };
    $rootScope.refresh = false;

    $scope.refresh = function(){
      //>>>>>>>>>>>>>>>>>>>>> if already updating stop
      $rootScope.refresh = true;
      var refresher = document.getElementsByClassName("refresher");
      for(var i = 0; i < refresher.length; i++){
        refresher[i].style.visibility = "visible";
      }

      var refreshButton = document.getElementsByClassName("refreshButton");
      for(var i = 0; i < refreshButton.length; i++){
        refreshButton[i].style.visibility = "hidden";
      }

      $rootScope.livefeedRefresher = true;
      $rootScope.nearbyRefresher = true;
      $rootScope.starredRefresher = true;
    }

  })

  .controller('LivefeedCtrl', function ($timeout, $scope, $state, $rootScope, $http, sharedProperties) {

    $scope.onSwipeLeft = function () {
      $state.go('tab.nearby');
      window.plugins.nativepagetransitions.slide(
        {"direction": "left", "fixedPixelsTop": 92},
        function (msg) {
          console.log("success: " + msg)
        },
        function (msg) {
          alert("error: " + msg)
        }
      );
    };

    $scope.APP_STARTING = true;

    if ($scope.APP_STARTING) {
      sharedProperties.getCurrentLocation("no-limit") // Get Location and create New User
        .then(function () {
          sharedProperties.getPower("no-limit") // Get how much power left
            .then(function (responseP) {

              sharedProperties.setCurPower(responseP.power);

              sharedProperties.getLivefeed("no-limit") // Get Livefeed
                .then(function (responseL) {

                  for (var i = 0; i < responseL.counter; i++) {
                    responseL.liveFeed[i].starByMe = "Star";
                    responseL.liveFeed[i].timestamp = sharedProperties.display_time(responseL.liveFeed[i].timestamp);
                    responseL.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseL.liveFeed[i].maxDistance);
                    for (var j = 0; j < responseL.liveFeed[i].answers; j++) {
                      responseL.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseL.liveFeed[i].replies[j].timestamp);
                    }
                  }


                  sharedProperties.getStarred("no-limit")
                    .then(function (responseS) { // Get Starred

                      for (var i = 0; i < responseS.counter; i++) {
                        responseS.liveFeed[i].starByMe = "Unstar";
                        responseS.liveFeed[i].timestamp = sharedProperties.display_time(responseS.liveFeed[i].timestamp);
                        responseS.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseS.liveFeed[i].maxDistance);
                        for (var j = 0; j < responseS.liveFeed[i].answers; j++) {
                          responseS.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseS.liveFeed[i].replies[j].timestamp);
                        }
                      }

                      for (var i = 0; i < responseS.counter; i++) {
                        for (var j = 0; j < responseL.counter; j++) {
                          if (responseS.liveFeed[i].rayzId === responseL.liveFeed[j].rayzId) {
                            responseL.liveFeed[j].starByMe = "Unstar";
                          }

                        }
                      }
                      $rootScope.starredRayzs = responseS.liveFeed; // root Starred is ready
                      $rootScope.livefeed = responseL.liveFeed; // root Livefeed is ready

                      var spinnerToHide1 = document.getElementsByClassName("spinner1");
                      spinnerToHide1[0].style.visibility = "hidden";
                      $rootScope.hideValL = false;

                      $rootScope.spinner3 = false;
                      var spinnerToHide3 = document.getElementsByClassName("spinner3");
                      if (spinnerToHide3.length > 0)
                        spinnerToHide3[0].style.visibility = "hidden";


                      sharedProperties.getNearby("no-limit")
                        .then(function (responseN) { // Get Nearby
                          var max_nearby_rayzs = sharedProperties.getMaxNearbyRayzs();

                          if (max_nearby_rayzs < responseN.counter) {
                            responseN.counter = max_nearby_rayzs;
                            responseN.liveFeed = responseN.liveFeed.slice(0, max_nearby_rayzs);
                          }

                          for (var i = 0; i < responseN.counter; i++) {
                            responseN.liveFeed[i].starByMe = "Star";
                            responseN.liveFeed[i].timestamp = sharedProperties.display_time(responseN.liveFeed[i].timestamp);
                            responseN.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseN.liveFeed[i].maxDistance);
                            for (var j = 0; j < responseN.liveFeed[i].answers; j++) {
                              responseN.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseN.liveFeed[i].replies[j].timestamp);
                            }
                          }

                          for (var i = 0; i < responseS.counter; i++) {
                            for (var j = 0; j < responseN.counter; j++) {
                              if (responseS.liveFeed[i].rayzId === responseN.liveFeed[j].rayzId) {
                                responseN.liveFeed[j].starByMe = "Unstar";
                              }
                            }
                          }
                          $rootScope.nearbyRayzs = responseN.liveFeed; // root Nearby is ready
                          $rootScope.spinner2 = false;
                          $rootScope.hideValN = false;
                          var spinnerToHide2 = document.getElementsByClassName("spinner2");
                          var buttonhideValN = document.getElementsByClassName("hideValN");
                          if (spinnerToHide2.length > 0)
                            spinnerToHide2[0].style.visibility = "hidden";

                          if (buttonhideValN.length > 0)
                            buttonhideValN[0].style.visibility = "visible";
                          $rootScope.hideValN = false;
                          $scope.APP_STARTING = false;
						  FlurryAgent.startSession('S2YMYGTKR9C722MRVXM7');
                        })
                    })

                })
            })

        })
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

    var offline = false;
    document.addEventListener("offline", onOffline, false);
    function onOffline() {
      offline = true;
    }

    document.addEventListener("online", onOnline, false);
    function onOnline() {
      offline = false;
    }

    var updateAuto = function () {
      if (background || offline)return;

      // getPower
      sharedProperties.getPower("max-requests")
        .then(function (responseP) {
          sharedProperties.setCurPower(responseP.power);
          $rootScope.power = responseP.power;
        });

      // getStarred
      sharedProperties.getStarred("max-requests").then(
        function (responseS) {
          for (var i = 0; i < responseS.counter; i++) {
            responseS.liveFeed[i].timestamp = sharedProperties.display_time(responseS.liveFeed[i].timestamp);
            responseS.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseS.liveFeed[i].maxDistance);
            for (var j = 0; j < responseS.liveFeed[i].answers; j++) {
              try {
                responseS.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseS.liveFeed[i].replies[j].timestamp);
              }
              catch (err) {
                return;
              }
            }
          }
          for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
            responseS.liveFeed[i].starByMe = "Unstar";
          }
          $rootScope.starredRayzs = responseS.liveFeed;

          // getLivefeed
          sharedProperties.getLivefeed("max-requests").then(
            function (responseL) {

              for (var i = 0; i < responseL.counter; i++) {
                responseL.liveFeed[i].starByMe = "Star";
                responseL.liveFeed[i].timestamp = sharedProperties.display_time(responseL.liveFeed[i].timestamp);
                responseL.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseL.liveFeed[i].maxDistance);
                for (var j = 0; j < responseL.liveFeed[i].answers; j++) {
                  try {
                    responseL.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseL.liveFeed[i].replies[j].timestamp);
                  }
                  catch (err) {
                    return;
                  }
                }
              }

              // getNearby
              sharedProperties.getNearby("max-requests").then(
                function (responseN) {
                  var max_nearby_rayzs = sharedProperties.getMaxNearbyRayzs();

                  if (max_nearby_rayzs < responseN.counter) {
                    responseN.counter = max_nearby_rayzs;
                    responseN.liveFeed = responseN.liveFeed.slice(0, max_nearby_rayzs);
                  }

                  for (var i = 0; i < responseN.counter; i++) {
                    responseN.liveFeed[i].starByMe = "Star";
                    responseN.liveFeed[i].timestamp = sharedProperties.display_time(responseN.liveFeed[i].timestamp);
                    responseN.liveFeed[i].maxDistance = sharedProperties.getRayzDistance(responseN.liveFeed[i].maxDistance);
                    for (var j = 0; j < responseN.liveFeed[i].answers; j++) {
                      try {
                        responseN.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseN.liveFeed[i].replies[j].timestamp);
                      }
                      catch (err) {
                        return;
                      }
                    }
                  }

                  // Table - Starred
                  for (var i = 0; i < responseS.counter; i++) {
                    // Livefeed rayzs already starred
                    for (var j = 0; j < responseL.counter; j++) {
                      if (responseS.liveFeed[i].rayzId === responseL.liveFeed[j].rayzId)
                        responseL.liveFeed[j].starByMe = "Unstar";
                    }
                    // Nearby rayzs already starred
                    for (var j = 0; j < responseN.counter; j++) {
                      if (responseS.liveFeed[i].rayzId === responseN.liveFeed[j].rayzId)
                        responseN.liveFeed[j].starByMe = "Unstar";
                    }
                  }
                  $rootScope.livefeed = responseL.liveFeed;
                  $rootScope.nearbyRayzs = responseN.liveFeed;
                }
              );
            }
          );
        }
      );

      $timeout(updateAuto, 300000); // 5 min
    };

    $timeout(updateAuto, 300000); // 5 min


    if ($rootScope.livefeedRefresher) {
      var refresher = document.getElementsByClassName("refresher");
      for(var i = 0; i < refresher.length; i++){
        refresher[i].style.visibility = "visible";
      }

      var refreshButton = document.getElementsByClassName("refreshButton");
      for(var i = 0; i < refreshButton.length; i++){
        refreshButton[i].style.visibility = "hidden";
      }
    }


  })

  .controller('NearbyCtrl', function ($window, $ionicGesture, $scope, $rootScope, $state) {
    if (!$rootScope.spinner2) {
      var spinnerToHide2 = document.getElementsByClassName("spinner2");
      spinnerToHide2[0].style.visibility = "hidden";
    }
    if (!$rootScope.hideValN) {
      var buttonhideValN = document.getElementsByClassName("hideValN");
      buttonhideValN[0].style.visibility = "visible";
    }

    if ($rootScope.nearbyRefresher) {
      var refresher = document.getElementsByClassName("refresher");
      for(var i = 0; i < refresher.length; i++){
        refresher[i].style.visibility = "visible";
      }

      var refreshButton = document.getElementsByClassName("refreshButton");
      for(var i = 0; i < refreshButton.length; i++){
        refreshButton[i].style.visibility = "hidden";
      }
    }


    $scope.onGesture = function (gesture) {
      if (gesture === 'Swipe Left') {
        $state.go('tab.starred');
        window.plugins.nativepagetransitions.slide(
          {"direction": "left", "fixedPixelsTop": 92},
          function (msg) {
            console.log("success: " + msg)
          },
          function (msg) {
            alert("error: " + msg)
          }
        );
      } else {
        $state.go('tab.livefeed');
        window.plugins.nativepagetransitions.slide(
          {"direction": "right", "fixedPixelsTop": 92},
          function (msg) {
            console.log("success: " + msg)
          },
          function (msg) {
            alert("error: " + msg)
          }
        );
      }
    };

  })

  .controller('StarredCtrl', function ($scope, $state, $rootScope) {
    if (!$rootScope.spinner3) {
      var spinnerToHide3 = document.getElementsByClassName("spinner3");
      spinnerToHide3[0].style.visibility = "hidden";
    }

    if ($rootScope.starredRefresher) {
      var refresher = document.getElementsByClassName("refresher");
      for(var i = 0; i < refresher.length; i++){
        refresher[i].style.visibility = "visible";
      }

      var refreshButton = document.getElementsByClassName("refreshButton");
      for(var i = 0; i < refreshButton.length; i++){
        refreshButton[i].style.visibility = "hidden";
      }
    }

    $scope.onSwipeRight = function () {
      $state.go('tab.nearby');
      window.plugins.nativepagetransitions.slide(
        {"direction": "right", "fixedPixelsTop": 92},
        function (msg) {
          console.log("success: " + msg)
        },
        function (msg) {
          alert("error: " + msg)
        }
      );
    };

  })

  .
  controller('NewRayzCtrl', function ($ionicLoading, $rootScope, $window, $scope, $state, $timeout, sharedProperties) {
    $scope.$on('$ionicView.beforeEnter', function (event, viewData) {
      viewData.enableBack = true;
    });

    sharedProperties.getPower("max-requests")
      .then(function (responseP) {
        sharedProperties.setCurPower(responseP.power);
        $rootScope.power = responseP.power;
        $scope.power_width = $rootScope.power;
        var spinnerToHide4 = document.getElementsByClassName("spinner4");
        if (spinnerToHide4.length > 0)
          spinnerToHide4[0].style.visibility = "hidden";
      });

    if ($rootScope.data.selectDistance === null) {
      $rootScope.data.selectDistance = sharedProperties.getUserMaxDistance();
    }
    $scope.messageModel = "";
    $scope.power_width = $rootScope.power;

    $scope.sendRayz = function (messageModel) {
      messageModel = messageModel.replace(/^\s+/, "");
      if (messageModel !== "" && $scope.power_width >= 2) {
        <!--android, dots, ripple, spiral, lines-->
        $scope.loadingIndicator = $ionicLoading.show({
          showBackdrop: false,
          maxWidth: 3000,
          showDelay: 500,
          template: '<svg class="circular1" viewBox="25 25 50 50">' +
          '<circle class="path" cx="50" cy="50" r="20" fill="none" stroke-width="2" stroke-miterlimit="10"/>' +
          '</svg>'
        });

        sharedProperties.createRayz(messageModel)
          .then(function (response1) {
            // get My Rayzs
            $timeout(function () {
              sharedProperties.getMyRayzs('max-requests')
                .then(function (response2) {
                  // star the first myrayz
                  sharedProperties.starRayz(response2.rayzId, "no-limit")
                    .then(function () {
                      // copy the first my rayz to every table
                      response2.starByMe = "Unstar";
                      response2.follow++;
                      response2.timestamp = sharedProperties.display_time(response2.timestamp);
                      response2.maxDistance = sharedProperties.getRayzDistance(response2.maxDistance);

                      $rootScope.starredRayzs.unshift(response2);
                      $rootScope.nearbyRayzs.unshift(angular.copy(response2));
                      $rootScope.livefeed.unshift(angular.copy(response2));

                      $scope.loadingIndicator.hide();
                      document.getElementById("messageArea").value = "";
                      $window.history.go(-1);
                    });
                });
            }, 300);
          });
      } else if ($scope.power_width < 2) {
        $ionicPopup.alert({
          title: "<b>New Rayz Failed</b>",
          content: "You don't have enough power!",
          okText: '<b>Ok</b>',
          okType: 'button-royal'
        })
      }
    }
  })

  .controller('RepliesCtrl', function ($rootScope, $rootScope, $scope, $timeout, sharedProperties, $window) {
    $scope.$on('$ionicView.beforeEnter', function (event, viewData) {
      viewData.enableBack = true;
    });


    $scope.powerUp = function (replyId) {
      if (sharedProperties.getCurPower() >= 4) {

        for (var i = 0; i < $rootScope.livefeed.length; i++) {
          if ($rootScope.livefeed[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.livefeed[i].replies.length; pos++) {
              if ($rootScope.livefeed[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.livefeed[i].replies[pos].upVotes++;
                break;
              }
            }
            break;
          }
        }
        for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
          if ($rootScope.nearbyRayzs[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.nearbyRayzs[i].replies.length; pos++) {
              if ($rootScope.nearbyRayzs[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.nearbyRayzs[i].replies[pos].upVotes++;
                break;
              }
            }
            break;
          }
        }

        for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
          if ($rootScope.starredRayzs[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.starredRayzs[i].replies.length; pos++) {
              if ($rootScope.starredRayzs[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.starredRayzs[i].replies[pos].upVotes++;
                break;
              }
            }
            break;
          }
        }

        for (var pos = 0; pos < $rootScope.rayz.replies.length; pos++) {
          if ($rootScope.rayz.replies[pos].rayzReplyId === replyId) {
            $rootScope.rayz.replies[pos].upVotes++;
            break;
          }
        }

        sharedProperties.powerUpRayzReply(replyId.rayzReplyId, "max-requests");/////////////////////////////////////replyId only
      } else {
        $ionicPopup.alert({
          title: "<b>Power Up Failed</b>",
          content: "You don't have enough power!",
          okText: '<b>Ok</b>',
          okType: 'button-royal'
        })
      }

    };

    $scope.powerDown = function (replyId) {
      if (sharedProperties.getCurPower() >= 4) {

        for (var i = 0; i < $rootScope.livefeed.length; i++) {
          if ($rootScope.livefeed[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.livefeed[i].replies.length; pos++) {
              if ($rootScope.livefeed[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.livefeed[i].replies[pos].upVotes--;
                break;
              }
            }
            break;
          }
        }
        for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
          if ($rootScope.nearbyRayzs[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.nearbyRayzs[i].replies.length; pos++) {
              if ($rootScope.nearbyRayzs[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.nearbyRayzs[i].replies[pos].upVotes--;
                break;
              }
            }
            break;
          }
        }

        for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
          if ($rootScope.starredRayzs[i].rayzId === $rootScope.rayz.rayzId) {
            for (var pos = 0; pos < $rootScope.starredRayzs[i].replies.length; pos++) {
              if ($rootScope.starredRayzs[i].replies[pos].rayzReplyId === replyId) {
                $rootScope.starredRayzs[i].replies[pos].upVotes--;
                break;
              }
            }
            break;
          }
        }

        for (var pos = 0; pos < $rootScope.rayz.replies.length; pos++) {
          if ($rootScope.rayz.replies[pos].rayzReplyId === replyId) {
            $rootScope.rayz.replies[pos].upVotes--;
            break;
          }
        }

        sharedProperties.powerDownRayzReply(replyId.rayzReplyId, "max-requests");
      } else {
        $ionicPopup.alert({
          title: "<b>Power Down Failed</b>",
          content: "You don't have enough power!",
          okText: '<b>Ok</b>',
          okType: 'button-royal'
        })
      }

    };

  })

  .controller('NewReplyCtrl', function ($rootScope, $ionicLoading, $window, $scope, $timeout, sharedProperties) {
    $scope.$on('$ionicView.beforeEnter', function (event, viewData) {
      viewData.enableBack = true;
    });

    sharedProperties.getPower("max-requests")
      .then(function (responseP) {
        sharedProperties.setCurPower(responseP.power);
        $rootScope.power = responseP.power;
        $scope.power_width = $rootScope.power;
        var spinnerToHide5 = document.getElementsByClassName("spinner5");
        if (spinnerToHide5.length > 0)
          spinnerToHide5[0].style.visibility = "hidden";
      });

    $scope.messageModel = "";
    $scope.power_width = $rootScope.power;


    $scope.sendRayzReply = function (messageModel) {
      var rayzId = sharedProperties.getRayzId_Reply();

      messageModel = messageModel.replace(/^\s+/, "");
      if (messageModel !== "" && $scope.power_width >= 1) {

        $scope.loadingIndicator = $ionicLoading.show({
          showBackdrop: false,
          maxWidth: 3000,
          showDelay: 500,
          template: '<svg class="circular1" viewBox="25 25 50 50">' +
          '<circle class="path" cx="50" cy="50" r="20" fill="none" stroke-width="2" stroke-miterlimit="10"/>' +
          '</svg>'
        });

        sharedProperties.createRayzReply(rayzId, messageModel)
          .then(function (response) {
            var flag = $rootScope.rayz.starByMe;
            var auto_star = window.localStorage.getItem("auto-star");
            if ((auto_star === null || auto_star === true) && $rootScope.rayz.starByMe === "Star") {
              $scope.starToggle(rayzId, 'other-tabs');
              flag = "Unstar";
            }

            sharedProperties.getLivefeed("max-requests")
              .then(function (responseL) {

                var pos1 = -1;
                var pos2 = -1;
                for (var i = 0; i < responseL.counter; i++) {
                  if (responseL.liveFeed[i].rayzId === rayzId)
                    pos1 = i;
                  if ($rootScope.livefeed[i].rayzId === rayzId)
                    pos2 = i;
                }
                for (var i = responseL.counter; i < $rootScope.livefeed.length; i++) {
                  if ($rootScope.livefeed[i].rayzId === rayzId)
                    pos2 = i;
                }

                if (pos1 !== -1) {
                  responseL.liveFeed[pos1].starByMe = flag;
                  responseL.liveFeed[pos1].timestamp = sharedProperties.display_time(responseL.liveFeed[pos1].timestamp);
                  responseL.liveFeed[pos1].maxDistance = sharedProperties.getRayzDistance(responseL.liveFeed[pos1].maxDistance);
                  for (var j = 0; j < responseL.liveFeed[pos1].answers; j++) {
                    responseL.liveFeed[pos1].replies[j].timestamp = sharedProperties.display_time(responseL.liveFeed[pos1].replies[j].timestamp);
                  }

                  $rootScope.livefeed[pos2] = angular.copy(responseL.liveFeed[pos1]);
                  $rootScope.rayz = angular.copy(responseL.liveFeed[pos1]);
                  //search in starred
                  for (var i = 0; i < $rootScope.starredRayzs.length; i++) {
                    if ($rootScope.starredRayzs[i].rayzId === rayzId) {
                      $rootScope.starredRayzs[i] = angular.copy(responseL.liveFeed[pos1]);
                      break;
                    }
                  }
                  // search in nearby
                  for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
                    if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
                      $rootScope.nearbyRayzs[i] = angular.copy(responseL.liveFeed[pos1]);
                      break;
                    }
                  }

                  document.getElementById("messageArea").value = "";
                  $scope.loadingIndicator.hide();
                  $window.history.go(-1);
                } else {
                  sharedProperties.getStarred("max-requests").then(function (responseS) {
                    for (var i = 0; i < responseS.counter; i++) {
                      if (responseS.liveFeed[i].rayzId === rayzId)
                        pos1 = i;
                      if ($rootScope.starredRayzs[i].rayzId === rayzId)
                        pos2 = i;
                    }
                    for (var i = responseS.counter; i < $rootScope.starredRayzs.length; i++) {
                      if ($rootScope.starredRayzs[i].rayzId === rayzId)
                        pos2 = i;
                    }

                    if (pos1 !== -1) {
                      responseS.liveFeed[pos1].starByMe = flag;
                      responseS.liveFeed[pos1].timestamp = sharedProperties.display_time(responseS.liveFeed[pos1].timestamp);
                      responseS.liveFeed[pos1].maxDistance = sharedProperties.getRayzDistance(responseS.liveFeed[pos1].maxDistance);
                      for (var j = 0; j < responseS.liveFeed[pos1].answers; j++) {
                        responseS.liveFeed[pos1].replies[j].timestamp = sharedProperties.display_time(responseS.liveFeed[pos1].replies[j].timestamp);
                      }

                      $rootScope.starredRayzs[pos2] = angular.copy(responseS.liveFeed[pos1]);
                      $rootScope.rayz = angular.copy(responseS.liveFeed[pos1]);
                      // search in nearby
                      for (var i = 0; i < $rootScope.nearbyRayzs.length; i++) {
                        if ($rootScope.nearbyRayzs[i].rayzId === rayzId) {
                          $rootScope.nearbyRayzs[i] = angular.copy(responseS.liveFeed[pos1]);
                          break;
                        }
                      }

                      document.getElementById("messageArea").value = "";
                      $scope.loadingIndicator.hide();
                      $window.history.go(-1);
                    } else {
                      sharedProperties.getNearby("max-requests").then(function (responseN) {
                        for (var i = 0; i < responseN.counter; i++) {
                          if (responseN.liveFeed[i] === rayzId)
                            pos1 = i;
                          if ($rootScope.nearbyRayzs[i] === rayzId)
                            pos2 = i;
                        }
                        for (var i = responseN.counter; i < $rootScope.nearbyRayzs.length; i++) {
                          if ($rootScope.nearbyRayzs[i].rayzId === rayzId)
                            pos2 = i;
                        }
                        responseN.liveFeed[pos1].starByMe = flag;
                        responseN.liveFeed[pos1].timestamp = sharedProperties.display_time(responseN.liveFeed[pos1].timestamp);
                        responseN.liveFeed[pos1].maxDistance = sharedProperties.getRayzDistance(responseN.liveFeed[pos1].maxDistance);
                        for (var j = 0; j < responseN.liveFeed[pos1].answers; j++) {
                          responseN.liveFeed[pos1].replies[j].timestamp = sharedProperties.display_time(responseN.liveFeed[pos1].replies[j].timestamp);
                        }
                        $rootScope.nearbyRayzs[pos2] = angular.copy(responseN.liveFeed[pos1]);

                        if (pos1 != -1)
                          $rootScope.rayz = angular.copy(responseN.liveFeed[pos1]);

                        document.getElementById("messageArea").value = "";
                        $scope.loadingIndicator.hide();
                        $window.history.go(-1);
                      })
                    }

                  });
                }

              });

          });
      } else if ($scope.power_width < 1) {
        $ionicPopup.alert({
          title: "<b>New Reply Failed</b>",
          content: "You don't have enough power!",
          okText: '<b>Ok</b>',
          okType: 'button-royal'
        })
      }
    }

  })

  .controller('SettingsCtrl', function ($scope, $rootScope) {

    $scope.$on('$ionicView.beforeEnter', function (event, viewData) {
      viewData.enableBack = true;
    });

    $scope.countS = -1;
    $scope.updateAutoStar = function (key) {
      var val = window.localStorage.getItem(key);
      $scope.countS = $scope.countS + 1;

      if (val === null) {
        window.localStorage.setItem("Auto-star", true);
        return true;
      }

      if ($scope.countS != 0) {

        if (val === "false") {
          window.localStorage.setItem("Auto-star", true);
          return true;
        } else {
          window.localStorage.setItem("Auto-star", false);
          return false;
        }
      } else {
        return !(val === "false");
      }
    };

    $scope.auto_star = {text: "Auto-star", checked: $scope.updateAutoStar("Auto-star")};


    $scope.countL = -1;
    $scope.updateLocation = function (key) {
      var val = window.localStorage.getItem(key);
      $scope.countL = $scope.countL + 1;

      if (val === null) {
        window.localStorage.setItem("Location", true);
        return true;
      }

      if ($scope.countL != 0) {

        if (val === "false") {
          window.localStorage.setItem("Location", true);
          return true;
        } else {
          window.localStorage.setItem("Location", false);
          return false;
        }
      } else {
        return !(val === "false");
      }
    };
    $scope.location = {text: "Location", checked: $scope.updateLocation("Location")};


    $scope.dev_width = window.innerWidth;
    $scope.dev_height = window.innerHeight;
    if ($scope.dev_width < 150) {
      $scope.dev_width = 150;
    }
    $scope.settings_chevron = $scope.dev_width - 13;
    $scope.settings_toggle = $scope.dev_width - 117;

    $scope.settings_check = $scope.dev_width - 27;
    $scope.arr = {"first": 111};

    var all_distance = ["unlimited", "0.5 km", "5 km", "50 km", "500 km", "5000 km"];
    $rootScope.data = {};
    $rootScope.data2 = {};

    $scope.updateMaxDistance = function (key) {
      var val = null;
      var index = 0;

      if (key === null) {
        for (var i = 0; i < all_distance.length; i++) {
          val = window.localStorage.getItem(all_distance[i]);

          if (val !== null) {
            index = i;
            break;
          }
        }

        if (val === null) {
          window.localStorage.setItem("unlimited", true);
          $rootScope.data.selectDistance = "unlimited";
          return "unlimited";
        } else {
          for (var i = 0; i < all_distance.length; i++) {
            window.localStorage.removeItem(all_distance[i]);
          }
          window.localStorage.setItem(all_distance[index], true);
          $rootScope.data.selectDistance = all_distance[index];
          return all_distance[index];
        }

      } else {
        for (var i = 0; i < all_distance.length; i++) {
          window.localStorage.removeItem(all_distance[i]);
        }
        window.localStorage.setItem(key, true);
        $rootScope.data.selectDistance = key;
        return key;
      }
    };

    $rootScope.data = {selectDistance: $scope.updateMaxDistance(null)};

    $rootScope.data2 = {selectDistanceMetric: "kilometers"};


  })


  .controller('AboutCtrl', function ($scope, $state, $timeout) {
    $scope.$on('$ionicView.beforeEnter', function (event, viewData) {
      viewData.enableBack = true;
    });
    $scope.dev_width = window.innerWidth;
    if ($scope.dev_width < 150) {
      $scope.dev_width = 150;
    }
    $scope.settings_chevron = $scope.dev_width - 13;
    $scope.settings_toggle = $scope.dev_width - 167;


    $scope.settings_check = $scope.dev_width - 27;

    $scope.open_Send_FeedBack = {value: true};
    $scope.open_Term_Of_Service = {value: true};
    $scope.open_Rules = {value: true};
    $scope.open_Privacy_Policy = {value: true};

    $scope.openOk = true;

    $scope.openUrl = function (url, name, location) {
      if ($scope.openOk) {
        $scope.openOk = false;
        window.open(url, name, location);
        $timeout(changeOpenOk, 2000);
      }
    };

    var changeOpenOk = function () {
      $scope.openOk = true;
    }

  });
