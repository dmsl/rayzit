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
//  RayzReply.h
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "ApiEntitiesProtocol.h"

@class Attachments, Rayz;

@interface RayzReply : NSManagedObject <ApiEntitiesProtocol>

@property (nonatomic, retain) NSString * rayz_reply_message;
@property (nonatomic, retain) NSString * rayzReplyId;
@property (nonatomic, retain) NSString * rayzId;
@property (nonatomic, retain) NSDate * timestamp;
@property (nonatomic, retain) NSString * userId;
@property (nonatomic, retain) NSNumber * upVotes;
@property (nonatomic, retain) NSNumber * report;
@property (nonatomic, retain) NSNumber * unread;
@property (nonatomic, retain) NSString * mtype;
@property (nonatomic, retain) NSString * status;
@property (nonatomic, retain) Rayz *rayz;
@property (nonatomic, retain) Attachments *attachments;


+ (RayzReply *)newRayzReply;

- (void)create;
- (void)powerUp;
- (void)powerDown;
- (void)reportRayzReply;

- (BOOL)isUnread;

- (NSString *)stringFromTimestamp;
- (NSString*)rayzReplyInfo;
- (UIColor*)rayzReplyColor;

@end
