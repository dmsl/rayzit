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
//  RemoteStore.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 20/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RemoteStore.h"

// Requests headers
#import "UserRequests.h"
#import "RayzRequests.h"
#import "RayzReplyRequests.h"

#import "Rayz.h"

#import "Reachability.h"
#import "LocationManager.h"

#ifdef DEBUG
    #import <PDDebugger.h>
#endif
#ifndef DEBUG
    #import <Crashlytics/Crashlytics.h>
#endif

static NSInteger kConnectionAlertTag = 10;

@interface NSManagedObjectContext ()
+ (void)MR_setRootSavingContext:(NSManagedObjectContext *)context;
+ (void)MR_setDefaultContext:(NSManagedObjectContext *)moc;
@end

@implementation RemoteStore

+ (instancetype)sharedInstance {
    static dispatch_once_t once;
    static id sharedInstance;
    dispatch_once(&once, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

#pragma mark Initialization
- (id)init {
    if (self = [super init]) {
        [self setConnected:NO];
        [self configureRestKit];
    }
    return self;
}

- (void)configureRestKit {
    
#ifdef DEBUG
    RKLogConfigureByName("App", RKLogLevelTrace);
    RKLogConfigureByName("RestKit", RKLogLevelTrace);
    RKLogConfigureByName("RestKit/CoreData", RKLogLevelInfo);
    RKLogConfigureByName("RestKit/CoreData/Cache", RKLogLevelInfo);
    RKLogConfigureByName("RestKit/Network", RKLogLevelTrace);
    RKLogConfigureByName("RestKit/Network/CoreData", RKLogLevelInfo);
    RKLogConfigureByName("RestKit/ObjectMapping", RKLogLevelInfo);
    RKLogConfigureByName("RestKit/Search", RKLogLevelTrace);
    RKLogConfigureByName("RestKit/Support", RKLogLevelTrace);
    RKLogConfigureByName("RestKit/Testing", RKLogLevelTrace);
    RKLogConfigureByName("RestKit/UI", RKLogLevelTrace);
#else
    RKLogConfigureByName("APP", RKLogLevelOff);
    RKLogConfigureByName("RestKit*", RKLogLevelOff);
#endif
    
    NSString *reachabilityString = [kApiUrl stringByReplacingOccurrencesOfString:@"https://" withString:@""];
    reachabilityString = [reachabilityString stringByReplacingOccurrencesOfString:@"http://" withString:@""];
    
    Reachability *reach;
    
#ifdef DEBUG_LOCALHOST
    reach = [Reachability reachabilityWithHostname:@"google.com"];
#else
    reach = [Reachability reachabilityWithHostname:@"google.com"];
#endif
    
    reach.reachableBlock = ^(Reachability *reachability) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self setConnected:YES];
            [[NSNotificationCenter defaultCenter] postNotificationName:kApiReachabilityChangedNotification object:nil];
            [[LocationManager sharedManager] startLocationUpdates];
#ifndef DEBUG
            [CrashlyticsKit setBoolValue:YES forKey:@"hasInternetConnection"];
#endif
        });
    };
    
    reach.unreachableBlock = ^(Reachability *reachability) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self setConnected:NO];
            [[NSNotificationCenter defaultCenter] postNotificationName:kApiReachabilityChangedNotification object:nil];
            if (![self isShowingAlert]) {
                [self setShowingAlert:YES];
                UIAlertView * alert = [AlertUtils connectionAlertView];
                [alert setDelegate:self];
                [alert setTag:kConnectionAlertTag];
                [alert show];
//                UIAlertView * alert = [AlertUtils alertWithTitle:@"Warning" message:@"Your internet connection was lost." delegate:self];
//                [alert setTag:kConnectionAlertTag];
//                [alert show];
            }
#ifndef DEBUG
            [CrashlyticsKit setBoolValue:NO forKey:@"hasInternetConnection"];
