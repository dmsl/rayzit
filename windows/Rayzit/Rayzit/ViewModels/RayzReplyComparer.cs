using System.Collections.Generic;

namespace Rayzit.ViewModels
{
    public class RayzReplyComparer : IEqualityComparer<RayzReply>
    {
        #region IEqualityComparer<RayzReply> Members
        public bool Equals(RayzReply x, RayzReply y)
        {
            return (x.Id == y.Id);
        }

        public int GetHashCode(RayzReply obj)
        {
            return obj.Id.GetHashCode();
        }
        #endregion
    }
    
    
}
