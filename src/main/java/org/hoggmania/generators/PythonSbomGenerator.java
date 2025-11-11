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

public class PythonSbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "Python"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "setup.py"; 
    }
    
    @Override
    public List<String> getAdditionalBuildFilePatterns() {
        return Arrays.asList("pyproject.toml", "requirements.txt");
    }
    
    @Override
    public List<String> getExcludedDirectories() {
        return Arrays.asList("venv", ".venv", "env", "site-packages");
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "python --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        String outputFile = projectName + "-bom.json";
        return String.format("cyclonedx-py --format json --output %s/%s",
            outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        String outputFile = projectName + "-bom.json";
        // cyclonedx-py needs to run in the directory with the build file
        return String.format("cd %s && cyclonedx-py --format json --output %s/%s",
            buildFile.getParent().toAbsolutePath(), outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String extractProjectName(Path buildFile) {
        try {
            String content = Files.readString(buildFile);
            String fileName = buildFile.getFileName().toString();
            
            if (fileName.equals("setup.py")) {
                int nameStart = content.indexOf("name=");
                if (nameStart > 0) {
                    int quoteStart = content.indexOf("\"", nameStart);
                    if (quoteStart > 0) {
                        int quoteEnd = content.indexOf("\"", quoteStart + 1);
                        if (quoteEnd > 0) {
                            return content.substring(quoteStart + 1, quoteEnd).trim();
                        }
                    }
                }
            } else if (fileName.equals("pyproject.toml")) {
                int nameStart = content.indexOf("name =");
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
        return "python-project";
    }
}
