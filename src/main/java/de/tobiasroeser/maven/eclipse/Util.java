package de.tobiasroeser.maven.eclipse;

import java.io.File;
import java.net.URI;

import org.apache.maven.project.MavenProject;

public abstract class Util {

	public static String relativePath(MavenProject mavenProject, final String file) {
		return relativePath(mavenProject.getBasedir(), file);
	}

	public static String relativePath(File basedir, final String file) {
		final String relPath;
		if (new File(file).isAbsolute()) {
			final URI basePath = basedir.toURI();
			relPath = basePath.relativize(new File(file).toURI()).getPath();
		} else {
			relPath = file;
		}
		if (relPath.length() > 1 && relPath.endsWith("/")) {
			return relPath.substring(0, relPath.length() - 1);
		} else {
			return relPath;
		}
	}

}
