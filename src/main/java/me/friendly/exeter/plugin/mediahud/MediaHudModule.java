package me.friendly.exeter.plugin.mediahud;

import me.friendly.api.minecraft.render.font.FontUtil;
import me.friendly.exeter.module.impl.active.render.Colors;
import me.friendly.exeter.module.impl.toggle.render.hud.HudModule;

public class MediaHudModule extends HudModule {

  public MediaHudModule() {
    super("MediaHud", new String[] {"media", "nowplaying", "music"}, Corner.TOP_LEFT);
    setDescription("Displays the currently playing track on screen.");
    this.offerProperties();
  }

  @Override
  public int getWidth() {
    MediaInfo media = MediaInfo.getInstance();
    if (!media.isPlaying()) return 0;
    String title = media.getTitle();
    String artist = media.getArtist();
    int titleW = FontUtil.getStringWidth(title);
    int artistW = artist.isEmpty() ? 0 : FontUtil.getStringWidth(artist);
    return Math.max(titleW, artistW);
  }

  @Override
  public int getHeight() {
    return MediaInfo.getInstance().isPlaying() ? 18 : 0;
  }

  @Override
  public void render(int scaledWidth, int scaledHeight) {
    MediaInfo media = MediaInfo.getInstance();
    if (!media.isPlaying()) return;

    String title = media.getTitle();
    String artist = media.getArtist();

    FontUtil.drawString(title, getX(), getY(), Colors.getHudMain());

    if (!artist.isEmpty()) {
      FontUtil.drawString(artist, getX(), getY() + 9, Colors.getHudAccent());
    }
  }
}
