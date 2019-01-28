package tabvalidator;

import java.io.File;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ValidateEntityFromFile {

	public static void main(String[] args) throws Exception {

		Options options = new Options();

		// Option set
		Option filename = new Option("n","name",true,"filename");
		filename.setRequired(true);
		options.addOption(filename);

		Option filetype = new Option("t","type",true,"spreadsheet file type");
		filetype.setRequired(true);
		options.addOption(filetype);

		Option pack = new Option("g","generatejar",true,"jar file with JPA classes used for validation / template creation");
		pack.setRequired(true);
		options.addOption(pack);

		// Parse the arguments
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			formatter.printHelp("argument names", options);
			System.exit(1);
			return;
		}

		// Get arguments' options 
		String inputFileName = cmd.getOptionValue("name");	
		String inputFileType = cmd.getOptionValue("type");	
		String generatejar = cmd.getOptionValue("generatejar");
		ReadWorkBook f = new ReadWorkBook(inputFileName,inputFileType,generatejar);
		File file = new File(inputFileName);
		
/*		if ( (generatejar.isEmpty()) && !generatejar.exists() ) {
			System.err.println("jar not found "+generatejar.getAbsolutePath());
			System.exit(1);
		}*/

		// Read the given input file or print an error
		//FIXME : rajouter un vérificateur pour le jar ? car il repère l'erreur mais ne fait rien
		if ( !(inputFileName==null) && file.exists() ) {
			f.parseFile();
		}
		else {
			System.err.println("file not found "+file.getAbsolutePath());
			System.exit(1);
		}

	}
}