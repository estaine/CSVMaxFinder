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
	
	private static int 			veryImportantColumnNumber;
	private static String		csvFilePath;
	private static int 			lastSymbol;
	
	private static boolean isDelimiter(int symbol) {
		return ((symbol == CSV_DELIMITER) || (symbol == CARRIAGE_RETURN) || (symbol == LINE_FEED) || (symbol == DOCUMENT_END));
	}
	
	private static boolean isCellDelimiter(int symbol) {
		return symbol == CSV_DELIMITER;
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
							throw new CellDoesNotExistException("Cell does not exist");
				}
			}
		}
		catch(CellDoesNotExistException e) {
			System.out.print(e.getMessage());
		}
	}
	
	private static double getVeryImportantCell(InputStreamReader reader) throws IOException, CellIsEmptyException, RowIsEmptyException {
		StringBuilder currentCell = new StringBuilder();
		int currentSymbol = reader.read();
		
		while(!isDelimiter(currentSymbol))
		{
			currentCell.append((char)currentSymbol);
			currentSymbol = reader.read();			
		}
		
		if(currentCell.length() == 0)
			throw new CellIsEmptyException("Cell is empty");			
				
		lastSymbol = currentSymbol;
		return Double.parseDouble(currentCell.toString());		
	}
	
	private static boolean moveToNextLine(InputStreamReader reader) throws IOException, RowIsEmptyException {
		int currentSymbol = reader.read();
	
		while((!isDelimiter(lastSymbol)) || (isCellDelimiter(lastSymbol))) {
			lastSymbol = currentSymbol;
			currentSymbol = reader.read();
		}
		
		switch(lastSymbol){
			case DOCUMENT_END: return false;
			case LINE_FEED: lastSymbol = currentSymbol; break;
			case CARRIAGE_RETURN:
				if((!isDelimiter(currentSymbol)) || (currentSymbol == LINE_FEED))
					lastSymbol = currentSymbol;					
				else
					throw new RowIsEmptyException("Row is empty");				
		}		
		return true;
	}


	private static double getMaxVeryImportantValue(InputStreamReader reader) throws IOException, CellDoesNotExistException, CellIsEmptyException, RowIsEmptyException {
		double maxVeryImportantValue = Double.MIN_VALUE;
		double currentVeryImportantValue;
		boolean endOfFile = false;
		while(!endOfFile)
		{
			moveToVeryImportantCell(reader);
			
			currentVeryImportantValue = getVeryImportantCell(reader);
			if(currentVeryImportantValue > maxVeryImportantValue)
				maxVeryImportantValue = currentVeryImportantValue;
			
			endOfFile = !moveToNextLine(reader);
		}			
		
		return maxVeryImportantValue;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Scanner inputScanner = new Scanner(System.in);
		
		System.out.println("Enter column number (0 for the first)");
		veryImportantColumnNumber = inputScanner.nextInt();
		
		System.out.println("Enter file path");
		csvFilePath = inputScanner.next();
		
		inputScanner.close();
		
		FileInputStream csvFileStream = new FileInputStream(new File(csvFilePath));
		InputStreamReader csvReader = new InputStreamReader(csvFileStream);
		
		double maxValue = Double.MIN_VALUE;
		try {
			maxValue = getMaxVeryImportantValue(csvReader);
			System.out.println(maxValue);
		}
		catch(CellDoesNotExistException | CellIsEmptyException | RowIsEmptyException e) {
			System.out.println(e.getMessage());
		}
		
		

	}

}
