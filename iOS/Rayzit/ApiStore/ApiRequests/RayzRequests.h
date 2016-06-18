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
//  RayzRequests.h
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 25/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "ApiRequestsProtocol.h"
@class Rayz;

static NSString * const kCreateRayzPath = @"/rayz/create";
static NSString * const kRerayzPath = @"/rayz/rerayz";
static NSString * const kDeleteRayzPath = @"/rayz/delete";
static NSString * const kStarRayzPath = @"/rayz/star";
static NSString * const kUnstarRayzPath = @"/rayz/star/delete";
static NSString * const kReportRayzPath = @"/rayz/report";
static NSString * const kRayzAnswersPath = @"/rayz/answers";
static NSString * const kRayzRepliesPath = @"/rayz/replies";
static NSString * const kCheckRayzsPath = @"/rayz/check";

@interface RayzRequests : NSObject  <ApiRequestsProtocol>

- (void)postCreateRayz:(Rayz*)rayz;
- (void)postRerayz:(Rayz*)rayz;
- (void)deleteRayz:(Rayz*)rayz;
- (void)starRayz:(Rayz *)rayz;
- (void)unstarRayz:(Rayz *)rayz;
- (void)reportRayz:(Rayz *)rayz;
- (void)rayzAnswers:(Rayz *)rayz;
- (void)rayzReplies:(Rayz *)rayz;

- (void)checkRayzs;

@end
