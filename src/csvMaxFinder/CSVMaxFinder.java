package csvMaxFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class CSVMaxFinder {
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Scanner inputScanner = new Scanner(System.in);
		FileInputStream csvFileStream = new FileInputStream(new File("D:\\input.csv"));
		CSVStream csvReader = new CSVStream(csvFileStream, "UTF-8");
		
		System.out.println("Enter column name");
		String veryImportantColumnName = inputScanner.next();
		
//		System.out.println("Enter file path");
	//	csvFilePath = inputScanner.next();
		
		inputScanner.close();
		
		try {
			double maxValue = csvReader.getMaxVeryImportantValue(veryImportantColumnName);
			System.out.println(maxValue);
		}
		catch(CellIsEmptyException | RowIsEmptyException | NoDocumentBodyException | NoTitleFoundException | EnteredTitleIsEmptyException e) {
			System.out.println(e.getMessage());
			
		}
		csvReader.close();

	}

}
