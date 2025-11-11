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

/**
 * ANSI color codes for console output formatting.
 * Provides methods to colorize text for better readability.
 */
public class ConsoleColors {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";
    
    // Check if running on Windows terminal that supports ANSI
    private static final boolean COLORS_ENABLED = checkAnsiSupport();
    
    private static boolean checkAnsiSupport() {
        // Check if running in a terminal that supports ANSI colors
        String os = System.getProperty("os.name");
        if (os == null) {
            return false; // Can't determine OS
        }
        
        if (os.toLowerCase().contains("win")) {
            // Only enable colors in Windows Terminal or VS Code integrated terminal
            String wtSession = System.getenv("WT_SESSION");
            String termProgram = System.getenv("TERM_PROGRAM");
            String vscodeInjection = System.getenv("VSCODE_INJECTION");
            
            // Windows Terminal has WT_SESSION
            // VS Code terminal has TERM_PROGRAM=vscode or VSCODE_INJECTION
            return wtSession != null || 
                   "vscode".equals(termProgram) || 
                   vscodeInjection != null;
        }
        
        // Unix-like systems generally support ANSI
        String term = System.getenv("TERM");
        return term != null && !"dumb".equals(term);
    }
    
    /**
     * Wrap text in green color for success messages
     */
    public static String success(String text) {
        return COLORS_ENABLED ? GREEN + text + RESET : text;
    }
    
    /**
     * Wrap text in red color for error messages
     */
    public static String error(String text) {
        return COLORS_ENABLED ? RED + text + RESET : text;
    }
    
    /**
     * Wrap text in yellow color for warning messages
     */
    public static String warning(String text) {
        return COLORS_ENABLED ? YELLOW + text + RESET : text;
    }
    
    /**
     * Wrap text in blue color for info messages
     */
    public static String info(String text) {
        return COLORS_ENABLED ? BLUE + text + RESET : text;
    }
    
    /**
     * Wrap text in cyan color for highlights
     */
    public static String highlight(String text) {
        return COLORS_ENABLED ? CYAN + text + RESET : text;
    }
    
    /**
     * Wrap text in bold
     */
    public static String bold(String text) {
        return COLORS_ENABLED ? BOLD + text + RESET : text;
    }
}
