import java.io.File
import java.nio.file.Files
import java.util.*
import static de.tobiasroeser.lambdatest.Expect.*

expectEquals(
  basedir.listFiles().collect { it.getName() }.toSet(), 
  ["build.log", "pom.xml", "verify.groovy", ".project", ".classpath", ".settings"].toSet()
)

settingsdir = new File(basedir, ".settings")

expectEquals(
  settingsdir.listFiles().collect { it.getName() }.toSet(),
  ["org.eclipse.core.resources.prefs", "org.eclipse.jdt.core.prefs", "org.eclipse.m2e.core.prefs"].toSet()
)
  
expectEquals(
  Files.readAllLines(new File(basedir , ".project").toPath()).join("\n"),
  """<?xml version="1.0" encoding="UTF-8"?>
<!-- Generated by eclipse-maven-plugin -->
<projectDescription>
	<name>project name</name>
	<comment>project description</comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.ajdt.core.ajbuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.m2e.core.maven2Builder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
		<nature>org.eclipse.ajdt.ui.ajnature</nature>
		<nature>org.eclipse.m2e.core.maven2Nature</nature>
	</natures>
</projectDescription>"""
)

expectEquals(
  Files.readAllLines(new File(basedir , ".classpath").toPath()).join("\n"),
  """<?xml version="1.0" encoding="UTF-8"?>
<!-- Generated by eclipse-maven-plugin -->
<classpath>
	<classpathentry kind="src" path="src/main/java">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="src" path="src/main/aspect">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="src" path="src/main/resources">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="src" path="src/test/java">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="test" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="src" path="src/test/resources">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="test" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="output" path="target-ide"/>
</classpath>"""
)

expectEquals(
  Files.readAllLines(new File(settingsdir, "org.eclipse.m2e.core.prefs").toPath()).join("\n"),
  """activeProfiles=
eclipse.preferences.version=1
resolveWorkspaceProjects=true
version=1"""
)

expectEquals(
  Files.readAllLines(new File(settingsdir, "org.eclipse.core.resources.prefs").toPath()).join("\n"),
  """eclipse.preferences.version=1
encoding//src/main/java=UTF-8
encoding//src/main/aspect=UTF-8
encoding//src/main/resources=UTF-8
encoding//src/test/java=UTF-8
encoding//src/test/resources=UTF-8
encoding/<project>=UTF-8"""
)

expectEquals(
  Files.readAllLines(new File(settingsdir, "org.eclipse.jdt.core.prefs").toPath()).join("\n"),
  """eclipse.preferences.version=1
org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8
org.eclipse.jdt.core.compiler.compliance=1.8
org.eclipse.jdt.core.compiler.source=1.8"""
)

