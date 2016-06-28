using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Media;
using Rayzit.ViewModels;
using RayzitServiceClient.HelperClasses;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace Rayzit.Pages.Attachments
{
    public partial class Attachments
    {
        public static Rayz RayzSelectedItem;
        public static RayzReply RayzReplySelectedItem;

        private SoundEffectInstance _soundInstance;

        bool _isPinch;

        public Attachments()
        {
            InitializeComponent();

            DataContext = App.ViewModel;
            Loaded += Attachments_Loaded;

            App.ViewModel.LoadAttachmentsCompleted += ViewModel_LoadAttachmentsCompleted;
        }

        async void Attachments_Loaded(object sender, RoutedEventArgs e)
        {
            if (RayzSelectedItem != null)
            {
                var prog = new ProgressIndicator { IsVisible = true, IsIndeterminate = true, Text = "Loading attachments..." };
                SystemTray.SetProgressIndicator(this, prog);
                await App.ViewModel.LoadAttachmentsFromRayz(RayzSelectedItem);
            }

            if (RayzReplySelectedItem != null)
            {
                var prog = new ProgressIndicator { IsVisible = true, IsIndeterminate = true, Text = "Loading attachments..." };
                SystemTray.SetProgressIndicator(this, prog);
                await App.ViewModel.LoadAttachmentsFromRayzReply(RayzReplySelectedItem);
            }
        }

        void ViewModel_LoadAttachmentsCompleted(object sender, bool e)
        {
            if (SystemTray.ProgressIndicator != null)
                SystemTray.ProgressIndicator.IsVisible = false;
        }

        protected override void OnBackKeyPress(CancelEventArgs e)
        {
            StopSound();

            if (ZoomGrid.Visibility == Visibility.Visible)
            {
                ZoomGrid.Visibility = Visibility.Collapsed;
                SystemTray.IsVisible = true;
                PageTitle.Visibility = Visibility.Visible;
                e.Cancel = true;
                return;
            }

            App.ViewModel.ClearAttachmentsList();

            RayzSelectedItem = null;
            RayzReplySelectedItem = null;
        }

        protected override void OnRemovedFromJournal(System.Windows.Navigation.JournalEntryRemovedEventArgs e)
        {
            App.ViewModel.LoadAttachmentsCompleted -= ViewModel_LoadAttachmentsCompleted;
        }

        /// <summary>
        /// Called when a picture is tapped : open it
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnPictureItemTap(object sender, GestureEventArgs e)
        {
            var fe = sender as FrameworkElement;
            if (fe != null)
            {
                var temp = fe.DataContext as Attachment;
                if (temp != null)
                    switch (temp.Type)
                    {
                        case RayzItAttachment.ContentType.Image:
                            StopSound();
                            CurrentPicture = temp;
                            ResetImagePosition();
                            SystemTray.IsVisible = false;
                            PageTitle.Visibility = Visibility.Collapsed;
                            break;
                        case RayzItAttachment.ContentType.Audio:
                            PlaySound(temp.ByteArray);
                            break;
                        case RayzItAttachment.ContentType.Video:
                            StopSound();
                            var mediaPlayerLauncher = new MediaPlayerLauncher
                                {
                                    Media = new Uri(temp.FileName, UriKind.Relative),
                                    Location = MediaLocationType.Data,
                                    Controls = MediaPlaybackControls.Pause | MediaPlaybackControls.Stop,
                                    Orientation = MediaPlayerOrientation.Landscape
                                };

                            mediaPlayerLauncher.Show();
                            break;
                    }
            }
        }

        /// <summary>
        /// Plays the audio using SoundEffectInstance 
        /// so we can monitor the playback status.
        /// </summary>
        private void PlaySound(byte[] stream)
        {
            StopSound();

            // Play audio using SoundEffectInstance so we can monitor it's State 
            // and update the UI in the dt_Tick handler when it is done playing.
            try
            {
                var sound = new SoundEffect(stream, 16000, AudioChannels.Mono);
                _soundInstance = sound.CreateInstance();
                _soundInstance.Play();
            }
            catch (Exception)
            {
                MessageBox.Show("Something went wrong. Could not play file.", "Oops,", MessageBoxButton.OK);
            }
        }

        private void StopSound()
        {
            if (_soundInstance != null)
            {
                _soundInstance.Stop();
                _soundInstance = null;
            }
        } 

        #region Context Menu Commands
        private void ContextMenu_Unloaded(object sender, RoutedEventArgs e)
        {
            var conmen = (sender as ContextMenu);
            if (conmen != null)
                conmen.ClearValue(DataContextProperty);
        }
        #endregion

        #region Picture display
        Attachment _currentPicture;

        /// <summary>
        /// Sets or gets the current displayed picture
        /// </summary>
        public Attachment CurrentPicture
        {
            set
            {
                _currentPicture = value;
                ZoomGrid.DataContext = _currentPicture;
                var hasPicture = (_currentPicture != null);
                ZoomGrid.Visibility = hasPicture ? Visibility.Visible : Visibility.Collapsed;
            }
            get { return _currentPicture; }
        }
        #endregion

        #region Image Viewer
        private void OnDragDelta(ManipulationDeltaEventArgs e)
        {
            ImageTransformation.CenterX = (ImageTransformation.CenterX - e.DeltaManipulation.Translation.X * 2);
            ImageTransformation.CenterY = (ImageTransformation.CenterY - e.DeltaManipulation.Translation.Y * 2);

            if (ImageTransformation.CenterX < 0)
                ImageTransformation.CenterX = 0;
            else if (ImageTransformation.CenterX > ZoomGrid.ActualWidth)
                ImageTransformation.CenterX = ZoomGrid.ActualWidth;

            if (ImageTransformation.CenterY < 0)
                ImageTransformation.CenterY = 0;
            else if (ImageTransformation.CenterY > ZoomGrid.ActualHeight)
                ImageTransformation.CenterY = ZoomGrid.ActualHeight;
        }

        private void OnPinchDelta(ManipulationDeltaEventArgs e)
        {
            var curZoom = ImageTransformation.ScaleX * e.PinchManipulation.CumulativeScale;

            // Disable zooming in more than double the size and zooming out less than original size
            if (!(curZoom >= 1.0) || !(curZoom <= 4.0))
                return;

            ImageTransformation.ScaleX *= e.PinchManipulation.CumulativeScale;
            ImageTransformation.ScaleY *= e.PinchManipulation.CumulativeScale;
        }

        /// <summary>
        /// Resets the zoom to its original scale and position
        /// </summary>
        private void ResetImagePosition()
        {
            ImageTransformation.ScaleX = 1.0;
            ImageTransformation.ScaleY = 1.0;
            ImageTransformation.CenterX = 0;
            ImageTransformation.CenterY = 0;
            ImageTransformation.TranslateX = 0;
            ImageTransformation.TranslateY = 0;
        }

        private void ZoomGrid_OnManipulationDelta(object sender, ManipulationDeltaEventArgs e)
        {
            var oldIsPinch = _isPinch;
            _isPinch = e.PinchManipulation != null;

            if (oldIsPinch == false && _isPinch == false)
            {
                OnDragDelta(e);
            }
            else if (oldIsPinch && _isPinch)
            {
                OnPinchDelta(e);
            }
        }

        private void ZoomImage_OnDoubleTap(object sender, GestureEventArgs e)
        {
            var zoomLevel = ImageTransformation.ScaleX;

            if (zoomLevel >= 1.0 && zoomLevel < 2.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 2.0;
            else if (zoomLevel >= 2.0 && zoomLevel < 3.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 3.0;
            else if (zoomLevel >= 3.0 && zoomLevel < 4.0)
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 4.0;
            else
                ImageTransformation.ScaleX = ImageTransformation.ScaleY = 1.0;
        }
        #endregion

        private void SaveAttachment(object sender, RoutedEventArgs e)
        {
            var attachment = ((MenuItem)sender).DataContext as Attachment;
            if (attachment == null)
                return;

            var library = new MediaLibrary();
            library.SavePicture(attachment.FileName, attachment.ByteArray);
        }
    }
}