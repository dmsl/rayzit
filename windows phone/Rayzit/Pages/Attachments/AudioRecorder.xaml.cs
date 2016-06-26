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
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Threading;
using Microsoft.Phone.Shell;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using RayzitServiceClient.HelperClasses;

namespace Rayzit.Pages.Attachments
{
    public partial class AudioRecorder
    {
        private readonly Microphone _microphone = Microphone.Default;       // Object representing the physical microphone on the device
        private byte[] _buffer;                                             // Dynamic buffer to retrieve audio data from the microphone
        private readonly MemoryStream _stream = new MemoryStream();         // Stores the audio data for later playback
        private SoundEffectInstance _soundInstance;                         // Used to play back audio
        private bool _soundIsPlaying;                                       // Flag to monitor the state of sound playback

        Random random = new Random();

        // Status images
        private readonly BitmapImage _blankImage;
        private readonly BitmapImage _microphoneImage;
        private readonly BitmapImage _speakerImage;

        private readonly BitmapImage _microphoneImage1;
        private readonly BitmapImage _microphoneImage2;
        private readonly BitmapImage _microphoneImage3;
        private readonly BitmapImage _microphoneImage4;
        private readonly BitmapImage _microphoneImage5;
        private readonly BitmapImage _microphoneImage6;
        private readonly BitmapImage _microphoneImage7;
        private readonly BitmapImage _microphoneImage8;
        private readonly BitmapImage _microphoneImage9;
        private readonly BitmapImage _microphoneImage10;

        private readonly DispatcherTimer _dt;
        private readonly DispatcherTimer _dt2;

        public AudioRecorder()
        {
            InitializeComponent();

            // Timer to simulate the XNA Framework game loop (Microphone is 
            // from the XNA Framework). We also use this timer to monitor the 
            // state of audio playback so we can update the UI appropriately.
            _dt = new DispatcherTimer {Interval = TimeSpan.FromMilliseconds(33)};
            _dt2 = new DispatcherTimer { Interval = TimeSpan.FromSeconds(15) };
            _dt.Tick += dt_Tick;
            _dt2.Tick += dt2_Tick;
            _dt.Start();

            // Event handler for getting audio data when the buffer is full
            _microphone.BufferReady += microphone_BufferReady;

            _blankImage = new BitmapImage(new Uri("/Assets/Attachments/blank.png", UriKind.RelativeOrAbsolute));
            _microphoneImage = new BitmapImage(new Uri("/Assets/Attachments/microphone.png", UriKind.RelativeOrAbsolute));
            _speakerImage = new BitmapImage(new Uri("/Assets/Attachments/speaker.png", UriKind.RelativeOrAbsolute));
            _microphoneImage1 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_1.png", UriKind.RelativeOrAbsolute));
            _microphoneImage2 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_2.png", UriKind.RelativeOrAbsolute));
            _microphoneImage3 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_3.png", UriKind.RelativeOrAbsolute));
            _microphoneImage4 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_4.png", UriKind.RelativeOrAbsolute));
            _microphoneImage5 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_5.png", UriKind.RelativeOrAbsolute));
            _microphoneImage6 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_6.png", UriKind.RelativeOrAbsolute));
            _microphoneImage7 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_7.png", UriKind.RelativeOrAbsolute));
            _microphoneImage8 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_8.png", UriKind.RelativeOrAbsolute));
            _microphoneImage9 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_9.png", UriKind.RelativeOrAbsolute));
            _microphoneImage10 = new BitmapImage(new Uri("/Assets/Attachments/Microphone Levels/mic_level_10.png", UriKind.RelativeOrAbsolute));
        }

        /// <summary>
        /// Updates the XNA FrameworkDispatcher and checks to see if a sound is playing.
        /// If sound has stopped playing, it updates the UI.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void dt_Tick(object sender, EventArgs e)
        {
            try { FrameworkDispatcher.Update(); }
            catch
            { }

            if (_soundIsPlaying)
            {
                if (_soundInstance.State != SoundState.Playing)
                {
                    // Audio has finished playing
                    _soundIsPlaying = false;

                    // Update the UI to reflect that the 
                    // sound has stopped playing
                    SetButtonStates(true, true, false, true);
                    UserHelp.Text = "press play or save to exit";
                    StatusImage.Source = _blankImage;
                }
            }
        }

        void dt2_Tick(object sender, EventArgs e)
        {
            if (_microphone.State == MicrophoneState.Started)
            {
                // In RECORD mode, user clicked the 
                // stop button to end recording
                _dt2.Stop();
                _microphone.Stop();
                UpdateWavHeader(_stream);
            }

            SetButtonStates(true, true, false, true);
            UserHelp.Text = "click play to listen or save to exit";
            StatusImage.Source = _blankImage;
        }

