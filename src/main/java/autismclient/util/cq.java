package autismclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;

public final class cq {
   private static final String read_Str = "\u001f";
   private static final long init_J = 15000L;
   private static final long load_J = 5000L;
   private static final int load_I = 3;
   private static final long read_J = 30000L;
   private static final long write_J = 60000L;
   private static final long update_J = 2000L;
   private static final long sync_J = 3000L;
   private static final long flush_J = 2000L;
   private static final long close_J = 1500L;
   private static final long open = 3000L;
   private static final long poll = 3000L;
   private static final long push = 3000L;
   private static final int read_I = 524288;
   private static volatile cq.Inner_ConfigStore init_cq_ConfigStore = new cq.Inner_ConfigStore(
      cq.Inner_TaskQueue.write, "", "", 0L, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, ""
   );
   private static volatile long pop;
   private static volatile long tick = -9223372036854775808L;
   private static volatile long run;
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static final Object init_Obj = new Object();
   private static Process init_Process;
   private static Thread init_Thread;
   private static int write_I;
   private static int update_I;
   private static boolean read_Z;
   private static long get;
   private static final Map<String, Boolean> read_Map = new ConcurrentHashMap<>();
   private static volatile Path init_Path;
   private static volatile Path load_Path;
   private static final Object load_Obj = new Object();
   private static final Object read_Obj = new Object();
   private static final Pattern init_Pattern = Pattern.compile("\"artworkUrl100\" *: *\"([^\"]+)\"");
   private static volatile String write_Str;
   private static final long set = 30000L;
   static final Map<String, Long> init_Map = new ConcurrentHashMap<>();
   static final int init_I = 512;
   static final Map<String, String> load_Map = new LinkedHashMap<String, String>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> eldest) {
         return this.size() > 512;
      }
   };
   private static volatile String update_Str;
   private static volatile String sync_Str;
   static String init_Str = "https://itunes.apple.com/search";
   private static final String[] init_StrArr = new String[]{"strict", "title", "artist", "top-hit", "artist-query"};
   private static volatile int sync_I = -1;
   private static final String[] load_StrArr = new String[]{
      "karaoke",
      "tribute",
      "emulation",
      "chiptune",
      "8-bit",
      "16-bit",
      "8 bit",
      "16 bit",
      "lofi version",
      "cover version",
      "made famous",
      "in the style of",
      "instrumental version"
   };
   static String load_Str = "https://api.deezer.com/search";
   private static final String flush_Str = "$ErrorActionPreference = 'Continue'\n[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n\n$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\nfunction Await-Op($op, $resultType) {\n    $task = $asTaskGeneric.MakeGenericMethod($resultType).Invoke($null, @($op))\n    if (-not $task.Wait(5000)) { throw 'timed out waiting for a WinRT operation' }\n    return $task.Result\n}\n\n$code = @'\nusing System;\nusing System.Collections.Generic;\nusing System.Diagnostics;\nusing System.Runtime.InteropServices;\n\n// ISimpleAudioVolume get/set for the session belonging to a process-name hint.\n// Raw vtable throughout: on machines with an audio enhancement driver (probed on\n// this one), the session ENUMERATOR object answers QueryInterface with\n// E_NOINTERFACE for interfaces its vtable actually implements, so the typed RCW\n// path cannot reach it; the vtable contracts are stable, so calling slots\n// directly works on wrapped and healthy machines alike.\npublic static class AutismSpotifyVolume {\n    [ComImport, Guid(\"A95664D2-9614-4F35-A746-DE8DB63617E6\"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]\n    public interface IMMDeviceEnumerator {\n        [PreserveSig] int EnumAudioEndpoints(int dataFlow, int stateMask, out IntPtr devices);\n        [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IntPtr device);\n        [PreserveSig] int GetDevice([MarshalAs(UnmanagedType.LPWStr)] string id, out IntPtr device);\n        [PreserveSig] int RegisterEndpointNotificationCallback(IntPtr client);\n        [PreserveSig] int UnregisterEndpointNotificationCallback(IntPtr client);\n    }\n\n    [ComImport, Guid(\"BCDE0395-E52F-467C-8E3D-C4579291692E\")]\n    public class MMDeviceEnumeratorComObject { }\n\n    static readonly Guid IID_MGR2 = new Guid(\"77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F\");\n    static readonly Guid IID_VOLUME = new Guid(\"87CE5498-68D6-44E5-9215-6DA47EF883D8\");\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComOutOut(IntPtr t, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetCount(IntPtr t, out int v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComItem(IntPtr t, int i, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComActivate(IntPtr t, [MarshalAs(UnmanagedType.LPStruct)] Guid iid, int c, IntPtr p, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetPid(IntPtr t, out uint pid);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetVolume(IntPtr t, out float level);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComSetVolume(IntPtr t, float level, IntPtr ctx);\n\n    static IntPtr Slot(IntPtr obj, int slot) {\n        return Marshal.ReadIntPtr(Marshal.ReadIntPtr(obj), slot * IntPtr.Size);\n    }\n\n    // Album art, take four. Every earlier read path failed on this box, all\n    // probed: PowerShell's binder cannot bind the WinRT AsStream extension, a\n    // winmd reference for compile-time WinRT types fails to load (0x80131047),\n    // the returned stream's RCW exposes no interfaces, and vtable reads against a\n    // NATIVE buffer (IBufferFactory) either lie (hollow S_OK) or segfault the\n    // helper (exit 139). So the read buffer is a .NET-BACKED one:\n    // System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer wraps a\n    // managed byte[] as an IBuffer with a CLR-generated, guaranteed-valid vtable.\n    // The only native vtable call left is IInputStream.ReadAsync (slot 6), proven\n    // safe here. Bytes come back through WindowsRuntimeBufferExtensions.ToArray,\n    // purely managed. Crash containment: the MTA worker (apartment sensitivity,\n    // probed) plus HandleProcessCorruptedStateExceptions, and an all-zero guard -\n    // on this stack the read returns S_OK with a NULL op and a zero-filled buffer\n    // (hollow success), which must read as failure, never as a 4 MB black image.\n    [System.Runtime.ExceptionServices.HandleProcessCorruptedStateExceptions]\n    static byte[] ReadArtStreamMta(object streamObj) {\n        IntPtr streamPtr = IntPtr.Zero, istream = IntPtr.Zero, bufPtr = IntPtr.Zero, op = IntPtr.Zero;\n        try {\n            System.Reflection.Assembly wrtAsm = null;\n            foreach (System.Reflection.Assembly asm in AppDomain.CurrentDomain.GetAssemblies()) {\n                if (asm.GetName().Name == \"System.Runtime.WindowsRuntime\") { wrtAsm = asm; break; }\n            }\n            if (wrtAsm == null) return null;\n            Type wrb = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer\");\n            Type ext = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBufferExtensions\");\n            if (wrb == null || ext == null) return null;\n            System.Reflection.MethodInfo create = null, toArray = null;\n            foreach (System.Reflection.MethodInfo m in wrb.GetMethods()) {\n                if (m.Name == \"Create\" && m.GetParameters().Length == 4) { create = m; break; }\n            }\n            foreach (System.Reflection.MethodInfo m in ext.GetMethods()) {\n                if (m.Name == \"ToArray\" && m.GetParameters().Length == 1) { toArray = m; break; }\n            }\n            if (create == null || toArray == null) return null;\n            byte[] backing = new byte[4 * 1024 * 1024];\n            object buffer = create.Invoke(null, new object[] { backing, 0, backing.Length, backing.Length });\n            Guid iidStream = new Guid(\"905a0fe1-bc53-11df-8c49-001e4fc686da\");\n            streamPtr = Marshal.GetIUnknownForObject(streamObj);\n            if (Marshal.QueryInterface(streamPtr, ref iidStream, out istream) < 0 || istream == IntPtr.Zero) return null;\n            bufPtr = Marshal.GetIUnknownForObject(buffer);\n            int hrRead = Marshal.GetDelegateForFunctionPointer<ComReadAsync>(Slot(istream, 6))(istream, bufPtr, (uint)backing.Length, 0u, out op);\n            if (hrRead < 0) return null;\n            if (op != IntPtr.Zero) { // async op exists: wait it out; a null op means the fill was synchronous\n                int status = 0;\n                long deadline = DateTime.UtcNow.Ticks + 5L * 10000000L;\n                while (status == 0 && DateTime.UtcNow.Ticks < deadline) {\n                    System.Threading.Thread.Sleep(10);\n                    if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(op, 7))(op, out status) < 0) return null;\n                }\n                if (status != 1) return null;\n            }\n            byte[] result = (byte[])toArray.Invoke(null, new object[] { buffer });\n            // The hollow-success guard: a hollow read leaves the wrapper at its\n            // initial all-zero content; real album art always has non-zero bytes.\n            bool anyNonZero = false;\n            for (int i = 0; i < result.Length; i++) {\n                if (result[i] != 0) { anyNonZero = true; break; }\n            }\n            return anyNonZero ? result : null;\n        } catch (System.Exception) {\n            return null;\n        } finally {\n            if (op != IntPtr.Zero) Marshal.Release(op);\n            if (bufPtr != IntPtr.Zero) Marshal.Release(bufPtr);\n            if (istream != IntPtr.Zero) Marshal.Release(istream);\n            if (streamPtr != IntPtr.Zero) Marshal.Release(streamPtr);\n        }\n    }\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComReadAsync(IntPtr t, IntPtr buffer, uint count, uint options, out IntPtr op);\n\n    static HashSet<int> hintPids = new HashSet<int>();\n    static string hintCached = \"\";\n    static DateTime hintRefreshed = DateTime.MinValue;\n\n    // Process-name matching by either-way containment: \"spotify\" hits Spotify.exe,\n    // and a packaged AUMID head like \"SpotifyAB.SpotifyMusic_xxx\" still contains it.\n    static HashSet<int> PidsForHint(string appHint) {\n        if (appHint != hintCached || (DateTime.UtcNow - hintRefreshed).TotalSeconds > 2.0) {\n            HashSet<int> set = new HashSet<int>();\n            string hint = (appHint ?? \"\").ToLowerInvariant();\n            if (hint.Length > 0) {\n                foreach (Process p in Process.GetProcesses()) {\n                    try {\n                        string n = p.ProcessName.ToLowerInvariant();\n                        if (hint.Contains(n) || n.Contains(hint)) set.Add(p.Id);\n                    } catch { }\n                }\n            }\n            hintPids = set;\n            hintCached = appHint ?? \"\";\n            hintRefreshed = DateTime.UtcNow;\n        }\n        return hintPids;\n    }\n\n    public static int GetSessionVolume(string appHint) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return -1;\n        try {\n            float v;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetVolume>(Slot(volume, 4))(volume, out v) < 0) return -1;\n            return (int)Math.Round(v * 100.0f);\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    public static bool SetSessionVolume(string appHint, double fraction) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return false;\n        try {\n            return Marshal.GetDelegateForFunctionPointer<ComSetVolume>(Slot(volume, 3))(volume, (float)fraction, IntPtr.Zero) >= 0;\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    static IntPtr FindVolume(string appHint) {\n        HashSet<int> pids = PidsForHint(appHint);\n        if (pids.Count == 0) return IntPtr.Zero;\n        IMMDeviceEnumerator enumerator = (IMMDeviceEnumerator)new MMDeviceEnumeratorComObject();\n        IntPtr collection;\n        if (enumerator.EnumAudioEndpoints(0, 1, out collection) < 0) return IntPtr.Zero;\n        try {\n            int deviceCount;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(collection, 3))(collection, out deviceCount) < 0) return IntPtr.Zero;\n            for (int d = 0; d < deviceCount; d++) {\n                IntPtr device;\n                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(collection, 4))(collection, d, out device) < 0) continue;\n                if (device == IntPtr.Zero) continue;\n                try {\n                    IntPtr manager;\n                    if (Marshal.GetDelegateForFunctionPointer<ComActivate>(Slot(device, 3))(device, IID_MGR2, 23, IntPtr.Zero, out manager) < 0) continue;\n                    if (manager == IntPtr.Zero) continue;\n                    try {\n                        IntPtr enumPtr;\n                        if (Marshal.GetDelegateForFunctionPointer<ComOutOut>(Slot(manager, 5))(manager, out enumPtr) < 0) continue;\n                        if (enumPtr == IntPtr.Zero) continue;\n                        try {\n                            int count;\n                            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(enumPtr, 3))(enumPtr, out count) < 0) continue;\n                            for (int i = 0; i < count; i++) {\n                                IntPtr sessionPtr;\n                                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(enumPtr, 4))(enumPtr, i, out sessionPtr) < 0) continue;\n                                if (sessionPtr == IntPtr.Zero) continue;\n                                try {\n                                    uint sessionPid;\n                                    if (Marshal.GetDelegateForFunctionPointer<ComGetPid>(Slot(sessionPtr, 14))(sessionPtr, out sessionPid) < 0) continue;\n                                    if (!pids.Contains((int)sessionPid)) continue;\n                                    Guid iid = IID_VOLUME;\n                                    IntPtr volumePtr;\n                                    if (Marshal.QueryInterface(sessionPtr, ref iid, out volumePtr) < 0) continue;\n                                    return volumePtr; // caller releases\n                                } finally {\n                                    Marshal.Release(sessionPtr);\n                                }\n                            }\n                        } finally {\n                            Marshal.Release(enumPtr);\n                        }\n                    } finally {\n                        Marshal.Release(manager);\n                    }\n                } finally {\n                    Marshal.Release(device);\n                }\n            }\n            return IntPtr.Zero;\n        } finally {\n            Marshal.Release(collection);\n        }\n    }\n}\n'@\nAdd-Type -TypeDefinition $code\n\n$managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType=WindowsRuntime]\n$propsType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType=WindowsRuntime]\n$boolType = [bool]\n$us = [char]31\n$inv = [Globalization.CultureInfo]::InvariantCulture\n$manager = $null\n$artKey = ''\n$artPath = Join-Path $env:TEMP 'autism_spotify_art.png'\n$anyMedia = $env:AUTISM_SPOTIFY_SOURCE -eq 'ANY'\n# Stdin commands are read through a raw StreamReader over the standard input,\n# NOT [Console]::In.ReadLineAsync: on .NET Framework that call takes the console\n# sync root while it blocks on an EMPTY anonymous pipe (exactly what Java's\n# ProcessBuilder hands us - probed: the helper froze silently after 'before\n# readline' with zero output). StreamReader.ReadLineAsync runs on a pool thread\n# with no console lock, so the status loop never stalls on an empty pipe.\n$stdinReader = New-Object System.IO.StreamReader([Console]::OpenStandardInput())\n$readTask = $stdinReader.ReadLineAsync()\n$stdinDead = $false\n# Event-driven refresh: MediaPropertiesChanged (track skip) and\n# PlaybackInfoChanged (play/pause) set a GLOBAL dirty flag (global, not script\n# scope, because the handler executes off the main runspace thread). The loop\n# below re-reads immediately on dirty, otherwise every 450 ms. NOTE: on this box\n# GSMTC property-change events register fine but NEVER fire (probed: two real\n# skips, zero firings - the same stack that hollows art streams and returns null\n# RepeatMode), so the event path is inert here and the 450 ms poll is what\n# actually delivers faster updates: worst-case staleness is halved.\n$global:spotifyDirty = $false\n$subscribedAumid = ''\n$lastPoll = [DateTime]::UtcNow.AddSeconds(-1)\n\nfunction Save-Art($props, $path) {\n    try {\n        if ($null -eq $props.Thumbnail) { return $false }\n        $stream = Await-Op ($props.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\n        if ($null -eq $stream) { return $false }\n        $bytes = [AutismSpotifyVolume]::ReadArtStream($stream)\n        if ($null -eq $bytes) { return $false }\n        # Atomic-ish publish: write a temp sibling, then Copy over the stable path\n        # and delete the temp. [IO.File]::Move(source, dest, overwrite) does NOT\n        # exist on .NET Framework 4.x (only .NET Core 3.0+), so Copy+Delete is the\n        # safe form here - a render-thread read never sees a torn image.\n        $tmp = $path + '.part'\n        [System.IO.File]::WriteAllBytes($tmp, $bytes)\n        [System.IO.File]::Copy($tmp, $path, $true)\n        Remove-Item $tmp -Force\n        return $true\n    } catch {\n        return $false\n    }\n}\n\nfunction Invoke-SpotifyCommand($session, $hint, $line) {\n    if ($line -eq 'PLAY_PAUSE') {\n        if ($null -ne $session) { try { Await-Op ($session.TryTogglePlayPauseAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'NEXT') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipNextAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'PREV') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipPreviousAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_ON') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($true)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_OFF') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($false)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SOURCE=ANY') {\n        $script:anyMedia = $true\n    } elseif ($line -eq 'SOURCE=SPOTIFY') {\n        $script:anyMedia = $false\n    } elseif ($line -match '^REPEAT=(OFF|ALL|ONE)$') {\n        if ($null -ne $session) {\n            # WinRT enum values passed as their underlying int (None=0, Track=1,\n            # All=2): on some boxes the repeat-mode enum type cannot be projected\n            # into PowerShell at all (probed: [type] load fails and the binder then\n            # hides the method). There the call simply no-ops inside this catch;\n            # where the enum projects, the conversion binds and the command works.\n            $modeInt = switch ($Matches[1]) { 'OFF' { 0 } 'ALL' { 2 } 'ONE' { 1 } }\n            try { Await-Op ($session.TryChangeRepeatModeAsync($modeInt)) $boolType | Out-Null } catch { }\n        }\n    } elseif ($line -match '^VOLUME=(\\d+)$') {\n        [AutismSpotifyVolume]::SetSessionVolume($hint, [double]$Matches[1] / 100.0) | Out-Null\n    }\n}\n\nwhile ($true) {\n    $elapsed = ([DateTime]::UtcNow - $lastPoll).TotalMilliseconds\n    if ($global:spotifyDirty -or $elapsed -ge 450) {\n        $global:spotifyDirty = $false\n        $lastPoll = [DateTime]::UtcNow\n    try {\n        if ($null -eq $manager) {\n            $subscribedAumid = ''\n            $manager = Await-Op ($managerType::RequestAsync()) $managerType\n        }\n        $session = $null\n        if ($anyMedia) {\n            $session = $manager.GetCurrentSession()\n        } else {\n            foreach ($candidate in $manager.GetSessions()) {\n                if ($candidate.SourceAppUserModelId -match 'spotify') { $session = $candidate; break }\n            }\n        }\n        if ($null -ne $session -and ([string]$session.SourceAppUserModelId) -ne $subscribedAumid) {\n            # Re-subscribe keyed on the AUMID STRING, not RCW object identity:\n            # if the projection ever yields a fresh RCW for the same session, an\n            # object-reference comparison would re-register handlers every poll.\n            try {\n                $null = $session.add_MediaPropertiesChanged({ $global:spotifyDirty = $true })\n                $null = $session.add_PlaybackInfoChanged({ $global:spotifyDirty = $true })\n                $subscribedAumid = [string]$session.SourceAppUserModelId\n            } catch { }\n        }\n        if ($null -eq $session) {\n            [Console]::WriteLine('STOPPED')\n        } else {\n            $hint = if ($anyMedia) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n            $info = $session.GetPlaybackInfo()\n            $status = ([string]$info.PlaybackStatus).ToUpperInvariant()\n            $props = Await-Op ($session.TryGetMediaPropertiesAsync()) $propsType\n            $artist = ([string]$props.Artist) -replace \"[\\r\\n]\", ' '\n            $title = ([string]$props.Title) -replace \"[\\r\\n]\", ' '\n            $pos = '0'\n            $dur = '0'\n            try {\n                $tl = $session.GetTimelineProperties()\n                if ($null -ne $tl -and $tl.EndTime -gt $tl.StartTime) {\n                    $pos = [Math]::Max(0.0, $tl.Position.TotalSeconds).ToString('F1', $inv)\n                    $dur = ($tl.EndTime - $tl.StartTime).TotalSeconds.ToString('F1', $inv)\n                }\n            } catch { }\n            $shuffle = if ($info.ShuffleActive) { '1' } else { '0' }\n            $repeat = switch ([string]$info.RepeatMode) { 'None' { 'OFF' } 'Track' { 'ONE' } 'All' { 'ALL' } default { 'UNKNOWN' } }\n            $vol = [AutismSpotifyVolume]::GetSessionVolume($hint)\n            $art = ''\n            if ($title.Length -gt 0) {\n                $key = $artist + '|' + $title\n                if ($key -ne $artKey) {\n                    $artKey = $key\n                    if (Save-Art $props $artPath) {\n                        $art = $artPath\n                    } elseif (Test-Path $artPath) {\n                        # A failed save must not leave the PREVIOUS track's art\n                        # behind for the elseif below to serve with the NEW track.\n                        Remove-Item $artPath -Force\n                    }\n                } elseif (Test-Path $artPath) {\n                    $art = $artPath\n                }\n            }\n            [Console]::WriteLine($status + $us + $artist + $us + $title + $us + $pos + $us + $dur + $us + $shuffle + $us + $repeat + $us + $vol + $us + $art)\n        }\n    } catch {\n        $manager = $null\n        $subscribedAumid = ''\n        [Console]::WriteLine('UNAVAILABLE')\n    }\n    }\n    if (-not $stdinDead -and $readTask.IsCompleted) {\n        try {\n            $line = $readTask.Result\n            if ($null -eq $line) {\n                $stdinDead = $true\n            } else {\n                $readTask = $stdinReader.ReadLineAsync()\n                $hintNow = if ($anyMedia -and $null -ne $session) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n                Invoke-SpotifyCommand $session $hintNow $line\n            }\n        } catch {\n            $stdinDead = $true\n        }\n    }\n    Start-Sleep -Milliseconds 100\n}\n";
   private static final String close_Str = "if application \"Spotify\" is running then\n    tell application \"Spotify\"\n        set out to (player state as string) & (ASCII character 31)\n        try\n            set out to out & (artist of current track) & (ASCII character 31) & (name of current track)\n        on error\n            set out to out & (ASCII character 31) & (ASCII character 31)\n        end try\n        try\n            set out to out & (ASCII character 31) & (((player position) * 1000) as integer) & (ASCII character 31) & ((duration of current track) as integer)\n        on error\n            set out to out & (ASCII character 31) & \"0\" & (ASCII character 31) & \"0\"\n        end try\n        try\n            set out to out & (ASCII character 31) & (shuffling as string) & (ASCII character 31) & (repeating as string) & (ASCII character 31) & (sound volume as string)\n        on error\n            set out to out & (ASCII character 31) & \"0\" & (ASCII character 31) & \"\" & (ASCII character 31) & \"-1\"\n        end try\n        try\n            set artPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.png\"\n            set tiffPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.tiff\"\n            set keyPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.key\"\n            set trackKey to (artist of current track) & \"|\" & (name of current track)\n            set haveKey to false\n            try\n                set haveKey to (read (POSIX file keyPath) as string) is trackKey\n            end try\n            if haveKey then\n                set out to out & (ASCII character 31) & artPath\n            else\n                set d to artwork of current track\n                set f to open for access (POSIX file tiffPath) with write permission\n                write d to f\n                close access f\n                do shell script \"sips -s format png \" & quoted form of tiffPath & \" --out \" & quoted form of artPath & \" >/dev/null 2>&1\"\n                set kf to open for access (POSIX file keyPath) with write permission\n                write trackKey to kf\n                close access kf\n                set out to out & (ASCII character 31) & artPath\n            end if\n        on error\n            try\n                close access (POSIX file tiffPath)\n            end try\n            set out to out & (ASCII character 31) & \"\"\n        end try\n        return out\n    end tell\nend if\n";

   private cq() {
   }

   public static cq.Inner_ConfigStore init_cq_ConfigStore() {
      return init_cq_ConfigStore;
   }

   public static boolean init_Z() {
      return init_Z_J(Util.getMillis());
   }

   static boolean init_Z_J(long nowMs) {
      cq.Inner_ConfigStore snap = init_cq_ConfigStore;
      return !snap.load_Str().isBlank()
         && (snap.init_cq_TaskQueue() == cq.Inner_TaskQueue.init || snap.init_cq_TaskQueue() == cq.Inner_TaskQueue.load && nowMs - pop <= 3000L);
   }

   public static void init_V() {
      long now = System.currentTimeMillis();
      run = now;

      try {
         sync();
         synchronized (init_Obj) {
            if (init_Thread != null) {
               return;
            }

            if (read_Z && now - get < 60000L) {
               return;
            }

            read_Z = false;
            update_I = 0;
            poll();
         }
      } catch (Throwable var5) {
      }
   }

   public static void load_V() {
      init_V_Str("PLAY_PAUSE");
   }

   public static void read_V() {
      init_V_Str("NEXT");
   }

   public static void write_V() {
      init_V_Str("PREV");
   }

   public static void init_V_Z(boolean on) {
      init_V_Str(on ? "SHUFFLE_ON" : "SHUFFLE_OFF");
   }

   public static void update() {
      init_V_Str("REPEAT=" + init_cq_CachePool_cq_CachePool(init_cq_ConfigStore().init_cq_CachePool()).name());
   }

   public static void init_V_I(int percent) {
      init_V_Str("VOLUME=" + init_I_I(percent));
   }

   public static void load(boolean anyMedia) {
      load_Z = anyMedia;
      init_V_Str(anyMedia ? "SOURCE=ANY" : "SOURCE=SPOTIFY");
   }

   public static boolean load_Z() {
      return load_Z;
   }

   static cq.Inner_CachePool init_cq_CachePool_cq_CachePool(cq.Inner_CachePool repeat) {
      if (repeat == null) {
         return cq.Inner_CachePool.init;
      }

      cq.Inner_CachePool var10000;
      switch (repeat) {
         case init:
            var10000 = cq.Inner_CachePool.read;
            break;
         case read:
            var10000 = cq.Inner_CachePool.load;
            break;
         default:
            var10000 = cq.Inner_CachePool.init;
      }

      return var10000;
   }

   static int init_I_I(int percent) {
      return Math.max(0, Math.min(100, percent));
   }

   private static void sync() {
      if (!init_Z) {
         synchronized (init_Obj) {
            if (!init_Z) {
               init_Z = true;
               Thread watchdog = new Thread(cq::flush, "autism-spotify-watchdog");
               watchdog.setDaemon(true);
               watchdog.start();
               Runtime.getRuntime().addShutdownHook(new Thread(cq::close, "autism-spotify-shutdown"));
               Thread artWorker = new Thread(cq::push, "autism-spotify-art");
               artWorker.setDaemon(true);
               artWorker.start();
            }
         }
      }
   }

   private static void flush() {
      while (true) {
         try {
            Thread.sleep(2000L);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
         }

         synchronized (init_Obj) {
            if (init_Thread != null && System.currentTimeMillis() - run > 15000L) {
               open();
            }
         }
      }
   }

   private static void close() {
      synchronized (init_Obj) {
         open();
      }
   }

   private static void open() {
      write_I++;
      Process child = init_Process;
      init_Process = null;
      init_Thread = null;
      if (child != null) {
         child.destroy();
      }

      init_V_J(System.currentTimeMillis());
   }

   // Backend probing starts processes, so it runs on the backend thread, not on the HUD tick.
   private static void poll() {
      int gen = ++write_I;
      Thread thread = new Thread(() -> {
         cq.Inner_StreamUtil backend = init_cq_StreamUtil();
         if (backend != null) {
            init_V_I_cq_StreamUtil(gen, backend);
            return;
         }

         synchronized (init_Obj) {
            if (init_Thread == Thread.currentThread()) {
               init_Thread = null;
               read_Z = true;
               get = System.currentTimeMillis();
               if (init_cq_ConfigStore.init_cq_TaskQueue() != cq.Inner_TaskQueue.write) {
                  init_cq_ConfigStore = new cq.Inner_ConfigStore(
                     cq.Inner_TaskQueue.write, "", "", System.currentTimeMillis(), 0.0, 0.0, false, cq.Inner_CachePool.write, -1, ""
                  );
               }
            }
         }
      }, "autism-spotify-backend");
      thread.setDaemon(true);
      init_Thread = thread;
      thread.start();
   }

   private static cq.Inner_StreamUtil init_cq_StreamUtil() {
      OS os = Util.getPlatform();
      if (os == OS.WINDOWS) {
         return cq.Inner_StreamUtil.init;
      }

      if (os == OS.OSX) {
         return cq.Inner_StreamUtil.write;
      }

      if (os == OS.LINUX) {
         if (init_Z_Str("playerctl")) {
            return cq.Inner_StreamUtil.load;
         }

         if (init_Z_Str("gdbus")) {
            return cq.Inner_StreamUtil.read;
         }
      }

      return null;
   }

   private static void init_V_I_cq_StreamUtil(int gen, cq.Inner_StreamUtil backend) {
      try {
         switch (backend) {
            case read:
               load(gen);
               break;
            case write:
               read(gen);
               break;
            default:
               load(gen, backend);
         }
      } catch (Throwable t) {
         synchronized (init_Obj) {
            if (init_Thread == Thread.currentThread()) {
               init_Thread = null;
               read_Z = true;
               get = System.currentTimeMillis();
               init_cq_ConfigStore = new cq.Inner_ConfigStore(
                  cq.Inner_TaskQueue.write, "", "", System.currentTimeMillis(), 0.0, 0.0, false, cq.Inner_CachePool.write, -1, ""
               );
            }
         }
      }
   }

   private static void load(int gen, cq.Inner_StreamUtil backend) {
      boolean respawning = false;

      while (true) {
         if (respawning) {
            try {
               Thread.sleep(5000L);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         }

         synchronized (init_Obj) {
            if (gen != write_I) {
               return;
            }
         }

         Process child = init_Process_cq_StreamUtil(backend);
         if (child == null) {
            if (!init_Z_I_J(gen, 0L)) {
               return;
            }

            respawning = true;
         } else {
            long startedAt = System.currentTimeMillis();
            synchronized (init_Obj) {
               if (gen != write_I) {
                  child.destroy();
                  return;
               }

               init_Process = child;
            }

            init(gen, child, backend);
            long aliveMs = System.currentTimeMillis() - startedAt;
            child.destroy();
            synchronized (init_Obj) {
               if (init_Process == child) {
                  init_Process = null;
               }
            }

            if (!init_Z_I_J(gen, aliveMs)) {
               return;
            }

            respawning = true;
         }
      }
   }

   private static boolean init_Z_I_J(int gen, long aliveMs) {
      synchronized (init_Obj) {
         if (gen != write_I) {
            return false;
         } else {
            update_I = aliveMs >= 30000L ? 0 : update_I + 1;
            if (update_I >= 3) {
               read_Z = true;
               get = System.currentTimeMillis();
               init_Thread = null;
               init_cq_ConfigStore = new cq.Inner_ConfigStore(cq.Inner_TaskQueue.write, "", "", get, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
               return false;
            } else {
               return true;
            }
         }
      }
   }

   private static Process init_Process_cq_StreamUtil(cq.Inner_StreamUtil backend) {
      try {
         ProcessBuilder var10000;
         switch (backend) {
            case init:
               var10000 = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", init_Path().toString());
               break;
            case load:
               var10000 = init_ProcessBuilder();
               break;
            case read:
            case write:
               throw new IllegalStateException("polled backends have no streaming process");
            default:
               throw new MatchException(null, null);
         }

         ProcessBuilder builder = var10000;
         if (backend == cq.Inner_StreamUtil.init) {
            builder.environment().put("AUTISM_SPOTIFY_SOURCE", load_Z ? "ANY" : "SPOTIFY");
         }

         return builder.redirectError(Redirect.DISCARD).start();
      } catch (Exception e) {
         return null;
      }
   }

   private static ProcessBuilder init_ProcessBuilder() {
      return new ProcessBuilder(init_List());
   }

   static List<String> init_List() {
      List<String> argv = new ArrayList<>();
      argv.add("playerctl");
      if (!load_Z) {
         argv.add("--player=spotify");
      }

      argv.addAll(
         List.of(
            "--follow",
            "metadata",
            "--format",
            "{{status}}\u001f{{artist}}\u001f{{title}}\u001f{{position}}\u001f{{mpris:length}}\u001f{{shuffle}}\u001f{{loop}}\u001f{{volume}}\u001f{{mpris:artUrl}}"
         )
      );
      return argv;
   }

   static String init_Str() {
      return "$ErrorActionPreference = 'Continue'\n[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n\n$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\nfunction Await-Op($op, $resultType) {\n    $task = $asTaskGeneric.MakeGenericMethod($resultType).Invoke($null, @($op))\n    if (-not $task.Wait(5000)) { throw 'timed out waiting for a WinRT operation' }\n    return $task.Result\n}\n\n$code = @'\nusing System;\nusing System.Collections.Generic;\nusing System.Diagnostics;\nusing System.Runtime.InteropServices;\n\n// ISimpleAudioVolume get/set for the session belonging to a process-name hint.\n// Raw vtable throughout: on machines with an audio enhancement driver (probed on\n// this one), the session ENUMERATOR object answers QueryInterface with\n// E_NOINTERFACE for interfaces its vtable actually implements, so the typed RCW\n// path cannot reach it; the vtable contracts are stable, so calling slots\n// directly works on wrapped and healthy machines alike.\npublic static class AutismSpotifyVolume {\n    [ComImport, Guid(\"A95664D2-9614-4F35-A746-DE8DB63617E6\"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]\n    public interface IMMDeviceEnumerator {\n        [PreserveSig] int EnumAudioEndpoints(int dataFlow, int stateMask, out IntPtr devices);\n        [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IntPtr device);\n        [PreserveSig] int GetDevice([MarshalAs(UnmanagedType.LPWStr)] string id, out IntPtr device);\n        [PreserveSig] int RegisterEndpointNotificationCallback(IntPtr client);\n        [PreserveSig] int UnregisterEndpointNotificationCallback(IntPtr client);\n    }\n\n    [ComImport, Guid(\"BCDE0395-E52F-467C-8E3D-C4579291692E\")]\n    public class MMDeviceEnumeratorComObject { }\n\n    static readonly Guid IID_MGR2 = new Guid(\"77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F\");\n    static readonly Guid IID_VOLUME = new Guid(\"87CE5498-68D6-44E5-9215-6DA47EF883D8\");\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComOutOut(IntPtr t, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetCount(IntPtr t, out int v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComItem(IntPtr t, int i, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComActivate(IntPtr t, [MarshalAs(UnmanagedType.LPStruct)] Guid iid, int c, IntPtr p, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetPid(IntPtr t, out uint pid);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetVolume(IntPtr t, out float level);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComSetVolume(IntPtr t, float level, IntPtr ctx);\n\n    static IntPtr Slot(IntPtr obj, int slot) {\n        return Marshal.ReadIntPtr(Marshal.ReadIntPtr(obj), slot * IntPtr.Size);\n    }\n\n    // Album art, take four. Every earlier read path failed on this box, all\n    // probed: PowerShell's binder cannot bind the WinRT AsStream extension, a\n    // winmd reference for compile-time WinRT types fails to load (0x80131047),\n    // the returned stream's RCW exposes no interfaces, and vtable reads against a\n    // NATIVE buffer (IBufferFactory) either lie (hollow S_OK) or segfault the\n    // helper (exit 139). So the read buffer is a .NET-BACKED one:\n    // System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer wraps a\n    // managed byte[] as an IBuffer with a CLR-generated, guaranteed-valid vtable.\n    // The only native vtable call left is IInputStream.ReadAsync (slot 6), proven\n    // safe here. Bytes come back through WindowsRuntimeBufferExtensions.ToArray,\n    // purely managed. Crash containment: the MTA worker (apartment sensitivity,\n    // probed) plus HandleProcessCorruptedStateExceptions, and an all-zero guard -\n    // on this stack the read returns S_OK with a NULL op and a zero-filled buffer\n    // (hollow success), which must read as failure, never as a 4 MB black image.\n    [System.Runtime.ExceptionServices.HandleProcessCorruptedStateExceptions]\n    static byte[] ReadArtStreamMta(object streamObj) {\n        IntPtr streamPtr = IntPtr.Zero, istream = IntPtr.Zero, bufPtr = IntPtr.Zero, op = IntPtr.Zero;\n        try {\n            System.Reflection.Assembly wrtAsm = null;\n            foreach (System.Reflection.Assembly asm in AppDomain.CurrentDomain.GetAssemblies()) {\n                if (asm.GetName().Name == \"System.Runtime.WindowsRuntime\") { wrtAsm = asm; break; }\n            }\n            if (wrtAsm == null) return null;\n            Type wrb = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer\");\n            Type ext = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBufferExtensions\");\n            if (wrb == null || ext == null) return null;\n            System.Reflection.MethodInfo create = null, toArray = null;\n            foreach (System.Reflection.MethodInfo m in wrb.GetMethods()) {\n                if (m.Name == \"Create\" && m.GetParameters().Length == 4) { create = m; break; }\n            }\n            foreach (System.Reflection.MethodInfo m in ext.GetMethods()) {\n                if (m.Name == \"ToArray\" && m.GetParameters().Length == 1) { toArray = m; break; }\n            }\n            if (create == null || toArray == null) return null;\n            byte[] backing = new byte[4 * 1024 * 1024];\n            object buffer = create.Invoke(null, new object[] { backing, 0, backing.Length, backing.Length });\n            Guid iidStream = new Guid(\"905a0fe1-bc53-11df-8c49-001e4fc686da\");\n            streamPtr = Marshal.GetIUnknownForObject(streamObj);\n            if (Marshal.QueryInterface(streamPtr, ref iidStream, out istream) < 0 || istream == IntPtr.Zero) return null;\n            bufPtr = Marshal.GetIUnknownForObject(buffer);\n            int hrRead = Marshal.GetDelegateForFunctionPointer<ComReadAsync>(Slot(istream, 6))(istream, bufPtr, (uint)backing.Length, 0u, out op);\n            if (hrRead < 0) return null;\n            if (op != IntPtr.Zero) { // async op exists: wait it out; a null op means the fill was synchronous\n                int status = 0;\n                long deadline = DateTime.UtcNow.Ticks + 5L * 10000000L;\n                while (status == 0 && DateTime.UtcNow.Ticks < deadline) {\n                    System.Threading.Thread.Sleep(10);\n                    if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(op, 7))(op, out status) < 0) return null;\n                }\n                if (status != 1) return null;\n            }\n            byte[] result = (byte[])toArray.Invoke(null, new object[] { buffer });\n            // The hollow-success guard: a hollow read leaves the wrapper at its\n            // initial all-zero content; real album art always has non-zero bytes.\n            bool anyNonZero = false;\n            for (int i = 0; i < result.Length; i++) {\n                if (result[i] != 0) { anyNonZero = true; break; }\n            }\n            return anyNonZero ? result : null;\n        } catch (System.Exception) {\n            return null;\n        } finally {\n            if (op != IntPtr.Zero) Marshal.Release(op);\n            if (bufPtr != IntPtr.Zero) Marshal.Release(bufPtr);\n            if (istream != IntPtr.Zero) Marshal.Release(istream);\n            if (streamPtr != IntPtr.Zero) Marshal.Release(streamPtr);\n        }\n    }\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComReadAsync(IntPtr t, IntPtr buffer, uint count, uint options, out IntPtr op);\n\n    static HashSet<int> hintPids = new HashSet<int>();\n    static string hintCached = \"\";\n    static DateTime hintRefreshed = DateTime.MinValue;\n\n    // Process-name matching by either-way containment: \"spotify\" hits Spotify.exe,\n    // and a packaged AUMID head like \"SpotifyAB.SpotifyMusic_xxx\" still contains it.\n    static HashSet<int> PidsForHint(string appHint) {\n        if (appHint != hintCached || (DateTime.UtcNow - hintRefreshed).TotalSeconds > 2.0) {\n            HashSet<int> set = new HashSet<int>();\n            string hint = (appHint ?? \"\").ToLowerInvariant();\n            if (hint.Length > 0) {\n                foreach (Process p in Process.GetProcesses()) {\n                    try {\n                        string n = p.ProcessName.ToLowerInvariant();\n                        if (hint.Contains(n) || n.Contains(hint)) set.Add(p.Id);\n                    } catch { }\n                }\n            }\n            hintPids = set;\n            hintCached = appHint ?? \"\";\n            hintRefreshed = DateTime.UtcNow;\n        }\n        return hintPids;\n    }\n\n    public static int GetSessionVolume(string appHint) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return -1;\n        try {\n            float v;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetVolume>(Slot(volume, 4))(volume, out v) < 0) return -1;\n            return (int)Math.Round(v * 100.0f);\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    public static bool SetSessionVolume(string appHint, double fraction) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return false;\n        try {\n            return Marshal.GetDelegateForFunctionPointer<ComSetVolume>(Slot(volume, 3))(volume, (float)fraction, IntPtr.Zero) >= 0;\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    static IntPtr FindVolume(string appHint) {\n        HashSet<int> pids = PidsForHint(appHint);\n        if (pids.Count == 0) return IntPtr.Zero;\n        IMMDeviceEnumerator enumerator = (IMMDeviceEnumerator)new MMDeviceEnumeratorComObject();\n        IntPtr collection;\n        if (enumerator.EnumAudioEndpoints(0, 1, out collection) < 0) return IntPtr.Zero;\n        try {\n            int deviceCount;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(collection, 3))(collection, out deviceCount) < 0) return IntPtr.Zero;\n            for (int d = 0; d < deviceCount; d++) {\n                IntPtr device;\n                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(collection, 4))(collection, d, out device) < 0) continue;\n                if (device == IntPtr.Zero) continue;\n                try {\n                    IntPtr manager;\n                    if (Marshal.GetDelegateForFunctionPointer<ComActivate>(Slot(device, 3))(device, IID_MGR2, 23, IntPtr.Zero, out manager) < 0) continue;\n                    if (manager == IntPtr.Zero) continue;\n                    try {\n                        IntPtr enumPtr;\n                        if (Marshal.GetDelegateForFunctionPointer<ComOutOut>(Slot(manager, 5))(manager, out enumPtr) < 0) continue;\n                        if (enumPtr == IntPtr.Zero) continue;\n                        try {\n                            int count;\n                            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(enumPtr, 3))(enumPtr, out count) < 0) continue;\n                            for (int i = 0; i < count; i++) {\n                                IntPtr sessionPtr;\n                                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(enumPtr, 4))(enumPtr, i, out sessionPtr) < 0) continue;\n                                if (sessionPtr == IntPtr.Zero) continue;\n                                try {\n                                    uint sessionPid;\n                                    if (Marshal.GetDelegateForFunctionPointer<ComGetPid>(Slot(sessionPtr, 14))(sessionPtr, out sessionPid) < 0) continue;\n                                    if (!pids.Contains((int)sessionPid)) continue;\n                                    Guid iid = IID_VOLUME;\n                                    IntPtr volumePtr;\n                                    if (Marshal.QueryInterface(sessionPtr, ref iid, out volumePtr) < 0) continue;\n                                    return volumePtr; // caller releases\n                                } finally {\n                                    Marshal.Release(sessionPtr);\n                                }\n                            }\n                        } finally {\n                            Marshal.Release(enumPtr);\n                        }\n                    } finally {\n                        Marshal.Release(manager);\n                    }\n                } finally {\n                    Marshal.Release(device);\n                }\n            }\n            return IntPtr.Zero;\n        } finally {\n            Marshal.Release(collection);\n        }\n    }\n}\n'@\nAdd-Type -TypeDefinition $code\n\n$managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType=WindowsRuntime]\n$propsType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType=WindowsRuntime]\n$boolType = [bool]\n$us = [char]31\n$inv = [Globalization.CultureInfo]::InvariantCulture\n$manager = $null\n$artKey = ''\n$artPath = Join-Path $env:TEMP 'autism_spotify_art.png'\n$anyMedia = $env:AUTISM_SPOTIFY_SOURCE -eq 'ANY'\n# Stdin commands are read through a raw StreamReader over the standard input,\n# NOT [Console]::In.ReadLineAsync: on .NET Framework that call takes the console\n# sync root while it blocks on an EMPTY anonymous pipe (exactly what Java's\n# ProcessBuilder hands us - probed: the helper froze silently after 'before\n# readline' with zero output). StreamReader.ReadLineAsync runs on a pool thread\n# with no console lock, so the status loop never stalls on an empty pipe.\n$stdinReader = New-Object System.IO.StreamReader([Console]::OpenStandardInput())\n$readTask = $stdinReader.ReadLineAsync()\n$stdinDead = $false\n# Event-driven refresh: MediaPropertiesChanged (track skip) and\n# PlaybackInfoChanged (play/pause) set a GLOBAL dirty flag (global, not script\n# scope, because the handler executes off the main runspace thread). The loop\n# below re-reads immediately on dirty, otherwise every 450 ms. NOTE: on this box\n# GSMTC property-change events register fine but NEVER fire (probed: two real\n# skips, zero firings - the same stack that hollows art streams and returns null\n# RepeatMode), so the event path is inert here and the 450 ms poll is what\n# actually delivers faster updates: worst-case staleness is halved.\n$global:spotifyDirty = $false\n$subscribedAumid = ''\n$lastPoll = [DateTime]::UtcNow.AddSeconds(-1)\n\nfunction Save-Art($props, $path) {\n    try {\n        if ($null -eq $props.Thumbnail) { return $false }\n        $stream = Await-Op ($props.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\n        if ($null -eq $stream) { return $false }\n        $bytes = [AutismSpotifyVolume]::ReadArtStream($stream)\n        if ($null -eq $bytes) { return $false }\n        # Atomic-ish publish: write a temp sibling, then Copy over the stable path\n        # and delete the temp. [IO.File]::Move(source, dest, overwrite) does NOT\n        # exist on .NET Framework 4.x (only .NET Core 3.0+), so Copy+Delete is the\n        # safe form here - a render-thread read never sees a torn image.\n        $tmp = $path + '.part'\n        [System.IO.File]::WriteAllBytes($tmp, $bytes)\n        [System.IO.File]::Copy($tmp, $path, $true)\n        Remove-Item $tmp -Force\n        return $true\n    } catch {\n        return $false\n    }\n}\n\nfunction Invoke-SpotifyCommand($session, $hint, $line) {\n    if ($line -eq 'PLAY_PAUSE') {\n        if ($null -ne $session) { try { Await-Op ($session.TryTogglePlayPauseAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'NEXT') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipNextAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'PREV') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipPreviousAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_ON') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($true)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_OFF') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($false)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SOURCE=ANY') {\n        $script:anyMedia = $true\n    } elseif ($line -eq 'SOURCE=SPOTIFY') {\n        $script:anyMedia = $false\n    } elseif ($line -match '^REPEAT=(OFF|ALL|ONE)$') {\n        if ($null -ne $session) {\n            # WinRT enum values passed as their underlying int (None=0, Track=1,\n            # All=2): on some boxes the repeat-mode enum type cannot be projected\n            # into PowerShell at all (probed: [type] load fails and the binder then\n            # hides the method). There the call simply no-ops inside this catch;\n            # where the enum projects, the conversion binds and the command works.\n            $modeInt = switch ($Matches[1]) { 'OFF' { 0 } 'ALL' { 2 } 'ONE' { 1 } }\n            try { Await-Op ($session.TryChangeRepeatModeAsync($modeInt)) $boolType | Out-Null } catch { }\n        }\n    } elseif ($line -match '^VOLUME=(\\d+)$') {\n        [AutismSpotifyVolume]::SetSessionVolume($hint, [double]$Matches[1] / 100.0) | Out-Null\n    }\n}\n\nwhile ($true) {\n    $elapsed = ([DateTime]::UtcNow - $lastPoll).TotalMilliseconds\n    if ($global:spotifyDirty -or $elapsed -ge 450) {\n        $global:spotifyDirty = $false\n        $lastPoll = [DateTime]::UtcNow\n    try {\n        if ($null -eq $manager) {\n            $subscribedAumid = ''\n            $manager = Await-Op ($managerType::RequestAsync()) $managerType\n        }\n        $session = $null\n        if ($anyMedia) {\n            $session = $manager.GetCurrentSession()\n        } else {\n            foreach ($candidate in $manager.GetSessions()) {\n                if ($candidate.SourceAppUserModelId -match 'spotify') { $session = $candidate; break }\n            }\n        }\n        if ($null -ne $session -and ([string]$session.SourceAppUserModelId) -ne $subscribedAumid) {\n            # Re-subscribe keyed on the AUMID STRING, not RCW object identity:\n            # if the projection ever yields a fresh RCW for the same session, an\n            # object-reference comparison would re-register handlers every poll.\n            try {\n                $null = $session.add_MediaPropertiesChanged({ $global:spotifyDirty = $true })\n                $null = $session.add_PlaybackInfoChanged({ $global:spotifyDirty = $true })\n                $subscribedAumid = [string]$session.SourceAppUserModelId\n            } catch { }\n        }\n        if ($null -eq $session) {\n            [Console]::WriteLine('STOPPED')\n        } else {\n            $hint = if ($anyMedia) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n            $info = $session.GetPlaybackInfo()\n            $status = ([string]$info.PlaybackStatus).ToUpperInvariant()\n            $props = Await-Op ($session.TryGetMediaPropertiesAsync()) $propsType\n            $artist = ([string]$props.Artist) -replace \"[\\r\\n]\", ' '\n            $title = ([string]$props.Title) -replace \"[\\r\\n]\", ' '\n            $pos = '0'\n            $dur = '0'\n            try {\n                $tl = $session.GetTimelineProperties()\n                if ($null -ne $tl -and $tl.EndTime -gt $tl.StartTime) {\n                    $pos = [Math]::Max(0.0, $tl.Position.TotalSeconds).ToString('F1', $inv)\n                    $dur = ($tl.EndTime - $tl.StartTime).TotalSeconds.ToString('F1', $inv)\n                }\n            } catch { }\n            $shuffle = if ($info.ShuffleActive) { '1' } else { '0' }\n            $repeat = switch ([string]$info.RepeatMode) { 'None' { 'OFF' } 'Track' { 'ONE' } 'All' { 'ALL' } default { 'UNKNOWN' } }\n            $vol = [AutismSpotifyVolume]::GetSessionVolume($hint)\n            $art = ''\n            if ($title.Length -gt 0) {\n                $key = $artist + '|' + $title\n                if ($key -ne $artKey) {\n                    $artKey = $key\n                    if (Save-Art $props $artPath) {\n                        $art = $artPath\n                    } elseif (Test-Path $artPath) {\n                        # A failed save must not leave the PREVIOUS track's art\n                        # behind for the elseif below to serve with the NEW track.\n                        Remove-Item $artPath -Force\n                    }\n                } elseif (Test-Path $artPath) {\n                    $art = $artPath\n                }\n            }\n            [Console]::WriteLine($status + $us + $artist + $us + $title + $us + $pos + $us + $dur + $us + $shuffle + $us + $repeat + $us + $vol + $us + $art)\n        }\n    } catch {\n        $manager = $null\n        $subscribedAumid = ''\n        [Console]::WriteLine('UNAVAILABLE')\n    }\n    }\n    if (-not $stdinDead -and $readTask.IsCompleted) {\n        try {\n            $line = $readTask.Result\n            if ($null -eq $line) {\n                $stdinDead = $true\n            } else {\n                $readTask = $stdinReader.ReadLineAsync()\n                $hintNow = if ($anyMedia -and $null -ne $session) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n                Invoke-SpotifyCommand $session $hintNow $line\n            }\n        } catch {\n            $stdinDead = $true\n        }\n    }\n    Start-Sleep -Milliseconds 100\n}\n";
   }

   private static Path init_Path() throws IOException {
      Path existing = init_Path;
      if (existing != null) {
         return existing;
      }

      synchronized (init_Obj) {
         if (init_Path == null) {
            Path file = Files.createTempFile("autism_spotify", ".ps1");
            Files.writeString(
               file,
               "$ErrorActionPreference = 'Continue'\n[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n\n$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\nfunction Await-Op($op, $resultType) {\n    $task = $asTaskGeneric.MakeGenericMethod($resultType).Invoke($null, @($op))\n    if (-not $task.Wait(5000)) { throw 'timed out waiting for a WinRT operation' }\n    return $task.Result\n}\n\n$code = @'\nusing System;\nusing System.Collections.Generic;\nusing System.Diagnostics;\nusing System.Runtime.InteropServices;\n\n// ISimpleAudioVolume get/set for the session belonging to a process-name hint.\n// Raw vtable throughout: on machines with an audio enhancement driver (probed on\n// this one), the session ENUMERATOR object answers QueryInterface with\n// E_NOINTERFACE for interfaces its vtable actually implements, so the typed RCW\n// path cannot reach it; the vtable contracts are stable, so calling slots\n// directly works on wrapped and healthy machines alike.\npublic static class AutismSpotifyVolume {\n    [ComImport, Guid(\"A95664D2-9614-4F35-A746-DE8DB63617E6\"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]\n    public interface IMMDeviceEnumerator {\n        [PreserveSig] int EnumAudioEndpoints(int dataFlow, int stateMask, out IntPtr devices);\n        [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IntPtr device);\n        [PreserveSig] int GetDevice([MarshalAs(UnmanagedType.LPWStr)] string id, out IntPtr device);\n        [PreserveSig] int RegisterEndpointNotificationCallback(IntPtr client);\n        [PreserveSig] int UnregisterEndpointNotificationCallback(IntPtr client);\n    }\n\n    [ComImport, Guid(\"BCDE0395-E52F-467C-8E3D-C4579291692E\")]\n    public class MMDeviceEnumeratorComObject { }\n\n    static readonly Guid IID_MGR2 = new Guid(\"77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F\");\n    static readonly Guid IID_VOLUME = new Guid(\"87CE5498-68D6-44E5-9215-6DA47EF883D8\");\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComOutOut(IntPtr t, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetCount(IntPtr t, out int v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComItem(IntPtr t, int i, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComActivate(IntPtr t, [MarshalAs(UnmanagedType.LPStruct)] Guid iid, int c, IntPtr p, out IntPtr v);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetPid(IntPtr t, out uint pid);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComGetVolume(IntPtr t, out float level);\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComSetVolume(IntPtr t, float level, IntPtr ctx);\n\n    static IntPtr Slot(IntPtr obj, int slot) {\n        return Marshal.ReadIntPtr(Marshal.ReadIntPtr(obj), slot * IntPtr.Size);\n    }\n\n    // Album art, take four. Every earlier read path failed on this box, all\n    // probed: PowerShell's binder cannot bind the WinRT AsStream extension, a\n    // winmd reference for compile-time WinRT types fails to load (0x80131047),\n    // the returned stream's RCW exposes no interfaces, and vtable reads against a\n    // NATIVE buffer (IBufferFactory) either lie (hollow S_OK) or segfault the\n    // helper (exit 139). So the read buffer is a .NET-BACKED one:\n    // System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer wraps a\n    // managed byte[] as an IBuffer with a CLR-generated, guaranteed-valid vtable.\n    // The only native vtable call left is IInputStream.ReadAsync (slot 6), proven\n    // safe here. Bytes come back through WindowsRuntimeBufferExtensions.ToArray,\n    // purely managed. Crash containment: the MTA worker (apartment sensitivity,\n    // probed) plus HandleProcessCorruptedStateExceptions, and an all-zero guard -\n    // on this stack the read returns S_OK with a NULL op and a zero-filled buffer\n    // (hollow success), which must read as failure, never as a 4 MB black image.\n    [System.Runtime.ExceptionServices.HandleProcessCorruptedStateExceptions]\n    static byte[] ReadArtStreamMta(object streamObj) {\n        IntPtr streamPtr = IntPtr.Zero, istream = IntPtr.Zero, bufPtr = IntPtr.Zero, op = IntPtr.Zero;\n        try {\n            System.Reflection.Assembly wrtAsm = null;\n            foreach (System.Reflection.Assembly asm in AppDomain.CurrentDomain.GetAssemblies()) {\n                if (asm.GetName().Name == \"System.Runtime.WindowsRuntime\") { wrtAsm = asm; break; }\n            }\n            if (wrtAsm == null) return null;\n            Type wrb = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBuffer\");\n            Type ext = wrtAsm.GetType(\"System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBufferExtensions\");\n            if (wrb == null || ext == null) return null;\n            System.Reflection.MethodInfo create = null, toArray = null;\n            foreach (System.Reflection.MethodInfo m in wrb.GetMethods()) {\n                if (m.Name == \"Create\" && m.GetParameters().Length == 4) { create = m; break; }\n            }\n            foreach (System.Reflection.MethodInfo m in ext.GetMethods()) {\n                if (m.Name == \"ToArray\" && m.GetParameters().Length == 1) { toArray = m; break; }\n            }\n            if (create == null || toArray == null) return null;\n            byte[] backing = new byte[4 * 1024 * 1024];\n            object buffer = create.Invoke(null, new object[] { backing, 0, backing.Length, backing.Length });\n            Guid iidStream = new Guid(\"905a0fe1-bc53-11df-8c49-001e4fc686da\");\n            streamPtr = Marshal.GetIUnknownForObject(streamObj);\n            if (Marshal.QueryInterface(streamPtr, ref iidStream, out istream) < 0 || istream == IntPtr.Zero) return null;\n            bufPtr = Marshal.GetIUnknownForObject(buffer);\n            int hrRead = Marshal.GetDelegateForFunctionPointer<ComReadAsync>(Slot(istream, 6))(istream, bufPtr, (uint)backing.Length, 0u, out op);\n            if (hrRead < 0) return null;\n            if (op != IntPtr.Zero) { // async op exists: wait it out; a null op means the fill was synchronous\n                int status = 0;\n                long deadline = DateTime.UtcNow.Ticks + 5L * 10000000L;\n                while (status == 0 && DateTime.UtcNow.Ticks < deadline) {\n                    System.Threading.Thread.Sleep(10);\n                    if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(op, 7))(op, out status) < 0) return null;\n                }\n                if (status != 1) return null;\n            }\n            byte[] result = (byte[])toArray.Invoke(null, new object[] { buffer });\n            // The hollow-success guard: a hollow read leaves the wrapper at its\n            // initial all-zero content; real album art always has non-zero bytes.\n            bool anyNonZero = false;\n            for (int i = 0; i < result.Length; i++) {\n                if (result[i] != 0) { anyNonZero = true; break; }\n            }\n            return anyNonZero ? result : null;\n        } catch (System.Exception) {\n            return null;\n        } finally {\n            if (op != IntPtr.Zero) Marshal.Release(op);\n            if (bufPtr != IntPtr.Zero) Marshal.Release(bufPtr);\n            if (istream != IntPtr.Zero) Marshal.Release(istream);\n            if (streamPtr != IntPtr.Zero) Marshal.Release(streamPtr);\n        }\n    }\n\n    [UnmanagedFunctionPointer(CallingConvention.StdCall)]\n    delegate int ComReadAsync(IntPtr t, IntPtr buffer, uint count, uint options, out IntPtr op);\n\n    static HashSet<int> hintPids = new HashSet<int>();\n    static string hintCached = \"\";\n    static DateTime hintRefreshed = DateTime.MinValue;\n\n    // Process-name matching by either-way containment: \"spotify\" hits Spotify.exe,\n    // and a packaged AUMID head like \"SpotifyAB.SpotifyMusic_xxx\" still contains it.\n    static HashSet<int> PidsForHint(string appHint) {\n        if (appHint != hintCached || (DateTime.UtcNow - hintRefreshed).TotalSeconds > 2.0) {\n            HashSet<int> set = new HashSet<int>();\n            string hint = (appHint ?? \"\").ToLowerInvariant();\n            if (hint.Length > 0) {\n                foreach (Process p in Process.GetProcesses()) {\n                    try {\n                        string n = p.ProcessName.ToLowerInvariant();\n                        if (hint.Contains(n) || n.Contains(hint)) set.Add(p.Id);\n                    } catch { }\n                }\n            }\n            hintPids = set;\n            hintCached = appHint ?? \"\";\n            hintRefreshed = DateTime.UtcNow;\n        }\n        return hintPids;\n    }\n\n    public static int GetSessionVolume(string appHint) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return -1;\n        try {\n            float v;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetVolume>(Slot(volume, 4))(volume, out v) < 0) return -1;\n            return (int)Math.Round(v * 100.0f);\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    public static bool SetSessionVolume(string appHint, double fraction) {\n        IntPtr volume = FindVolume(appHint);\n        if (volume == IntPtr.Zero) return false;\n        try {\n            return Marshal.GetDelegateForFunctionPointer<ComSetVolume>(Slot(volume, 3))(volume, (float)fraction, IntPtr.Zero) >= 0;\n        } finally {\n            Marshal.Release(volume);\n        }\n    }\n\n    static IntPtr FindVolume(string appHint) {\n        HashSet<int> pids = PidsForHint(appHint);\n        if (pids.Count == 0) return IntPtr.Zero;\n        IMMDeviceEnumerator enumerator = (IMMDeviceEnumerator)new MMDeviceEnumeratorComObject();\n        IntPtr collection;\n        if (enumerator.EnumAudioEndpoints(0, 1, out collection) < 0) return IntPtr.Zero;\n        try {\n            int deviceCount;\n            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(collection, 3))(collection, out deviceCount) < 0) return IntPtr.Zero;\n            for (int d = 0; d < deviceCount; d++) {\n                IntPtr device;\n                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(collection, 4))(collection, d, out device) < 0) continue;\n                if (device == IntPtr.Zero) continue;\n                try {\n                    IntPtr manager;\n                    if (Marshal.GetDelegateForFunctionPointer<ComActivate>(Slot(device, 3))(device, IID_MGR2, 23, IntPtr.Zero, out manager) < 0) continue;\n                    if (manager == IntPtr.Zero) continue;\n                    try {\n                        IntPtr enumPtr;\n                        if (Marshal.GetDelegateForFunctionPointer<ComOutOut>(Slot(manager, 5))(manager, out enumPtr) < 0) continue;\n                        if (enumPtr == IntPtr.Zero) continue;\n                        try {\n                            int count;\n                            if (Marshal.GetDelegateForFunctionPointer<ComGetCount>(Slot(enumPtr, 3))(enumPtr, out count) < 0) continue;\n                            for (int i = 0; i < count; i++) {\n                                IntPtr sessionPtr;\n                                if (Marshal.GetDelegateForFunctionPointer<ComItem>(Slot(enumPtr, 4))(enumPtr, i, out sessionPtr) < 0) continue;\n                                if (sessionPtr == IntPtr.Zero) continue;\n                                try {\n                                    uint sessionPid;\n                                    if (Marshal.GetDelegateForFunctionPointer<ComGetPid>(Slot(sessionPtr, 14))(sessionPtr, out sessionPid) < 0) continue;\n                                    if (!pids.Contains((int)sessionPid)) continue;\n                                    Guid iid = IID_VOLUME;\n                                    IntPtr volumePtr;\n                                    if (Marshal.QueryInterface(sessionPtr, ref iid, out volumePtr) < 0) continue;\n                                    return volumePtr; // caller releases\n                                } finally {\n                                    Marshal.Release(sessionPtr);\n                                }\n                            }\n                        } finally {\n                            Marshal.Release(enumPtr);\n                        }\n                    } finally {\n                        Marshal.Release(manager);\n                    }\n                } finally {\n                    Marshal.Release(device);\n                }\n            }\n            return IntPtr.Zero;\n        } finally {\n            Marshal.Release(collection);\n        }\n    }\n}\n'@\nAdd-Type -TypeDefinition $code\n\n$managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType=WindowsRuntime]\n$propsType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType=WindowsRuntime]\n$boolType = [bool]\n$us = [char]31\n$inv = [Globalization.CultureInfo]::InvariantCulture\n$manager = $null\n$artKey = ''\n$artPath = Join-Path $env:TEMP 'autism_spotify_art.png'\n$anyMedia = $env:AUTISM_SPOTIFY_SOURCE -eq 'ANY'\n# Stdin commands are read through a raw StreamReader over the standard input,\n# NOT [Console]::In.ReadLineAsync: on .NET Framework that call takes the console\n# sync root while it blocks on an EMPTY anonymous pipe (exactly what Java's\n# ProcessBuilder hands us - probed: the helper froze silently after 'before\n# readline' with zero output). StreamReader.ReadLineAsync runs on a pool thread\n# with no console lock, so the status loop never stalls on an empty pipe.\n$stdinReader = New-Object System.IO.StreamReader([Console]::OpenStandardInput())\n$readTask = $stdinReader.ReadLineAsync()\n$stdinDead = $false\n# Event-driven refresh: MediaPropertiesChanged (track skip) and\n# PlaybackInfoChanged (play/pause) set a GLOBAL dirty flag (global, not script\n# scope, because the handler executes off the main runspace thread). The loop\n# below re-reads immediately on dirty, otherwise every 450 ms. NOTE: on this box\n# GSMTC property-change events register fine but NEVER fire (probed: two real\n# skips, zero firings - the same stack that hollows art streams and returns null\n# RepeatMode), so the event path is inert here and the 450 ms poll is what\n# actually delivers faster updates: worst-case staleness is halved.\n$global:spotifyDirty = $false\n$subscribedAumid = ''\n$lastPoll = [DateTime]::UtcNow.AddSeconds(-1)\n\nfunction Save-Art($props, $path) {\n    try {\n        if ($null -eq $props.Thumbnail) { return $false }\n        $stream = Await-Op ($props.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\n        if ($null -eq $stream) { return $false }\n        $bytes = [AutismSpotifyVolume]::ReadArtStream($stream)\n        if ($null -eq $bytes) { return $false }\n        # Atomic-ish publish: write a temp sibling, then Copy over the stable path\n        # and delete the temp. [IO.File]::Move(source, dest, overwrite) does NOT\n        # exist on .NET Framework 4.x (only .NET Core 3.0+), so Copy+Delete is the\n        # safe form here - a render-thread read never sees a torn image.\n        $tmp = $path + '.part'\n        [System.IO.File]::WriteAllBytes($tmp, $bytes)\n        [System.IO.File]::Copy($tmp, $path, $true)\n        Remove-Item $tmp -Force\n        return $true\n    } catch {\n        return $false\n    }\n}\n\nfunction Invoke-SpotifyCommand($session, $hint, $line) {\n    if ($line -eq 'PLAY_PAUSE') {\n        if ($null -ne $session) { try { Await-Op ($session.TryTogglePlayPauseAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'NEXT') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipNextAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'PREV') {\n        if ($null -ne $session) { try { Await-Op ($session.TrySkipPreviousAsync()) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_ON') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($true)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SHUFFLE_OFF') {\n        if ($null -ne $session) { try { Await-Op ($session.TryChangeShuffleActiveAsync($false)) $boolType | Out-Null } catch { } }\n    } elseif ($line -eq 'SOURCE=ANY') {\n        $script:anyMedia = $true\n    } elseif ($line -eq 'SOURCE=SPOTIFY') {\n        $script:anyMedia = $false\n    } elseif ($line -match '^REPEAT=(OFF|ALL|ONE)$') {\n        if ($null -ne $session) {\n            # WinRT enum values passed as their underlying int (None=0, Track=1,\n            # All=2): on some boxes the repeat-mode enum type cannot be projected\n            # into PowerShell at all (probed: [type] load fails and the binder then\n            # hides the method). There the call simply no-ops inside this catch;\n            # where the enum projects, the conversion binds and the command works.\n            $modeInt = switch ($Matches[1]) { 'OFF' { 0 } 'ALL' { 2 } 'ONE' { 1 } }\n            try { Await-Op ($session.TryChangeRepeatModeAsync($modeInt)) $boolType | Out-Null } catch { }\n        }\n    } elseif ($line -match '^VOLUME=(\\d+)$') {\n        [AutismSpotifyVolume]::SetSessionVolume($hint, [double]$Matches[1] / 100.0) | Out-Null\n    }\n}\n\nwhile ($true) {\n    $elapsed = ([DateTime]::UtcNow - $lastPoll).TotalMilliseconds\n    if ($global:spotifyDirty -or $elapsed -ge 450) {\n        $global:spotifyDirty = $false\n        $lastPoll = [DateTime]::UtcNow\n    try {\n        if ($null -eq $manager) {\n            $subscribedAumid = ''\n            $manager = Await-Op ($managerType::RequestAsync()) $managerType\n        }\n        $session = $null\n        if ($anyMedia) {\n            $session = $manager.GetCurrentSession()\n        } else {\n            foreach ($candidate in $manager.GetSessions()) {\n                if ($candidate.SourceAppUserModelId -match 'spotify') { $session = $candidate; break }\n            }\n        }\n        if ($null -ne $session -and ([string]$session.SourceAppUserModelId) -ne $subscribedAumid) {\n            # Re-subscribe keyed on the AUMID STRING, not RCW object identity:\n            # if the projection ever yields a fresh RCW for the same session, an\n            # object-reference comparison would re-register handlers every poll.\n            try {\n                $null = $session.add_MediaPropertiesChanged({ $global:spotifyDirty = $true })\n                $null = $session.add_PlaybackInfoChanged({ $global:spotifyDirty = $true })\n                $subscribedAumid = [string]$session.SourceAppUserModelId\n            } catch { }\n        }\n        if ($null -eq $session) {\n            [Console]::WriteLine('STOPPED')\n        } else {\n            $hint = if ($anyMedia) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n            $info = $session.GetPlaybackInfo()\n            $status = ([string]$info.PlaybackStatus).ToUpperInvariant()\n            $props = Await-Op ($session.TryGetMediaPropertiesAsync()) $propsType\n            $artist = ([string]$props.Artist) -replace \"[\\r\\n]\", ' '\n            $title = ([string]$props.Title) -replace \"[\\r\\n]\", ' '\n            $pos = '0'\n            $dur = '0'\n            try {\n                $tl = $session.GetTimelineProperties()\n                if ($null -ne $tl -and $tl.EndTime -gt $tl.StartTime) {\n                    $pos = [Math]::Max(0.0, $tl.Position.TotalSeconds).ToString('F1', $inv)\n                    $dur = ($tl.EndTime - $tl.StartTime).TotalSeconds.ToString('F1', $inv)\n                }\n            } catch { }\n            $shuffle = if ($info.ShuffleActive) { '1' } else { '0' }\n            $repeat = switch ([string]$info.RepeatMode) { 'None' { 'OFF' } 'Track' { 'ONE' } 'All' { 'ALL' } default { 'UNKNOWN' } }\n            $vol = [AutismSpotifyVolume]::GetSessionVolume($hint)\n            $art = ''\n            if ($title.Length -gt 0) {\n                $key = $artist + '|' + $title\n                if ($key -ne $artKey) {\n                    $artKey = $key\n                    if (Save-Art $props $artPath) {\n                        $art = $artPath\n                    } elseif (Test-Path $artPath) {\n                        # A failed save must not leave the PREVIOUS track's art\n                        # behind for the elseif below to serve with the NEW track.\n                        Remove-Item $artPath -Force\n                    }\n                } elseif (Test-Path $artPath) {\n                    $art = $artPath\n                }\n            }\n            [Console]::WriteLine($status + $us + $artist + $us + $title + $us + $pos + $us + $dur + $us + $shuffle + $us + $repeat + $us + $vol + $us + $art)\n        }\n    } catch {\n        $manager = $null\n        $subscribedAumid = ''\n        [Console]::WriteLine('UNAVAILABLE')\n    }\n    }\n    if (-not $stdinDead -and $readTask.IsCompleted) {\n        try {\n            $line = $readTask.Result\n            if ($null -eq $line) {\n                $stdinDead = $true\n            } else {\n                $readTask = $stdinReader.ReadLineAsync()\n                $hintNow = if ($anyMedia -and $null -ne $session) { ([string]$session.SourceAppUserModelId).Split('!')[0] } else { 'spotify' }\n                Invoke-SpotifyCommand $session $hintNow $line\n            }\n        } catch {\n            $stdinDead = $true\n        }\n    }\n    Start-Sleep -Milliseconds 100\n}\n",
               StandardCharsets.UTF_8
            );
            file.toFile().deleteOnExit();
            init_Path = file;
         }

         return init_Path;
      }
   }

   private static void init(int gen, Process child, cq.Inner_StreamUtil backend) {
      String lastArtUrl = "";
      String lastArtPath = "";

      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream(), StandardCharsets.UTF_8));

         label76:
         while (true) {
            try {
               label82: {
                  cq.Inner_ConfigStore snap;
                  while (true) {
                     String line;
                     if ((line = reader.readLine()) == null) {
                        break label76;
                     }

                     try {
                        snap = backend == cq.Inner_StreamUtil.load ? load_cq_ConfigStore(line) : init_cq_ConfigStore_Str(line);
                     } catch (Throwable var11) {
                        continue;
                     }

                     if (backend == cq.Inner_StreamUtil.load && snap.read().startsWith("http")) {
                        if (!snap.read().equals(lastArtUrl)) {
                           lastArtUrl = snap.read();
                           lastArtPath = init_Str_Str_Str(lastArtUrl, snap.init_Str() + "|" + snap.load_Str());
                        }

                        snap = snap.init(lastArtPath);
                     }
                     break;
                  }

                  synchronized (init_Obj) {
                     if (gen != write_I) {
                        break label82;
                     }

                     init_V_cq_ConfigStore(snap);
                  }

                  load(snap);
                  continue;
               }
            } catch (Throwable var13) {
               try {
                  reader.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }

               throw var13;
            }

            reader.close();
            return;
         }

         reader.close();
      } catch (Throwable var14) {
      }
   }

   static void init_V_cq_ConfigStore(cq.Inner_ConfigStore next) {
      init_V_cq_ConfigStore_J(next, Util.getMillis());
   }

   static void init_V_cq_ConfigStore_J(cq.Inner_ConfigStore next, long nowMs) {
      cq.Inner_ConfigStore prev = init_cq_ConfigStore;
      boolean missing = next.load_Str().isBlank()
         && (next.init_cq_TaskQueue() == cq.Inner_TaskQueue.read || next.init_cq_TaskQueue() == cq.Inner_TaskQueue.write);
      if (missing && !prev.load_Str().isBlank() && (prev.init_cq_TaskQueue() == cq.Inner_TaskQueue.init || prev.init_cq_TaskQueue() == cq.Inner_TaskQueue.load)
         )
       {
         if (tick == -9223372036854775808L) {
            tick = nowMs;
         }

         if (nowMs - tick < 3000L) {
            return;
         }
      } else {
         tick = -9223372036854775808L;
      }

      if (next.init_cq_TaskQueue() == cq.Inner_TaskQueue.load
         && (prev.init_cq_TaskQueue() != cq.Inner_TaskQueue.load || !prev.init_Str().equals(next.init_Str()) || !prev.load_Str().equals(next.load_Str()))) {
         pop = nowMs;
      }

      if (prev.init_Str().equals(next.init_Str()) && prev.load_Str().equals(next.load_Str()) && next.read().isEmpty() && !prev.read().isEmpty()) {
         next = next.init(prev.read());
      }

      if (prev.init_cq_TaskQueue() == next.init_cq_TaskQueue() && prev.init_Str().equals(next.init_Str()) && prev.load_Str().equals(next.load_Str())) {
         if (next.read().isEmpty() && !prev.read().isEmpty()) {
            next = next.init(prev.read());
         }

         init_cq_ConfigStore = next.init(prev.init_J());
      } else {
         init_cq_ConfigStore = next;
      }
   }

   static void init_V_J(long nowMs) {
      init_cq_ConfigStore = new cq.Inner_ConfigStore(cq.Inner_TaskQueue.write, "", "", nowMs, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
      pop = 0L;
      tick = -9223372036854775808L;
   }

   private static cq.Inner_ConfigStore load_cq_ConfigStore() {
      return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.write, "", "", System.currentTimeMillis(), 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
   }

   private static void init_V_Str(String cmd) {
      try {
         Process child;
         synchronized (init_Obj) {
            child = init_Process;
         }

         Thread t = new Thread(() -> {
            cq.Inner_StreamUtil backend = init_cq_StreamUtil();
            if (backend != null) {
               init(backend, child, cmd);
            }
         }, "autism-spotify-command");
         t.setDaemon(true);
         t.start();
      } catch (Throwable var6) {
      }
   }

   private static void init(cq.Inner_StreamUtil backend, Process child, String cmd) {
      try {
         switch (backend) {
            case init:
               if (child != null && child.isAlive()) {
                  synchronized (load_Obj) {
                     OutputStream stdin = child.getOutputStream();
                     stdin.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
                     stdin.flush();
                     break;
                  }
               }

               return;
            case load:
               init_V_StrArr(init_StrArr_Str(cmd));
            case read:
            default:
               break;
            case write:
               init_V_StrArr(load_StrArr(cmd));
         }
      } catch (Throwable var7) {
      }
   }

   static String[] init_StrArr_Str(String cmd) {
      String[] base = load_Z ? new String[]{"playerctl"} : new String[]{"playerctl", "--player=spotify"};
      String[] var10000;
      switch (cmd) {
         case "PLAY_PAUSE":
            var10000 = new String[]{"play-pause"};
            break;
         case "NEXT":
            var10000 = new String[]{"next"};
            break;
         case "PREV":
            var10000 = new String[]{"previous"};
            break;
         case "SHUFFLE_ON":
            var10000 = new String[]{"shuffle", "On"};
            break;
         case "SHUFFLE_OFF":
            var10000 = new String[]{"shuffle", "Off"};
            break;
         case "REPEAT=OFF":
            var10000 = new String[]{"loop", "None"};
            break;
         case "REPEAT=ALL":
            var10000 = new String[]{"loop", "Playlist"};
            break;
         case "REPEAT=ONE":
            var10000 = new String[]{"loop", "Track"};
            break;
         default:
            var10000 = cmd.startsWith("VOLUME=")
               ? new String[]{"volume", String.format(Locale.ROOT, "%.2f", init_I_I(Integer.parseInt(cmd.substring(7))) / 100.0)}
               : null;
      }

      String[] action = var10000;
      if (action == null) {
         return null;
      }

      String[] argv = new String[base.length + action.length];
      System.arraycopy(base, 0, argv, 0, base.length);
      System.arraycopy(action, 0, argv, base.length, action.length);
      return argv;
   }

   static String[] load_StrArr(String cmd) {
      String var10000;
      switch (cmd) {
         case "PLAY_PAUSE":
            var10000 = "tell application \"Spotify\" to playpause";
            break;
         case "NEXT":
            var10000 = "tell application \"Spotify\" to next track";
            break;
         case "PREV":
            var10000 = "tell application \"Spotify\" to previous track";
            break;
         case "SHUFFLE_ON":
            var10000 = "tell application \"Spotify\" to set shuffling to true";
            break;
         case "SHUFFLE_OFF":
            var10000 = "tell application \"Spotify\" to set shuffling to false";
            break;
         case "REPEAT=OFF":
            var10000 = "tell application \"Spotify\" to set repeating to false";
            break;
         case "REPEAT=ALL":
         case "REPEAT=ONE":
            var10000 = "tell application \"Spotify\" to set repeating to true";
            break;
         default:
            var10000 = cmd.startsWith("VOLUME=") ? "tell application \"Spotify\" to set sound volume to " + init_I_I(Integer.parseInt(cmd.substring(7))) : null;
      }

      String apple = var10000;
      return apple == null ? null : new String[]{"osascript", "-e", apple};
   }

   private static void init_V_StrArr(String[] argv) {
      if (argv != null) {
         Process process = null;

         try {
            process = new ProcessBuilder(argv).redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD).start();
            if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
               process.destroyForcibly();
            }
         } catch (Exception e) {
            if (process != null) {
               process.destroyForcibly();
            }
         }
      }
   }

   private static boolean init_Z_Str(String tool) {
      return read_Map.computeIfAbsent(tool, cq::load_Z);
   }

   private static boolean load_Z(String tool) {
      Process process = null;

      try {
         process = new ProcessBuilder("sh", "-c", "command -v " + tool).redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD).start();
         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return false;
         } else {
            return process.exitValue() == 0;
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return false;
      }
   }

   private static void load(int gen) {
      while (true) {
         cq.Inner_ConfigStore parsed;
         try {
            parsed = write_cq_ConfigStore(load_Str());
         } catch (Throwable t) {
            parsed = load_cq_ConfigStore();
         }

         synchronized (init_Obj) {
            if (gen != write_I) {
               return;
            }

            init_V_cq_ConfigStore(parsed);
         }

         load(parsed);

         try {
            Thread.sleep(2000L);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   private static String load_Str() {
      Process process = null;

      try {
         process = new ProcessBuilder(
               "gdbus",
               "call",
               "--session",
               "--dest",
               "org.mpris.MediaPlayer2.spotify",
               "--object-path",
               "/org/mpris/MediaPlayer2",
               "--method",
               "org.freedesktop.DBus.Properties.GetAll",
               "org.mpris.MediaPlayer2.Player"
            )
            .redirectError(Redirect.DISCARD)
            .start();
         Process child = process;
         CompletableFuture<byte[]> reader = CompletableFuture.supplyAsync(() -> {
            try {
               return child.getInputStream().readAllBytes();
            } catch (IOException ex) {
               return new byte[0];
            }
         });
         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return "";
         } else {
            byte[] out = reader.get(1L, TimeUnit.SECONDS);
            return process.exitValue() != 0 ? "" : new String(out, StandardCharsets.UTF_8);
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return "";
      }
   }

   private static void read(int gen) {
      while (true) {
         cq.Inner_ConfigStore parsed;
         try {
            parsed = read_cq_ConfigStore(read_Str());
         } catch (Throwable t) {
            parsed = load_cq_ConfigStore();
         }

         synchronized (init_Obj) {
            if (gen != write_I) {
               return;
            }

            init_V_cq_ConfigStore(parsed);
         }

         load(parsed);

         try {
            Thread.sleep(1500L);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   private static String read_Str() {
      Process process = null;

      try {
         process = new ProcessBuilder(
               "osascript",
               "-e",
               "if application \"Spotify\" is running then\n    tell application \"Spotify\"\n        set out to (player state as string) & (ASCII character 31)\n        try\n            set out to out & (artist of current track) & (ASCII character 31) & (name of current track)\n        on error\n            set out to out & (ASCII character 31) & (ASCII character 31)\n        end try\n        try\n            set out to out & (ASCII character 31) & (((player position) * 1000) as integer) & (ASCII character 31) & ((duration of current track) as integer)\n        on error\n            set out to out & (ASCII character 31) & \"0\" & (ASCII character 31) & \"0\"\n        end try\n        try\n            set out to out & (ASCII character 31) & (shuffling as string) & (ASCII character 31) & (repeating as string) & (ASCII character 31) & (sound volume as string)\n        on error\n            set out to out & (ASCII character 31) & \"0\" & (ASCII character 31) & \"\" & (ASCII character 31) & \"-1\"\n        end try\n        try\n            set artPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.png\"\n            set tiffPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.tiff\"\n            set keyPath to POSIX path of (path to temporary items folder) & \"autism_spotify_art.key\"\n            set trackKey to (artist of current track) & \"|\" & (name of current track)\n            set haveKey to false\n            try\n                set haveKey to (read (POSIX file keyPath) as string) is trackKey\n            end try\n            if haveKey then\n                set out to out & (ASCII character 31) & artPath\n            else\n                set d to artwork of current track\n                set f to open for access (POSIX file tiffPath) with write permission\n                write d to f\n                close access f\n                do shell script \"sips -s format png \" & quoted form of tiffPath & \" --out \" & quoted form of artPath & \" >/dev/null 2>&1\"\n                set kf to open for access (POSIX file keyPath) with write permission\n                write trackKey to kf\n                close access kf\n                set out to out & (ASCII character 31) & artPath\n            end if\n        on error\n            try\n                close access (POSIX file tiffPath)\n            end try\n            set out to out & (ASCII character 31) & \"\"\n        end try\n        return out\n    end tell\nend if\n"
            )
            .redirectError(Redirect.DISCARD)
            .start();
         Process child = process;
         CompletableFuture<byte[]> reader = CompletableFuture.supplyAsync(() -> {
            try {
               return child.getInputStream().readAllBytes();
            } catch (IOException ex) {
               return new byte[0];
            }
         });
         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return "";
         } else {
            byte[] out = reader.get(1L, TimeUnit.SECONDS);
            return process.exitValue() != 0 ? "" : new String(out, StandardCharsets.UTF_8);
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return "";
      }
   }

   static String init_Str_Str(String url) {
      return init_Str_Str_Str(url, null);
   }

   static String init_Str_Str_Str(String url, String ownerKey) {
      try {
         URLConnection connection = URI.create(url).toURL().openConnection();
         connection.setConnectTimeout(3000);
         connection.setReadTimeout(3000);

         byte[] data;
         try (InputStream in = connection.getInputStream()) {
            data = in.readNBytes(524289);
         }

         if (!init_Z_BArr(data)) {
            return "";
         }

         data = aq.init_BArr(data);
         if (data == null) {
            return "";
         }

         Path file = load_Path;
         if (file == null) {
            synchronized (init_Obj) {
               if (load_Path == null) {
                  load_Path = Files.createTempFile("autism_spotify_art", ".img");
                  load_Path.toFile().deleteOnExit();
               }

               file = load_Path;
            }
         }

         Path sibling = file.resolveSibling(file.getFileName() + "." + System.nanoTime() + ".part");
         Files.write(sibling, data);
         synchronized (read_Obj) {
            try {
               Files.move(sibling, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
               Files.deleteIfExists(sibling);
            }

            update_Str = ownerKey;
            sync_Str = ownerKey == null ? null : file.toString();
         }

         return file.toString();
      } catch (Throwable t) {
         return "";
      }
   }

   static boolean init_Z_BArr(byte[] data) {
      if (data != null && data.length >= 3) {
         return (data[0] & 255) == 255 && (data[1] & 255) == 216 ? true : (data[0] & 255) == 137 && (data[1] & 255) == 80 && (data[2] & 255) == 78;
      } else {
         return false;
      }
   }

   static void init_V_Str_Str(String stage, String message) {
   }

   static String load_Str(String key) {
      return key != null && key.equals(update_Str) ? sync_Str : load_Map.get(key);
   }

   static void load_V(String key, String path) {
      if (key != null && path != null) {
         if (path.isEmpty()) {
            load_Map.put(key, "");
         } else {
            update_Str = key;
            sync_Str = path;
         }
      }
   }

   private static void load(cq.Inner_ConfigStore snap) {
      if ((snap.init_cq_TaskQueue() == cq.Inner_TaskQueue.init || snap.init_cq_TaskQueue() == cq.Inner_TaskQueue.load)
         && !snap.init_Str().isEmpty()
         && !snap.load_Str().isEmpty()
         && snap.read().isEmpty()) {
         sync();
         write_Str = snap.init_Str() + "|" + snap.load_Str();
         init_V_Str_Str("enqueue", write_Str);
      }
   }

   private static void push() {
      while (true) {
         String key = write_Str;
         if (key == null) {
            try {
               Thread.sleep(200L);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         } else {
            write_Str = null;

            try {
               String path = load_Str(key);
               boolean freshQuery = path == null;
               if (freshQuery) {
                  path = read_Str(key);
                  load_V(key, path);
               }

               init_V_Str_Str(
                  "query",
                  key
                     + " -> "
                     + (
                        path == null
                           ? "transient-failure(retry)"
                           : (path.isEmpty() ? "no-result(cached)" : path + (freshQuery ? write_Str() : " (file alive)"))
                     )
               );
               if (path != null && !path.isEmpty()) {
                  synchronized (init_Obj) {
                     cq.Inner_ConfigStore cur = init_cq_ConfigStore;
                     if ((cur.init_Str() + "|" + cur.load_Str()).equals(key) && cur.read().isEmpty()) {
                        init_V_cq_ConfigStore(cur.init(path));
                        init_V_Str_Str("publish", key + " -> " + path);
                     }
                  }
               }
            } catch (Throwable var8) {
            }
         }
      }
   }

   private static String write_Str() {
      return sync_I < 0 ? "" : " (rung " + init_StrArr[sync_I] + ")";
   }

   static String read_Str(String key) {
      return init_Str_Str_BiFunction(
         key,
         (source, term) -> open(
            "itunes".equals(source)
               ? init_Str + "?term=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&entity=song&limit=5"
               : load_Str + "?q=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&limit=5"
         )
      );
   }

   static String init_Str_Str_BiFunction(String key, BiFunction<String, String, String> fetcher) {
      sync_I = -1;
      String trackTerm = flush(key).isEmpty() ? close(key) : key.replace('|', ' ');
      cq.Inner_BufferHelper selection = init(
         key, init_List_Str_Z(fetcher.apply("itunes", trackTerm), true), init_List_Str_Z(fetcher.apply("deezer", trackTerm), false)
      );
      if (selection.init_cq_DataUtil != null) {
         return init(selection, key, selection.init_I());
      }

      if (selection.load()) {
         return null;
      }

      String artist = flush(key).trim();
      if (!artist.isEmpty() && !close(key).trim().isEmpty()) {
         cq.Inner_BufferHelper widened = init(
            key, init_List_Str_Z(fetcher.apply("itunes", artist), true), init_List_Str_Z(fetcher.apply("deezer", artist), false)
         );
         if (widened.init_cq_DataUtil != null) {
            return init(widened, key, 4);
         } else {
            return widened.load() ? null : "";
         }
      } else {
         return "";
      }
   }

   private static List<cq.Inner_DataUtil> init_List_Str_Z(String json, boolean itunes) {
      return json == null ? null : (itunes ? init_List_Str(json) : load_List(json));
   }

   static cq.Inner_BufferHelper init(String key, List<cq.Inner_DataUtil> itunes, List<cq.Inner_DataUtil> deezer) {
      String artist = flush(key);
      String title = close(key);

      for (int rung = 0; rung <= 3; rung++) {
         cq.Inner_DataUtil candidate = init(itunes, artist, title, rung);
         if (candidate != null) {
            return new cq.Inner_BufferHelper(candidate, rung, true, false);
         }

         candidate = init(deezer, artist, title, rung);
         if (candidate != null) {
            return new cq.Inner_BufferHelper(candidate, rung, false, false);
         }
      }

      return new cq.Inner_BufferHelper(null, -1, false, itunes == null || deezer == null);
   }

   private static cq.Inner_DataUtil init(List<cq.Inner_DataUtil> candidates, String artist, String title, int rung) {
      if (candidates == null) {
         return null;
      }

      cq.Inner_DataUtil firstJunk = null;

      for (cq.Inner_DataUtil candidate : candidates) {
         boolean junk = init(artist, title, candidate);
         if (!junk || rung >= 3) {
            boolean var10000;
            switch (rung) {
               case 0:
                  var10000 = init(artist, title, candidate.init(), candidate.load());
                  break;
               case 1:
                  var10000 = init_Z_Str_Str(title, candidate.load());
                  break;
               case 2:
                  var10000 = load_Z(artist, candidate.init());
                  break;
               default:
                  var10000 = true;
            }

            boolean match = var10000;
            if (match) {
               if (!junk) {
                  return candidate;
               }

               if (firstJunk == null) {
                  firstJunk = candidate;
               }
            }
         }
      }

      return rung == 3 ? firstJunk : null;
   }

   static boolean init(String snapshotArtist, String snapshotTitle, cq.Inner_DataUtil candidate) {
      String snapArtist = snapshotArtist.toLowerCase(Locale.ROOT);
      String snapTitle = snapshotTitle.toLowerCase(Locale.ROOT);
      String candArtist = candidate.init().toLowerCase(Locale.ROOT);
      String candTitle = candidate.load().toLowerCase(Locale.ROOT);

      for (String marker : load_StrArr) {
         if ((candArtist.contains(marker) || candTitle.contains(marker)) && !snapArtist.contains(marker) && !snapTitle.contains(marker)) {
            return true;
         }
      }

      return false;
   }

   private static String init(cq.Inner_BufferHelper selection, String key, int rungLabel) {
      String url = selection.init_Z() ? sync(selection.init_cq_DataUtil().read()) : selection.init_cq_DataUtil().read();
      String path = init_Str_Str_Str(url, key);
      if (path.isEmpty()) {
         return null;
      }

      sync_I = rungLabel;
      return path;
   }

   private static String flush(String key) {
      int sep = key.indexOf(124);
      return sep < 0 ? key : key.substring(0, sep);
   }

   private static String close(String key) {
      int sep = key.indexOf(124);
      return sep < 0 ? key : key.substring(sep + 1);
   }

   private static String open(String url) {
      try {
         URLConnection connection = URI.create(url).toURL().openConnection();
         connection.setConnectTimeout(3000);
         connection.setReadTimeout(3000);

         try (InputStream in = connection.getInputStream()) {
            return new String(in.readNBytes(262144), StandardCharsets.UTF_8);
         }
      } catch (Throwable t) {
         return null;
      }
   }

   static List<cq.Inner_DataUtil> init_List_Str(String json) {
      List<cq.Inner_DataUtil> candidates = new ArrayList<>();
      if (json == null) {
         return candidates;
      }

      try {
         for (JsonElement element : JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("results")) {
            try {
               JsonObject result = element.getAsJsonObject();
               String artist = init_Str_JsonObject_Str(result, "artistName");
               String title = init_Str_JsonObject_Str(result, "trackName");
               String imageUrl = init_Str_JsonObject_Str(result, "artworkUrl100");
               if (artist != null && title != null && imageUrl != null) {
                  candidates.add(new cq.Inner_DataUtil(artist, title, imageUrl));
               }
            } catch (Throwable var8) {
            }
         }
      } catch (Throwable var9) {
      }

      return candidates;
   }

   static List<cq.Inner_DataUtil> load_List(String json) {
      List<cq.Inner_DataUtil> candidates = new ArrayList<>();
      if (json == null) {
         return candidates;
      }

      try {
         for (JsonElement element : JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("data")) {
            try {
               JsonObject item = element.getAsJsonObject();
               String title = init_Str_JsonObject_Str(item, "title");
               String artist = init_Str_JsonObject_Str(item.getAsJsonObject("artist"), "name");
               JsonObject album = item.getAsJsonObject("album");
               String imageUrl = init_Str_JsonObject_Str(album, "cover_big");
               if (imageUrl == null) {
                  imageUrl = init_Str_JsonObject_Str(album, "cover_xl");
               }

               if (artist != null && title != null && imageUrl != null) {
                  candidates.add(new cq.Inner_DataUtil(artist, title, imageUrl));
               }
            } catch (Throwable var9) {
            }
         }
      } catch (Throwable var10) {
      }

      return candidates;
   }

   private static String init_Str_JsonObject_Str(JsonObject object, String field) {
      JsonElement element = object == null ? null : object.get(field);
      return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
   }

   static String write_Str(String json) {
      if (json == null) {
         return "";
      }

      Matcher matcher = init_Pattern.matcher(json);
      return matcher.find() ? matcher.group(1) : "";
   }

   static String update(String text) {
      return text == null
         ? ""
         : text.toLowerCase(Locale.ROOT)
            .replaceAll("\\([^)]*\\)", " ")
            .replaceAll("\\[[^]]*\\]", " ")
            .replaceAll("\\b(feat\\.|ft\\.|featuring|slowed|reverb|remix|sped up|nightcore)\\b", " ")
            .replaceAll("[^\\p{L}\\p{N}]+", " ")
            .trim();
   }

   private static boolean init_Z_Str_Str(String snapshotTitle, String candidateTitle) {
      String snapTitle = update(snapshotTitle);
      String candTitle = update(candidateTitle);
      return !snapTitle.isEmpty() && !candTitle.isEmpty() && (snapTitle.contains(candTitle) || candTitle.contains(snapTitle));
   }

   private static boolean load_Z(String snapshotArtist, String candidateArtist) {
      String snapArtist = update(snapshotArtist);
      String candArtist = update(candidateArtist);
      if (!snapArtist.isEmpty() && !candArtist.isEmpty()) {
         for (String token : snapArtist.split(" ")) {
            if (!token.isEmpty() && candArtist.contains(token)) {
               return true;
            }
         }

         for (String token : candArtist.split(" ")) {
            if (!token.isEmpty() && snapArtist.contains(token)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init(String snapshotArtist, String snapshotTitle, String candidateArtist, String candidateTitle) {
      return init_Z_Str_Str(snapshotTitle, candidateTitle) && load_Z(snapshotArtist, candidateArtist);
   }

   static cq.Inner_DataUtil init(List<cq.Inner_DataUtil> candidates, String snapshotArtist, String snapshotTitle) {
      for (cq.Inner_DataUtil candidate : candidates) {
         if (init(snapshotArtist, snapshotTitle, candidate.init(), candidate.load())) {
            return candidate;
         }
      }

      return null;
   }

   static String sync(String url) {
      return url == null ? "" : url.replace("100x100bb", "600x600bb");
   }

   static cq.Inner_ConfigStore init_cq_ConfigStore_Str(String line) {
      long now = System.currentTimeMillis();
      if (line != null && !line.isBlank()) {
         String[] p = line.split("\u001f", -1);
         cq.Inner_TaskQueue status = init_cq_TaskQueue_Str(p[0]);
         if (status == cq.Inner_TaskQueue.write) {
            return init_cq_ConfigStore_J(now);
         } else {
            String artist = p.length > 1 ? p[1] : "";
            String title = p.length > 2 ? p[2] : "";
            if (status != cq.Inner_TaskQueue.read && !title.isEmpty()) {
               double pos = p.length > 3 ? init_D_Str_D(p[3], 0.0) : 0.0;
               double dur = p.length > 4 ? init_D_Str_D(p[4], 0.0) : 0.0;
               boolean shuffle = p.length > 5 && read_Z(p[5]);
               cq.Inner_CachePool repeat = p.length > 6 ? init_cq_CachePool_Str(p[6]) : cq.Inner_CachePool.write;
               int volume = p.length > 7 ? init_I_Str(p[7]) : -1;
               String art = p.length > 8 ? p[8] : "";
               return new cq.Inner_ConfigStore(status, artist, title, now, pos, dur, shuffle, repeat, volume, art);
            } else {
               return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.read, "", "", now, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
            }
         }
      } else {
         return init_cq_ConfigStore_J(now);
      }
   }

   static cq.Inner_ConfigStore load_cq_ConfigStore(String line) {
      long now = System.currentTimeMillis();
      if (line != null && !line.isBlank()) {
         String[] p = line.split("\u001f", -1);
         cq.Inner_TaskQueue status = init_cq_TaskQueue_Str(p[0]);
         if (status == cq.Inner_TaskQueue.write) {
            return init_cq_ConfigStore_J(now);
         }

         String artist = p.length > 1 ? p[1] : "";
         String title = p.length > 2 ? p[2] : "";
         if (status != cq.Inner_TaskQueue.read && !title.isEmpty()) {
            double pos = p.length > 3 ? init_D_Str(p[3]) : 0.0;
            double dur = p.length > 4 ? init_D_Str(p[4]) : 0.0;
            boolean shuffle = p.length > 5 && read_Z(p[5]);
            cq.Inner_CachePool repeat = p.length > 6 ? init_cq_CachePool_Str(p[6]) : cq.Inner_CachePool.write;
            int volume = -1;
            if (p.length > 7 && !p[7].isBlank()) {
               double v = init_D_Str_D(p[7], 0.0 / 0.0);
               if (!Double.isNaN(v)) {
                  volume = (int)Math.round(v * 100.0);
               }
            }

            String art = "";
            if (p.length > 8) {
               String url = p[8];
               if (url.startsWith("file://")) {
                  art = url.substring("file://".length()).replace("%20", " ");
               } else if (url.startsWith("http://") || url.startsWith("https://")) {
                  art = url;
               }
            }

            return new cq.Inner_ConfigStore(status, artist, title, now, pos, dur, shuffle, repeat, volume, art);
         } else {
            return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.read, "", "", now, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
         }
      } else {
         return init_cq_ConfigStore_J(now);
      }
   }

   static cq.Inner_ConfigStore read_cq_ConfigStore(String rawStdout) {
      if (rawStdout == null) {
         return init_cq_ConfigStore_J(System.currentTimeMillis());
      }

      String line = rawStdout;
      int newline = line.indexOf(10);
      if (newline >= 0) {
         line = line.substring(0, newline);
      }

      if (line.endsWith("\r")) {
         line = line.substring(0, line.length() - 1);
      }

      long now = System.currentTimeMillis();
      if (line.isBlank()) {
         return init_cq_ConfigStore_J(now);
      }

      String[] p = line.split("\u001f", -1);
      cq.Inner_TaskQueue status = init_cq_TaskQueue_Str(p[0]);
      if (status == cq.Inner_TaskQueue.write) {
         return init_cq_ConfigStore_J(now);
      }

      String artist = p.length > 1 ? p[1] : "";
      String title = p.length > 2 ? p[2] : "";
      if (status != cq.Inner_TaskQueue.read && !title.isEmpty()) {
         double pos = p.length > 3 ? init_D_Str_D(p[3], 0.0) / 1000.0 : 0.0;
         double dur = p.length > 4 ? init_D_Str_D(p[4], 0.0) / 1000.0 : 0.0;
         boolean shuffle = p.length > 5 && read_Z(p[5]);
         cq.Inner_CachePool repeat = cq.Inner_CachePool.write;
         if (p.length > 6) {
            if (read_Z(p[6])) {
               repeat = cq.Inner_CachePool.read;
            } else if (!p[6].isBlank()) {
               repeat = cq.Inner_CachePool.init;
            }
         }

         int volume = p.length > 7 ? init_I_Str(p[7]) : -1;
         String art = p.length > 8 ? p[8] : "";
         return new cq.Inner_ConfigStore(status, artist, title, now, pos, dur, shuffle, repeat, volume, art);
      } else {
         return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.read, "", "", now, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
      }
   }

   static cq.Inner_ConfigStore write_cq_ConfigStore(String output) {
      long now = System.currentTimeMillis();
      if (output != null && !output.isBlank()) {
         String statusToken = load_Str(output, "PlaybackStatus");
         if (statusToken == null) {
            return init_cq_ConfigStore_J(now);
         }

         cq.Inner_TaskQueue status = init_cq_TaskQueue_Str(statusToken);
         if (status == cq.Inner_TaskQueue.write) {
            return init_cq_ConfigStore_J(now);
         }

         String title = load_Str(output, "xesam:title");
         if (status != cq.Inner_TaskQueue.read && title != null && !title.isEmpty()) {
            String artist = read(output, "xesam:artist");
            double dur = 0.0;
            String length = load_Str(output, "mpris:length");
            if (length != null) {
               String digits = length.replaceAll("[^0-9]", "");
               if (!digits.isEmpty()) {
                  dur = init_D_Str_D(digits, 0.0) / 1000000.0;
               }
            }

            return new cq.Inner_ConfigStore(status, artist == null ? "" : artist, title, now, 0.0, dur, false, cq.Inner_CachePool.write, -1, "");
         } else {
            return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.read, "", "", now, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
         }
      } else {
         return init_cq_ConfigStore_J(now);
      }
   }

   private static cq.Inner_ConfigStore init_cq_ConfigStore_J(long now) {
      return new cq.Inner_ConfigStore(cq.Inner_TaskQueue.write, "", "", now, 0.0, 0.0, false, cq.Inner_CachePool.write, -1, "");
   }

   private static cq.Inner_TaskQueue init_cq_TaskQueue_Str(String token) {
      if (token == null) {
         return cq.Inner_TaskQueue.write;
      }

      cq.Inner_TaskQueue var10000;
      switch (token.trim().toUpperCase(Locale.ROOT)) {
         case "PLAYING":
            var10000 = cq.Inner_TaskQueue.init;
            break;
         case "PAUSED":
            var10000 = cq.Inner_TaskQueue.load;
            break;
         case "STOPPED":
            var10000 = cq.Inner_TaskQueue.read;
            break;
         default:
            var10000 = cq.Inner_TaskQueue.write;
      }

      return var10000;
   }

   private static double init_D_Str_D(String token, double fallback) {
      try {
         return Double.parseDouble(token.trim());
      } catch (Exception e) {
         return fallback;
      }
   }

   private static double init_D_Str(String token) {
      return init_D_Str_D(token, 0.0) / 1000000.0;
   }

   static int init_I_Str(String token) {
      if (token != null && !token.isBlank()) {
         try {
            int v = Integer.parseInt(token.trim());
            return v < 0 ? -1 : init_I_I(v);
         } catch (Exception e) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static boolean read_Z(String token) {
      String t = token.trim().toLowerCase(Locale.ROOT);
      return t.equals("1") || t.equals("true") || t.equals("on");
   }

   static cq.Inner_CachePool init_cq_CachePool_Str(String token) {
      cq.Inner_CachePool var10000;
      switch (token.trim().toUpperCase(Locale.ROOT)) {
         case "OFF":
         case "NONE":
            var10000 = cq.Inner_CachePool.init;
            break;
         case "ONE":
         case "TRACK":
            var10000 = cq.Inner_CachePool.load;
            break;
         case "ALL":
         case "PLAYLIST":
            var10000 = cq.Inner_CachePool.read;
            break;
         default:
            var10000 = cq.Inner_CachePool.write;
      }

      return var10000;
   }

   private static String load_Str(String output, String key) {
      return write(output, "'" + key + "': <");
   }

   private static String read(String output, String key) {
      return write(output, "'" + key + "': <[");
   }

   private static String write(String output, String needle) {
      int start = output.indexOf(needle);
      if (start < 0) {
         return null;
      }

      start += needle.length();
      if (start >= output.length()) {
         return null;
      }

      char quote = output.charAt(start);
      if (quote != '\'' && quote != '"') {
         return null;
      }

      int from = start + 1;

      while (true) {
         int end = output.indexOf(quote, from);
         if (end < 0) {
            return null;
         }

         int backslashes = 0;

         for (int j = end - 1; j >= from && output.charAt(j) == '\\'; j--) {
            backslashes++;
         }

         if (backslashes % 2 == 0) {
            return init_Str_Str_C(output.substring(from, end), quote);
         }

         from = end + 1;
      }
   }

   private static String init_Str_Str_C(String value, char quote) {
      StringBuilder out = new StringBuilder(value.length());

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c == '\\' && i + 1 < value.length() && (value.charAt(i + 1) == '\\' || value.charAt(i + 1) == quote)) {
            out.append(value.charAt(++i));
         } else {
            out.append(c);
         }
      }

      return out.toString();
   }

   record Inner_BufferHelper(cq.Inner_DataUtil init_cq_DataUtil, int init_I, boolean init_Z, boolean load) {
   }

   public enum Inner_CachePool {
      init,
      load,
      read,
      write;
   }

   public record Inner_ConfigStore(
      cq.Inner_TaskQueue init_cq_TaskQueue,
      String init_Str,
      String load_Str,
      long init_J,
      double init_D,
      double load_D,
      boolean init_Z,
      cq.Inner_CachePool init_cq_CachePool,
      int init_I,
      String read
   ) {
      public Inner_ConfigStore {
         if (init_cq_TaskQueue == null) {
            init_cq_TaskQueue = cq.Inner_TaskQueue.write;
         }

         if (init_Str == null) {
            init_Str = "";
         }

         if (load_Str == null) {
            load_Str = "";
         }

         if (init_cq_CachePool == null) {
            init_cq_CachePool = cq.Inner_CachePool.write;
         }

         if (read == null) {
            read = "";
         }

         if (init_I > 100) {
            init_I = 100;
         }

         if (init_I < -1) {
            init_I = -1;
         }

         if (Double.isNaN(init_D) || init_D < 0.0) {
            init_D = 0.0;
         }

         if (Double.isNaN(load_D) || load_D < 0.0) {
            load_D = 0.0;
         }
      }

      public cq.Inner_ConfigStore init(long ms) {
         return new cq.Inner_ConfigStore(
            this.init_cq_TaskQueue, this.init_Str, this.load_Str, ms, this.init_D, this.load_D, this.init_Z, this.init_cq_CachePool, this.init_I, this.read
         );
      }

      public cq.Inner_ConfigStore init(String path) {
         return new cq.Inner_ConfigStore(
            this.init_cq_TaskQueue, this.init_Str, this.load_Str, this.init_J, this.init_D, this.load_D, this.init_Z, this.init_cq_CachePool, this.init_I, path
         );
      }
   }

   record Inner_DataUtil(String init, String load, String read) {
   }

   private enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }

   public enum Inner_TaskQueue {
      init,
      load,
      read,
      write;
   }
}
