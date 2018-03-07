package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.mkString;
import static de.tototec.utils.functional.FList.take;

import java.util.Collections;
import java.util.List;

public class ProjectConfig {

	private final String name;
	private final String comment;
	private final List<String> sources;
	private final List<String> testSources;
	private final List<String> resources;
	private final List<String> testResources;
	private final List<Builder> builders;
	private final List<Nature> natures;
	private String javaVersion;

	public ProjectConfig() {
		this("", "",
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				javaVersion(System.getProperty("java.version")));
	}

	public ProjectConfig(
			final String name,
			final String comment,
			final List<String> sources,
			final List<String> testSources,
			final List<String> resources,
			final List<String> testResources,
			final List<Builder> builders,
			final List<Nature> natures,
			final String javaVersion) {
		this.name = name;
		this.comment = comment;
		this.sources = sources;
		this.testSources = testSources;
		this.resources = resources;
		this.testResources = testResources;
		this.builders = builders;
		this.natures = natures;
		this.javaVersion = javaVersion;
	}

	public static String javaVersion(final String javaVersion) {
		return mkString(take(javaVersion.split("[.]"), 2), ".");
	}

	public String getName() {
		return name;
	}

	public ProjectConfig withName(final String name) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public String getComment() {
		return comment;
	}

	public ProjectConfig withComment(final String comment) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getSources() {
		return sources;
	}

	public ProjectConfig withSources(final List<String> sources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getTestSources() {
		return testSources;
	}

	public ProjectConfig withTestSources(final List<String> testSources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getResources() {
		return resources;
	}

	public ProjectConfig withResources(final List<String> resources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getTestResources() {
		return testResources;
	}

	public ProjectConfig withTestResources(final List<String> testResources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<Builder> getBuilders() {
		return builders;
	}

	public ProjectConfig withBuilders(final List<Builder> builders) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<Nature> getNatures() {
		return natures;
	}

	public ProjectConfig withNatures(final List<Nature> natures) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public ProjectConfig withJavaVersion(final String javaVersion) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " {" +
				"\n  name: " + name +
				"\n  comment: " + comment +
				"\n  sources: " + sources +
				"\n  resources: " + resources +
				"\n  testSources: " + testSources +
				"\n  testResources: " + testResources +
				"\n  builders: " + builders +
				"\n  natures: " + natures +
				"\n}";
	}
}
