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
     * directory specified with the setting workingDirectory. Returns whether
     * or not the clone was successful
     *
     * Note that submodules are not initialized.
     */
    def cloneLocally() : Boolean = {
        val dir = Settings.getProperty("workingDirectory") + "/" + this.owner + "/" + this.name
        var ownerDir = new File(Settings.getProperty("workingDirectory") + "/" + this.owner)
        if (!ownerDir.exists()) {
            ownerDir.mkdir()
        }
        // This will return a status code 0 if the clone was successful
        val result = Process("git clone " + this.cloneUrl + " " + dir).!
        if (result != 0) {
            Utils.deleteDirectoryRecursive(dir)
        }
        result == 0
    }

    def checkout(branch: String,  createBranch: Boolean = true) : Boolean = {
        var command = ""
        if (createBranch) {
            command = "git checkout -b " + branch
        } else {
            command = "git checkout " + branch
        }
        val result = Process(command).!
        result == 0
    }

    def add(fileName: String) : Boolean = {
        val result = Process("git add " + fileName).!
        result == 0
    }

    def commit(message: String, allFiles: Boolean = false) : Boolean = {
        var command = ""
        if (allFiles) {
            command = "git commit -a -m \"" + message + "\""
        } else {
            command = "git commit -m \"" + message + "\""
        }
        val result = Process(command).!
        result == 0
    }

    def pushRemote(remoteBranch: String) : Boolean = {
        val result = Process("git push origin " + remoteBranch).!
        result == 0
    }

}