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

import org.hoggmania.generators.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry for all build system generators.
 * Provides a single point to access build system-specific logic.
 */
public class BuildSystemGeneratorRegistry {
    
    private static final Map<String, BuildSystemSbomGenerator> GENERATORS = new HashMap<>();
    
    static {
        // Register all build system generators
        registerGenerator(new MavenSbomGenerator());
        registerGenerator(new GradleSbomGenerator());
        registerGenerator(new NpmSbomGenerator());
        registerGenerator(new PythonSbomGenerator());
        registerGenerator(new GoSbomGenerator());
        registerGenerator(new DotnetSbomGenerator());
        registerGenerator(new RustSbomGenerator());
        registerGenerator(new PhpSbomGenerator());
        registerGenerator(new RubySbomGenerator());
    }
    
    private static void registerGenerator(BuildSystemSbomGenerator generator) {
        GENERATORS.put(generator.getBuildSystemName(), generator);
    }
    
    /**
     * Get a build system generator by name
     * @param buildSystemName The name of the build system (e.g., "Maven", "Gradle")
     * @return Optional containing the generator if found
     */
    public static Optional<BuildSystemSbomGenerator> getGenerator(String buildSystemName) {
        return Optional.ofNullable(GENERATORS.get(buildSystemName));
    }
    
    /**
     * Get all registered generators
     * @return Map of build system names to their generators
     */
    public static Map<String, BuildSystemSbomGenerator> getAllGenerators() {
        return new HashMap<>(GENERATORS);
    }
    
    /**
     * Check if a build system is supported
     * @param buildSystemName The name of the build system
     * @return true if the build system has a registered generator
     */
    public static boolean isSupported(String buildSystemName) {
        return GENERATORS.containsKey(buildSystemName);
    }
}
