You must have a template html file like thg_XXX.html with lines
like that :
<!--#CmdTabThumb SRC=my_image_directory THUMB_SIZE=32 COLUMNS=5 LIMIT_FULL_SIZE=600  NAME=1 WH=1 SIZE=1  BORDER=0 #-->

The program create an XXX.html file with thumbnail table
and a thumb_my_image_directory directory for the thumbnail files.


Use -Imy_directory for indicate to the program the target directory.
The progam explore recursivly all the directory tree and create a new html
file when it find a thg_XXX.html file.

