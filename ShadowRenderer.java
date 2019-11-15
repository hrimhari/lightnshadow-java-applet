package lightnshadow;

class ShadowRenderer
{
	int						vectorWidth;
	int						vectorLength;
	int						iSWidth;
	ShadowCast[]			vtShadowSpace;
	public static final int	CHECK_NEIGHBORS		= 1;
	public static final int	SET_NEIGHBORS		= 2;

	class ShadowCast
	{
		public int		iCasterIx;
		public float	fHeight;
		
		public ShadowCast()
		{
			init( 0, 0 );
		}
		
		public ShadowCast( int ix, float height )
		{
			init( ix, height );
		}
		
		void init( int ix, float height )
		{
			iCasterIx	= ix;
			fHeight		= height;
		}
	}
	
	public ShadowRenderer( int width, int length )
	{
		init( width, length );
	}
	
	void init( int width, int length )
	{
		int		ix;
		
		vectorWidth		= width;
		vectorLength	= length;
		
		iSWidth			= ( int ) Math.sqrt( width * width + length * length )
							+ 1;
		vtShadowSpace	= new ShadowCast[ iSWidth * iSWidth + 1 ];
		
		for( ix = 0; ix < iSWidth * iSWidth; ix++ )
		{
			vtShadowSpace[ ix ]		= new ShadowCast();
		};
	}
	
