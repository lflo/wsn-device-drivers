package de.uniluebeck.itm.messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

/**
 * The Class Main.
 */
public class Main {
	
	/** The version. */
	private static double version = 0.1;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		// create Options object
		Option help_option = new Option( "help", "print this message" );
		Option version_option = new Option( "version", "print the version information" );
		
		Options options = new Options();
		
		options.addOption(help_option);
		options.addOption(version_option);
		
		// add options for Messenger
		options.addOption("port", true, "port");
		options.addOption("server", true, "server");
		options.addOption("message", true, "Die Nachricht, die verschickt werden soll in Hex-Code");
		options.addOption("user", true, "Benutzername, um sich auf einen Server zu verbinden");
		options.addOption("passwd", true, "Passwort, um sich auf einen Server zu verbinden");
		options.addOption("device", true, "Art des Geraets im lokalen Fall: jennec, telosb oder pacemate");
		options.addOption("id", true, "ID des Geraets im Remote-Fall");
		
		// for help statement
		HelpFormatter formatter = new HelpFormatter();

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Diese Option gibt es nicht.");
		}
		if(cmd != null){
			//standard-options
			if(cmd.hasOption("help")){
				System.out.println("Aufrufbeispiele:");
				System.out.println("Messenger: send -message 0a 3f 41 -port 141.83.1.546:1282");
				System.out.println("");
				formatter.printHelp("help", options);
			}
			if(cmd.hasOption("version")){
				System.out.println(version);
			}
			
			//der Messenger
			if(args[0].equals("send")) {
				System.out.println("starte Messenger...");
				
				String port = cmd.getOptionValue("port");
				String server = cmd.getOptionValue("server");
				String message = cmd.getOptionValue("message");
				String user = cmd.getOptionValue("user");
				String password = cmd.getOptionValue("passwd");
				String device = cmd.getOptionValue("device");
				String id = cmd.getOptionValue("id");
				
				if(server != null && (user == null || password == null)){
					System.out.println("Bitte geben Sie Benutzername und Passwort ein, um sich zu dem Server zu verbinden.");
				}
				else{				
					Messenger messenger = new Messenger();
					messenger.setPort(port);
					messenger.setServer(server);
					messenger.setUser(user);
					messenger.setPassword(password);
					messenger.setDevice(device);
					messenger.setId(id);
					messenger.connect();
					messenger.send(message);	
				}						
			}
		}
	}
}