        protected override void OnRemovedFromJournal(JournalEntryRemovedEventArgs e)
        {
            _dt.Stop();
            _dt2.Stop();
        }

        /// <summary>
        /// The Microphone.BufferReady event handler.
        /// Gets the audio data from the microphone and stores it in a buffer,
        /// then writes that buffer to a stream for later playback.
        /// Any action in this event handler should be quick!
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void microphone_BufferReady(object sender, EventArgs e)
        {
            // Retrieve audio data
            _microphone.GetData(_buffer);

            GetSampleRate(_buffer);

            // Store the audio data in a stream
            _stream.Write(_buffer, 0, _buffer.Length);
        }

        private async void GetSampleRate(IList<byte> buffer)
        {
            await Task.Run(() =>
                {
                    float max = 0;

                    for (var index = 0; index < buffer.Count; index += 2)
                    {
                        var sample = (short)((buffer[index + 1] << 8) | buffer[index + 0]);
                        var sample32 = (sample / 32768f) * 10;
                        if (sample32 > max)
                            max = sample32;
                    }

                    UpdateMicrophone(max);
                });
        }

        private void UpdateMicrophone(float value)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() => 
            {
                var upper = (int)Math.Round(value + random.Next(0, 2));

                switch (upper)
                {
                    case 2:
                        StatusImage.Source = _microphoneImage2;
                        break;
                    case 3:
                        StatusImage.Source = _microphoneImage3;
                        break;
                    case 4:
                        StatusImage.Source = _microphoneImage4;
                        break;
                    case 5:
                        StatusImage.Source = _microphoneImage5;
                        break;
                    case 6:
                        StatusImage.Source = _microphoneImage6;
                        break;
                    case 7:
                        StatusImage.Source = _microphoneImage7;
                        break;
                    case 8:
                        StatusImage.Source = _microphoneImage8;
                        break;
                    case 9:
                        StatusImage.Source = _microphoneImage9;
                        break;
                    case 10:
                        StatusImage.Source = _microphoneImage10;
                        break;
                    default:
                        StatusImage.Source = _microphoneImage1;
                        break;
                }
            });
        }

        /// <summary>
        /// Handles the Click event for the record button.
        /// Sets up the microphone and data buffers to collect audio data,
        /// then starts the microphone. Also, updates the UI.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void recordButton_Click(object sender, EventArgs e)
        {
            // Get audio data in 1/2 second chunks
            _microphone.BufferDuration = TimeSpan.FromMilliseconds(500);

            // Allocate memory to hold the audio data
            _buffer = new byte[_microphone.GetSampleSizeInBytes(_microphone.BufferDuration)];

            // Set the stream back to zero in case there is already something in it
            _stream.SetLength(0);

            WriteWavHeader(_stream, _microphone.SampleRate);

            // Start recording
            _microphone.Start();
            _dt2.Start();

            SetButtonStates(false, false, true, false);
            UserHelp.Text = "start talking";
            StatusImage.Source = _microphoneImage;
        }

        /// <summary>
        /// Handles the Click event for the stop button.
        /// Stops the microphone from collecting audio and updates the UI.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void stopButton_Click(object sender, EventArgs e)
        {
            if (_microphone.State == MicrophoneState.Started)
            {
                // In RECORD mode, user clicked the 
                // stop button to end recording
                _dt2.Stop();
                _microphone.Stop();
                UpdateWavHeader(_stream);
            }
            else if (_soundInstance.State == SoundState.Playing)
            {
                // In PLAY mode, user clicked the 
                // stop button to end playing back
                _soundInstance.Stop();
            }

            SetButtonStates(true, true, false, true);
            UserHelp.Text = "click play to listen or save to exit";
            StatusImage.Source = _blankImage;
        }

        /// <summary>
        /// Handles the Click event for the play button.
        /// Plays the audio collected from the microphone and updates the UI.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void playButton_Click(object sender, EventArgs e)
        {
            if (_stream.Length > 0)
            {
                // Update the UI to reflect that
                // sound is playing
                SetButtonStates(false, false, true, false);
                UserHelp.Text = "playing recorded message";
                StatusImage.Source = _speakerImage;

                // Play the audio in a new thread so the UI can update.
                var soundThread = new Thread(PlaySound);
                soundThread.Start();
            }
        }

        /// <summary>
        /// Handles the Click event for the save button.
        /// Saves the audio collected from the microphone and sends it to the caller.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void saveButton_Click(object sender, EventArgs e)
        {
            if (_stream != null)
            {
                var msec = (DateTime.Now - new DateTime(1970, 1, 1, 0, 0, 0, 0).ToLocalTime()).TotalMilliseconds;

                App.ViewModel.AddAttachment(_stream, "audio_" + msec + ".wav", RayzItAttachment.ContentType.Audio);
            }

            NavigationService.GoBack();
        }

        public void UpdateWavHeader(Stream stream)
        {
            if (!stream.CanSeek) throw new Exception("Can't seek stream to update wav header");

            var oldPos = stream.Position;

            // ChunkSize 36 + SubChunk2Size
            stream.Seek(4, SeekOrigin.Begin);
            stream.Write(BitConverter.GetBytes((int)stream.Length - 8), 0, 4);

            // Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8 This is the number of bytes in the data.
            stream.Seek(40, SeekOrigin.Begin);
            stream.Write(BitConverter.GetBytes((int)stream.Length - 44), 0, 4);

            stream.Seek(oldPos, SeekOrigin.Begin);
        }

        public void WriteWavHeader(Stream stream, int sampleRate)
        {
            const int bitsPerSample = 16;
            const int bytesPerSample = bitsPerSample / 8;
            var encoding = System.Text.Encoding.UTF8;

            // ChunkID Contains the letters "RIFF" in ASCII form (0x52494646 big-endian form).
            stream.Write(encoding.GetBytes("RIFF"), 0, 4);

            // NOTE this will be filled in later
            stream.Write(BitConverter.GetBytes(0), 0, 4);

            // Format Contains the letters "WAVE"(0x57415645 big-endian form).
            stream.Write(encoding.GetBytes("WAVE"), 0, 4);

            // Subchunk1ID Contains the letters "fmt " (0x666d7420 big-endian form).
            stream.Write(encoding.GetBytes("fmt "), 0, 4);

            // Subchunk1Size 16 for PCM. This is the size of therest of the Subchunk which follows this number.
            stream.Write(BitConverter.GetBytes(16), 0, 4);

            // AudioFormat PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression.
            stream.Write(BitConverter.GetBytes((short)1), 0, 2);

            // NumChannels Mono = 1, Stereo = 2, etc.
            stream.Write(BitConverter.GetBytes((short)1), 0, 2);

            // SampleRate 8000, 44100, etc.
            stream.Write(BitConverter.GetBytes(sampleRate), 0, 4);

            // ByteRate = SampleRate * NumChannels * BitsPerSample/8
            stream.Write(BitConverter.GetBytes(sampleRate * bytesPerSample), 0, 4);

            // BlockAlign NumChannels * BitsPerSample/8 The number of bytes for one sample including all channels.
            stream.Write(BitConverter.GetBytes((short)(bytesPerSample)), 0, 2);

            // BitsPerSample 8 bits = 8, 16 bits = 16, etc.
            stream.Write(BitConverter.GetBytes((short)(bitsPerSample)), 0, 2);

            // Subchunk2ID Contains the letters "data" (0x64617461 big-endian form).
            stream.Write(encoding.GetBytes("data"), 0, 4);

            // NOTE to be filled in later
            stream.Write(BitConverter.GetBytes(0), 0, 4);
        }

        /// <summary>
        /// Plays the audio using SoundEffectInstance 
        /// so we can monitor the playback status.
        /// </summary>
        private void PlaySound()
        {
            // Play audio using SoundEffectInstance so we can monitor it's State 
            // and update the UI in the dt_Tick handler when it is done playing.
            var sound = new SoundEffect(_stream.ToArray(), _microphone.SampleRate, AudioChannels.Mono);
            _soundInstance = sound.CreateInstance();
            _soundIsPlaying = true;
            _soundInstance.Play();
        }

        /// <summary>
        /// Helper method to change the IsEnabled property for the ApplicationBarIconButtons.
        /// </summary>
        /// <param name="recordEnabled">New state for the record button.</param>
        /// <param name="playEnabled">New state for the play button.</param>
        /// <param name="stopEnabled">New state for the stop button.</param>
        /// <param name="saveEnabled"></param>
        private void SetButtonStates(bool recordEnabled, bool playEnabled, bool stopEnabled, bool saveEnabled)
        {
            (ApplicationBar.Buttons[0] as ApplicationBarIconButton).IsEnabled = recordEnabled;
            (ApplicationBar.Buttons[1] as ApplicationBarIconButton).IsEnabled = playEnabled;
            (ApplicationBar.Buttons[2] as ApplicationBarIconButton).IsEnabled = stopEnabled;
            (ApplicationBar.Buttons[3] as ApplicationBarIconButton).IsEnabled = saveEnabled;
        }
    }
}