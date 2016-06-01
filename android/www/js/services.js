angular.module('ionicApp.services', [])
  .factory('sharedProperties', function ($http, $q, $timeout, $httpParamSerializerJQLike, $ionicPopup) {
    var LATITUDE;
    var LONGITUDE;
    var ACCURACY = "1000";
    var USER_ID;
    var APP_ID = "***************************";
    var STAR_PAGE_NUM = "1";
    var CUR_POWER = 0;
    var MAX_REQUESTS = 20;

    /* POST */
    var UPDATE_LOCATION_URL = 'https://api.rayzit.com/user/update';
    var CREATE_RAYZ_URL = 'https://api.rayzit.com/rayz/create';
    var RERAYZ_URL = 'https://api.rayzit.com/rayz/rerayz';
    var STAR_RAYZ_URL = 'https://api.rayzit.com/rayz/star';
    var UNSTAR_RAYZ_URL = 'https://api.rayzit.com/rayz/star/delete';
    var RAYZ_REPLIES_URL = 'https://api.rayzit.com/rayz/replies';
    var REPORT_RAYZ_URL = 'https://api.rayzit.com/rayz/report';
    var CREATE_RAYZ_REPLY_URL = 'https://api.rayzit.com/rayz/reply';
    var POWERUP_RAYZ_URL = 'https://api.rayzit.com/rayz/reply/powerup';
    var POWERDOWN_RAYZ_URL = 'https://api.rayzit.com/rayz/reply/powerdown';
    var REPORT_RAYZ_REPLY_URL = 'https://api.rayzit.com/rayz/reply/report';


    var position = {latitude: "35.143449", longitude: "33.406699 "}; // coordinates of ucy
    var all_distance = ["unlimited", "0.5 km", "5 km", "50 km", "500 km", "5000 km"];
    var MAX_NEARBY_RAYZS = 20;

    var rayz_replies = {};
    var rayzId_reply;

    var createUserID = function () {
      USER_ID = window.localStorage.getItem("USER_ID");

      if (USER_ID === null) {
        var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var newuserID = "";

        for (var i = 0; i < 64; i++)
          newuserID += possible.charAt(Math.floor(Math.random() * possible.length));

        window.localStorage.setItem("USER_ID", newuserID);
        USER_ID = newuserID;
      }
    };

    var updateLocation = function (requests) {
      var deferred = $q.defer();
      var counter = 0;
      var updateLocation_info = {
        "userId": USER_ID,
        "appId": APP_ID,
        "latitude": LATITUDE + "",
        "longitude": LONGITUDE + "",
        "accuracy": ACCURACY
      };

      // POST: UPDATE LOCATION OF USER
      var request = function () {
        $http.post(UPDATE_LOCATION_URL, updateLocation_info)
          .success(function (response) {
            deferred.resolve();
          })
          .error(function () {
            if (requests === "max-requests") {
              if (counter < MAX_REQUESTS) {
                request();
                counter++;
              } else {
                deferred.reject("Could not update location after multiple tries");
              }
            } else {
              request();
            }
          });
      };
      request();

      setlivefeedURL();
      setStarredURL();
      setNearbyURL();
      setPowerURL();
      setMyRayzsURL();

      return deferred.promise;
    };

    var getRayzAnswers = function (response, requests) {

      var all_data = [];
      var promises = [];

      for (var i = 0; i < response.counter; i++) {
        all_data[i] = $httpParamSerializerJQLike({"userId": USER_ID, "rayzId": response.liveFeed[i].rayzId});
      }


      // POST: GET REPLIES OF RAYZS
      angular.forEach(all_data, function (data) {
        var deferred = $q.defer();

        var counter = 0;

        var request = function () {

          $http.post(RAYZ_REPLIES_URL, data, {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get rayzs answers after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        promises.push(deferred.promise);

      });

      return $q.all(promises);
    };


    var getMaxDistance = function () {
      for (var i = 0; i < all_distance.length; i++) {
        if (window.localStorage.getItem(all_distance[i]) !== null) {
          if (all_distance[i] === "unlimited") {
            return "0";
          } else if (all_distance[i] === "0.5 km") {
            return "0.5";
          } else if (all_distance[i] === "5 km") {
            return "5";
          } else if (all_distance[i] === "50 km") {
            return "50";
          } else if (all_distance[i] === "500 km") {
            return "500";
          } else { // all_distance[i] === "5000 km"
            return "5000";
          }
        }
      }

      return "0";
    };

    /* GET */
    var setlivefeedURL = function () {
      LIVEFEED_URL = 'https://api.rayzit.com/user/' + USER_ID + '/livefeed';
    };
    var setStarredURL = function () {
      STARRED_RAYZ_URL = 'https://api.rayzit.com/user/' + USER_ID + '/starred/' + STAR_PAGE_NUM;
    };
    var setNearbyURL = function () {
      LIVEFEED_NEAREST_URL = 'http://api.rayzit.com/user/' + USER_ID + '/livefeedNearest';
    };
    var setPowerURL = function () {
      POWER_URL = 'http://api.rayzit.com/user/' + USER_ID + '/power';
    };

    var setMyRayzsURL = function () {
      MY_RAYZS_URL = 'http://api.rayzit.com/user/' + USER_ID + '/myrayz/' + STAR_PAGE_NUM;
    };


    return {
      getCurrentLocation: function (requests) {
        var deferred = $q.defer();
        var loc = false;

        var options = {timeout: 15000, enableHighAccuracy: true};
        var auto_location = window.localStorage.getItem("Location");

        if (auto_location === false) {
          //get location from storage
          LATITUDE = window.localStorage.getItem("latitude");
          LONGITUDE = window.localStorage.getItem("longitude");

          if (LATITUDE === null || LONGITUDE === null) { // a location not exist in storage
            window.localStorage.setItem("latitude", position.latitude);
            window.localStorage.setItem("latitude", position.longitude);
            LATITUDE = position.latitude;
            LONGITUDE = position.longitude;
          }

          createUserID();

          updateLocation(requests)
            .then(function () {
              deferred.resolve();
            });

        } else {
          //get current Location
          function success(pos) {
            loc = false;
            //save current location to vars
            LATITUDE = pos.coords.latitude;
            LONGITUDE = pos.coords.longitude;

            //saveCurrentLocation(storage)
            window.localStorage.setItem("latitude", pos.coords.latitude);
            window.localStorage.setItem("longitude", pos.coords.longitude);

            createUserID();
            updateLocation(requests)
              .then(function () {
                deferred.resolve();
              });
          }

          function error(err) {
            if (auto_location === true) {
              //get location from storage
              LATITUDE = window.localStorage.getItem("latitude");
              LONGITUDE = window.localStorage.getItem("longitude");

              if (LATITUDE === null || LONGITUDE === null) { // a location not exist in storage
                window.localStorage.setItem("latitude", position.latitude);
                window.localStorage.setItem("longitude", position.longitude);
                LATITUDE = position.latitude;
                LONGITUDE = position.longitude;
              }

              createUserID();
              updateLocation(requests)
                .then(function () {
                  deferred.resolve();
                });
            } else {
              loc = true;
              $ionicPopup.alert({
                cssClass: 'error',
                title: "Location services are disabled on your device. Enable location services and then try again.",
                okText: '<b>Ok</b>'
              }).then(function (result) {
                ionic.Platform.exitApp();
              });
            }
          }

          do {
            navigator.geolocation.getCurrentPosition(success, error, options);
          }while(loc);

        }

        return deferred.promise;
      },


      getLivefeed: function (requests) {
        var deferred = $q.defer();
        var userId = {
          "userid": USER_ID
        };
        var counter = 0;

        // GET: Livefeed with replies
        var request = function () {
          $http.get(LIVEFEED_URL, userId)
            .success(function (response) {
              getRayzAnswers(response, requests).then(
                function (all_replies) {
                  for (var i = 0; i < response.counter; i++) {
                    response.liveFeed[i].answers = all_replies[i].replies.length;
                    response.liveFeed[i].replies = all_replies[i].replies;
                    response.liveFeed[i].replies.sort(function (a, b) {
                      return parseInt(a.timestamp) - parseInt(b.timestamp);
                    });
                  }

                  $timeout(function () {
                    deferred.resolve(response);
                  });
                }
              )
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get livefeed after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      getStarred: function (requests) {

        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userid": USER_ID,
          "page": STAR_PAGE_NUM + ""
        };

        // GET: Starred with replies
        var request = function () {
          $http.get(STARRED_RAYZ_URL, data)
            .success(function (response) {
              response.liveFeed = response.rayzFeed;
              delete response.rayzFeed;

              getRayzAnswers(response, requests).then(
                function (all_replies) {
                  for (var i = 0; i < response.counter; i++) {
                    response.liveFeed[i].answers = all_replies[i].replies.length;
                    response.liveFeed[i].replies = all_replies[i].replies;
                    response.liveFeed[i].replies.sort(function (a, b) {
                      return parseInt(a.timestamp) - parseInt(b.timestamp);
                    });
                  }

                  response.liveFeed.sort(function (a, b) {
                    return parseInt(b.timestamp) - parseInt(a.timestamp);
                  });

                  $timeout(function () {
                    deferred.resolve(response);
                  });
                }
              )
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get starred rayzs after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      getNearby: function (requests) {
        var counter = 0;
        var deferred = $q.defer();
        var data = {
          "userid": USER_ID
        };

        // GET: Nearby with replies
        var request = function () {
          $http.get(LIVEFEED_NEAREST_URL, data)
            .success(function (response) {

              getRayzAnswers(response, requests).then(
                function (all_replies) {
                  for (var i = 0; i < response.counter; i++) {
                    response.liveFeed[i].answers = all_replies[i].replies.length;
                    response.liveFeed[i].replies = all_replies[i].replies;
                    response.liveFeed[i].replies.sort(function (a, b) {
                      return parseInt(a.timestamp) - parseInt(b.timestamp);
                    });
                  }

                  $timeout(function () {
                    deferred.resolve(response);
                  });

                }
              )

            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get nearby rayzs after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      starRayz: function (rayzId, requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzId": rayzId
        };

        // POST: STAR A RAYZ
        var request = function () {
          $http.post(STAR_RAYZ_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not star a rayz after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      unstarRayz: function (rayzId, requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzId": rayzId
        };

        // POST: UNSTAR A RAYZ
        var request = function () {
          $http.post(UNSTAR_RAYZ_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not unstar a rayz after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      rerayz: function (rayzId, requests) {
        var deferred = $q.defer();
        var max_distance = getMaxDistance();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzId": rayzId,
          "latitude": LATITUDE + "",
          "longitude": LONGITUDE + "",
          "accuracy": ACCURACY,
          "maxDistance": max_distance
        };

        // POST: RERAYZ
        var request = function () {
          $http.post(RERAYZ_URL, data)
            .success(function (response) {
              deferred.resolve(response);
              CUR_POWER = response.power;
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not rerayz a rayz after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();
        return deferred.promise;
      },

      getPower: function (requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userid": USER_ID
        };

        // GET: POWER
        var request = function () {
          $http.get(POWER_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get power after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      getMyRayzs: function (requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userid": USER_ID,
          "page": STAR_PAGE_NUM + ""
        };

        // GET: My Rayzs
        var request = function () {
          $http.get(MY_RAYZS_URL, data)
            .success(function (response) {

              var latest_rayz_pos = -1;
              var latest_timestamp = -1;

              for (var i = 0; i < response.counter; i++) {
                var num = Number(response.myRayz[i].timestamp);
                if (latest_timestamp < num) {
                  latest_timestamp = num;
                  latest_rayz_pos = i;
                }
              }

              response.counter = 1;
              var rayz = response.myRayz[latest_rayz_pos];
              response.myRayz = [];
              response.myRayz[0] = rayz;
              response.liveFeed = response.myRayz;

              getRayzAnswers(response, requests).then(
                function (all_replies) {
                  response.myRayz[0].answers = all_replies[0].replies.length;
                  response.myRayz[0].replies = all_replies[0].replies;

                  delete response.liveFeed;
                  $timeout(function () {
                    deferred.resolve(response.myRayz[0]);
                  });

                }
              );

            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get my rayzs after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      createRayz: function (message) {
        var deferred = $q.defer();
        var max_distance = getMaxDistance();
        var counter = 0;

        var data = $httpParamSerializerJQLike({
          "userId": USER_ID + "",
          "rayzMessage": message + "",
          "latitude": LATITUDE + "",
          "longitude": LONGITUDE + "",
          "accuracy": ACCURACY + "",
          "maxDistance": max_distance + ""
        });


        // POST: CREATE A RAYZ
        var request = function (requests) {
          $http.post(CREATE_RAYZ_URL, data, {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function (response) {
              deferred.resolve(response);
              CUR_POWER = response.power;
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not create rayz after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;

      },

      postRayzReplies: function (rayzId, requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzId": rayzId
        };

        // POST: RAYZ REPLIES
        var request = function () {
          $http.post(RAYZ_REPLIES_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not get rayzs replies after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      reportRayz: function (rayzId, requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzId": rayzId
        };

        // POST: REPORT A RAYZ
        var request = function () {
          $http.post(REPORT_RAYZ_URL, data)
		    .success(function (response) {
                   deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not report a rayz after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      createRayzReply: function (rayzId, message, requests) {
        var deferred = $q.defer();
        var counter = 0;

        var data = $httpParamSerializerJQLike({
          "userId": USER_ID,
          "rayzId": rayzId + "",
          "rayzReplyMessage": message
        });

        // POST: CREATE A RAYZ REPLY
        var request = function () {
          $http.post(CREATE_RAYZ_REPLY_URL, data, {
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function (response) {
              deferred.resolve(response);
              CUR_POWER = response.power;
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not create rayz reply after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      powerUpRayzReply: function (rayzReplyId, requests) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzReplyId": rayzReplyId,
        };

        // POST: POWER UP A RAYZ REPLY
        var request = function () {
          $http.post(POWERUP_RAYZ_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not power up a reply after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      powerDownRayzReply: function (rayzReplyId) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzReplyId": rayzReplyId,
        };

        // POST: POWER DOWN A RAYZ REPLY
        var request = function () {
          $http.post(POWERDOWN_RAYZ_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not power down a reply after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      reportReply: function (rayzReplyId) {
        var deferred = $q.defer();
        var counter = 0;
        var data = {
          "userId": USER_ID,
          "rayzReplyId": rayzReplyId,
        };

        // POST: REPORT A REPLY
        var request = function () {
          $http.post(REPORT_RAYZ_REPLY_URL, data)
            .success(function (response) {
              deferred.resolve(response);
            })
            .error(function () {
              if (requests === "max-requests") {
                if (counter < MAX_REQUESTS) {
                  request();
                  counter++;
                } else {
                  deferred.reject("Could not report a reply after multiple tries");
                }
              } else {
                request();
              }
            });
        };
        request();

        return deferred.promise;
      },

      getMaxNearbyRayzs: function () {
        return MAX_NEARBY_RAYZS;
      },

      getCurPower: function () {
        return CUR_POWER;
      },

      setCurPower: function (power) {
        CUR_POWER = power;
      },


      setRayzId_Replies: function (livefeed_rayz) {
        rayz_replies = livefeed_rayz;
      },

      getRayzId_Replies: function () {
        return rayz_replies;
      },

      setRayzId_Reply: function (rayzId) {
        rayzId_reply = rayzId;
      },

      getRayzId_Reply: function () {
        return rayzId_reply;
      },

      display_time: function (msgtime) {

        var time = new Date(msgtime);
        //time = new Date(time.getUTCFullYear(), time.getUTCMonth(), time.getUTCDate(), time.getUTCHours(), time.getUTCMinutes(), time.getUTCSeconds());
        var diff_hours = Math.abs(time.getUTCHours() - new Date(msgtime).getUTCHours());
        var diff = diff_hours * 3600;

        time = Math.abs(new Date().getTime() - time.getTime());
        time = Math.floor(time / 1000);
        time = Math.abs(time - diff);
        time = time - 50;
        if (time < 0)time = 0;

        if (time < 60) { // seconds
          if (time < 2) time = "a second ago";
          else time = time + " seconds ago";
        } else if ((time / 60) < 60) { // minutes
          time = Math.floor(time / (60));
          if (time < 2) time = "a minute ago";
          else time = time + " minutes ago";
        } else if ((time / (60 * 60)) < 24) { // hours
          time = Math.floor(time / (60 * 60));
          if (time < 2) time = "an hour ago";
          else time = time + " hours ago";
        } else if ((time / (60 * 60 * 24)) < 7) { // days
          time = Math.floor(time / (60 * 60 * 24));
          if (time < 2) time = "a day ago";
          else time = time + " days ago";
        } else if ((time / (60 * 60 * 24 * 7)) < 5) { // weeks
          time = Math.floor(time / (60 * 60 * 24 * 7));
          if (time < 2) time = "a week ago";
          else time = time + " weeks ago";
        } else if ((time / (60 * 60 * 24 * 7 * 5)) < 12) { // months
          time = new Date(msgtime);
          time = new Date(time.getUTCFullYear(), time.getUTCMonth(), time.getUTCDate(), Math.abs(time.getUTCHours() - diff_hours), time.getUTCMinutes(), time.getUTCSeconds());

          time = time.getDate() + " " + time.toUTCString().split(' ')[2] + " " + time.toUTCString().split(' ')[4];
        } else { // years
          time = new Date(msgtime);
          time = new Date(time.getUTCFullYear(), time.getUTCMonth(), time.getUTCDate(), Math.abs(time.getUTCHours() - diff_hours), time.getUTCMinutes(), time.getUTCSeconds());

          time = time.getDate() + " " + time.toUTCString().split(' ')[2] + " " + time.getFullYear() + " " + time.toUTCString().split(' ')[4];
        }

        return time;
      },

      getRayzDistance: function (rayzDistance) {
        if (rayzDistance === 0) {
          rayzDistance = "unlimited";
        } else if (rayzDistance === 0.5) {
          rayzDistance = "0.5 km";
        } else if (rayzDistance === 5) {
          rayzDistance = "5 km";
        } else if (rayzDistance === 50) {
          rayzDistance = "50 km";
        } else if (rayzDistance === 500) {
          rayzDistance = "500 km";
        } else { // rayzDistance === 5000
          rayzDistance = "5000 km";
        }
        return rayzDistance;
      },

      getUserMaxDistance: function () {
        var rayzDistance = getMaxDistance();

        if (rayzDistance === "0") {
          rayzDistance = "unlimited";
        } else if (rayzDistance === "0.5") {
          rayzDistance = "0.5 km";
        } else if (rayzDistance === "5") {
          rayzDistance = "5 km";
        } else if (rayzDistance === "50") {
          rayzDistance = "50 km";
        } else if (rayzDistance === "500") {
          rayzDistance = "500 km";
        } else { // rayzDistance === 5000
          rayzDistance = "5000 km";
        }

        return rayzDistance;
      }


    };
  });
