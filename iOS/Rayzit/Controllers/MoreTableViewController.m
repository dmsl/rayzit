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
//  MoreTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 5/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "MoreTableViewController.h"
#import <MediaPlayer/MediaPlayer.h>

@interface MoreTableViewController ()

@property (nonatomic, strong) MPMoviePlayerController * player;

@end

@implementation MoreTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath section]==0 && [indexPath row]==1) {
        if (_player == nil) {
            NSString * videoUrlString = [[NSBundle mainBundle] pathForResource:@"Rayzit_Promo" ofType:@"mp4"];
            NSURL * videoUrl = [NSURL fileURLWithPath:videoUrlString];
            _player = [[MPMoviePlayerController alloc] initWithContentURL:videoUrl];
        }
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(moviePlayBackDidFinish:) name:MPMoviePlayerPlaybackDidFinishNotification object:nil];
        [_player setControlStyle:MPMovieControlStyleDefault];
        [_player setShouldAutoplay:YES];
        [[self tableView] addSubview:[_player view]];
        [_player setFullscreen:YES animated:YES];
    }
}

#pragma mark Notification Center
- (void) moviePlayBackDidFinish:(NSNotification*)notification
{
    
    MPMoviePlayerController *player = [notification object];
    [player stop];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:MPMoviePlayerPlaybackDidFinishNotification object:nil];
    
    if ([player respondsToSelector:@selector(setFullscreen:animated:)])
    {
        [player.view removeFromSuperview];
    }
}

@end
