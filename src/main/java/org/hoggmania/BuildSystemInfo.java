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

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
public class BuildSystemInfo {
    public String buildSystem;
    public String pluginCommand;
    public String version;
    public boolean multiModule;
    public List<String> buildFiles;
    public String projectName;
    public Path workingDirectory;

    public BuildSystemInfo(String buildSystem, String pluginCommand) {
        this.buildSystem = buildSystem;
        this.pluginCommand = pluginCommand;
        this.buildFiles = new ArrayList<>();
        this.projectName = "project";
    }
    
    // Getters and setters for reflection
    public String getBuildSystem() { return buildSystem; }
    public void setBuildSystem(String buildSystem) { this.buildSystem = buildSystem; }
    
    public String getPluginCommand() { return pluginCommand; }
    public void setPluginCommand(String pluginCommand) { this.pluginCommand = pluginCommand; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public boolean isMultiModule() { return multiModule; }
    public void setMultiModule(boolean multiModule) { this.multiModule = multiModule; }
    
    public List<String> getBuildFiles() { return buildFiles; }
    public void setBuildFiles(List<String> buildFiles) { this.buildFiles = buildFiles; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public Path getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(Path workingDirectory) { this.workingDirectory = workingDirectory; }
}
