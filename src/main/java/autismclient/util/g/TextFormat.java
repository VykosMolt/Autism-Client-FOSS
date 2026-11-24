package autismclient.util.g;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.util.PkgCls_b;
import autismclient.util.aM;
import autismclient.util.bo;
import autismclient.util.macro.G;
import autismclient.util.macro.H;
import autismclient.util.macro.I;
import autismclient.util.macro.K;
import autismclient.util.macro.L;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.N;
import autismclient.util.macro.Q;
import autismclient.util.macro.S;
import autismclient.util.macro.U;
import autismclient.util.macro.V;
import autismclient.util.macro.W;
import autismclient.util.macro.X;
import autismclient.util.macro.aA;
import autismclient.util.macro.aB;
import autismclient.util.macro.aC;
import autismclient.util.macro.aD;
import autismclient.util.macro.aE;
import autismclient.util.macro.aF;
import autismclient.util.macro.aG;
import autismclient.util.macro.aH;
import autismclient.util.macro.aI;
import autismclient.util.macro.aJ;
import autismclient.util.macro.aK;
import autismclient.util.macro.aL;
import autismclient.util.macro.aN;
import autismclient.util.macro.aP;
import autismclient.util.macro.aQ;
import autismclient.util.macro.ac;
import autismclient.util.macro.ad;
import autismclient.util.macro.ae;
import autismclient.util.macro.af;
import autismclient.util.macro.ah;
import autismclient.util.macro.ai;
import autismclient.util.macro.ao;
import autismclient.util.macro.ap;
import autismclient.util.macro.ar;
import autismclient.util.macro.as;
import autismclient.util.macro.at;
import autismclient.util.macro.au;
import autismclient.util.macro.av;
import autismclient.util.macro.aw;
import autismclient.util.macro.ax;
import autismclient.util.macro.ay;
import autismclient.util.macro.f;
import autismclient.util.macro.j;
import autismclient.util.macro.l;
import autismclient.util.macro.m;
import autismclient.util.macro.o;
import autismclient.util.macro.s;
import autismclient.util.macro.u;
import autismclient.util.macro.v;
import autismclient.util.macro.w;
import autismclient.util.macro.y;
import autismclient.util.macro.z;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Direction;

final class TextFormat {
   private static final int init_I = 128;
   private static final int load_I = 8;
   private static final int read_I = 64;
   private static final int write_I = 1000;
   private static final long init_J = 3000L;
   private static final long load_J = 10000L;
   private static final long read_J = 600000L;
   private static final long write_J = 2500L;
   private static final long update_J = 1500L;
   private static final long sync_J = 50L;
   private final aM init_aM;
   private final List<MacroAction> init_List;
   private final Map<String, Integer> init_Map = new HashMap<>();
   private final Map<String, String> load_Map = new HashMap<>();
   private final Map<String, l> read_Map = new HashMap<>();
   private final Map<autismclient.util.macro.TaskQueue, autismclient.util.macro.ConfigStore.Inner_BufferHelper> write_Map = new IdentityHashMap<>();
   private final Map<String, List<String>> update_Map = new HashMap<>();
   private static final String init_Str = "abcdefghijklmnopqrstuvwxyz0123456789_";
   private static final int update_I = 8;
   private static final int sync_I = 10000;
   private final Map<String, TextFormat.Inner_DataUtil> sync_Map = new HashMap<>();
   private TextFormat.Inner_DataUtil init_TextFormat_DataUti;
   private final Deque<int[]> init_Deque = new ArrayDeque<>();
   private final int flush_I;
   private int close_I;
   private int open_I;
   private int poll_I = -1;
   private int push_I;
   private long flush_J;
   private FileCache init_FileCache;
   private boolean init_Z;
   private boolean load_Z;
   private String load_Str = "";
   private long close_J;
   private long open_J;
   private ao.Inner_DataUtil init_ao_DataUtil;
   private boolean read_Z;
   private boolean write_Z;
   private int pop_I;
   private long poll_J;
   private autismclient.util.macro.TaskQueue init_TaskQueue;
   private int tick_I = -1;
   private String read_Str = "";
   private long push_J;
   private boolean update_Z;
   private String write_Str = "";
   private String update_Str = "";
   private y init_y;
   private int run_I;
   private long pop_J;
   private u init_u;
   private int get_I;
   private int set_I;
   private int put_I;
   private long tick_J;
   private int add_I;
   private aQ init_aQ;
   private autismclient.util.macro.StreamGate init_StreamGate;
   private w init_w;
   private boolean sync_Z;
   private double init_D;
   private double load_D;
   private double read_D;
   private double write_D;
   private double update_D;
   private int clear_I;
   private long run_J;
   private long get_J;
   private long set_J;
   private long put_J;
   private long add_J;
   private double sync_D;
   private boolean flush_Z;
   private String sync_Str = "";
   private autismclient.util.macro.UiHelper init_UiHelper;
   private long clear_J;
   private MacroAction init_MacroAction;
   private long reset_J;
   private int reset_I;
   private boolean close_Z;
   private double flush_D;
   private double close_D;
   private double open_D;
   private long start_J;
   private String flush_Str = "";
   private String[] init_StrArr;
   private int start_I;
   private long stop_J;
   private long next_J;
   private boolean open_Z;
   private long prev_J;
   private boolean poll_Z;
   private long valid_J;
   private List<String> load_List;
   private int stop_I;
   private long size_J;
   private int next_I;
   private EventBus init_EventBus;
   private List<int[]> read_List;
   private int prev_I;
   private long data_J;
   private long value_J;
   private boolean push_Z;
   private boolean pop_Z;
   private boolean tick_Z;
   private boolean run_Z;
   private int valid_I = -1;
   private int size_I;
   private boolean get_Z;
   private boolean set_Z;
   private boolean put_Z;
   private String close_Str;
   private List<MacroAction> write_List;
   private Set<Integer> init_Set;
   private long cache;
   private int data_I = -1;
   private boolean add_Z;
   private boolean clear_Z;
   private volatile String open_Str = "idle";
   private final boolean reset_Z;
   private final int value_I;
   private boolean start_Z;
   private String poll_Str = "done";
   private final Set<autismclient.util.macro.StreamGate> load_Set = EnumSet.noneOf(autismclient.util.macro.StreamGate.class);

   TextFormat(aM macro) {
      this.init_aM = macro;
      this.init_List = macro.init_List;
      this.flush_I = macro.init_Z ? (macro.init_I < 0 ? 2147483647 : Math.max(1, macro.init_I)) : 1;
      boolean anyCustomMenu = false;
      int lastJoin = -1;

      for (int i = 0; i < this.init_List.size(); i++) {
         MacroAction a = this.init_List.get(i);
         if (a instanceof autismclient.util.macro.KeyHolder label) {
            this.init_Map.putIfAbsent(label.init(), i);
         }

         if (a != null && a.getType() == autismclient.util.macro.StreamGate.open) {
            anyCustomMenu = true;
         }

         if (a instanceof autismclient.util.macro.ClassCache && a.isEnabled()) {
            lastJoin = i;
         }
      }

      this.reset_Z = anyCustomMenu;
      this.value_I = lastJoin;
      this.open_Str = "Running " + macro.init_Str;
   }

   boolean init_Z() {
      return this.clear_Z;
   }

   String init_Str() {
      return this.open_Str;
   }

   String load_Str() {
      return this.init_aM.init_Str;
   }

   int init_I() {
      return Math.max(0, Math.min(this.init_List.size(), this.close_I));
   }

   int load_I() {
      return this.init_List.size();
   }

   int read_I() {
      return Math.max(1, this.open_I + 1);
   }

   ClassCache.Inner_UiHelper init_ClassCache_UiHelpe() {
      return new ClassCache.Inner_UiHelper(
         this.init_aM.init_Str, !this.clear_Z, this.init_I(), this.load_I(), this.read_I(), PacketUtil.update(this.open_Str, 64)
      );
   }

