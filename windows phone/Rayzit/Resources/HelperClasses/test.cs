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
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace Rayzit.Resources.HelperClasses
{
    public static class Test
    {
        public static void Animate(this DependencyObject target, double from, double to,
                          object propertyPath, int duration, int startTime,
                          IEasingFunction easing = null, Action completed = null)
        {
            if (easing == null)
            {
                easing = new SineEase();
            }

            var db = new DoubleAnimation();
            db.To = to;
            db.From = from;
            db.EasingFunction = easing;
            db.Duration = TimeSpan.FromMilliseconds(duration);
            Storyboard.SetTarget(db, target);
            Storyboard.SetTargetProperty(db, new PropertyPath(propertyPath));

            var sb = new Storyboard();
            sb.BeginTime = TimeSpan.FromMilliseconds(startTime);

            if (completed != null)
            {
                sb.Completed += (s, e) => completed();
            }

            sb.Children.Add(db);
            sb.Begin();
        }

        public static void SetHorizontalOffset(this FrameworkElement fe, double offset)
        {
            var translateTransform = fe.RenderTransform as TranslateTransform;
            if (translateTransform == null)
            {
                // create a new transform if one is not alreayd present
                var trans = new TranslateTransform()
                {
                    X = offset
                };
                fe.RenderTransform = trans;
            }
            else
            {
                translateTransform.X = offset;
            }
        }

        public static Offset GetHorizontalOffset(this FrameworkElement fe)
        {
            var trans = fe.RenderTransform as TranslateTransform;
            if (trans == null)
            {
                // create a new transform if one is not alreayd present
                trans = new TranslateTransform()
                {
                    X = 0
                };
                fe.RenderTransform = trans;
            }
            return new Offset()
            {
                Transform = trans,
                Value = trans.X
            };
        }

        public struct Offset
        {
            public double Value { get; set; }
            public TranslateTransform Transform { get; set; }
        }
    }
}
