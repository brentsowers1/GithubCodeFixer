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
    val language = "Ruby"
    var majorVersion   : Int = -1
    var minorVersion   : Int = -1
    var releaseVersion : Int = -1
    var isError        : Boolean = false

    def fixCode(fileData: String, projectPath: String,  fileName: String) : String = {
        var fixedData = fileData
        if (!isError) {
            if (majorVersion == -1) {
                this._findRailsVersion()
            }
            if (!isError) {
                if (majorVersion < 2 || (majorVersion == 2 && minorVersion == 0) ||
                    majorVersion > 3 ||
                    (majorVersion == 3 && minorVersion >= 1)) {
                    // We're not going to attempt to fix 2.0 or earlier, or
                    // 3.1 or later.  These are all deprecation warnings for
                    // functionality that was removed in 3.1.
                    return
                }
                if (_isRails2() || _isRails30() ) {
                    // Rails 3 makes the RAILS_ constants deprecated, they are not
                    // supported in Rails 3.1

                    fixedData = fixedData.replaceAll("#{RAILS_ROOT}", "#{Rails.root}")
                    // Special case here, calling Rails.root + "asdf" will return "asdf", since
                    // Rails.root is a PathSpec.
                    fixedData = fixedData.replaceAll("RAILS_ROOT + ", "Rails.root.to_s +")
                    fixedData = fixedData.replaceAll("RAILS_ROOT", "Rails.root")

                    fixedData = fixedData.replaceAll("RAILS_ENV", "Rails.env")
                    fixedData = fixedData.replaceAll("RAILS_DEFAULT_LOGGER", "Rails.logger")
                }
            }
            if (_isRails30()) {
                fixedData = fixedData.replaceAll("named_scope", "scope")
                fixedData = fixedData.replaceAll("save(false)", "save(:validate => false)")
                val Pattern = """validate_on_create (:?"?[A-Za-z_]+"?)""".r
                fixedData = Pattern.replaceAllIn(fixedData, m => "validate " + m.subgroups.head + ", :on => :create")
                val Pattern = """validate_on_update (:?"?[A-Za-z_]+"?)""".r
                fixedData = Pattern.replaceAllIn(fixedData, m => "validate " + m.subgroups.head + ", :on => :update")

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

    private def _findRailsVersion() {
        // First file, we've got to check for the version of Rails
        // Check for a Gemfile, the Rails 3 with Bundler way
        var gemFile = new File(projectPath + "/Gemfile")
        if (gemFile.exists) {
            val gemData = scala.io.Source.fromFile(gemFile).getLines.mkString
            val Pattern = """gem 'rails', '(\d+).(\d+).(\d+)""".r
            gemData match {
                case Pattern(major, minor, release) => {
                    majorVersion = major
                    minorVersion = minor
                    releaseVersion = release
                    return;
                }
                case _ => { }
            }
        }

        var envFile = new File(projectPath + "/config/environment.rb")
        if (envFile.exists) {
            val gemData = scala.io.Source.fromFile(envFile).getLines.mkString
            val Pattern = """RAILS_GEM_VERSION = '(\d+)\.(\d+)\.(\d+)""".r
            gemData match {
                case Pattern(major, minor, release) => {
                    majorVersion = major
                    minorVersion = minor
                    releaseVersion = release
                    return;
                }
                case _ => { }
            }
        }

        // If we've gotten to this point, then we can't find the version
        isError = true;
    }

}