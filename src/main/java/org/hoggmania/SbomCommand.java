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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command to generate SBOM (Software Bill of Materials) using appropriate CycloneDX plugin.
 * 
 * <p>This command leverages {@link EnvScannerCommand} for environment detection and build system
 * discovery, eliminating code duplication and ensuring consistent detection logic across both
 * scan and sbom commands.</p>
 * 
 * <p>Architecture:
 * <ul>
 *   <li>Uses {@link EnvScannerCommand#scanFiles(Path)} to detect build systems</li>
 *   <li>Uses {@link EnvScannerCommand#detectMultiModuleBuilds(Map)} to determine multi-module status</li>
 *   <li>Delegates SBOM generation to build-system-specific generators in org.hoggmania.generators</li>
 * </ul>
 * </p>
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

    @RegisterForReflection
    static class SbomGenerationResult {
        BuildSystemInfo buildInfo;
        boolean success;
        String output;
        String errorOutput;
        String expectedSbomPath;
        boolean sbomFileExists;
        long sbomFileSize;
        String timestamp;
        int exitCode;
        String errorMessage;

        public SbomGenerationResult(BuildSystemInfo buildInfo) {
            this.buildInfo = buildInfo;
            this.timestamp = java.time.Instant.now().toString();
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
            
            // Use EnvScannerCommand to scan for build systems
            EnvScannerCommand scanner = new EnvScannerCommand();
            Map<String, List<Path>> foundFiles = scanner.scanFiles(rootDir.toPath());
            
            // Use detailed build system detection to get filtered instances (excludes child modules)
            Map<String, List<EnvScannerCommand.BuildSystemInstance>> detailedBuildSystems = 
                scanner.detectDetailedBuildSystems(foundFiles);
            
            List<BuildSystemInfo> buildSystems = convertToBuildSystemInfo(detailedBuildSystems);
            
            if (buildSystems.isEmpty()) {
                System.err.println(ConsoleColors.error("[ERROR]") + " No supported build system detected");
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

            System.out.println(ConsoleColors.bold("\n=== Build Systems Detected: " + buildSystems.size() + " ==="));
            for (BuildSystemInfo buildInfo : buildSystems) {
                System.out.println("\n--- " + ConsoleColors.info(buildInfo.buildSystem) + " ---");
                System.out.println("Project Name: " + ConsoleColors.highlight(buildInfo.projectName));
                System.out.println("Multi-Module: " + buildInfo.multiModule);
                System.out.println("Build Files Found: " + buildInfo.buildFiles.size());
                buildInfo.buildFiles.forEach(f -> System.out.println("  - " + f));
                System.out.println("Working Directory: " + (buildInfo.workingDirectory != null ? buildInfo.workingDirectory : rootDir.toPath()));
                System.out.println("Command: " + buildInfo.pluginCommand);
            }
            
            if (dryRun) {
                System.out.println(ConsoleColors.bold("\n[DRY-RUN]") + " Would execute the following commands:");
                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                for (BuildSystemInfo buildInfo : buildSystems) {
                    System.out.println("\n--- " + ConsoleColors.info(buildInfo.buildSystem) + " ---");
                    System.out.println("Project Name: " + ConsoleColors.highlight(buildInfo.projectName));
                    File workDir = buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toFile() : rootDir;
                    System.out.println("Working Directory: " + workDir.getAbsolutePath());
                    System.out.println("\nBuild Files:");
                    buildInfo.buildFiles.forEach(f -> System.out.println("  - " + f));
                    System.out.println("\nCommand to execute:");
                    if (isWindows) {
                        System.out.println("  cmd.exe /c " + buildInfo.pluginCommand);
                    } else {
                        System.out.println("  sh -c " + buildInfo.pluginCommand);
                    }
                }
                writeSummaryJson(buildSystems, true, new ArrayList<>());
                return;
            }

            System.out.println(ConsoleColors.bold("\n=== Generating SBOMs ==="));
            List<Boolean> results = new ArrayList<>();
            List<SbomGenerationResult> generationResults = new ArrayList<>();
            for (BuildSystemInfo buildInfo : buildSystems) {
                System.out.println("\n--- Generating SBOM for " + ConsoleColors.info(buildInfo.buildSystem) + " ---");
                SbomGenerationResult result = generateSbomWithDetails(buildInfo);
                results.add(result.success);
                generationResults.add(result);
            }

            if (merge && buildSystems.size() > 1) {
                System.out.println(ConsoleColors.bold("\n=== Merging SBOMs ==="));
                mergeSboms();
            }

            // Always write summary JSON
            writeSummaryJson(buildSystems, false, results);
            
            // Write aggregate log
            writeAggregateLog(generationResults);

        } catch (IOException e) {
            throw new RuntimeException("Error inspecting environment", e);
        }
    }

    /**
     * Convert the detailed build system instances from EnvScannerCommand into BuildSystemInfo objects
     * that contain the necessary information for SBOM generation.
     * 
     * Uses BuildSystemGeneratorRegistry to abstract build system-specific logic.
     * Only processes build systems that are not filtered out (i.e., multi-module roots and standalone projects).
     */
    private List<BuildSystemInfo> convertToBuildSystemInfo(
            Map<String, List<EnvScannerCommand.BuildSystemInstance>> detailedBuildSystems) throws IOException {
        List<BuildSystemInfo> buildSystems = new ArrayList<>();
        
        for (Map.Entry<String, List<EnvScannerCommand.BuildSystemInstance>> entry : detailedBuildSystems.entrySet()) {
            String buildSystemName = entry.getKey();
            List<EnvScannerCommand.BuildSystemInstance> instances = entry.getValue();
            
            // Process each instance (these are already filtered - child modules excluded)
            for (EnvScannerCommand.BuildSystemInstance instance : instances) {
                // Get the generator for this build system
                BuildSystemGeneratorRegistry.getGenerator(buildSystemName).ifPresent(generator -> {
                    Path buildFilePath = Paths.get(instance.getPath());
                    
                    // Extract project name using generator
                    String projectName = generator.extractProjectName(buildFilePath);
                    
                    // Generate SBOM command using generator with absolute build file path
                    String sbomCommand = generator.generateSbomCommand(projectName, outputDir, buildFilePath);
                    
                    // Create BuildSystemInfo
                    BuildSystemInfo info = new BuildSystemInfo(buildSystemName, sbomCommand);
                    info.buildFiles = Collections.singletonList(instance.getPath());
                    info.multiModule = instance.isMultiModule();
                    info.projectName = projectName;
                    
                    // Set working directory to build file's parent directory
                    // (where settings.gradle/pom.xml/package.json etc. are located)
                    info.workingDirectory = buildFilePath.getParent();
                    
                    buildSystems.add(info);
                });
            }
        }

        return buildSystems;
    }

    private SbomGenerationResult generateSbomWithDetails(BuildSystemInfo buildInfo) {
        SbomGenerationResult result = new SbomGenerationResult(buildInfo);
        
        // First check if the required build tool is available
        if (!checkBuildToolAvailable(buildInfo.buildSystem)) {
            result.success = false;
            result.errorMessage = "Build tool not available for " + buildInfo.buildSystem + 
                            "\nPlease ensure the required tool is installed and in your PATH";
            System.err.println(ConsoleColors.error("[ERROR]") + " " + result.errorMessage);
            writeSbomLogFile(result);
            return result;
        }
        
        // For Go projects, ensure dependencies are downloaded first
        if ("Go".equals(buildInfo.buildSystem)) {
            if (!prepareGoModule(buildInfo)) {
                System.err.println(ConsoleColors.warning("[WARNING]") + " Failed to prepare Go module, continuing anyway...");
            }
        }
        
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        File workDir = buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toFile() : rootDir;
        StringBuilder outputCapture = new StringBuilder();
        StringBuilder errorCapture = new StringBuilder();
        
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
            pb.directory(workDir);
            pb.redirectErrorStream(false);
            
            System.out.println("Executing: " + buildInfo.pluginCommand);
            System.out.println("Working directory: " + workDir.getAbsolutePath());
            System.out.println();
            
            Process process = pb.start();
            
            // Capture stdout and stderr
            Thread outputThread = new Thread(() -> {
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        outputCapture.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            Thread errorThread = new Thread(() -> {
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                        errorCapture.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            outputThread.start();
            errorThread.start();
            
            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();
            
            result.exitCode = exitCode;
            result.output = outputCapture.toString();
            result.errorOutput = errorCapture.toString();
            result.expectedSbomPath = getExpectedSbomPath(buildInfo);
            
            File sbomFile = new File(result.expectedSbomPath);
            result.sbomFileExists = sbomFile.exists();
            if (sbomFile.exists()) {
                result.sbomFileSize = sbomFile.length();
            }
            
            if (exitCode == 0) {
                result.success = true;
                System.out.println("\n" + ConsoleColors.success("[SUCCESS]") + " SBOM generated successfully");
                System.out.println("Output location: " + ConsoleColors.highlight(outputDir.getAbsolutePath()));
            } else {
                result.success = false;
                result.errorMessage = "SBOM generation failed with exit code: " + exitCode;
                System.err.println("\n" + ConsoleColors.error("[ERROR]") + " " + result.errorMessage);
            }
            
            writeSbomLogFile(result);
            return result;
            
        } catch (IOException | InterruptedException e) {
            result.success = false;
            result.errorMessage = "Failed to execute SBOM generation: " + e.getMessage();
            result.output = outputCapture.toString();
            result.errorOutput = errorCapture.toString();
            System.err.println(ConsoleColors.error("[ERROR]") + " " + result.errorMessage);
            e.printStackTrace();
            writeSbomLogFile(result);
            return result;
        }
    }

    private String getExpectedSbomPath(BuildSystemInfo buildInfo) {
        String sbomFileName = buildInfo.projectName + "-bom.json";
        return new File(outputDir, sbomFileName).getAbsolutePath();
    }

    private void writeSbomLogFile(SbomGenerationResult result) {
        try {
            BuildSystemInfo buildInfo = result.buildInfo;
            String logFileName = buildInfo.projectName + "-" + buildInfo.buildSystem.toLowerCase().replace(" ", "-") + "-sbom-log.json";
            File logFile = new File(outputDir, logFileName);
            
            File workDir = buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toFile() : rootDir;
            
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode logData = mapper.createObjectNode();
            logData.put("buildSystem", buildInfo.buildSystem);
            logData.put("projectName", buildInfo.projectName);
            logData.put("success", result.success);
            logData.put("timestamp", result.timestamp);
            logData.put("workingDirectory", workDir.getAbsolutePath());
            logData.put("command", buildInfo.pluginCommand);
            logData.put("outputDirectory", outputDir.getAbsolutePath());
            
            if (result.exitCode != 0 || !result.success) {
                logData.put("exitCode", result.exitCode);
            }
            
            if (result.expectedSbomPath != null) {
                logData.put("expectedSbomPath", result.expectedSbomPath);
                logData.put("sbomFileExists", result.sbomFileExists);
                if (result.sbomFileExists) {
                    logData.put("sbomFileSize", result.sbomFileSize);
                }
            }
            
            ArrayNode buildFilesArray = mapper.createArrayNode();
            buildInfo.buildFiles.forEach(buildFilesArray::add);
            logData.set("buildFiles", buildFilesArray);
            
            logData.put("multiModule", buildInfo.multiModule);
            
            if (result.output != null && !result.output.trim().isEmpty()) {
                logData.put("stdout", result.output);
            }
            
            if (result.errorOutput != null && !result.errorOutput.trim().isEmpty()) {
                logData.put("stderr", result.errorOutput);
            }
            
            if (result.errorMessage != null && !result.errorMessage.trim().isEmpty()) {
                logData.put("errorMessage", result.errorMessage);
            }
            
            mapper.writerWithDefaultPrettyPrinter().writeValue(logFile, logData);
            System.out.println("Log file created: " + ConsoleColors.highlight(logFile.getAbsolutePath()));
            
        } catch (IOException e) {
            System.err.println(ConsoleColors.warning("[WARNING]") + " Failed to write SBOM log file: " + e.getMessage());
        }
    }

    private void writeAggregateLog(List<SbomGenerationResult> results) {
        try {
            File aggregateLogFile = new File(outputDir, "sbom-generation-aggregate.json");
            
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode aggregateData = mapper.createObjectNode();
            
            aggregateData.put("timestamp", java.time.Instant.now().toString());
            aggregateData.put("rootDirectory", rootDir.getAbsolutePath());
            aggregateData.put("outputDirectory", outputDir.getAbsolutePath());
            aggregateData.put("totalBuildSystems", results.size());
            
            int successCount = 0;
            int failureCount = 0;
            long totalSbomSize = 0;
            
            for (SbomGenerationResult result : results) {
                if (result.success) {
                    successCount++;
                    if (result.sbomFileExists) {
                        totalSbomSize += result.sbomFileSize;
                    }
                } else {
                    failureCount++;
                }
            }
            
            aggregateData.put("successfulGenerations", successCount);
            aggregateData.put("failedGenerations", failureCount);
            aggregateData.put("totalSbomSize", totalSbomSize);
            
            // Add summary array
            ArrayNode summaryArray = mapper.createArrayNode();
            for (SbomGenerationResult result : results) {
                ObjectNode summaryItem = mapper.createObjectNode();
                summaryItem.put("buildSystem", result.buildInfo.buildSystem);
                summaryItem.put("projectName", result.buildInfo.projectName);
                summaryItem.put("success", result.success);
                summaryItem.put("multiModule", result.buildInfo.multiModule);
                
                if (result.expectedSbomPath != null) {
                    summaryItem.put("sbomPath", result.expectedSbomPath);
                    summaryItem.put("sbomExists", result.sbomFileExists);
                    if (result.sbomFileExists) {
                        summaryItem.put("sbomSize", result.sbomFileSize);
                    }
                }
                
                if (!result.success && result.errorMessage != null) {
                    summaryItem.put("error", result.errorMessage);
                }
                
                if (result.exitCode != 0) {
                    summaryItem.put("exitCode", result.exitCode);
                }
                
                File workDir = result.buildInfo.workingDirectory != null ? result.buildInfo.workingDirectory.toFile() : rootDir;
                summaryItem.put("workingDirectory", workDir.getAbsolutePath());
                
                String logFileName = result.buildInfo.projectName + "-" + result.buildInfo.buildSystem.toLowerCase().replace(" ", "-") + "-sbom-log.json";
                summaryItem.put("detailedLogFile", logFileName);
                
                summaryArray.add(summaryItem);
            }
            
            aggregateData.set("buildSystems", summaryArray);
            
            mapper.writerWithDefaultPrettyPrinter().writeValue(aggregateLogFile, aggregateData);
            
            System.out.println(ConsoleColors.bold("\n=== SBOM Generation Summary ==="));
            System.out.println("Total build systems: " + results.size());
            System.out.println(ConsoleColors.success("Successful: " + successCount));
            if (failureCount > 0) {
                System.out.println(ConsoleColors.error("Failed: " + failureCount));
            } else {
                System.out.println("Failed: " + failureCount);
            }
            System.out.println("Aggregate log: " + ConsoleColors.highlight(aggregateLogFile.getAbsolutePath()));
            
        } catch (IOException e) {
            System.err.println(ConsoleColors.warning("[WARNING]") + " Failed to write aggregate log file: " + e.getMessage());
        }
    }

    private boolean prepareGoModule(BuildSystemInfo buildInfo) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        File workDir = buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toFile() : rootDir;
        
        try {
            List<String> command = new ArrayList<>();
            
            if (isWindows) {
                command.add("cmd.exe");
                command.add("/c");
            } else {
                command.add("sh");
                command.add("-c");
            }
            
            command.add("go mod download");
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            
            System.out.println("[INFO] Downloading Go module dependencies...");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
            
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private boolean checkBuildToolAvailable(String buildSystem) {
        // Get the version check command from the registry
        String command = BuildSystemGeneratorRegistry.getGenerator(buildSystem)
                .map(BuildSystemSbomGenerator::getVersionCheckCommand)
                .orElse(null);
        
        if (command == null) {
            return true; // Skip check if no version command defined
        }

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        try {
            List<String> cmd = new ArrayList<>();
            
            if (isWindows) {
                cmd.add("cmd.exe");
                cmd.add("/c");
            } else {
                cmd.add("sh");
                cmd.add("-c");
            }
            
            cmd.add(command);
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
            
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private void writeSummaryJson(List<BuildSystemInfo> buildSystems, boolean dryRun, List<Boolean> results) throws IOException {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", new Date().toString());
        summary.put("rootDirectory", rootDir.getAbsolutePath());
        summary.put("outputDirectory", outputDir.getAbsolutePath());
        summary.put("buildSystemsDetected", buildSystems.size());
        summary.put("format", "json");
        summary.put("dryRun", dryRun);
        
        // Add details for each build system
        List<Map<String, Object>> buildSystemDetails = new ArrayList<>();
        for (int i = 0; i < buildSystems.size(); i++) {
            BuildSystemInfo buildInfo = buildSystems.get(i);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("buildSystem", buildInfo.buildSystem);
            details.put("projectName", buildInfo.projectName);
            details.put("multiModule", buildInfo.multiModule);
            details.put("buildFiles", buildInfo.buildFiles);
            details.put("workingDirectory", buildInfo.workingDirectory != null ? buildInfo.workingDirectory.toString() : null);
            details.put("pluginCommand", buildInfo.pluginCommand);
            if (!dryRun && i < results.size()) {
                details.put("generationSuccess", results.get(i));
            }
            buildSystemDetails.add(details);
        }
        summary.put("buildSystems", buildSystemDetails);
        
        if (!dryRun) {
            // Calculate overall success
            boolean allSuccess = results.stream().allMatch(Boolean::booleanValue);
            summary.put("overallSuccess", allSuccess);
            
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
        System.out.println("\n" + ConsoleColors.success("[SUCCESS]") + " Summary written to " + 
            ConsoleColors.highlight(summaryFile.getAbsolutePath()));
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
