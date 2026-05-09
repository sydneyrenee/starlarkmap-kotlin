# Claude Code Project Instructions

## Project Overview

This is **starlarkmap-kotlin**, a line-by-line port of Facebook's `starlark-rust/starlark_map` crate to Kotlin Multiplatform. The Rust sources will live in `tmp/starlark_map/src/` and the Kotlin implementation in `src/commonMain/kotlin/io/github/kotlinmania/starlarkmap/`.

Upstream: <https://github.com/facebook/starlark-rust/tree/main/starlark_map>

## Translator's mindset

This is a translation project, not a software-engineering project. While porting a file, you are
the Kotlin author of the same document a Rust author wrote. Architecture, optimization, design
critique, drift measurement — all later. While translating, the only job is the translation.

The discipline:

1. **Read the whole upstream file before you type.** A line-by-line port composes only when you
   know how the file ends. If the file is too long to read in one sitting, split your turn into
   "read the file" and "write the file" — never start typing on a file you've only half-read.

2. **One Rust file → one Kotlin file. Always.** No splitting one `.rs` across several `.kt`. No
   merging several `.rs` into one `.kt`. The 1:1 mapping is the contract; everything downstream
   (ast_distance, port-lint headers, code review) assumes it. If a `.rs` is genuinely too big for
   one Kotlin file, that's a sign you're in `mod.rs`-equivalent territory and the upstream itself
   is a re-export — verify, don't split.

3. **Translate top to bottom in upstream order.** Preserve the declaration order. Don't reorder
   for "logical flow" — the upstream's order *is* the logical flow. The reader who already knows
   the Rust file should be able to scroll the Kotlin file and find every item in the same place.

4. **Comments are content.** License header, module-level doc, every `///` block, every inline
   `//` note, every upstream `// TODO`/`// FIXME` — all translate. Rust syntax inside doc comments
   gets rewritten to Kotlin equivalents (`Vec<T>` → `List<T>`, `Self::foo()` → `foo()`, lifetimes
   dropped, `cfg(test)` and `#[derive(...)]` lifted into prose). You are translating a *document*,
   not just the code.

5. **When a Rust idiom has no Kotlin analog, apply the mapping rule and move on.** `Box<T>`,
   `Arc<T>`, `Cell<T>`, `RefCell<T>`, `Rc<T>`, lifetimes, `PhantomData`, `mem::forget`,
   `drop_in_place`, `Pin`, `MaybeUninit`, `dyn Trait` — all collapse per the mapping table.
   Don't relitigate. A proc-macro becomes a builder/runtime API, not nothing. An upstream Rust
   crate with no KMP equivalent becomes a *separate Kotlin port*, not a `// TODO` placeholder.
   Pay the snowball cost upfront — the next consumer will thank you.

6. **Don't measure mid-port.** ast_distance, FnSim, similarity reports — useful *after* a file is
   done, useless *during*. Mid-translation measurement is procrastination dressed as rigor. Run
   the tools when a file lands or when a port phase wraps, not while you're choosing between
   `Result<T>` and `T?`.

7. **Don't optimize the translation.** "This Kotlin shape would be simpler" is the wrong
   thought. The upstream shape is the spec. If a faithful translation produces a function that
   takes a parameter you'd never write in Kotlin from scratch, take it. Optimization is a
   separate, named pass after parity is reached — never blended into the translation.

8. **Don't re-architect mid-port.** "This whole module would be cleaner if..." — write the
   thought on a sticky note, throw the sticky note away, finish the file. The current architecture
   is the upstream's architecture. Earn the right to redesign by first reaching parity.

9. **Compile errors during translation are normal and expected.** A bottom-of-tree file compiles
   when its deps are ported, not before. Don't pause to "make it compile" mid-port — that pulls
   you into stub-shaped fixes that you'll have to undo. Climb the dep tree bottom-up; the leaves
   compile first, then their parents, then everything compiles together at the end.

10. **Bottom-up always.** Port dependencies before consumers. If `state.rs` uses `EvalException`,
    port `eval_exception.rs` first. If `eval_exception.rs` uses `Error`/`WithDiagnostic`/`CallStack`,
    port those first. The order isn't optional; trying to port top-down produces a tree of stubs
    that all need replacing.

