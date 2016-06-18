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
//  NSDate+CAStringifiedIntervals.m
//  FunifiDO
//
//  Created by Chrysovalantis Anastasiou on 16/6/14.
//  Copyright (c) 2014 Funifi LTD. All rights reserved.
//

#import "NSDate+CAStringifiedIntervals.h"

#define kSecondsInMinute    60
#define kSecondsInHour      3600    // 60 * 60
#define kSecondsInDay       86400   // 60 * 60 * 24

@implementation NSDate (CAStringifiedIntervals)

+ (NSString *)stringifiedIntervalFrom:(NSDate *)date {
    
    NSTimeInterval interval = [[NSDate date] timeIntervalSinceDate:date];
    
    return [NSDate stringifyInterval:interval fromDate:date];
}

- (NSString*)stringifiedIntervalSince:(NSDate *)date {
    
    NSTimeInterval interval = [date timeIntervalSinceDate:self];
    
    return [NSDate stringifyInterval:interval fromDate:date];
}

+ (NSString*)stringifyInterval:(NSTimeInterval)interval fromDate:(NSDate*)date {
    NSInteger days = interval / kSecondsInDay;
    
    if (days > 0) {
        if (days == 1) {
            return @"yesterday";
        }
        else {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
            [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
            
            return [dateFormatter stringFromDate:date];
//            return [NSString stringWithFormat:@"%ld days ago",(long)days];
        }
    }
    
    NSInteger hours = interval / kSecondsInHour;
    
    if (hours > 0) {
        if (hours == 1) {
            return @"An hour ago";
        }
        else {
            return [NSString stringWithFormat:@"%ld hours ago",(long)hours];
        }
    }
    
    NSInteger minutes = interval / kSecondsInMinute;
    
    if (minutes > 0) {
        if (minutes == 1) {
            return @"just now";
        }
        else {
            return [NSString stringWithFormat:@"%ld mins ago",(long)minutes];
        }
    }
    
//    if (interval > 20) {
//        return @"moments ago";
//    }
    
    // otherwise
    return @"just now";
}

@end
