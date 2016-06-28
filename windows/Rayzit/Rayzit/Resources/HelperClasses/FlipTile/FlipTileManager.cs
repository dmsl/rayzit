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
