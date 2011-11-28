package com.coordinatecommons.GithubCodeFixer

object GithubCodeFixer {
    def main(args: Array[String]) {
        println("This doesn't actually do anything yet. See GithubApiExamples.scala for examples of how you can use it")
        val languages = Map("Ruby" -> 5000)
        val r = new GitRepo("git@github.com:brentsowers11/GithubCodeFixer.git", "brentsowers11", "GithubCodeFixer", languages)
        val clone = r.cloneLocally()
        println("output - " + clone.toString)
    }

}