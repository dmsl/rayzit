using System;
using System.ComponentModel;
using System.Data.Linq;
using System.Data.Linq.Mapping;
using Microsoft.Phone.Data.Linq.Mapping;

namespace Rayzit.ViewModels
{
    class RayzDataContext : DataContext
    {
        // Pass the connection string to the base class.
        public RayzDataContext(string connectionString)
            : base(connectionString)
        { }

        // Specify a table for the to-do items.
        public Table<Rayz> RayzItems;

        // Specify a table for the to-do items.
        public Table<RayzReply> RayzRepliesItems;
    }

    [Table]
    [Index(Columns = "Id", Name = "_idUnique", IsUnique = true)]
    public class Rayz : INotifyPropertyChanged, INotifyPropertyChanging
    {
        #region Table Columns
        // Define ID: private field, public property, and database column.
        private int _id;

        [Column(IsPrimaryKey = true, IsDbGenerated = true, DbType = "INT NOT NULL Identity", CanBeNull = false, AutoSync = AutoSync.OnInsert)]
        public int Id
        {
            get { return _id; }
            set
            {
                NotifyPropertyChanging("Id");
                _id = value;
                NotifyPropertyChanged("Id");
            }
        }

        // Define ID: private field, public property, and database column.
        private string _rayzId;

        [Column(CanBeNull = true, UpdateCheck = UpdateCheck.Never)]
        public string RayzId
        {
            get { return _rayzId; }
            set
            {
                NotifyPropertyChanging("RayzId");
                _rayzId = value;
                NotifyPropertyChanged("RayzId");
            }
        }

        // Define category name: private field, public property, and database column.
        private string _rayzMessage;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string RayzMessage
        {
            get { return _rayzMessage; }
            set
            {
                NotifyPropertyChanging("RayzMessage");
                _rayzMessage = value;
                NotifyPropertyChanged("RayzMessage");
            }
        }

        private Int64 _rayzDate;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 RayzDate
        {
            get
            {
                return _rayzDate;
            }
            set
            {
                NotifyPropertyChanging("RayzDate");
                _rayzDate = value;
                NotifyPropertyChanged("RayzDate");
            }
        }

        private int _totalRayzReplies;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public int TotalRayzReplies
        {
            get { return _totalRayzReplies; }
            set
            {
                NotifyPropertyChanging("TotalRayzReplies");
                _totalRayzReplies = value;
                NotifyPropertyChanged("TotalRayzReplies");
            }
        }

        private int _unreadRayzReplies;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public int UnreadRayzReplies
        {
            get { return _unreadRayzReplies; }
            set
            {
                NotifyPropertyChanging("UnreadRayzReplies");
                _unreadRayzReplies = value;
                NotifyPropertyChanged("UnreadRayzReplies");
            }
        }

        private Int64 _maxDistance;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 MaxDistance
        {
            get { return _maxDistance; }
            set
            {
                NotifyPropertyChanging("MaxDistance");
                _maxDistance = value;
                NotifyPropertyChanged("MaxDistance");
            }
        }

        private Boolean _isLive;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsLive
        {
            get { return _isLive; }
            set
            {
                NotifyPropertyChanging("IsLive");
                _isLive = value;
                NotifyPropertyChanged("IsLive");
            }
        }

        private Boolean _isStarred;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsStarred
        {
            get { return _isStarred; }
            set
            {
                NotifyPropertyChanging("IsStarred");
                _isStarred = value;
                Status = 1;
                NotifyPropertyChanged("IsStarred");
            }
        }

        private Boolean _isMy;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsMy
        {
            get { return _isMy; }
            set
            {
                NotifyPropertyChanging("IsMy");
                _isMy = value;
                Status = 1;
                NotifyPropertyChanged("IsMy");
            }
        }

        private Int64 _followersCount;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 FollowersCount
        {
            get
            {
                return _followersCount;
            }
            set
            {
                NotifyPropertyChanging("FollowersCount");
                _followersCount = value;
                NotifyPropertyChanged("FollowersCount");
            }
        }

        private Int64 _reportCount;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 ReportCount
        {
            get
            {
                return _reportCount;
            }
            set
            {
                NotifyPropertyChanging("ReportCount");
                _reportCount = value;
                NotifyPropertyChanged("ReportCount");
            }
        }

        private Int64 _rerayzCount;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 RerayzCount
        {
            get
            {
                return _rerayzCount;
            }
            set
            {
                NotifyPropertyChanging("RerayzCount");
                _rerayzCount = value;
                NotifyPropertyChanged("RerayzCount");
            }
        }

        private int _status;
        public int Status
        {
            get { return _status; }
            set
            {
                NotifyPropertyChanging("Status");
                if (_isStarred)
                    _status = 1;
                else if (_isMy && !_isStarred)
                    _status = 2;
                else
                    _status = 0;
                NotifyPropertyChanged("Status");
            }
        }

        private Boolean _isHidden;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsHidden
        {
            get { return _isHidden; }
            set
            {
                NotifyPropertyChanging("IsHidden");
                _isHidden = value;
                NotifyPropertyChanged("IsHidden");
            }
        }

