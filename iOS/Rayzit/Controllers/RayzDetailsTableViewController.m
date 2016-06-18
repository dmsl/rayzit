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
//  RayzDetailsTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 10/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzDetailsTableViewController.h"
#import "NewRayzViewController.h"
#import "AttachmentsCollectionViewController.h"
#import "Rayz.h"
#import "RayzTableViewDataSource.h"
#import <SwipeView/SwipeView.h>

@interface RayzDetailsTableViewController () <SwipeViewDataSource, SwipeViewDelegate, RayzTableViewDelegate, NSFetchedResultsControllerDelegate>

@property (weak, nonatomic) IBOutlet SwipeView *swipeView;

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
//@property (strong, nonatomic) NSFetchedResultsController *fetchedRepliesController;
@property (nonatomic, strong) Rayz *currentRayz;

@property (nonatomic, strong) NSMutableDictionary * dataSources;

@end

@implementation RayzDetailsTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(showAttachmentsIfAny:) name:kShowAttachmentsNotification object:nil];
    
    [self setDataSources:[[NSMutableDictionary alloc] initWithCapacity:10]];
    
    if ([self fetchPredicate] == nil) {
        [self setFetchPredicate:[NSPredicate predicateWithFormat:@"hidden == NO"]];
    }
    [self setFetchedResultsController:[Rayz MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:[self fetchPredicate] groupBy:nil delegate:self]];
    [self updateCurrentRayz];
    [self updatePageTitle];
}

- (void)viewDidLayoutSubviews {
    [[self swipeView] setCurrentItemIndex:[self currentRayzIndex]];
    
    [super viewDidLayoutSubviews];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [[self swipeView] setWrapEnabled:YES];
    [self updateCurrentRayz];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)updatePageTitle {
    long curr = self.currentRayzIndex + 1;
    long total = [[self.fetchedResultsController sections][0] numberOfObjects];
    [self setTitle:[NSString stringWithFormat:@"%ld of %ld",curr,total]];
}

#pragma mark SwipeViewDatasource
- (NSInteger)numberOfItemsInSwipeView:(SwipeView *)swipeView {
    return [[[self fetchedResultsController] fetchedObjects] count];
}

- (UIView *)swipeView:(SwipeView *)swipeView viewForItemAtIndex:(NSInteger)index reusingView:(UIView *)view {
    UITableView * t = (UITableView*)view;
    if (view == nil) {
        t = [[UITableView alloc] initWithFrame:[swipeView frame]];
    }
    
    Rayz * r = [[self fetchedResultsController] objectAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0]];
    RayzTableViewDataSource * ds = [[self dataSources] objectForKey:[r rayzId]];
    if (ds == nil) {
        ds = [[RayzTableViewDataSource alloc] init];
        [ds setRayz:r];
        [ds setDelegate:self];
        if ([r rayzId] == nil) {
            [r setRayzId:[Utilities randomStringOfSize:15]];
        }
        [[self dataSources] setObject:ds forKey:[r rayzId]];
    }
    [ds setTableView:t];
    return t;
}

#pragma mark SwipeViewDelegate
- (void)swipeViewCurrentItemIndexDidChange:(SwipeView *)swipeView {
    if (_currentRayzIndex != [swipeView currentItemIndex]) {
        Rayz * r = [[self fetchedResultsController] objectAtIndexPath:[NSIndexPath indexPathForItem:_currentRayzIndex inSection:0]];
        RayzTableViewDataSource * ds = [[self dataSources] objectForKey:[r rayzId]];
        [ds setActive:NO];
        self.currentRayzIndex = [swipeView currentItemIndex];
        [self updateCurrentRayz];
        [self updatePageTitle];
        r = [self currentRayz];
        if ([[r status] isEqualToString:kApiRayzStatusFailed] || [[r status] isEqualToString:kApiRayzStatusPending]) {
            [[[self navigationItem] rightBarButtonItem] setEnabled:NO];
        }
        else {
            [[[self navigationItem] rightBarButtonItem] setEnabled:YES];
        }
    }
}

//- (void)swipeViewDidEndDecelerating:(SwipeView *)swipeView {
//    Rayz * r = [[self fetchedResultsController] objectAtIndexPath:[NSIndexPath indexPathForRow:[swipeView currentItemIndex] inSection:0]];
//    [RemoteStore getRayzAnswers:r];
//}

#pragma mark Next / Prev
- (void)goToNextRayz {
    _currentRayzIndex++;
    if (_currentRayzIndex >= [[[self fetchedResultsController] fetchedObjects] count]) {
        _currentRayzIndex = 0;
    }
    [self updateCurrentRayz];
    [[self swipeView] setCurrentItemIndex:_currentRayzIndex];
}

- (void)goToPreviousRayz {
    _currentRayzIndex--;
    if (_currentRayzIndex < 0) {
        _currentRayzIndex = [[[self fetchedResultsController] fetchedObjects] count] - 1;
    }
    [self updateCurrentRayz];
    [[self swipeView] setCurrentItemIndex:_currentRayzIndex];
}

- (void)updateCurrentRayz {
    Rayz * r = [[self fetchedResultsController] objectAtIndexPath:[NSIndexPath indexPathForRow:_currentRayzIndex inSection:0]];
    BOOL isUnread = [r numOfUnreadReplies] > 0;
    [r setUnread:@(isUnread)];
    [self setCurrentRayz:r];
    RayzTableViewDataSource * ds = [[self dataSources] objectForKey:[r rayzId]];
    [ds setActive:YES];
}

#pragma mark FetchedResultsController delegate
- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    switch (type) {
        case NSFetchedResultsChangeInsert: {
            [self updatePageTitle];
            break;
        }
        case NSFetchedResultsChangeDelete: {
            //                [self goToNextRayz];
            if (_currentRayzIndex >= [[[self fetchedResultsController] fetchedObjects] count]) {
                _currentRayzIndex = [[[self fetchedResultsController] fetchedObjects] count]-1;
            }
            
            [self updateCurrentRayz];
            [self updatePageTitle];
            [[self swipeView] reloadData];
            break;
        }
        case NSFetchedResultsChangeUpdate: {
            if ([indexPath row] == _currentRayzIndex) {
                UITableView * t = (UITableView*)[[self swipeView] currentItemView];
                [t reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:0 inSection:0]] withRowAnimation:UITableViewRowAnimationNone];
            }
            break;
        }
        case NSFetchedResultsChangeMove: {
            NSIndexPath * i = [[self fetchedResultsController] indexPathForObject:[self currentRayz]];
            [self setCurrentRayzIndex:[i row]];
            [self updatePageTitle];
            break;
        }
    }
}

#pragma mark RayzTableViewDelegate
- (void)tableView:(UITableView *)tableView shouldShowAttachments:(Attachments *)attachments {
    AttachmentsCollectionViewController * vc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"attachmentsController"];
    [vc setAttachments:attachments];
    if ([[self navigationController] respondsToSelector:@selector(showViewController:sender:)]) {
        [[self navigationController] showViewController:vc sender:self];
    }
    else {
        [[self navigationController] pushViewController:vc animated:YES];
    }
}

- (void)showAttachmentsIfAny:(NSNotification*)notification {
    Attachments * attachments = [[notification userInfo] objectForKey:kAttachmentsObjectAttributeName];
    [self tableView:(UITableView*)[[self swipeView] currentItemView] shouldShowAttachments:attachments];
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
