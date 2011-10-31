/**
 * Contains "default" settings.  To override any of these at run time, add -Dpropertyname=value
 * to the java line
 */
object Settings {

    private val defaultProperties = Map(
        // User name used to log in to github
        "githubUsername" -> "Put your github username here",

        // Password of the user name from above
        "githubPassword" -> "Put your github password here"
    )

    def getProperty(name : String) = {
        val sysProp = System.getProperty(name)
        if (sysProp == null) {
            defaultProperties(name)
        } else {
            sysProp
        }
    }
}