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
//  RayzViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 20/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzViewController.h"
#import "Rayz.h"
#import "RayzReply.h"
#import "Attachments.h"
#import "RayzAnswerTableViewCell.h"
#import "AttachmentsCollectionViewController.h"
#import "NewRayzViewController.h"

@interface RayzViewController () <NSFetchedResultsControllerDelegate, UITableViewDataSource, UITableViewDelegate, UIActionSheetDelegate>

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *fetchedRepliesController;
@property (nonatomic, strong) Rayz *currentRayz;

// UI
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;
@property (weak, nonatomic) IBOutlet UILabel *distanceLabel;
@property (weak, nonatomic) IBOutlet UITextView *rayzMessageTextView;
@property (weak, nonatomic) IBOutlet UIView *colorTrack;
@property (weak, nonatomic) IBOutlet UILabel *rayzRepliesInfo;
@property (weak, nonatomic) IBOutlet UIImageView *reportImageView;
@property (weak, nonatomic) IBOutlet UIImageView *attachmentsImageView;
@property (weak, nonatomic) IBOutlet UILabel *rerayzLabel;
@property (weak, nonatomic) IBOutlet UILabel *starLabel;

@property (weak, nonatomic) IBOutlet UITableView *repliesTableView;
@property (weak, nonatomic) IBOutlet UIView *bottomView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint * reportWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint * attachmentsWidth;

@end

static NSString * const rayzReplyCellIdentifier = @"rayzReplyCell";

@implementation RayzViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[self repliesTableView] registerNib:[UINib nibWithNibName:@"RayzAnswerTableViewCell" bundle:nil] forCellReuseIdentifier:rayzReplyCellIdentifier];
    
    if ([self fetchPredicate] == nil) {
        [self setFetchPredicate:[NSPredicate predicateWithFormat:@"hidden == NO"]];
    }
    
    [self drawBorderLineOnBottomView];
    
    [self setFetchedResultsController:[Rayz MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:[self fetchPredicate] groupBy:nil delegate:self]];
    [self updateFetchAndGetAnswers];
//    [self updateUI];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)drawBorderLineOnBottomView {
    CGSize screenSize = [[UIScreen mainScreen] bounds].size;
    
    UIBezierPath *path = [UIBezierPath bezierPath];
    [path moveToPoint:CGPointMake(0, 0)];
    [path addLineToPoint:CGPointMake(screenSize.width, 0)];
    CAShapeLayer *shapeLayer = [CAShapeLayer layer];
    shapeLayer.path = [path CGPath];
    shapeLayer.strokeColor = [[UIColor lightGrayColor] CGColor];
    shapeLayer.lineWidth = .4f;
    [[[self bottomView] layer] addSublayer:shapeLayer];
}

- (void)updateFetchAndGetAnswers {
    
    [self setCurrentRayz:[[self fetchedResultsController] objectAtIndexPath:[NSIndexPath indexPathForRow:self.currentRayzIndex inSection:0]]];
    BOOL isUnread = [[self currentRayz] numOfUnreadReplies] > 0;
    [[self currentRayz] setUnread:@(isUnread)];
//    [[self currentRayz] performSelectorOnMainThread:@selector(setUnread:) withObject:@(isUnread) waitUntilDone:YES];
//    [RemoteStore save];
    
    NSPredicate * repliesPredicate = [NSPredicate predicateWithFormat:@"rayzId MATCHES %@",[self.currentRayz rayzId]];
    [self setFetchedRepliesController:[RayzReply MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:repliesPredicate groupBy:nil delegate:self]];
    
    [[self repliesTableView] reloadData];
    [RemoteStore getRayzReplies:[self currentRayz]];
//    [RemoteStore getRayzAnswers:[self currentRayz]];
}

