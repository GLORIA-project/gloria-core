package eu.gloria.gs.services.api.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

public class SHA1 {

	@SuppressWarnings("restriction")
	public static String encode(String input) {
		MessageDigest mDigest = null;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
		}

		byte[] bytes;
		try {
			bytes = input.getBytes(("UTF-8"));
			mDigest.update(bytes);
			byte[] digest = mDigest.digest();
			String hash = (new BASE64Encoder()).encode(digest);
			return hash;
		} catch (UnsupportedEncodingException e) {
		}

		return null;
	}
}
