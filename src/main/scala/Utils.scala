package com.coordinatecommons.GithubCodeFixer

/**
 * General utility functions
 */

import java.io._

object Utils {
    def deleteDirectoryRecursive(dir: String) {
        var f = new File(dir)
        if (f.exists) {
            if (f.isDirectory) {
                for (c : File <- f.listFiles)
                    deleteDirectoryRecursive(c.toString);
            }
            if (!f.delete)
                throw new FileNotFoundException("Failed to delete file: " + f.toString);
        }
    }
}