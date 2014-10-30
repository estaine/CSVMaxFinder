package csvMaxFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CSVMaxFinder {
	
	private static final int	CSV_DELIMITER = ',';
	private static final int	CARRIAGE_RETURN = 13; 
	private static final int	LINE_FEED = 10;
	private static final int	DOCUMENT_END = -1;
	private static final int	BYTE_ORDER_MARK = 0xFEFF;
	
	private static int 			veryImportantColumnNumber = -1;
	private static String		veryImportantColumnName;
	private static String		csvFilePath;
	private static int 			lastSymbol;
	
	private static boolean isDelimiter(int symbol) {
		return ((symbol == CSV_DELIMITER) || (symbol == CARRIAGE_RETURN) || (symbol == LINE_FEED) || (symbol == DOCUMENT_END));
	}
	
	private static boolean isCellDelimiter(int symbol) {
		return symbol == CSV_DELIMITER;
	}
	
	private static boolean processLineEnd(int currentSymbol) throws RowIsEmptyException {
		switch(lastSymbol){
		case DOCUMENT_END: return false;
		case LINE_FEED: lastSymbol = currentSymbol; break;
		case CARRIAGE_RETURN:
			if((!isDelimiter(currentSymbol)) || (currentSymbol == LINE_FEED))
				lastSymbol = currentSymbol;					
			else
				throw new RowIsEmptyException("Row is empty\n");				
		}
		return true;
	}
	

	
	private static void getVeryImportantColumnNumber(InputStreamReader reader)
			throws IOException, NoDocumentBodyException, RowIsEmptyException, NoTitleFoundException {
		
		
		StringBuilder currentTitle = new StringBuilder();
		int currentColumnNumber = -1;
		int currentSymbol;
		
		if(reader.read() != BYTE_ORDER_MARK)
			reader.reset();

		do {
			currentSymbol = reader.read();
			if(isDelimiter(currentSymbol))
			{
				currentColumnNumber++;
				if(currentTitle.toString().equals(veryImportantColumnName))
					veryImportantColumnNumber = currentColumnNumber;
				else
					currentTitle.setLength(0);
			}
			else
				currentTitle.append((char)currentSymbol);			
		} while((!isDelimiter(currentSymbol)) || (isCellDelimiter(currentSymbol)));
		
		if(veryImportantColumnNumber == -1)
			throw new NoTitleFoundException("No column with such name found\n");
		lastSymbol = currentSymbol;
		currentSymbol = reader.read();
		if(!processLineEnd(currentSymbol))
			throw new NoDocumentBodyException("No data rows found\n");	
	}
	
	private static void moveToVeryImportantCell(InputStreamReader reader) throws IOException {
		int delimitersFound = 0;
		int currentSymbol;
		try {
			while(delimitersFound < veryImportantColumnNumber)
			{
				currentSymbol = reader.read();
				if(isDelimiter(currentSymbol)) {
						if(isCellDelimiter(currentSymbol))
							delimitersFound++;
						else
							throw new CellDoesNotExistException("Cell does not exist\n");
				}
			}
		}
		catch(CellDoesNotExistException e) {
			System.out.print(e.getMessage());
		}
	}
	
	private static double getVeryImportantCell(InputStreamReader reader) throws IOException, CellIsEmptyException, RowIsEmptyException {
		StringBuilder currentCell = new StringBuilder();
		int currentSymbol = (!isDelimiter(lastSymbol) && (veryImportantColumnNumber == 0)) ? lastSymbol : reader.read();
		
		while(!isDelimiter(currentSymbol))
		{
			currentCell.append((char)currentSymbol);
			currentSymbol = reader.read();			
		}
		
		if(currentCell.length() == 0)
			throw new CellIsEmptyException("Cell is empty\n");			
				
		lastSymbol = currentSymbol;
		return Double.parseDouble(currentCell.toString());		
	}
	
	private static boolean moveToNextLine(InputStreamReader reader) throws IOException, RowIsEmptyException {
		int currentSymbol = reader.read();
	
		while((!isDelimiter(currentSymbol)) || (isCellDelimiter(currentSymbol)))
			currentSymbol = reader.read();
		
		lastSymbol = currentSymbol;
		currentSymbol = reader.read();
		
		
		return processLineEnd(currentSymbol);
	}


	private static double getMaxVeryImportantValue(InputStreamReader reader)
			throws IOException, CellDoesNotExistException, CellIsEmptyException,
			RowIsEmptyException, NoDocumentBodyException, NoTitleFoundException {
		
		double maxVeryImportantValue = Double.MIN_VALUE;
		double currentVeryImportantValue;
		
		getVeryImportantColumnNumber(reader);
		do {
			moveToVeryImportantCell(reader);
			currentVeryImportantValue = getVeryImportantCell(reader);
			if(currentVeryImportantValue > maxVeryImportantValue)
				maxVeryImportantValue = currentVeryImportantValue;			
		} while(moveToNextLine(reader));			
		
		return maxVeryImportantValue;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Scanner inputScanner = new Scanner(System.in);
		
		System.out.println("Enter column name");
		veryImportantColumnName = inputScanner.next();
		
		System.out.println("Enter file path");
		csvFilePath = inputScanner.next();
		
		inputScanner.close();
		
		FileInputStream csvFileStream = new FileInputStream(new File(csvFilePath));
		InputStreamReader csvReader = new InputStreamReader(csvFileStream, "UTF-8");
		
		try {
			double maxValue = getMaxVeryImportantValue(csvReader);
			System.out.println(maxValue);
		}
		catch(CellDoesNotExistException | CellIsEmptyException | RowIsEmptyException | NoDocumentBodyException | NoTitleFoundException e) {
			System.out.println(e.getMessage());
		}

	}

}
