#!/usr/bin/env node
"use strict";

const fs = require("fs");
const fsp = fs.promises;
const path = require("path");

/**
 * ========= CONFIG =========
 * Skip by NAME (exact match)
 *
 * Based on your project tree, you asked to skip:
 * - doc
 * - httpRequests
 * - .idea
 * - generate.js
 */
const skipFolderArr = [".idea", ".mvn", "doc", "httpRequests", // (already inside .idea, but keep it in case you move it elsewhere)
    "node_modules", ".git", "dist", "target",];

const skipFilesArr = ["project-core.md", "generate-necessary.js", "generate.js", "project-tree.md", "project-content.md", ".DS_Store", "create-env.sh.example",];
// Content safety limits
const MAX_BYTES = 200_000; // skip content if file > 200KB
const MAX_LINES = 2000;    // truncate after N lines

// Simple text extensions heuristic
const TEXT_EXT = new Set([".txt", ".md", ".yml", ".yaml", ".properties", ".json", ".xml", ".html", ".htm", ".css", ".js", ".ts", ".java", ".kt", ".go", ".py", ".sh", ".conf", ".ini", ".sql",]);

async function main() {
    // Always generate for the CURRENT directory
    const root = process.cwd();
    const rootName = path.basename(root);

    const treeLines = [];
    const contentLines = [];

    // Root line in project-tree.md
    treeLines.push(`${rootName}/`);

    await walkTree(root, root, rootName, "", treeLines, contentLines);

    // Output files in current directory (renamed as requested)
    const outTree = path.join(root, "project-tree.md");
    const outContent = path.join(root, "project-content.md");

    await fsp.writeFile(outTree, treeLines.join("\n") + "\n", "utf8");
    await fsp.writeFile(outContent, contentLines.join("\n") + "\n", "utf8");

    console.log("Generated:");
    console.log("  " + outTree);
    console.log("  " + outContent);
}

/**
 * Pretty tree traversal with connectors.
 */
async function walkTree(rootDir, currentDir, rootName, prefix, treeLines, contentLines) {
    let entries = await fsp.readdir(currentDir, {withFileTypes: true});

    // filter skip + sort
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

    for (let i = 0; i < entries.length; i++) {
        const entry = entries[i];
        const isLast = i === entries.length - 1;

        const connector = isLast ? "└── " : "├── ";
        const fullPath = path.join(currentDir, entry.name);

        const displayName = entry.isDirectory() ? `${entry.name}/` : entry.name;
        treeLines.push(prefix + connector + displayName);

        // Only files go into project-content.md
        if (!entry.isDirectory()) {
            const rel = toPosixPath(path.relative(rootDir, fullPath));
            const displayPath = `${rootName}/${rel}`;
            await appendFileContent(contentLines, displayPath, fullPath);
            continue;
        }

        const nextPrefix = prefix + (isLast ? "    " : "│   ");
        await walkTree(rootDir, fullPath, rootName, nextPrefix, treeLines, contentLines);
    }
}

async function appendFileContent(contentLines, displayPath, filePath) {
    contentLines.push(displayPath);

    let stat;
    try {
        stat = await fsp.lstat(filePath);
    } catch {
        contentLines.push("  [content skipped: cannot stat file]");
        contentLines.push("");
        return;
    }

    if (stat.size > MAX_BYTES) {
        contentLines.push(`  [content skipped: file too large ${stat.size} bytes]`);
        contentLines.push("");
        return;
    }

    const looksText = await isLikelyTextFile(filePath);
    if (!looksText) {
        contentLines.push("  [content skipped: not a text file]");
        contentLines.push("");
        return;
    }

    let text;
    try {
        text = await fsp.readFile(filePath, "utf8");
    } catch {
        contentLines.push("  [content skipped: not UTF-8 text]");
        contentLines.push("");
        return;
    }

    const lines = text.split(/\r?\n/);
    const limit = Math.min(lines.length, MAX_LINES);

    for (let i = 0; i < limit; i++) {
        contentLines.push("  " + lines[i]);
    }

    if (lines.length > MAX_LINES) {
        contentLines.push(`  [content truncated after ${MAX_LINES} lines]`);
    }

    contentLines.push("");
}

async function isLikelyTextFile(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    if (TEXT_EXT.has(ext)) return true;

    // Read first 2KB and check for NUL byte
    try {
        const fd = await fsp.open(filePath, "r");
        const buf = Buffer.alloc(2048);
        const {bytesRead} = await fd.read(buf, 0, buf.length, 0);
        await fd.close();

        for (let i = 0; i < bytesRead; i++) {
            if (buf[i] === 0) return false;
        }
        return true;
    } catch {
        return false;
    }
}

function toPosixPath(p) {
    return p.split(path.sep).join("/");
}

main().catch((err) => {
    console.error("ERROR:", err && err.stack ? err.stack : err);
    process.exit(1);
});
