package autismclient.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

public final class TokenStore extends df<BitSetUtil> implements Iterable<BitSetUtil> {
   private static final TokenStore init_TokenStore = new TokenStore();

   private TokenStore() {
   }

   public static TokenStore init_TokenStore() {
      init_TokenStore.load_V();
      aV.init();
      init_TokenStore.init_V();
      return init_TokenStore;
   }

   private synchronized void init_V() {
      boolean changed = false;

      for (BitSetUtil account : this.init_List) {
         if (account != null) {
            account.write_Str();
            changed |= account.init_Z;
            account.init_Z = false;
         }
      }

      if (changed) {
         this.read_V();
      }
   }

   public synchronized BitSetUtil init_BitSetUtil(String id) {
      if (id != null && !id.isBlank()) {
         for (BitSetUtil account : this.init_List) {
            if (id.equals(account.write_Str())) {
               return account;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public synchronized void init_V(BitSetUtil resolved) {
      if (resolved != null && resolved.init_Str != null) {
         BitSetUtil stored = this.init_BitSetUtil(resolved.init_Str);
         if (stored != null) {
            stored.load_Str = resolved.load_Str;
            stored.read_Str = resolved.read_Str;
            stored.write = resolved.write;
            stored.update = resolved.update;
            stored.sync = resolved.sync;
            stored.init_J = resolved.init_J;
            this.read_V();
         }
      }
   }

   public synchronized boolean init(String accountId, String newLabel) {
      if (newLabel != null && !newLabel.isBlank()) {
         BitSetUtil stored = this.init_BitSetUtil(accountId);
         if (stored != null && stored.init_MethodUtil == MethodUtil.init) {
            String trimmed = newLabel.trim();
            if (trimmed.equals(stored.load_Str)) {
               return true;
            }

            for (BitSetUtil account : this.init_List) {
               if (account != stored && trimmed.equalsIgnoreCase(account.load_Str)) {
                  return false;
               }
            }

            stored.load_Str = trimmed;
            stored.update = trimmed;
            stored.sync = UUIDUtil.createOfflinePlayerUUID(trimmed).toString();
            this.read_V();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public synchronized void init_V(String accountId) {
      BitSetUtil stored = this.init_BitSetUtil(accountId);
      if (stored != null && stored.init_MethodUtil == MethodUtil.read) {
         stored.read_Str = "";
         stored.init_J = 0L;
         this.read_V();
      }
   }

   @Override
   protected File init_File() {
      return new File(Minecraft.getInstance().gameDirectory, "autism-accounts.nbt");
   }

   @Override
   protected String init_Str() {
      return "accounts";
   }

   @Override
   protected BitSetUtil init_Obj_CompoundTag(CompoundTag tag) {
      return new BitSetUtil().init_BitSetUtil_CompoundTag(tag);
   }

   @Override
   protected CompoundTag init_CompoundTag_Obj(BitSetUtil item) {
      return item.init_CompoundTag();
   }

   @Override
   protected String load_Str() {
      return "Autism accounts";
   }

   public synchronized void load(BitSetUtil account) {
      if (account != null) {
         this.init_List.add(account);
         this.read_V();
      }
   }

   public synchronized int init(List<BitSetUtil> accounts) {
      if (accounts != null && !accounts.isEmpty()) {
         int added = 0;

         for (BitSetUtil account : accounts) {
            if (account != null) {
               this.init_List.add(account);
               added++;
            }
         }

         if (added > 0) {
            this.read_V();
         }

         return added;
      } else {
         return 0;
      }
   }

   public synchronized void read(BitSetUtil account) {
      if (this.init_List.remove(account)) {
         this.read_V();
      }
   }

   public synchronized int init_I() {
      int removed = 0;
      Iterator<BitSetUtil> iterator = this.init_List.iterator();

      while (iterator.hasNext()) {
         if (iterator.next().init_BitSetUtil_DataUti == BitSetUtil.Inner_DataUtil.write) {
            iterator.remove();
            removed++;
         }
      }

      if (removed > 0) {
         this.read_V();
      }

      return removed;
   }

   public void write(BitSetUtil account) {
      if (account != null) {
         Thread thread = new Thread(() -> {
            if (account.init_Z() && account.read_Z()) {
               this.read_V();
               AutismClientMessaging.sendPrefixed("Logged in as " + account.init_Str() + ".");
            } else {
               this.auditSaveRefreshed(account);
               AutismClientMessaging.sendPrefixed("Failed to login account: " + account.init_Str() + account.read_Str());
            }
         }, "Autism-Account-Login");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public void update(BitSetUtil account) {
      if (account != null && account.init_MethodUtil == MethodUtil.read) {
         aW.init(refreshToken -> {
            if (refreshToken == null) {
               AutismClientMessaging.sendPrefixed("Microsoft login cancelled or failed.");
            } else {
               account.load_Str = refreshToken;
               Thread thread = new Thread(() -> {
                  if (account.init_Z() && account.read_Z()) {
                     synchronized (this) {
                        if (!this.init_List.contains(account)) {
                           this.init_List.add(account);
                        }
                     }

                     this.read_V();
                     AutismClientMessaging.sendPrefixed("Logged in as " + account.init_Str() + ".");
                  } else {
                     this.auditSaveRefreshed(account);
                     AutismClientMessaging.sendPrefixed("Failed to login Microsoft account" + account.read_Str() + ".");
                  }
               }, "Autism-Microsoft-Login");
               thread.setDaemon(true);
               thread.start();
            }
         });
      }
   }

   // A refresh can rotate the token even when the session apply fails; keep the new token on disk.
   private synchronized void auditSaveRefreshed(BitSetUtil account) {
      if (account.init_Z && this.init_List.contains(account)) {
         account.init_Z = false;
         this.read_V();
      }
   }

   @Override
   public Iterator<BitSetUtil> iterator() {
      return this.init_List().iterator();
   }
}
