package lightnshadow;

class Coord3D
{
	public float		x;
	public float		y;
	public float		z;
	
	public Coord3D()
	{
		init( 0, 0, 0 );
	}
	
	public Coord3D( float x, float y, float z )
	{
		init( x, y, z );
	}
	
	void init( float x, float y, float z )
	{
		this.x	= x;
		this.y	= y;
		this.z	= z;
	}
}
