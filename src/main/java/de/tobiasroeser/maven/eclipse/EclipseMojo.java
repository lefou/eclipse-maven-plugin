package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.foreach;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.Optional;

/**
 * Generated Eclipse project files from the current Maven project.
 */
@Mojo(name = "eclipse", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class EclipseMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	@Parameter(required = true, property = "eclipse.outputDirectory", defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	@Parameter(required = true, property = "eclipse.testOutputDirectory", defaultValue = "${project.build.outputDirectory}")
	private String testOutputDirectory;

	/**
	 * Use the alternative build output directory.
	 */
	@Parameter(required = false, property = "eclipse.alternativeOutput")
	private String alternativeOutput;

	/**
	 * Should the generated source paths be optional.
	 */
	@Parameter(required = false, property = "eclipse.sourcesOptional")
	private boolean sourcesOptional = true;

	@Parameter(required = false, property = "eclipse.dryrun")
	private boolean dryrun = false;

	public EclipseMojo() {
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final ProjectConfig projectConfig = new ProjectConfigAnalyzer(mavenProject).getProjectConfig();
		getLog().debug("Analyzer: " + projectConfig);

		final File classpathFile = new File(getBasedir().getPath(), ".classpath");
		if (classpathFile.exists()) {
			getLog().warn("Overwriting existing file: " + classpathFile);
		}
		generateClasspathFile(classpathFile, projectConfig);

	}

	private void generateClasspathFile(final File classpathFile, final ProjectConfig projectConfig)
			throws MojoExecutionException {
		final Charset charset = StandardCharsets.UTF_8;

		final OutputStream out;

		final LinkedList<Runnable> onEnd = new LinkedList<>();

		if (dryrun) {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			onEnd.add(() -> {
				final String fileContent = new String(bout.toByteArray(), charset);
				getLog().info("(dryrun) I would generate: " + classpathFile +
						" with content:\n" + fileContent);
			});
			out = bout;
		} else {
			try {
				out = new FileOutputStream(classpathFile);
			} catch (final FileNotFoundException e) {
				throw new MojoExecutionException("Could not write file: " + classpathFile, e);
			}
		}

		try (final PrintStream printStream = new PrintStream(out, false, charset.name());) {
			generateClasspathFileContent(printStream, projectConfig, Optional.lift(alternativeOutput));
			printStream.flush();
		} catch (final UnsupportedEncodingException e) {
			throw new MojoExecutionException("Could not create print stream", e);
		}

		foreach(onEnd, r -> r.run());
	}

	protected File getBasedir() {
		return mavenProject.getBasedir();
	}

	public String relativePath(final String file) {
		final URI basePath = getBasedir().toURI();
		final String relPath = basePath.relativize(new File(file).toURI()).getPath();
		if (relPath.length() > 1 && relPath.endsWith("/")) {
			return relPath.substring(0, relPath.length() - 1);
		} else {
			return relPath;
		}
	}

	private Optional<String> whenUndefined(final Optional<?> predicate, final String useWhenDefined) {
		if (predicate.isDefined())
			return Optional.none();
		else
			return Optional.some(useWhenDefined);
	}

	public void generateClasspathFileContent(final PrintStream printStream, final ProjectConfig projectConfig,
			final Optional<String> buildOutput) {

		printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		printStream.println("<classpath>");

		// src
		foreach(projectConfig.getSources(),
				s -> generatePath(printStream, "src", s,
						whenUndefined(buildOutput, outputDirectory),
						sourcesOptional));
		foreach(projectConfig.getResources(),
				s -> generatePath(printStream, "src", s,
						whenUndefined(buildOutput, outputDirectory),
						sourcesOptional));
		foreach(projectConfig.getTestSources(),
				s -> generatePath(printStream, "src", s,
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional));
		foreach(projectConfig.getTestResources(),
				s -> generatePath(printStream, "src", s,
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional));

		// con
		generatePath(printStream, "con",
				"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8",
				Optional.none(), false);

		generatePath(printStream, "con", "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", Optional.none(), false);

		// output
		printStream.println("\t<classpathentry kind=\"output\" path=\""
				+ buildOutput.getOrElse(relativePath(outputDirectory)) + "\"/>");

		printStream.println("</classpath>");
	}

	private void generatePath(final PrintStream printStream, final String kind, final String path,
			final Optional<String> outputPath, final boolean optional) {
		printStream.print("\t<classpathentry kind=\"" + kind + "\" path=\"" + relativePath(path) + "\"");
		outputPath.foreach(p -> printStream.print(" output=\"" + relativePath(p) + "\""));
		printStream.println(">");
		printStream.println("\t\t<attributes>");
		if (optional) {
			printStream.println("\t\t\t<attribute name=\"optional\" value=\"true\"/>");
		}
		printStream.println("\t\t\t<attribute name=\"maven.pomderived\" value=\"true\"/>");
		printStream.println("\t\t</attributes>");
		printStream.println("\t</classpathentry>");
	}

}
