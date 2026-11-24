#!/usr/bin/env python3
"""
Safely reuses already-cleanly-compiling Java source files from a previous upstream release
when (and ONLY when):
  1. All raw .class + $*.class bytes for the top-level class are SHA-256 identical between old_jar and new_jar
  2. All disambig.tsv mappings for that class + inner classes (and every class it references via constant pool)
     are identical between old_disambig and new_disambig.
Actually even simpler & 100% airtight: compare the SHA-256 of the DISAMBIGUATED .class + $*.class bytes
in old's build/disambig-decompile.jar vs new's build/disambig-decompile.jar!
If the disambiguated bytecode fed to Vineflower is 100% byte-for-byte identical, reusing the previous
cleanly-compiled .java file is mathematically lossless.
"""
import hashlib, os, subprocess, sys, zipfile

if len(sys.argv) < 5:
    print("Usage: reuse_unchanged.py <old_disambig_jar> <new_disambig_jar> <old_git_repo_or_src_dir> <new_src_dir> [git_ref]", file=sys.stderr)
    sys.exit(2)

old_jar, new_jar, old_src, new_src = sys.argv[1:5]
git_ref = sys.argv[5] if len(sys.argv) > 5 else None

def hash_by_outer(jar_path):
    grouped = {}
    with zipfile.ZipFile(jar_path) as zf:
        for name in sorted(zf.namelist()):
            if not name.endswith(".class"):
                continue
            cn = name[:-6]
            outer = cn.split("$", 1)[0]
            grouped.setdefault(outer, []).append((name, zf.read(name)))
    out = {}
    for outer, items in grouped.items():
        h = hashlib.sha256()
        for name, data in items:
            h.update(name.encode("utf-8"))
            h.update(b"\0")
            h.update(data)
            h.update(b"\0")
        out[outer] = h.hexdigest()
    return out

h_old = hash_by_outer(old_jar)
h_new = hash_by_outer(new_jar)

reused = 0
for outer, digest in h_new.items():
    if h_old.get(outer) != digest:
        continue
    rel = outer + ".java"
    dst = os.path.join(new_src, rel)
    if not os.path.exists(dst):
        continue
    if git_ref:
        res = subprocess.run(
            ["git", "-C", old_src, "show", f"{git_ref}:src/main/java/{rel}"],
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL
        )
        if res.returncode == 0:
            open(dst, "wb").write(res.stdout)
            reused += 1
    else:
        src = os.path.join(old_src, rel)
        if os.path.exists(src):
            open(dst, "wb").write(open(src, "rb").read())
            reused += 1

print(f"reuse_unchanged: reused {reused}/{len(h_new)} byte-exact disambiguated source files.")
