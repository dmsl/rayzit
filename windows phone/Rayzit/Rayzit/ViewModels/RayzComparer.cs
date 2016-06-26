using System.Collections.Generic;

namespace Rayzit.ViewModels
{
    public class RayzComparer : IEqualityComparer<Rayz>
    {
        #region IEqualityComparer<Rayz> Members
        public bool Equals(Rayz x, Rayz y)
        {
            return (x.Id == y.Id);
        }

        public int GetHashCode(Rayz obj)
        {
            return obj.Id.GetHashCode();
        }
        #endregion
    }
}
