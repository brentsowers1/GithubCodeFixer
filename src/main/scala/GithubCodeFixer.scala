object GithubCodeFixer {
    def main(args: Array[String]) {
        val gh = new GithubApi
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

//        val forked = gh.createFork("brentsowers1", "GithubCodeFixer")
//        println("Forked? " + forked.toString)

//        val watchers = gh.repoWatchers("rails", "rails")
//        val watchersStr = for (watcher <- watchers) yield watcher.login
//        print("Number of watchers: " + watchers.length.toString + "\n\n")

//        val details = gh.repoDetails("brentsowers1", "GithubCodeFixer")
//        details.parent match {
//            case Some(repo) => println("Parent repo owner is " + repo.owner.login)
//            case None => println("Repo has no parent")
//        }
//        println("Repo name: " + details.name)
    }

}