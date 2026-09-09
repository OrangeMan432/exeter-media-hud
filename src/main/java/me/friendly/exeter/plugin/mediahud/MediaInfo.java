package me.friendly.exeter.plugin.mediahud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaInfo {
  private static final String OS = System.getProperty("os.name").toLowerCase();
  private static MediaInfo instance;

  private volatile String title = "";
  private volatile String artist = "";
  private volatile boolean playing = false;

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MediaHud-Poller");
        t.setDaemon(true);
        return t;
      });

  public static MediaInfo getInstance() {
    if (instance == null) {
      instance = new MediaInfo();
    }
    return instance;
  }

  private MediaInfo() {
    if (isWindows()) {
      scheduler.scheduleAtFixedRate(this::pollWindows, 0, 2, TimeUnit.SECONDS);
    } else if (isMac()) {
      scheduler.scheduleAtFixedRate(this::pollMac, 0, 2, TimeUnit.SECONDS);
    } else if (isLinux()) {
      scheduler.scheduleAtFixedRate(this::pollLinux, 0, 2, TimeUnit.SECONDS);
    }
  }

  public String getTitle() { return title; }
  public String getArtist() { return artist; }
  public boolean isPlaying() { return playing; }

  private void pollWindows() {
    try {
      Path pluginsDir = Paths.get("config", "exeter", "plugins");
      Path smtcExe = pluginsDir.resolve("smtc.exe");
      if (!java.nio.file.Files.exists(smtcExe)) {
        try (var in = getClass().getResourceAsStream("/smtc.exe")) {
          if (in != null) {
            java.nio.file.Files.copy(in, smtcExe);
          }
        }
      }
      if (!java.nio.file.Files.exists(smtcExe)) {
        title = "";
        artist = "";
        playing = false;
        return;
      }

      ProcessBuilder pb = new ProcessBuilder(smtcExe.toAbsolutePath().toString());
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String output = readOutput(proc);
      boolean ok = proc.waitFor(4, TimeUnit.SECONDS);
      if (!ok) proc.destroyForcibly();

      if (output != null && output.contains("|")) {
        String[] parts = output.split("\\|", 2);
        String a = parts[0].trim();
        String t = parts[1].trim();
        if (!t.isEmpty()) {
          artist = a;
          title = t;
          playing = true;
          return;
        }
      }
    } catch (Exception e) {
      // fall through
    }
    title = "";
    artist = "";
    playing = false;
  }

  private void pollMac() {
    try {
      String script = "tell application \"Music\"\n"
          + "  if player state is playing then\n"
          + "    return artist of current track & \"|\" & name of current track\n"
          + "  end if\nend tell\n"
          + "tell application \"Spotify\"\n"
          + "  if player state is playing then\n"
          + "    return artist of current track & \"|\" & name of current track\n"
          + "  end if\nend tell";

      ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String output = readOutput(proc);
      boolean ok = proc.waitFor(4, TimeUnit.SECONDS);
      if (!ok) proc.destroyForcibly();

      if (output != null && output.contains("|")) {
        String[] parts = output.split("\\|", 2);
        artist = parts[0].trim();
        title = parts[1].trim();
        playing = !title.isEmpty();
        return;
      }
    } catch (Exception e) {
      // fall through
    }
    title = "";
    artist = "";
    playing = false;
  }

  private void pollLinux() {
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "playerctl", "metadata", "--format", "{{ artist }}|{{ title }}");
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String output = readOutput(proc);
      boolean ok = proc.waitFor(4, TimeUnit.SECONDS);
      if (!ok) proc.destroyForcibly();

      if (output != null && output.contains("|")) {
        String[] parts = output.split("\\|", 2);
        artist = parts[0].trim();
        title = parts[1].trim();
        playing = !title.isEmpty();
        return;
      }
    } catch (Exception e) {
      // fall through
    }
    title = "";
    artist = "";
    playing = false;
  }

  private String readOutput(Process proc) throws Exception {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    }
    return sb.toString().trim();
  }

  private static boolean isWindows() { return OS.contains("win"); }
  private static boolean isMac() { return OS.contains("mac"); }
  private static boolean isLinux() { return OS.contains("nux") || OS.contains("nix"); }
}
