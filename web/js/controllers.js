angular.module('ionicApp.controllers', [])

  .controller('MainCtrl', function ($ionicPopover, $rootScope, $state, $scope, ionicMaterialInk, ionicMaterialMotion, $timeout, $ionicPopup,$ionicModal, sharedProperties) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);
     $rootScope.livefeed = [];
     $rootScope.starredRayzs = [];
     $rootScope.nearbyRayzs = [];
     $rootScope.rayz = {};
     $rootScope.power;
     $rootScope.spinner1 = true;
     $rootScope.spinner2 = true;
     $rootScope.spinner3 = true;
     $rootScope.data = {selectDistance: null};

     window.location = "index.html#/tab/livefeed";
     $scope.APP_STARTING = true;
     $scope.NEW_RAYZ = false;

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
                            $rootScope.spinner1 = false;
                            var spinnerToHide1 = document.getElementsByClassName("spinner1");
                            if (spinnerToHide1.length > 0)
                               spinnerToHide1[0].style.visibility = "hidden";

                            $rootScope.spinner3 = false;
                            var spinnerToHide3 = document.getElementsByClassName("spinner3");
                            if (spinnerToHide3.length > 0)
                               spinnerToHide3[0].style.visibility = "hidden";
                            $scope.NEW_RAYZ = true;

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
                                 var spinnerToHide2 = document.getElementsByClassName("spinner2");
                                 if (spinnerToHide2.length > 0)
                                    spinnerToHide2[0].style.visibility = "hidden";

                                 $scope.APP_STARTING = false;
								 $timeout(updateAuto, 120000);
                              })
                         })

                    })
               })

          })
     }

     var updateAuto = function () {
        // getPower
        sharedProperties.getPower("no-limit")
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
                   responseS.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseS.liveFeed[i].replies[j].timestamp);
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
                             responseN.liveFeed[i].replies[j].timestamp = sharedProperties.display_time(responseN.liveFeed[i].replies[j].timestamp);
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

        $timeout(updateAuto, 120000); // 2 min, 12000
     };
	 
	 
	 $scope.modalSettings = null;
	 $scope.modalNewRayz = null;
	 $scope.modalNewReply = null;
	 $scope.modalReplies = null;
	 
	 $scope.openModal = function(destination) {
		// Settings
		if(destination === "settings"){
			if($scope.modalSettings === null){
				$ionicModal.fromTemplateUrl('templates/modalSettings.html', {
					scope: $scope
				 }).then(function(modal) {
					$scope.modalSettings = modal;
					$scope.modalSettings.show();
				 });
			}else{
			   $scope.modalSettings.show();
			}
		}else if(destination === "replies"){
            if($scope.modalReplies === null){
				$ionicModal.fromTemplateUrl('templates/modalReplies.html', {
					scope: $scope
				 }).then(function(modal) {
					$scope.modalReplies = modal;
					$scope.modalReplies.show();
				 });
			}else{
			   $scope.modalReplies.show();
			}    			
		}else{
			if ($scope.NEW_RAYZ) {
			   // New Reply
			   if (destination === "new-reply") {
				  sharedProperties.setRayzId_Reply($rootScope.rayz.rayzId);				  
				  if($scope.modalNewReply === null){
					 $ionicModal.fromTemplateUrl('templates/modalNewReply.html', {
						scope: $scope
					 }).then(function(modal) {
						$scope.modalNewReply = modal;
						$scope.modalNewReply.show(); // new reply 
					 }); 
				  }else{
					 $scope.modalNewReply.show(); // new reply 
				  }
			   // New Rayz  
			   }else{
				  if($scope.modalNewRayz === null){
					$ionicModal.fromTemplateUrl('templates/modalNewRayz.html', {
						scope: $scope
					 }).then(function(modal) {
						$scope.modalNewRayz = modal;
						$scope.modalNewRayz.show(); // new rayz
					});
				  }else{
					$scope.modalNewRayz.show(); // new rayz
				  } 
			   }
			}
		}
	
     };
	 
	 
	 var template = '<div style="position:relative; right:25px; ">' +
       '            <ion-popover-view style="height:40px!important; width: 100px;!important;border-radius: 5px;">' +
       '                <ion-content scroll="false"> ' +
       '                   <div class="list" style="position:relative; bottom:10px;"> ' +
       '                      <a class="item" ng-click="closePopover()">' +
       '<img src="img/report-blue.png" height="20px" width="20px">' +
       '                         <i class="icon report-blue-icon" style="height:25px!important;width:25px;!important;"></i> ' +
       '                         <span class="actionsheet_letters" style="position:relative;bottom:4px; color:#007AFF"> Report </span>' +
       '                     </a>' +
       '                   </div>' +
       '                </ion-content>' +
       '            </ion-popover-view>' +
       '          </div>';
	   
	   var template2 = '<div style="position:relative; right:25px; ">' +
       '            <ion-popover-view style="height:40px!important; width: 100px;!important;border-radius: 5px;">' +
       '                <ion-content scroll="false"> ' +
       '                   <div class="list" style="position:relative; bottom:10px;"> ' +
       '                      <a class="item" ng-click="closePopover()">' +
       '<img src="img/report-blue.png" height="20px" width="20px">' +
       '                         <i class="icon report-blue-icon" style="height:25px!important;width:25px;!important;"></i> ' +
       '                         <span class="actionsheet_letters" style="position:relative;bottom:4px; color:#007AFF"> Report </span>' +
       '                     </a>' +
       '                   </div>' +
       '                </ion-content>' +
       '            </ion-popover-view>' +
       '          </div>';

     $scope.popover = $ionicPopover.fromTemplate(template, {
        scope: $scope
     });

	 $scope.popover2 = $ionicPopover.fromTemplate(template2, {
        scope: $scope
     });
	 
     $scope.openPopover = function ($event, rayzId, templ) {
        $scope.tmp = {'id': rayzId, 'templ': templ};
		if(templ === "rayzId")$scope.popover.show($event);
	    else $scope.popover2.show($event);
     };
	 
     $scope.closePopover = function () {
        if ($scope.tmp.templ === "rayzId") {
           sharedProperties.reportRayz($scope.tmp.id, "max-requests").then(function (response) {
			  if(response.message === "Cannot report your own Rayz."){
				$ionicPopup.alert({
					title: "<b>Report Rayz Failed</b>",
					content: "Cannot report your own Rayz.",
					okText: '<b>Ok</b>',
					okType: 'button-royal'
			    })
			  }else{
				$scope.changeRayzs_Report($scope.tmp.id);
			  }
		   })
        } else {
           sharedProperties.reportReply($scope.tmp.id, "max-requests").then(function (response) {
			   if(response.message === "Cannot report your own Rayz Reply."){
				  $ionicPopup.alert({
					  title: "<b>Report Reply Failed</b>",
					  content: "Cannot report your own Reply.",
					  okText: '<b>Ok</b>',
					  okType: 'button-royal'
			      })
			  }else{
				$scope.changeReplies_Report($scope.tmp.id);
			  }
		   })
        }
        $scope.popover.hide();
		$scope.popover2.hide();
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
		$scope.openModal("replies");
     };

  })


  .
  controller('LivefeedCtrl', function ($rootScope, $window, $state, $ionicLoading, $http, $scope, ionicMaterialInk, ionicMaterialMotion, $timeout, sharedProperties) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);

     if (!$rootScope.spinner1) {
        var spinnerToHide1 = document.getElementsByClassName("spinner1");
        spinnerToHide1[0].style.visibility = "hidden";
     }

  })


  .controller('StarredCtrl', function ($rootScope, $scope, ionicMaterialInk, ionicMaterialMotion, $timeout) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);

     if (!$rootScope.spinner3) {
        var spinnerToHide3 = document.getElementsByClassName("spinner3");
        spinnerToHide3[0].style.visibility = "hidden";
     }
  })

  .controller('NearbyCtrl', function ($rootScope, $scope, ionicMaterialInk, ionicMaterialMotion, $timeout) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);

     if (!$rootScope.spinner2) {
        var spinnerToHide2 = document.getElementsByClassName("spinner2");
        spinnerToHide2[0].style.visibility = "hidden";
     }
  })

  .controller('NewRayzCtrl', function ($ionicPopup, $window,$ionicLoading,$rootScope, $scope, $state, ionicMaterialInk, ionicMaterialMotion, $timeout, sharedProperties) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);
	 
	sharedProperties.getPower("max-requests")
	  .then(function (responseP) {
	    sharedProperties.setCurPower(responseP.power);
	    $rootScope.power = responseP.power;
	    $scope.power_width = $rootScope.power;
	    $scope.powerBarColor();
	    var spinnerToHide4 = document.getElementsByClassName("spinner4");
	    if (spinnerToHide4.length > 0)
		  spinnerToHide4[0].style.visibility = "hidden";
	});
	   
	$scope.$watch('power', function(newVal, oldVal){
	  $scope.powerBarColor();
	}, true);

	$scope.powerBarColor = function(){
	  if($rootScope.power >= 66 ){
		  document.getElementById("progressBar").style.backgroundColor = "#33cd5f";
	  }else if($rootScope.power >= 33){
		  document.getElementById("progressBar").style.backgroundColor = "#ffc900";
	  }else{
		  document.getElementById("progressBar").style.backgroundColor = "#A80700";
	  } 
	}
      	 
     if ($rootScope.data.selectDistance === null) {
        $rootScope.data.selectDistance = sharedProperties.getUserMaxDistance();
     }

     $scope.selectSendDistance = $rootScope.data.selectDistance;
     $scope.updateSendDistance = function(distance){
        $scope.selectSendDistance = distance; // send this distance
     };
     var all_distance = ["unlimited", "0.5 km", "5 km", "50 km", "500 km", "5000 km"];

     $scope.messageModel = "";
     $scope.power_width = $rootScope.power;

	 $scope.clearRayzArea = function(){
		document.getElementById("messageArea").value = "";
		document.getElementById("rayz-temp-distance").value = $rootScope.data.selectDistance;
		$scope.selectSendDistance = $rootScope.data.selectDistance;
	 }

     $scope.sendRayz = function (messageModel) {  
        messageModel = messageModel.replace(/^\s+/, "");
        if (messageModel !== "" && $scope.power_width >= 2) {
           
		   var spinnerToHide4 = document.getElementsByClassName("spinner4");
	       if (spinnerToHide4.length > 0)
		     spinnerToHide4[0].style.visibility = "visible";
		 
           sharedProperties.createRayz(messageModel, $scope.selectSendDistance)
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

                             if (spinnerToHide4.length > 0)
		                        spinnerToHide4[0].style.visibility = "hidden";
                             
							 $scope.clearRayzArea();
							 $scope.modalNewRayz.hide();
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

  .controller('RepliesCtrl', function ($ionicPopup, $rootScope, $rootScope, $scope, $timeout, sharedProperties, ionicMaterialInk, ionicMaterialMotion) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);

     $scope.powerUp = function (replyId) {
        if (sharedProperties.getCurPower() >= 4) {
           var l, n, s;
		   l = n = s = -1;
		   var l_pos, n_pos, s_pos, r_pos;
		   l_pos = n_pos = s_pos = r_pos = -1;
		   
           for (var i = 0; i < $rootScope.livefeed.length; i++) {
              if ($rootScope.livefeed[i].rayzId === $rootScope.rayz.rayzId) {
                 for (var pos = 0; pos < $rootScope.livefeed[i].replies.length; pos++) {
                    if ($rootScope.livefeed[i].replies[pos].rayzReplyId === replyId) {
                       $rootScope.livefeed[i].replies[pos].upVotes++;
					   l = i;
					   l_pos=pos;
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
					   n = i;
					   n_pos=pos;
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
					   s = i;
					   s_pos=pos;
                       break;
                    }
                 }
                 break;
              }
           }

           for (var pos = 0; pos < $rootScope.rayz.replies.length; pos++) {
              if ($rootScope.rayz.replies[pos].rayzReplyId === replyId) {
                 $rootScope.rayz.replies[pos].upVotes++;
				 r_pos = pos;
                 break;
              }
           }
           
		sharedProperties.powerUpRayzReply(replyId, "max-requests").then(function (response) {
			if(response.message === "Cannot powered-up your own Rayz Reply."){
			   if(l != -1){
				   $rootScope.livefeed[l].replies[l_pos].upVotes--;
			   }
			    if(n != -1){
				   $rootScope.nearbyRayzs[n].replies[n_pos].upVotes--;
			   }
			   if(s != -1){
				   $rootScope.starredRayzs[s].replies[s_pos].upVotes--;
			   }
			   if(r_pos != -1){
				   $rootScope.rayz.replies[r_pos].upVotes--;
			   }
                $ionicPopup.alert({
                  title: "<b>Power Up Failed</b>",
                  content: "Cannot powered-up your own Rayz Reply.",
                  okText: '<b>Ok</b>',
                  okType: 'button-royal'
               })			   
		    }
		});  
		   
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
           var l, n, s;
		   l = n = s = -1;
		   var l_pos, n_pos, s_pos, r_pos;
		   l_pos = n_pos = s_pos = r_pos = -1;
		   
           for (var i = 0; i < $rootScope.livefeed.length; i++) {
              if ($rootScope.livefeed[i].rayzId === $rootScope.rayz.rayzId) {
                 for (var pos = 0; pos < $rootScope.livefeed[i].replies.length; pos++) {
                    if ($rootScope.livefeed[i].replies[pos].rayzReplyId === replyId) {
                       $rootScope.livefeed[i].replies[pos].upVotes--;
					   l = i;
					   l_pos = pos;
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
					   n = i;
					   n_pos = pos;
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
					   s = i;
					   s_pos = pos;
                       break;
                    }
                 }
                 break;
              }
           }

           for (var pos = 0; pos < $rootScope.rayz.replies.length; pos++) {
              if ($rootScope.rayz.replies[pos].rayzReplyId === replyId) {
                 $rootScope.rayz.replies[pos].upVotes--;
				 r_pos = pos;
                 break;
              }
           }

           sharedProperties.powerDownRayzReply(replyId, "max-requests").then(function (response) {
			if(response.message === "Cannot powered down your own Rayz Reply."){
			   if(l != -1){
				   $rootScope.livefeed[l].replies[l_pos].upVotes++;
			   }
			   if(n != -1){
				   $rootScope.nearbyRayzs[n].replies[n_pos].upVotes++;
			   }
			   if(s != -1){
				   $rootScope.starredRayzs[s].replies[s_pos].upVotes++;
			   }
			   if(r_pos != -1){
				   $rootScope.rayz.replies[r_pos].upVotes++;
			   }
               $ionicPopup.alert({
                  title: "<b>Power Down Failed</b>",
                  content: "Cannot powered down your own Rayz Reply.",
                  okText: '<b>Ok</b>',
                  okType: 'button-royal'
               })			   
		    }
		});  
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

  .controller('NewReplyCtrl', function ($ionicPopup, $rootScope, $window, $scope, ionicMaterialInk, ionicMaterialMotion, $timeout, sharedProperties) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);


     sharedProperties.getPower("max-requests")
       .then(function (responseP) {
          sharedProperties.setCurPower(responseP.power);
          $rootScope.power = responseP.power;
          $scope.power_width = $rootScope.power;
		  $scope.powerBarColor();
          var spinnerToHide5 = document.getElementsByClassName("spinner5");
          if (spinnerToHide5.length > 0){
			  spinnerToHide5[0].style.visibility = "hidden";
		  }
             
       });
     $scope.$watch('power', function(newVal, oldVal){
	    $scope.powerBarColor();
	 }, true);

	 $scope.powerBarColor = function(){
	   if($rootScope.power >= 66 ){
		  document.getElementById("progressBar2").style.backgroundColor = "#33cd5f";
	   }else if($rootScope.power >= 33){
		  document.getElementById("progressBar2").style.backgroundColor = "#ffc900";
	   }else{
		  document.getElementById("progressBar2").style.backgroundColor = "#A80700";
	   } 
	 }

     $scope.messageModel = "";
     $scope.power_width = $rootScope.power;
     
	 $scope.clearReplyArea = function(){
		document.getElementById("messageArea2").value = "";
	 }


     $scope.sendRayzReply = function (messageModel) {
        var rayzId = sharedProperties.getRayzId_Reply();

        messageModel = messageModel.replace(/^\s+/, "");
        if (messageModel !== "" && $scope.power_width >= 1) {

           var spinnerToHide5 = document.getElementsByClassName("spinner5");
	       if (spinnerToHide5.length > 0)
		     spinnerToHide5[0].style.visibility = "visible";///////////////////////


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


						if (spinnerToHide5.length > 0)
		                        spinnerToHide5[0].style.visibility = "hidden";                             
						$scope.clearReplyArea();
						$scope.modalNewReply.hide();                
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

                              if (spinnerToHide5.length > 0)
		                        spinnerToHide5[0].style.visibility = "hidden";                             
						      $scope.clearReplyArea();
						      $scope.modalNewReply.hide();
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

                                 if (spinnerToHide5.length > 0)
		                            spinnerToHide5[0].style.visibility = "hidden";                             
								 $scope.clearReplyArea();
								 $scope.modalNewReply.hide();
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


  .controller('SettingsCtrl', function ($scope, ionicMaterialInk, ionicMaterialMotion, $timeout, $rootScope) {
     $timeout(function () {
        ionicMaterialInk.displayEffect();
     }, 0);


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
     $scope.settings_chevron = $scope.dev_width - 23;
     $scope.settings_toggle = $scope.dev_width - 127;

     $scope.settings_check = $scope.dev_width - 44;
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


  });

