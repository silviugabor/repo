package repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantumldependency.cli.main.program.PlantUMLDependencyProgram;
import net.sourceforge.plantumldependency.commoncli.command.CommandLine;
import net.sourceforge.plantumldependency.commoncli.command.impl.CommandLineImpl;
import net.sourceforge.plantumldependency.commoncli.program.JavaProgram;
import net.sourceforge.plantumldependency.commoncli.program.execution.JavaProgramExecution;
import repo.uml.service.DiagramGenerationService;
import repo.uml.util.PlantUMLUtils;

//@SpringBootApplication
public class Main implements CommandLineRunner{
	
	@Autowired
	public DiagramGenerationService diagramGenerationService;
	
	static Pattern methodsPattern = Pattern.compile("(((public|private|protected|static|final|native|synchronized|abstract|transient)+\\s)+[\\$_\\w\\<\\>\\[\\] ,&]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*)\\{?[^\\}]*\\}?");
	static Pattern variablesPattern = Pattern.compile(".*(private|public|protected|static|final|transient)+\\s+([\\$_\\w\\<\\>\\[\\] ,&]*)\\s+([\\$_\\w\\<\\>\\[\\]]*);");
	static Pattern packagePattern = Pattern.compile("package +([\\w. ]*?) *;");
	static String generatedUMLContent;
	public static void main (String[] args){
		
		SpringApplication.run(Main.class, args);
		
	}
	
	private String getAttributes(String path) throws IOException{
		generatedUMLContent = IOUtils.toString(Files.newBufferedReader(Paths.get("test.txt"),Charset.defaultCharset()));
		Path startPath = Paths.get(path);
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() { 
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
            	if (file.getFileName().toString().endsWith(".java")){
            		String classContent = IOUtils.toString(Files.newBufferedReader(file, Charset.defaultCharset()));
            		Matcher m = packagePattern.matcher(classContent);
            		if (m.find())
            		{
            			String javaCompleteClassName = m.group(1) + "." + file.getFileName().toString().replaceAll(".java", "");
            			if (generatedUMLContent.contains(javaCompleteClassName))
            			{
            				Set<String> classMethods = getClassAttributes(classContent);
            				generatedUMLContent = addAttributesToClass(javaCompleteClassName, generatedUMLContent, classMethods);
            			}
            		}
            	}
                return FileVisitResult.CONTINUE;
            }
        });
        return generatedUMLContent;
	}
	
	private Set<String> getClassAttributes(String classContent) {
		Set<String> classMethods = new LinkedHashSet<>();
		classContent = classContent.trim();
		Matcher m = methodsPattern.matcher(classContent);
		while (m.find()){
			String methodSignature = PlantUMLUtils.replaceModifiers(m.group(1));
			classMethods.add(methodSignature);
		}
		classContent = m.replaceAll("");
		m = variablesPattern.matcher(classContent);
		while (m.find()){
			String variable = PlantUMLUtils.replaceModifiers(m.group(0));
			classMethods.add(variable);
		}
		return classMethods;
	}

	private static String addAttributesToClass(String fullClassName, String stringContent, Set<String> attributes){
		if (attributes != null){
			StringBuilder sb = new StringBuilder(stringContent);
			int offset = sb.indexOf(fullClassName);
			sb.insert(offset + fullClassName.length(), " {\n %s \n}");
			
			StringBuilder methods = new StringBuilder();
			attributes.forEach(method->methods.append(method + "\n"));
			
			return String.format(sb.toString(), methods.toString());
		}
		return null;
	}

	@Override
	public void run(String... arg0) throws Exception {
//		final CommandLine commandLineArguments = new CommandLineImpl(new String[]{"-o", "test.txt", "-b", "D:\\work\\sts\\licenta\\repo"});
//		final JavaProgram plantUmlDependencyProgram = new PlantUMLDependencyProgram();
//		final JavaProgramExecution plantUmlDependencyProgramExecution = plantUmlDependencyProgram.parseCommandLine(commandLineArguments);
//		plantUmlDependencyProgramExecution.execute();
////		File source = new File("test.txt");
////		SourceFileReader reader = new SourceFileReader(source);
////		reader.setFileFormatOption(new FileFormatOption(FileFormat.SVG));
////		List<GeneratedImage> generatedImages = reader.getGeneratedImages();
//		
//		
//		generatedUMLContent = getAttributes("D:\\work\\sts\\licenta\\repo");
//		OutputStream os = new FileOutputStream(new File("test.txt"));
//		IOUtils.write(generatedUMLContent, os);
//		IOUtils.closeQuietly(os);
//		
//		File source = new File("test.txt");
//		SourceFileReader reader = new SourceFileReader(source);
//		reader.setFileFormatOption(new FileFormatOption(FileFormat.SVG));
//		List<GeneratedImage> generatedImages = reader.getGeneratedImages();
//		generatedImages.forEach((image)->{
//			System.out.println(image.getPngFile());
//		});

		diagramGenerationService.generateClassDiagram(8L, FileFormat.SVG);
	}
}
