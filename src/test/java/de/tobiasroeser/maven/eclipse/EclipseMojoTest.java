package de.tobiasroeser.maven.eclipse;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.io.File;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class EclipseMojoTest extends FreeSpec {

	public EclipseMojoTest() {
		setExpectFailFast(false);

		final String basedir = "/tmp/basedir";
		final EclipseMojo eclipseMojo = new EclipseMojo() {
			@Override
			protected File getBasedir() {
				return new File(basedir);
			}
		};

		section("relativePath should", () -> {
			test("Cut not a single path '/'", () -> {
				expectEquals(eclipseMojo.relativePath("/"), "/");
			});
			test("Cut the last '/' of " + basedir + "/target/", () -> {
				expectEquals(eclipseMojo.relativePath("/"), "/");
				expectEquals(eclipseMojo.relativePath(basedir + "/target/"), "target");
			});
			test("Cut the last '/' of /tmp/", () -> {
				expectEquals(eclipseMojo.relativePath("/"), "/");
				expectEquals(eclipseMojo.relativePath("/tmp/"), "/tmp");
			});

			test("Remove the shared path prefix", () -> {
				expectEquals(eclipseMojo.relativePath(basedir + "/target"), "target");
			});
		});

	}
}
