#!/usr/bin/env node
"use strict";

const fsp = require("fs").promises;
const path = require("path");

/**
 * generate-necessary.js
 *
 * ✅ Only generate a directory tree for:
 *    - necessaryFolderArr (subtrees)
 *    - necessaryFilesArr  (single files)
 *    - necessaryTypeArr   (match file "type" by suffix, e.g. ".java", "pom.xml")
 * ✅ Output: project-core.md (only one file)
 * ✅ Run: node generate-necessary.js
 *
 * Rules:
 * - All paths are relative to CURRENT directory (project root).
 * - If a necessary folder is provided, the subtree is scanned (skips applied).
 *   - If necessaryTypeArr is NOT empty: only files matching necessaryTypeArr are included.
 *   - If necessaryTypeArr is empty: all files are included.
 * - necessaryFilesArr files are ALWAYS included (even if not matching necessaryTypeArr).
 * - If necessaryFolderArr is empty but necessaryTypeArr is not empty, the script scans the project root
 *   and includes only files matching necessaryTypeArr.
 */


/**
 * Match file "type" by suffix (case-insensitive).
 * Examples:
 *   ["pom.xml"]              -> matches only files ending with "pom.xml"
 *   [".java", ".yml"]        -> matches Java + YAML
 *   ["application.yml"]      -> matches only files ending with "application.yml"
 */
const necessaryTypeArr = [
    "pom.xml",
    // ".java",
];

/** =======================
 * REQUIRED INPUTS (edit me)
 * ======================= */
const necessaryFolderArr = [
    "api-common/",
    // "api-framework/src/main/java/com/api/framework/config/email",
];

const necessaryFilesArr = [
    // "pom.xml",
    // "api-boot/pom.xml",
];


/** =======================
 * OPTIONAL SKIPS (by NAME)
 * Skips apply INSIDE the scanned subtrees
 * ======================= */
const skipFolderArr = [
    ".idea",
    ".mvn",
    "doc",
    "httpRequests",
    "node_modules",
    ".git",
    "dist",
    "target",
];

const skipFilesArr = [
    "project-core.md",
    "generate-necessary.js",
    "generate.js",
    "project-tree.md",
    "project-content.md",
    ".DS_Store",
];

// Output file name (only one file)
const OUT_FILE = "project-core.md";

async function main() {
    const root = process.cwd();
    const rootName = path.basename(root);

    const rootNode = new TreeNode(rootName, true);

    const hasNecessaryFolders = Array.isArray(necessaryFolderArr) && necessaryFolderArr.length > 0;
    const hasNecessaryFiles = Array.isArray(necessaryFilesArr) && necessaryFilesArr.length > 0;
    const hasNecessaryTypes = Array.isArray(necessaryTypeArr) && necessaryTypeArr.length > 0;

    if (!hasNecessaryFolders && !hasNecessaryFiles && !hasNecessaryTypes) {
        const lines = [
            `${rootName}/`,
            "",
            "  [necessaryFolderArr, necessaryFilesArr, and necessaryTypeArr are empty. Please add paths/types in generate-necessary.js]",
        ];
        await writeOutput(path.join(root, OUT_FILE), lines.join("\n") + "\n");
        console.log("Generated:");
        console.log("  " + path.join(root, OUT_FILE));
        return;
    }

    const missing = [];
    const invalidType = [];

    // 1) Process necessary folders (include subtree, filtered by necessaryTypeArr if provided)
    for (const item of necessaryFolderArr || []) {
        const rel = normalizeRel(item);
        if (!rel) continue;

        const abs = path.resolve(root, rel);

        let stat;
        try {
            stat = await fsp.lstat(abs);
        } catch {
            missing.push(rel);
            continue;
        }

        if (!stat.isDirectory()) {
            invalidType.push(`${rel} (expected directory)`);
            continue;
        }

        addPath(rootNode, rel, true);
        await walkDirIntoTree(abs, rel, rootNode, hasNecessaryTypes);
    }

    // 2) Process necessary types at root if no necessary folders provided
    //    (so necessaryTypeArr can work alone)
    if (!hasNecessaryFolders && hasNecessaryTypes) {
        addPath(rootNode, ".", true);
        await walkDirIntoTree(root, ".", rootNode, true);
    }

    // 3) Process necessary files (always include)
    for (const item of necessaryFilesArr || []) {
        const rel = normalizeRel(item);
        if (!rel) continue;

        const abs = path.resolve(root, rel);

        let stat;
        try {
            stat = await fsp.lstat(abs);
        } catch {
            missing.push(rel);
            continue;
        }

        if (stat.isDirectory()) {
            invalidType.push(`${rel} (expected file)`);
            continue;
        }

        addPath(rootNode, rel, false);
    }

    // Print tree to project-core.md
    const lines = [];
    lines.push(`${rootName}/`);
    printTree(rootNode, "", lines);

    if (missing.length > 0) {
        lines.push("");
        lines.push("[Missing paths]");
        missing.forEach((p) => lines.push("  - " + p));
    }

    if (invalidType.length > 0) {
        lines.push("");
        lines.push("[Invalid path type]");
        invalidType.forEach((p) => lines.push("  - " + p));
    }

    await writeOutput(path.join(root, OUT_FILE), lines.join("\n") + "\n");

    console.log("Generated:");
    console.log("  " + path.join(root, OUT_FILE));
}

