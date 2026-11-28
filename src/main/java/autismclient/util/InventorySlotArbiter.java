package autismclient.util;

import autismclient.modules.Module;
import autismclient.modules.ClutchModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class InventorySlotArbiter {
   public static final int PRIORITY_NORMAL = 0;
   public static final int PRIORITY_CRITICAL = 100;
   public static final int OFFHAND_SLOT_INDEX = 40;
   private static final Set<String> CRITICAL_MODULES = Set.of("auto-totem", "clutch");
   private static final Object LOCK = new Object();
   private static final Map<Integer, String> slotClaims = new HashMap<>();
   private static String hotbarOwner;
   private static String actionGroupOwner;
   private static int actionGroupTick;
   private static boolean actionGroupOpen;
   private static String clickOwner;
   private static String lastClickOwner;
   private static int lastClickReleaseTick;

   private InventorySlotArbiter() {
   }

   public static int getModulePriority(String moduleId) {
      String id = normalizeModuleId(moduleId);
      return id != null && CRITICAL_MODULES.contains(id) ? 100 : 0;
   }

   public static boolean claimHotbar(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id == null) {
         return false;
      }

      synchronized (LOCK) {
         String owner = getActiveHotbarOwnerLocked();
         if (owner != null && !owner.equals(id) && getModulePriority(id) <= getModulePriority(owner)) {
            return false;
         }

         hotbarOwner = id;
         return true;
      }
   }

   public static String getHotbarOwner() {
      synchronized (LOCK) {
         return getActiveHotbarOwnerLocked();
      }
   }

   public static boolean isHotbarClaimedByOther(String moduleId) {
      String id = normalizeModuleId(moduleId);
      synchronized (LOCK) {
         String owner = getActiveHotbarOwnerLocked();
         return owner != null && !owner.equals(id);
      }
   }

   public static void releaseHotbar(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id != null) {
         synchronized (LOCK) {
            if (id.equals(hotbarOwner)) {
               hotbarOwner = null;
            }
         }
      }
   }

   public static boolean claimSlot(String moduleId, int inventorySlot) {
      String id = normalizeModuleId(moduleId);
      if (id != null && isValidInventorySlot(inventorySlot)) {
         synchronized (LOCK) {
            String owner = getActiveSlotOwnerLocked(inventorySlot);
            if (owner != null && !owner.equals(id) && getModulePriority(id) <= getModulePriority(owner)) {
               return false;
            }

            slotClaims.values().removeIf(id::equals);
            slotClaims.put(inventorySlot, id);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void releaseSlot(String moduleId, int inventorySlot) {
      String id = normalizeModuleId(moduleId);
      if (id != null && isValidInventorySlot(inventorySlot)) {
         synchronized (LOCK) {
            if (id.equals(slotClaims.get(inventorySlot))) {
               slotClaims.remove(inventorySlot);
            }
         }
      }
   }

   public static void releaseAllSlots(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id != null) {
         synchronized (LOCK) {
            slotClaims.values().removeIf(id::equals);
         }
      }
   }

   public static boolean isSlotClaimedByOther(int inventorySlot, String exceptModuleId) {
      if (!isValidInventorySlot(inventorySlot)) {
         return false;
      }

      String id = normalizeModuleId(exceptModuleId);
      synchronized (LOCK) {
         String owner = getActiveSlotOwnerLocked(inventorySlot);
         return owner != null && !owner.equals(id);
      }
   }

   public static String getSlotOwner(int inventorySlot) {
      if (!isValidInventorySlot(inventorySlot)) {
         return null;
      }

      synchronized (LOCK) {
         return getActiveSlotOwnerLocked(inventorySlot);
      }
   }

   public static boolean canBeginActionGroup(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id == null) {
         return false;
      }

      if (AutismLiteVariant.enabled() || !ClutchModule.isClutchEngaged() || "clutch".equals(id) || "auto-totem".equals(id) || "danger".equals(id) && ClutchModule.catchHolds()) {
         if (!AutismLiteVariant.enabled() && autismclient.modules.DangerModule.sync_Z_12() && !"danger".equals(id) && !"clutch".equals(id) && !"auto-totem".equals(id)) {
            return false;
         }

         synchronized (LOCK) {
            return canAcquireActionGroup(id, AutismSessionState.getInstance().getClientTick());
         }
      } else {
         return false;
      }
   }

   public static boolean beginActionGroup(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id == null) {
         return false;
      }

      if (AutismLiteVariant.enabled() || !ClutchModule.isClutchEngaged() || "clutch".equals(id) || "auto-totem".equals(id) || "danger".equals(id) && ClutchModule.catchHolds()) {
         if (!AutismLiteVariant.enabled() && autismclient.modules.DangerModule.sync_Z_12() && !"danger".equals(id) && !"clutch".equals(id) && !"auto-totem".equals(id)) {
            return false;
         }

         synchronized (LOCK) {
            int tick = AutismSessionState.getInstance().getClientTick();
            if (!canAcquireActionGroup(id, tick)) {
               return false;
            }

            actionGroupOwner = id;
            actionGroupTick = tick;
            actionGroupOpen = true;
            return true;
         }
      } else {
         return false;
      }
   }

   public static void endActionGroup(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id != null) {
         synchronized (LOCK) {
            if (id.equals(actionGroupOwner)) {
               actionGroupOpen = false;
            }
         }
      }
   }

   public static String getOpenActionGroupOwner() {
      synchronized (LOCK) {
         if (actionGroupOpen && actionGroupOwner != null) {
            return actionGroupTick == AutismSessionState.getInstance().getClientTick() ? actionGroupOwner : null;
         } else {
            return null;
         }
      }
   }

   public static boolean claimInventoryClick(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id == null) {
         return false;
      }

      synchronized (LOCK) {
         String owner = getActiveClickOwnerLocked();
         if (owner != null && !owner.equals(id) && getModulePriority(id) <= getModulePriority(owner)) {
            return false;
         }

         clickOwner = id;
         return true;
      }
   }

   public static void releaseInventoryClick(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id != null) {
         synchronized (LOCK) {
            if (id.equals(clickOwner)) {
               clickOwner = null;
               lastClickOwner = id;
               lastClickReleaseTick = AutismSessionState.getInstance().getClientTick();
            }
         }
      }
   }

   public static boolean didOtherReleaseClickRecently(String moduleId) {
      String id = normalizeModuleId(moduleId);
      synchronized (LOCK) {
         if (lastClickOwner == null) {
            return false;
         } else {
            int tick = AutismSessionState.getInstance().getClientTick();
            if (tick != lastClickReleaseTick && tick != lastClickReleaseTick + 1) {
               lastClickOwner = null;
               return false;
            } else {
               return !lastClickOwner.equals(id);
            }
         }
      }
   }

   public static void releaseAllClaims(String moduleId) {
      String id = normalizeModuleId(moduleId);
      if (id != null) {
         synchronized (LOCK) {
            if (id.equals(hotbarOwner)) {
               hotbarOwner = null;
            }

            slotClaims.values().removeIf(id::equals);
            if (id.equals(actionGroupOwner)) {
               actionGroupOwner = null;
               actionGroupOpen = false;
            }

            if (id.equals(clickOwner)) {
               clickOwner = null;
               lastClickOwner = id;
               lastClickReleaseTick = AutismSessionState.getInstance().getClientTick();
            }
         }
      }
   }

   private static boolean canAcquireActionGroup(String id, int tick) {
      return evaluateActionGroupAccess(id, tick, actionGroupOwner, actionGroupTick, actionGroupOpen);
   }

   static boolean evaluateActionGroupAccess(String id, int tick, String groupOwner, int groupTick, boolean groupOpen) {
      if (groupOwner == null || groupTick != tick) {
         return true;
      } else if (id.equals(groupOwner)) {
         return true;
      } else if (groupOpen) {
         return false;
      } else {
         return !"danger".equals(id) && !"clutch".equals(id) ? getModulePriority(id) > getModulePriority(groupOwner) : true;
      }
   }

   private static String getActiveClickOwnerLocked() {
      if (clickOwner != null && !isModuleEnabled(clickOwner)) {
         clickOwner = null;
      }

      return clickOwner;
   }

   private static String getActiveHotbarOwnerLocked() {
      if (hotbarOwner != null && !isModuleEnabled(hotbarOwner)) {
         hotbarOwner = null;
      }

      return hotbarOwner;
   }

   private static String getActiveSlotOwnerLocked(int inventorySlot) {
      String owner = slotClaims.get(inventorySlot);
      if (owner == null) {
         return null;
      } else if (!isModuleEnabled(owner)) {
         slotClaims.remove(inventorySlot);
         return null;
      } else {
         return owner;
      }
   }

   private static boolean isModuleEnabled(String moduleId) {
      Module module = autismclient.modules.al.init_Module_Str(moduleId);
      return module != null && module.isEnabled();
   }

   private static boolean isValidInventorySlot(int inventorySlot) {
      return inventorySlot >= 0 && inventorySlot < 41;
   }

   private static String normalizeModuleId(String moduleId) {
      if (moduleId == null) {
         return null;
      }

      String trimmed = moduleId.trim();
      return trimmed.isEmpty() ? null : trimmed;
   }
}
