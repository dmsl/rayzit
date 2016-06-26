using System;
using System.IO;
using System.IO.IsolatedStorage;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Threading;
using Microsoft.Devices;
using Microsoft.Phone.Shell;
using Windows.Phone.Media.Capture;
using Windows.Storage;
using Windows.Storage.Streams;
using RayzitServiceClient.HelperClasses;
using Size = Windows.Foundation.Size;

namespace Rayzit.Pages.Attachments
{
    public partial class VideoRecorder
    {
        private readonly DispatcherTimer _dt;

        // Viewfinder for capturing video.
        private VideoBrush _videoRecorderBrush;

        // Source and device for capturing video.
        private AudioVideoCaptureDevice _videoCaptureDevice;

        // File details for storing the recording.        
        StorageFile _outputFile;
        IRandomAccessStream _stream;
        private IsolatedStorageFileStream _isoVideoFile;
        private readonly string _isoVideoFileName;

        // For managing button and application state.
        private enum ButtonState { Initialized, Ready, Recording, Playback, Paused, NoChange, CameraNotSupported };
        private ButtonState _currentAppState;

        public VideoRecorder()
        {
            InitializeComponent();

            _dt = new DispatcherTimer { Interval = TimeSpan.FromSeconds(15) };
            _dt.Tick += dt_Tick;

            // Prepare ApplicationBar and buttons.
            PhoneAppBar = (ApplicationBar)ApplicationBar;
            PhoneAppBar.IsVisible = true;
            StartRecording = ((ApplicationBarIconButton)ApplicationBar.Buttons[0]);
            StopPlaybackRecording = ((ApplicationBarIconButton)ApplicationBar.Buttons[1]);
            StartPlayback = ((ApplicationBarIconButton)ApplicationBar.Buttons[2]);
            SavePlayback = ((ApplicationBarIconButton)ApplicationBar.Buttons[3]);

            // Create the file name.
            var msec = (DateTime.Now - new DateTime(1970, 1, 1, 0, 0, 0, 0).ToLocalTime()).TotalMilliseconds;
            _isoVideoFileName = "VID_" + msec + ".mp4";

            Loaded += VideoRecorder_Loaded;
        }

        #region Initialization / UI

        void VideoRecorder_Loaded(object sender, RoutedEventArgs e)
        {
            // Initialize the video recorder.
            InitializeVideoRecorder();
        }

        public async void InitializeVideoRecorder()
        {
            try
            {
                if (_videoCaptureDevice == null)
                {
                    var applicationFolder = ApplicationData.Current.LocalFolder;
                    _outputFile = await applicationFolder.CreateFileAsync(_isoVideoFileName, CreationCollisionOption.ReplaceExisting);
                    _stream = await _outputFile.OpenAsync(FileAccessMode.ReadWrite);
                    _videoCaptureDevice = await AudioVideoCaptureDevice.OpenAsync(CameraSensorLocation.Back, new Size(320, 240));
                    _videoCaptureDevice.VideoEncodingFormat = CameraCaptureVideoFormat.H264;

                    // Add eventhandlers for captureSource.
                    _videoCaptureDevice.RecordingFailed += RecordingFailed;

                    // Initialize the camera if it exists on the device.
                    if (_videoCaptureDevice != null)
                    {
                        // Create the VideoBrush for the viewfinder.
                        _videoRecorderBrush = new VideoBrush();
                        _videoRecorderBrush.SetSource(_videoCaptureDevice);

                        // Display the viewfinder image on the rectangle.
                        ViewfinderRectangle.Fill = _videoRecorderBrush;

                        // Set the button state and the message.
                        UpdateUi(ButtonState.Initialized, "Tap record to start recording...");
                    }
                    else
                    {
                        // Disable buttons when the camera is not supported by the device.
                        UpdateUi(ButtonState.CameraNotSupported, "A camera is not supported on this device.");
                    }
                }
            }
            catch (Exception)
            {
                if (_videoCaptureDevice != null)
                    _videoCaptureDevice.Dispose();

                // Disable buttons when the camera is not supported by the device.
                UpdateUi(ButtonState.CameraNotSupported, "Camera malfunction occurred.");
            }
        }

