package org.hoggmania.generators;

import org.hoggmania.BuildSystemSbomGenerator;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Generator for creating SBOMs from standalone binaries using Syft.
 * Used when no package manager is detected in the project.
 */
public class JarSbomGenerator implements BuildSystemSbomGenerator {

    @Override
    public String getBuildSystemName() {
        return "Standalone Binaries (Syft)";
    }

    @Override
    public String getBuildFilePattern() {
        return "*.jar";
    }

    @Override
    public String getVersionCheckCommand() {
        return "syft version";
    }

    @Override
    public String generateSbomCommand(String projectName, File outputDir) {
        return String.format("syft scan dir:. -o cyclonedx-json=%s",
            new File(outputDir, projectName + "-bom.json").getAbsolutePath());
    }

    @Override
    public String generateSbomCommand(String projectName, File outputDir, Path buildFile) {
        Path scanDir = buildFile.getParent() != null ? buildFile.getParent() : Path.of(".");
        return String.format("syft scan dir:%s -o cyclonedx-json=%s",
            scanDir.toAbsolutePath(),
            new File(outputDir, projectName + "-bom.json").getAbsolutePath());
    }

    @Override
    public String extractProjectName(Path buildFile) {
        Path parent = buildFile.getParent();
        if (parent != null && parent.getFileName() != null) {
            return parent.getFileName().toString();
        }
        return "binary-scan";
    }

    @Override
    public boolean isMultiModule(List<Path> buildFiles) {
        return false;
    }
}
