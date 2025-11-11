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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralized file pattern definitions for scanning projects.
 * Contains patterns for IaC tools, source code extensions, and container files.
 */
public class FilePatterns {
    
    /**
     * Non-build-system patterns (IaC and source code categories for reporting)
     */
    public static final Map<String, List<String>> NON_BUILD_SYSTEM_PATTERNS = Map.ofEntries(
        // IaC
        Map.entry("Terraform", List.of("*.tf", "*.tf.json")),
        Map.entry("CloudFormation", List.of("template.yaml", "template.yml")),
        Map.entry("Ansible", List.of("ansible.cfg", "playbook.yml", "site.yml")),
        Map.entry("Kubernetes", List.of("deployment.yaml", "deployment.yml")),
        Map.entry("Docker", List.of("Dockerfile", "docker-compose.yml")),
        Map.entry("Pulumi", List.of("Pulumi.yaml", "Pulumi.yml")),
        // Source code
        Map.entry("Java", List.of("*.java")),
        Map.entry("Go Source", List.of("*.go")),
        Map.entry("Python Source", List.of("*.py")),
        Map.entry("C/C++", List.of("*.c", "*.cpp", "*.h", "*.hpp")),
        Map.entry("JavaScript", List.of("*.js", "*.jsx")),
        Map.entry("TypeScript", List.of("*.ts", "*.tsx")),
        Map.entry("Kotlin", List.of("*.kt", "*.kts")),
        Map.entry("C#", List.of("*.cs")),
        Map.entry("Rust Source", List.of("*.rs")),
        Map.entry("Ruby Source", List.of("*.rb")),
        Map.entry("Shell", List.of("*.sh")),
        Map.entry("YAML/JSON Config", List.of("*.yaml", "*.yml", "*.json"))
    );
    
    /**
     * Source code language extensions for file counting
     */
    public static final Set<String> SOURCE_CODE_EXTENSIONS = Set.of(
        // Programming languages
        "java", "kt", "kts",                    // Java, Kotlin
        "c", "cpp", "cc", "cxx", "h", "hpp",    // C/C++
        "cs", "vb",                             // C#, VB.NET
        "py", "pyw",                            // Python
        "js", "jsx", "ts", "tsx",               // JavaScript, TypeScript
        "go",                                   // Go
        "rs",                                   // Rust
        "rb",                                   // Ruby
        "php",                                  // PHP
        "swift",                                // Swift
        "m", "mm",                              // Objective-C
        "scala",                                // Scala
        "groovy",                               // Groovy
        "clj", "cljs",                          // Clojure
        "erl", "hrl",                           // Erlang
        "ex", "exs",                            // Elixir
        "lua",                                  // Lua
        "pl", "pm",                             // Perl
        "r",                                    // R
        "dart",                                 // Dart
        "f", "f90", "f95",                      // Fortran
        "asm", "s",                             // Assembly
        "sh", "bash", "zsh",                    // Shell scripts
        "ps1", "psm1",                          // PowerShell
        "bat", "cmd"                            // Batch scripts
    );
    
    /**
     * Container build files (exact filenames to match)
     */
    public static final Set<String> CONTAINER_BUILD_FILES = Set.of(
        "dockerfile",
        "containerfile",
        "docker-compose.yml",
        "docker-compose.yaml",
        ".dockerignore"
    );
    
    /**
     * Common build output directories to exclude from scanning
     */
    public static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
        "target",           // Maven
        "build",            // Gradle
        "bin",              // General binary output
        "out",              // IntelliJ IDEA output
        "dist",             // Distribution directories
        "node_modules",     // npm
        ".gradle",          // Gradle cache
        ".mvn",             // Maven wrapper
        "__pycache__",      // Python
        ".pytest_cache",    // Pytest
        "venv",             // Python virtual env
        ".venv",            // Python virtual env
        "env",              // Python virtual env
        ".tox",             // Tox
        "obj",              // .NET
        ".vs",              // Visual Studio
        ".idea",            // IntelliJ IDEA
        ".git",             // Git
        ".svn",             // SVN
        ".hg"               // Mercurial
    );
    
    private FilePatterns() {
        // Utility class - prevent instantiation
    }
}
