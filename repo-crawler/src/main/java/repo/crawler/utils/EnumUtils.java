package repo.crawler.utils;

public class EnumUtils {
	/**
	 * A common method for all enums since they can't have another base class
	 * 
	 * @param <T>
	 *            Enum type
	 * @param c
	 *            enum type. All enums must be all caps.
	 * @param string
	 *            case insensitive
	 * @return corresponding enum, or null
	 */
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
		try{
			if (c != null && string != null)
				return Enum.valueOf(c, string.trim().toUpperCase());
		}
		catch(IllegalArgumentException e){
			return null;
		}
		return null;
	}
}
