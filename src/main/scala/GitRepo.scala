package com.coordinatecommons.GithubCodeFixer

import java.io._
import sbt._


/**
 * Class to perform git operations.
 */

class GitRepo(val cloneUrl: String,
              val owner: String,
              val name: String,
              val languages: Map[String, Int]) {

    /**
     * Makes a clone of the repo passed in. This clone is placed in to the
     * directory specified with the setting workingDirectory. Returns the
     * path to this newly cloned repo.
     *
     * Note that submodules are not initialized.
     */
    def cloneLocally() : String = {
        val dir = Settings.getProperty("workingDirectory") + "/" + this.owner + "/" + this.name
        var ownerDir = new File(Settings.getProperty("workingDirectory") + "/" + this.owner)
        if (!ownerDir.exists()) {
            ownerDir.mkdir()
        }
        val result = Process("git clone " + this.cloneUrl + " " + dir).!!
        result
    }

}