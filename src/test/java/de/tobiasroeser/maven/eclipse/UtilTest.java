package de.tobiasroeser.maven.eclipse;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.io.File;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class UtilTest extends FreeSpec {

	public UtilTest() {
		setExpectFailFast(false);

		final File basedir = new File("/tmp/basedir");

		section("relativePath should", () -> {
			test("Cut not a single path '/'", () -> {
				expectEquals(Util.relativePath(basedir, "/"), "/");
			});
			test("Cut the last '/' of " + basedir + "/target/", () -> {
				expectEquals(Util.relativePath(basedir, "/"), "/");
				expectEquals(Util.relativePath(basedir, basedir + "/target/"), "target");
			});
			test("Cut the last '/' of /tmp/", () -> {
				expectEquals(Util.relativePath(basedir, "/"), "/");
				expectEquals(Util.relativePath(basedir, "/tmp/"), "/tmp");
			});

			test("Remove the shared path prefix", () -> {
				expectEquals(Util.relativePath(basedir, basedir + "/target"), "target");
			});

			test("Preserve a relative path", () -> {
				expectEquals(Util.relativePath(basedir, "src/main/aspectj"), "src/main/aspectj");
			});
		});

	}
}
