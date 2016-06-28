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
        /// Checks If Internet connection is available.
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
