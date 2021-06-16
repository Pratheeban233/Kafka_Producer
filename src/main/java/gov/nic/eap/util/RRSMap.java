package gov.nic.eap.util;

import java.util.HashMap;

public class RRSMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 4018063427862508300L;

	/*
	 *  The Fatal Error on Duplicate entry code will never reach following condition.
	 *  snake YAML itself throws the duplicate error and exit.
	 *  This method just displays the REST End points configured at the console. 
	 */
	@Override
	public V put(K key, V value) {
		if (this.containsKey(key)) {
			System.out.println("FATAL:: " + key.toString() + " REST End Point already defined. Duplicate Found.");
			System.exit(1);
			return null;
		} else {
			System.out.println("Info:: " + key.toString() + " REST End Point configured --> " + value.toString());
			return super.put(key, value);
		}
	}
}