11. **Hard files are not skippable.** logos-codegen, lalrpop's table generator, an annotate-snippets
    equivalent — when you hit one, port it. Skipping leaves a `// TODO`-shaped hole that grows
    every time another consumer needs it. The snowball is the whole point: each hard port done
    makes the next port easier, because the dep is now in Kotlin.

12. **Warnings are real, but `@Suppress` is never the answer.** `UNUSED_PARAMETER` on a callback
    helper means the function shape doesn't fit Kotlin — restructure the signature, don't suppress.
    `UNCHECKED_CAST` means the type system is missing an invariant — encode it. Every warning is
    either a real bug or a translation choice that needs revisiting; treat them as compile errors.

13. **Stop at file boundaries, not function boundaries.** After every completed file, exhale,
    commit, move on. Don't pause mid-function to second-guess a choice. The whole-file context
    is what makes individual choices coherent.

14. **Doc-port discipline applies even when the upstream doc is awkward.** If the upstream
    author wrote a tortured English sentence in a doc comment, translate the tortured sentence.
    Don't smooth it. Don't paraphrase. Their doc is the contract for the Kotlin doc.

15. **The cheat detector is your friend.** If `ast_distance` forces your file's score to 0
    because you left snake_case identifiers or `pub` keywords in Kotlin comments, take it as a
    literal instruction: rewrite those comments to be Kotlin-native. Rust syntax in Kotlin source
    — code or comments — is the cheat we're catching.

The sticky-note version: **"Read the file. Translate it. Don't think about anything else."**

## Critical Workflows

### 0. No Subagents

**Do not delegate translation work to subagents (Agent / Task tool).** Translation happens in the main loop where Sydney can see each edit and correct course immediately.

### 2. Port-Lint Headers (REQUIRED)

Every Kotlin file MUST start with:

```kotlin
// port-lint: source <path-relative-to-tmp/starlark_map>
package io.github.kotlinmania.starlarkmap.module
```

Example:

```kotlin
// port-lint: source src/small_map.rs
package io.github.kotlinmania.starlarkmap.smallmap
```

### 3. Namespace

All Kotlin code lives under `io.github.kotlinmania.starlarkmap`, mirroring the Rust crate's module tree:

```
io.github.kotlinmania.starlarkmap
io.github.kotlinmania.starlarkmap.smallmap
io.github.kotlinmania.starlarkmap.smallset
io.github.kotlinmania.starlarkmap.sortedmap
io.github.kotlinmania.starlarkmap.sortedset
io.github.kotlinmania.starlarkmap.sortedvec
io.github.kotlinmania.starlarkmap.unorderedmap
io.github.kotlinmania.starlarkmap.unorderedset
io.github.kotlinmania.starlarkmap.vec2
io.github.kotlinmania.starlarkmap.vecmap
```

## Build Commands

```bash
./gradlew build
./gradlew test
./gradlew jvmTest
./gradlew macosArm64Test
```

## Audit: function-placement check

`tools/ast_distance/audit_functions.py` cross-references function names
between every `.rs` and `.kt` file, after pairing them by `// port-lint:
source` header. It's the cheap-and-fast complement to `ast_distance --deep`:
where `ast_distance` scores each pair, the audit asks the inverse question —
"is the function defined in the *right* file?".

Run it from the project root:

```bash
python tools/ast_distance/audit_functions.py \
    tmp/starlark_map/src \
    src/commonMain/kotlin/io/github/kotlinmania/starlarkmap
```

The report has three sections:

