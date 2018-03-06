import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._

val url = "NONE"

object Deps {
  val junit4 = "junit" % "junit" % "4.12"
  val lambdaTest = "de.tototec" % "de.tobiasroeser.lambdatest" % "0.3.0"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  val utilsFunctional = "de.tototec" % "de.tototec.utils.functional" % "1.0.0"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val mavenPluginApi = "org.apache.maven" % "maven-plugin-api" % "3.3.3"
  val mavenPluginAnnotations = "org.apache.maven.plugin-tools" % "maven-plugin-annotations" % "3.4"
  val mavenCore = "org.apache.maven" % "maven-core" % "3.3.3"
}

object Plugins {
  val clean = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0"
  val gpg = "org.apache.maven.plugins" % "maven-gpg-plugin" % "1.6"
  val jar = "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5"
  val plugin = "org.apache.maven.plugins" % "maven-plugin-plugin" % "3.5.1"
  val polyglotTranslate = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.2.1"
  val surefire = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.17"
}

Model(
  gav = "com.github.lefou" % "eclipse-maven-plugin" % "0.0.1-SNAPSHOT",
  modelVersion = "4.0.0",
  packaging = "maven-plugin",
  properties = Map(
    "maven.compiler.source" -> "1.8",
    "maven.compiler.target" -> "1.8",
    "project.build.sourceEncoding" -> "UTF-8"
  ),
  name = "eclipse-maven-plugin",
  description = "A Maven Plugin to generate Eclipse Project files",
  dependencies = Seq(
    // compile dependencies
    Deps.mavenCore,
    Deps.mavenPluginApi,
    Deps.mavenPluginAnnotations,
    Deps.utilsFunctional,

    // test dependencies
    Deps.lambdaTest % "test",
    Deps.junit4 % "test",
    Deps.logbackClassic % "test"
  ),
  url = url,
  scm = Scm(
    url = url,
    connection = "scm:git:" + url,
    developerConnection = "scm:git:" + url
  ),
  licenses = Seq(License(
    name = "Apache License, Version 2",
    url = "http://www.apache.org/licenses",
    distribution = "repo"
  )),
  developers = Seq(
    Developer(
      name = "Tobias Roeser",
      email = "le.petit.fou@web.de"
    )
  ),
  build = Build(
    plugins = Seq(
      Plugin(
        gav = Plugins.plugin,
        executions = Seq(
          Execution(
            id = "default-descriptor",
            phase = "process-classes",
            goals = Seq(
              "descriptor"
            )
          ),
          Execution(
            id = "help-goal",
            goals = Seq(
              "helpmojo"
            ),
            configuration = Config(
              skipErrorNoDescriptorsFound = "true"
            )
          )
        )
      )
    )
  ),
  profiles = Seq(
    Profile(
      id = "gen-pom-xml",
      build = BuildBase(
        plugins = Seq(
          Plugin(
            Plugins.polyglotTranslate,
            executions = Seq(
              Execution(
                id = "pom-scala-to-pom-xml",
                phase = "initialize",
                goals = Seq("translate-project"),
                configuration = Config(
                  input = "pom.scala",
                  output = "pom.xml"
                )
              )
            )
          ),
          Plugin(
            Plugins.clean,
            configuration = Config(
              filesets = Config(
                fileset = Config(
                  directory = "${basedir}",
                  includes = Config(
                    include = "pom.xml"
                  )
                )
              )
            )
          )
        )
      )
    )
  )
)
