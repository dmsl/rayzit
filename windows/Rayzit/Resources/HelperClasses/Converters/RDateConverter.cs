/**
 * Copyright (c) 2016 Data Management Systems Laboratory, University of Cyprus
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

//
//  Rayzit
//
//  Created by COSTANTINOS COSTA - GEORGE NIKOLAIDES.
//  Copyright (c) 2014 DMSL. All rights reserved.
//

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
