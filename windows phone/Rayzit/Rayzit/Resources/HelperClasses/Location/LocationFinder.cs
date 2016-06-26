using System;
using System.Globalization;
using System.IO.IsolatedStorage;
using System.Threading.Tasks;
using System.Windows;
using Microsoft.Phone.Controls;
using Windows.Devices.Geolocation;

namespace Rayzit.Resources.HelperClasses.Location
{
    public class LocationFinder
    {
        public LocationInfo Info;
        private readonly Geolocator _geolocator;
        readonly IsolatedStorageSettings _settings;

        public LocationFinder()
        {
            _settings = IsolatedStorageSettings.ApplicationSettings;
            Info = new LocationInfo { Status = PositionStatus.NotInitialized };
            _geolocator = new Geolocator { DesiredAccuracyInMeters = 200, DesiredAccuracy = PositionAccuracy.Default };

            LoadFromCache();
        }

        public async void Update()
        {
            if (_geolocator.LocationStatus == PositionStatus.Disabled)
            {
                Info.Status = PositionStatus.Disabled;
                return;
            }

            try
            {
                var gp = await _geolocator.GetGeopositionAsync(TimeSpan.FromSeconds(6), TimeSpan.FromSeconds(20));
                UpdateInfo(gp);
            }

            catch (Exception)
            {
                // something else happened acquring the location
            }
        }

        public void UpdateInfo(Geoposition gp)
        {
            Info.Latitude = gp.Coordinate.Latitude.ToString("0.000000");
            Info.Longitude = gp.Coordinate.Longitude.ToString("0.000000");
            Info.Accuracy = gp.Coordinate.Accuracy.ToString(CultureInfo.InvariantCulture);
            Info.Status = PositionStatus.Ready;
            Info.Ttl = DateTime.Now.Millisecond;
            AddOrUpdateCache();
        }

        public async Task<bool> UpdateAsync()
        {
            if (_geolocator.LocationStatus == PositionStatus.Disabled)
            {
                Info.Status = PositionStatus.Disabled;
                return true;
            }

            try
            {
                var gp = await _geolocator.GetGeopositionAsync(TimeSpan.FromSeconds(6), TimeSpan.FromSeconds(20));
                UpdateInfo(gp);
            }

            catch (Exception)
            {
                // something else happened acquring the location
                return false;
            }

            return true;
        }

        private void AddOrUpdateCache()
        {
            // If the key exists
            if (_settings.Contains("locInfo"))
            {
                // Store the new value
                _settings["locInfo"] = Info;
            }
            // Otherwise create the key.
            else
            {
                _settings.Add("locInfo", Info);
            }

            _settings.Save();
        }

        private void LoadFromCache()
        {
            if (_settings.Contains("locInfo"))
            {
                Info = (LocationInfo)_settings["locInfo"];
            }
            // Otherwise, use the default value.
            else
            {
                Info.Status = PositionStatus.NoData;
            }
        }

        public bool GeolocationSettingEnabled()
        {
            if (!App.Settings.LocationToggleSwitchSetting)
            {
                var result =
                        MessageBox.Show((string)Application.Current.Resources["LocationSettingMessage"],
                                        (string)Application.Current.Resources["LocationSettingTitle"], MessageBoxButton.OK);

                if (result == MessageBoxResult.OK)
                    ((PhoneApplicationFrame)Application.Current.RootVisual).Navigate(new Uri("/Pages/Settings.xaml?NavigateTo=location", UriKind.RelativeOrAbsolute));

                return false;
            }

            return true;
        }

        public bool GeolocationAvailable(bool silenceMode)
        {
            if (Info.Status == PositionStatus.Disabled)
            {
                if (!silenceMode)
                {
                    var result =
                        MessageBox.Show((string)Application.Current.Resources["LocationMessage"],
                                        (string)Application.Current.Resources["LocationTitle"], MessageBoxButton.OK);

                    if (result == MessageBoxResult.OK)
                        ShowLocationSettings();
                }

                return false;
            }

            // RAYZIT SPECIFIC
            if (!App.Settings.LocationToggleSwitchSetting)
            {
                var result =
                        MessageBox.Show((string)Application.Current.Resources["LocationSettingMessage"],
                                        (string)Application.Current.Resources["LocationSettingTitle"], MessageBoxButton.OK);

                if (result == MessageBoxResult.OK)
                    ((PhoneApplicationFrame)Application.Current.RootVisual).Navigate(new Uri("/Pages/Settings.xaml", UriKind.RelativeOrAbsolute));

                return false;
            }

            if (_geolocator == null)
            {
                if (!silenceMode)
                    Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show((string)Application.Current.Resources["WaitingGeolocationMessage"], (string)Application.Current.Resources["WaitingGeolocationTitle"], MessageBoxButton.OK));
                Update();
                return false;
            }

            if (Info.Longitude == null)
            {
                Update();
                return false;
            }

            return true;
        }

        public async Task<bool> GeolocationAvailableAsync(bool silenceMode)
        {
            if (Info.Status == PositionStatus.Disabled)
            {
                if (!silenceMode)
                {
                    var result =
                        MessageBox.Show((string)Application.Current.Resources["LocationMessage"],
                                        (string)Application.Current.Resources["LocationTitle"], MessageBoxButton.OK);

                    if (result == MessageBoxResult.OK)
                        ShowLocationSettings();
                }

                return false;
            }

            // RAYZIT SPECIFIC
            if (!App.Settings.LocationToggleSwitchSetting)
            {
                var result =
                        MessageBox.Show((string)Application.Current.Resources["LocationSettingMessage"],
                                        (string)Application.Current.Resources["LocationSettingTitle"], MessageBoxButton.OK);

                if (result == MessageBoxResult.OK)
                    ((PhoneApplicationFrame)Application.Current.RootVisual).Navigate(new Uri("/Pages/Settings.xaml", UriKind.RelativeOrAbsolute));

                return false;
            }

            if (_geolocator == null)
            {
                if (!silenceMode)
                    Deployment.Current.Dispatcher.BeginInvoke(() => MessageBox.Show((string)Application.Current.Resources["WaitingGeolocationMessage"], (string)Application.Current.Resources["WaitingGeolocationTitle"], MessageBoxButton.OK));
                Update();
                return false;
            }


            if (Info.Longitude == null)
            {
                await UpdateAsync();
                return true;
            }

            return true;
        }

        public async void ShowLocationSettings()
        {
            await Windows.System.Launcher.LaunchUriAsync(new Uri("ms-settings-location:"));
        }
    }

    public class LocationInfo
    {
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string Accuracy { get; set; }
        public PositionStatus Status { get; set; }
        public Int64 Ttl { get; set; }
    }
}
