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
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		// create Options object
		Option help_option = new Option("help", "print this message");
		Option version_option = new Option("version",
				"print the version information");

		Options options = new Options();

		options.addOption(help_option);
		options.addOption(version_option);

		// add options for Messenger
		options.addOption("port", true, "port");
		options.addOption("server", true, "server");
		options.addOption("message", true, "message to send");
		options.addOption("user", true, "username to connect to the server");
		options.addOption("passwd", true, "password to connect to the server");
		options.addOption("device", true,
				"type of the device in local case: jennec, telosb or pacemate");
		options.addOption("id", true, "ID of the device in remote case");
		options.addOption("message_type", true, "Type of the Message to be send");

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
			}
			if (cmd != null) {
				// standard-options
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				if (cmd.hasOption("version")) {
					System.out.println(version);
				}

				// der Messenger
				if (args[0].equals("send")) {
					System.out.println("start Messenger...");

					String port = cmd.getOptionValue("port");
					String server = cmd.getOptionValue("server");
					String message = cmd.getOptionValue("message");
					String user = cmd.getOptionValue("user");
					String password = cmd.getOptionValue("passwd");
					String device = cmd.getOptionValue("device");
					String id = cmd.getOptionValue("id");
					String message_type = cmd.getOptionValue("message_type");
					
					if(message_type == null){
						System.out.println("Please enter a message_type!");
						System.exit(1);
					}
					if(device == null && server == null){
						System.out.println("Please enter device or server!");
						System.exit(1);
					}
					if(port == null){
						System.out.println("Please enter port!");
						System.exit(1);
					}
					if(server != null && id == null){
						System.out.println("Please enter id of the node!");
						System.exit(1);
					}

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
					Messenger messenger = new Messenger(port, server, user, password, device, id, message_type);
					messenger.connect();
					messenger.send(message);
				}
			}
		}	
	}
	
	public static void printHelp(Options options){
		System.out.println("Example:");
		System.out
				.println("Messenger: Remote example: send -message 68616c6c6f -port 8181 -server localhost -id 1 -message_type 1");
		System.out
		.println("Messenger: Local example: send -message 68616c6c6f -port 1282 -device jennec -message_type 1");
		System.out.println("");
		// for help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("help", options);
	}
}
