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
//  ImageCollectionViewCell.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 16/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "AttachmentCollectionViewCell.h"
#import "CAFileManager.h"
#import <DAProgressOverlayView/DAProgressOverlayView.h>
#import <MediaPlayer/MediaPlayer.h>
#import "Attachment.h"

@interface AttachmentCollectionViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *cellImageView;
@property (weak, nonatomic) IBOutlet UIImageView *attachmentTypeImageView;
@property (strong, nonatomic) DAProgressOverlayView * progressView;

@property (strong, nonatomic) UIImage *image;

@end

@implementation AttachmentCollectionViewCell

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [[self cellImageView] setClipsToBounds:YES];
    [[[self cellImageView] layer] setMasksToBounds:YES];
}

- (IBAction)removeAttachment:(id)sender {
    [_delegate deleteAttachmentForCell:self];
}

- (DAProgressOverlayView *)progressView {
    if (_progressView == nil) {
        CGRect frame = [self frame];
        frame.origin = CGPointZero;
        DAProgressOverlayView * overlay = [[DAProgressOverlayView alloc] initWithFrame:frame];
        _progressView = overlay;
        [[self progressView] setTriggersDownloadDidFinishAnimationAutomatically:YES];
        [self addSubview:_progressView];
    }
    return _progressView;
}

#pragma mark Setup
- (void)configureWithAttachment:(Attachment *)attachment {
    if ([self image] != nil) {
        [[self cellImageView] setImage:[self image]];
    }
    else if ([attachment imagesParent] != nil) {
        [self configureWithImage:attachment];
    }
    else if ([attachment videoParent] != nil) {
        [self configureWithVideo:attachment];
    }
    else if ([attachment audioParent] != nil) {
        [self configureWithAudio:attachment];
    }
    else {
        [[AlertUtils alertWithTitle:@"Unsupported attachment" message:@"This type of attachment is not supported in this version"] show];
    }
}

- (void)configureWithImage:(Attachment*)attachment {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_image_small"]];
    UIImage * image = [CAFileManager loadImageWithName:[attachment filename]];
    if (image != nil) {
        [self setImage:image];
        [[self cellImageView] setImage:image];
    }
    else {
        [[self cellImageView] setImage:[UIImage imageNamed:@"load_image"]];
        
        NSString * urlString = [NSString stringWithFormat:@"%@%@",kApiUrl,[attachment url]];
        NSURLRequest * imageRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        
        DAProgressOverlayView * overlay = [self progressView];
        
        AFImageRequestOperation * operation = [AFImageRequestOperation imageRequestOperationWithRequest:imageRequest imageProcessingBlock:nil
                                        success:^(NSURLRequest *request, NSHTTPURLResponse *response, UIImage *image) {
                                            
                                            [overlay setProgress:1];
                                            [overlay displayOperationDidFinishAnimation];
                                            
                                            [self setImage:image];
                                            [[self cellImageView] setImage:image];
                                            
                                            NSString * name = [Utilities randomStringOfSize:10];
                                            if ([CAFileManager storeImage:image withFilename:name]) {
                                                [attachment setFilename:name];
                                                [RemoteStore save];
                                            }
                                            
                                        }
                                        failure:^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error) {
                                            
                                            [[AlertUtils alertWithTitle:@"Attachement deleted" message:@"This attachment has been removed from our servers"] show];
                                            
                                        }];
        
        [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long totalBytesRead, long long totalBytesExpectedToRead) {
            if (totalBytesExpectedToRead==-1) {
                [[self progressView] setProgress:[[self progressView] progress]+0.001];
            } else {
                [[self progressView] setProgress:(totalBytesRead * 1.0 / totalBytesExpectedToRead)];
            }
        }];
        
        [overlay displayOperationWillTriggerAnimation];
        [operation start];
    }
}

