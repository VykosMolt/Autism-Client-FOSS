#!/usr/bin/env python3
"""
Lossless Java class renamer for AUTISM Client FOSS.

Renames top-level classes in src/main/java and tools/checks while updating
mappings/disambig.tsv so ReverseRemapJar restores the exact original runtime
ABI in the packaged Fabric mod JAR (keeping binary addons 100% compatible).

Usage:
  python3 tools/deobf_rename.py apply <mappings.txt> [--dry-run]
  python3 tools/deobf_rename.py verify-abi <baseline.jar> <rebuilt.jar>

Mapping file format (one per line, # comments and blank lines ignored):
  autismclient.util.aW=MicrosoftAuth
  autismclient.commands.b.RingBuffer=HelpCommand
"""

import pathlib
import re
import subprocess
import sys
import zipfile
from typing import Dict, List, Tuple

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC_ROOT = ROOT / "src" / "main" / "java"
CHECKS_ROOT = ROOT / "tools" / "checks"
DISAMBIG_TSV = ROOT / "mappings" / "disambig.tsv"

# Java lexer: preserves exact text when concatenated
TOKEN_RE = re.compile(
    r'(?P<block_comment>/\*[\s\S]*?\*/)'
    r'|(?P<line_comment>//[^\n]*)'
    r'|(?P<text_block>"""[\s\S]*?""")'
    r'|(?P<string>"(?:\\.|[^"\\])*")'
    r'|(?P<char>\'(?:\\.|[^\'\\])*\')'
    r'|(?P<ws>\s+)'
    r'|(?P<ident>[A-Za-z_$][A-Za-z0-9_$]*)'
    r'|(?P<punct>::|->|[.,;(){}\[\]<>:?@=+\-*/&|^!~%])'
    r'|(?P<other>.)'
)

TYPE_BEFORE_KW = {
    "class", "interface", "enum", "record", "extends", "implements",
    "permits", "new", "instanceof", "throws",
}
MODIFIER_OR_BOUNDARY = {
    "public", "protected", "private", "static", "final", "abstract",
    "synchronized", "native", "strictfp", "transient", "volatile",
    "sealed", "non-sealed", "default", "{", "}", ";", ")",
}
JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
    "class", "const", "continue", "default", "do", "double", "else", "enum",
    "extends", "false", "final", "finally", "float", "for", "goto", "if",
    "implements", "import", "instanceof", "int", "interface", "long", "native",
    "new", "null", "package", "private", "protected", "public", "record",
    "return", "short", "static", "strictfp", "super", "switch", "synchronized",
    "this", "throw", "throws", "transient", "true", "try", "var", "void",
    "volatile", "while", "yield", "sealed", "non-sealed", "permits",
}


def tokenize(text: str) -> List[Tuple[str, str]]:
    return [(m.lastgroup, m.group()) for m in TOKEN_RE.finditer(text)]


def non_trivia_indices(tokens: List[Tuple[str, str]]) -> List[int]:
    return [
        i for i, (kind, _) in enumerate(tokens)
        if kind not in ("ws", "line_comment", "block_comment")
    ]


def parse_package_and_imports(tokens: List[Tuple[str, str]], nt: List[int]):
    pkg = ""
    # direct_imports: simple_name -> fq_pkg
    direct_imports: Dict[str, str] = {}
    wildcard_pkgs: List[str] = []
    pos = 0
    n = len(nt)
    while pos < n:
        kind, val = tokens[nt[pos]]
        if kind == "ident" and val == "package":
            pos += 1
            parts = []
            while pos < n and tokens[nt[pos]][1] != ";":
                parts.append(tokens[nt[pos]][1])
                pos += 1
            pkg = "".join(parts)
            pos += 1
        elif kind == "ident" and val == "import":
            pos += 1
            is_static = False
            if pos < n and tokens[nt[pos]][1] == "static":
                is_static = True
                pos += 1
            parts = []
            while pos < n and tokens[nt[pos]][1] != ";":
                parts.append(tokens[nt[pos]][1])
                pos += 1
            imp = "".join(parts)
            pos += 1
            if not is_static:
                if imp.endswith(".*"):
                    wildcard_pkgs.append(imp[:-2])
                elif "." in imp:
                    p, cls = imp.rsplit(".", 1)
                    direct_imports[cls] = p
        else:
            break
    return pkg, direct_imports, wildcard_pkgs


