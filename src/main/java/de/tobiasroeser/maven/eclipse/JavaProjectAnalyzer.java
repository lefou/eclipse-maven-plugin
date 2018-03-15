package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.append;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.Optional;

public class JavaProjectAnalyzer implements MavenProjectAnalyzer {

	public static final String ORG_ECLIPSE_JDT_CORE_JAVABUILDER = "org.eclipse.jdt.core.javabuilder";
	public static final String ORG_ECLIPSE_JDT_CORE_JAVANATURE = "org.eclipse.jdt.core.javanature";

	private final Log log;
	private final boolean addingAllowed;

	public JavaProjectAnalyzer(final Log log, final boolean addingAllowed) {
		this.log = log;
		this.addingAllowed = addingAllowed;
	}

	@Override
	public ProjectConfig analyze(final ProjectConfig projectConfig, final MavenProject mavenProject) {
		if ("pom".equals(mavenProject.getPackaging())) {
			log.debug("Skip java-specific project settings for pom project: " + mavenProject);
			return projectConfig;
		}

		ProjectConfig updated = projectConfig;
		if (addingAllowed) {
			log.debug("Adding Java builder and nature");
			updated = updated
					.withBuilders(append(updated.getBuilders(),
							new Builder(ORG_ECLIPSE_JDT_CORE_JAVABUILDER, "Default Java Builder")))
					.withNatures(append(updated.getNatures(),
							new Nature(ORG_ECLIPSE_JDT_CORE_JAVANATURE, "Default Java Nature")));

			final Optional<String> javaVersion = Optional
					.of(mavenProject.getProperties().getProperty("maven.compiler.source"))
					.orElse(Optional.of(mavenProject.getProperties().getProperty("maven.compiler.target")))
					.orElse(updated.getJavaVersion());

			log.debug("Adding Java classpath container");
			final String cpEntry;
			final String jrePrefix = "org.eclipse.jdt.launching.JRE_CONTAINER";
			if (javaVersion.isDefined()) {
				final String jreMiddle = "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/";
				if ("1.5".equals(javaVersion.get()) || "1.4".equals(javaVersion.get())) {
					cpEntry = jrePrefix + jreMiddle + "J2SE-" + javaVersion.get();
				} else {
					cpEntry = jrePrefix + jreMiddle + "JavaSE-" + javaVersion.get();
				}
			} else {
				cpEntry = jrePrefix;
			}

			updated = updated
					.withJavaVersion(javaVersion)
					.withClasspathContainers(append(updated.getClasspathContainers(), cpEntry));

		}
		return updated;
	}

}
