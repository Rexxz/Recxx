package org.recxx.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.recxx.domain.Default;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public class FileAssert {

	private static void processLine(int lineNumber,
										BufferedReader expectedData, 
										BufferedReader actualData)
										throws IOException {

		StringBuilder sb = new StringBuilder();
		String expectedLine = expectedData.readLine();
		if (!actualData.ready()) {
			sb.append("at line ").append(lineNumber).append(", expected:").append(Default.LINE_DELIMITER)
				.append(expectedLine).append(Default.LINE_DELIMITER)
				.append("but actual file was not available for read operation at this line");
			Assert.fail(sb.toString());
		} 
		else {
			String actualLine = actualData.readLine();
			if (!expectedLine.equals(actualLine)) {
				sb.append("at line ").append(lineNumber)
					.append(" there was a mismatch:").append(Default.LINE_DELIMITER)
					.append("Expect: ").append(expectedLine).append(Default.LINE_DELIMITER)
					.append("Actual: ").append(actualLine).append(Default.LINE_DELIMITER);
				Assert.fail(sb.toString());
			}
		}
	}

	public static void assertEquals(BufferedReader expected, BufferedReader actual) throws Exception {
		Assert.assertNotNull(expected);
		Assert.assertNotNull(actual);
		boolean problem = false;
		try {
			int lineCounter = 0;
			while (expected.ready() && !problem) {
				processLine(lineCounter, expected, actual);
				lineCounter++;
			}
		} finally {
			expected.close();
			actual.close();
		}

	}

	public static void assertEquals(InputStream expected, File actual)
			throws Exception {
		Assert.assertNotNull(expected);
		Assert.assertNotNull(actual);
		Assert.assertTrue(actual.canRead());

		BufferedReader expectedData = new BufferedReader(new InputStreamReader(expected));
		BufferedReader actualData = new BufferedReader(new InputStreamReader(new FileInputStream(actual)));
		assertEquals(expectedData, actualData);
	}

	public static void assertEquals(String expectedFilePath, String actualFilePath) throws Exception {
		assertEquals(new File(expectedFilePath), new File(actualFilePath));
	}

	public static void assertEquals(File expected, File actual)
			throws Exception {
		Assert.assertNotNull(expected);
		Assert.assertNotNull(actual);

		Assert.assertTrue(expected.canRead());
		Assert.assertTrue(actual.canRead());

		BufferedReader expectedData = new BufferedReader(new InputStreamReader(new FileInputStream(expected)));
		BufferedReader actualData = new BufferedReader(new InputStreamReader(new FileInputStream(actual)));
		try {
			assertEquals(expectedData, actualData);
		} catch (AssertionFailedError e) {
			StringBuilder sb = new StringBuilder(e.getMessage());
			sb.append("Expect file: ").append(expected.getPath()).append(Default.LINE_DELIMITER)
			  .append("Actual file: ").append(actual.getPath()).append(Default.LINE_DELIMITER);
			throw new AssertionFailedError(sb.toString());
		}
	}
}
