using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class ReadUnreadRayzConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (int)value == 0 ? "#404041" : Application.Current.Resources["PhoneAccentBrush"];
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return 1;
        }

    }
}
