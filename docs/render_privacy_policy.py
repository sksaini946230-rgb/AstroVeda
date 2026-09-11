#!/usr/bin/env python3
"""Render docs/PRIVACY_POLICY.md into the page the app opens from Settings.

The privacy policy lives in two places: this markdown file, which is the URL on
the Play listing, and app/src/main/assets/privacy_policy.html, which the app
shows in a WebView. They were once maintained by hand and by September 2026 had
become two different documents making contradictory claims. Edit the markdown,
then run this from the repository root:

    python3 docs/render_privacy_policy.py

It keeps the HTML file's existing <head> (and so its styling) and regenerates
the body. PrivacyPolicyTest fails if the two copies stop saying the same thing.

Only the markdown this policy uses is understood: #/## headings, paragraphs,
"- " bullet lists with indented continuation lines, **bold**, *italic*, ---
rules (dropped) and bare URLs (linked).
"""
import html
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "docs" / "PRIVACY_POLICY.md"
TARGET = ROOT / "app" / "src" / "main" / "assets" / "privacy_policy.html"
INDENT = " " * 8


def inline(text):
    text = html.escape(text, quote=False)
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
    text = re.sub(r"\*(.+?)\*", r"<em>\1</em>", text)
    return re.sub(r"(https?://[^\s<]+)", r'<a href="\1">\1</a>', text)


def blocks(lines):
    """Yield (kind, text) for each heading, paragraph and bullet list."""
    para, items = [], []

    def flush():
        if para:
            yield ("p", " ".join(para))
            para.clear()
        if items:
            yield ("ul", list(items))
            items.clear()

    for raw in lines:
        line = raw.rstrip()
        stripped = line.strip()
        if not stripped or stripped == "---":
            yield from flush()
        elif stripped.startswith("## "):
            yield from flush()
            yield ("h2", stripped[3:])
        elif stripped.startswith("# "):
            yield from flush()
            yield ("h1", stripped[2:])
        elif line.startswith("- "):
            if para:
                yield from flush()
            items.append(stripped[2:])
        elif items and line.startswith("  "):
            items[-1] += " " + stripped
        else:
            if items:
                yield from flush()
            para.append(stripped)
    yield from flush()


def render_body(markdown):
    out = []
    for kind, text in blocks(markdown.splitlines()):
        if kind == "ul":
            out.append(f"{INDENT}<ul>")
            out.extend(f"{INDENT}    <li>{inline(item)}</li>" for item in text)
            out.append(f"{INDENT}</ul>")
        elif kind == "p" and text.startswith("**Effective"):
            out.append(f'{INDENT}<p class="last-updated">{inline(text)}</p>')
        else:
            out.append(f"{INDENT}<{kind}>{inline(text)}</{kind}>")
    return "\n".join(out)


def main():
    existing = TARGET.read_text(encoding="utf-8")
    if "<body>" not in existing:
        sys.exit(f"{TARGET} has no <body> tag to keep the head from")
    head = existing.split("<body>", 1)[0]
    page = (
        head
        + "<body>\n"
        + "    <!-- Generated from docs/PRIVACY_POLICY.md by docs/render_privacy_policy.py."
        + " Edit the markdown, then re-run it. -->\n"
        + '    <div class="container">\n'
        + render_body(SOURCE.read_text(encoding="utf-8"))
        + "\n    </div>\n</body>\n</html>\n"
    )
    TARGET.write_text(page, encoding="utf-8")
    print(f"wrote {TARGET.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
