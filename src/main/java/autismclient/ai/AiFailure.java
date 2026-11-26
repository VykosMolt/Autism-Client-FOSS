package autismclient.ai;

import java.util.List;

public enum AiFailure {
   init_A(true),
   load(true),
   read(false),
   write(true),
   update(true),
   sync(true),
   flush(true),
   close(true, "game:goto", "game:surface"),
   open(true, "game:shoot", "game:goto"),
   poll(true, "game:goto", "game:explore"),
   push(true, "game:goto"),
   pop(true, "game:collect", "game:craft"),
   tick(true, "game:obtain", "game:craft"),
   run(true, "game:obtain"),
   get(true, "game:obtain", "game:craft"),
   set(true, "game:craft", "game:obtain"),
   put(true, "game:container", "game:tidy"),
   add(true, "game:entities", "game:raycast"),
   clear(true, "game:trade"),
   reset(true, "game:fight", "game:flee", "game:eat"),
   start(true, "game:eat", "game:flee"),
   stop(true, "game:eat"),
   next(true, "game:flee", "game:bucket"),
   prev(true),
   valid(true),
   size(false),
   data(false),
   value(false);

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
