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
//  Attachments.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "Attachments.h"
#import "Attachment.h"
#import "Rayz.h"
#import "RayzReply.h"


@implementation Attachments

@dynamic rayz;
@dynamic reply;

@dynamic images;
@dynamic audio;
@dynamic videos;

- (NSArray *)allAttachments {
    NSMutableArray *arr = [NSMutableArray arrayWithCapacity:[self totalCount]];
    
    [arr addObjectsFromArray:[[self images] allObjects]];
    [arr addObjectsFromArray:[[self audio]  allObjects]];
    [arr addObjectsFromArray:[[self videos] allObjects]];
    
    return arr;
}

- (NSInteger)totalCount {
    return [[self images] count] + [[self audio] count] + [[self videos] count];
}

#pragma mark ApiEntitiesProtocol
+ (RKEntityMapping *)modelMappingForStore:(RKManagedObjectStore *)store {
    RKEntityMapping *modelMapping = [RKEntityMapping mappingForEntityForName:NSStringFromClass([Attachments class]) inManagedObjectStore:store];
    
    [modelMapping addRelationshipMappingWithSourceKeyPath:@"images" mapping:[Attachment modelMappingForStore:store]];
    [modelMapping addRelationshipMappingWithSourceKeyPath:@"audio" mapping:[Attachment modelMappingForStore:store]];
    [modelMapping addRelationshipMappingWithSourceKeyPath:@"videos" mapping:[Attachment modelMappingForStore:store]];
    
    return modelMapping;
}

@end