   void init_V_J_EventBus(long now, EventBus host) {
      this.init_EventBus = host;
      if (this.clear_Z || this.init_List.isEmpty()) {
         this.clear_Z = true;
      } else if (this.init_FileCache == null || this.init_Z_J_EventBus(now, host)) {
         boolean ready = host.init_Z();
         if (!ready && !host.load_Z()) {
            if (!this.load_Z() || host.init_FileCache_StreamUt().load_Z() && !this.init_Z) {
               return;
            }

            if (this.init_MacroAction != null
               || this.init_TaskQueue != null
               || this.init_y != null
               || this.init_u != null
               || this.init_ao_DataUtil != null
               || this.init_w != null
               || this.init_aQ != null
               || this.read_List != null
               || this.load_Z
               || this.load_List != null
               || this.next_I > 0) {
               return;
            }
         }

         if (this.init_UiHelper != null) {
            if (!this.load_Z_EventBus_J(host, now)) {
               return;
            }

            this.init_UiHelper = null;
            this.clear_J = 0L;
            if (this.sync()) {
               return;
            }
         }

         ready = host.init_Z();
         if (ready && !this.flush()) {
            EventBus.Inner_DataUtil inventorySync = host.init_EventBus_DataUtil();
            if (inventorySync != EventBus.Inner_DataUtil.init) {
               this.open_Str = inventorySync == EventBus.Inner_DataUtil.read
                  ? "Waiting for authoritative inventory update"
                  : "Waiting for inventory transaction";
               return;
            }
         }

         if (this.tick_Z || this.run_Z) {
            boolean sendClose = this.tick_Z;
            this.tick_Z = false;
            this.run_Z = false;
            host.init_Str_Str_Str(sendClose ? "close" : "close-silent", "");
         }

         if (this.flush_J > 0L) {
            if (now < this.flush_J) {
               return;
            }

            this.flush_J = 0L;
         }

         if (!host.open_Z()) {
            if (this.init_StreamGate != null) {
               autismclient.util.macro.StreamGate type = this.init_StreamGate;
               this.init_StreamGate = null;
               if (host.poll_Z()) {
                  this.init_V_StreamGate(type, host, "movement packets were refused");
               }
            }

            if (this.init_w != null) {
               if (this.sync_Z) {
                  this.sync_Z = false;
                  if (host.poll_Z()) {
                     this.init_V_StreamGate(autismclient.util.macro.StreamGate.f_u_StreamGate, host, "movement packets were refused");
                     this.init_V();
                  }
               }

               if (this.init_w != null) {
                  if (!this.init_Z_EventBus_J(host, now)) {
                     return;
                  }

                  this.init_V();
               }
            }

            if (this.init_ao_DataUtil != null) {
               if (this.write_Z && this.init_ao_DataUtil != ao.Inner_DataUtil.read) {
                  host.init_V_ao_DataUtil_Z(ao.Inner_DataUtil.read, this.read_Z);
               }

               if (this.pop_I > 0) {
                  host.init_V_ao_DataUtil_Z(this.init_ao_DataUtil, this.read_Z);
                  this.pop_I--;
                  this.flush_J = now + this.poll_J;
                  return;
               }

               this.init_ao_DataUtil = null;
               this.write_Z = false;
               this.poll_J = 0L;
            }

            if (this.init_TaskQueue != null) {
               if (!this.read(host, now)) {
                  return;
               }

               this.init_TaskQueue = null;
               this.tick_I = -1;
               this.read_Str = "";
               this.init_TextFormat_DataUti = null;
               this.push_J = 0L;
               this.update_Z = false;
               this.write_Str = "";
               this.update_Str = "";
            }

            if (this.init_y != null) {
               boolean sent = host.init_Z_y(this.init_y);
               if (!sent) {
                  this.init_V_StreamGate(autismclient.util.macro.StreamGate.f_w_StreamGate);
                  this.init_y = null;
                  this.run_I = 0;
                  this.pop_J = 0L;
               } else {
                  if (--this.run_I > 0) {
                     this.flush_J = now + this.pop_J;
                     return;
                  }

                  this.init_y = null;
                  this.pop_J = 0L;
               }
            }

            if (this.init_u != null) {
               int bookResult = this.init_I_u(this.init_u, this.get_I, this.set_I, host);
               if (bookResult == -2) {
                  if (++this.add_I <= 2) {
                     return;
                  }

                  this.init_V_StreamGate(autismclient.util.macro.StreamGate.f_i_StreamGate);
                  this.init_V_EventBus_Z(host, false);
                  return;
               }

               if (bookResult < 0) {
                  this.init_V_StreamGate(autismclient.util.macro.StreamGate.f_i_StreamGate);
                  this.init_V_EventBus_Z(host, false);
               } else {
                  if (++this.get_I < this.set_I) {
                     this.add_I = 0;
                     this.flush_J = now + this.tick_J;
                     return;
                  }

                  this.init_V_EventBus_Z(host, true);
               }
            }

            if (this.init_aQ != null) {
               int result = host.init_I_aQ_J(this.init_aQ, now);
               if (result == 0) {
                  return;
               }

               if (result < 0) {
                  this.init_V_StreamGate(autismclient.util.macro.StreamGate.add);
               }

               this.init_aQ = null;
            }

            if (this.load_Z) {
               boolean opened = host.init_J() != this.close_J
                  && (this.load_Str.isEmpty() || update(host.flush_Str()).toLowerCase(Locale.ROOT).contains(this.load_Str));
               if (!opened && now < this.open_J) {
                  return;
               }

               this.load_Z = false;
            }

            if (this.init_MacroAction != null) {
               if (now < this.reset_J && !this.init_Z_MacroAction_EventBus(this.init_MacroAction, host)) {
                  return;
               }

               this.init_MacroAction = null;
               if (this.open_Z) {
                  host.init_V_Z(false);
                  this.open_Z = false;
               }

               if (this.poll_Z) {
                  host.load_V_Z(false);
                  this.poll_Z = false;
               }
            }

            int budget = 128;
            int emits = 0;

            while (budget-- > 0) {
               if (this.clear_Z) {
                  return;
               }

               EventBus.Inner_DataUtil inventorySync = host.init_EventBus_DataUtil();
               if (host.init_Z() && !this.flush() && inventorySync != EventBus.Inner_DataUtil.init) {
                  this.open_Str = inventorySync == EventBus.Inner_DataUtil.read
                     ? "Waiting for authoritative inventory update"
                     : "Waiting for inventory transaction";
                  return;
               }

               boolean looped = false;

               while (!this.init_Deque.isEmpty() && this.close_I == ((int[])this.init_Deque.peek())[1]) {
                  int[] top = this.init_Deque.peek();
                  if (top[2] > 0) {
                     top[2]--;
                     this.close_I = top[0];
                     looped = true;
                     break;
                  }

                  this.init_Deque.pop();
               }

               if (!looped) {
                  if (this.put_Z && this.close_I >= this.size_I) {
                     this.load_V_Str(this.close_Str);
                     return;
                  }

                  if (this.close_I < this.init_List.size()) {
                     if (this.poll_I == this.close_I && this.push_I > 0) {
                        this.close_I = this.close_I + this.push_I;
                        this.poll_I = -1;
                        this.push_I = 0;
                     } else if (this.init_Set != null && this.init_Set.contains(this.close_I)) {
                        this.close_I++;
                     } else {
                        if (this.data_I >= 0 && !this.add_Z && this.close_I >= this.data_I) {
                           this.data_I = -1;
                           this.init_Set = null;
                           this.write_List = null;
                        }

                        MacroAction action = this.init_List.get(this.close_I);
                        if (action != null && action.isEnabled()) {
                           autismclient.util.macro.StreamGate type = action.getType();
                           if (type != null) {
                              ready = host.init_Z();
                              if (!ready && !load_Z_StreamGate(type) && (!this.load_Z() || !init_Z_StreamGate(type))) {
                                 return;
                              }

                              switch (type) {
                                 case f_X_StreamGate:
                                    autismclient.util.macro.ClassCache join = (autismclient.util.macro.ClassCache)action;
                                    String configured = join.server == null ? "" : join.server.trim();
                                    String target = configured.isEmpty() ? host.read_Str() : this.write(configured);
                                    if (configured.isEmpty() || target != null && !target.isBlank()) {
                                       this.init_FileCache = new FileCache(target, join.init_aA_StreamUtil(), join.timeoutSec);
                                       if (!this.init_Z_J_EventBus(now, host)) {
                                          return;
                                       }
                                       break;
                                    }

                                    this.open_Str = "Join: server variable is missing or empty";
                                    host.init_V_Str(this.open_Str);
                                    this.load_V_Str(this.open_Str);
                                    return;
                                 case buffer:
                                    if (autismclient.util.macro.ClassCache.init(this.init_List, this.close_I + 1, this.init_aM.init_Z)) {
                                       this.init_Z = true;
                                       host.load_V_Str("Macro");
                                    } else {
                                       host.read_V_Str("Macro");
                                    }

                                    this.close_I++;
                                    return;
                                 case f_T_StreamGate:
                                    autismclient.util.macro.FieldCache ifA = (autismclient.util.macro.FieldCache)action;
                                    if (UiHelper.init(ifA.condition, host, this.load_Map)) {
                                       this.poll_I = this.close_I + 1 + Math.max(0, ifA.thenSteps);
                                       this.push_I = Math.max(0, ifA.elseSteps);
                                    } else {
                                       this.close_I = this.close_I + Math.max(0, ifA.thenSteps);
                                    }

                                    this.close_I++;
                                    break;
                                 case f_I_StreamGate:
                                    autismclient.util.macro.StreamUtil b = (autismclient.util.macro.StreamUtil)action;
                                    if (this.init_Z_StreamUtil_EventBus(b, host)) {
                                       this.poll_I = this.close_I + 1 + Math.max(0, b.thenSteps);
                                       this.push_I = Math.max(0, b.elseSteps);
                                    } else {
                                       this.close_I = this.close_I + Math.max(0, b.thenSteps);
                                    }

                                    this.close_I++;
                                    break;
                                 case close:
                                 case f_H_StreamGate:
                                 case update:
                                 case sync:
                                 case table:
                                 case f_D_StreamGate:
                                 case f_F_StreamGate:
                                 case state:
                                 case f_E_StreamGate:
                                 case write:
                                 case block:
                                 case flush:
                                 case chunk:
                                 case f_G_StreamGate:
                                 case data:
                                 case f_c_StreamGate:
                                 case f_l_StreamGate:
                                 case f_m_StreamGate:
                                 case f_n_StreamGate:
                                 case f_o_StreamGate:
                                 case f_p_StreamGate:
                                 case f_C_StreamGate:
                                 case f_k_StreamGate:
                                    if (load_Z_MacroAction_EventBus(init_MacroAction_MacroAction(action), host)) {
                                       this.init_V_MacroAction_EventBus(action, host);
                                       this.init_MacroAction = action;
                                       this.reset_J = now + init_J_MacroAction(action);
                                       this.close_I++;
                                       return;
                                    }

                                    this.init_V_StreamGate(type);
                                    this.close_I++;
                                    break;
                                 case f_U_StreamGate:
                                    autismclient.util.macro.TableIndex flow = (autismclient.util.macro.TableIndex)action;
                                    boolean take = !flow.conditional || UiHelper.init(flow.condition, host, this.load_Map);
                                    if (take) {
                                       Integer jumpTarget = this.init_Integer_TableIndex(flow);
                                       if (jumpTarget == null) {
                                          if (this.init_Z_Str("stopped")) {
                                             return;
                                          }
                                       } else {
                                          this.close_I = jumpTarget;
                                          this.init_V_I(jumpTarget);
                                       }
                                    } else {
                                       this.close_I++;
                                    }
                                    break;
                                 case f_V_StreamGate:
                                    this.close_I++;
                                    break;
                                 case f_J_StreamGate:
                                    int count = Math.max(0, ((autismclient.util.macro.PoolEntry)action).bodyCount);
                                    this.valid_I = Math.min(this.init_List.size(), this.close_I + 1);
                                    this.size_I = Math.min(this.init_List.size(), this.close_I + 1 + count);
                                    this.get_Z = this.size_I > this.valid_I;
                                    this.close_I = this.size_I;
                                    break;
                                 case f_q_StreamGate:
                                    L race = (L)action;
                                    if (!this.add_Z) {
                                       int bodyCount = race.init_I_List_I(this.init_List, this.close_I);
                                       this.data_I = Math.min(this.init_List.size(), this.close_I + 1 + bodyCount);
                                       this.write_List = new ArrayList<>();
                                       this.init_Set = new HashSet<>();

                                       for (int idx = this.close_I + 1; idx < this.data_I; idx++) {
                                          MacroAction child = this.init_List.get(idx);
                                          if (child != null && child.isEnabled() && L.init_Z(child)) {
                                             this.write_List.add(child);
                                             this.init_Set.add(idx);
                                             this.init_V_MacroAction_EventBus(child, host);
                                          }
                                       }

                                       this.cache = now + Math.max(0, race.timeoutMs);
                                       this.add_Z = true;
                                    }

                                    boolean fired = this.write_List.isEmpty();

                                    for (int i = 0; !fired && i < this.write_List.size(); i++) {
                                       if (this.init_Z_MacroAction_EventBus(this.write_List.get(i), host)) {
                                          fired = true;
                                       }
                                    }

                                    boolean timedOut = now >= this.cache;
                                    if (!fired && !timedOut) {
                                       this.flush_J = now + 50L;
                                       return;
                                    }

                                    if (this.open_Z) {
                                       host.init_V_Z(false);
                                       this.open_Z = false;
                                    }

                                    if (this.poll_Z) {
                                       host.load_V_Z(false);
                                       this.poll_Z = false;
                                    }

                                    this.add_Z = false;
                                    if (fired) {
                                       this.close_I++;
                                    } else {
                                       this.init_V_StreamGate(type);
                                       this.close_I = this.data_I;
                                       this.data_I = -1;
                                       this.init_Set = null;
                                       this.write_List = null;
                                    }
                                    break;
                                 case frame:
                                    N rep = (N)action;
                                    int bodyStart = this.close_I + 1;
                                    int bodyEnd = Math.min(this.init_List.size(), bodyStart + Math.max(0, rep.stepCount));
                                    int times = Math.max(0, rep.repeatCount);
                                    if (times <= 0) {
                                       this.close_I = bodyEnd;
                                    } else {
                                       if (times > 1 && bodyEnd > bodyStart) {
                                          this.init_Deque.push(new int[]{bodyStart, bodyEnd, times - 1});
                                       }

                                       this.close_I++;
                                    }
                                    break;
                                 case init:
                                    autismclient.util.macro.EventBus d = (autismclient.util.macro.EventBus)action;
                                    long ms = d.useTicks ? d.delayTicks * 50L : d.delayMs;
                                    this.flush_J = now + Math.max(0L, ms);
                                    this.close_I++;
                                    return;
                                 case stop:
                                 case prev:
                                    this.flush_J = now + 50L;
                                    this.close_I++;
                                    return;
                                 case next:
                                    this.valid_J = host.sync_J();
                                    this.init_MacroAction = action;
                                    this.reset_J = now + 600000L;
                                    this.close_I++;
                                    return;
                                 case reset:
                                 case f_b_StreamGate:
                                 case size:
                                 case start:
                                 case f_x_StreamGate:
                                    if (this.read_List == null) {
                                       TextFormat.Inner_StreamUtil cp = this.init_TextFormat_StreamU_MacroAction(action, type, host);
                                       if (cp.init_List.isEmpty()) {
                                          if (cp.init_J > 0L && host.update_Z()) {
                                             if (this.data_J == 0L) {
                                                this.data_J = now + cp.init_J;
                                             }

                                             if (now < this.data_J) {
                                                this.flush_J = now + 50L;
                                                return;
                                             }
                                          }

                                          this.data_J = 0L;
                                          this.init_V_StreamGate(type);
                                          this.close_I++;
                                          break;
                                       }

                                       this.data_J = 0L;
                                       this.read_List = cp.init_List;
                                       this.prev_I = 0;
                                       this.value_J = cp.load_J;
                                       this.push_Z = cp.init_Z;
                                       this.pop_Z = cp.load_Z;
                                    }

                                    int[] click = this.read_List.get(this.prev_I++);
                                    host.init_V_I(click[0], click[1], click[2]);
                                    if (this.prev_I < this.read_List.size()) {
                                       if (this.value_J > 0L) {
                                          this.flush_J = now + Math.min(5000L, this.value_J);
                                          return;
                                       }

                                       return;
                                    }

                                    boolean closeAfter = this.push_Z;
                                    boolean closeSilent = this.pop_Z;
                                    this.read_List = null;
                                    this.prev_I = 0;
                                    this.value_J = 0L;
                                    this.push_Z = false;
                                    this.pop_Z = false;
                                    this.tick_Z = closeAfter;
                                    this.run_Z = !closeAfter && closeSilent;
                                    if (action instanceof aP w && w.write_Z()) {
                                       this.init_V_aP(w, host, now);
                                       this.close_I++;
                                       return;
                                    }

                                    this.close_I++;
                                    return;
                                 case f_g_StreamGate:
                                    if (this.load_List == null) {
                                       G p = (G)action;
                                       this.load_List = this.init_List_G(p);
                                       this.stop_I = 0;
                                       this.size_J = p.delayEnabled ? Math.max(0, p.delayMs) : 0L;
                                       if (this.load_List.isEmpty()) {
                                          this.load_List = null;
                                          this.close_I++;
                                          break;
                                       }
                                    }

                                    host.load_Str_Str(this.load_List.get(this.stop_I++));
                                    if (this.stop_I >= this.load_List.size()) {
                                       this.load_List = null;
                                       this.stop_I = 0;
                                       this.size_J = 0L;
                                       this.close_I++;
                                       if (++emits >= 8) {
                                          return;
                                       }
                                    } else {
                                       if (this.size_J > 0L) {
                                          this.flush_J = now + Math.min(60000L, this.size_J);
                                          return;
                                       }

                                       if (++emits >= 8) {
                                          return;
                                       }
                                    }
                                    break;
                                 default:
                                    boolean yield;
                                    try {
                                       yield = this.init_Z_MacroAction(action, type, host, now);
                                    } catch (RuntimeException ex) {
                                       this.init_V_StreamGate(type);
                                       yield = false;
                                       this.next_I = 0;
                                    }

                                    if (this.start_Z) {
                                       this.start_Z = false;
                                       if (this.init_Z_Str(this.poll_Str)) {
                                          return;
                                       }
                                    } else {
                                       if (yield) {
                                          this.close_I++;
                                          return;
                                       }

                                       if (this.next_I > 0) {
                                          if (++emits >= 8) {
                                             return;
                                          }
                                       } else {
                                          this.close_I++;
                                          if (++emits >= 8) {
                                             return;
                                          }
                                       }
                                    }
                              }
                           } else {
                              this.close_I++;
                           }
                        } else {
                           if (action instanceof L disabledRace) {
                              this.close_I = this.close_I + disabledRace.init_I_List_I(this.init_List, this.close_I);
                           }

                           this.close_I++;
                        }
                     }
                  } else if (++this.open_I >= this.flush_I) {
                     if (this.init_Z_Str("done")) {
                        return;
                     }
                  } else {
                     this.load_V();
                  }
               }
            }
         }
      }
   }

