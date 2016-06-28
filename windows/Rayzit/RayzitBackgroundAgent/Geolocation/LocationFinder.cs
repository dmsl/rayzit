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