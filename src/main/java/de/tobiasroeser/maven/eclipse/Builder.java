package de.tobiasroeser.maven.eclipse;

public class Builder extends Tuple2<String, String> {

	private static final long serialVersionUID = 20180307L;

	public Builder(final String name, final String comment) {
		super(name, comment);
	}

	public String getName() {
		return a();
	}

	public String getComment() {
		return b();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(name=" + getName() +
				",comment=" + getComment() +
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