   private boolean init_Z_MacroAction(MacroAction action, autismclient.util.macro.StreamGate type, EventBus host, long now) {
      label338:
      switch (type) {
         case buffer:
            host.read_V_Str("Macro");
            break;
         case f_T_StreamGate:
         case f_I_StreamGate:
         case close:
         case f_H_StreamGate:
         case update:
         case sync:
         case table:
         case f_D_StreamGate:
         case f_F_StreamGate:
         case state:
         case f_E_StreamGate:
         case write:
         case block:
         case flush:
         case chunk:
         case f_G_StreamGate:
         case data:
         case f_c_StreamGate:
         case f_l_StreamGate:
         case f_m_StreamGate:
         case f_n_StreamGate:
         case f_o_StreamGate:
         case f_p_StreamGate:
         case f_C_StreamGate:
         case f_k_StreamGate:
         case f_U_StreamGate:
         case f_V_StreamGate:
         case f_J_StreamGate:
         case f_q_StreamGate:
         case frame:
         case init:
         case stop:
         case prev:
         case next:
         case reset:
         case f_b_StreamGate:
         case size:
         case start:
         case f_x_StreamGate:
         case f_g_StreamGate:
         default:
            this.init_V_StreamGate(type, host, "not supported on Multi bots (needs client world/state)");
            break;
         case f_h_StreamGate: {
            j.Inner_CachePool r = this.init_j_CachePool_Str(((W)action).message);
            if (r.init_Z()) {
               host.load_Str_Str(r.init_Str());
            }
            break;
         }
         case open:
            String matcherError = autismclient.util.macro.RenderState.init((autismclient.util.macro.UiHelper)action);
            if (!matcherError.isEmpty()) {
               this.init_V_Str(matcherError);
            } else {
               this.init_UiHelper = (autismclient.util.macro.UiHelper)action;
               this.clear_J = now + this.init_UiHelper.init();
               if (!this.load_Z_EventBus_J(host, now)) {
                  return true;
               }

               this.init_UiHelper = null;
               this.clear_J = 0L;
            }
            break;
         case f_A_StreamGate: {
            X c = (X)action;
            j.Inner_CachePool r = this.init_j_CachePool_Str(c.command);
            if (r.init_Z()) {
               String cmd = r.init_Str().trim();
               if (c.stripLeadingSlash && cmd.startsWith("/")) {
                  cmd = cmd.substring(1);
               }

               if (!cmd.isBlank()) {
                  host.load_Str_Str("/" + cmd);
               }
            }
            break;
         }
         case put:
            host.init_Str_Str_Str("change-slot", String.valueOf(Math.max(0, Math.min(8, ((V)action).slot)) + 1));
            break;
         case pop:
            host.init_Str_Str_Str("use", "");
            break;
         case valid:
            host.init_Str_Str_Str("close", "");
            break;
         case tick:
            if (((autismclient.util.macro.RefQueue)action).mode == autismclient.util.macro.RefQueue.Inner_DataUtil.load) {
               host.init_Str_Str_Str("close", "");
            }
            break;
         case clear:
            autismclient.util.macro.PacketUtil d = (autismclient.util.macro.PacketUtil)action;
            if (!d.init_Z_4()) {
               host.init_Str_Str_Str(
                  "drop", d.mode == autismclient.util.macro.PacketUtil.Inner_DataUtil.init ? "fullinventory" : Integer.toString(Math.max(1, d.dropCount))
               );
            } else {
               boolean any = false;
               int dropped = 0;
               List<autismclient.util.macro.ArrayUtil> targets = d.init_List_5();

               for (int i = 0; i < targets.size() && dropped < 64; i++) {
                  autismclient.util.macro.ArrayUtil t = targets.get(i);
                  if (t != null && (t.update() || t.init_Z())) {
                     int count = d.init_I_I_4(i);
                     if (t.update()) {
                        String name = t.load_Z() ? t.flush : t.close;
                        if (name == null || name.isBlank()) {
                           continue;
                        }

                        host.init_Str_Str_Str("drop", count == 0 ? name : count + " " + name);
                     } else {
                        host.init_Str_Str_Str("click-slot", t.init_I + (count == 0 ? " drop-stack" : " drop-item " + count));
                     }

                     any = true;
                     dropped++;
                  }
               }

               if (!any) {
                  this.init_V_StreamGate(type);
               }
            }
            break;
         case read:
            z pc = (z)action;
            if (pc.target == null) {
               this.init_V_StreamGate(type);
            } else {
               if (this.next_I <= 0) {
                  this.next_I = Math.max(1, Math.min(64, pc.times));
               }

               host.init_Str_Str_Str("click-slot", pc.target.write_I() + " " + init_Str_bo_DataUtil(pc.target.init_bo_DataUtil()));
               this.next_I--;
            }
            break;
         case f_y_StreamGate:
            autismclient.util.macro.DataUtil a = (autismclient.util.macro.DataUtil)action;
            if (!this.init_Z_DataUtil_EventBus(a, host) && a.failureBehavior == autismclient.util.macro.DataUtil.Inner_StreamUtil.init) {
               this.init_V_Str("assert failed: " + a.check);
            }
            break;
         case f_Q_StreamGate:
            ac s = (ac)action;
            host.init_Z_ac(s, this.write(s.line1), this.write(s.line2), this.write(s.line3), this.write(s.line4));
            if (s.sendCommandAfter && s.commandAfter != null && !s.commandAfter.isBlank()) {
               String cmd = this.write(s.commandAfter).trim();
               if (cmd.startsWith("/")) {
                  cmd = cmd.substring(1);
               }

               if (!cmd.isBlank()) {
                  host.load_Str_Str("/" + cmd);
               }
            }
            break;
         case f_L_StreamGate:
            if (this.init_Z_TaskQueue((autismclient.util.macro.TaskQueue)action, host, now)) {
               return true;
            }
            break;
         case queue: {
            H p = (H)action;
            String data = p.payloadData == null ? "" : p.payloadData;
            if (data.isBlank()) {
               this.init_V_StreamGate(type);
            } else {
               j.Inner_CachePool ch = this.init_j_CachePool_Str(p.channel);
               j.Inner_CachePool dr = this.init_j_CachePool_Str(data);
               if (ch.init_Z() && !ch.init_Str().isBlank() && dr.init_Z()) {
                  host.init_V_Str_Str(ch.init_Str().trim(), dr.init_Str());
               } else {
                  this.init_V_StreamGate(type);
               }
            }
            break;
         }
         case f_K_StreamGate:
            o mv = (o)action;
            int count = Math.min(mv.names.size(), mv.values.size());
            Map<String, String> resolved = new LinkedHashMap<>();
            boolean ok = true;

            for (int i = 0; i < count; i++) {
               j.Inner_CachePool rx = this.init_j_CachePool_Str(mv.values.get(i));
               if (!rx.init_Z()) {
                  ok = false;
                  break;
               }

               String name = m.load(mv.names.get(i));
               if (!name.isEmpty()) {
                  resolved.put(name, rx.init_Str());
               }
            }

            if (ok) {
               this.init_V_Map(resolved);
            }
            break;
         case f_w_StreamGate:
            y burst = (y)action;
            int burstCount = Math.max(1, Math.min(64, burst.count));
            long delay = Math.max(0L, Math.min(60000L, burst.delayTicks * 50L));
            if (!host.init_Z_y(burst)) {
               this.init_V_StreamGate(type);
            } else {
               if (burstCount > 1 && delay > 0L) {
                  this.init_y = burst;
                  this.run_I = burstCount - 1;
                  this.pop_J = delay;
                  this.flush_J = now + delay;
                  return true;
               }

               for (int i = 1; i < burstCount; i++) {
                  if (!host.init_Z_y(burst)) {
                     this.init_V_StreamGate(type);
                     break label338;
                  }
               }
            }
            break;
         case f_i_StreamGate:
            u book = (u)action;
            int total = Math.max(1, Math.min(9, book.bookCount));
            this.put_I = 0;
            int bookResult = this.init_I_u(book, 0, total, host);
            if (bookResult < -1) {
               this.init_u = book;
               this.get_I = 0;
               this.set_I = total;
               this.tick_J = Math.max(50L, Math.min(60000L, book.delayTicks * 50L));
               this.add_I = 1;
               return true;
            }

            if (bookResult < 0) {
               this.init_V_StreamGate(type);
            } else {
               if (total > 1) {
                  this.init_u = book;
                  this.get_I = 1;
                  this.set_I = total;
                  this.tick_J = Math.max(50L, Math.min(60000L, book.delayTicks * 50L));
                  this.flush_J = now + this.tick_J;
                  return true;
               }

               if (book.disconnectAfter) {
                  host.read_V_Str("NBT book macro");
               }
            }
            break;
         case scan:
            U save = (U)action;
            if (!host.init_Z_Z_Z(save.closeAfter, save.sendPacket)) {
               this.init_V_StreamGate(type);
            }
            break;
         case fill:
            if (!host.flush_Z()) {
               this.init_V_StreamGate(type);
            }
            break;
         case mark:
            if (!host.close_Z()) {
               this.init_V_StreamGate(type);
            }

            return false;
         case add:
            aQ resolvedUse = this.init_aQ_aQ((aQ)action);
            int result = host.init_I_aQ_J(resolvedUse, now);
            if (result == 0) {
               this.init_aQ = resolvedUse;
               return true;
            }

            if (result < 0) {
               this.init_V_StreamGate(type);
            }
            break;
         case task:
            host.write_V();
            this.init_V_Str("stopped");
            break;
         case node:
            String next = ((af)action).macroName;
            if (host.init_Z_Str(next)) {
               this.init_V_Str("chained");
            } else {
               host.init_V_Str("Macro chain failed: macro not found: " + update(next));
               this.init_V_Str("error");
            }
            break;
         case f_z_StreamGate:
            ao phase = (ao)action;
            int repeats = Math.max(1, Math.min(1000, phase.repeat));
            boolean offhand = "OFF_HAND".equalsIgnoreCase(phase.hand);
            if (phase.holdTicks > 0) {
               host.init_V_ao_DataUtil_Z(phase.phase, offhand);
               this.init_ao_DataUtil = phase.phase;
               this.read_Z = offhand;
               this.write_Z = phase.releaseAfterHold;
               this.pop_I = repeats - 1;
               this.poll_J = Math.min(60000L, phase.holdTicks * 50L);
               this.flush_J = now + this.poll_J;
               return true;
            }

            if (this.next_I <= 0) {
               this.next_I = repeats;
            }

            host.init_V_ao_DataUtil_Z(phase.phase, offhand);
            if (phase.releaseAfterHold && phase.phase != ao.Inner_DataUtil.read) {
               host.init_V_ao_DataUtil_Z(ao.Inner_DataUtil.read, offhand);
            }

            this.next_I--;
            break;
         case poll:
            autismclient.util.macro.NetHelper ca = (autismclient.util.macro.NetHelper)action;
            if (this.next_I <= 0) {
               this.next_I = Math.max(1, Math.min(1000, ca.clickCount));
            }

            String verb = ca.type == autismclient.util.macro.NetHelper.Inner_DataUtil.init ? "swing" : "use";
            host.init_Str_Str_Str(verb, "");
            this.next_I--;
            break;
         case f_e_StreamGate:
            autismclient.util.macro.MethodUtil ib = (autismclient.util.macro.MethodUtil)action;
            int n = Math.max(1, Math.min(64, ib.times));
            int x = ib.blockPos.getX();
            int y = ib.blockPos.getY();
            int z = ib.blockPos.getZ();
            String face = ib.direction == null ? "UP" : ib.direction.name();

            for (int i = 0; i < n; i++) {
               host.load(x, y, z, face);
            }

            if (ib.interact) {
               host.init_Str_I_2(x, y, z, face);
            }
            break;
         case push: {
            S r = (S)action;
            host.init_V_F_F(r.yaw, r.pitch);
            break;
         }
         case level:
            this.init_V_ValueSink_EventBus((autismclient.util.macro.ValueSink)action, host);
            break;
         case flags:
            return this.init_Z_s((s)action, host, now);
         case index:
            host.update_V();
            this.flush_J = now + Math.max(50L, ((autismclient.util.macro.NodeWalker)action).durationTicks * 50L);
            return true;
         case pool:
            host.read_V_Z(((ad)action).sneak);
            break;
         case count:
            host.write_V_Z(((ae)action).sprint);
            break;
         case drain:
            if (!host.read_Z()) {
               this.init_V_StreamGate_EventBus(type, host);
            } else {
               this.init_V_PathHelper_EventBus((autismclient.util.macro.PathHelper)action, host);
            }
            break;
         case f_f_StreamGate:
            autismclient.util.macro.CachePool b = (autismclient.util.macro.CachePool)action;
            int bTimes = Math.max(1, Math.min(64, b.times));
            int bx = b.blockPos.getX();
            int by = b.blockPos.getY();
            int bz = b.blockPos.getZ();
            String bFace = b.direction == null ? "UP" : b.direction.name();

            for (int i = 0; i < bTimes; i++) {
               host.load(bx, by, bz, bFace);
            }

            if (b.interact) {
               host.init_Str_I_2(bx, by, bz, bFace);
            }
            break;
         case f_P_StreamGate: {
            K p = (K)action;
            host.init_Str_I_2(p.blockPos.getX(), p.blockPos.getY(), p.blockPos.getZ(), p.direction.name());
            break;
         }
         case store:
            v oc = (v)action;
            if (oc.targetMode == v.Inner_DataUtil.init) {
               host.init_Str_I_2(oc.blockPos.getX(), oc.blockPos.getY(), oc.blockPos.getZ(), "UP");
            } else if (oc.targetMode == v.Inner_DataUtil.load) {
               String t = oc.entityTargets.isEmpty() ? read(oc.entityTarget) : read(oc.entityTargets.get(0));
               int id = host.read_I_Str(t);
               if (id < 0) {
                  this.init_V_StreamGate(type, host, "no tracked entity of that type to open");
               } else {
                  host.init_Str_I_Z(id, false);
               }
            } else {
               this.init_V_StreamGate(type, host, "Last-target container needs the client's crosshair; use Block coordinates");
            }
            break;
         case f_s_StreamGate:
            ap clipx = (ap)action;
            if (clipx.mode != ap.Inner_StreamUtil.init) {
               this.init_V_StreamGate(type, host, "only Manual clip mode works on bots (Top/Bottom scan the client world)");
            } else {
               int segments = init_I_D(clipx.deltaY, clipx.useSegmented, clipx.segmentBlocks, clipx.maxPackets);
               int queued = host.init_I_D(0.0, clipx.deltaY, 0.0, segments, clipx.forceGrounded);
               if (queued >= 0) {
                  this.init_StreamGate = type;
                  this.flush_J = now + host.flush_J();
                  return true;
               }

               this.init_V_StreamGate(type, host, "movement packets were refused");
            }
            break;
         case f_t_StreamGate:
            autismclient.util.macro.TokenStore clip = (autismclient.util.macro.TokenStore)action;
            if (clip.mode != autismclient.util.macro.TokenStore.Inner_CachePool.init) {
               this.init_V_StreamGate(type, host, "only Manual clip mode works on bots (Forward/Back scan the client world)");
            } else {
               double yaw = Math.toRadians(host.write_F());
               double dx = -Math.sin(yaw) * clip.blocks;
               double dz = Math.cos(yaw) * clip.blocks;
               int segments = init_I_D(clip.blocks, clip.useSegmented, clip.segmentBlocks, clip.maxPackets);
               int queued = host.init_I_D(dx, 0.0, dz, segments, clip.forceGrounded);
               if (queued >= 0) {
                  this.init_StreamGate = type;
                  this.flush_J = now + host.flush_J();
                  return true;
               }

               this.init_V_StreamGate(type, host, "movement packets were refused");
            }
            break;
         case f_u_StreamGate:
            this.init_V_w_EventBus((w)action, host);
            return true;
         case f_W_StreamGate:
            this.init_V_StreamGate(type, host, "orchestrates other bots from the main client");
      }

      if (action instanceof aP w && w.write_Z()) {
         this.init_V_aP(w, host, now);
         return true;
      } else {
         return false;
      }
   }

