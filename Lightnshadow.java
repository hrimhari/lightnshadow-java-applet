package lightnshadow;

/*
 * @(#)Lightnshadow.java 1.0 03/02/24
 *
 * You can modify the template of this file in the
 * directory ..\JCreator\Templates\Template_2\Project_Name.java
 *
 * You can also create your own project template by making a new
 * folder in the directory ..\JCreator\Template\. Use the other
 * templates as examples.
 *
 */

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.image.*;

public class Lightnshadow extends Applet implements MouseListener, MouseMotionListener {
	Image				imgHeight;
	PixelGrabber		pixelGrabber;
	int[]				imgPixels;
	Frame				frame;
	boolean				bDebug;
	Normal				imgNormal;
	LightRenderer		imgLight;
	ShadowRenderer		shadowRenderer;
	int					iImgHeight;
	int					iImgWidth;
	int					iImgPosX;
	int					iImgPosY;
	Color				color;
	Image				imgLightDisplay;
	MemoryImageSource	imgMemory;
	float				fLightX;
	float				fLightY;
	float				fLightZ;
	boolean				bProcess;
	boolean				bLoaded;
	String				sMsg;
	long				lLastTime;
	long				lTimeStart;
	Coord3D				normalizedLight;
	Image				imgDoubleBuffer;
	float				fFPS;

	class AlertDialog implements ActionListener
	{
		Dialog	d;
		Button	bOk;
		
		public AlertDialog( String sTitle, String sMsg )
		{
			if ( bDebug )
			{
				System.out.println( "AlertDialog, title = " + sTitle + ", msg = " + sMsg );
			}
			
			d		= new Dialog( frame, sTitle, true );
			d.add( new Label( "sMsg" ) );
			
			bOk		= new Button( "Ok" );
			bOk.setActionCommand( "OK" );
			bOk.addActionListener( this );
			
			d.add( bOk );
			d.show();
		}
		
		public void actionPerformed( ActionEvent e )
		{
			if ( e.getActionCommand().equals( "OK" ) )
			{
				d.dispose();
			}
		}
	}
	
	public void init() {
		imgHeight			= null;
		bDebug				= false;
		this.addMouseListener( this );
		this.addMouseMotionListener( this );
		frame				= new Frame();
		imgPixels			= null;
		imgNormal			= null;
		imgLight			= null;
		iImgPosX			= 0;
		iImgPosY			= 20;
		color				= new Color( 0, 0, 0 );
		bProcess			= false;
		sMsg				= "Click to load image";
		lLastTime			= System.currentTimeMillis();
		normalizedLight		= new Coord3D();		
		imgDoubleBuffer		= createImage( getSize().width, getSize().height );
		fFPS				= -1;
	}

	public void mouseDragged( MouseEvent e )
	{
	}
	
	public void mouseMoved( MouseEvent e )
	{
		int		iTrueX;
		int		iTrueY;
		int		iBound;
	
		if ( !bLoaded )
		{
			return;
		}
		
		lTimeStart	= System.currentTimeMillis();
		
		iBound		= ( int ) Math.min( iImgWidth / 2, iImgHeight / 2 );
		iTrueX		= - e.getX() + ( iImgWidth / 2 ) + iImgPosX;
		iTrueY		= - e.getY() + ( iImgHeight / 2 ) + iImgPosY;
		
		if ( iTrueX < 0 )
		{
			iTrueX	= ( int ) Math.max( iTrueX, - iBound );
		}
		else
		{
			iTrueX	= ( int ) Math.min( iTrueX, iBound );
		}
		
		if ( iTrueY < 0 )
		{
			iTrueY	= ( int ) Math.max( iTrueY, - iBound );
		}
		else
		{
			iTrueY	= ( int ) Math.min( iTrueY, iBound );
		}
		
		fLightX		= ( float ) iTrueX;
		fLightY		= ( float ) iTrueY;
		fLightZ		= - ( float ) Math.sqrt( Math.max( iBound * iBound - ( iTrueX * iTrueX + iTrueY * iTrueY ), 0 ) );

		Normal.normalizedVector( fLightX, fLightY, fLightZ, normalizedLight );
				
		if ( bDebug )
		{
			System.out.println( "Processing light: normal = " + imgNormal.vectorSpace + ", light = " + imgLight.vtLight );
		}
		
		imgLight.process( imgNormal.vectorSpace, fLightX, fLightY, fLightZ );

		if ( bDebug )
		{
			System.out.println( "Processing shadow" );
		}
		
		shadowRenderer.process( imgPixels, imgLight.vtLight, imgNormal.vtMax
								, fLightX, fLightY, fLightZ
								, ShadowRenderer.SET_NEIGHBORS
									| ShadowRenderer.CHECK_NEIGHBORS );
			
		imgMemory.newPixels();	
		bProcess	= true;
		
		paint( getGraphics() );
	}
			
