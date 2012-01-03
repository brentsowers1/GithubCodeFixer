package com.coordinatecommons.GithubCodeFixer

object GithubCodeFixer {
    def main(args: Array[String]) {
        println("This doesn't actually do anything yet. See GithubApiExamples.scala for examples of how you can use it")
//        val languages = Map("Ruby" -> 5000)
//        val r = new GitRepo("git@github.com:brentsowers11/GithubCodeFixer.git", "brentsowers11", "GithubCodeFixer", languages)
//        val clone = r.cloneLocally()
//        println("output - " + clone.toString)

        // Test, fix a project locally
//        val fixer = new ProjectFixer
//        fixer.fixProject("/home/brent/Rails3Bak")

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
                api.createPullRequest(Settings.getProperty("githubUsername"), repoName,
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