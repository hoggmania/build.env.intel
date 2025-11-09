package org.hoggmania;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.RegisterForReflection;
import picocli.CommandLine.Option;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive file scanner for build systems, IaC, and source code types.
 * Uses Picocli CLI under Quarkus runtime.
 */
@QuarkusMain
@TopCommand
@Command(name = "scan", mixinStandardHelpOptions = true, description = "Scan repository for build, IaC, and source files.")
public class EnvScannerCommand implements Runnable {

    @Parameters(index = "0", description = "Root directory to scan.", defaultValue = "./")
    File rootDir;

    @Option(names = "--json", description = "Output results as JSON")
    boolean json;

    @Option(names = "--output", description = "Output file path (for JSON only)")
    File output;

    // Core pattern groups
    private static final Map<String, List<String>> FILE_PATTERNS = Map.ofEntries(
        // Build systems
        Map.entry("Maven", List.of("pom.xml")),
        Map.entry("Gradle", List.of("build.gradle", "build.gradle.kts")),
        Map.entry("npm", List.of("package.json")),
        Map.entry("Python", List.of("setup.py", "pyproject.toml", "requirements.txt")),
        Map.entry(".NET", List.of(".csproj", ".vbproj")),
        Map.entry("Ruby", List.of("Gemfile")),
        Map.entry("Go", List.of("go.mod")),
        Map.entry("Rust", List.of("Cargo.toml")),
        // IaC
        Map.entry("Terraform", List.of("*.tf", "*.tf.json")),
        Map.entry("CloudFormation", List.of("template.yaml", "template.yml")),
        Map.entry("Ansible", List.of("ansible.cfg", "playbook.yml", "site.yml")),
        Map.entry("Kubernetes", List.of("deployment.yaml", "deployment.yml")),
        Map.entry("Docker", List.of("Dockerfile", "docker-compose.yml")),
        Map.entry("Pulumi", List.of("Pulumi.yaml", "Pulumi.yml")),
        // Source code
        Map.entry("Java", List.of("*.java")),
        Map.entry("Go Source", List.of("*.go")),
        Map.entry("Python Source", List.of("*.py")),
        Map.entry("C/C++", List.of("*.c", "*.cpp", "*.h", "*.hpp")),
        Map.entry("JavaScript", List.of("*.js", "*.jsx")),
        Map.entry("TypeScript", List.of("*.ts", "*.tsx")),
        Map.entry("Kotlin", List.of("*.kt", "*.kts")),
        Map.entry("C#", List.of("*.cs")),
        Map.entry("Rust Source", List.of("*.rs")),
        Map.entry("Ruby Source", List.of("*.rb")),
        Map.entry("Shell", List.of("*.sh")),
        Map.entry("YAML/JSON Config", List.of("*.yaml", "*.yml", "*.json"))
    );

