/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.hoggmania;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import io.quarkus.runtime.annotations.RegisterForReflection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command to inspect the environment and generate SBOM using appropriate CycloneDX plugin.
 */
@Command(name = "sbom", mixinStandardHelpOptions = true, description = "Generate SBOM using appropriate CycloneDX plugin for detected build system")
public class SbomCommand implements Runnable {

    @Parameters(index = "0", description = "Root directory to inspect.", defaultValue = "./")
    File rootDir;

    @Option(names = "--output", description = "Output directory for SBOM file", defaultValue = "generated-sboms")
    File outputDir;

    @Option(names = "--dry-run", description = "Show which plugin would be used without executing")
    boolean dryRun;

    @Option(names = "--merge", description = "Merge all generated SBOMs into a single file")
    boolean merge;

    @RegisterForReflection
    static class BuildSystemInfo {
        String buildSystem;
        String pluginCommand;
        String version;
        boolean multiModule;
        List<String> buildFiles;
        String projectName;
        Path workingDirectory;

        public BuildSystemInfo(String buildSystem, String pluginCommand) {
            this.buildSystem = buildSystem;
            this.pluginCommand = pluginCommand;
            this.buildFiles = new ArrayList<>();
            this.projectName = "project";
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] {".", "--dry-run"};
        }

