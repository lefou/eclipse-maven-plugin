package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.append;
import static de.tototec.utils.functional.FList.exists;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class AspectjProjectAnalyzer implements MavenProjectAnalyzer {

	public static final String ORG_CODEHAUS_MOJO_ASPECTJ_MAVEN_PLUGIN = "org.codehaus.mojo:aspectj-maven-plugin";
	public static final String ORG_ECLIPSE_AJDT_CORE_AJBUILDER = "org.eclipse.ajdt.core.ajbuilder";
	public static final String ORG_ECLIPSE_AJDT_UI_AJNATURE = "org.eclipse.ajdt.ui.ajnature";

	private final Log log;
	private final boolean addingAllowed;

	public AspectjProjectAnalyzer(Log log, boolean addingAllowed) {
		this.log = log;
		this.addingAllowed = addingAllowed;
	}

	@Override
	public ProjectConfig analyze(ProjectConfig projectConfig, MavenProject mavenProject) {
		ProjectConfig updated = projectConfig;

		boolean pluginDetected = exists(
				mavenProject.getPluginArtifactMap().keySet(),
				key -> ORG_CODEHAUS_MOJO_ASPECTJ_MAVEN_PLUGIN.equals(key));

		if (pluginDetected) {
			log.debug("Detected aspectj plugin");

			if (addingAllowed) {
				log.debug("Adding scala nature and builder, disabling java nature and builder");
				updated = updated
						.withNatures(append(updated.getNatures(),
								new Nature(ORG_ECLIPSE_AJDT_UI_AJNATURE, "Auto-detected from pom")))
						.withBuilders(append(updated.getBuilders(),
								new Builder(ORG_ECLIPSE_AJDT_CORE_AJBUILDER, "Auto-detected from pom")))
						.withDisabledBuilders(append(updated.getDisabledBuilders(),
								JavaProjectAnalyzer.ORG_ECLIPSE_JDT_CORE_JAVABUILDER));
			}

			// TODO: generate settings

			// TODO: add aspectj classpath container (if no aspect lib is on the path)
		}

		return updated;
	}

}
