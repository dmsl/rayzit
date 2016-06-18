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
//  RecordAudioViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 4/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "RecordAudioViewController.h"
#import "CAFileManager.h"
#import "MediaPickerHelper.h"
#import <AVFoundation/AVFoundation.h>

NSString * const kAudioRecordingReferenceURL = @"AudioRecordingReferenceURL";

@interface RecordAudioViewController () <AVAudioRecorderDelegate, AVAudioPlayerDelegate>

@property (weak, nonatomic) IBOutlet UILabel *infoLabel;
@property (weak, nonatomic) IBOutlet UIButton *recordButton;
@property (weak, nonatomic) IBOutlet UIButton *playButton;
@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;

@property (nonatomic, getter=isRecording) BOOL recording;
@property (nonatomic, getter=isPlaying) BOOL playing;
@property (nonatomic, strong) AVAudioRecorder * audioRecorder;
@property (nonatomic, strong) AVAudioPlayer * audioPlayer;
@property (nonatomic, strong) NSTimer * timer;

@end

@implementation RecordAudioViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[[self navigationItem] rightBarButtonItem] setEnabled:NO];

}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [[AVAudioSession sharedInstance] setActive:NO error:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setRecording:(BOOL)recording {
    _recording = recording;
    [[self recordButton] setSelected:_recording];
    if (_recording) {
        [[self infoLabel] setText:@"tap the mic to stop"];
        _audioPlayer = nil;
    }
    else {
        [[self infoLabel] setText:@"tap the mic to start"];
    }
}

- (void)setPlaying:(BOOL)playing {
    _playing = playing;
    [[self playButton] setSelected:_playing];
    [[AVAudioSession sharedInstance] setActive:_playing error:nil];
}


+ (BOOL)isRecordingAvailable {
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    NSError *err = nil;
    [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord error:&err];
    if(err){
        DDLogError(@"audioSession: %@ %ld %@", [err domain], (long)[err code], [[err userInfo] description]);
        return NO;
    }
    
    [audioSession setActive:YES error:&err];
    err = nil;
    if(err){
        DDLogError(@"audioSession: %@ %ld %@", [err domain], (long)[err code], [[err userInfo] description]);
        return NO;
    }
    
    return [audioSession isInputAvailable];
}

+ (UINavigationController *)instantiateRecordingControllers {
    UINavigationController * nav = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"recordController"];
    return nav;
}

#pragma mark -
#pragma mark Controller logic
- (IBAction)record:(id)sender {
    if ([self isRecording]) {
        [self setRecording:NO];
        [self stopRecording];
    }
    else {
        [self setRecording:YES];
        [self startRecording];
    }
}

- (IBAction)play:(id)sender {
    if ([self isPlaying]) {
        [self setPlaying:NO];
        [_audioPlayer stop];
    }
    else {
        [self setPlaying:YES];
        if (_audioPlayer == nil) {
            _audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:[_audioRecorder url] error:nil];
            [_audioPlayer setVolume:1.0];
            [_audioPlayer setDelegate:self];
        }
        [_audioPlayer setCurrentTime:0];
        [_audioPlayer play];
    }
}

- (IBAction)cancel:(id)sender {
    if ([self isRecording]) {
        [self stopRecording];
        [[self audioRecorder] deleteRecording];
    }
    if (_delegate) {
        [_delegate audioRecorderDidCancel:self];
    }
    else {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (IBAction)done:(id)sender {
    if (_delegate && _audioPlayer) {
        [_delegate audioRecorder:self didFinishRecordingWithInfo:@{
                                                                   kAudioRecordingReferenceURL: [_audioRecorder url]
                                                                   }];
    }
}

- (void)startRecording {
    [[self playButton] setEnabled:NO];
    
    if (_audioRecorder) {
        [_audioRecorder deleteRecording];
    }
    else {
        NSMutableDictionary * recordSetting = [[NSMutableDictionary alloc] init];
        
        [recordSetting setValue :[NSNumber numberWithInt:kAudioFormatLinearPCM] forKey:AVFormatIDKey];
        [recordSetting setValue:[NSNumber numberWithFloat:16000.0] forKey:AVSampleRateKey];
        [recordSetting setValue:[NSNumber numberWithInt:2] forKey:AVNumberOfChannelsKey];
        
        [recordSetting setValue :[NSNumber numberWithInt:16] forKey:AVLinearPCMBitDepthKey];
        [recordSetting setValue :[NSNumber numberWithBool:NO] forKey:AVLinearPCMIsBigEndianKey];
        [recordSetting setValue :[NSNumber numberWithBool:NO] forKey:AVLinearPCMIsFloatKey];
        
        [recordSetting setValue:[NSNumber numberWithInt:AVAudioQualityLow] forKey:AVSampleRateConverterAudioQualityKey];
        
        NSString * recorderFilePath = [[Utilities randomStringOfSize:10] stringByAppendingString:@".wav"];
        NSURL *url = [CAFileManager cacheURLforFile:recorderFilePath];
        
        NSError * err = nil;
        _audioRecorder = [[AVAudioRecorder alloc] initWithURL:url settings:recordSetting error:&err];
        
        //prepare to record
        [_audioRecorder setDelegate:self];
        _audioRecorder.meteringEnabled = YES;
        
        [[[self navigationItem] rightBarButtonItem] setEnabled:YES];
    }
    
    [_audioRecorder prepareToRecord];
    [_audioRecorder recordForDuration:(NSTimeInterval) 10];
    _timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateTimestampLabel) userInfo:nil repeats:YES];
}

- (void)updateTimestampLabel {
    NSTimeInterval interval = [[self audioRecorder] currentTime];
    int ti = (int)interval;
    
    int seconds = ti % 60;
    int minutes = ti / 60;
    
    [[self timestampLabel] setText:[NSString stringWithFormat:@"%02d:%02d",minutes,seconds]];
}

- (void)stopRecording {
    [_audioRecorder stop];
}

#pragma mark AudioRecorder delegate
- (void)audioRecorderDidFinishRecording:(AVAudioRecorder *)aRecorder successfully:(BOOL)flag
{
    [self setRecording:NO];
    [[self playButton] setEnabled:YES];
    [_timer invalidate];
    _timer = nil;
    
    
}

#pragma mark AudioPlayer delegate
- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player successfully:(BOOL)flag {
    [self setPlaying:NO];
}

@end
