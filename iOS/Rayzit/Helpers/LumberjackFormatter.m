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
//  LumberjackFormatter.m
//  FunifiDO
//
//  Created by Łukasz Imielski on 23.07.2014.
//  Copyright (c) 2014 Funifi LTD. All rights reserved.
//

#import "LumberjackFormatter.h"
#import <libkern/OSAtomic.h>

NSString *const kTimeFormat = @"HH:mm:ss:SSS dd.MM.yyyy";

@implementation LumberjackFormatter

- (NSString *)stringFromDate:(NSDate *)date {
    int32_t loggerCount = OSAtomicAdd32(0, &atomicLoggerCount);

    if (loggerCount <= 1) {
        // Single-threaded mode.

        if (threadUnsafeDateFormatter == nil) {
            threadUnsafeDateFormatter = [[NSDateFormatter alloc] init];
            [threadUnsafeDateFormatter setFormatterBehavior:NSDateFormatterBehavior10_4];
            [threadUnsafeDateFormatter setDateFormat:kTimeFormat];
        }

        return [threadUnsafeDateFormatter stringFromDate:date];
    }
    else {
        // Multi-threaded mode.
        // NSDateFormatter is NOT thread-safe.

        NSString *key = @"CustomFormatter_NSDateFormatter";

        NSMutableDictionary *threadDictionary = [[NSThread currentThread] threadDictionary];
        NSDateFormatter *dateFormatter = threadDictionary[key];

        if (dateFormatter == nil) {
            dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setFormatterBehavior:NSDateFormatterBehavior10_4];
            [dateFormatter setDateFormat:kTimeFormat];

            threadDictionary[key] = dateFormatter;
        }

        return [dateFormatter stringFromDate:date];
    }
}

- (NSString *)formatLogMessage:(DDLogMessage *)logMessage {
    NSString *logLevel;
    switch (logMessage->logFlag) {
        case LOG_FLAG_ERROR :
            logLevel = @"E";
            break;
        case LOG_FLAG_WARN  :
            logLevel = @"W";
            break;
        case LOG_FLAG_INFO  :
            logLevel = @"I";
            break;
        default             :
            logLevel = @"V";
            break;
    }

    NSString *dateAndTime = [self stringFromDate:(logMessage->timestamp)];

    return [NSString stringWithFormat:@"%@|%@|[%@.m:%i] %s| %@", logLevel, dateAndTime, [logMessage fileName], logMessage->lineNumber, logMessage->function, logMessage->logMsg];
}

- (void)didAddToLogger:(id <DDLogger>)logger {
    OSAtomicIncrement32(&atomicLoggerCount);
}

- (void)willRemoveFromLogger:(id <DDLogger>)logger {
    OSAtomicDecrement32(&atomicLoggerCount);
}

@end
