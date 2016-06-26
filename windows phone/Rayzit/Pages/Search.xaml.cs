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
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Coding4Fun.Toolkit.Controls;
using Microsoft.Phone.Shell;
using Rayzit.ViewModels;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace Rayzit.Pages
{
    public partial class Search
    {
        public Search()
        {
            InitializeComponent();

            if (App.ViewModel.SearchRayz != null)
                App.ViewModel.SearchRayz.Clear();

            Loaded += Search_Loaded;
        }

        void Search_Loaded(object sender, RoutedEventArgs e)
        {
            FocusTextBox();
        }

        private void FocusTextBox()
        {
            SearchBox.Focus();

            if (SearchBox.Text.Equals(String.Empty))
            {
                SearchBox.Text = " ";
                SearchBox.Text = "";
            }
        }

        private void ListTap(object sender, GestureEventArgs gestureEventArgs)
        {
            var item = ((FrameworkElement)sender).DataContext as Rayz;

            // Something went wrong with the item
            if (item == null || item.RayzDate == 0)
                return;

            // The Rayz has failed so try to resend it
            if (item.RayzDate == 1)
            {
                App.ViewModel.RetrySendingRayz(item);
                return;
            }

            if (item.RayzId.Equals(String.Empty))
                return;

            App.ViewModel.SelectedRayz = item;
            RayzDetails.List = "Search";
            NavigationService.Navigate(new Uri("/Pages/RayzDetails.xaml", UriKind.Relative));
        }

        private void SearchBox_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.Key != Key.Enter) 
                return;

            if (SearchBox.Text.Equals(String.Empty))
            {
                ResultsRayzList.ItemsSource = null;
                NoResultsGrid.Visibility = Visibility.Collapsed;
            }
            else
            {
                LoadingBar.Visibility = Visibility.Visible;

                App.ViewModel.GetRayzByTerm(SearchBox.Text.ToLower());

                if (App.ViewModel.SearchRayz.Count == 0)
                    NoResultsGrid.Visibility = Visibility.Visible;
                else
                {
                    NoResultsGrid.Visibility = Visibility.Collapsed;
                    ResultsRayzList.ItemsSource = App.ViewModel.SearchRayz;
                }

                LoadingBar.Visibility = Visibility.Collapsed;
            }

            Focus();
        }

        private void ShareRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((Button)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            // Create a toast notification.
            // The toast notification will not be shown if the foreground app is running.
            var toast = GetToastWithImgAndTitle();
            toast.Show();

            Clipboard.SetText("rayzit.com/" + selectedRayz.RayzId);
        }

        private static ToastPrompt GetToastWithImgAndTitle()
        {
            return new ToastPrompt
            {
                Message = "Rayz share link copied to clipboard!",
                Foreground = new SolidColorBrush(Colors.White),
                Background = new SolidColorBrush(Color.FromArgb(250, 236, 62, 35)),
                ImageSource = new BitmapImage(new Uri("/Assets/Toast/toast_ico.png", UriKind.RelativeOrAbsolute)),
                ImageHeight = 30,
                ImageWidth = 30
            };
        }

        private void ReRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((Button)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.ReRayz(selectedRayz);
        }

        private void StarRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((Button)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.CreateStarredRayz(selectedRayz);
        }
    }
}