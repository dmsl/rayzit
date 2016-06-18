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
//  SettingsManager.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 28/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "SettingsManager.h"

NSString * const distanceMetricKilometers = @"kilometers";
NSString * const distanceMetricMiles = @"miles";


NSString * const kDistanceMetricKey = @"distanceMetricKey";
NSString * const kMaximumRayzDistanceKey = @"maximumRayzDistanceKey";
NSString * const kAutoStarRayzKey = @"autoStarRayzKey";
NSString * const kLocationEnabledKey = @"locationEnabledKey";
NSString * const kUserIdKey = @"userIdKey";
NSString * const kLastLatitude = @"lastLatitude";
NSString * const kLastLongitude = @"lastLongitude";
NSString * const kLastAccuracy = @"lastAccuracy";
NSString * const kDidPresentTermsPage = @"presentedTermsPage";

static SettingsManager * _settingsManager;

@interface SettingsManager ()

@property (nonatomic, weak) NSUserDefaults * defaults;

@end

@implementation SettingsManager

@synthesize presentedTermsPage = _presentedTermsPage;

#pragma mark Singleton
+ (SettingsManager *)sharedManager {
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        _settingsManager = [[self alloc] init];
    });
    return _settingsManager;
}


- (instancetype)init {
    if (self = [super init]) {
        _defaults = [NSUserDefaults standardUserDefaults];
        if ([_defaults objectForKey:kDistanceMetricKey]) {
            [self setDistanceMetric:[_defaults objectForKey:kDistanceMetricKey]];
        }
        else {
            [self setDistanceMetric:distanceMetricKilometers];
        }
        if ([_defaults objectForKey:kMaximumRayzDistanceKey]) {
            [self setMaximumRayzSendingDistance:[_defaults integerForKey:kMaximumRayzDistanceKey]];
        }
        else {
            [self setMaximumRayzSendingDistance:0];
        }
        if ([_defaults objectForKey:kAutoStarRayzKey]) {
            [self setAutoStarRayz:[_defaults boolForKey:kAutoStarRayzKey]];
        }
        else {
            [self setAutoStarRayz:YES];
        }
        if ([_defaults objectForKey:kLocationEnabledKey]) {
            [self setLocationEnabled:[_defaults boolForKey:kLocationEnabledKey]];
        }
        else {
            [self setLocationEnabled:YES];
        }
        if ([_defaults stringForKey:kLastLatitude]) {
            [self setLatitude:[_defaults stringForKey:kLastLatitude]];
        }
        if ([_defaults stringForKey:kLastLongitude]) {
            [self setLongitude:[_defaults stringForKey:kLastLongitude]];
        }
        if ([_defaults stringForKey:kLastAccuracy]) {
            [self setAccuracy:[_defaults stringForKey:kLastAccuracy]];
        }
        if ([_defaults boolForKey:kDidPresentTermsPage]) {
            [self setPresentedTermsPage:[_defaults boolForKey:kDidPresentTermsPage]];
        }
        else {
            [self setPresentedTermsPage:NO];
        }
    }
    return self;
}

- (void)setDistanceMetric:(NSString *)distanceMetric {
    _distanceMetric = distanceMetric;
    [_defaults setObject:distanceMetric forKey:kDistanceMetricKey];
    [_defaults synchronize];
}

- (void)setMaximumRayzSendingDistance:(NSInteger)maximumRayzSendingDistance {
    _maximumRayzSendingDistance = maximumRayzSendingDistance;
    [_defaults setInteger:maximumRayzSendingDistance forKey:kMaximumRayzDistanceKey];
    [_defaults synchronize];
}

- (void)setAutoStarRayz:(BOOL)autoStarRayz {
    _autoStarRayz = autoStarRayz;
    [_defaults setBool:autoStarRayz forKey:kAutoStarRayzKey];
    [_defaults synchronize];
}

- (void)setLocationEnabled:(BOOL)locationEnabled {
    _locationEnabled = locationEnabled;
    [_defaults setBool:locationEnabled forKey:kLocationEnabledKey];
    [_defaults synchronize];
}

- (NSString *)userId {
    if ([_defaults objectForKey:kUserIdKey]) {
        return [_defaults objectForKey:kUserIdKey];
    }
    else {
        [_defaults setObject:[SettingsManager UUID] forKey:kUserIdKey];
        return [self userId];
    }
}

- (void)setLatitude:(NSString *)latitude {
    _latitude = latitude;
    [_defaults setObject:latitude forKey:kLastLatitude];
    [_defaults synchronize];
}

- (void)setLongitude:(NSString *)longitude {
    _longitude = longitude;
    [_defaults setObject:longitude forKey:kLastLongitude];
    [_defaults synchronize];
}

- (void)setAccuracy:(NSString *)accuracy {
    _accuracy = accuracy;
    [_defaults setObject:accuracy forKey:kLastAccuracy];
    [_defaults synchronize];
}

- (void)setPresentedTermsPage:(BOOL)presentedTermsPage {
    _presentedTermsPage = presentedTermsPage;
    [_defaults setBool:presentedTermsPage forKey:kDidPresentTermsPage];
    [_defaults synchronize];
}

- (BOOL)shouldPresentTermsPage {
    return !_presentedTermsPage;
}

+(NSString*)UUID
{
    return [[[[UIDevice currentDevice] identifierForVendor] UUIDString] stringByReplacingOccurrencesOfString:@"-" withString:@""];
}

@end
