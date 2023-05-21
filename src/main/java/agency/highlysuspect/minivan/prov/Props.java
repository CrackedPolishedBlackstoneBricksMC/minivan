package agency.highlysuspect.minivan.prov;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;

public class Props {
	public Props() {
		this(new TreeMap<>());
	}
	
	public Props(Props clone) {
		this(clone.props);
	}
	
	public Props(SortedMap<String, String> props) {
		this.props = props;
	}
	
	private final SortedMap<String, String> props;
	
	public Props set(String key, String value) {
		props.put(key, value);
		return this;
	}
	
	public Props setAll(Props other) {
		props.putAll(other.props);
		return this;
	}
	
	public boolean has(String key) {
		return props.containsKey(key);
	}
	
	/**
	 * @return the empty string if the properties is empty,
	 *         or a string like "-ab4c9324" where the characters correspond to a hash of the map's contents
	 */
	public String suffix() {
		if(props.isEmpty()) return "";
		
		//obtain hasher
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException what) {
			throw new RuntimeException("Someone's been tampering with the universe!", what);
		}
		
		//pour data into the hasher
		digest.update((byte) props.size());
		props.forEach((k, v) -> {
			digest.update(k.getBytes(StandardCharsets.UTF_8));
			digest.update((byte) 0);
			digest.update(v.getBytes(StandardCharsets.UTF_8));
			digest.update((byte) 1);
		});
		
		//hex-ify first 4 bytes of the hash
		byte[] result = digest.digest();
		StringBuilder out = new StringBuilder(9);
		out.append('-');
		for(int i = 0; i < 4; i++) {
			int hi = (result[i] & 0xF0) >> 4;
			int lo = result[i] & 0x0F;
			out.append((char) ((hi < 10 ? '0' : 'W') + hi)); //'W' == 'a'-10
			out.append((char) ((lo < 10 ? '0' : 'W') + lo));
		}
		
		return out.toString();
	}
}
