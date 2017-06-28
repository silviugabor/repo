package repo.uml.util;

public final class PlantUMLUtils {
	
	public static String replaceModifiers(String syntaxLine){
		return replaceOtherModifiers(replaceVisibilityModifiers(syntaxLine));
	}
	
	private static String replaceVisibilityModifiers(String syntaxLine){
		if (syntaxLine.contains("private"))
			return syntaxLine.replaceFirst("private", "-");
		else if (syntaxLine.contains("public"))
			return syntaxLine.replaceFirst("public", "+");
		else if (syntaxLine.contains("protected"))
			return syntaxLine.replaceFirst("protected", "#");
		else
			return "~".concat(syntaxLine);
	}
	
	private static String replaceOtherModifiers(String syntaxLine){
		if (syntaxLine.contains("abstract"))
			syntaxLine = syntaxLine.replaceFirst("abstract", "\\{abstract\\}");
		if (syntaxLine.contains("static"))
			syntaxLine = syntaxLine.replaceFirst("static", "\\{static\\}");
		return syntaxLine;
	}
}
