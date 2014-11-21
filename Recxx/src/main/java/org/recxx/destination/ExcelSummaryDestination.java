package org.recxx.destination;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.recxx.domain.Difference;
import org.recxx.domain.ExcelFileMetaData;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;
import org.recxx.source.Source;

public class ExcelSummaryDestination extends AbstractDestination {

	private static final int SUMMARY_COLUMN_COUNT = 11;

	private static Logger LOGGER = Logger.getLogger(DatabaseDestination.class);

	private final ExcelFileMetaData excelMetaData;
	private Workbook workbook;
	private Sheet sheet;
	private Row currentRow;

	private boolean newWorkbook;

	public ExcelSummaryDestination (ExcelFileMetaData excelMetaData) {
		this.excelMetaData = excelMetaData;
	}

	@Override
	public void open() throws IOException {
		File excelFile = new File(excelMetaData.getFilePath());
		if (!excelFile.exists()) {
			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("Reconciliation Report");
			FileOutputStream outputStream = new FileOutputStream(excelMetaData.getFilePath());
			workbook.write(outputStream);
			newWorkbook = true;
		}
		try {
			InputStream inputStream = new FileInputStream(excelMetaData.getFilePath());
			workbook = WorkbookFactory.create(inputStream);
			sheet = workbook.getSheet("Reconciliation Report");
			currentRow = sheet.getRow(sheet.getLastRowNum());
			LOGGER.info("Destination " + excelMetaData.getFilePath());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void writeHeader(Source<Key> source1, Source<Key> source2) {
		if (newWorkbook) {
			createRow();
			currentRow.getCell(0).setCellValue("configName");
			currentRow.getCell(1).setCellValue("subject");
			currentRow.getCell(2).setCellValue("businessDate");
		    currentRow.getCell(3).setCellValue("reconciliationDate");
			currentRow.getCell(4).setCellValue("alias1");
			currentRow.getCell(5).setCellValue("alias2");
			currentRow.getCell(6).setCellValue("alias1Count");
			currentRow.getCell(7).setCellValue("alias2Count");
			currentRow.getCell(8).setCellValue("matchCount");
			currentRow.getCell(9).setCellValue("alias1MatchPercent");
			currentRow.getCell(10).setCellValue("alias2MatchPercent");
			newWorkbook = false;
    	}
	}

	@Override
	public void writeDifference(Difference difference) {
		// Differences are not persisted by this summary writer
	}

	@Override
	public void writeSummary(Summary summary) {
		createRow();
		currentRow.getCell(0).setCellValue(summary.getConfigName());
		currentRow.getCell(1).setCellValue(summary.getSubject());
		if (summary.getBusinessDate() != null) {
			currentRow.getCell(2).setCellValue(summary.getBusinessDate());
			currentRow.getCell(2).setCellStyle(createDataCellStyle("dd-MMM-yyyy"));
		}
		currentRow.getCell(3).setCellValue(summary.getReconciliationDate());
		currentRow.getCell(3).setCellStyle(createDataCellStyle("dd-MMM-yyyy hh:mm:ss"));
		currentRow.getCell(4).setCellValue(summary.getAlias1());
		currentRow.getCell(5).setCellValue(summary.getAlias2());
		currentRow.getCell(6).setCellValue(summary.getAlias1Count());
		currentRow.getCell(7).setCellValue(summary.getAlias2Count());
		currentRow.getCell(8).setCellValue(summary.getMatchCount());
		currentRow.getCell(9).setCellValue(summary.getAlias1MatchPercent().doubleValue());
		currentRow.getCell(9).setCellStyle(createDataCellStyle("#,##0.0000%"));
		currentRow.getCell(10).setCellValue(summary.getAlias2MatchPercent().doubleValue());
		currentRow.getCell(10).setCellStyle(createDataCellStyle("#,##0.0000%"));
	}

	@Override
	public void close() throws IOException {
		try {
			formatSheet(sheet);
			FileOutputStream outputStream = new FileOutputStream(excelMetaData.getFilePath());
			workbook.write(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void createRow() {
		int rowNum = currentRow != null ? currentRow.getRowNum() + 1 : 0;
		currentRow = sheet.createRow(rowNum);
		for (int i = 0; i < SUMMARY_COLUMN_COUNT; i++) {
			currentRow.createCell(i);
		}
	}

	private CellStyle createDataCellStyle(String format) {
		CreationHelper creationHelper = workbook.getCreationHelper();
		CellStyle cellStyle = workbook.createCellStyle();
	    cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(format));
		return cellStyle;
	}



	private void formatSheet(Sheet sheet) {
		for (int i = 0; i < SUMMARY_COLUMN_COUNT; i++) {
			sheet.autoSizeColumn(i);
		}
	}

}
