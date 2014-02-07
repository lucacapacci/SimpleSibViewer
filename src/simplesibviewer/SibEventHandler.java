package simplesibviewer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Vector;

import jlibs.core.lang.Ansi;

import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;
import sofia_kp.iKPIC_subscribeHandler;


public class SibEventHandler implements iKPIC_subscribeHandler{
	
	private String URI = "uri";
    private KPICore kp;
    
    private Monitor monitor;
    
    private Ansi allTheTriples = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.WHITE, Ansi.Color.RED);
    
    private Ansi newEvent = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.WHITE, Ansi.Color.GREEN);
	
    private Ansi newTriples = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.WHITE, Ansi.Color.BLUE);
	
    private Ansi oldTriples = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.YELLOW, Ansi.Color.BLACK);
    
	public SibEventHandler(KPICore kp)
	{
		monitor = new Monitor();
		
		this.kp = kp;
		
		//shows the initial SIB's triples
		printSibContent();
		
		kp.setEventHandler(this);
		
		SSAP_XMLTools xml_tools = new SSAP_XMLTools();			
		String xml = "";
		
		//------------------- event registration --------------------
		
		xml_tools = new SSAP_XMLTools();			
		
		xml = kp.subscribeRDF(null, null , null , URI);
		
		if(xml_tools.isSubscriptionConfirmed(xml))
		{
			try
			{
				xml_tools.getSubscriptionID(xml);
			}
			catch(Exception e)
			{
			}
		}
		else
		{
			System.out.println ("Error during subscription to URIs");
		}
		
		//------------------- input ------------------- 
		while (true)
		{
			String s = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				s = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (s.startsWith("s"))
			{
				printSibContent();
			}
			else
			{
				System.out.println ("Disconnecting. Please wait...");
				xml = kp.leave();
				xml_tools = new SSAP_XMLTools();

				boolean ack = xml_tools.isLeaveConfirmed(xml);

				if(!ack)
				{
					System.out.println ("Error during LEAVE");
				} 
				else
				{
					System.out.println ("Disconnected from the SIB");
				}
				System.exit(0);
			}		
		}
	}

	@Override
	public void kpic_SIBEventHandler(String xml_received) 
	{
		final String xml = xml_received;

		new Thread(
				new Runnable() 
				{
					public void run() 
					{
						SSAP_XMLTools xmlTools = new SSAP_XMLTools();
						boolean isunsubscription = xmlTools.isUnSubscriptionConfirmed(xml);
						if(!isunsubscription)
						{	
							monitor.startPrinting();
							System.out.println();
							java.util.Date date= new java.util.Date();
							newEvent.outln("******************************** SIB Event: "+new Timestamp(date.getTime())+" ********************************");
							
							//new triples
							Vector<Vector<String>> triples_n = new Vector<Vector<String>>();
							triples_n = xmlTools.getNewResultEventTriple(xml);
							
							//old triples
							Vector<Vector<String>> triples_o = new Vector<Vector<String>>();
							triples_o = xmlTools.getObsoleteResultEventTriple(xml);
							
							//prints the new triples
							System.out.println();
							newTriples.outln("++++++++ New triples: "+triples_n.size()+" ++++++++");
							for(int i = 0; i < triples_n.size(); i++ )
							{
								System.out.println();
								newTriples.outln("++++++++ "+i+" ++++++++");
								newTriples.outln("subject = " + triples_n.elementAt(i).elementAt(0));
								newTriples.outln("predicate = " + triples_n.elementAt(i).elementAt(1));
								newTriples.outln("object = " + triples_n.elementAt(i).elementAt(2));
							}
							
							//prints the removed triples
							System.out.println();
							oldTriples.outln("-------- Old triples: "+triples_o.size()+" --------");
							for(int i = 0; i < triples_o.size(); i++ )
							{
								System.out.println();
								oldTriples.outln("-------- "+i+" --------");
								oldTriples.outln("subject = " + triples_o.elementAt(i).elementAt(0));
								oldTriples.outln("predicate = " + triples_o.elementAt(i).elementAt(1));
								oldTriples.outln("object = " + triples_o.elementAt(i).elementAt(2));
							}	
							
							monitor.stopPrinting();
						}
					}
				}
				).start();
	}
	
	private void printSibContent()
	{	
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		
		SSAP_XMLTools xml_tools = new SSAP_XMLTools();
			
		//makes a query to discover the SIB's content
		String xml = kp.queryRDF (null, null, null ,URI, URI);

		boolean ack = xml_tools.isQueryConfirmed(xml);
		if(!ack)
		{
			System.out.println ("Error during RDF-M3  URI query");
		}    
		else
		{
			triples = xml_tools.getQueryTriple(xml);
		}
		
		monitor.startPrinting();
		
		System.out.println();
		allTheTriples.outln("******** Current triples in the SIB: "+triples.size()+" *******");
		for(int i = 0; i < triples.size(); i++ )
		{
			System.out.println();
			allTheTriples.outln("******** "+i+" ********");
			allTheTriples.outln("subject = " + triples.elementAt(i).elementAt(0));
			allTheTriples.outln("predicate = " + triples.elementAt(i).elementAt(1));
			allTheTriples.outln("object = " + triples.elementAt(i).elementAt(2));
		}
		
		monitor.stopPrinting();
	}

}
