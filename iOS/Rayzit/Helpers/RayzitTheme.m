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
//  RayzitTheme.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 1/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzitTheme.h"

#import "UIColor+CAHexConverter.h"

static NSString * const kMainOrangeColorHex = @"#ED3D23";

static NSString * const kNormalRayzColor = @"#7A7A7A";
static NSString * const kStarredRayzColor = @"#F7D55C";
static NSString * const kMyRayzColor = @"#0558A7";
static NSString * const kFailedRayzColor = @"#E60000";

@implementation RayzitTheme

+ (void)customizeAppAppearance {
    
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    [[UITabBar appearance] setTintColor:[UIColor colorWithHexString:kMainOrangeColorHex]];
    [[UIToolbar appearance] setTintColor:[UIColor colorWithHexString:kMainOrangeColorHex]];
    
}

+ (UIColor *)mainAppColor {
    return [UIColor colorWithHexString:kMainOrangeColorHex];
}

+ (UIColor *)normalRayzColor {
    return [UIColor colorWithHexString:kNormalRayzColor];
}

+ (UIColor *)starredRayzColor {
    return [UIColor colorWithHexString:kStarredRayzColor];
}

+ (UIColor *)myRayzColor {
    return [UIColor colorWithHexString:kMyRayzColor];
}

+ (UIColor *)failedRayzColor {
    return [UIColor colorWithHexString:kFailedRayzColor];
}

+ (UILabel *)tableViewBackgroundLabelWithTitle:(NSString *)title message:(NSString *)message {
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
    [label setNumberOfLines:0];
    
    NSAttributedString * titleStr = [[NSAttributedString alloc] initWithString:title attributes:@{
                                                                                                  NSFontAttributeName: [UIFont systemFontOfSize:27],
                                                                                                  NSForegroundColorAttributeName: [UIColor darkGrayColor]
                                                                                                  }];
    NSAttributedString * messageStr = [[NSAttributedString alloc] initWithString:message attributes:@{
                                                                                                      NSFontAttributeName: [UIFont systemFontOfSize:21],
                                                                                                      NSForegroundColorAttributeName: [UIColor darkGrayColor]
                                                                                                      }];
    
    NSMutableAttributedString * full = [[NSMutableAttributedString alloc] initWithAttributedString:titleStr];
    [full appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n"]];
    [full appendAttributedString:messageStr];
    
    [label setAttributedText:full];
    
    [label sizeToFit];
    
    CGRect r = [label frame];
    r.origin = CGPointMake(15, 45);
    [label setFrame:r];
    
    return label;
}

@end
