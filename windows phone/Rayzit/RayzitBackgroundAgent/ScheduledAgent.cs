using System;
using System.Diagnostics;
using System.IO.IsolatedStorage;
using System.Linq;
using System.Net.NetworkInformation;
using System.Threading.Tasks;
using System.Windows;
using CodeTitans.JSon;
using Microsoft.Phone.Scheduler;
using Microsoft.Phone.Shell;
using RayzitBackgroundAgent.Geolocation;
using RayzitServiceClient;

namespace RayzitBackgroundAgent
{
    public class ScheduledAgent : ScheduledTaskAgent
    {
        // Application Wide Variables
        public static LocationInfoBG Geolocation;
        private static readonly string MDeviceUniqueId;

        /// <remarks>
        /// ScheduledAgent constructor, initializes the UnhandledException handler
        /// </remarks>
        static ScheduledAgent()
        {
            MDeviceUniqueId = BitConverter.ToString(Convert.FromBase64String(Windows.Phone.System.Analytics.HostInformation.PublisherHostId)).Replace("-", string.Empty);

            // Subscribe to the managed exception handler
            Deployment.Current.Dispatcher.BeginInvoke(delegate
            {
                Application.Current.UnhandledException += UnhandledException;
            });
        }

        /// Code to execute on Unhandled Exceptions
        private static void UnhandledException(object sender, ApplicationUnhandledExceptionEventArgs e)
        {
            if (Debugger.IsAttached)
            {
                // An unhandled exception has occurred; break into the debugger
                Debugger.Break();
            }

            e.Handled = true;
        }

        /// <summary>
        /// Agent that runs a scheduled task
        /// </summary>
        /// <param name="task">
        /// The invoked task
        /// </param>
        /// <remarks>
        /// This method is called when a periodic or resource intensive task is invoked
        /// </remarks>
        protected override async void OnInvoke(ScheduledTask task)
        {
            // Nothing to be done without Internet Connectivity
            if (!NetworkInterface.GetIsNetworkAvailable())
            {
                NotifyComplete();
                return;
            }

            var appSettings = IsolatedStorageSettings.ApplicationSettings;

            var enabled = appSettings.Contains("LocationToggleSwitchSetting") && (bool)appSettings["LocationToggleSwitchSetting"];

            if (!enabled)
            {
                NotifyComplete();
                return;
            }

            var rsc = new Rsc("WP8APP_AVC3PAFGO054FLP3AGEL0L", MDeviceUniqueId);

            Geolocation = await new LocationFinderBG().GetLocation();

            // Problem occured with Geolocation
            if (Geolocation == null)
            {
                NotifyComplete();
                return;
            }

            // Update User's Position
            await rsc.UpdatePosition(Geolocation.Latitude, Geolocation.Longitude, Geolocation.Accuracy);

            // Request User's LiveFeed with one random (latest) rayz
            var reply = await rsc.GetLiveFeedRandom();

            await ParseLiveFeed(reply);

            //ScheduledActionService.LaunchForTest(task.Name, TimeSpan.FromSeconds(60));

            NotifyComplete();
        }

        private async static Task<bool> ParseLiveFeed(IJSonObject e)
        {
            var imageUrl = String.Empty;
            var counter = 0;
            var rayzMessage = String.Empty;

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
                        {
                            // Extract Rayz details
                            foreach (var oi in x.ObjectItems)
                            {
                                switch (oi.Key)
                                {
                                    case "rayz_message":
                                        rayzMessage = oi.Value.ToString();
                                        break;
                                    case "attachments":
                                        foreach (var attachmentObject in oi.Value.ObjectItems)
                                        {
                                            var stop = false;
                                            switch (attachmentObject.Key)
                                            {
                                                case "images":
                                                    var img = attachmentObject.Value.ArrayItems.FirstOrDefault();
                                                    if (img != null) imageUrl = img["url"].StringValue;
                                                    stop = true;
                                                    break;
                                            }

                                            if (stop)
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }


            if (!String.IsNullOrEmpty(imageUrl))
            {
                var rsc = new Rsc("WP8APP_AVC3PAFGO054FLP3AGEL0L", MDeviceUniqueId);
                var image = await rsc.RequestAttachment(imageUrl);

                if (image != null)
                {

                    Deployment.Current.Dispatcher.BeginInvoke(() =>
                        {
                            const string fileName = "/Shared/ShellContent/tile.jpg";

                            using (
                                var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication()
                                )
                            {
                                if (myIsolatedStorage.FileExists(fileName))
                                {
                                    myIsolatedStorage.DeleteFile(fileName);
                                }

                                var fileStream = myIsolatedStorage.CreateFile(fileName);

                                fileStream.Write(image, 0, image.Length);
                                fileStream.Close();
                            }
                        });
                }
            }


            //if (!String.IsNullOrEmpty(rayzMessage))
            //    ShowToast(rayzMessage);

            if (String.IsNullOrEmpty(rayzMessage))
                ResetLiveTile();
            else
                UpdateLiveTile(rayzMessage, counter, !String.IsNullOrEmpty(imageUrl));

            return true;
        }

        private static void UpdateLiveTile(String message, int count, bool image)
        {
            var appSettings = IsolatedStorageSettings.ApplicationSettings;

            var enabled = appSettings.Contains("LiveTileToggleSwitchSetting") && (bool) appSettings["LiveTileToggleSwitchSetting"];

            if (!enabled)
                return;

            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            if (image)
            {
                var oFliptile = new FlipTileData
                    {
                        Title = "Rayzit",
                        Count = count,
                        BackTitle = "Live Feed",
                        BackContent = message,
                        WideBackContent = message,
                        BackBackgroundImage = new Uri("isostore:/Shared/ShellContent/tile.jpg", UriKind.Absolute),
                        WideBackBackgroundImage = new Uri("isostore:/Shared/ShellContent/tile.jpg", UriKind.Absolute)
                    };
                oTile.Update(oFliptile);
            }
            else
            {
                var oFliptile = new FlipTileData
                {
                    Title = "Rayzit",
                    Count = count,
                    BackTitle = "Live Feed",
                    BackContent = message,
                    WideBackContent = message,
                    BackBackgroundImage = new Uri("", UriKind.Relative),
                    WideBackBackgroundImage = new Uri("", UriKind.Relative)
                };
                
                oTile.Update(oFliptile);
            }
        }

        private static void ResetLiveTile()
        {
            var appSettings = IsolatedStorageSettings.ApplicationSettings;

            var enabled = appSettings.Contains("LiveTileToggleSwitchSetting") && (bool)appSettings["LiveTileToggleSwitchSetting"];

            if (!enabled)
                return;

            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            var oFliptile = new FlipTileData
            {
                Count = 0,
                BackTitle = String.Empty,
                BackContent = String.Empty,
                WideBackContent = String.Empty,
                BackBackgroundImage = null,
                WideBackBackgroundImage = null,
            };

            oTile.Update(oFliptile);
        }
    }
}