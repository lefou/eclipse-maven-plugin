package de.tobiasroeser.maven.eclipse;

import java.util.Collections;
import java.util.List;

public class Resource {

	private final String path;
	private final List<String> includes;
	private final List<String> excludes;

	public Resource(String path, List<String> includes, List<String> excludes) {
		this.path = path;
		this.includes = includes == null ? Collections.emptyList() : includes;
		this.excludes = excludes == null ? Collections.emptyList() : excludes;
	}

	public Resource() {
		this("", Collections.emptyList(), Collections.emptyList());
	}

	public String getPath() {
		return path;
	}

	public Resource withPath(String path) {
		return new Resource(path, includes, excludes);
	}

	public List<String> getIncludes() {
		return includes;
	}

	public Resource withIncludes(List<String> includes) {
		return new Resource(path, includes, excludes);
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public Resource withExcludes(List<String> excludes) {
		return new Resource(path, includes, excludes);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(path=" + path +
				",includes=" + includes +
				",excludes=" + excludes +
				")";
	}

}
