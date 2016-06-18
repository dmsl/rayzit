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
//  LiveFeedTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 13/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "LiveFeedTableViewController.h"

@interface LiveFeedTableViewController ()

@end

@implementation LiveFeedTableViewController {
    BOOL initialLoad;
    BOOL shouldReload;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    initialLoad = YES;
    shouldReload = YES;
    
    NSPredicate *notHidden = [NSPredicate predicateWithFormat:@"hidden == NO"];
    [self setFetchedResultsController:[Rayz MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:notHidden groupBy:nil delegate:self]];
    
    [self setTableViewBackgroundMessage:@"Be the first to rayz!"];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(locationDidUpdate:) name:kUserAdded object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(locationDidUpdate:) name:kUserLocationUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(locationDidFailToUpdate:) name:kUserUpdateFailed object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(liveFeedReloaded:) name:kLiveFeedLoaded object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(liveFeedReloaded:) name:kLiveFeedFailedToLoad object:nil];
}

- (void)refreshFeed {
    [RemoteStore getLiveFeedForUser:[User appUser]];
}

- (void)liveFeedReloaded:(NSNotification*)notification {
    [[self refreshControl] endRefreshing];
    if ([[notification name] isEqualToString:kLiveFeedFailedToLoad]) {
        [[AlertUtils alertWithTitle:@"Service Unavailable" message:@"Looks like our service is currently unavailable"] show];
    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    if (initialLoad) {
        if ([[User appUser] shouldPresentTermsPage]) {
            [self performSegueWithIdentifier:@"termsPage" sender:self];
        }
        [RemoteStore updateUserLocation:[User appUser]];
        initialLoad = NO;
    }
    else {
        if (shouldReload) {
            [RemoteStore getLiveFeedForUser:[User appUser]];
        }
        shouldReload = YES;
    }
}

- (void)locationDidUpdate:(NSNotification*)notification {
    [RemoteStore getLiveFeedForUser:[User appUser]];
    [RemoteStore getUserPower:[User appUser]];
}

- (void)locationDidFailToUpdate:(NSNotification*)notification {
    [RemoteStore getLiveFeedForUser:[User appUser]];
    [RemoteStore getUserPower:[User appUser]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Navigation
// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([[segue identifier] isEqualToString:@"newRayz"]) {
        shouldReload = NO;
    }
}


@end
