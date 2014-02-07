package simplesibviewer;
import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;


public class MyMain {
	
	private static String SIB_Host;
	private static int SIB_Port;
	private static String SIB_Name;
	
	private static KPICore kp;

	public static void main(String[] args) 
	{
		if (args.length < 2) //auto setting
		{
			SIB_Host = "127.0.0.1";
			SIB_Port = 10010;
			SIB_Name = "X";
		}
		else if (args.length < 3) //manual IP and port setting
		{
			SIB_Host = args[0];
			SIB_Port = Integer.parseInt(args[1]);
			SIB_Name = "X";
		}
		else if (args.length < 4)
		{
			SIB_Host = args[0];
			SIB_Port = Integer.parseInt(args[1]);
			SIB_Name = args[2];
		}
		else
		{
			System.out.println("Invalid arguments. Usage: \n java -jar simpleSibViewer.jar [<SIB_Host>] [<SIB_Port>] [<SIB_Name>]");
			System.exit(0);
		}
		
		System.out.println("SIB_Host: "+SIB_Host);
		System.out.println("SIB_Port: "+SIB_Port);
		System.out.println("SIB_Name: "+SIB_Name);
		
		initKPI();
	}
	
	private static void initKPI()
	{
		kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);

		kp.disable_debug_message();
		kp.disable_error_message();

		String xml = kp.join();
		SSAP_XMLTools xml_tools = new SSAP_XMLTools();

		boolean ack = xml_tools.isJoinConfirmed(xml);
		if(!ack)
		{
			System.out.println ("Error joining the SIB");
			System.exit(0);
		}
		else
		{
			System.out.println ("Connected to the SIB");
		}

		//creation of the SIB event handler
		new SibEventHandler(kp);
	}

}
