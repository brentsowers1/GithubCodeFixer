/**
 * Contains "default" settings.  To override any of these at run time, add -Dpropertyname=value
 * to the java line
 */

package com.coordinatecommons.GithubCodeFixer

object Settings {

    private val defaultProperties = Map(
        // User name used to log in to github
        "githubUsername" -> "Put your github user name here",

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