package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.concat;
import static de.tototec.utils.functional.FList.filter;
import static de.tototec.utils.functional.FList.foldLeft;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.tototec.utils.functional.Optional;
import de.tototec.utils.functional.Procedure1;

/**
 * Generates Eclipse project files from the current Maven project.
 * The typical files are:
 * <ul>
 * <li><code>.project</code>
 * <li><code>.classpath</code>
 * <li><code>.settings/org.eclipse.m2e.core.prefs</code>
 * <li><code>.settings/org.eclipse.core.resources.prefs</code>
 * <li><code>.settings/org.eclipse.jdt.core.prefs</code>
 * </ul>
 */
@Mojo(name = "eclipse", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class EclipseMojo extends AbstractMojo {

	private static final String ORG_ECLIPSE_JDT_CORE_PREFS = "org.eclipse.jdt.core.prefs";
	private static final String ORG_ECLIPSE_CORE_RESOURCES_PREFS = "org.eclipse.core.resources.prefs";
	private static final String ORG_ECLIPSE_M2E_CORE_PREFS = "org.eclipse.m2e.core.prefs";

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
	 * value is the template file. Settings files listed here will be used as
	 * is, even if this plugin would otherwise generate them based on the maven
	 * setup.
	 */
	@Parameter(required = false, property = "eclipse.settingsTemplates")
	private Map<String, String> settingsTemplates = new LinkedHashMap<>();

	/**
	 * A directory containing files, which should be placed into the '.settings'
	 * directory. This works almost like {@link #settingsTemplates}, but
	 * auto-scans the directory.
	 */
	@Parameter(required = false, property = "eclipse.settingsTemplatesDir")
	private File settingsTemplatesDir;

	public EclipseMojo() {
	}

	protected ProjectConfig extraConfigEnhancements(final ProjectConfig projectConfig, MavenProject mavenProject) {
		// enhance with config values
		return projectConfig
				.withSources(concat(projectConfig.getSources(), extraSources))
				.withResources(concat(projectConfig.getResources(),
						map(extraResources, r -> new Resource().withPath(r))))
				.withTestSources(concat(projectConfig.getTestSources(), extraTestSources))
				.withTestResources(concat(projectConfig.getTestResources(),
						map(extraTestResources, r -> new Resource().withPath(r))))
				.withBuilders(concat(projectConfig.getBuilders(),
						map(extraBuilders, b -> new Builder(b, "Explicit Builder from pom"))))
				.withNatures(concat(projectConfig.getNatures(),
						map(extraNatures, n -> new Nature(n, "Explicit Nature from pom"))));
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping eclipse");
			return;
		}

		final File basedir = mavenProject.getBasedir();
		final Tasks tasks = new Tasks(basedir, Optional.of(getLog()));

		final String packaging = mavenProject.getPackaging();

		List<MavenProjectAnalyzer> analyzers = Arrays.asList(
				new MinimalPomAnalyzer(getLog()),
				new JavaProjectAnalyzer(getLog(), defaultBuilders),
				new ScalaProjectAnalyzer(getLog(), autodetect),
				new AspectjProjectAnalyzer(getLog(), autodetect),
				(pc, mp) -> extraConfigEnhancements(pc, mp),
				new M2eProjectAnalyzer(getLog(), defaultBuilders));

		ProjectConfig projectConfig = foldLeft(
				analyzers,
				new ProjectConfig(),
				(pc, a) -> a.analyze(pc, mavenProject));

		// Reading/checking templates
		final Map<String, String> templates = new LinkedHashMap<>();
		// First, add templates from template dir
		if (settingsTemplatesDir != null && settingsTemplatesDir.exists()) {
			final File[] files = Optional.of(settingsTemplatesDir.listFiles()).getOrElse(new File[0]);
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

		if (!templates.containsKey(ORG_ECLIPSE_M2E_CORE_PREFS)) {
			generateFile(new File(basedir, ".settings/" + ORG_ECLIPSE_M2E_CORE_PREFS), dryrun, printStream -> {
				tasks.generateSettingOrgEclipseM2eCorePrefs(printStream, activeProfiles);
			});
		}

		if (!templates.containsKey(ORG_ECLIPSE_CORE_RESOURCES_PREFS)) {
			generateFile(new File(basedir, ".settings/" + ORG_ECLIPSE_CORE_RESOURCES_PREFS), dryrun, printStream -> {
				tasks.generateSettingOrgEclipseCoreResourcesPrefs(printStream, projectConfig);
			});
		}

		if (!"pom".equals(packaging)) {
			final File classpathFile = new File(basedir, ".classpath");
			generateFile(classpathFile, dryrun, printStream -> {
				tasks.generateClasspathFileContent(
						printStream, projectConfig,
						Optional.of(alternativeOutput),
						outputDirectory, testOutputDirectory,
						sourcesOptional);
			});

			if (!templates.containsKey(ORG_ECLIPSE_JDT_CORE_PREFS)) {
				generateFile(new File(basedir, ".settings/" + ORG_ECLIPSE_JDT_CORE_PREFS), dryrun, printStream -> {
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
