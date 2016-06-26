using Microsoft.Phone.Controls;
using System;
using System.Windows.Controls.Primitives;

namespace Rayzit.Resources.HelperClasses
{

    public class PullDetectorM
    {
        private LongListMultiSelector listbox;

        private bool viewportChanged = false;
        private bool isMoving = false;
        private double manipulationStart = 0;
        private double manipulationEnd = 0;

        public bool Bound { get; private set; }

        public void Bind(LongListMultiSelector listbox)
        {
            Bound = true;
            this.listbox = listbox;
            listbox.ManipulationStateChanged += listbox_ManipulationStateChanged;
            listbox.MouseMove += listbox_MouseMove;
            listbox.ItemRealized += OnViewportChanged;
            listbox.ItemUnrealized += OnViewportChanged;
        }

        public void Unbind()
        {
            Bound = false;

            if (listbox != null)
            {
                listbox.ManipulationStateChanged -= listbox_ManipulationStateChanged;
                listbox.MouseMove -= listbox_MouseMove;
                listbox.ItemRealized -= OnViewportChanged;
                listbox.ItemUnrealized -= OnViewportChanged;
            }
        }

        private void OnViewportChanged(object sender, ItemRealizationEventArgs e)
        {
            viewportChanged = true;
        }

        private void listbox_MouseMove(object sender, System.Windows.Input.MouseEventArgs e)
        {
            var pos = e.GetPosition(null);

            if (!isMoving)
                manipulationStart = pos.Y;
            else
                manipulationEnd = pos.Y;

            isMoving = true;
        }

        private void listbox_ManipulationStateChanged(object sender, EventArgs e)
        {
            if (listbox.ManipulationState == ManipulationState.Idle)
            {
                isMoving = false;
                viewportChanged = false;
            }
            else if (listbox.ManipulationState == ManipulationState.Manipulating)
            {
                viewportChanged = false;
            }
            else if (listbox.ManipulationState == ManipulationState.Animating)
            {
                var total = manipulationStart - manipulationEnd;

                if (!viewportChanged && Compression != null)
                {
                    if (total < 0)
                        Compression(this, new CompressionEventArgs(CompressionType.Top));
                    else if (total > 0) // Explicitly exclude total == 0 case
                        Compression(this, new CompressionEventArgs(CompressionType.Bottom));
                }
            }
        }

        public event OnCompression Compression;
    }
}