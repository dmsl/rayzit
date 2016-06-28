using System;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Data.Linq;
using Microsoft.Phone.Scheduler;
using Microsoft.Phone.Shell;
using Rayzit.Pages;
using Rayzit.Resources.HelperClasses;
using Rayzit.Resources.HelperClasses.ErrorReporting;
using Rayzit.Resources.HelperClasses.Location;
using Rayzit.Resources.HelperClasses.NetworkManager;
using Rayzit.Resources.HelperClasses.RpncMngr;
using Rayzit.Resources.UriMapper;
using Rayzit.ViewModels;
using RayzitPushNotificationService;
using RayzitServiceClient;
using Windows.Devices.Geolocation;
using Windows.Networking.Connectivity;

namespace Rayzit
{
    public partial class App : Application
    {
        // The current version of the application.
        public static int AppVersion = 2;

        // Application Wide Variables
        public static Geolocator Geolocator { get; set; }
        public static bool RunningInBackground { get; set; }
        // Rayzit Service Client
        public static Rsc Rsc;

        //Periodic Agent
        PeriodicTask _periodicTask;
        private const string PeriodicTaskName = "RayzitPeriodicAgent";

        // Push Notification Service
        public static RayzitPushNotification Rpnc;
        public static RpncMngr RpncMgr = new RpncMngr();

        // Network Checker 
        public static NetworkChecker NetMngr = new NetworkChecker();

        public static RayzitSettings Settings = new RayzitSettings();
        public static PowerBar Pb = new PowerBar();

        // OTHER VARIABLES
        public static String MDeviceUniqueId;
        public static LocationFinder LocFinder = new LocationFinder();
        public static MainViewModel ViewModel { get; set; }

        /// <summary>
        /// Provides easy access to the root frame of the Phone Application.
        /// </summary>
        /// <returns>The root frame of the Phone Application.</returns>
        public static PhoneApplicationFrame RootFrame { get; private set; }

        //readonly IsolatedStorageSettings _settings = IsolatedStorageSettings.ApplicationSettings;

        /// <summary>
        /// Constructor for the Application object.
        /// </summary>
        public App()
        {
            // set sync context for ui thread so async void exceptions can be handled, keeps process alive
            AsyncSynchronizationContext.Register();

            // ensure unobserved task exceptions (unawaited async methods returning Task or Task<T>) are handled
            TaskScheduler.UnobservedTaskException += TaskScheduler_UnobservedTaskException;

            // Global handler for uncaught exceptions.
            UnhandledException += Application_UnhandledException;

            // Standard XAML initialization
            InitializeComponent();

            // Phone-specific initialization
            InitializePhoneApplication();

            // Show graphics profiling information while debugging.
            if (Debugger.IsAttached)
            {
                // Display the current frame rate counters.
                //Current.Host.Settings.EnableFrameRateCounter = true;

                // Show the areas of the app that are being redrawn in each frame.
                //Application.Current.Host.Settings.EnableRedrawRegions = true;

                // Enable non-production analysis visualization mode,
                // which shows areas of a page that are handed off to GPU with a colored overlay.
                //Application.Current.Host.Settings.EnableCacheVisualization = true;

                // Prevent the screen from turning off while under the debugger by disabling
                // the application's idle detection.
                // Caution:- Use this under debug mode only. Application that disables user idle detection will continue to run
                // and consume battery power when the user is not using the phone.
                PhoneApplicationService.Current.UserIdleDetectionMode = IdleDetectionMode.Disabled;
            }
        }

        #region Main Helper Methods

        static void TaskScheduler_UnobservedTaskException(object sender, UnobservedTaskExceptionEventArgs e)
        {
            e.SetObserved();
            FlurryWP8SDK.Api.LogError("Async Error", e.Exception);
        }

        public static bool EnoughRayzPower(int value)
        {
            if (Settings.PowerValueSetting < value)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show((string)Current.Resources["RayzPowerNotEnoughMessage"], (string)Current.Resources["GeneralErrorTitle"], MessageBoxButton.OK));
                Rsc.GetPower();
                return false;
            }

