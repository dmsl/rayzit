﻿#pragma checksum "C:\Users\Costantinos\Source\Workspaces\Rayzit\Rayzit\Pages\Attachments\Attachments.xaml" "{406ea660-64cf-4c82-b6f0-42d48172a799}" "D468383D6F3E0C8853019A0CBEE8A03F"
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


namespace Rayzit.Pages.Attachments {
    
    
    public partial class Attachments : Microsoft.Phone.Controls.PhoneApplicationPage {
        
        internal System.Windows.Controls.Grid LayoutRoot;
        
        internal System.Windows.Controls.StackPanel PageTitle;
        
        internal System.Windows.Controls.Grid ContentPanel;
        
        internal Microsoft.Phone.Controls.LongListMultiSelector AttachmentsGridSelector;
        
        internal System.Windows.Controls.Grid ZoomGrid;
        
        internal System.Windows.Controls.Image ZoomImage;
        
        internal System.Windows.Media.CompositeTransform ImageTransformation;
        
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
            System.Windows.Application.LoadComponent(this, new System.Uri("/Rayzit;component/Pages/Attachments/Attachments.xaml", System.UriKind.Relative));
            this.LayoutRoot = ((System.Windows.Controls.Grid)(this.FindName("LayoutRoot")));
            this.PageTitle = ((System.Windows.Controls.StackPanel)(this.FindName("PageTitle")));
            this.ContentPanel = ((System.Windows.Controls.Grid)(this.FindName("ContentPanel")));
            this.AttachmentsGridSelector = ((Microsoft.Phone.Controls.LongListMultiSelector)(this.FindName("AttachmentsGridSelector")));
            this.ZoomGrid = ((System.Windows.Controls.Grid)(this.FindName("ZoomGrid")));
            this.ZoomImage = ((System.Windows.Controls.Image)(this.FindName("ZoomImage")));
            this.ImageTransformation = ((System.Windows.Media.CompositeTransform)(this.FindName("ImageTransformation")));
        }
    }
}

