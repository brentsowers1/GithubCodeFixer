/**
 * Examples of how to use the GithubApi class. To use these, you'll have to
 * either edit Settings.scala and set your Github user name and password,
 * or add -DgithubUsername=yourgithubuser and -DgithubPassword=yourgithubpassword
 * to the JVM when running this
 */

class GithubApiExamples {
    def runExamples() {
        val gh = new GithubApi

        // This will return all repos that the user rails owns. This should
        // return 2 pages of results
        val repos = gh.reposByUser("rails")
        for (repo <- repos) {
            println("Repo name:   " + repo.name)
            println("Description: " + repo.description)
            println("Language:    " + repo.language)
            println("Forked?:     " + repo.fork)
            println("Watchers:    " + repo.watchers)
            println("Forks:       " + repo.forks)
            println("Size:        " + repo.size + "\n")
        }

        // This will return all users that are watching the rails repo.
        // As of this writing it's 11,160+ users. Which means that it/ actually
        // takes at least 372 HTTP requests to get all of these users (30
        // results per page). This is all done concurrently through actors.
        // If it seems to take a long time to run, then increase your
        // thread pool by starting your program or SBT with the property
        // actors.maxPoolSize=1024 or some other higher number (you can
        // do this by editing your sbt file and adding
        // -Dactors.maxPoolSize=1024
        val watchers = gh.repoWatchers("rails", "rails")
        val watchersStr = for (watcher <- watchers) yield watcher.login
        print("Number of watchers: " + watchers.length.toString + "\n\n")

        // This will get all languages that the Rails repo has
        val languages = gh.getLanguages("rails", "rails")
        for ((language, numLines) <- languages) {
            println("(" + numLines.toString + ") " + language)
        }

        // This will create a fork of the repo that this code lives in in to
        // your project space
        val forked = gh.createFork("brentsowers1", "GithubCodeFixer")
        println("Forked? " + forked.toString)

        // This will get all of the details on the repo that was
        // forked from above
        val details = gh.repoDetails(Settings.getProperty("githubUsername"), "GithubCodeFixer")
        details.parent match {
            case Some(repo) => println("Parent repo owner is " + repo.owner.login) // Should be brentsowers1
            case None => println("Repo has no parent")
        }
        println("Repo name: " + details.name)

        // To fully exercise this, pause in here, make a clone of the forked repo,
        // make a branch called forkTest, make a commit, and push this back up

        // Assuming that you've cloned, branched, committed, and pushed to
        // a branch forkTest, this will create a request for brentsowers1 to pull
        // your change back in
        val pullReq = gh.createPullRequest("brentsowers1", "GithubCodeFixer", "Scala code test",
                                           "This is a long description for this pull request. It can be " +
                                           " discarded", "master", "forkTest")
        println("Pull request number - " + pullReq.number.toString)
        println("Pull request state - " + pullReq.state)
        println("Pull request page - " + pullReq.html_url)
    }

}