object GithubCodeFixer {
    def main(args: Array[String]) {
        val gh = new GithubApi;
        gh.reposByUser("brentsowers1")
    }
}