# CycloneDX SBOM Generators and Plugins by Language

## Project Integration Notes (build.env.intel)

- This project auto-detects build systems and invokes the correct CycloneDX plugin/CLI for each language.
- SBOM generation is always JSON (CycloneDX v1.5+ recommended).
- For multi-language/polyglot repos, all detected build systems are processed in a single run.
- Output directory defaults to `generated-sboms/` (configurable via `--output`).
- SBOM merging (for polyglot projects) is supported via the `--merge` flag.
- All SBOM commands are executed in the directory where the build file exists (not repo root).
- For .NET, wildcard project file detection is supported (e.g., `*.csproj`).
- If no build system is detected, standalone JARs are scanned with Syft (requires Syft installed).
- All commands and results are logged as JSON for traceability.

# CycloneDX SBOM Generators and Plugins by Language

This table lists the primary CycloneDX-supported tools for generating Software Bill of Materials (SBOMs) across major programming languages and ecosystems.

| Language / Ecosystem | Tool / Plugin | Latest Version & Schema Support | Notes on Integration & CI/CD Usage |
|----------------------|--------------|---------------------------------|------------------------------------|
| **Java (Maven)** | [`org.cyclonedx:cyclonedx-maven-plugin`](https://github.com/CycloneDX/cyclonedx-maven-plugin) | Version ~2.9.x; supports CycloneDX schema v1.5+ | Add plugin to `pom.xml`. Common goals: `makeBom`, `makeAggregateBom`. Integrate into Maven build lifecycle (e.g., at `package`). Produces JSON or XML SBOMs. |
| **Java (Gradle)** | [`org.cyclonedx.bom` Gradle plugin](https://plugins.gradle.org/plugin/org.cyclonedx.bom) | Version ~3.0.1 (supports JSON + XML) | Apply via `plugins { id("org.cyclonedx.bom") version "3.0.1" }`. Run task `cyclonedxBom`. Suitable for Gradle or Kotlin builds. |
| **Go (Modules)** | [`cyclonedx-gomod`](https://github.com/CycloneDX/cyclonedx-gomod) | Supports CycloneDX spec v1.6 | CLI tool for Go modules. Install via `go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@latest`. Use modes: `app`, `mod`, or `bin`. Ideal for Go CI/CD integration. |
| **Go (Library)** | [`cyclonedx-go`](https://github.com/CycloneDX/cyclonedx-go) | Supports multiple spec versions | Go library for consuming and producing SBOMs. Useful for embedding CycloneDX generation in custom Go tools. |
| **Python** | [`cyclonedx-bom`](https://github.com/CycloneDX/cyclonedx-python) | Supports venv, Poetry, Pipenv, requirements.txt | Install via `pip install cyclonedx-bom`. Run `cyclonedx-py` or `cyclonedx-py --format json`. Integrates easily into Python build pipelines. |
| **JavaScript / Node.js / Webpack** | [`cyclonedx-webpack-plugin`](https://github.com/CycloneDX/cyclonedx-webpack-plugin) | Compatible with CycloneDX spec v1.5+ | Generates SBOMs for bundled JS/TS projects. Add to webpack config; emits CycloneDX JSON file. |
| **.NET / C#** | [`cyclonedx-dotnet`](https://github.com/CycloneDX/cyclonedx-dotnet) | Supports CycloneDX spec v1.4+ | Install via `dotnet tool install --global CycloneDX`. Generates BOM from .NET project or solution. |
| **Rust** | [`cyclonedx-rust`](https://github.com/CycloneDX/cyclonedx-rust) | Supports v1.5+ | Generates SBOMs from Cargo dependencies. |
| **PHP** | [`cyclonedx-php-composer`](https://github.com/CycloneDX/cyclonedx-php-composer) | Supports v1.5+ | Integrates with Composer projects to output SBOMs. |
| **Ruby** | [`cyclonedx-ruby-gem`](https://github.com/CycloneDX/cyclonedx-ruby-gem) | Supports v1.4+ | Creates SBOMs from Gemfile/Gemfile.lock. |
| **C/C++** | [`cyclonedx-cxx`](https://github.com/CycloneDX/cyclonedx-cxx) | Supports v1.5 | SBOM generator for C/C++ projects using compilation databases. |
| **Multi-Ecosystem / CLI** | [`cyclonedx-cli`](https://github.com/CycloneDX/cyclonedx-cli) | Supports conversion, validation, merging, diffing | Multi-purpose CLI for validating or transforming SBOMs. Works across all supported ecosystems. |

---

## Recommended CI/CD Integration Practices

## Troubleshooting Common Errors

- **Syntax error on token(s), misplaced construct(s):**
	- This usually means a misplaced or extra curly brace (`}`) or code outside a class in a Java file.
	- Ensure all methods and fields are inside the correct class (e.g., `EnvScannerCommand`).
	- The license block at the top of Java files must be inside a `/* ... */` comment, immediately followed by the `package` declaration.
	- No blank lines or stray characters should appear between the license comment and `package`.
- **SBOM not generated:**
	- Check that the required CycloneDX plugin/CLI is installed and available in your PATH.
	- For standalone JARs, Syft must be installed separately: https://github.com/anchore/syft
- **Output directory not created:**
	- The tool will attempt to create the output directory if it does not exist, but check permissions if this fails.


- **Generate SBOMs as part of the build stage** – integrate plugin or CLI invocation into your pipeline (e.g., Maven `package`, Gradle `build`, Go `build`, Python `install`).
- **Use consistent SBOM formats and schema versions** (preferably CycloneDX v1.5 or newer).
- **Archive and version SBOMs** (e.g., `app-name-version-cyclonedx.json`) as build artifacts.
- **Feed SBOMs into downstream tools** for vulnerability scanning, license compliance, and governance.
- **Automate validation** with `cyclonedx-cli validate --input-file bom.json`.
- **Include metadata** (commit hash, build time, tool version) for traceability.

---

## References

- CycloneDX Tool Center: [https://cyclonedx.org/tool-center/](https://cyclonedx.org/tool-center/)
- CycloneDX Specification: [https://cyclonedx.org/specification/](https://cyclonedx.org/specification/)
- Official GitHub Organization: [https://github.com/CycloneDX](https://github.com/CycloneDX)
