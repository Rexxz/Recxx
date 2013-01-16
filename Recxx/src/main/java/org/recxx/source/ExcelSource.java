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
import org.recxx.domain.Column;
import org.recxx.domain.Default;
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
			LOGGER.info(" source " + fileMetaData.getFilePath());
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
				int coordinateCount = 0;
				
				for(Row row : sheet) {
				
			    	for (ExcelType type : ExcelType.values()) {
			    		
			    		if (type.equals(ExcelType.FORMAT) && formatComparison ||
			    				!type.equals(ExcelType.FORMAT) ) {
			    			
			    			Key key = new Key(Arrays.asList(sheet.getSheetName(), Integer.toString(row.getRowNum() + 1), type.name()));

			    			if (rowContainsData(row)) {
			    				ExcelCoordinates coordinates = new ExcelCoordinates(sheet.getSheetName(), row.getRowNum(), type);
			    				coordinatesMap.put(key, coordinates);
			    				coordinateCount++;
			    			}
			    		}
					}
				}
				LOGGER.info(alias + ": Mapped " + sheet.getSheetName() + " : " + coordinateCount);
				
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

	private boolean rowContainsData(Row row) {
		boolean containsData = false;

		for (Cell cell : row) {
			if (cell != null) {
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					if (cell.getRichStringCellValue().getString() != null && !cell.getRichStringCellValue().getString().isEmpty()) {
						containsData = true;
					}
					break;
				case Cell.CELL_TYPE_NUMERIC:
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					break;
				case Cell.CELL_TYPE_FORMULA:
					break;
				default:
				}
				if (containsData) continue;	
			}
		}
		return containsData;
	}

	public Set<Key> getKeySet() {
		return coordinatesMap.keySet();
	}

	public List<?> getRow(Key key) {
		ExcelCoordinates coordinates = coordinatesMap.get(key);
		Row row = workbook.getSheet(coordinates.getSheetName()).getRow(coordinates.getRow());
		ArrayList<Object> returnRow = new ArrayList<Object>();
		
 	   for(int cn = 0; cn < row.getLastCellNum(); cn++) {

 		   	Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
    	   
			ExcelType type = coordinates.getType();
			if (type.equals(ExcelType.CELL)) {
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
 	    return trimRow(returnRow);
	}

	private List<?> trimRow(ArrayList<Object> returnRow) {
		returnRow.trimToSize();
 	    for (int i = returnRow.size() - 1; i >= 0; i--) {
 	    	if (returnRow.get(i) == null) {
 	    		returnRow.remove(i);
 	    	}
 	    	else {
 	    		break;
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
		return Arrays.asList(Default.ALL_COLUMNS); //?
	}

	public List<String> getIgnoreColumns() {
		return Arrays.asList(""); //?
	}

	public int getColumnIndex(String columnName) {
		return CellReference.convertColStringToIndex(columnName);
	}

	public void close() {
		
	}


}
