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
//  RayzTableViewCell.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 17/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzTableViewCell.h"

#import "Rayz.h"
#import "Attachments.h"

@interface RayzTableViewCell () <UIActionSheetDelegate>

@property (weak, nonatomic) Rayz * rayz;

@property (weak, nonatomic) IBOutlet UIView *cellContentView;
@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;
@property (weak, nonatomic) IBOutlet UILabel *messageLabel;
@property (weak, nonatomic) IBOutlet UITextView *messageTextView;
@property (weak, nonatomic) IBOutlet UILabel *infoLabel;
@property (weak, nonatomic) IBOutlet UILabel *distanceLabel;
@property (weak, nonatomic) IBOutlet UIView *colorTrack;
@property (weak, nonatomic) IBOutlet UIView *colorCircle;
@property (weak, nonatomic) IBOutlet UIImageView *attachmentsImageView;
@property (weak, nonatomic) IBOutlet UIImageView *reportImageView;
@property (weak, nonatomic) IBOutlet UIButton *starUnstar;
@property (weak, nonatomic) IBOutlet UIButton *rerayz;
@property (weak, nonatomic) IBOutlet UIButton *more;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint * attachmentsWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint * reportWidth;

@end

@implementation RayzTableViewCell

- (void)awakeFromNib {
    [[[self colorCircle] layer] setCornerRadius:[[self colorCircle] frame].size.height/2];
    
    if ([self messageTextView] != nil) {
        [[self messageTextView] setTextContainerInset:UIEdgeInsetsZero];
        [[self messageTextView] setContentInset:UIEdgeInsetsZero];
        [[self messageTextView] scrollRangeToVisible:NSMakeRange(0, 1)];
    }
    
    [[[self cellContentView] layer] setShadowColor:[UIColor darkGrayColor].CGColor];
    [[[self cellContentView] layer] setShadowOffset:CGSizeZero];
    [[[self cellContentView] layer] setShadowRadius:0.3];
    [[[self cellContentView] layer] setShadowOpacity:0.4];
}

- (void)setTextSelectable:(BOOL)textSelectable {
    _textSelectable = textSelectable;
    if ([self messageTextView] != nil) {
        [[self messageTextView] setSelectable:_textSelectable];
        [[self messageTextView] setUserInteractionEnabled:_textSelectable];
    }
}

- (IBAction)starOrUnstar:(id)sender {
    if ([[[self rayz] starred] boolValue] == YES) {
        [[self rayz] unstar];
        [[self starUnstar] setTitle:@"star" forState:UIControlStateNormal];
    }
    else {
        [[self rayz] star];
        [[self starUnstar] setTitle:@"unstar" forState:UIControlStateNormal];
    }
}

- (IBAction)rerayz:(id)sender {
    [[self rayz] rerayzRayz];
}

- (IBAction)more:(id)sender {
    if ([[self rayz] status] != nil && [[[self rayz] status] isEqualToString:kApiRayzStatusFailed]) {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Remove",@"Copy",@"Resend", nil];
        [moreActions showInView:sender];
    }
    else if ([[[self rayz] attachments] totalCount] > 0) {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Remove",@"Copy",@"Report",@"Show attachments", nil];
        [moreActions showInView:sender];
    }
    else {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Remove",@"Copy",@"Report", nil];
        [moreActions showInView:sender];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 0) {
        [[self rayz] deleteRayz];
    }
    else if (buttonIndex == 1) {
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        [pasteboard setString:[[self rayz] rayz_message]];
    }
    else if (buttonIndex == 2) {
        if ([[[self rayz] status] isEqualToString:kApiRayzStatusFailed]) {
            [[self rayz] create];
        }
        else {
            [[self rayz] reportRayz];
        }
    }
    else if (buttonIndex == 3) {
        if ([[[self rayz] attachments] totalCount] > 0) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kShowAttachmentsNotification object:nil userInfo:@{kAttachmentsObjectAttributeName: [[self rayz] attachments]}];
        }
    }
}

#pragma mark Configuration methods
- (void)configureCellWithRayz:(Rayz *)rayz {
    _rayz = rayz;

    [[self timestampLabel] setText:[_rayz stringFromTimestamp]];
    if ([self messageTextView]) {
        [[self messageTextView] setText:[_rayz rayz_message]];
    }
    else {
        [[self messageLabel] setText:[_rayz rayz_message]];
    }
    [[self infoLabel] setText:[_rayz rayzInfo]];
    [[self distanceLabel] setText:[_rayz distanceString]];
    UIColor * rColor = [rayz rayzColor];
    [[self colorTrack] setBackgroundColor:rColor];
    [[self colorCircle] setBackgroundColor:rColor];
    
    if ([rayz status] == nil || [[rayz status] isEqualToString:kApiRayzStatusRayzed]) {
        [[self starUnstar] setEnabled:YES];
        [[self rerayz] setEnabled:YES];
//        [[self more] setEnabled:YES];
    }
    else {
        [[self starUnstar] setEnabled:NO];
        [[self rerayz] setEnabled:NO];
//        [[self more] setEnabled:NO];
    }
    if ([rayz isUnread] || [rayz numOfUnreadReplies] > 0) {
        if ([self messageTextView]) {
            [[self messageTextView] setTextColor:[RayzitTheme mainAppColor]];
        }
        else {
            [[self messageLabel] setTextColor:[RayzitTheme mainAppColor]];
        }
    }
    else {
        if ([self messageTextView]) {
            [[self messageTextView] setTextColor:[UIColor darkGrayColor]];
        }
        else {
            [[self messageLabel] setTextColor:[UIColor darkGrayColor]];
        }
    }
    
    if ([[rayz starred] boolValue] == YES) {
        [[self starUnstar] setTitle:@"unstar" forState:UIControlStateNormal];
    }
    else {
        [[self starUnstar] setTitle:@"star" forState:UIControlStateNormal];
    }
    
    if ([[rayz attachments] totalCount]==0) {
        [[self attachmentsWidth] setConstant:0];
    }
    else {
        [[self attachmentsWidth] setConstant:20];
    }
    if ([[rayz report] integerValue]==0) {
        [[self reportWidth] setConstant:0];
    }
    else {
        [[self reportWidth] setConstant:20];
    }
}

@end
