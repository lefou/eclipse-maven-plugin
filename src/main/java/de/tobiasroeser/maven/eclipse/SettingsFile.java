package de.tobiasroeser.maven.eclipse;

import java.util.List;

public class SettingsFile {

	private String name;
	private List<String> content;

	public SettingsFile(String name, List<String> content) {
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return name;
	}
	
	public List<String> getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(name=" + name +
				",content=" + content.size() + " lines" +
				")";
	}
}
