using System;
using System.Globalization;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class RDateConverterBg : IValueConverter
    {
        public object Convert(object date, Type targetType, object parameter, CultureInfo culture)
        {
            if ((Int64)date == 1)
                return "#B2EC3E23";

            return "#B2363636";
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return -1;
        }
    }
}
