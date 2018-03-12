import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._

val projectName = "eclipse-maven-plugin"
val gav = "de.tototec" % s"de.tobiasroeser.${projectName}" % "0.0.4-SNAPSHOT"
val url = "https://github.com/lefou/eclipse-maven-plugin"

implicit class RichDependency(d: Dependency) {
  def %(scope: String): Dependency = new Dependency(d.gav, d.`type`, d.classifier, Option(scope), d.systemPath, d.exclusions, d.optional)
}

object Deps {
  val antContrib = "ant-contrib" % "ant-contrib" % "1.0b3"
  val junit4 = "junit" % "junit" % "4.12"
  // we use the Java7 version here, because the maven site plugin has problems with the java8 version
  val lambdaTest = Dependency("de.tototec" % "de.tobiasroeser.lambdatest" % "0.4.0", classifier = "java7")
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  val utilsFunctional = "de.tototec" % "de.tototec.utils.functional" % "1.0.0"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val mavenPluginApi = "org.apache.maven" % "maven-plugin-api" % "3.3.3"
  val mavenPluginAnnotations = "org.apache.maven.plugin-tools" % "maven-plugin-annotations" % "3.4"
  val mavenCore = "org.apache.maven" % "maven-core" % "3.3.3"
}

object Plugins {
  val antrun = "org.apache.maven.plugins" % "maven-antrun-plugin" % "1.8"
  val asciidoctor = "org.asciidoctor" % "asciidoctor-maven-plugin" % "1.5.6"
  val clean = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0"
  val deploy = "org.apache.maven.plugins" % "maven-deploy-plugin" % "2.8.2"
  val gpg = "org.apache.maven.plugins" % "maven-gpg-plugin" % "1.6"
  val jar = "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5"
  val javadoc = "org.apache.maven.plugins" % "maven-javadoc-plugin" % "3.0.0"
  val jxr = "org.apache.maven.plugins" % "maven-jxr-plugin" % "2.5"
  val nexusStaging = "org.sonatype.plugins" % "nexus-staging-maven-plugin" % "1.6.7"
  val plugin = "org.apache.maven.plugins" % "maven-plugin-plugin" % "3.5.1"
  val polyglotTranslate = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.3.0"
  val projectInfoReports = "org.apache.maven.plugins" % "maven-project-info-reports-plugin" % "2.9"
  val site = "org.apache.maven.plugins" % "maven-site-plugin" % "3.7"
  val source = "org.apache.maven.plugins" % "maven-source-plugin" % "3.0.1"
  val surefire = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.20.1"
}

//#include mvn-release.scala

Model(
  gav = gav,
  modelVersion = "4.0.0",
  packaging = "maven-plugin",
  properties = Map(
    "maven.compiler.source" -> "1.8",
    "maven.compiler.target" -> "1.8",
    "project.build.sourceEncoding" -> "UTF-8"
  ),
  name = projectName,
  description = "A Maven Plugin to generate project files for Eclipse with M2E-Plugin",
  prerequisites = Prerequisites(
    maven = "3.3"
  ),
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
  scm = Scm(url = url, connection = "scm:git:" + url, developerConnection = "scm:git:" + url),
  licenses = Seq(License(
    name = "Apache License, Version 2",
    url = "http://www.apache.org/licenses",
    distribution = "repo"
  )),
  developers = Seq(
    Developer(name = "Tobias Roeser", email = "le.petit.fou@web.de")
  ),
  build = Build(
    resources = Seq(
      Resource(
        directory = "src/main/resources"
      ),
      Resource(
        directory = ".",
        includes = Seq("README.adoc", "LICENSE.txt")
      )
    ),
    plugins = Seq(
      Plugin(gav = Plugins.plugin,
        executions = Seq(
          Execution(id = "default-descriptor", phase = "process-classes", goals = Seq("descriptor")),
          Execution(id = "help-goal", goals = Seq("helpmojo"),
            configuration = Config(skipErrorNoDescriptorsFound = "true")
          )
        )
      ),
      // Use Asciidoctor to render site pages
      Plugin(Plugins.site, dependencies = Seq(Plugins.asciidoctor)),
      // Disable deploy plugin, we use nexus-staging in release profile
      Plugin(Plugins.deploy, configuration = Config(skip = "true"))
    )
  ),
  reporting = Reporting(
    plugins = Seq(
      ReportPlugin(Plugins.projectInfoReports),
      ReportPlugin(Plugins.plugin),
      ReportPlugin(Plugins.javadoc),
      ReportPlugin(Plugins.jxr)
    )
  ),
  distributionManagement = DistributionManagement(
    repository = DeploymentRepository(
      id = "ossrh",
      url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    )
  ),
  profiles = Seq(
    ReleaseProfile(),
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