- (void)configureWithVideo:(Attachment*)attachment {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_video_small"]];
    if ([CAFileManager isCachedFile:[attachment filename]]) {
        UIImage * thumbnail = [CAFileManager thumbnailForVideoAtPath:[attachment filename]];
        [self setImage:thumbnail];
        [[self cellImageView] setImage:thumbnail];
    }
    else {
        [[self cellImageView] setImage:[UIImage imageNamed:@"load_video"]];
        
        NSString * urlString = [NSString stringWithFormat:@"%@%@",kApiUrl,[attachment url]];
        NSURLRequest * request = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        AFHTTPRequestOperation * operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
        
        DAProgressOverlayView * overlay = [self progressView];
        
        [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
            
            [overlay setProgress:1];
            [overlay displayOperationDidFinishAnimation];
            
            NSString * filename = [urlString lastPathComponent];
            [CAFileManager cacheData:responseObject withFilename:filename];
            UIImage * image = [CAFileManager thumbnailForVideoAtPath:filename];
            [self setImage:image];
            [[self cellImageView] setImage:image];
            [attachment setFilename:filename];
            [RemoteStore save];
            
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            
            
        }];
        
        [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long totalBytesRead, long long totalBytesExpectedToRead) {
            if (totalBytesExpectedToRead==-1) {
                [[self progressView] setProgress:[[self progressView] progress]+0.001];
            } else {
                [[self progressView] setProgress:(totalBytesRead * 1.0 / totalBytesExpectedToRead)];
            }
        }];
        
        [overlay displayOperationWillTriggerAnimation];
        [operation start];
    }
}

- (void)configureWithAudio:(Attachment*)attachment {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_audio_small"]];
    [[self cellImageView] setImage:[UIImage imageNamed:@"load_sound"]];
    [self setImage:[[self cellImageView] image]];
    if (![CAFileManager isCachedFile:[attachment filename]]) {
        
        NSString * urlString = [NSString stringWithFormat:@"%@%@",kApiUrl,[attachment url]];
        NSURLRequest * request = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        AFHTTPRequestOperation * operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
        
        DAProgressOverlayView * overlay = [self progressView];
        
        [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
            
            [overlay setProgress:1];
            [overlay displayOperationDidFinishAnimation];
            
            NSString * filename = [urlString lastPathComponent];
            [CAFileManager cacheData:responseObject withFilename:filename];
            
            [attachment setFilename:filename];
            [RemoteStore save];
            
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            
            
        }];
        
        [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long totalBytesRead, long long totalBytesExpectedToRead) {
            if (totalBytesExpectedToRead==-1) {
                [[self progressView] setProgress:[[self progressView] progress]+0.001];
            } else {
                [[self progressView] setProgress:(totalBytesRead * 1.0 / totalBytesExpectedToRead)];
            }
        }];
        
        [overlay displayOperationWillTriggerAnimation];
        [operation start];
    }
}


#pragma mark Configuration
- (void)configureWithImageAttachment:(NSString *)filename {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_image_small"]];
    UIImage * image = [CAFileManager loadImageWithName:filename];
    if (image != nil) {
        [[self cellImageView] setImage:image];
    }
    else {
        [[self cellImageView] setImage:[UIImage imageNamed:@"load_image"]];
        
        NSString * urlString = [NSString stringWithFormat:@"%@%@",kApiUrl,filename];
        NSURLRequest * imageRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        
        DAProgressOverlayView * overlay = [self progressView];
        
        AFImageRequestOperation * operation = [AFImageRequestOperation imageRequestOperationWithRequest:imageRequest success:^(UIImage *image) {
            
            [overlay setProgress:1];
            [overlay displayOperationDidFinishAnimation];
            
            [[self cellImageView] setImage:image];
        }];
        
        [operation setDownloadProgressBlock:^(NSUInteger bytesRead, long long totalBytesRead, long long totalBytesExpectedToRead) {
            if (totalBytesExpectedToRead==-1) {
                [[self progressView] setProgress:[[self progressView] progress]+0.001];
            } else {
                [[self progressView] setProgress:(totalBytesRead * 1.0 / totalBytesExpectedToRead)];
            }
        }];
        
        [overlay displayOperationWillTriggerAnimation];
        [operation start];
    }
}

- (void)configureWithVideoAttachment:(NSString *)filename {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_video_small"]];
    [[self cellImageView] setImage:[UIImage imageNamed:@"load_video"]];
    if ([CAFileManager isCachedFile:filename]) {
        UIImage * thumb = [CAFileManager thumbnailForVideoAtPath:filename];
        [[self cellImageView] setImage:thumb];
    }
    else {

    }
}

- (void)configureWithAudioAttachment:(NSString *)filename {
    [[self attachmentTypeImageView] setImage:[UIImage imageNamed:@"load_audio_small"]];
    [[self cellImageView] setImage:[UIImage imageNamed:@"load_sound"]];
}

@end
