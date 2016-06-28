using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using CodeTitans.JSon;
using RayzitServiceClient.HelperClasses;

namespace RayzitServiceClient
{
    /// <summary>
    /// Rayzit Service Client
    /// 
    /// Implements all the Rayzit API Requests for Windows Phone
    /// </summary>
    public class Rsc
    {
        private static string _applicationId;
        private static string _deviceId;
        private static HttpClient _client;

        public static Uri ServerBaseUri
        {
            get { return new Uri("https://api.rayzit.com"); }
            //get { return new Uri("http://dev.rayzit.com"); }
            //get { return new Uri("http://10.16.20.17:9000"); }
        }

        public Rsc(string appId, string deviceId)
        {
            _applicationId = appId;
            _deviceId = deviceId;
            _client = new HttpClient();
        }

        #region General Checkers

        private static string CheckResponse(HttpResponseMessage resp)
        {
            if (resp.StatusCode != HttpStatusCode.OK)
            {
                if (resp.StatusCode == HttpStatusCode.ServiceUnavailable)
                {
                    var writer = new JSonWriter(true);

                    writer.WriteObjectBegin();
                    writer.WriteMember("status", "error");
                    writer.WriteMember("message", ErrorMessages.ServiceUnavailable);
                    writer.WriteObjectEnd();

                    return writer.ToString();
                }

                var writer2 = new JSonWriter(true);

                writer2.WriteObjectBegin();
                writer2.WriteMember("status", "error");
                writer2.WriteMember("message", ErrorMessages.ConnectionProblem);
                writer2.WriteObjectEnd();

                return writer2.ToString();
            }

            return null;
        }

        #endregion

        #region Application Setting Requests

        /// <summary>
        /// Requests the live feed settings values for the application
        /// </summary>
        public async Task<IJSonObject> GetLiveSettings()
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/settings"));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        #endregion

        #region User Requests

