﻿#pragma checksum "C:\Users\Costantinos\Source\Workspaces\Rayzit\Rayzit\Pages\NewRayz.xaml" "{406ea660-64cf-4c82-b6f0-42d48172a799}" "5E4A3EAD03FC50B8233D1B0308D2A00C"
//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using System;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Automation.Peers;
using System.Windows.Automation.Provider;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using System.Windows.Resources;
using System.Windows.Shapes;
using System.Windows.Threading;


namespace Rayzit.Pages {
    
    
    public partial class NewRayz : Microsoft.Phone.Controls.PhoneApplicationPage {
        
        internal System.Windows.DataTemplate LbOptionsItemTemplate;
        
        internal Microsoft.Phone.Shell.ApplicationBarIconButton SendBeamBeamsArroundSendAppBarButton;
        
        internal Microsoft.Phone.Shell.ApplicationBarIconButton AttachBeamsArroundSendAppBarButton;
        
        internal Microsoft.Phone.Shell.ApplicationBarIconButton DistanceSettingSendAppBarButton;
        
        internal System.Windows.Controls.Grid LayoutRoot;
        
        internal System.Windows.Controls.Grid ContentPanel;
        
        internal Microsoft.Phone.Controls.ListPicker DistanceLP;
        
        internal Microsoft.Phone.Controls.PhoneTextBox RayzTb;
        
        internal System.Windows.Controls.ListBox AttachmentsList;
        
        internal System.Windows.Controls.ProgressBar PowerBar;
        
        internal System.Windows.Controls.Grid ZoomGrid;
        
        internal System.Windows.Controls.Image ZoomImage;
        
        internal System.Windows.Media.CompositeTransform ImageTransformation;
        
        internal Microsoft.Phone.Controls.ListPicker AttachmentPicker;
        
        private bool _contentLoaded;
        
        /// <summary>
        /// InitializeComponent
        /// </summary>
        [System.Diagnostics.DebuggerNonUserCodeAttribute()]
        public void InitializeComponent() {
            if (_contentLoaded) {
                return;
            }
            _contentLoaded = true;
            System.Windows.Application.LoadComponent(this, new System.Uri("/Rayzit;component/Pages/NewRayz.xaml", System.UriKind.Relative));
            this.LbOptionsItemTemplate = ((System.Windows.DataTemplate)(this.FindName("LbOptionsItemTemplate")));
            this.SendBeamBeamsArroundSendAppBarButton = ((Microsoft.Phone.Shell.ApplicationBarIconButton)(this.FindName("SendBeamBeamsArroundSendAppBarButton")));
            this.AttachBeamsArroundSendAppBarButton = ((Microsoft.Phone.Shell.ApplicationBarIconButton)(this.FindName("AttachBeamsArroundSendAppBarButton")));
            this.DistanceSettingSendAppBarButton = ((Microsoft.Phone.Shell.ApplicationBarIconButton)(this.FindName("DistanceSettingSendAppBarButton")));
            this.LayoutRoot = ((System.Windows.Controls.Grid)(this.FindName("LayoutRoot")));
            this.ContentPanel = ((System.Windows.Controls.Grid)(this.FindName("ContentPanel")));
            this.DistanceLP = ((Microsoft.Phone.Controls.ListPicker)(this.FindName("DistanceLP")));
            this.RayzTb = ((Microsoft.Phone.Controls.PhoneTextBox)(this.FindName("RayzTb")));
            this.AttachmentsList = ((System.Windows.Controls.ListBox)(this.FindName("AttachmentsList")));
            this.PowerBar = ((System.Windows.Controls.ProgressBar)(this.FindName("PowerBar")));
            this.ZoomGrid = ((System.Windows.Controls.Grid)(this.FindName("ZoomGrid")));
            this.ZoomImage = ((System.Windows.Controls.Image)(this.FindName("ZoomImage")));
            this.ImageTransformation = ((System.Windows.Media.CompositeTransform)(this.FindName("ImageTransformation")));
            this.AttachmentPicker = ((Microsoft.Phone.Controls.ListPicker)(this.FindName("AttachmentPicker")));
        }
    }
}