#endif
        });
    };
    
    [reach startNotifier];
    
    
    // initialize AFNetworking HTTPClient
    NSURL *baseURL = [NSURL URLWithString:kApiUrl];
    AFHTTPClient *client = [[AFHTTPClient alloc] initWithBaseURL:baseURL];
    [AFNetworkActivityIndicatorManager sharedManager].enabled = YES;
    //TODO maybe remove this
    [client setAllowsInvalidSSLCertificate:YES];
    
    RKObjectManager *objectManager = [[RKObjectManager alloc] initWithHTTPClient:client];
    
    
    NSDictionary *options = @{
                              NSMigratePersistentStoresAutomaticallyOption : @YES,
                              NSInferMappingModelAutomaticallyOption : @YES
                              };
    
    //    NSURL *modelURL = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"ApiStoreModel" ofType:@"momd"]];
    //NSManagedObjectModel *managedObjectModel = [[[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL] mutableCopy];
    NSManagedObjectModel *managedObjectModel = [NSManagedObjectModel mergedModelFromBundles:nil];
    RKManagedObjectStore *managedObjectStore = [[RKManagedObjectStore alloc] initWithManagedObjectModel:managedObjectModel];
    
    
    [managedObjectStore createPersistentStoreCoordinator];
    NSError *error = nil;
    BOOL success = RKEnsureDirectoryExistsAtPath(RKApplicationDataDirectory(), &error);
    if (!success) {
        DDLogError(@"Failed to create Application Data Directory at path '%@': %@", RKApplicationDataDirectory(), error);
    }
    NSString *storePath = [RKApplicationDataDirectory() stringByAppendingPathComponent:@"ApiStoreModel.sqlite"];
    NSPersistentStore *persistentStore = [managedObjectStore addSQLitePersistentStoreAtPath:storePath fromSeedDatabaseAtPath:nil withConfiguration:nil options:options error:&error];
    NSAssert(persistentStore, @"Failed to add persistent store with error: %@, path: %@, RKPath: %@", error, storePath, RKApplicationDataDirectory());
    [managedObjectStore createManagedObjectContexts];
    
    //    [managedObjectStore setManagedObjectCache:[[RKInMemoryManagedObjectCache alloc] initWithManagedObjectContext:[managedObjectStore persistentStoreManagedObjectContext]]];
    
    [NSPersistentStoreCoordinator MR_setDefaultStoreCoordinator:managedObjectStore.persistentStoreCoordinator];
    [NSManagedObjectContext MR_setRootSavingContext:managedObjectStore.persistentStoreManagedObjectContext];
    [NSManagedObjectContext MR_setDefaultContext:managedObjectStore.mainQueueManagedObjectContext];
    
    
    objectManager.managedObjectStore = managedObjectStore;
    
    [objectManager setRequestSerializationMIMEType:RKMIMETypeJSON];
    
    // Value transformer for java epoch timestamps
    RKValueTransformer* transformer = [RemoteStore millisecondsSince1970ToDateValueTransformer];
    [[RKValueTransformer defaultValueTransformer] insertValueTransformer:transformer atIndex:0];
    
    [objectManager addRequestDescriptorsFromArray:[UserRequests requestDescriptorsForStore:managedObjectStore]];
    [objectManager addRequestDescriptorsFromArray:[RayzRequests requestDescriptorsForStore:managedObjectStore]];
    [objectManager addRequestDescriptorsFromArray:[RayzReplyRequests requestDescriptorsForStore:managedObjectStore]];
    
    
    [objectManager addResponseDescriptorsFromArray:[UserRequests responseDescriptorsForStore:managedObjectStore]];
    [objectManager addResponseDescriptorsFromArray:[RayzRequests responseDescriptorsForStore:managedObjectStore]];
    [objectManager addResponseDescriptorsFromArray:[RayzReplyRequests responseDescriptorsForStore:managedObjectStore]];
    
    
#ifdef DEBUG
    PDDebugger *debugger = [PDDebugger defaultInstance];
    [debugger addManagedObjectContext:[managedObjectStore persistentStoreManagedObjectContext] withName:@"ApiStoreModel"];
#endif
    
    
    [[LocationManager sharedManager] startLocationUpdates];
    
}

+ (RKValueTransformer*)millisecondsSince1970ToDateValueTransformer
{
    return [RKBlockValueTransformer valueTransformerWithValidationBlock:^BOOL(__unsafe_unretained Class sourceClass, __unsafe_unretained Class destinationClass) {
        return [sourceClass isSubclassOfClass:[NSNumber class]] && [destinationClass isSubclassOfClass:[NSDate class]];
    } transformationBlock:^BOOL(id inputValue, __autoreleasing id *outputValue, __unsafe_unretained Class outputValueClass, NSError *__autoreleasing *error) {
        RKValueTransformerTestInputValueIsKindOfClass(inputValue, (@[ [NSNumber class] ]), error);
        RKValueTransformerTestOutputValueClassIsSubclassOfClass(outputValueClass, (@[ [NSDate class] ]), error);

        unsigned long long timestamp = [inputValue unsignedLongLongValue] / 1000;
        NSDate * value = [NSDate dateWithTimeIntervalSince1970:timestamp];
        *outputValue = value;
        return YES;
    }];
}

+ (void)save {
    [[NSManagedObjectContext MR_defaultContext] MR_saveToPersistentStoreAndWait];
}

