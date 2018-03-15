package de.tobiasroeser.maven.eclipse;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class RuntimeMojoException extends RuntimeException {

	private static final long serialVersionUID = 20180515L;

	public RuntimeMojoException(MojoExecutionException wrapped) {
		super(wrapped);
	}
	
	public RuntimeMojoException(MojoFailureException wrapped) {
		super(wrapped);
	}

}
