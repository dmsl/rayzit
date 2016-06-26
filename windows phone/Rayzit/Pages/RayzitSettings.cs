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
using System.IO.IsolatedStorage;

namespace Rayzit.Pages
{
	public class RayzitSettings
	{
		// Rayzit Settings
		readonly IsolatedStorageSettings _settings;

		// APP CRITICAL
		const string FirstLaunchKeyName = "FirstLaunchSetting";
		const bool FirstLaunchDefault = true;

		const string SyncKeyName = "SyncSetting";
		const bool SyncDefault = true;

		// The key names of our settings
		const string LocationToggleSwitchKeyName = "LocationToggleSwitchSetting";
		const string LiveTileToggleSwitchSettingKeyName = "LiveTileToggleSwitchSetting";
		const string SoundsToggleSwitchSettingKeyName = "SoundsToggleSwitchSetting";

		const string ListBoxSettingKeyName = "ListBoxSetting";
		const string MetricListBoxSettingKeyName = "MetricListBoxSetting";
		const string PowerValueKeyName = "PowerValue";

		const string AutoFavToggleSwitchSettingKeyName = "AutoFavToggleSwitchSetting";

		const string PlayIntroVideoSettingKeyName = "PlayIntroVideoSetting";

		const string LiveQSettingKeyName = "LiveQSetting";
		const string LiveTimeSettingKeyName = "LiveTimeSettingKeyName";

		const string UserRegisteredSettingKeyName = "UserRegisteredSetting";

		// The default value of our settings
		const bool LocationToggleSwitchDefault = true;
		const bool LiveTileToggleSwitchSettingDefault = false;
		const bool SoundsToggleSwitchDefault = true;

		const int ListBoxSettingDefault = 0;
		const int MetricListBoxSettingDefault = 0;
		const Int32 LiveQSettingDefault = 100;
		const Int64 LiveTimeSettingDefault = 172800000;

		const int PowerValueDefault = 100;

		const bool AutoFavToggleSwitchSettingDefault = true;

		const bool PlayIntroVideoSettingDefault = true;

		const bool UserRegisteredSettingDefault = false;

		/// <summary>
		/// Constructor that gets the application settings.
		/// </summary>
		public RayzitSettings()
		{
			if (!System.ComponentModel.DesignerProperties.IsInDesignTool)
			{
				// Get the settings for this application.
				_settings = IsolatedStorageSettings.ApplicationSettings;

				// Set all settings to their default values
				if (FirstLaunchSetting)
				{
					LocationToggleSwitchSetting = LocationToggleSwitchDefault;
					LiveTileToggleSwitchSetting = LiveTileToggleSwitchSettingDefault;
					FirstLaunchSetting = false;
				}
			}
		}

		/// <summary>
		/// Update a setting value for our application. If the setting does not
		/// exist, then add the setting.
		/// </summary>
		/// <param name="key"></param>
		/// <param name="value"></param>
		/// <returns></returns>
		public bool AddOrUpdateValue(string key, Object value)
		{
			var valueChanged = false;

			// If the key exists
			if (_settings.Contains(key))
			{
				// If the value has changed
				if (_settings[key] != value)
				{
					// Store the new value
					_settings[key] = value;
					valueChanged = true;
				}
			}
			// Otherwise create the key.
			else
			{
				_settings.Add(key, value);
				valueChanged = true;
			}
		   return valueChanged;
		}

		/// <summary>
		/// Get the current value of the setting, or if it is not found, set the 
		/// setting to the default setting.
		/// </summary>
		/// <typeparam name="T"></typeparam>
		/// <param name="key"></param>
		/// <param name="defaultValue"></param>
		/// <returns></returns>
		public T GetValueOrDefault<T>(string key, T defaultValue)
		{
			T value;

			// If the key exists, retrieve the value.
			if (_settings.Contains(key))
			{
				value = (T)_settings[key];
			}
			// Otherwise, use the default value.
			else
			{
				value = defaultValue;
			}
			return value;
		}

