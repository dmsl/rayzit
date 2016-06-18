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
//  RayzTextCollectionViewCell.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 16/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzTextCollectionViewCell.h"
#import <SZTextView/SZTextView.h>

@interface RayzTextCollectionViewCell () <UITextViewDelegate>

@property (weak, nonatomic) IBOutlet SZTextView *messageTextView;
@property (weak, nonatomic) IBOutlet UILabel *characterCountLabel;
@property (weak, nonatomic) IBOutlet UIImageView *rayzDistanceImageView;
@property (weak, nonatomic) IBOutlet UILabel *rayzDistanceLabel;

@end

@implementation RayzTextCollectionViewCell

#pragma mark Initializers
- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self commonInit];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self commonInit];
    }
    return self;
}

- (instancetype)init {
    if (self = [super init]) {
        [self commonInit];
    }
    return self;
}

- (void)prepareForReuse {
    [super prepareForReuse];
    [self commonInit];
}

- (void)commonInit {
    [[self messageTextView] setDelegate:self];
    UIToolbar * accessory = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, [self frame].size.width, 44)];
    
    UIBarButtonItem * flex = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    UIBarButtonItem * done = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:[self messageTextView] action:@selector(resignFirstResponder)];
    
    [accessory setItems:@[flex, done]];
    [[self messageTextView] setInputAccessoryView:accessory];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self commonInit];
}

#pragma mark PlaceHolder accessors
- (void)setMessagePlaceholder:(NSString *)messagePlaceholder {
    [[self messageTextView] setPlaceholder:messagePlaceholder];
}

- (NSString *)messagePlaceholder {
    return [[self messageTextView] placeholder];
}

#pragma mark MaxLength setter
- (void)setMaxMessageLength:(NSInteger)maxMessageLength {
    _maxMessageLength = maxMessageLength;
    [self updateCharacterCounter];
}

#pragma mark Rayz Distance setter
- (void)setMaxDistance:(NSString *)maxDistance {
    [[self rayzDistanceLabel] setHidden:NO];
    [[self rayzDistanceImageView] setHidden:NO];
    _maxDistance = maxDistance;
    [[self rayzDistanceLabel] setText:maxDistance];
}

- (void)hideDistanceLabel {
    [[self rayzDistanceLabel] setHidden:YES];
    [[self rayzDistanceImageView] setHidden:YES];
}

#pragma mark MessageText getter
- (NSString *)messageText {
    [[self messageTextView] resignFirstResponder];
    return [[[self messageTextView] text] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

#pragma mark UITextViewDelegate
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    NSUInteger newLength = [textView.text length] + [text length] - range.length;
    if (newLength > [self maxMessageLength]) {
        NSInteger charsToDelete = newLength - [self maxMessageLength];
        NSString *trimmedString = [text substringToIndex:([text length]-charsToDelete)];
        
        UITextPosition *beginning = textView.beginningOfDocument;
        UITextPosition *start = [textView positionFromPosition:beginning offset:range.location];
        UITextPosition *end = [textView positionFromPosition:start offset:range.length];
        UITextRange *textRange = [textView textRangeFromPosition:start toPosition:end];
        
        [textView replaceRange:textRange withText:trimmedString];
        return NO;
    }
    else {
        [self updateCharacterCounterWithLength:newLength];
        return YES;
    }
}

- (void)textViewDidChange:(UITextView *)textView {
    [self updateCharacterCounter];
}

#pragma mark Length counter update
- (void)updateCharacterCounter {
    NSInteger currentLength = [[[self messageTextView] text] length];
    [self updateCharacterCounterWithLength:currentLength];
}

- (void)updateCharacterCounterWithLength:(NSInteger)length {
    [[self characterCountLabel] setText:[NSString stringWithFormat:@"%ld/%ld",(long)length,(long)[self maxMessageLength]]];
}

@end