def collect_local_shadows(tokens: List[Tuple[str, str]], nt: List[int], target_names: set) -> set:
    """
    Detect any target short identifier used as a local variable, lambda param,
    method param, or field name in this file (so we never rename a variable).
    """
    shadows = set()
    n = len(nt)
    for k in range(n):
        kind, val = tokens[nt[k]]
        if kind != "ident" or val not in target_names:
            continue
        prev_tok = tokens[nt[k - 1]] if k > 0 else ("", "")
        next_tok = tokens[nt[k + 1]] if k + 1 < n else ("", "")
        # Lambda parameter: `b ->` or `(a, b) ->`
        if next_tok[1] == "->":
            shadows.add(val)
            continue
        # Variable/param/field declaration: `<Type> val [= , ) ; :]`
        if prev_tok[0] == "ident" and prev_tok[1] not in JAVA_KEYWORDS:
            if next_tok[1] in ("=", ",", ")", ";", ":"):
                shadows.add(val)
                continue
        if prev_tok[1] in (">", "]") and next_tok[1] in ("=", ",", ")", ";", ":"):
            shadows.add(val)
            continue
        # Catch / primitive type var: `int a =`, `boolean b;`
        if prev_tok[1] in ("int", "long", "short", "byte", "char", "float", "double", "boolean", "var"):
            if next_tok[1] in ("=", ",", ")", ";", ":"):
                shadows.add(val)
                continue
    return shadows


def rewrite_java_file(
    text: str,
    renames: Dict[Tuple[str, str], str],
    is_own_file: Tuple[str, str] | None,
) -> str:
    """
    renames: {(pkg, old_simple): new_simple}
    """
    tokens = tokenize(text)
    nt = non_trivia_indices(tokens)
    cur_pkg, direct_imports, _ = parse_package_and_imports(tokens, nt)

    # Determine which bare simple names in this file resolve to one of our renames
    bare_map: Dict[str, str] = {}
    for (pkg, old), new in renames.items():
        if old in direct_imports:
            if direct_imports[old] == pkg:
                bare_map[old] = new
        elif cur_pkg == pkg:
            bare_map[old] = new

    # Pre-build qualified lookup: parts tuple -> (len, new_simple)
    pkg_sequences: Dict[Tuple[str, ...], str] = {}
    for (pkg, old), new in renames.items():
        parts = tuple(pkg.split(".") + [old])
        pkg_sequences[parts] = new
    max_seq_len = max((len(k) for k in pkg_sequences), default=0)

    shadows = collect_local_shadows(tokens, nt, set(bare_map.keys())) if bare_map else set()
    # Never treat the class's own name inside its own file as shadowed unless explicitly a local var
    # (we still guard per-occurrence context below)

    out_tokens = list(tokens)
    n = len(nt)
    k = 0
    while k < n:
        idx = nt[k]
        kind, val = tokens[idx]

        # 1. Check qualified package.Class reference (including inside import statements!)
        matched_qual = False
        if kind == "ident" and val == "autismclient":
            # Read dot-separated ident chain starting at k
            chain_k = [k]
            chain_vals = [val]
            p = k
            while p + 2 < n and tokens[nt[p + 1]][1] == "." and tokens[nt[p + 2]][0] == "ident":
                p += 2
                chain_k.append(p)
                chain_vals.append(tokens[nt[p]][1])
                if len(chain_vals) <= max_seq_len:
                    tup = tuple(chain_vals)
                    if tup in pkg_sequences:
                        # Replace the final class name token in the qualified chain
                        cls_tok_idx = nt[p]
                        out_tokens[cls_tok_idx] = ("ident", pkg_sequences[tup])
                        k = p + 1
                        matched_qual = True
                        break
        if matched_qual:
            continue

        # 2. Check bare identifier reference (outside package/import headers)
        if kind == "ident" and val in bare_map:
            prev_tok = tokens[nt[k - 1]] if k > 0 else ("", "")
            next_tok = tokens[nt[k + 1]] if k + 1 < n else ("", "")
            prev2_tok = tokens[nt[k - 2]] if k > 1 else ("", "")

            # Never rename member access (`obj.val` or `Foo::val`)
            if prev_tok[1] in (".", "::"):
                k += 1
                continue

            new_name = bare_map[val]

            # Always rename in explicit type/declaration positions
            if prev_tok[1] in TYPE_BEFORE_KW:
                out_tokens[idx] = ("ident", new_name)
                k += 1
                continue

            # Constructor declaration in its own file: `[public|private|protected|{] Old(`
            if is_own_file and val == is_own_file[1] and next_tok[1] == "(" and prev_tok[1] in MODIFIER_OR_BOUNDARY:
                out_tokens[idx] = ("ident", new_name)
                k += 1
                continue

            # Skip if this specific token is a local variable / lambda param / field declaration
            if next_tok[1] == "->":
                k += 1
                continue
            if prev_tok[0] == "ident" and prev_tok[1] not in JAVA_KEYWORDS and next_tok[1] in ("=", ",", ")", ";", ":"):
                k += 1
                continue
            if prev_tok[1] in (">", "]") and next_tok[1] in ("=", ",", ")", ";", ":"):
                k += 1
                continue
            if prev_tok[1] in ("int", "long", "short", "byte", "char", "float", "double", "boolean", "var") and next_tok[1] in ("=", ",", ")", ";", ":"):
                k += 1
                continue

            # If this file has NO local variable shadowing `val`, check remaining unambiguous type/static positions
            is_type_context = False
            if next_tok[1] in (".", "::", "<", "["):
                # `Old.foo`, `Old.class`, `Old.this`, `Old.Inner`, `Old::m`, `Old<T>`, `Old[]`
                # If shadowed by a local variable, only rename if `.class` / `.this` / `.Inner_` / `<` / `[`
                if val not in shadows:
                    is_type_context = True
                elif next_tok[1] in ("<", "["):
                    is_type_context = True
                elif next_tok[1] == "." and k + 2 < n and (
                    tokens[nt[k + 2]][1] in ("class", "this", "super")
                    or tokens[nt[k + 2]][1].startswith("Inner_")
                ):
                    is_type_context = True
            elif next_tok[0] == "ident" and next_tok[1] not in JAVA_KEYWORDS:
                # `Old varName` (type position!)
                if prev_tok[1] not in (".", "::"):
                    is_type_context = True
            elif prev_tok[1] == "(" and next_tok[1] == ")":
                # Cast `(Old)expr` — check token after `)`
                after = tokens[nt[k + 2]] if k + 2 < n else ("", "")
                if after[0] in ("ident", "string", "char") or after[1] in ("(", "!", "~", "new", "this", "super"):
                    if prev2_tok[1] not in ("if", "while", "switch", "for", "synchronized") and prev2_tok[0] != "ident":
                        is_type_context = True
                    elif prev2_tok[1] in ("return", "throw", "case"):
                        is_type_context = True
            elif prev_tok[1] in ("<", ",", "extends", "super") and next_tok[1] in (">", ",", "&"):
                # Generic type arg `List<Old>`, `Map<K, Old>`, `<? extends Old>`
                if val not in shadows or prev_tok[1] in ("<", "extends", "super"):
                    is_type_context = True

            if is_type_context:
                out_tokens[idx] = ("ident", new_name)

        k += 1

    return "".join(v for _, v in out_tokens)


