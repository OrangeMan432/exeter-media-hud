package me.friendly.exeter.plugin.mediahud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MediaInfo {
  private static final String OS = System.getProperty("os.name").toLowerCase();
  private static MediaInfo instance;

  private String title = "";
  private String artist = "";
  private long lastUpdate = 0;
  private static final long UPDATE_INTERVAL_MS = 2000;

  public static MediaInfo getInstance() {
    if (instance == null) {
      instance = new MediaInfo();
    }
    return instance;
  }

  private MediaInfo() {}

  public String getTitle() {
    updateIfNeeded();
    return title;
  }

  public String getArtist() {
    updateIfNeeded();
    return artist;
  }

  public boolean isPlaying() {
    updateIfNeeded();
    return !title.isEmpty();
  }

  private void updateIfNeeded() {
    long now = System.currentTimeMillis();
    if (now - lastUpdate < UPDATE_INTERVAL_MS) return;
    lastUpdate = now;
    fetchMediaInfo();
  }

  private void fetchMediaInfo() {
    try {
      if (isWindows()) {
        fetchWindows();
      } else if (isMac()) {
        fetchMac();
      } else if (isLinux()) {
        fetchLinux();
      }
    } catch (Exception e) {
      title = "";
      artist = "";
    }
  }

  private void fetchWindows() throws Exception {
    String script =
        "try {"
            + "  [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime] | Out-Null;"
            + "  $mgr = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync().GetAwaiter().GetResult();"
            + "  $session = $mgr.GetCurrentSession();"
            + "  if ($session -ne $null) {"
            + "    $info = $session.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();"
            + "    Write-Output ($info.Artist + '|' + $info.Title);"
            + "  }"
            + "} catch { }";

    ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", script);
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    String output = readOutput(proc);
    boolean finished = proc.waitFor(5, TimeUnit.SECONDS);
    if (!finished) {
      proc.destroyForcibly();
      title = "";
      artist = "";
      return;
    }

    if (output != null && output.contains("|")) {
      String[] parts = output.split("\\|", 2);
      artist = parts[0].trim();
      title = parts[1].trim();
    } else {
      title = "";
      artist = "";
    }
  }

  private void fetchMac() throws Exception {
    String script =
        "tell application \"Music\"\n"
            + "  if player state is playing then\n"
            + "    set a to artist of current track\n"
            + "    set t to name of current track\n"
            + "    return a & \"|\" & t\n"
            + "  end if\n"
            + "end tell\n"
            + "tell application \"Spotify\"\n"
            + "  if player state is playing then\n"
            + "    set a to artist of current track\n"
            + "    set t to name of current track\n"
            + "    return a & \"|\" & t\n"
            + "  end if\n"
            + "end tell";

    ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    String output = readOutput(proc);
    boolean finished = proc.waitFor(5, TimeUnit.SECONDS);
    if (!finished) {
      proc.destroyForcibly();
      title = "";
      artist = "";
      return;
    }

    if (output != null && output.contains("|")) {
      String[] parts = output.split("\\|", 2);
      artist = parts[0].trim();
      title = parts[1].trim();
    } else {
      title = "";
      artist = "";
    }
  }

  private void fetchLinux() throws Exception {
    ProcessBuilder pb = new ProcessBuilder("playerctl", "metadata", "--format", "{{ artist }}|{{ title }}");
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    String output = readOutput(proc);
    boolean finished = proc.waitFor(5, TimeUnit.SECONDS);
    if (!finished) {
      proc.destroyForcibly();
      title = "";
      artist = "";
      return;
    }

    if (output != null && output.contains("|")) {
      String[] parts = output.split("\\|", 2);
      artist = parts[0].trim();
      title = parts[1].trim();
    } else {
      title = "";
      artist = "";
    }
  }

  private String readOutput(Process proc) throws Exception {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    }
    return sb.toString().trim();
  }

  private static boolean isWindows() {
    return OS.contains("win");
  }

  private static boolean isMac() {
    return OS.contains("mac");
  }

  private static boolean isLinux() {
    return OS.contains("nux") || OS.contains("nix");
  }
}
