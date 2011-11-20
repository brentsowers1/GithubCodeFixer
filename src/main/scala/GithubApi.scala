/**
 * Class for retrieving data from Github
 */

import dispatch._
import net.liftweb.json._

class GithubClass

/**
* A github user
*/
case class User(login: String,        // login name of hte user
                id: Int,              // numeric github ID
                avatar_url: String,   // URL to their avatar pic
                url: String)          // API url to retrieve JSON of the user
    extends GithubClass

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
    extends GithubClass

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
        getData[Repo]("users/" + user + "/repos")
    }

    def repoWatchers(repoOwner: String,  repoName: String) : List[User] = {
        getData[User]("repos/" + repoOwner + "/" + repoName + "/watchers")
    }

    /**
     * Makes an HTTP request to Github, and returns a list of the type passed in
     */
    private def getData[T <: GithubClass : Manifest](path: String,  responseIsArray: Boolean = true,
                                          previousResults: List[T] = List(), pageNum: Int = 1,
                                          maxPages: Int = 1) : List[T] = {
        val h = new Http
        var strUrl = baseUrl + "/" + path
        if (pageNum > 1) {
            strUrl = strUrl + "?page=" + pageNum.toString()
        }
        val req = url(strUrl)
        var maxPagesVar = maxPages

        // Gets both the headers and response body
        val rspStr = h(req >:+ { (headers, req) =>
            // The first time we run this, look at the headers for what the last
            // page is.
            if (pageNum == 1) {
                if (headers.contains("link") && headers("link").length > 0) {
                    val NextLinkPattern = """.*\?page=(\d+)>; rel="next", .*?page=(\d+)>; rel="last".*""".r
                    headers("link").head match {
                        case NextLinkPattern(next, last) => maxPagesVar = last.toInt
                        case _ => {}
                    }
                }
            }
            req as_str
        })
        val rspJVal = parse(rspStr)
        var results : List[T] = List()
        if (responseIsArray) {
            val rspList = rspJVal.children
            results = (for (jObj <- rspList) yield jObj.extract[T])
        } else {
            results = List(rspJVal.extract[T])
        }
        val allResults = previousResults ::: results
        if (maxPagesVar > 1 && pageNum < maxPagesVar) {
            getData(path, responseIsArray, allResults, pageNum + 1, maxPagesVar)
        } else {
            allResults
        }
    }

}
