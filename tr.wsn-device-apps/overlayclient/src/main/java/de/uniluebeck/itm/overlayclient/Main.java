package de.uniluebeck.itm.overlayclient;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

/**
 * The Class Main.
 */
public class Main {

	/** The version. */
	private static double version = 1.0;
	
	private static String ipRegex = "(((\\d{1,3}.){3})(\\d{1,3}))";
	private static boolean validInput = true;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException, java.lang.Exception {
		// create Options object
		Option helpOption = new Option("help", "print this message");
		Option versionOption = new Option("version",
				"print the version information");

		Options options = new Options();

		options.addOption(helpOption);
		options.addOption(versionOption);

		// add options for Meta-Service
		options.addOption("id", true, "id to search for");
		options.addOption("microcontroller", true,
				"microcontroller to search for");
		options.addOption("capability", true, "capability to search for");
		options.addOption("username", true, "username to connect to the sever");
		options.addOption("passwd", true, "password to connect to the server");
		options.addOption("server", true, "IP-Adress of the server");
		options.addOption("serverPort", true, "Port of the server");
		options.addOption("clientPort", true, "Port of the client");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		if(args.length == 0){
			printHelp(options);
		}
		else{
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println("One of these options is not registered.");
				printHelp(options);
			}
			if (cmd != null) {
				// standard-options
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				if (cmd.hasOption("version")) {
					System.out.println(version);
				}

				String id = cmd.getOptionValue("id");
				String microcontroller = cmd.getOptionValue("microcontroller");
				String capability = cmd.getOptionValue("capability");
				String user = cmd.getOptionValue("username");
				String password = cmd.getOptionValue("passwd");
				String server = cmd.getOptionValue("server");
				String serverPort = cmd.getOptionValue("serverPort");
				String clientPort = cmd.getOptionValue("clientPort");
				
				//Begin: validate input-data
				if (server == null) {
					System.out.println("Wrong input: Please enter server!");
					validInput = false;
				}
				if (server != null) {
					if (!server.matches(ipRegex) && !server.equals("localhost")) {
						System.out
								.println("Wrong input: This is no valide server address.");
						validInput = false;
					}
				}
				if (serverPort == null) {
					System.out.println("Wrong input: Please enter port of the server!");
					validInput = false;
				}
				if (id == null && microcontroller == null && capability == null) {
					System.out.println("Wrong input: Please enter id, microcontroller or capability to search for.!");
					validInput = false;
				}
			    //End: validate input-data
				
				if(validInput){
					if (server != null && (user == null && password == null || user == null)) {
						System.out.println("Username and Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Username: ");
						user = in.readLine();
						System.out.print("Password: ");
						password = in.readLine();
						in.close();
					}
					if (server != null && (password == null)) {
						System.out.println("Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Password: ");
						password = in.readLine();
						in.close();
					}
					
					OverlayClient metaService = new OverlayClient(user, password, server, serverPort, clientPort);
	
					if (id != null) {
						metaService.searchDeviceWithId(id);
					} else if (microcontroller != null) {
						metaService
								.searchDeviceWithMicrocontroller(microcontroller);
					} else if (capability != null) {
						metaService.searchDeviceWithCapability(capability);
					}
				}
			}
		}
	}
	
	public static void printHelp(Options options){
		System.out.println("Example:");
		System.out.println("Search by id of the node: -id 123 -server localhost -server_port 8181");
		//TODO Example for microcontroller
		//TODO Example for capabilities
		System.out.println("");

		// for help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("help", options);
	}
}
