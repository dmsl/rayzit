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
using System.Windows.Navigation;

namespace Rayzit.Resources.UriMapper
{
    class CustomUriMapper : UriMapperBase
    {
        public override Uri MapUri(Uri uri)
        {
            var tempUri = uri.ToString();
            string mappedUri;

            // Launch from the photo share picker.
            // Incoming URI example: /MainPage.xaml?Action=ShareContent&FileId=%7BA3D54E2D-7977-4E2B-B92D-3EB126E5D168%7D
            if ((tempUri.Contains("ShareContent")) && (tempUri.Contains("FileId")))
            {
                // Redirect to NewRayz.xaml.
                mappedUri = tempUri.Replace("MainPage", "Pages/NewRayz");
                return new Uri(mappedUri, UriKind.Relative);
            }
            // Incoming URI example: /MainPage.xaml?Action=RichMediaEdit&token=%7Bd72d036e-3733-4f24-97b8-28b348c85b05%7D
            if ((tempUri.Contains("RichMediaEdit")) && (tempUri.Contains("token")))
            {
                // Redirect to NewRayz.xaml.
                mappedUri = tempUri.Replace("MainPage", "Pages/NewRayz");
                return new Uri(mappedUri, UriKind.Relative);
            }

            // Otherwise perform normal launch.
            return uri;
        }
    }
}
