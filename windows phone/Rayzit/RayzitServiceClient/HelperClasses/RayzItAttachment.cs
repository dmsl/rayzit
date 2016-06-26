namespace RayzitServiceClient.HelperClasses
{
    public class RayzItAttachment
    {
        public enum ContentType
        {
            Image,
            Video,
            Audio
        }

        public byte[] FileBody { get; set; }
        public string ContType { get; set; }

        public RayzItAttachment(byte[] bytes, ContentType contType)
        {
            FileBody = bytes;
            switch (contType)
            {
                case ContentType.Image:
                    ContType = "image/jpeg";
                    break;
                case ContentType.Video:
                    ContType = "video/mp4";
                    break;
                case ContentType.Audio:
                    ContType = "audio/wav";
                    break;
            }
        }
    }
}
