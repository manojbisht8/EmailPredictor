import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @author Manoj
 *
 */
public class EnronDataProcessor {

	public static final String NEW_LINE_SEPERATOR = "\n";
	// Output CSV file header
	public static final Object[] OUTPUT_FILE_HEADER = { "From", "To", "GroupNumber" };

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Error: Please enter csv file path.");
			System.exit(1);
		}
		System.out.println("Dataset File path: " + args[0]);
		String inputFilePath = args[0];
		String outputFilePath = args[1];

		Reader in;
		try {

			CSVParser parser = new CSVParser(new FileReader(inputFilePath), CSVFormat.DEFAULT.withHeader());
			List<EnronRecord> enronRecordList = createEmailGroups(parser);
			for (EnronRecord enronRecord : enronRecordList) {
				System.out.println(
						enronRecord.getFrom() + "|" + enronRecord.getTo() + "|" + enronRecord.getGroupNumber());
			}
			generateOutputFile(enronRecordList, outputFilePath);

			parser.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param parser
	 * @return
	 */
	public static List<EnronRecord> createEmailGroups(CSVParser parser) {

		System.out.println("createEmailGroups starts...");
		List<CSVRecord> csvRecordList;
		List<EnronRecord> enronRecordList = new ArrayList<EnronRecord>();
		try {
			csvRecordList = parser.getRecords();

			Map<Integer, ArrayList<String>> emailGroupMap = new HashMap<Integer, ArrayList<String>>();

			// for (CSVRecord record : parser) {
			for (CSVRecord record : csvRecordList) {

				EnronRecord enronRecord = new EnronRecord();

				String from = record.get("From");
				System.out.println("from: " + from);
				enronRecord.setFrom(from);
				String toEmailStr = record.get("To");
				System.out.println("To: " + toEmailStr);
				enronRecord.setTo(toEmailStr);
				ArrayList<String> toEmailList = new ArrayList<String>(Arrays.asList(toEmailStr.split("\\s*,\\s*")));
				System.out.println("toEmailList: " + toEmailList);
				boolean groupIdentified = false;
				if (!toEmailList.isEmpty()) {
					if (emailGroupMap.isEmpty()) {
						// create first group
						emailGroupMap.put(1, toEmailList);
						enronRecord.setGroupNumber(1);
					} else {
						// Check if To email_ids fit in any of the existing
						// Groups
						Iterator it = emailGroupMap.entrySet().iterator();
						int matchCounter = 0;
						while (it.hasNext()) {
							Map.Entry pair = (Map.Entry) it.next();
							ArrayList<String> existingEmailList = (ArrayList<String>) pair.getValue();
							System.out.println("existingEmailList: " + existingEmailList);
							for (String email : existingEmailList) {
								for (String newEmail : toEmailList) {
									if (email.equalsIgnoreCase(newEmail)) {
										System.out.println("matching emailId found");
										matchCounter++;
									}
								}

							}
							if (matchCounter >= 2) {
								System.out.println("Mark this as the group number as per Majority.");
								groupIdentified = true;
								enronRecord.setGroupNumber((int) pair.getKey());
							}

						}
						if (!groupIdentified) {
							// This is new group
							int newGroupNumber = emailGroupMap.size() + 1;
							emailGroupMap.put(newGroupNumber, toEmailList);
							enronRecord.setGroupNumber(newGroupNumber);
						}

					}
				} else {
					System.out.println("To list is empty. Assign a default Group number.");
				}

				// add record to list
				enronRecordList.add(enronRecord);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("createEmailGroups ends...");
		return enronRecordList;
	}

	/**
	 * @param enronRecordList
	 * @param outputFile
	 */
	public static void generateOutputFile(List<EnronRecord> enronRecordList, String outputFile) {
		System.out.println("generateOutputFile start..");
		if (enronRecordList == null) {
			System.out.println("No list to generate...");
			return;
		}
		FileWriter fileWriter = null;

		CSVPrinter csvFilePrinter = null;

		// Create the CSVFormat object with "\n" as a record delimiter

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPERATOR);

		try {

			fileWriter = new FileWriter(outputFile);

			// initialize CSVPrinter object

			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			// Create CSV file header
			csvFilePrinter.printRecord(OUTPUT_FILE_HEADER);

			// Write a new student object list to the CSV file
			for (EnronRecord enronRecord : enronRecordList) {
				List<String> enronDataRecord = new ArrayList<String>();
				enronDataRecord.add(enronRecord.getFrom());
				enronDataRecord.add(enronRecord.getTo());
				enronDataRecord.add(Integer.toString(enronRecord.getGroupNumber()));
				csvFilePrinter.printRecord(enronDataRecord);

			}

			System.out.println("Report CSV file was created successfully !!!");
			System.out.println("Location of report file: " + outputFile);

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();

		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
				e.printStackTrace();

			}

		}
		System.out.println("generateOutputFile ends..");
	}

}
