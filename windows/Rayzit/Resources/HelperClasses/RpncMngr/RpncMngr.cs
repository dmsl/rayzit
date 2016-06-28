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

using CodeTitans.JSon;
using RayzitPushNotificationService;

namespace Rayzit.Resources.HelperClasses.RpncMngr
{
    public class RpncMngr
    {
        private void InitializeFaye()
        {
            App.Rpnc = new RayzitPushNotification();

            // Register Push Notification Service Callbacks
            App.Rpnc.RayzMessageReceived += Rpnc_RayzMessageReceived;
            App.Rpnc.RayzReplyMessageReceived += Rpnc_RayzReplyMessageReceived;
            App.Rpnc.PowerUpdateReceived += Rpnc_PowerUpdateReceived;
        }

        public void ConnectFaye()
        {
            if (App.Rpnc != null)
                App.Rpnc.Connect(App.MDeviceUniqueId);
            else
            {
                InitializeFaye();
                if (App.Rpnc != null)
                    App.Rpnc.Connect(App.MDeviceUniqueId);
            }
        }

        public void DestroyFaye()
        {
            if (App.Rpnc == null)
                return;

            App.Rpnc.Disconnect();

            // Register Push Notification Service Callbacks
            App.Rpnc.RayzMessageReceived -= Rpnc_RayzMessageReceived;
            App.Rpnc.RayzReplyMessageReceived -= Rpnc_RayzReplyMessageReceived;
            App.Rpnc.PowerUpdateReceived -= Rpnc_PowerUpdateReceived;

            App.Rpnc = null;
        }

        void Rpnc_RayzMessageReceived(object sender, IJSonObject e)
        {
            App.ViewModel.CreateNewRayzFromIncomming(e);
        }

        void Rpnc_RayzReplyMessageReceived(object sender, IJSonObject e)
        {
            App.ViewModel.CreateNewRayzReplyFromIncomming(e);
        }

        void Rpnc_PowerUpdateReceived(object sender, IJSonObject e)
        {
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
    }
}