    private static final Map<String, List<String>> VERSION_COMMANDS = Map.ofEntries(
        Map.entry("Maven", List.of("mvn", "-v")),
        Map.entry("Gradle", List.of("gradle", "-v")),
        Map.entry("npm", List.of("npm", "-v")),
        Map.entry("Python", List.of("python3", "--version")),
        Map.entry(".NET", List.of("dotnet", "--version")),
        Map.entry("Ruby", List.of("ruby", "--version")),
        Map.entry("Go", List.of("go", "version")),
        Map.entry("Rust", List.of("cargo", "--version")),
        Map.entry("Terraform", List.of("terraform", "-version"))
    );

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] {"./", "--json", "--output=build.json"};
        }

        System.exit(new CommandLine(new EnvScannerCommand()).execute(args));
    }

    @Override
    public void run() {
        try {
            Map<String, List<Path>> foundFiles = scanFiles(rootDir.toPath());
            
            // Detect multi-module builds
            Map<String, Boolean> multiModuleInfo = detectMultiModuleBuilds(foundFiles);
            
            Map<String, ToolVersionInfo> toolVersions = detectBuildToolVersions(foundFiles.keySet());
            Map<String, Long> fileTypeCounts = countSourceFiles(rootDir.toPath());
            
            // Calculate percentages and create FileTypeInfo objects
            long totalFiles = fileTypeCounts.values().stream().mapToLong(Long::longValue).sum();
            Map<String, FileTypeInfo> fileTypeStats = new LinkedHashMap<>();
            fileTypeCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        double percentage = totalFiles > 0 ? (entry.getValue() * 100.0 / totalFiles) : 0.0;
                        // Round to 2 decimal places
                        percentage = Math.round(percentage * 100.0) / 100.0;
                        fileTypeStats.put(entry.getKey(), new FileTypeInfo(entry.getValue(), percentage));
                    });

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("scannedRoot", rootDir.getAbsolutePath());
            result.put("foundFiles", foundFiles.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> e.getValue().stream().map(Path::toString).collect(Collectors.toList()))));
            result.put("multiModule", multiModuleInfo);
            result.put("toolVersions", toolVersions);
            result.put("fileTypeCounts", fileTypeStats);

            outputResults(result);
        } catch (IOException e) {
            throw new RuntimeException("Error scanning files", e);
        }
    }

    // Inner class to hold tool version information
    @RegisterForReflection
    static class ToolVersionInfo {
        private boolean detected;
        private String versionInfo;

        public ToolVersionInfo() {
            // Default constructor for Jackson
        }

        public ToolVersionInfo(boolean detected, String versionInfo) {
            this.detected = detected;
            this.versionInfo = versionInfo;
        }

        public boolean isDetected() {
            return detected;
        }

        public void setDetected(boolean detected) {
            this.detected = detected;
        }

        public String getVersionInfo() {
            return versionInfo;
        }

        public void setVersionInfo(String versionInfo) {
            this.versionInfo = versionInfo;
        }
    }

    // Inner class to hold file type count and percentage information
    @RegisterForReflection
    static class FileTypeInfo {
        private long count;
        private double percentage;

        public FileTypeInfo() {
            // Default constructor for Jackson
        }

        public FileTypeInfo(long count, double percentage) {
            this.count = count;
            this.percentage = percentage;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    @SuppressWarnings("unchecked")
    private void outputResults(Map<String, Object> result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (json) {
            if (output != null) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(output, result);
                System.out.println("[SUCCESS] Results written to " + output.getAbsolutePath());
            } else {
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            }
        } else {
            System.out.println("Scanned directory: " + rootDir);
            ((Map<String, List<String>>) result.get("foundFiles")).forEach((tool, files) ->
                    System.out.println("  " + tool + ": " + files.size() + " files"));
            
            // Display multi-module information
            Map<String, Boolean> multiModuleInfo = (Map<String, Boolean>) result.get("multiModule");
            if (multiModuleInfo != null && !multiModuleInfo.isEmpty()) {
                System.out.println("\nMulti-Module Builds:");
                multiModuleInfo.forEach((tool, isMultiModule) ->
                        System.out.println("   " + tool + ": " + isMultiModule));
            }
            
            System.out.println("\nTool Versions:");
            ((Map<String, ToolVersionInfo>) result.get("toolVersions")).forEach((tool, versionInfo) -> {
                System.out.println("   " + tool + ": " + versionInfo.isDetected());
                if (versionInfo.isDetected()) {
                    // Print version information with indentation
                    String[] lines = versionInfo.getVersionInfo().split("\n");
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            System.out.println("      " + line);
                        }
                    }
                } else {
                    System.out.println("      " + versionInfo.getVersionInfo());
                }
            });
            System.out.println("\nFile Type Counts:");
            Map<String, FileTypeInfo> fileTypeCounts = (Map<String, FileTypeInfo>) result.get("fileTypeCounts");
            fileTypeCounts.forEach((type, info) -> {
                System.out.println(String.format("   %s: %d (%.2f%%)", type, info.getCount(), info.getPercentage()));
            });
        }
    }

    private Map<String, List<Path>> scanFiles(Path root) throws IOException {
        Map<String, List<Path>> found = new HashMap<>();
        for (var entry : FILE_PATTERNS.entrySet()) {
            List<Path> matches = new ArrayList<>();
            for (String pattern : entry.getValue()) {
                try (var stream = Files.find(root, Integer.MAX_VALUE,
                        (p, attr) -> p.getFileName().toString().matches(globToRegex(pattern)))) {
                    matches.addAll(stream.collect(Collectors.toList()));
                }
            }
            if (!matches.isEmpty()) found.put(entry.getKey(), matches);
        }
        return found;
    }

    private String globToRegex(String glob) {
        return glob.replace(".", "\\.").replace("*", ".*");
    }

    private Map<String, Boolean> detectMultiModuleBuilds(Map<String, List<Path>> foundFiles) {
        Map<String, Boolean> multiModuleInfo = new LinkedHashMap<>();
        
        // Check Maven for multi-module
        if (foundFiles.containsKey("Maven")) {
            List<Path> pomFiles = foundFiles.get("Maven");
            boolean isMultiModule = false;
            
            if (pomFiles.size() > 1) {
                // Check if any pom.xml contains <modules> or <module> tags
                for (Path pomFile : pomFiles) {
                    try {
                        String content = Files.readString(pomFile);
                        if (content.contains("<modules>") || content.contains("<module>")) {
                            isMultiModule = true;
                            break;
                        }
                    } catch (IOException e) {
                        // Continue checking other files
                    }
                }
            }
            multiModuleInfo.put("Maven", isMultiModule);
        }
        
        // Check Gradle for multi-module
        if (foundFiles.containsKey("Gradle")) {
            List<Path> gradleFiles = foundFiles.get("Gradle");
            boolean isMultiModule = false;
            
            if (gradleFiles.size() > 1) {
                // Look for settings.gradle or settings.gradle.kts with include statements
                for (Path gradleFile : gradleFiles) {
                    String fileName = gradleFile.getFileName().toString();
                    if (fileName.equals("settings.gradle") || fileName.equals("settings.gradle.kts")) {
                        try {
                            String content = Files.readString(gradleFile);
                            if (content.contains("include(") || content.contains("include ")) {
                                isMultiModule = true;
                                break;
                            }
                        } catch (IOException e) {
                            // Continue checking
                        }
                    }
                }
                // If no settings file found but multiple build files exist, likely multi-module
                if (!isMultiModule && gradleFiles.size() > 1) {
                    long buildFileCount = gradleFiles.stream()
                        .filter(p -> p.getFileName().toString().startsWith("build.gradle"))
                        .count();
                    isMultiModule = buildFileCount > 1;
                }
            }
            multiModuleInfo.put("Gradle", isMultiModule);
        }
        
        return multiModuleInfo;
    }

    private Map<String, ToolVersionInfo> detectBuildToolVersions(Set<String> detectedTools) {
        Map<String, ToolVersionInfo> versions = new LinkedHashMap<>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        for (String tool : detectedTools) {
            if (VERSION_COMMANDS.containsKey(tool)) {
                try {
                    List<String> command = VERSION_COMMANDS.get(tool);
                    List<String> fullCommand = new ArrayList<>();
                    
                    // On Windows, run through cmd.exe to resolve PATH and .cmd/.bat files
                    if (isWindows) {
                        fullCommand.add("cmd.exe");
                        fullCommand.add("/c");
                        fullCommand.addAll(command);
                    } else {
                        fullCommand.addAll(command);
                    }
                    
                    ProcessBuilder pb = new ProcessBuilder(fullCommand);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    StringBuilder output = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    }
                    
                    int exitCode = process.waitFor();
                    String result = output.toString().trim();
                    
                    if (exitCode == 0 && !result.isEmpty()) {
                        versions.put(tool, new ToolVersionInfo(true, result));
                    } else {
                        versions.put(tool, new ToolVersionInfo(false, "Not installed or inaccessible"));
                    }
                } catch (IOException | InterruptedException e) {
                    versions.put(tool, new ToolVersionInfo(false, "Not installed or inaccessible: " + e.getMessage()));
                }
            }
        }
        return versions;
    }

    private Map<String, Long> countSourceFiles(Path root) throws IOException {
        Map<String, Long> counts = new LinkedHashMap<>();
        
        // Common build output directories to exclude
        Set<String> excludeDirs = Set.of(
            "target",           // Maven
            "build",            // Gradle
            "bin",              // General binary output
            "out",              // IntelliJ IDEA output
            "dist",             // Distribution directories
            "node_modules",     // npm
            ".gradle",          // Gradle cache
            ".mvn",             // Maven wrapper
            "__pycache__",      // Python
            ".pytest_cache",    // Pytest
            "venv",             // Python virtual env
            ".venv",            // Python virtual env
            "env",              // Python virtual env
            ".tox",             // Tox
            "obj",              // .NET
            ".vs",              // Visual Studio
            ".idea",            // IntelliJ IDEA
            ".git",             // Git
            ".svn",             // SVN
            ".hg"               // Mercurial
        );
        
        // Source code language extensions and container build files
        Set<String> sourceCodeExtensions = Set.of(
            // Programming languages
            "java", "kt", "kts",                    // Java, Kotlin
            "c", "cpp", "cc", "cxx", "h", "hpp",    // C/C++
            "cs", "vb",                             // C#, VB.NET
            "py", "pyw",                            // Python
            "js", "jsx", "ts", "tsx",               // JavaScript, TypeScript
            "go",                                   // Go
            "rs",                                   // Rust
            "rb",                                   // Ruby
            "php",                                  // PHP
            "swift",                                // Swift
            "m", "mm",                              // Objective-C
            "scala",                                // Scala
            "groovy",                               // Groovy
            "clj", "cljs",                          // Clojure
            "erl", "hrl",                           // Erlang
            "ex", "exs",                            // Elixir
            "lua",                                  // Lua
            "pl", "pm",                             // Perl
            "r",                                    // R
            "dart",                                 // Dart
            "f", "f90", "f95",                      // Fortran
            "asm", "s",                             // Assembly
            "sh", "bash", "zsh",                    // Shell scripts
            "ps1", "psm1",                          // PowerShell
            "bat", "cmd"                            // Batch scripts
        );
        
        // Container build files (exact filenames to match)
        Set<String> containerBuildFiles = Set.of(
            "dockerfile",
            "containerfile",
            "docker-compose.yml",
            "docker-compose.yaml",
            ".dockerignore"
        );
        
        Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(p -> {
                    // Check if any part of the path contains an excluded directory
                    for (Path parent = p.getParent(); parent != null && parent.startsWith(root); parent = parent.getParent()) {
                        if (excludeDirs.contains(parent.getFileName().toString().toLowerCase())) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    
                    // Check if it's a container build file
                    if (containerBuildFiles.contains(name) || name.startsWith("dockerfile.")) {
                        return true;
                    }
                    
                    // Check if it has a source code extension
                    if (name.contains(".")) {
                        String ext = name.substring(name.lastIndexOf('.') + 1);
                        return sourceCodeExtensions.contains(ext);
                    }
                    
                    return false;
                })
                .forEach(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    
                    // Special handling for container files
                    if (containerBuildFiles.contains(name) || name.startsWith("dockerfile.")) {
                        counts.merge("Dockerfile/Container", 1L, Long::sum);
                    } else {
                        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "<no extension>";
                        counts.merge(ext, 1L, Long::sum);
                    }
                });
        return counts;
    }



}
