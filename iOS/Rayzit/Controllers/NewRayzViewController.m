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
//  NewRayzViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 15/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "NewRayzViewController.h"
#import "Rayz.h"
#import "RayzReply.h"
#import "Attachments.h"
#import "Attachment.h"
#import "MediaPickerHelper.h"
#import "CAFileManager.h"
#import "SettingsManager.h"

#import <MBProgressHUD/MBProgressHUD.h>

#import "SelectDistanceTableViewController.h"
#import "RayzTextCollectionViewCell.h"
#import "AttachmentCollectionViewCell.h"

@interface NewRayzViewController () <AttachmentCollectionViewCellDelegate, UICollectionViewDelegateFlowLayout, UIAlertViewDelegate, MediaPickerHelperDelegate, SelectDistanceDelegate>

@property (nonatomic, strong) MediaPickerHelper * mediaPicker;
@property (nonatomic, strong) NSMutableArray * attachmentsArray;
@property (nonatomic, strong) NSNumber * maxDistance;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *distanceButton;
@end

@implementation NewRayzViewController

static NSString * const rayzMessageCellIdentifier = @"rayzMessageCell";
static NSString * const attachmentCellIdentifier = @"attachmentCell";
static NSString * const userPowerCellIdentifier = @"powerCell";

static NSString * const kTypeAttributeName = @"type";
static NSString * const kFilenameAttributeName = @"filename";

static NSString * const kNewRayzPlaceholder = @"Rayz your message...";
static NSString * const kNewReplyPlaceholder = @"Rayz your answer...";

- (void)viewDidLoad {
    [super viewDidLoad];
    
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRayz:) name:kRayzCreated object:nil];
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToRayz:) name:kRayzFailed object:nil];
    
    [self setMaxDistance:@([[SettingsManager sharedManager] maximumRayzSendingDistance])];
    [self setAttachmentsArray:[NSMutableArray arrayWithCapacity:kMaxNumberOfAttachments/2]];
    [self setMediaPicker:[[MediaPickerHelper alloc] init]];
    [[self mediaPicker] setDelegate:self];
    
    [RemoteStore getUserPower:[User appUser]];

    if ([self isReply]) {
        [self setTitle:@"New reply"];
        NSMutableArray * items = [[self toolbarItems] mutableCopy];
        [items removeObject:[self distanceButton]];
        [self setToolbarItems:items];
    }
    else {
        [self setTitle:@"New rayz"];
    }
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    [[self collectionViewLayout] invalidateLayout];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark Notification center
- (void)didRayz:(NSNotification*)notification {
    [MBProgressHUD hideHUDForView:[self view] animated:YES];
    [self dismissController];
}

- (void)didFailToRayz:(NSNotification*)notification {
    [MBProgressHUD hideHUDForView:[self view] animated:YES];
    [self dismissController];
}

#pragma mark Controller dismiss
- (IBAction)dismiss:(id)sender {
    RayzTextCollectionViewCell * cell = (RayzTextCollectionViewCell*)[[self collectionView] cellForItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0]];
    NSString * message = [cell messageText];
    if ([message length] > 0 || [[self attachmentsArray] count] > 0) {
        [[[UIAlertView alloc] initWithTitle:@"Warning" message:@"Are you sure you want to discard your changes" delegate:self cancelButtonTitle:@"No" otherButtonTitles:@"Yes",nil] show];
    }
    else {
        [self dismissController];
    }
}

- (void)dismissController {
    [[self presentingViewController] dismissViewControllerAnimated:YES completion:^{
        
    }];
}

#pragma mark Rayz
- (IBAction)post:(id)sender {
    
    [(UIBarButtonItem*)sender setEnabled:NO];
    
    if ([self validateInput]) {
        RayzTextCollectionViewCell * cell = (RayzTextCollectionViewCell*)[[self collectionView] cellForItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0]];
        NSString * message = [cell messageText];
        
        
        if ([self isReply]) {
            RayzReply * reply = [RayzReply newRayzReply];
            [reply setRayz_reply_message:message];
            
            Attachments * rAttachments = [Attachments MR_createEntity];
            for (NSDictionary * att in [self attachmentsArray]) {
                Attachment * a = [Attachment MR_createEntity];
                [a setType:att[kTypeAttributeName]];
                [a setFilename:att[kFilenameAttributeName]];
                if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeImage]) {
                    [rAttachments addImagesObject:a];
                }
                else if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeVideo]) {
                    [rAttachments addVideosObject:a];
                }
                else if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeAudio]) {
                    [rAttachments addAudioObject:a];
                }
            }
            [reply setAttachments:rAttachments];
            [reply setRayzId:[[self replyTo] rayzId]];
            [reply setRayz:[self replyTo]];
