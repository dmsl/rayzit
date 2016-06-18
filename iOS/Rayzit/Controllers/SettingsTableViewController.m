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
//  SettingsTableViewController.m
//  Rayzit
//
//  Created by Chrysovalantis Anastasiou on 28/10/14.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

#import "SettingsTableViewController.h"
#import "SettingsManager.h"

#import "Rayz.h"

static NSInteger const kDistanceActionTag = 123;
static NSInteger const kMetricActionTag = 234;

@interface SettingsTableViewController () <UIActionSheetDelegate>

@property (weak, nonatomic) IBOutlet UILabel *distanceMetricLabel;
@property (weak, nonatomic) IBOutlet UILabel *rayzSendingDistanceLabel;
@property (weak, nonatomic) IBOutlet UISwitch *autoStarSwitch;
@property (weak, nonatomic) IBOutlet UISwitch *locationSwitch;

@end

@implementation SettingsTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self constructFooter];
    [self refreshUI];
}

- (void)constructFooter {
    CGSize screenSize = [Utilities screenSize];
    
    UILabel * lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, screenSize.width, 300)];
    [lbl setNumberOfLines:0];
    
    NSMutableAttributedString * attrString = [[NSMutableAttributedString alloc] initWithString:NSLocalizedString(@"settings.location.footer.title1", @"")
                                                                                    attributes:@{
                                                                                                 NSFontAttributeName: [UIFont systemFontOfSize:14],
                                                                                                 NSForegroundColorAttributeName: [RayzitTheme mainAppColor]
                                                                                                 }];
    [attrString appendAttributedString:[[NSAttributedString alloc] initWithString:NSLocalizedString(@"settings.location.footer.text1", @"")
                                                                       attributes:@{
                                                                                    NSFontAttributeName: [UIFont systemFontOfSize:14],
                                                                                    NSForegroundColorAttributeName: [UIColor darkGrayColor]
                                                                                    }]];
    [attrString appendAttributedString:[[NSAttributedString alloc] initWithString:NSLocalizedString(@"settings.location.footer.title2", @"")
                                                                       attributes:@{
                                                                                    NSFontAttributeName: [UIFont systemFontOfSize:14],
                                                                                    NSForegroundColorAttributeName: [RayzitTheme mainAppColor]
                                                                                    }]];
    [attrString appendAttributedString:[[NSAttributedString alloc] initWithString:NSLocalizedString(@"settings.location.footer.text2", @"")
                                                                       attributes:@{
                                                                                    NSFontAttributeName: [UIFont systemFontOfSize:14],
                                                                                    NSForegroundColorAttributeName: [UIColor darkGrayColor]
                                                                                    }]];
    
    [lbl setAttributedText:attrString];
    [lbl sizeToFit];
    
    CGRect r = [lbl frame];
    r.origin.x = 15;
    r.size.width = screenSize.width - 30;
    r.size.height += 50;
    [lbl setFrame:r];
    
    UIView * footer = [[UIView alloc] initWithFrame:CGRectMake(0, 0, screenSize.width, r.size.height + 10)];
    [footer addSubview:lbl];
    
    [[self tableView] setTableFooterView:footer];
    
    [lbl setFrame:r];
}

- (void)refreshUI {
    
    [[self distanceMetricLabel] setText:[[SettingsManager sharedManager] distanceMetric]];
    for (NSDictionary * d in [Rayz validDistances]) {
        if ([d[kApiDistanceValue] integerValue] == [[SettingsManager sharedManager] maximumRayzSendingDistance]) {
            if ([[[SettingsManager sharedManager] distanceMetric] isEqualToString:distanceMetricKilometers]) {
                [[self rayzSendingDistanceLabel] setText:d[kApiDistanceKilometersName]];
            }
            else {
                [[self rayzSendingDistanceLabel] setText:d[kApiDistanceMilesName]];
            }
        }
    }
    [[self autoStarSwitch] setOn:[[SettingsManager sharedManager] shouldAutoStarRayz]];
    [[self locationSwitch] setOn:[[SettingsManager sharedManager] isLocationEnabled]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)autoStarChanged:(id)sender {
    [[SettingsManager sharedManager] setAutoStarRayz:[[self autoStarSwitch] isOn]];
}

- (IBAction)locationChanged:(id)sender {
    [[SettingsManager sharedManager] setLocationEnabled:[[self locationSwitch] isOn]];
}

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath section] == 0) {
        if ([indexPath row] == 0) {
            UIActionSheet * metricAction = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:distanceMetricKilometers, distanceMetricMiles, nil];
            [metricAction setTag:kMetricActionTag];
            [metricAction showFromTabBar:[[[self navigationController] tabBarController] tabBar]];
        }
        else if ([indexPath row] == 1) {
            NSArray * distances = [Rayz validDistances];
            UIActionSheet * distanceAction = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
            [distanceAction setTag:kDistanceActionTag];
            for (NSDictionary * d in distances) {
                if ([[[SettingsManager sharedManager] distanceMetric] isEqualToString:distanceMetricKilometers]) {
                    [distanceAction addButtonWithTitle:d[kApiDistanceKilometersName]];
                }
                else {
                    [distanceAction addButtonWithTitle:d[kApiDistanceMilesName]];
                }
            }
            
            [distanceAction addButtonWithTitle:@"Cancel"];
            [distanceAction setCancelButtonIndex:[distances count]];
            
            [distanceAction showFromTabBar:[[[self navigationController] tabBarController] tabBar]];
        }
    }
    [[self tableView] deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark UIActionSheet delegate
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if ([actionSheet tag] == kDistanceActionTag) {
        if (buttonIndex != [actionSheet cancelButtonIndex]) {
            NSArray * distances = [Rayz validDistances];
            [[SettingsManager sharedManager] setMaximumRayzSendingDistance:[distances[buttonIndex][kApiDistanceValue] integerValue]];
            [self refreshUI];
        }
    }
    else if ([actionSheet tag] == kMetricActionTag) {
        if (buttonIndex != [actionSheet cancelButtonIndex]) {
            [[SettingsManager sharedManager] setDistanceMetric:[actionSheet buttonTitleAtIndex:buttonIndex]];
            [self refreshUI];
        }
    }
}

@end
