object GithubCodeFixer {
    def main(args: Array[String]) {
        val gh = new GithubApi
//        val repos = gh.reposByUser("rails")
//        for (repo <- repos) {
//            println("Repo name:   " + repo.name)
//            println("Description: " + repo.description)
//            println("Language:    " + repo.language)
//            println("Forked?:     " + repo.fork)
//            println("Watchers:    " + repo.watchers)
//            println("Forks:       " + repo.forks)
//            println("Size:        " + repo.size + "\n")
//        }

        val watchers = gh.repoWatchers("rails", "rails")
        val watchersStr = for (watcher <- watchers) yield watcher.login
        print("Watchers: " + watchersStr.mkString(", ") + "\n\n")
    }

}