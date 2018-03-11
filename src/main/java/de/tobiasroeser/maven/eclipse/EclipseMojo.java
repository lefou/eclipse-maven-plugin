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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * Generates Eclipse project files from the current Maven project.
 */
@Mojo(name = "eclipse", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class EclipseMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject mavenProject;

	/**
	 * The output directory e.g. for compiled classes. This setting can be
	 * overridden with {@link #alternativeOutput}.
	 */
	@Parameter(required = false, property = "eclipse.outputDirectory", defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	/**
	 * The test output directory e.g. for compiled test classes. This setting
	 * can be overridden with {@link #alternativeOutput}.
	 */
	@Parameter(required = false, property = "eclipse.testOutputDirectory", defaultValue = "${project.build.testOutputDirectory}")
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

	/**
	 * List of Maven profiles that should be activated in Eclipse.
	 */
	@Parameter(required = false, property = "eclipse.activeProfiles")
	private List<String> activeProfiles = new LinkedList<String>();

	/**
	 * Map of settings file templates, which will be placed in the '.settings'
	 * directory. The map entry key is the settings file name. The map entry
	 * value is the template file.
	 */
	@Parameter(required = false, property = "eclipse.settingsTemplates")
	private Map<String, String> settingsTemplates = new LinkedHashMap<>();

	/**
	 * A directory containing files, which should be placed into the '.settings'
	 * directory.
	 */
	@Parameter(required = false, property = "eclipse.settingsTemplatesDir")
	private File settingsTemplatesDir;

	public EclipseMojo() {
	}

	public List<Builder> defaultM2eBuilders() {
		return Arrays.asList(
				new Builder("org.eclipse.m2e.core.maven2Builder", "Default Maven Builder"));
	}

	public List<Builder> defaultJavaBuilders() {
		return Arrays.asList(
				new Builder("org.eclipse.jdt.core.javabuilder", "Default Java Builder"),
				new Builder("org.eclipse.m2e.core.maven2Builder", "Default Maven Builder"));
	}

	public List<Nature> defaultJavaNatures() {
		return Arrays.asList(
				new Nature("org.eclipse.jdt.core.javanature", "Default Java Nature"),
				new Nature("org.eclipse.m2e.core.maven2Nature", "Default Maven Nature"));
	}

	public List<Nature> defaultM2eNatures() {
		return Arrays.asList(
				new Nature("org.eclipse.m2e.core.maven2Nature", "Default Maven Nature"));
	}

	protected ProjectConfig readPomProjectConfig() {
		ProjectConfig projectConfig = new ProjectConfig()
				.withName(Optional.lift(mavenProject.getName())
						.orElse(Optional.lift(mavenProject.getArtifactId()))
						.getOrElse("undefined"))
				.withComment(Optional.lift(mavenProject.getDescription()).getOrElse(""))
				.withSources(mavenProject.getCompileSourceRoots())
				.withTestSources(mavenProject.getTestCompileSourceRoots())
				.withResources(map(mavenProject.getBuild().getResources(), r -> r.getDirectory()))
				.withTestResources(map(mavenProject.getBuild().getTestResources(), r -> r.getDirectory()));

		final Optional<String> encoding = Optional
				.lift(mavenProject.getProperties().getProperty("project.build.sourceEncoding"))
				.orElse(Optional.lift("UTF-8"));
		projectConfig = projectConfig.withEncoding(encoding);

		if (defaultBuilders) {
			// Add M2e nature and builder
			projectConfig = projectConfig
					.withBuilders(concat(projectConfig.getBuilders(), defaultM2eBuilders()))
					.withNatures(concat(projectConfig.getNatures(), defaultM2eNatures()));
		}
		return projectConfig;
	}

	protected ProjectConfig readFullProjectConfig() {

		final List<Builder> autodetectBuilders = !autodetect ? Collections.emptyList() : autodetectableBuilders();
		final List<Nature> autodetectNatures = !autodetect ? Collections.emptyList() : autodetectableNatures();

		// initial config from pom

		ProjectConfig projectConfig = readPomProjectConfig();

		if (defaultBuilders) {
			projectConfig = projectConfig
					.withBuilders(concat(projectConfig.getBuilders(), defaultJavaBuilders()))
					.withNatures(concat(projectConfig.getNatures(), defaultJavaNatures()));
		}

		final Optional<String> javaVersion = Optional
				.lift(mavenProject.getProperties().getProperty("maven.compiler.source"))
				.orElse(Optional.lift(mavenProject.getProperties().getProperty("maven.compiler.target")))
				.orElse(projectConfig.getJavaVersion());
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
		projectConfig = projectConfig
				.withSources(concat(projectConfig.getSources(), extraSources))
				.withResources(concat(projectConfig.getResources(), extraResources))
				.withTestSources(concat(projectConfig.getTestSources(), extraTestSources))
				.withTestResources(concat(projectConfig.getTestResources(), extraTestResources))
				.withBuilders(concat(projectConfig.getBuilders(),
						map(extraBuilders, b -> new Builder(b, "Explicit Builder from pom"))))
				.withNatures(concat(projectConfig.getNatures(),
						map(extraNatures, n -> new Nature(n, "Explicit Nature from pom"))));
		return projectConfig;
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

		final File basedir = mavenProject.getBasedir();
		final Tasks tasks = new Tasks(basedir);

		final String packaging = mavenProject.getPackaging();

		final ProjectConfig projectConfig;
		if ("pom".equals(packaging)) {
			projectConfig = readPomProjectConfig();
		} else {
			projectConfig = readFullProjectConfig();
		}

		// Reading/checking templates
		final Map<String, String> templates = new LinkedHashMap<>();
		// First, add templates from template dir
		if (settingsTemplatesDir != null && settingsTemplatesDir.exists()) {
			final File[] files = Optional.lift(settingsTemplatesDir.listFiles()).getOrElse(new File[0]);
			foreach(
					filter(files, f -> f.isFile()),
					f -> templates.put(f.getName(), f.getAbsolutePath()));
		}
		templates.putAll(settingsTemplates);

		getLog().debug("Final eclipse project config: " + projectConfig);

		final File projectFile = new File(basedir, ".project");
		generateFile(projectFile, dryrun, printStream -> {
			tasks.generateProjectFile(printStream, projectConfig);
		});

		final String orgEclipseM2eCorePrefs = "org.eclipse.m2e.core.prefs";
		if (!templates.containsKey(orgEclipseM2eCorePrefs)) {
			generateFile(new File(basedir, ".settings/" + orgEclipseM2eCorePrefs), dryrun, printStream -> {
				tasks.generateSettingOrgEclipseM2eCorePrefs(printStream, activeProfiles);
			});
		}

		final String orgEclipseCoreResourcesPrefs = "org.eclipse.core.resources.prefs";
		if (!templates.containsKey(orgEclipseCoreResourcesPrefs)) {
			generateFile(new File(basedir, ".settings/" + orgEclipseCoreResourcesPrefs), dryrun, printStream -> {
				tasks.generateSettingOrgEclipseCoreResourcesPrefs(printStream, projectConfig);
			});
		}

		if (!"pom".equals(packaging)) {
			final File classpathFile = new File(basedir, ".classpath");
			generateFile(classpathFile, dryrun, printStream -> {
				tasks.generateClasspathFileContent(
						printStream, projectConfig,
						Optional.lift(alternativeOutput),
						outputDirectory, testOutputDirectory,
						sourcesOptional);
			});

			final String orgEclipseJdtCorePrefs = "org.eclipse.jdt.core.prefs";
			if (!templates.containsKey(orgEclipseJdtCorePrefs)) {
				generateFile(new File(basedir, ".settings/" + orgEclipseJdtCorePrefs), dryrun, printStream -> {
					tasks.generateSettingOrgEclipseJdtCorePrefs(printStream, projectConfig.getJavaVersion());
				});
			}
		}

		try {
			foreach(templates.entrySet(), entry -> {
				final String fileName = ".settings/" + entry.getKey();
				final File templateFile = Optional.some(new File(entry.getValue()))
						.map(f -> f.isAbsolute() ? f : new File(basedir, f.getPath())).get();
				getLog().debug("Processing template file: " + templateFile);
				// we need to wrap the exception because our foreach-loop
				// cannot handle checked exceptions
				try {
					generateFile(new File(basedir, fileName), dryrun, printStream -> {
						try (FileReader in = new FileReader(templateFile);
								LineNumberReader lnr = new LineNumberReader(in);) {
							String line;
							while ((line = lnr.readLine()) != null) {
								printStream.println(line);
							}
						} catch (final IOException e) {
							throw new RuntimeException("WRAP",
									new MojoExecutionException("Could not read template file " + templateFile));
						}
					});
				} catch (final MojoExecutionException e) {
					throw new RuntimeException("WRAP", e);
				}
			});
		} catch (

		final RuntimeException e) {
			if (e.getCause() instanceof MojoExecutionException) {
				throw (MojoExecutionException) e.getCause();
			} else {
				throw e;
			}
		}
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
				getLog().info("(dryrun) I would generate: " + file + " with content:\n" + fileContent);
			});
			out = bout;
		} else {
			if (file.exists()) {
				getLog().info("Overwriting existing file: " + file);
			} else {
				getLog().debug("Writing file: " + file);
				if (file.getParentFile() != null && !file.getParentFile().exists()) {
					// ensure, dir exists
					file.getParentFile().mkdirs();
				}
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