- **WRONG-FILE** — a Kotlin file defines a function whose only upstream
  definition lives in a *different* `.rs` file. This is the copy-paste
  drift case (e.g. `Entry::or_insert` from `small_map.rs` getting added to
  `RawEntryMut` in `unordered_map.rs`'s Kotlin port). Per-type derived
  methods (`fmt`, `eq`, `hash`, `default`, `clone`, `drop`, …) are filtered
  by ignoring any name that appears in ≥3 `.rs` files; tune the
  `DERIVE_NOISE_THRESHOLD` constant to widen or narrow the report.
- **UNPORTED** — function names defined in a `.rs` and missing from every
  `.kt` that targets it. This is the "what's left to translate" list per
  file.
- **KT-ONLY** — function names defined in a Kotlin file with no matching
  Rust function anywhere in the source tree. Often legitimate Kotlin idiom
  (`fromIterator`, `hasNext`, `equivalent`); occasionally drift.

Names are normalized so the comparison ignores stylistic difference: Rust
snake_case → camelCase, and the standard Kotlin overrides map to their
Rust analog (`hashCode → hash`, `equals → eq`, `toString → fmt`,
`iterator → intoIter`).

Headers that don't resolve to any `.rs` file are listed at the top of the
report — that surfaces port-lint header drift (camelCase paths, missing
`src/` prefix, etc.) without needing a separate sweep.

## STRICT RULES — Translation, Not Engineering

This is a translation project. Every Kotlin file is a line-by-line port of a Rust source file in `tmp/starlark_map/src/`. The `// port-lint: source` header tracks provenance.

- **No code stubs.** Don't write empty placeholder classes or `TODO()` bodies. A missing file is better than a stub.
- **No translator-note comments.** Don't write `// Kotlin: ...` explanations. Don't put Rust syntax inside Kotlin comments — the cheat detector will zero the score.
- **No `// where T: ...` comments retaining the Rust `where` clause.** Translate `where` clauses into Kotlin's `where` syntax.
- **`mod.rs` files are not ported.** Rewire callers to import from the defining module directly.
- **Typealiases follow Rust 1:1.** If Rust declares `pub type Foo = Bar;` as a semantic alias inside a regular `.rs` file, mirror it in Kotlin. If the alias only exists in `mod.rs` as a re-export, drop it and rewire callers.

### Single-file edit, single commit

After every `Edit` / `Write` to a `.kt` file, immediately `git add <file>` and commit before editing anything else. One file → one commit. No bulk regex passes across multiple files.

## Maven Coordinates

```
io.github.kotlinmania:starlarkmap:<version>
```

## Commit Messages

Sydney's style: no AI branding, no Co-Authored-By lines, no emoji. Clear, descriptive messages focused on what changed and why.

## Re-exports from upstream `mod.rs` files

When an upstream Rust `mod.rs` is **only re-exporting** something that actually lives elsewhere
(`pub use <crate-path>::<Name>;`, often under a different name), do **not** preserve that
re-export shape in Kotlin as a "central alias" API. Do not write a `typealias` for the
re-exported name. The existing `Forbidden` rule against "Re-export typealias files at root
packages" is enforced through this procedure.

Workflow:

1. **Identify what the `mod.rs` is re-exporting and the name it's exported as.** Record both
   the original symbol's fully-qualified upstream path and the (possibly different) re-export
   name.

2. **Find callers across the kotlinmania monorepo.** A caller is any Kotlin file in another
   `*-kotlin` repo that has both a `tmp/` folder and a Cargo.toml depending on the Rust
   counterpart of *this* crate, where the file references the re-exported name. Search for:
   - direct imports: `import <reexport-package>.<Name>`
   - wildcard imports of the re-export package, when `<Name>` is used in the file body
   - fully-qualified inline references

3. **Rewrite each caller to reference the upstream/original symbol directly.** If the caller
   still needs to write `<Name>` unchanged, use Kotlin aliasing:
   `import <upstream-fully-qualified-name> as <Name>`. Never bridge with a Kotlin `typealias`.

4. **Keep `Mod.kt` (or the equivalent file for that package) as a tracking file.** It carries
   the translated upstream module-level comments and a literal-quoted reference to each upstream
   `pub use` line (e.g. `// pub use crate::lib::result::Result;`). Each time a caller is migrated
   off the re-export, append the caller's absolute path under a `// Callers migrated:` ledger in
   `Mod.kt`. Append, never delete. Once all callers are migrated, the `typealias` (if any) is
   removed; the tracking file remains as the ledger of the migration.

Reference example: [/Volumes/stuff/Projects/kotlinmania/serde-kotlin/tmp/serde/serde_core/src/private/mod.rs](/Volumes/stuff/Projects/kotlinmania/serde-kotlin/tmp/serde/serde_core/src/private/mod.rs)
re-exports `Result` from `crate::lib::result`. The Kotlin tracking file lives at
[/Volumes/stuff/Projects/kotlinmania/serde-kotlin/src/commonMain/kotlin/io/github/kotlinmania/serde/core/private/Mod.kt](/Volumes/stuff/Projects/kotlinmania/serde-kotlin/src/commonMain/kotlin/io/github/kotlinmania/serde/core/private/Mod.kt).
A caller that previously did `import io.github.kotlinmania.serde.core.private.Result` is
rewritten to `import kotlin.Result as Result` (or just removes the import and relies on the
auto-imported `kotlin.Result`).
