package me.friendly.exeter.plugin.mediahud;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.render.font.FontUtil;
import me.friendly.exeter.events.RenderGameOverlayEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;

public class MediaHudModule extends ToggleableModule {
  private static final int X = 2;
  private static final int Y = 12;

  private final Listener<RenderGameOverlayEvent> renderListener =
      new Listener<RenderGameOverlayEvent>("media_hud_render_listener") {
        @Override
        public void call(RenderGameOverlayEvent event) {
          if (event.getType() != RenderGameOverlayEvent.Type.IN_GAME) return;
          renderMediaInfo();
        }
      };

  public MediaHudModule() {
    super("MediaHud", new String[] {"media", "nowplaying", "music"}, ModuleType.RENDER);
    setDescription("Displays the currently playing track on screen.");
  }

  @Override
  protected void onEnable() {
    super.onEnable();
  }

  @Override
  protected void onDisable() {
    super.onDisable();
  }

  private void renderMediaInfo() {
    MediaInfo media = MediaInfo.getInstance();
    if (!media.isPlaying()) return;

    String title = media.getTitle();
    String artist = media.getArtist();

    FontUtil.drawString(title, X, Y, -1);

    if (!artist.isEmpty()) {
      FontUtil.drawString(artist, X, Y + 9, 0xFF888888);
    }
  }
}
