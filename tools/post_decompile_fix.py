#!/usr/bin/env python3
"""Deterministic post-decompile cleanup for Vineflower 1.12.0 output on AUTISM 5.3."""
import glob, os, re, sys

src_root = sys.argv[1]
counts = {"unrep": 0, "xor": 0, "mixin_cast": 0, "mixin_inst": 0, "win_acc": 0, "brigadier": 0}

XOR_RE = re.compile(r'return \(boolean\)\(([a-zA-Z0-9_$.()]+)\s*\^\s*([a-zA-Z0-9_]+)\s*\^\s*([a-zA-Z0-9_]+)\);')
MIXIN_CAST_RE = re.compile(r'\(([A-Z][A-Za-z0-9_$.<>, ?]*)\)this\b')
MIXIN_INST_RE = re.compile(r'(?<!\(Object\))\bthis\s+instanceof\b')
WIN_ACC_RE = re.compile(r'\(AutismWindowAccessor\)(?!\(Object\))')
LIT_RE = re.compile(r'LiteralArgumentBuilder\.literal\(')
REQ_RE = re.compile(r'RequiredArgumentBuilder\.argument\(')

for p in glob.glob(os.path.join(src_root, "**", "*.java"), recursive=True):
    s = open(p, encoding="utf-8").read()
    orig = s
    if "<unrepresentable>" in s:
        s = s.replace("<unrepresentable>.super.", "this.").replace("<unrepresentable>.", "")
        counts["unrep"] += 1
    if "return (boolean)(" in s and "^" in s:
        s2 = XOR_RE.sub(r'return ((\1 ? 1 : 0) ^ \2 ^ \3) != 0;', s)
        if s2 != s:
            s = s2
            counts["xor"] += 1
    if "(AutismWindowAccessor)" in s:
        s2 = WIN_ACC_RE.sub("(AutismWindowAccessor)(Object)", s)
        if s2 != s:
            s = s2
            counts["win_acc"] += 1
    if "/mixin/" in p and "@Mixin" in s:
        def fix_cast(m):
            t = m.group(1)
            return m.group(0) if t == "Object" else f"({t})(Object)this"
        s2 = MIXIN_CAST_RE.sub(fix_cast, s)
        if s2 != s:
            s = s2
            counts["mixin_cast"] += 1
        s2 = MIXIN_INST_RE.sub("(Object)this instanceof", s)
        s2 = s2.replace("== this)", "== (Object)this)").replace("!= this)", "!= (Object)this)")
        if s2 != s:
            s = s2
            counts["mixin_inst"] += 1
    if "/commands/b/" in p and "AutismCommandSource" in s:
        s2 = LIT_RE.sub("LiteralArgumentBuilder.<AutismCommandSource>literal(", s)
        if s2 != s:
            s = s2
            counts["brigadier"] += 1
    if s != orig:
        open(p, "w", encoding="utf-8").write(s)

print("post_decompile_fix:", counts)
