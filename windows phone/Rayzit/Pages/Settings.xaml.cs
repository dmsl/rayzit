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
//  Rayzit
//
//  Created by COSTANTINOS COSTA - GEORGE NIKOLAIDES.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

using System;
using System.Windows;
using System.Windows.Navigation;
using Rayzit.Resources.HelperClasses.FlipTile;

namespace Rayzit.Pages
{
    public partial class Settings
    {
        readonly String[] _options = { "unlimited","0.5 km",
                              "5 km","50 km",
                              "500 km", "5000 km"};

        readonly String[] _optionsMiles = { "unlimited","0.3 miles",
                              "3 miles","30 miles",
                              "300 miles", "3000 miles"};

        readonly String[] _distanceMetrics = { "Kilometers ", "Miles" };

        //RayzitSettings settings = new RayzitSettings();

        public Settings()
        {
            InitializeComponent();
            DistanceMetricLP.ItemsSource = _distanceMetrics;
            SetDistanceMetric();

            Loaded += Settings_Loaded;
        }

        void Settings_Loaded(object sender, RoutedEventArgs e)
        {
            // Put list pickers to the appropriate values
            var temp2 = App.Settings.MetricListBoxSetting;
            DistanceMetricLP.SelectedIndex = 0;
            DistanceMetricLP.SelectedIndex = temp2;

            SetDistanceMetric();
        }

        private void SetDistanceMetric()
        {
            var temp = App.Settings.ListBoxSetting;
            DistanceLP.ItemsSource = App.Settings.MetricListBoxSetting == 0 ? _options : _optionsMiles;
            DistanceLP.SelectedIndex = 0;
            DistanceLP.SelectedIndex = temp;
        }

        private void LiveTileSwitch_Unchecked(object sender, RoutedEventArgs e)
        {
            FlipTileManager.ClearFlipTile();
        }

        private void DistanceMetricLP_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            SetDistanceMetric();
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            // Get a dictionary of query string keys and values.
            var queryStrings = NavigationContext.QueryString;

            string msg;
            if (queryStrings.TryGetValue("NavigateTo", out msg))
                if (msg.Equals("location"))
                    RayzitSettingsPivot.SelectedIndex = 2;
        }
    }
}