def update_disambig_tsv(renames: Dict[Tuple[str, str], str], dry_run: bool) -> Tuple[int, int]:
    """
    Update mappings/disambig.tsv so ReverseRemapJar continues mapping every
    renamed class/member back to its exact original runtime ABI name.
    """
    # Map old_internal -> new_internal
    int_map: Dict[str, str] = {
        f"{pkg.replace('.', '/')}/{old}": f"{pkg.replace('.', '/')}/{new}"
        for (pkg, old), new in renames.items()
    }

    def remap_owner(owner: str) -> str:
        if owner in int_map:
            return int_map[owner]
        if "$" in owner:
            top, rest = owner.split("$", 1)
            if top in int_map:
                return int_map[top] + "$" + rest
        return owner

    desc_re = re.compile(r'L(autismclient/[^;]+);')

    def remap_desc(desc: str) -> str:
        return desc_re.sub(lambda m: f"L{remap_owner(m.group(1))};", desc)

    lines = DISAMBIG_TSV.read_text(encoding="utf-8").splitlines()
    out_lines: List[str] = []
    mapped_c_targets = set()
    updated_count = 0

    for line in lines:
        if not line or line.startswith("#"):
            out_lines.append(line)
            continue
        p = line.split("\t")
        tag = p[0]
        if tag == "C" and len(p) >= 3:
            new_cur = remap_owner(p[2])
            if new_cur != p[2]:
                p[2] = new_cur
                updated_count += 1
            mapped_c_targets.add(p[2])
            out_lines.append("\t".join(p))
        elif tag in ("F", "M") and len(p) >= 7:
            new_owner = remap_owner(p[4])
            new_desc = remap_desc(p[6])
            if new_owner != p[4] or new_desc != p[6]:
                p[4] = new_owner
                p[6] = new_desc
                updated_count += 1
            out_lines.append("\t".join(p))
        else:
            out_lines.append(line)

    # Add C entries for classes that previously had identity mapping (not in disambig.tsv C section)
    added_c = []
    for old_int, new_int in sorted(int_map.items()):
        if new_int not in mapped_c_targets:
            added_c.append(f"C\t{old_int}\t{new_int}")

    if added_c:
        # Insert new C entries right after the last C line (or header)
        insert_idx = 0
        for i, l in enumerate(out_lines):
            if l.startswith("C\t") or l.startswith("#"):
                insert_idx = i + 1
        out_lines[insert_idx:insert_idx] = added_c

    if not dry_run:
        DISAMBIG_TSV.write_text("\n".join(out_lines) + "\n", encoding="utf-8")

    return len(added_c), updated_count


