package de.tobiasroeser.maven.eclipse;

import java.util.Collections;
import java.util.List;

/**
 * Configuration of an Eclipse Builder, based on information extracted from the
 * Maven pom.
 */
public class Builder extends Tuple4<String, String, List<String>, List<String>> {

	private static final long serialVersionUID = 20180307L;

	public Builder(final String name, final String comment, final List<String> disablesBuilders,
			final List<String> mavenPluginKeys) {
		super(name, comment, disablesBuilders, mavenPluginKeys);
	}

	public Builder(final String name, final String comment) {
		this(name, comment, Collections.emptyList(), Collections.emptyList());
	}

	public String getName() {
		return a();
	}

	public String getComment() {
		return b();
	}

	public List<String> getDisablesBuilders() {
		return c();
	}

	public List<String> getMavenPluginKeys() {
		return d();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(name=" + getName() +
				",comment=" + getComment() +
				",disablesBuilder=" + getDisablesBuilders() +
				",mavenPluginKeys=" + getMavenPluginKeys() +
				")";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object other) {
		return (other instanceof Builder) && super.equals(other);
	}

	@Override
	public boolean canEqual(final Object other) {
		return other instanceof Builder;
	}

}
