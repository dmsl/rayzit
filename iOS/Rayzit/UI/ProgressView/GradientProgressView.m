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
//  GradientProgressView.m
//  GradientProgressView
//
//  Created by Nick Jensen on 11/22/13.
//  Copyright (c) 2013 Nick Jensen. All rights reserved.
//

#import "GradientProgressView.h"

@implementation GradientProgressView

@synthesize animating, progress;

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self commonInit];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    
    if ((self = [super initWithFrame:frame])) {
        [self commonInit];
    }
    return self;
}

- (void)commonInit {
    // Use a horizontal gradient
    
    CAGradientLayer *layer = (id)[self layer];
    [layer setStartPoint:CGPointMake(0.0, 0.5)];
    [layer setEndPoint:CGPointMake(1.0, 0.5)];
    
    NSMutableArray *colors = [NSMutableArray array];
//    for (NSInteger deg = 0; deg <= 360; deg += 5) {
//        
//        UIColor *color;
//        color = [UIColor colorWithHue:1.0 * deg / 360.0
//                           saturation:1.0
//                           brightness:1.0
//                                alpha:1.0];
//        [colors addObject:(id)[color CGColor]];
//    }
    [colors addObject:(id)[UIColor redColor].CGColor];
    [colors addObject:(id)[UIColor yellowColor].CGColor];
    [colors addObject:(id)[UIColor greenColor].CGColor];
    [layer setColors:[NSArray arrayWithArray:colors]];
    
    // Create another layer to use as a mask. The width of this layer will
    // be modified to reflect the current progress value.
    
    maskLayer = [CALayer layer];
    [maskLayer setFrame:CGRectMake(0, 0, 0, self.frame.size.height)];
    [maskLayer setBackgroundColor:[[UIColor blackColor] CGColor]];
    [layer setMask:maskLayer];
}

+ (Class)layerClass {
    
    // Tells UIView to use CAGradientLayer as our backing layer
    
    return [CAGradientLayer class];
}

- (void)setProgress:(CGFloat)value {
    
    if (progress != value) {
        
        // progress values go from 0.0 to 1.0
        
        progress = MIN(1.0, fabs(value));
        [self setNeedsLayout];
    }
}

- (void)layoutSubviews {
    
    // Resize our mask layer based on the current progress
    
    CGRect maskRect = [maskLayer frame];
    maskRect.size.width = CGRectGetWidth([self bounds]) * progress;
    [maskLayer setFrame:maskRect];
}

- (NSArray *)shiftColors:(NSArray *)colors {
    
    // Moves the last item in the array to the front
    // shifting all the other elements.
    
    NSMutableArray *mutable = [colors mutableCopy];
    id last = [mutable lastObject];
    [mutable removeLastObject];
    [mutable insertObject:last atIndex:0];
    return [NSArray arrayWithArray:mutable];
}

- (void)performAnimation {
    
    // Update the colors on the model layer
    
    CAGradientLayer *layer = (id)[self layer];
    NSArray *fromColors = [layer colors];
    NSArray *toColors = [self shiftColors:fromColors];
    [layer setColors:toColors];
    
    // Create an animation to slowly move the hue gradient left to right.
    
    CABasicAnimation *animation;
    animation = [CABasicAnimation animationWithKeyPath:@"colors"];
    [animation setFromValue:fromColors];
    [animation setToValue:toColors];
    [animation setDuration:0.08];
    [animation setRemovedOnCompletion:YES];
    [animation setFillMode:kCAFillModeForwards];
    [animation setTimingFunction:[CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionLinear]];
    [animation setDelegate:self];
    
    // Add the animation to our layer
    
    [layer addAnimation:animation forKey:@"animateGradient"];
}

- (void)animationDidStop:(CAAnimation *)animation finished:(BOOL)flag {
    
    if ([self isAnimating]) {
        
        [self performAnimation];
    }
}

- (void)startAnimating {
    
    if (![self isAnimating]) {
        
        animating = YES;
        
        [self performAnimation];
    }
}

- (void)stopAnimating {
    
    if ([self isAnimating]) {
        
        animating = NO;
    }
}

@end
