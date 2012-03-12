package org.recxx.writer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class TestFileManager {

	private final String testFileName = FileUtils.getTempDirectoryPath() + "csvLoggerTestFile.csv";

	public String getTestFileName() {
		return testFileName;
	}

	public File getTestFile() {
		return new File(testFileName);
	}

	public long getLastModifiedTimestampOfTestFile() throws IOException {
		return getTestFile().lastModified();
	}

	public long getLengthOfTestFile() throws IOException {
		return getTestFile().length();
	}

	public void deleteTestFile() throws IOException {
		getTestFile().delete();
	}

	public String getLastModifiedTimestampOfTestFileAsString() throws IOException {
		long lastModifiedTimestampOfTestFile = getLastModifiedTimestampOfTestFile();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date lastModifiedDateOfTestFile = new Date(lastModifiedTimestampOfTestFile);
		String lastModifiedDateStringOfTestFile = dateFormat.format(lastModifiedDateOfTestFile);
		return lastModifiedDateStringOfTestFile;
	}
}
