/* The Main class sets up queues used by the GUI thread and the PersonDataGetter
 * threads. Main starts these subordinate threads, but closing the GUI window
 * signals to Main to terminate both Main and PersonDataGetter threads. */

package testthings;

import java.util.concurrent.*;

import testthings.gui.GUI_Rendering;
import testthings.persondata.PersonDataGetter;
import testthings.persondata.model.Person;;

public class Main 
{
	public static final int queueCapacity = 10;
	public static volatile boolean mainShouldContinue = true;

	public static void main(String[] args) 
	{
		final ArrayBlockingQueue<Person> personQueueModel = 
			new ArrayBlockingQueue<>(queueCapacity);
		
		final ArrayBlockingQueue<Person> personQueueGUI = 
				new ArrayBlockingQueue<>(queueCapacity);
		
		final PersonDataGetter personDataGetter = 
			new PersonDataGetter(personQueueModel);
	
		personDataGetter.start();		
		
		/* Start GUI thread */
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                GUI_Rendering.setUpAndShow("Who is new?", personQueueGUI);
            }
        });
		
		while( mainShouldContinue )
		{
			try
			{
				Thread.sleep( 100l );
			}
			catch( InterruptedException ie )
			{}
			
			if( !personQueueModel.isEmpty() )
			{
				Person p = personQueueModel.poll();				
				try
				{
					personQueueGUI.put(p);
				}
				catch( InterruptedException ie )
				{}
			}
		}
		
		personDataGetter.notifyToQuit();
		try
		{
			personDataGetter.join();
		}
		catch( InterruptedException ie )
		{}
		
		personQueueModel.clear();
		personQueueGUI.clear();
		
		System.out.println("Main finished.");
	}
}