#pragma mark UI updates
- (void)updateUI {
    
    long curr = self.currentRayzIndex + 1;
    long total = [[self.fetchedResultsController sections][0] numberOfObjects];
    [self setTitle:[NSString stringWithFormat:@"%ld of %ld",curr,total]];
    
    [[self timeLabel] setText:[self.currentRayz stringFromTimestamp]];
    [[self distanceLabel] setText:[self.currentRayz distanceString]];
    [[self rayzMessageTextView] setDataDetectorTypes:UIDataDetectorTypeNone];
    [[self rayzMessageTextView] setText:[self.currentRayz rayz_message]];
    [[self rayzMessageTextView] setDataDetectorTypes:(UIDataDetectorTypeLink|UIDataDetectorTypePhoneNumber)];
    [[self rayzMessageTextView] setTextContainerInset:UIEdgeInsetsZero];
    [[self rayzMessageTextView] scrollRangeToVisible:NSMakeRange(0, 1)];
    [[self colorTrack] setBackgroundColor:[self.currentRayz rayzColor]];
    [[self rayzRepliesInfo] setText:[self.currentRayz rayzRepliesInfo]];
    [[self starLabel] setText:[NSString stringWithFormat:@"%@",[[self.currentRayz starred] stringValue]]];
    [[self starLabel] sizeToFit];
    [[self rerayzLabel] setText:[NSString stringWithFormat:@"%@",[[self.currentRayz rerayz] stringValue]]];
    [[self rerayzLabel] sizeToFit];
    
    if ([[self.currentRayz attachments] totalCount]==0) {
        [[self attachmentsWidth] setConstant:0];
    }
    else {
        [[self attachmentsWidth] setConstant:20];
    }
    if ([[self.currentRayz report] integerValue]==0) {
        [[self reportWidth] setConstant:0];
    }
    else {
        [[self reportWidth] setConstant:20];
    }
}

- (IBAction)showAttachmentsIfAny:(id)sender {
    if ([[[self currentRayz] attachments] totalCount] > 0) {
        AttachmentsCollectionViewController * vc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"attachmentsController"];
        [vc setAttachments:[self.currentRayz attachments]];
        
        if ([[self navigationController] respondsToSelector:@selector(showViewController:sender:)]) {
            [[self navigationController] showViewController:vc sender:self];
        }
        else {
            [[self navigationController] pushViewController:vc animated:YES];
        }
    }
}

- (IBAction)actionsForRayz:(UILongPressGestureRecognizer*)sender {
    if ([sender state]==UIGestureRecognizerStateBegan) {
        NSString * starUnstar = [[[self currentRayz] starred] boolValue] ? @"Unstar" : @"Star";
        UIActionSheet * rayzActions = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:starUnstar,@"Rerayz",@"Remove",@"Copy",@"Report",nil];
        [rayzActions showFromTabBar:[[[self navigationController] tabBarController] tabBar]];
    }
}

- (IBAction)replyToRayz:(id)sender {
    [self performSegueWithIdentifier:@"replyRayz" sender:sender];
}

- (IBAction)changeRayz:(UISegmentedControl*)sender {
    switch ([sender selectedSegmentIndex]) {
        case 0:
            [self goToPreviousRayz];
            break;
        case 1:
            [self goToNextRayz];
            break;
        default:
            break;
    }
}

- (void)goToNextRayz {
    _currentRayzIndex++;
    if (_currentRayzIndex >= [[[self fetchedResultsController] fetchedObjects] count]) {
        _currentRayzIndex = 0;
    }
    [self updateFetchAndGetAnswers];
//    [self performSelectorInBackground:@selector(updateFetchAndGetAnswers) withObject:nil];
}

- (void)goToPreviousRayz {
    _currentRayzIndex--;
    if (_currentRayzIndex < 0) {
        _currentRayzIndex = [[[self fetchedResultsController] fetchedObjects] count] - 1;
    }
    [self updateFetchAndGetAnswers];
//    [self performSelectorInBackground:@selector(updateFetchAndGetAnswers) withObject:nil];
}

