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
//  RayzTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 10/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RayzTableViewController.h"
#import "AttachmentsCollectionViewController.h"

@interface RayzTableViewController ()

@end

@implementation RayzTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(showAttachmentsIfAny:) name:kShowAttachmentsNotification object:nil];
    [[self tableView] registerNib:[UINib nibWithNibName:@"RayzLabelTableViewCell" bundle:nil] forCellReuseIdentifier:rayzCellIdentifier];
    
    [self setRefreshControl:[[UIRefreshControl alloc] init]];
    [[self refreshControl] addTarget:self action:@selector(refreshFeed) forControlEvents:UIControlEventValueChanged];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self setActive:YES];
    [[self tableView] reloadData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self setActive:NO];
}

- (void)refreshFeed {
    NSAssert(![self isKindOfClass:[RayzTableViewController class]], @"should be implemented by child");
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark FetchedResultsController delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    if ([self isActive]) {
        [self.tableView beginUpdates];
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    if ([self isActive]) {
        [self.tableView endUpdates];
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    if ([self isActive]) {
        switch (type) {
            case NSFetchedResultsChangeInsert: {
                [self.tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeDelete: {
                [self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
            case NSFetchedResultsChangeUpdate: {
                [self configureCell:(RayzTableViewCell*)[self.tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
                break;
            }
            case NSFetchedResultsChangeMove: {
                [self.tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
                [self.tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
                break;
            }
        }
    }
}

#pragma mark Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return [[[self fetchedResultsController] sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    NSArray *sections = [self.fetchedResultsController sections];
    id<NSFetchedResultsSectionInfo> sectionInfo = [sections objectAtIndex:section];
    
    if ([sectionInfo numberOfObjects] == 0) {
        UIView * bgView = [[UIView alloc] initWithFrame:[tableView frame]];
        [tableView setBackgroundView:bgView];
        [bgView addSubview:[RayzitTheme tableViewBackgroundLabelWithTitle:@"It's lonely here :(" message:[self tableViewBackgroundMessage]]];
    }
    else {
        [tableView setBackgroundView:nil];
    }
    
    return [sectionInfo numberOfObjects];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    RayzTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:rayzCellIdentifier forIndexPath:indexPath];
    
    [self configureCell:cell atIndexPath:indexPath];
    
    return cell;
}

- (void)configureCell:(RayzTableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath {
    Rayz * r = [self.fetchedResultsController objectAtIndexPath:indexPath];
    [cell configureCellWithRayz:r];
//    [cell setTextSelectable:NO];
}

#pragma mark TableView delegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    static CGFloat const defaultHeight = 120;
    static CGFloat const maxHeight = 200;
    static CGFloat const minLabelHeight = 30;
    CGFloat labelWidth = [[self tableView] frame].size.width - 30;
    
    Rayz * r = [[self fetchedResultsController] objectAtIndexPath:indexPath];
    
    CGSize size = [[r rayz_message] boundingRectWithSize:CGSizeMake(labelWidth, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:17]} context:NULL].size;
    
    CGFloat estimatedHeight = defaultHeight - minLabelHeight + size.height + 15;
    
    if (estimatedHeight > maxHeight) {
        return maxHeight;
    }
    
    return estimatedHeight;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    Rayz * r = [[self fetchedResultsController] objectAtIndexPath:indexPath];
    if ([[r status] isEqualToString:kApiRayzStatusFailed]) {
        [r create];
    }
    else if ([[r status] isEqualToString:kApiRayzStatusPending]) {
        // do nothing
    }
    else {
        RayzDetailsTableViewController * rayzDetails = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"rayzDetailsController"];
        [rayzDetails setFetchPredicate:[[[self fetchedResultsController] fetchRequest] predicate]];
        [rayzDetails setCurrentRayzIndex:indexPath.row];
        if ([[self navigationController] respondsToSelector:@selector(showViewController:sender:)]) {
            [[self navigationController] showViewController:rayzDetails sender:self];
        }
        else {
            [[self navigationController] pushViewController:rayzDetails animated:YES];
        }
    }
}

- (void)showAttachmentsIfAny:(NSNotification*)notification {
    Attachments * attachments = [[notification userInfo] objectForKey:kAttachmentsObjectAttributeName];
    AttachmentsCollectionViewController * vc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"attachmentsController"];
    [vc setAttachments:attachments];
    if ([[self navigationController] respondsToSelector:@selector(showViewController:sender:)]) {
        [[self navigationController] showViewController:vc sender:self];
    }
    else {
        [[self navigationController] pushViewController:vc animated:YES];
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
