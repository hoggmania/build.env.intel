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

@RegisterForReflection
public class SbomGenerationResult {
    public BuildSystemInfo buildInfo;
    public boolean success;
    public String output;
    public String errorOutput;
    public String expectedSbomPath;
    public boolean sbomFileExists;
    public long sbomFileSize;
    public String timestamp;
    public int exitCode;
    public String errorMessage;

    public SbomGenerationResult(BuildSystemInfo buildInfo) {
        this.buildInfo = buildInfo;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    // Getters and setters for reflection
    public BuildSystemInfo getBuildInfo() { return buildInfo; }
    public void setBuildInfo(BuildSystemInfo buildInfo) { this.buildInfo = buildInfo; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    
    public String getErrorOutput() { return errorOutput; }
    public void setErrorOutput(String errorOutput) { this.errorOutput = errorOutput; }
    
    public String getExpectedSbomPath() { return expectedSbomPath; }
    public void setExpectedSbomPath(String expectedSbomPath) { this.expectedSbomPath = expectedSbomPath; }
    
    public boolean isSbomFileExists() { return sbomFileExists; }
    public void setSbomFileExists(boolean sbomFileExists) { this.sbomFileExists = sbomFileExists; }
    
    public long getSbomFileSize() { return sbomFileSize; }
    public void setSbomFileSize(long sbomFileSize) { this.sbomFileSize = sbomFileSize; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
