/**
 * Class for retrieving data from Github
 */

import dispatch._
import net.liftweb.json._
import scala.actors.Actor._

/**
 * Base class for Github data, nothing in this class, simply used to restrict
 * types sent to functions
 */
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

    /**
     * Returns a list of User instances for users watching the passed in
     * repo name.
     */
    def repoWatchers(repoOwner: String,  repoName: String) : List[User] = {
        getData[User]("repos/" + repoOwner + "/" + repoName + "/watchers")
    }

    /**
     * Makes an HTTP request to Github, and returns a list of the type passed in
     */
    private def getData[T <: GithubClass : Manifest](path: String,
                                                     responseIsArray: Boolean = true) : List[T] = {
        val h = new Http
        var strUrl = baseUrl + "/" + path
        val req = url(strUrl)
        var maxPage = 1
        val caller = self
        var rspStr = ""
        var rateLimitHit = false

        do {
            // To know if there are multiple pages, we have to make the first request
            rspStr = h(req >:+ { (headers, req) =>
                rateLimitHit = isRateLimitExceeded(headers)
                if (rateLimitHit) {
                    Thread.sleep(15000)
                }
                // The first time we run this, look at the headers for what the last
                // page is.
                if (!rateLimitHit && headers.contains("link") && headers("link").length > 0) {
                    val NextLinkPattern = """.*\?page=(\d+)>; rel="next", .*?page=(\d+)>; rel="last".*""".r
                    headers("link").head match {
                        case NextLinkPattern(next, last) => maxPage = last.toInt
                        case _ => {}
                    }
                }
                req as_str
            })
        } while (rateLimitHit)
        h.shutdown()
        var results = parseResults[T](rspStr, responseIsArray)
        if (responseIsArray && maxPage > 1) {
            for(i <- 2 until maxPage+1) {
                actor {
                    caller ! getSinglePage[T](path, i)
                }
            }
            for(i <- 2 until maxPage+1) {
                receive {
                    case pageResults: List[T] => {
                        results = results ::: pageResults
                    }
                }
            }
        }
        results
    }

    /**
     * Gets a single page of responses from Github of the type passed in.
     */
    private def getSinglePage[T <: GithubClass : Manifest](path: String, pageNum: Int = 1) : List[T] = {
        val h = new Http
        var strUrl = baseUrl + "/" + path + "?page=" + pageNum.toString
        val req = url(strUrl)
        var rateLimitHit = false
        var rspStr = ""

        do {
            // Gets both the headers and response body
            rspStr = h(req >:+ { (headers, req) =>
                rateLimitHit = isRateLimitExceeded(headers)
                if (rateLimitHit) {
                    Thread.sleep(15000)
                }
                req as_str
            })
        } while (rateLimitHit)
        h.shutdown()
        parseResults[T](rspStr)
    }

    /**
     * Parses a string response from Github, converting it in to a list of instances
     * of the class passed in.  responseIsArray determines whether the result string is
     * expected to be a JSON array.  If not, then a list with a single
     * item is returned.
     */
    private def parseResults[T <: GithubClass : Manifest](resultData: String,
                                                          responseIsArray: Boolean = true) : List[T] = {
        val rspJVal = parse(resultData)
        var results : List[T] = List()
        if (responseIsArray) {
            val rspList = rspJVal.children
            for (jObj <- rspList) yield jObj.extract[T]
        } else {
            List(rspJVal.extract[T])
        }
    }

    /**
     * Returns whether or not the Github rate limit has been exceeded
     */
    private def isRateLimitExceeded(headers: Map[String,Seq[String]]) : Boolean = {
        if (headers.contains("X-RateLimit-Remaining") && headers("X-RateLimit-Remaining").length > 0) {
            headers("X-RateLimit-Remaining").head.toInt <= 10
        } else {
            false
        }
    }


}
