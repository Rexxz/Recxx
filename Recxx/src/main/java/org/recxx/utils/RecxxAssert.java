package org.recxx.utils;

import org.recxx.Recxx2;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Summary;

import junit.framework.Assert;

public class RecxxAssert {

	public static void assertReconciles(RecxxConfiguration configuration) throws Exception {
		Assert.assertNotNull(configuration);
		Recxx2 recxx = new Recxx2(configuration);
		Summary summary = recxx.execute();
		Assert.assertEquals("'" + summary.getAlias1() + "' has " + summary.getAlias1Count() + " but only matches " + summary.getMatchCount() + " from '" + summary.getAlias2() + "'",
							summary.getAlias1Count(), 
							summary.getMatchCount());
		Assert.assertEquals("'" + summary.getAlias2() + "' has " + summary.getAlias2Count() + " but only matches " + summary.getMatchCount() + " from '" + summary.getAlias1() + "'",
							summary.getAlias2Count(), 
							summary.getMatchCount());
	}
	
}
