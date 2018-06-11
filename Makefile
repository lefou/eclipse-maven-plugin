.PHONY: all clean distclean eclipse release

all:
	mvn install

clean:
	rm -r target

distclean: clean
	rm mvn-deploy-settings.xml
	rm pom.xml

eclipse: pom.xml
	mvn initialize de.tototec:de.tobiasroeser.eclipse-maven-plugin:0.1.1:eclipse

pom.xml: pom.scala
	mvn -Pgen-pom-xml initialize

release:
	mvn -Prelease -s mvn-deploy-settings.xml clean deploy
