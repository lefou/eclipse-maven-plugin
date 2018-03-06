package de.tobiasroeser.maven.eclipse;

import java.util.List;

public class ProjectConfig {

	private List<String> sources;
	private List<String> testSources;
	private List<String> resources;
	private List<String> testResources;

	public ProjectConfig(
			final List<String> sources,
			final List<String> testSources,
			final List<String> resources,
			final List<String> testResources) {
		this.sources = sources;
		this.testSources = testSources;
		this.resources = resources;
		this.testResources = testResources;
	}

	public List<String> getSources() {
		return sources;
	}

	public List<String> getTestSources() {
		return testSources;
	}

	public List<String> getResources() {
		return resources;
	}

	public List<String> getTestResources() {
		return testResources;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				" {\n  sources: " + sources +
				"\n  resources: " + resources +
				"\n  testSources: " + testSources +
				"\n  testResources: " + testResources +
				"\n}";
	}
}
