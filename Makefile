.PHONY: all clean eclipse

all:
	mvn install

clean:
	rm -r target

eclipse: pom.xml
	mvn initialize de.tototec:de.tobiasroeser.eclipse-maven-plugin:0.1.0:eclipse

pom.xml: pom.scala
	mvn -Pgen-pom-xml initialize
