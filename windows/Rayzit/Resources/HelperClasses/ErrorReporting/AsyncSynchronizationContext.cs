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
using System.Threading;

namespace Rayzit.Resources.HelperClasses.ErrorReporting
{
    public class AsyncSynchronizationContext : SynchronizationContext
    {
        public static AsyncSynchronizationContext Register()
        {
            var syncContext = Current;
            if (syncContext == null)
                throw new InvalidOperationException("Ensure a synchronization context exists before calling this method.");

            var customSynchronizationContext = syncContext as AsyncSynchronizationContext;

            if (customSynchronizationContext == null)
            {
                customSynchronizationContext = new AsyncSynchronizationContext(syncContext);
                SetSynchronizationContext(customSynchronizationContext);
            }

            return customSynchronizationContext;
        }

        private readonly SynchronizationContext _syncContext;

        public AsyncSynchronizationContext(SynchronizationContext syncContext)
        {
            _syncContext = syncContext;
        }

        public override SynchronizationContext CreateCopy()
        {
            return new AsyncSynchronizationContext(_syncContext.CreateCopy());
        }

        public override void OperationCompleted()
        {
            _syncContext.OperationCompleted();
        }

        public override void OperationStarted()
        {
            _syncContext.OperationStarted();
        }

        public override void Post(SendOrPostCallback d, object state)
        {
            _syncContext.Post(WrapCallback(d), state);
        }

        public override void Send(SendOrPostCallback d, object state)
        {
            _syncContext.Send(d, state);
        }

        private static SendOrPostCallback WrapCallback(SendOrPostCallback sendOrPostCallback)
        {
            return state =>
                {
                    Exception exception = null;

                    try
                    {
                        sendOrPostCallback(state);
                    }
                    catch (Exception ex)
                    {
                        exception = ex;
                    }

                    if (exception != null)

                        WP8SDK.Api.LogError("Async Error", exception.InnerException);
                };
        }
    }
}