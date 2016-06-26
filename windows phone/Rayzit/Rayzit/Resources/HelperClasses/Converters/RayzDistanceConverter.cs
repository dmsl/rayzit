using System;
using System.Globalization;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class RayzDistanceConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var distance = (Int64)value;

            switch (distance)
            {
                case 0:
                    return "unlimited";
                case 500:
                    return App.Settings.MetricListBoxSetting == 0 ? "0.5 km" : "0.3 miles";
                case 5000:
                    return App.Settings.MetricListBoxSetting == 0 ? "5 km" : "3 miles";
                case 50000:
                    return App.Settings.MetricListBoxSetting == 0 ? "50 km" : "30 miles";
                case 500000:
                    return App.Settings.MetricListBoxSetting == 0 ? "500 km" : "300 miles";
                case 5000000:
                    return App.Settings.MetricListBoxSetting == 0 ? "5000 km" : "3000 miles";
                default:
                    return "limited";
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return "unknown";
        }
    }
}