	void process( int[] vtHeight, int[] vtLight, int[] vtMax
			, float fLightX, float fLightY
			, float fLightZ, int iFlags )
	{
		float			fLightX2;
		float			fLightY2;
		float			fLightZ2;
		float			fCos;
		float			fTan;
		float			fSinProj;
		float			fCosProj;
		float			fW;
		float			fL;
		float			fWTemp;
		float			fLTemp;
		float			fSHeight;
		float			fLength;
		float			fWidth;
		int				iSHeight;
		int				iW;
		int				iL;
		int				ixHeight;
		int				iWShadow;
		int				iLShadow;
		int				ixShadow;
		int				iSWidth2;
	
		fLightX2	= fLightX * fLightX;
		fLightY2	= fLightY * fLightY;
		fLightZ2	= fLightZ * fLightZ;
		iSWidth2	= iSWidth * iSWidth;
		
		/* Compute cossine and tangent of the light ray with its projection 
		 * on the surface's plane 
		 */
		fCos	= ( fLightX2 + fLightY2 )
				/ ( float ) Math.sqrt( ( fLightX2 + fLightY2 + fLightZ2 )
									* ( fLightX2 + fLightY2 ) );
	
		if ( fCos == 0 )
		{
			/* If cossine is zero, there's no shadow */
			return;
		}
	
		fTan	= ( float ) Math.sqrt( 1 / ( fCos*fCos ) - 1 );
	
		/* Sine, cossine, tangent of the x and y components to the light projection */
		fSinProj	= fLightY / ( float ) Math.sqrt( fLightX2 + fLightY2 );
		fCosProj	= fLightX / ( float ) Math.sqrt( fLightX2 + fLightY2 );
	
		/* Rotation matrix:
		 * [ x y ] * [ cos sin   = [ z t ]
		 *            -sin cos ]
		 *
		 * z = ( x*cos - y*sin ), t = ( x*sin + y*cos )
		 */
	
		/* Compute displacement required on the image space to make it fit inside
		 * the shadow space after rotation.
		 */
		if ( ( fSinProj >= 0 ) && ( fCosProj >= 0 ) )
		{
			fWTemp	= - ( ( ( float ) vectorWidth * fCosProj ) * fCosProj ) + vectorWidth;
			fLTemp	= - ( ( ( float ) vectorWidth * fCosProj ) * fSinProj );
		}
		else if ( ( fSinProj <= 0 ) && ( fCosProj >= 0 ) )
		{
			fWTemp	= + ( ( ( float ) vectorLength * fCosProj ) * fSinProj ); 
			fLTemp	= - ( ( ( float ) vectorLength * fCosProj ) * fCosProj )
						+ vectorLength;
		}
		else if ( ( fSinProj <= 0 ) && ( fCosProj <= 0 ) )
		{
			fWTemp	= + ( ( ( float ) vectorWidth * fCosProj ) * fCosProj );
			fLTemp	= + ( ( ( float ) vectorWidth * fCosProj ) * fSinProj )
						+ vectorLength;
		}
		else
		{
			fWTemp	= - ( ( ( float ) vectorLength * fCosProj ) * fSinProj )
						+ vectorWidth; 
			fLTemp	= + ( ( ( float ) vectorLength * fCosProj ) * fCosProj );
		}
	
		/* Process shadow data from the shadow space perspective. This way
		 * the shadow casts are always parallel to the x axis, starting at
		 * top-left and going bottom-right.
		 */
		ixShadow		= 0;
		fLength		= ( float ) vectorLength;
		fWidth		= ( float ) vectorWidth;
	
		vtShadowSpace[ 0 ].fHeight	= 0;
	
		for( iLShadow = 0; iLShadow < ( int ) iSWidth; iLShadow++ )
		{
			/* W and L are computed based on the rotation matrix. Instead of
			 * resetting them every loop step, we just add the differences
			 * and eventually reset them with their values at line start.
			 * With this method we avoid using multiplications within the
			 * n2 loop.
			 */
			fW		= fWTemp;
			fL		= fLTemp;
			fWTemp	-=	fSinProj;
			fLTemp	+=	fCosProj;
	
			for( iWShadow = 0; iWShadow < ( int ) iSWidth; iWShadow++, ixShadow++ )
			{
				fW	+= fCosProj;
				fL	+= fSinProj;
	
				/* Reset and continue if we're outside the image space */
				if ( ( fW < 0 ) || ( fL < 0 ) 
					|| ( fW >= fWidth ) || ( fL >= fLength ) )
				{
					if ( ixShadow + 1 < iSWidth2 )
					{
						vtShadowSpace[ ixShadow + 1 ].fHeight	= 0;
					}
					continue;
				}
	
				iW			= ( int ) fW;
				iL			= ( int ) fL;
				ixHeight	= iW + iL * vectorWidth;
	
				fSHeight	= vtShadowSpace[ ixShadow ].fHeight;
				iSHeight	= ( int ) fSHeight;
	
				/* Check if this point AND his neighbors are under the shadow */
				if ( iSHeight > ( ( iFlags & CHECK_NEIGHBORS ) > 0 ? 
									vtMax[ ixHeight ] : vtHeight[ ixHeight ] ) )
				{
					/* Bit shift is faster than setting to zero */
					if ( ( iFlags & SET_NEIGHBORS ) > 0 )
					{
						if ( iW > 0 )
						{
							vtLight[ ixHeight - 1 ]			>>= 1;
						}
	
						if ( iL < ( int ) vectorLength - 1 )
						{
							vtLight[ ixHeight + vectorWidth ]	>>= 1;
						}
	
						if ( iW < ( int ) vectorWidth - 1 )
						{
							vtLight[ ixHeight + 1 ]			>>= 1;
						}
	
						if ( iL > 0 )
						{
							vtLight[ ixHeight - vectorWidth ]	>>= 1;
						}
					}
	
					vtLight[ ixHeight ]				>>= 8;
				}
				else if ( iSHeight < vtHeight[ ixHeight ] )
				{
					/* If, in the other hand, the shadow is lower than this point,
					 * it means that this point should override the shadow caster.
					 */
					vtShadowSpace[ ixShadow ].iCasterIx	= ixHeight;
	
					iSHeight								= vtHeight[ ixHeight ];
					fSHeight								= ( float ) vtHeight[ ixHeight ];
					vtShadowSpace[ ixShadow ].fHeight		= fSHeight;
				}
	
				/* Process shadow only if it has some height or 
				 * if its not the last item of a line. Otherwise,
				 * reset next shadow item.
				 */
				if ( ( iWShadow >= ( int ) iSWidth ) || ( iSHeight == 0 ) )
				{
					if ( ixShadow + 1 < iSWidth2 )
					{
						vtShadowSpace[ ixShadow + 1 ].fHeight	= 0;
					}
				}
				else
				{
					/* Process next shadow point */
					fSHeight	-= fTan;
	
					if ( fSHeight < 0 )
					{
						fSHeight	= 0;
					}
	
					vtShadowSpace[ ixShadow + 1 ].fHeight		= fSHeight;
	
					vtShadowSpace[ ixShadow + 1 ].iCasterIx	= 
						vtShadowSpace[ ixShadow ].iCasterIx;
				}
			}
		}
	}
	
}