   private static int init_I_D(double distance, boolean segmented, int segmentBlocks, int maxPackets) {
      if (!segmented) {
         return 1;
      }

      int packets = Math.max(1, (int)Math.ceil(Math.abs(distance) / Math.max(1, segmentBlocks)));
      return packets > Math.max(1, maxPackets) ? 1 : Math.min(64, packets);
   }

   private void init_V_w_EventBus(w action, EventBus host) {
      this.init_w = action;
      this.sync_Z = false;
      this.init_D = action.relativeX ? host.init_D() + action.f_x_D : action.f_x_D;
      this.load_D = action.relativeY ? host.load_D() + action.f_y_D : action.f_y_D;
      this.read_D = action.relativeZ ? host.read_D() + action.f_z_D : action.f_z_D;
      this.write_D = 10.0;
      this.update_D = this.init_D_EventBus(host);
      this.clear_I = 0;
      this.run_J = 0L;
      this.get_J = host.load_J();
      this.set_J = 0L;
      this.put_J = 0L;
      this.add_J = 0L;
      this.sync_D = this.update_D;
      this.flush_Z = false;
      this.sync_Str = update(host.update_Str());
      this.open_Str = String.format(Locale.ROOT, "TP %.1f %.1f %.1f", this.init_D, this.load_D, this.read_D);
   }

