package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.Optional;

public class MinimalPomAnalyzer implements MavenProjectAnalyzer {

	public MinimalPomAnalyzer(final Log log) {
	}

	@Override
	public ProjectConfig analyze(final ProjectConfig projectConfig, final MavenProject mavenProject) {

		ProjectConfig updated = projectConfig
				.withName(Optional.of(mavenProject.getName())
						.orElse(Optional.of(mavenProject.getArtifactId()))
						.getOrElse("undefined"))
				.withComment(Optional.of(mavenProject.getDescription()).getOrElse(""))
				.withSources(mavenProject.getCompileSourceRoots())
				.withTestSources(mavenProject.getTestCompileSourceRoots())
				.withResources(map(mavenProject.getBuild().getResources(), r -> readResource(r)))
				.withTestResources(map(mavenProject.getBuild().getTestResources(), r -> readResource(r)));

		final Optional<String> encoding = Optional
				.of(mavenProject.getProperties().getProperty("project.build.sourceEncoding"))
				.orElse(Optional.of("UTF-8"));
		updated = updated.withEncoding(encoding);

		return updated;
	}

	protected Resource readResource(org.apache.maven.model.Resource resource) {
		return new Resource(resource.getDirectory(), resource.getIncludes(), resource.getExcludes());
	}

}
