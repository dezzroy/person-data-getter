package testthings.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.*;

import testthings.persondata.model.Person;

public abstract class GUI_Rendering 
{	
	public static void 
    setUpAndShow( String frameName, ArrayBlockingQueue<Person> queue )
    {
        try 
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch( UnsupportedLookAndFeelException ulafe )
        {
            System.err.println( ulafe );
        } 
        catch (ClassNotFoundException cnfe) 
        {
        	System.err.println( cnfe );
		} 
        catch (InstantiationException ie) 
        {
        	System.err.println( ie );
		} 
        catch (IllegalAccessException iae) 
        {
        	System.err.println( iae );
		}
        
        mainPanel = new JPanel() {
        	@Override
            public void paint(Graphics g)
            {                
                super.paint(g);
                
                final Graphics2D gCopy = (Graphics2D)g.create();
                
                gCopy.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                gCopy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                final int initialVerticalOffsetPixels = 48;
                
                final int verticalOffsetPixels = 48;
                
                final int drawnImageWidthAndHeight = verticalOffsetPixels - 8;
                
                int i = 0;
                for( Map.Entry<Person,BufferedImage> mapping : mapPersonImage.entrySet() )
                {
                	final Person p = mapping.getKey();
                	gCopy.setColor( Color.BLACK );
                	gCopy.drawString( p.name.first + " " + p.name.last, 16, 
            			initialVerticalOffsetPixels + verticalOffsetPixels * i );
                	
                	gCopy.drawString( p.location.toString(), 160, 
            			initialVerticalOffsetPixels + verticalOffsetPixels * i );
                	
                	gCopy.drawString( p.phone, 552, 
            			initialVerticalOffsetPixels + verticalOffsetPixels * i );
                	
                	final Image image = mapping.getValue();
                	
                	if( image != null )
                	{
                		gCopy.drawImage( image, 
            				mainPanel.getPreferredSize().width 
            					- drawnImageWidthAndHeight - 8, 
            				initialVerticalOffsetPixels - drawnImageWidthAndHeight 
            					+ verticalOffsetPixels * i, 
            				drawnImageWidthAndHeight, drawnImageWidthAndHeight, null);
                	}   
                	
                	++i;
                }
                
                gCopy.dispose();
            }
        };
        mainPanel.setLayout( null );
        mainPanel.setPreferredSize( new Dimension(800, 600) );
        
        frame = new JFrame( frameName );
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                windowCloseProcedure();
            }
        });        
        frame.add( mainPanel );
        frame.pack();
        frame.setVisible(true);
        
        guiPersonQueue = queue;
        
        GUI_UpdateTimer.start();
    }
	
	static void windowCloseProcedure()
    {
		GUI_UpdateTimer.stop();
		testthings.Main.mainShouldContinue = false;
        frame.setVisible(false); 
        frame.dispose();        
    }
		
	private static JFrame frame;
	private static JPanel mainPanel;
	private static HashMap<Person, BufferedImage> mapPersonImage = new HashMap<>();
	private static ArrayBlockingQueue<Person> guiPersonQueue;	
	
	private static final int renderFrameTriggerPeriodMS = 250;
	
	private static javax.swing.Timer GUI_UpdateTimer = 
		new javax.swing.Timer( renderFrameTriggerPeriodMS, new ActionListener()
	    {
	        @Override
	        public void actionPerformed( ActionEvent evt )
	        {
	        	/* Check for new data to render */
	        	if( !guiPersonQueue.isEmpty() )
				{
					Person p = guiPersonQueue.poll();
					System.out.println("GUI: received \"" + p + "\".");					
					
					BufferedImage image = null;
                	try
                	{
	                	URL url = new URL( p.picture.thumbnail );
	                    image = ImageIO.read(url);
                	}
                	catch( MalformedURLException mue )
                	{
                		System.err.println("GUI: " + mue);
                	}
                	catch( IOException ioe )
                	{
                		System.err.println("GUI: " + ioe);
                	}
                	
                	/* If there was trouble getting an image, the Person
                	 * instance will deliberately map to null */
					mapPersonImage.put(p, image);
				}      	
        	
	        	mainPanel.repaint();
	        }
    });
}
