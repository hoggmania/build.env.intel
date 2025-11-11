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

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Interface for build system specific SBOM generation strategies
 */
public interface BuildSystemSbomGenerator {
    /**
     * Get the build system name (e.g., "Maven", "npm", "Go")
     */
    String getBuildSystemName();
    
    /**
     * Generate the SBOM command for this build system
     * @deprecated Use {@link #generateSbomCommand(String, File, Path)} instead
     */
    @Deprecated
    String generateSbomCommand(String projectName, File outputDir);
    
    /**
     * Generate the SBOM command for this build system with the build file path.
     * Default implementation delegates to the deprecated method for backward compatibility.
     */
    default String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        return generateSbomCommand(projectName, outputDir);
    }
    
    /**
     * Extract project name from build files
     */
    String extractProjectName(Path primaryBuildFile);
    
    /**
     * Get the primary build file name pattern (e.g., "pom.xml", "package.json")
     */
    String getBuildFilePattern();
    
    /**
     * Get additional build file patterns (e.g., for .NET: *.csproj, *.vbproj)
     */
    default List<String> getAdditionalBuildFilePatterns() {
        return Collections.emptyList();
    }
    
    /**
     * Get directories to exclude when searching for build files
     */
    default List<String> getExcludedDirectories() {
        return Collections.emptyList();
    }
    
    /**
     * Check if this is a multi-module project
     */
    default boolean isMultiModule(List<Path> buildFiles) {
        return buildFiles.size() > 1;
    }
    
    /**
     * Prepare the build environment before SBOM generation (e.g., download dependencies)
     */
    default boolean prepareEnvironment(BuildSystemInfo buildInfo, File rootDir) {
        return true; // No preparation needed by default
    }
    
    /**
     * Map error output to user-friendly error messages
     */
    default String mapErrorMessage(String errorOutput, int exitCode) {
        return "SBOM generation failed with exit code: " + exitCode;
    }
    
    /**
     * Get the version check command for this build tool
     */
    String getVersionCheckCommand();
}
