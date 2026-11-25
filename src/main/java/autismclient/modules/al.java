package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.api.module.Setting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bK;
import autismclient.util.cf;
import autismclient.util.g.PkgCls_a;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class al {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Map<String, Module> init_Map = new LinkedHashMap<>();
   private static final Map<String, Boolean> load_Map = new LinkedHashMap<>();
   private static final Map<String, Boolean> read_Map = new LinkedHashMap<>();
   private static final Map<String, Boolean> write_Map = new LinkedHashMap<>();
   private static final Map<String, Set<String>> update_Map = new LinkedHashMap<>();
   private static final Set<String> init_Set = new LinkedHashSet<>();
   private static final Map<ae, List<Module>> sync_Map = new LinkedHashMap<>();
   private static int init_I;
   private static int load_I;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write_Z;
   private static final al.Inner_DataUtil<List<Module>> init_al_DataUtil = new al.Inner_DataUtil<>(() -> load_I, () -> {
      List<Module> modules = new ArrayList<>();

      for (Module module : init_Map.values()) {
         if (module.isEnabled() && !Boolean.TRUE.equals(write_Map.get(module.id()))) {
            modules.add(module);
         }
      }

      return Collections.unmodifiableList(modules);
   });
   private static final al.Inner_DataUtil<List<Module>> load_al_DataUtil = new al.Inner_DataUtil<>(() -> init_I, () -> {
      List<Module> modules = new ArrayList<>();

      for (Module module : init_Map.values()) {
         if (module.ticksWhenDisabled()) {
            modules.add(module);
         }
      }

      return Collections.unmodifiableList(modules);
   });
   private static final al.Inner_DataUtil<List<Module>> read_al_DataUtil = new al.Inner_DataUtil<>(() -> init_I, () -> {
      List<Module> modules = new ArrayList<>();

      for (Module module : init_Map.values()) {
         if (module.keybind() != -1) {
            modules.add(module);
         }
      }

      return Collections.unmodifiableList(modules);
   });
   private static final al.Inner_DataUtil<List<Module>> write_al_DataUtil = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onPacketSend", Packet.class)
   );
   private static final al.Inner_DataUtil<List<Module>> update_al_DataUtil = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onPacketReceive", Packet.class)
   );
   private static final al.Inner_DataUtil<List<Module>> sync_al_DataUtil = new al.Inner_DataUtil<>(
      () -> init_I, () -> load_List_Str_ClassArr("onPacketReceive", Packet.class)
   );
   private static final al.Inner_DataUtil<List<Module>> flush = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onSoundPacket", ClientboundSoundPacket.class)
   );
   private static final al.Inner_DataUtil<List<Module>> close = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onRenderLevel", float.class)
   );
   private static final al.Inner_DataUtil<List<Module>> open = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onBlockBreakingProgress", BlockPos.class, Direction.class)
   );
   private static final al.Inner_DataUtil<List<Module>> poll = new al.Inner_DataUtil<>(
      () -> load_I,
      () -> {
         List<Module> modules = new ArrayList<>();

         for (Module module : load_List()) {
            if (init_Z_Module_2(module, "shouldCancelStartBreakingBlock", BlockPos.class, Direction.class)
               || init_Z_Module_2(module, "onStartBreakingBlock", BlockPos.class, Direction.class)
               || init_Z_Module_2(module, "onStartDestroyBlock", BlockPos.class, Direction.class)) {
               modules.add(module);
            }
         }

         return Collections.unmodifiableList(modules);
      }
   );
   private static final al.Inner_DataUtil<List<Module>> push = new al.Inner_DataUtil<>(() -> load_I, () -> init_List_Str_ClassArr("preMovementTick"));
   private static final al.Inner_DataUtil<List<Module>> pop = new al.Inner_DataUtil<>(() -> load_I, () -> init_List_Str_ClassArr("onNetworkMovementTickPre"));
   private static final al.Inner_DataUtil<List<Module>> tick = new al.Inner_DataUtil<>(() -> load_I, () -> init_List_Str_ClassArr("onPacketProcessFrame"));
   private static final al.Inner_DataUtil<List<Module>> run = new al.Inner_DataUtil<>(() -> load_I, () -> init_List_Str_ClassArr("onInputFrame"));
   private static final al.Inner_DataUtil<List<Module>> get = new al.Inner_DataUtil<>(() -> load_I, () -> init_List_Str_ClassArr("shouldCancelPlayerTick"));
   private static final al.Inner_DataUtil<List<Module>> set = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onPlayerMove", MoverType.class, Vec3.class)
   );
   private static final al.Inner_DataUtil<List<Module>> put = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("onMouseRotation", double.class, double.class)
   );
   private static final al.Inner_DataUtil<List<Module>> add = new al.Inner_DataUtil<>(
      () -> load_I, () -> init_List_Str_ClassArr("appendTooltip", ItemStack.class, List.class)
   );
   private static final al.Inner_DataUtil<List<Module>> clear = new al.Inner_DataUtil<>(
      () -> load_I,
      () -> {
         List<Module> modules = new ArrayList<>();

         for (Module module : load_List()) {
            if (init_Z_Module_2(module, "shouldCancelAttack", HitResult.class)
               || init_Z_Module_2(module, "shouldCancelUse", HitResult.class, InteractionHand.class)) {
               modules.add(module);
            }
         }

         return Collections.unmodifiableList(modules);
      }
   );
   private static final al.Inner_DataUtil<Boolean> reset = new al.Inner_DataUtil<>(() -> load_I, () -> !close_List().isEmpty() || !open_List().isEmpty());
   private static final Set<String> load_Set = new HashSet<>();
   private static int read_I = -2147483648;

   private al() {
   }

   public static void init_V_AutismConfig(AutismConfig config) {
      if (!init_Z) {
         f.init_V();
         as.init_V_29();
         ar.init_V();
         init_Z = true;
         am.write_V();
         cf.init_V_I(init_I);
         aD.load_V();
         autismclient.util.Q.load(config);
      }
   }

   static void init_V_Module(Module module) {
      if (module != null) {
         if (module.id() == null || module.id().isBlank()) {
            throw new IllegalArgumentException("Built-in module id cannot be blank");
         }

         if (init_Map.containsKey(module.id())) {
            throw new IllegalStateException("Duplicate built-in module id: " + module.id());
         }

         init_Map.put(module.id(), module);
         init_V_Z(true);
      }
   }

   public static boolean init_Z_Module_Str(Module module, String addonId) {
      return init_AddonRegistrationR_Module_Str(module, addonId).accepted();
   }

   public static AddonRegistrationResult init_AddonRegistrationR_Module_Str(Module module, String addonId) {
      if (module == null) {
         return AddonRegistrationResult.rejected("module", "", "module was null");
      }

      if (addonId != null && !addonId.isBlank()) {
         String id = module.id();
         if (id == null || !id.contains(":")) {
            return init_AddonRegistrationR_Str(addonId, id, "non-namespaced id - scope it with AutismAddons.id(...) so it becomes addonId:localId");
         }

         if (!id.startsWith(addonId + ":")) {
            return init_AddonRegistrationR_Str(addonId, id, "foreign namespace - the id must start with your addon id; scope it with AutismAddons.id(...)");
         }

         if (init_Map.containsKey(id)) {
            return init_AddonRegistrationR_Str(addonId, id, "duplicate id");
         }

         module.markAddon();
         if (module.category() == null) {
            module.assignCategory(ae.init(addonId, autismclient.b.DataUtil.load_Str(null)));
         }

         init_Map.put(id, module);
         init_V_Z(true);
         autismclient.b.DataUtil.init_V("module", id);
         return AddonRegistrationResult.accepted("module", id);
      } else {
         return init_AddonRegistrationR_Str(addonId, "", "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }
   }

   private static AddonRegistrationResult init_AddonRegistrationR_Str(String addonId, String id, String reason) {
      AutismClientAddon.LOG.warn("[Modules] Rejecting addon module '{}': {}", id, reason);
      autismclient.b.DataUtil.init(addonId, "module", id, reason);
      return AddonRegistrationResult.rejected("module", id, reason);
   }

   public static void init_V_Str(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         boolean removed = false;
         Iterator<Entry<String, Module>> it = init_Map.entrySet().iterator();

         while (it.hasNext()) {
            Entry<String, Module> entry = it.next();
            Module module = entry.getValue();
            if (module != null && module.isAddon() && entry.getKey().startsWith(addonId + ":")) {
               try {
                  module.setEnabled(false);
               } catch (Throwable var6) {
               }

               it.remove();
               load_Map.remove(entry.getKey());
               removed = true;
            }
         }

         if (removed) {
            init_V_Z(true);
         }
      }
   }

   private static void init_V_Module(Module module, String hook, Runnable body) {
      try {
         body.run();
      } catch (Throwable t) {
         init_V_Module_2(module, hook, t);
      }
   }

   private static boolean init_Z_Module(Module module, String hook, BooleanSupplier body) {
      try {
         return body.getAsBoolean();
      } catch (Throwable t) {
         init_V_Module_2(module, hook, t);
         return false;
      }
   }

   private static void init_V_Module_2(Module module, String hook, Throwable t) {
      AutismClientAddon.LOG.error("[Modules] Addon module '{}' threw in {}; disabling it.", module.id(), hook, t);
      String id = module.id();
      int colon = id == null ? -1 : id.indexOf(58);
      if (colon > 0) {
         autismclient.b.DataUtil.load_V(id.substring(0, colon), "Module " + id + " threw in " + hook);
      }

      try {
         module.setEnabled(false);
      } catch (Throwable var7) {
      }

      try {
         AutismClientMessaging.sendPrefixed("§cAddon module '" + module.name() + "' errored and was disabled.");
      } catch (Throwable var6) {
      }
   }

   public static Collection<Module> init_Collection() {
      return Collections.unmodifiableCollection(init_Map.values());
   }

   public static List<Module> init_List_ae(ae category) {
      if (category == null) {
         return List.of();
      }

      List<Module> cached = sync_Map.get(category);
      if (cached != null) {
         return cached;
      }

      List<Module> modules = new ArrayList<>();

      for (Module module : init_Map.values()) {
         if (module.category() == category && module.showInModuleMenu()) {
            modules.add(module);
         }
      }

      List<Module> immutable = Collections.unmodifiableList(modules);
      sync_Map.put(category, immutable);
      return immutable;
   }

   public static Module init_Module_Str(String idOrName) {
      if (idOrName != null && !idOrName.isBlank()) {
         Module exact = init_Map.get(idOrName);
         if (exact != null) {
            return exact;
         }

         String needle = load_Str_Str(idOrName);
         Module direct = init_Map.get(needle);
         if (direct != null) {
            return direct;
         }

         if (read_I != init_I) {
            read_I = init_I;
            load_Set.clear();
         }

         if (load_Set.contains(idOrName)) {
            return null;
         }

         for (Module module : init_Map.values()) {
            if (load_Str_Str(module.name()).equals(needle)) {
               return module;
            }
         }

         String squashed = init_Str_Str(idOrName);

         for (Module module : init_Map.values()) {
            if (init_Str_Str(module.name()).equals(squashed) || init_Str_Str(module.id()).equals(squashed)) {
               return module;
            }
         }

         load_Set.add(idOrName);
         return null;
      } else {
         return null;
      }
   }

   private static String init_Str_Str(String value) {
      return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
   }

   public static boolean init_Z_Str_ak_StreamUtil(String idOrName, autismclient.util.macro.ak.Inner_StreamUtil mode) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.CharEncoder.init()) {
         return false;
      }

      Module module = init_Module_Str(idOrName);
      if (module == null) {
         return false;
      }

      autismclient.util.macro.ak.Inner_StreamUtil resolvedMode = mode == null ? autismclient.util.macro.ak.Inner_StreamUtil.init : mode;
      if (!aD.init_Z()
         || aD.init_Z(module)
         || resolvedMode != autismclient.util.macro.ak.Inner_StreamUtil.load && resolvedMode != autismclient.util.macro.ak.Inner_StreamUtil.init) {
         switch (resolvedMode) {
            case load:
               module.setEnabled(true);
               break;
            case read:
               module.setEnabled(false);
               break;
            default:
               module.toggle();
         }

         return true;
      } else {
         return false;
      }
   }

   public static List<String> init_List() {
      List<String> names = new ArrayList<>();

      for (Module module : init_Map.values()) {
         if (module.showInModuleMenu()) {
            names.add(module.name());
         }
      }

      return names;
   }

   public static List<Module> load_List() {
      return init_al_DataUtil.init_Obj();
   }

   public static boolean init_Z() {
      return !load_List().isEmpty();
   }

   public static boolean init_Z_Str(String idOrName) {
      Module module = init_Module_Str(idOrName);
      return module != null && module.isEnabled();
   }

   public static boolean load_Z() {
      return reset.init_Obj();
   }

   private static List<Module> sync_List() {
      return load_al_DataUtil.init_Obj();
   }

   private static List<Module> flush_List() {
      return read_al_DataUtil.init_Obj();
   }

   private static List<Module> init_List_Str_ClassArr(String methodName, Class<?>... parameterTypes) {
      List<Module> modules = new ArrayList<>();

      for (Module module : load_List()) {
         if (init_Z_Module_2(module, methodName, parameterTypes)) {
            modules.add(module);
         }
      }

      return Collections.unmodifiableList(modules);
   }

   private static List<Module> load_List_Str_ClassArr(String methodName, Class<?>... parameterTypes) {
      List<Module> modules = new ArrayList<>();

      for (Module module : sync_List()) {
         if (!module.isEnabled() && init_Z_Module_2(module, methodName, parameterTypes)) {
            modules.add(module);
         }
      }

      return Collections.unmodifiableList(modules);
   }

   private static List<Module> close_List() {
      return write_al_DataUtil.init_Obj();
   }

   private static List<Module> open_List() {
      return update_al_DataUtil.init_Obj();
   }

   private static List<Module> poll_List() {
      return sync_al_DataUtil.init_Obj();
   }

   private static List<Module> push_List() {
      return flush.init_Obj();
   }

   private static List<Module> pop_List() {
      return close.init_Obj();
   }

   private static List<Module> tick_List() {
      return open.init_Obj();
   }

   private static List<Module> run_List() {
      return poll.init_Obj();
   }

   private static List<Module> get_List() {
      return push.init_Obj();
   }

   private static List<Module> set() {
      return pop.init_Obj();
   }

   private static List<Module> put() {
      return tick.init_Obj();
   }

   private static List<Module> add() {
      return get.init_Obj();
   }

   private static List<Module> clear() {
      return set.init_Obj();
   }

   private static List<Module> reset() {
      return put.init_Obj();
   }

   private static List<Module> start() {
      return add.init_Obj();
   }

   private static List<Module> stop() {
      return clear.init_Obj();
   }

   private static boolean get_Z() {
      for (Module module : sync_List()) {
         if (!module.isEnabled() && module.hasDisabledTickWork()) {
            return true;
         }
      }

      return false;
   }

   public static boolean read_Z() {
      return init_Z && init_Minecraft != null ? !flush_List().isEmpty() || !load_List().isEmpty() || get_Z() : false;
   }

   public static boolean write_Z() {
      return !get_List().isEmpty();
   }

   public static boolean update_Z() {
      return !get_List().isEmpty() || !clear().isEmpty();
   }

   public static boolean sync_Z() {
      return !reset().isEmpty();
   }

   public static boolean flush_Z() {
      return !pop_List().isEmpty();
   }

   public static boolean close_Z() {
      return !start().isEmpty();
   }

   public static boolean open_Z() {
      return !stop().isEmpty();
   }

   public static boolean poll_Z() {
      return !push_List().isEmpty();
   }

   public static boolean push_Z() {
      return !tick_List().isEmpty();
   }

   public static boolean pop_Z() {
      return !run_List().isEmpty();
   }

   static void init_V() {
      init_V_Z(false);
   }

   static void load_V() {
      init_V_Z(true);
   }

   static void init_V_Module_Z(Module module, boolean enabled) {
      if (module != null) {
         if (module.isAddon()) {
            init_V_Module(module, enabled ? "onEnable" : "onDisable", enabled ? module::onEnable : module::onDisable);
         } else if (enabled) {
            module.onEnable();
         } else {
            module.onDisable();
         }
      }
   }

   static void read_V() {
      read_Map.clear();
      write_Map.clear();
      update_Map.clear();
      init_Set.clear();
   }

   static void write_V() {
      for (Module module : init_Map.values()) {
         if (module.isEnabled()) {
            Set<String> ids = new LinkedHashSet<>();

            for (Setting<?, ?> setting : module.settings()) {
               ids.add(setting.id());
            }

            module.applyConfiguredSettings(ids, false);
         }
      }
   }

   static void init_V_Module_4(Module module, boolean oldEnabled, boolean newEnabled) {
      if (module != null) {
         String id = module.id();
         boolean baseline = read_Map.computeIfAbsent(id, ignored -> oldEnabled);
         if (newEnabled == baseline) {
            read_Map.remove(id);
            write_Map.remove(id);
         } else {
            write_Map.put(id, newEnabled);
         }
      }
   }

   static void init_V_Module_3(Module module, String optionId, boolean reset) {
      if (module != null) {
         if (reset) {
            init_Set.add(module.id());
            update_Map.remove(module.id());
         } else if (optionId != null && !optionId.isBlank()) {
            update_Map.computeIfAbsent(module.id(), ignored -> new LinkedHashSet<>()).add(optionId);
         }
      }
   }

   public static int init_I() {
      return init_I;
   }

   public static int load_I() {
      return load_I;
   }

   private static void init_V_Z(boolean activeChanged) {
      init_I++;
      sync_Map.clear();
      load_al_DataUtil.init_V();
      read_al_DataUtil.init_V();
      sync_al_DataUtil.init_V();
      if (activeChanged) {
         load_I++;
         init_al_DataUtil.init_V();
         reset.init_V();
         write_al_DataUtil.init_V();
         update_al_DataUtil.init_V();
         flush.init_V();
         close.init_V();
         open.init_V();
         poll.init_V();
         push.init_V();
         pop.init_V();
         tick.init_V();
         run.init_V();
         get.init_V();
         set.init_V();
         put.init_V();
         add.init_V();
         clear.init_V();
      }

      if (init_Z) {
         am.write_V();
         cf.init_V_I(init_I);
      }
   }

   public static void update_V() {
      boolean locked = false;
      if (!AutismLiteVariant.enabled() && !autismclient.e.PacketUtil.init()) {
         locked = true;
      }

      if (locked != load_Z) {
         load_Z = locked;
         if (init_Z) {
            init_V_Z(true);
            as.init_V_29();
            if (!locked && read_Z && init_Minecraft != null && init_Minecraft.level != null) {
               push_V();
            }
         }
      }
   }

   public static void sync_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.FileCache.init()) && init_Z && init_Minecraft != null && !aD.init_Z() && read_Z()) {
         if (!flush_List().isEmpty()) {
            get_V();
         }

         boolean profileJoin = bK.load_Z();

         for (Module module : load_List()) {
            if (AutismLiteVariant.enabled() || !PkgCls_a.init_Z() || !init_Z_Module(module)) {
               if (profileJoin) {
                  long perf = bK.read_J();
                  load_V_Module(module);
                  bK.load("join.module.tick." + module.id(), perf);
               } else {
                  load_V_Module(module);
               }
            }
         }

         for (Module module : sync_List()) {
            if (!module.isEnabled() && module.hasDisabledTickWork()) {
               if (profileJoin) {
                  long perf = bK.read_J();
                  load_V_Module(module);
                  bK.load("join.module.tick." + module.id(), perf);
               } else {
                  load_V_Module(module);
               }
            }
         }
      }
   }

   private static void load_V_Module(Module module) {
      if (module.isAddon()) {
         init_V_Module(module, "tick", module::tick);
      } else {
         module.tick();
      }
   }

   private static boolean init_Z_Module(Module module) {
      if (module == null) {
         return false;
      }

      boolean var10000;
      switch (module.id()) {
         case "flight":
         case "speed":
         case "sprint":
         case "sneak":
         case "no-fall":
         case "boat-fly":
         case "entity-control":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public static boolean tick_Z() {
      return init_Module_Str("sneak") instanceof f.Inner_PathHelper sneak && sneak.init_Z_46();
   }

   public static void flush_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.ArrayUtil.init()) && init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : get_List()) {
            if (module.isAddon()) {
               init_V_Module(module, "preMovementTick", module::preMovementTick);
            } else {
               module.preMovementTick();
            }
         }
      }
   }

   public static void close_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.MethodUtil.init()) && init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : set()) {
            if (module.isAddon()) {
               init_V_Module(module, "onNetworkMovementTickPre", module::onNetworkMovementTickPre);
            } else {
               module.onNetworkMovementTickPre();
            }
         }
      }
   }

   public static void open_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.FileCache.init()) && init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : put()) {
            if (module.isAddon()) {
               init_V_Module(module, "onPacketProcessFrame", module::onPacketProcessFrame);
            } else {
               module.onPacketProcessFrame();
            }
         }
      }
   }

   public static void poll_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.PoolEntry.init()) && init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : run.init_Obj()) {
            if (module.isAddon()) {
               init_V_Module(module, "onInputFrame", module::onInputFrame);
            } else {
               module.onInputFrame();
            }
         }
      }
   }

   public static boolean run_Z() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.ArrayUtil.init()) {
         return false;
      }

      if (init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : add()) {
            boolean cancel = module.isAddon()
               ? init_Z_Module(module, "shouldCancelPlayerTick", module::shouldCancelPlayerTick)
               : module.shouldCancelPlayerTick();
            if (cancel) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void init_V_F(float partialTick) {
      if ((AutismLiteVariant.enabled() || autismclient.e.CharEncoder.init())
         && init_Z
         && init_Minecraft != null
         && init_Minecraft.level != null
         && init_Minecraft.player != null
         && init_Minecraft.getConnection() != null
         && !aD.init_Z()
         && flush_Z()) {
         for (Module module : pop_List()) {
            if (module.isAddon()) {
               init_V_Module(module, "onRenderLevel", () -> module.onRenderLevel(partialTick));
            } else {
               module.onRenderLevel(partialTick);
            }
         }
      }
   }

   public static void init_V_D_D(double deltaYaw, double deltaPitch) {
      if ((AutismLiteVariant.enabled() || autismclient.e.PoolEntry.init()) && init_Z && init_Minecraft != null && !aD.init_Z()) {
         for (Module module : reset()) {
            if (module.isAddon()) {
               init_V_Module(module, "onMouseRotation", () -> module.onMouseRotation(deltaYaw, deltaPitch));
            } else {
               module.onMouseRotation(deltaYaw, deltaPitch);
            }
         }
      }
   }

   public static Vec3 init_Vec3_MoverType_Vec3(MoverType type, Vec3 movement) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.RefQueue.init()) {
         return movement;
      }

      if (init_Z && init_Minecraft != null && movement != null) {
         if (aD.init_Z()) {
            return movement;
         }

         Vec3 adjusted = movement;

         for (Module module : clear()) {
            if (module.isAddon()) {
               Vec3 in = adjusted;
               adjusted = init_Obj_Module(module, "onPlayerMove", () -> module.onPlayerMove(type, in), in);
            } else {
               adjusted = module.onPlayerMove(type, adjusted);
            }
         }

         return adjusted;
      } else {
         return movement;
      }
   }

   private static <T> T init_Obj_Module(Module module, String hook, Supplier<T> body, T fallback) {
      try {
         return body.get();
      } catch (Throwable t) {
         init_V_Module_2(module, hook, t);
         return fallback;
      }
   }

   public static boolean init_Z_I(int keyCode) {
      if (init_Minecraft != null && init_Minecraft.getWindow() != null && keyCode != -1) {
         boolean pressed = autismclient.util.ValueSink.init(init_Minecraft, keyCode);
         boolean justPressed = pressed && !write_Z;
         write_Z = pressed;
         return justPressed;
      } else {
         return false;
      }
   }

   public static void push_V() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.CharEncoder.init()) {
         read_Z = true;
      } else {
         read_Z = false;
         run_V();
         boolean profileJoin = bK.load_Z();

         for (Module module : init_Map.values()) {
            if (module.isEnabled() || module.ticksWhenDisabled()) {
               if (profileJoin) {
                  long perf = bK.read_J();
                  read(module);
                  bK.load("join.module.onGameJoin." + module.id(), perf);
               } else {
                  read(module);
               }
            }
         }
      }
   }

   private static void read(Module module) {
      if (module.isAddon()) {
         init_V_Module(module, "onGameJoin", module::onGameJoin);
      } else {
         module.onGameJoin();
      }
   }

   public static void pop_V() {
      read_Z = false;
      if (AutismLiteVariant.enabled() || autismclient.e.FileCache.init()) {
         for (Module module : init_Map.values()) {
            if (module.isEnabled() || module.ticksWhenDisabled()) {
               if (module.isAddon()) {
                  init_V_Module(module, "onGameLeft", module::onGameLeft);
               } else {
                  module.onGameLeft();
               }
            }
         }

         load_Map.clear();
         write_Z = false;
      }
   }

   public static boolean init_Z_Packet(Packet<?> packet) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.FileCache.init()) {
         return false;
      }

      if (aD.init_Z()) {
         return false;
      }

      if (!load_Z()) {
         return false;
      }

      for (Module module : close_List()) {
         boolean cancel = module.isAddon() ? init_Z_Module(module, "onPacketSend", () -> module.onPacketSend(packet)) : module.onPacketSend(packet);
         if (cancel) {
            return true;
         }
      }

      return false;
   }

   public static boolean load_Z_Packet(Packet<?> packet) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.SyncManager.init()) {
         return false;
      }

      if (aD.init_Z()) {
         return false;
      }

      if (!load_Z() && poll_List().isEmpty()) {
         return false;
      }

      for (Module module : open_List()) {
         boolean cancel = module.isAddon() ? init_Z_Module(module, "onPacketReceive", () -> module.onPacketReceive(packet)) : module.onPacketReceive(packet);
         if (cancel) {
            return true;
         }
      }

      for (Module module : poll_List()) {
         boolean cancel = module.isAddon() ? init_Z_Module(module, "onPacketReceive", () -> module.onPacketReceive(packet)) : module.onPacketReceive(packet);
         if (cancel) {
            return true;
         }
      }

      return false;
   }

   public static void init_V_ClientboundSoundPa(ClientboundSoundPacket packet) {
      if ((AutismLiteVariant.enabled() || autismclient.e.RefQueue.init()) && init_Z && !aD.init_Z() && poll_Z()) {
         for (Module module : push_List()) {
            if (module.isAddon()) {
               init_V_Module(module, "onSoundPacket", () -> module.onSoundPacket(packet));
            } else {
               module.onSoundPacket(packet);
            }
         }
      }
   }

   public static void init_V_BlockPos_Direction(BlockPos pos, Direction direction) {
      if ((AutismLiteVariant.enabled() || autismclient.e.ArrayUtil.init()) && init_Z && !aD.init_Z() && push_Z()) {
         for (Module module : tick_List()) {
            if (module.isAddon()) {
               init_V_Module(module, "onBlockBreakingProgress", () -> module.onBlockBreakingProgress(pos, direction));
            } else {
               module.onBlockBreakingProgress(pos, direction);
            }
         }
      }
   }

   private static void run_V() {
      if (!write_Map.isEmpty() || !update_Map.isEmpty() || !init_Set.isEmpty()) {
         Map<String, Boolean> transitions = new LinkedHashMap<>(write_Map);
         Map<String, Set<String>> settingChanges = new LinkedHashMap<>();
         update_Map.forEach((id, values) -> settingChanges.put(id, new LinkedHashSet<>((Collection<? extends String>)values)));
         Set<String> settingResets = new LinkedHashSet<>(init_Set);
         write_Map.clear();
         read_Map.clear();
         update_Map.clear();
         init_Set.clear();

         for (Module module : init_Map.values()) {
            if (module.isEnabled()
               && !Boolean.TRUE.equals(transitions.get(module.id()))
               && (settingResets.contains(module.id()) || settingChanges.containsKey(module.id()))) {
               try {
                  module.applyConfiguredSettings(settingChanges.getOrDefault(module.id(), Set.of()), settingResets.contains(module.id()));
               } catch (Throwable error) {
                  if (module.isAddon()) {
                     init_V_Module_2(module, "deferred settings", error);
                  } else {
                     AutismClientAddon.LOG.error("[Modules] Deferred title-screen settings failed for '{}'.", module.id(), error);
                  }
               }
            }
         }

         for (Entry<String, Boolean> entry : transitions.entrySet()) {
            Module module = init_Map.get(entry.getKey());
            if (module != null && module.isEnabled() == entry.getValue()) {
               try {
                  if (entry.getValue()) {
                     module.onEnable();
                  } else {
                     module.onDisable();
                  }
               } catch (Throwable error) {
                  if (module.isAddon()) {
                     init_V_Module_2(module, entry.getValue() ? "deferred onEnable" : "deferred onDisable", error);
                  } else {
                     AutismClientAddon.LOG.error("[Modules] Deferred title-screen transition failed for '{}'.", module.id(), error);
                  }
               }
            }
         }

         init_V_Z(true);
      }
   }

   public static float init_F_F_BlockPos(float original, BlockPos pos) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.PacketUtil.init()) {
         return original;
      }

      if (init_Z && !aD.init_Z()) {
         if (init_Module_Str("fast-break") instanceof f.Inner_RenderState fastBreak
            && fastBreak.isEnabled()
            && fastBreak.init_Z_47()
            && init_Minecraft != null
            && init_Minecraft.level != null) {
            BlockState state = pos == null ? null : init_Minecraft.level.getBlockState(pos);
            return fastBreak.init(original, state, pos);
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   public static boolean init_Z_BlockPos_Direction(BlockPos pos, Direction direction) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.CharEncoder.init()) {
         return false;
      }

      if (init_Z && !aD.init_Z()) {
         if (!pop_Z()) {
            return false;
         }

         for (Module module : run_List()) {
            boolean handled = module.isAddon()
               ? init_Z_Module(module, "onStartDestroyBlock", () -> module.onStartDestroyBlock(pos, direction))
               : module.onStartDestroyBlock(pos, direction);
            if (handled) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean load_Z_BlockPos_Direction(BlockPos pos, Direction direction) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.RefQueue.init()) {
         return false;
      }

      if (init_Z && !aD.read_Z() && pop_Z()) {
         for (Module module : run_List()) {
            boolean cancel = module.isAddon()
               ? init_Z_Module(module, "shouldCancelStartBreakingBlock", () -> module.shouldCancelStartBreakingBlock(pos, direction))
               : module.shouldCancelStartBreakingBlock(pos, direction);
            if (cancel) {
               return true;
            }

            if (module.isAddon()) {
               init_V_Module(module, "onStartBreakingBlock", () -> module.onStartBreakingBlock(pos, direction));
            } else {
               module.onStartBreakingBlock(pos, direction);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean init_Z_HitResult(HitResult hitResult) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.MethodUtil.init()) {
         return false;
      }

      if (init_Z && !aD.read_Z() && open_Z()) {
         for (Module module : stop()) {
            boolean cancel = module.isAddon()
               ? init_Z_Module(module, "shouldCancelAttack", () -> module.shouldCancelAttack(hitResult))
               : module.shouldCancelAttack(hitResult);
            if (cancel) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean init_Z_HitResult_InteractionHand(HitResult hitResult, InteractionHand hand) {
      return init_Z_HitResult(hitResult, hand, null);
   }

   public static boolean init_Z_HitResult(HitResult hitResult, InteractionHand hand, String excludedModuleId) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.SyncManager.init()) {
         return false;
      }

      if (init_Z && !aD.read_Z() && open_Z()) {
         for (Module module : stop()) {
            if (excludedModuleId == null || !excludedModuleId.equals(module.id())) {
               boolean cancel = module.isAddon()
                  ? init_Z_Module(module, "shouldCancelUse", () -> module.shouldCancelUse(hitResult, hand))
                  : module.shouldCancelUse(hitResult, hand);
               if (cancel) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void init_V_ItemStack_List(ItemStack stack, List<?> lines) {
      if ((AutismLiteVariant.enabled() || autismclient.e.ArrayUtil.init()) && init_Z && !aD.read_Z() && close_Z()) {
         for (Module module : start()) {
            if (module.isAddon()) {
               init_V_Module(module, "appendTooltip", () -> module.appendTooltip(stack, lines));
            } else {
               module.appendTooltip(stack, lines);
            }
         }
      }
   }

   private static void get_V() {
      if (AutismLiteVariant.enabled() || !autismclient.ai.aP.load_Z()) {
         if (aD.init_Z()) {
            load_Map.clear();
         } else if (flush_List().isEmpty()) {
            load_Map.clear();
         } else {
            for (Module module : flush_List()) {
               int bind = module.keybind();
               boolean pressed = autismclient.util.ValueSink.init(init_Minecraft, bind);
               boolean wasPressed = load_Map.getOrDefault(module.id(), false);
               if (module.holdToActivate()) {
                  if (pressed != wasPressed && autismclient.util.at.init() && module.hasActivationToggle() && module.isEnabled() != pressed) {
                     module.setEnabled(pressed);
                  }
               } else if (pressed && !wasPressed && autismclient.util.at.init() && module.hasActivationToggle()) {
                  module.toggle();
               }

               load_Map.put(module.id(), pressed);
            }
         }
      }
   }

   private static boolean load_Z_Module(Module module) {
      return module == null ? false : init_Z_Module_2(module, "onPacketSend", Packet.class) || init_Z_Module_2(module, "onPacketReceive", Packet.class);
   }

   public static List<Module> read_List() {
      return run_List();
   }

   public static List<Module> write_List() {
      return stop();
   }

   public static List<Module> update_List() {
      return start();
   }

   private static boolean init_Z_Module_2(Module module, String methodName, Class<?>... parameterTypes) {
      if (module != null && methodName != null && !methodName.isBlank()) {
         try {
            return module.getClass().getMethod(methodName, parameterTypes).getDeclaringClass() != Module.class;
         } catch (NoSuchMethodException ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void tick_V() {
      load_Map.clear();
      write_Z = false;
   }

   private static String load_Str_Str(String value) {
      return value.toLowerCase(Locale.ROOT).replace(' ', '-').replace("_", "-");
   }

   private static final class Inner_DataUtil<T> {
      private final IntSupplier init_IntSupplier;
      private final Supplier<T> init_Supplier;
      private T init_Obj;
      private int init_I = -1;

      Inner_DataUtil(IntSupplier currentRevision, Supplier<T> builder) {
         this.init_IntSupplier = currentRevision;
         this.init_Supplier = builder;
      }

      T init_Obj() {
         int rev = this.init_IntSupplier.getAsInt();
         if (this.init_I == rev) {
            return this.init_Obj;
         }

         this.init_Obj = this.init_Supplier.get();
         this.init_I = rev;
         return this.init_Obj;
      }

      void init_V() {
         this.init_I = -1;
         this.init_Obj = null;
      }
   }
}