            return true;
        }

        #endregion

        #region RayzIT Specific

        private void InitializePeriodicAgent()
        {
            // Obtain a reference to the period task, if one exists
            _periodicTask = ScheduledActionService.Find(PeriodicTaskName) as PeriodicTask;

            if (_periodicTask != null)
            {
                try
                {
                    ScheduledActionService.Remove(PeriodicTaskName);
                }
                catch (Exception)
                {
                    Console.WriteLine(@"Periodic agent error.");
                }
            }

            _periodicTask = new PeriodicTask(PeriodicTaskName);
            _periodicTask.Description = "This is Rayzit background task. It is used to update your location and the default live tile every 30 minutes.";

            // The description is required for periodic agents. This is the string that the user
            // will see in the background services Settings page on the device.

            // Place the call to Add in a try block in case the user has disabled agents.
            try
            {
                ScheduledActionService.Add(_periodicTask);

                // If debugging is enabled, use LaunchForTest to launch the agent in one minute.

                ScheduledActionService.LaunchForTest(PeriodicTaskName, TimeSpan.FromSeconds(30));
            }
            catch (InvalidOperationException exception)
            {
                if (exception.Message.Contains("BNS Error: The action is disabled"))
                {
                    MessageBox.Show((string)Current.Resources["BackgroundAgentDisabledMessage"]);
                }

                if (exception.Message.Contains((string)Current.Resources["BackgroundAgentsExitedMessage"]))
                {
                    // No user action required. The system prompts the user when the hard limit of periodic tasks has been reached.
                    MessageBox.Show((string)Current.Resources["BackgroundAgentsExited2Message"]);
                }
            }
            catch (SchedulerServiceException)
            {
                // No user action required.  
            }
        }

        private void NetworkInformation_NetworkStatusChanged(object sender)
        {
            var status = System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable();

            if (!status)
            {
                RpncMgr.DestroyFaye();
            }
            else
            {
                ServerSync();
            }
        }

        private async void ServerSync()
        {
            if (!(await NetMngr.InternetAvailableAsync(true)))
                return;

            RpncMgr.ConnectFaye();

            await RegisterUser();
            await ViewModel.UpdateUi(true);
        }

        private static async Task<bool> RegisterUser()
        {
            if (!Settings.LocationToggleSwitchSetting)
                return false;

            if (await LocFinder.GeolocationAvailableAsync(true))
                await Rsc.UpdatePosition(LocFinder.Info.Latitude, LocFinder.Info.Longitude, LocFinder.Info.Accuracy);

            return true;
        }



        #endregion

        // Code to execute when the application is launching (eg, from Start)
        // This code will not execute when the application is reactivated
        private void Application_Launching(object sender, LaunchingEventArgs e)
        {
            MDeviceUniqueId = BitConverter.ToString(Convert.FromBase64String(Windows.Phone.System.Analytics.HostInformation.PublisherHostId)).Replace("-", string.Empty);

            // Rayzit API Client & Push Notification Service
            Rsc = new Rsc("WP8APP_AVC3PAFGO054FLP3AGEL0L", MDeviceUniqueId);

            LocFinder.Update();

            FlurryWP8SDK.Api.StartSession("H66V7XHYX85YKD7RMH7G");
            FlurryWP8SDK.Api.LogEvent("Application_Launching");

            CheckDatabase();

            // Network Interface Event Callbacks
            NetworkInformation.NetworkStatusChanged += NetworkInformation_NetworkStatusChanged;
        }

        private static void CheckDatabase()
        {
            // Specify the local database connection string.
            const string dbConnectionString = "Data Source=isostore:/rayzitDB.sdf";

            // Create the database if it does not exist.
            using (var db = new RayzDataContext(dbConnectionString))
            {
                if (db.DatabaseExists() == false)
                {
                    // Create the local database.
                    db.CreateDatabase();
                    // Set the new database version.
                    var dbUpdater = db.CreateDatabaseSchemaUpdater();
                    dbUpdater.DatabaseSchemaVersion = AppVersion;
                    dbUpdater.Execute();
                }
                else
                {
                    // Check whether a database update is needed.
                    var dbUpdater = db.CreateDatabaseSchemaUpdater();

                    if (dbUpdater.DatabaseSchemaVersion < AppVersion)
                    {
                        // Delete the local database.
                        db.DeleteDatabase();

                        // Create the local database.
                        db.CreateDatabase();
                        // Set the new database version.
                        dbUpdater.DatabaseSchemaVersion = AppVersion;
                        dbUpdater.Execute();
                    }
                }
            }

            // Create the ViewModel object.
            ViewModel = new MainViewModel(dbConnectionString);

            ViewModel.LoadCollectionsFromDatabase();
        }

        // Code to execute when the application is activated (brought to foreground)
        // This code will not execute when the application is first launched
        private void Application_Activated(object sender, ActivatedEventArgs e)
        {
            InitializeComponent();

            FlurryWP8SDK.Api.StartSession("H66V7XHYX85YKD7RMH7G");

            NetMngr.RefreshStatus();
            LocFinder.Update();
            ServerSync();
            RunningInBackground = false;
            ViewModel.UpdateSendingStatus();
        }

        // Code to execute when the application is deactivated (sent to background)
        // This code will not execute when the application is closing
        private void Application_Deactivated(object sender, DeactivatedEventArgs e)
        {
            // Ensure that required application state is persisted here.
            FlurryWP8SDK.Api.LogEvent("Application_Deactivated");
            RpncMgr.DestroyFaye();
            ViewModel.SaveDatabaseChanges();
        }

        // Code to execute when the application is closing (eg, user hit Back)
        // This code will not execute when the application is deactivated
        private void Application_Closing(object sender, ClosingEventArgs e)
        {
            FlurryWP8SDK.Api.LogEvent("Application_Closing");

            // Periodic Agent
            InitializePeriodicAgent();
            ViewModel.SaveDatabaseChanges();
        }

        private void Application_RunningInBackground(object sender, RunningInBackgroundEventArgs args)
        {
            RunningInBackground = true;
        }

        // Code to execute if a navigation fails
        private void RootFrame_NavigationFailed(object sender, NavigationFailedEventArgs e)
        {
            if (Debugger.IsAttached)
            {
                // A navigation has failed; break into the debugger
                Debugger.Break();
            }
        }

        // Code to execute on Unhandled Exceptions
        private void Application_UnhandledException(object sender, ApplicationUnhandledExceptionEventArgs e)
        {
            ViewModel.SaveDatabaseChanges();
            if (Debugger.IsAttached)
            {
                // An unhandled exception has occurred; break into the debugger
                Debugger.Break();
            }

            e.Handled = true;
            FlurryWP8SDK.Api.LogError(e.ExceptionObject.Message, e.ExceptionObject);
        }

        #region Phone application initialization

        // Avoid double-initialization
        private bool phoneApplicationInitialized = false;

        // Do not add any additional code to this method
        private void InitializePhoneApplication()
        {
            if (phoneApplicationInitialized)
                return;

            // Create the frame but don't set it as RootVisual yet; this allows the splash
            // screen to remain active until the application is ready to render.
            RootFrame = new TransitionFrame();
            RootFrame.Background = new SolidColorBrush(Colors.White);
            RootFrame.Navigated += CompleteInitializePhoneApplication;

            // Handle navigation failures
            RootFrame.NavigationFailed += RootFrame_NavigationFailed;

            // Assign the custom URI mapper class to the application frame.
            RootFrame.UriMapper = new CustomUriMapper();

            // Ensure we don't initialize again
            phoneApplicationInitialized = true;
        }

        // Do not add any additional code to this method
        private void CompleteInitializePhoneApplication(object sender, NavigationEventArgs e)
        {
            // Set the root visual to allow the application to render
            if (RootVisual != RootFrame)
                RootVisual = RootFrame;

            // Remove this handler since it is no longer needed
            RootFrame.Navigated -= CompleteInitializePhoneApplication;
        }

        #endregion

    }
}