using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Data.Linq;
using System.IO;
using System.IO.IsolatedStorage;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media.Imaging;
using CodeTitans.JSon;
using Rayzit.Pages;
using Rayzit.Pages.Attachments;
using Rayzit.Resources.HelperClasses.Encryption;
using Rayzit.Resources.HelperClasses.FlipTile;
using Rayzit.Resources.HelperClasses.SoundEffects;
using RayzitServiceClient.HelperClasses;

namespace Rayzit.ViewModels
{
    public class MainViewModel : INotifyPropertyChanged, INotifyPropertyChanging
    {
        // MUTEX
        private static readonly Mutex Mut = new Mutex();

        // Callback to update UI
        public event EventHandler<bool> LoadAttachmentsCompleted;
        public event EventHandler<bool> NotifyRemovingItem;
        public event EventHandler<bool> NotifyRemovingItemCompleted;

        // LINQ to SQL data context for the local database. 
        private readonly RayzDataContext _rayzitDb;

        private bool _possibleSync;

        // Constructor
        public MainViewModel(string rayzitDbConnectionString)
        {
            // Connect to local database
            _rayzitDb = new RayzDataContext(rayzitDbConnectionString);

            UpdateLiveSettings();
        }

        #region API Response Checkers

        public bool CheckResponseError(IJSonObject e, bool silentMode)
        {
            if (e == null)
                return true;

            var value = e["status"].StringValue;

            // Error occured - unknown
            if (value == null)
                return true;

            // Error occured - known
            if (!value.Equals("success"))
            {
                var stringValue = e["message"].StringValue;

                // Error occured - unknown
                if (stringValue == null)
                    return true;

                if (silentMode)
                    return true;

                // Error occured - known
                if (stringValue.Equals("Insufficient power."))
                {
                    App.Pb.UpdatePower();
                    Deployment.Current.Dispatcher.BeginInvoke(
                        () => MessageBox.Show(e["message"].StringValue, (string)Application.Current.Resources["GeneralErrorTitle"], MessageBoxButton.OK));
                }
                else if (stringValue.Equals("You are already following this Rayz."))
                    return false;
                else if (stringValue.Equals("Cannot update user's power."))
                    return false;
                else
                    Deployment.Current.Dispatcher.BeginInvoke(
                        () => MessageBox.Show(e["message"].StringValue, (string)Application.Current.Resources["GeneralErrorTitle"], MessageBoxButton.OK));

                return true;
            }

            return false;
        }

        #endregion

        #region Application Settings Related

        public async void UpdateLiveSettings()
        {
            var settings = await App.Rsc.GetLiveSettings();

            if (CheckResponseError(settings, true))
                return;

            if (settings.Contains("t"))
                App.Settings.LiveTimeSetting = settings["t"].Int64Value;

            if (settings.Contains("q"))
                App.Settings.LiveQSetting = settings["q"].Int32Value;
        }

        #endregion

        #region Rayz View Model Methods & Declarations

        #region Declarations

        /// <summary>
        /// Keeps All the Rayz from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<Rayz> _allRayz;
        public ObservableCollection<Rayz> AllRayz
        {
            get { return _allRayz; }
            set
            {
                NotifyPropertyChanging("AllRayz");
                _allRayz = value;
                NotifyPropertyChanged("AllRayz");
            }
        }

        /// <summary>
        /// Keeps only the Live Rayz from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<Rayz> _liveRayz;
        public ObservableCollection<Rayz> LiveRayz
        {
            get { return _liveRayz; }
            set
            {
                NotifyPropertyChanging("LiveRayz");
                _liveRayz = value;
                NotifyPropertyChanged("LiveRayz");
            }
        }

        /// <summary>
        /// Keeps only the Starred Rayz from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<Rayz> _starredRayz;
        public ObservableCollection<Rayz> StarredRayz
        {
            get { return _starredRayz; }
            set
            {
                NotifyPropertyChanging("StarredRayz");
                _starredRayz = value;
                NotifyPropertyChanged("StarredRayz");
            }
        }

        /// <summary>
        /// Keeps only My Rayz from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<Rayz> _myRayz;
        public ObservableCollection<Rayz> MyRayz
        {
            get { return _myRayz; }
            set
            {
                NotifyPropertyChanging("MyRayz");
                _myRayz = value;
                NotifyPropertyChanged("MyRayz");
            }
        }

        /// <summary>
        /// Keeps only My Rayz from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<Rayz> _searchRayz;
        public ObservableCollection<Rayz> SearchRayz
        {
            get { return _searchRayz; }
            set
            {
                NotifyPropertyChanging("SearchRayz");
                _searchRayz = value;
                NotifyPropertyChanged("SearchRayz");
            }
        }

        private Rayz _selectedRayz;
        public Rayz SelectedRayz
        {
            get { return _selectedRayz; }
            set
            {
                NotifyPropertyChanging("SelectedRayz");
                _selectedRayz = value;
                NotifyPropertyChanged("SelectedRayz");
            }
        }

        /// <summary>
        /// Private Lists to make the changes on. Not UI Related.
        /// First update these structures and then update the UI related structures.
        /// Overcomes the Life Cycle Exception!
        /// </summary>
        private ObservableCollection<Rayz> _liveR;
        private ObservableCollection<Rayz> _starredR;
        private ObservableCollection<Rayz> _myR;

        #endregion

        #region Methods

        #region Helper Methods

        /// <summary>
        /// Parses the JSON Object of a Rayz and generates the Rayz object
        /// </summary>
        /// <param name="o"> The JSON Object </param>
        /// <returns></returns>
        private static Rayz GenerateIncomingRayz(IJSonObject o)
        {
            String rayzId = String.Empty, rayzMessage = String.Empty, attachments = String.Empty, attachMd5 = String.Empty;
            Int64 rayzDate = 0;
            Int64 reportCount = 0;
            Int64 followersCount = 0;
            Int64 reRayzCount = 0;
            Int64 maxDistance = 0;
            var hasAttach = false;

            // Extract Rayz details
            foreach (var objItem in o.ObjectItems)
            {
                switch (objItem.Key)
                {
                    case "rayzId":
                        rayzId = objItem.Value.ToString();
                        break;
                    case "rayz_message":
                        rayzMessage = objItem.Value.ToString();
                        break;
                    case "timestamp":
                        rayzDate = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "maxDistance":
                        try
                        {
                            maxDistance = Convert.ToInt64(objItem.Value.ToString());
                        }
                        catch (Exception)
                        {
                            maxDistance = 0;
                        }
                        break;
                    case "report":
                        reportCount = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "follow":
                        followersCount = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "rerayz":
                        reRayzCount = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "attachments":
                        foreach (var attachmentObject in objItem.Value.ObjectItems)
                        {
                            switch (attachmentObject.Key)
                            {
                                case "images":
                                case "audio":
                                case "videos":
                                    foreach (var x in attachmentObject.Value.ArrayItems.SelectMany(item => item.ObjectItems))
                                        switch (x.Key)
                                        {
                                            case "url":
                                                attachments += x.Value + ";";
                                                break;
                                            case "md5":
                                                attachMd5 += x.Value + ";";
                                                break;
                                        }
                                    break;
                            }
                        }
                        break;
                }
            }

            // Check if the rayz has attachments
            if (attachments != String.Empty)
                hasAttach = true;

            // Check if the rayz is "mine"
            var isMy = rayzId.Split('_').FirstOrDefault(s => s.Equals(App.MDeviceUniqueId)) != null;

            var newRayz = new Rayz
            {
                RayzId = rayzId,
                RayzMessage = rayzMessage,
                RayzDate = rayzDate,
                ReportCount = reportCount,
                FollowersCount = followersCount,
                RerayzCount = reRayzCount,
                TotalRayzReplies = 0,
                UnreadRayzReplies = 0,
                MaxDistance = maxDistance,
                IsHidden = false,
                HasBeenDeleted = false,
                HasAttachments = hasAttach,
                Attachments = attachments,
                AttachMd5 = attachMd5,
                IsMy = isMy,
                IsLive = true
            };

            return newRayz;
        }

        /// <summary>
        /// Copies the rayz message to Clipboard
        /// </summary>
        /// <param name="rayz"> The Rayz object to be copied </param>
        public void CopyRayzToClipboard(Rayz rayz)
        {
            Clipboard.SetText(rayz.RayzMessage);
        }

        /// <summary>
        /// Copies the rayz reply message to Clipboard
        /// </summary>
        /// <param name="rayzReply"> The Rayz Reply object to be copied </param>
        public void CopyRayzReplyToClipboard(RayzReply rayzReply)
        {
            Clipboard.SetText(rayzReply.RayzReplyMessage);
        }

        public async Task<bool> UpdateUi(bool silentMode)
        {
            UpdateUserPower();

            if (!(await UpdateLiveFeed(silentMode)))
            {
                RefreshAllLists();
                return false;
            }

            if (!(await GetRayzUpdates(silentMode)))
            {
                RefreshAllLists();
                return false;
            }

            RefreshAllLists();

            return true;
        }

