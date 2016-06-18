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
//  LocationManager.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 13/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "LocationManager.h"
#import <CoreLocation/CoreLocation.h>
#import "SettingsManager.h"
#ifndef DEBUG
    #import <Crashlytics/Crashlytics.h>
#endif

static LocationManager * _manager;

@interface LocationManager () <CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager * locationManager;

@end

@implementation LocationManager {
    BOOL _accuracyAchieved;
}

#pragma mark Singleton
+ (LocationManager *)sharedManager {
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        _manager = [[self alloc] init];
    });
    return _manager;
}

+ (BOOL)locationServicesAvailable {
    BOOL locationAvailable = [[SettingsManager sharedManager] isLocationEnabled];
    return [CLLocationManager locationServicesEnabled] && locationAvailable;
}

#pragma mark Initialization
- (instancetype)init {
    if (self = [super init]) {
        CLLocationManager * temp = [[CLLocationManager alloc] init];
        [self setLocationManager:temp];
        [_locationManager setDesiredAccuracy:kCLLocationAccuracyHundredMeters];
        [_locationManager setDistanceFilter:10];
        [_locationManager setDelegate:self];
        _accuracyAchieved = NO;
    }
    return self;
}

- (void)startLocationUpdates {
    if ([LocationManager locationServicesAvailable]) {
        if ([[UIDevice currentDevice].systemVersion floatValue] >= 8.0) {
//            if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined) {
                [_locationManager requestWhenInUseAuthorization];
//            }
//            else {
//                [_locationManager startUpdatingLocation];
//            }
        }
        else {
            [_locationManager startUpdatingLocation];
        }
    }
    else {
        //TODO: alert users
    }
}

#pragma mark CLLocationManager delegate methods
- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    CLLocation * location = [locations lastObject];
    
    DDLogCInfo(@"current accuracy: %f", [location horizontalAccuracy]);
    
    if (_accuracyAchieved) {
        [[User appUser] setLocation:location];
//        [RemoteStore updateUserLocation:[User appUser]];
    }
    else if ([location horizontalAccuracy] <= kCLLocationAccuracyHundredMeters) {
        DDLogCInfo(@"Accuracy achieved");
#ifndef DEBUG
        [CrashlyticsKit setBoolValue:YES forKey:@"accuracyAchieved"];
        [CrashlyticsKit setFloatValue:location.coordinate.longitude forKey:@"longitude"];
        [CrashlyticsKit setFloatValue:location.coordinate.latitude forKey:@"latitude"];
        [CrashlyticsKit setUserIdentifier:[[User appUser] userId]];
#endif
        [manager stopUpdatingLocation];
        _accuracyAchieved = YES;
        
        [[User appUser] setLocation:location];
//        [RemoteStore updateUserLocation:[User appUser]];
        
//        [manager startMonitoringSignificantLocationChanges];
        [self performSelector:@selector(startLocationUpdates) withObject:nil afterDelay:60*60];
    }
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    DDLogError(@"%@",error);
}

#pragma mark CLLocationManager authorization
- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if (status == kCLAuthorizationStatusAuthorizedWhenInUse) {
#ifndef DEBUG
        [CrashlyticsKit setBoolValue:YES forKey:@"locationAuthorized"];
#endif
        [_locationManager startUpdatingLocation];
    }
    else {
        // TODO alert them
    }
}

@end
