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
//  FayeManager.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 27/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "FayeManager.h"
#import <MZFayeClient/MZFayeClient.h>
#import "Rayz.h"
#import "RayzReply.h"

static FayeManager * _fayeManager;

@interface FayeManager () <MZFayeClientDelegate>

@property (nonatomic, strong) MZFayeClient * fayeClient;

@property (nonatomic, strong) RKManagedObjectMappingOperationDataSource * dataSource;
@property (nonatomic, strong) NSOperationQueue * operationQueue;

@end

@implementation FayeManager

#pragma mark Singleton
+ (FayeManager *)sharedManager {
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        _fayeManager = [[self alloc] init];
    });
    return _fayeManager;
}

- (instancetype)init {
    if (self = [super init]) {
        _fayeClient = [[MZFayeClient alloc] initWithURL:[NSURL URLWithString:kFayeUrl]];
        [_fayeClient setDelegate:self];
        _dataSource = [[RKManagedObjectMappingOperationDataSource alloc] initWithManagedObjectContext:[[[RKObjectManager sharedManager] managedObjectStore] mainQueueManagedObjectContext] cache:[[[RKObjectManager sharedManager] managedObjectStore] managedObjectCache]];
        _operationQueue = [[NSOperationQueue alloc] init];
        [_operationQueue setMaxConcurrentOperationCount:5];
    }
    return self;
}


- (void)subscribeToUserChannel:(NSString *)userId {
    NSString *channel = [NSString stringWithFormat:@"/messages/User_%@",userId];
    [_fayeClient subscribeToChannel:channel];
}

- (void)connect {
    [_fayeClient connect];
}

#pragma mark MZFayeClientDelegate
- (void)fayeClient:(MZFayeClient *)client didConnectToURL:(NSURL *)url {
    DDLogInfo(@"faye connected: %@",url);
}

- (void)fayeClient:(MZFayeClient *)client didDisconnectWithError:(NSError *)error {
    DDLogError(@"faye disconnected: %@",error);
}

- (void)fayeClient:(MZFayeClient *)client didSubscribeToChannel:(NSString *)channel {
    DDLogInfo(@"faye subscribed to: %@",channel);
}

- (void)fayeClient:(MZFayeClient *)client didUnsubscribeFromChannel:(NSString *)channel {
    DDLogInfo(@"faye unsubscribed from: %@",channel);
}

- (void)fayeClient:(MZFayeClient *)client didReceiveMessage:(NSDictionary *)messageData fromChannel:(NSString *)channel {
    DDLogInfo(@"faye message: %@",messageData);
    [_operationQueue addOperationWithBlock:^{
        NSString * mtype = messageData[@"mtype"];
        if ([mtype isEqualToString:@"rayz"]) {
            RKObjectMapping * mapping = [Rayz modelMappingForStore:[RKObjectManager sharedManager].managedObjectStore];
            RKMappingOperation * operation = [[RKMappingOperation alloc] initWithSourceObject:messageData destinationObject:nil mapping:mapping];
            [operation setDataSource:_dataSource];
            
            //        [_operationQueue addOperation:operation];
            [_operationQueue addOperationWithBlock:^{
                [operation performMapping:nil];
                DDLogInfo(@"%@",[operation destinationObject]);
                [[operation destinationObject] setUnread:@(YES)];
            }];
            
            //    [RemoteStore save];
        }
        else if ([mtype isEqualToString:@"rayz_reply"]) {
            RKObjectMapping * mapping = [RayzReply modelMappingForStore:[RKObjectManager sharedManager].managedObjectStore];
            RKMappingOperation * operation = [[RKMappingOperation alloc] initWithSourceObject:messageData destinationObject:nil mapping:mapping];
            [operation setDataSource:_dataSource];
            
            [_operationQueue addOperation:operation];
            //        [operation performMapping:nil];
            
            //        DDLogInfo(@"%@",[operation destinationObject]);
            
            //        RayzReply * r = [operation destinationObject];
            //        if ([r isUnread]) {
            //            [[r rayz] setUnread:@(YES)];
            //        }
        }
        else if ([mtype isEqualToString:@"power"]) {
            [[User appUser] setUserPower:[messageData[@"power"] integerValue]];
        }
    }];
}

@end
