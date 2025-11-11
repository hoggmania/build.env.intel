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

public class GradleSbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "Gradle"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "build.gradle"; 
    }
    
    @Override
    public List<String> getAdditionalBuildFilePatterns() {
        return Arrays.asList("build.gradle.kts");
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "gradle --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        return String.format("gradle cyclonedxBom " +
            "-PcyclonedxOutputFormat=json -PcyclonedxOutputDirectory=%s -PcyclonedxOutputName=%s-bom",
            outputDir.getAbsolutePath(), projectName);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        return String.format("gradle cyclonedxBom " +
            "-PcyclonedxOutputFormat=json -PcyclonedxOutputDirectory=%s -PcyclonedxOutputName=%s-bom -b %s",
            outputDir.getAbsolutePath(), projectName, buildFile.toAbsolutePath());
    }
    
    @Override
    public String extractProjectName(Path buildFile) {
        Path settingsFile = buildFile.getParent().resolve("settings.gradle");
        if (!Files.exists(settingsFile)) {
            settingsFile = buildFile.getParent().resolve("settings.gradle.kts");
        }
        
        if (Files.exists(settingsFile)) {
            try {
                String content = Files.readString(settingsFile);
                int nameStart = content.indexOf("rootProject.name");
                if (nameStart > 0) {
                    int quoteStart = content.indexOf("\"", nameStart);
                    if (quoteStart > 0) {
                        int quoteEnd = content.indexOf("\"", quoteStart + 1);
                        if (quoteEnd > 0) {
                            return content.substring(quoteStart + 1, quoteEnd).trim();
                        }
                    }
                    int singleQuoteStart = content.indexOf("'", nameStart);
                    if (singleQuoteStart > 0) {
                        int singleQuoteEnd = content.indexOf("'", singleQuoteStart + 1);
                        if (singleQuoteEnd > 0) {
                            return content.substring(singleQuoteStart + 1, singleQuoteEnd).trim();
                        }
                    }
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return buildFile.getParent().getFileName().toString();
    }
    
    @Override
    public boolean isMultiModule(List<Path> buildFiles) {
        for (Path buildFile : buildFiles) {
            Path settingsFile = buildFile.getParent().resolve("settings.gradle");
            if (!Files.exists(settingsFile)) {
                settingsFile = buildFile.getParent().resolve("settings.gradle.kts");
            }
            
            if (Files.exists(settingsFile)) {
                try {
                    String content = Files.readString(settingsFile);
                    if (content.contains("include(") || content.contains("include ")) {
                        return true;
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return false;
    }
}
