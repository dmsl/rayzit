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
//  RayzRequests.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 25/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzRequests.h"

#import "ApiAlertHelper.h"

#import "Rayz.h"
#import "ApiRayz.h"
#import "ApiReRayz.h"
#import "ApiUserRayzAction.h"
#import "Attachment.h"
#import "ApiStatusMessageResponse.h"
#import "ApiCreateRayzResponse.h"
#import "ApiReRayzResponse.h"
#import "ApiRayzAnswersResponse.h"
#import "ApiRayzRepliesResponse.h"
#import "ApiUserRayzActionResponse.h"
#import "ApiCheckRayzs.h"
#import "ApiCheckRayzsResponse.h"

@implementation RayzRequests

+ (NSArray *)responseDescriptorsForStore:(RKManagedObjectStore *)store {
    RKResponseDescriptor *createRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiCreateRayzResponse objectMapping] method:RKRequestMethodPOST pathPattern:kCreateRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *rerayzRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiReRayzResponse objectMapping] method:RKRequestMethodPOST pathPattern:kRerayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *deleteRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiStatusMessageResponse objectMapping] method:RKRequestMethodPOST pathPattern:kDeleteRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *starRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiUserRayzActionResponse starUnstarMapping] method:RKRequestMethodPOST pathPattern:kStarRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *unstarRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiUserRayzActionResponse starUnstarMapping] method:RKRequestMethodPOST pathPattern:kUnstarRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *reportRayzDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiUserRayzActionResponse reportMapping] method:RKRequestMethodPOST pathPattern:kReportRayzPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *rayzAnswersDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiRayzAnswersResponse objectMapping] method:RKRequestMethodPOST pathPattern:kRayzAnswersPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *rayzRepliesDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiRayzRepliesResponse objectMappingWithStore:store] method:RKRequestMethodPOST pathPattern:kRayzRepliesPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    RKResponseDescriptor *checkRayzsDescriptor = [RKResponseDescriptor responseDescriptorWithMapping:[ApiCheckRayzsResponse objectMapping] method:RKRequestMethodPOST pathPattern:kCheckRayzsPath keyPath:nil statusCodes:RKStatusCodeIndexSetForClass(RKStatusCodeClassSuccessful)];
    
    return @[createRayzDescriptor, rerayzRayzDescriptor, deleteRayzDescriptor, starRayzDescriptor, unstarRayzDescriptor, reportRayzDescriptor, rayzAnswersDescriptor, rayzRepliesDescriptor, checkRayzsDescriptor, ];
}

+ (NSArray *)requestDescriptorsForStore:(RKManagedObjectStore *)store {
    RKRequestDescriptor *createRayzDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiRayz objectMapping] objectClass:[ApiRayz class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    RKRequestDescriptor *rerayzRayzDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiReRayz objectMapping] objectClass:[ApiReRayz class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    RKRequestDescriptor *userRayzActionDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiUserRayzAction objectMapping] objectClass:[ApiUserRayzAction class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    RKRequestDescriptor *checkRayzsDescriptor = [RKRequestDescriptor requestDescriptorWithMapping:[ApiCheckRayzs objectMapping] objectClass:[ApiCheckRayzs class] rootKeyPath:nil method:RKRequestMethodPOST];
    
    return @[createRayzDescriptor, rerayzRayzDescriptor, userRayzActionDescriptor, checkRayzsDescriptor, ];
}


#pragma mark Rayz Requests