        private Boolean _hasAttachments;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean HasAttachments
        {
            get { return _hasAttachments; }
            set
            {
                NotifyPropertyChanging("HasAttachments");
                _hasAttachments = value;
                NotifyPropertyChanged("HasAttachments");
            }
        }

        private Boolean _hasBeenDeleted;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean HasBeenDeleted
        {
            get { return _hasBeenDeleted; }
            set
            {
                NotifyPropertyChanging("HasBeenDeleted");
                _hasBeenDeleted = value;
                NotifyPropertyChanged("HasBeenDeleted");
            }
        }

        private string _attachments;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string Attachments
        {
            get { return _attachments; }
            set
            {
                NotifyPropertyChanging("Attachments");
                _attachments = value;
                NotifyPropertyChanged("Attachments");
            }
        }

        private string _attachMd5;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string AttachMd5
        {
            get { return _attachMd5; }
            set
            {
                NotifyPropertyChanging("AttachMd5");
                _attachMd5 = value;
                NotifyPropertyChanged("AttachMd5");
            }
        }

        #endregion

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

    [Table]
    [Index(Columns = "Id", Name = "_idUnique", IsUnique = true)]
    public class RayzReply : INotifyPropertyChanged, INotifyPropertyChanging
    {

        #region Table Columns

        // Define ID: private field, public property, and database column.
        private int _id;

        [Column(IsPrimaryKey = true, IsDbGenerated = true, DbType = "INT NOT NULL Identity", CanBeNull = false, AutoSync = AutoSync.OnInsert)]
        public int Id
        {
            get { return _id; }
            set
            {
                if (_id != value)
                {
                    NotifyPropertyChanging("Id");
                    _id = value;
                    NotifyPropertyChanged("Id");
                }
            }
        }

        // Define ID: private field, public property, and database column.
        private string _rayzReplyId;

        [Column(CanBeNull = true, UpdateCheck = UpdateCheck.Never)]
        public string RayzReplyId
        {
            get { return _rayzReplyId; }
            set
            {
                NotifyPropertyChanging("RayzReplyId");
                _rayzReplyId = value;
                NotifyPropertyChanged("RayzReplyId");
            }
        }

        // Define category name: private field, public property, and database column.
        private string _rayzReplyMessage;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string RayzReplyMessage
        {
            get { return _rayzReplyMessage; }
            set
            {
                NotifyPropertyChanging("RayzReplyMessage");
                _rayzReplyMessage = value;
                NotifyPropertyChanged("RayzReplyMessage");
            }
        }

        private Int64 _rayzReplyDate;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 RayzReplyDate
        {
            get
            {
                return _rayzReplyDate;
            }
            set
            {
                NotifyPropertyChanging("RayzReplyDate");
                _rayzReplyDate = value;
                NotifyPropertyChanged("RayzReplyDate");
            }
        }

        private Int64 _upVotes;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 UpVotes
        {
            get
            {
                return _upVotes;
            }
            set
            {
                NotifyPropertyChanging("UpVotes");
                _upVotes = value;
                NotifyPropertyChanged("UpVotes");
            }
        }

        private Int64 _reportCount;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Int64 ReportCount
        {
            get
            {
                return _reportCount;
            }
            set
            {
                NotifyPropertyChanging("ReportCount");
                _reportCount = value;
                NotifyPropertyChanged("ReportCount");
            }
        }

        private Boolean _isRead;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsRead
        {
            get { return _isRead; }
            set
            {
                NotifyPropertyChanging("IsRead");
                _isRead = value;
                NotifyPropertyChanged("IsRead");
            }
        }

        private Boolean _isMy;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean IsMy
        {
            get { return _isMy; }
            set
            {
                NotifyPropertyChanging("IsMy");
                _isMy = value;
                NotifyPropertyChanged("IsMy");
            }
        }

        private Boolean _hasAttachment;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public Boolean HasAttachment
        {
            get { return _hasAttachment; }
            set
            {
                NotifyPropertyChanging("HasAttachment");
                _hasAttachment = value;
                NotifyPropertyChanged("HasAttachment");
            }
        }

        private string _attachments;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string Attachments
        {
            get { return _attachments; }
            set
            {
                NotifyPropertyChanging("Attachments");
                _attachments = value;
                NotifyPropertyChanged("Attachments");
            }
        }

        private string _attachMd5;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string AttachMd5
        {
            get { return _attachMd5; }
            set
            {
                NotifyPropertyChanging("AttachMd5");
                _attachMd5 = value;
                NotifyPropertyChanged("AttachMd5");
            }
        }

        private string _rayzId;

        [Column(CanBeNull = false, UpdateCheck = UpdateCheck.Never)]
        public string RayzId
        {
            get { return _rayzId; }
            set
            {
                NotifyPropertyChanging("RayzId");
                _rayzId = value;
                NotifyPropertyChanged("RayzId");
            }
        }

        #endregion

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
