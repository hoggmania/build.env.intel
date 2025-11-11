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
import java.util.List;

public class MavenSbomGenerator implements BuildSystemSbomGenerator {
    @Override
    public String getBuildSystemName() { 
        return "Maven"; 
    }
    
    @Override
    public String getBuildFilePattern() { 
        return "pom.xml"; 
    }
    
    @Override
    public String getVersionCheckCommand() { 
        return "mvn --version"; 
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        return String.format("mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom " +
            "-DoutputFormat=json -DoutputDirectory=%s -DoutputName=%s-bom",
            outputDir.getAbsolutePath(), projectName);
    }
    
    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        return String.format("mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom " +
            "-DoutputFormat=json -DoutputDirectory=%s -DoutputName=%s-bom -f %s",
            outputDir.getAbsolutePath(), projectName, buildFile.toAbsolutePath());
    }
    
    @Override
    public String extractProjectName(Path pomFile) {
        try {
            String content = Files.readString(pomFile);
            int artifactIdStart = content.indexOf("<artifactId>");
            if (artifactIdStart > 0) {
                int artifactIdEnd = content.indexOf("</artifactId>", artifactIdStart);
                if (artifactIdEnd > artifactIdStart) {
                    return content.substring(artifactIdStart + 12, artifactIdEnd).trim();
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return "maven-project";
    }
    
    @Override
    public boolean isMultiModule(List<Path> pomFiles) {
        if (pomFiles.size() <= 1) return false;
        for (Path pomFile : pomFiles) {
            try {
                String content = Files.readString(pomFile);
                if (content.contains("<modules>") || content.contains("<module>")) {
                    return true;
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return false;
    }
}
