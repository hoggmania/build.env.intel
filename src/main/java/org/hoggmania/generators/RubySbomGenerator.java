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

public class RubySbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "Ruby"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "Gemfile"; 
    }
    
    @Override
    public List<String> getExcludedDirectories() {
        return Arrays.asList("vendor", ".bundle");
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "gem --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        String outputFile = projectName + "-bom.json";
        return String.format("cyclonedx-ruby -o %s/%s -t json",
            outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        String outputFile = projectName + "-bom.json";
        return String.format("cyclonedx-ruby -p %s -o %s/%s -t json",
            buildFile.getParent().toAbsolutePath(), outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String extractProjectName(Path root) {
        try {
            List<Path> gemspecFiles = Files.find(root, 2,
                (p, attr) -> p.getFileName().toString().endsWith(".gemspec")).toList();
            if (!gemspecFiles.isEmpty()) {
                String fileName = gemspecFiles.get(0).getFileName().toString();
                return fileName.substring(0, fileName.lastIndexOf('.'));
            }
        } catch (IOException e) {
            // Ignore
        }
        return root.getFileName().toString();
    }
}
