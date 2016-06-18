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
//  AboutTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 5/11/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "AboutTableViewController.h"

@interface AboutTableViewController ()

@end

@implementation AboutTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 4;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 40;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView * header = [[UIView alloc] init];
    
    UILabel * lbl = [[UILabel alloc] initWithFrame:CGRectMake(10, 15, [tableView frame].size.width-20, 25)];
    
    NSString * text = [NSString stringWithFormat:@"about.sections.section%ld",(long)section+1];
    [lbl setText:NSLocalizedString(text, @"")];
    [lbl setTextColor:[RayzitTheme mainAppColor]];
    [lbl setFont:[UIFont systemFontOfSize:17]];
    
    [header addSubview:lbl];
    
    return header;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    switch (section) {
        case 0:
        case 2:
        case 3:
            return 1;
            break;
        case 1:
            return 4;
            break;
        default:
            break;
    }

    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    switch ([indexPath section]) {
        case 0:
        case 1:
            return 44;
            break;
        case 2: {
            CGSize size = [NSLocalizedString(@"about.section3.content1", @"") boundingRectWithSize:CGSizeMake([tableView frame].size.width, CGFLOAT_MAX)
                                                                                           options:NSStringDrawingUsesLineFragmentOrigin
                                                                                        attributes:@{NSFontAttributeName: [UIFont systemFontOfSize:16]} context:NULL].size;
            return size.height + 50;
            break;
        }
        case 3:
            return 80;
            break;
        default:
            break;
    }
    
    return 44;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString * const normalCellIdentifier = @"normalCell";
    static NSString * const emptyCellIdentifier = @"emptyCell";
    
    
    UITableViewCell *cell = nil;
    
    if ([indexPath section]==0) {
        cell = [tableView dequeueReusableCellWithIdentifier:normalCellIdentifier forIndexPath:indexPath];
        NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
        [[cell textLabel] setText:infoDictionary[@"CFBundleShortVersionString"]];
    }
    else if ([indexPath section]==3) {
        cell = [tableView dequeueReusableCellWithIdentifier:emptyCellIdentifier forIndexPath:indexPath];
        
        CGFloat btnWidth = [tableView frame].size.width / 2 - 15;
        CGFloat btnHeight = [self tableView:tableView heightForRowAtIndexPath:indexPath];
        
        UIButton * dmsl = [[UIButton alloc] initWithFrame:CGRectMake(15, 0, btnWidth, btnHeight)];
        [dmsl setImage:[UIImage imageNamed:@"dmsl_logo"] forState:UIControlStateNormal];
        [[dmsl imageView] setContentMode:UIViewContentModeScaleAspectFit];
        [dmsl setTag:kDmslLogoButtonTag];
        [dmsl addTarget:self action:@selector(logoButtonTapped:) forControlEvents:UIControlEventTouchUpInside];
        [cell addSubview:dmsl];
        
        UIButton * ucy = [[UIButton alloc] initWithFrame:CGRectMake(15+btnWidth, 0, btnWidth, btnHeight)];
        [ucy setImage:[UIImage imageNamed:@"ucy_logo"] forState:UIControlStateNormal];
        [[ucy imageView] setContentMode:UIViewContentModeScaleAspectFit];
        [ucy setTag:kUcyLogoButtonTag];
        [ucy addTarget:self action:@selector(logoButtonTapped:) forControlEvents:UIControlEventTouchUpInside];
        [cell addSubview:ucy];
        
        [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
    }
    else {
        cell = [tableView dequeueReusableCellWithIdentifier:normalCellIdentifier forIndexPath:indexPath];
        NSString * text = [NSString stringWithFormat:@"about.section%ld.content%ld",(long)[indexPath section]+1,(long)[indexPath row]+1];
        [[cell textLabel] setText:NSLocalizedString(text, @"")];
        if ([indexPath section]!=1) {
            [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
        }
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath section]==1) {
        NSURL * url = nil;
        switch ([indexPath row]) {
            case 0:
                url = [NSURL URLWithString:NSLocalizedString(@"website.rayzit.contact", @"")];
                break;
            case 1:
                url = [NSURL URLWithString:NSLocalizedString(@"website.rayzit.tos", @"")];
                break;
            case 2:
                url = [NSURL URLWithString:NSLocalizedString(@"website.rayzit.rules", @"")];
                break;
            case 3:
                url = [NSURL URLWithString:NSLocalizedString(@"website.rayzit.privacy", @"")];
                break;
            default:
                break;
        }
        if (url!=nil && [[UIApplication sharedApplication] canOpenURL:url]) {
            [[UIApplication sharedApplication] openURL:url];
        }
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark Logos Action
static NSInteger const kUcyLogoButtonTag = 11;
static NSInteger const kDmslLogoButtonTag = 12;
- (void)logoButtonTapped:(UIButton*)sender {
    NSURL * url;
    if ([sender tag]==kUcyLogoButtonTag) {
        url = [NSURL URLWithString:NSLocalizedString(@"website.ucy", @"")];
    }
    else if ([sender tag]==kDmslLogoButtonTag) {
        url = [NSURL URLWithString:NSLocalizedString(@"website.dmsl", @"")];
    }
    if (url!=nil && [[UIApplication sharedApplication] canOpenURL:url]) {
        [[UIApplication sharedApplication] openURL:url];
    }
}

@end
