# minivan demo project

If you use this as an example:

* do not copy the `includeBuild` line from `settings.gradle`
* remove the commented-out repo at the top of `build.gradle`

This stuff is here purely to support "a gradle project dependent on a plugin from its parent directory" nonsense. This is generally a Bad Idea (subprojects would probably be the better approach)