package org.recxx.utils;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.recxx.Recxx;
import org.recxx.destination.Destination;
import org.recxx.domain.Summary;

@RunWith(MockitoJUnitRunner.class)
public class ReconciliationAssertTest {

	@Mock private Recxx recxx;
	@Mock private List<Destination> list;
	@Mock private Destination destination;
	@Mock private Summary reconciledSummary;
	@Mock private Summary unreconciledAlias1Summary;
	@Mock private Summary unreconciledAlias2Summary;
	@Mock private Summary unreconciledAliasBothSummary;

	@Before
	public void setup() {
		when(reconciledSummary.getMatchCount()).thenReturn(10);
		when(reconciledSummary.getAlias1Count()).thenReturn(10);
		when(reconciledSummary.getAlias2Count()).thenReturn(10);
		when(unreconciledAlias1Summary.getMatchCount()).thenReturn(10);
		when(unreconciledAlias1Summary.getAlias1Count()).thenReturn(9);
		when(unreconciledAlias1Summary.getAlias2Count()).thenReturn(10);
		when(unreconciledAlias1Summary.toOutputString()).thenReturn("Alias 1: 9, Alias 2: 10");
		when(unreconciledAlias2Summary.getAlias1Count()).thenReturn(10);
		when(unreconciledAlias2Summary.getAlias2Count()).thenReturn(9);
		when(unreconciledAlias2Summary.getMatchCount()).thenReturn(10);
		when(unreconciledAlias2Summary.toOutputString()).thenReturn("Alias 1: 10, Alias 2: 9");
		when(unreconciledAliasBothSummary.getAlias1Count()).thenReturn(9);
		when(unreconciledAliasBothSummary.getAlias2Count()).thenReturn(9);
		when(unreconciledAliasBothSummary.getMatchCount()).thenReturn(8);
		when(unreconciledAliasBothSummary.toOutputString()).thenReturn("Alias 1: 9, Alias 2: 9");
	}

	@Test
	public void testReconcilesWhenAliasCountsMatch() throws Exception {

		when(recxx.execute()).thenReturn(list);
		when(list.get(0)).thenReturn((Destination) destination);
		when(destination.getSummary()).thenReturn(reconciledSummary);
		ReconciliationAssert.assertReconciles(recxx);

	}

	@Test
	public void testFailsToReconcileWhenAlias1CountDoesntMatch() throws Exception {

		when(recxx.execute()).thenReturn(list);
		when(list.get(0)).thenReturn((Destination) destination);
		when(destination.getSummary()).thenReturn(unreconciledAlias1Summary);
		ReconciliationAssert.failsToReconcile(recxx);

	}

	@Test
	public void testFailsToReconcileWhenAlias2CountDoesntMatch() throws Exception {

		when(recxx.execute()).thenReturn(list);
		when(list.get(0)).thenReturn((Destination) destination);
		when(destination.getSummary()).thenReturn(unreconciledAlias2Summary);
		ReconciliationAssert.failsToReconcile(recxx);

	}

	@Test
	public void testFailsToReconcileWhenBothAliasesCountDoesntMatch() throws Exception {

		when(recxx.execute()).thenReturn(list);
		when(list.get(0)).thenReturn((Destination) destination);
		when(destination.getSummary()).thenReturn(unreconciledAliasBothSummary);
		ReconciliationAssert.failsToReconcile(recxx);

	}
}
