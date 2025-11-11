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
@Command(
    name = "scan", 
    mixinStandardHelpOptions = true, 
    description = "Scan repository for build, IaC, and source files.",
    subcommands = {SbomCommand.class}
)
public class EnvScannerCommand implements Runnable {

    @Parameters(index = "0", description = "Root directory to scan.", defaultValue = "./")
    File rootDir;

    @Option(names = "--output", description = "Output file path for JSON (default: scan-results.json)")
    File output;

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] {"./", "--output=scan-results.json"};
        }

        System.exit(new CommandLine(new EnvScannerCommand()).execute(args));
    }

    @Override
    public void run() {
        try {
            Map<String, List<Path>> foundFiles = scanFiles(rootDir.toPath());
            
            // Detect multi-module builds (legacy, kept for backward compatibility)
            Map<String, Boolean> multiModuleInfo = detectMultiModuleBuilds(foundFiles);
            
            // Detect detailed build system instances with multi-module filtering
            Map<String, List<BuildSystemInstance>> detailedBuildSystems = detectDetailedBuildSystems(foundFiles);
            
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
            result.put("buildSystemInstances", detailedBuildSystems);
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

    // Inner class to hold build system instance information
    @RegisterForReflection
    static class BuildSystemInstance {
        private String path;
        private boolean multiModule;
        private boolean isRoot;

        public BuildSystemInstance() {
            // Default constructor for Jackson
        }

        public BuildSystemInstance(String path, boolean multiModule, boolean isRoot) {
            this.path = path;
            this.multiModule = multiModule;
            this.isRoot = isRoot;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isMultiModule() {
            return multiModule;
        }

        public void setMultiModule(boolean multiModule) {
            this.multiModule = multiModule;
        }

        public boolean isRoot() {
            return isRoot;
        }

        public void setRoot(boolean root) {
            isRoot = root;
        }
    }

    @SuppressWarnings("unchecked")
    private void outputResults(Map<String, Object> result) throws IOException {
        // Always output to console
        System.out.println(ConsoleColors.bold("\nBuild Environment Intelligence Scanner"));
        System.out.println(ConsoleColors.bold("====================================="));
        System.out.println("\nScanned directory: " + ConsoleColors.highlight(rootDir.getAbsolutePath()));
        
        // Display detailed build system instances
        Map<String, List<BuildSystemInstance>> buildSystemInstances = 
            (Map<String, List<BuildSystemInstance>>) result.get("buildSystemInstances");
        
        if (buildSystemInstances != null && !buildSystemInstances.isEmpty()) {
            System.out.println(ConsoleColors.bold("\nBuild Systems Detected:"));
            buildSystemInstances.forEach((buildSystem, instances) -> {
                System.out.println("\n   " + ConsoleColors.info(buildSystem) + ":");
                for (BuildSystemInstance instance : instances) {
                    String status = instance.isMultiModule() ? 
                        ConsoleColors.success("[Multi-Module Root]") : 
                        ConsoleColors.info("[Standalone]");
                    System.out.println("      " + status + " " + instance.getPath());
                }
            });
        }
        
        System.out.println(ConsoleColors.bold("\nTool Versions:"));
        ((Map<String, ToolVersionInfo>) result.get("toolVersions")).forEach((tool, versionInfo) -> {
            String detected = versionInfo.isDetected() ? 
                ConsoleColors.success("✓") : ConsoleColors.error("✗");
            System.out.println("   " + tool + ": " + detected);
            if (versionInfo.isDetected()) {
                // Print version information with indentation
                String[] lines = versionInfo.getVersionInfo().split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        System.out.println("      " + line);
                    }
                }
            } else {
                System.out.println("      " + ConsoleColors.warning(versionInfo.getVersionInfo()));
            }
        });
        
        System.out.println(ConsoleColors.bold("\nFile Type Counts:"));
        Map<String, FileTypeInfo> fileTypeCounts = (Map<String, FileTypeInfo>) result.get("fileTypeCounts");
        fileTypeCounts.forEach((type, info) -> {
            System.out.println(String.format("   %s: %d (%.2f%%)", type, info.getCount(), info.getPercentage()));
        });
        
        // Always generate JSON file
        ObjectMapper mapper = new ObjectMapper();
        File jsonOutput = output != null ? output : new File("scan-results.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonOutput, result);
        System.out.println("\n" + ConsoleColors.success("[SUCCESS]") + " JSON results written to " + 
            ConsoleColors.highlight(jsonOutput.getAbsolutePath()));
    }

    /**
     * Scan files in the given directory for build systems, IaC, and source code.
     * Public method that can be used by other commands like SbomCommand.
     * 
     * Uses BuildSystemGeneratorRegistry to get build file patterns from generators.
     * 
     * @param root Root directory to scan
     * @return Map of build system/file type names to list of matching file paths
     * @throws IOException if scanning fails
     */
    public Map<String, List<Path>> scanFiles(Path root) throws IOException {
        Map<String, List<Path>> found = new HashMap<>();
        Set<String> excludeDirs = getExcludedDirectories();
        
        // Scan for build systems using registry
        for (BuildSystemSbomGenerator generator : BuildSystemGeneratorRegistry.getAllGenerators().values()) {
            String buildSystemName = generator.getBuildSystemName();
            List<Path> matches = new ArrayList<>();
            
            // Get all patterns for this build system
            List<String> patterns = new ArrayList<>();
            patterns.add(generator.getBuildFilePattern());
            patterns.addAll(generator.getAdditionalBuildFilePatterns());
            
            // Search for each pattern
            for (String pattern : patterns) {
                try (var stream = Files.find(root, Integer.MAX_VALUE,
                        (p, attr) -> !shouldExcludePath(p, excludeDirs) && 
                                     p.getFileName().toString().matches(globToRegex(pattern)))) {
                    matches.addAll(stream.collect(Collectors.toList()));
                }
            }
            
            if (!matches.isEmpty()) {
                found.put(buildSystemName, matches);
            }
        }
        
        // Scan for non-build-system patterns (IaC and source code)
        for (var entry : FilePatterns.NON_BUILD_SYSTEM_PATTERNS.entrySet()) {
            List<Path> matches = new ArrayList<>();
            for (String pattern : entry.getValue()) {
                try (var stream = Files.find(root, Integer.MAX_VALUE,
                        (p, attr) -> !shouldExcludePath(p, excludeDirs) && 
                                     p.getFileName().toString().matches(globToRegex(pattern)))) {
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

    // Common build output directories to exclude from scanning
    private Set<String> getExcludedDirectories() {
        return FilePatterns.EXCLUDED_DIRECTORIES;
    }

    // Check if a path should be excluded from scanning
    private boolean shouldExcludePath(Path path, Set<String> excludeDirs) {
        return path.toString().contains(File.separator) &&
               excludeDirs.stream().anyMatch(dir -> 
                   path.toString().contains(File.separator + dir + File.separator) ||
                   path.toString().endsWith(File.separator + dir)
               );
    }

    /**
     * Detect if the found build systems are configured for multi-module builds.
     * Public method that can be used by other commands like SbomCommand.
     * 
     * Uses the BuildSystemGeneratorRegistry to delegate multi-module detection
     * to build system-specific implementations.
     * 
     * @param foundFiles Map of build system names to their file paths
     * @return Map of build system names to their multi-module status (true/false)
     */
    public Map<String, Boolean> detectMultiModuleBuilds(Map<String, List<Path>> foundFiles) {
        Map<String, Boolean> multiModuleInfo = new LinkedHashMap<>();
        
        // Use generators from registry for multi-module detection
        for (Map.Entry<String, List<Path>> entry : foundFiles.entrySet()) {
            String buildSystemName = entry.getKey();
            List<Path> buildFiles = entry.getValue();
            
            BuildSystemGeneratorRegistry.getGenerator(buildSystemName).ifPresent(generator -> {
                boolean isMultiModule = generator.isMultiModule(buildFiles);
                multiModuleInfo.put(buildSystemName, isMultiModule);
            });
        }
        
        return multiModuleInfo;
    }

    /**
     * Create detailed build system instance information.
     * For each build system, identify all build file instances and determine:
     * 1. Which files are multi-module roots
     * 2. Which files should be excluded (under a multi-module root)
     * 
     * @param foundFiles Map of build system names to their file paths
     * @return Map of build system names to list of instances (with multi-module filtering applied)
     */
    public Map<String, List<BuildSystemInstance>> detectDetailedBuildSystems(Map<String, List<Path>> foundFiles) {
        Map<String, List<BuildSystemInstance>> detailedInfo = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<Path>> entry : foundFiles.entrySet()) {
            String buildSystemName = entry.getKey();
            List<Path> buildFiles = entry.getValue();
            
            BuildSystemGeneratorRegistry.getGenerator(buildSystemName).ifPresent(generator -> {
                List<BuildSystemInstance> instances = new ArrayList<>();
                
                // First pass: identify multi-module roots by checking each file for multi-module markers
                Map<Path, List<String>> multiModuleRootsWithModules = new LinkedHashMap<>();
                for (Path buildFile : buildFiles) {
                    if (isFileMultiModule(buildFile, buildSystemName)) {
                        List<String> moduleNames = extractModuleNames(buildFile, buildSystemName);
                        multiModuleRootsWithModules.put(buildFile.getParent(), moduleNames);
                    }
                }
                
                // Second pass: create instances, excluding files that are declared child modules
                for (Path buildFile : buildFiles) {
                    Path buildFileParent = buildFile.getParent();
                    
                    // Check if this file is a child module of any multi-module root
                    boolean isChildModule = false;
                    
                    for (Map.Entry<Path, List<String>> rootEntry : multiModuleRootsWithModules.entrySet()) {
                        Path rootDir = rootEntry.getKey();
                        List<String> moduleNames = rootEntry.getValue();
                        
                        // If buildFileParent starts with rootDir and is not the same
                        if (buildFileParent.startsWith(rootDir) && !buildFileParent.equals(rootDir)) {
                            // Check if this is actually a declared child module
                            String relativePath = rootDir.relativize(buildFileParent).toString();
                            // Normalize path separators for comparison
                            String normalizedPath = relativePath.replace(File.separator, "/");
                            
                            for (String moduleName : moduleNames) {
                                String normalizedModule = moduleName.replace(File.separator, "/");
                                if (normalizedPath.equals(normalizedModule) || 
                                    normalizedPath.startsWith(normalizedModule + "/")) {
                                    isChildModule = true;
                                    break;
                                }
                            }
                            
                            if (isChildModule) break;
                        }
                    }
                    
                    // Skip files that are declared child modules
                    if (isChildModule) {
                        continue;
                    }
                    
                    // Determine if this specific file is a multi-module root
                    boolean isMultiModule = isFileMultiModule(buildFile, buildSystemName);
                    
                    instances.add(new BuildSystemInstance(
                        buildFile.toString(),
                        isMultiModule,
                        isMultiModule // If it's multi-module, it's a root
                    ));
                }
                
                if (!instances.isEmpty()) {
                    detailedInfo.put(buildSystemName, instances);
                }
            });
        }
        
        return detailedInfo;
    }

    /**
     * Extract module names from a multi-module build file.
     * 
     * @param buildFile The build file to parse
     * @param buildSystemName The name of the build system
     * @return List of module directory names
     */
    private List<String> extractModuleNames(Path buildFile, String buildSystemName) {
        List<String> modules = new ArrayList<>();
        
        try {
            String content = Files.readString(buildFile);
            
            if ("Maven".equals(buildSystemName)) {
                // Extract <module>...</module> entries
                int pos = 0;
                while ((pos = content.indexOf("<module>", pos)) >= 0) {
                    int endPos = content.indexOf("</module>", pos);
                    if (endPos > pos) {
                        String moduleName = content.substring(pos + 8, endPos).trim();
                        modules.add(moduleName);
                        pos = endPos;
                    } else {
                        break;
                    }
                }
            } else if ("Gradle".equals(buildSystemName)) {
                // Extract include statements
                // This is a simplified version - full Gradle parsing would be more complex
                String[] lines = content.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("include") && (line.contains("'") || line.contains("\""))) {
                        // Extract module name from include(':module-name') or include("module-name")
                        int startQuote = Math.max(line.indexOf("'"), line.indexOf("\""));
                        if (startQuote > 0) {
                            int endQuote = line.indexOf(line.charAt(startQuote), startQuote + 1);
                            if (endQuote > startQuote) {
                                String moduleName = line.substring(startQuote + 1, endQuote);
                                // Remove leading : if present
                                if (moduleName.startsWith(":")) {
                                    moduleName = moduleName.substring(1);
                                }
                                // Convert : to / for nested modules
                                moduleName = moduleName.replace(":", "/");
                                modules.add(moduleName);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        
        return modules;
    }

    /**
     * Check if a specific build file contains multi-module markers.
     * This is build-system specific logic.
     * 
     * @param buildFile The build file to check
     * @param buildSystemName The name of the build system
     * @return true if the file contains multi-module markers
     */
    private boolean isFileMultiModule(Path buildFile, String buildSystemName) {
        try {
            String content = Files.readString(buildFile);
            
            // Maven: check for <modules> or <module> tags
            if ("Maven".equals(buildSystemName)) {
                return content.contains("<modules>") || content.contains("<module>");
            }
            
            // Gradle: check for include( or include statements in settings.gradle
            if ("Gradle".equals(buildSystemName)) {
                String fileName = buildFile.getFileName().toString();
                if (fileName.equals("settings.gradle") || fileName.equals("settings.gradle.kts")) {
                    return content.contains("include(") || content.contains("include ");
                }
            }
            
            // Other build systems: not currently supported for multi-module detection
            return false;
            
        } catch (IOException e) {
            return false;
        }
    }

    private Map<String, ToolVersionInfo> detectBuildToolVersions(Set<String> detectedTools) {
        Map<String, ToolVersionInfo> versions = new LinkedHashMap<>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        for (String tool : detectedTools) {
            // Get the generator for this build system from the registry
            BuildSystemGeneratorRegistry.getGenerator(tool).ifPresent(generator -> {
                try {
                    String versionCommand = generator.getVersionCheckCommand();
                    if (versionCommand == null || versionCommand.isEmpty()) {
                        return; // Skip if no version command defined
                    }
                    
                    List<String> fullCommand = new ArrayList<>();
                    
                    // On Windows, run through cmd.exe to resolve PATH and .cmd/.bat files
                    if (isWindows) {
                        fullCommand.add("cmd.exe");
                        fullCommand.add("/c");
                        fullCommand.add(versionCommand);
                    } else {
                        fullCommand.add("sh");
                        fullCommand.add("-c");
                        fullCommand.add(versionCommand);
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
            });
        }
        return versions;
    }

    private Map<String, Long> countSourceFiles(Path root) throws IOException {
        Map<String, Long> counts = new LinkedHashMap<>();
        
        Set<String> excludeDirs = getExcludedDirectories();
        
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
                    if (FilePatterns.CONTAINER_BUILD_FILES.contains(name) || name.startsWith("dockerfile.")) {
                        return true;
                    }
                    
                    // Check if it has a source code extension
                    if (name.contains(".")) {
                        String ext = name.substring(name.lastIndexOf('.') + 1);
                        return FilePatterns.SOURCE_CODE_EXTENSIONS.contains(ext);
                    }
                    
                    return false;
                })
                .forEach(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    
                    // Special handling for container files
                    if (FilePatterns.CONTAINER_BUILD_FILES.contains(name) || name.startsWith("dockerfile.")) {
                        counts.merge("Dockerfile/Container", 1L, Long::sum);
                    } else {
                        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "<no extension>";
                        counts.merge(ext, 1L, Long::sum);
                    }
                });
        return counts;
    }



}
