package autismclient.util;

import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class VirtualInputDispatcher {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static boolean init_Z;
   private static boolean load_Z;
   private static GuardedInputTicket init_cW;
   private static GuardedInputTicket load_cW;
   private static VirtualInputDispatcher.Inner_DataUtil init_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
   private static long init_J;
   private static int init_I = -1;
   private static int load_I = -1;
   private static boolean read_Z;
   private static boolean write;
   private static VirtualInputDispatcher.Inner_DataUtil load_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
   private static VirtualInputDispatcher.Inner_DataUtil read_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
   private static long load_J;
   private static long read_J;
   private static KeyMapping init_KeyMapping;
   private static boolean update;
   private static boolean sync;

   private VirtualInputDispatcher() {
   }

   public static void init_V() {
      if (!autismclient.modules.aD.read_Z()) {
         init_Z = true;
      }
   }

   public static void load_V() {
      if (!autismclient.modules.aD.read_Z()) {
         load_Z = true;
      }
   }

   public static boolean init(String owner, BooleanSupplier guard, Consumer<Boolean> receipt) {
      if (owner != null && guard != null && receipt != null && !autismclient.modules.aD.read_Z() && init_cW == null && load_cW == null) {
         init_cW = new GuardedInputTicket(owner, guard, receipt);
         return true;
      } else {
         return false;
      }
   }

   public static boolean init_Z_Str(String owner) {
      return load_cW != null && load_cW.owner.equals(owner);
   }

   public static void init_V_Str(String owner) {
      if (init_Z_Str(owner)) {
         load_cW.markSent();
      }
   }

   public static void load(String owner) {
      if (init_cW != null && init_cW.owner.equals(owner)) {
         GuardedInputTicket old = init_cW;
         init_cW = null;
         old.read();
      }
   }

   private static void tick() {
      GuardedInputTicket old = load_cW;
      load_cW = null;
      if (old != null) {
         old.load();
      }
   }

   private static void run() {
      GuardedInputTicket old = init_cW;
      init_cW = null;
      if (old != null) {
         old.read();
      }
   }

   public static void init_V_J(long generation) {
      if (generation > 0L && !autismclient.modules.aD.read_Z()) {
         init_J = generation;
         init_V_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD);
      }
   }

   public static void read_V() {
      init_V_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.FAST_EXP);
   }

   public static void write_V() {
      init_V_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.FAST_BLOCK);
   }

   public static boolean init_Z() {
      return init_Z_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.FAST_EXP);
   }

   public static boolean load_Z() {
      return init_Z_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.FAST_BLOCK);
   }

   public static boolean init_Z_J(long generation) {
      if (generation <= 0L || load_J != generation) {
         return false;
      }

      if (!init_Z_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD)) {
         return false;
      }

      read_J = generation;
      load_J = 0L;
      return true;
   }

   public static boolean read_Z() {
      return read_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD;
   }

   public static long init_J() {
      return read_Z() ? read_J : 0L;
   }

   public static void update_V() {
      if (init_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD
         || load_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD
         || read_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD
         || init_J != 0L
         || load_J != 0L
         || read_J != 0L) {
         load(VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD);
         init_J = 0L;
         load_J = 0L;
         read_J = 0L;
      }
   }

   public static boolean write_Z() {
      return read_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.FAST_EXP;
   }

   public static boolean update_Z() {
      return load_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.FAST_EXP || load_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.FAST_BLOCK;
   }

   public static void sync_V() {
      load(VirtualInputDispatcher.Inner_DataUtil.FAST_EXP);
   }

   public static void flush_V() {
      load(VirtualInputDispatcher.Inner_DataUtil.FAST_BLOCK);
   }

   private static void init_V_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil owner) {
      if (!autismclient.modules.aD.read_Z() && owner != null && owner != VirtualInputDispatcher.Inner_DataUtil.NONE) {
         init_as_DataUtil = owner;
      }
   }

   static boolean init(boolean plainQueued, boolean scaffoldQueued) {
      return plainQueued && !scaffoldQueued;
   }

   private static boolean init_Z_as_DataUtil(VirtualInputDispatcher.Inner_DataUtil owner) {
      if (load_as_DataUtil != owner) {
         return false;
      }

      load_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      read_as_DataUtil = owner;
      return true;
   }

   private static void load(VirtualInputDispatcher.Inner_DataUtil owner) {
      if (init_as_DataUtil == owner) {
         init_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      }

      if (load_as_DataUtil == owner && init_Minecraft != null && init_Minecraft.options != null) {
         init(init_Minecraft.options.keyUse, false);
         write = false;
      }

      if (load_as_DataUtil == owner) {
         load_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      }

      if (read_as_DataUtil == owner) {
         read_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      }

      if (owner == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD) {
         init_J = 0L;
         load_J = 0L;
         read_J = 0L;
      }

      if (init_Minecraft != null && init_Minecraft.options != null && init_Minecraft.options.keyUse != null) {
         KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
      }
   }

   public static void init_V_Z(boolean held) {
      update = init(init_Minecraft != null && init_Minecraft.options != null ? init_Minecraft.options.keyAttack : null, update, held);
   }

   public static boolean init_Z_Z(boolean use) {
      return use ? sync : update;
   }

   public static void load(boolean held) {
      sync = init(init_Minecraft != null && init_Minecraft.options != null ? init_Minecraft.options.keyUse : null, sync, held);
   }

   private static boolean init(KeyMapping mapping, boolean current, boolean held) {
      if (mapping == null) {
         return false;
      }

      if (held && !mapping.isUnbound() && !autismclient.modules.aD.read_Z() && flush_Z()) {
         if (!mapping.isDown()) {
            init(mapping, true);
         }

         return true;
      } else {
         if (current) {
            if (flush_Z()) {
               init(mapping, false);
            }

            KeyMappingControl.of(mapping).autism$resetPressedState();
         }

         return false;
      }
   }

   private static void get() {
      init_V_Z(false);
      load(false);
   }

   public static void init_V_I(int slot) {
      if (!autismclient.modules.aD.read_Z()) {
         init_I = Math.max(0, Math.min(8, slot));
         load_I = -1;
      }
   }

   public static void init(int slot, int from) {
      if (!autismclient.modules.aD.read_Z()) {
         init_I = Math.max(0, Math.min(8, slot));
         load_I = from;
      }
   }

   public static void close() {
      if (!flush_Z()) {
         set();
      } else {
         boolean combatOwnsUse = ClientInteractionOverride.ownsUseInput();
         boolean combatOwnsAttack = ClientInteractionOverride.ownsAttackInput();
         if (init_cW != null && !init_cW.isValid()) {
            run();
         }

         if (init_Z && !combatOwnsAttack) {
            init(init_Minecraft.options.keyAttack, true);
            read_Z = true;
         }

         if (!combatOwnsAttack) {
            init_Z = false;
         }

         if (init_cW != null && !combatOwnsUse && !load_Z && init_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.NONE) {
            boolean physicalUse = init_Minecraft.options.keyUse.isDown() || ((AutismKeyMappingAccessor)init_Minecraft.options.keyUse).autism$clickCount() > 0;
            if (physicalUse) {
               run();
            } else {
               load_cW = init_cW;
               init_cW = null;
               init(init_Minecraft.options.keyUse, true);
               write = true;
            }
         }

         if (!combatOwnsUse && (load_Z || init_as_DataUtil != VirtualInputDispatcher.Inner_DataUtil.NONE)) {
            boolean plainWinsTick = init_as_DataUtil != VirtualInputDispatcher.Inner_DataUtil.NONE && init(load_Z, init_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD);
            load_as_DataUtil = plainWinsTick ? VirtualInputDispatcher.Inner_DataUtil.NONE : init_as_DataUtil;
            load_J = load_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.SCAFFOLD ? init_J : 0L;
            if (load_as_DataUtil != VirtualInputDispatcher.Inner_DataUtil.NONE) {
               while (init_Minecraft.options.keyUse.consumeClick()) {
               }
            }

            init(init_Minecraft.options.keyUse, true);
            write = true;
         }

         if (!AutismLiteVariant.enabled() && autismclient.modules.BedNukerModule.init_Z_23()) {
            init_I = -1;
            load_I = -1;
         }

         if (init_I >= 0
            && init_Minecraft.options.keyHotbarSlots != null
            && init_I < init_Minecraft.options.keyHotbarSlots.length
            && (load_I < 0 || init_Minecraft.player.getInventory().getSelectedSlot() == load_I)) {
            init_KeyMapping = init_Minecraft.options.keyHotbarSlots[init_I];
            init(init_KeyMapping, true);
         }

         if (!combatOwnsUse) {
            load_Z = false;
            if (load_as_DataUtil != VirtualInputDispatcher.Inner_DataUtil.NONE || init_as_DataUtil == VirtualInputDispatcher.Inner_DataUtil.NONE) {
               init_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
               init_J = 0L;
            }
         }

         init_I = -1;
         load_I = -1;
      }
   }

   public static void open() {
      if (!flush_Z()) {
         set();
      }
   }

   public static void poll() {
      tick();
      if (init_Minecraft != null && init_Minecraft.options != null) {
         if (read_Z) {
            init(init_Minecraft.options.keyAttack, false);
            read_Z = false;
         }

         if (write) {
            init(init_Minecraft.options.keyUse, false);
            write = false;
         }

         load_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
         read_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
         load_J = 0L;
         read_J = 0L;
         if (init_KeyMapping != null) {
            init(init_KeyMapping, false);
            init_KeyMapping = null;
         }
      } else {
         read_Z = false;
         write = false;
         load_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
         read_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
         load_J = 0L;
         read_J = 0L;
      }
   }

   private static void set() {
      if (sync_Z()) {
         pop();
      } else {
         push();
      }
   }

   private static boolean sync_Z() {
      return init_Minecraft == null
         || init_Minecraft.player == null
         || init_Minecraft.level == null
         || init_Minecraft.options == null
         || init_Minecraft.getWindow() == null
         || init_Minecraft.gui.screen() != null
         || init_Minecraft.gui.overlay() != null;
   }

   public static void push() {
      run();
      init_Z = false;
      load_Z = false;
      init_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      init_J = 0L;
      load_J = 0L;
      read_J = 0L;
      init_I = -1;
      load_I = -1;
      poll();
      get();
   }

   public static void pop() {
      run();
      init_Z = false;
      load_Z = false;
      init_as_DataUtil = VirtualInputDispatcher.Inner_DataUtil.NONE;
      init_J = 0L;
      load_J = 0L;
      read_J = 0L;
      init_I = -1;
      load_I = -1;
      poll();
      get();
      put();
   }

   private static void put() {
      if (init_Minecraft != null && init_Minecraft.options != null) {
         init_V_KeyMapping(init_Minecraft.options.keyUse);
         init_V_KeyMapping(init_Minecraft.options.keyAttack);
      }
   }

   private static void init_V_KeyMapping(KeyMapping mapping) {
      if (mapping != null) {
         while (mapping.consumeClick()) {
         }

         KeyMappingControl.of(mapping).autism$resetPressedState();
      }
   }

   private static boolean flush_Z() {
      return init_Minecraft != null
         && init_Minecraft.player != null
         && init_Minecraft.level != null
         && init_Minecraft.options != null
         && init_Minecraft.getWindow() != null
         && init_Minecraft.gui.screen() == null
         && init_Minecraft.gui.overlay() == null
         && !autismclient.modules.aD.init_Z();
   }

   private static void init(KeyMapping mapping, boolean pressed) {
      if (mapping != null) {
         KeyMappingControl.of(mapping).autism$simulatePress(pressed);
      }
   }

   private enum Inner_DataUtil {
      NONE,
      FAST_BLOCK,
      FAST_EXP,
      SCAFFOLD;
   }
}