//            [reply setRayzReplyId:[Utilities randomStringOfSize:10]];
//            [[NSManagedObjectContext MR_contextForCurrentThread] save:nil];
            
            [reply create];
        }
        else {
            Rayz * rayz = [Rayz newRayz];
            [rayz setRayz_message:message];
            [rayz setMaxDistance:[self maxDistance]];
            
            Attachments * rAttachments = [Attachments MR_createEntity];
            for (NSDictionary * att in [self attachmentsArray]) {
                Attachment * a = [Attachment MR_createEntity];
                [a setType:att[kTypeAttributeName]];
                [a setFilename:att[kFilenameAttributeName]];
                if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeImage]) {
                    [rAttachments addImagesObject:a];
                }
                else if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeVideo]) {
                    [rAttachments addVideosObject:a];
                }
                else if ([att[kTypeAttributeName] isEqualToString:kApiAttachmentTypeAudio]) {
                    [rAttachments addAudioObject:a];
                }
            }
            [rayz setAttachments:rAttachments];
            
//            [rayz setRayzId:[Utilities randomStringOfSize:10]];
//            [[NSManagedObjectContext MR_contextForCurrentThread] save:nil];
            
            [rayz create];
        }
        
        [self dismissController];
    }
}

- (BOOL)validateInput {
    
    RayzTextCollectionViewCell * cell = (RayzTextCollectionViewCell*)[[self collectionView] cellForItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0]];
    NSString * message = [cell messageText];
    if ([message length] == 0 || [message length] > kMaxRayzMessageCharacters) {
        [[[UIAlertView alloc] initWithTitle:@"Oops" message:@"You can't rayz and empty message" delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil] show];
        return NO;
    }
    
    if ([[self attachmentsArray] count] > kMaxNumberOfAttachments) {
        return NO;
    }
    
    return YES;
}

#pragma mark UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != [alertView cancelButtonIndex]) {
        [self dismissController];
    }
}

#pragma mark Attachments
- (IBAction)addImageAttachment:(id)sender {
    if ([self checkAttachmentsAndAlert]) {
        [[self mediaPicker] pickImage:sender];
    }
}

- (IBAction)addVideoAttachment:(id)sender {
    if ([self checkAttachmentsAndAlert]) {
        [[self mediaPicker] pickVideo:sender];
    }
}

- (IBAction)addAudioAttachment:(id)sender {
    if ([self checkAttachmentsAndAlert]) {
        [[self mediaPicker] recordAudio:sender];
    }
}

- (BOOL)checkAttachmentsAndAlert {
    if ([[self attachmentsArray] count] >= kMaxNumberOfAttachments) {
        [[[UIAlertView alloc] initWithTitle:@"Info" message:@"You have reached the maximum number of attachments" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        return NO;
    }
    return YES;
}

- (void)deleteAttachmentForCell:(AttachmentCollectionViewCell *)cell {
    NSIndexPath * indexPath = [[self collectionView] indexPathForCell:cell];
    [CAFileManager removeFileAtPath:[self attachmentsArray][indexPath.item][kFilenameAttributeName]];
    [[self attachmentsArray] removeObjectAtIndex:[indexPath item]];
    [[self collectionView] deleteItemsAtIndexPaths:@[indexPath]];
}

#pragma mark MediaPickerHelperDelegate
- (UIViewController *)controllerForPresentingImagePicker {
    return self;
}

- (void)pickerSelectedImage:(UIImage *)image withName:(NSString *)name {
    [[self attachmentsArray] addObject:@{kTypeAttributeName: kApiAttachmentTypeImage, kFilenameAttributeName: name}];
    [[self collectionView] insertItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:[[self attachmentsArray] count]-1 inSection:2]]];
}

- (void)pickerSelectedVideoWithName:(NSString *)name {
    [[self attachmentsArray] addObject:@{kTypeAttributeName: kApiAttachmentTypeVideo, kFilenameAttributeName: name}];
    [[self collectionView] insertItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:[[self attachmentsArray] count]-1 inSection:2]]];
}

- (void)pickerRecordedAudioWithName:(NSString *)name {
    [[self attachmentsArray] addObject:@{kTypeAttributeName: kApiAttachmentTypeAudio, kFilenameAttributeName: name}];
    [[self collectionView] insertItemsAtIndexPaths:@[[NSIndexPath indexPathForItem:[[self attachmentsArray] count]-1 inSection:2]]];
}

