# build.env.intel

A powerful Quarkus-based CLI tool for intelligently analyzing build environments and generating Software Bill of Materials (SBOM) with automatic build system detection.

## Features

- **Build Environment Scanning**: Automatically detects build tools (Maven, Gradle, npm, Python, Go), their versions, and multi-module configurations
- **Intelligent SBOM Generation**: Automatically selects and executes the appropriate CycloneDX plugin based on detected build system
- **File Type Analysis**: Analyzes source code files with percentage breakdowns and rankings
- **Multi-Module Detection**: Identifies Maven and Gradle multi-module projects
- **Cross-Platform**: Works on Windows, Linux, and macOS
- **Native Image Support**: Optimized for GraalVM native compilation for instant startup
- **JSON Export**: Machine-readable output for integration with other tools

## Prerequisites

- **Java 21** (LTS) or later
- **Maven 3.9+** or use the included Maven wrapper (`mvnw`)
- For native builds: **GraalVM 21+** with native-image installed

## Installation

### Clone and Build

```bash
git clone https://github.com/hoggmania/build.env.intel.git
cd build.env.intel

# Build JVM version
./mvnw clean package

# Build native executable (optional, requires GraalVM)
./mvnw clean package -Dnative
```

## Usage

### Scan Command

The `scan` command analyzes your build environment and source code:

```bash
# JVM mode (default output: scan-results.json)
java -jar target/quarkus-app/quarkus-run.jar scan

# Native executable
./target/build.env.intel-1.0.0-SNAPSHOT-runner scan

# Specify custom JSON output file
java -jar target/quarkus-app/quarkus-run.jar scan --output custom-scan.json
```

**Output:**

- **Console Output**: Always displays formatted results to stdout
- **JSON File**: Always generates a JSON file (default: `scan-results.json`)

**Information Included:**

- Detected build tools with versions (Maven, Gradle, npm, Python, Go)
- Multi-module build detection
- Source file type distribution with percentages
- Ranked file types by count

**Example Output:**

```json
Build Environment Intelligence Scanner
=====================================

Scanned directory: D:\dev\github\my-project

Multi-Module Builds:
   Maven: true

Tool Versions:
   Maven: true
      Apache Maven 3.9.6
   Gradle: false
      Not detected
   npm: true
      10.2.4
   Python: false
      Not detected
   Go: false
      Not detected

File Type Counts:
   .java: 147 (68.37%)
   .js: 42 (19.53%)
   .xml: 18 (8.37%)
   .properties: 8 (3.72%)

[SUCCESS] JSON results written to D:\dev\github\my-project\scan-results.json
```

### SBOM Command

The `sbom` command generates a Software Bill of Materials using the appropriate CycloneDX plugin:

```bash
# Auto-detect and generate SBOM (outputs to generated-sboms/ directory in JSON format)
java -jar target/quarkus-app/quarkus-run.jar sbom

# Specify output directory
java -jar target/quarkus-app/quarkus-run.jar sbom --output ./custom-sbom-dir

# Generate and merge multiple SBOMs (for multi-language projects)
java -jar target/quarkus-app/quarkus-run.jar sbom --merge

# Dry run (show what would be executed)
java -jar target/quarkus-app/quarkus-run.jar sbom --dry-run
```

**Output:**

- **Console Output**: Always displays build system detection, command execution, and generation status
- **SBOM Files**: Generated in the specified output directory (default: `generated-sboms/`)
- **Summary JSON**: Always creates `sbom-summary.json` with metadata about the generation process

**Key Features:**

- **Auto-detection**: Automatically identifies build system and selects appropriate CycloneDX plugin
- **JSON Format**: All SBOMs are generated in JSON format for maximum compatibility
- **Project-based naming**: SBOM files are automatically named using the project name extracted from build files (e.g., `build.env.intel-bom.json`, `my-app-bom.json`)
- **SBOM Merging**: Use `--merge` flag to combine multiple SBOMs into a single `merged-bom.json` (useful for polyglot projects)
- **Default output**: All SBOMs are generated in the `generated-sboms/` directory at the project root
- **JSON Summary**: Includes timestamp, build system, project name, working directory, command executed, and list of generated files

**Project Name Detection:**

