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

//
//  RemoteStore.h
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 20/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <MagicalRecord/CoreData+MagicalRecord.h>
#import <RestKit/RestKit.h>
#import <RestKit/CoreData.h>

#import <CocoaLumberjack/DDASLLogger.h>
#import <CocoaLumberjack/DDTTYLogger.h>
#import "LumberjackFormatter.h"
#import "LumberjackSettings.h"

#import "ApiConstants.h"

@class User, Rayz, RayzReply;

#ifdef DEBUG
#ifdef DEBUG_LOCALHOST
static NSString * const kApiUrl = @"http://10.16.20.5:9000"; // My URL
#else
static NSString * const kApiUrl = @"http://dev.rayzit.com:9000"; //dev URL
#endif
#else
#ifdef TESTFLIGHT
static NSString * const kApiUrl = @"https://api.rayzit.com"; // test prod URL
#elif DEV_TESTFLIGHT
static NSString * const kApiUrl = @"http://dev.rayzit.com:9000"; // dev URL
#else
static NSString * const kApiUrl = @"https://api.rayzit.com"; //production URL
#endif
#endif

@class User;

@interface RemoteStore : NSObject <UIAlertViewDelegate>

@property (assign, nonatomic, getter = isConnected) BOOL connected;
@property (assign, nonatomic, getter = isShowingAlert) BOOL showingAlert;


+ (instancetype)sharedInstance;

+ (void)resetStore;
+ (void)resetStoreAndConfigureRestKit;
+ (void)save;


// Api Requests
+ (void)updateUserLocation:(User *)user;
+ (void)getLiveFeedForUser:(User *)user;
+ (void)getNearbyFeedForUser:(User *)user;
+ (void)getUserPower:(User *)user;
+ (void)getUserRayzs:(User *)user;
+ (void)getUserStarredRayzs:(User *)user;
+ (void)postCreateRayz:(Rayz *)rayz;
+ (void)postRerayz:(Rayz *)rayz;
+ (void)deleteRayz:(Rayz *)rayz;
+ (void)starRayz:(Rayz *)rayz;
+ (void)unstarRayz:(Rayz *)rayz;
+ (void)reportRayz:(Rayz *)rayz;
+ (void)getRayzAnswers:(Rayz *)rayz;
+ (void)getRayzReplies:(Rayz *)rayz;
+ (void)postCreateRayzReply:(RayzReply *)rayzReply;
+ (void)powerUpRayzReply:(RayzReply *)rayzReply;
+ (void)powerDownRayzReply:(RayzReply *)rayzReply;
+ (void)reportRayzReply:(RayzReply *)rayzReply;

+ (void)checkRayzs;

// Reachability tests
+ (void)checkConnectionWithConnectionBlock:(void (^)(void))connectionBlock noConnectionBlock:(void (^)(void))noConnectionBlock;
+ (void)checkConnectionWithConnectionBlock:(void (^)(void))connectionBlock;
+ (void)checkConnectionWithNoConnectionBlock:(void (^)(void))noConnectionBlock;

@end