#pragma mark Distance Setting
- (void)controller:(SelectDistanceTableViewController *)controller didSelectDistance:(NSNumber *)distance {
    [self setMaxDistance:distance];
    [[Rayz validDistances] enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([[obj objectForKey:kApiDistanceValue] integerValue] == [[self maxDistance] integerValue]) {
            
            if ([[[SettingsManager sharedManager] distanceMetric] isEqualToString:distanceMetricKilometers]) {
                [(RayzTextCollectionViewCell*)[[self collectionView] cellForItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0]] setMaxDistance:[obj objectForKey:kApiDistanceKilometersName]];
            }
            else {
                [(RayzTextCollectionViewCell*)[[self collectionView] cellForItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0]] setMaxDistance:[obj objectForKey:kApiDistanceMilesName]];
            }
            *stop = YES;
        }
    }];
}

#pragma mark <UICollectionViewDataSource>

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 3;
}


- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    switch (section) {
        case 0:
            return 1;
            break;
        case 1:
            return 1;
            break;
        case 2:
            return [[self attachmentsArray] count];
            break;
        default:
            break;
    }
    return 0;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    UICollectionViewCell *cell = nil;
    
    if ([indexPath section] == 0) {
        RayzTextCollectionViewCell * mCell = [collectionView dequeueReusableCellWithReuseIdentifier:rayzMessageCellIdentifier forIndexPath:indexPath];
        
        [mCell setMessagePlaceholder:[self isReply] ? kNewReplyPlaceholder : kNewRayzPlaceholder];
        [mCell setMaxMessageLength:kMaxRayzMessageCharacters];
        if ([self isReply]) {
            [mCell hideDistanceLabel];
        }
        else {
            [[Rayz validDistances] enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                if ([[obj objectForKey:kApiDistanceValue] integerValue] == [[self maxDistance] integerValue]) {
                    
                    if ([[[SettingsManager sharedManager] distanceMetric] isEqualToString:distanceMetricKilometers]) {
                        [mCell setMaxDistance:[obj objectForKey:kApiDistanceKilometersName]];
                    }
                    else {
                        [mCell setMaxDistance:[obj objectForKey:kApiDistanceMilesName]];
                    }
                    *stop = YES;
                }
            }];
        }
        
        cell = mCell;
    }
    else if ([indexPath section] == 1) {
        cell = [collectionView dequeueReusableCellWithReuseIdentifier:userPowerCellIdentifier forIndexPath:indexPath];
    }
    else if ([indexPath section] == 2) {
        AttachmentCollectionViewCell * aCell = [collectionView dequeueReusableCellWithReuseIdentifier:attachmentCellIdentifier forIndexPath:indexPath];
        [aCell setDelegate:self];
        
        NSDictionary * temp = [self attachmentsArray][indexPath.item];
        if ([temp[kTypeAttributeName] isEqualToString:kApiAttachmentTypeImage]) {
            [aCell configureWithImageAttachment:temp[kFilenameAttributeName]];
        }
        else if ([temp[kTypeAttributeName] isEqualToString:kApiAttachmentTypeVideo]) {
            [aCell configureWithVideoAttachment:temp[kFilenameAttributeName]];
        }
        else if ([temp[kTypeAttributeName] isEqualToString:kApiAttachmentTypeAudio]) {
            [aCell configureWithAudioAttachment:temp[kFilenameAttributeName]];
        }
        cell = aCell;
    }
    
    return cell;
}

#pragma mark <UICollectionViewDelegateFlowLayout>
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    switch ([indexPath section]) {
        case 0: {
            CGFloat width = [collectionView frame].size.width - 30; // 15 inset left and 15 inset right
            CGFloat minHeight = 120;
            
            CGFloat height = minHeight;
            
            return CGSizeMake(width, height);
            break;
        }
        case 1: {
            CGFloat width = [collectionView frame].size.width - 30; // 15 inset left and 15 inset right
            CGFloat height = 30;
            
            return CGSizeMake(width, height);
            break;
        }
        case 2: {
            return CGSizeMake(110, 110);
            break;
        }
        default:
            break;
    }
    
    return CGSizeMake(0, 0);
}

#pragma mark <UICollectionViewDelegate>
// Uncomment these methods to specify if an action menu should be displayed for the specified item, and react to actions performed on the item
- (BOOL)collectionView:(UICollectionView *)collectionView shouldShowMenuForItemAtIndexPath:(NSIndexPath *)indexPath {
	return YES;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath section] == 1) {
        [self powerInfo:collectionView];
    }
}

- (IBAction)powerInfo:(id)sender {
    [[AlertUtils alertWithTitle:NSLocalizedString(@"new.powerbar.info.title", @"") message:NSLocalizedString(@"new.powerbar.info.text", @"")] show];
}

#pragma mark Navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([[segue identifier] isEqualToString:@"selectDistance"]) {
        [(SelectDistanceTableViewController*)[segue destinationViewController] setDelegate:self];
        [(SelectDistanceTableViewController*)[segue destinationViewController] setMaxDistance:[self maxDistance]];
    }
}

@end
