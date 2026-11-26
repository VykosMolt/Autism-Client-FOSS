package autismclient.protector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

public final class VanillaKnownPacksManager extends KnownPacksManager {
   private final PackRepository init_PackRepository = ServerPacksSource.createVanillaTrustedRepository();
   private final Map<KnownPack, String> init_Map;

   public VanillaKnownPacksManager() {
      this.init_PackRepository.reload();
      Builder<KnownPack, String> knownPacks = ImmutableMap.builder();
      this.init_PackRepository.getAvailablePacks().forEach(pack -> {
         PackLocationInfo location = pack.location();
         location.knownPackInfo().ifPresent(knownPack -> knownPacks.put(knownPack, location.id()));
      });
      this.init_Map = knownPacks.build();
   }

   public static boolean init(ConnectionPrivacyRegistry.ConnectionSession context) {
      return context == null || context.init_BufferHelper_Cache() != ConnectionPrivacyRegistry.PrivacyMode.MODDED || !ConnectionPrivacyRegistry.init_Z(context);
   }

   @Override
   public List<KnownPack> trySelectingPacks(List<KnownPack> packsToSelect) {
      List<KnownPack> response = new ArrayList<>(packsToSelect.size());
      List<String> selectedPacks = new ArrayList<>(packsToSelect.size());

      for (KnownPack knownPack : packsToSelect) {
         String knownPackId = this.init_Map.get(knownPack);
         if (knownPackId != null) {
            selectedPacks.add(knownPackId);
            response.add(knownPack);
         }
      }

      this.init_PackRepository.setSelected(selectedPacks);
      return response;
   }

   @Override
   public CloseableResourceManager createResourceManager() {
      return new MultiPackResourceManager(PackType.SERVER_DATA, this.init_PackRepository.openAllSelected());
   }
}
