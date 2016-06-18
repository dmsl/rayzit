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
//  NearbyRayzViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 29/5/15.
//  Copyright (c) 2015 DMSL. All rights reserved.
//

#import "NearbyRayzViewController.h"

@interface NearbyRayzViewController ()

@end

@implementation NearbyRayzViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    NSPredicate *notHidden = [NSPredicate predicateWithFormat:@"hidden == NO AND nearby == YES"];
    [self setFetchedResultsController:[Rayz MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:notHidden groupBy:nil delegate:self]];
    
    [self setTableViewBackgroundMessage:@"Be the first to rayz!"];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(nearbyFeedLoaded:) name:kNearbyFeedLoaded object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(nearbyFeedLoaded:) name:kNearbyFeedFailedToLoad object:nil];
    
    [RemoteStore getNearbyFeedForUser:[User appUser]];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    
}

- (void)refreshFeed {
    [RemoteStore getNearbyFeedForUser:[User appUser]];
}

- (void)nearbyFeedLoaded:(NSNotification*)notification {
    [[self refreshControl] endRefreshing];
    if ([[notification name] isEqualToString:kLiveFeedFailedToLoad]) {
        [[AlertUtils alertWithTitle:@"Service Unavailable" message:@"Looks like our service is currently unavailable"] show];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//#pragma mark - Navigation
//// In a storyboard-based application, you will often want to do a little preparation before navigation
//- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
//    if ([[segue identifier] isEqualToString:@"newRayz"]) {
//
//    }
//}

@end
