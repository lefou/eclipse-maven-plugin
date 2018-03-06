package de.tobiasroeser.maven.eclipse;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.io.File;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class TasksTest extends FreeSpec {

	public TasksTest() {
		setExpectFailFast(false);

		final String basedir = "/tmp/basedir";
		final Tasks tasks = new Tasks(new File(basedir));

		section("relativePath should", () -> {
			test("Cut not a single path '/'", () -> {
				expectEquals(tasks.relativePath("/"), "/");
			});
			test("Cut the last '/' of " + basedir + "/target/", () -> {
				expectEquals(tasks.relativePath("/"), "/");
				expectEquals(tasks.relativePath(basedir + "/target/"), "target");
			});
			test("Cut the last '/' of /tmp/", () -> {
				expectEquals(tasks.relativePath("/"), "/");
				expectEquals(tasks.relativePath("/tmp/"), "/tmp");
			});

			test("Remove the shared path prefix", () -> {
				expectEquals(tasks.relativePath(basedir + "/target"), "target");
			});
		});

	}
}