        public async Task<bool> UpdateLive(bool silentMode)
        {
            UpdateUserPower();

            if (!(await UpdateLiveFeed(silentMode)))
                return false;

            return true;
        }

        #endregion

        #region Create New Rayz

        /// <summary>
        /// Creates and Adds a new Rayz to the live and my rayz lists.
        /// Status is set to sending by setting the date to 0.
        /// Requires internet connection, valid geolocation info and enough rayz power.
        /// UI is updated Automatically (binding).
        /// </summary>
        /// <param name="rayzMessage"> The rayz message to be sent</param>
        /// <param name="distanceSetting">The rayz distance setting</param>
        public async void AddLiveRayz(string rayzMessage, int distanceSetting)
        {
            var hasAttachment = false;
            var attachmentsLocations = "";
            var x = new ObservableCollection<RayzItAttachment>();

            // Fill the WindowsPhone API Client Attachments
            if (Attachments != null && Attachments.Count > 0)
            {
                hasAttachment = true;
                attachmentsLocations = Attachments.Aggregate(attachmentsLocations, (current, a) => current + (a.FileName + ";"));

                foreach (var attach in Attachments.ToList().Select(a => a.ByteArray != null ? new RayzItAttachment(a.ByteArray, a.Type) : null))
                    x.Add(attach);
            }

            App.ViewModel.ClearAttachmentsList();

            var distance = "0";

            switch (distanceSetting)
            {
                case 1:
                    distance = "500";
                    break;
                case 2:
                    distance = "5000";
                    break;
                case 3:
                    distance = "50000";
                    break;
                case 4:
                    distance = "500000";
                    break;
                case 5:
                    distance = "5000000";
                    break;
            }

            // Create the new rayz to be send
            var newRayz = new Rayz { RayzId = String.Empty, RayzMessage = rayzMessage, RayzDate = 0, TotalRayzReplies = 0, UnreadRayzReplies = 0, IsLive = true, IsMy = true, HasAttachments = hasAttachment, HasBeenDeleted = false, ReportCount = 0, FollowersCount = 0, RerayzCount = 0, MaxDistance = Convert.ToInt64(distance), IsHidden = false, Attachments = attachmentsLocations, AttachMd5 = String.Empty };

            // Insert it into the database
            _rayzitDb.RayzItems.InsertOnSubmit(newRayz);
            SaveDatabaseChanges();

            // Update the lists
            _liveR.Insert(0, newRayz);
            _myR.Insert(0, newRayz);
            AllRayz.Insert(0, newRayz);

            // Update UI
            RefreshLiveRayzList();
            RefreshMyRayzList();

            // Check for Internet and Geolocation Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayz.RayzDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!(App.LocFinder.GeolocationAvailable(false)))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayz.RayzDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!App.EnoughRayzPower(2))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayz.RayzDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            // Call WindowsPhone API Client method to send the rayz 
            var e = await App.Rsc.CreateNewRayz(App.LocFinder.Info.Latitude, App.LocFinder.Info.Longitude, App.LocFinder.Info.Accuracy, distance, rayzMessage, x);

            if (CheckResponseError(e, false))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayz.RayzDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (e.Contains("power"))
            {
                App.Settings.PowerValueSetting = int.Parse(e["power"].StringValue);
                App.Pb.UpdatePower();
            }

            if (e.Contains("timestamp") && e.Contains("rayzId"))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                    {
                        newRayz.RayzDate = Convert.ToInt64(e["timestamp"].StringValue);
                        newRayz.RayzId = e["rayzId"].StringValue;
                        SaveDatabaseChanges();
                    });
            }
            else
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                    {
                        newRayz.RayzDate = 1;
                        SaveDatabaseChanges();
                    });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (App.Settings.SoundsToggleSwitchSetting)
                SoundEffects.PlayMessageSuccessSound();

            if (newRayz.IsMy)
                RefreshMyRayzList();

            RefreshLiveRayzList();
        }

        /// <summary>
        /// Retries to send a rayz.
        /// Rayz status is set back to sending by setting the date to 0.
        /// Requires internet connection, valid geolocation info and enough rayz power.
        /// UI is updated Automatically (binding).
        /// </summary>
        /// <param name="rayz"> The rayz to be sent </param>
        public async void RetrySendingRayz(Rayz rayz)
        {
            // Update the status message to sending
            rayz.RayzDate = 0;

            // Load the attachments of the selected rayz and fill the lists
            await App.ViewModel.LoadAttachmentsFromRayz(rayz);
            var rayzAttachments = new ObservableCollection<RayzItAttachment>();
            if (Attachments != null && Attachments.Count > 0)
            {
                foreach (var attach in Attachments.ToList().Select(a => a.ByteArray != null ? new RayzItAttachment(a.ByteArray, a.Type) : null))
                    rayzAttachments.Add(attach);
            }

            App.ViewModel.ClearAttachmentsList();

            // Check for Internet and Geolocation Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
            {
                rayz.RayzDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!(App.LocFinder.GeolocationAvailable(false)))
            {
                rayz.RayzDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!App.EnoughRayzPower(2))
            {
                rayz.RayzDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            var distance = "0";

            switch (App.Settings.ListBoxSetting)
            {
                case 1:
                    distance = "500";
                    break;
                case 2:
                    distance = "5000";
                    break;
                case 3:
                    distance = "50000";
                    break;
                case 4:
                    distance = "500000";
                    break;
                case 5:
                    distance = "5000000";
                    break;
            }

            // Call WindowsPhone API Client method to send the rayz 
            var e = await App.Rsc.CreateNewRayz(App.LocFinder.Info.Latitude, App.LocFinder.Info.Longitude, App.LocFinder.Info.Accuracy, distance, rayz.RayzMessage, rayzAttachments);

            if (CheckResponseError(e, false))
            {
                rayz.RayzDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (e.Contains("power"))
            {
                App.Settings.PowerValueSetting = int.Parse(e["power"].StringValue);
                App.Pb.UpdatePower();
            }

            if (e.Contains("timestamp") && e.Contains("rayzId"))
            {
                rayz.RayzDate = Convert.ToInt64(e["timestamp"].StringValue);
                rayz.RayzId = e["rayzId"].StringValue;
                SaveDatabaseChanges();
            }
            else
            {
                rayz.RayzDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (App.Settings.SoundsToggleSwitchSetting)
                SoundEffects.PlayMessageSuccessSound();

            if (rayz.IsMy)
                RefreshMyRayzList();

            RefreshLiveRayzList();
        }

        /// <summary>
        /// Creates a new Rayz from an incomming push notification message.
        /// The new Rayz is then added to the live feed.
        /// </summary>
        /// <param name="jSonReply"> JSON Object of the Rayz to be created </param>
        public void CreateNewRayzFromIncomming(IJSonObject jSonReply)
        {
            var newRayz = GenerateIncomingRayz(jSonReply);

            // Search all Rayz to see if its already added
            var ra = AllRayz.ToList().FirstOrDefault(r => r.RayzId != null && r.RayzId.Equals(newRayz.RayzId));

            // If the Rayz exists update its corresponding fileds
            if (ra != null)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                    {
                        ra.RerayzCount = newRayz.RerayzCount;
                        ra.FollowersCount = newRayz.FollowersCount;
                        ra.ReportCount = newRayz.ReportCount;
                        SaveDatabaseChanges();
                    });

                return;
            }

            AllRayz.Insert(0, newRayz);

            // If not add it
            // Update the database
            _rayzitDb.RayzItems.InsertOnSubmit(newRayz);
            SaveDatabaseChanges();

            // Update the lists
            if (newRayz.IsMy)
            {
                _myR.Insert(0, newRayz);
                RefreshMyRayzList();
            }

            _liveR.Insert(0, newRayz);
            RefreshLiveRayzList();
        }

        #endregion

        #region ReRayz

        /// <summary>
        /// ReRayz a current Rayz.
        /// Requires internet connection, valid geolocation info and enough rayz power.
        /// </summary>
        /// <param name="r"> The Rayz object to be rerayzed </param>
        public async void ReRayz(Rayz r)
        {
            if (r == null)
                return;

            if (r.RayzDate == 0 || r.RayzDate == 1)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show((string)Application.Current.Resources["MessageNotSendMessage"],
                                                                                (string)Application.Current.Resources["MessageNotSendTitle"], MessageBoxButton.OK));
                return;
            }

            if (String.IsNullOrEmpty(r.RayzId))
                return;

            // Check for Internet and Geolocation Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            if (!(App.LocFinder.GeolocationAvailable(false)))
                return;

            if (!App.EnoughRayzPower(2))
                return;

            var distance = "0";

            switch (App.Settings.ListBoxSetting)
            {
                case 1:
                    distance = "500";
                    break;
                case 2:
                    distance = "5000";
                    break;
                case 3:
                    distance = "50000";
                    break;
                case 4:
                    distance = "500000";
                    break;
                case 5:
                    distance = "5000000";
                    break;
            }

            var e = await App.Rsc.ReRayz(r.RayzId, App.LocFinder.Info.Latitude, App.LocFinder.Info.Longitude, App.LocFinder.Info.Accuracy, distance);

            if (CheckResponseError(e, false))
                return;

            r.RerayzCount = Convert.ToInt64(e["rerayz"].StringValue);

            App.Settings.PowerValueSetting = int.Parse(e["power"].StringValue);
            App.Pb.UpdatePower();
        }

        #endregion

        #region Star/Unstar Rayz

        /// <summary>
        /// Stars the given Rayz if it's not starred and unstars it if it's starred.
        /// Requires internet connection. 
        /// </summary>
        /// <param name="rayz"></param>
        public async void CreateStarredRayz(Rayz rayz)
        {
            if (rayz == null)
                return;

            if (rayz.RayzDate == 0 || rayz.RayzDate == 1)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show((string)Application.Current.Resources["MessageNotSendMessage"],
                                                                                (string)Application.Current.Resources["MessageNotSendTitle"], MessageBoxButton.OK));
                return;
            }

            if (String.IsNullOrEmpty(rayz.RayzId))
                return;

            if (rayz.IsStarred)
            {
                _starredR.Remove(rayz);

                rayz.IsStarred = false;
                rayz.FollowersCount--;
                RefreshStarredRayzList();

                // If the Rayz is not in my rayz and live lists completly remove it
                if (!rayz.IsMy && !rayz.IsLive)
                {
                    // Remove all its replies
                    foreach (var reply in AllRayzReplies.ToList())
                    {
                        if (!reply.RayzId.Equals(rayz.RayzId))
                            continue;

                        AllRayzReplies.Remove(reply);
                        _rayzitDb.RayzRepliesItems.DeleteOnSubmit(reply);
                    }

                    AllRayz.Remove(rayz);
                    _rayzitDb.RayzItems.DeleteOnSubmit(rayz);
                }

                // Check for Internet Availability
                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    return;

                var e = await App.Rsc.DeleteStarredRayz(rayz.RayzId);

                if (CheckResponseError(e, true))
                    return;

                if (e.Contains("follow"))
                    rayz.FollowersCount = Convert.ToInt64(e["follow"].StringValue);
            }
            else
            {
                rayz.IsStarred = true;
                rayz.FollowersCount++;
                _starredR.Insert(0, rayz);
                RefreshStarredRayzList();

                // Check for Internet Availability
                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    return;

                var e = await App.Rsc.StarRayz(rayz.RayzId);

                if (CheckResponseError(e, true))
                    return;

                if (e.Contains("follow"))
                    rayz.FollowersCount = Convert.ToInt64(e["follow"].StringValue);
            }
        }

        /// <summary>
        /// Unstars the given Rayz.
        /// Requires internet connection. 
        /// </summary>
        /// <param name="rayz"></param>
        public async void UnstarStarredRayz(Rayz rayz)
        {
            _starredR.Remove(rayz);

            rayz.IsStarred = false;
            rayz.FollowersCount--;
            RefreshStarredRayzList();

            // If the Rayz is not in my rayz and live lists completly remove it
            if (!rayz.IsMy && !rayz.IsLive)
            {
                // Remove all its replies
                foreach (var reply in AllRayzReplies.ToList())
                {
                    if (!reply.RayzId.Equals(rayz.RayzId))
                        continue;

                    AllRayzReplies.Remove(reply);
                    _rayzitDb.RayzRepliesItems.DeleteOnSubmit(reply);
                }

                AllRayz.Remove(rayz);
                _rayzitDb.RayzItems.DeleteOnSubmit(rayz);
            }

            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(true)))
                return;

            var e = await App.Rsc.DeleteStarredRayz(rayz.RayzId);

            if (CheckResponseError(e, true))
                return;

            if (e.Contains("follow"))
                rayz.FollowersCount = Convert.ToInt64(e["follow"].StringValue);
        }

        /// <summary>
        /// Unstars the given Rayz.
        /// Requires internet connection. 
        /// </summary>
        /// <param name="rayzList"></param>
        public async void MultiUnstarStarredRayz(ObservableCollection<Rayz> rayzList)
        {
            foreach (var rayz in rayzList)
            {
                _starredR.Remove(rayz);
                StarredRayz.Remove(rayz);

                rayz.IsStarred = false;
                rayz.FollowersCount--;

                // If the Rayz is not in my rayz and live lists completly remove it
                if (!rayz.IsMy && !rayz.IsLive)
                {
                    // Remove all its replies
                    foreach (var reply in AllRayzReplies.ToList())
                    {
                        if (!reply.RayzId.Equals(rayz.RayzId))
                            continue;

                        AllRayzReplies.Remove(reply);
                        _rayzitDb.RayzRepliesItems.DeleteOnSubmit(reply);
                    }

                    AllRayz.Remove(rayz);
                    _rayzitDb.RayzItems.DeleteOnSubmit(rayz);
                }
            }

            RefreshStarredRayzList();

            foreach (var rayz in rayzList)
            {
                // Check for Internet Availability
                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    continue;

                var e = await App.Rsc.DeleteStarredRayz(rayz.RayzId);

                if (CheckResponseError(e, true))
                    continue;

                if (e.Contains("follow"))
                    rayz.FollowersCount = Convert.ToInt64(e["follow"].StringValue);
            }
        }

        #endregion

        #region Remove Rayz

        /// <summary>
        /// Marks a Rayz as hidden and removes it from the livefeed list
        /// If the rayz is deleted from the database and API resends it it will be shown again!
        /// </summary>
        /// <param name="rayz"> The Rayz object to hide </param>
        public void HideLiveRayz(Rayz rayz)
        {
            if (rayz == null)
                return;

            if (NotifyRemovingItem != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItem(this, true));

            _liveR.Remove(rayz);

            rayz.IsHidden = true;
            rayz.IsLive = false;

            RefreshLiveRayzList();

            if (NotifyRemovingItemCompleted != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItemCompleted(this, true));
        }

        /// <summary>
        /// Removes a rayz (hides actually)
        /// </summary>
        /// <param name="rayz"> The Rayz object to be deleted </param>
        public void RemoveRayz(Rayz rayz)
        {
            if (NotifyRemovingItem != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItem(this, true));

            _myR.Remove(rayz);
            _starredR.Remove(rayz);
            _liveR.Remove(rayz);

            rayz.IsStarred = false;
            rayz.IsMy = false;
            rayz.IsLive = false;
            rayz.IsHidden = true;

            RefreshAllLists();

            if (NotifyRemovingItemCompleted != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItemCompleted(this, true));

            foreach (var rr in AllRayzReplies.ToList().Where(rre => rre.RayzId.Equals(rayz.RayzId)))
            {
                AllRayzReplies.Remove(rr);
                _rayzitDb.RayzRepliesItems.DeleteOnSubmit(rr);
            }
        }

        /// <summary>
        /// Makes a request to Delete a Rayz
        /// </summary>
        /// <param name="rayz"> The Rayz object to be deleted </param>
        public async void DeleteMyRayz(Rayz rayz)
        {
            if (NotifyRemovingItem != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItem(this, true));

            _myR.Remove(rayz);
            _starredR.Remove(rayz);
            _liveR.Remove(rayz);

            rayz.IsStarred = false;
            rayz.IsMy = false;
            rayz.IsLive = false;
            rayz.IsHidden = true;

            RefreshAllLists();

            if (NotifyRemovingItemCompleted != null)
                Deployment.Current.Dispatcher.BeginInvoke(() => NotifyRemovingItemCompleted(this, true));

            foreach (var rr in AllRayzReplies.ToList().Where(rre => rre.RayzId.Equals(rayz.RayzId)))
            {
                AllRayzReplies.Remove(rr);
                _rayzitDb.RayzRepliesItems.DeleteOnSubmit(rr);
            }

            // No need to notify the server if the rayz has not been send
            if (rayz.RayzDate != 0 && rayz.RayzDate != 1)
            {
                // Check for Internet Availability
                if (!(await App.NetMngr.InternetAvailableAsync(true)))
                    return;

                await App.Rsc.DeleteRayz(rayz.RayzId);
            }
        }

        /// <summary>
        /// Makes a request to Delete a list of Rayz
        /// </summary>
        /// <param name="rayzList"> The Rays list object to be deleted </param>
        public async void MultiDeleteMyRayz(ObservableCollection<Rayz> rayzList)
        {
            foreach (var rayz in rayzList)
            {
                _myR.Remove(rayz);
                MyRayz.Remove(rayz);
                _starredR.Remove(rayz);
                StarredRayz.Remove(rayz);
                _liveR.Remove(rayz);
                LiveRayz.Remove(rayz);

                rayz.IsStarred = false;
                rayz.IsMy = false;
                rayz.IsLive = false;
                rayz.IsHidden = true;

                Rayz rayz1 = rayz;
                foreach (var rr in AllRayzReplies.ToList().Where(rre => rre.RayzId.Equals(rayz1.RayzId)))
                {
                    AllRayzReplies.Remove(rr);
                    _rayzitDb.RayzRepliesItems.DeleteOnSubmit(rr);
                }
            }

            RefreshAllLists();

            foreach (var rayz in rayzList)
            {
                // No need to notify the server if the rayz has not been send
                if (rayz.RayzDate != 0 && rayz.RayzDate != 1)
                {
                    // Check for Internet Availability
                    if (!(await App.NetMngr.InternetAvailableAsync(true)))
                        continue;

                    await App.Rsc.DeleteRayz(rayz.RayzId);
                }
            }
        }

        #endregion

        #region Report Rayz

        /// <summary>
        /// Makes a request to report a Rayz
        /// </summary>
        /// <param name="rayz"> The Rayz object to be reported </param>
        public async void ReportLiveRayz(Rayz rayz)
        {
            // Check for Internet and Geolocation Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            var e = await App.Rsc.ReportRayz(rayz.RayzId);

            if (CheckResponseError(e, false))
                return;

            rayz.ReportCount = Convert.ToInt64(e["report"].StringValue);
            SaveDatabaseChanges();
        }

        #endregion

        /// <summary>
        /// Updates the User's Live Feed.
        /// If a message already exists it updates it, else it adds it to the database
        /// </summary>
        public async Task<bool> UpdateLiveFeed(bool silentMode)
        {
            var e = await App.Rsc.GetLiveFeed();

            if (CheckResponseError(e, silentMode))
                return false;

            foreach (var obj in e.ObjectItems)
            {
                if (!obj.Key.Equals("liveFeed"))
                    continue;

                // For each Rayz
                foreach (var o in obj.Value.ArrayItems)
                {
                    // Generate the Rayz
                    var newRayz = GenerateIncomingRayz(o);

                    // Add the constructed Rayz
                    // Search all Rayz to see if its already added
                    var ra = AllRayz.ToList().FirstOrDefault(r => r.RayzId != null && r.RayzId.Equals(newRayz.RayzId));

                    // If the Rayz exists update its corresponding fileds
                    if (ra != null)
                    {
                        ra.RerayzCount = newRayz.RerayzCount;
                        ra.FollowersCount = newRayz.FollowersCount;
                        ra.ReportCount = newRayz.ReportCount;
                        SaveDatabaseChanges();

                        continue;
                    }

                    AllRayz.Insert(0, newRayz);

                    // If not add it
                    // Update the database
                    _rayzitDb.RayzItems.InsertOnSubmit(newRayz);

                    // Update the lists
                    if (newRayz.IsMy)
                    {
                        _myR.Insert(0, newRayz);
                    }

                    _liveR.Insert(0, newRayz);
                }
            }

            SaveDatabaseChanges();

            RefreshAllLists();
            return true;
        }

        /// <summary>
        /// Used for when application is in background to keep the UI and the LiveTile updated
        /// </summary>
        public async void UpdateLiveFeedRandom()
        {
            Rayz rayz = null;
            var counter = 0;

            var e = await App.Rsc.GetLiveFeedRandom();

            if (CheckResponseError(e, true))
                return;

            // Extract Rayz details
            foreach (var objItem in e.ObjectItems)
            {
                switch (objItem.Key)
                {
                    case "counter":
                        counter = Convert.ToInt16(objItem.Value.ToString());
                        break;
                    case "liveFeed":
                        var x = objItem.Value.ArrayItems.FirstOrDefault();
                        if (x != null)
                            rayz = GenerateIncomingRayz(x);
                        break;
                }
            }

            if (rayz == null)
                return;

            // UPDATE LIVE TILE
            if (rayz.HasAttachments)
            {
                var locations = rayz.Attachments.Split(new[] { ';' }, StringSplitOptions.RemoveEmptyEntries);

                var imageLocation = locations.FirstOrDefault(loc => loc.Substring(loc.Length - 3).Equals(".jpg"));

                if (!String.IsNullOrEmpty(imageLocation))
                {
                    var image = await App.Rsc.RequestAttachment(imageLocation);
                    if (image == null)
                        return;
                    FlipTileManager.SetFlipTileImage(image);
                    FlipTileManager.UpdateFlipTile(rayz.RayzMessage, counter, true);
                }

                FlipTileManager.UpdateFlipTile(rayz.RayzMessage, counter, false);
            }
            else
                FlipTileManager.UpdateFlipTile(rayz.RayzMessage, counter, false);
        }

        /// <summary>
        /// Makes a request to the server for all the Rayz updates
        /// Called when the application is activated
        /// </summary>
        public async Task<bool> GetRayzUpdates(bool silentMode)
        {
            if (AllRayz == null)
                return false;

            var rayzIds = (from r in AllRayz.ToList() where !String.IsNullOrEmpty(r.RayzId) && (r.IsMy || r.IsStarred) select r.RayzId).ToList();

            var e = await App.Rsc.GetMultiCounter(rayzIds);

            if (CheckResponseError(e, silentMode))
                return false;

            var rayzId = String.Empty;
            Int64 reportCount = 0;
            Int64 followersCount = 0;
            Int64 reRayzCount = 0;
            var rayzReplies = 0;

            foreach (var obj in e.ObjectItems)
            {
                if (!obj.Key.Equals("rayzIds"))
                    continue;

                if (!obj.Value.ArrayItems.Any())
                    return false;

                // For each Rayz
                foreach (var o in obj.Value.ArrayItems)
                {
                    // Get the updates
                    // Extract Rayz details
                    foreach (var objItem in o.ObjectItems)
                    {
                        switch (objItem.Key)
                        {
                            case "rayzId":
                                rayzId = objItem.Value.ToString();
                                break;
                            case "report":
                                reportCount = Convert.ToInt64(objItem.Value.ToString());
                                break;
                            case "follow":
                                followersCount = Convert.ToInt64(objItem.Value.ToString());
                                break;
                            case "rerayz":
                                reRayzCount = Convert.ToInt64(objItem.Value.ToString());
                                break;
                            case "rayzReplies":
                                rayzReplies = Convert.ToInt16(objItem.Value.ToString());
                                break;
                        }
                    }

                    // Search the Rayz to be updated
                    var ra = AllRayz.ToList().FirstOrDefault(r => r.RayzId != null && r.RayzId.Equals(rayzId));

                    if (ra == null)
                        continue;

                    var localRayzReplies = (AllRayzReplies.Count(reply => reply.RayzId.Equals(rayzId) && reply.IsRead && reply.RayzReplyDate != 0 && reply.RayzReplyDate != 1));

                    var rrCount = rayzReplies - localRayzReplies - ra.UnreadRayzReplies;

                    if (rrCount < 0)
                        FlurryWP8SDK.Api.LogError("REPLY COUNT NEGATIVE", null);
                    else if (rrCount > 0)
                        ra.UnreadRayzReplies += rrCount;

                    ra.RerayzCount = reRayzCount;
                    ra.FollowersCount = followersCount;
                    ra.ReportCount = reportCount;
                }
            }

            SaveDatabaseChanges();

            return true;
        }

        public async void UpdateUserPower()
        {
            var e = await App.Rsc.GetPower();

            if (CheckResponseError(e, true))
                return;

            var stringValue = e["status"].StringValue;
            if (stringValue != null && stringValue.Equals("error"))
                return;

            var value = e["power"].StringValue;
            if (value != null)
            {
                var power = int.Parse(value);

                App.Settings.PowerValueSetting = power;
            }
            App.Pb.UpdatePower();
        }

        #endregion

        #endregion

        #region Rayz Reply View Model Methods & Declarations

        #region Declarations

        /// <summary>
        /// Keeps All the Rayz Replies from the Database
        /// UI Related
        /// </summary>
        private static ObservableCollection<RayzReply> _allRayzReplies;
        public ObservableCollection<RayzReply> AllRayzReplies
        {
            get { return _allRayzReplies; }
            set
            {
                NotifyPropertyChanging("AllRayzReplies");
                _allRayzReplies = value;
                NotifyPropertyChanged("AllRayzReplies");
            }
        }

        /// <summary>
        /// Keeps the Rayz Replies of a selected Rayz
        /// UI Related
        /// </summary>
        private static ObservableCollection<RayzReply> _activeRayzReplies;
        public ObservableCollection<RayzReply> ActiveRayzReplies
        {
            get { return _activeRayzReplies; }
            set
            {
                NotifyPropertyChanging("ActiveRayzReplies");
                _activeRayzReplies = value;
                NotifyPropertyChanged("ActiveRayzReplies");
            }
        }

        /// <summary>
        /// Private List to make the changes on. Not UI Related.
        /// First update these structures and then update the UI related structures.
        /// Overcomes the Life Cycle Exception!
        /// </summary>
        private ObservableCollection<RayzReply> _activeRr;

        #endregion

        #region Methods

        #region New Rayz Reply

        public async void AddRayzReply(string rayzReplyMessage)
        {
            var hasAttachment = false;
            var attachmentsLocations = String.Empty;

            var x = new ObservableCollection<RayzItAttachment>();

            if (Attachments != null && Attachments.Count > 0)
            {
                hasAttachment = true;
                attachmentsLocations = Attachments.Aggregate(attachmentsLocations, (current, a) => current + (a.FileName + ";"));

                foreach (var attach in Attachments.ToList().Select(a => a.ByteArray != null ? new RayzItAttachment(a.ByteArray, a.Type) : null))
                    x.Add(attach);
            }

            var newRayzReply = new RayzReply { RayzReplyMessage = rayzReplyMessage, RayzReplyDate = 0, RayzId = App.ViewModel.SelectedRayz.RayzId, IsRead = true, IsMy = true, UpVotes = 0, ReportCount = 0, HasAttachment = hasAttachment, Attachments = attachmentsLocations, AttachMd5 = String.Empty };

            _rayzitDb.RayzRepliesItems.InsertOnSubmit(newRayzReply);
            SaveDatabaseChanges();

            AllRayzReplies.Add(newRayzReply);
            _activeRr.Insert(0, newRayzReply);

            // Update UI
            ActiveRayzReplies = new ObservableCollection<RayzReply>(_activeRr);

            App.ViewModel.ClearAttachmentsList();

            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
            {

                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayzReply.RayzReplyDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!App.EnoughRayzPower(1))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayzReply.RayzReplyDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            var e = await App.Rsc.NewRayzReply(App.ViewModel.SelectedRayz.RayzId, rayzReplyMessage, x);

            // Sending Failed
            if (CheckResponseError(e, false))
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayzReply.RayzReplyDate = 1;
                    SaveDatabaseChanges();
                });

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    newRayzReply.RayzReplyDate = Convert.ToInt64(e["timestamp"].StringValue);
                    newRayzReply.RayzReplyId = e["rayzReplyId"].StringValue;
                    SaveDatabaseChanges();
                });

            if (App.Settings.SoundsToggleSwitchSetting)
                SoundEffects.PlayMessageSuccessSound();

            App.Settings.PowerValueSetting = int.Parse(e["power"].StringValue);
            App.Pb.UpdatePower();

            // Find the Rayz the reply attaches to
            var rayz = AllRayz.FirstOrDefault(r => r.RayzId.Equals(newRayzReply.RayzId));

            if (rayz == null)
                return;

            rayz.TotalRayzReplies++;

            RefreshStarredRayzList();
            RefreshMyRayzList();
        }

        public async void RetrySendingRayzReply(RayzReply rr)
        {
            // Update the status message to sending
            Deployment.Current.Dispatcher.BeginInvoke(() => rr.RayzReplyDate = 0);

            // Load the attachments of the selected rayz and fill the lists
            await App.ViewModel.LoadAttachmentsFromRayzReply(rr);

            var x = new ObservableCollection<RayzItAttachment>();

            if (Attachments != null && Attachments.Count > 0)
            {
                foreach (var attach in Attachments.ToList().Select(a => a.ByteArray != null ? new RayzItAttachment(a.ByteArray, a.Type) : null))
                    x.Add(attach);
            }

            App.ViewModel.ClearAttachmentsList();

            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
            {
                rr.RayzReplyDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (!App.EnoughRayzPower(1))
            {
                rr.RayzReplyDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            var e = await App.Rsc.NewRayzReply(App.ViewModel.SelectedRayz.RayzId, rr.RayzReplyMessage, x);

            // Sending Failed
            if (CheckResponseError(e, false))
            {
                rr.RayzReplyDate = 1;

                if (App.Settings.SoundsToggleSwitchSetting)
                    SoundEffects.PlayMessageFailedSound();

                return;
            }

            if (App.Settings.SoundsToggleSwitchSetting)
                SoundEffects.PlayMessageSuccessSound();

            App.Settings.PowerValueSetting = int.Parse(e["power"].StringValue);
            App.Pb.UpdatePower();

            // Find the Rayz the reply attaches to
            var rayz = AllRayz.FirstOrDefault(r => r.RayzId.Equals(rr.RayzId));

            if (rayz == null)
                return;

            rayz.TotalRayzReplies++;

            RefreshStarredRayzList();
            RefreshMyRayzList();

            rr.RayzReplyDate = Convert.ToInt64(e["timestamp"].StringValue);
            rr.RayzReplyId = e["rayzReplyId"].StringValue;
            SaveDatabaseChanges();
        }

        public async void CreateNewRayzReplyFromIncomming(IJSonObject jSonReply)
        {
            String rayzId = String.Empty, rayzReplyId = String.Empty, rayzReplyMessage = String.Empty, attachments = String.Empty, attachMd5 = String.Empty;
            Int64 rayzDate = 0;
            Int64 upVotes = 0;
            Int64 reportCount = 0;
            var hasAttach = false;

            foreach (var objItem in jSonReply.ObjectItems)
            {
                switch (objItem.Key)
                {
                    case "rayzId":
                        rayzId = objItem.Value.ToString();
                        break;
                    case "rayzReplyId":
                        rayzReplyId = objItem.Value.ToString();
                        break;
                    case "rayz_reply_message":
                        rayzReplyMessage = objItem.Value.ToString();
                        break;
                    case "timestamp":
                        rayzDate = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "upVotes":
                        upVotes = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "report":
                        reportCount = Convert.ToInt64(objItem.Value.ToString());
                        break;
                    case "attachments":
                        foreach (var attachmentObject in objItem.Value.ObjectItems)
                        {
                            switch (attachmentObject.Key)
                            {
                                case "images":
                                case "audio":
                                case "videos":
                                    foreach (var x in attachmentObject.Value.ArrayItems.SelectMany(item => item.ObjectItems))
                                        switch (x.Key)
                                        {
                                            case "url":
                                                attachments += x.Value + ";";
                                                break;
                                            case "md5":
                                                attachMd5 += x.Value + ";";
                                                break;
                                        }
                                    break;
                            }
                        }
                        break;
                }
            }

            if (!String.IsNullOrEmpty(attachments))
                hasAttach = true;

            // Find the Rayz the reply attaches to
            var rayz = AllRayz.ToList().FirstOrDefault(r => r.RayzId.Equals(rayzId));

            // If not found dont add
            if (rayz == null)
                return;

            // Search all Rayz Replies to see if its already added
            var rayzReply = AllRayzReplies.ToList().FirstOrDefault(rr => rr.RayzReplyId != null && rr.RayzReplyId.Equals(rayzReplyId));

            // If found update it
            if (rayzReply != null)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                    {
                        rayzReply.ReportCount = reportCount;
                        rayzReply.UpVotes = upVotes;
                    });
                return;
            }

            var isMy = rayzReplyId.Split('_').FirstOrDefault(s => s.Equals(App.MDeviceUniqueId)) != null;

            var isRead = App.ViewModel.SelectedRayz != null && App.ViewModel.SelectedRayz.Id != rayz.Id;

            var newRayzReply = new RayzReply
            {
                RayzId = rayzId,
                RayzReplyId = rayzReplyId,
                RayzReplyMessage = rayzReplyMessage,
                RayzReplyDate = rayzDate,
                IsRead = isRead,
                UpVotes = upVotes,
                ReportCount = reportCount,
                IsMy = isMy,
                HasAttachment = hasAttach,
                Attachments = attachments,
                AttachMd5 = attachMd5
            };

            _rayzitDb.RayzRepliesItems.InsertOnSubmit(newRayzReply);
            SaveDatabaseChanges();

            AllRayzReplies.Add(newRayzReply);

            if (App.ViewModel.SelectedRayz != null && App.ViewModel.SelectedRayz.Id == rayz.Id)
            {
                await SetActiveRayzReplies(App.ViewModel.SelectedRayz.RayzId);
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    var xtotal = AllRayzReplies.Count(rr => rr.RayzId.Equals(rayz.RayzId));
                    rayz.TotalRayzReplies = xtotal;
                    RayzDetails.NoRepliesGG.Visibility = ActiveRayzReplies.Count > 0 ? Visibility.Collapsed : Visibility.Visible;
                });
            }
            else
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    var xtotal = AllRayzReplies.Count(rr => rr.RayzId.Equals(rayz.RayzId));
                    rayz.TotalRayzReplies = xtotal;
                    rayz.UnreadRayzReplies++;
                });
            }
        }

        #endregion

        #region Power UP/DOWN Rayz Reply

        public async void PowerUp(RayzReply rr)
        {
            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            if (!App.EnoughRayzPower(4))
                return;

            var e = await App.Rsc.PowerUp(rr.RayzReplyId);

            if (CheckResponseError(e, false))
                return;

            rr.UpVotes = Convert.ToInt64(e["upVotes"].StringValue);
        }

        public async void PowerDown(RayzReply rr)
        {
            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            if (!App.EnoughRayzPower(4))
                return;

            var e = await App.Rsc.PowerDown(rr.RayzReplyId);

            if (CheckResponseError(e, false))
                return;

            var rayzReplyToUpdate = AllRayzReplies.FirstOrDefault(rayzReply => rayzReply.Id.Equals(rr.Id));

            if (rayzReplyToUpdate == null)
                return;

            rr.UpVotes = Convert.ToInt64(e["upVotes"].StringValue);
        }

        #endregion

        #region Report Rayz Reply

        public async void ReportRayzReply(RayzReply rr)
        {
            // Check for Internet Availability
            if (!(await App.NetMngr.InternetAvailableAsync(false)))
                return;

            var e = await App.Rsc.ReportRayzReply(rr.RayzReplyId);

            if (CheckResponseError(e, false))
                return;

            rr.ReportCount = Convert.ToInt64(e["report"].StringValue);
        }

        #endregion

        #region Delete Rayz Reply

        public void DeleteRayzReply(RayzReply rr)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    AllRayzReplies.Remove(rr);
                    ActiveRayzReplies.Remove(rr);
                    _rayzitDb.RayzRepliesItems.DeleteOnSubmit(rr);
                });
        }

        #endregion

        #endregion

        public void MarkActiveListAsRead(Rayz r)
        {
            Task.Run(() =>
                {
                    foreach (var reply in AllRayzReplies.ToList().Where(rr => rr.RayzId.Equals(r.RayzId) && !rr.IsRead))
                        MarkRayzReplyAsRead(reply);
                });
        }

        public void MarkActiveListAsRead(string rId)
        {
            var rayz = AllRayz.FirstOrDefault(r => r.RayzId.Equals(rId));

            if (rayz == null)
                return;

            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                rayz.TotalRayzReplies = ActiveRayzReplies.Count;
                rayz.UnreadRayzReplies = 0;
            });

            Task.Run(() =>
            {
                foreach (var reply in AllRayzReplies.ToList().Where(rr => rr.RayzId.Equals(rId) && !rr.IsRead))
                    MarkRayzReplyAsRead(reply);
            });
        }

        public void MarkRayzReplyAsRead(RayzReply rayzReply)
        {
            var rayz = AllRayz.FirstOrDefault(r => r.RayzId.Equals(rayzReply.RayzId));

            if (rayz == null)
                return;

            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                rayzReply.IsRead = true;
                if (rayz.UnreadRayzReplies > 0)
                    rayz.UnreadRayzReplies--;
            });
        }

        #endregion

        #region Attachments Methods & Declarations

        #region Declarations

        // Attachments
        private ObservableCollection<Attachment> _attachments;

        public ObservableCollection<Attachment> Attachments
        {
            get { return _attachments; }
            set
            {
                NotifyPropertyChanging("Attachments");
                _attachments = value;
                NotifyPropertyChanged("Attachments");
            }
        }

        #endregion

        #region Methods

        public void AddAttachment(Stream attachmentStream, string fileName, RayzItAttachment.ContentType type)
        {
            if (Attachments == null)
                Attachments = new ObservableCollection<Attachment>();

            switch (type)
            {
                case RayzItAttachment.ContentType.Image:

                    var bitmap = new BitmapImage();
                    bitmap.SetSource(attachmentStream);

                    var wb = new WriteableBitmap(bitmap);

                    var temp = new MemoryStream();

                    var x = (int)(wb.PixelWidth / 1.25);
                    var y = (int)(wb.PixelHeight / 1.25);

                    wb.SaveJpeg(temp, x, y, 0, 50);

                    SaveToIsolatedStorage(temp, fileName, type);

                    Attachments.Add(new Attachment(temp.ToArray(), fileName, type));
                    break;
                case RayzItAttachment.ContentType.Audio:
                    SaveToIsolatedStorage(attachmentStream, fileName, type);
                    Attachments.Add(new Attachment(((MemoryStream)attachmentStream).ToArray(), fileName, type));
                    break;
                case RayzItAttachment.ContentType.Video:

                    using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                    {
                        using (var fileStream = myIsolatedStorage.OpenFile(fileName, FileMode.Open, FileAccess.Read))
                        {
                            var bytes = new byte[fileStream.Length];
                            fileStream.Read(bytes, 0, (int)fileStream.Length);

                            Attachments.Add(new Attachment(bytes, fileName, type));
                        }
                    }
                    break;
            }
        }

        public void AddAttachmentFromBytes(byte[] attachmentBytes, string fileName, RayzItAttachment.ContentType type)
        {
            SaveBytesToIsolatedStorage(attachmentBytes, fileName);

            switch (type)
            {
                case RayzItAttachment.ContentType.Audio:
                    var t = new BitmapImage { UriSource = new Uri("/Assets/Attachments/speaker.png", UriKind.RelativeOrAbsolute) };
                    Attachments.Add(new Attachment(attachmentBytes, t, fileName, type));
                    break;
                case RayzItAttachment.ContentType.Video:
                    var tt = new BitmapImage { UriSource = new Uri("/Assets/Attachments/video.png", UriKind.RelativeOrAbsolute) };
                    SaveVideoAttachmentThumbnail(fileName);
                    Attachments.Add(new Attachment(attachmentBytes, tt, fileName, type));
                    break;
                default:
                    Attachments.Add(new Attachment(attachmentBytes, fileName, type));
                    break;
            }
        }

        public void AddAttachmentFromIsoStorage(string fileName, RayzItAttachment.ContentType type)
        {
            var attachmentBytes = ReadFromIsolatedStorage(fileName);
            if (attachmentBytes != null)
                Attachments.Add(new Attachment(attachmentBytes, fileName, type));
        }

        public void DeleteAttachment(Attachment a)
        {
            DeletePictureFromIsolatedStorage(a.FileName);
            Attachments.Remove(a);
        }

        public void DeleteAllAttachments()
        {
            foreach (var a in Attachments.ToList())
                DeleteAttachment(a);
        }

        public void ClearAttachmentsList()
        {
            if (Attachments == null)
                Attachments = new ObservableCollection<Attachment>();

            foreach (var test in Attachments)
                test.Dispose();

            Attachments.Clear();
        }

        public async Task<bool> LoadAttachmentsFromRayz(Rayz rayz)
        {
            var locations = rayz.Attachments.Split(new[] { ';' }, StringSplitOptions.RemoveEmptyEntries);
            var md5 = rayz.AttachMd5.Split(new[] { ';' }, StringSplitOptions.RemoveEmptyEntries);

            var localLocations = await LoadAttachments(locations, md5);

            if (localLocations != "")
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                    {
                        rayz.Attachments = localLocations;
                    });
            }

            if (LoadAttachmentsCompleted != null)
                LoadAttachmentsCompleted(this, true);

            return true;
        }

        public async Task<bool> LoadAttachmentsFromRayzReply(RayzReply rayzReply)
        {
            var locations = rayzReply.Attachments.Split(new[] { ';' }, StringSplitOptions.RemoveEmptyEntries);
            var md5 = rayzReply.AttachMd5.Split(new[] { ';' }, StringSplitOptions.RemoveEmptyEntries);

            var localLocations = await LoadAttachments(locations, md5);

            if (localLocations != "")
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    rayzReply.Attachments = localLocations;
                });
            }

            if (LoadAttachmentsCompleted != null)
                LoadAttachmentsCompleted(this, true);

            return true;
        }

        public async Task<String> LoadAttachments(String[] locations, String[] md5)
        {
            ClearAttachmentsList();
            var index = 0;

            var localLocations = "";
            foreach (var loc in locations)
            {
                if (loc[0] == '/')
                {
                    if (!(await App.NetMngr.InternetAvailableAsync(false)))
                        break;

                    //load from url
                    var fileName = loc.Split('/').Last();
                    var fileType = fileName.Substring(fileName.Length - 3);

                    var file = await App.Rsc.RequestAttachment(loc);

                    if (file == null)
                        continue;

                    var md5Ver = await Task.Run(() => MD5Core.GetHashString(file));

                    if (md5Ver != md5[index])
                    {
                        index++;
                        continue;
                    }

                    switch (fileType)
                    {
                        case "jpg":
                            AddAttachmentFromBytes(file, fileName, RayzItAttachment.ContentType.Image);
                            break;
                        case "wav":
                            AddAttachmentFromBytes(file, fileName, RayzItAttachment.ContentType.Audio);
                            break;
                        case "mp4":
                            AddAttachmentFromBytes(file, fileName, RayzItAttachment.ContentType.Video);
                            break;
                    }
                    localLocations += fileName + ";";
                }
                else
                {
                    // load from isolated storage
                    var fileType = loc.Substring(loc.Length - 3);

                    switch (fileType)
                    {
                        case "png":
                        case "jpeg":
                        case "jpg":
                        case "dat": //skydrive
                            var loc1 = loc;
                            //Deployment.Current.Dispatcher.BeginInvoke(() => AddAttachmentFromIsoStorage(loc1, RayzItAttachment.ContentType.Image));
                            AddAttachmentFromIsoStorage(loc1, RayzItAttachment.ContentType.Image);
                            break;
                        case "wav":
                            var loc2 = loc;
                            //Deployment.Current.Dispatcher.BeginInvoke(() => AddAttachmentFromIsoStorage(loc2, RayzItAttachment.ContentType.Audio));
                            AddAttachmentFromIsoStorage(loc2, RayzItAttachment.ContentType.Audio);
                            break;
                        case "mp4":
                            var loc3 = loc;
                            //Deployment.Current.Dispatcher.BeginInvoke(() => AddAttachmentFromIsoStorage(loc3, RayzItAttachment.ContentType.Video));
                            AddAttachmentFromIsoStorage(loc3, RayzItAttachment.ContentType.Video);
                            break;
                    }
                }

                index++;
            }

            return localLocations;
        }

        private void SaveBytesToIsolatedStorage(byte[] attachmentBytes, string fileName)
        {
            try
            {
                using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    if (myIsolatedStorage.FileExists(fileName))
                    {
                        myIsolatedStorage.DeleteFile(fileName);
                    }

                    var fileStream = myIsolatedStorage.CreateFile(fileName);

                    fileStream.Write(attachmentBytes, 0, attachmentBytes.Length);
                    fileStream.Close();
                }
            }
            catch (Exception)
            {
                Console.WriteLine(@"SaveBytesToIsolatedStorage error");
            }
        }

        private void SaveToIsolatedStorage(Stream attachmentStream, string fileName, RayzItAttachment.ContentType type)
        {
            try
            {
                using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    if (myIsolatedStorage.FileExists(fileName))
                    {
                        myIsolatedStorage.DeleteFile(fileName);
                    }

                    var fileStream = myIsolatedStorage.CreateFile(fileName);

                    switch (type)
                    {
                        case RayzItAttachment.ContentType.Image:
                            fileStream.Write(((MemoryStream)attachmentStream).ToArray(), 0, ((MemoryStream)attachmentStream).ToArray().Length);
                            fileStream.Close();
                            break;
                        case RayzItAttachment.ContentType.Audio:
                            fileStream.Write(((MemoryStream)attachmentStream).ToArray(), 0, ((MemoryStream)attachmentStream).ToArray().Length);
                            fileStream.Close();
                            break;
                    }
                }
            }
            catch (Exception)
            {
                Console.WriteLine(@"SaveToIsolatedStorage error");
            }
        }

        private void SaveVideoAttachmentThumbnail(string fileName)
        {
            var thumb = new BitmapImage
                {
                    UriSource = new Uri("/Assets/Attachments/video_white.png", UriKind.RelativeOrAbsolute),
                    CreateOptions = BitmapCreateOptions.None
                };
            thumb.ImageOpened += (s, e) =>
            {
                var wbm = new WriteableBitmap((BitmapImage)s);
                try
                {
                    byte[] data;
                    using (var ms = new MemoryStream())
                    {
                        wbm.SaveJpeg(ms, thumb.PixelHeight, thumb.PixelWidth, 0, 50);
                        data = ms.ToArray();
                    }
                    SaveBytesToIsolatedStorage(data, fileName + ".jpg");
                }
                catch (Exception)
                {
                    Console.WriteLine(@"SaveVideoAttachmentThumbnail error");
                }

            };
        }

        private byte[] ReadFromIsolatedStorage(string fileName)
        {
            try
            {
                using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    using (var fileStream = myIsolatedStorage.OpenFile(fileName, FileMode.Open, FileAccess.Read))
                    {
                        var bytes = new byte[fileStream.Length];
                        fileStream.Read(bytes, 0, (int)fileStream.Length);

                        return bytes;
                    }
                }
            }
            catch (Exception)
            {
                Console.WriteLine(@"ReadFromIsolatedStorage error");
                return null;
            }
        }

        private void DeletePictureFromIsolatedStorage(string fileName)
        {
            try
            {
                using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    if (myIsolatedStorage.FileExists(fileName))
                    {
                        myIsolatedStorage.DeleteFile(fileName);
                    }
                }
            }
            catch (Exception)
            {
                Console.WriteLine(@"DeletePictureFromIsolatedStorage error");
            }
        }

        #endregion

        #endregion

        #region Main View Model Generic Methods

        // Query database and load the collections and lists used by the pages.
        public void LoadCollectionsFromDatabase()
        {
            // Specify the query for live rayz in the database.
            var allRayzItemsInDb = from Rayz r in _rayzitDb.RayzItems
                                   orderby r.RayzDate descending
                                   select r;


            // Query the database and load live rayz.
            AllRayz = new ObservableCollection<Rayz>(allRayzItemsInDb);

            // Specify the query for all rayz replies in the database.
            var rayzRepliesItemsInDb = from RayzReply rr in _rayzitDb.RayzRepliesItems
                                       orderby rr.RayzReplyDate ascending
                                       select rr;

            // Query the database and load all to-do items.
            AllRayzReplies = new ObservableCollection<RayzReply>(rayzRepliesItemsInDb);

            GenerateLists();
            CheckSyncStatus();
            RemoveOutdatedRayz();
            UpdateSendingStatus();
        }

        private void CheckSyncStatus()
        {
            if (_starredR.Count == 0)
                _possibleSync = true;
        }

        public async void ServerDataSyncronization()
        {
            if (_possibleSync && App.Settings.SyncSetting)
            {
                var a = await UpdateStarredRayz();
                var b = await UpdateMyRayz();

                RefreshStarredRayzList();
                RefreshMyRayzList();

                if (a && b)
                {
                    App.Settings.SyncSetting = false;
                    _possibleSync = false;
                }
            }
        }

        /// <summary>
        /// Updates the User's Starred Rayz
        /// If a message already exists it updates it, else it adds it to the database
        /// </summary>
        public async Task<bool> UpdateStarredRayz()
        {
            var count = 0;
            var page = 0;

            do
            {
                page++;
                var e = await App.Rsc.GetStarredRayz(page);

                if (CheckResponseError(e, true))
                    return false;

                foreach (var obj in e.ObjectItems)
                {
                    if (!obj.Key.Equals("rayzFeed"))
                        continue;

                    count = 0;

                    // For each Rayz
                    foreach (var o in obj.Value.ArrayItems)
                    {
                        count++;

                        // Generate the Rayz
                        var newRayz = GenerateIncomingRayz(o);

                        // Add the constructed Rayz
                        // Search all Rayz to see if its already added
                        var ra =
                            AllRayz.ToList().FirstOrDefault(r => r.RayzId != null && r.RayzId.Equals(newRayz.RayzId));

                        // If the Rayz exists update its corresponding fileds
                        if (ra != null)
                        {
                            ra.IsStarred = true;
                            _starredR.Insert(0, ra);
                            SaveDatabaseChanges();
                            continue;
                        }

                        newRayz.IsStarred = true;
                        AllRayz.Insert(0, newRayz);

                        // If not add it
                        // Update the database
                        _rayzitDb.RayzItems.InsertOnSubmit(newRayz);

                        _starredR.Insert(0, newRayz);
                    }
                }

            } while (count == 20);
            SaveDatabaseChanges();
            return true;
        }

        /// <summary>
        /// Updates the User's Rayz
        /// If a message already exists it updates it, else it adds it to the database
        /// </summary>
        public async Task<bool> UpdateMyRayz()
        {
            var count = 0;
            var page = 0;

            do
            {
                page++;
                var e = await App.Rsc.GetMyRayz(page);

                if (CheckResponseError(e, true))
                    return false;

                foreach (var obj in e.ObjectItems)
                {
                    if (!obj.Key.Equals("myRayz"))
                        continue;

                    count = 0;

                    // For each Rayz
                    foreach (var o in obj.Value.ArrayItems)
                    {
                        count++;

                        // Generate the Rayz
                        var newRayz = GenerateIncomingRayz(o);

                        // Add the constructed Rayz
                        // Search all Rayz to see if its already added
                        var ra = AllRayz.ToList().FirstOrDefault(r => r.RayzId != null && r.RayzId.Equals(newRayz.RayzId));

                        // If the Rayz exists update its corresponding fileds
                        if (ra != null)
                        {
                            ra.IsMy = true;
                            _myR.Insert(0, ra);
                            SaveDatabaseChanges();
                            continue;
                        }

                        newRayz.IsMy = true;
                        AllRayz.Insert(0, newRayz);

                        // If not add it
                        // Update the database
                        _rayzitDb.RayzItems.InsertOnSubmit(newRayz);

                        // Update the lists
                        _myR.Insert(0, newRayz);
                    }
                }

            } while (count == 20);

            SaveDatabaseChanges();
            return true;
        }

        private void RemoveOutdatedRayz()
        {
            if (_liveR.Count < App.Settings.LiveQSetting)
                return;

            var d = (long)(DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0)).TotalMilliseconds;
            var toDelete = new ObservableCollection<Rayz>();

            foreach (var rayz in _liveR.Where(rayz => d - rayz.RayzDate > App.Settings.LiveTimeSetting))
                toDelete.Insert(0, rayz);

            foreach (var rayz in toDelete)
            {
                _liveR.Remove(rayz);

                if (rayz.IsMy || rayz.IsStarred)
                    continue;

                AllRayz.Remove(rayz);

                var rayz2 = rayz;
                foreach (var rr in AllRayzReplies.ToList().Where(rre => rre.RayzId.Equals(rayz2.RayzId)))
                {
                    AllRayzReplies.Remove(rr);
                    _rayzitDb.RayzRepliesItems.DeleteOnSubmit(rr);
                }

                _rayzitDb.RayzItems.DeleteOnSubmit(rayz);

                // Stop removing items if the count it less than the desired number
                if (_liveR.Count < App.Settings.LiveQSetting)
                    return;
            }
        }

        private void GenerateLists()
        {
            _liveR = new ObservableCollection<Rayz>(AllRayz.Where(r => r.IsLive && !r.IsHidden));
            _starredR = new ObservableCollection<Rayz>(AllRayz.Where(r => r.IsStarred && !r.IsHidden));
            _myR = new ObservableCollection<Rayz>(AllRayz.Where(r => r.IsMy && !r.IsHidden));

            SortLive();
            SortStarred();
            SortMy();
        }

        private void SortLive()
        {
            var failed = new ObservableCollection<Rayz>(_liveR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
            var sorted = new ObservableCollection<Rayz>(_liveR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

            var final = new ObservableCollection<Rayz>(sorted);

            foreach (var r in failed)
                final.Insert(0, r);

            LiveRayz = new ObservableCollection<Rayz>(final);
        }

        private void SortStarred()
        {
            var failed = new ObservableCollection<Rayz>(_starredR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
            var sorted = new ObservableCollection<Rayz>(_starredR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

            var final = new ObservableCollection<Rayz>(sorted);

            foreach (var r in failed)
                final.Insert(0, r);

            StarredRayz = new ObservableCollection<Rayz>(final);
        }

        private void SortMy()
        {
            var failed = new ObservableCollection<Rayz>(_myR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
            var sorted = new ObservableCollection<Rayz>(_myR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

            var final = new ObservableCollection<Rayz>(sorted);

            foreach (var r in failed)
                final.Insert(0, r);

            MyRayz = new ObservableCollection<Rayz>(final);
        }

        private async void RefreshLiveRayzList()
        {
            if (_liveR == null)
                return;

            if (App.ViewModel.SelectedRayz == null)
            {
                var finalList = await Task.Run(() =>
                {
                    var failed = new ObservableCollection<Rayz>(_liveR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
                    var sorted = new ObservableCollection<Rayz>(_liveR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

                    var final = new ObservableCollection<Rayz>(sorted);

                    foreach (var r in failed)
                        final.Insert(0, r);

                    return final;
                });

                _liveR = new ObservableCollection<Rayz>(finalList);
                Deployment.Current.Dispatcher.BeginInvoke(() => LiveRayz = new ObservableCollection<Rayz>(finalList));
            }
            else
                Deployment.Current.Dispatcher.BeginInvoke(() => LiveRayz = new ObservableCollection<Rayz>(_liveR));
        }

        public async void RefreshStarredRayzList()
        {
            if (_starredR == null)
                return;

            if (App.ViewModel.SelectedRayz == null)
            {
                var finalList = await Task.Run(() =>
                {
                    var failed = new ObservableCollection<Rayz>(_starredR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
                    var sorted = new ObservableCollection<Rayz>(_starredR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

                    var final = new ObservableCollection<Rayz>(sorted);

                    var enumerable = failed as Rayz[] ?? failed.ToArray();

                    for (var i = 0; i < enumerable.Count(); i++)
                        final.Insert(0, enumerable.ElementAt(i));

                    return final;
                });

                _starredR = new ObservableCollection<Rayz>(finalList);
                Deployment.Current.Dispatcher.BeginInvoke(() => StarredRayz = new ObservableCollection<Rayz>(finalList));
            }
            else
                Deployment.Current.Dispatcher.BeginInvoke(() => StarredRayz = new ObservableCollection<Rayz>(_starredR));
        }

        public async void RefreshMyRayzList()
        {
            if (_myR == null)
                return;

            if (App.ViewModel.SelectedRayz == null)
            {
                var finalList = await Task.Run(() =>
                {
                    var failed = new ObservableCollection<Rayz>(_myR.ToList().Where(r => (r.RayzDate == 0 || r.RayzDate == 1))).Distinct(new RayzComparer());
                    var sorted = new ObservableCollection<Rayz>(_myR.ToList().Where(r => (r.RayzDate != 0 && r.RayzDate != 1))).OrderByDescending(rayzL => rayzL.UnreadRayzReplies).ThenByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

                    var final = new ObservableCollection<Rayz>(sorted);

                    var enumerable = failed as Rayz[] ?? failed.ToArray();

                    for (var i = 0; i < enumerable.Count(); i++)
                        final.Insert(0, enumerable.ElementAt(i));

                    return final;
                });

                _myR = new ObservableCollection<Rayz>(finalList);
                Deployment.Current.Dispatcher.BeginInvoke(() => MyRayz = new ObservableCollection<Rayz>(finalList));
            }
            else
                Deployment.Current.Dispatcher.BeginInvoke(() => MyRayz = new ObservableCollection<Rayz>(_myR));
        }

        public void RefreshAllLists()
        {
            RefreshLiveRayzList();
            RefreshStarredRayzList();
            RefreshMyRayzList();
        }

        public void UpdateSendingStatus()
        {
            foreach (var rayz in AllRayz.Where(rayz => rayz.RayzDate == 0))
                rayz.RayzDate = 1;

            foreach (var rayzReply in AllRayzReplies.Where(rayz => rayz.RayzReplyDate == 0))
                rayzReply.RayzReplyDate = 1;
        }

        public void GetRayzByTerm(string term)
        {
            if (SearchRayz != null)
                SearchRayz.Clear();

            var results = new ObservableCollection<Rayz>(AllRayz.ToList().Where(r => (r.RayzMessage.ToLower().Contains(term)))).OrderByDescending(rayzL => rayzL.RayzDate).Distinct(new RayzComparer());

            SearchRayz = new ObservableCollection<Rayz>(results);
        }

        /// <summary>
        /// Calls the SubmitChanges() method to save all changes in the database.
        /// </summary>
        public void SaveDatabaseChanges()
        {
            try
            {
                Mut.WaitOne();
                _rayzitDb.SubmitChanges();
                Mut.ReleaseMutex();
            }
            catch (ChangeConflictException)
            {
                Mut.ReleaseMutex();
            }
        }

        public async Task<bool> SetActiveRayzReplies(string rayzId)
        {
            var finalList = await Task.Run(() =>
            {
                var send = new ObservableCollection<RayzReply>(AllRayzReplies.ToList().Where(r => (r.RayzReplyDate != 0 && r.RayzReplyDate != 1) && r.RayzId == rayzId)).Distinct(new RayzReplyComparer());

                var activeRrF = new ObservableCollection<RayzReply>(AllRayzReplies.ToList().Where(r => (r.RayzReplyDate == 0 || r.RayzReplyDate == 1) && r.RayzId == rayzId)).Distinct(new RayzReplyComparer());

                var sorted = new ObservableCollection<RayzReply>(send.OrderByDescending(r => r.RayzReplyDate));

                foreach (var r in activeRrF)
                    sorted.Insert(0, r);

                return sorted;
            });

            _activeRr = new ObservableCollection<RayzReply>(finalList);

            var rayz = AllRayz.FirstOrDefault(r => r.RayzId.Equals(rayzId));

            if (rayz == null)
                return true;

            Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    ActiveRayzReplies = new ObservableCollection<RayzReply>(finalList);
                    rayz.TotalRayzReplies = ActiveRayzReplies.Count;
                });

            return true;
        }

        public bool IsActiveRayzRepliesListEmpty()
        {
            if (_activeRr == null)
                return true;

            return _activeRr.Count <= 0;
        }

        public void ClearActiveRayzReplies()
        {
            if (_activeRr == null)
                return;

            Task.Run(() => _activeRr.Clear());

            Deployment.Current.Dispatcher.BeginInvoke(() => ActiveRayzReplies.Clear());
        }

        #endregion

        #region INotifyPropertyChanging Members

        public event PropertyChangingEventHandler PropertyChanging;

        // Used to notify that a property is about to change
        private void NotifyPropertyChanging(string propertyName)
        {
            if (PropertyChanging != null)
            {
                PropertyChanging(this, new PropertyChangingEventArgs(propertyName));
            }
        }

        #endregion

        #region INotifyPropertyChanged Members

        public event PropertyChangedEventHandler PropertyChanged;

        // Used to notify Silverlight that a property has changed.
        private void NotifyPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }
        #endregion
    }
}