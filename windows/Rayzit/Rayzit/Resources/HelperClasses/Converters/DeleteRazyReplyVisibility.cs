using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class DeleteRazyReplyVisibility : IValueConverter
    {
        public object Convert(object date, Type targetType, object parameter, CultureInfo culture)
        {
            return (Int64)date == 1 ? Visibility.Visible : Visibility.Collapsed;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return 1;
        }
    }
}

