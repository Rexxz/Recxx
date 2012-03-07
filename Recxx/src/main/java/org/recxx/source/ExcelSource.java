package org.recxx.source;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Column;
import org.recxx.domain.ExcelCoordinates;
import org.recxx.domain.ExcelFileMetaData;
import org.recxx.domain.ExcelType;
import org.recxx.domain.Key;

public class ExcelSource implements Source<Key> {
	
	private static Logger LOGGER = Logger.getLogger(ExcelSource.class); 

	private final Map<Key, ExcelCoordinates> coordinatesMap = new HashMap<Key, ExcelCoordinates>();
	private final String alias;
	private final boolean formatComparison;
	private final List<String> omitSheets;
	
	private final Workbook workbook;
	private final List<Column> columns = new ArrayList<Column>();


	public ExcelSource(String alias, ExcelFileMetaData fileMetaData) throws Exception {
		this.alias = alias;
		this.formatComparison = fileMetaData.isFormatComparison();
		this.omitSheets = fileMetaData.getOmitSheets();
		
		try {
			InputStream inputStream = new FileInputStream(fileMetaData.getFilePath());
			workbook = WorkbookFactory.create(inputStream);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Source<Key> call() throws Exception {
		
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			Sheet sheet = workbook.getSheetAt(i);

			if (omitSheets.contains(sheet.getSheetName())) {
				LOGGER.info(alias + ": Omitting " + sheet.getSheetName());
			}
			else {
				
				LOGGER.info(alias + ": Mapping " + sheet.getSheetName());
				
				for(Row row : sheet) {
				
			    	for (ExcelType type : ExcelType.values()) {
			    		
			    		if (type.equals(ExcelType.FORMAT) && formatComparison ||
			    				!type.equals(ExcelType.FORMAT) ) {
			    			
			    			Key key = new Key(Arrays.asList(sheet.getSheetName(), Integer.toString(row.getRowNum() + 1), type.name()));
			    			ExcelCoordinates coordinates = new ExcelCoordinates(sheet.getSheetName(), row.getRowNum(), type);
			    			coordinatesMap.put(key, coordinates);
			    		}
					}
				}
		    }
		}
		LOGGER.info(alias + ": Mapping complete " + coordinatesMap.size() + " keys loaded");

		for (int i = 0; i < 255; i++) {
			String columnName = CellReference.convertNumToColString(i);
			Column column = new Column(columnName, String.class);
			columns.add(column);
		}

		return this;
	}

	public Set<Key> getKeySet() {
		return coordinatesMap.keySet();
	}

	public List<?> getRow(Key key) {
		ExcelCoordinates coordinates = coordinatesMap.get(key);
		Row row = workbook.getSheet(coordinates.getSheetName()).getRow(coordinates.getRow());
		List<Object> returnRow = new ArrayList<Object>();
		
 	   for(int cn = 0; cn < row.getLastCellNum(); cn++) {

 		   Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
    	   
			ExcelType type = coordinates.getType();
			if (type.equals(ExcelType.VALUE)) {
				 switch (cell.getCellType()) {
				    case Cell.CELL_TYPE_STRING:
				    	returnRow.add(cell.getRichStringCellValue().getString());
				        break;
				    case Cell.CELL_TYPE_NUMERIC:
				        if (DateUtil.isCellDateFormatted(cell)) {
				        	returnRow.add(cell.getDateCellValue());
				        } else {
				        	returnRow.add(cell.getNumericCellValue());
				        }
				        break;
				    case Cell.CELL_TYPE_BOOLEAN:
				    	returnRow.add(cell.getBooleanCellValue());
				        break;
				    default:
				    	returnRow.add(null);
				 }
			} 
			else if (type.equals(ExcelType.EXPRESSION)) {
				 switch (cell.getCellType()) {
				    case Cell.CELL_TYPE_FORMULA:
				    	returnRow.add(cell.getCellFormula());
				        break;
				    default:
				    	returnRow.add(null);
				 }
			}
			else {
				// TODO Something with formatting!
			}
		}
		return returnRow;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public String getAlias() {
		return alias;
	}

	public List<String> getKeyColumns() {
		return Arrays.asList("Sheet", "Row", "Type");
	}

	public List<String> getCompareColumns() {
		return Arrays.asList(RecxxConfiguration.ALL_COLUMNS); //?
	}

}
