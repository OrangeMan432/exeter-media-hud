using System;
using Windows.Media.Control;

class Program {
    static int Main() {
        try {
            var mgr = GlobalSystemMediaTransportControlsSessionManager.RequestAsync().GetAwaiter().GetResult();
            var session = mgr.GetCurrentSession();
            if (session == null) {
                Console.WriteLine("NO_SESSION");
                return 0;
            }
            var props = session.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            string artist = props.Artist ?? "";
            string title = props.Title ?? "";
            if (string.IsNullOrEmpty(title)) {
                Console.WriteLine("NO_TRACK");
            } else {
                Console.WriteLine(artist + "|" + title);
            }
        } catch (Exception ex) {
            Console.WriteLine("ERROR:" + ex.Message);
        }
        return 0;
    }
}
