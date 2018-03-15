package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.append;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class M2eProjectAnalyzer implements MavenProjectAnalyzer {

	public static final String ORG_ECLIPSE_M2E_CORE_MAVEN2_NATURE = "org.eclipse.m2e.core.maven2Nature";
	public static final String ORG_ECLIPSE_M2E_CORE_MAVEN2_BUILDER = "org.eclipse.m2e.core.maven2Builder";
	public static final String ORG_ECLIPSE_M2E_MAVEN2_CLASSPATH_CONTAINER = "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER";

	private final boolean addingAllowed;

	public M2eProjectAnalyzer(Log log, boolean addingAllowed) {
		this.addingAllowed = addingAllowed;
	}

	@Override
	public ProjectConfig analyze(final ProjectConfig projectConfig, final MavenProject mavenProject) {
		if (addingAllowed) {
			// Add M2e nature and builder
			return projectConfig
					.withBuilders(append(projectConfig.getBuilders(),
							new Builder(ORG_ECLIPSE_M2E_CORE_MAVEN2_BUILDER, "Default M2E Builder")))
					.withNatures(append(projectConfig.getNatures(),
							new Nature(ORG_ECLIPSE_M2E_CORE_MAVEN2_NATURE, "Defaut M2E Nature")))
					.withClasspathContainers(append(projectConfig.getClasspathContainers(),
							ORG_ECLIPSE_M2E_MAVEN2_CLASSPATH_CONTAINER));
		} else
			return projectConfig;
	}

}
