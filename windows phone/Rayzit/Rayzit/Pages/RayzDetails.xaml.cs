using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Rayzit.Resources.HelperClasses;
using Rayzit.ViewModels;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace Rayzit.Pages
{
    public partial class RayzDetails
    {
        private bool _isActive;

        public static String List = "None";
        private bool _loaded;
        private bool _noreload;
        public static Grid NoRepliesGG;

        // Constructor
        public RayzDetails()
        {
            InitializeComponent();

            DataContext = App.ViewModel;

            Loaded += RayzDetails_Loaded;

            // Pull to Refresh
            //var pdL = new PullDetector();
            //pdL.Bind(RayzRepliesList);
            //pdL.Compression += pd_Compression;

            UpdateTitle();

            App.ViewModel.NotifyRemovingItem += ViewModel_NotifyRemovingItem;
            App.ViewModel.NotifyRemovingItemCompleted += ViewModel_NotifyRemovingItemCompleted;

            NoRepliesGG = NoRepliesGrid;

            App.ViewModel.ClearAttachmentsList();
        }

        #region Page Initialization

        void RayzDetails_Loaded(object sender, RoutedEventArgs e)
        {
            ApplicationBar = Resources["AppBarFav"] as ApplicationBar;

            if (!_noreload)
                LoadRayzReplies(App.ViewModel.SelectedRayz);

            _noreload = false;
        }

        private async void LoadRayzReplies(Rayz rayz)
        {
            if (rayz.HasBeenDeleted)
            {
                RayzDeletedGrid.Visibility = Visibility.Visible;
                NoRepliesGrid.Visibility = Visibility.Collapsed;
                App.ViewModel.ClearActiveRayzReplies();
                DisalbeAppBarButtons();
                return;
            }

            if (rayz.RayzId == null)
            {
                NavigationService.GoBack();
                return;
            }

            if (!_loaded)
            {
                await App.ViewModel.SetActiveRayzReplies(App.ViewModel.SelectedRayz.RayzId);
                NoRepliesGrid.Visibility = App.ViewModel.IsActiveRayzRepliesListEmpty() ? Visibility.Visible : Visibility.Collapsed;
                _loaded = true;
            }

            await LoadAnswers(true, rayz);
        }

        private async Task<bool> LoadAnswers(bool silentMode, Rayz rayz)
        {
            if (!(await App.NetMngr.InternetAvailableAsync(silentMode)))
                return true;

            if (rayz == null)
                return true;

            App.RpncMgr.ConnectFaye();

            var prog = new ProgressIndicator
                {
                    IsVisible = true,
                    IsIndeterminate = true,
                    Text = "Loading Rayz Replies..."
                };

            SystemTray.SetProgressIndicator(this, prog);

            var e = await App.Rsc.GetAnswers(rayz.RayzId);

            if (SystemTray.ProgressIndicator != null)
                SystemTray.ProgressIndicator.IsVisible = false;

            if (e == null)
                return true;

            var value = e["status"].StringValue;

            if (value.Equals("deleted"))
            {
                rayz.HasBeenDeleted = true;

                var temp = GetActiveRayzList();

                if (temp == null)
                {
                    NavigationService.GoBack();
                    return true;
                }

                RayzDeletedGrid.Visibility = Visibility.Visible;
                NoRepliesGrid.Visibility = Visibility.Collapsed;

                DisalbeAppBarButtons();
            }

            return true;
        }

        void ViewModel_NotifyRemovingItem(object sender, bool e)
        {
            var temp = GetActiveRayzList();

            if (temp == null)
            {
                NavigationService.GoBack();
                return;
            }

            if (temp.IndexOf(App.ViewModel.SelectedRayz) > 0)
                LoadPrevRayzDetails(null);
            else
                LoadNextRayzDetails(null);
        }

        void ViewModel_NotifyRemovingItemCompleted(object sender, bool e)
        {
            UpdateTitle();
        }

        private static ObservableCollection<Rayz> GetActiveRayzList()
        {
            switch (List)
            {
                case "Live":
                    return App.ViewModel.LiveRayz;
                case "Starred":
                    return App.ViewModel.StarredRayz;
                case "My":
                    return App.ViewModel.MyRayz;
                case "Search":
                    return App.ViewModel.SearchRayz;
                default:
                    return null;
            }
        }

        private void AddNewReply_Tap(object sender, GestureEventArgs e)
        {
            _noreload = true;
            NavigationService.Navigate(new Uri("/Pages/NewRayz.xaml?NavigatedFrom=RayzReply", UriKind.Relative));        
        }

        private bool LoadPrevRayzDetails(FrameworkElement fe)
        {
            if (fe != null)
            {
                var trans = fe.GetHorizontalOffset().Transform;
                trans.Animate(trans.X, 0, TranslateTransform.XProperty, 0, 0, new SineEase());
            }

            RayzDeletedGrid.Visibility = Visibility.Collapsed;
            EnableAppBarButtons();

            var temp = GetActiveRayzList();

            if (temp == null || temp.Count == 0)
                return false;

            var prevRayzIndex = temp.IndexOf(App.ViewModel.SelectedRayz) - 1;

            if (prevRayzIndex < 0 && temp.Count == 1)
                return false;

            if (App.ViewModel.SelectedRayz.UnreadRayzReplies > 0)
                App.ViewModel.MarkActiveListAsRead(App.ViewModel.SelectedRayz.RayzId);

            if (prevRayzIndex < 0)
                prevRayzIndex = temp.Count - 1;

            var prevRayz = temp[prevRayzIndex];

            if (String.IsNullOrEmpty(prevRayz.RayzId))
                LoadPrevRayzDetails(null);

            App.ViewModel.ClearAttachmentsList();
            App.ViewModel.SelectedRayz = prevRayz;
            _loaded = false;
            UpdateTitle();

            LoadRayzReplies(prevRayz);
            return true;
        }

        private bool LoadNextRayzDetails(FrameworkElement fe)
        {
            if (fe != null)
            {
                var trans = fe.GetHorizontalOffset().Transform;
                trans.Animate(trans.X, 0, TranslateTransform.XProperty, 0, 0, new SineEase());
            }

            RayzDeletedGrid.Visibility = Visibility.Collapsed;
            EnableAppBarButtons();

            var temp = GetActiveRayzList();

            if (temp == null || temp.Count == 0)
                return false;

            var nextRayzIndex = temp.IndexOf(App.ViewModel.SelectedRayz) + 1;

            if (nextRayzIndex >= temp.Count && temp.Count == 1)
                return false;

            if (App.ViewModel.SelectedRayz.UnreadRayzReplies > 0)
                App.ViewModel.MarkActiveListAsRead(App.ViewModel.SelectedRayz.RayzId);

            if (nextRayzIndex >= temp.Count)
                nextRayzIndex = 0;

            var nextRayz = temp[nextRayzIndex];

            if (String.IsNullOrEmpty(nextRayz.RayzId))
                LoadNextRayzDetails(null);

            App.ViewModel.ClearAttachmentsList();
            App.ViewModel.SelectedRayz = nextRayz;
            _loaded = false;
            UpdateTitle();

            LoadRayzReplies(nextRayz);
            return true;
        }

        private void UpdateTitle()
        {
            if (GetActiveRayzList() == null || GetActiveRayzList().Count == 0)
                NavigationService.GoBack();

            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    switch (List)
                    {
                        case "Live":
                            PageTitle.Text = (App.ViewModel.LiveRayz.IndexOf(App.ViewModel.SelectedRayz) + 1) + " OF " + App.ViewModel.LiveRayz.Count + " LIVE";
                            break;
                        case "Starred":
                            PageTitle.Text = (App.ViewModel.StarredRayz.IndexOf(App.ViewModel.SelectedRayz) + 1) + " OF " + App.ViewModel.StarredRayz.Count + " STARRED";
                            break;
                        case "My":
                            PageTitle.Text = (App.ViewModel.MyRayz.IndexOf(App.ViewModel.SelectedRayz) + 1) + " OF " + App.ViewModel.MyRayz.Count + " MY";
                            break;
                        case "Search":
                            PageTitle.Text = (App.ViewModel.SearchRayz.IndexOf(App.ViewModel.SelectedRayz) + 1) + " OF " + App.ViewModel.SearchRayz.Count + " SEARCH RESULTS";
                            break;
                        default:
                            return;
                    }
                });

            RayzContextMenu.DataContext = App.ViewModel.SelectedRayz;
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

        #region Page Transitions

        private void ContentPanel_OnManipulationDelta(object sender, ManipulationDeltaEventArgs e)
        {
            if (!_isActive)
            {
                // has the user dragged far enough?
                if (Math.Abs(e.CumulativeManipulation.Translation.X) < 2.0)
                    return;

                _isActive = true;

                // initialize the drag
                var fe = sender as FrameworkElement;
                fe.SetHorizontalOffset(0);
            }
            else
            {
                // handle the drag to offset the element
                var fe = sender as FrameworkElement;
                var offset = fe.GetHorizontalOffset().Value + e.DeltaManipulation.Translation.X;
                fe.SetHorizontalOffset(offset);
            }
        }

        private void ContentPanel_OnManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
            if (!_isActive)
                return;

            var fe = sender as FrameworkElement;

            if (fe != null && (Math.Abs(e.TotalManipulation.Translation.X) > fe.ActualWidth / 2 ||
                               Math.Abs(e.FinalVelocities.LinearVelocity.X) > 2000.0))
            {
                // Right to Left [<--]
                if (e.TotalManipulation.Translation.X < 0.0)
                {
                    var res = LoadNextRayzDetails(fe);

                    if (res)
                        BounceInRight(fe);
                    else
                        BounceBackAction(fe);
                }
                // Left to Right [-->]
                else
                {
                    var res = LoadPrevRayzDetails(fe);

                    if (res)
                        BounceInLeft(fe);
                    else
                        BounceBackAction(fe);
                }
            }
            else
                BounceBackAction(fe);

            _isActive = false;
        }

        private static void BounceBackAction(FrameworkElement fe)
        {
            var trans = fe.GetHorizontalOffset().Transform;

            trans.Animate(trans.X, 0, TranslateTransform.XProperty, 300, 0, new SineEase());
        }

        private static void BounceInRight(FrameworkElement fe)
        {
            var trans = fe.GetHorizontalOffset().Transform;

            trans.Animate(1000, 0, TranslateTransform.XProperty, 300, 0, new SineEase());
        }

        private static void BounceInLeft(FrameworkElement fe)
        {
            var trans = fe.GetHorizontalOffset().Transform;

            trans.Animate(-1000, 0, TranslateTransform.XProperty, 300, 0, new SineEase());
        }

        #endregion

        #region Context Menu Commands

        #region Rayz Commands
        private void ReRayzItemClick(object sender, RoutedEventArgs e)
        {
            App.ViewModel.ReRayz(App.ViewModel.SelectedRayz);
        }

        private void LiveStarItemClick(object sender, RoutedEventArgs e)
        {
            App.ViewModel.CreateStarredRayz(App.ViewModel.SelectedRayz);
        }

        private void LiveHideItemClick(object sender, RoutedEventArgs e)
        {
            //if (App.ViewModel.SelectedRayz.IsMy)
            //    App.ViewModel.DeleteMyRayz(App.ViewModel.SelectedRayz);
            //else
            //    App.ViewModel.HideLiveRayz(App.ViewModel.SelectedRayz);
            App.ViewModel.RemoveRayz(App.ViewModel.SelectedRayz);
        }

        private void LiveReportItemClick(object sender, RoutedEventArgs e)
        {
            App.ViewModel.ReportLiveRayz(App.ViewModel.SelectedRayz);
        }

        private void CopyToClipboardItemClick(object sender, RoutedEventArgs e)
        {
            App.ViewModel.CopyRayzToClipboard(App.ViewModel.SelectedRayz);
        }
        #endregion

        #region Rayz Replies Commands
        private void ContextMenu_Unloaded(object sender, RoutedEventArgs e)
        {
            var conmen = (sender as ContextMenu);
            if (conmen != null)
                conmen.ClearValue(DataContextProperty);
        }

        private void PowerUpRayzReply(object sender, RoutedEventArgs e)
        {
            var selectedRayzReply = ((Button)sender).DataContext as RayzReply;
            if (selectedRayzReply == null)
                return;

            App.ViewModel.PowerUp(selectedRayzReply);
        }

        private void PowerDownRayzReply(object sender, RoutedEventArgs e)
        {
            var selectedRayzReply = ((Button)sender).DataContext as RayzReply;
            if (selectedRayzReply == null)
                return;

            App.ViewModel.PowerDown(selectedRayzReply);
        }

        private void ReportRayzReply(object sender, RoutedEventArgs e)
        {
            var selectedRayzReply = ((MenuItem)sender).DataContext as RayzReply;
            if (selectedRayzReply == null)
                return;

            App.ViewModel.ReportRayzReply(selectedRayzReply);
        }

        private void DeleteRayzReply(object sender, RoutedEventArgs e)
        {
            var selectedRayzReply = ((MenuItem)sender).DataContext as RayzReply;
            if (selectedRayzReply == null)
                return;

            App.ViewModel.DeleteRayzReply(selectedRayzReply);
        }

        private void CopyToClipboard(object sender, RoutedEventArgs e)
        {
            var selectedRayzReply = ((MenuItem)sender).DataContext as RayzReply;
            if (selectedRayzReply == null)
                return;

            App.ViewModel.CopyRayzReplyToClipboard(selectedRayzReply);
        }

        #endregion

        #endregion

        #region General Helper Classes

        private void EnableAppBarButtons()
        {
            ReplyTextBox.Visibility = Visibility.Visible;

            foreach (var button in ApplicationBar.Buttons)
                ((ApplicationBarIconButton)button).IsEnabled = true;
        }

        private void DisalbeAppBarButtons()
        {
            ReplyTextBox.Visibility = Visibility.Collapsed;

            foreach (var button in ApplicationBar.Buttons)
                ((ApplicationBarIconButton)button).IsEnabled = false;
        }

        #endregion

        #region Rayz Actions

        // Pull to refresh
        //async void pd_Compression(object sender, CompressionEventArgs e)
        //{
        //    if (e.Type != CompressionType.Bottom)
        //        return;

        //    if (!(await App.NetMngr.InternetAvailableAsync(false)))
        //        return;

        //    await LoadAnswers(false);
        //}

        /// <summary>
        /// If the active Rayz has attachments opens the attachments page in view mode to show them.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Rayz_Tap(object sender, GestureEventArgs e)
        {
            if (!App.ViewModel.SelectedRayz.HasAttachments)
                return;

            Attachments.Attachments.RayzSelectedItem = App.ViewModel.SelectedRayz;
            NavigationService.Navigate(new Uri("/Pages/Attachments/Attachments.xaml", UriKind.Relative));
        }

        #endregion

        #region Rayz Reply List Actions

        /// <summary>
        /// Called when a Rayz Reply from the List is tapped.
        /// Resends a Rayz Reply if it failed,
        /// Opens the attachments for that Rayz Reply if they exists
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void RayzRepliesListTap(object sender, GestureEventArgs e)
        {
            var item = ((FrameworkElement)sender).DataContext as RayzReply;

            // Something went wrong with the item
            if (item == null || item.RayzReplyDate == 0)
                return;

            // The Rayz has failed so try to resend it
            if (item.RayzReplyDate == 1)
            {
                App.ViewModel.RetrySendingRayzReply(item);
            }

            if (!item.IsRead)
                App.ViewModel.MarkRayzReplyAsRead(item);

            if (item.HasAttachment)
            {
                Attachments.Attachments.RayzReplySelectedItem = item;
                NavigationService.Navigate(new Uri("/Pages/Attachments/Attachments.xaml", UriKind.Relative));
            }
        }

        #endregion

        #region ApplicationBar Handlers

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
        /// Opens the Settings page.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Settings_Click(object sender, EventArgs e)
        {
            NavigationService.Navigate(new Uri("/Pages/Settings.xaml", UriKind.Relative));
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
        /// Refreshes the rayz replies
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void RefreshRayzReplies_OnClick(object sender, EventArgs e)
        {
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            await LoadAnswers(false, App.ViewModel.SelectedRayz);
        }
        #endregion

        #region Page Overrides

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            NoRepliesGrid.Visibility = App.ViewModel.IsActiveRayzRepliesListEmpty() ? Visibility.Visible : Visibility.Collapsed;
        }

        protected override void OnBackKeyPress(CancelEventArgs e)
        {
            if (RayzContextMenu.IsOpen)
            {
                RayzContextMenu.IsOpen = false; // Close menu
                e.Cancel = true; // Cancel Navigation
                return;
            }

            if (SystemTray.ProgressIndicator != null && SystemTray.ProgressIndicator.IsVisible)
                SystemTray.ProgressIndicator.IsVisible = false;

            base.OnBackKeyPress(e);
        }

        protected override void OnRemovedFromJournal(JournalEntryRemovedEventArgs e)
        {
            App.ViewModel.NotifyRemovingItem -= ViewModel_NotifyRemovingItem;
            App.ViewModel.NotifyRemovingItemCompleted -= ViewModel_NotifyRemovingItemCompleted;
        }

        #endregion
    }
}