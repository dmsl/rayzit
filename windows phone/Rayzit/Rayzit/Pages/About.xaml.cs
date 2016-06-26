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