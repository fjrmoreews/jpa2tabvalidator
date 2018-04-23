package tabtemplatecreator;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

 
public class CreateTemplateFromJPA {

	

	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		
		// Option set
		Option templateDirOpt = new Option("o","templatedir",true,"ouput spreadsheet  template directory ");
		templateDirOpt.setRequired(true);
		options.addOption(templateDirOpt);
		
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
		String templateDir = cmd.getOptionValue("templatedir");	
		String inputFileType = cmd.getOptionValue("type");	
		String generatejar = cmd.getOptionValue("generatejar");
		WorkBookCreator workBookCreator = new WorkBookCreator(templateDir,inputFileType,generatejar);
		File templateDirectory = new File(templateDir);
		
		
		// Read the given input file or create it if does not exist
		if ( !(templateDir==null) && templateDirectory.exists() ) {
 
			workBookCreator.createSpreadSheetFromClasses();
		}
		else {
			System.err.println("file not found "+templateDirectory.getAbsolutePath());
			}
	
		}
	 


}
