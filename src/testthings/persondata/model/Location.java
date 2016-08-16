package testthings.persondata.model;

public class Location 
{
	String street;
	String city;
	String state;
	int postcode;
	
	@Override
	public String toString()
	{
		return street + " " + city + ", " + state + " " + postcode;
	}
}
