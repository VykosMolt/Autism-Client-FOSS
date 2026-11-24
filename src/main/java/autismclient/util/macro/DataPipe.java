package autismclient.util.macro;

import java.util.EnumSet;
import java.util.List;

public final class DataPipe {
   private DataPipe() {
   }

   public static EnumSet<DataPipe.Inner_DataUtil> init(MacroAction action) {
      EnumSet<DataPipe.Inner_DataUtil> resources = EnumSet.noneOf(DataPipe.Inner_DataUtil.class);
      if (action instanceof al) {
         return resources;
      }

      if (action instanceof NetHelper || action instanceof K || action instanceof ao || action instanceof v || action instanceof PathHelper) {
         resources.addAll(
            EnumSet.of(
               DataPipe.Inner_DataUtil.read,
               DataPipe.Inner_DataUtil.write,
               DataPipe.Inner_DataUtil.init,
               DataPipe.Inner_DataUtil.load,
               DataPipe.Inner_DataUtil.close
            )
         );
      }

      if (action instanceof ap || action instanceof TokenStore || action instanceof w) {
         resources.addAll(EnumSet.of(DataPipe.Inner_DataUtil.read, DataPipe.Inner_DataUtil.update, DataPipe.Inner_DataUtil.sync));
      }

      if (action instanceof NetHelper
         || action instanceof RefQueue
         || action instanceof MapStore
         || action instanceof PacketUtil
         || action instanceof SyncManager
         || action instanceof ah
         || action instanceof ListPool
         || action instanceof aQ
         || action instanceof ai
         || action instanceof V) {
         resources.add(DataPipe.Inner_DataUtil.init);
         resources.add(DataPipe.Inner_DataUtil.load);
      }

      if (action instanceof ar durability && durability.useNext) {
         resources.add(DataPipe.Inner_DataUtil.init);
         resources.add(DataPipe.Inner_DataUtil.load);
      }

      if (action instanceof v
         || action instanceof PathHelper
         || action instanceof JsonUtil
         || action instanceof U
         || action instanceof P
         || action instanceof TextFormat
         || action instanceof u
         || action instanceof UiHelper) {
         resources.add(DataPipe.Inner_DataUtil.load);
         resources.add(DataPipe.Inner_DataUtil.sync);
      }

      if (action instanceof Y || action instanceof x || action instanceof z || action instanceof H || action instanceof G || action instanceof W) {
         resources.add(DataPipe.Inner_DataUtil.sync);
      }

      if (action instanceof FileCache) {
         resources.add(DataPipe.Inner_DataUtil.sync);
         resources.add(DataPipe.Inner_DataUtil.flush);
      }

      if (action instanceof an) {
         resources.add(DataPipe.Inner_DataUtil.read);
         resources.add(DataPipe.Inner_DataUtil.sync);
      }

      if (action instanceof s || action instanceof ad || action instanceof ae || action instanceof NodeWalker) {
         resources.add(DataPipe.Inner_DataUtil.read);
      }

      if (action instanceof S || action instanceof ValueSink) {
         resources.add(DataPipe.Inner_DataUtil.write);
      }

      if (action instanceof CharEncoder || action instanceof q || action instanceof MethodUtil || action instanceof CachePool) {
         resources.add(DataPipe.Inner_DataUtil.update);
         resources.add(DataPipe.Inner_DataUtil.read);
         resources.add(DataPipe.Inner_DataUtil.write);
         resources.add(DataPipe.Inner_DataUtil.sync);
      }

      if (action instanceof ak) {
         resources.add(DataPipe.Inner_DataUtil.close);
      }

      if (action instanceof aq) {
         resources.add(DataPipe.Inner_DataUtil.init);
         resources.add(DataPipe.Inner_DataUtil.load);
      }

      if (action instanceof aO wait && wait.holdEmerald) {
         resources.add(DataPipe.Inner_DataUtil.init);
      }

      return resources;
   }

   public static EnumSet<DataPipe.Inner_DataUtil> init(List<MacroAction> actions) {
      EnumSet<DataPipe.Inner_DataUtil> resources = EnumSet.noneOf(DataPipe.Inner_DataUtil.class);
      if (actions == null) {
         return resources;
      }

      for (MacroAction action : actions) {
         if (action != null && action.isEnabled() && L.load(action)) {
            resources.addAll(init(action));
         }
      }

      return resources;
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
}
