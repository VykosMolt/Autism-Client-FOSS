package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;

public final class ay extends Module {
   private final BoolSetting init_BoolSetting = this.add(
      new BoolSetting("portal-overlay", "Portal Overlay", false).group("Overlay").description("Hide nether portal overlay")
   );
   private final BoolSetting load_BoolSetting = this.add(
      new BoolSetting("spyglass-overlay", "Spyglass Overlay", false).group("Overlay").description("Hide spyglass overlay")
   );
   private final BoolSetting read = this.add(new BoolSetting("nausea", "Nausea", false).group("Overlay").description("Disable nausea/portal warp"));
   private final BoolSetting write = this.add(new BoolSetting("pumpkin-overlay", "Pumpkin Overlay", false).group("Overlay").description("Hide pumpkin overlay"));
   private final BoolSetting update = this.add(
      new BoolSetting("powdered-snow-overlay", "Powdered Snow Overlay", false).group("Overlay").description("Hide powder snow overlay")
   );
   private final BoolSetting sync = this.add(new BoolSetting("fire-overlay", "Fire Overlay", false).group("Overlay").description("Hide fire overlay"));
   private final BoolSetting flush = this.add(
      new BoolSetting("liquid-overlay", "Liquid Overlay", false).group("Overlay").description("Hide underwater overlay")
   );
   private final BoolSetting close = this.add(
      new BoolSetting("in-wall-overlay", "In-Wall Overlay", false).group("Overlay").description("Hide in-block overlay")
   );
   private final BoolSetting open = this.add(new BoolSetting("vignette", "Vignette", false).group("Overlay").description("Disable screen vignette"));
   private final BoolSetting poll = this.add(new BoolSetting("gui-background", "GUI Background", false).group("Overlay").description("Hide GUI background dim"));
   private final BoolSetting push = this.add(new BoolSetting("totem-animation", "Totem Animation", false).group("Overlay").description("Hide totem animation"));
   private final BoolSetting pop = this.add(
      new BoolSetting("eating-particles", "Eating Particles", false).group("Overlay").description("Hide eating particles")
   );
   private final BoolSetting tick = this.add(
      new BoolSetting("enchantment-glint", "Enchantment Glint", false).group("Overlay").description("Hide enchantment glint")
   );
   private final BoolSetting run = this.add(
      new BoolSetting("hurt-cam", "Hurt Cam", false).group("Overlay").description("Disable hurt camera tilt and animation")
   );
   private final BoolSetting get = this.add(new BoolSetting("boss-bar", "Boss Bar", false).group("HUD").description("Hide boss bars"));
   private final BoolSetting set = this.add(new BoolSetting("scoreboard", "Scoreboard", false).group("HUD").description("Hide scoreboard"));
   private final BoolSetting put = this.add(new BoolSetting("crosshair", "Crosshair", false).group("HUD").description("Hide crosshair"));
   private final BoolSetting add = this.add(new BoolSetting("title", "Title", false).group("HUD").description("Hide on-screen titles"));
   private final BoolSetting clear = this.add(new BoolSetting("held-item-name", "Held Item Name", false).group("HUD").description("Hide held item name"));
   private final BoolSetting reset = this.add(new BoolSetting("obfuscation", "Obfuscation", false).group("HUD").description("Show obfuscated text"));
   private final BoolSetting start = this.add(new BoolSetting("potion-icons", "Potion Icons", false).group("HUD").description("Hide effect icons"));
   private final BoolSetting stop = this.add(new BoolSetting("weather", "Weather", false).group("World").description("Hide rain and snow"));
   private final BoolSetting next = this.add(new BoolSetting("world-border", "World Border", false).group("World").description("Hide world border"));
   private final BoolSetting prev = this.add(new BoolSetting("blindness", "Blindness", false).group("World").description("Disable blindness fog"));
   private final BoolSetting valid = this.add(new BoolSetting("darkness", "Darkness", false).group("World").description("Disable darkness fog"));
   private final BoolSetting size = this.add(new BoolSetting("fog", "Fog", false).group("World").description("Disable fog"));
   private final BoolSetting data = this.add(
      new BoolSetting("enchantment-table-book", "Enchant Table Book", false).group("World").description("Hide enchant table book")
   );
   private final BoolSetting value = this.add(new BoolSetting("sign-text", "Sign Text", false).group("World").description("Hide sign text"));
   private final BoolSetting cache = this.add(
      new BoolSetting("block-break-overlay", "Block Break Overlay", false).group("World").description("Hide block crack overlay")
   );
   private final BoolSetting state = this.add(
      new BoolSetting("block-break-particles", "Block Break Particles", false).group("World").description("Hide block break particles")
   );
   private final BoolSetting queue = this.add(new BoolSetting("beacon-beams", "Beacon Beams", false).group("World").description("Hide beacon beams"));
   private final BoolSetting buffer = this.add(new BoolSetting("falling-blocks", "Falling Blocks", false).group("World").description("Hide falling blocks"));
   private final BoolSetting entry = this.add(new BoolSetting("map-markers", "Map Markers", false).group("World").description("Hide map markers"));
   private final BoolSetting node = this.add(new BoolSetting("map-contents", "Map Contents", false).group("World").description("Hide map contents"));
   private final BoolSetting task = this.add(new BoolSetting("banners", "Banners", false).group("World").description("Hide banners"));
   private final BoolSetting pool = this.add(
      new BoolSetting("firework-explosions", "Firework Explosions", false).group("World").description("Hide firework explosions")
   );
   private final BoolSetting index = this.add(
      new BoolSetting("hide-all-particles", "Hide All Particles", false).group("World").description("Hide all particles")
   );
   private final BoolSetting count = this.add(
      new BoolSetting("texture-rotations", "Texture Rotations", false).group("World").description("Constant texture rotations")
   );
   private final RegistryListSetting init_RegistryListSettin = this.add(
      RegistryListSetting.blocks("block-entities", "Block Entities").group("World").description("Hide chosen block entities")
   );
   private final BoolSetting flags = this.add(new BoolSetting("sky", "Sky", false).group("Sky").description("Hide the sky"));
   private final BoolSetting level = this.add(new BoolSetting("stars", "Stars", false).group("Sky").description("Hide the stars"));
   private final BoolSetting frame = this.add(new BoolSetting("sun", "Sun", false).group("Sky").description("Hide the sun"));
   private final BoolSetting block = this.add(new BoolSetting("moon", "Moon", false).group("Sky").description("Hide the moon"));
   private final BoolSetting chunk = this.add(new BoolSetting("sunrise-glow", "Sunset Glow", false).group("Sky").description("Hide sunrise/sunset glow"));
   private final BoolSetting table = this.add(new BoolSetting("clouds", "Clouds", false).group("Sky").description("Hide the clouds"));
   private final EnumSetting<ay.Inner_DataUtil> init_EnumSetting = this.add(
      new EnumSetting<>("time", "Time", ay.Inner_DataUtil.init, ay.Inner_DataUtil.values()).group("Time & Weather").description("Force a client time")
   );
   private final IntSetting init_IntSetting = this.add(
      new IntSetting("custom-time", "Custom Time", 6000, 0, 24000, 100)
         .group("Time & Weather")
         .description("Custom time in ticks")
         .visibleWhen(() -> this.init_EnumSetting.get() == ay.Inner_DataUtil.close)
   );
   private final EnumSetting<ay.Inner_StreamUtil> load_EnumSetting = this.add(
      new EnumSetting<>("weather-changer", "Weather Changer", ay.Inner_StreamUtil.init, ay.Inner_StreamUtil.values())
         .group("Time & Weather")
         .description("Force client weather")
   );
   private final RegistryListSetting load_RegistryListSettin = this.add(
      RegistryListSetting.entityTypes("entities", "Entities").group("Entity").description("Hide chosen entities")
   );
   private final BoolSetting store = this.add(
      new BoolSetting("drop-spawn-packets", "Drop Spawn Packets", false).group("Entity").description("Drop listed spawn packets")
   );
   private final BoolSetting drain = this.add(new BoolSetting("armor", "Armor", false).group("Entity").description("Hide entity armor"));
   private final BoolSetting fill = this.add(new BoolSetting("invisibility", "Invisibility", false).group("Entity").description("Show invisible entities"));
   private final BoolSetting mark = this.add(new BoolSetting("glowing", "Glowing", false).group("Entity").description("Disable glowing outline"));
   private final BoolSetting scan = this.add(new BoolSetting("spawner-entities", "Spawner Entities", false).group("Entity").description("Hide spawner mobs"));
   private final BoolSetting parse = this.add(new BoolSetting("dead-entities", "Dead Entities", false).group("Entity").description("Hide dead entities"));
   private final BoolSetting bind = this.add(new BoolSetting("nametags", "Nametags", false).group("Entity").description("Hide entity nametags"));
   private boolean init_Z;