- **Maven**: Extracted from `<artifactId>` in `pom.xml`
- **Gradle**: Extracted from `rootProject.name` in `settings.gradle` or directory name
- **npm**: Extracted from `"name"` field in `package.json`
- **Python**: Extracted from `name=` in `setup.py` or `pyproject.toml`
- **Go**: Extracted from module name in `go.mod`
- **.NET**: Extracted from `<AssemblyName>` or project filename
- **Rust**: Extracted from `name` in `Cargo.toml`
- **PHP**: Extracted from `"name"` in `composer.json`
- **Ruby**: Extracted from `.gemspec` filename or directory name

**Supported Build Systems:**

| Build System | Detection File | CycloneDX Tool | Command |
|--------------|---------------|----------------|---------|
| Maven | `pom.xml` | cyclonedx-maven-plugin | `mvn cyclonedx:makeAggregateBom` |
| Gradle | `build.gradle(.kts)` | cyclonedxBom task | `gradle cyclonedxBom` |
| npm | `package.json` | @cyclonedx/cyclonedx-npm | `npx @cyclonedx/cyclonedx-npm` |
| Python | `setup.py`, `pyproject.toml`, `requirements.txt` | cyclonedx-py | `cyclonedx-py` |
| Go | `go.mod` | cyclonedx-gomod | `cyclonedx-gomod app` |
| .NET | `*.csproj`, `*.vbproj`, `*.fsproj`, `*.sln` | cyclonedx-dotnet | `dotnet CycloneDX` |
| Rust | `Cargo.toml` | cyclonedx-rust | `cargo cyclonedx` |
| PHP | `composer.json` | cyclonedx-php-composer | `composer make-bom` |
| Ruby | `Gemfile` | cyclonedx-ruby-gem | `cyclonedx-ruby` |

For detailed information about CycloneDX plugins for each language, see [cyclonedx_plugins_by_language.md](cyclonedx_plugins_by_language.md).

**SBOM Features:**

- Automatic multi-module aggregation for Maven and Gradle
- Configurable output format (JSON/XML)
- Version detection and reporting
- Dry-run mode to preview commands
- **Multi-SBOM merging**: Automatically merges SBOMs from different build systems into a single consolidated SBOM
- **Intelligent naming**: Uses actual project names instead of generic prefixes (e.g., `quarkus-app-bom.json` instead of `maven-bom.json`)

## Use Cases

### CI/CD Integration

The tool includes GitHub Actions workflows for automated builds:

```yaml
# .github/workflows/ci.yml includes:
- JVM build: Creates uber-jar artifact
- Native build: Creates GraalVM native executable
- Artifact retention: 7 days
```

### Security Auditing

Generate SBOMs for security compliance and vulnerability scanning:

```bash
# Generate SBOM and pipe to vulnerability scanner
java -jar target/quarkus-app/quarkus-run.jar sbom --output ./reports
grype sbom:./reports/*-bom.json
```

### Project Analysis

Analyze legacy projects to understand technology stack:

```bash
# Scan unknown project
cd /path/to/legacy-project
java -jar /path/to/build.env.intel.jar scan --json analysis.json

# Review detected tools and file distribution
cat analysis.json
```

## Development

### Running in Dev Mode

```bash
./mvnw quarkus:dev
```

In dev mode, you can test commands directly:

```bash
# In another terminal
curl http://localhost:8080 # (if web endpoints added)

# Or use the CLI directly
./mvnw quarkus:dev -Dquarkus.args="scan"
./mvnw quarkus:dev -Dquarkus.args="sbom --dry-run"
```

### Building

```bash
# JVM package (uber-jar)
./mvnw clean package -Dquarkus.package.type=uber-jar

# Native executable (requires GraalVM)
./mvnw clean package -Dnative

# Native in container (no local GraalVM needed)
./mvnw clean package -Dnative -Dquarkus.native.container-build=true
```

## Configuration

Application configuration in `src/main/resources/application.properties`:

```properties
quarkus.package.type=uber-jar
quarkus.banner.enabled=false
```

## JSON Output Schema

The `scan` command with `--json` produces output like:

```json
{
  "buildTools": {
    "maven": {
      "detected": true,
      "versionInfo": "Apache Maven 3.9.6\n..."
    },
    "gradle": {
      "detected": false,
      "versionInfo": ""
    }
  },
  "multiModuleBuilds": {
    "maven": true,
    "gradle": false
  },
  "sourceFiles": {
    ".java": {
      "count": 147,
      "percentage": 68.37
    }
  }
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Created by **hoggmania** (<hoggmania@gamil.com>)

---

**Built with Quarkus** - Supersonic Subatomic Java Framework
