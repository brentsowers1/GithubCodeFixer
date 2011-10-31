import dispatch._
import net.liftweb.json._

class GithubApi {
    private var username : String = ""
    private var password : String = ""
    private var baseUrl : String = "https://api.github.com"

    println("asdf")

    username = Settings.getProperty("githubUsername")
    password = Settings.getProperty("githubPassword")

    def reposByUser(user: String) {
        makeRequest("users/" + user + "/repos")
    }

    private def makeRequest(path: String) {
        val h = new Http
        val req = url(baseUrl + "/" + path)

        val rspStr = h(req >:+ { (headers, req) =>
            val asdf = headers
            req as_str
        })
        val rsp = parse(rspStr)


    }

}