package com.coordinatecommons.GithubCodeFixer.CodeFixes

import java.io.File

/**
 * Fixes code that would cause deprecation warnings in Rails 3, for functionality
 * that was removed in Rails 3.1.
 *
 * See http://stackoverflow.com/questions/3648063/rails-3-deprecated-methods-and-apis
 * for a list of things that were deprecated in Rails 3.0. All of these were abandoned
 * in Rails 3.1.
 */

class Rails3DeprecatedFixes extends CodeFix {
    override val language = "Ruby"
    var majorVersion   : Int = -1
    var minorVersion   : Int = -1
    var releaseVersion : Int = -1
    var isError        : Boolean = false

    def fixCode(fileData: String, projectPath: String,  fileName: String) : String = {
        var fixedData = fileData
        if (!isError) {
            if (majorVersion == -1) {
                this._findRailsVersion(projectPath)
                println("---- read rails version " + majorVersion + "." + minorVersion + "." + releaseVersion)
            }
            if (!isError) {
                if (majorVersion < 2 || (majorVersion == 2 && minorVersion == 0) ||
                    majorVersion > 3 ||
                    (majorVersion == 3 && minorVersion >= 1)) {
                    // We're not going to attempt to fix 2.0 or earlier, or
                    // 3.1 or later.  These are all deprecation warnings for
                    // functionality that was removed in 3.1.
                } else if (_isRails2() || _isRails30() ) {
                    //println("in data = " + fixedData)
                    // Rails 3 makes the RAILS_ constants deprecated, they are not
                    // supported in Rails 3.1

                    // Special case here, calling Rails.root + "asdf" will return "asdf", since
                    // Rails.root is a PathSpec.
                    fixedData = fixedData.replaceAll("RAILS_ROOT + ", "Rails.root.to_s +")
                    fixedData = fixedData.replaceAll("RAILS_ROOT", "Rails.root")

                    fixedData = fixedData.replaceAll("RAILS_ENV", "Rails.env")
                    fixedData = fixedData.replaceAll("RAILS_DEFAULT_LOGGER", "Rails.logger")
                }

                if (_isRails30()) {
                    fixedData = fixedData.replaceAll("named_scope", "scope")
                    fixedData = fixedData.replaceAll("""save\(\s*false\s*\)""", "save(:validate => false)")
                    val CreatePattern = """(?s)(.*)validate_on_create (:?"?[A-Za-z_]+"?)(.*)""".r
                    fixedData = CreatePattern.replaceAllIn(fixedData, m => m.subgroups(0) + "validate " + m.subgroups(1) + ", :on => :create" + m.subgroups(2))
                    val UpdatePattern = """(?s)(.*)validate_on_update (:?"?[A-Za-z_]+"?)(.*)""".r
                    fixedData = UpdatePattern.replaceAllIn(fixedData, m => m.subgroups(0) + "validate " + m.subgroups(1) + ", :on => :update" + m.subgroups(2))
                }

            }
        }
        fixedData
    }

    private def _isRails30() : Boolean = {
        majorVersion == 3 && minorVersion == 0
    }

    private def _isRails2() : Boolean = {
        majorVersion == 2
    }

    private def _findRailsVersion(projectPath : String) {
        // First file, we've got to check for the version of Rails
        // Check for a Gemfile, the Rails 3 with Bundler way
        var gemFile = new File(projectPath + "/Gemfile")
        if (gemFile.exists) {
            val gemFileIn = scala.io.Source.fromFile(gemFile)
            val gemData = gemFileIn.mkString
            gemFileIn.close()
            val Pattern = """(?s).*gem 'rails', '(\d+)\.(\d+)\.(\d+)'.*""".r
            gemData match {
                case Pattern(major, minor, release) => {
                    majorVersion = major.toInt
                    minorVersion = minor.toInt
                    releaseVersion = release.toInt
                    return;
                }
                case _ => { }
            }
        }

        var envFile = new File(projectPath + "/config/environment.rb")
        if (envFile.exists) {
            val gemData = scala.io.Source.fromFile(envFile).getLines.mkString
            val Pattern = """(?s).*RAILS_GEM_VERSION = '(\d+)\.(\d+)\.(\d+).*""".r
            gemData match {
                case Pattern(major, minor, release) => {
                    majorVersion = major.toInt
                    minorVersion = minor.toInt
                    releaseVersion = release.toInt
                    return;
                }
                case _ => { }
            }
        }

        // If we've gotten to this point, then we can't find the version
        isError = true;
    }

}