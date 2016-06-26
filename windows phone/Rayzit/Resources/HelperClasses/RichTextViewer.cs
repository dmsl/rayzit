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