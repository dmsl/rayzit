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
using System.Windows;
using System.Windows.Controls;

namespace Rayzit.Resources.Controls
{
    public class SplitPanel : Panel
    {
        protected override Size MeasureOverride(Size availableSize)
        {
            // the final measure size is the available size for the width, and the maximum
            // desired size of our children for the height
            var finalSize = new Size { Width = availableSize.Width };

            if (Children.Count != 0)
                availableSize.Width /= Children.Count;

            foreach (var current in Children)
            {
                current.Measure(availableSize);

                Size desiredSize = current.DesiredSize;
                finalSize.Height = Math.Max(finalSize.Height, desiredSize.Height);
            }

            // make sure it will works in design time mode
            if (double.IsPositiveInfinity(finalSize.Height) || double.IsPositiveInfinity(finalSize.Width))
                return Size.Empty;

            return finalSize;
        }

        protected override Size ArrangeOverride(Size arrangeSize)
        {
            Rect finalRect = new Rect(new Point(), arrangeSize);
            double width = arrangeSize.Width / Children.Count;

            foreach (var child in Children)
            {
                finalRect.Height = Math.Max(arrangeSize.Height, child.DesiredSize.Height);
                finalRect.Width = width;

                child.Arrange(finalRect);

                // move each child by the width increment 
                finalRect.X += width;
            }

            return arrangeSize;
        }
    }
}
