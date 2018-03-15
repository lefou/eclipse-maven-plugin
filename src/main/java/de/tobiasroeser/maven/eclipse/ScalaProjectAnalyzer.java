package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.append;
import static de.tototec.utils.functional.FList.exists;
import static de.tototec.utils.functional.FList.flatten;
import static de.tototec.utils.functional.FList.mkString;
import static de.tototec.utils.functional.FList.take;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.Optional;

public class ScalaProjectAnalyzer implements MavenProjectAnalyzer {

	public static final String ORG_SCALA_IDE_SDT_CORE_SCALABUILDER = "org.scala-ide.sdt.core.scalabuilder";
	public static final String ORG_SCALA_IDE_SDT_CORE_SCALANATURE = "org.scala-ide.sdt.core.scalanature";

	private final Log log;
	private final boolean addingAllowed;

	public ScalaProjectAnalyzer(Log log, boolean addingAllowed) {
		this.log = log;
		this.addingAllowed = addingAllowed;
	}

	@Override
	public ProjectConfig analyze(ProjectConfig projectConfig, MavenProject mavenProject) {
		ProjectConfig updated = projectConfig;

		final List<String> scalaPlugins = Arrays.asList(
				"net.alchim31.maven:scala-maven-plugin",
				"com.google.code.sbt-compiler-maven-plugin:sbt-compiler-maven-plugin",
				"com.carrotgarden.maven:scalor-maven-plugin_2.12",
				"com.carrotgarden.maven:scalor-maven-plugin_2.13");

		final boolean pluginDetected = exists(
				mavenProject.getPluginArtifactMap().keySet(),
				key -> exists(
						scalaPlugins,
						p -> p.equals(key)));

		if (pluginDetected) {
			log.debug("Detected scala plugin");

			// FList.toHashMap("org.scala-ide.sdt.core.prefs", printStream -> {
			// TODO: generate settings file

			// scala.compiler.installation=2.11
			// scala.compiler.sourceLevel=2.11
			// target=jvm-1.8

			if (addingAllowed) {
				final Optional<String> scalaVersion = Optional
						.of(mavenProject.getProperties().getProperty("scala.version"))
						.map(v -> mkString(take(v.split("[.]"), 2), "."));

				final Optional<String> javaVersion = projectConfig.getJavaVersion();

				final List<String> settings = flatten(
						Arrays.asList(
								scalaVersion.map(v -> "scala.compiler.installation=" + v),
								scalaVersion.map(v -> "scala.compiler.sourceLevel=" + v),
								javaVersion.map(v -> "target=jvm-" + v)));
				final SettingsFile settingsFile = new SettingsFile("org.scala-ide.sdt.core.prefs", settings);

				log.debug("Adding scala nature and builder, disabling java nature and builder");
				updated = updated
						.withNatures(append(updated.getNatures(),
								new Nature(ORG_SCALA_IDE_SDT_CORE_SCALANATURE, "Auto-detected from pom")))
						.withBuilders(append(updated.getBuilders(),
								new Builder(ORG_SCALA_IDE_SDT_CORE_SCALABUILDER, "Auto-detected from pom")))
						.withDisabledBuilders(append(updated.getDisabledBuilders(),
								JavaProjectAnalyzer.ORG_ECLIPSE_JDT_CORE_JAVABUILDER));
				updated = updated.withSettingsFiles(append(updated.getSettingsFiles(), settingsFile));

			}

			// TODO: add scala classpath container if no scala lib is on the path
		}

		return updated;
	}

}
