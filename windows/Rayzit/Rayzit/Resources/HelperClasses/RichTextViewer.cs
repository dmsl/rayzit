using System;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Media;

namespace Rayzit.Resources.HelperClasses
{
    public class RichTextViewer : RichTextBox
    {
        private static readonly Regex UrlRegex = new Regex(@"(http|https|ftp|)\://|[a-zA-Z0-9\-\.]+\.[a-zA-Z](:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\-\._\?\,\'/\\\+&amp;%\$#\=~])*[^\.\,\)\(\s]");

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(RichTextViewer), new PropertyMetadata(default(string), TextPropertyChanged));

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        private static void TextPropertyChanged(DependencyObject dependencyObject, DependencyPropertyChangedEventArgs dependencyPropertyChangedEventArgs)
        {
            var textBox = dependencyObject as RichTextViewer;
            if (textBox == null)
                return;

            textBox.Blocks.Clear();

            var newText = (string)dependencyPropertyChangedEventArgs.NewValue;
            if (String.IsNullOrEmpty(newText))
                return;

            var paragraph = new Paragraph();
            try
            {
                int lastPos = 0;
                foreach (Match match in UrlRegex.Matches(newText))
                {
                    // Copy raw string from the last position up to the match
                    if (match.Index != lastPos)
                    {
                        string rawText = newText.Substring(lastPos, match.Index - lastPos);
                        paragraph.Inlines.Add(rawText);
                    }

                    // Add matched url
                    var rawUrl = match.Value;

                    var isUri = Uri.IsWellFormedUriString(rawUrl, UriKind.RelativeOrAbsolute);

                    Uri uri;
                    if (!Uri.TryCreate(rawUrl, UriKind.Absolute, out uri) && isUri)
                    {
                        // Attempt to craft a valid url
                        if (!rawUrl.StartsWith("http://"))
                        {
                            Uri.TryCreate("http://" + rawUrl, UriKind.Absolute, out uri);
                        }
                    }
                    if (uri != null)
                    {
                        var link = new Hyperlink
                        {
                            NavigateUri = uri,
                            TargetName = "_blank",
                            Foreground = Application.Current.Resources["PhoneAccentBrush"] as Brush
                        };
                        link.Inlines.Add(rawUrl);
                        paragraph.Inlines.Add(link);
                    }
                    else
                    {
                        paragraph.Inlines.Add(rawUrl);
                    }

                    // Update the last matched position
                    lastPos = match.Index + match.Length;
                }

                // Finally, copy the remainder of the string
                if (lastPos < newText.Length)
                    paragraph.Inlines.Add(newText.Substring(lastPos));
            }
            catch (Exception)
            {
                paragraph.Inlines.Clear();
                paragraph.Inlines.Add(newText);
            }

            // Add the paragraph to the RichTextBox.
            textBox.Blocks.Add(paragraph);
        }
    }
}