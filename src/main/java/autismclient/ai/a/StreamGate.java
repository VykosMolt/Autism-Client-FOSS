package autismclient.ai.a;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class StreamGate implements NodeWalker.Inner_CachePool {
   private static final int flowState = 65536;
   private static final int flowTemp = 4096;
   private static final int readEx_I = 4096;
   private static final int value_I = 4096;
   private final ClassCache.Inner_StateCache putAt_FileStore_TextStor;
   private final ClassCache.Inner_RenderState.Inner_CachePool putAt_FileStore_IoHelper;
   private double putAt_D;
   private static final AABB[] putAt_AABBArr = new AABB[0];
   private final AABB[][] putAt_AABBArr2 = new AABB[4096][];
   private final long[] putAt_JArr = new long[4096];
   private final AABB[][] fill_AABBArr2 = new AABB[4096][];
   private final long[] fill_JArr = new long[4096];
   private final int[] putAt_IArr = new int[4096];
   private boolean putAt_Z;
   private int prevEx_I;
   private int prev_I;
   private int chunkAt_I;
   private int state_I;
   private int closeOf_I;
   private int pollAt;
   private AABB[] fill_AABBArr;
   final double[] putAt_DArr = new double[3];
   final double[] fill_DArr = new double[3];
   final float[] putAt_FArr = new float[16];
   private final Long2ShortOpenHashMap putAt_Long2ShortOpenHash = new Long2ShortOpenHashMap();
   private final long[] readEx_JArr = new long[4096];
   private final int[] fill_IArr = new int[4096];
   private final Long2ByteOpenHashMap putAt_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap fill_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap readEx_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final long[] value_JArr = new long[4096];
   private final byte[] putAt_BArr = new byte[4096];
   private final long[] prevEx_JArr = new long[4096];
   private final byte[] fill_BArr = new byte[4096];
   private final Long2IntOpenHashMap putAt_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private final long[] prev_JArr = new long[4096];
   private final int[] readEx_IArr = new int[4096];
   private final long[] chunkAt_JArr = new long[4096];
   private final long[] state_JArr = new long[4096];
   private final long[] closeOf_JArr = new long[4096];
   private final byte[] readEx_BArr = new byte[4096];
   private final Long2IntOpenHashMap fill_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private boolean fill_Z;
   private final LongArrayList putAt_LongArrayList = new LongArrayList();
   private final IntArrayList putAt_IntArrayList = new IntArrayList();
   private final LongOpenHashSet putAt_LongOpenHashSet = new LongOpenHashSet();
   private ClassCache.UiHelper putAt_FileStore_PacketHe;
   private t.Inner_DataUtil putAt_DataCodec_CacheHel;
   private boolean readEx_Z;
   long putAt_J;

   @Override
   public double putAt_D() {
      return this.putAt_FileStore_IoHelper == null ? NodeWalker.Inner_CachePool.super.putAt_D() : this.putAt_FileStore_IoHelper.putAt_FileStore_RingUtil.putAt_D;
   }

   @Override
   public double movementSpeed() {
      return this.putAt_D;
   }

   void putAt(double var1) {
      this.putAt_D = var1;
   }

   boolean fill() {
      return this.putAt_FileStore_IoHelper != null && this.putAt_FileStore_IoHelper.fill_Z;
   }

   StreamGate(ClassCache.Inner_StateCache var1, ClassCache.Inner_RenderState.Inner_CachePool var2) {
      this.putAt_FileStore_TextStor = var1;
      this.putAt_FileStore_IoHelper = var2;
      this.putAt_D = var2 == null ? var1.movementSpeed() : var2.putAt_D;
      this.putAt_Long2ShortOpenHash.defaultReturnValue((short)-32768);
      this.putAt_Long2ByteOpenHashM.defaultReturnValue((byte)-1);
      this.fill_Long2ByteOpenHashM.defaultReturnValue((byte)-1);
      this.readEx_Long2ByteOpenHashM.defaultReturnValue((byte)-1);
      Arrays.fill(this.putAt_BArr, (byte)-1);
      Arrays.fill(this.fill_BArr, (byte)-1);
      this.putAt_Long2IntOpenHashMa.defaultReturnValue(-1);
   }

   @Override
   public List<AABB> collisions(AABB var1) {
      ArrayList var2 = null;

      for (AABB var6 : this.fill(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ)) {
         if (var6.intersects(var1)) {
            if (var2 == null) {
               var2 = new ArrayList(4);
            }

            var2.add(var6);
         }
      }

      return var2 == null ? List.of() : var2;
   }

   @Override
   public boolean putAt_Z_AABB(AABB var1) {
      return this.putAt_Z_D(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
   }

   @Override
   public boolean putAt_Z_D(double var1, double var3, double var5, double var7, double var9, double var11) {
      for (AABB var16 : this.putAt_AABBArr_D(var1, var3, var5, var7, var9, var11)) {
         if (var16.intersects(var1, var3, var5, var7, var9, var11)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public long putAt_J_AABB(AABB var1, double var2, double var4, double var6) {
      return this.putAt(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2, var4, var6);
   }

   @Override
   public long putAt(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17) {
      return NodeWalker.putAt(var1, var3, var5, var7, var9, var11, var13, var15, var17, null, this.putAt_AABBArr_D(var1, var3, var5, var7, var9, var11));
   }

   @Override
   public Vec3 putAt_Vec3_Vec3(Vec3 var1, AABB var2, AABB var3) {
      return this.putAt(
         var1.x,
         var1.y,
         var1.z,
         var2.minX,
         var2.minY,
         var2.minZ,
         var2.maxX,
         var2.maxY,
         var2.maxZ,
         var3.minX,
         var3.minY,
         var3.minZ,
         var3.maxX,
         var3.maxY,
         var3.maxZ
      );
   }

   @Override
   public Vec3 putAt(
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      double var17,
      double var19,
      double var21,
      double var23,
      double var25,
      double var27,
      double var29
   ) {
      double[] var31 = new double[3];
      this.putAt(var31, var1, var3, var5, var7, var9, var11, var13, var15, var17, var19, var21, var23, var25, var27, var29);
      return new Vec3(var31[0], var31[1], var31[2]);
   }

   long putAt(
      double[] var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      double var22,
      double var24,
      double var26,
      double var28,
      double var30
   ) {
      long var32 = this.putAt_J;
      int var34 = (int)Math.floor(var20);
      int var35 = (int)Math.floor(var26);
      int var36 = (int)Math.floor(var22) - 1;
      int var37 = (int)Math.floor(var28);
      int var38 = (int)Math.floor(var24);
      int var39 = (int)Math.floor(var30);
      AABB[] var40 = this.putAt_AABBArr_I(var34, var36, var38, var35, var37, var39);
      if (this.putAt_FileStore_IoHelper != null && this.putAt_J == var32) {
         this.putAt_Z = true;
         this.prevEx_I = var34;
         this.prev_I = var36;
         this.chunkAt_I = var38;
         this.state_I = var35;
         this.closeOf_I = var37;
         this.pollAt = var39;
         this.fill_AABBArr = var40;
      } else {
         this.putAt_Z = false;
         this.fill_AABBArr = null;
      }

      if (var40.length == 0) {
         var1[0] = var2;
         var1[1] = var4;
         var1[2] = var6;
         return 0L;
      }

      if (var40.length >= 64) {
         Vec3 var44 = NodeWalker.Inner_CachePool.super.putAt_Vec3_Vec3(
            new Vec3(var2, var4, var6), new AABB(var8, var10, var12, var14, var16, var18), new AABB(var20, var22, var24, var26, var28, var30)
         );
         var1[0] = var44.x;
         var1[1] = var44.y;
         var1[2] = var44.z;
         return -1L;
      }

      long var41 = 0L;

      for (int var43 = 0; var43 < var40.length; var43++) {
         if (var40[var43].intersects(var20, var22, var24, var26, var28, var30)) {
            var41 |= 1L << var43;
         }
      }

      if (var41 == 0L) {
         var1[0] = var2;
         var1[1] = var4;
         var1[2] = var6;
      } else {
         NodeWalker.putAt(var1, var2, var4, var6, var8, var10, var12, var14, var16, var18, var40, var41);
      }

      return var41;
   }

   AABB[] putAt_AABBArr() {
      return this.fill_AABBArr;
   }

   AABB[] putAt_AABBArr_D(double var1, double var3, double var5, double var7, double var9, double var11) {
      this.putAt_V_2();
      int var13 = (int)Math.floor(var1);
      int var14 = (int)Math.floor(var7);
      int var15 = (int)Math.floor(var3) - 1;
      int var16 = (int)Math.floor(var9);
      int var17 = (int)Math.floor(var5);
      int var18 = (int)Math.floor(var11);
      return this.putAt_Z
            && var13 >= this.prevEx_I
            && var14 <= this.state_I
            && var15 >= this.prev_I
            && var16 <= this.closeOf_I
            && var17 >= this.chunkAt_I
            && var18 <= this.pollAt
         ? this.fill_AABBArr
         : this.putAt_AABBArr_I(var13, var15, var17, var14, var16, var18);
   }

   private AABB[] fill(double var1, double var3, double var5, double var7, double var9, double var11) {
      return this.putAt_AABBArr_I(
         (int)Math.floor(var1), (int)Math.floor(var3) - 1, (int)Math.floor(var5), (int)Math.floor(var7), (int)Math.floor(var9), (int)Math.floor(var11)
      );
   }

   private AABB[] putAt_AABBArr_I(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.putAt_V_2();
      int var7 = var4 - var1;
      int var8 = var5 - var2;
      int var9 = var6 - var3;
      boolean var10 = this.putAt_FileStore_IoHelper != null && (var7 | var8 | var9) < 16;
      long var11 = BlockPos.asLong(var1, var2, var3);
      int var13 = var7 << 8 | var8 << 4 | var9;
      int var14 = (int)HashCommon.mix(var11 ^ var13 * -7046029254386353131L) & this.fill_AABBArr2.length - 1;
      if (var10 && this.fill_AABBArr2[var14] != null && this.fill_JArr[var14] == var11 && this.putAt_IArr[var14] == var13) {
         return this.fill_AABBArr2[var14];
      }

      long var15 = this.putAt_J;
      ArrayList var17 = null;
      double var18 = var2 + 1;

      for (int var20 = var1; var20 <= var4; var20++) {
         for (int var21 = var2; var21 <= var5; var21++) {
            for (int var22 = var3; var22 <= var6; var22++) {
               AABB[] var23 = this.putAt_AABBArr_I(var20, var21, var22);
               if (var23.length != 0) {
                  if (var21 == var2) {
                     for (AABB var32 : var23) {
                        if (!(var32.maxY <= var18)) {
                           if (var17 == null) {
                              var17 = new ArrayList(4);
                           }

                           var17.add(var32);
                        }
                     }
                  } else {
                     if (var17 == null) {
                        var17 = new ArrayList(4);
                     }

                     for (AABB var27 : var23) {
                        var17.add(var27);
                     }
                  }
               }
            }
         }
      }

      AABB[] var28 = var17 == null ? putAt_AABBArr : ((java.util.Collection<AABB>)var17).toArray(putAt_AABBArr);
      if (var10 && this.putAt_J == var15) {
         this.fill_AABBArr2[var14] = var28;
         this.fill_JArr[var14] = var11;
         this.putAt_IArr[var14] = var13;
      }

      return var28;
   }

   private void putAt_V_2() {
      if (this.putAt_FileStore_IoHelper != null
         && (
            this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe != this.putAt_FileStore_PacketHe
               || this.putAt_FileStore_IoHelper.putAt_DataCodec_CacheHel != this.putAt_DataCodec_CacheHel
         )) {
         boolean var1 = this.putAt_FileStore_IoHelper.readEx();
         if (this.readEx_Z || var1) {
            Arrays.fill(this.putAt_AABBArr2, null);
            Arrays.fill(this.fill_AABBArr2, null);
            this.putAt_Z = false;
            this.fill_AABBArr = null;
            this.putAt_Long2ShortOpenHash.clear();
            this.fill_Long2IntOpenHashMa.clear();
            this.putAt_Long2ByteOpenHashM.clear();
            this.fill_Long2ByteOpenHashM.clear();
            this.readEx_Long2ByteOpenHashM.clear();
            Arrays.fill(this.putAt_BArr, (byte)-1);
            Arrays.fill(this.fill_BArr, (byte)-1);
            this.putAt_Long2IntOpenHashMa.clear();
            this.fill_Z = false;
            this.putAt_LongArrayList.clear();
            this.putAt_IntArrayList.clear();
            this.putAt_LongOpenHashSet.clear();
            Arrays.fill(this.readEx_IArr, 0);
            Arrays.fill(this.readEx_BArr, (byte)0);
            Arrays.fill(this.fill_IArr, 0);
         }

         this.readEx_Z = var1;
         this.putAt_FileStore_PacketHe = this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe;
         this.putAt_DataCodec_CacheHel = this.putAt_FileStore_IoHelper.putAt_DataCodec_CacheHel;
      }
   }

   private AABB[] putAt_AABBArr_I(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      int var6 = putAt_I_I(var1, var2, var3);
      AABB[] var7 = this.putAt_AABBArr2[var6];
      if (var7 != null && this.putAt_JArr[var6] == var4) {
         return var7;
      }

      long var8 = this.putAt_J;
      short var10 = this.putAt_S_I(var1, var2, var3, var4);
      var var11 = e.init_List_S(var10);
      AABB[] var12 = var11.isEmpty() ? putAt_AABBArr : new AABB[var11.size()];

      for (int var13 = 0; var13 < var12.length; var13++) {
         var12[var13] = ((AABB)var11.get(var13)).move(var1, var2, var3);
      }

      if (this.putAt_J == var8) {
         this.putAt_JArr[var6] = var4;
         this.putAt_AABBArr2[var6] = var12;
      }

      return var12;
   }

   private static int putAt_I_I(int var0, int var1, int var2) {
      return (var0 & 15) << 8 | (var1 & 15) << 4 | var2 & 15;
   }

   short putAt_S_I(int var1, int var2, int var3) {
      this.putAt_V_2();
      return this.putAt_S_I(var1, var2, var3, BlockPos.asLong(var1, var2, var3));
   }

   private short putAt_S_I(int var1, int var2, int var3, long var4) {
      short var6 = this.putAt_Long2ShortOpenHash.get(var4);
      if (var6 != -32768) {
         return var6;
      }

      long var7 = this.putAt_J;
      short var9 = this.fill_S(var1, var2, var3);
      if (this.putAt_J != var7) {
         return var9;
      }

      if (this.putAt_Long2ShortOpenHash.size() >= 65536) {
         this.putAt_Long2ShortOpenHash.clear();
         this.fill_Long2IntOpenHashMa.clear();
      }

      this.putAt_Long2ShortOpenHash.put(var4, var9);
      if (var9 == 0) {
         int var10 = this.flowTemp(var1, var2, var3);
         if (var10 != 0) {
            this.fill_Long2IntOpenHashMa.put(var4, var10);
            this.fill_Z = true;
            if (this.putAt_LongOpenHashSet.add(var4)) {
               this.putAt_LongArrayList.add(var4);
               this.putAt_IntArrayList.add(var10);
            }
         }
      }

      return var9;
   }

   private int flowTemp(int var1, int var2, int var3) {
      if (this.putAt_FileStore_IoHelper != null
         && this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe != null
         && this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe.cellsToBreak().contains(new BlockPos(var1, var2, var3))) {
         return 0;
      } else {
         ClassCache.Inner_FileCache var4 = this.putAt_FileStore_TextStor.probe(var1, var2, var3);
         if (var4 == null) {
            return 0;
         } else {
            return var4.web() ? 1 : (var4.thorny() ? 2 : 0);
         }
      }
   }

   @Override
   public boolean state(int var1, int var2, int var3) {
      if (this.putAt_FileStore_IoHelper == null) {
         ClassCache.Inner_FileCache var9 = this.putAt_FileStore_TextStor.probe(var1, var2, var3);
         return var9 != null && var9.climbable();
      }

      this.putAt_V_2();
      if (this.readEx_Z
         && this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe != null
         && !this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe.cellsToBreak().isEmpty()
         && this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe.cellsToBreak().contains(new BlockPos(var1, var2, var3))) {
         return false;
      }

      long var4 = BlockPos.asLong(var1, var2, var3);
      int var6 = putAt_I_I(var1, var2, var3);
      byte var7 = this.fill_BArr[var6];
      if (var7 >= 0 && this.prevEx_JArr[var6] == var4) {
         return var7 != 0;
      }

      ClassCache.Inner_FileCache var8 = this.putAt_FileStore_TextStor.probe(var1, var2, var3);
      if (var8 != null && var8.loaded()) {
         this.prevEx_JArr[var6] = var4;
         this.fill_BArr[var6] = (byte)(var8.climbable() ? 1 : 0);
      }

      return var8 != null && var8.climbable();
   }

   @Override
   public boolean fill_Z(int var1, int var2, int var3) {
      this.putAt_V_2();
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.putAt_FileStore_IoHelper == null ? -1 : this.fill_Long2ByteOpenHashM.get(var4);
      if (var6 >= 0) {
         return var6 != 0;
      }

      ClassCache.Inner_FileCache var7 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (this.putAt_FileStore_IoHelper != null && var7 != null && var7.loaded()) {
         if (this.fill_Long2ByteOpenHashM.size() >= 65536) {
            this.fill_Long2ByteOpenHashM.clear();
         }

         this.fill_Long2ByteOpenHashM.put(var4, (byte)(var7.water() ? 1 : 0));
      }

      return var7 != null && var7.water();
   }

   @Override
   public boolean putAt_Z_I_2(int var1, int var2, int var3) {
      this.putAt_V_2();
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.putAt_FileStore_IoHelper == null ? -1 : this.putAt_Long2ByteOpenHashM.get(var4);
      if (var6 >= 0) {
         return var6 != 0;
      }

      ClassCache.Inner_FileCache var7 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (var7 != null && var7.loaded()) {
         if (this.putAt_Long2ByteOpenHashM.size() >= 65536) {
            this.putAt_Long2ByteOpenHashM.clear();
         }

         if (this.putAt_FileStore_IoHelper != null) {
            this.putAt_Long2ByteOpenHashM.put(var4, (byte)(var7.lava() ? 1 : 0));
         }

         return var7.lava();
      } else {
         this.putAt_J++;
         return true;
      }
   }

   @Override
   public boolean prevEx(int var1, int var2, int var3) {
      this.putAt_V_2();
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.putAt_FileStore_IoHelper == null ? -1 : this.readEx_Long2ByteOpenHashM.get(var4);
      if (var6 >= 0) {
         return var6 != 0;
      }

      ClassCache.Inner_FileCache var7 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (var7 != null && var7.loaded()) {
         boolean var8 = var7.hazardFeet() || var7.floorContactHazard() || var7.thorny();
         if (this.putAt_FileStore_IoHelper != null) {
            if (this.readEx_Long2ByteOpenHashM.size() >= 65536) {
               this.readEx_Long2ByteOpenHashM.clear();
            }

            this.readEx_Long2ByteOpenHashM.put(var4, (byte)(var8 ? 1 : 0));
         }

         return var8;
      } else {
         this.putAt_J++;
         return true;
      }
   }

   @Override
   public boolean prev(int var1, int var2, int var3) {
      this.putAt_V_2();
      ClassCache.Inner_FileCache var4 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (var4 != null && var4.loaded()) {
         return var4.hazardFeet() || var4.thorny() || !var4.passable() && var4.floorContactHazard() && var4.top16() == 15;
      }

      this.putAt_J++;
      return true;
   }

   @Override
   public boolean readEx_Z(int var1, int var2, int var3) {
      this.putAt_V_2();
      ClassCache.Inner_FileCache var4 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (var4 != null && var4.loaded()) {
         return var4.feetTrigger();
      }

      this.putAt_J++;
      return false;
   }

   @Override
   public boolean value_Z(int var1, int var2, int var3) {
      this.putAt_V_2();
      ClassCache.Inner_FileCache var4 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.probe(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      return var4 != null && var4.loaded() && var4.pressurePlate();
   }

   @Override
   public boolean putAt(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (this.putAt_FileStore_IoHelper == null) {
         return false;
      }

      int var8 = var4 - var1;
      int var9 = var5 - var2;
      int var10 = var6 - var3;
      int var11 = var7 - var2;
      if ((var8 | var9 | var10 | var11) >>> 4 != 0) {
         return false;
      }

      this.putAt_V_2();
      long var12 = BlockPos.asLong(var1, var2, var3);
      int var14 = 1 + (var8 << 12 | var9 << 8 | var10 << 4 | var11);
      int var15 = (int)HashCommon.mix(var12 ^ var14 * -7046029254386353131L) & 4095;
      if (this.fill_IArr[var15] == var14 && this.readEx_JArr[var15] == var12) {
         return true;
      }

      for (int var16 = var1; var16 <= var4; var16++) {
         for (int var17 = var2; var17 <= var5; var17++) {
            for (int var18 = var3; var18 <= var6; var18++) {
               ClassCache.Inner_FileCache var19 = this.putAt_FileStore_IoHelper.probe(var16, var17, var18);
               if (!var19.loaded() || var19.hazardFeet() || var19.thorny() || var19.feetTrigger()) {
                  return false;
               }

               if (var17 < var7) {
                  if (var19.floorContactHazard()) {
                     return false;
                  }
               } else if (var19.lava() || var19.water() || !var19.passable() && var19.floorContactHazard() && var19.top16() == 15) {
                  return false;
               }
            }
         }
      }

      this.readEx_JArr[var15] = var12;
      this.fill_IArr[var15] = var14;
      return true;
   }

   @Override
   public boolean putAt_Z() {
      return this.fill_Z;
   }

   @Override
   public int putAt_I_AABB(AABB var1) {
      return this.putAt_I_D(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
   }

   int putAt_I_D(double var1, double var3, double var5, double var7, double var9, double var11) {
      if (!this.fill_Z) {
         return 0;
      }

      int var13 = 0;
      int var14 = (int)Math.floor(var1);
      int var15 = (int)Math.floor(var7);
      int var16 = (int)Math.floor(var3);
      int var17 = (int)Math.floor(var9);
      int var18 = (int)Math.floor(var5);
      int var19 = (int)Math.floor(var11);
      if (this.putAt_LongArrayList.size() <= 64) {
         for (int var27 = 0; var27 < this.putAt_LongArrayList.size(); var27++) {
            long var28 = this.putAt_LongArrayList.getLong(var27);
            int var29 = BlockPos.getX(var28);
            int var24 = BlockPos.getY(var28);
            int var25 = BlockPos.getZ(var28);
            if (var29 >= var14 && var29 <= var15 && var24 >= var16 && var24 <= var17 && var25 >= var18 && var25 <= var19) {
               int var26 = this.putAt_IntArrayList.getInt(var27);
               if (var26 == 1) {
                  return var26;
               }

               var13 = var26;
            }
         }

         return var13;
      } else {
         for (int var20 = var14; var20 <= var15; var20++) {
            for (int var21 = var16; var21 <= var17; var21++) {
               for (int var22 = var18; var22 <= var19; var22++) {
                  this.putAt_S_I(var20, var21, var22);
                  int var23 = this.fill_Long2IntOpenHashMa.get(BlockPos.asLong(var20, var21, var22));
                  if (var23 == 1) {
                     return var23;
                  }

                  if (var23 != 0) {
                     var13 = var23;
                  }
               }
            }
         }

         return var13;
      }
   }

   private short fill_S(int var1, int var2, int var3) {
      if (this.putAt_FileStore_IoHelper != null && this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe != null) {
         ClassCache.UiHelper var4 = this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe;
         BlockPos var5 = new BlockPos(var1, var2, var3);
         if (var4.cellsToBreak().contains(var5)) {
            return 0;
         }

         if (var4.cellsToPlace().contains(var5)) {
            return 1;
         }

         if (var4.type() == ClassCache.Inner_SyncManager.set
            && var1 == var4.to().getX()
            && var3 == var4.to().getZ()
            && var2 >= var4.to().getY()
            && var2 <= var4.to().getY() + 1) {
            return 0;
         }
      }

      short var6 = this.putAt_FileStore_IoHelper == null
         ? this.putAt_FileStore_TextStor.putAt_S_I(var1, var2, var3)
         : this.putAt_FileStore_IoHelper.putAt_S_I(var1, var2, var3);
      if (var6 < 0) {
         this.putAt_J++;
         if (this.putAt_FileStore_IoHelper != null) {
            this.putAt_FileStore_IoHelper.flowState |= 1;
         }

         return 1;
      } else {
         return var6;
      }
   }

   long putAt(BlockPos var1, BlockPos var2) {
      return this.putAt_J_BlockPos(var1, var2, true);
   }

   boolean putAt_Z_I(int var1, int var2, double var3) {
      if (this.putAt_FileStore_IoHelper == null) {
         return TableIndex.fill(this, var1, var2, var3);
      }

      this.putAt_V_2();
      long var5 = BlockPos.asLong(var1, 0, var2);
      long var7 = Double.doubleToLongBits(var3);
      int var9 = (int)HashCommon.mix(var5 ^ Long.rotateLeft(var7, 17)) & 4095;
      if (this.readEx_BArr[var9] != 0 && this.state_JArr[var9] == var5 && this.closeOf_JArr[var9] == var7) {
         return this.readEx_BArr[var9] == 1;
      }

      long var10 = this.putAt_J;
      boolean var12 = TableIndex.fill(this, var1, var2, var3);
      if (this.putAt_J == var10) {
         this.state_JArr[var9] = var5;
         this.closeOf_JArr[var9] = var7;
         this.readEx_BArr[var9] = (byte)(var12 ? 1 : -1);
      }

      return var12;
   }

   long putAt_J_BlockPos(BlockPos var1, BlockPos var2, boolean var3) {
      int var4 = var3 ? (int)Math.ceil(4.0) + 1 : 2;
      int var5 = Math.min(var1.getX() - var4, var2.getX() - 1);
      int var6 = Math.max(var1.getX() + var4, var2.getX() + 1);
      int var7 = Math.min(var1.getZ() - var4, var2.getZ() - 1);
      int var8 = Math.max(var1.getZ() + var4, var2.getZ() + 1);
      int var9 = Math.min(var1.getY(), var2.getY()) - 1;
      int var10 = Math.max(var1.getY(), var2.getY()) + 3;
      this.putAt_V_2();
      long var11 = -7046029254386353131L;

      for (int var13 = var5; var13 <= var6; var13++) {
         for (int var14 = var9; var14 <= var10; var14++) {
            var11 = (var11 ^ Long.rotateLeft(this.putAt_J_I(var13, var14, var7, var8), 21)) * 1099511628211L;
         }
      }

      return var11;
   }

   private long putAt_J_I(int var1, int var2, int var3, int var4) {
      int var5 = var4 - var3 + 1;
      long var6 = BlockPos.asLong(var1, var2, var3);
      int var8 = (int)HashCommon.mix(var6 ^ var5 * -7046029254386353131L) & 4095;
      if (this.putAt_FileStore_IoHelper != null && this.readEx_IArr[var8] == var5 && this.prev_JArr[var8] == var6) {
         return this.chunkAt_JArr[var8];
      }

      long var9 = this.putAt_J;
      long var11 = 5871781006564002453L ^ var5;

      for (int var13 = var3; var13 <= var4; var13++) {
         var11 = (var11 ^ HashCommon.mix((long)this.readEx_I(var1, var2, var13))) * 1099511628211L;
      }

      if (this.putAt_FileStore_IoHelper != null && this.putAt_J == var9) {
         this.prev_JArr[var8] = var6;
         this.readEx_IArr[var8] = var5;
         this.chunkAt_JArr[var8] = var11;
      }

      return var11;
   }

   private int readEx_I(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      int var6 = this.putAt_FileStore_IoHelper == null ? -1 : this.putAt_Long2IntOpenHashMa.get(var4);
      if (var6 >= 0) {
         return var6;
      }

      long var7 = this.putAt_J;
      int var9 = this.putAt_S_I(var1, var2, var3) & '\uffff';
      var9 |= (this.putAt_B_I(var1, var2, var3) & 255) << 16;
      if (this.state(var1, var2, var3)) {
         var9 |= 16777216;
      }

      if (this.putAt_Z_I_2(var1, var2, var3)) {
         var9 |= 33554432;
      }

      if (this.fill_Z(var1, var2, var3)) {
         var9 |= 67108864;
      }

      var9 |= this.fill_Long2IntOpenHashMa.get(var4) << 27;
      if (this.prevEx(var1, var2, var3)) {
         var9 |= 536870912;
      }

      if (this.putAt_FileStore_IoHelper != null && this.putAt_J == var7) {
         if (this.putAt_Long2IntOpenHashMa.size() >= 65536) {
            this.putAt_Long2IntOpenHashMa.clear();
         }

         this.putAt_Long2IntOpenHashMa.put(var4, var9);
      }

      return var9;
   }

   private m putAt_TableEntry_I(int var1, int var2, int var3) {
      return m.init(this.putAt_B_I(var1, var2, var3));
   }

   byte putAt_B_I(int var1, int var2, int var3) {
      if (this.putAt_FileStore_IoHelper == null) {
         return this.putAt_FileStore_TextStor.putAt_B_I(var1, var2, var3);
      }

      this.putAt_V_2();
      long var4 = BlockPos.asLong(var1, var2, var3);
      int var6 = putAt_I_I(var1, var2, var3);
      if (this.value_JArr[var6] == var4 && this.putAt_BArr[var6] >= 0) {
         return this.putAt_BArr[var6];
      }

      byte var7 = this.putAt_FileStore_TextStor.putAt_B_I(var1, var2, var3);
      ClassCache.Inner_FileCache var8 = this.putAt_FileStore_IoHelper.probe(var1, var2, var3);
      if (var8 != null && var8.loaded()) {
         this.value_JArr[var6] = var4;
         this.putAt_BArr[var6] = var7;
      }

      return var7;
   }

   @Override
   public double putAt_D_I(int var1, int var2, int var3) {
      byte var4 = this.putAt_B_I(var1, var2, var3);
      return var4 == 0 ? 0.6 : m.init(var4).init_D;
   }

   @Override
   public double fill_D(int var1, int var2, int var3) {
      byte var4 = this.putAt_B_I(var1, var2, var3);
      return var4 == 0 ? 1.0 : m.init(var4).load_D;
   }

   @Override
   public double value_D(int var1, int var2, int var3) {
      byte var4 = this.putAt_B_I(var1, var2, var3);
      return var4 == 0 ? 1.0 : m.init(var4).read_D;
   }

   @Override
   public double readEx_D(int var1, int var2, int var3) {
      byte var4 = this.putAt_B_I(var1, var2, var3);
      return var4 == 0 ? 0.0 : m.init(var4).write_D;
   }
}
