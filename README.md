GithubCodeFixer
===============

Overview
--------

This is a work in progress.  My goal with this is to create a program that will automatically
scan public repos on Github and apply fixes to these repos for things like security vulnerabilities
and deprecated functionality.  The project is currently pretty close to being able to do this, as
it can apply a single fix to a single repo and issue a pull request for the original author to
pull in the changes.  read the comments in GithubCodeFixer.scala to see what is remaining and
more detail on what the code currently does.

Running the code
----------------

This project is written in Scala with SBT to do the building.  To run this, follow the steps at
my blog post: http://scalalift.brentsowers.com/2010/10/starting-lift-project-with-sbt-and.html.
Follow the Scala and SBT steps, you don't need to do anything after that.  Then in the directory
that contains this code, type sbt, then at the prompt type update followed by reload followed by
run.

Code Modifications
------------------

If you want to add your own class to do code fixes, just add a class to the CodeFixes directory
that extends CodeFix.  See Rails3DeprecatedFixes as an example.  If you've tested it, please,
submit a pull request to me to get the code.

GithubCodeFixer.scala contains the main function which runs when you type run.  This is currently
hard coded to fork my example repo, fix it, and make a pull request.  If you want to test your
own repo or your own code fixes just change this section.

Scala
-----

This is also my first attempt at using Scala.  If you have any suggestions for how to make the
code better, I'd love to hear it.

Author:: Brent Sowers (brent@coordinatecommons.com)
License:: You're free to do whatever you want with this, although I'd just ask out of courtesy if you add features or fix things, that you submit a pull request

