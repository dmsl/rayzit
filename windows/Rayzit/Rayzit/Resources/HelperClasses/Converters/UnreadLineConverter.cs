using System;
using System.Globalization;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class UnreadLineConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var i = (int) value;

            if (i == 1)
                return "#FFF3A927";

            if (i == 2)
                return "#FF0056AC";

            return "#FF7A7A7A";
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return 1;
        }
    }
}