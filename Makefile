all::  GenTabImgHtml.class

clean::
	rm *.class  *.jar

jar::
	rm  GenTabImgHtml.jar;jar cvfm GenTabImgHtml.jar GenTabImgHtml.mf *.class  

%.class: %.java
	javac  -classpath ../.. -deprecation -g $*.java
 
