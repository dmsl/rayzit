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
//  RayzReplyRequests.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 28/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzReplyRequests.h"

#import "ApiAlertHelper.h"

#import "RayzReply.h"
#import "Attachments.h"
#import "Attachment.h"
#import "ApiRayzReply.h"
#import "ApiUserRayzReplyAction.h"
#import "ApiCreateRayzReplyResponse.h"
#import "ApiPowerReportRayzReplyResponse.h"

@implementation RayzReplyRequests

+ (NSArray *)responseDescriptorsForStore:(RKManagedObjectStore *)store {
    RKResponseDescriptor *createRayzReplyDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiCreateRayzReplyResponse objectMapping] method:RKRequestMethodPOST pathPattern:kCreateRayzReplyPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *powerUpDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiPowerReportRayzReplyResponse objectMapping] method:RKRequestMethodPOST pathPattern:kPowerUpRayzReplyPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *powerDownDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiPowerReportRayzReplyResponse objectMapping] method:RKRequestMethodPOST pathPattern:kPowerDownRayzReplyPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *reportRayzReplyDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiPowerReportRayzReplyResponse objectMapping] method:RKRequestMethodPOST pathPattern:kReportRayzReplyPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    
    return @[createRayzReplyDescriptor, powerUpDescriptor, powerDownDescriptor, reportRayzReplyDescriptor, ];
}

+ (NSArray *)requestDescriptorsForStore:(RKManagedObjectStore *)store {
    RKRequestDescriptor *createRayzReplyDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiRayzReply objectMapping] objectClass:[ApiRayzReply class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    RKRequestDescriptor *userRayzReplyActionDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiUserRayzReplyAction objectMapping] objectClass:[ApiUserRayzReplyAction class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    return @[createRayzReplyDescriptor, userRayzReplyActionDescriptor, ];
}

#pragma mark Rayz Reply requests
- (void)postCreateRayzReply:(RayzReply *)rayzReply {
    
    ApiRayzReply * apiRayzReply = [ApiRayzReply apiRayzReplyFromRayzReply:rayzReply];
    
    NSMutableURLRequest * request = [[RKObjectManager sharedManager] multipartFormRequestWithObject:apiRayzReply method:RKRequestMethodPOST path:kCreateRayzReplyPath parameters:nil constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = paths[0];
        for (Attachment * att in [apiRayzReply attachments]) {
            
            NSString * path = [documentsDirectory stringByAppendingPathComponent:[att filename]];
            NSData * rawData = [NSData dataWithContentsOfFile:path];
            NSString * mimeType = nil;
            if ([[att type] isEqualToString:kApiAttachmentTypeImage]) {
                mimeType = @"image/jpeg";
            }
            else if ([[att type] isEqualToString:kApiAttachmentTypeVideo]) {
                mimeType = @"video/mp4";
            }
            else if ([[att type] isEqualToString:kApiAttachmentTypeAudio]) {
                mimeType = @"audio/wav";
            }
            else {
                mimeType = @"";
            }
            
            [formData appendPartWithFileData:rawData name:@"file" fileName:[att filename] mimeType:mimeType];
            
        }
        
    }];
    
    [rayzReply setStatus:kApiRayzStatusPending];
    RKObjectRequestOperation * operation = [[RKObjectManager sharedManager] objectRequestOperationWithRequest:request success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiCreateRayzReplyResponse * createRayzResponse = [mappingResult firstObject];
        
        if ([[createRayzResponse status] isEqualToString:kApiStatusSuccess]) {
            
            [rayzReply setRayzReplyId:[createRayzResponse rayzReplyId]];
            [rayzReply setTimestamp:[createRayzResponse timestamp]];
            [rayzReply setStatus:kApiRayzStatusRayzed];
            
            [[User appUser] setUserPower:[createRayzResponse power]];
            
            [[NSNotificationCenter defaultCenter] postNotificationName:kRayzReplyCreated object:nil];
        }
        else {
            [rayzReply setStatus:kApiRayzStatusFailed];
            [[NSNotificationCenter defaultCenter] postNotificationName:kRayzReplyFailed object:nil];
        }
        
        [RemoteStore save];
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
        [rayzReply setStatus:kApiRayzStatusFailed];
        [RemoteStore save];
        
    }];
    
    [[RKObjectManager sharedManager] enqueueObjectRequestOperation:operation];
}

- (void)powerUpRayzReply:(RayzReply *)rayzReply {
    ApiUserRayzReplyAction * powerUpAction = [ApiUserRayzReplyAction withRayzReply:rayzReply];
    
    [[RKObjectManager sharedManager] postObject:powerUpAction path:kPowerUpRayzReplyPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiPowerReportRayzReplyResponse * powerUpResponse = [mappingResult firstObject];
        
        if ([[powerUpResponse status] isEqualToString:kApiStatusSuccess]) {
            [rayzReply setUpVotes:@([powerUpResponse upVotes])];
        }
        else {
            [ApiAlertHelper showAlertWithTitle:@"Warning" message:[powerUpResponse message]];
        }
        
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        RKLogError(@"%@",error);
    }];
}

- (void)powerDownRayzReply:(RayzReply *)rayzReply {
    ApiUserRayzReplyAction * powerDownAction = [ApiUserRayzReplyAction withRayzReply:rayzReply];
    
    [[RKObjectManager sharedManager] postObject:powerDownAction path:kPowerDownRayzReplyPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiPowerReportRayzReplyResponse * powerDownResponse = [mappingResult firstObject];
        
        if ([[powerDownResponse status] isEqualToString:kApiStatusSuccess]) {
            [rayzReply setUpVotes:@([powerDownResponse upVotes])];
        }
        else {
            [ApiAlertHelper showAlertWithTitle:@"Warning" message:[powerDownResponse message]];
        }
        
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        RKLogError(@"%@",error);
    }];
}

- (void)reportRayzReply:(RayzReply *)rayzReply {
    ApiUserRayzReplyAction * reportAction = [ApiUserRayzReplyAction withRayzReply:rayzReply];
    
    [[RKObjectManager sharedManager] postObject:reportAction path:kReportRayzReplyPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiPowerReportRayzReplyResponse * reportResponse = [mappingResult firstObject];
        
        if ([[reportResponse status] isEqualToString:kApiStatusSuccess]) {
            [rayzReply setReport:@([reportResponse report])];
        }
        else if ([[reportResponse status] isEqualToString:kApiStatusDeleted]) {
            [rayzReply MR_deleteEntity];
        }
        else {
            [ApiAlertHelper showAlertWithTitle:@"Warning" message:[reportResponse message]];
        }
        
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        RKLogError(@"%@",error);
    }];
}

@end
