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
//  UserRequests.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "UserRequests.h"
#import "User.h"
#import "ApiLiveFeedResponse.h"
#import "ApiStatusMessageResponse.h"
#import "ApiUserPowerResponse.h"
#import "Rayz.h"
#import "ApiAlertHelper.h"

@implementation UserRequests

#pragma mark Descriptors
+ (NSArray *)responseDescriptorsForStore:(RKManagedObjectStore *)store {
    RKResponseDescriptor *userUpdateDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiStatusMessageResponse objectMapping] method:RKRequestMethodAny pathPattern:kUserUpdatePath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *userPowerDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiUserPowerResponse objectMapping] method:RKRequestMethodGET pathPattern:kUserPowerPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *liveFeedDescriptorModel = [RKResponseDescriptor responseDescriptorWithMapping:[ApiLiveFeedResponse objectMapping] method:RKRequestMethodGET pathPattern:kLiveFeedPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *liveFeedDescriptorEntity = [RKResponseDescriptor responseDescriptorWithMapping:[Rayz modelMappingForStore:store] method:RKRequestMethodGET pathPattern:kLiveFeedPath keyPath:@"liveFeed" statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *nearbyFeedDescriptorModel = [RKResponseDescriptor responseDescriptorWithMapping:[ApiLiveFeedResponse objectMapping] method:RKRequestMethodGET pathPattern:kNearbyFeedPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *nearbyFeedDescriptorEntity = [RKResponseDescriptor responseDescriptorWithMapping:[Rayz modelMappingForStore:store] method:RKRequestMethodGET pathPattern:kNearbyFeedPath keyPath:@"liveFeed" statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *userRayzsDescriptorModel = [RKResponseDescriptor responseDescriptorWithMapping:[ApiLiveFeedResponse objectMapping] method:RKRequestMethodAny pathPattern:kUserRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *userRayzsDescriptorEntity = [RKResponseDescriptor responseDescriptorWithMapping:[Rayz modelMappingForStore:store] method:RKRequestMethodGET pathPattern:kUserRayzPath keyPath:@"myRayz" statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *userStarredRayzsDescriptorModel = [RKResponseDescriptor responseDescriptorWithMapping:[ApiLiveFeedResponse objectMapping] method:RKRequestMethodAny pathPattern:kUserStarredPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *userStarredRayzsDescriptorEntity = [RKResponseDescriptor responseDescriptorWithMapping:[Rayz modelMappingForStore:store] method:RKRequestMethodGET pathPattern:kUserStarredPath keyPath:@"rayzFeed" statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    return @[userUpdateDescriptor, userPowerDescriptor, liveFeedDescriptorModel, liveFeedDescriptorEntity, nearbyFeedDescriptorModel, nearbyFeedDescriptorEntity, userRayzsDescriptorModel, userRayzsDescriptorEntity, userStarredRayzsDescriptorModel, userStarredRayzsDescriptorEntity];
}

+ (NSArray *)requestDescriptorsForStore:(RKManagedObjectStore *)store {
    RKRequestDescriptor *userUpdateDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[User updateLocationRequestMapping] objectClass:[User class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    return @[userUpdateDescriptor];
}

#pragma mark Requests
- (void)postUserLocation:(User *)user {
    NSString *path = kUserUpdatePath;
    
    [[RKObjectManager sharedManager] postObject:user path:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        ApiStatusMessageResponse *response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            if ([response message] != nil && [[response message] rangeOfString:@"Added User"].location != NSNotFound) {
                [[NSNotificationCenter defaultCenter] postNotificationName:kUserAdded object:nil];
            }
            else {
                [[NSNotificationCenter defaultCenter] postNotificationName:kUserLocationUpdated object:nil];
            }
            [RemoteStore checkRayzs];
        }
        else {
            if ([response message] != nil) {
                if ([[response message] isEqualToString:@"Please specify a correct Application Id."]) {
                    [ApiAlertHelper generateInvalidAppIdAlert];
                    [[NSNotificationCenter defaultCenter] postNotificationName:kInvalidAppId object:nil];
                }
                else if ([[response message] isEqualToString:@"Your Rayzit account has been disabled due to a violation of the Rayzit Terms (http://rayzit.com/tos) or Rules (http://rayzit.com/rules)."]) {
                    [ApiAlertHelper generateUserBlockedAlert];
                    [[NSNotificationCenter defaultCenter] postNotificationName:kBlockedUser object:nil];
                }
                else {
                    [[NSNotificationCenter defaultCenter] postNotificationName:kUserUpdateFailed object:@{@"message":[response message]}];
                }
            }
            else {
                [[NSNotificationCenter defaultCenter] postNotificationName:kUserUpdateFailed object:@{@"message":@"Unknown error"}];
            }
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)getUserPower:(User *)user {
    NSString *path = RKPathFromPatternWithObject(kUserPowerPath, user);
    
    ApiUserPowerResponse *userPowerResponse = [ApiUserPowerResponse new];
    [[RKObjectManager sharedManager] getObject:userPowerResponse path:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        if ([[userPowerResponse status] isEqualToString:kApiStatusSuccess]) {
            [[User appUser] setUserPower:[userPowerResponse power]];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)getUserLiveFeed:(User *)user {
    NSString *path = RKPathFromPatternWithObject(kLiveFeedPath, user);
    
    [[RKObjectManager sharedManager] getObjectsAtPath:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
        [response setFeed:[[mappingResult dictionary] objectForKey:@"liveFeed"]];
        RKLogInfo(@"%@", response);
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kLiveFeedLoaded object:nil];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        [[NSNotificationCenter defaultCenter] postNotificationName:kLiveFeedFailedToLoad object:nil];
        
    }];
}

- (void)getUserNearbyFeed:(User *)user {
    NSString *path = RKPathFromPatternWithObject(kNearbyFeedPath, user);
    
    [[RKObjectManager sharedManager] getObjectsAtPath:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
        [response setFeed:[[mappingResult dictionary] objectForKey:@"liveFeed"]];
        for (Rayz* r in [response feed]) {
            [r setNearby:@(YES)];
        }
        RKLogInfo(@"%@", response);
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kNearbyFeedLoaded object:nil];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        [[NSNotificationCenter defaultCenter] postNotificationName:kNearbyFeedFailedToLoad object:nil];
        
    }];
}

- (void)getUserRayz:(User *)user {
    NSString *path = RKPathFromPatternWithObject(kUserRayzPath, user);
    
    [[RKObjectManager sharedManager] getObjectsAtPath:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
        [response setFeed:[[mappingResult dictionary] objectForKey:@"myRayz"]];
        RKLogInfo(@"%@", response);
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kUserRayzsLoaded object:nil userInfo:@{@"page":@([[User appUser] userRayzsPage])}];
            [[User appUser] incrementUserRayzsPage];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        [[NSNotificationCenter defaultCenter] postNotificationName:kUserRayzsFailedToLoad object:nil userInfo:@{@"page":@([[User appUser] userRayzsPage])}];
        
    }];
}

- (void)getUserStarredRayz:(User *)user {
    NSString *path = RKPathFromPatternWithObject(kUserStarredPath, user);
    
    [[RKObjectManager sharedManager] getObjectsAtPath:path parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
        [response setFeed:[[mappingResult dictionary] objectForKey:@"rayzFeed"]];
        
        for (Rayz *r in [response feed]) {
            [r setStarred:@YES];
        }
        [RemoteStore save];
        
        RKLogInfo(@"%@", response);
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kUserStarredRayzsLoaded object:nil userInfo:@{@"page":@([[User appUser] userStarredPage])}];
            [[User appUser] incrementUserStarredPage];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        [[NSNotificationCenter defaultCenter] postNotificationName:kUserStarredRayzsFailedToLoad object:nil userInfo:@{@"page":@([[User appUser] userStarredPage])}];
        
    }];
}

@end
