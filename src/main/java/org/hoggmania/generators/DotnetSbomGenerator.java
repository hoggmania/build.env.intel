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

public class DotnetSbomGenerator implements BuildSystemSbomGenerator {
    
    @Override
    public String getBuildSystemName() { 
        return ".NET"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "*.csproj"; 
    }
    
    @Override
    public List<String> getAdditionalBuildFilePatterns() {
        return Arrays.asList("*.vbproj", "*.fsproj", "*.sln");
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "dotnet --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        // Note: For .NET, the actual project file name is needed at generation time
        // This will be handled by passing the build file info
        return String.format("dotnet CycloneDX -o %s -f json -n %s-bom",
            outputDir.getAbsolutePath(), projectName);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        // Use --json flag and -o for output file path (not directory)
        // The tool expects full output path including filename
        String outputFile = new File(outputDir, projectName + "-bom.json").getAbsolutePath();
        return String.format("dotnet CycloneDX %s --json -o %s",
            buildFile.toAbsolutePath(), outputFile);
    }
    
    @Override
    public String extractProjectName(Path projectFile) {
        try {
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
        String fileName = projectFile.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    @Override
    public String mapErrorMessage(String errorOutput, int exitCode) {
        if (errorOutput.contains("contains more than one project file")) {
            return "Multiple project files found - specify which project to analyze";
        }
        return ".NET SBOM generation failed with exit code: " + exitCode;
    }
}
