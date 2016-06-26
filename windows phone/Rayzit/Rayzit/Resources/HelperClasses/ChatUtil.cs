using System;
using System.Windows;
using System.Windows.Media.Animation;

namespace Rayzit.Resources.HelperClasses
{
    public static class ChatUtil
    {
        /// <summary>
        /// Shows the element, playing a storyboard if one is present
        /// </summary>
        /// <param name="element"></param>
        public static void Show(this FrameworkElement element, Action completedAction)
        {
            var animationName = element.Name + "ShowAnim";

            // check for presence of a show animation
            var showAnim = element.Resources[animationName] as Storyboard;
            if (showAnim != null)
            {
                showAnim.Begin();
                showAnim.Completed += (s, e) => completedAction();
            }
            else
            {
                element.Visibility = Visibility.Visible;
            }
        }

        /// <summary>
        /// Hides the element, playing a storyboard if one is present
        /// </summary>
        /// <param name="element"></param>
        public static void Hide(this FrameworkElement element)
        {
            var animationName = element.Name + "HideAnim";

            // check for presence of a hide animation
            var showAnim = element.Resources[animationName] as Storyboard;
            if (showAnim != null)
            {
                showAnim.Begin();
            }
            else
            {
                element.Visibility = Visibility.Collapsed;
            }
        }
    }
}
