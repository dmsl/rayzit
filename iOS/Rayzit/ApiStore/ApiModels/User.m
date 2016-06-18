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
//  User.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/9/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "User.h"
#import "SettingsManager.h"

static User * _user;

#ifdef DEBUG
static NSString * const APP_ID = @"<YOUR_RAYZIT_API_KEY>";
#else
static NSString * const APP_ID = @"<YOUR_RAYZIT_API_KEY>";
#endif

@interface User ()

@property (nonatomic, strong) NSString * userId;
@property (nonatomic) NSInteger userRayzsPage;
@property (nonatomic) NSInteger userStarredPage;

@property (nonatomic, strong) CLLocation *location;

@end

@implementation User

+ (User *)appUser {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _user = [[User alloc] initAppUser];
    });
    return _user;
}

+ (User *)testUser {
    [[self appUser] setUserId:@"1"];
    return [self appUser];
}


- (instancetype)initAppUser {
    if (self = [super init]) {
        [self setUserId:[[SettingsManager sharedManager] userId]];
        [self setUserPower:0];
        CLLocationDegrees lat = [[[SettingsManager sharedManager] latitude] doubleValue];
        CLLocationDegrees lon = [[[SettingsManager sharedManager] longitude] doubleValue];
//        CGFloat accuracy = [[[SettingsManager sharedManager] accuracy] doubleValue];
        CLLocation * location = [[CLLocation alloc] initWithLatitude:lat longitude:lon];
        _location = location;
        [self resetUserRayzPage];
        [self resetUserStarredPage];
    }
    return self;
}

- (NSString *)appId {
    return APP_ID;
}

- (void)setLocation:(CLLocation *)location {
    _location = location;
    [RemoteStore updateUserLocation:self];
    [[SettingsManager sharedManager] setLatitude:[@(location.coordinate.latitude) stringValue]];
    [[SettingsManager sharedManager] setLongitude:[@(location.coordinate.longitude) stringValue]];
    [[SettingsManager sharedManager] setAccuracy:[@(location.horizontalAccuracy) stringValue]];
}

- (NSString*)latitude{
    return [@(_location.coordinate.latitude) stringValue];
}

- (NSString*)longitude {
    return [@(_location.coordinate.longitude) stringValue];
}

- (NSString*)accuracy {
    return [@(_location.horizontalAccuracy) stringValue];
}


- (void)incrementUserRayzsPage {
    _userRayzsPage++;
}

- (void)resetUserRayzPage {
    [self setUserRayzsPage:1];
}

- (void)incrementUserStarredPage {
    _userStarredPage++;
}

- (void)resetUserStarredPage {
    [self setUserStarredPage:1];
}

- (BOOL)shouldPresentTermsPage {
    return [[SettingsManager sharedManager] shouldPresentTermsPage];
}

- (void)didPresentTermsPage {
    [[SettingsManager sharedManager] setPresentedTermsPage:YES];
}

#pragma mark Mappings
+ (RKObjectMapping *)updateLocationRequestMapping {
    RKObjectMapping *mapping = [RKObjectMapping requestMapping];

    [mapping addAttributeMappingsFromArray:@[@"userId",@"appId",@"latitude",@"longitude",@"accuracy"]];
    
    return mapping;
}

@end
