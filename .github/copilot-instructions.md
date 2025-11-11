# Copilot Instructions for build.env.intel

## Project Overview

A Quarkus 3.29.1 CLI tool for build environment analysis and SBOM generation. Built with Picocli, targets Java 21 LTS, supports GraalVM native compilation. Two main commands: `scan` (environment analysis) and `sbom` (CycloneDX SBOM generation for 9 languages).

## Architecture

### Command Structure (Picocli)
- **Entry Point**: `EnvScannerCommand` is the `@TopCommand` with `@QuarkusMain`
- **Subcommand**: `SbomCommand` registered via `@Command(subcommands = {SbomCommand.class})`
- **Critical Pattern**: Commands MUST implement `Runnable` with `void run()` - NOT `Callable<Integer>` or method-based approaches
- **Execution**: Quarkus runtime automatically executes the `run()` method

### Cross-Platform Command Execution
**Windows-specific critical pattern**:
```java
List<String> command = new ArrayList<>();
if (System.getProperty("os.name").toLowerCase().contains("win")) {
    command.add("cmd.exe");
    command.add("/c");
} else {
    command.add("sh");
    command.add("-c");
}
command.add(actualCommand);
ProcessBuilder pb = new ProcessBuilder(command);
```
Always wrap shell commands with `cmd.exe /c` on Windows or you'll get "CreateProcess error=2".

### GraalVM Native Image Support
**Reflection Registration Required**: Any class serialized to JSON needs `@RegisterForReflection` with JavaBean getters/setters:
```java
@RegisterForReflection
static class BuildSystemInfo {
    private String buildSystem;
    public String getBuildSystem() { return buildSystem; }
    public void setBuildSystem(String buildSystem) { this.buildSystem = buildSystem; }
}
```
Affects: `BuildSystemInfo`, `ToolVersionInfo`, `FileTypeInfo`

### JSON Output Pattern
Both commands ALWAYS generate JSON (no optional flags):
- **scan**: Prints to console + writes `scan-results.json` (or `--output` file)
- **sbom**: Prints to console + writes `sbom-summary.json` + actual SBOM files for ALL detected build systems

## Build System Detection Flow

### Multi-System Detection (SbomCommand)
**Critical**: `detectAllBuildSystems()` returns `List<BuildSystemInfo>` - detects ALL build systems, not just the first one
- Checks for all 9 build systems: Maven → Gradle → npm → Python → Go → .NET → Rust → PHP → Ruby
- Generates SBOMs for each detected system in a single run
- Automatically merges if multiple systems found (polyglot projects)

### Project Name Extraction Pattern
Each build system has dedicated extraction logic:
- **Maven**: Parse `<artifactId>` from `pom.xml`
- **Gradle**: Parse `rootProject.name` from `settings.gradle(.kts)`, fallback to directory
- **npm/PHP**: Parse `"name"` field from JSON files
- **Python**: Parse `name=` from `setup.py` or TOML
- **Go**: Extract from `module` directive in `go.mod`
- **.NET**: Parse `<AssemblyName>` from `*.csproj`, fallback to filename
- **Rust**: Parse `[package] name` from `Cargo.toml`
- **Ruby**: Extract from `*.gemspec` filename

### Working Directory Strategy
**Critical for SBOM generation**: Commands MUST execute where build files exist:
```java
info.workingDirectory = primaryBuildFile.getParent(); // NOT rootDir
pb.directory(workingDirectory.toFile()); // Set ProcessBuilder directory
```

## Developer Workflows

### Build Commands
```bash
# JVM uber-jar (standard)
./mvnw clean package -Dquarkus.package.type=uber-jar

# Native build~
cmd /c "call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvars64.bat" && mvn clean package -Pnative"

# Native in Docker (no local GraalVM needed)
./mvnw clean package -Dnative -Dquarkus.native.container-build=true
```

### Testing Commands
```bash
# Run JVM build
java -jar target/quarkus-app/quarkus-run.jar scan
java -jar target/quarkus-app/quarkus-run.jar sbom --dry-run

# Dev mode with arguments
./mvnw quarkus:dev -Dquarkus.args="scan"
./mvnw quarkus:dev -Dquarkus.args="sbom --merge"
```

### CI/CD (.github/workflows/ci.yml)
Two parallel jobs:
1. **build**: JVM uber-jar on Temurin JDK 21, uploads `*-runner.jar`
2. **build-native**: GraalVM Community 21, container-build native image, uploads `*-runner`

## Project Conventions

### File Organization
- **SBOM Output**: Always defaults to `generated-sboms/` directory (configurable via `--output`)
- **Scan Results**: Defaults to `scan-results.json` in current directory
- **Summary File**: Always `sbom-summary.json` in output directory

### Multi-Module Detection
**Maven**: Checks for `<modules>` or `<module>` tags in any `pom.xml`
**Gradle**: Checks for `include(` or `include ` in `settings.gradle(.kts)`

### SBOM Merging
`--merge` flag combines multiple SBOMs (polyglot projects) into `merged-bom.json`

### File Pattern Matching
**Wildcard support for .NET**: Use `findFilesByPattern()` for patterns like `*.csproj`:
```java
String regex = pattern.replace("*", ".*").replace("?", ".");
Files.find(root, Integer.MAX_VALUE, (p, attr) -> p.getFileName().toString().matches(regex))
```

## CycloneDX Integration

### Plugin Commands by Language
Reference `cyclonedx_plugins_by_language.md` for official documentation. Key patterns:
- **Maven/Gradle**: Use project properties for output dir (always JSON format)
- **npm/Python/Go**: CLI tools with `--output-file` or similar flags (JSON format)
- **.NET**: `dotnet CycloneDX --json -o <outputDir>`
- **Rust**: `cargo cyclonedx --format json`
- **PHP**: `composer make-bom --spec-version=1.4 --output-format=json`
- **Ruby**: `cyclonedx-ruby -o <outputFile> -t json`

### Command Construction Pattern
Each build system has dedicated `get{BuildSystem}SbomCommand(String projectName)` method:
```java
private String getMavenSbomCommand(String projectName) {
    return String.format("mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom " +
        "-DoutputFormat=json -DoutputDirectory=%s -DoutputName=%s-bom",
        outputDir.getAbsolutePath(), projectName);
}
```

## Common Pitfalls

1. **ProcessBuilder on Windows**: Always use `cmd.exe /c` wrapper
2. **Reflection for native**: Forget `@RegisterForReflection` = serialization fails
3. **Working directory**: Execute commands in wrong location = build failures
4. **Picocli interface**: Use `Runnable`, not `Callable<Integer>` or method-based
5. **Project names**: Parse from build files, don't use generic prefixes
6. **JSON output**: Both commands always generate JSON - no optional flags

## Key Files
- `EnvScannerCommand.java` (475 lines): Main entry, scan logic, multi-module detection
- `SbomCommand.java` (792 lines): Build system detection, 9 extraction methods, SBOM generation
- `pom.xml`: Quarkus 3.29.1, Java 21, uber-jar packaging, native profile
- `.github/workflows/ci.yml`: Parallel JVM + native builds
- `cyclonedx_plugins_by_language.md`: CycloneDX tool reference
