package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.distinct;
import static de.tototec.utils.functional.FList.mkString;
import static de.tototec.utils.functional.FList.take;

import java.util.Collections;
import java.util.List;

import de.tototec.utils.functional.Optional;

/**
 * Project configuration data used to generate Eclipse project files.
 */
public class ProjectConfig {

	private final String name;
	private final String comment;
	private final List<String> sources;
	private final List<String> testSources;
	private final List<Resource> resources;
	private final List<Resource> testResources;
	private final List<Builder> builders;
	private final List<Nature> natures;
	private final Optional<String> javaVersion;
	private final Optional<String> encoding;
	private final List<String> disabledNatures;
	private final List<String> disabledBuilders;
	private final List<String> classpathContainers;

	public ProjectConfig() {
		this("", "",
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Optional.of(System.getProperty("java.version")).map(v -> javaVersion(v)),
				Optional.none(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList());
	}

	public ProjectConfig(
			final String name,
			final String comment,
			final List<String> sources,
			final List<String> testSources,
			final List<Resource> resources,
			final List<Resource> testResources,
			final List<Builder> builders,
			final List<Nature> natures,
			final Optional<String> javaVersion,
			final Optional<String> encoding,
			List<String> disabledNatures,
			List<String> disabledBuilders,
			List<String> classpathContainers) {
		this.name = name;
		this.comment = comment;
		this.sources = distinct(sources);
		this.testSources = distinct(testSources);
		this.resources = distinct(resources);
		this.testResources = distinct(testResources);
		this.builders = distinct(builders);
		this.natures = distinct(natures);
		this.javaVersion = javaVersion;
		this.encoding = encoding;
		this.disabledNatures = disabledNatures;
		this.disabledBuilders = disabledBuilders;
		this.classpathContainers = classpathContainers;
	}

	public static String javaVersion(final String javaVersion) {
		String[] parts = javaVersion.split("[.]");
		if (parts[0].startsWith("9") || parts[0].length() > 1) {
			return parts[0];
		} else {
			return mkString(take(parts, 2), ".");
		}
	}

	public String getName() {
		return name;
	}

	public ProjectConfig withName(final String name) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public String getComment() {
		return comment;
	}

	public ProjectConfig withComment(final String comment) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<String> getSources() {
		return sources;
	}

	public ProjectConfig withSources(final List<String> sources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<String> getTestSources() {
		return testSources;
	}

	public ProjectConfig withTestSources(final List<String> testSources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<Resource> getResources() {
		return resources;
	}

	public ProjectConfig withResources(final List<Resource> resources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<Resource> getTestResources() {
		return testResources;
	}

	public ProjectConfig withTestResources(final List<Resource> testResources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<Builder> getBuilders() {
		return builders;
	}

	public ProjectConfig withBuilders(final List<Builder> builders) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<Nature> getNatures() {
		return natures;
	}

	public ProjectConfig withNatures(final List<Nature> natures) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public Optional<String> getJavaVersion() {
		return javaVersion;
	}

	public ProjectConfig withJavaVersion(final Optional<String> javaVersion) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public Optional<String> getEncoding() {
		return encoding;
	}

	public ProjectConfig withEncoding(final Optional<String> encoding) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<String> getDisabledBuilders() {
		return disabledBuilders;
	}

	public ProjectConfig withDisabledBuilders(final List<String> disabledBuilders) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<String> getDisabledNatures() {
		return disabledNatures;
	}

	public ProjectConfig withDisabledNatures(final List<String> disabledNatures) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
	}

	public List<String> getClasspathContainers() {
		return classpathContainers;
	}

	public ProjectConfig withClasspathContainers(List<String> classpathContainers) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion, encoding, disabledNatures, disabledBuilders, classpathContainers);
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
				"\n  javaVersion: " + javaVersion +
				"\n  encoding: " + encoding +
				"\n}";
	}
}
