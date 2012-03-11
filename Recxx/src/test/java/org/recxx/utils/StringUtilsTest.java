package org.recxx.utils;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVParser;

public class StringUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void Split() throws Exception {
		String toughie = new String(",2,3,4,\"\",\"hi, im here\", ,someother text,'sausage',,,");
		String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(toughie, ",");
		StrTokenizer tokenizer = new StrTokenizer(toughie, StrMatcher.commaMatcher(), StrMatcher.doubleQuoteMatcher());
		List<String> list = tokenizer.getTokenList();
		System.out.println(list);
		System.out.println(split);
		FileUtils.deleteQuietly(new  File("nothing"));

		CSVParser c = new CSVParser(CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, false);
		String[] parseLine = c.parseLine(toughie);
		System.out.println(parseLine);
	}
	
}
