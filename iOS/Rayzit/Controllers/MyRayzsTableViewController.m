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
//  MyRayzsTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 22/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "MyRayzsTableViewController.h"

@interface MyRayzsTableViewController ()

@end

@implementation MyRayzsTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSPredicate *notHidden = [NSPredicate predicateWithFormat:@"hidden == NO AND userId CONTAINS[cd] %@",[[User appUser] userId]];
    [self setFetchedResultsController:[Rayz MR_fetchAllSortedBy:@"timestamp" ascending:NO withPredicate:notHidden groupBy:nil delegate:self]];
    [RemoteStore getUserRayzs:[User appUser]];
    
    [self setTableViewBackgroundMessage:@"Send some rayz, don't be shy!"];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(liveFeedReloaded:) name:kUserRayzsLoaded object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(liveFeedReloaded:) name:kUserRayzsFailedToLoad object:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)refreshFeed {
    [RemoteStore getUserRayzs:[User appUser]];
}

- (void)liveFeedReloaded:(NSNotification*)notification {
    [[self refreshControl] endRefreshing];
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
