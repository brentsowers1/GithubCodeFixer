package com.coordinatecommons.GithubCodeFixer

object GithubCodeFixer {
    def main(args: Array[String]) {
        // This is not finished yet. Note that to do anything, you must run this from a machine
        // that has an SSH key linked up to github.com, and you must specify your username
        // and password for git in src/main/Settings.scala.  This code currently:
        // 1) Forks an example repo in my Github account (brentsowers1/rails_3_deprecated_example)
        // 2) Makes a local clone of this repo
        // 3) Makes a branch called autoCodeFixes for these changes
        // 4) Runs a fix class called Rails3DeprecatedFixes on all files in the repo (this class
        //    fixes some of the functions/features that were deprecated in Rails 3.0 and removed
        //    in Rails 3.1)
        // 5) Commits these changes
        // 6) Pushes the changes up to a new remote branch autoCodeFixes
        // 7) Issues a pull request for brentsowers1 to pull these changes back in to the main
        //    repo
        // A lot of debug info is printed to the screen when you run this
        //
        // Things that are still need to be done:
        // * Don't hard code the class name of the code fix, dynamically load them so others
        //   can just add new fixes to the CodeFixes directory
        // * Figure out how to run git from the command line properly. This currently has to
        //   create symlinks and can't put spaces in the commit message
        // * Github API rate limit is not being handled properly
        // * Search for repos to fix instead of being hard coded to just one. Here is what I
        //   am thinking for this:
        //   1) Get all of the watchers of a "seed" repo (a large project with lots of watchers)
        //   2) If we haven't processed this user (will probably have to use some sort of DB to
        //      keep track of who we've scanned), create an actor to process this user.
        //     1) Get all repos for this user. For each repo, create an actor:
        //       1) Get the repo details.
        //       2) If the repo is not a fork, list languages for the repo
        //       3) If it has any languages defined in CodeFixRegistry, do what I'm
        //          doing below for the brentsowers1/rails_3_deprecated_fixes repo
        //
        // to have this run continously, in the actor for each repo, I could list watchers and
        // Start at step 2 for this repo.
        //
        // If you need any help getting this project up and running, follow the steps in:
        // http://scalalift.brentsowers.com/2011/10/starting-lift-project-with-sbt-and.html
        // Follow the Scala and SBT steps.  Once you do this, type SBT in the directory for
        // this project, then type update, then reload, then run to run the code.
        //
        // Note that this will only run in Linux and Mac, assuming that your git
        // executable is in /usr/bin/.
        val api = new GithubApi()
        val repoName = "rails_3_deprecated_example"
        val repoOwner = "brentsowers1"
        val branchName = "autoCodeFixes"
        val repo = api.createFork("brentsowers1", repoName)
        println("Forked to " + repo.clone_url)
        val r = new GitRepo("git@github.com:" + Settings.getProperty("githubUsername") + "/" + repoName,
                            repoOwner, repoName,
                            Map())
        val cloned = r.cloneLocally()
        if (cloned) {
            r.checkout(branchName, true)
            println("Checked out " + branchName)
            val baseDir = Settings.getProperty("workingDirectory") + "/" + repoOwner + "/" + repoName
            val fixer = new ProjectFixer
            val fixedFiles = fixer.fixProject(baseDir)
            if (!fixedFiles.isEmpty) {
                println("Fixed code")
                fixedFiles.foreach {f => r.add(f)}
                // TODO: Figure out why spaces cause the git commit command to not work
                r.commit("Code_fixes_automatically_applied_by_the_GithubCodeFixer_project")
                println("Committed changes")
                r.pushRemote(branchName)
                println("Pushed changes to " + Settings.getProperty("githubUsername") + "/" + repoName)
                api.createPullRequest(repoOwner, repoName,
                    "Automatic deprecation fixes by GithubCodeFixer",
                    "The open source project GithubCodeFixer (https://github.com/brentsowers1/GithubCodeFixer) has scanned your code and automatically made some fixes:\n" +
                    "   Some of the Rails 3 deprecated functions in your code have been fixed so that your code will work with Rails 3.1\n" +
                    "Simply pull this request in to your code and you will have the fixes.\n" +
                    "If you have ideas for some other fixes to apply to code, submit the code to the GithubCodeFixer project. The module to do the fixing must be in Scala or Java, but the code that it scans for fixes can be in any languages.  Deprecation warnings and security fixes are some common fixes that you may want to write. Thank you.",
                    "master", branchName)
                println("Created pull request")
            } else {
                println("Nothing to fix")
            }
        } else {
            println("Error cloning")
        }
    }

}