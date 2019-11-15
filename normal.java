package lightnshadow;

class Normal
{
	public Coord3D[]	vectorSpace;
	public int[]		vtMax;
	int					vectorLength;
	int					vectorWidth;
	boolean				bDebug;
	
	public Normal()
	{
		init( -1, -1 );
	}
	
	public Normal( int width, int length )
	{
		init( length, width );
	}

	public Normal( int width, int length, int[] vectorHeight )
	{
		init( length, width );
		process( vectorHeight );
	}
		
	void init( int length, int width )
	{
		int		ix;
		int		iLen;
		
		bDebug			= false;
		vectorLength	= length;
		vectorWidth		= width;
		
		if ( ( length < 0 ) || ( width < 0 ) )
		{
			vectorSpace	= null;
			vtMax		= null;
		}
		else
		{
			iLen			= length * width;
			vectorSpace		= new Coord3D[ iLen ];
			vtMax			= new int[ iLen ];
			
			for( ix = 0; ix < iLen; ix++ )
			{
				vectorSpace[ ix ] 	= new Coord3D();
				vtMax[ ix ]			= 0;
			}
		}
	}
	
	public void process( int[]	vectorHeight )
	{
		int		iL;
		int		iW;
		float	fVectorXx;
		float	fVectorXy;
		float	fVectorXz;
		float	fVectorYx;
		float	fVectorYy;
		float	fVectorYz;
		float	fNorm;

		if ( vectorSpace	== null )
		{
			/* @@@ throw exception */
			return;
		}
					
		if ( vectorHeight.length < vectorSpace.length )
		{
			/* @@@ throw exception */
			System.out.println( "Aborting: height len = " + vectorHeight.length
							+ ", space len = " + vectorSpace.length );
			return;
		}
		
		fVectorXx	= 1;
		fVectorXy	= 0;
		fVectorYx	= 0;
		fVectorYy	= 1;

		for( iW	= 0; iW < vectorWidth - 1; iW++ )
		{
			for( iL = 0;	iL < vectorLength - 1; iL++ )
			{
				fVectorXz	= ( ( float ) vectorHeight[ iW + ( ( iL + 1 ) * vectorWidth ) ]
								- vectorHeight[ iW + ( iL * vectorWidth ) ] );
								
				fVectorYz	= ( ( float ) vectorHeight[ iW + ( iL * vectorWidth ) + 1 ]
								- vectorHeight[ iW + ( iL * vectorWidth ) ] );
								
				vectorSpace[ iW + ( iL * vectorWidth ) ].x
						= fVectorXy * fVectorYz - fVectorXz * fVectorYy;
				vectorSpace[ iW + ( iL * vectorWidth ) ].y
						= fVectorXz * fVectorYx - fVectorXx * fVectorYz;
				vectorSpace[ iW + ( iL * vectorWidth ) ].z
						= fVectorXx * fVectorYy - fVectorXy * fVectorYx;
						
				if ( bDebug )
				{
					if ( ( iL > 120 ) && ( iL < 140 )
						&& ( iW > 120 ) && ( iW < 140 ) )
					{
						System.out.println( "(" + iW + ", " + iL + "): "
							+ vectorHeight[ iW + ( iL * vectorWidth ) ]
							+ " (" + vectorSpace[ iW + ( iL * vectorWidth ) ].x
							+ ", " + vectorSpace[ iW + ( iL * vectorWidth ) ].y
							+ ", " + vectorSpace[ iW + ( iL * vectorWidth ) ].z
							+ ")" );
					}
				}
			}
		}
		
		for( iW = 0; iW < vectorWidth; iW++ )
		{
			vectorSpace[ iW + ( vectorLength - 1 ) * vectorWidth ].x
					= vectorSpace[ iW + ( vectorLength - 2 ) * vectorWidth ].x;
			vectorSpace[ iW + ( vectorLength - 1 ) * vectorWidth ].y
					= vectorSpace[ iW + ( vectorLength - 2 ) * vectorWidth ].y;
			vectorSpace[ iW + ( vectorLength - 1 ) * vectorWidth ].z
					= vectorSpace[ iW + ( vectorLength - 2 ) * vectorWidth ].z;
		}
		
		for ( iL = 0; iL < vectorLength; iL++ )
		{
			vectorSpace[ vectorWidth - 1 + ( vectorWidth * iL ) ].x
					= vectorSpace[ vectorWidth - 2 + ( vectorWidth * iL ) ].x;
			vectorSpace[ vectorWidth - 1 + ( vectorWidth * iL ) ].y
					= vectorSpace[ vectorWidth - 2 + ( vectorWidth * iL ) ].y;
			vectorSpace[ vectorWidth - 1 + ( vectorWidth * iL ) ].z
					= vectorSpace[ vectorWidth - 2 + ( vectorWidth * iL ) ].z;
		}
		
		/* Normalize */
		for( iW = 0; iW < ( vectorWidth * vectorLength ); iW++ )
		{
			fNorm		= ( float ) Math.sqrt( vectorSpace[ iW ].x * vectorSpace[ iW ].x
											+ vectorSpace[ iW ].y * vectorSpace[ iW ].y
											+ vectorSpace[ iW ].z * vectorSpace[ iW ].z );
			vectorSpace[ iW ].x		= vectorSpace[ iW ].x / fNorm;
			vectorSpace[ iW ].y		= vectorSpace[ iW ].y / fNorm;
			vectorSpace[ iW ].z		= vectorSpace[ iW ].z / fNorm;
		}
		
	}

	public static void normalizedVector( float x, float y, float z, Coord3D vector )
	{
		float		norm;
		
		norm		= ( float ) Math.sqrt( x * x + y * y + z * z );

		vector.x	= x / norm;
		vector.y	= y / norm;
		vector.z	= z / norm;		
	}
	
	public void neighborMaxHeight( int[] vtHeight )
	{
		int	ixW;
		int	ixL;
	
		if ( vtMax == null )
		{
			return;
		}
	
		for ( ixW = 0; ixW < vectorWidth; ixW++ )
		{
			for ( ixL = 0; ixL < vectorLength; ixL++ )
			{
				vtMax[ ixW + ixL * vectorWidth ] =
					( int ) Math.max( vtHeight[ ixW + ixL * vectorWidth ]
							, Math.max( ixW > 0 
										? vtHeight[ ixW - 1 + ixL * vectorWidth ] 
										: 0
									, Math.max( ixL > 0 
												? vtHeight[ ixW + ( ixL - 1 ) * vectorWidth ] 
												: 0
											, Math.max( ixW < vectorWidth - 1
														? vtHeight[ ixW + 1 + ixL * vectorWidth ]
														: 0
													, ( ixL < vectorLength - 1 ) && ( ixW < vectorWidth - 1 )
														? vtHeight[ ixW + 1 + ( ixL + 1 ) * vectorWidth ]
														: 0
												)
										)
								)
						);
	
			}
		}
	}
}
