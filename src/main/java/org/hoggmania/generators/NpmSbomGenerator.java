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
package org.hoggmania.generators;

import org.hoggmania.BuildSystemSbomGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class NpmSbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "npm"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "package.json"; 
    }
    
    @Override
    public List<String> getExcludedDirectories() {
        return Arrays.asList("node_modules");
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "npm --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        String outputFile = projectName + "-bom.json";
        return String.format("npm sbom --sbom-format=cyclonedx > %s/%s",
            outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        String outputFile = projectName + "-bom.json";
        return String.format("npm sbom --sbom-format=cyclonedx --package-lock-only --prefix %s > %s/%s",
            buildFile.getParent().toAbsolutePath(), outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String extractProjectName(Path packageJson) {
        try {
            String content = Files.readString(packageJson);
            int nameStart = content.indexOf("\"name\"");
            if (nameStart > 0) {
                int colonPos = content.indexOf(":", nameStart);
                if (colonPos > 0) {
                    int quoteStart = content.indexOf("\"", colonPos);
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
        return "npm-project";
    }
    
    @Override
    public String mapErrorMessage(String errorOutput, int exitCode) {
        if (errorOutput.contains("Did you forget to run `npm install`")) {
            return "Missing node_modules - run 'npm install' first";
        }
        if (errorOutput.contains("No evidence: no package lock file")) {
            return "Missing package-lock.json - run 'npm install' to generate it";
        }
        return "npm SBOM generation failed with exit code: " + exitCode;
    }
}
