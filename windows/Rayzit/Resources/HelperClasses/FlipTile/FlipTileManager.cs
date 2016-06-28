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
using System.IO.IsolatedStorage;
using System.Linq;
using System.Windows;
using Microsoft.Phone.Shell;

namespace Rayzit.Resources.HelperClasses.FlipTile
{
    public class FlipTileManager
    {
        public static void ClearFlipTile()
        {
            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            var oFliptile = new FlipTileData
            {
                Count = 0,
                BackTitle = String.Empty,
                BackContent = String.Empty,
                WideBackContent = String.Empty,
                BackBackgroundImage = null,
                WideBackBackgroundImage = null,
            };

            oTile.Update(oFliptile);
        }

        public static void UpdateFlipTile(String message, int count, bool hasImage)
        {
            if (!App.Settings.LiveTileToggleSwitchSetting)
                return;

            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            if (hasImage)
                UpdateFlipTileWithImage(message, count);
            else
                UpdateFlipTileWithoutImage(message, count);
        }

        private static void UpdateFlipTileWithImage(String message, int count)
        {
            if (!App.Settings.LiveTileToggleSwitchSetting)
                return;

            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            var oFliptile = new FlipTileData
            {
                Count = count,
                BackContent = message,
                WideBackContent = message,
                BackBackgroundImage = new Uri("isostore:/Shared/ShellContent/tile.jpg", UriKind.Absolute),
                WideBackBackgroundImage = new Uri("isostore:/Shared/ShellContent/tile.jpg", UriKind.Absolute)
            };
            oTile.Update(oFliptile);
        }

        private static void UpdateFlipTileWithoutImage(String message, int count)
        {
            if (!App.Settings.LiveTileToggleSwitchSetting)
                return;

            var oTile = ShellTile.ActiveTiles.First();

            if (oTile == null)
                return;

            var oFliptile = new FlipTileData
            {
                Count = count,
                BackContent = message,
                WideBackContent = message,
                BackBackgroundImage = new Uri("", UriKind.Relative),
                WideBackBackgroundImage = new Uri("", UriKind.Relative)
            };

            oTile.Update(oFliptile);
        }

        public static void SetFlipTileImage(byte[] image)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                const string fileName = "/Shared/ShellContent/tile.jpg";

                using (
                    var myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication()
                    )
                {
                    if (myIsolatedStorage.FileExists(fileName))
                    {
                        myIsolatedStorage.DeleteFile(fileName);
                    }

                    var fileStream = myIsolatedStorage.CreateFile(fileName);

                    fileStream.Write(image, 0, image.Length);
                    fileStream.Close();
                }
            });
        }
    }
}
