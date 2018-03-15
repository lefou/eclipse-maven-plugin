package de.tobiasroeser.maven.eclipse;

import org.apache.maven.project.MavenProject;

public interface MavenProjectAnalyzer {

	ProjectConfig analyze(ProjectConfig projectConfig, MavenProject mavenProject);

}
