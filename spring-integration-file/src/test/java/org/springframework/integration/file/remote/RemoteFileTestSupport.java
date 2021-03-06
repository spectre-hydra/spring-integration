/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.file.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

/**
 * Abstract base class for tests requiring remote file servers, e.g. (S)FTP.
 *
 * @author Gary Russell
 * @since 4.3
 *
 */
public abstract class RemoteFileTestSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	protected static int port;

	@ClassRule
	public static final TemporaryFolder remoteTemporaryFolder = new TemporaryFolder();

	@ClassRule
	public static final TemporaryFolder localTemporaryFolder = new TemporaryFolder();

	@Rule
	public final TestName testName = new TestName();

	protected volatile File sourceRemoteDirectory;

	protected volatile File targetRemoteDirectory;

	protected volatile File sourceLocalDirectory;

	protected volatile File targetLocalDirectory;

	public File getSourceRemoteDirectory() {
		return this.sourceRemoteDirectory;
	}

	public File getTargetRemoteDirectory() {
		return this.targetRemoteDirectory;
	}

	public String getTargetRemoteDirectoryName() {
		return this.targetRemoteDirectory.getAbsolutePath() + File.separator;
	}

	public File getSourceLocalDirectory() {
		return this.sourceLocalDirectory;
	}

	public File getTargetLocalDirectory() {
		return this.targetLocalDirectory;
	}

	public String getTargetLocalDirectoryName() {
		return this.targetLocalDirectory.getAbsolutePath() + File.separator;
	}

	/**
	 * Default implementation creates the following folder structures:
	 *
	 * <pre class="code">
	 *  $ tree remoteSource/
	 *  remoteSource/
	 *  ├── remoteSource1.txt - contains 'source1'
	 *  ├── remoteSource2.txt - contains 'source2'
	 *  ├── subRemoteSource
	 *      ├── subRemoteSource1.txt - contains 'subSource1'
	 *  remoteTarget/
	 *  $ tree localSource/
	 *  localSource/
	 *  ├── localSource1.txt - contains 'local1'
	 *  ├── localSource2.txt - contains 'local2'
	 *  ├── subLocalSource
	 *      ├── subLocalSource1.txt - contains 'subLocal1'
	 *  localTarget/
	 * </pre>
	 *
	 * The intent is tests retrieve from remoteSource and verify arrival in localTarget or send from localSource and verify
	 * arrival in remoteTarget.
	 * <p>
	 * Subclasses can change 'remote' in these names by overriding {@link #prefix()} or override this method completely to
	 * create a different structure.
	 * <p>
	 * While a single server exists for all tests, the directory structure is rebuilt for each test.
	 * @throws IOException IO Exception.
	 */
	@Before
	public void setupFolders() throws IOException {
		String prefix = prefix();
		recursiveDelete(new File(remoteTemporaryFolder.getRoot(), prefix + "Source"));
		this.sourceRemoteDirectory = remoteTemporaryFolder.newFolder(prefix + "Source");
		recursiveDelete(new File(remoteTemporaryFolder.getRoot(), prefix + "Target"));
		this.targetRemoteDirectory = remoteTemporaryFolder.newFolder(prefix + "Target");
		recursiveDelete(new File(localTemporaryFolder.getRoot(), "localSource"));
		this.sourceLocalDirectory = localTemporaryFolder.newFolder("localSource");
		recursiveDelete(new File(localTemporaryFolder.getRoot(), "localTarget"));
		this.targetLocalDirectory = localTemporaryFolder.newFolder("localTarget");

		File file = new File(this.sourceRemoteDirectory, " " + prefix + "Source1.txt");
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write("source1".getBytes());
		fos.close();
		file = new File(this.sourceRemoteDirectory, prefix + "Source2.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("source2".getBytes());
		fos.close();
		String camelCasePrefix = camelCase(prefix);
		File subSourceDirectory = new File(this.sourceRemoteDirectory, "sub" + camelCasePrefix + "Source");
		subSourceDirectory.mkdir();
		file = new File(subSourceDirectory, "sub" + camelCasePrefix + "Source1.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("subSource1".getBytes());
		fos.close();
		file = new File(sourceLocalDirectory, "localSource1.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("local1".getBytes());
		fos.close();
		file = new File(sourceLocalDirectory, "localSource2.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("local2".getBytes());
		fos.close();
		File subSourceLocalDirectory = new File(this.sourceLocalDirectory, "subLocalSource");
		subSourceLocalDirectory.mkdir();
		file = new File(subSourceLocalDirectory, "subLocalSource1.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("subLocal1".getBytes());
		fos.close();
	}

	private String camelCase(String prefix) {
		char[] chars = prefix.toCharArray();
		chars[0] &= 0xdf;
		return new String(chars);
	}

	public void recursiveDelete(File file) {
		if (file != null && file.exists()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File fyle : files) {
					logger.info("Deleting: " + fyle + " in " + testName.getMethodName());
					if (fyle.isDirectory()) {
						recursiveDelete(fyle);
					}
					else {
						if (!fyle.delete()) {
							logger.error("Couldn't delete: " + fyle + " in " + testName.getMethodName());
						}
					}
				}
			}
			logger.info("Deleting: " + file + " in " + testName.getMethodName());
			if (!file.delete()) {
				logger.error("Couldn't delete: " + file + " in " + testName.getMethodName());
				if (file.isDirectory()) {
					logger.error("Contents: " + Arrays.toString(file.listFiles()));
				}
			}
		}
	}

	/**
	 * Prefix for directory/file structure; default 'remote'.
	 * @return the prefix.
	 */
	protected String prefix() {
		return "remote";
	}

}
