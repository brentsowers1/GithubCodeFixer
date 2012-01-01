package com.coordinatecommons.GithubCodeFixer.CodeFixes

/**
 * Base class for all code fixing rules
 */

abstract class CodeFix() {
    // Set this to the programming language that your class fixes
    abstract val language: String = ""

    // Implement this function to fix the code passed in. The program will
    // read an entire file and send the data for the file in. You return the
    // fixed code. If no fixes are applied, just return the data sent in.
    // A new file will not be written unless the data has changed.
    //
    // The full path to the project that this file is in is passed in,
    // followed by the name of this file relative to that. (example -
    // /home/brent/GithubCodeFixer/working/brentsowers1/googlestaticmap for
    // projectPath, and lib/googlestaticmap.rb for fileName)
    // Only files of the type supported by your class will be
    // passed in. Example - only .scala files will be sent to you if you set the
    // language to Scala (or scala or SCALA, case insensitive). Only .rb,
    // .erb, .rhtml, .rjs, and .rxml files will be sent if your language is
    // Ruby.  The language to file extension mapping is defined in
    // CodeFixLanguages.scala.
    //
    // It's up to you to figure out how to modify the code. If you just want
    // to do a simple replace, you can just do a simple regex replace. The
    // implementation here is left up to you to allow for complex logic.
    //
    // Note that your class is only instantiated once per project. So you can
    // do things like read a project settings file once on the first call to
    // fix code and use the settings on every subsequent call
    abstract def fixCode(fileData: String, projectPath: String,  fileName: String) : String

}