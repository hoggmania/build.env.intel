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

Scanned directory: D:\dev\github\.

Build Systems Detected:

   Maven:
      [Standalone] .\Binary-scanning-examples\source-projects\code-with-quarkus\pom.xml
      [Multi-Module Root] .\Binary-scanning-examples\source-projects\sample-java-maven-multi\pom.xml
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-maven-single\pom.xml
      [Standalone] .\build.env.intel\pom.xml
      [Standalone] .\cdxgen\test\pom.xml
      [Standalone] .\ClouderaVEX\pom.xml
      [Standalone] .\mvn-repo-vex\pom.xml
      [Standalone] .\picocli\pom.xml
      [Standalone] .\PURL-Service\pom.xml
      [Standalone] .\quarkus-agentic-ai\pom.xml
      [Standalone] .\random-generator\pom.xml
      [Standalone] .\SBOMMerge\pom.xml
      [Standalone] .\scalibr\osv-cli\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\javareach-test\META-INF\maven\com\example\hello-tester\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\javareach-test\META-INF\maven\mock\reachable\test-jar\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\javareach-test\META-INF\maven\mock\unreachable\test-jar\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\pomxml\testdata\parent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\pomxmlnet\testdata\maven\parent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\maven\testdata\my-app\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\maven\testdata\no-dependency-management\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\maven\testdata\parent\grandparent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\maven\testdata\parent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\maven-classifier\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\workaround\commons\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\workaround\guava\android-to-android\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\workaround\guava\jre-to-jre\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\workaround\guava\none-to-jre\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\zeppelin-server\parent\parent\pom.xml
      [Multi-Module Root] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\zeppelin-server\parent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\override\testdata\zeppelin-server\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\maven\basic\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\maven\patchchoice\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\maven\update\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\internal\mavenutil\testdata\my-app\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\internal\mavenutil\testdata\parent\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\internal\mavenutil\testdata\pom.xml
      [Standalone] .\scalibr\scalibr-java-bindings\pom.xml
      [Standalone] .\ScanStatus\pom.xml
      [Standalone] .\service.purl\pom.xml
      [Standalone] .\sigstore-maven-plugin\pom.xml
      [Standalone] .\sigstore-maven-plugin\src\it\simple-it\pom.xml
      [Standalone] .\sigstore-maven-plugin\src\test\resources\project-to-test\pom.xml
      [Standalone] .\snyk-api-client\pom.xml
      [Standalone] .\utils.snykParser\pom.xml

   Go:
      [Standalone] .\cdxgen\test\gomod\go.mod
      [Standalone] .\cyclonedx-gomod\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\linter\plugger\plugger\testdata\go.mod

   npm:
      [Standalone] .\cdxgen\package.json
      [Standalone] .\cdxgen\test\data\package-json\v1\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\app\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\scripts\package.json
      [Standalone] .\cdxgen\test\data\package-json\v3\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\accepts\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\acorn\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\acorn-globals\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\no-person-name\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\window-size\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\with\deps\acorn\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\lockfile\npm\testdata\v1\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\ws\jquery\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\ws\ugh\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\z\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\deepen\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\diamond\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\introduce-vuln\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\non-constraining\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\removed-vuln\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\simple\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\vuln-without-fix\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\npm\basicrelax\package.json

   Standalone Binaries (Syft):
      [Standalone] .\Binary-scanning-examples\binary-projects\code-with-quarkus-built\code-with-quarkus-1.0.0-SNAPSHOT.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\aether-api-0.9.0.M2.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\aether-impl-0.9.0.M2.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\aether-spi-0.9.0.M2.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\cdi-api-1.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\commons-beanutils-1.7.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\commons-chain-1.1.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\commons-digester-1.8.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\commons-lang-2.4.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\dom4j-1.1.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\jsr250-api-1.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\oro-2.0.8.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\plexus-cipher-1.4.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\plexus-classworlds-2.4.2.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\plexus-i18n-1.0-beta-10.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\plexus-interpolation-1.16.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\plexus-sec-dispatcher-1.3.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\velocity-1.7.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\just-a-bag-of-jars\lib\velocity-tools-2.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-gradle-multi-built\core-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-gradle-multi-built\service-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-gradle-single-built\gradle-single-app-1.0.0-plain.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-gradle-single-built\gradle-single-app-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-maven-multi-built\core-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-maven-multi-built\service-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-maven-multi-built\web-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\binary-projects\sample-java-maven-single-built\maven-single-app-1.0.0.jar
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-single\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\cli-test\jars\commons-codec-1.16.1.jar
      [Standalone] .\cli-test\jars\commons-io-2.14.0.jar
      [Standalone] .\cli-test\jars\cyclonedx-core-java-8.0.3.jar
      [Standalone] .\cli-test\jars\json-schema-validator-1.0.87.jar
      [Standalone] .\cli-test\jars\snakeyaml-2.0.jar
      [Standalone] .\cli-test\jars\woodstox-core-6.5.1.jar
      [Standalone] .\cli-test\jars\woodstox-core-modified.jar
      [Standalone] .\ClouderaVEX\maven-wrapper.jar
      [Standalone] .\quarkus.gradle.kotlin.test\gradle.test\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\quarkus.gradle.test\gradle.test\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\random-generator\jfiglet-0.0.9.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\javareach-test.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\reachable-dep-test.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\unreachable-dep-test.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\com.google.src.yolo-0.1.2.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\complex.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\empty.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\guava-31.1-jre.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\invalid_jar.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\nested_at_10.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\nested_at_100.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\no_pom_properties-2.4.0.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\no_pom_properties.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\org.eclipse.sisu.inject-0.3.5.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\pom_missing_group_id-2.4.0.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\pom_missing_group_id.jar
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\java\archive\testdata\simple.jar

   .NET:
      [Standalone] .\cdxgen\test\data\Logging.csproj
      [Standalone] .\cdxgen\test\data\sample-dotnet.csproj
      [Standalone] .\cdxgen\test\data\Server.csproj
      [Standalone] .\cdxgen\test\data\WindowsFormsApplication1.csproj
      [Standalone] .\cdxgen\test\sample.csproj

   PHP:
      [Standalone] .\cdxgen\test\data\composer.json

   Gradle:
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\core\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\service\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\web\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-single\build.gradle
      [Standalone] .\quarkus.gradle.test\gradle.test\build.gradle
      [Standalone] .\quarkus.gradle.kotlin.test\gradle.test\build.gradle.kts

   Python:
      [Standalone] .\cdxgen\test\data\pyproject.toml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\python\testdata\poetry\pyproject.toml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\python\relax\poetry\pyproject.toml
      [Standalone] .\cdxgen\contrib\requirements.txt
      [Standalone] .\cdxgen\test\diff\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\python\requirementsnet\testdata\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\python\testdata\requirements\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\deepen\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\diamond\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\introduce\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\max-depth\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\no-fix\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\non-constraining\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\removed\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\simple\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\python\relax\requirements\requirements.txt