        System.exit(new CommandLine(new SbomCommand()).execute(args));
    }


    @Override
    public void run() {
        try {
            // Create output directory if it doesn't exist
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                System.out.println("Created output directory: " + outputDir.getAbsolutePath());
            }

            System.out.println("Inspecting environment at: " + rootDir.getAbsolutePath());
            System.out.println("Output directory: " + outputDir.getAbsolutePath());
            
            BuildSystemInfo buildInfo = detectBuildSystem();
            
            if (buildInfo == null) {
                System.err.println("[ERROR] No supported build system detected");
                System.err.println("Supported build systems:");
                System.err.println("  - Maven (pom.xml)");
                System.err.println("  - Gradle (build.gradle, build.gradle.kts)");
                System.err.println("  - npm (package.json)");
                System.err.println("  - Python (setup.py, pyproject.toml, requirements.txt)");
                System.err.println("  - Go (go.mod)");
                System.err.println("  - .NET (*.csproj, *.vbproj, *.fsproj, *.sln)");
                System.err.println("  - Rust (Cargo.toml)");
                System.err.println("  - PHP (composer.json)");
                System.err.println("  - Ruby (Gemfile)");
                return;
            }

            System.out.println("\n=== Build System Detection ===");
            System.out.println("Build System: " + buildInfo.buildSystem);
            System.out.println("Project Name: " + buildInfo.projectName);
            System.out.println("Multi-Module: " + buildInfo.multiModule);
            System.out.println("Build Files Found: " + buildInfo.buildFiles.size());
            buildInfo.buildFiles.forEach(f -> System.out.println("  - " + f));

            System.out.println("\n=== CycloneDX Plugin Recommendation ===");
            System.out.println("Plugin Command: " + buildInfo.pluginCommand);
            
            if (dryRun) {
                System.out.println("\n[DRY-RUN] Would execute: " + buildInfo.pluginCommand);
                writeSummaryJson(buildInfo, true, false);
                return;
            }

            System.out.println("\n=== Generating SBOM ===");
            boolean sbomSuccess = generateSbom(buildInfo);

            if (merge) {
                System.out.println("\n=== Merging SBOMs ===");
                mergeSboms();
            }

            // Always write summary JSON
            writeSummaryJson(buildInfo, false, sbomSuccess);

        } catch (IOException e) {
            throw new RuntimeException("Error inspecting environment", e);
        }
    }

    private BuildSystemInfo detectBuildSystem() throws IOException {
        Path root = rootDir.toPath();
        
        // Check for Maven
        List<Path> pomFiles = findFiles(root, "pom.xml");
        if (!pomFiles.isEmpty()) {
            Path primaryPomFile = pomFiles.get(0);
            String projectName = extractMavenProjectName(primaryPomFile);
            BuildSystemInfo info = new BuildSystemInfo("Maven", getMavenSbomCommand(projectName));
            info.buildFiles = pomFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = checkMavenMultiModule(pomFiles);
            info.projectName = projectName;
            info.workingDirectory = primaryPomFile.getParent();
            return info;
        }

        // Check for Gradle
        List<Path> gradleFiles = new ArrayList<>();
        gradleFiles.addAll(findFiles(root, "build.gradle"));
        gradleFiles.addAll(findFiles(root, "build.gradle.kts"));
        if (!gradleFiles.isEmpty()) {
            String projectName = extractGradleProjectName(root);
            BuildSystemInfo info = new BuildSystemInfo("Gradle", getGradleSbomCommand(projectName));
            info.buildFiles = gradleFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = checkGradleMultiModule(root, gradleFiles);
            info.projectName = projectName;
            // Use root directory for Gradle (settings.gradle location)
            info.workingDirectory = root;
            return info;
        }

        // Check for npm
        List<Path> packageJsonFiles = findFiles(root, "package.json");
        if (!packageJsonFiles.isEmpty()) {
            Path primaryPackageJson = packageJsonFiles.get(0);
            String projectName = extractNpmProjectName(primaryPackageJson);
            BuildSystemInfo info = new BuildSystemInfo("npm", getNpmSbomCommand(projectName));
            info.buildFiles = packageJsonFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = packageJsonFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryPackageJson.getParent();
            return info;
        }

        // Check for Python
        List<Path> pythonFiles = new ArrayList<>();
        pythonFiles.addAll(findFiles(root, "setup.py"));
        pythonFiles.addAll(findFiles(root, "pyproject.toml"));
        pythonFiles.addAll(findFiles(root, "requirements.txt"));
        if (!pythonFiles.isEmpty()) {
            Path primaryPythonFile = pythonFiles.get(0);
            String projectName = extractPythonProjectName(pythonFiles);
            BuildSystemInfo info = new BuildSystemInfo("Python", getPythonSbomCommand(projectName));
            info.buildFiles = pythonFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.projectName = projectName;
            info.workingDirectory = primaryPythonFile.getParent();
            return info;
        }

        // Check for Go
        List<Path> goModFiles = findFiles(root, "go.mod");
        if (!goModFiles.isEmpty()) {
            Path primaryGoMod = goModFiles.get(0);
            String projectName = extractGoProjectName(primaryGoMod);
            BuildSystemInfo info = new BuildSystemInfo("Go", getGoSbomCommand(projectName));
            info.buildFiles = goModFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = goModFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryGoMod.getParent();
            return info;
        }

        // Check for .NET
        List<Path> dotnetFiles = new ArrayList<>();
        dotnetFiles.addAll(findFilesByPattern(root, "*.csproj"));
        dotnetFiles.addAll(findFilesByPattern(root, "*.vbproj"));
        dotnetFiles.addAll(findFilesByPattern(root, "*.fsproj"));
        dotnetFiles.addAll(findFiles(root, "*.sln"));
        if (!dotnetFiles.isEmpty()) {
            Path primaryDotnetFile = dotnetFiles.get(0);
            String projectName = extractDotnetProjectName(primaryDotnetFile);
            BuildSystemInfo info = new BuildSystemInfo(".NET", getDotnetSbomCommand(projectName));
            info.buildFiles = dotnetFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = dotnetFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryDotnetFile.getParent();
            return info;
        }

        // Check for Rust
        List<Path> cargoFiles = findFiles(root, "Cargo.toml");
        if (!cargoFiles.isEmpty()) {
            Path primaryCargoFile = cargoFiles.get(0);
            String projectName = extractRustProjectName(primaryCargoFile);
            BuildSystemInfo info = new BuildSystemInfo("Rust", getRustSbomCommand(projectName));
            info.buildFiles = cargoFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = cargoFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryCargoFile.getParent();
            return info;
        }

        // Check for PHP (Composer)
        List<Path> composerFiles = findFiles(root, "composer.json");
        if (!composerFiles.isEmpty()) {
            Path primaryComposerFile = composerFiles.get(0);
            String projectName = extractPhpProjectName(primaryComposerFile);
            BuildSystemInfo info = new BuildSystemInfo("PHP", getPhpSbomCommand(projectName));
            info.buildFiles = composerFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = composerFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryComposerFile.getParent();
            return info;
        }

        // Check for Ruby
        List<Path> gemFiles = findFiles(root, "Gemfile");
        if (!gemFiles.isEmpty()) {
            Path primaryGemFile = gemFiles.get(0);
            String projectName = extractRubyProjectName(root);
            BuildSystemInfo info = new BuildSystemInfo("Ruby", getRubySbomCommand(projectName));
            info.buildFiles = gemFiles.stream().map(Path::toString).collect(Collectors.toList());
            info.multiModule = gemFiles.size() > 1;
            info.projectName = projectName;
            info.workingDirectory = primaryGemFile.getParent();
            return info;
        }

        return null;
    }

    private List<Path> findFiles(Path root, String filename) throws IOException {
        try (var stream = Files.find(root, Integer.MAX_VALUE,
                (p, attr) -> p.getFileName().toString().equals(filename))) {
            return stream.collect(Collectors.toList());
        }
    }

    private List<Path> findFilesByPattern(Path root, String pattern) throws IOException {
        String regex = pattern.replace("*", ".*").replace("?", ".");
        try (var stream = Files.find(root, Integer.MAX_VALUE,
                (p, attr) -> p.getFileName().toString().matches(regex))) {
            return stream.collect(Collectors.toList());
        }
    }

    private boolean checkMavenMultiModule(List<Path> pomFiles) {
        if (pomFiles.size() <= 1) return false;
        
        for (Path pomFile : pomFiles) {
            try {
                String content = Files.readString(pomFile);
                if (content.contains("<modules>") || content.contains("<module>")) {
                    return true;
                }
            } catch (IOException e) {
                // Continue checking
            }
        }
        return false;
    }

    private boolean checkGradleMultiModule(Path root, List<Path> gradleFiles) {
        if (gradleFiles.size() <= 1) return false;
        
        try {
            Path settingsGradle = root.resolve("settings.gradle");
            Path settingsGradleKts = root.resolve("settings.gradle.kts");
            
            if (Files.exists(settingsGradle)) {
                String content = Files.readString(settingsGradle);
                if (content.contains("include(") || content.contains("include ")) {
                    return true;
                }
            }
            if (Files.exists(settingsGradleKts)) {
                String content = Files.readString(settingsGradleKts);
                if (content.contains("include(") || content.contains("include ")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        
        return gradleFiles.size() > 1;
    }

    private String extractMavenProjectName(Path pomFile) {
        try {
            String content = Files.readString(pomFile);
            // Extract artifactId from pom.xml
            int artifactIdStart = content.indexOf("<artifactId>");
            int artifactIdEnd = content.indexOf("</artifactId>");
            if (artifactIdStart > 0 && artifactIdEnd > artifactIdStart) {
                return content.substring(artifactIdStart + 12, artifactIdEnd).trim();
            }
        } catch (IOException e) {
            // Ignore
        }
        return "maven-project";
    }

    private String extractGradleProjectName(Path root) {
        try {
            Path settingsGradle = root.resolve("settings.gradle");
            Path settingsGradleKts = root.resolve("settings.gradle.kts");
            
            Path settingsFile = Files.exists(settingsGradle) ? settingsGradle : 
                               Files.exists(settingsGradleKts) ? settingsGradleKts : null;
            
            if (settingsFile != null) {
                String content = Files.readString(settingsFile);
                // Extract rootProject.name
                int nameStart = content.indexOf("rootProject.name");
                if (nameStart > 0) {
                    int equalsPos = content.indexOf("=", nameStart);
                    if (equalsPos > 0) {
                        int lineEnd = content.indexOf("\n", equalsPos);
                        if (lineEnd < 0) lineEnd = content.length();
                        String nameLine = content.substring(equalsPos + 1, lineEnd).trim();
                        return nameLine.replaceAll("['\"]", "").trim();
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        // Fallback to directory name
        return root.getFileName().toString();
    }

    private String extractNpmProjectName(Path packageJson) {
        try {
            String content = Files.readString(packageJson);
            // Extract "name" field from package.json
            int nameStart = content.indexOf("\"name\"");
            if (nameStart > 0) {
                int colonPos = content.indexOf(":", nameStart);
                int commaPos = content.indexOf(",", colonPos);
                int bracePos = content.indexOf("}", colonPos);
                int endPos = commaPos > 0 ? Math.min(commaPos, bracePos > 0 ? bracePos : Integer.MAX_VALUE) : bracePos;
                if (colonPos > 0 && endPos > colonPos) {
                    String name = content.substring(colonPos + 1, endPos).trim();
                    return name.replaceAll("['\",]", "").trim();
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "npm-project";
    }

    private String extractPythonProjectName(List<Path> pythonFiles) {
        // Check setup.py for name
        for (Path file : pythonFiles) {
            if (file.getFileName().toString().equals("setup.py")) {
                try {
                    String content = Files.readString(file);
                    int nameStart = content.indexOf("name=");
                    if (nameStart > 0) {
                        int quoteStart = content.indexOf("\"", nameStart);
                        if (quoteStart < 0) quoteStart = content.indexOf("'", nameStart);
                        if (quoteStart > 0) {
                            int quoteEnd = content.indexOf(content.charAt(quoteStart), quoteStart + 1);
                            if (quoteEnd > 0) {
                                return content.substring(quoteStart + 1, quoteEnd).trim();
                            }
                        }
                    }
                } catch (IOException e) {
                    // Continue
                }
            }
        }
        
        // Check pyproject.toml for name
        for (Path file : pythonFiles) {
            if (file.getFileName().toString().equals("pyproject.toml")) {
                try {
                    String content = Files.readString(file);
                    int nameStart = content.indexOf("name =");
                    if (nameStart > 0) {
                        int quoteStart = content.indexOf("\"", nameStart);
                        if (quoteStart < 0) quoteStart = content.indexOf("'", nameStart);
                        if (quoteStart > 0) {
                            int quoteEnd = content.indexOf(content.charAt(quoteStart), quoteStart + 1);
                            if (quoteEnd > 0) {
                                return content.substring(quoteStart + 1, quoteEnd).trim();
                            }
                        }
                    }
                } catch (IOException e) {
                    // Continue
                }
            }
        }
        
        return "python-project";
    }

    private String extractGoProjectName(Path goMod) {
        try {
            String content = Files.readString(goMod);
            // Extract module name from go.mod
            int moduleStart = content.indexOf("module ");
            if (moduleStart >= 0) {
                int lineEnd = content.indexOf("\n", moduleStart);
                if (lineEnd < 0) lineEnd = content.length();
                String moduleLine = content.substring(moduleStart + 7, lineEnd).trim();
                // Get the last part of the module path
                String[] parts = moduleLine.split("/");
                return parts[parts.length - 1];
            }
        } catch (IOException e) {
            // Ignore
        }
        return "go-project";
    }

    private String extractDotnetProjectName(Path projectFile) {
        try {
            String fileName = projectFile.getFileName().toString();
            // Extract from .csproj, .vbproj, .fsproj, or .sln filename
            if (fileName.endsWith(".sln")) {
                return fileName.substring(0, fileName.length() - 4);
            } else if (fileName.endsWith(".csproj") || fileName.endsWith(".vbproj") || fileName.endsWith(".fsproj")) {
                return fileName.substring(0, fileName.lastIndexOf('.'));
            }
            
            // Try to extract from project file content
            String content = Files.readString(projectFile);
            int nameStart = content.indexOf("<AssemblyName>");
            if (nameStart > 0) {
                int nameEnd = content.indexOf("</AssemblyName>", nameStart);
                if (nameEnd > nameStart) {
                    return content.substring(nameStart + 14, nameEnd).trim();
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "dotnet-project";
    }

    private String extractRustProjectName(Path cargoToml) {
        try {
            String content = Files.readString(cargoToml);
            // Extract name from [package] section
            int packageStart = content.indexOf("[package]");
            if (packageStart >= 0) {
                int nameStart = content.indexOf("name =", packageStart);
                if (nameStart > 0) {
                    int quoteStart = content.indexOf("\"", nameStart);
                    if (quoteStart > 0) {
                        int quoteEnd = content.indexOf("\"", quoteStart + 1);
                        if (quoteEnd > 0) {
                            return content.substring(quoteStart + 1, quoteEnd).trim();
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "rust-project";
    }

    private String extractPhpProjectName(Path composerJson) {
        try {
            String content = Files.readString(composerJson);
            // Extract "name" field from composer.json
            int nameStart = content.indexOf("\"name\"");
            if (nameStart > 0) {
                int colonPos = content.indexOf(":", nameStart);
                int quoteStart = content.indexOf("\"", colonPos);
                if (quoteStart > 0) {
                    int quoteEnd = content.indexOf("\"", quoteStart + 1);
                    if (quoteEnd > 0) {
                        String fullName = content.substring(quoteStart + 1, quoteEnd).trim();
                        // composer.json names are typically "vendor/package"
                        String[] parts = fullName.split("/");
                        return parts.length > 1 ? parts[1] : fullName;
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "php-project";
    }

    private String extractRubyProjectName(Path root) {
        // Try to extract from .gemspec file
        try {
            List<Path> gemspecFiles = findFilesByPattern(root, "*.gemspec");
            if (!gemspecFiles.isEmpty()) {
                String fileName = gemspecFiles.get(0).getFileName().toString();
                return fileName.substring(0, fileName.lastIndexOf('.'));
            }
        } catch (IOException e) {
            // Continue
        }
        
        // Fallback to directory name
        return root.getFileName().toString();
    }

    private String getMavenSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        
        return String.format("mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom -DoutputFormat=json -DoutputDirectory=%s -DoutputName=%s-bom",
                outputPath, projectName);
    }

    private String getGradleSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        
        return String.format("gradle cyclonedxBom -PcyclonedxOutputFormat=json -PcyclonedxOutputDirectory=%s -PcyclonedxOutputName=%s-bom",
                outputPath, projectName);
    }

    private String getNpmSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("npx @cyclonedx/cyclonedx-npm --output-file %s/%s",
                outputPath, outputFile);
    }

    private String getPythonSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("cyclonedx-py --format json --output %s/%s",
                outputPath, outputFile);
    }

    private String getGoSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("cyclonedx-gomod app -json=true -output %s/%s",
                outputPath, outputFile);
    }

    private String getDotnetSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        
        return String.format("dotnet CycloneDX . -o %s -f json -n %s-bom",
                outputPath, projectName);
    }

    private String getRustSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("cargo cyclonedx -f json --output-file %s/%s",
                outputPath, outputFile);
    }

    private String getPhpSbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("composer make-bom --output-format=json --output-file=%s/%s",
                outputPath, outputFile);
    }

    private String getRubySbomCommand(String projectName) {
        String outputPath = outputDir.getAbsolutePath();
        String outputFile = projectName + "-bom.json";
        
        return String.format("cyclonedx-ruby -o %s/%s -t json",
                outputPath, outputFile);
    }

    private boolean generateSbom(BuildSystemInfo buildInfo) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        try {
            List<String> command = new ArrayList<>();
            
            if (isWindows) {
                command.add("cmd.exe");
                command.add("/c");
            } else {
                command.add("sh");
                command.add("-c");
            }
            
            command.add(buildInfo.pluginCommand);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            // Use the working directory where the build file is located
            File workDir = buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toFile() : rootDir;
            pb.directory(workDir);
            pb.inheritIO(); // Show output in real-time
            
            System.out.println("Executing: " + buildInfo.pluginCommand);
            System.out.println("Working directory: " + workDir.getAbsolutePath());
            System.out.println();
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("\n[SUCCESS] SBOM generated successfully");
                System.out.println("Output location: " + outputDir.getAbsolutePath());
                return true;
            } else {
                System.err.println("\n[ERROR] SBOM generation failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Failed to execute SBOM generation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void writeSummaryJson(BuildSystemInfo buildInfo, boolean dryRun, boolean success) throws IOException {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", new Date().toString());
        summary.put("rootDirectory", rootDir.getAbsolutePath());
        summary.put("outputDirectory", outputDir.getAbsolutePath());
        summary.put("buildSystem", buildInfo.buildSystem);
        summary.put("projectName", buildInfo.projectName);
        summary.put("multiModule", buildInfo.multiModule);
        summary.put("buildFiles", buildInfo.buildFiles);
        summary.put("workingDirectory", buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toString() : null);
        summary.put("pluginCommand", buildInfo.pluginCommand);
        summary.put("format", "json");
        summary.put("dryRun", dryRun);
        
        if (!dryRun) {
            summary.put("generationSuccess", success);
            
            // List generated SBOM files (JSON only)
            List<String> generatedFiles = new ArrayList<>();
            try {
                if (outputDir.exists()) {
                    Files.list(outputDir.toPath())
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .filter(p -> p.getFileName().toString().contains("-bom"))
                        .forEach(p -> generatedFiles.add(p.toString()));
                }
            } catch (IOException e) {
                // Ignore
            }
            summary.put("generatedSbomFiles", generatedFiles);
        }
        
        ObjectMapper mapper = new ObjectMapper();
        File summaryFile = new File(outputDir, "sbom-summary.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(summaryFile, summary);
        System.out.println("\n[SUCCESS] Summary written to " + summaryFile.getAbsolutePath());
    }

    private void mergeSboms() throws IOException {
        Path outputPath = outputDir.toPath();
        
        // Find all JSON SBOM files in the output directory
        List<Path> sbomFiles = Files.list(outputPath)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .filter(p -> p.getFileName().toString().contains("-bom"))
                .collect(Collectors.toList());
        
        if (sbomFiles.isEmpty()) {
            System.out.println("No SBOM files found to merge");
            return;
        }
        
        if (sbomFiles.size() == 1) {
            System.out.println("Only one SBOM file found, no merge needed");
            return;
        }
        
        System.out.println("Found " + sbomFiles.size() + " SBOM files to merge:");
        sbomFiles.forEach(f -> System.out.println("  - " + f.getFileName()));
        
        // Always use JSON format
        mergeJsonSboms(sbomFiles, outputPath);
    }

    private void mergeJsonSboms(List<Path> sbomFiles, Path outputPath) throws IOException {
        // For JSON, we'll create a simple merged structure
        // In production, you'd use a proper CycloneDX library
        StringBuilder merged = new StringBuilder();
        merged.append("{\n");
        merged.append("  \"bomFormat\": \"CycloneDX\",\n");
        merged.append("  \"specVersion\": \"1.5\",\n");
        merged.append("  \"version\": 1,\n");
        merged.append("  \"components\": [\n");
        
        boolean first = true;
        for (Path sbomFile : sbomFiles) {
            String content = Files.readString(sbomFile);
            // Extract components array (simplified - real implementation would use JSON parser)
            int componentsStart = content.indexOf("\"components\"");
            if (componentsStart > 0) {
                int arrayStart = content.indexOf("[", componentsStart);
                int arrayEnd = content.indexOf("]", arrayStart);
                if (arrayStart > 0 && arrayEnd > 0) {
                    String components = content.substring(arrayStart + 1, arrayEnd).trim();
                    if (!components.isEmpty()) {
                        if (!first) {
                            merged.append(",\n");
                        }
                        merged.append("    ").append(components);
                        first = false;
                    }
                }
            }
        }
        
        merged.append("\n  ]\n");
        merged.append("}\n");
        
        Path mergedFile = outputPath.resolve("merged-bom.json");
        Files.writeString(mergedFile, merged.toString());
        System.out.println("\n[SUCCESS] Merged SBOM created: " + mergedFile.getFileName());
        System.out.println("Location: " + mergedFile.toAbsolutePath());
    }
}