#pragma mark Api Calls
+ (void)updateUserLocation:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests * request = [[UserRequests alloc] init];
        [request postUserLocation:user];
    }];
}

+ (void)getLiveFeedForUser:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests *userRequests = [[UserRequests alloc] init];
        [userRequests getUserLiveFeed:user];
    }];
}

+ (void)getNearbyFeedForUser:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests *userRequests = [[UserRequests alloc] init];
        [userRequests getUserNearbyFeed:user];
    }];
}

+ (void)getUserPower:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests * request = [[UserRequests alloc] init];
        [request getUserPower:user];
    }];
}

+ (void)getUserRayzs:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests * request = [[UserRequests alloc] init];
        [request getUserRayz:user];
    }];
}

+ (void)getUserStarredRayzs:(User *)user {
    [self checkConnectionWithConnectionBlock:^{
        UserRequests * request = [[UserRequests alloc] init];
        [request getUserStarredRayz:user];
    }];
}

+ (void)postCreateRayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request postCreateRayz:rayz];
    } noConnectionBlock:^{
        [rayz setStatus:kApiRayzStatusFailed];
        [[NSNotificationCenter defaultCenter] postNotificationName:kRayzFailed object:nil];
        [RemoteStore save];
    }];
}

+ (void)postRerayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request postRerayz:rayz];
    }];
}

+ (void)deleteRayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request deleteRayz:rayz];
    }];
}

+ (void)starRayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request starRayz:rayz];
    }];
}

+ (void)unstarRayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request unstarRayz:rayz];
    }];
}

+ (void)reportRayz:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request reportRayz:rayz];
    }];
}

+ (void)getRayzAnswers:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request rayzAnswers:rayz];
    }];
}

+ (void)getRayzReplies:(Rayz *)rayz {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request rayzReplies:rayz];
    }];
}

+ (void)postCreateRayzReply:(RayzReply *)rayzReply {
    [self checkConnectionWithConnectionBlock:^{
        RayzReplyRequests * request = [[RayzReplyRequests alloc] init];
        [request postCreateRayzReply:rayzReply];
    }];
}

+ (void)powerUpRayzReply:(RayzReply *)rayzReply {
    [self checkConnectionWithConnectionBlock:^{
        RayzReplyRequests * request = [[RayzReplyRequests alloc] init];
        [request powerUpRayzReply:rayzReply];
    }];
}

+ (void)powerDownRayzReply:(RayzReply *)rayzReply {
    [self checkConnectionWithConnectionBlock:^{
        RayzReplyRequests * request = [[RayzReplyRequests alloc] init];
        [request powerDownRayzReply:rayzReply];
    }];
}

+ (void)reportRayzReply:(RayzReply *)rayzReply {
    [self checkConnectionWithConnectionBlock:^{
        RayzReplyRequests * request = [[RayzReplyRequests alloc] init];
        [request reportRayzReply:rayzReply];
    }];
}

+ (void)checkRayzs {
    [self checkConnectionWithConnectionBlock:^{
        RayzRequests * request = [[RayzRequests alloc] init];
        [request checkRayzs];
    }];
}

#pragma mark Helpers

+ (void)resetStore {
    [[[RKObjectManager sharedManager] operationQueue] cancelAllOperations];
    [[[RKObjectManager sharedManager] managedObjectStore] setManagedObjectCache:nil];
    [RKObjectManager setSharedManager:nil];
    [[RKManagedObjectStore defaultStore] resetPersistentStores:nil];
}

+ (void)resetStoreAndConfigureRestKit {
    [self resetStore];
    [[RemoteStore sharedInstance] configureRestKit];
}

+ (void)checkConnectionWithConnectionBlock:(void (^)(void))connectionBlock noConnectionBlock:(void (^)(void))noConnectionBlock {
    if ([[RemoteStore sharedInstance] isConnected]) {
        connectionBlock();
    } else {
        noConnectionBlock();
    }
}

+ (void)checkConnectionWithConnectionBlock:(void (^)(void))connectionBlock {
    if ([[RemoteStore sharedInstance] isConnected]) {
        connectionBlock();
    } else {
//        [ApiAlertsHelper showAlertForNoConnection];
    }
}

+ (void)checkConnectionWithNoConnectionBlock:(void (^)(void))noConnectionBlock {
    if (![[RemoteStore sharedInstance] isConnected]) {
        noConnectionBlock();
    }
}

#pragma mark UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
//    if ([alertView tag] == kConnectionAlertTag) {
        [self setShowingAlert:NO];
//    }
}

@end
