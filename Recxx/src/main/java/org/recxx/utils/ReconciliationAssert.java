package org.recxx.utils;

import junit.framework.Assert;

import org.recxx.Recxx;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Summary;

public class ReconciliationAssert {

	public static void assertReconciles(RecxxConfiguration configuration) throws Exception {
		Assert.assertNotNull(configuration);
		Recxx recxx = new Recxx();
		Summary summary = recxx.execute(configuration).get(0).getSummary();
		Assert.assertEquals(summary.toOutputString(), summary.getAlias1Count(), summary.getMatchCount());
		Assert.assertEquals(summary.toOutputString(), summary.getAlias2Count(), summary.getMatchCount());
	}

	public static void failsToReconcile(RecxxConfiguration configuration) throws Exception {
		Assert.assertNotNull(configuration);
		Recxx recxx = new Recxx();
		Summary summary = recxx.execute(configuration).get(0).getSummary();
		Assert.assertNotSame(summary.toOutputString(), summary.getAlias1Count(), summary.getMatchCount());
		Assert.assertNotSame(summary.toOutputString(), summary.getAlias2Count(), summary.getMatchCount());
	}
}
