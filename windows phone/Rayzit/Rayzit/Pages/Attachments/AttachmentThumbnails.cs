using System;
using System.IO;
using System.IO.IsolatedStorage;
using System.Threading.Tasks;
using System.Windows.Media.Imaging;
using RayzitServiceClient.HelperClasses;

namespace Rayzit.Pages.Attachments
{
    /// <summary>
    /// Class for the Attachments
    /// </summary>
    public class Attachment : IDisposable
    {
        private bool _disposed;

        ~Attachment()  // destructor
        {
            Dispose(false);
        }

        public void Dispose()
        {
            if (!_disposed)
            {
                Dispose(true);
                GC.SuppressFinalize(this);
                _disposed = true;
            }
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                ByteArray = null;
                FileName = null;
                if (ThumbImage != null)
                    ThumbImage.UriSource = null;
            }
        }

        /// <summary>
        /// Gets the FileName of the attachment
        /// </summary>
        public string FileName { get; private set; }

        /// <summary>
        /// Gets the type of the attachment
        /// </summary>
        public RayzItAttachment.ContentType Type { get; private set; }

        private BitmapImage _temp;

        public byte[] ByteArray { get; set; }

        /// <summary>
        /// Gets the local picture
        /// </summary>
        public BitmapImage ThumbImage
        {
            get
            {
                //WriteableBitmap bitmap;
                if (_temp != null)
                    return _temp;

                try
                {
                    using (var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                    {
                        using (var fileStream = myIsolatedStorage.OpenFile(FileName, FileMode.Open, FileAccess.Read))
                        {
                            switch (Type)
                            {
                                case RayzItAttachment.ContentType.Image:
                                    _temp = new BitmapImage();
                                    _temp.SetSource(fileStream);
                                    break;
                                case RayzItAttachment.ContentType.Audio:
                                    var one = new Uri("/Assets/Attachments/speaker.png", UriKind.RelativeOrAbsolute);
                                    _temp = new BitmapImage { UriSource = one };
                                    break;
                                case RayzItAttachment.ContentType.Video:
                                    using (var fileStream2 = myIsolatedStorage.OpenFile(FileName + ".jpg", FileMode.Open, FileAccess.Read))
                                    {
                                        _temp = new BitmapImage();
                                        _temp.SetSource(fileStream2);
                                    }
                                    break;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                }


                return _temp;
            }
            set { _temp = value; }
        }

        public String ThumbType
        {
            get
            {
                switch (Type)
                {
                    case RayzItAttachment.ContentType.Image:
                        return "/Assets/Attachments/image_indi.png";
                    case RayzItAttachment.ContentType.Audio:
                        return "/Assets/Attachments/audio_indi.png";
                    case RayzItAttachment.ContentType.Video:
                        return "/Assets/Attachments/video_indi.png";
                }
                return "";
            }
        }

        /// <summary>
        /// Constructs an Attachment object
        /// </summary>
        /// <param name="fileName"></param>
        /// <param name="type"></param>
        public Attachment(string fileName, RayzItAttachment.ContentType type)
        {
            FileName = fileName;
            Type = type;
        }

        public Attachment(byte[] bytes, string fileName, RayzItAttachment.ContentType type)
        {
            FileName = fileName;
            ByteArray = bytes;
            Type = type;
        }

        public Attachment(byte[] bytes, BitmapImage temp, string fileName, RayzItAttachment.ContentType type)
        {
            FileName = fileName;
            ByteArray = bytes;
            Type = type;
            _temp = temp;
        }
    }
}
