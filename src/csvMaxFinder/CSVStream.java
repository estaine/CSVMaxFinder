package csvMaxFinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class CSVStream extends InputStreamReader {

	private static final int	CSV_DELIMITER = ',';
	private static final int	CARRIAGE_RETURN = 13; 
	private static final int	LINE_FEED = 10;
	private static final int	DOCUMENT_END = -1;
	private static final int	BYTE_ORDER_MARK = 0xFEFF;
	
	//private int 				veryImportantColumnNumber;
	//private String				veryImportantColumnName;
	private String				csvFilePath;
	private int 				currentSymbol;
	

	private static boolean isDelimiter(int symbol) {
		return ((symbol == CSV_DELIMITER) || (symbol == CARRIAGE_RETURN) || (symbol == LINE_FEED) || (symbol == DOCUMENT_END));
	}
	
	private static boolean isCellDelimiter(int symbol) {
		return symbol == CSV_DELIMITER;
	}
	
	private static boolean isRowDelimiter(int symbol) {
		return isDelimiter(symbol) && !isCellDelimiter(symbol);
	}
	
	private void processByteOrderMark() throws IOException {
		
		if(read() != BYTE_ORDER_MARK)
			reset();
	}
	
	private boolean processRowEnd() throws RowIsEmptyException, IOException {
		
		int nextSymbol = read();
		switch(currentSymbol){
		case DOCUMENT_END: return false;
		case LINE_FEED: currentSymbol = nextSymbol; break;
		case CARRIAGE_RETURN:
			if((!isDelimiter(nextSymbol)) || (nextSymbol == LINE_FEED))
				currentSymbol = nextSymbol;					
			else
				throw new RowIsEmptyException("Row is empty\n");				
		}
		currentSymbol = nextSymbol;
		return true;
	}

	private boolean processNextSymbol() throws IOException {
		currentSymbol = read();
		return !isDelimiter(currentSymbol);
	}
	
	private String getNextCellValue() throws IOException, CellIsEmptyException {
		
		StringBuilder currentCellValue = (isDelimiter(currentSymbol)) ? new StringBuilder() : new StringBuilder(Character.toString((char)currentSymbol));
			
		while(processNextSymbol()) {
			currentCellValue.append((char)currentSymbol);
		}
		if(currentCellValue.length() == 0)
			throw new CellIsEmptyException("Cell is empty\n");
		return currentCellValue.toString();
	}
	
	private int getVeryImportantColumnNumber(String veryImportantColumnName)
			throws IOException, NoDocumentBodyException, RowIsEmptyException, NoTitleFoundException, CellIsEmptyException {
		
		processByteOrderMark();
		
		int currentColumnNumber = -1, veryImportantColumnNumber = -1;
		String currentCellValue = "";
		
		do {
			currentCellValue = getNextCellValue();
			currentColumnNumber++;
			if(veryImportantColumnName.equals(currentCellValue)) {
				veryImportantColumnNumber = currentColumnNumber;
				break;
			}
				
		} while(!isRowDelimiter(currentSymbol));
		
		if("".equals(currentCellValue))
			throw new NoTitleFoundException("No column with such name found\n");
		
		if(!moveToNextLine())
			throw new NoDocumentBodyException("No data rows found\n");	
		return veryImportantColumnNumber;

	}
	
	private double getVeryImportantCellValue(int veryImportantColumnNumber) throws IOException, CellIsEmptyException {
		//currentSymbol = (!isDelimiter(currentSymbol) && (veryImportantColumnNumber == 0)) ? currentSymbol : read();
		int currentColumnNumber = -1;
		String currentCellValue;
		
		do {
			currentCellValue = getNextCellValue();
			currentColumnNumber++;
		}
		while(currentColumnNumber < veryImportantColumnNumber);
		
		return Double.parseDouble(currentCellValue);
					
	}
	
	private boolean moveToNextLine() throws IOException, RowIsEmptyException {
		do
			processNextSymbol();			
		while(!isRowDelimiter(currentSymbol));
		return processRowEnd();
	}
	
	public double getMaxVeryImportantValue(String veryImportantColumnName)
			throws	IOException, NoDocumentBodyException, RowIsEmptyException,
					NoTitleFoundException, CellIsEmptyException, EnteredTitleIsEmptyException {
		
		if (veryImportantColumnName == null)
			throw new EnteredTitleIsEmptyException("Entered column title is blank\n");
		
		int veryImportantColumnNumber = getVeryImportantColumnNumber(veryImportantColumnName);
		
		double maxVeryImportantValue = Double.MIN_VALUE, currentVeryImportantValue;
		
		do {
			currentVeryImportantValue = getVeryImportantCellValue(veryImportantColumnNumber);
			if(currentVeryImportantValue > maxVeryImportantValue)
				maxVeryImportantValue = currentVeryImportantValue;
		} while(moveToNextLine());
		return maxVeryImportantValue;
	}
	
	
	public CSVStream(InputStream inputStream, String charset) throws UnsupportedEncodingException {
		super(inputStream, charset);
	}

	
	
}
