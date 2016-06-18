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
//  RayzTableViewDataSource.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 10/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzTableViewDataSource.h"

#import "Rayz.h"
#import "RayzReply.h"
#import "Attachments.h"

#import "RayzTableViewCell.h"
#import "RayzAnswerTableViewCell.h"

@interface RayzTableViewDataSource () <NSFetchedResultsControllerDelegate, UITableViewDataSource, UITableViewDelegate>

//@property (nonatomic, getter=shouldGetAnswers) BOOL getAnswers;
@property (nonatomic, strong) NSFetchedResultsController * fetchedRepliesController;

@end

@implementation RayzTableViewDataSource

- (void)dealloc {
    [[self fetchedRepliesController] setDelegate:nil];
}

- (void)setRayz:(Rayz *)rayz {
    _rayz = rayz;
//    if (_rayz && ([_rayz status]==nil || [[_rayz status] isEqualToString:kApiRayzStatusRayzed])) {
//        NSPredicate * repliesPredicate = [NSPredicate predicateWithFormat:@"rayzId MATCHES %@",[_rayz rayzId]];
//        [self setFetchedRepliesController:[RayzReply MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:repliesPredicate groupBy:nil delegate:self]];
//    }
}

- (void)setActive:(BOOL)active {
    _active = active;
    if (_active) {
        [self performSelector:@selector(requestRayzAnswers) withObject:nil afterDelay:0.2];
    }
}

- (void)requestRayzAnswers {
    if (_active) {
        if (_rayz && ([_rayz status]==nil || [[_rayz status] isEqualToString:kApiRayzStatusRayzed])) {
            NSPredicate * repliesPredicate = [NSPredicate predicateWithFormat:@"rayzId MATCHES %@",[_rayz rayzId]];
            [self setFetchedRepliesController:[RayzReply MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:repliesPredicate groupBy:nil delegate:self]];
            [[self tableView] reloadSections:[NSIndexSet indexSetWithIndex:1] withRowAnimation:UITableViewRowAnimationAutomatic];
            [RemoteStore getRayzReplies:_rayz];
//            [RemoteStore getRayzAnswers:_rayz];
        }
    }
}

- (void)setTableView:(UITableView *)tableView {
    _tableView = tableView;
    [tableView setDataSource:self];
    [tableView setDelegate:self];
//    [tableView setBounces:NO];
    [tableView registerNib:[UINib nibWithNibName:@"RayzTableViewCell" bundle:nil] forCellReuseIdentifier:kRayzCellIdentifier];
    [tableView registerNib:[UINib nibWithNibName:@"RayzAnswerTableViewCell" bundle:nil] forCellReuseIdentifier:kReplyCellIdentifier];
    [tableView setTableFooterView:[[UIView alloc] initWithFrame:CGRectZero]];
    [tableView setSeparatorStyle:UITableViewCellSeparatorStyleNone];
}

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    if ([self isActive]) {
        [[self tableView] beginUpdates];
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    if ([self isActive]) {
        [[self tableView] endUpdates];
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    if ([self isActive]) {
        switch (type) {
            case NSFetchedResultsChangeInsert: {
                newIndexPath = [NSIndexPath indexPathForRow:newIndexPath.row inSection:1];
                [[self tableView] insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeDelete: {
                indexPath = [NSIndexPath indexPathForRow:indexPath.row inSection:1];
                [[self tableView] deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeUpdate: {
                RayzReply * reply = [[self fetchedRepliesController] objectAtIndexPath:indexPath];
                indexPath = [NSIndexPath indexPathForRow:indexPath.row inSection:1];
                [(RayzAnswerTableViewCell*)[[self tableView] cellForRowAtIndexPath:indexPath] configureCellWithRayzReply:reply];
                break;
            }
            case NSFetchedResultsChangeMove: {
                indexPath = [NSIndexPath indexPathForRow:indexPath.row inSection:1];
                newIndexPath = [NSIndexPath indexPathForRow:newIndexPath.row inSection:1];
                [self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
                [self.tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
        }
    }
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section==0) {
        return 1;
    }
    return [[[self fetchedRepliesController] fetchedObjects] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath section] == 0) {
        RayzTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:kRayzCellIdentifier forIndexPath:indexPath];
        
        [cell configureCellWithRayz:[self rayz]];
        [[cell contentView] setBackgroundColor:[RayzitTheme mainAppColor]];
        
        return cell;
    }
    else {
        RayzAnswerTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:kReplyCellIdentifier forIndexPath:indexPath];
        
        [cell configureCellWithRayzReply:[[self fetchedRepliesController] objectAtIndexPath:[NSIndexPath indexPathForRow:indexPath.row inSection:0]]];
        
        return cell;
    }
}

#pragma mark Table view delegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    static CGFloat const defaultHeight = 120;
    static CGFloat const minLabelHeight = 30;
    CGFloat labelWidth = [[self tableView] frame].size.width - 30;
    CGSize size;
    
    if ([indexPath section] == 0) {
        size = [[[self rayz] rayz_message] boundingRectWithSize:CGSizeMake(labelWidth-10, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:17]} context:NULL].size;
//        size.height += 10;
    }
    else {
        RayzReply * r = [[self fetchedRepliesController] objectAtIndexPath:[NSIndexPath indexPathForRow:indexPath.row inSection:0]];
    
        size = [[r rayz_reply_message] boundingRectWithSize:CGSizeMake(labelWidth, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:17]} context:NULL].size;
    }
    
    CGFloat estimatedHeight = defaultHeight - minLabelHeight + size.height + 15;
    
    return estimatedHeight;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (_delegate) {
        if ([indexPath section] == 0) {
            if ([[[self rayz] status] isEqualToString:kApiRayzStatusFailed]) {
                [[self rayz] create];
            }
            else if ([[self rayz] attachments]!=nil && [[[self rayz] attachments] totalCount] > 0) {
                [_delegate tableView:tableView shouldShowAttachments:[[self rayz] attachments]];
            }
        }
        else {
            RayzReply * rr = [[self fetchedRepliesController] objectAtIndexPath:[NSIndexPath indexPathForRow:indexPath.row inSection:0]];
            if ([[rr status] isEqualToString:kApiRayzStatusFailed]) {
                [rr create];
            }
            else if ([rr attachments] != nil && [[rr attachments] totalCount] > 0) {
                [_delegate tableView:tableView shouldShowAttachments:[rr attachments]];
            }
        }
    }
}

@end