        /// <summary>
        /// Updates the User's position
        /// Registers a new user if the user does not exist.
        /// </summary>
        /// <param name="latitude"> Latitude Coordinate </param>
        /// <param name="longitude"> Longitude Coordinate </param>
        /// <param name="accuracy"> Accuracy of the user's position in meters </param>
        public async Task<IJSonObject> UpdatePosition(String latitude, String longitude, String accuracy)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                // Make sure that lat and long are dot separated
                latitude = latitude.Replace(@",", @".");
                longitude = longitude.Replace(@",", @".");

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("appId", _applicationId);
                writer.WriteMember("latitude", latitude);
                writer.WriteMember("longitude", longitude);
                writer.WriteMember("accuracy", accuracy);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/user/update"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
            
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the latest live feed of the user
        /// </summary>
        public async Task<IJSonObject> GetLiveFeed()
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/user/" + _deviceId + "/livefeed"));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the starred rayz of the user
        /// </summary>
        public async Task<IJSonObject> GetStarredRayz(int page)
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/user/" + _deviceId + "/starred/" + page));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the rayz of the user
        /// </summary>
        public async Task<IJSonObject> GetMyRayz(int page)
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/user/" + _deviceId + "/myrayz/" + page));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the latest live feed of the user and a random Rayz from the list
        /// Random is latest
        /// </summary>
        public async Task<IJSonObject> GetLiveFeedRandom()
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/user/" + _deviceId + "/livefeed/random"));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the user's rayz power
        /// </summary>
        public async Task<IJSonObject> GetPower()
        {
            try
            {
                var reader = new JSonReader();

                var response = await _client.GetAsync(new Uri(ServerBaseUri, "/user/" + _deviceId + "/power"));

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();
                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        #endregion

        #region Rayz Requests

        /// <summary>
        /// Creates a new Rayz
        /// </summary>
        /// <param name="latitude"> Latitude Coordinate </param>
        /// <param name="longitude"> Longitude Coordinate </param>
        /// <param name="accuracy"> Accuracy of the user's position in meters </param>
        /// <param name="maxDistance"> Max Rayz Distance - NOT USED </param>
        /// <param name="rayzMessage"> The Rayz message </param>
        /// <param name="attachments"> The Rayz Attachments </param>
        public async Task<IJSonObject> CreateNewRayz(String latitude, String longitude, String accuracy, String maxDistance, String rayzMessage, ObservableCollection<RayzItAttachment> attachments)
        {
            try
            {
                var content = new MultipartFormDataContent();
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                // PLAY PROBLEM
                foreach (var param in content.Headers.ContentType.Parameters.Where(param => param.Name.Equals("boundary")))
                    param.Value = param.Value.Replace("\"", String.Empty);

                // Make sure that lat and long are dot separated
                latitude = latitude.Replace(@",", @".");
                longitude = longitude.Replace(@",", @".");

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("latitude", latitude);
                writer.WriteMember("longitude", longitude);
                writer.WriteMember("accuracy", accuracy);
                writer.WriteMember("maxDistance", maxDistance);
                writer.WriteMember("rayzMessage", rayzMessage);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                content.Add(json, "\"json\"");

                foreach (var a in attachments)
                {
                    var fileContent = new StreamContent(new MemoryStream(a.FileBody));
                    fileContent.Headers.ContentDisposition = new ContentDispositionHeaderValue("form-data")
                    {
                        Name = "\"attachment\"",
                        FileName = "\"attachment.file\""
                    };
                    fileContent.Headers.ContentType = MediaTypeHeaderValue.Parse(a.ContType);
                    content.Add(fileContent);
                }

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/create"), content);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Makes a Re-Rayz Request
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        /// <param name="latitude"> Latitude Coordinate </param>
        /// <param name="longitude"> Longitude Coordinate </param>
        /// <param name="accuracy"> Accuracy of the user's position in meters </param>
        /// <param name="maxDistance"> Max Rayz Distance - NOT USED </param>
        public async Task<IJSonObject> ReRayz(String rayzId, String latitude, String longitude, String accuracy, String maxDistance)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                // Make sure that lat and long are dot separated
                latitude = latitude.Replace(@",", @".");
                longitude = longitude.Replace(@",", @".");

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteMember("latitude", latitude);
                writer.WriteMember("longitude", longitude);
                writer.WriteMember("accuracy", accuracy);
                writer.WriteMember("maxDistance", maxDistance);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/rerayz"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Makes a Rayz Delete Request
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> DeleteRayz(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/delete"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Reports a Rayz
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> ReportRayz(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/report"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Creates a new RayzReply
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        /// <param name="rayzReply"> The RayzReply message </param>
        /// <param name="attachments"> The RayzReply Attachments </param>
        public async Task<IJSonObject> NewRayzReply(String rayzId, String rayzReply, ObservableCollection<RayzItAttachment> attachments)
        {
            try
            {
                var content = new MultipartFormDataContent();
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                // PLAY PROBLEM
                foreach (var param in content.Headers.ContentType.Parameters.Where(param => param.Name.Equals("boundary")))
                    param.Value = param.Value.Replace("\"", String.Empty);

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteMember("rayzReplyMessage", rayzReply);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                content.Add(json, "\"json\"");

                foreach (var a in attachments)
                {
                    var fileContent = new StreamContent(new MemoryStream(a.FileBody));
                    fileContent.Headers.ContentDisposition = new ContentDispositionHeaderValue("form-data")
                    {
                        Name = "\"attachment\"",
                        FileName = "\"attachment.file\""
                    };
                    fileContent.Headers.ContentType = MediaTypeHeaderValue.Parse(a.ContType);
                    content.Add(fileContent);
                }

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/reply"), content);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Stars a Rayz
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> StarRayz(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/star"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Unstars a Rayz
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> DeleteStarredRayz(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/star/delete"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Reuqests all the update status of the given Rayz
        /// </summary>
        /// <param name="rayzIds"> List of the Rayz to get the updates for </param>
        public async Task<IJSonObject> GetMultiCounter(List<String> rayzIds)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzIds");
                writer.Write(rayzIds.ToArray());
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/multicounter"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the number of the replies of the specified Rayz
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> GetAnswersCounter(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/counter"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Requests the replies of the specified Rayz
        /// </summary>
        /// <param name="rayzId"> Rayz ID </param>
        public async Task<IJSonObject> GetAnswers(String rayzId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzId", rayzId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/answers"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Powers Up a RayzReply
        /// </summary>
        /// <param name="rayzReplyId"> RayzReply ID </param>
        public async Task<IJSonObject> PowerUp(String rayzReplyId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzReplyId", rayzReplyId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/reply/powerup"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Powers Down a RayzReply
        /// </summary>
        /// <param name="rayzReplyId"> RayzReply ID </param>
        public async Task<IJSonObject> PowerDown(String rayzReplyId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzReplyId", rayzReplyId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/reply/powerdown"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Reports a RayzReply
        /// </summary>
        /// <param name="rayzReplyId"> RayzReply ID </param>
        public async Task<IJSonObject> ReportRayzReply(String rayzReplyId)
        {
            try
            {
                var writer = new JSonWriter(true);
                var reader = new JSonReader();

                writer.WriteObjectBegin();
                writer.WriteMember("userId", _deviceId);
                writer.WriteMember("rayzReplyId", rayzReplyId);
                writer.WriteObjectEnd();

                var json = new StringContent(writer.ToString());
                json.Headers.ContentType = MediaTypeHeaderValue.Parse("application/json");

                var response = await _client.PostAsync(new Uri(ServerBaseUri, "/rayz/reply/report"), json);

                var cr = CheckResponse(response);
                if (cr != null)
                {
                    var creply = reader.ReadAsJSonObject(cr);
                    return creply;
                }

                var r = await response.Content.ReadAsStringAsync();

                var reply = reader.ReadAsJSonObject(r);
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        #endregion

        #region Attachments Requests

        /// <summary>
        /// Requests the specified attachment
        /// </summary>
        /// <param name="fileUrl"> Attachment's location </param>
        /// <returns> The byte array of the attachment </returns>
        public async Task<byte[]> RequestAttachment(String fileUrl)
        {
            try
            {
                var response = await _client.GetAsync(new Uri(ServerBaseUri, fileUrl));

                var cr = CheckResponse(response);
                if (cr != null)
                    return null;

                var reply = await response.Content.ReadAsByteArrayAsync();
                return reply;
            }
            catch (Exception)
            {
                return null;
            }
        }

        #endregion
    }
}
