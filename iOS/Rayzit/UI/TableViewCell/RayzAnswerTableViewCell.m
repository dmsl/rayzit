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
//  RayzAnswerTableViewCell.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 28/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzAnswerTableViewCell.h"

#import "RayzReply.h"
#import "Attachments.h"

@interface RayzAnswerTableViewCell () <UIActionSheetDelegate>

@property (weak, nonatomic) RayzReply * rayzReply;

@property (weak, nonatomic) IBOutlet UIView *cellContentView;
@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;
@property (weak, nonatomic) IBOutlet UILabel *messageLabel;
@property (weak, nonatomic) IBOutlet UILabel *infoLabel;
@property (weak, nonatomic) IBOutlet UIView *colorTrack;
@property (weak, nonatomic) IBOutlet UIView *colorCircle;
@property (weak, nonatomic) IBOutlet UIImageView *attachmentsImageView;
@property (weak, nonatomic) IBOutlet UIImageView *reportImageView;
@property (weak, nonatomic) IBOutlet UIButton *powerUp;
@property (weak, nonatomic) IBOutlet UIButton *powerDown;
@property (weak, nonatomic) IBOutlet UIButton *more;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint * attachmentsWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint * reportWidth;

@end

@implementation RayzAnswerTableViewCell

- (void)awakeFromNib {
    [[[self colorCircle] layer] setCornerRadius:[[self colorCircle] frame].size.height/2];
    
    [[[self cellContentView] layer] setShadowColor:[UIColor darkGrayColor].CGColor];
    [[[self cellContentView] layer] setShadowOffset:CGSizeZero];
    [[[self cellContentView] layer] setShadowRadius:0.3];
    [[[self cellContentView] layer] setShadowOpacity:0.3];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (IBAction)powerUp:(id)sender {
    [[self rayzReply] powerUp];
}

- (IBAction)powerDown:(id)sender {
    [[self rayzReply] powerDown];
}

- (IBAction)more:(id)sender {
    if ([[self rayzReply] status] != nil && [[[self rayzReply] status] isEqualToString:kApiRayzStatusFailed]) {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Copy",@"Resend",@"Remove", nil];
        [moreActions showInView:sender];
    }
    else if ([[[self rayzReply] attachments] totalCount] > 0) {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Copy",@"Report",@"Show attachments", nil];
        [moreActions showInView:sender];
    }
    else {
        UIActionSheet * moreActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Copy",@"Report", nil];
        [moreActions showInView:sender];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 0) {
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        [pasteboard setString:[[self rayzReply] rayz_reply_message]];
    }
    else if (buttonIndex == 1) {
        if ([[[self rayzReply] status] isEqualToString:kApiRayzStatusFailed]) {
            [[self rayzReply] create];
        }
        else {
            [[self rayzReply] reportRayzReply];
        }
    }
    else if (buttonIndex == 2) {
        if ([[[self rayzReply] status] isEqualToString:kApiRayzStatusFailed]) {
            [[self rayzReply] MR_deleteEntity];
        }
        else if ([[[self rayzReply] attachments] totalCount] > 0) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kShowAttachmentsNotification object:nil userInfo:@{kAttachmentsObjectAttributeName: [[self rayzReply] attachments]}];
        }
    }
}

#pragma mark Configuration
- (void)configureCellWithRayzReply:(RayzReply *)rayzReply {
    _rayzReply = rayzReply;
    
    [[self timestampLabel] setText:[_rayzReply stringFromTimestamp]];
    [[self messageLabel] setText:[_rayzReply rayz_reply_message]];
    [[self infoLabel] setText:[_rayzReply rayzReplyInfo]];
    UIColor * rColor = [_rayzReply rayzReplyColor];
    [[self colorTrack] setBackgroundColor:rColor];
    [[self colorCircle] setBackgroundColor:rColor];
    
    if ([_rayzReply status] == nil || [[_rayzReply status] isEqualToString:kApiRayzStatusRayzed]) {
        [[self powerUp] setEnabled:YES];
        [[self powerDown] setEnabled:YES];
        [[self more] setEnabled:YES];
    }
    else {
        [[self powerUp] setEnabled:NO];
        [[self powerDown] setEnabled:NO];
        [[self more] setEnabled:NO];
    }
    
    if ([_rayzReply isUnread]) {
        [[self messageLabel] setTextColor:[RayzitTheme mainAppColor]];
    }
    else {
        [[self messageLabel] setTextColor:[UIColor darkGrayColor]];
    }
    
    if ([[_rayzReply attachments] totalCount]==0) {
        [[self attachmentsWidth] setConstant:0];
    }
    else {
        [[self attachmentsWidth] setConstant:20];
    }
    if ([[_rayzReply report] integerValue]==0) {
        [[self reportWidth] setConstant:0];
    }
    else {
        [[self reportWidth] setConstant:20];
    }
}

@end
