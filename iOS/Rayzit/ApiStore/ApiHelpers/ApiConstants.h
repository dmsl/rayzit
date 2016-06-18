//
//  ApiConstants.h
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 21/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#pragma mark Api
extern NSString * const kApiStoreLocalizationTable;
extern NSString * const kApiNoConnection;
extern NSString * const kApiReachabilityChangedNotification;

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
extern NSString * const kApiStatusSuccess;
extern NSString * const kApiStatusFailure;
extern NSString * const kApiStatusDeleted;

#pragma mark Rayz Status
extern NSString * const kApiRayzStatusPending;
extern NSString * const kApiRayzStatusRayzed;
extern NSString * const kApiRayzStatusFailed;

#pragma mark Attachment Types
extern NSString * const kApiAttachmentTypeImage;
extern NSString * const kApiAttachmentTypeVideo;
extern NSString * const kApiAttachmentTypeAudio;

#pragma mark Distance Attribute Names
extern NSString * const kApiDistanceValue;
extern NSString * const kApiDistanceKilometersName;
extern NSString * const kApiDistanceMilesName;

#pragma mark Notifications
extern NSString * const kUserAdded;
extern NSString * const kUserLocationUpdated;
extern NSString * const kUserUpdateFailed;
extern NSString * const kInvalidAppId;
extern NSString * const kBlockedUser;
extern NSString * const kLiveFeedLoaded;
extern NSString * const kLiveFeedFailedToLoad;
extern NSString * const kNearbyFeedLoaded;
extern NSString * const kNearbyFeedFailedToLoad;
extern NSString * const kUserRayzsLoaded;
extern NSString * const kUserRayzsFailedToLoad;
extern NSString * const kUserStarredRayzsLoaded;
extern NSString * const kUserStarredRayzsFailedToLoad;
extern NSString * const kRayzCreated;
extern NSString * const kRayzFailed;
extern NSString * const kRayzReplyCreated;
extern NSString * const kRayzReplyFailed;
extern NSString * const kApiRayzDeleted;