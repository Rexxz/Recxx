package org.recxx.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SummaryTest {

	@Test
	public void testGetSource1Count() {
		Summary summary = new Summary.Builder()
								.alias1Count(100)
								.build();
		assertEquals(Integer.valueOf(100), summary.getAlias1Count());
	}

	@Test
	public void testGetSourceMatchPercentage() {
		Summary summary = new Summary.Builder()
								.alias1Count(100)
								.alias2Count(100)
								.matchCount(50)
								.build();
		assertEquals("50.00%", summary.getAlias1MatchPercentage());
		assertEquals("50.00%", summary.getAlias2MatchPercentage());
	}
	

}
