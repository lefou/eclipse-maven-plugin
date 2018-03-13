package de.tobiasroeser.maven.eclipse;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class ProjectConfigTest extends FreeSpec {

	public ProjectConfigTest() {
		setExpectFailFast(false);

		section("ProjectConfig.javaVersion should", () -> {

			test("shorten Java versions up to 1.8 to a two-part version number", () -> {
				expectEquals(ProjectConfig.javaVersion("1.7.0_13"), "1.7");
				expectEquals(ProjectConfig.javaVersion("1.8"), "1.8");
				expectEquals(ProjectConfig.javaVersion("1.8.0_162"), "1.8");
			});

			test("shorten Java version >= 9 to a one-part version number", () -> {
				expectEquals(ProjectConfig.javaVersion("9.0"), "9");
			});
		});

	}
}
