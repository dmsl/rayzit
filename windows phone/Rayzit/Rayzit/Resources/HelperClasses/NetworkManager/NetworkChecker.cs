using System;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using Microsoft.Phone.Net.NetworkInformation;
using Windows.Networking.Connectivity;

namespace Rayzit.Resources.HelperClasses.NetworkManager
{
    public class NetworkChecker
    {
        public bool NetworkStatus;
        private bool _initialized;
        private bool _initializing;

        public NetworkChecker()
        {
            InitializeChecker();
        }

        private async void InitializeChecker()
        {
            _initializing = true;
            NetworkStatus = await GetInternetStatusAsync();
            _initializing = false;
            _initialized = true;

            // Network Interface Event Callbacks
            NetworkInformation.NetworkStatusChanged += NetworkInformation_NetworkStatusChanged;
        }

        public async void RefreshStatus()
        {
            NetworkStatus = await GetInternetStatusAsync();
        }

        /// <summary>
        /// Checks If internet connection is available.
        /// If silent mode is off shows a message and navigates user to WiFi Settings. 
        /// </summary>
        /// <param name="silenceMode"></param>
        /// <returns></returns>
        public async Task<bool> InternetAvailableAsync(bool silenceMode)
        {
            if (!_initialized || !NetworkStatus)
            {
                if (!_initializing)
                {
                    _initializing = true;
                    NetworkStatus = await GetInternetStatusAsync();
                    _initializing = false;
                    _initialized = true;
                }
            }

            if (!silenceMode && !NetworkStatus)
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    var result = MessageBox.Show(NetworkCheckerErrorMessages.NetworkErrorMessage,
                                                 NetworkCheckerErrorMessages.NetworkErrorTitle,
                                                    MessageBoxButton.OK);

                    if (result == MessageBoxResult.OK)
                    {
                        var task = new Microsoft.Phone.Tasks.ConnectionSettingsTask
                        {
                            ConnectionSettingsType = Microsoft.Phone.Tasks.ConnectionSettingsType.WiFi
                        };
                        task.Show();
                    }
                });

            return NetworkStatus;
        }

        private static async Task<bool> GetInternetStatusAsync()
        {
            var res = await Task.Run(() =>
            {
                try
                {
                    var status = NetworkInterface.NetworkInterfaceType != NetworkInterfaceType.None;
                    return status;
                }
                catch (Exception)
                {
                    return false;
                }
            });

            return res;
        }

        private void NetworkInformation_NetworkStatusChanged(object sender)
        {
            NetworkStatus = System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable();

            if (!NetworkStatus)
                _initialized = false;
        }
    }
}
