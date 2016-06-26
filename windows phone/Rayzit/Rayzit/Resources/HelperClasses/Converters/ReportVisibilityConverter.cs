using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class ReportVisibilityConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var i = (bool)value;

            return i ? Visibility.Collapsed : Visibility.Visible;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return 1;
        }
    }
}
