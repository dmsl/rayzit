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
//  MediaPickerHelper.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 16/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "MediaPickerHelper.h"
#import "CAFileManager.h"
#import <MobileCoreServices/UTCoreTypes.h>
#import "RecordAudioViewController.h"

@interface MediaPickerHelper () <UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIActionSheetDelegate, AudioRecorderDelegate>

@end

@implementation MediaPickerHelper

static NSInteger const kImageActionSheetTag = 1;
static NSInteger const kVideoActionSheetTag = 2;

static NSString * const kMediaTypeImage = @"public.image";
static NSString * const kMediaTypeVideo = @"public.movie";
static NSString * const kMediaTypeAudio = @"public.audio";

- (void)pickImage:(id)sender {
    DDLogInfo(@"User is about to select image from picker");
    
    UIActionSheet *actionSheet = nil;
    
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Choose existing", @"Take photo", nil];
        [actionSheet setTag:kImageActionSheetTag];
    }
    else {
//        actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Choose existing", nil];
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
        imagePicker.delegate = self;
        [[_delegate controllerForPresentingImagePicker] presentViewController:imagePicker animated:YES completion:^{}];
        return;
    }
    
    if (_delegate) {
        [actionSheet showInView:[[_delegate controllerForPresentingImagePicker] view]];
    }
    
}

- (void)pickVideo:(id)sender {
    DDLogInfo(@"User is about to select video from picker");
    
    UIActionSheet *actionSheet = nil;
    
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Choose existing", @"Take video", nil];
        [actionSheet setTag:kVideoActionSheetTag];
    }
    else {
        //        actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Choose existing", nil];
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
        imagePicker.delegate = self;
        imagePicker.mediaTypes = @[(NSString*)kUTTypeVideo];
        imagePicker.videoMaximumDuration = 5 * 60;
        imagePicker.allowsEditing = YES;
        [[_delegate controllerForPresentingImagePicker] presentViewController:imagePicker animated:YES completion:^{}];
        return;
    }
    
    if (_delegate) {
        [actionSheet showInView:[[_delegate controllerForPresentingImagePicker] view]];
    }
}

- (void)recordAudio:(id)sender {
    if ([RecordAudioViewController isRecordingAvailable]) {
        UINavigationController * nav = [RecordAudioViewController instantiateRecordingControllers];
        RecordAudioViewController * rec = (RecordAudioViewController*)[nav topViewController];
        [rec setDelegate:self];
        [[_delegate controllerForPresentingImagePicker] presentViewController:nav animated:YES completion:^{
            
        }];
    }
    else {
        [[AlertUtils alertWithTitle:@"No recording device found" message:@"You device does not support recording of audio files"] show];
    }
}

#pragma mark UIImagePickerControllerDelegate
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    if ([info[UIImagePickerControllerMediaType] isEqualToString:kMediaTypeVideo]) {
        DDLogInfo(@"User chose video: %@", info[UIImagePickerControllerMediaURL]);
        
        NSString * filename = [NSString stringWithFormat:@"%@.mp4",[Utilities randomStringOfSize:15]];
        [CAFileManager cacheData:[NSData dataWithContentsOfURL:info[UIImagePickerControllerMediaURL]] withFilename:filename];
        [CAFileManager removeFileAtPath:[info[UIImagePickerControllerMediaURL] absoluteString]];
        
        [_delegate pickerSelectedVideoWithName:filename];
    }
    else if ([info[UIImagePickerControllerMediaType] isEqualToString:kMediaTypeImage]) {
        UIImage* image = [info objectForKey:UIImagePickerControllerOriginalImage];
        DDLogInfo(@"User chose image: %@", image);
        
        image = [Utilities fixOrientation:image];
        
        NSData * lowerQuality = UIImageJPEGRepresentation([UIImage imageWithCGImage:image.CGImage scale:0.2 orientation:image.imageOrientation], 0.0);
        UIImage * newImage = [UIImage imageWithData:lowerQuality];
        
        NSString *photoName = [Utilities randomStringOfSize:15];
        NSString*photoUrl = [NSString stringWithFormat:@"%@.png",photoName];
        
//        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
            [CAFileManager cacheData:lowerQuality withFilename:photoUrl];
//        });
        
        [_delegate pickerSelectedImage:newImage withName:photoUrl];
    }
    
    [picker dismissViewControllerAnimated:YES completion:^{
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
    }];
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    DDLogInfo(@"User cancelled image selection");
    
    [picker dismissViewControllerAnimated:YES completion:^{
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
    }];
}

#pragma mark UINavigationControllerDelegate
- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated {
    
    UIViewController *v = [_delegate controllerForPresentingImagePicker];
    [navigationController.navigationBar setBarTintColor:v.navigationController.navigationBar.barTintColor];
    [navigationController.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName: [UIColor whiteColor]}];
    [navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent animated:animated];
    
}

#pragma mark AudioRecorderDelegare
- (void)audioRecorder:(RecordAudioViewController *)recorder didFinishRecordingWithInfo:(NSDictionary *)info {
    DDLogInfo(@"User recorded audio with info: %@", info);
    
    if (info[kAudioRecordingReferenceURL]) {
        NSURL * url = info[kAudioRecordingReferenceURL];
        [_delegate pickerRecordedAudioWithName:[url lastPathComponent]];
    }
    
    [recorder dismissViewControllerAnimated:YES completion:^{
        
    }];
}

- (void)audioRecorderDidCancel:(RecordAudioViewController *)recorder {
    DDLogInfo(@"User canceled audio recording");
    
    [recorder dismissViewControllerAnimated:YES completion:^{
        
    }];
}

#pragma mark UIActionSheetDelegate
- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == [actionSheet cancelButtonIndex]) {
        
    }
    else if (buttonIndex == 0) {
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
        imagePicker.delegate = self;
        if ([actionSheet tag] == kVideoActionSheetTag) {
            imagePicker.mediaTypes = @[(NSString*)kUTTypeMovie];
        }
        [[_delegate controllerForPresentingImagePicker] presentViewController:imagePicker animated:YES completion:^{}];
    }
    else if (buttonIndex == 1) {
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
        imagePicker.delegate = self;
        [imagePicker setSourceType:UIImagePickerControllerSourceTypeCamera];
        if ([actionSheet tag] == kImageActionSheetTag) {
            [imagePicker setCameraCaptureMode:UIImagePickerControllerCameraCaptureModePhoto];
        }
        else if ([actionSheet tag] == kVideoActionSheetTag) {
            [imagePicker setMediaTypes:@[(NSString*)kUTTypeMovie]];
            [imagePicker setCameraCaptureMode:UIImagePickerControllerCameraCaptureModeVideo];
            [imagePicker setVideoQuality:UIImagePickerControllerQualityTypeMedium];
            [imagePicker setVideoMaximumDuration:5*60];
            [imagePicker setAllowsEditing:YES];
        }
        [imagePicker setCameraDevice:UIImagePickerControllerCameraDeviceRear];
        [imagePicker setShowsCameraControls:YES];
        [[_delegate controllerForPresentingImagePicker] presentViewController:imagePicker animated:YES completion:^{
            [[UIApplication sharedApplication] setStatusBarHidden:YES];
        }];
    }
}


@end
