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
//

using System;
using System.Threading.Tasks;
using Windows.Devices.Geolocation;

namespace RayzitBackgroundAgent.Geolocation
{
    public class LocationFinderBG
    {
        public async Task<LocationInfoBG> GetLocation()
        {
            var locationInfo = new LocationInfoBG();
            var geolocator = new Geolocator { DesiredAccuracyInMeters = 200, DesiredAccuracy = PositionAccuracy.Default };

            if (geolocator.LocationStatus == PositionStatus.Disabled)
                return null;

            try
            {
                var geoposition = await geolocator.GetGeopositionAsync(TimeSpan.FromSeconds(5), TimeSpan.FromSeconds(10)
                                                    );

                locationInfo.Latitude = geoposition.Coordinate.Latitude.ToString("0.00");
                locationInfo.Longitude = geoposition.Coordinate.Longitude.ToString("0.00");
                locationInfo.Accuracy = geoposition.Coordinate.Accuracy.ToString();

                return locationInfo;
            }

            catch (Exception ex)
            {
                if ((uint)ex.HResult == 0x80004004)
                {
                    // the application does not have the right capability or the location master switch is off
                    return null;
                }
                // something else happened acquring the location
                return null;
            }
        }
    }

    public class LocationInfoBG
    {
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string Accuracy { get; set; }
    }
}