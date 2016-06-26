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

using Microsoft.Phone.Controls;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace Rayzit.Resources.HelperClasses
{
    public class WrapLongListSelector : LongListSelector, INotifyPropertyChanged
    {
        // implement the INotify
        public event PropertyChangedEventHandler PropertyChanged;
        private void NotifyPropertyChanged(String propertyName)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (null != handler)
            {
                handler(this, new PropertyChangedEventArgs(propertyName));
            }
        }


        public System.Windows.Controls.Primitives.ScrollBar sb;

        private bool _scrollDirectionDown = false;   // or whatever default you want
        private const double Speed = 15;

        public bool ScrollDirectionDown
        { get { return _scrollDirectionDown; } }

        public double ScrollValue
        {
            get
            {
                if (sb != null)
                {
                    return sb.Value;
                }
                else
                    return 0;
            }
            set
            {
                sb.Value = value;
                NotifyPropertyChanged(this.Name + "ScrollValue");
            }
        }

        public override void OnApplyTemplate()
        {
            base.OnApplyTemplate();

            // dat grab doe
            sb = this.GetTemplateChild("VerticalScrollBar") as System.Windows.Controls.Primitives.ScrollBar;
            sb.ValueChanged += sb_ValueChanged;
        }

        void sb_ValueChanged(object sender, RoutedPropertyChangedEventArgs<double> e)
        {
            // an animation has happen and they have moved a significance distance
            // set the new value
            ScrollValue = e.NewValue;

            // determine scroll direction
            if (e.NewValue > e.OldValue+Speed)
            {
                _scrollDirectionDown = true;
            }
            else
            {
                _scrollDirectionDown = false;
            }
        }

      
    }
}
