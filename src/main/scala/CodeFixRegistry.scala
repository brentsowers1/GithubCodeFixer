package com.coordinatecommons.GithubCodeFixer
import com.coordinatecommons.GithubCodeFixer.CodeFixes._

object CodeFixRegistry {
    val mapping : scala.collection.mutable.Map[String,  List[CodeFix]] = scala.collection.mutable.Map()

    // TODO: Dynamically read all clases in the package and
    // apply them in to the map
    mapping("Ruby") = List(new Rails3DeprecatedFixes())
}