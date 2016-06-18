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
//  CAFileManager.h
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 16/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CAFileManager : NSObject

+ (BOOL)fileExistsAtPath:(NSString*)path;
+ (BOOL)isCachedFile:(NSString*)filename;
+ (BOOL)removeFileAtPath:(NSString*)path;
+ (BOOL)cacheData:(NSData*)data withFilename:(NSString*)filename;
+ (NSData*)loadDataFromCacheWithFilename:(NSString*)filename;
+ (BOOL)storeImage:(UIImage*)image withFilename:(NSString*)filename;
+ (UIImage*)loadImageWithName:(NSString*)filename;

+ (NSURL*)cacheURLforFile:(NSString*)filename;
+ (UIImage*)thumbnailForVideoAtPath:(NSString*)path;

//+ (BOOL)storeImage:(UIImage *)image withDirPath:(NSString*)path;

@end
