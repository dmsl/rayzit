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
using System.Windows.Input;
using System.Xml.Linq;
using Microsoft.Phone.Tasks;

namespace Rayzit.Pages
{
    public partial class About
    {
        public About()
        {
            InitializeComponent();

            UpdateVersion();
        }

        private void UpdateVersion()
        {
            var ver = XDocument.Load("WMAppManifest.xml").Root.Element("App").Attribute("Version").Value;
            AppVersion.Text = ver;
        }

        private void DMSL_Logo_OnClick(object sender, MouseButtonEventArgs e)
        {
            var wbtask = new WebBrowserTask { Uri = new Uri("http://dmsl.cs.ucy.ac.cy/", UriKind.Absolute) };
            wbtask.Show();
        }

        private void UCY_Logo_OnClick(object sender, MouseButtonEventArgs e)
        {
            var wbtask = new WebBrowserTask { Uri = new Uri("http://www.ucy.ac.cy/", UriKind.Absolute) };
            wbtask.Show();
        }
    }
}