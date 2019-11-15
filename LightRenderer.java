package lightnshadow;

class LightRenderer
{
	public int[]	vtLight;
	int				vectorWidth;
	int				vectorLength;
	
	public LightRenderer( int width, int length )
	{
		init( width, length );
	}
	
	public LightRenderer( int width, int length, Coord3D[] vtSurface, Coord3D light )
	{
		init( width, length );
		processCoord3D( vtSurface, light );
	}
	
	void init( int width, int length )
	{
		vectorWidth		= width;
		vectorLength	= length;
		
		vtLight			= new int[ width * length ];
	}
	
	void processCoord3D( Coord3D[] vtSurface, Coord3D light )
	{
		process( vtSurface, light.x, light.y, light.z );
	}
	
	void process( Coord3D[] vtSurface, float fLightX, float fLightY, float fLightZ )
	{
		int			ix;
		float		fLightX2;
		float		fLightY2;
		float		fLightZ2;
		float		fLight2Sum;
		int			iLength;
		
		fLightX2		= fLightX * fLightX;
		fLightY2		= fLightY * fLightY;
		fLightZ2		= fLightZ * fLightZ;
		fLight2Sum		= ( float ) Math.sqrt( fLightX2 + fLightY2 + fLightZ2 );
		iLength			= vectorWidth * vectorLength;

		fLightX2		= fLightX / fLight2Sum;
		fLightY2		= fLightY / fLight2Sum;
		fLightZ2		= fLightZ / fLight2Sum;

		/* Light intensity: cossine of the angle between light vector and normal
		 * (0 <= cossine <= 1) multiplied by 255.
		 */
		for( ix = 0; ix < iLength; ix++ )
		{
			vtLight[ ix ]	= ( int ) Math.max(
								( - fLightY2 * vtSurface[ ix ].x
									- fLightX2 * vtSurface[ ix ].y
									- fLightZ2 * vtSurface[ ix ].z
								) * 255
								, 0 );
		}
	}
}