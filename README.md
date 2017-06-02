# yara-utils
utility toolset for using YARA 

What is this project about ?

When I started out learning to use YARA and how to write rule files, 
I soon got to the point where I wanted to use rule-files which where already made by someone else. 
In my particular case I wanted to use rules provided by this  repository https://github.com/Yara-Rules/rules/ - btw. many thanks for providing these rules !!!

The problem was, there where many rulefiles and some of them where using filter criterias which could onle be provided using environment variables ( eg filepaths,etc ...) inside the rules. 
Therefore the yara tool needed to be called multiple times for each file in particular. 

That lead to 2 problems: 

first problem: 
--------------
I had to call yara multiple times - for each rule file using filter criterias comprising data from environment variables - for each file under investigation. 
In my case I wanted to scan a whole directory tree comprising more than 1 TB of data with thousands of files.

second problem: 
---------------
I wanted to scan the directory tree in parallel. Yara provides the capability to scan using multiple threads , but in this case it could not be used 
because I needed to provide the external variables ( eg the absolute filepath ) for each file to scan.


At that point I started to think about a set of tools - which could tackle all the problems.

first problem solution:
-----------------------
A tool which could merge an arbitrary number of yara rule files into 1 file. 
Then it would be possible to just call yara only one time per file to be scanned. 

- The tool should be able to scan a directory (with subdirectories) of yara rules files and generate a resulting merged file.
- The tool should be capable of copying the various yara rule files in a single resulting rule file , as well as using 
the yara "include" clause for just referencing the particular rule files.
- Further more, when using the "include" clause,  it should be able to reference the other rule files either by full qualified pathnames as well as relative pathnames.
- It should be able to either treat all files in a directory as yara rules files ( and combining them ) or just use a given file-extension for filtering which files to be taken.


second problem solution:
------------------------
A tool for calling yara in parallel and providing all the needed external variables referenced in the yara rules. 
- It should be able handle a list of external variables which it uses to call yara 
- It should be able to define the maximum number of processes
- optionally: In case a big set of files or directories is scanned, it should be able keep track of the progress and 
  provide a means of relaunch mechanism in case the processes where killed for some reason ( eg. system restart )


 
