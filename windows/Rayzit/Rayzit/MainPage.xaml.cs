using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using CodeTitans.JSon;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Rayzit.Pages;
using Rayzit.Resources.HelperClasses.FlipTile;
using Rayzit.ViewModels;
using Windows.Devices.Geolocation;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace Rayzit
{
    public partial class MainPage
    {
        private bool _initialized;

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            // Set the Data Context
            DataContext = App.ViewModel;

            //// Pull to Refresh
            //// Live
            //var pdL = new PullDetector();
            //pdL.Bind(LiveRayzList);
            //pdL.Compression += pd_Compression;
            //// Starred
            //var pdS = new PullDetectorM();
            //pdS.Bind(StarredRayzList);
            //pdS.Compression += pd_CompressionM;
            //// My
            //var pdM = new PullDetectorM();
            //pdM.Bind(MyRayzList);
            //pdM.Compression += pd_CompressionM;

            // Delegates
            Loaded += MainPage_Loaded;

            FlipTileManager.ClearFlipTile();
        }

        #region Page Initialization

        void MainPage_Loaded(object sender, RoutedEventArgs e)
        {
            // Refresh the Starred Rayz List 
            // (update the starred list when navigating from 
            //     rayzdetails in case a rayz has been starred)
            App.ViewModel.RefreshStarredRayzList();

            if (!_initialized)
            {
                InitializeRayzitComponents();

                ServerSync(true);
                _initialized = true;
            }

            if (App.Settings.PlayIntroVideoSetting)
                ShowIntroVideoMessageBox();
        }

        private void ShowIntroVideoMessageBox()
        {
            var messageBox = new CustomMessageBox
            {
                Title = (string)Application.Current.Resources["IntroVideoTitle"],
                Message = (string)Application.Current.Resources["IntroVideoMessage"],
                LeftButtonContent = "yes",
                RightButtonContent = "don't show again"
            };

            messageBox.Dismissed += (s2, e2) =>
            {
                switch (e2.Result)
                {
                    case CustomMessageBoxResult.LeftButton:
                        App.Settings.PlayIntroVideoSetting = false;
                        PlayIntroVideo();
                        break;
                    case CustomMessageBoxResult.RightButton:
                        App.Settings.PlayIntroVideoSetting = false;
                        break;
                }
            };

            messageBox.Show();
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

        private async void ServerSync(bool silentMode)
        {
            if (!(await App.NetMngr.InternetAvailableAsync(silentMode)))
                return;

            App.RpncMgr.ConnectFaye();

            await RegisterUser(silentMode);

            ServerSyncStarredMy(silentMode);
            ServerSyncLive(silentMode);

            App.ViewModel.ServerDataSyncronization();
        }

        private async void ServerSyncLive(bool silentMode)
        {
            ShowLoadingBars();

            if (!(await App.NetMngr.InternetAvailableAsync(silentMode)))
            {
                HideLoadingBars();
                return;
            }

            App.RpncMgr.ConnectFaye();
            await App.ViewModel.UpdateLive(silentMode);

            HideLoadingBars();
        }

        private async void ServerSyncStarredMy(bool silentMode)
        {
            ShowLoadingBars();

            if (!(await App.NetMngr.InternetAvailableAsync(silentMode)))
            {
                HideLoadingBars();
                return;
            }

            if (!(await App.ViewModel.GetRayzUpdates(silentMode)))
            {
                HideLoadingBars();
                return;
            }

            App.RpncMgr.ConnectFaye();

            HideLoadingBars();

            App.ViewModel.RefreshAllLists();
        }

        private static async Task<bool> RegisterUser(bool silentMode)
        {
            if (!App.Settings.LocationToggleSwitchSetting)
                return true;

            if (await App.LocFinder.GeolocationAvailableAsync(silentMode))
            {
                var x = await
                       App.Rsc.UpdatePosition(App.LocFinder.Info.Latitude, App.LocFinder.Info.Longitude, App.LocFinder.Info.Accuracy);

                CheckResponseError(x);

                return x != null;
            }

            return true;
        }

        public static void CheckResponseError(IJSonObject e)
        {
            if (e == null)
                return;

            if (e.Contains("message"))
            {
                if (e["message"].StringValue.Equals("Please specify a correct Application Id."))
                    Deployment.Current.Dispatcher.BeginInvoke(
                        () =>
                        {
                            var res = MessageBox.Show((string)Application.Current.Resources["VersionKeyErrorMessage"],
                                            (string)Application.Current.Resources["GeneralErrorTitle"],
                                            MessageBoxButton.OK);

                            if (res == MessageBoxResult.OK || res == MessageBoxResult.None)
                                Application.Current.Terminate();
                        });
            }
        }

        private void InitializeRayzitComponents()
        {
            ApplicationBar = Resources["BeamsArroundAppBar"] as ApplicationBar;
        }

        private void ShowLoadingBars()
        {
            var selectedItem = ContentPivot.SelectedItem as PivotItem;

            if (selectedItem == null)
                return;

            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    var strTag = (string)selectedItem.Tag;

                    if (strTag.Equals("LiveRayz"))
                    {
                        LoadingBarsSp.Visibility = Visibility.Visible;
                    }
                    else if (strTag.Equals("StarredRayz"))
                    {
                        LoadingBarsSpS.Visibility = Visibility.Visible;
                    }
                    else if (strTag.Equals("MyRayz"))
                    {
                        LoadingBarsSpM.Visibility = Visibility.Visible;
                    }
                });
        }

        private void HideLoadingBars()
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    LoadingBarsSp.Visibility = Visibility.Collapsed;

                    LoadingBarsSpS.Visibility = Visibility.Collapsed;

                    LoadingBarsSpM.Visibility = Visibility.Collapsed;
                });
        }

        #endregion

        #region Application Event Handlers

        #region PANORAMA LIVE RAYZ

        // Pull to refresh
        //void pd_Compression(object sender, CompressionEventArgs e)
        //{
        //    if (e.Type != CompressionType.Top)
        //        return;

        //    ServerSyncLive(false);
        //}

        private void LiveRayzListTap(object sender, GestureEventArgs gestureEventArgs)
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
            RayzDetails.List = "Live";
            NavigationService.Navigate(new Uri("/Pages/RayzDetails.xaml", UriKind.Relative));
        }

        private void FilterBeamsFeedAppBarButton_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/Search.xaml", UriKind.Relative));
        }


        #endregion

        #region PANORAMA STARRED RAYZ

        // Pull To Refresh
        //void pd_CompressionM(object sender, CompressionEventArgs e)
        //{
        //    if (e.Type != CompressionType.Top)
        //        return;

        //    ServerSyncStarredMy(false);
        //}

        private void StarredRayzTap(object sender, GestureEventArgs e)
        {
            var item = ((FrameworkElement)sender).DataContext as Rayz;

            // Something went wrong with the item
            if (item == null)
                return;

            // The Rayz has failed so try to resend it
            if (item.RayzDate == 1 || item.RayzDate == 0)
            {
                App.ViewModel.RetrySendingRayz(item);
                return;
            }

            if (item.RayzId.Equals(String.Empty))
                return;

            App.ViewModel.SelectedRayz = item;
            RayzDetails.List = "Starred";
            NavigationService.Navigate(new Uri("/Pages/RayzDetails.xaml", UriKind.Relative));
        }

        private void SelectBeamsFavoritesAppBar_Click(object sender, EventArgs e)
        {
            StarredRayzList.IsSelectionEnabled = !StarredRayzList.IsSelectionEnabled;
        }

        private void SelectAllBeamsFavoritesAppBar_Click(object sender, EventArgs e)
        {
            var b = ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text.Equals("select all");

            ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri =
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text == "select all"
                        ? new Uri("/Toolkit.Content/ApplicationBar.Cancel.png", UriKind.Relative)
                        : new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
            ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text =
                ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text == "select all"
                    ? "select none"
                    : "select all";

            if (b)
            {
                foreach (var item in StarredRayzList.ItemsSource)
                    if (!(StarredRayzList.SelectedItems.Contains(item)))
                        StarredRayzList.SelectedItems.Add(item);
            }
            else
                StarredRayzList.SelectedItems.Clear();
        }

        private void DeleteBeamsFavoriteAppBarButton_Click(object sender, EventArgs e)
        {
            var selectedRayz = new ObservableCollection<Rayz>();

            foreach (var rayz in StarredRayzList.SelectedItems)
                selectedRayz.Add((Rayz)rayz);

            App.ViewModel.MultiUnstarStarredRayz(selectedRayz);
        }

        #endregion

        #region PANORAMA MY RAYZ
        // PANORAMA MY RAYZ

        private void MyRayzTap(object sender, GestureEventArgs e)
        {
            var item = ((FrameworkElement)sender).DataContext as Rayz;

            // Something went wrong with the item
            if (item == null)
                return;

            // The Rayz has failed so try to resend it
            if (item.RayzDate == 1 || item.RayzDate == 0)
            {
                App.ViewModel.RetrySendingRayz(item);
                return;
            }

            if (item.RayzId.Equals(String.Empty))
                return;

            App.ViewModel.SelectedRayz = item;
            RayzDetails.List = "My";
            NavigationService.Navigate(new Uri("/Pages/RayzDetails.xaml", UriKind.Relative));
        }

        private void SelectBeamsInboxAppBarButton_Click(object sender, EventArgs e)
        {
            MyRayzList.IsSelectionEnabled = !MyRayzList.IsSelectionEnabled;
        }

        private void SelectAllBeamsInboxAppBarButton_Click(object sender, EventArgs e)
        {
            var b = ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text.Equals("select all");

            ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri = ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text == "select all" ? new Uri("/Toolkit.Content/ApplicationBar.Cancel.png", UriKind.Relative) : new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
            ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text = ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text == "select all" ? "select none" : "select all";

            if (b)
            {
                foreach (var item in MyRayzList.ItemsSource)
                    if (!(MyRayzList.SelectedItems.Contains(item)))
                        MyRayzList.SelectedItems.Add(item);
            }
            else
                MyRayzList.SelectedItems.Clear();
        }

        private void DeleteBeamsInboxAppBarButton_Click(object sender, EventArgs e)
        {
            var selectedRayz = new ObservableCollection<Rayz>();

            foreach (var rayz in MyRayzList.SelectedItems)
                selectedRayz.Add((Rayz)rayz);

            App.ViewModel.MultiDeleteMyRayz(selectedRayz);
        }

        #endregion

        // GENERAL BUTTONS

        /// <summary>
        /// Opens the Create Message (Rayz) page.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void NewRayzAppBarButton_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/NewRayz.xaml?NavigatedFrom=Rayz", UriKind.Relative));
        }

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
        }

        /// <summary>
        /// Called to manually refresh the lists
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Live_RefreshTapped(object sender, EventArgs e)
        {
            ServerSyncLive(false);
        }

        /// <summary>
        /// Called to manually refresh the lists
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void StarredMy_RefreshTapped(object sender, EventArgs e)
        {
            ServerSyncStarredMy(false);
        }

        #endregion

        #region Context Menu Commands
        private void ContextMenu_Unloaded(object sender, RoutedEventArgs e)
        {
            var conmen = (sender as ContextMenu);
            if (conmen != null)
                conmen.ClearValue(DataContextProperty);
        }

        private void ReRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((Button)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.ReRayz(selectedRayz);
        }

        //private void ShareRayz(object sender, RoutedEventArgs e)
        //{
        //    var selectedRayz = ((Button)sender).DataContext as Rayz;
        //    if (selectedRayz == null)
        //        return;

        //    // Create a toast notification.
        //    // The toast notification will not be shown if the foreground app is running.
        //    var toast = GetToastWithImgAndTitle();
        //    toast.Show();

        //    Clipboard.SetText("rayzit.com/" + selectedRayz.RayzId);
        //}

        //private static ToastPrompt GetToastWithImgAndTitle()
        //{
        //    return new ToastPrompt
        //    {
        //        Message = "Rayz share link copied to clipboard!",
        //        Foreground = new SolidColorBrush(Colors.White),
        //        Background = new SolidColorBrush(Color.FromArgb(250, 236, 62, 35)),
        //        ImageSource = new BitmapImage(new Uri("/Assets/Toast/toast_ico.png", UriKind.RelativeOrAbsolute)),
        //        ImageHeight = 30,
        //        ImageWidth = 30
        //    };
        //}

        private void StarRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((Button)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.CreateStarredRayz(selectedRayz);
        }

        private void HideRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((MenuItem)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            //if (selectedRayz.IsMy)
            //    App.ViewModel.DeleteMyRayz(selectedRayz);
            //else
            //    App.ViewModel.HideLiveRayz(selectedRayz);

            App.ViewModel.RemoveRayz(selectedRayz);
        }

        private void DeleteRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((MenuItem)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.RemoveRayz(selectedRayz);
        }

        private void ReportRayz(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((MenuItem)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.ReportLiveRayz(selectedRayz);
        }

        private void CopyToClipboard(object sender, RoutedEventArgs e)
        {
            var selectedRayz = ((MenuItem)sender).DataContext as Rayz;
            if (selectedRayz == null)
                return;

            App.ViewModel.CopyRayzToClipboard(selectedRayz);
        }
        #endregion

        #region MainPage Overrides

        protected override void OnBackKeyPress(System.ComponentModel.CancelEventArgs e)
        {
            if (StarredRayzList.IsSelectionEnabled)
            {
                if (((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text.Equals("select none"))
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri = new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text = "select all";
                }

                StarredRayzList.IsSelectionEnabled = false;
                ApplicationBar = Resources["FavoritesAppBar"] as ApplicationBar;
                e.Cancel = true;
                return;
            }

            if (MyRayzList.IsSelectionEnabled)
            {
                if (((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text.Equals("select none"))
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri = new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text = "select all";
                }

                MyRayzList.IsSelectionEnabled = false;
                ApplicationBar = Resources["MyBeamsAppBar"] as ApplicationBar;
                e.Cancel = true;
                return;
            }

            base.OnBackKeyPress(e);
        }

        protected override void OnNavigatedTo(System.Windows.Navigation.NavigationEventArgs e)
        {
            string msg;
            if (NavigationContext.QueryString.TryGetValue("RemoveEntry", out msg))
            {
                while (NavigationService.CanGoBack)
                    NavigationService.RemoveBackEntry();

                NavigationContext.QueryString.Clear();
            }

            // Clear Selected Rayz
            if (App.ViewModel.SelectedRayz != null)
            {
                if (App.ViewModel.SelectedRayz.UnreadRayzReplies > 0)
                    App.ViewModel.MarkActiveListAsRead(App.ViewModel.SelectedRayz.RayzId);

                App.ViewModel.ClearActiveRayzReplies();
                App.ViewModel.ClearAttachmentsList();

                App.ViewModel.SelectedRayz = null;
            }

            if (App.Geolocator != null)
                return;

            App.Geolocator = new Geolocator { DesiredAccuracyInMeters = 200, DesiredAccuracy = PositionAccuracy.Default, MovementThreshold = 200 };
            App.Geolocator.PositionChanged += geolocator_PositionChanged;
        }

        protected override void OnRemovedFromJournal(System.Windows.Navigation.JournalEntryRemovedEventArgs e)
        {
            App.Geolocator.PositionChanged -= geolocator_PositionChanged;
            App.Geolocator = null;
        }

        #endregion

        #region Background Jobs

        static async void geolocator_PositionChanged(Geolocator sender, PositionChangedEventArgs args)
        {
            if (!App.RunningInBackground)
            {
                App.LocFinder.UpdateInfo(args.Position);

                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    return;

                if (App.Settings.LocationToggleSwitchSetting)
                    await
                        App.Rsc.UpdatePosition(args.Position.Coordinate.Latitude.ToString("0.00"),
                                               args.Position.Coordinate.Longitude.ToString("0.00"),
                                               args.Position.Coordinate.Accuracy.ToString(CultureInfo.InvariantCulture));

            }
            else
            {
                App.LocFinder.UpdateInfo(args.Position);

                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    return;

                if (App.Settings.LocationToggleSwitchSetting)
                    await App.Rsc.UpdatePosition(args.Position.Coordinate.Latitude.ToString("0.00"), args.Position.Coordinate.Longitude.ToString("0.00"), args.Position.Coordinate.Accuracy.ToString(CultureInfo.InvariantCulture));

                App.ViewModel.UpdateLiveFeedRandom();
            }
        }

        #endregion

        #region Dynamic Application Bar

        private void Panorama_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (e.AddedItems.Count < 1) return;

            if (!(e.AddedItems[0] is PivotItem)) return;

            var selectedItem = (PivotItem)e.AddedItems[0];

            var strTag = (string)selectedItem.Tag;

            StarredRayzList.IsSelectionEnabled = false;
            MyRayzList.IsSelectionEnabled = false;

            if (strTag.Equals("LiveRayz"))
                ApplicationBar = Resources["BeamsArroundAppBar"] as ApplicationBar;
            else if (strTag.Equals("StarredRayz"))
            {
                NoStarredGrid.Visibility = App.ViewModel.StarredRayz.Count == 0 ? Visibility.Visible : Visibility.Collapsed;

                ApplicationBar = Resources["FavoritesAppBar"] as ApplicationBar;
                if (ApplicationBar != null)
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[2]).IsEnabled = StarredRayzList.ItemsSource.Count > 0;
            }
            else if (strTag.Equals("MyRayz"))
            {
                NoMyGrid.Visibility = App.ViewModel.MyRayz.Count == 0 ? Visibility.Visible : Visibility.Collapsed;

                ApplicationBar = Resources["MyBeamsAppBar"] as ApplicationBar;
                if (ApplicationBar != null)
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[2]).IsEnabled = MyRayzList.ItemsSource.Count > 0;
            }
        }

        private void StarredRayzList_IsSelectionEnabledChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            var selectedItem = ContentPivot.SelectedItem as PivotItem;

            if (selectedItem == null) return;
            if (!selectedItem.Tag.Equals("StarredRayz")) return;

            if (StarredRayzList.IsSelectionEnabled)
            {
                ApplicationBar = Resources["FavoritesDeleteAppBar"] as ApplicationBar;
                if (ApplicationBar != null)
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri = new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text = "select all";

                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = StarredRayzList.ItemsSource.Count > 0;
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[1]).IsEnabled = false;
                }
            }
            else
            {
                ApplicationBar = Resources["FavoritesAppBar"] as ApplicationBar;
            }
        }

        private void StarredRayzList_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    if (ApplicationBar != Resources["FavoritesDeleteAppBar"] as ApplicationBar)
                        return;

                    if (ApplicationBar != null)
                        ((ApplicationBarIconButton)ApplicationBar.Buttons[1]).IsEnabled = StarredRayzList.SelectedItems.Count >
                                                                                          0;
                });
        }

        private void MyRayzList_IsSelectionEnabledChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            var selectedItem = ContentPivot.SelectedItem as PivotItem;

            if (selectedItem == null) return;
            if (!selectedItem.Tag.Equals("MyRayz")) return;

            if (MyRayzList.IsSelectionEnabled)
            {
                ApplicationBar = Resources["MyBeamsDeleteAppBar"] as ApplicationBar;
                if (ApplicationBar != null)
                {
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IconUri = new Uri("/Toolkit.Content/ApplicationBar.Check.png", UriKind.Relative);
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).Text = "select all";

                    ((ApplicationBarIconButton)ApplicationBar.Buttons[0]).IsEnabled = MyRayzList.ItemsSource.Count > 0;
                    ((ApplicationBarIconButton)ApplicationBar.Buttons[1]).IsEnabled = false;
                }
            }
            else
            {
                ApplicationBar = Resources["MyBeamsAppBar"] as ApplicationBar;
            }
        }

        private void MyRayzList_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    if (ApplicationBar != Resources["MyBeamsDeleteAppBar"] as ApplicationBar)
                        return;

                    if (ApplicationBar != null)
                        ((ApplicationBarIconButton)ApplicationBar.Buttons[1]).IsEnabled = MyRayzList.SelectedItems.Count >
                                                                                           0;
                });
        }

        #endregion
    }
}