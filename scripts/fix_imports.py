#!/usr/bin/env python3
"""Reorder Java imports to Google Checkstyle CustomImportOrder."""

import re
import sys
from pathlib import Path

SKIP_FILES = {"UserInterface.java", "Main.java"}


def fix_imports(content: str) -> str:
    lines = content.splitlines(keepends=True)
    pkg_end = 0
    for i, line in enumerate(lines):
        if line.startswith("package "):
            pkg_end = i + 1
            break

    import_start = None
    import_end = None
    for i in range(pkg_end, len(lines)):
        stripped = lines[i].strip()
        if stripped.startswith("import "):
            if import_start is None:
                import_start = i
            import_end = i + 1
        elif import_start is not None and stripped and not stripped.startswith("//"):
            break

    if import_start is None:
        return content

    static_imports = []
    regular_imports = []
    for i in range(import_start, import_end):
        line = lines[i].rstrip("\n")
        if not line.strip():
            continue
        if line.strip().startswith("import static "):
            static_imports.append(line)
        elif line.strip().startswith("import "):
            regular_imports.append(line)

    static_imports.sort(key=lambda s: s.strip())
    regular_imports.sort(key=lambda s: s.strip())

    new_import_block = []
    if static_imports:
        new_import_block.extend(s + "\n" for s in static_imports)
        new_import_block.append("\n")
    if regular_imports:
        new_import_block.extend(s + "\n" for s in regular_imports)
    if new_import_block and new_import_block[-1] != "\n":
        new_import_block.append("\n")
    elif new_import_block and new_import_block[-1] == "\n" and not regular_imports:
        pass
    if new_import_block and new_import_block[-1] == "\n":
        # ensure single blank line before class/javadoc
        pass

    # Remove duplicate blank lines at end of import block
    while len(new_import_block) > 1 and new_import_block[-1] == "\n" and new_import_block[-2] == "\n":
        new_import_block.pop()

    if new_import_block and new_import_block[-1] != "\n":
        new_import_block.append("\n")

    before = lines[:import_start]
    after = lines[import_end:]

    # Normalize: one blank line after package if imports follow
    result = []
    result.extend(before)
    if result and result[-1].strip() and new_import_block:
        result.append("\n")
    result.extend(new_import_block)
    # Skip leading blank lines in after
    idx = 0
    while idx < len(after) and after[idx].strip() == "":
        idx += 1
    result.extend(after[idx:])
    return "".join(result)


def main() -> int:
    root = Path("src/main/java")
    changed = 0
    for path in sorted(root.rglob("*.java")):
        if path.name in SKIP_FILES:
            continue
        text = path.read_text(encoding="utf-8")
        fixed = fix_imports(text)
        if fixed != text:
            path.write_text(fixed, encoding="utf-8")
            changed += 1
            print(path)
    print(f"Updated {changed} files", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
