package me.friendly.exeter.plugin.mediahud;

import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.module.ModuleManager;
import me.friendly.exeter.plugin.Plugin;

public class MediaHudPlugin extends Plugin {
  private MediaHudModule module;

  public MediaHudPlugin() {
    super("MediaHud");
  }

  @Override
  public void startPlugin() {
    module = new MediaHudModule();
    ModuleManager manager = Exeter.getInstance().getModuleManager();
    manager.register(module);
  }

  @Override
  public void stopPlugin() {
    if (module != null && module.isRunning()) {
      module.toggle();
    }
  }
}
