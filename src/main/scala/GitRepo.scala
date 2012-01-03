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

    val workingDirectory : String = Settings.getProperty("workingDirectory") + "/" + this.owner + "/" + this.name

    /**
     * Makes a clone of the repo passed in. This clone is placed in to the
     * directory specified with the setting workingDirectory. Returns whether
     * or not the clone was successful
     *
     * Note that submodules are not initialized.
     */
    def cloneLocally() : Boolean = {
        var ownerDir = new File(Settings.getProperty("workingDirectory") + "/" + this.owner)
        if (!ownerDir.exists()) {
            ownerDir.mkdir()
            // Hack here, I can't figure out how to get Process to recognize git when I
            // specify a working directory
            Process("ln -s /usr/bin/git " + ownerDir.getAbsolutePath + "/git").!
        }

        // This will return a status code 0 if the clone was successful
        val result = Process("git clone " + this.cloneUrl + " " + workingDirectory, ownerDir).!
        if (result != 0) {
            Utils.deleteDirectoryRecursive(workingDirectory)
        } else {
            // Hack here, I can't figure out how to get Process to recognize git when I
            // specify a working directory
            Process("ln -s /usr/bin/git " + workingDirectory + "/git").!
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
        this._executeCommand(command)
    }

    def add(fileName: String) : Boolean = {
        this._executeCommand("git add " + fileName)
    }

    def commit(message: String, allFiles: Boolean = false) : Boolean = {
        var command = ""
        if (allFiles) {
            command = "git commit -a -m '" + message + "'"
        } else {
            command = "git commit -m '" + message + "'"
        }
        this._executeCommand(command)
    }

    def pushRemote(remoteBranch: String) : Boolean = {
        this._executeCommand("git push origin " + remoteBranch)
    }

    def _executeCommand(command : String) : Boolean = {
        println("About to run command " + command)
        val result = Process(command, new File(workingDirectory)).!
        result == 0
    }

}