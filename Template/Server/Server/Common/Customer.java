// -------------------------------
// Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import java.util.*;

public class Customer extends RMItem
{
	private int m_ID;
	private RMHashMap m_reservations;

	public Customer(int id) {
		super();
		m_reservations = new RMHashMap();
		m_ID = id;
	}

	public void setID(int id)
	{
		m_ID = id;
	}

	public int getID()
	{
		return m_ID;
	}

	public ReservedItem getReservedItem(String key) {
		ReservedItem item = (ReservedItem) m_reservations.get(key);
		if (item == null) {
			return null;
		}
		return (ReservedItem) item.clone();
	}


	public String getBill() {
		StringBuilder sb = new StringBuilder("Bill for customer " + m_ID + ": ");
		for (String key : m_reservations.keySet()) {
			ReservedItem item = (ReservedItem) m_reservations.get(key);
			sb.append(item.getCount())
					.append(" ")
					.append(item.getReservableItemKey())
					.append(" $")
					.append(item.getPrice())
					.append(" | ");
		}
		if (sb.length() > 0 && sb.lastIndexOf("|") == sb.length() - 2) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}


	public String toString() {
		String ret = "--- BEGIN CUSTOMER key='";
		ret += getKey() + "', id='" + getID() + "', reservations=>\n" + m_reservations.toString() + "\n";
		ret += "--- END CUSTOMER ---";
		return ret;
	}

	public static String getKey(int customerID) {
		String s = "customer-" + customerID;
		return s.toLowerCase();
	}

	public String getKey()
	{
		return Customer.getKey(getID());
	}

	public RMHashMap getReservations() {
		return (RMHashMap) m_reservations;
	}

	public Object clone() {
		Customer obj = (Customer)super.clone();
		obj.m_ID = m_ID;
		obj.m_reservations = (RMHashMap)m_reservations.clone();
		return obj;
	}

	//	public void cancelReservation(String key, String location, int price) {
//		ReservedItem reservedItem = getReservedItem(key);
//		if (reservedItem == null) {
//			return; // nothing to cancel
//		}
//
//		int count = reservedItem.getCount();
//		if (count <= 1) {
//			// remove reservation entirely
//			m_reservations.remove(key);
//		} else {
//			// decrease count
//			reservedItem.setCount(count - 1);
//			// keep last known price (optional: could update here)
//			reservedItem.setPrice(price);
//			m_reservations.put(reservedItem.getKey(), reservedItem);
//		}
//	}

}

