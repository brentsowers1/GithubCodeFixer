/**
 * Class for retrieving data from Github
 */

import dispatch._
import net.liftweb.json._

/**
 * A github user
 */
case class User(login: String,        // login name of hte user
                id: Int,              // numeric github ID
                avatar_url: String,   // URL to their avatar pic
                url: String)          // API url to retrieve JSON of the user

/**
 * A single code repo.
 */
case class Repo(url: String,                // URL to get JSON for this repo via the API
                html_url: String,           // web page for this repo
                clone_url: String,          // HTTP clone URL
                git_url: String,            // git clone url
                ssh_url: String,            // ssh clone url
                svn_url: String,            // URL to clone as svn
                owner: User,                // The owner of this
                name: String,               // name of this repository
                description: String,        // optional description of this repo
                homepage: String,           // optional URL of the home page for this project (not necessarily github)
                language: String,           // optional programming language
                `private`: Boolean,         // true/false for whether this is a private repo
                fork: Boolean,              // true if this was forked from another repo
                forks: Int,                 // number of forks made from this repo
                watchers: Int,              // number of users watching
                size: Int,                  // number of lines of code?
                open_issues: Int,           // number of open issues?
                pushed_at: java.util.Date,  // last time repo was pushed to
                created_at: java.util.Date) // when the repo was created

class GithubApi {
    private var username : String = ""
    private var password : String = ""
    private var baseUrl : String = "https://api.github.com"

    username = Settings.getProperty("githubUsername")
    password = Settings.getProperty("githubPassword")
    // Don't or modify remove this, it's needed by the Lift JSON library
    implicit val formats = net.liftweb.json.DefaultFormats

    /**
     * Returns all repos owned by the passed in Github user name.
     * Response is a list of Repo objects as specified the case class above
     */
    def reposByUser(user: String) : List[Repo] = {
        val rspJVal = makeRequest("users/" + user + "/repos")
        // Github response is an array of objects, this will get that array
        // as a List
        val rspList = rspJVal.children
        for (msg <- rspList) yield msg.extract[Repo]
    }

    /**
     * Makes an HTTP request to Github, and returns the response as a JValue.
     * Individual functions should parse the JValue as needed
     */
    private def makeRequest(path: String) : JValue = {
        val h = new Http
        val req = url(baseUrl + "/" + path)

        // Gets both the headers and response body
        val rspStr = h(req >:+ { (headers, req) =>
            // Not doing anything with the headers yet, we'll want to check
            // these for the rate limits, and if we're close the rate limit,
            // sleep for a little while. We'll also need to check if we've
            // hit the rate limit, and try again.
            val asdf = headers
            req as_str
        })
        parse(rspStr)
    }

}
