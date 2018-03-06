package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.map;

import java.util.List;

import org.apache.maven.project.MavenProject;

public class ProjectConfigAnalyzer {

	private MavenProject mavenProject;

	public ProjectConfigAnalyzer(final MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	public ProjectConfig getProjectConfig() {

		final List<String> compileSourceRoots = mavenProject.getCompileSourceRoots();

		final List<String> testCompileSourceRoots = mavenProject.getTestCompileSourceRoots();

		final List<String> resources = map(mavenProject.getBuild().getResources(), r -> r.getDirectory());

		final List<String> testResources = map(mavenProject.getBuild().getTestResources(), r -> r.getDirectory());

		final ProjectConfig projectConfig = new ProjectConfig(
				compileSourceRoots,
				testCompileSourceRoots,
				resources,
				testResources);

		return projectConfig;
	}

}