def load_mappings(path: pathlib.Path) -> Dict[Tuple[str, str], str]:
    renames: Dict[Tuple[str, str], str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        left, right = line.split("=", 1)
        left, right = left.strip(), right.strip()
        pkg, old = left.rsplit(".", 1)
        renames[(pkg, old)] = right
    return renames


def cmd_apply(mapping_path: pathlib.Path, dry_run: bool):
    renames = load_mappings(mapping_path)
    print(f"Loaded {len(renames)} class renames from {mapping_path}")

    # Validate sources exist and targets don't clash
    for (pkg, old), new in renames.items():
        pkg_dir = SRC_ROOT / pkg.replace(".", "/")
        src = pkg_dir / f"{old}.java"
        dst = pkg_dir / f"{new}.java"
        if not src.exists():
            sys.exit(f"ERROR: source file missing: {src}")
        if dst.exists() and (pkg, new) not in renames:
            sys.exit(f"ERROR: target file already exists: {dst}")

    # Collect all Java files in src/main/java and tools/checks
    java_files = sorted(SRC_ROOT.rglob("*.java")) + sorted(CHECKS_ROOT.rglob("*.java"))
    old_file_set = {
        (SRC_ROOT / pkg.replace(".", "/") / f"{old}.java"): (pkg, old)
        for (pkg, old) in renames
    }

    edited_files = 0
    for jf in java_files:
        orig = jf.read_text(encoding="utf-8")
        own = old_file_set.get(jf)
        rewritten = rewrite_java_file(orig, renames, own)
        if rewritten != orig:
            edited_files += 1
            if not dry_run:
                jf.write_text(rewritten, encoding="utf-8")

    # Rename source files via git mv
    moved_files = 0
    for (pkg, old), new in sorted(renames.items()):
        pkg_dir = SRC_ROOT / pkg.replace(".", "/")
        src = pkg_dir / f"{old}.java"
        dst = pkg_dir / f"{new}.java"
        moved_files += 1
        if not dry_run:
            subprocess.run(["git", "mv", str(src), str(dst)], cwd=ROOT, check=True)

    added_c, updated_rows = update_disambig_tsv(renames, dry_run)
    print(
        f"{'[DRY-RUN] ' if dry_run else ''}Renamed {moved_files} files, "
        f"edited {edited_files} Java files, added {added_c} C mappings, "
        f"updated {updated_rows} disambig.tsv rows."
    )


def cmd_verify_abi(baseline_jar: pathlib.Path, rebuilt_jar: pathlib.Path):
    with zipfile.ZipFile(baseline_jar) as zb, zipfile.ZipFile(rebuilt_jar) as zr:
        base_entries = sorted(zb.namelist())
        new_entries = sorted(zr.namelist())

    base_set = set(base_entries)
    new_set = set(new_entries)
    only_base = sorted(base_set - new_set)
    only_new = sorted(new_set - base_set)
    print(f"Baseline entries: {len(base_entries)}, Rebuilt entries: {len(new_entries)}")
    if only_base or only_new:
        print(f"ABI ENTRY MISMATCH! only_in_baseline={len(only_base)}, only_in_rebuilt={len(only_new)}")
        for x in only_base[:30]:
            print(f"  - {x}")
        for x in only_new[:30]:
            print(f"  + {x}")
        sys.exit(1)
    print("PASS: Packaged JAR entry list (all classes + resources) is 100% identical!")


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(2)
    cmd = sys.argv[1]
    if cmd == "apply":
        dry_run = "--dry-run" in sys.argv[3:]
        cmd_apply(pathlib.Path(sys.argv[2]), dry_run)
    elif cmd == "verify-abi" and len(sys.argv) >= 4:
        cmd_verify_abi(pathlib.Path(sys.argv[2]), pathlib.Path(sys.argv[3]))
    else:
        print(__doc__)
        sys.exit(2)


if __name__ == "__main__":
    main()
