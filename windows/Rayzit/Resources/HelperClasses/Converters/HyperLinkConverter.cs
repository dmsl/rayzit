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
using System.Collections.Generic;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using Microsoft.Phone.Tasks;

namespace Rayzit.Resources.HelperClasses.Converters
{
    public class HyperLinkConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var message = (string)value;

            var textBlock = new RichTextBox()
            {
                TextWrapping = TextWrapping.Wrap,
                IsReadOnly = true,
            };

            var paragraph = new Paragraph();

            var runs = new List<Inline>();

            foreach (var word in message.Split(' '))
            {
                Uri uri;

                if (Uri.TryCreate(word, UriKind.Absolute, out uri) && uri.Scheme == Uri.UriSchemeHttp)
                {
                    var link = new Hyperlink();
                    link.Inlines.Add(new Run() { Text = word });
                    link.Click += (sender2, e2) =>
                    {
                        var hyperLink = (sender2 as Hyperlink);
                        new WebBrowserTask() { Uri = uri }.Show();
                    };

                    runs.Add(link);
                }
                else
                {
                    runs.Add(new Run() { Text = word });
                }

                runs.Add(new Run() { Text = " " });
            }

            foreach (var run in runs)
                paragraph.Inlines.Add(run);

            textBlock.Blocks.Add(paragraph);

            return textBlock.DataContext;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return 1;
        }
    }
}
