# Live Score Display (lsd)
### Author: morgan, da yin 
## REQUIREMENTS

* git
* Java >= 1.8
* Maven

Development has been mainly done with Eclipse 4.4.1 and the m2e plugin with git
on the command line. For instructions on using Eclipse, see below.

## DOWNLOAD SOURCE

First, checkout the code by running:
`git clone https://github.com/DeadHeadRussell/livescoredisplay.git`
This puts the source code into the folder `livescoredisplay`.

## INSTALLATION

Run `maven install` on the `score` project then `maven install` on the `lsd` project.

## USEAGE

`cmu.edu.mat.lsd.Main` contains the entry point for the program.

## ECLIPSE INSTRUCTIONS

### Workspace
After checking out the source code (see the "Download Source" section), run
Eclipse and choose the folder where the source code lives as the workspace
(`livescoredisplay`).

### Import Projects
Select `File -> Import` and select `Maven -> Existing Maven Projects`. Select
the `livescoredisplay` folder as the "Root Directory" and make sure that there
are two projects titled "lsd" and "score".  Select them and click "Finish".

These two projects should now appear in the "Package Explorer" on the left.

### Install Projects
To install them, right click on the "score" project, and select
"Maven -> Update Project...".  Select both projects, and click "OK".

Then, right click the "score" project and select "Run As -> Maven install".

If "Maven install" does not exist, instead select "Run As -> Run Configurations...".
In the top left, select "Maven Build" then click on the new configuration icon
(with the plus sign).  Name the new configuration "Maven install".  Select
"Browse Workspace" and select the project "score" project. In the "Goals" field,
input "install".  In the bottom right, click "Run".

Once that is complete, right click on "lsd" and select "Run As -> Maven install".
Again, if "Maven install" does not exist, follow the above instructions but replace
"score" with "lsd".

### Run LSD
Open up the file "lsd -> src/main/java -> edu.cmu.mat.lsd -> Main.java" and
click on a line of code in the "main" function.  Click on the "Run" button in
the menu bar (green play button) or select "Run -> Run" from the menus. For
running the program in the future, simply clicking the "Run" button should be
enough.