	public void mouseClicked( MouseEvent e )
	{
	}
	
	public void mouseEntered( MouseEvent e )
	{
	}
	
	public void mouseExited( MouseEvent e )
	{
	}
	
	public void mouseReleased( MouseEvent e )
	{
	}
	
	public void mousePressed( MouseEvent e )
	{
		URL					url;
		FileDialog			wFile;
		boolean				bChoosen;
		String				sFile;
		String				sPath;
		InputStream			inFile;
		AlertDialog			alert;
		
		bLoaded		= false;
		bProcess	= false;

		if ( imgHeight != null )
		{
			imgHeight.flush();
		}
		
		if ( bDebug )
		{
			System.out.println( "mousePressed" );
		}
/*			
		bChoosen	= false;
		wFile		= new FileDialog( frame, "Choose an Image", FileDialog.LOAD );

		url			= null;
				
		while( !bChoosen )
		{
			wFile.show();
			
			sFile		= wFile.getFile();
			sPath		= wFile.getDirectory();
			
			try
			{
				url			= new URL( "file", "", sPath + "/" + sFile );			
			}
			catch( MalformedURLException exc )
			{
				alert		= new AlertDialog( "Error loading image", exc.getMessage() );
				return;
			}
			
			bChoosen	= true;
			
			try
			{
				inFile		= url.openStream();
			}
			catch( IOException exc )
			{
				bChoosen	= false;
				alert		= new AlertDialog( "Error loading image", exc.getMessage() );
			}
		}			
		*/
		url		= null;
		
		try
		{
			url				= new URL( getDocumentBase(), "hills.gif" );
		}
		catch ( MalformedURLException exc )
		{
			sMsg	= "Error!";
			exc.printStackTrace();
			return;
		}
		
		try
		{
			inFile		= url.openStream();
		}
		catch( Exception exc )
		{
			sMsg	= "Error!";
			System.out.println( "For URL: " + url );
			exc.printStackTrace();
			
			if ( lLastTime + 100 < System.currentTimeMillis() )
			{
				repaint();
			}
			return;
		}

		try
		{
			imgHeight		= getImage( url );
		}
		catch( Exception exc )
		{
			sMsg	= "Error!";
			System.out.println( "For URL: " + url );
			exc.printStackTrace();
			
			if ( lLastTime + 100 < System.currentTimeMillis() )
			{
				repaint();
			}
			return;
		}

		if ( bDebug )
		{
			System.out.println( "Grabbing pixels" );
		}
		
		pixelGrabber	= new PixelGrabber( imgHeight, 0, 0, -1, -1, true );
		
		iImgHeight		= imgHeight.getHeight( this );
		iImgWidth		= imgHeight.getWidth( this );
		
		try
		{
			pixelGrabber.grabPixels();
		}
		catch( InterruptedException exc )
		{
			sMsg	= "Error!";
			
			if ( lLastTime + 100 < System.currentTimeMillis() )
			{
				repaint();
			}
			exc.printStackTrace();
			return;
		}
		sMsg	= "Loading image...";	

		if ( lLastTime + 100 < System.currentTimeMillis() )
		{
			repaint();
		}
	}
	
	public void paint(Graphics trueGraphics ) {
		int			iW;
		int			iL;
		Graphics	g;

		g	= imgDoubleBuffer.getGraphics();
		
		g.clearRect( 0, 0, imgDoubleBuffer.getWidth( this ), imgDoubleBuffer.getHeight( this ) );
		
		lLastTime	= System.currentTimeMillis();
		
		if ( bProcess )
		{
			g.drawImage( imgLightDisplay, iImgPosX, iImgPosY, this );
			/*/
			for ( iW = 0; iW < iImgWidth; iW++ )
			{
				for ( iL = 0; iL < iImgHeight; iL++ )
				{
					color	= new Color( imgLight.vtLight[ iW + iL * iImgWidth ]
										, imgLight.vtLight[ iW + iL * iImgWidth ]
										, imgLight.vtLight[ iW + iL * iImgWidth ]
									);
					if ( bDebug  && imgLight.vtLight[ iW + iL * iImgWidth ] > 200 )
					{
						System.out.println( imgLight.vtLight[ iW + iL * iImgWidth ] );
					}
					
					g.setColor( color );
					g.drawLine( iW, iL, iW, iL );
				}
			}
			*/
			sMsg	= "Vl = (" + normalizedLight.x + ", " + normalizedLight.y 
						+ ", " + normalizedLight.z
						+ ")";
						
			if ( lTimeStart > 0 )
			{
				fFPS		= 1000 / ( ( float ) ( System.currentTimeMillis() - lTimeStart ) );
				lTimeStart	= -1;
			}
			
			if ( fFPS > 0 )
			{
				sMsg	= "FPS = " + fFPS + ", " + sMsg;
			}
		}
		else if ( bLoaded )
		{
			g.drawImage( imgHeight, iImgPosX, iImgPosY, this );
		}

		g.drawString( sMsg, 5, iImgPosY - 1 );
		
		trueGraphics.drawImage( imgDoubleBuffer, 0, 0, this );
		
	}
	
