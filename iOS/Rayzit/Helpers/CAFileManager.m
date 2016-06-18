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
//  CAFileManager.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 16/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "CAFileManager.h"
#import <AVFoundation/AVFoundation.h>

@implementation CAFileManager

+ (BOOL)fileExistsAtPath:(NSString*)path {
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:path];
    return fileExists;
}

+ (BOOL)isCachedFile:(NSString*)filename {
    if (filename == nil) return NO;
    NSString *path = [self cachePathForFilename:filename];
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:path];
    return fileExists;
}

+ (BOOL)removeFileAtPath:(NSString *)path {
    if ([self fileExistsAtPath:path]) {
        return [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
    }
    return YES;
}

+ (NSString*)cachePathForFilename:(NSString*)filename {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    return [documentsDirectory stringByAppendingPathComponent:filename];
}

+ (BOOL)cacheData:(NSData*)data withFilename:(NSString*)filename {
    NSString *filePath = [self cachePathForFilename:filename];
    return [data writeToFile:filePath atomically:YES];
}

+ (NSData*)loadDataFromCacheWithFilename:(NSString*)filename {
    return [NSData dataWithContentsOfFile:[self cachePathForFilename:filename]];
}

+ (BOOL)storeImage:(UIImage *)image withFilename:(NSString*)filename {
    return [self cacheData:UIImageJPEGRepresentation(image, 0.2) withFilename:filename];
}

+ (UIImage*)loadImageWithName:(NSString *)filename {
    return [UIImage imageWithData:[self loadDataFromCacheWithFilename:filename]];
}

//+ (BOOL)storeImage:(UIImage *)image withDirPath:(NSString*)path {
//    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
//    NSString *documentsDirectory = paths[0];
//    NSString *filePath = [documentsDirectory stringByAppendingPathComponent:path];
//    NSString *dirPath = [filePath stringByDeletingLastPathComponent];
//    
//    NSData *imageData = UIImagePNGRepresentation(image);
//    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
//        [[NSFileManager defaultManager] createDirectoryAtPath:dirPath withIntermediateDirectories:YES attributes:nil error:NULL];
//    }
//    return [imageData writeToFile:filePath atomically:YES];
//}

+ (NSURL *)cacheURLforFile:(NSString *)filename {
    NSURL * url = [NSURL fileURLWithPath:[self cachePathForFilename:filename]];
    return url;
}

+ (UIImage *)thumbnailForVideoAtPath:(NSString *)path {
    NSURL *videoURL = [NSURL fileURLWithPath:[self cachePathForFilename:path]];
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:videoURL options:nil];
    AVAssetImageGenerator *generateImg = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    NSError *error = NULL;
    CMTime time = CMTimeMakeWithSeconds(1, 1);
    CGImageRef refImg = [generateImg copyCGImageAtTime:time actualTime:NULL error:&error];
    
    UIImage *videoImage= [[UIImage alloc] initWithCGImage:refImg];
    return [Utilities fixOrientation:videoImage];
}

@end
