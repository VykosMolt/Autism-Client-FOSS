#!/usr/bin/env python3
"""
Transfers `throws ...` clauses from a previous clean upstream git ref onto newly decompiled
Java files (handling obfuscator class renames via DiffReleases old->new map), because the
obfuscator strips JVM Exceptions attributes so Vineflower cannot recover checked `throws`.
"""
import os, re, subprocess, sys

if len(sys.argv) < 5:
    print("Usage: restore_throws.py <prev_git_repo> <prev_git_ref> <obf_map_tsv> <new_src_dir>", file=sys.stderr)
    sys.exit(2)

repo, ref, map_tsv, new_src = sys.argv[1:5]

old_to_new = {}
for l in open(map_tsv, encoding="utf-8"):
    if l.startswith("#") or not l.strip():
        continue
    p = l.strip().split("\t")
    old_to_new[p[0]] = p[1]

# Match method header ending with `throws <exc_list> {` or `throws <exc_list>;`
THROWS_RE = re.compile(r'([A-Za-z0-9_<>?, \[\].$]+?\([^;{}]*\))\s+(throws\s+[A-Za-z0-9_., $]+?)(\s*[\{;])')

out = subprocess.check_output(["git", "-C", repo, "grep", "-l", "throws ", ref, "--", "src/main/java/"], text=True)
patched_files = 0
patched_methods = 0

for line in out.splitlines():
    rel = line.split("src/main/java/", 1)[1]
    old_c = rel[:-5]
    new_c = old_to_new.get(old_c, old_c)
    new_p = os.path.join(new_src, new_c + ".java")
    if not os.path.exists(new_p):
        continue
    old_txt = subprocess.check_output(["git", "-C", repo, "show", f"{ref}:src/main/java/{rel}"], text=True)
    new_txt = open(new_p, encoding="utf-8").read()
    orig_new = new_txt

    # Extract every (method_name, param_count, throws_clause) and exact signature from old_txt
    for m in THROWS_RE.finditer(old_txt):
        sig = m.group(1).strip()
        thr = m.group(2).strip()
        # Normalize whitespace in sig
        # Extract method name + parameter types/names
        m_name = re.search(r'\b([A-Za-z0-9_$]+)\s*\(([^)]*)\)$', sig)
        if not m_name:
            continue
        mname = m_name.group(1)
        params_raw = m_name.group(2).strip()
        # Try exact match first (after simple class rename if outer class renamed)
        old_simple = old_c.rsplit("/", 1)[-1]
        new_simple = new_c.rsplit("/", 1)[-1]
        cand_sigs = [sig]
        if old_simple != new_simple:
            cand_sigs.append(re.sub(r'\b' + re.escape(old_simple) + r'\b', new_simple, sig))
        applied = False
        for cs in cand_sigs:
            # Match cs followed by `{` or `;` WITHOUT already having `throws`
            pat = re.escape(cs) + r'(\s*[\{;])'
            if re.search(pat, new_txt):
                new_txt = re.sub(pat, lambda mm: cs + " " + thr + mm.group(1), new_txt, count=1)
                patched_methods += 1
                applied = True
                break
        if not applied and mname not in ("init", "load", "read", "write", "update", "sync"):
            # Fallback by unique method name + same param count
            pcount = 0 if not params_raw else len(params_raw.split(","))
            pat2 = re.compile(r'(\b' + re.escape(mname) + r'\s*\([^){};]*\))(\s*\{)')
            matches = [mm for mm in pat2.finditer(new_txt) if (0 if not mm.group(1).split("(",1)[1][:-1].strip() else len(mm.group(1).split("(",1)[1][:-1].split(","))) == pcount]
            if len(matches) == 1:
                mm = matches[0]
                if "throws " not in new_txt[mm.start():mm.end()+30]:
                    new_txt = new_txt[:mm.end(1)] + " " + thr + new_txt[mm.end(1):]
                    patched_methods += 1

    if new_txt != orig_new:
        # Ensure java.io.IOException etc. are imported or FQN? If throws uses simple name not imported, qualify common ones!
        for simple, fqn in [
            ("IOException", "java.io.IOException"),
            ("InterruptedException", "java.lang.InterruptedException"),
            ("ExecutionException", "java.util.concurrent.ExecutionException"),
            ("TimeoutException", "java.util.concurrent.TimeoutException"),
            ("CommandSyntaxException", "com.mojang.brigadier.exceptions.CommandSyntaxException"),
            ("CryptException", "net.minecraft.util.CryptException"),
        ]:
            if re.search(r'throws[^{;]*\b' + simple + r'\b', new_txt) and f"import {fqn};" not in new_txt and fqn not in new_txt:
                new_txt = re.sub(r'(throws[^{;]*?)\b' + simple + r'\b', r'\1' + fqn, new_txt)
        open(new_p, "w", encoding="utf-8").write(new_txt)
        patched_files += 1

print(f"restore_throws: updated {patched_methods} method(s) across {patched_files} file(s).")