        // Update the buttons and text on the UI thread based on app state.
        private void UpdateUi(ButtonState currentButtonState, string statusMessage)
        {
            // Run code on the UI thread.
            Dispatcher.BeginInvoke(delegate
            {

                switch (currentButtonState)
                {
                    // When the camera is not supported by the device.
                    case ButtonState.CameraNotSupported:
                        StartRecording.IsEnabled = false;
                        StopPlaybackRecording.IsEnabled = false;
                        StartPlayback.IsEnabled = false;
                        SavePlayback.IsEnabled = false;
                        break;

                    // First launch of the application, so no video is available.
                    case ButtonState.Initialized:
                        StartRecording.IsEnabled = true;
                        StopPlaybackRecording.IsEnabled = false;
                        StartPlayback.IsEnabled = false;
                        SavePlayback.IsEnabled = false;
                        break;

                    // Ready to record, so video is available for viewing.
                    case ButtonState.Ready:
                        StartRecording.IsEnabled = true;
                        StopPlaybackRecording.IsEnabled = false;
                        StartPlayback.IsEnabled = true;
                        SavePlayback.IsEnabled = true;
                        break;

                    // Video recording is in progress.
                    case ButtonState.Recording:
                        StartRecording.IsEnabled = false;
                        StopPlaybackRecording.IsEnabled = true;
                        StartPlayback.IsEnabled = false;
                        SavePlayback.IsEnabled = false;
                        break;

                    // Video playback is in progress.
                    case ButtonState.Playback:
                        StartRecording.IsEnabled = false;
                        StopPlaybackRecording.IsEnabled = true;
                        StartPlayback.IsEnabled = false;
                        SavePlayback.IsEnabled = false;
                        break;

                    // Video playback has been paused.
                    case ButtonState.Paused:
                        StartRecording.IsEnabled = false;
                        StopPlaybackRecording.IsEnabled = true;
                        StartPlayback.IsEnabled = true;
                        SavePlayback.IsEnabled = false;
                        break;
                }

                // Display a message.
                txtDebug.Text = statusMessage;

                // Note the current application state.
                _currentAppState = currentButtonState;
            });
        }

        #endregion

        #region Helper Methods
        
        /// <summary>
        /// Used to limit video duration to 15 seconds
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void dt_Tick(object sender, EventArgs e)
        {
            // Stop recording.
            if (_currentAppState == ButtonState.Recording && _videoCaptureDevice != null)
            {
                await _videoCaptureDevice.StopRecordingAsync();
                _stream.AsStream().Dispose();
                _dt.Stop();

                // Set the button states and the message.
                UpdateUi(ButtonState.NoChange, "Preparing viewfinder...");

                StartVideoPreview();
            }
        }

        /// <summary>
        /// Saves a thumbnail of the video
        /// </summary>
        private void SaveThumbnail()
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                var w = (int)_videoCaptureDevice.PreviewResolution.Width;
                var h = (int)_videoCaptureDevice.PreviewResolution.Height;

                var argbPx = new Int32[w * h];

                _videoCaptureDevice.GetPreviewBufferArgb(argbPx);
                var wb = new WriteableBitmap(w, h);
                argbPx.CopyTo(wb.Pixels, 0);
                wb.Invalidate();

