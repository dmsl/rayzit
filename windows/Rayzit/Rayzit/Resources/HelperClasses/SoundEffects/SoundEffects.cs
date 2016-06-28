using System;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Media;

namespace Rayzit.Resources.HelperClasses.SoundEffects
{
    public class SoundEffects
    {

        public static void PlayMessageFailedSound()
        {
            try
            {
                var s = Song.FromUri("MessageFailed", new Uri(@"Resources/HelperClasses/SoundEffects/audio/MessageFailed.mp3", UriKind.Relative));
                FrameworkDispatcher.Update();
                MediaPlayer.Play(s);
            }
            catch (Exception)
            {
                Console.WriteLine("SOUND COULD NOT BE PLAYED");
            }
        }

        public static void PlayMessageSuccessSound()
        {
            try
            {
                var s = Song.FromUri("MessageSuccess", new Uri(@"Resources/HelperClasses/SoundEffects/audio/MessageSent.mp3", UriKind.Relative));
                FrameworkDispatcher.Update();
                MediaPlayer.Play(s);
            }
            catch (Exception)
            {
                Console.WriteLine("SOUND COULD NOT BE PLAYED");
            }
        }

    }
}