/**
 * Walk a directory and add all entries into the tree node.
 * If filterByType=true, only include files matching necessaryTypeArr.
 *
 * @param {string} dirAbs  - absolute directory to walk
 * @param {string} baseRel - directory path relative to project root
 * @param {TreeNode} rootNode
 * @param {boolean} filterByType
 */
async function walkDirIntoTree(dirAbs, baseRel, rootNode, filterByType) {
    let entries = await fsp.readdir(dirAbs, {withFileTypes: true});

    entries = entries
        .filter((e) => {
            if (e.isDirectory()) return !skipFolderArr.includes(e.name);
            return !skipFilesArr.includes(e.name);
        })
        .sort((a, b) => {
            // dirs first, then files; then name
            if (a.isDirectory() !== b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.name.localeCompare(b.name);
        });

    for (const entry of entries) {
        const childAbs = path.join(dirAbs, entry.name);
        const childRel = normalizeRel(`${toPosix(baseRel)}/${entry.name}`);

        if (entry.isDirectory()) {
            // Always traverse directories (so we can find matching files deeper)
            // But we only add it to tree if it ultimately contributes children.
            // Simpler approach: add directory now (may show empty dirs). If you want "prune empty dirs",
            // tell me and I'll adjust.
            addPath(rootNode, childRel, true);
            await walkDirIntoTree(childAbs, childRel, rootNode, filterByType);
            continue;
        }

        if (filterByType && !matchesNecessaryType(entry.name)) {
            continue;
        }

        addPath(rootNode, childRel, false);
    }
}

function matchesNecessaryType(fileName) {
    if (!Array.isArray(necessaryTypeArr) || necessaryTypeArr.length === 0) return true;

    const lower = String(fileName).toLowerCase();
    return necessaryTypeArr
        .map((x) => String(x).trim().toLowerCase())
        .filter((x) => x.length > 0)
        .some((suffix) => lower.endsWith(suffix));
}

/** ===== Tree Structure ===== */
class TreeNode {
    constructor(name, isDir) {
        this.name = name;
        this.isDir = isDir;
        /** @type {Map<string, TreeNode>} */
        this.children = new Map();
    }
}

/**
 * Add a path (dir or file) into the tree.
 * relPath example: "api-boot/src/main/java"
 */
function addPath(rootNode, relPath, isDir) {
    const normalized = normalizeRel(relPath);
    if (!normalized || normalized === ".") return;

    const parts = normalized.split("/").filter(Boolean);
    let cur = rootNode;

    for (let i = 0; i < parts.length; i++) {
        const part = parts[i];
        const last = i === parts.length - 1;
        const shouldBeDir = last ? isDir : true;

        if (!cur.children.has(part)) {
            cur.children.set(part, new TreeNode(part, shouldBeDir));
        } else {
            const existing = cur.children.get(part);
            if (shouldBeDir && existing && !existing.isDir) {
                existing.isDir = true;
            }
        }

        cur = cur.children.get(part);
    }
}

/**
 * Print pretty tree with connectors.
 */
function printTree(node, prefix, outLines) {
    const children = Array.from(node.children.values()).sort((a, b) => {
        if (a.isDir !== b.isDir) return a.isDir ? -1 : 1;
        return a.name.localeCompare(b.name);
    });

    for (let i = 0; i < children.length; i++) {
        const child = children[i];
        const isLast = i === children.length - 1;

        const connector = isLast ? "└── " : "├── ";
        const display = child.isDir ? `${child.name}/` : child.name;

        outLines.push(prefix + connector + display);

        if (child.isDir) {
            const nextPrefix = prefix + (isLast ? "    " : "│   ");
            printTree(child, nextPrefix, outLines);
        }
    }
}

/**
 * Normalize user input to POSIX relative path (no leading ./ or /, no trailing /).
 */
function normalizeRel(p) {
    if (!p) return "";
    const s = toPosix(String(p).trim());
    return s.replace(/^(\.\/)+/, "").replace(/^\/+/, "").replace(/\/+$/, "");
}

function toPosix(p) {
    return p.split(path.sep).join("/");
}

async function writeOutput(filePath, content) {
    await fsp.writeFile(filePath, content, "utf8");
}

main().catch((err) => {
    console.error("ERROR:", err && err.stack ? err.stack : err);
    process.exit(1);
});