                using (var isoStore = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    var fileName = _isoVideoFileName + ".jpg";

                    if (isoStore.FileExists(fileName))
                        isoStore.DeleteFile(fileName);

                    var file = isoStore.CreateFile(fileName);
                    wb.SaveJpeg(file, w, h, 0, 20);
                    file.Close();
                }
            });
        }

        private void DisposeVideoPlayer()
        {
            if (VideoPlayer != null)
            {
                // Stop the VideoPlayer MediaElement.
                VideoPlayer.Stop();

                // Remove playback objects.
                VideoPlayer.Source = null;
                _isoVideoFile = null;

                // Remove the event handler.
                VideoPlayer.MediaEnded -= VideoPlayerMediaEnded;
            }
        }

        private void DisposeVideoRecorder()
        {
            if (_videoCaptureDevice != null)
            {
                // Remove the event handlers for capturesource and the shutter button.
                _videoCaptureDevice.RecordingFailed -= RecordingFailed;

                _videoCaptureDevice.Dispose();

                // Remove the video recording objects.
                _videoCaptureDevice = null;
                _videoRecorderBrush = null;
            }
        }

        #endregion

        #region Recroding Methods

        // Start the video recording.
        private void StartRecording_Click(object sender, EventArgs e)
        {
            // Avoid duplicate taps.
            StartRecording.IsEnabled = false;
            StartVideoRecording();
        }

        // Set recording state: start recording.
        private async void StartVideoRecording()
        {
            try
            {
                // Connect fileSink to captureSource.
                if (_videoCaptureDevice != null)
                {
                    if (_currentAppState == ButtonState.Ready)
                    {
                        // Create the VideoBrush for the viewfinder.
                        _videoRecorderBrush = new VideoBrush();
                        _videoRecorderBrush.SetSource(_videoCaptureDevice);

                        // Display the viewfinder image on the rectangle.
                        ViewfinderRectangle.Fill = _videoRecorderBrush;
                    }

                    if (_currentAppState != ButtonState.Initialized)
                    {
                        _videoRecorderBrush = new VideoBrush();
                        _videoRecorderBrush.SetSource(_videoCaptureDevice);

                        // Display the viewfinder image on the rectangle.
                        ViewfinderRectangle.Fill = _videoRecorderBrush;

                        _stream = await _outputFile.OpenAsync(FileAccessMode.ReadWrite);
                        await _stream.FlushAsync();
                    }

                    await _videoCaptureDevice.StartRecordingToStreamAsync(_stream);
                    _dt.Start();
                    SaveThumbnail();
                }

                // Set the button states and the message.
                UpdateUi(ButtonState.Recording, "Recording...");
            }
            // If recording fails, display an error.
            catch (Exception e)
            {
                Dispatcher.BeginInvoke(delegate
                {
                    txtDebug.Text = "ERROR1: " + e.Message;
                });
            }
        }

        // Handle stop requests.
        private void StopPlaybackRecording_Click(object sender, EventArgs e)
        {
            // Avoid duplicate taps.
            StopPlaybackRecording.IsEnabled = false;

            // Stop during video recording.
            if (_currentAppState == ButtonState.Recording)
            {
                StopVideoRecording();
                // Set the button state and the message.
                UpdateUi(ButtonState.NoChange, "Recording stopped.");
            }

            // Stop during video playback.
            else
            {
                // Remove playback objects.
                DisposeVideoPlayer();
                StartVideoPreview();

                // Set the button state and the message.
                UpdateUi(ButtonState.NoChange, "Playback stopped.");
            }
        }

        // Set the recording state: stop recording.
        private async void StopVideoRecording()
        {
            try
            {
                // Stop recording.
                if (_videoCaptureDevice != null)
                {
                    await _videoCaptureDevice.StopRecordingAsync();
                    _stream.AsStream().Dispose();
                    _dt.Stop();

                    // Set the button states and the message.
                    UpdateUi(ButtonState.NoChange, "Preparing viewfinder...");

                    StartVideoPreview();
                }
            }
            // If stop fails, display an error.
            catch (Exception e)
            {
                Dispatcher.BeginInvoke(delegate
                {
                    txtDebug.Text = "ERROR: " + e.Message;
                });
            }
        }
        
        #endregion

        #region PlayBack Methods
        
        // Set the recording state: display the video on the viewfinder.
        private void StartVideoPreview()
        {
            try
            {
                // Display the video on the viewfinder.
                if (_videoCaptureDevice != null)
                {
                    // Add captureSource to videoBrush.
                    var isoVideoThumbnailFile = new IsolatedStorageFileStream(_isoVideoFileName + ".jpg",
                                                              FileMode.Open, FileAccess.Read,
                                                              IsolatedStorageFile.GetUserStoreForApplication());

                    var image = new BitmapImage();
                    var myBrush = new ImageBrush();
                    image.SetSource(isoVideoThumbnailFile);
                    isoVideoThumbnailFile.Close();
                    myBrush.ImageSource = image;

                    // Add videoBrush to the visual tree.
                    ViewfinderRectangle.Fill = myBrush;

                    // Set the button states and the message.
                    UpdateUi(ButtonState.Ready, "Ready to record.");
                }
            }
            // If preview fails, display an error.
            catch (Exception e)
            {
                Dispatcher.BeginInvoke(delegate
                {
                    txtDebug.Text = "ERROR: " + e.Message;
                });
            }
        }

        // Start video playback.
        private void StartPlayback_Click(object sender, EventArgs e)
        {
            // Avoid duplicate taps.
            StartPlayback.IsEnabled = false;

            // Start video playback when the file stream exists.
            if (_isoVideoFile != null)
            {
                VideoPlayer.Play();
            }
            // Start the video for the first time.
            else
            {
                // Remove VideoBrush from the tree.
                ViewfinderRectangle.Fill = null;

                // Create the file stream and attach it to the MediaElement.
                _isoVideoFile = new IsolatedStorageFileStream(_isoVideoFileName,
                                                              FileMode.Open, FileAccess.Read,
                                                              IsolatedStorageFile.GetUserStoreForApplication());

                VideoPlayer.SetSource(_isoVideoFile);

                // Add an event handler for the end of playback.
                VideoPlayer.MediaEnded += VideoPlayerMediaEnded;

                // Start video playback.
                VideoPlayer.Play();
            }

            // Set the button state and the message.
            UpdateUi(ButtonState.Playback, "Playback started.");
        }

        #endregion

        // Save video playback.
        private void SavePlayback_Click(object sender, EventArgs e)
        {
            // Avoid duplicate taps.
            SavePlayback.IsEnabled = false;

            if (_isoVideoFileName != null)
            {
                App.ViewModel.AddAttachment(null, _isoVideoFileName, RayzItAttachment.ContentType.Video);
            }

            NavigationService.GoBack();
        }

        #region AudioVideoRecorder / VideoPlay Event Handlers

        void RecordingFailed(AudioVideoCaptureDevice sender, CaptureFailedEventArgs args)
        {
            Dispatcher.BeginInvoke(delegate
            {
                txtDebug.Text = "ERROR: " + args.ErrorCode;
            });
        }

        // Display the viewfinder when playback ends.
        public void VideoPlayerMediaEnded(object sender, RoutedEventArgs e)
        {
            // Remove the playback objects.
            DisposeVideoPlayer();
            StartVideoPreview();
        }

        #endregion

        #region Page Overrides
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            // Dispose of camera and media objects.
            DisposeVideoPlayer();
            DisposeVideoRecorder();

            base.OnNavigatedFrom(e);
        }

        protected override void OnRemovedFromJournal(JournalEntryRemovedEventArgs e)
        {
            _dt.Stop();
        }
        #endregion
    }
}