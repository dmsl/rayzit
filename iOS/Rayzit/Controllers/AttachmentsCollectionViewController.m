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
//  AttachmentsCollectionViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 20/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "AttachmentsCollectionViewController.h"
#import <MWPhotoBrowser/MWPhotoBrowser.h>
#import <MediaPlayer/MediaPlayer.h>
#import "CAFileManager.h"
#import "Attachments.h"
#import "Attachment.h"

#import "AttachmentCollectionViewCell.h"

@interface AttachmentsCollectionViewController () <UICollectionViewDelegateFlowLayout, MWPhotoBrowserDelegate>

@property (nonatomic, strong) NSArray *attachmentsArray;
@property (strong, nonatomic) MPMoviePlayerController *moviePlayer;

@end

@implementation AttachmentsCollectionViewController

static NSString * const attachmentCellIdentifier = @"attachmentCell";

- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSArray * images = [[[self attachments] images] allObjects];
    NSArray * videos = [[[self attachments] videos] allObjects];
    NSArray * audio  = [[[self attachments] audio ] allObjects];
    
    [self setAttachmentsArray:@[images,videos,audio]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */

#pragma mark <UICollectionViewDataSource>

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return [[self attachmentsArray] count];
}


- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return [[self attachmentsArray][section] count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    AttachmentCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:attachmentCellIdentifier forIndexPath:indexPath];
    
    Attachment * att = [self attachmentsArray][indexPath.section][indexPath.row];
    [cell configureWithAttachment:att];
    
    return cell;
}

#pragma mark <UICollectionViewDelegate>

// Uncomment this method to specify if the specified item should be selected
- (BOOL)collectionView:(UICollectionView *)collectionView shouldSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    Attachment * att  = [self attachmentsArray][indexPath.section][indexPath.row];
    return [CAFileManager isCachedFile:[att filename]];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    Attachment * att  = [self attachmentsArray][indexPath.section][indexPath.row];
    
    if ([att imagesParent] != nil) {
        MWPhotoBrowser * photoBrowser = [[MWPhotoBrowser alloc] initWithDelegate:self];
        [photoBrowser setStatusBarStyle:UIStatusBarStyleLightContent];
        [photoBrowser setCurrentPhotoIndex:indexPath.row];
        UINavigationController * nav = [[UINavigationController alloc] initWithRootViewController:photoBrowser];
        [[nav navigationBar] setBarTintColor:[RayzitTheme mainAppColor]];
        
        [self presentViewController:nav animated:YES completion:nil];
    }
    else if ([att videoParent] != nil) {
        NSURL * url = [CAFileManager cacheURLforFile:[att filename]];
        _moviePlayer =  [[MPMoviePlayerController alloc] initWithContentURL:url];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(moviePlayBackDidFinish:)
                                                     name:MPMoviePlayerPlaybackDidFinishNotification
                                                   object:_moviePlayer];
        
        _moviePlayer.controlStyle = MPMovieControlStyleFullscreen;
        _moviePlayer.shouldAutoplay = YES;
        [self.view addSubview:_moviePlayer.view];
        [_moviePlayer setFullscreen:YES animated:YES];
    }
    else if ([att audioParent] != nil) {
        NSURL * url = [CAFileManager cacheURLforFile:[att filename]];
        _moviePlayer =  [[MPMoviePlayerController alloc] initWithContentURL:url];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(moviePlayBackDidFinish:)
                                                     name:MPMoviePlayerPlaybackDidFinishNotification
                                                   object:_moviePlayer];
        
        _moviePlayer.controlStyle = MPMovieControlStyleFullscreen;
        _moviePlayer.shouldAutoplay = YES;
        [self.view addSubview:_moviePlayer.view];
        [_moviePlayer setFullscreen:YES animated:YES];
    }
    else {
        [AlertUtils alertWithTitle:@"Warning" message:@"Unsupported attachment type"];
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    //    return [self isGrid] ? CGSizeMake(140, 140) : CGSizeMake(300, 300);
    return CGSizeMake(300, 300);
}

#pragma mark Photo Browser
- (NSUInteger)numberOfPhotosInPhotoBrowser:(MWPhotoBrowser *)photoBrowser {
    return [[self attachmentsArray][0] count];
}

- (id <MWPhoto>)photoBrowser:(MWPhotoBrowser *)photoBrowser photoAtIndex:(NSUInteger)index {
    Attachment * att  = [self attachmentsArray][0][index];
    UIImage * image = [CAFileManager loadImageWithName:[att filename]];
    return [MWPhoto photoWithImage:image];
}

#pragma mark MPMoviePlayer
- (void)moviePlayBackDidFinish:(NSNotification*)notification {
    MPMoviePlayerController *player = [notification object];
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:MPMoviePlayerPlaybackDidFinishNotification
                                                  object:player];
    
    if ([player respondsToSelector:@selector(setFullscreen:animated:)]) {
        [player.view removeFromSuperview];
    }
}

@end
