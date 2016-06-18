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
//  Rayz.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "Rayz.h"
#import "SettingsManager.h"
#import "Attachments.h"
#import "RayzReply.h"

#import "NSDate+CAStringifiedIntervals.h"

static NSString * const kInfoFormat = @"%ld starred, %ld rerayzed";
static NSString * const kRepliesInfoFormat = @"%ld total, %ld unread";

@implementation Rayz

@dynamic status;
@dynamic rayzId;
@dynamic userId;
@dynamic timestamp;
@dynamic follow;
@dynamic rayz_message;
@dynamic rerayz;
@dynamic report;
@dynamic maxDistance;
@dynamic deliveredK;
@dynamic mtype;
@dynamic hidden;
@dynamic starred;
@dynamic unread;
@dynamic nearby;
@dynamic replies;
@dynamic attachments;

+ (Rayz *)newRayz {
    Rayz * r = [Rayz MR_createEntity];
    [r setMtype:@"rayz"];
    [r setUserId:[[User appUser] userId]];
    [r setStatus:kApiRayzStatusPending];
    [r setTimestamp:[NSDate date]];
    [r setUnread:@(NO)];
    return r;
}

+ (NSArray *)validDistances {
    NSMutableArray * distances = [NSMutableArray arrayWithCapacity:6];
    
    NSDictionary * dict = @{
                            kApiDistanceValue: @0,
                            kApiDistanceKilometersName: @"unlimited",
                            kApiDistanceMilesName: @"unlimited"
                            };
    [distances addObject:dict];
    
    dict = @{
             kApiDistanceValue: @500,
             kApiDistanceKilometersName: @"0.5 km",
             kApiDistanceMilesName: @"0.3 miles"
             };
    [distances addObject:dict];
    
    dict = @{
             kApiDistanceValue: @5000,
             kApiDistanceKilometersName: @"5 km",
             kApiDistanceMilesName: @"3 miles"
             };
    [distances addObject:dict];
    
    dict = @{
             kApiDistanceValue: @50000,
             kApiDistanceKilometersName: @"50 km",
             kApiDistanceMilesName: @"30 miles"
             };
    [distances addObject:dict];
    
    dict = @{
             kApiDistanceValue: @500000,
             kApiDistanceKilometersName: @"500 km",
             kApiDistanceMilesName: @"300 miles"
             };
    [distances addObject:dict];
    
    dict = @{
             kApiDistanceValue: @5000000,
             kApiDistanceKilometersName: @"5000 km",
             kApiDistanceMilesName: @"3000 miles"
             };
    [distances addObject:dict];
    
    return distances;
}

- (void)create {
    [self setStatus:kApiRayzStatusPending];
    [RemoteStore postCreateRayz:self];
}

- (void)rerayzRayz {
    [RemoteStore postRerayz:self];
}

- (void)star {
    [RemoteStore starRayz:self];
}

- (void)unstar {
    [RemoteStore unstarRayz:self];
}

- (void)reportRayz {
    [RemoteStore reportRayz:self];
}

- (void)deleteRayz {
    if ([[self status] isEqualToString:kApiRayzStatusFailed]) {
        [self MR_deleteEntity];
    }
    else if ([[self status] isEqualToString:kApiRayzStatusRayzed] || [[self userId] rangeOfString:[[User appUser] userId]].location!=NSNotFound) {
        [RemoteStore deleteRayz:self];
    }
    else {
        [self setHidden:@YES];
    }
}

- (BOOL)isUnread {
    return [[self unread] boolValue];
}

- (BOOL)isNearby {
    return [[self nearby] boolValue];
}

- (NSString *)stringFromTimestamp {
    if ([[self status] isEqualToString:kApiRayzStatusPending]) {
        return @"sending...";
    }
    else if ([[self status] isEqualToString:kApiRayzStatusFailed]) {
        return @"failed to send. tap to retry";
    }
    return [NSDate stringifiedIntervalFrom:[self timestamp]];
}

- (NSString *)rayzInfo {
    return [NSString stringWithFormat:kInfoFormat,(long)[self.follow integerValue],(long)[self.rerayz integerValue]];
}

- (NSInteger)numOfUnreadReplies {
    NSPredicate * unreadReplies = [NSPredicate predicateWithFormat:@"unread == YES"];
    return [[[self replies] filteredSetUsingPredicate:unreadReplies] count];
}

- (NSString *)rayzRepliesInfo {
    return [NSString stringWithFormat:kRepliesInfoFormat,(unsigned long)[[self replies] count],(long)[self numOfUnreadReplies]];
}

- (NSString *)distanceString {
    if ([self.maxDistance integerValue] == 0) {
        return @"unlimited";
    }
    for (NSDictionary * d in [Rayz validDistances]) {
        if ([d[kApiDistanceValue] integerValue] == [[self maxDistance] integerValue]) {
            if ([[[SettingsManager sharedManager] distanceMetric] isEqualToString:distanceMetricKilometers]) {
                return d[kApiDistanceKilometersName];
            }
            else {
                return d[kApiDistanceMilesName];
            }
        }
    }
    return @"unlimited";
}

- (UIColor *)rayzColor {
    if ([self.status isEqualToString:kApiRayzStatusFailed]) {
        return [RayzitTheme failedRayzColor];
    }
    else if ([[self starred] boolValue] == YES) {
        return [RayzitTheme starredRayzColor];
    }
//    else if ([self.userId rangeOfString:[[User appUser] userId]].location != NSNotFound) {
//        return [RayzitTheme myRayzColor];
//    }
    return [RayzitTheme normalRayzColor];
}

#pragma mark ApiEntitiesProtocol
+ (RKEntityMapping *)modelMappingForStore:(RKManagedObjectStore *)store {
    RKEntityMapping *modelMapping = [RKEntityMapping mappingForEntityForName:NSStringFromClass([Rayz class]) inManagedObjectStore:store];
    
    [modelMapping addAttributeMappingsFromArray:@[@"rayzId", @"userId", @"timestamp", @"follow", @"rayz_message", @"rerayz", @"report", @"maxDistance", @"mtype"]];
    
    [modelMapping addRelationshipMappingWithSourceKeyPath:@"attachments" mapping:[Attachments modelMappingForStore:store]];
    [modelMapping setIdentificationAttributes:@[@"rayzId"]];
    
    return modelMapping;
}

+ (RKEntityMapping *)deleteRayzMappingForStore:(RKManagedObjectStore *)store {
    RKEntityMapping *deleteMapping = [RKEntityMapping mappingForEntityForName:NSStringFromClass([Rayz class]) inManagedObjectStore:store];
    
    [deleteMapping addAttributeMappingsFromArray:@[@"rayzId",@"userId"]];
    
    return deleteMapping;
}

@end
