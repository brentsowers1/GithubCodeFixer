package com.coordinatecommons.GithubCodeFixer
/**
 * Class for retrieving data from Github
 */

import dispatch._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
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
                language: String,           // optional primary programming language
                `private`: Boolean,         // true/false for whether this is a private repo
                fork: Boolean,              // true if this was forked from another repo
                forks: Int,                 // number of forks made from this repo
                watchers: Int,              // number of users watching
                size: Int,                  // number of lines of code?
                open_issues: Int,           // number of open issues?
                pushed_at: java.util.Date,  // last time repo was pushed to
                created_at: java.util.Date, // when the repo was created
                parent: Option[Repo]        // If this repo was forked, and you requested details on a specific repo (instead of a list of repos), this is the repo that it was forked from
                )
    extends GithubClass

case class Link(href: String)

case class PullRequestLinks(self: Link,           // API url to this request
                            html: Link,           // URL to view this pull request
                            comments: Link,       // API URL to view comments
                            review_comments: Link // API URL to view the review comments
                            )

/**
 * A single pull request
 */
case class PullRequest(url : String,                       // API URL for this request
                       html_url: String,                   // URL to view this request
                       diff_url: String,                   // URL to view the diff
                       patch_url: String,                  // URL to view the patch
                       issue_url: String,                  // URL to view this as an issue
                       number: Int,                        // Sequential pull request number for the repo
                       state: String,                      // "open", (not sure what else this can be)
                       title: String,                      // title for this request
                       body: String,                       // long description of the request
                       created_at: java.util.Date,         // When the request was created
                       updated_at: java.util.Date,         // When it was last updated
                       closed_at: Option[java.util.Date],  // When it was closed
                       merged_at: Option[java.util.Date],  // WHen it was merged in
                       _links: PullRequestLinks            // More links
                      )
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
     * Returns full details on a specific repo
     */
    def repoDetails(user: String, repo: String) : Repo = {
        val repoList = getData[Repo]("repos/" + user + "/" + repo,  false)
        repoList.head
    }

    /**
     * Returns a list of User instances for users watching the passed in
     * repo name.
     */
    def repoWatchers(repoOwner: String,  repoName: String) : List[User] = {
        getData[User]("repos/" + repoOwner + "/" + repoName + "/watchers")
    }

    /**
     * Creates a fork of the repo specified. This fork is placed in the logged
     * in user's list of repositories
     */
    def createFork(repoOwner: String, repoName: String) : Repo = {
        val rsp = getData[Repo]("repos/" + repoOwner + "/" + repoName + "/forks",
                                false, "POST")
        rsp.head
    }

    /**
     * Gets all languages in this repo, and the number of lines for each language.  For
     * example, rails/rails will return something like:
     * ("Ruby" -> 7000000,
     *  "CoffeeScript" -> 750,
     *  "Javascript" -> 75000)
     */
    def getLanguages(repoOwner: String, repoName: String) : Map[String, Int] = {
        val (headers, rspStr) = makeSingleRequest("repos/" + repoOwner + "/" + repoName + "/languages")
        val rspJVal = parse(rspStr)
        rspJVal.values.asInstanceOf[Map[String,Int]]
    }

    /**
     * Creates a new request to pull your (user with the login credentials specified
     * in Settings) changes back in to the original repo.
     * @param repoOwner User name of the repo to request to
     * @param repoName Name of the repo to request the pull in to
     * @param title A short title for this request
     * @param body A longer description of the request
     * @param destinationBase Either a git ref or a branch of repoName that you want
     *                 your changes pulled in to (typically master)
     * @param sourceHead either a git ref or the branch name of the location of
     *                   your changes.
     */
    def createPullRequest(repoOwner: String, repoName: String,
                          title: String, body: String,
                          destinationBase: String, sourceHead: String) : PullRequest = {
        val params = Map("title" -> title,
                         "body" -> body,
                         "base" -> destinationBase,
                         "head" -> (Settings.getProperty("githubUsername") + ":" + sourceHead))
        val rsp = getData[PullRequest]("repos/" + repoOwner + "/" + repoName + "/pulls",
                                       false, "POST", Option(params))
        rsp.head
    }

    /**
     * Makes an HTTP request to Github, and returns a list of the type passed in
     */
    private def getData[T <: GithubClass : Manifest](path: String,
                                                     responseIsArray: Boolean = true,
                                                     method: String = "GET",
                                                     params: Option[Map[String,String]] = None) : List[T] = {
        var maxPage = 1
        val caller = self

        val (headers, rspStr) = makeSingleRequest(path, method, params)

        if (headers.contains("link") && headers("link").length > 0) {
            val NextLinkPattern = """.*\?page=(\d+)>; rel="next", .*?page=(\d+)>; rel="last".*""".r
            headers("link").head match {
                case NextLinkPattern(next, last) => maxPage = last.toInt
                case _ => {}
            }
        }

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
        val pathWithPage = path + "?page=" + pageNum.toString
        val (headers, rspStr) = makeSingleRequest(pathWithPage)
        parseResults[T](rspStr)
    }

    /**
     * Makes a single HTTP request, returning a tuple with the headers first and the
     * response as a string next.  The rate limit will be checked and the request
     * tried again after sleeping if the rate limit was hit.
     */
    private def makeSingleRequest(path: String,
                                  method: String = "GET",
                                  params: Option[Map[String,String]] = None) : (Map[String,Seq[String]], String) = {
        val h = new Http
        val strUrl = baseUrl + "/" + path
        var req = url(strUrl).as_!(Settings.getProperty("githubUsername"), Settings.getProperty("githubPassword"))
        if (params.isDefined) {
            val paramsStr = write(params.get)
            req = req <<(paramsStr, "application/json")
        } else {
            req = attachMethodToRequest(req, method)
        }
        var rateLimitHit = false
        var rspStr = ""
        var headers = Map[String, Seq[String]]()

        do {
            // Gets both the headers and response body
            rspStr = h(req >:+ { (hdrs, req) =>
                rateLimitHit = isRateLimitExceeded(hdrs)
                if (rateLimitHit) {
                    Thread.sleep(15000)
                }
                headers = hdrs
                req as_str
            })
        } while (rateLimitHit)
        h.shutdown()
        (headers,rspStr)
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

    private def attachMethodToRequest(request: Request, method: String) : Request = {
        method match {
            case "POST"   => request.POST
            case "PUT"    => request.PUT
            case "DELETE" => request.DELETE
            case _        => request
        }
    }


}
