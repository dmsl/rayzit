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

using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows.Controls;
using System.Windows.Media;

namespace Rayzit.Pages.Tutorial
{
    public partial class RayzitTutorial
    {
        private static ObservableCollection<HelpItems> _helpItems;

        public RayzitTutorial()
        {
            InitializeComponent();

            ChangeLockStatus();

            InitializeMenu();
        }

        void InitializeMenu()
        {
            _helpItems = new ObservableCollection<HelpItems>
                {
                    new HelpItems("create rayz", "learn how to create new rayz messages"),
                    new HelpItems("create reply", "learn how to create rayz reply messages"),
                    new HelpItems("rayz message", "all you need to know about rayz messages"),
                    new HelpItems("rayz reply message", "all you need to know about rayz reply messages"),
                    new HelpItems("attachments", "some tips for attachments"),
                    new HelpItems("icons", "learn the meaning of the different icons used")
                };

            MenuList.ItemsSource = _helpItems;
        }

        private void ChangeLockStatus()
        {
            RayzitTutorialPivot.IsLocked = !RayzitTutorialPivot.IsLocked;
            RayzitTutorialPivot.Foreground = new SolidColorBrush(Colors.Black);
        }

        private void MenuList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (MenuList == null)
                return;

            switch (MenuList.SelectedIndex)
            {
                // create rayz
                case 0:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 1;
                    ChangeLockStatus();
                    break;
                // create reply
                case 1:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 2;
                    ChangeLockStatus();
                    break;
                // rayz message
                case 2:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 3;
                    ChangeLockStatus();
                    break;
                // rayz reply message
                case 3:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 4;
                    ChangeLockStatus();
                    break;
                // attachments
                case 4:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 5;
                    ChangeLockStatus();
                    break;
                // icons
                case 5:
                    ChangeLockStatus();
                    RayzitTutorialPivot.SelectedIndex = 6;
                    ChangeLockStatus();
                    break;
            }
        }

        protected override void OnBackKeyPress(CancelEventArgs e)
        {
            if (RayzitTutorialPivot.SelectedIndex != 0)
            {
                MenuList.SelectedIndex = -1;
                ChangeLockStatus();
                RayzitTutorialPivot.SelectedIndex = 0;
                ChangeLockStatus();
                e.Cancel = true;
            }
        }
    }

    internal class HelpItems
    {
        private string _title;
        public string Title
        {
            get { return _title; }
            set
            {
                NotifyPropertyChanging("Title");
                _title = value;
                NotifyPropertyChanged("Title");
            }
        }

        private string _tip;
        public string Tip
        {
            get { return _tip; }
            set
            {
                NotifyPropertyChanging("Tip");
                _tip = value;
                NotifyPropertyChanged("Tip");
            }
        }

        public HelpItems(string title, string tip)
        {
            _title = title;
            _tip = tip;
        }

        #region INotifyPropertyChanged Members

        public event PropertyChangedEventHandler PropertyChanged;

        // Used to notify that a property changed
        private void NotifyPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        #endregion

        #region INotifyPropertyChanging Members

        public event PropertyChangingEventHandler PropertyChanging;

        // Used to notify that a property is about to change
        private void NotifyPropertyChanging(string propertyName)
        {
            if (PropertyChanging != null)
            {
                PropertyChanging(this, new PropertyChangingEventArgs(propertyName));
            }
        }

        #endregion
    }
}