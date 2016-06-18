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
//  Utilities.m
//  FunifiDO
//
//  Created by Chrysovalantis Anastasiou on 3/6/14.
//  Copyright (c) 2014 Funifi LTD. All rights reserved.
//

@implementation NSString (Utils)

- (NSString *)trim {
    return [self stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet
                                                  ]];
}

@end

@implementation Utilities

+ (CGSize)screenSize {
    return [[UIScreen mainScreen] bounds].size;
}

+ (BOOL)isValidEmail:(NSString *)email {
    NSString *emailRegex = @"[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
    NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];
    return [emailTest evaluateWithObject:email];
}

+ (NSString*)randomStringOfSize:(NSUInteger)size {
    
    static NSString *letters = @"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    
    NSMutableString *randomString = [NSMutableString stringWithCapacity:size];
    
    for (int i=0; i<size; i++) {
        [randomString appendFormat: @"%C", [letters characterAtIndex:arc4random_uniform((unsigned int)[letters length]) % [letters length]]];
    }
    
    return randomString;
}

+ (NSString*)randomStringOfSize:(NSUInteger)size withString:(NSString *)string {
    NSString *oldString = [self randomStringOfSize:size];
    return [oldString stringByAppendingString:string];
}

+ (UIImage *)fixOrientation:(UIImage*)image {
    if (image.imageOrientation != UIImageOrientationUp) {
        UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
        [image drawInRect:(CGRect){0, 0, image.size}];
        UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        image = normalizedImage;
    }
    return image;
}

@end