Tool Versions:
   Maven: [OK]
      Picked up JAVA_TOOL_OPTIONS: --enable-native-access=ALL-UNNAMED
      Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
      Maven home: D:\dev\java\M2\maven
      Java version: 25.0.1, vendor: Oracle Corporation, runtime: D:\dev\java\jdk\graalvm\graalvm-jdk-25.0.1+8.1
      Default locale: en_GB, platform encoding: UTF-8
      OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
   Go: [OK]
      go version go1.25.4 windows/amd64
   npm: [OK]
      11.6.1
   Standalone Binaries (Syft): [OK]
      Application:   syft
      Version:       1.36.0
      BuildDate:     2025-10-22T20:08:43Z
      GitCommit:     8be463911ce718ff70179ded9a2a4dd37549d374
      GitDescription: v1.36.0
      Platform:      windows/amd64
      GoVersion:     go1.24.7
      Compiler:      gc
      SchemaVersion: 16.0.41
   .NET: [OK]
      9.0.306
   PHP: [NOT FOUND]
      Not installed or inaccessible
   Gradle: [OK]
      Picked up JAVA_TOOL_OPTIONS: --enable-native-access=ALL-UNNAMED
      ------------------------------------------------------------
      Gradle 9.2.0
      ------------------------------------------------------------
      Build time:    2025-10-29 13:53:23 UTC
      Revision:      d9d6bbce03b3d88c67ef5a0ff31f7ae5e332d6bf
      Kotlin:        2.2.20
      Groovy:        4.0.28
      Ant:           Apache Ant(TM) version 1.10.15 compiled on August 25 2024
      Launcher JVM:  25.0.1 (Oracle Corporation 25.0.1+8-LTS-jvmci-b01)
      Daemon JVM:    D:\dev\java\jdk\graalvm\graalvm-jdk-25.0.1+8.1 (no JDK specified, using current Java home)
      OS:            Windows 10 10.0 amd64
   Python: [OK]
      Python 3.14.0

File Type Counts:
   go: 946 (69.05%)
   java: 189 (13.80%)
   Dockerfile/Container: 130 (9.49%)
   js: 34 (2.48%)
   ts: 22 (1.61%)
   sh: 16 (1.17%)
   cmd: 9 (0.66%)
   py: 7 (0.51%)
   ps1: 5 (0.36%)
   bat: 5 (0.36%)
   kts: 2 (0.15%)
   rs: 2 (0.15%)
   clj: 1 (0.07%)
   rb: 1 (0.07%)
   groovy: 1 (0.07%)

[SUCCESS] JSON results written to D:\dev\github\scan-results.json

D:\dev\github>

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
