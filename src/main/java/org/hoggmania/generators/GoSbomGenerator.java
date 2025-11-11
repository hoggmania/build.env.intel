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

public class GoSbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "Go"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "go.mod"; 
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "go version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        String outputFile = projectName + "-bom.json";
        return String.format("go list -m -json all > %s/%s",
            outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        String outputFile = projectName + "-bom.json";
        // Go commands need to run in the directory with go.mod
        return String.format("cd %s && go list -m -json all > %s/%s",
            buildFile.getParent().toAbsolutePath(), outputDir.getAbsolutePath(), outputFile);
    }
    
    @Override
    public String extractProjectName(Path goMod) {
        try {
            String content = Files.readString(goMod);
            int moduleStart = content.indexOf("module ");
            if (moduleStart >= 0) {
                int lineEnd = content.indexOf("\n", moduleStart);
                if (lineEnd > moduleStart) {
                    String modulePath = content.substring(moduleStart + 7, lineEnd).trim();
                    int lastSlash = modulePath.lastIndexOf('/');
                    return lastSlash > 0 ? modulePath.substring(lastSlash + 1) : modulePath;
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "go-project";
    }
}
