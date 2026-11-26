package autismclient.ai;

import java.util.List;

public enum AiFailure {
   TARGET_NOT_FOUND(true),
   NO_REACHABLE_TARGET(true),
   UNBREAKABLE_OBSTACLE(false),
   PLACEMENT_IMPOSSIBLE(true),
   WORLD_UNKNOWN(true),
   STUCK(true),
   EXECUTION_FAILED(true),
   UNREACHABLE(true, "game:goto", "game:surface"),
   UNREACHABLE_AIRBORNE(true, "game:shoot", "game:goto"),
   NO_ROUTE(true, "game:goto", "game:explore"),
   LAVA_DETECTED(true, "game:goto"),
   MISSING_MATERIALS(true, "game:collect", "game:craft"),
   MISSING_TOOL(true, "game:obtain", "game:craft"),
   NO_RANGED(true, "game:obtain"),
   TOOL_WORN(true, "game:obtain", "game:craft"),
   MISSING_STATION(true, "game:craft", "game:obtain"),
   INVENTORY_FULL(true, "game:container", "game:tidy"),
   TARGET_GONE(true, "game:entities", "game:raycast"),
   OUT_OF_STOCK(true, "game:trade"),
   THREAT_INTERRUPT(true, "game:fight", "game:flee", "game:eat"),
   LOW_HEALTH(true, "game:eat", "game:flee"),
   HUNGER(true, "game:eat"),
   ENVIRONMENTAL_HAZARD(true, "game:flee", "game:bucket"),
   TIMEOUT(true),
   WORLD_CHANGED(true),
   CANCELLED(false),
   IMPOSSIBLE_STATE(false),
   UNKNOWN(false);

   private final boolean init_Z;
   private final List<String> init_List;

   AiFailure(boolean recoverable, String... suggestedActions) {
      this.init_Z = recoverable;
      this.init_List = List.of(suggestedActions);
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public List<String> init_List() {
      return this.init_List;
   }
}
