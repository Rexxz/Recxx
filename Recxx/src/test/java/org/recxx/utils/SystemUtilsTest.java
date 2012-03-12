package org.recxx.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class SystemUtilsTest {

	private static final String _01_JAN_2012 = "01-Jan-2012";
	private static final String FILE_PATH = "/tmp/";
	private static final String STRING_WITH_ESCAPED_BUSINESS_DATE = "date=${BUSINESS_DATE}";
	private static final String STRING_WITH_ESCAPED_PATH_AND_BUSINESS_DATE = "${FILE_PATH}${BUSINESS_DATE}file";
	
	@Before
	public void setUp() throws Exception {
		System.setProperty("BUSINESS_DATE", _01_JAN_2012);
		System.setProperty("FILE_PATH", FILE_PATH);
	}

	@Test
	public void testEscapedBusinessDate() {
		assertEquals("date=" + _01_JAN_2012, SystemUtils.replaceSystemProperties(STRING_WITH_ESCAPED_BUSINESS_DATE));
	}

	@Test
	public void testEscapedFilePathAndBusinessDate() {
		assertEquals(FILE_PATH + _01_JAN_2012 + "file", SystemUtils.replaceSystemProperties(STRING_WITH_ESCAPED_PATH_AND_BUSINESS_DATE));
	}
	
	@Test
	public void testMemoryUsed() {
		assertNotNull(SystemUtils.memoryUsed());
	}
	
	

}
