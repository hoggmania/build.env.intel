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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenSbomGeneratorTest {

    private final MavenSbomGenerator generator = new MavenSbomGenerator();

    @Test
    void testGetBuildSystemName() {
        assertEquals("Maven", generator.getBuildSystemName());
    }

    @Test
    void testGetBuildFilePattern() {
        assertEquals("pom.xml", generator.getBuildFilePattern());
    }

    @Test
    void testGetVersionCheckCommand() {
        assertEquals("mvn --version", generator.getVersionCheckCommand());
    }

    @Test
    void testGetAdditionalBuildFilePatterns() {
        assertTrue(generator.getAdditionalBuildFilePatterns().isEmpty());
    }

    @Test
    void testExtractProjectName(@TempDir Path tempDir) throws IOException {
        // Create a pom.xml with artifactId
        Path pomFile = tempDir.resolve("pom.xml");
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>my-test-project</artifactId>
                <version>1.0.0</version>
            </project>
            """;
        Files.writeString(pomFile, pomContent);

        String projectName = generator.extractProjectName(pomFile);
        assertEquals("my-test-project", projectName);
    }

    @Test
    void testExtractProjectNameFallback(@TempDir Path tempDir) throws IOException {
        // Create a pom.xml without artifactId
        Path pomFile = tempDir.resolve("pom.xml");
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <modelVersion>4.0.0</modelVersion>
            </project>
            """;
        Files.writeString(pomFile, pomContent);

        String projectName = generator.extractProjectName(pomFile);
        assertEquals("maven-project", projectName);
    }

    @Test
    void testIsMultiModuleSinglePom(@TempDir Path tempDir) throws IOException {
        // Create a single standalone pom.xml
        Path pomFile = tempDir.resolve("pom.xml");
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>standalone</artifactId>
            </project>
            """;
        Files.writeString(pomFile, pomContent);

        boolean isMultiModule = generator.isMultiModule(Collections.singletonList(pomFile));
        assertFalse(isMultiModule, "Single pom.xml should not be detected as multi-module");
    }

    @Test
    void testIsMultiModuleWithModulesTag(@TempDir Path tempDir) throws IOException {
        // Create a parent pom.xml with <modules> tag
        Path parentPom = tempDir.resolve("pom.xml");
        String parentContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>parent</artifactId>
                <packaging>pom</packaging>
                <modules>
                    <module>module1</module>
                    <module>module2</module>
                </modules>
            </project>
            """;
        Files.writeString(parentPom, parentContent);

        // Create module poms
        Path module1Dir = tempDir.resolve("module1");
        Files.createDirectories(module1Dir);
        Path module1Pom = module1Dir.resolve("pom.xml");
        Files.writeString(module1Pom, "<project><artifactId>module1</artifactId></project>");

        List<Path> pomFiles = Arrays.asList(parentPom, module1Pom);
        boolean isMultiModule = generator.isMultiModule(pomFiles);
        assertTrue(isMultiModule, "Pom with <modules> tag should be detected as multi-module");
    }

    @Test
    void testIsMultiModuleWithSingleModuleTag(@TempDir Path tempDir) throws IOException {
        // Create a parent pom.xml with single <module> tag (no wrapper <modules>)
        Path parentPom = tempDir.resolve("pom.xml");
        String parentContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>parent</artifactId>
                <module>child</module>
            </project>
            """;
        Files.writeString(parentPom, parentContent);

        // Create child pom to have multiple pom files
        Path childDir = tempDir.resolve("child");
        Files.createDirectories(childDir);
        Path childPom = childDir.resolve("pom.xml");
        Files.writeString(childPom, "<project><artifactId>child</artifactId></project>");

        List<Path> pomFiles = Arrays.asList(parentPom, childPom);
        boolean isMultiModule = generator.isMultiModule(pomFiles);
        assertTrue(isMultiModule, "Pom with <module> tag should be detected as multi-module");
    }

    @Test
    void testIsMultiModuleMultiplePomWithoutModules(@TempDir Path tempDir) throws IOException {
        // Create multiple pom.xml files but none have <modules> tag
        Path pom1 = tempDir.resolve("project1").resolve("pom.xml");
        Files.createDirectories(pom1.getParent());
        Files.writeString(pom1, "<project><artifactId>project1</artifactId></project>");

        Path pom2 = tempDir.resolve("project2").resolve("pom.xml");
        Files.createDirectories(pom2.getParent());
        Files.writeString(pom2, "<project><artifactId>project2</artifactId></project>");

        List<Path> pomFiles = Arrays.asList(pom1, pom2);
        boolean isMultiModule = generator.isMultiModule(pomFiles);
        assertFalse(isMultiModule, "Multiple poms without <modules> tag should not be multi-module");
    }

    @Test
    void testGenerateSbomCommand(@TempDir Path tempDir) {
        String projectName = "test-project";
        String command = generator.generateSbomCommand(projectName, tempDir.toFile());

        assertTrue(command.contains("mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"));
        assertTrue(command.contains("-DoutputFormat=json"));
        assertTrue(command.contains("-DoutputDirectory=" + tempDir.toAbsolutePath()));
        assertTrue(command.contains("-DoutputName=test-project-bom"));
    }

    @Test
    void testIsMultiModuleEmptyList() {
        boolean isMultiModule = generator.isMultiModule(Collections.emptyList());
        assertFalse(isMultiModule, "Empty list should not be multi-module");
    }

    @Test
    void testExtractProjectNameInvalidFile(@TempDir Path tempDir) {
        Path nonExistentFile = tempDir.resolve("nonexistent.xml");
        String projectName = generator.extractProjectName(nonExistentFile);
        assertEquals("maven-project", projectName, "Should return fallback name for invalid file");
    }

    @Test
    void testIsMultiModuleWithIOError(@TempDir Path tempDir) throws IOException {
        // Create a directory instead of a file to cause IOException
        Path invalidPom = tempDir.resolve("pom.xml");
        Files.createDirectories(invalidPom);

        boolean isMultiModule = generator.isMultiModule(Collections.singletonList(invalidPom));
        assertFalse(isMultiModule, "Should return false on IOException");
    }
}
