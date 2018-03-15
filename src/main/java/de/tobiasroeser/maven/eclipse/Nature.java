package de.tobiasroeser.maven.eclipse;

/**
 * Configuration of an Eclipse Nature, based on information extracted from the
 * Maven pom.
 */
public class Nature {

	private final String name;
	private final String comment;

	public Nature(final String name, final String comment) {
		this.name = name;
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(name=" + name +
				",comment=" + comment +
				")";
	}

}
