#!/usr/bin/env python3
import argparse
import json
import sys
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

CATALOG = Path(__file__).resolve().parent.parent / "hub" / "catalog.v1.json"
HF_API = "https://huggingface.co/api/models"
MIN_RAM_BYTES = 3_200_000_000
RUNTIME_BYTES = 2_800_000_000
USER_AGENT = "MishtiHubTool/1"


def get_json(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def file_details(repo, filename):
    quoted_repo = urllib.parse.quote(repo, safe="/")
    tree = get_json(f"{HF_API}/{quoted_repo}/tree/main?recursive=true")
    for entry in tree:
        if entry.get("path") == filename and entry.get("lfs"):
            return entry["size"], entry["lfs"]["oid"]
    raise SystemExit(f"{filename} was not found in {repo}")


def gguf_details(repo):
    quoted_repo = urllib.parse.quote(repo, safe="/")
    info = get_json(f"{HF_API}/{quoted_repo}?expand[]=gguf&expand[]=cardData")
    gguf = info.get("gguf") or {}
    card = info.get("cardData") or {}
    return gguf.get("total"), gguf.get("context_length"), gguf.get("architecture"), card.get("license")


def estimated_ram(size):
    return max(MIN_RAM_BYTES, round(size * 1.5) + RUNTIME_BYTES)


def load():
    return json.loads(CATALOG.read_text())


def save(catalog):
    stamp = datetime.now(timezone.utc)
    previous = catalog.get("catalog_version", "")
    day = stamp.strftime("%Y-%m-%d")
    serial = int(previous.split(".")[-1]) + 1 if previous.startswith(day) else 1
    catalog["catalog_version"] = f"{day}.{serial}"
    catalog["updated_at"] = stamp.strftime("%Y-%m-%dT%H:%M:%SZ")
    CATALOG.write_text(json.dumps(catalog, indent=2, ensure_ascii=False) + "\n")
    print(f"Wrote {CATALOG} ({catalog['catalog_version']}, {len(catalog['models'])} models)")


def add(args):
    catalog = load()
    if any(model["id"] == args.id for model in catalog["models"]):
        raise SystemExit(f"{args.id} is already in the catalog")
    size, sha = file_details(args.repo, args.file)
    parameters, context, architecture, license_name = gguf_details(args.repo)
    entry = {
        "id": args.id,
        "name": args.name,
        "description": args.description,
        "publisher": args.publisher or args.repo.split("/")[0],
        "parameters": parameters,
        "quantization": args.quant,
        "architecture": architecture,
        "hf_repo": args.repo,
        "hf_file": args.file,
        "size_bytes": size,
        "sha256": sha,
        "min_ram_bytes": args.min_ram or estimated_ram(size),
        "context_length": context,
        "license": args.license or license_name,
        "tags": [tag for tag in args.tags.split(",") if tag],
    }
    catalog["models"].append({key: value for key, value in entry.items() if value is not None})
    save(catalog)


def validate(_args):
    catalog = load()
    problems = 0
    seen = set()
    for model in catalog["models"]:
        model_id = model["id"]
        if model_id in seen:
            print(f"{model_id}: duplicate id")
            problems += 1
        seen.add(model_id)
        try:
            size, sha = file_details(model["hf_repo"], model["hf_file"])
        except Exception as error:
            print(f"{model_id}: {error}")
            problems += 1
            continue
        if size != model["size_bytes"]:
            print(f"{model_id}: size is {size}, catalog says {model['size_bytes']}")
            problems += 1
        if sha != model["sha256"]:
            print(f"{model_id}: sha256 is {sha}, catalog says {model['sha256']}")
            problems += 1
        print(f"{model_id}: ok" if size == model["size_bytes"] and sha == model["sha256"] else f"{model_id}: CHANGED")
    if problems:
        raise SystemExit(f"{problems} problem(s) found")
    print("Catalog is valid")


def main():
    parser = argparse.ArgumentParser(prog="hub.py")
    commands = parser.add_subparsers(required=True)

    add_parser = commands.add_parser("add")
    add_parser.add_argument("--id", required=True)
    add_parser.add_argument("--name", required=True)
    add_parser.add_argument("--repo", required=True)
    add_parser.add_argument("--file", required=True)
    add_parser.add_argument("--quant", required=True)
    add_parser.add_argument("--description", required=True)
    add_parser.add_argument("--publisher")
    add_parser.add_argument("--license")
    add_parser.add_argument("--tags", default="")
    add_parser.add_argument("--min-ram", type=int)
    add_parser.set_defaults(run=add)

    validate_parser = commands.add_parser("validate")
    validate_parser.set_defaults(run=validate)

    args = parser.parse_args()
    args.run(args)


if __name__ == "__main__":
    sys.exit(main())
