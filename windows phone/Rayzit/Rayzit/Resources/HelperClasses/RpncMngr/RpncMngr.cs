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
