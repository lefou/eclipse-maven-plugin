package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.concat;
import static de.tototec.utils.functional.FList.exists;
import static de.tototec.utils.functional.FList.filter;
import static de.tototec.utils.functional.FList.foreach;
import static de.tototec.utils.functional.FList.map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.F1;
import de.tototec.utils.functional.Optional;
import de.tototec.utils.functional.Procedure1;

/**
 * Generated Eclipse project files from the current Maven project.
 */
@Mojo(name = "eclipse", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class EclipseMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	/**
	 * The output directory e.g. for compiled classes. This setting can be
	 * overridden with {@link #alternativeOutput}.
	 */
	@Parameter(required = true, property = "eclipse.outputDirectory", defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	/**
	 * The test output directory e.g. for compiled test classes. This setting
	 * can be overridden with {@link #alternativeOutput}.
	 */
	@Parameter(required = true, property = "eclipse.testOutputDirectory", defaultValue = "${project.build.testOutputDirectory}")
	private String testOutputDirectory;

	/**
	 * Use the alternative build output directory.
	 */
	@Parameter(required = false, property = "eclipse.alternativeOutput")
	private String alternativeOutput;

	/**
	 * Should the generated source paths be optional.
	 */
	@Parameter(required = false, property = "eclipse.sourcesOptional", defaultValue = "true")
	private boolean sourcesOptional = true;

	/**
	 * When <code>true</code>, no files will be written but their content will
	 * be written to the log/console.
	 */
	@Parameter(required = false, property = "eclipse.dryrun", defaultValue = "false")
	private boolean dryrun = false;

	/**
	 * If <code>true</code>, execution is skipped and nothings will be
	 * generated.
	 */
	@Parameter(required = false, property = "eclipse.skip", defaultValue = "false")
	private boolean skip = false;

	/**
	 * When <code>true</code>, the predefined default builders will be added to
	 * the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.defaultBuilders", defaultValue = "true")
	private boolean defaultBuilders = true;

	/**
	 * When <code>true</code>, the predefined default natures will be added to
	 * the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.defaultNatures", defaultValue = "true")
	private boolean defaultNatures = true;

	/**
	 * Add additional builders to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraBuilders")
	private List<String> extraBuilders = new LinkedList<String>();

	/**
	 * Add additional natures to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraNatures")
	private List<String> extraNatures = new LinkedList<String>();

	/**
	 * Add additional source directories to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraSources")
	private List<String> extraSources = new LinkedList<String>();

	/**
	 * Add additional resource directories to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraResources")
	private List<String> extraResources = new LinkedList<String>();

	/**
	 * Add additional test source directories to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraTestSources")
	private List<String> extraTestSources = new LinkedList<String>();

	/**
	 * Add additional test resource directories to the eclipse project.
	 */
	@Parameter(required = false, property = "eclipse.extraTestResources")
	private List<String> extraTestResources = new LinkedList<String>();

	/**
	 * Try to auto-detect additional builders and natures.
	 */
	@Parameter(required = false, property = "eclipse.autodetect", defaultValue = "true")
	private boolean autodetect = true;

	public EclipseMojo() {
	}

	public List<Builder> defaultBuilders() {
		return Arrays.asList(
				new Builder("org.eclipse.jdt.core.javabuilder", "Default Java Builder"),
				new Builder("org.eclipse.m2e.core.maven2Builder", "Default Maven Builder"));
	}

	public List<Nature> defaultNatures() {
		return Arrays.asList(
				new Nature("org.eclipse.jdt.core.javanature", "Default Java Nature"),
				new Nature("org.eclipse.m2e.core.maven2Nature", "Default Maven Nature"));
	}

	protected ProjectConfig readProjectConfig() {

		final List<Builder> defBuilders = !defaultBuilders ? Collections.emptyList() : defaultBuilders();
		final List<Nature> defNatures = !defaultNatures ? Collections.emptyList() : defaultNatures();

		final List<Builder> autodetectBuilders = !autodetect ? Collections.emptyList() : autodetectableBuilders();
		final List<Nature> autodetectNatures = !autodetect ? Collections.emptyList() : autodetectableNatures();

		// initial config from pom

		ProjectConfig projectConfig = new ProjectConfig()
				.withName(Optional.lift(mavenProject.getName())
						.orElse(Optional.lift(mavenProject.getArtifactId()))
						.getOrElse("undefined"))
				.withComment(Optional.lift(mavenProject.getDescription()).getOrElse(""))
				.withSources(mavenProject.getCompileSourceRoots())
				.withTestSources(mavenProject.getTestCompileSourceRoots())
				.withResources(map(mavenProject.getBuild().getResources(), r -> r.getDirectory()))
				.withTestResources(map(mavenProject.getBuild().getTestResources(), r -> r.getDirectory()))
				.withBuilders(defBuilders)
				.withNatures(defNatures);

		final String javaVersion = Optional.lift(mavenProject.getProperties().getProperty("maven.compiler.source"))
				.orElse(Optional.lift(mavenProject.getProperties().getProperty("maven.compiler.target")))
				.getOrElse(projectConfig.getJavaVersion());
		projectConfig = projectConfig.withJavaVersion(javaVersion);

		final List<Plugin> plugins = mavenProject.getBuild().getPlugins();
		for (final Plugin plugin : plugins) {
			{
				// Auto-detectable Builders
				final List<Builder> detectedBuilders = filter(
						autodetectBuilders,
						a -> exists(a.getMavenPluginKeys(), k -> k.equals(plugin.getKey())));

				if (!detectedBuilders.isEmpty()) {
					getLog().debug("Auto-detected builders [" + detectedBuilders + "] for plugin ["
							+ plugin.getKey() + "]");
				}

				// determines, if a given builderName should be excluded
				final F1<String, Boolean> excludeB = builderName -> exists(
						detectedBuilders,
						b -> exists(
								b.getDisablesBuilders(),
								disablesBuilder -> {
									final boolean disable = builderName.equals(disablesBuilder);
									if (disable) {
										getLog().debug(
												"Auto-detected builder [" + b + "] disables default builder ["
														+ builderName + "]");
									}
									return disable;
								}));

				// default Builder (if allowed) minus excluded ones plus
				// auto-detected ones
				final List<Builder> filteredBuilders = filter(projectConfig.getBuilders(),
						b -> !excludeB.apply(b.getName()));

				projectConfig = projectConfig.withBuilders(concat(filteredBuilders, detectedBuilders));
			}

			{
				// Auto-detectable Natures
				final List<Nature> detectedNatures = filter(
						autodetectNatures,
						a -> exists(a.getMavenPluginKeys(), k -> k.equals(plugin.getKey())));

				if (!detectedNatures.isEmpty()) {
					getLog().debug("Auto-detected natures [" + detectedNatures + "] for plugin ["
							+ plugin.getKey() + "]");
				}

				// determines, if a given natureName should be excluded
				final F1<String, Boolean> excludeN = natureName -> exists(
						detectedNatures,
						n -> exists(
								n.getDisablesNatures(),
								disablesNature -> {
									final boolean disable = natureName.equals(disablesNature);
									if (disable) {
										getLog().debug("Auto-detected nature [" + n + "] disables default nature ["
												+ natureName + "]");
									}
									return disable;
								}));

				// default nature (if allowed) minus excluded ones plus
				// auto-detected ones
				final List<Nature> filteredNatures = filter(
						projectConfig.getNatures(),
						b -> !excludeN.apply(b.getName()));

				projectConfig = projectConfig.withNatures(concat(filteredNatures, detectedNatures));

			}
		}

		// enhance with config values
		return projectConfig
				.withSources(concat(projectConfig.getSources(), extraSources))
				.withResources(concat(projectConfig.getResources(), extraResources))
				.withTestSources(concat(projectConfig.getTestSources(), extraTestSources))
				.withTestResources(concat(projectConfig.getTestResources(), extraTestResources))
				.withBuilders(concat(projectConfig.getBuilders(),
						map(extraBuilders, b -> new Builder(b, "Explicit Builder from pom"))))
				.withNatures(concat(projectConfig.getNatures(),
						map(extraNatures, n -> new Nature(n, "Explicit Nature from pom"))));
	}

	protected List<Nature> autodetectableNatures() {
		return Arrays.asList(
				new Nature("org.eclipse.ajdt.ui.ajnature", "Auto-detected AspectJ Nature from pom",
						Collections.emptyList(),
						Arrays.asList("org.codehaus.mojo:aspectj-maven-plugin")),
				new Nature(
						"org.scala-ide.sdt.core.scalanature", "Auto-detected Scala Nature from pom",
						Collections.emptyList(),
						Arrays.asList(
								"net.alchim31.maven:scala-maven-plugin",
								"com.google.code.sbt-compiler-maven-plugin:sbt-compiler-maven-plugin",
								"com.carrotgarden.maven:scalor-maven-plugin_2.12",
								"com.carrotgarden.maven:scalor-maven-plugin_2.13")));
	}

	protected List<Builder> autodetectableBuilders() {
		return Arrays.asList(
				new Builder(
						"org.eclipse.ajdt.core.ajbuilder", "Auto-detected AspectJ Builder from pom",
						Arrays.asList("org.eclipse.jdt.core.javabuilder"),
						Arrays.asList("org.codehaus.mojo:aspectj-maven-plugin")),
				new Builder(
						"org.scala-ide.sdt.core.scalabuilder", "Auto-detected Scala Builder from pom",
						Arrays.asList("org.eclipse.jdt.core.javabuilder"),
						Arrays.asList(
								"net.alchim31.maven:scala-maven-plugin",
								"com.google.code.sbt-compiler-maven-plugin:sbt-compiler-maven-plugin",
								"com.carrotgarden.maven:scalor-maven-plugin_2.12",
								"com.carrotgarden.maven:scalor-maven-plugin_2.13")));
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping eclipse");
			return;
		}

		final ProjectConfig projectConfig = readProjectConfig();
		getLog().debug("Final eclipse project config: " + projectConfig);

		final File basedir = mavenProject.getBasedir();
		final Tasks tasks = new Tasks(basedir);

		final File projectFile = new File(basedir.getPath(), ".project");
		generateFile(projectFile, dryrun, printStream -> {
			tasks.generateProjectFile(printStream, projectConfig);
		});

		final File classpathFile = new File(basedir.getPath(), ".classpath");
		generateFile(classpathFile, dryrun, printStream -> {
			tasks.generateClasspathFileContent(
					printStream, projectConfig,
					Optional.lift(alternativeOutput),
					outputDirectory, testOutputDirectory,
					sourcesOptional);
		});
	}

	protected void generateFile(final File file, final boolean dryrun, final Procedure1<PrintStream> generator)
			throws MojoExecutionException {

		final Charset charset = StandardCharsets.UTF_8;
		final OutputStream out;
		final LinkedList<Runnable> onEnd = new LinkedList<>();

		if (dryrun) {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			onEnd.add(() -> {
				final String fileContent = new String(bout.toByteArray(), charset);
				getLog().info("(dryrun) I would generate: " + file +
						" with content:\n" + fileContent);
			});
			out = bout;
		} else {
			if (file.exists()) {
				getLog().info("Overwriting existing file: " + file);
			}
			try {
				out = new FileOutputStream(file);
			} catch (final FileNotFoundException e) {
				throw new MojoExecutionException("Could not write file: " + file, e);
			}
		}

		try (final PrintStream printStream = new PrintStream(out, false, charset.name());) {
			generator.apply(printStream);
			printStream.flush();
		} catch (final UnsupportedEncodingException e) {
			throw new MojoExecutionException("Could not create print stream", e);
		}

		foreach(onEnd, r -> r.run());
	}

}
