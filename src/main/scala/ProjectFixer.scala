package com.coordinatecommons.GithubCodeFixer
import java.io._

class ProjectFixer {

    /**
     * Runs through every file in the directory specified and applies all fixes
     * provided in the CodeFixes folder.  Returns whether or not any fixes were
     * actually applied.
     */
    def fixProject(baseDirectory: String) : Boolean = {
        var fixed = false
        val baseFile = new File(baseDirectory)
        val files = _recursiveListFiles(baseFile)
        files.foreach(f => {
            val name = f.getName
            println("Got name " + name)
            val Pattern = """.*\.([^\.]+)$""".r
            name match {
                case Pattern(extension) => {
                    println("Extension " + extension + " found")
                    if (CodeFixLanguages.mapping.get(extension).isDefined) {
                        val language = CodeFixLanguages.mapping(extension)
                        if (CodeFixRegistry.mapping.get(language).isDefined) {
                            println("---- About to apply fix for language " + language)
                            val fixers = CodeFixRegistry.mapping(language)
                            fixers.foreach { fixer =>
                                val inFile = scala.io.Source.fromFile(f)
                                val inData = inFile.mkString
                                //println("---- Read data " + inData)
                                inFile.close()
                                val outData = fixer.fixCode(inData, baseDirectory, f.getAbsolutePath)
                                if (inData != outData) {
                                    println("---- ---- Fix made, about to write file")
                                    fixed = true
                                    _writeFile(f, outData)
                                    println("---- ---- New file written")
                                }
                            }
                        }

                    }
                }
                case _ => { }
            }
        })
        fixed
    }

    def _recursiveListFiles(f: File): Array[File] = {
        if (f.getName == ".git") {
            // TODO: Implement some sort of excluded directories list here
            Array()
        } else {
            val these = f.listFiles
            these ++ these.filter(_.isDirectory).flatMap(_recursiveListFiles)
        }
    }

    def _writeFile(f: File,  data: String) {
        val out = new FileWriter(f)
        try {
            out.write(data)
        } catch {
            case e: Exception => println("Error while trying to write file " + f.getAbsolutePath)
        } finally {
            out.close()
        }

    }
}