- (void)postCreateRayz:(Rayz *)rayz {
    
    ApiRayz *apiRayz = [ApiRayz apiRayzFromRayz:rayz];
    
    NSMutableURLRequest * request = [[RKObjectManager sharedManager] multipartFormRequestWithObject:apiRayz method:RKRequestMethodPOST path:kCreateRayzPath parameters:nil constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
        
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = paths[0];
        for (Attachment * att in [apiRayz attachments]) {
            
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
    
    [rayz setStatus:kApiRayzStatusPending];
    RKObjectRequestOperation * operation = [[RKObjectManager sharedManager] objectRequestOperationWithRequest:request success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiCreateRayzResponse * createRayzResponse = [mappingResult firstObject];
        
        if ([[createRayzResponse status] isEqualToString:kApiStatusSuccess]) {
            [rayz setRayzId:[createRayzResponse rayzId]];
            [rayz setTimestamp:[createRayzResponse timestamp]];
            [rayz setDeliveredK:@([createRayzResponse delivered_k])];
            [rayz setStatus:kApiRayzStatusRayzed];
            
            [[User appUser] setUserPower:[createRayzResponse power]];
            
            [[NSNotificationCenter defaultCenter] postNotificationName:kRayzCreated object:nil];
        }
        else {
            [rayz setStatus:kApiRayzStatusFailed];
            [[NSNotificationCenter defaultCenter] postNotificationName:kRayzFailed object:nil];
        }
        
        [RemoteStore save];
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
        [rayz setStatus:kApiRayzStatusFailed];
        [RemoteStore save];
        
    }];
    
    [[RKObjectManager sharedManager] enqueueObjectRequestOperation:operation];
    
}

- (void)postRerayz:(Rayz *)rayz {
    
    ApiReRayz * rerayz = [ApiReRayz apiReRayzFromRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:rerayz path:kRerayzPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiReRayzResponse * reRayzResponse = [mappingResult firstObject];
        
        if ([[reRayzResponse status] isEqualToString:kApiStatusSuccess]) {
            [rayz setRerayz:@([reRayzResponse rerayz])];
            
            [[User appUser] setUserPower:[reRayzResponse power]];
            
            [RemoteStore save];
        }
        else {
            [[[UIAlertView alloc] initWithTitle:@"Error" message:@"Could not rerayz this rayz" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        }

    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)deleteRayz:(Rayz *)rayz {
    ApiUserRayzAction * deleteAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:deleteAction path:kDeleteRayzPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiStatusMessageResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [rayz MR_deleteEntity];
            [RemoteStore save];
        }
        else if ([[response status] isEqualToString:kApiStatusDeleted]) {
            [ApiAlertHelper showAlertWithTitle:@"Error" message:[response message]];
            [rayz MR_deleteEntity];
            [RemoteStore save];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)starRayz:(Rayz *)rayz {
    ApiUserRayzAction * starAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:starAction path:kStarRayzPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiUserRayzActionResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [rayz setStarred:@YES];
            [rayz setFollow:@([response follow])];
            [RemoteStore save];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)unstarRayz:(Rayz *)rayz {
    ApiUserRayzAction * unstarAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:unstarAction path:kUnstarRayzPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiUserRayzActionResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [rayz setStarred:@NO];
            [rayz setFollow:@([response follow])];
            [RemoteStore save];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)reportRayz:(Rayz *)rayz {
    ApiUserRayzAction * reportAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:reportAction path:kReportRayzPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiUserRayzActionResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusSuccess]) {
            [rayz setReport:@([response report])];
            [RemoteStore save];
        }
        else if ([[response status] isEqualToString:kApiStatusDeleted]) {
            [rayz MR_deleteEntity];
            [ApiAlertHelper showAlertWithTitle:@"Rayz Deleted" message:[response message]];
        }
        else {
            [ApiAlertHelper showAlertWithTitle:@"Warning" message:[response message]];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)rayzAnswers:(Rayz *)rayz {
    ApiUserRayzAction * rayzAnswersAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:rayzAnswersAction path:kRayzAnswersPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiRayzAnswersResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusDeleted]) {
            if (![[RemoteStore sharedInstance] isShowingAlert]) {
                [[AlertUtils alertWithTitle:@"Rayz Deleted" message:[response message] delegate:[RemoteStore sharedInstance]] show];
                [[RemoteStore sharedInstance] setShowingAlert:YES];
            }
            [rayz MR_deleteEntity];
//            [[NSNotificationCenter defaultCenter] postNotificationName:kApiRayzDeleted object:nil];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)rayzReplies:(Rayz *)rayz {
    ApiUserRayzAction * rayzAnswersAction = [ApiUserRayzAction withRayz:rayz];
    
    [[RKObjectManager sharedManager] postObject:rayzAnswersAction path:kRayzRepliesPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@",[mappingResult firstObject]);
        
        ApiRayzRepliesResponse * response = [mappingResult firstObject];
        
        if ([[response status] isEqualToString:kApiStatusDeleted]) {
            if (![[RemoteStore sharedInstance] isShowingAlert]) {
                [[AlertUtils alertWithTitle:@"Rayz Deleted" message:[response message] delegate:[RemoteStore sharedInstance]] show];
                [[RemoteStore sharedInstance] setShowingAlert:YES];
            }
            [rayz MR_deleteEntity];
            //            [[NSNotificationCenter defaultCenter] postNotificationName:kApiRayzDeleted object:nil];
        }
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@",error);
        
    }];
}

- (void)checkRayzs {
    NSArray * rayzs = [Rayz MR_findAll];
    
    if ([rayzs count] == 0) {
        return;
    }
    
    NSMutableArray * rayzIds = [NSMutableArray arrayWithCapacity:[rayzs count]];
    
    for (Rayz * r in rayzs) {
        [rayzIds addObject:[r rayzId]];
    }
    
    ApiCheckRayzs * checkR = [[ApiCheckRayzs alloc] init];
    [checkR setUserId:[[User appUser] userId]];
    [checkR setRayzIds:[rayzIds copy]];
    
    [[RKObjectManager sharedManager] postObject:checkR path:kCheckRayzsPath parameters:nil success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
        
        RKLogInfo(@"%@", [mappingResult firstObject]);
        
        ApiCheckRayzsResponse * response = [mappingResult firstObject];
        
        NSMutableArray * todelete = [NSMutableArray arrayWithCapacity:[[response message] count]];
        for (ApiRayzStateResponse * state in [response message]) {
            if (![state isValid]) {
                [todelete addObject:[state rayzId]];
            }
        }
        
        [Rayz MR_deleteAllMatchingPredicate:[NSPredicate predicateWithFormat:@"rayzId IN %@", todelete]];
        
    } failure:^(RKObjectRequestOperation *operation, NSError *error) {
        
        RKLogError(@"%@", error);
        
    }];
}

@end
