# Live Score Display (lsd)
### Author: Buisson Morgan, Ben Yin, Zhouyi Sun 
## REQUIREMENTS

* git
* Java >= 1.8
* Maven

Development has gone through two periods. The latest version is developed on Intellij and has been installed successfully on eclipse as well.

There is three module with the `lsd` as the major module. The `score` module has some basic modules while the `SCompiler` module is used for compile notations.

### INSTALL

First, use `git clone https://github.com/Somefive/lsd.git` to download the source.

If you use `Intellij`, then choose `File | New | projects from existing sources...`. Then choose the `pom.xml` in `LSD/lsd`. Then choose `File | New | modules from existing sources...` and add `pom.xml` from `LSD/score` and `LSD/SCompiler`. If you open `Auto-Import` option, then dependencies should be installed and configured automatically. Go to `LSD/lsd/src/main/java/edu.cmu.mat.lsd/Main` and `run`/`debug` it. It should work now.

If you use `Eclipse`, configure your workspace to `LSD` and you can open these three `pom.xml` at the same time and the dependencies will be resolved automatically and it should work.

The old import might help for `Eclipse`

> Select `File -> Import` and select `Maven -> Existing Maven Projects`. Select
> the `livescoredisplay` folder as the "Root Directory" and make sure that there
> are two projects titled "lsd" and "score".  Select them and click "Finish".
>
> These two projects should now appear in the "Package Explorer" on the left.
>
> To install them, right click on the "score" project, and select
> "Maven -> Update Project...".  Select both projects, and click "OK".
>
> Then, right click the "score" project and select "Run As -> Maven install".
>
> If "Maven install" does not exist, instead select "Run As -> Run Configurations...".
> In the top left, select "Maven Build" then click on the new configuration icon
> (with the plus sign).  Name the new configuration "Maven install".  Select
> "Browse Workspace" and select the project "score" project. In the "Goals" field,
> input "install".  In the bottom right, click "Run".
>
> Once that is complete, right click on "lsd" and select "Run As -> Maven install".
> Again, if "Maven install" does not exist, follow the above instructions but replace
> "score" with "lsd".

### NOTICE

Sometimes, you need to choose the library path after you run the program. The `init.json`  file might causes some troubles. If it hurts, you can delete these `init.json` and it should works.