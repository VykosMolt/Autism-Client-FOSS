package autismclient.protector;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;

public record PacketOriginComponentCodec(Codec<Component> init) implements Codec<Component> {
   @Override
   public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> ops, T input) {
      DataResult<Pair<Component, T>> result = this.init.decode(ops, input);
      return !PacketDecodeOriginStack.isDecoding() ? result : result.map(pair -> pair.mapFirst(c -> {
         PacketComponentOriginTracker.tag(c);
         return (Component)c;
      }));
   }

   public <T> DataResult<T> encode(Component input, DynamicOps<T> ops, T prefix) {
      return this.init(input, ops, prefix);
   }

   public <T> DataResult<T> init(Component input, DynamicOps<T> ops, T prefix) {
      return this.init.encode(input, ops, prefix);
   }
}
