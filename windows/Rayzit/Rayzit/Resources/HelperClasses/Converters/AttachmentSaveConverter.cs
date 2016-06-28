using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using RayzitServiceClient.HelperClasses;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class AttachmentSaveConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var type = (RayzItAttachment.ContentType)value;

            switch (type)
            {
                case RayzItAttachment.ContentType.Image:
                    return Visibility.Visible;
                default:
                    return Visibility.Collapsed;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return Visibility.Collapsed;
        }
    }
}