	public boolean imageUpdate( Image img
							, int infoflags
							, int x
							, int y
							, int w
							, int h )
	{
		int					iPercent;
		Object				oPixels;
		int					ix;
		IndexColorModel		colorModel;
		byte[]				r;
		byte[]				g;
		byte[]				b;
		int					iCMSize;
		int					iCMBits;
		
		if ( img.equals( imgHeight ) )
		{
			if ( ( infoflags & ALLBITS ) != 0 )
			{
				/* Create grayscale color model */
				iCMBits		= 8;
				iCMSize		= 256;
				
				r			= new byte[ iCMSize ];
				g			= new byte[ iCMSize ];
				b			= new byte[ iCMSize ];
				
				for ( ix = 0; ix < iCMSize; ix++ )
				{
					r[ ix ]		= ( byte ) ix;
					g[ ix ]		= ( byte ) ix;
					b[ ix ]		= ( byte ) ix;
				}
				
				colorModel	= new IndexColorModel( iCMBits, iCMSize, r, g, b );
				
				oPixels			= pixelGrabber.getPixels();

				if ( ( pixelGrabber.getStatus() & ImageObserver.ABORT ) != 0 )
				{
					sMsg	= "Error!";
					System.out.println( "Image fetch aborted or errored." );
					return true;
				}
		
				imgPixels	= ( int[] ) oPixels;
				
				iImgWidth		= pixelGrabber.getWidth( );
				iImgHeight		= pixelGrabber.getHeight( );
				
				/* Filter unwanted components */
				for( ix = 0; ix < iImgWidth * iImgHeight; ix++ )
				{
					imgPixels[ ix ]		= ( imgPixels[ ix ] >> 8 ) & 0xFF;
				}
				
				if ( bDebug )
				{
					System.out.println( "Processing Normal: w = " + iImgWidth + ", l = " + iImgHeight );
				}
		
				imgNormal		= new Normal( iImgWidth, iImgHeight, imgPixels );
				imgLight		= new LightRenderer( iImgWidth, iImgHeight );
				
				/* Initialize pixels for light image */
				for ( ix = 0; ix < iImgWidth * iImgHeight; ix++ )
				{
					imgLight.vtLight[ ix ]	= 0;
				}
				
				/* Create memory image based on light pixels array */
				imgMemory		= new MemoryImageSource( iImgWidth, iImgHeight
												, colorModel
												, imgLight.vtLight, 0, iImgWidth);
				imgMemory.setAnimated( true );
				imgLightDisplay	= createImage( imgMemory );
				
				imgNormal.neighborMaxHeight( imgPixels );
				
				/* Create shadow renderer */
				shadowRenderer		= new ShadowRenderer( iImgWidth, iImgHeight );
				sMsg				= "Loaded. Move the mouse to begin";
				
				iImgPosX			= getSize().width / 2 - iImgWidth / 2;
				bLoaded				= true;
				repaint();

				return false;
			}
			else if ( ( infoflags & HEIGHT ) != 0 )
			{
				iImgHeight	= h;
			}
			else if ( ( infoflags & WIDTH ) != 0 )
			{
				iImgWidth	= w;
			}
			else if ( ( infoflags & SOMEBITS ) != 0 )
			{
				if ( ( iImgHeight < 0 ) || ( iImgWidth < 0 ) )
				{
					iPercent	= 0;
				}
				else
				{
					iPercent	= 100 - ( ( iImgWidth - x ) * 100 / iImgWidth );
				}
				
				sMsg	= "Loading: " + iPercent + "%";
			}
			else if ( ( infoflags & ERROR ) != 0 )
			{
				sMsg	= "Error!";
			}

			if ( lLastTime + 100 < System.currentTimeMillis() )
			{
				repaint();
			}
			return true;
		}
		else
		{
			if ( ( infoflags & ALLBITS ) != 0 )
			{
				repaint();
				return false;
			}
			else
			{
				return true;
			}
		}				
	}
}
