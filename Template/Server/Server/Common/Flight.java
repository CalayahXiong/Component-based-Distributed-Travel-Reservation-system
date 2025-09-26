// -------------------------------
// Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

public class Flight extends ReservableItem
{
	public Flight(String flightNum, int flightSeats, int flightPrice)
	{
		super(flightNum, flightSeats, flightPrice);
	}

	public String getKey()
	{
		return getLocation();
	}

	public static String getKey(String flightNum)
	{
		String s = "flight-" + flightNum;
		return s.toLowerCase();
	}
}

