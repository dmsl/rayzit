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
//  ApiTests.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 21/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "Specta.h"
#define EXP_SHORTHAND
#import "Expecta.h"
#import <OCMock/OCMock.h>

#import "RemoteStore.h"

#import "UserRequests.h"
#import "RayzRequests.h"
#import "User.h"
#import "Rayz.h"
#import "ApiRayz.h"
#import "Attachments.h"
#import "Attachment.h"

#import "ApiLiveFeedResponse.h"
#import "ApiStatusMessageResponse.h"
#import "ApiUserPowerResponse.h"

SpecBegin(ApiTests)

describe(@"ApiTests Spec", ^{
   
    setAsyncSpecTimeout(30.0);
    //init RemoteStore
    [RemoteStore sharedInstance];
    RKObjectManager *manager = [RKObjectManager sharedManager];
    
    describe(@"Live feed process", ^{
        
        it(@"should add or update user", ^AsyncBlock {
           
            User *user = [User testUser];
            
            void (^successblock)(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) = ^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                
                ApiStatusMessageResponse *response = [mappingResult firstObject];
                expect(response).toNot.beNil();
                
                expect([[response status] isEqualToString:kApiStatusSuccess]).to.beTruthy();
                
                done();
            };
            
            void (^failBlock)(RKObjectRequestOperation *operation, NSError *) = ^(RKObjectRequestOperation *operation, NSError *error) {
                
                expect(error).to.beNil();
                
                done();
            };
            
            NSString *path = kUserUpdatePath;
            [manager postObject:user path:path parameters:nil success:successblock failure:failBlock];
            
        });
        
        it(@"should get user power", ^AsyncBlock {
            
            User *user = [User testUser];
            
            ApiUserPowerResponse *response = [ApiUserPowerResponse new];
            
            void (^successblock)(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) = ^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                
                expect(response).toNot.beNil();
                
                expect([[response status] isEqualToString:kApiStatusSuccess]).to.beTruthy();
                
                done();
            };
            
            void (^failBlock)(RKObjectRequestOperation *operation, NSError *) = ^(RKObjectRequestOperation *operation, NSError *error) {
                
                expect(error).to.beNil();
                
                done();
            };
            
            NSString *path = RKPathFromPatternWithObject(kUserPowerPath, user);
            [manager getObject:response path:path parameters:nil success:successblock failure:failBlock];
            
        });
        
        it(@"should get live feed", ^AsyncBlock{
            
            User *user = [User testUser];
            
            void (^successblock)(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) = ^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                
                ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
                expect(response).toNot.beNil();
                
                NSArray *rayzs = [[mappingResult dictionary] objectForKey:@"liveFeed"];
                expect(rayzs).toNot.beNil();
                expect([rayzs count] == [[response counter] integerValue]).to.beTruthy();
                
                done();
            };
            
            void (^failBlock)(RKObjectRequestOperation *operation, NSError *) = ^(RKObjectRequestOperation *operation, NSError *error) {
                
                expect(error).to.beNil();
                
                done();
            };
            
            NSString *path = RKPathFromPatternWithObject(kLiveFeedPath, user);
            [manager getObjectsAtPath:path parameters:nil success:successblock failure:failBlock];
            
        });
        
        it(@"should get user rayzs", ^AsyncBlock{
            
            User *user = [User testUser];
            
            void (^successblock)(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) = ^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                
                ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
                expect(response).toNot.beNil();
                
                NSArray *rayzs = [[mappingResult dictionary] objectForKey:@"myRayz"];
                expect(rayzs).toNot.beNil();
                expect([rayzs count] == [[response counter] integerValue]).to.beTruthy();
                
                done();
            };
            
            void (^failBlock)(RKObjectRequestOperation *operation, NSError *) = ^(RKObjectRequestOperation *operation, NSError *error) {
                
                expect(error).to.beNil();
                
                done();
            };
            
            NSString *path = RKPathFromPatternWithObject(kUserRayzPath, user);
            [manager getObjectsAtPath:path parameters:nil success:successblock failure:failBlock];
            
        });
        
        it(@"should get user starred rayzs", ^AsyncBlock{
            
            User *user = [User testUser];
            
            void (^successblock)(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) = ^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                
                ApiLiveFeedResponse *response = [[mappingResult dictionary] objectForKey:[NSNull new]];
                expect(response).toNot.beNil();
                
                NSArray *rayzs = [[mappingResult dictionary] objectForKey:@"rayzFeed"];
                expect(rayzs).toNot.beNil();
                expect([rayzs count] == [[response counter] integerValue]).to.beTruthy();
                
                done();
            };
            
            void (^failBlock)(RKObjectRequestOperation *operation, NSError *) = ^(RKObjectRequestOperation *operation, NSError *error) {
                
                expect(error).to.beNil();
                
                done();
            };
            
            NSString *path = RKPathFromPatternWithObject(kUserStarredPath, user);
            [manager getObjectsAtPath:path parameters:nil success:successblock failure:failBlock];
            
        });
        
        it(@"create a rayz", ^AsyncBlock {
            ApiRayz *rayz = [[ApiRayz alloc] init];
            
            [rayz setUserId:@"1"];
            [rayz setRayz_message:@"test"];
            [rayz setLatitude:33];
            [rayz setLongitude:34];
            [rayz setAccuracy:100];
            [rayz setMaxDistance:5000];
            
            NSMutableURLRequest * request = [[RKObjectManager sharedManager] multipartFormRequestWithObject:rayz method:RKRequestMethodPOST path:kCreateRayzPath parameters:nil constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
       
                for (Attachment * att in [rayz attachments]) {
                    [formData appendPartWithFileURL:[NSURL URLWithString:[att url]] name:@"file" error:nil];
                }
                
            }];
            
            RKLogInfo(@"%@",[[NSString alloc] initWithData:[request HTTPBody] encoding:NSUTF8StringEncoding]);
            expect(request).toNot.beNil();
            
            RKObjectRequestOperation * operation = [[RKObjectManager sharedManager] objectRequestOperationWithRequest:request success:^(RKObjectRequestOperation *operation, RKMappingResult *mappingResult) {
                
                expect(mappingResult).toNot.beNil();
                expect([mappingResult firstObject]).toNot.beNil();
                
                RKLogInfo(@"%@",[mappingResult firstObject]);
                
                done();
                
            } failure:^(RKObjectRequestOperation *operation, NSError *error) {
                
                RKLogError(@"%@",error);
                expect(error).toNot.beNil();
                
                done();
                
            }];
            
            [[RKObjectManager sharedManager] enqueueObjectRequestOperation:operation];
            
        });
        
    });
    
});

SpecEnd
