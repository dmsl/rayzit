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
//  UserPowerCollectionViewCell.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 17/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "UserPowerCollectionViewCell.h"
#import "GradientProgressView.h"

@interface UserPowerCollectionViewCell ()

@property (weak, nonatomic) IBOutlet GradientProgressView *userPowerBar;

@end

@implementation UserPowerCollectionViewCell

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [[User appUser] addObserver:self forKeyPath:@"userPower" options:NSKeyValueObservingOptionNew context:NULL];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [[self userPowerBar] setProgress:[[User appUser] userPower]/100.0];
}

- (void)dealloc {
    [[User appUser] removeObserver:self forKeyPath:@"userPower"];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if ([keyPath isEqualToString:@"userPower"]) {
        dispatch_async(dispatch_get_main_queue(), ^{
           [[self userPowerBar] setProgress:[[User appUser] userPower]/100.0]; 
        });
    }
}

@end