   private boolean init_Z_EventBus_J(EventBus host, long now) {
      if (!update(host.update_Str()).equals(this.sync_Str)) {
         host.init_V_Str("TP stopped: dimension changed");
         return true;
      }

      long teleportSeq = host.load_J();
      if (teleportSeq != this.get_J) {
         this.get_J = teleportSeq;
         double distance = this.init_D_EventBus(host);
         if (this.update_D - distance >= 0.25) {
            this.update_D = distance;
            this.write_D = Math.min(10.0, this.write_D * 1.25);
            if (this.flush_Z) {
               this.sync_D = distance;
               this.put_J = now + 200L;
               this.add_J = now + 1200L;
            }
         } else {
            this.write_D = Math.max(0.0625, this.write_D * 0.5);
            this.flush_Z = true;
            this.sync_D = distance;
            this.put_J = now + 200L;
            this.add_J = now + 1200L;
         }

         this.clear_I = 0;
         this.run_J = this.flush_Z ? now + 200L : now;
         this.set_J = 0L;
      }

      double dx = this.init_D - host.init_D();
      double dy = this.load_D - host.load_D();
      double dz = this.read_D - host.read_D();
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance <= 0.05) {
         if (this.set_J == 0L) {
            this.set_J = now;
         }

         long settle = 500L;
         return now - this.set_J >= settle;
      } else {
         this.set_J = 0L;
         if (this.flush_Z) {
            if (now >= this.add_J && this.sync_D - distance >= 0.5) {
               this.flush_Z = false;
               this.write_D = Math.min(10.0, Math.max(1.0, this.write_D * 2.0));
               this.clear_I = 0;
               this.run_J = now;
            } else if (now < this.put_J) {
               return false;
            }
         }

         if (now < this.run_J) {
            return false;
         }

         int window = Math.max(1, Math.min(100, this.init_w.maxPackets));
         int remaining = this.flush_Z ? 1 : Math.max(1, window - this.clear_I);
         double amount = this.flush_Z ? Math.min(this.write_D, distance) : distance;
         int packets = Math.max(1, Math.min(remaining, (int)Math.ceil(amount / this.write_D)));
         int queued = host.init_I_D(dx / distance * amount, dy / distance * amount, dz / distance * amount, packets, true);
         if (queued < 0) {
            this.init_V_StreamGate(autismclient.util.macro.StreamGate.f_u_StreamGate, host, "movement packets were refused");
            return true;
         }

         if (queued > 0) {
            this.sync_Z = true;
         }

         if (this.flush_Z) {
            this.put_J = now + 200L;
         }

         this.clear_I += packets;
         if (this.clear_I >= window) {
            this.clear_I = 0;
            this.run_J = now + Math.max(50, Math.min(10000, this.init_w.pauseMs));
         }

         return false;
      }
   }

   private double init_D_EventBus(EventBus host) {
      double dx = this.init_D - host.init_D();
      double dy = this.load_D - host.load_D();
      double dz = this.read_D - host.read_D();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private void init_V() {
      this.init_w = null;
      this.sync_Z = false;
      this.clear_I = 0;
      this.run_J = 0L;
      this.set_J = 0L;
      this.put_J = 0L;
      this.add_J = 0L;
      this.sync_D = 0.0;
      this.flush_Z = false;
      this.sync_Str = "";
   }

   private void init_V_aP(aP w, EventBus host, long now) {
      this.load_Z = true;
      String name = w.init_Str_7();
      this.load_Str = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
      this.close_J = host.init_J();
      this.open_J = now + 3000L;
   }

   private TextFormat.Inner_StreamUtil init_TextFormat_StreamU_MacroAction(MacroAction action, autismclient.util.macro.StreamGate type, EventBus host) {
      switch (type) {
         case reset: {
            autismclient.util.macro.MapStore ia = (autismclient.util.macro.MapStore)action;
            long grace = ia.waitForItem ? 10000L : 2500L;
            long delay = ia.useClickDelay && ia.clickDelayMs > 0 ? ia.clickDelayMs : 0L;
            return new TextFormat.Inner_StreamUtil(host.init_List_MapStore(ia), grace, delay, false, false);
         }
         case f_b_StreamGate: {
            ah sa = (ah)action;
            boolean closePacket = sa.closeAfter && !sa.persistent && sa.closeSendPkt;
            boolean closeLocal = sa.closeAfter && !sa.persistent && !sa.closeSendPkt;
            long delay = sa.delayTicks > 0 ? sa.delayTicks * 50L : 0L;
            return new TextFormat.Inner_StreamUtil(host.init_List_ah(sa), 1500L, delay, closePacket, closeLocal);
         }
         case size:
            return new TextFormat.Inner_StreamUtil(host.init_List_ai((ai)action), 1500L, 0L, false, false);
         case start:
            return new TextFormat.Inner_StreamUtil(host.init_List_I_2((I)action), 0L, 0L, false, false);
         case f_x_StreamGate: {
            autismclient.util.macro.StateCache ca = (autismclient.util.macro.StateCache)action;
            long delay = ca.delayTicks > 0 ? ca.delayTicks * 50L : 0L;
            return new TextFormat.Inner_StreamUtil(host.init_List_StateCache(ca), 1500L, delay, false, false);
         }
         default:
            return new TextFormat.Inner_StreamUtil(List.of(), 0L, 0L, false, false);
      }
   }

   private Integer init_Integer_TableIndex(autismclient.util.macro.TableIndex flow) {
      Integer var10000;
      switch (flow.target) {
         case init:
            var10000 = Math.min(this.init_List.size(), this.close_I + 1 + Math.max(0, flow.amount));
            break;
         case load:
            var10000 = Math.max(0, this.close_I - Math.max(0, flow.amount));
            break;
         case read:
            var10000 = Math.max(0, Math.min(this.init_List.size(), flow.amount));
            break;
         case update:
            var10000 = 0;
            break;
         case sync:
            var10000 = this.init_List.size();
            break;
         case flush:
            var10000 = null;
            break;
         case write:
            Integer idx = this.init_Map.get(autismclient.util.macro.KeyHolder.init(flow.labelName));
            var10000 = idx != null ? idx : (flow.onMissingLabel == autismclient.util.macro.TableIndex.Inner_DataUtil.load ? null : this.close_I + 1);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_s(s a, EventBus host, long now) {
      double yaw = Math.toRadians(host.write_F());
      double fx = -Math.sin(yaw);
      double fz = Math.cos(yaw);
      double dirX;
      double dirZ;
      switch (a.direction) {
         case load:
            dirX = -fx;
            dirZ = -fz;
            break;
         case read:
            dirX = fz;
            dirZ = -fx;
            break;
         case write:
            dirX = -fz;
            dirZ = fx;
            break;
         default:
            dirX = fx;
            dirZ = fz;
      }

      double speed = host.push_Z() ? 0.26 : 0.2;
      long durationMs = Math.max(50L, a.durationTicks * 50L);
      host.init_V_D(dirX * speed, dirZ * speed, durationMs);
      if (a.nonBlocking) {
         return false;
      }

      this.flush_J = now + durationMs;
      return true;
   }

   private void init_V_ValueSink_EventBus(autismclient.util.macro.ValueSink a, EventBus host) {
      double tx;
      double ty;
      double tz;
      if (a.targetMode == autismclient.util.macro.ValueSink.Inner_StreamUtil.read) {
         int id = host.read_I_Str(read(a.entityIds.isEmpty() ? "" : a.entityIds.get(0)));
         double[] p = host.init_DArr_I(id);
         if (p == null) {
            this.init_V_StreamGate(autismclient.util.macro.StreamGate.level);
            return;
         }

         tx = p[0];
         ty = p[1] + 1.0;
         tz = p[2];
      } else {
         if (a.targetMode != autismclient.util.macro.ValueSink.Inner_StreamUtil.init) {
            this.init_V_StreamGate(autismclient.util.macro.StreamGate.level);
            return;
         }

         tx = a.blockX + 0.5;
         ty = a.blockY + 0.5;
         tz = a.blockZ + 0.5;
      }

      float[] angles = init_FArr_EventBus(host, tx, ty, tz);
      host.init_V_F_F(angles[0], angles[1]);
   }

   private void init_V_PathHelper_EventBus(autismclient.util.macro.PathHelper a, EventBus host) {
      if (a.targetMode != autismclient.util.macro.PathHelper.Inner_DataUtil.init) {
         this.init_V_StreamGate(autismclient.util.macro.StreamGate.drain);
      } else {
         String type = a.entityTargets.isEmpty() ? "" : read(a.entityTargets.get(0));
         int id = host.read_I_Str(type);
         if (id < 0) {
            this.init_V_StreamGate(autismclient.util.macro.StreamGate.drain);
         } else {
            host.init_Str_I_Z(id, false);
         }
      }
   }

   private static float[] init_FArr_EventBus(EventBus host, double tx, double ty, double tz) {
      double dx = tx - host.init_D();
      double dy = ty - (host.load_D() + 1.62);
      double dz = tz - host.read_D();
      double horiz = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
      return new float[]{yaw, pitch};
   }

   private boolean init_Z_StreamUtil_EventBus(autismclient.util.macro.StreamUtil b, EventBus host) {
      String v = b.value == null ? "" : b.value;
      boolean var10000;
      switch (b.conditionKind) {
         case init:
            var10000 = true;
            break;
         case load:
            var10000 = !v.isBlank() && !v.equalsIgnoreCase("ANY")
               ? host.flush_Str().toLowerCase(Locale.ROOT).contains(v.toLowerCase(Locale.ROOT))
               : host.update_Z();
            break;
         case read:
            var10000 = host.init_I_Str(v) > 0;
            break;
         case update:
            var10000 = host.sync_Str().toLowerCase(Locale.ROOT).replace('_', ' ').contains(read(v).toLowerCase(Locale.ROOT).replace('_', ' '));
            break;
         case write:
            var10000 = host.read_Z() && host.read_I_Str(read(v)) >= 0;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean init_Z_Str_EventBus(String guiType, EventBus host) {
      boolean menuOpen = host.init_CustomMenuSnapshot() != null;
      if (f.init_Z(guiType)) {
         return menuOpen;
      }

      boolean any = guiType == null || guiType.isBlank() || "ANY".equalsIgnoreCase(guiType);
      return host.update_Z() || any && menuOpen;
   }

   private boolean load_Z_Str_EventBus(String wanted, EventBus host) {
      String resolved = this.write(wanted == null ? "" : wanted);
      return resolved != null && !resolved.isBlank() ? host.flush_Str().toLowerCase(Locale.ROOT).contains(resolved.toLowerCase(Locale.ROOT)) : true;
   }

   private boolean init_Z_MacroAction_EventBus(MacroAction action, EventBus host) {
      MacroAction eff = init_MacroAction_MacroAction(action);
      if (eff instanceof ax w) {
         boolean open = init_Z_Str_EventBus(w.guiType, host);
         return w.waitMode == ax.Inner_DataUtil.load ? !open : open && this.load_Z_Str_EventBus(w.guiTitle, host);
      } else if (!(eff instanceof aJ w)) {
         if (eff instanceof ay w) {
            return w.below ? host.init_F() < w.healthThreshold : host.init_F() > w.healthThreshold;
         } else if (eff instanceof aK w) {
            return this.init_Z_aK_EventBus(w, host);
         } else if (eff instanceof aN w) {
            return this.init_Z_aN_EventBus(w, host);
         } else if (eff instanceof ar w) {
            return init_Z_ar_EventBus(w, host);
         } else if (eff instanceof aD w) {
            return host.init_Z_aD_StrArr(w, this.init_StrArr);
         } else if (eff instanceof aC w) {
            return this.init_Z_aC_EventBus(w, host);
         } else if (eff instanceof aF w) {
            return this.init_Z_aF_EventBus(w, host);
         } else if (eff instanceof aG w) {
            return this.init_Z_aG_EventBus(w, host);
         } else if (eff instanceof au w) {
            return this.init_Z_au_EventBus(w, host);
         } else if (eff instanceof aI w) {
            return this.init_Z_aI_EventBus(w, host);
         } else if (eff instanceof aw w) {
            return this.init_Z_aw_EventBus(w, host);
         } else if (eff instanceof as w) {
            return this.init_Z_as_EventBus(w, host);
         } else if (eff instanceof aB w) {
            return host.init_Z_J_List(this.next_J, w.init());
         } else if (eff instanceof aE w) {
            return host.init_Z_J(this.prev_J, w.soundIds, w.checkDistance, w.maxDistance);
         } else if (eff instanceof autismclient.util.macro.aM w) {
            return host.init_Z_J_aM(this.next_J, w);
         } else if (eff instanceof aA w) {
            return host.init_Z_aA(w);
         } else if (eff instanceof Q) {
            return host.sync_J() != this.valid_J;
         } else if (eff instanceof at w) {
            return w.checkMode != at.Inner_DataUtil.init
               ? false
               : host.init_Z_I(w.blockPos.getX(), w.blockPos.getY(), w.blockPos.getZ(), w.blockIds, w.anyBlock, w.waitBehavior == at.Inner_StreamUtil.load);
         } else if (eff instanceof av w) {
            return !host.init_Z_ArrayUtil_Z(w.itemTarget, w.checkMainInteractionHand);
         } else if (eff instanceof aH w) {
            int free = host.read_I();
            int actual = w.countMode == aH.Inner_StreamUtil.load ? 36 - free : free;
            int target = Math.max(0, Math.min(36, w.slots));
            boolean var10000;
            switch (w.comparison == null ? aH.Inner_DataUtil.load : w.comparison) {
               case init:
                  var10000 = actual < target;
                  break;
               case load:
                  var10000 = actual <= target;
                  break;
               case read:
                  var10000 = actual == target;
                  break;
               case write:
                  var10000 = actual >= target;
                  break;
               case update:
                  var10000 = actual > target;
                  break;
               default:
                  throw new MatchException(null, null);
            }

            return var10000;
         } else {
            return false;
         }
      } else {
         boolean open = init_Z_Str_EventBus(w.guiType, host);
         return w.waitMode == aJ.Inner_DataUtil.load ? !open : open && this.load_Z_Str_EventBus(w.title, host);
      }
   }

   private static MacroAction init_MacroAction_MacroAction(MacroAction action) {
      return action instanceof aL wm ? wm.init_MacroAction_3() : action;
   }

   private void init_V_MacroAction_EventBus(MacroAction action, EventBus host) {
      MacroAction eff = init_MacroAction_MacroAction(action);
      if (eff instanceof aK w) {
         this.reset_I = host.init_I_ArrayUtil(autismclient.util.macro.ArrayUtil.update(w.itemName));
         this.close_Z = host.init_Z_I(w.slot);
      } else if (eff instanceof aD w) {
         this.init_StrArr = host.init_StrArr_aD(w);
      } else if (eff instanceof aC) {
         this.flush_D = host.init_D();
         this.close_D = host.load_D();
         this.open_D = host.read_D();
      } else if (eff instanceof aF) {
         this.flush_D = host.init_D();
         this.close_D = host.load_D();
         this.open_D = host.read_D();
         this.start_J = host.load_J();
      } else if (eff instanceof aG) {
         this.flush_Str = host.update_Str();
      } else if (eff instanceof aI) {
         this.start_I = host.write_I();
      } else if (eff instanceof au) {
         this.stop_J = host.read_J();
      } else if (eff instanceof aB) {
         host.init_V_Z(true);
         this.next_J = host.write_J();
         this.open_Z = true;
      } else if (eff instanceof aE) {
         host.load_V_Z(true);
         this.prev_J = host.update_J();
         this.poll_Z = true;
      } else if (eff instanceof autismclient.util.macro.aM) {
         host.init_V_Z(true);
         this.next_J = host.write_J();
         this.open_Z = true;
      }
   }

   private boolean init_Z_au_EventBus(au w, EventBus host) {
      for (String line : host.init_List_J(this.stop_J)) {
         if (init_Z_au_Str(w, line)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init_Z_au_Str(au w, String text) {
      if (text == null) {
         return false;
      } else {
         String pattern = w.patternJson != null && !w.patternJson.isBlank() ? w.pattern : MacroExecutor.init_Str_Str(w.pattern);
         if (pattern != null && !pattern.isBlank()) {
            autismclient.util.macro.UtilCore.Inner_DataUtil mode = w.init_UtilCore_DataUtil();
            return mode == autismclient.util.macro.UtilCore.Inner_DataUtil.init
               ? MacroExecutor.init_Z_Str(pattern, text, au.init_I_I_8(w.fuzzyPercent))
               : autismclient.util.macro.UtilCore.init(mode, pattern, text).isPresent();
         } else {
            return true;
         }
      }
   }

   private boolean init_Z_aI_EventBus(aI w, EventBus host) {
      int cur = host.write_I();
      if (cur < 0) {
         return false;
      } else {
         return w.match == aI.Inner_DataUtil.load ? cur == w.init_GameType().getId() : cur != this.start_I;
      }
   }

   private boolean init_Z_aw_EventBus(aw w, EventBus host) {
      if (!host.read_Z()) {
         return false;
      } else if (w.checkMode != aw.Inner_DataUtil.load && w.checkMode != aw.Inner_DataUtil.write) {
         double radius = w.checkMode == aw.Inner_DataUtil.read ? 4.5 : w.radius;
         return host.init(w.entityIds, w.containerEntitiesOnly, w.centerOnPlayer, w.f_x_D, w.f_y_D, w.f_z_D, radius);
      } else {
         return false;
      }
   }

   private boolean init_Z_as_EventBus(as w, EventBus host) {
      if (!host.read_Z()) {
         return false;
      } else if (w.condition != as.Inner_DataUtil.init && w.condition != as.Inner_DataUtil.read) {
         List<String> types = w.entityId != null && !w.entityId.isBlank() ? List.of(w.entityId) : List.of();
         return host.init(types, w.containerEntitiesOnly, true, 0.0, 0.0, 0.0, w.range);
      } else {
         return false;
      }
   }

   private boolean init_Z_DataUtil_EventBus(autismclient.util.macro.DataUtil a, EventBus host) {
      String item = this.write(a.itemName);
      boolean var10000;
      switch (a.check) {
         case poll:
            var10000 = host.init_Z();
            break;
         case read:
            String g = this.write(a.guiType);
            var10000 = g != null && !g.isBlank() && !g.equalsIgnoreCase("ANY")
               ? host.flush_Str().toLowerCase(Locale.ROOT).contains(g.toLowerCase(Locale.ROOT))
               : host.update_Z();
            break;
         case init:
            var10000 = host.sync_Str().toLowerCase(Locale.ROOT).replace('_', ' ').contains(init_Str_Str(item).toLowerCase(Locale.ROOT).replace('_', ' '));
            break;
         case load:
            var10000 = host.init_I_ArrayUtil(autismclient.util.macro.ArrayUtil.update(item)) > 0;
            break;
         case flush:
            var10000 = host.init_I_Str("bundle") > 0;
            break;
         case open:
            var10000 = host.init_I_Str("writable_book") > 0;
            break;
         default:
            var10000 = true;
      }

      return var10000;
   }

   private static String init_Str_Str(String s) {
      if (s == null) {
         return "";
      }

      int colon = s.indexOf(58);
      return colon >= 0 ? s.substring(colon + 1) : s;
   }

   private List<String> init_List_G(G p) {
      List<String> out = new ArrayList<>();
      if (p.players != null && !p.players.isEmpty()) {
         List<String> targets = new ArrayList<>();

         for (String player : p.players) {
            if (player != null) {
               String t = this.write(player).trim();
               if (!t.isEmpty() && !targets.contains(t)) {
                  targets.add(t);
               }
            }
         }

         if (targets.isEmpty()) {
            return out;
         }

         long amount = Math.max(0L, G.init_J_Str(this.write(p.amountInput)));
         long[] divided = p.divideEnabled ? G.init(amount, targets.size()) : null;
         String template = p.commandTemplate != null && !p.commandTemplate.isBlank() ? p.commandTemplate : "/pay <player> <amount>";

         for (int i = 0; i < targets.size(); i++) {
            long per = divided != null ? divided[i] : amount;
            if (per > 0L) {
               String amountValue = String.valueOf(per);
               String cmd = template.replace("<player>", targets.get(i))
                  .replace("{player}", targets.get(i))
                  .replace("<amount>", amountValue)
                  .replace("{amount}", amountValue)
                  .trim();
               cmd = this.write(cmd).trim();
               if (!cmd.isEmpty()) {
                  out.add(cmd);
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private boolean init_Z_aC_EventBus(aC w, EventBus host) {
      if (!host.write_Z()) {
         return false;
      }

      double dx = host.init_D() - this.flush_D;
      double dy = w.horizontalOnly ? 0.0 : host.load_D() - this.close_D;
      double dz = host.read_D() - this.open_D;
      double d2 = dx * dx + dy * dy + dz * dz;
      double dist = Math.max(0.0, w.distance);
      return dist <= 0.0 ? d2 > 1.0E-4 : d2 >= dist * dist;
   }

   private boolean init_Z_aF_EventBus(aF w, EventBus host) {
      if (host.load_J() == this.start_J) {
         return false;
      }

      if (w.minDistance <= 0.0) {
         return true;
      }

      if (!host.write_Z()) {
         return false;
      }

      double dx = host.init_D() - this.flush_D;
      double dy = w.horizontalOnly ? 0.0 : host.load_D() - this.close_D;
      double dz = host.read_D() - this.open_D;
      return dx * dx + dy * dy + dz * dz >= w.minDistance * w.minDistance;
   }

   private boolean init_Z_aG_EventBus(aG w, EventBus host) {
      String cur = load_Str_Str(host.update_Str());
      if (cur.equals(load_Str_Str(this.flush_Str))) {
         return false;
      }

      String target = w.targetDimension;
      return target == null || target.isBlank() || cur.equals(load_Str_Str(target));
   }

   private static String load_Str_Str(String raw) {
      if (raw == null) {
         return "";
      } else {
         String v = raw.trim().toLowerCase(Locale.ROOT);
         if (v.isEmpty()) {
            return "";
         } else if (v.equals("nether") || v.equals("the_nether")) {
            return "minecraft:the_nether";
         } else if (v.equals("overworld") || v.equals("world")) {
            return "minecraft:overworld";
         } else if (!v.equals("end") && !v.equals("the_end")) {
            return v.contains(":") ? v : "minecraft:" + v;
         } else {
            return "minecraft:the_end";
         }
      }
   }

   private boolean init_Z_aK_EventBus(aK w, EventBus host) {
      autismclient.util.macro.ArrayUtil t = autismclient.util.macro.ArrayUtil.update(w.itemName);
      boolean var10000;
      switch (w.condition) {
         case init:
            var10000 = host.init_I_ArrayUtil(t) > 0;
            break;
         case load:
            var10000 = host.init_I_ArrayUtil(t) >= Math.max(1, w.count);
            break;
         case read:
            var10000 = host.init_I_ArrayUtil(t) != this.reset_I;
            break;
         case write:
            var10000 = host.init_I_ArrayUtil(t) > this.reset_I;
            break;
         case update:
            var10000 = host.init_I_ArrayUtil(t) < this.reset_I;
            break;
         case sync:
            var10000 = !host.init_Z_I(w.slot);
            break;
         case flush:
            var10000 = host.init_Z_I(w.slot);
            break;
         case close:
            var10000 = host.init_Z_I(w.slot) != this.close_Z;
            break;
         case open:
            var10000 = host.read_I() <= 0;
            break;
         case poll:
            var10000 = host.init_I_Str("") <= 0;
            break;
         case push:
            var10000 = host.init_Z_ArrayUtil(t);
            break;
         case pop:
            var10000 = host.sync_Z();
            break;
         case tick:
            var10000 = !host.sync_Z();
            break;
         case run:
            var10000 = host.load_I() == Math.max(0, Math.min(8, w.slot));
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_aN_EventBus(aN w, EventBus host) {
      if (!host.write_Z()) {
         return false;
      }

      double dx = host.init_D() - w.f_x_D;
      double dy = host.load_D() - w.f_y_D;
      double dz = host.read_D() - w.f_z_D;
      double lee = Math.max(0.0, w.leeway);
      if (dx * dx + dy * dy + dz * dz > lee * lee) {
         return false;
      }

      if (!w.checkRotation) {
         return true;
      }

      float dyaw = Math.abs(init_F_F(host.write_F() - w.yaw));
      float dpitch = Math.abs(host.read_F() - w.pitch);
      return dyaw <= w.rotLeeway && dpitch <= w.rotLeeway;
   }

   static boolean init_Z_ar_EventBus(ar w, EventBus host) {
      boolean var10000;
      switch (w.targetMode) {
         case init:
            var10000 = init_Z_ar_IArr(w, host.init_IArr()) || init_Z_ar_IArr(w, host.init_IArr_I(40));
            break;
         case read:
            var10000 = init_Z_ar_IArr(w, host.init_IArr_I(w.slot));
            break;
         case load:
            autismclient.util.macro.ArrayUtil t = autismclient.util.macro.ArrayUtil.update(w.itemName);
            var10000 = !t.update() && !t.init_Z()
               ? init_Z_ar_IArr(w, host.init_IArr()) || init_Z_ar_IArr(w, host.init_IArr_I(40))
               : init_Z_ar_IArr(w, host.init_IArr_ArrayUtil(t));
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean init_Z_ar_IArr(ar w, int[] dur) {
      if (dur != null && dur.length >= 2 && dur[1] > 0) {
         int damage = Math.max(0, dur[0]);
         int max = dur[1];
         int remaining = Math.max(0, max - damage);
         int var10000;
         switch (w.measurement) {
            case init:
               var10000 = remaining;
               break;
            case load:
               var10000 = damage;
               break;
            case read:
               var10000 = Math.round(remaining * 100.0F / max);
               break;
            default:
               throw new MatchException(null, null);
         }

         int metric = var10000;
         int cap = w.measurement == ar.Inner_StreamUtil.read ? 100 : max;
         int target = Math.max(0, Math.min(cap, w.value));
         boolean var8;
         switch (w.comparison) {
            case init:
               var8 = metric < target;
               break;
            case load:
               var8 = metric <= target;
               break;
            case read:
               var8 = metric == target;
               break;
            case write:
               var8 = metric >= target;
               break;
            case update:
               var8 = metric > target;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var8;
      } else {
         return false;
      }
   }

   private static float init_F_F(float deg) {
      float d = deg % 360.0F;
      if (d >= 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return d;
   }

   private static boolean load_Z_MacroAction_EventBus(MacroAction eff, EventBus host) {
      if (eff instanceof aw w) {
         return host.read_Z() && w.checkMode != aw.Inner_DataUtil.load && w.checkMode != aw.Inner_DataUtil.write;
      } else if (!(eff instanceof as w)) {
         return eff instanceof at w ? w.checkMode == at.Inner_DataUtil.init : true;
      } else {
         return host.read_Z() && w.condition != as.Inner_DataUtil.init && w.condition != as.Inner_DataUtil.read;
      }
   }

   private static long init_J_MacroAction(MacroAction action) {
      MacroAction eff = init_MacroAction_MacroAction(action);
      if (eff instanceof ax w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aJ w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aH w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof autismclient.util.macro.aM w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aK w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof ar w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof au w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aw w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof as w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aI w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else if (eff instanceof aA w && w.timeoutMs > 0) {
         return w.timeoutMs;
      } else {
         return !(eff instanceof aD)
               && !(eff instanceof aC)
               && !(eff instanceof aF)
               && !(eff instanceof aG)
               && !(eff instanceof aN)
               && !(eff instanceof au)
               && !(eff instanceof aw)
               && !(eff instanceof as)
               && !(eff instanceof aI)
               && !(eff instanceof aB)
               && !(eff instanceof aE)
               && !(eff instanceof autismclient.util.macro.aM)
               && !(eff instanceof av)
               && !(eff instanceof Q)
               && !(eff instanceof at)
               && !(eff instanceof aA)
            ? 10000L
            : 600000L;
      }
   }

   private static String read(String ref) {
      if (ref != null && !ref.isBlank()) {
         String s = ref.trim();
         if (s.startsWith("~")) {
            String[] parts = s.split("~");
            if (parts.length >= 3) {
               s = parts[2];
            }
         }

         int colon = s.indexOf(58);
         if (colon >= 0) {
            s = s.substring(colon + 1);
         }

         return s;
      } else {
         return "";
      }
   }

   private void init_V_I(int target) {
      this.poll_I = -1;
      this.push_I = 0;

      while (!this.init_Deque.isEmpty()) {
         int[] top = this.init_Deque.peek();
         if (target >= top[0] && target < top[1]) {
            break;
         }

         this.init_Deque.pop();
      }

      if (this.data_I >= 0 && (target < 0 || target >= this.data_I)) {
         this.data_I = -1;
         this.init_Set = null;
         this.write_List = null;
         this.add_Z = false;
      }
   }

   private void load_V() {
      this.close_I = 0;
      this.poll_I = -1;
      this.push_I = 0;
      this.init_Deque.clear();
      this.flush_J = 0L;
      this.init_FileCache = null;
      this.init_Z = false;
      this.init_StreamGate = null;
      this.load_Z = false;
      this.init_MacroAction = null;
      this.init_ao_DataUtil = null;
      this.pop_I = 0;
      this.write_Z = false;
      this.poll_J = 0L;
      this.init_TaskQueue = null;
      this.tick_I = -1;
      this.read_Str = "";
      this.push_J = 0L;
      this.init_y = null;
      this.run_I = 0;
      this.pop_J = 0L;
      this.init_u = null;
      this.get_I = 0;
      this.set_I = 0;
      this.put_I = 0;
      this.tick_J = 0L;
      this.add_I = 0;
      this.init_V();
      this.init_UiHelper = null;
      this.clear_J = 0L;
      this.next_I = 0;
      this.read_List = null;
      this.prev_I = 0;
      this.data_J = 0L;
      this.value_J = 0L;
      this.push_Z = false;
      this.pop_Z = false;
      this.tick_Z = false;
      this.run_Z = false;
      this.load_List = null;
      this.stop_I = 0;
      this.size_J = 0L;
      this.open_Z = false;
      this.poll_Z = false;
      this.add_Z = false;
      this.data_I = -1;
      this.init_Set = null;
      this.write_List = null;
   }

   private void init_V_Str(String why) {
      this.start_Z = true;
      this.poll_Str = why != null && !why.isBlank() ? why : "done";
   }

   private boolean sync() {
      if (!this.start_Z) {
         return this.clear_Z;
      }

      this.start_Z = false;
      return this.init_Z_Str(this.poll_Str);
   }

   private void load_V_Str(String why) {
      this.clear_Z = true;
      this.open_Str = why != null && !why.isBlank() ? why : "done";
   }

   boolean load_Z() {
      return !this.clear_Z && (this.init_FileCache != null || this.init_Z || this.value_I >= 0 && (this.init_aM.init_Z || this.close_I <= this.value_I));
   }

   boolean read_Z() {
      return this.init_FileCache != null || this.init_Z;
   }

   private boolean flush() {
      if (this.load_Z() && this.close_I >= 0 && this.close_I < this.init_List.size()) {
         MacroAction action = this.init_List.get(this.close_I);
         return action == null || !action.isEnabled() || action.getType() == null || init_Z_StreamGate(action.getType());
      } else {
         return false;
      }
   }

   void init_V_EventBus(EventBus host) {
      if (this.init_FileCache != null || this.init_Z) {
         host.load_V();
      }

      this.init_FileCache = null;
      this.init_Z = false;
   }

   private boolean init_Z_J_EventBus(long now, EventBus host) {
      FileCache.Inner_DataUtil result = this.init_FileCache.init(now, host);
      this.open_Str = this.init_FileCache.init();
      if (result == FileCache.Inner_DataUtil.init) {
         return false;
      } else {
         this.init_FileCache = null;
         this.init_Z = false;
         if (result == FileCache.Inner_DataUtil.read) {
            host.load_V();
            host.init_V_Str(this.open_Str);
            this.load_V_Str(this.open_Str);
            return false;
         } else {
            this.close_I++;
            return true;
         }
      }
   }

   private static boolean init_Z_StreamGate(autismclient.util.macro.StreamGate type) {
      boolean var10000;
      switch (type) {
         case f_X_StreamGate:
         case buffer:
         case f_T_StreamGate:
         case f_I_StreamGate:
         case f_U_StreamGate:
         case f_V_StreamGate:
         case f_J_StreamGate:
         case frame:
         case init:
         case f_K_StreamGate:
         case task:
         case node:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean init_Z_Str(String why) {
      if (this.get_Z && !this.set_Z && !this.put_Z) {
         this.set_Z = true;
         this.put_Z = true;
         this.close_Str = why;
         this.close_I = this.valid_I;
         this.init_V_I(this.valid_I);
         return false;
      } else {
         this.load_V_Str(why);
         return true;
      }
   }

   private void init_V_StreamGate(autismclient.util.macro.StreamGate type) {
      this.open_Str = "skipped " + type.name().toLowerCase(Locale.ROOT);
   }

   private void init_V_StreamGate_EventBus(autismclient.util.macro.StreamGate type, EventBus host) {
      this.init_V_StreamGate(type, host, "entity tracking is unavailable on this bot");
   }

   private void init_V_StreamGate(autismclient.util.macro.StreamGate type, EventBus host, String reason) {
      this.init_V_StreamGate(type);
      if (this.load_Set.add(type)) {
         host.init_V_Str("Skipped " + type.name() + ": " + reason + ".");
      }
   }

   boolean init_Z_J(long now) {
      return this.init_UiHelper != null && now < this.clear_J;
   }

   boolean write() {
      return this.init_UiHelper != null;
   }

   boolean update() {
      return this.reset_Z || this.init_UiHelper != null;
   }

   private static boolean load_Z_StreamGate(autismclient.util.macro.StreamGate type) {
      return type == autismclient.util.macro.StreamGate.open
         || type == autismclient.util.macro.StreamGate.close
         || type == autismclient.util.macro.StreamGate.f_H_StreamGate
         || type == autismclient.util.macro.StreamGate.init;
   }

   private boolean load_Z_EventBus_J(EventBus host, long now) {
      autismclient.util.macro.UiHelper action = this.init_UiHelper;
      if (action == null) {
         return true;
      }

      CustomMenuSnapshot snapshot = host.init_CustomMenuSnapshot();
      if (snapshot == null) {
         if (now < this.clear_J) {
            return false;
         }

         this.init_V_Str("custom screen never appeared (timed out)");
         return true;
      } else {
         autismclient.util.macro.RenderState.Inner_DataUtil prepared = autismclient.util.macro.RenderState.init(
            action, snapshot, value -> host.init_Str_Str_Map(value, this.load_Map)
         );
         if (!prepared.init_Z()) {
            if (prepared.init_Str() != null && prepared.init_Str().contains("unavailable")) {
               this.clear_J = now + action.init();
               this.open_Str = "waiting for password";
               return false;
            } else {
               this.init_V_Str("custom screen failed: " + prepared.init_Str());
               return true;
            }
         } else {
            CustomMenuSubmitResult result = host.init_CustomMenuSubmitRe_CustomMenuSnapshot_CustomMenuSubmissi(snapshot, prepared.init_CustomMenuSubmissi());
            if (result != null && result.success()) {
               this.open_Str = "submitted custom screen";
               return true;
            } else {
               String error = result == null ? "no submission result" : result.error();
               this.init_V_Str("custom screen failed: " + error);
               return true;
            }
         }
      }
   }

   private boolean init_Z_TaskQueue(autismclient.util.macro.TaskQueue a, EventBus host, long now) {
      autismclient.util.macro.TaskQueue.Inner_StateCache source = a.source == null ? autismclient.util.macro.TaskQueue.Inner_StateCache.init : a.source;
      if (source == autismclient.util.macro.TaskQueue.Inner_StateCache.close) {
         String query = this.init_Str_TaskQueue(a);
         if (query.isBlank()) {
            return false;
         }

         if (a.autofillCacheList) {
            return this.init_Z_TaskQueue(a, query, host, now);
         }

         List<String> cached = this.update_Map.get(query);
         if (cached != null && this.init_Z_TaskQueue_List(a, cached)) {
            return false;
         }

         int requestId = host.load_I_Str(query);
         if (requestId < 0) {
            return false;
         }

         this.init_TaskQueue = a;
         this.tick_I = requestId;
         this.read_Str = query;
         this.push_J = now + Math.max(100L, Math.min(60000L, a.autofillTimeoutMs));
         return true;
      } else if (source == autismclient.util.macro.TaskQueue.Inner_StateCache.read && a.waitForTrigger) {
         this.init_TaskQueue = a;
         this.push_J = now + 600000L;
         autismclient.util.macro.TaskQueue.Inner_JsonUtil line = this.init_TaskQueue_JsonUtil_TaskQueue_List(a, host.init_List());
         if (line != null) {
            this.update_Z = true;
            this.write_Str = line.init_Str();
            this.update_Str = update(line.flush());
         }

         return true;
      } else {
         if (this.init_Z_TaskQueue_EventBus(a, host)) {
            return false;
         }

         if (!a.waitForTrigger) {
            return false;
         }

         this.init_TaskQueue = a;
         this.push_J = now + 600000L;
         return true;
      }
   }

   private boolean read(EventBus host, long now) {
      autismclient.util.macro.TaskQueue.Inner_StateCache source = this.init_TaskQueue.source == null
         ? autismclient.util.macro.TaskQueue.Inner_StateCache.init
         : this.init_TaskQueue.source;
      if (source == autismclient.util.macro.TaskQueue.Inner_StateCache.close) {
         List<String> suggestions = host.init_List_I(this.tick_I);
         if (suggestions == null) {
            return now >= this.push_J;
         } else if (this.init_TextFormat_DataUti != null) {
            TextFormat.Inner_DataUtil union = this.init_TextFormat_DataUti;
            union.init_LinkedHashSet.addAll(suggestions);
            union.load = Math.max(union.load, suggestions.size());
            union.init_Z = true;
            this.init_Z_TaskQueue_List(this.init_TaskQueue, init_List_TextFormat_DataUti_TaskQueue(union, this.init_TaskQueue));
            this.init_V_TextFormat_DataUti_EventBus(union, host);
            return true;
         } else {
            this.init_Z_TaskQueue_List(this.init_TaskQueue, suggestions);
            return true;
         }
      } else if (source == autismclient.util.macro.TaskQueue.Inner_StateCache.read) {
         autismclient.util.macro.TaskQueue.Inner_JsonUtil line = this.init_TaskQueue_JsonUtil_TaskQueue(this.init_TaskQueue, host.init_List(), this.write_Str);
         if (line == null) {
            return now >= this.push_J;
         }

         if (!this.update_Z) {
            this.update_Z = true;
            this.write_Str = line.init_Str();
            this.update_Str = update(line.flush());
            return now >= this.push_J;
         }

         String currentText = update(line.flush());
         if (!currentText.equals(this.update_Str)) {
            this.write_Str = line.init_Str();
            this.update_Str = currentText;
            if (this.init_Z_TaskQueue_NetHelpe(this.init_TaskQueue.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(line))) {
               return true;
            }
         }

         return now >= this.push_J;
      } else {
         return this.init_Z_TaskQueue_EventBus(this.init_TaskQueue, host) ? true : now >= this.push_J;
      }
   }

   private boolean init_Z_TaskQueue_EventBus(autismclient.util.macro.TaskQueue a, EventBus host) {
      boolean var10000;
      switch (a.source == null ? autismclient.util.macro.TaskQueue.Inner_StateCache.init : a.source) {
         case open:
            var10000 = this.init_Z_TaskQueue_List(a, host.init_List_Z(a.excludeSelf));
            break;
         case read:
            var10000 = this.load_Z_TaskQueue_List(a, host.init_List());
            break;
         case close:
            var10000 = false;
            break;
         default:
            var10000 = this.load_Z_TaskQueue_EventBus(a, host);
      }

      return var10000;
   }

   private boolean load_Z_TaskQueue_EventBus(autismclient.util.macro.TaskQueue a, EventBus host) {
      String text = this.init_Str_TaskQueue_EventBus(a, host);
      if (text != null && init_Z_TaskQueue_Str(a, text)) {
         Map<String, String> additions = new LinkedHashMap<>();
         if (init_Z_TaskQueue(a) && a.pattern != null && !a.pattern.isBlank()) {
            autismclient.util.macro.UtilCore.init(a.matchMode, a.pattern, text).ifPresent(r -> r.init_Map().forEach((k, v) -> additions.put(k, v.init_Str())));
         }

         String saveAs = m.load(a.saveAs);
         if (!saveAs.isEmpty()) {
            additions.putIfAbsent(saveAs, text);
         }

         if (additions.isEmpty()) {
            return false;
         }

         autismclient.util.macro.TaskQueue.Inner_BufferHelper mode = a.numberMode == null
            ? autismclient.util.macro.TaskQueue.Inner_BufferHelper.init
            : a.numberMode;
         if (mode != autismclient.util.macro.TaskQueue.Inner_BufferHelper.init) {
            additions.replaceAll((k, v) -> init_Str_Str(v, k.equals(saveAs), init_j_DataUtil_TaskQueue_BufferHe(mode)));
         }

         if (!init_Z_TaskQueue_Map(a, additions)) {
            return false;
         }

         this.init_V_Map(additions);
         return true;
      } else {
         return false;
      }
   }

   private String init_Str_TaskQueue_EventBus(autismclient.util.macro.TaskQueue a, EventBus host) {
      String var10000;
      switch (a.source == null ? autismclient.util.macro.TaskQueue.Inner_StateCache.init : a.source) {
         case init:
            String t = host.flush_Str();
            var10000 = t != null && !t.isEmpty() ? t : null;
            break;
         case load:
            List<String> lines = host.init_List_J(0L);

            for (int i = lines.size() - 1; i >= 0; i--) {
               if (init_Z_TaskQueue_Str(a, lines.get(i))) {
                  return lines.get(i);
               }
            }

            var10000 = null;
            break;
         case flush:
         case sync:
         case write:
         case update:
            autismclient.util.macro.ArrayUtil filter = a.itemFilter != null && !a.itemFilter.isBlank()
               ? autismclient.util.macro.ArrayUtil.update(this.write(a.itemFilter))
               : null;
            var10000 = host.init_Str_TaskQueue_ArrayUtil(a, filter);
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   private boolean init_Z_TaskQueue_List(autismclient.util.macro.TaskQueue a, List<String> candidates) {
      if (candidates != null && !candidates.isEmpty() && autismclient.util.macro.ConfigStore.init(a.listFilter, a.listFilterText).isBlank()) {
         List<String> pool = autismclient.util.macro.ConfigStore.init(candidates, a.listFilter, a.listFilterText);
         pool = autismclient.util.macro.ConfigStore.init(pool, a.listExcludeText);
         autismclient.util.macro.ConfigStore.Inner_BufferHelper state = this.write_Map
            .computeIfAbsent(a, ignored -> new autismclient.util.macro.ConfigStore.Inner_BufferHelper());
         Optional<autismclient.util.macro.ConfigStore.Inner_StreamUtil> picked = autismclient.util.macro.ConfigStore.init(
            pool, a.listSelection, a.listPickPosition, state
         );
         if (picked.isEmpty()) {
            return false;
         }

         String value = picked.get().init_Str();
         if (a.listStripPrefix) {
            value = autismclient.util.macro.ConfigStore.init(value, a.listFilter, a.listFilterText);
         }

         String saveAs = m.load(a.saveAs);
         if (saveAs.isEmpty()) {
            return false;
         }

         this.init_V_Map(Map.of(saveAs, value));
         return true;
      } else {
         return false;
      }
   }

   private boolean load_Z_TaskQueue_List(autismclient.util.macro.TaskQueue a, List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> lines) {
      autismclient.util.macro.TaskQueue.Inner_JsonUtil selected = this.init_TaskQueue_JsonUtil_TaskQueue_List(a, lines);
      return selected != null && this.init_Z_TaskQueue_NetHelpe(a.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(selected));
   }

   private autismclient.util.macro.TaskQueue.Inner_JsonUtil init_TaskQueue_JsonUtil_TaskQueue_List(
      autismclient.util.macro.TaskQueue a, List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> lines
   ) {
      return this.init_TaskQueue_JsonUtil_TaskQueue(a, lines, a.scoreboardRow);
   }

   private autismclient.util.macro.TaskQueue.Inner_JsonUtil init_TaskQueue_JsonUtil_TaskQueue(
      autismclient.util.macro.TaskQueue a, List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> lines, String preferredKeyValue
   ) {
      if (lines != null && !lines.isEmpty()) {
         String preferredKey = preferredKeyValue == null ? "" : preferredKeyValue;

         for (autismclient.util.macro.TaskQueue.Inner_JsonUtil line : lines) {
            if (!preferredKey.isBlank() && preferredKey.equals(line.init_Str())) {
               return line;
            }
         }

         autismclient.util.macro.TaskQueue.Inner_JsonUtil rowCandidate = null;

         for (autismclient.util.macro.TaskQueue.Inner_JsonUtil line : lines) {
            if (init_Z_TaskQueue_TaskQueue_JsonUtil(a, line) && line.init_I() == a.scoreboardRowIndex) {
               rowCandidate = line;
               break;
            }
         }

         if (rowCandidate != null && a.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(rowCandidate).init_Z()) {
            return rowCandidate;
         }

         autismclient.util.macro.TaskQueue.Inner_JsonUtil selected = this.init_TaskQueue_JsonUtil_TaskQueue_2(a, lines, true);
         return selected != null ? selected : this.init_TaskQueue_JsonUtil_TaskQueue_2(a, lines, false);
      } else {
         return null;
      }
   }

   private autismclient.util.macro.TaskQueue.Inner_JsonUtil init_TaskQueue_JsonUtil_TaskQueue_2(
      autismclient.util.macro.TaskQueue a, List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> lines, boolean requireObjective
   ) {
      autismclient.util.macro.TaskQueue.Inner_JsonUtil selected = null;
      int bestDistance = 2147483647;

      for (autismclient.util.macro.TaskQueue.Inner_JsonUtil line : lines) {
         if (!requireObjective || init_Z_TaskQueue_TaskQueue_JsonUtil(a, line)) {
            autismclient.util.macro.TaskQueue.Inner_NetHelper preview = a.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(line);
            if (preview.init_Z()) {
               int distance = a.scoreboardRowIndex < 0 ? line.init_I() : Math.abs(line.init_I() - a.scoreboardRowIndex);
               if (selected == null || distance < bestDistance) {
                  selected = line;
                  bestDistance = distance;
               }
            }
         }
      }

      return selected;
   }

   private static boolean init_Z_TaskQueue_TaskQueue_JsonUtil(autismclient.util.macro.TaskQueue action, autismclient.util.macro.TaskQueue.Inner_JsonUtil line) {
      return action.scoreboardObjective == null || action.scoreboardObjective.isBlank() || action.scoreboardObjective.equals(line.load());
   }

   private void init_V_Map(Map<String, String> additions) {
      additions.forEach((name, value) -> {
         this.load_Map.put(name, value == null ? "" : value);
         this.read_Map.remove(name);
      });
   }

   private boolean init_Z_TaskQueue_NetHelpe(autismclient.util.macro.TaskQueue.Inner_NetHelper preview) {
      if (preview != null && preview.init_Z() && !preview.init_Map().isEmpty()) {
         preview.init_Map().forEach((name, value) -> {
            this.load_Map.put(name, value == null ? "" : value.init_Str());
            if (value == null) {
               this.read_Map.remove(name);
            } else {
               this.read_Map.put(name, value);
            }
         });
         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_TaskQueue(autismclient.util.macro.TaskQueue a, String query, EventBus host, long now) {
      TextFormat.Inner_DataUtil union = this.sync_Map.computeIfAbsent(query, TextFormat.Inner_DataUtil::new);
      if (union.init_Z) {
         this.init_Z_TaskQueue_List(a, init_List_TextFormat_DataUti_TaskQueue(union, a));
         this.init_V_TextFormat_DataUti_EventBus(union, host);
         return false;
      } else {
         int id = host.load_I_Str(query);
         if (id < 0) {
            this.init_Z_TaskQueue_List(a, init_List_TextFormat_DataUti_TaskQueue(union, a));
            return false;
         } else {
            this.init_TaskQueue = a;
            this.tick_I = id;
            this.read_Str = query;
            this.init_TextFormat_DataUti = union;
            this.push_J = now + Math.max(100L, Math.min(60000L, a.autofillTimeoutMs));
            return true;
         }
      }
   }

   private void init_V_TextFormat_DataUti_EventBus(TextFormat.Inner_DataUtil union, EventBus host) {
      if (union.read >= 0) {
         List<String> reply = host.init_List_I(union.read);
         if (reply == null) {
            return;
         }

         union.init_LinkedHashSet.addAll(reply);
         int size = reply.size();
         boolean truncated = size >= union.load && union.load >= 8;
         union.load = Math.max(union.load, size);
         if (truncated) {
            union.init_ArrayDeque.add(union.init_Str);
         }

         union.read = -1;
         union.init_Str = "";
      }

      if (union.init_LinkedHashSet.size() >= 10000) {
         union.init_ArrayDeque.clear();
      } else {
         String target = init_Str_TextFormat_DataUti(union);
         if (target != null) {
            int id = host.load_I_Str(target);
            if (id >= 0) {
               union.read = id;
               union.init_Str = target;
            }
         }
      }
   }

   private static String init_Str_TextFormat_DataUti(TextFormat.Inner_DataUtil union) {
      while (!union.init_ArrayDeque.isEmpty()) {
         if (union.init_I < "abcdefghijklmnopqrstuvwxyz0123456789_".length()) {
            String target = union.init_ArrayDeque.peek() + "abcdefghijklmnopqrstuvwxyz0123456789_".charAt(union.init_I);
            union.init_I++;
            return target;
         }

         union.init_ArrayDeque.poll();
         union.init_I = 0;
      }

      return null;
   }

   private static List<String> init_List_TextFormat_DataUti_TaskQueue(TextFormat.Inner_DataUtil union, autismclient.util.macro.TaskQueue a) {
      List<String> pool = new ArrayList<>(union.init_LinkedHashSet);
      boolean ordered = a.listSelection == autismclient.util.macro.ConfigStore.Inner_CachePool.load
         || a.listSelection == autismclient.util.macro.ConfigStore.Inner_CachePool.write
         || a.listSelection == autismclient.util.macro.ConfigStore.Inner_CachePool.update
         || a.listSelection == autismclient.util.macro.ConfigStore.Inner_CachePool.sync;
      if (ordered) {
         pool.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return pool;
   }

   private String init_Str_TaskQueue(autismclient.util.macro.TaskQueue a) {
      String query = this.write(a.autofillCommand);
      if (query == null) {
         return "";
      }

      if (!query.startsWith("/")) {
         query = "/" + query;
      }

      if (!query.contains(" ")) {
         query = query + " ";
      }

      String prefix = a.listFilter == autismclient.util.macro.ConfigStore.Inner_DataUtil.load && a.listFilterText != null
         ? this.write(a.listFilterText).trim()
         : "";
      if (!prefix.isEmpty() && !prefix.contains(" ") && query.endsWith(" ")) {
         int end = query.length();

         while (end > 0 && query.charAt(end - 1) == ' ') {
            end--;
         }

         query = query.substring(0, end) + " " + prefix;
      }

      return query;
   }

   private int init_I_u(u book, int index, int total, EventBus host) {
      List<String> pages = this.init_List_u(book);
      if (pages != null && !pages.isEmpty()) {
         String title = book.title == null ? "" : this.write(book.title);
         if (book.appendCount && total > 1 && index > 0) {
            title = title + " #" + (index + 1);
         }

         int slot = host.init_I_List(pages, title, book.sign, book.requireHeldWritableBook, this.put_I);
         if (slot == -2) {
            return -2;
         } else if (slot >= 0 && slot <= 8) {
            this.put_I |= 1 << slot;
            return slot;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private List<String> init_List_u(u book) {
      int pageCount = Math.max(1, Math.min(100, book.pages));
      int chars = Math.max(1, Math.min(1024, book.characters));
      String source = book.dataSource == null ? "Random" : book.dataSource.trim();
      if ("File".equalsIgnoreCase(source)) {
         return null;
      }

      if ("Pasted".equalsIgnoreCase(source) && book.customComponent != null && !book.customComponent.isEmpty()) {
         String text = this.write(book.customComponent);
         List<String> pages = new ArrayList<>(pageCount);

         for (int offset = 0; offset < text.length() && pages.size() < pageCount; offset += chars) {
            pages.add(text.substring(offset, Math.min(text.length(), offset + chars)));
         }

         if (pages.isEmpty()) {
            pages.add("");
         }

         return List.copyOf(pages);
      } else {
         return PkgCls_b.init(pageCount, chars, book.randomType, new Random());
      }
   }

   private void init_V_EventBus_Z(EventBus host, boolean completed) {
      u book = this.init_u;
      this.init_u = null;
      this.get_I = 0;
      this.set_I = 0;
      this.put_I = 0;
      this.tick_J = 0L;
      this.add_I = 0;
      if (completed && book != null && book.disconnectAfter) {
         host.read_V_Str("NBT book macro");
      }
   }

   private static boolean init_Z_TaskQueue(autismclient.util.macro.TaskQueue a) {
      return a.matchMode != null && a.matchMode != autismclient.util.macro.UtilCore.Inner_DataUtil.init;
   }

   private static boolean init_Z_TaskQueue_Str(autismclient.util.macro.TaskQueue a, String text) {
      if (text == null) {
         return false;
      } else if (a.pattern != null && !a.pattern.isBlank()) {
         return init_Z_TaskQueue(a)
            ? autismclient.util.macro.UtilCore.init(a.matchMode, a.pattern, text).isPresent()
            : text.toLowerCase(Locale.ROOT).contains(a.pattern.toLowerCase(Locale.ROOT));
      } else {
         return true;
      }
   }

   private static j.Inner_DataUtil init_j_DataUtil_TaskQueue_BufferHe(autismclient.util.macro.TaskQueue.Inner_BufferHelper mode) {
      return mode == autismclient.util.macro.TaskQueue.Inner_BufferHelper.read ? j.Inner_DataUtil.load : j.Inner_DataUtil.init;
   }

   private static String init_Str_Str(String v, boolean embedded, j.Inner_DataUtil style) {
      BigDecimal n = init_BigDecimal_Str(v, embedded, style);
      return n == null ? v : init_Str_BigDecimal(n);
   }

   private static boolean init_Z_TaskQueue_Map(autismclient.util.macro.TaskQueue a, Map<String, String> values) {
      autismclient.util.macro.TaskQueue.Inner_TaskQueue mod = a.numberModifier == null
         ? autismclient.util.macro.TaskQueue.Inner_TaskQueue.init
         : a.numberModifier;
      if (mod != autismclient.util.macro.TaskQueue.Inner_TaskQueue.init && !values.isEmpty()) {
         if (!Double.isFinite(a.numberModifierAmount)) {
            return false;
         }

         if (mod == autismclient.util.macro.TaskQueue.Inner_TaskQueue.update && a.numberModifierAmount == 0.0) {
            return false;
         }

         BigDecimal operand = BigDecimal.valueOf(a.numberModifierAmount);
         String outputName = m.load(a.saveAs);
         boolean targetTracked = !outputName.isBlank() && values.containsKey(outputName);
         j.Inner_DataUtil style = init_j_DataUtil_TaskQueue_BufferHe(
            a.numberMode == null ? autismclient.util.macro.TaskQueue.Inner_BufferHelper.init : a.numberMode
         );
         boolean ok = false;

         for (Entry<String, String> e : values.entrySet()) {
            boolean target = targetTracked && e.getKey().equals(outputName);
            BigDecimal n = init_BigDecimal_Str(e.getValue(), target, style);
            if (n != null) {
               BigDecimal var10000;
               switch (mod) {
                  case load:
                     var10000 = n.add(operand);
                     break;
                  case read:
                     var10000 = n.subtract(operand);
                     break;
                  case write:
                     var10000 = n.multiply(operand);
                     break;
                  case update:
                     var10000 = init_BigDecimal_BigDecimal_BigDecimal(n, operand);
                     break;
                  case sync:
                     var10000 = load_BigDecimal_BigDecimal_BigDecimal(
                        n.multiply(BigDecimal.ONE.add(autismclient.util.macro.TaskQueue.init_BigDecimal_BigDecimal(operand))), n
                     );
                     break;
                  case flush:
                     var10000 = load_BigDecimal_BigDecimal_BigDecimal(
                        n.multiply(BigDecimal.ONE.subtract(autismclient.util.macro.TaskQueue.init_BigDecimal_BigDecimal(operand))), n
                     );
                     break;
                  case init:
                     var10000 = n;
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               BigDecimal r = var10000;
               e.setValue(init_Str_BigDecimal(r));
               if (target || !targetTracked) {
                  ok = true;
               }
            }
         }

         return ok;
      } else {
         return true;
      }
   }

   private static BigDecimal init_BigDecimal_BigDecimal_BigDecimal(BigDecimal number, BigDecimal operand) {
      return load_BigDecimal_BigDecimal_BigDecimal(number.divide(operand, new MathContext(Math.max(16, number.precision() + 8), RoundingMode.HALF_UP)), number);
   }

   private static BigDecimal load_BigDecimal_BigDecimal_BigDecimal(BigDecimal result, BigDecimal source) {
      int scale = Math.max(2, source.scale() + 2);
      int adjustedExponent = result.precision() - result.scale() - 1;
      if (result.signum() != 0 && adjustedExponent < 0) {
         scale = Math.max(scale, 1 - adjustedExponent);
      }

      return result.setScale(scale, RoundingMode.HALF_UP);
   }

   private static BigDecimal init_BigDecimal_Str(String raw, boolean embedded, j.Inner_DataUtil style) {
      if (raw == null) {
         return null;
      }

      try {
         return new BigDecimal(embedded ? j.init(raw, style) : j.load(raw, style));
      } catch (RuntimeException ex) {
         return null;
      }
   }

   private static String init_Str_BigDecimal(BigDecimal n) {
      BigDecimal stripped = n.stripTrailingZeros();
      return stripped.scale() <= 0 ? stripped.toBigInteger().toString() : stripped.toPlainString();
   }

   private aQ init_aQ_aQ(aQ source) {
      aQ copy = new aQ();
      copy.mode = source.mode;
      copy.transferMode = source.transferMode;
      copy.safeClickDelayTicks = source.safeClickDelayTicks;
      copy.safeClickDelayAfterPickup = source.safeClickDelayAfterPickup;
      copy.safeClickDelayBeforeReturn = source.safeClickDelayBeforeReturn;
      copy.carryCursor = source.carryCursor;
      copy.useCrafting = source.useCrafting;
      copy.useArmor = source.useArmor;
      copy.useOffhand = source.useOffhand;
      List<autismclient.util.macro.ArrayUtil> configured = source.entryTargets.isEmpty()
         ? source.entries.stream().map(autismclient.util.macro.ArrayUtil::update).toList()
         : source.entryTargets;
      int count = Math.min(11, configured.size());

      for (int i = 0; i < count; i++) {
         autismclient.util.macro.ArrayUtil target = configured.get(i);
         if (target != null) {
            autismclient.util.macro.ArrayUtil resolved = target.init_ArrayUtil();
            String template = target.poll != null && !target.poll.isBlank() ? target.poll : target.init_Str();
            if (template != null && template.indexOf(123) >= 0) {
               autismclient.util.macro.ArrayUtil dynamic = autismclient.util.macro.ArrayUtil.update(this.write(template));
               if (target.init_Z()) {
                  dynamic.init_I = target.init_I;
               }

               resolved = dynamic;
            }

            if (resolved.init_Z() || resolved.update()) {
               copy.entryTargets.add(resolved);
               copy.entryDestinations.add(source.load_I_I(i));
               copy.entryAmountModes.add(source.init_aQ_DataUtil_I(i));
               copy.entryAmounts.add(source.read_I_I(i));
            }
         }
      }

      return copy;
   }

   private j.Inner_CachePool init_j_CachePool_Str(String template) {
      return template != null && !template.isEmpty() && template.indexOf(123) >= 0
         ? j.init_j_CachePool(template, this.init_m(), null)
         : j.Inner_CachePool.init(template == null ? "" : template);
   }

   private String write(String template) {
      return this.init_j_CachePool_Str(template).init_Str();
   }

   private m init_m() {
      m ctx = new m();
      EventBus host = this.init_EventBus;
      if (host != null) {
         init_V_m_EventBus(ctx, host);
      }

      for (Entry<String, String> e : this.load_Map.entrySet()) {
         l structured = this.read_Map.get(e.getKey());
         ctx.init(e.getKey(), structured != null ? structured : l.init_l_Obj(e.getValue()));
      }

      return ctx;
   }

   private static void init_V_m_EventBus(m ctx, EventBus host) {
      String username = host.init_Str();
      if (!username.isBlank()) {
         l u = l.init_l_Obj(username);
         ctx.init("username", u);
         ctx.init("user", u);
         ctx.init("player", u);
      }

      String uuid = host.load_Str();
      if (!uuid.isBlank()) {
         ctx.init("uuid", l.init_l_Obj(uuid));
      }

      String server = host.read_Str();
      if (!server.isBlank()) {
         ctx.init("server", l.init_l_Obj(server));
      }

      String password = host.write_Str();
      if (!password.isBlank()) {
         ctx.init("password", l.init_l_Obj(password));
      }

      String dim = host.update_Str();
      if (dim != null && !dim.isBlank()) {
         l d = l.init_l_Obj(dim);
         ctx.init("dimension", d);
         ctx.init("dim", d);
      }

      l slot = l.init_l_I(Math.max(0, Math.min(8, host.load_I())));
      ctx.init("selected_slot", slot);
      ctx.init("target_slot", slot);
      if (host.write_Z()) {
         double x = host.init_D();
         double y = host.load_D();
         double z = host.read_D();
         ctx.init("x", l.init_l_Obj(String.format(Locale.ROOT, "%.3f", x)));
         ctx.init("y", l.init_l_Obj(String.format(Locale.ROOT, "%.3f", y)));
         ctx.init("z", l.init_l_Obj(String.format(Locale.ROOT, "%.3f", z)));
         int bx = (int)Math.floor(x);
         int by = (int)Math.floor(y);
         int bz = (int)Math.floor(z);
         ctx.init("bx", l.init_l_Obj(Integer.toString(bx)));
         ctx.init("by", l.init_l_Obj(Integer.toString(by)));
         ctx.init("bz", l.init_l_Obj(Integer.toString(bz)));
         ctx.init("pos", l.init_l_Obj(bx + " " + by + " " + bz));
         float yaw = host.write_F();
         float pitch = host.read_F();
         ctx.init("yaw", l.init_l_Obj(String.format(Locale.ROOT, "%.2f", yaw)));
         ctx.init("pitch", l.init_l_Obj(String.format(Locale.ROOT, "%.2f", pitch)));
         ctx.init("rot", l.init_l_Obj(String.format(Locale.ROOT, "%.2f %.2f", yaw, pitch)));
         ctx.init("facing", l.init_l_Obj(Direction.fromYRot(yaw).getName()));
      }
   }

   private static String init_Str_bo_DataUtil(bo.Inner_DataUtil mode) {
      if (mode == bo.Inner_DataUtil.load_bo_DataUtil) {
         return "right";
      } else {
         return mode == bo.Inner_DataUtil.read ? "shift" : "left";
      }
   }

   private static String update(String s) {
      return s == null ? "" : s;
   }

   private static final class Inner_DataUtil {
      final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
      final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();
      int init_I;
      int load;
      boolean init_Z;
      int read = -1;
      String init_Str = "";

      Inner_DataUtil(String baseQuery) {
         this.init_ArrayDeque.add(baseQuery);
      }
   }

   private record Inner_StreamUtil(List<int[]> init_List, long init_J, long load_J, boolean init_Z, boolean load_Z) {
   }
}
