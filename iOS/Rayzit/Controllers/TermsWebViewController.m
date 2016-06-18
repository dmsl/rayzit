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
//  TermsWebViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 4/6/15.
//  Copyright (c) 2015 DMSL. All rights reserved.
//

#import "TermsWebViewController.h"

@interface TermsWebViewController () <UIWebViewDelegate>

@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *backButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *forwardButton;
@property (weak, nonatomic) IBOutlet UIProgressView *progressView;

@property (strong, nonatomic) NSTimer * timer;

@end

@implementation TermsWebViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [[self webView] loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"about:blank"]]];
    [[self webView] loadHTMLString:NSLocalizedString(@"terms.of.use", @"terms of use html") baseURL:[[NSBundle mainBundle] bundleURL]];
    
    [[self progressView] setProgress:0.f];
    [[self webView] setDelegate:self];
    [[self backButton] setEnabled:NO];
    [[self forwardButton] setEnabled:NO];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)agreeTerms:(id)sender {
    [[User appUser] didPresentTermsPage];
    [[self presentingViewController] dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)goBack:(id)sender {
    [[self webView] goBack];
}

- (IBAction)goForward:(id)sender {
    [[self webView] goForward];
}

#pragma mark - Web View delegate
- (void)webViewDidStartLoad:(UIWebView *)webView {
    [[self progressView] setProgress:0.f];
    [[self progressView] setHidden:NO];
    
    [self setTimer:[NSTimer timerWithTimeInterval:0.2 target:self selector:@selector(updateProgressView) userInfo:nil repeats:YES]];
    [[self timer] fire];
}

- (void)updateProgressView {
    CGFloat progress = [[self progressView] progress] * 100;
    if (progress < 95) {
        [[self progressView] setProgress:(progress+5)/100];
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    [[self progressView] setProgress:100.f];
    [[self progressView] setHidden:YES];
    
    [[self timer] invalidate];
    [[self backButton] setEnabled:[[self webView] canGoBack]];
    [[self forwardButton] setEnabled:[[self webView] canGoForward]];
    
    if ([[[[[self webView] request] URL] absoluteString] isEqualToString:@"about:blank"] && ![[self webView] canGoBack] && [[self webView] canGoForward]) {
        [[self webView] loadHTMLString:NSLocalizedString(@"terms.of.use", @"terms of use html") baseURL:[[NSBundle mainBundle] bundleURL]];
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
