using System;
using System.Threading.Tasks;
using CodeTitans.Bayeux;
using CodeTitans.JSon;

namespace RayzitPushNotificationService
{
    /// <summary>
    /// Implements Faye pub/sub through Bayeux
    /// Long Polling Connection
    /// Specific class for Rayzit Server
    /// </summary>
    public class RayzitPushNotification
    {
        // Service specific Callback Event Handlers
        public event EventHandler<IJSonObject> RayzMessageReceived;
        public event EventHandler<IJSonObject> RayzReplyMessageReceived;
        public event EventHandler<IJSonObject> PowerUpdateReceived;

        // Service Private Variables
        private const string ConnectionUri = "http://faye.rayzit.com/faye";
        //private const string ConnectionUri = "http://dev.faye.rayzit.com/faye";
        private const string DefaultContentType = "application/json";
        private BayeuxConnection _connection;
        private bool _isConnected;

        // Default Channel - Checking Purposes
        private string _channel = "";

        /// <summary>
        /// Connects to the specified user channel
        /// Initializes a long polling connection and handshakes with the server
        /// Registers all the call back event handlers
        /// </summary>
        /// <param name="chan"> The channel to connect </param>
        public async void Connect(string chan)
        {
            await Task.Run(() =>
            {
                try
                {
                    if (_isConnected)
                        return;

                    _isConnected = true;

                    if (_channel.Equals(""))
                        _channel = "/messages/User_" + chan;

                    var httpDataSource = new HttpDataSource(ConnectionUri, null, DefaultContentType);
                    var httpLongPollingDataSource = new HttpDataSource(ConnectionUri, null, DefaultContentType);
                    _connection = new BayeuxConnection(httpDataSource, httpLongPollingDataSource) { LongPollingTimeout = 30000 };

                    _connection.Connected += connection_Connected;
                    _connection.Disconnected += connection_Disconnected;
                    _connection.EventReceived += LogChatEventReceived;
                    _connection.DataReceived += LogDataReceived;
                    _connection.DataFailed += LogDataFailed;
                    _connection.ConnectionFailed += LogConnectionFailed;
                    _connection.LongPollingFailed += _connection_LongPollingFailed;

                    _connection.Handshake();
                }
                catch (Exception)
                {
                    Console.WriteLine("Faye Connect Error");
                }
            });
        }

        /// <summary>
        /// Disconnects from the Server
        /// </summary>
        public void Disconnect()
        {
            try
            {
                if (_connection == null || _connection.ClientID == null)
                    return;

                _isConnected = false;

                _connection.Connected -= connection_Connected;
                _connection.EventReceived -= LogChatEventReceived;
                _connection.DataReceived -= LogDataReceived;
                _connection.DataFailed -= LogDataFailed;
                _connection.ConnectionFailed -= LogConnectionFailed;

                _connection.Disconnect();
            }
            catch (Exception)
            {
                Console.WriteLine("Faye Disconnect Error");
            }
        }

        /// <summary>
        /// Returns the Connection Status
        /// </summary>
        /// <returns> Connected, Connecting, Dictonnected, Unknown </returns>
        public String GetConnectionStatus()
        {
            switch (_connection.State)
            {
                case BayeuxConnectionState.Connected:
                    return "Connected";
                case BayeuxConnectionState.Connecting:
                    return "Connecting";
                case BayeuxConnectionState.Disconnected:
                    return "Disconnected";
            }
            return "unknown";
        }

        void _connection_LongPollingFailed(object sender, BayeuxConnectionEventArgs e)
        {
            Disconnect();
        }

        private void connection_Disconnected(object sender, BayeuxConnectionEventArgs e)
        {
            Disconnect();
        }

        /// <summary>
        /// Handles the messages from server to the user's subscribed channel and calls the appropriate event handler for further processing
        /// Supports 3 types of messages:
        ///     Rayz Received
        ///     Rayz Reply Received
        ///     Rayz Power Updates
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void LogChatEventReceived(object sender, BayeuxConnectionEventArgs e)
        {
            var reader = new JSonReader();

            if (e.Message != null)
            {
                var reply = reader.ReadAsJSonObject(e.Message.ToString());

                //System.Diagnostics.Debug.WriteLine(_count++);

                if (reply.ObjectItems != null)
                    foreach (var objItem in reply.ObjectItems)
                    {
                        if (objItem.Key != "data") continue;

                        if (objItem.Value != null)
                        {
                            var type = objItem.Value["mtype"].ToString();

                            switch (type)
                            {
                                case "rayz":
                                    if (RayzMessageReceived != null)
                                        RayzMessageReceived(this, objItem.Value);
                                    break;
                                case "rayz_reply":
                                    if (RayzReplyMessageReceived != null)
                                        RayzReplyMessageReceived(this, objItem.Value);
                                    break;
                                case "power":
                                    if (PowerUpdateReceived != null)
                                        PowerUpdateReceived(this, objItem.Value);
                                    break;
                            }
                        }
                    }
            }
        }

        /// <summary>
        /// Handles all the messages received from server
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void LogDataReceived(object sender, BayeuxConnectionEventArgs e)
        {
            if (!_connection.IsLongPolling)
                return;

            if (_connection.Subscribed(_channel))
                _isConnected = true;

            var reader = new JSonReader();
            if (e.Message != null)
            {
                var reply = reader.ReadAsJSonObject(e.Message.ToString());

                if (reply.ArrayItems != null)
                    foreach (var aItem in reply.ArrayItems)
                    {
                        if (aItem.ObjectItems != null)
                            foreach (var objItem in aItem.ObjectItems)
                            {
                                if (objItem.Key != "advice") continue;

                                if (objItem.Value != null)
                                {
                                    var msg = objItem.Value.ToString();

                                    if (msg.Equals("{\r\n    \"reconnect\": \"handshake\"\r\n}"))
                                    {
                                        Disconnect();
                                        Connect("");
                                    }
                                }
                            }
                    }
            }
        }

        /// <summary>
        /// Starts a long polling connection and subscribes to the user channel
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void connection_Connected(object sender, BayeuxConnectionEventArgs e)
        {
            try
            {
                _connection.Connect();
                _connection.StartLongPolling();
                _connection.Subscribe(_channel);
            }
            catch (Exception)
            {
                Console.WriteLine("connection_Connected");
            }
        }

        private void LogConnectionFailed(object sender, BayeuxConnectionEventArgs e)
        {
            // TODO CHECK
        }

        private void LogDataFailed(object sender, BayeuxConnectionEventArgs e)
        {
            // TODO CHECK
        }
    }
}
