package org.recxx.utils;

import junit.framework.Assert;

import org.recxx.Recxx;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Summary;

public class ReconciliationAssert {

	public static void assertReconciles(RecxxConfiguration configuration) throws Exception {
		Assert.assertNotNull(configuration);
		Recxx recxx = new Recxx();
		recxx.setConfiguration(configuration);
		assertReconciles(recxx);
	}

	public static void failsToReconcile(RecxxConfiguration configuration) throws Exception {
		Assert.assertNotNull(configuration);
		Recxx recxx = new Recxx();
		recxx.setConfiguration(configuration);
		failsToReconcile(recxx);
	}

	public static void assertReconciles(Recxx recxx) throws Exception {
		Assert.assertNotNull(recxx);
		Summary summary = recxx.execute().get(0).getSummary();
		Assert.assertTrue(summary.toOutputString(), summary.getAlias1Count() == summary.getMatchCount() && summary.getAlias2Count() == summary.getMatchCount() );
	}

	public static void failsToReconcile(Recxx recxx) throws Exception {
		Assert.assertNotNull(recxx);
		Summary summary = recxx.execute().get(0).getSummary();
		Assert.assertFalse(summary.toOutputString(), summary.getAlias1Count() == summary.getMatchCount() && summary.getAlias2Count() == summary.getMatchCount() );
	}

}