#pragma mark FetchedResultsController delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    if (controller == [self fetchedRepliesController]) {
        [[self repliesTableView] beginUpdates];
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    if (controller == [self fetchedRepliesController]) {
        [[self repliesTableView] endUpdates];
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    
    if ([[[controller fetchRequest] entityName] isEqualToString:NSStringFromClass([Rayz class])]) {
        if (type == NSFetchedResultsChangeDelete) {
            [self goToNextRayz];
        }
        [self updateUI];
//        [self performSelectorOnMainThread:@selector(updateUI) withObject:nil waitUntilDone:YES];
    }
    else {
        switch (type) {
            case NSFetchedResultsChangeInsert: {
                [[self repliesTableView] insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeDelete: {
                [[self repliesTableView] deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeUpdate: {
                RayzReply * reply = [[self fetchedRepliesController] objectAtIndexPath:indexPath];
                [(RayzAnswerTableViewCell*)[[self repliesTableView] cellForRowAtIndexPath:indexPath] configureCellWithRayzReply:reply];
//                [[self currentRayz] setUnread:@([[self currentRayz] numOfUnreadReplies] > 0)];
                break;
            }
            case NSFetchedResultsChangeMove: {
                [self.repliesTableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
                [self.repliesTableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
        }
    }
}

#pragma mark TableView
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return [[[self fetchedRepliesController] sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *sections = [self.fetchedRepliesController sections];
    id<NSFetchedResultsSectionInfo> sectionInfo = [sections objectAtIndex:section];
    
    if ([sectionInfo numberOfObjects] == 0) {
        UIView * bgView = [[UIView alloc] initWithFrame:[tableView frame]];
        [tableView setBackgroundView:bgView];
        [bgView addSubview:[RayzitTheme tableViewBackgroundLabelWithTitle:@"hmmmmm," message:@"It seems like no one replied yet.."]];
    }
    else {
        [tableView setBackgroundView:nil];
    }
    
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    RayzAnswerTableViewCell * cell = [tableView dequeueReusableCellWithIdentifier:rayzReplyCellIdentifier forIndexPath:indexPath];
    
    RayzReply * reply = [[self fetchedRepliesController] objectAtIndexPath:indexPath];
    [cell configureCellWithRayzReply:reply];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    static CGFloat const defaultHeight = 120;
    static CGFloat const minLabelHeight = 30;
    CGFloat labelWidth = [[self repliesTableView] frame].size.width - 30;
    
    RayzReply * r = [[self fetchedRepliesController] objectAtIndexPath:indexPath];
    
    CGSize size = [[r rayz_reply_message] boundingRectWithSize:CGSizeMake(labelWidth, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:17]} context:NULL].size;
    
    CGFloat estimatedHeight = defaultHeight - minLabelHeight + size.height + 10;
    
    return estimatedHeight;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    RayzReply * r = [[self fetchedRepliesController] objectAtIndexPath:indexPath];
    
    if ([[r status] isEqualToString:kApiRayzStatusFailed]) {
        [r create];
    }
    else {
        if ([r isUnread]) {
            [r setUnread:@(NO)];
            [[self currentRayz] setUnread:@([[self currentRayz] numOfUnreadReplies] > 0)];
        }
        if ([[r attachments] totalCount] > 0) {
            AttachmentsCollectionViewController * vc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"attachmentsController"];
            [vc setAttachments:[r attachments]];
            
            if ([[self navigationController] respondsToSelector:@selector(showViewController:sender:)]) {
                [[self navigationController] showViewController:vc sender:self];
            }
            else {
                [[self navigationController] pushViewController:vc animated:YES];
            }
        }
    }
}

#pragma mark ActiuonSheet delegate
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex != [actionSheet cancelButtonIndex]) {
        Rayz * r = [self currentRayz];
        switch (buttonIndex) {
            case 0: {
                if ([[r starred] boolValue]) {
                    [r unstar];
                }
                else {
                    [r star];
                }
                break;
            }
            case 1: {
                [r rerayzRayz];
                break;
            }
            case 2: {
                [r deleteRayz];
                break;
            }
            case 3: {
                UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
                [pasteboard setString:[r rayz_message]];
                break;
            }
            case 4: {
                [r reportRayz];
                break;
            }
            default:
                break;
        }
    }
}

#pragma mark Navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([[segue identifier] isEqualToString:@"replyRayz"]) {
        NewRayzViewController * dest = (NewRayzViewController*)[[segue destinationViewController] topViewController];
        [dest setReply:YES];
        [dest setReplyTo:[self currentRayz]];
    }
}

@end
