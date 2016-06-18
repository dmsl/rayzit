//
//  ApiConstants.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 21/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "ApiConstants.h"

#pragma mark Api
NSString * const kApiStoreLocalizationTable = @"ApiStore";
NSString * const kApiNoConnection = @"ApiNoConnection";
NSString * const kApiReachabilityChangedNotification = @"ApiReachabilityChangedNotification";

/**
 * Copyright (c) 2016 Data Management Systems Laboratory, University of Cyprus
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

#pragma mark ApiStatus
NSString * const kApiStatusSuccess = @"success";
NSString * const kApiStatusFailure = @"error";
NSString * const kApiStatusDeleted = @"deleted";

#pragma mark ApiRayz Status
NSString * const kApiRayzStatusPending = @"pending";
NSString * const kApiRayzStatusRayzed = @"rayzed";
NSString * const kApiRayzStatusFailed = @"failed";

#pragma mark Attachment Types
NSString * const kApiAttachmentTypeImage = @"images";
NSString * const kApiAttachmentTypeVideo = @"videos";
NSString * const kApiAttachmentTypeAudio = @"audio";

#pragma mark Distance Attribute Names
NSString * const kApiDistanceValue = @"ApiDistanceValue";
NSString * const kApiDistanceKilometersName = @"ApiDistanceKilometersName";
NSString * const kApiDistanceMilesName = @"ApiDistanceMilesName";

#pragma mark Notifications
NSString * const kUserAdded = @"UserAddedNotification";
NSString * const kUserLocationUpdated = @"UserLocationUpdatedNotification";
NSString * const kUserUpdateFailed = @"UserUpdateFailedNotification";
NSString * const kInvalidAppId = @"InvalidAppIdSpecified";
NSString * const kBlockedUser = @"UserBlockedNotification";
NSString * const kLiveFeedLoaded = @"LiveFeedLoadedNotification";
NSString * const kLiveFeedFailedToLoad = @"LiveFeedFailedToLoadNotification";
NSString * const kNearbyFeedLoaded = @"NearbyFeedLoadedNotification";
NSString * const kNearbyFeedFailedToLoad = @"NearbyFeedFailedToLoadNotification";
NSString * const kUserRayzsLoaded = @"UserRayzsLoadedNotification";
NSString * const kUserRayzsFailedToLoad = @"UserRayzsFailedToLoadNotification";
NSString * const kUserStarredRayzsLoaded = @"UserStarredRayzsLoadedNotification";
NSString * const kUserStarredRayzsFailedToLoad = @"UserStarredRayzsFailedToLoadNotification";
NSString * const kRayzCreated = @"NewRayzCreatedNotification";
NSString * const kRayzFailed = @"NewRayzFailedNotification";
NSString * const kRayzReplyCreated = @"NewRayzReplyCreatedNotification";
NSString * const kRayzReplyFailed = @"NewRayzReplyFailedNotification";
NSString * const kApiRayzDeleted = @"RayzDeletedNotification";