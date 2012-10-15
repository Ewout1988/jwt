package eu.webtoolkit.jwt;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.List;

import net.n3.nanoxml.XMLElement;

public class Utils {
	/** Computes an MD5 hash.
	 *
	 * This utility function computes an MD5 hash, and returns the hash value.
	 */
	public static byte[] md5(String msg) {
		try {
			MessageDigest d = MessageDigest.getInstance("MD5");
			return d.digest(msg.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Performs url encoding (aka percentage encoding).
	 *
	 * This utility function percent encodes a text so that it can be
	 * embodied verbatim in a URL (e.g. as a fragment).
	 *
	 * @see #urlDecode(String scope)
	 */
	public static String urlEncode(String scope) {
		return DomElement.urlEncodeS(scope);
	}
	
	/** Performs url decoding.
	 *
	 * This utility function percent encodes a text so that it can be
	 * embodied verbatim in a URL (e.g. as a fragment).
	 *
	 * @see #urlEncode(String scope)
	 */
	public static String urlDecode(String scope) {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < scope.length(); ++i) {
			char c = scope.charAt(i);

			if (c == '+') {
				result.append(' ');
			} else if (c == '%' && i + 2 < scope.length()) {
				int start = i + 1;
				String h = scope.substring(start, start + 2);
				try {
					long hval = Long.parseLong(h, 16);
					result.append("" + (byte) hval);
				} catch (NumberFormatException nfe) {
					result.append(c);
				}
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}
	
	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(String s) {
		return base64Encode(s.getBytes());
	}
	
	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.encodeBytes(bytes);
	}
	
	/** Performs Base64-decoding of data.
	 * 
	 * @throws IOException 
	 */
	public static byte[] base64Decode(String s) throws IOException {
		return base64Decode(s.getBytes("US-ASCII"));
	}
	
	/** Performs Base64-decoding of data.
	 * 
	 * @throws IOException 
	 */
	public static byte[] base64Decode(byte[] bytes) throws IOException {
		return Base64.decode(bytes);
	}
	
	/** An enumeration for HTML encoding flags.
	 */
	public enum HtmlEncodingFlag
	{
	  /** Encode new-lines as line breaks (&lt;br&gt;)
	   */
	  EncodeNewLines
	}
	
	/** Performs HTML encoding of text.
	 *
	 * This utility function escapes characters so that the text can
	 * be embodied verbatim in a HTML text block.
	 */
	public static String htmlEncode(String text, EnumSet<HtmlEncodingFlag> flags)
	{
	  return WWebWidget.escapeText(text, flags.contains(HtmlEncodingFlag.EncodeNewLines) ? true : false);
	}
	
	/**
	 * Performs HTML encoding of text.
	 * <p>
	 * Calls {@link Utils#htmlEncode(String text, EnumSet flags)
	 * Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class))}
	 */
	public static String htmlEncode(String text) {
		return Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class));
	}
	
	/** Performs HTML encoding of text.
	 *
	 * This utility function escapes characters so that the text can
	 * be embodied verbatim in a HTML text block.
	 *
	 * By default, newlines are ignored. By passing the {@link HtmlEncodingFlag#EncodeNewLines}
	 * flag, these may be encoded as line breaks (&lt;br&gt;).
	 */
	public static String htmlEncode(WString text, EnumSet<HtmlEncodingFlag> flags) 
	{
		return htmlEncode(text.toString(), flags);
	}
	
	/**
	 * Performs HTML encoding of text.
	 * <p>
	 * Calls {@link Utils#htmlEncode(WString text, EnumSet flags)
	 * Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class))}
	 */
	public static String htmlEncode(WString text) {
		return Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class));
	}
	
	/** Remove tags/attributes from text that are not passive.
	 *
	 * This removes tags and attributes from XHTML-formatted text that do
	 * not simply display something but may trigger scripting, and could
	 * have been injected by a malicious user for Cross-Site Scripting
	 * (XSS).
	 *
	 * This method is used by the library to sanitize XHTML-formatted text
	 * set in {@link WText}, but it may also be useful outside the library to
	 * sanitize user content when directly using JavaScript.
	 *
	 * Modifies the text if needed. When the text is not proper XML,
	 * returns false.
	 */
	public static boolean removeScript(CharSequence text) {
		return WWebWidget.removeScript(text);
	}

	static int memcmp(List<Integer> header, String string, int size) {
		for (int i = 0; i < size; i++) {
			if (header.get(i) != string.charAt(i))
				return 1;
		}
		return 0;
	}

	private static boolean isWhiteSpace(char c, String whiteSpaces) {
		for (int i = 0; i < whiteSpaces.length(); i++) { 
			if (c == whiteSpaces.charAt(i))
				return true;
		}
		return false;
	}
	
	public static String strip(String s, String whiteSpaces) {
		int start = -1;
		int end = -1;

		for (int i = 0; i < s.length(); i++) {
			if (!isWhiteSpace(s.charAt(i), whiteSpaces)) {
				start = i;
				break;
			}
		}
		
		if (start == -1) 
			return "";
		else
			s = s.substring(start);
		
		for (int i = s.length() - 1; i >= 0; i--) {
			if (!isWhiteSpace(s.charAt(i), whiteSpaces)) {
				end = i + 1;
				break;
			}
		}
		
		return s.substring(0, end);
	}

	public static void assignFontMatch(FontSupport.FontMatch fm1, FontSupport.FontMatch fm2) {
		fm1.setFileName(fm2.getFileName());
		fm1.setQuality(fm2.getQuality());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void copyList(List source, List destination) {
		destination.clear();
		for (Object o : source) {
			destination.add(o);
		}
	}

	public static int hexToInt(String s) {
		return Integer.parseInt(s, 16);
	}
}
