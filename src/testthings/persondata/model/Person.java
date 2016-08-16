/* This class is the POJO representation of the data about a person given by the
 * web service. */

package testthings.persondata.model;

public class Person 
{
	public String gender;
	public Name name;
	public Location location;
	public String email;
	public Login login;
	public long registered;
	public long dob;
	public String phone;
	public String cell;
	public ID id;
	public Picture picture;
	public String nat; // nationality, e.g. "US" means American
	
	@Override
	public String
	toString()
	{
		return name.first + " " + name.last;
	}
}
