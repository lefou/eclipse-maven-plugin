package de.tobiasroeser.maven.eclipse;

import java.util.Collections;
import java.util.List;

public class Nature extends Tuple4<String, String, List<String>, List<String>> {

	private static final long serialVersionUID = 20180307L;

	public Nature(final String name, final String comment, final List<String> disablesNatures,
			final List<String> mavenPluginKeys) {
		super(name, comment, disablesNatures, mavenPluginKeys);
	}

	public Nature(final String name, final String comment) {
		this(name, comment, Collections.emptyList(), Collections.emptyList());
	}

	public String getName() {
		return a();
	}

	public String getComment() {
		return b();
	}

	public List<String> getDisablesNatures() {
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
				",disablesNatrues=" + getDisablesNatures() +
				",mavenPluginKeys=" + getMavenPluginKeys() +
				")";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object other) {
		return (other instanceof Nature) && super.equals(other);
	}

	@Override
	public boolean canEqual(final Object other) {
		return other instanceof Nature;
	}

}
