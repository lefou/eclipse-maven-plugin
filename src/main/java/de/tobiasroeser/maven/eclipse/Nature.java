package de.tobiasroeser.maven.eclipse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tototec.utils.functional.Procedure1;

/**
 * Configuration of an Eclipse Nature, based on information extracted from the
 * Maven pom.
 */
public class Nature {

	private final String name;
	private final String comment;
	private final List<String> disablesNatures;
	private final List<String> mavenPluginKeys;
	private final Map<String, Procedure1<StringBuilder>> settingsGenerators;

	public Nature(final String name, final String comment, final List<String> disablesNatures,
			final List<String> mavenPluginKeys, Map<String, Procedure1<StringBuilder>> settingsGenerators) {
		this.name = name;
		this.comment = comment;
		this.disablesNatures = Collections.unmodifiableList(new LinkedList<>(disablesNatures));
		this.mavenPluginKeys = Collections.unmodifiableList(new LinkedList<>(mavenPluginKeys));
		this.settingsGenerators = Collections.unmodifiableMap(new LinkedHashMap<>(settingsGenerators));
	}

	public Nature(final String name, final String comment, final List<String> disablesNatures,
			final List<String> mavenPluginKeys) {
		this(name, comment, disablesNatures, mavenPluginKeys, Collections.emptyMap());
	}

	public Nature(final String name, final String comment) {
		this(name, comment, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public List<String> getDisablesNatures() {
		return disablesNatures;
	}

	public List<String> getMavenPluginKeys() {
		return mavenPluginKeys;
	}

	public Map<String, Procedure1<StringBuilder>> getSettingsGenerators() {
		return settingsGenerators;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(name=" + name +
				",comment=" + comment +
				",disablesNatures=" + disablesNatures +
				",mavenPluginKeys=" + mavenPluginKeys +
				",settingsGenerators=" + settingsGenerators +
				")";
	}

}