		/// <summary>
		/// Save the settings.
		/// </summary>
		public void Save()
		{
			_settings.Save();
		}

		/// <summary>
		/// Property to get and set the First Application Lunch Setting Key.
		/// </summary>
		public bool FirstLaunchSetting
		{
			get
			{
				return GetValueOrDefault(FirstLaunchKeyName, FirstLaunchDefault);
			}
			set
			{
				if (AddOrUpdateValue(FirstLaunchKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set the Syncronization Setting Key.
		/// </summary>
		public bool SyncSetting
		{
			get
			{
				return GetValueOrDefault(SyncKeyName, SyncDefault);
			}
			set
			{
				if (AddOrUpdateValue(SyncKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set the Location Toggle Switch Setting Key.
		/// </summary>
		public bool LocationToggleSwitchSetting
		{
			get
			{
				return GetValueOrDefault(LocationToggleSwitchKeyName, LocationToggleSwitchDefault);
			}
			set
			{
				if (AddOrUpdateValue(LocationToggleSwitchKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set the Notification Sounds Toggle Switch Setting Key.
		/// </summary>
		public bool SoundsToggleSwitchSetting
		{
			get
			{
				return GetValueOrDefault(SoundsToggleSwitchSettingKeyName, SoundsToggleSwitchDefault);
			}
			set
			{
				if (AddOrUpdateValue(SoundsToggleSwitchSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a ListBox Setting Key.
		/// </summary>
		public int ListBoxSetting
		{
			get
			{
				return GetValueOrDefault(ListBoxSettingKeyName, ListBoxSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(ListBoxSettingKeyName, value))
				{
				   Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a ListBox Setting Key.
		/// </summary>
		public int MetricListBoxSetting
		{
			get
			{
				return GetValueOrDefault(MetricListBoxSettingKeyName, MetricListBoxSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(MetricListBoxSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set the Powerbar Value Key.
		/// </summary>
		public int PowerValueSetting
		{
			get
			{
				return GetValueOrDefault(PowerValueKeyName, PowerValueDefault);
			}
			set
			{
				if (AddOrUpdateValue(PowerValueKeyName, value))
				{    
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a Auto Favorite Setting Key.
		/// </summary>
		public bool PlayIntroVideoSetting
		{
			get
			{
				return GetValueOrDefault(PlayIntroVideoSettingKeyName, PlayIntroVideoSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(PlayIntroVideoSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a Auto Favorite Setting Key.
		/// </summary>
		public bool AutoFavToggleSwitchSetting
		{
			get
			{
				return GetValueOrDefault(AutoFavToggleSwitchSettingKeyName, AutoFavToggleSwitchSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(AutoFavToggleSwitchSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a Live Tile Setting Key.
		/// </summary>
		public bool LiveTileToggleSwitchSetting
		{
			get
			{
				return GetValueOrDefault(LiveTileToggleSwitchSettingKeyName, LiveTileToggleSwitchSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(LiveTileToggleSwitchSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a ListBox Setting Key.
		/// </summary>
		public Int32 LiveQSetting
		{
			get
			{
				return GetValueOrDefault(LiveQSettingKeyName, LiveQSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(LiveQSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set a ListBox Setting Key.
		/// </summary>
		public Int64 LiveTimeSetting
		{
			get
			{
				return GetValueOrDefault(LiveTimeSettingKeyName, LiveTimeSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(LiveTimeSettingKeyName, value))
				{
					Save();
				}
			}
		}

		/// <summary>
		/// Property to get and set the Registered User Setting Key.
		/// </summary>
		public bool UserRegisteredSetting
		{
			get
			{
				return GetValueOrDefault(UserRegisteredSettingKeyName, UserRegisteredSettingDefault);
			}
			set
			{
				if (AddOrUpdateValue(UserRegisteredSettingKeyName, value))
				{
					Save();
				}
			}
		}
	}
}
