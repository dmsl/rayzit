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
