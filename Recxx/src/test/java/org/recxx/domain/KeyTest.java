package org.recxx.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class KeyTest {

	@Test
	public void testCompactCharSequenceString() {
		char c = '\u0013';
		String string = String.valueOf(c);
		Key compactCharSequence = new Key(string);
		assertEquals(string, compactCharSequence.toString());
	}

	@Test
	public void testCompactCharSequenceListOfString() {
		List<String> list = Arrays.asList("123","456");
		Key compactCharSequence = new Key(list);
		assertEquals("123" + Key.DELIMITER + "456", compactCharSequence.toString());
	}

	@Test
	public void testCharAt() {
		Key compactCharSequence = new Key("123");
		assertEquals('3', compactCharSequence.charAt(2));
	}

	@Test
	public void testLength() {
		List<String> list = Arrays.asList("123");
		Key compactCharSequence = new Key(list);
		assertEquals(3, compactCharSequence.length());
	}

	@Test
	public void testSubSequence() {
		List<String> list = Arrays.asList("123");
		Key compactCharSequence = new Key(list);
		assertEquals("12",compactCharSequence.subSequence(0, 2).toString());
	}

}
