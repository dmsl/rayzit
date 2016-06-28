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
using System.ComponentModel;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Media;
using Microsoft.Xna.Framework.Media.PhoneExtensions;
using Rayzit.Pages.Attachments;
using RayzitServiceClient.HelperClasses;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace Rayzit.Pages
{
    public partial class NewRayz
    {
        readonly String[] _options = { "unlimited","0.5 km",
                              "5 km","50 km",
                              "500 km", "5000 km"};

        readonly String[] _optionsMiles = { "unlimited","0.3 miles",
                              "3 miles","30 miles",
                              "300 miles", "3000 miles"};

        private string _navigatedFrom = "";
        private SoundEffectInstance _soundInstance;
        private bool _loaded;

        bool _isPinch;
        
        private int _distanceSettingOption;
        private int _settingFirst;

        public NewRayz()
        {
            InitializeComponent();

            InitializeAttachments();

            DataContext = App.ViewModel;

            SetDistanceMetric();
            _settingFirst = App.Settings.ListBoxSetting;

            PowerBar.DataContext = App.Pb;

            Loaded += NewRayz_Loaded;
        }

        private void SetDistanceMetric()
        {
            DistanceLP.ItemsSource = App.Settings.MetricListBoxSetting == 0 ? _options : _optionsMiles;
            DistanceLP.SelectedIndex = App.Settings.ListBoxSetting;
            _distanceSettingOption = App.Settings.ListBoxSetting;
        }

        void NewRayz_Loaded(object sender, RoutedEventArgs e)
        {
            if (!_loaded && _settingFirst != App.Settings.ListBoxSetting)
            {
                SetDistanceMetric();
                _settingFirst = App.Settings.ListBoxSetting;
                _loaded = true;
            }

            UpdateAttachmentsVisibility();
            UpdateRayzHint();
            FocusTextBox();
        }

        private void UpdateAttachmentsVisibility()
        {
            AttachmentsList.Visibility = App.ViewModel.Attachments != null && App.ViewModel.Attachments.Count > 0 ? Visibility.Visible : Visibility.Collapsed;
        }

        private void UpdateRayzHint()
        {
            if (_navigatedFrom.Equals("Rayz"))
            {
                switch (_distanceSettingOption)
                {
                    case 1:
                        RayzTb.Hint = App.Settings.MetricListBoxSetting == 0
                            ? "rayz your message (0.5 km)"
                            : "rayz your message (0.3 miles)";
                        break;
                    case 2:
                        RayzTb.Hint = App.Settings.MetricListBoxSetting == 0
                            ? "rayz your message (5 km)"
                            : "rayz your message (3 miles)";
                        break;
                    case 3:
                        RayzTb.Hint = App.Settings.MetricListBoxSetting == 0
                            ? "rayz your message (50 km)"
                            : "rayz your message (30 miles)";
                        break;
                    case 4:
                        RayzTb.Hint = App.Settings.MetricListBoxSetting == 0
                            ? "rayz your message (500 km)"
                            : "rayz your message (300 miles)";
                        break;
                    case 5:
                        RayzTb.Hint = App.Settings.MetricListBoxSetting == 0
                            ? "rayz your message (5000 km)"
                            : "rayz your message (3000 miles)";
                        break;
                    default:
                        RayzTb.Hint = "rayz your message";
                        break;
                }
            }
            else if (_navigatedFrom.Equals("RayzReply"))
            {
                RayzTb.Hint = "rayz your reply";

            }
        }

        private void RayzPower_Tap(object sender, GestureEventArgs gestureEventArgs)
        {
            MessageBox.Show((string)Application.Current.Resources["RayzPowerMessage"],
                                (string)Application.Current.Resources["RayzPowerTitle"], MessageBoxButton.OK);
        }

        private void SendNewRayzAppBarButton_Click(object sender, EventArgs e)
        {
            if (!App.LocFinder.GeolocationSettingEnabled())
                return;

            ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = false;
            Focus();
            //var rayzMessage = FixMessage(RayzTb.Text);
            var rayzMessage = RayzTb.Text;

            if (_navigatedFrom.Equals("Rayz") || _navigatedFrom.Equals("Share"))
                App.ViewModel.AddLiveRayz(rayzMessage, _distanceSettingOption);
            else if (_navigatedFrom.Equals("RayzReply"))
            {
                App.ViewModel.AddRayzReply(rayzMessage);

                if (App.Settings.AutoFavToggleSwitchSetting && !App.ViewModel.SelectedRayz.IsStarred)
                    App.ViewModel.CreateStarredRayz(App.ViewModel.SelectedRayz);
            }

            if (_navigatedFrom.Equals("Share"))
                NavigationService.Navigate(new Uri("/MainPage.xaml?RemoveEntry=true", UriKind.Relative));
            else
                NavigationService.GoBack();
        }

        private void RayzTb_GotFocus(object sender, RoutedEventArgs e)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                if (ApplicationBar != null)
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = RayzTb.Text.Length > 0;

                    if (App.ViewModel.Attachments != null)
                        ((ApplicationBarIconButton)ApplicationBar.Buttons[1]).IsEnabled = App.ViewModel.Attachments.Count < 6;
                }
            });
        }

        private void RayzTb_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (RayzTb.Text.Length > 0)
            {
                var shown = false;

                foreach (var c in RayzTb.Text.ToCharArray().Where(c => c > 9000))
                {
                    var curretPos = RayzTb.SelectionStart - 1;
                    RayzTb.Text = RayzTb.Text.Replace(c.ToString(CultureInfo.InvariantCulture), "");
                    RayzTb.Select(curretPos, 0);

                    if (shown)
                        continue;

                    MessageBox.Show("These emotions are not supported by the current version.", "Sorry",
                        MessageBoxButton.OK);
                    shown = true;
                }
            }

            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                if (ApplicationBar != null)
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = RayzTb.Text.Length > 0;
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = !String.IsNullOrWhiteSpace(RayzTb.Text);
                }
            });
        }

        //private static String FixMessage(String msg)
        //{
        //    if (string.IsNullOrWhiteSpace(msg))
        //        return String.Empty;

        //    msg = System.Text.RegularExpressions.Regex.Replace(msg, @"\s+", " ");

        //    return msg.Trim();
        //}

        private void AttachAppBarButton_Click(object sender, EventArgs e)
        {
            AttachmentPicker.SelectedIndex = 4;
            AttachmentPicker.Open();
        }

        private void DistanceSetting_Click(object sender, EventArgs e)
        {
            DistanceLP.Open();
        }

        /// <summary>
        /// Called when delete picture is tapped : deletes the attachment
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnPictureDeleteItemTap(object sender, GestureEventArgs e)
        {
            var fe = sender as FrameworkElement;
            if (fe != null)
            {
                var temp = fe.DataContext as Attachment;
                if (temp != null)
                    App.ViewModel.Attachments.Remove(temp);

                UpdateAttachmentsVisibility();
            }
        }

        #region Page Overrides
        protected override void OnNavigatedTo(System.Windows.Navigation.NavigationEventArgs e)
        {
            // Get a dictionary of query string keys and values.
            var queryStrings = NavigationContext.QueryString;

            // Ensure that there is at least one key in the query string, and check whether the "token" key is present.
            if (queryStrings.ContainsKey("token"))
            {
                _navigatedFrom = "Share";

                if (App.ViewModel.Attachments == null || (App.ViewModel.Attachments != null && App.ViewModel.Attachments.Count < 6))
                {
                    // Retrieve the photo from the media library using the token passed to the app.
                    var library = new MediaLibrary();
                    var photoFromLibrary = library.GetPictureFromToken(queryStrings["token"]);

                    // Create a BitmapImage object and add set it as the image control source.
                    // To retrieve a full-resolution image, use the GetImage() method instead.
                    var fileName = Path.GetFileName(photoFromLibrary.Name);
                    fileName = new TimeSpan(DateTime.Now.Ticks - DateTime.MinValue.Ticks).TotalMilliseconds + fileName;

                    App.ViewModel.AddAttachment(photoFromLibrary.GetPreviewImage(), fileName,
                                                RayzItAttachment.ContentType.Image);
                }
                else
                    Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show("Photo could not be attached. Limit exceeded.",
                                    "Warning", MessageBoxButton.OK));

                NavigationContext.QueryString.Clear();
            }

            if (queryStrings.ContainsKey("FileId"))
            {
                _navigatedFrom = "Share";

                if (App.ViewModel.Attachments == null || (App.ViewModel.Attachments != null && App.ViewModel.Attachments.Count < 6))
                {
                    // Retrieve the photo from the media library using the token passed to the app.
                    var library = new MediaLibrary();
                    var photoFromLibrary = library.GetPictureFromToken(queryStrings["FileId"]);

                    // Create a BitmapImage object and add set it as the image control source.
                    // To retrieve a full-resolution image, use the GetImage() method instead.
                    var fileName = Path.GetFileName(photoFromLibrary.Name);
                    fileName = new TimeSpan(DateTime.Now.Ticks - DateTime.MinValue.Ticks).TotalMilliseconds + fileName;

                    App.ViewModel.AddAttachment(photoFromLibrary.GetPreviewImage(), fileName,
                                                RayzItAttachment.ContentType.Image);
                }
                else
                    Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show("Photo could not be attached. Limit exceeded.",
                                    "Warning", MessageBoxButton.OK));

                NavigationContext.QueryString.Clear();
            }

            string msg;
            if (queryStrings.TryGetValue("NavigatedFrom", out msg))
            {
                _navigatedFrom = msg;
                NavigationContext.QueryString.Clear();
            }

            UpdateAttachmentsVisibility();
            UpdateApplicationBar();
            UpdateRayzHint();
            FocusTextBox();
        }

        private void UpdateApplicationBar()
        {
            if (!_navigatedFrom.Equals("Rayz"))
                ApplicationBar.Buttons.RemoveAt(2);
        }

        private void FocusTextBox()
        {
            RayzTb.Focus();

            if (RayzTb.Text.Equals(String.Empty))
            {
                RayzTb.Text = " ";
                RayzTb.Text = "";
            }
        }

        protected override void OnBackKeyPress(CancelEventArgs e)
        {
            StopSound();

            if (ZoomGrid.Visibility == Visibility.Visible)
            {
                ZoomGrid.Visibility = Visibility.Collapsed;
                if (ApplicationBar != null)
                    ApplicationBar.IsVisible = true;
                FocusTextBox();
                e.Cancel = true;
                return;
            }

            if (!RayzTb.Text.Equals(String.Empty) || (App.ViewModel.Attachments != null && App.ViewModel.Attachments.Count > 0))
            {
                var mb = MessageBox.Show("Your rayz will be discarded.", "Warning", MessageBoxButton.OKCancel);

                if (mb != MessageBoxResult.OK)
                    e.Cancel = true;
            }

            App.ViewModel.ClearAttachmentsList();
        }
        #endregion

        #region Application Bar

        /// <summary>
        /// Opens the About page.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void About_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/About.xaml", UriKind.Relative));
        }

        /// <summary>
        /// Opens the rate & review in market place.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void RateReview_Click(object sender, EventArgs e)
        {
            var marketplaceReviewTask = new MarketplaceReviewTask();
            marketplaceReviewTask.Show();
        }

        /// <summary>
        /// Opens the Rayzit Tutorial Pivot Page
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Help_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/Tutorial/RayzitTutorial.xaml", UriKind.Relative));
        }

        /// <summary>
        /// Opens the Settings page.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Settings_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/Settings.xaml", UriKind.Relative));
            
            // Force listbox and rayz display setting to update
            _loaded = false;
        }

        private void PlayIntroVideo()
        {
            var mediaPlayerLauncher = new MediaPlayerLauncher
            {
                Media = new Uri("Assets/IntroVideo/intro.mp4", UriKind.Relative),
                Location = MediaLocationType.Install,
                Controls = MediaPlaybackControls.Pause | MediaPlaybackControls.Stop,
                Orientation = MediaPlayerOrientation.Landscape
            };
            mediaPlayerLauncher.Show();
        }

        private void PlayIntroVideo_OnClick(object sender, EventArgs e)
        {
            PlayIntroVideo();
        }

        #endregion

        #region Attachments

        PhotoChooserTask _photoChooserTask;
        CameraCaptureTask _cameraCaptureTask;

        private void InitializeAttachments()
        {
            _photoChooserTask = new PhotoChooserTask();
            _photoChooserTask.Completed += photoChooserTask_Completed;

            _cameraCaptureTask = new CameraCaptureTask();
            _cameraCaptureTask.Completed += cameraCaptureTask_Completed;
        }

        void photoChooserTask_Completed(object sender, PhotoResult e)
        {
            if (e.TaskResult == TaskResult.OK)
            {
                // Create the file name.
                var fileName = Path.GetFileName(e.OriginalFileName);
                fileName = new TimeSpan(DateTime.Now.Ticks - DateTime.MinValue.Ticks).TotalMilliseconds + fileName;

                App.ViewModel.AddAttachment(e.ChosenPhoto, fileName, RayzItAttachment.ContentType.Image);
            }
        }

        public static void Save_VideoRecorderTask(String fileName)
        {
            if (fileName != null)
            {
                App.ViewModel.AddAttachment(null, fileName, RayzItAttachment.ContentType.Video);
            }
        }

        void cameraCaptureTask_Completed(object sender, PhotoResult e)
        {
            if (e.TaskResult == TaskResult.OK)
            {
                var library = new MediaLibrary();

                // Create the file name.
                var fileName = Path.GetFileName(e.OriginalFileName);
                fileName = new TimeSpan(DateTime.Now.Ticks - DateTime.MinValue.Ticks).TotalMilliseconds + fileName;

                library.SavePictureToCameraRoll(e.OriginalFileName, e.ChosenPhoto);

                App.ViewModel.AddAttachment(e.ChosenPhoto, fileName, RayzItAttachment.ContentType.Image);
            }
        }

        public static void Save_AudioRecorderTask(MemoryStream ms)
        {
            if (ms != null)
            {
                var msec = (DateTime.Now - new DateTime(1970, 1, 1, 0, 0, 0, 0).ToLocalTime()).TotalMilliseconds;

                App.ViewModel.AddAttachment(ms, "audio_" + msec + ".wav", RayzItAttachment.ContentType.Audio);
            }
        }

        private void AttachmentPicker_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (AttachmentPicker == null)
                return;

            switch (AttachmentPicker.SelectedIndex)
            {
                // album
                case 0:
                    _photoChooserTask.Show();
                    break;
                // photo
                case 1:
                    _cameraCaptureTask.Show();
                    break;
                // video
                case 2:
                    NavigationService.Navigate(new Uri("/Pages/Attachments/VideoRecorder.xaml", UriKind.Relative));
                    break;
                // recording
                case 3:
                    NavigationService.Navigate(new Uri("/Pages/Attachments/AudioRecorder.xaml", UriKind.Relative));
                    break;
            }
        }

        #endregion

        #region Picture display
        Attachment _currentPicture;

        /// <summary>
        /// Sets or gets the current displayed picture
        /// </summary>
        public Attachment CurrentPicture
        {
            set
            {
                _currentPicture = value;
                ZoomGrid.DataContext = _currentPicture;
                var hasPicture = (_currentPicture != null);
                ZoomGrid.Visibility = hasPicture ? Visibility.Visible : Visibility.Collapsed;
                if (ApplicationBar != null)
                    ApplicationBar.IsVisible = !hasPicture;
            }
            get { return _currentPicture; }
        }
        #endregion

        #region Image Viewer

        /// <summary>
        /// Called when a picture is tapped : open it
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnPictureItemTap(object sender, GestureEventArgs e)
        {
            var fe = sender as FrameworkElement;
            if (fe != null)
            {
                var temp = fe.DataContext as Attachment;
                if (temp != null)
                    switch (temp.Type)
                    {
                        case RayzItAttachment.ContentType.Image:
                            StopSound();
                            CurrentPicture = temp;
                            ResetImagePosition();
                            break;
                        case RayzItAttachment.ContentType.Audio:
                            PlaySound(temp.ByteArray);
                            break;
                        case RayzItAttachment.ContentType.Video:
                            StopSound();
                            var mediaPlayerLauncher = new MediaPlayerLauncher
                            {
                                Media = new Uri(temp.FileName, UriKind.Relative),
                                Location = MediaLocationType.Data,
                                Controls = MediaPlaybackControls.Pause | MediaPlaybackControls.Stop,
                                Orientation = MediaPlayerOrientation.Landscape
                            };

                            mediaPlayerLauncher.Show();
                            break;
                    }
            }
        }

        /// <summary>
        /// Plays the audio using SoundEffectInstance 
        /// so we can monitor the playback status.
        /// </summary>
        private void PlaySound(byte[] stream)
        {
            StopSound();

            // Play audio using SoundEffectInstance so we can monitor it's State 
            // and update the UI in the dt_Tick handler when it is done playing.
            var sound = new SoundEffect(stream, 16000, AudioChannels.Mono);
            _soundInstance = sound.CreateInstance();
            _soundInstance.Play();
        }

        private void StopSound()
        {
            if (_soundInstance != null)
            {
                _soundInstance.Stop();
                _soundInstance = null;
            }
        }

        private void OnDragDelta(ManipulationDeltaEventArgs e)
        {
            ImageTransformation.CenterX = (ImageTransformation.CenterX - e.DeltaManipulation.Translation.X * 2);
            ImageTransformation.CenterY = (ImageTransformation.CenterY - e.DeltaManipulation.Translation.Y * 2);

            if (ImageTransformation.CenterX < 0)
                ImageTransformation.CenterX = 0;
            else if (ImageTransformation.CenterX > ZoomGrid.ActualWidth)
                ImageTransformation.CenterX = ZoomGrid.ActualWidth;

            if (ImageTransformation.CenterY < 0)
                ImageTransformation.CenterY = 0;
            else if (ImageTransformation.CenterY > ZoomGrid.ActualHeight)
                ImageTransformation.CenterY = ZoomGrid.ActualHeight;
        }

        private void OnPinchDelta(ManipulationDeltaEventArgs e)
        {
            var curZoom = ImageTransformation.ScaleX * e.PinchManipulation.CumulativeScale;

            // Disable zooming in more than double the size and zooming out less than original size
            if (!(curZoom >= 1.0) || !(curZoom <= 4.0))
                return;

            ImageTransformation.ScaleX *= e.PinchManipulation.CumulativeScale;
            ImageTransformation.ScaleY *= e.PinchManipulation.CumulativeScale;
        }

        /// <summary>
        /// Resets the zoom to its original scale and position
        /// </summary>
        private void ResetImagePosition()
        {
            ImageTransformation.ScaleX = 1.0;
            ImageTransformation.ScaleY = 1.0;
            ImageTransformation.CenterX = 0;
            ImageTransformation.CenterY = 0;
            ImageTransformation.TranslateX = 0;
            ImageTransformation.TranslateY = 0;
        }

        private void ZoomGrid_OnManipulationDelta(object sender, ManipulationDeltaEventArgs e)
        {
            var oldIsPinch = _isPinch;
            _isPinch = e.PinchManipulation != null;

            if (oldIsPinch == false && _isPinch == false)
            {
                OnDragDelta(e);
            }
            else if (oldIsPinch && _isPinch)
            {
                OnPinchDelta(e);
            }
        }

        private void ZoomImage_OnDoubleTap(object sender, GestureEventArgs e)
        {
            var zoomLevel = ImageTransformation.ScaleX;

            if (zoomLevel >= 1.0 && zoomLevel < 2.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 2.0;
            else if (zoomLevel >= 2.0 && zoomLevel < 3.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 3.0;
            else if (zoomLevel >= 3.0 && zoomLevel < 4.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 4.0;
            else
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 1.0;
        }
        #endregion

        private void DistanceLP_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            _distanceSettingOption = DistanceLP.SelectedIndex;
            UpdateRayzHint();
        }
    }
}