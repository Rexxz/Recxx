package org.recxx.source;

import java.util.Arrays;
import java.util.List;

import org.recxx.domain.ExcelFileMetaData;
import org.recxx.domain.Key;

public class ExcelSourceTest {

//	@Test
	public void testCall() throws Exception {
		ExcelFileMetaData fileMetaData = new ExcelFileMetaData.Builder()
			.filePath("H:\\My Documents\\RDSReports\\StrucRisk_FO_20120202_processed_v01.xls")
			.omitSheets(Arrays.asList("Deal-Cpty"))
			.formatComparison(true)
			.build()
			
						;
		ExcelSource source = new ExcelSource("test", fileMetaData);
		source.call();
		System.out.println(source.getKeySet().size());
		List<?> row = source.getRow(new Key(Arrays.asList("Deal", "7", "VALUE")));
		System.err.println(row);
	}



}
