/* This class acts as a thread which occasionally makes an HTTPS call on a
 * web service to get data about a fictitious person. The JSON data from the
 * web service is converted to POJO data using Google Gson, and the POJO
 * data is sent to the Main thread via a queue. */

package testthings.persondata;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.*;

import testthings.persondata.model.Person;

public class PersonDataGetter extends Thread {

	public PersonDataGetter( ArrayBlockingQueue<Person> PersonQueue ) 
	{
		super(threadName);
		
		sb = new StringBuilder();
		personQueue = PersonQueue;
		gson = new GsonBuilder().create();
		resultsCount = 0;
		shouldContinue = true;
	}
	
	public void notifyToQuit()
	{
		shouldContinue = false;
		this.interrupt();
	}
	
	@Override
	public void run()
	{
		while( shouldContinue )
		{
			long msToSleep = minSleepMS + 
				(long)(Math.random() * (maxSleepMS - minSleepMS));
			if( resultsCount < 2 )
			{
				/* Cause first result(s) to show more quickly */
				msToSleep = 1000l;
			}
			
			try
			{
				Thread.sleep(msToSleep);
			}
			catch( InterruptedException ie )
			{
				if( !shouldContinue )
				{
					break;
				}
			}			
			
			final String contentFromWebService = getContentFromWebService();
			
			if( contentFromWebService.isEmpty() )
			{
				System.err.println(this.getName() + ":\ngetContentFromWebService()" +
					" returned an empty string.");
				continue;
			}
			
			/* Extract JSON data for one Person */
			final int indexFirstBracket = contentFromWebService.indexOf("[");
			final int indexLastBracket = contentFromWebService.indexOf("]");
			
			if( indexFirstBracket == -1 || indexLastBracket == -1 )
			{
				System.err.println(this.getName() + ":\n" + "Got unexpected JSON" +
					" from " + contentFromWebService);
				continue;
			}
			
			final String onePersonJSON = 
				contentFromWebService.substring( indexFirstBracket + 1, indexLastBracket );
			 
			Person person = null;
			try
			{
				/* Sometimes the web service has a value like "R4E 5EZ"
				 * for a number value. If it occurs, we will ignore the
				 * person data retrieved this iteration. */
				person = gson.fromJson( onePersonJSON, Person.class );
			}
			catch( NumberFormatException nfe )
			{
				System.out.println(this.getName() + ":\n" + nfe);
				continue;
			}
			catch( com.google.gson.JsonSyntaxException jse )
			{
				System.out.println(this.getName() + ":\n" + jse);
				continue;
			}
			
			try
			{
				personQueue.put(person);
				resultsCount++;
				if( resultsCount >= numberOfResultsUntilQuit )
				{
					shouldContinue = false;
				}
			}
			catch( InterruptedException ie )
			{}
		}
		
		System.out.println(this.getName() + " finished.");
	}

	private String getContentFromWebService()
	{
		// clear out StringBuilder instance content
		sb.delete(0, sb.length()); 
		
		/* HTTPS GET call code based on:
		 * http://alvinalexander.com/blog/post/java/simple-https-example */
		try
		{
			URL myurl = new URL(webServiceURL);
		    HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		    InputStream ins = con.getInputStream();
		    InputStreamReader isr = new InputStreamReader(ins);
		    BufferedReader in = new BufferedReader(isr);
	
		    String inputLine;
	
		    while( (inputLine = in.readLine()) != null )
		    {
		    	sb.append(inputLine);
		    }
		}
		catch( IOException ioe )
		{
			System.err.println(this.getName() + ":\n" + ioe);
		}
		
		return sb.toString();
	}
	
	volatile boolean shouldContinue;
	
	final private StringBuilder sb;
	final private ArrayBlockingQueue<Person> personQueue;
	final private Gson gson;
	private int resultsCount;
	
	static final private long minSleepMS = 3000l;
	static final private long maxSleepMS = 10000l;
	static final private int numberOfResultsUntilQuit = 10;

	static final private String threadName = "PersonDataGetter";
	static final private String webServiceURL = "https://randomuser.me/api/";
}
