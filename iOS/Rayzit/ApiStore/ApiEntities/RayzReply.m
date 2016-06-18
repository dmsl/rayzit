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
//  RayzReply.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzReply.h"
#import "Attachments.h"
#import "Rayz.h"

#import "SettingsManager.h"
#import "NSDate+CAStringifiedIntervals.h"

static NSString * const kInfoFormat = @"%ld power";

@implementation RayzReply

@dynamic rayz_reply_message;
@dynamic rayzReplyId;
@dynamic rayzId;
@dynamic timestamp;
@dynamic userId;
@dynamic rayz;
@dynamic upVotes;
@dynamic report;
@dynamic unread;
@dynamic mtype;
@dynamic status;
@dynamic attachments;

+ (RayzReply *)newRayzReply {
    RayzReply * r = [RayzReply MR_createEntity];
    [r setMtype:@"rayz_reply"];
    [r setUserId:[[User appUser] userId]];
    [r setStatus:kApiRayzStatusPending];
    [r setTimestamp:[NSDate date]];
    [r setUnread:@(NO)];
    return r;
}

- (void)create {
    [self setStatus:kApiRayzStatusPending];
    [RemoteStore postCreateRayzReply:self];
    if ([[SettingsManager sharedManager] shouldAutoStarRayz]) {
        [[self rayz] star];
    }
}

- (void)powerUp {
    [RemoteStore powerUpRayzReply:self];
}

- (void)powerDown {
    [RemoteStore powerDownRayzReply:self];
}

- (void)reportRayzReply {
    [RemoteStore reportRayzReply:self];
}

- (BOOL)isUnread {
    return [[self unread] boolValue];
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

- (NSString *)rayzReplyInfo {
    return [NSString stringWithFormat:kInfoFormat,(long)[self.upVotes integerValue]];
}

- (UIColor *)rayzReplyColor {
    if ([self.status isEqualToString:kApiRayzStatusFailed]) {
        return [RayzitTheme failedRayzColor];
    }
//    else if ([self.userId rangeOfString:[[User appUser] userId]].location != NSNotFound) {
//        return [RayzitTheme myRayzColor];
//    }
    return [RayzitTheme normalRayzColor];
}

#pragma mark ApiEntitiesProtocol
+ (RKEntityMapping *)modelMappingForStore:(RKManagedObjectStore *)store {
    RKEntityMapping *modelMapping = [RKEntityMapping mappingForEntityForName:NSStringFromClass([RayzReply class]) inManagedObjectStore:store];
    
    [modelMapping addAttributeMappingsFromArray:@[@"rayz_reply_message", @"userId", @"timestamp", @"upVotes", @"rayzReplyId", @"report", @"rayzId", @"mtype"]];
    
    [modelMapping addConnectionForRelationship:@"rayz" connectedBy:@{@"rayzId":@"rayzId"}];
    [modelMapping addRelationshipMappingWithSourceKeyPath:@"attachments" mapping:[Attachments modelMappingForStore:store]];
    [modelMapping setIdentificationAttributes:@[@"rayzReplyId"]];
    
    return modelMapping;
}

@end
