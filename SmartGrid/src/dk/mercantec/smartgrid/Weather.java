package dk.mercantec.smartgrid;

//Weather Class content weather station in denmark
public class Weather 
{
	public String icaoCode;
	public double lat;
	public double lng;
	public String name;
	
	Weather(String icao, double lat, double lng, String name)
	{
		this.icaoCode = icao;
		this.lat = lat;
		this.lng = lng;
		this.name = name;
	}
	
	public double getLat()
	{
		return lat;
	}
	
	public double getLng()
	{
		return lng;
	}
	
	public String getIcaoCode()
	{
		return icaoCode;
	}
	
	public String getName()
	{
		return name;
	}
	
}