   public ay() {
      super("no-render", "NoRender", ae.update, "Disables rendering of selected overlays, HUD elements, world features and entities.");
   }

   @Override
   public void onEnable() {
      this.init_Z = this.count.get();
      this.init();
      load();
   }

   @Override
   public void onDisable() {
      az.init_V();
      load();
   }

   @Override
   public void tick() {
      this.init();
      boolean tr = this.count.get();
      if (tr != this.init_Z) {
         this.init_Z = tr;
         load();
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return false;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (this.isEnabled()) {
         this.init();
      }
   }

   private void init() {
      az.init_Z = this.init_BoolSetting.get();
      az.load_Z = this.load_BoolSetting.get();
      az.read = this.read.get();
      az.write = this.write.get();
      az.update = this.update.get();
      az.sync = this.sync.get();
      az.flush = this.flush.get();
      az.close = this.close.get();
      az.open = this.open.get();
      az.poll = this.poll.get();
      az.push = this.push.get();
      az.pop = this.pop.get();
      az.tick = this.tick.get();
      az.run = this.run.get();
      az.get = this.get.get();
      az.set = this.set.get();
      az.put = this.put.get();
      az.add = this.add.get();
      az.clear = this.clear.get();
      az.reset = this.reset.get();
      az.start = this.start.get();
      az.stop = this.stop.get();
      az.next = this.next.get();
      az.prev = this.prev.get();
      az.valid = this.valid.get();
      az.size = this.size.get();
      az.data = this.data.get();
      az.value = this.value.get();
      az.cache = this.cache.get();
      az.state = this.state.get();
      az.queue = this.queue.get();
      az.buffer = this.buffer.get();
      az.entry = this.entry.get();
      az.node = this.node.get();
      az.task = this.task.get();
      az.pool = this.pool.get();
      az.index = this.index.get();
      az.count = this.count.get();
      az.load_List = this.init_RegistryListSettin.get();
      az.flags = this.flags.get();
      az.level = this.level.get();
      az.frame = this.frame.get();
      az.block = this.block.get();
      az.chunk = this.chunk.get();
      az.table = this.table.get();
      ay.Inner_DataUtil tm = this.init_EnumSetting.get();
      az.store = tm != ay.Inner_DataUtil.init;
      az.init_J = this.init(tm);
      ay.Inner_StreamUtil wm = this.load_EnumSetting.get();
      az.drain = wm != ay.Inner_StreamUtil.init;
      float var10000;
      switch (wm) {
         case read:
         case update:
            var10000 = 1.0F;
            break;
         case write:
            var10000 = 0.9F;
            break;
         default:
            var10000 = 0.0F;
      }

      az.init_F = var10000;
      az.load_F = wm == ay.Inner_StreamUtil.update ? 1.0F : 0.0F;
      az.init_List = this.load_RegistryListSettin.get();
      az.f_b_Z = this.store.get();
      az.fill = this.drain.get();
      az.mark = this.fill.get();
      az.scan = this.mark.get();
      az.parse = this.scan.get();
      az.bind = this.parse.get();
      az.f_a_Z = this.bind.get();
      az.load_V();
   }

   private long init(ay.Inner_DataUtil mode) {
      long var10000;
      switch (mode) {
         case init:
            var10000 = 0L;
            break;
         case load:
            var10000 = 1000L;
            break;
         case read:
            var10000 = 6000L;
            break;
         case write:
            var10000 = 12610L;
            break;
         case update:
            var10000 = 13000L;
            break;
         case sync:
            var10000 = 18000L;
            break;
         case flush:
            var10000 = 23041L;
            break;
         case close:
            var10000 = this.init_IntSetting.get().intValue();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static void load() {
      try {
         am.init_V();
      } catch (Throwable var1) {
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
