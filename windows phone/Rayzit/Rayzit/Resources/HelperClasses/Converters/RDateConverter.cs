using System;
using System.Globalization;
using System.Windows.Data;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class RDateConverter : IValueConverter
    {
        public object Convert(object date, Type targetType, object parameter, CultureInfo culture)
        {
            
            if ((Int64)date == 0)
                return "Sending ...";

            if ((Int64)date == 1)
                return "Failed. Tap to retry.";

            var d = new DateTime(1970, 1, 1, 0, 0, 0, 0);
            d = d.AddMilliseconds((Int64)date);

            return ToRelativeTime(d.ToLocalTime(), DateTime.Now);
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return -1;
        }

        private static string ToRelativeTime(DateTime time, DateTime relativeTo)
        {
            var ts = relativeTo.Subtract(time).Duration();

            string dir;

            if (relativeTo >= time)
                dir = "ago";
            else
                return "Just now";

            if (ts.Days < 10)
            {
                // Days
                if (ts.Days > 0)
                {
                    if (ts.Days > 1)
                        return string.Format("{0} days {1}", ts.Days, dir);

                    return string.Format("{0} day {1}", ts.Days, dir);
                }

                // Hours
                if (ts.Hours > 0)
                {
                    if (ts.Hours > 1)
                        return string.Format("{0} hours {1}", ts.Hours, dir);

                    return string.Format("{0} hour {1}", ts.Hours, dir);
                }

                // Minutes
                if (ts.Minutes > 2)
                {
                    if (ts.Minutes > 1)
                        return string.Format("{0} minutes {1}", ts.Minutes, dir);

                    return string.Format("{0} minute {1}", ts.Minutes, dir); 
                }

                // Minutes
                if (ts.Minutes > 3)
                    return string.Format("a moment ago");

                // Seconds
                return string.Format("Just now");
            }

            return time.ToString("d MMM yyyy HH:mm", new CultureInfo("en-US"));
        }
    }
}
