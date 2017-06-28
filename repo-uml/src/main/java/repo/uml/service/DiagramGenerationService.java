package repo.uml.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantumldependency.cli.main.program.PlantUMLDependencyProgram;
import net.sourceforge.plantumldependency.commoncli.command.CommandLine;
import net.sourceforge.plantumldependency.commoncli.command.impl.CommandLineImpl;
import net.sourceforge.plantumldependency.commoncli.exception.CommandLineException;
import net.sourceforge.plantumldependency.commoncli.program.JavaProgram;
import net.sourceforge.plantumldependency.commoncli.program.execution.JavaProgramExecution;
import repo.crawler.dao.GitHubRepositoryDAO;
import repo.crawler.entities.github.GitHubRepository;
import repo.uml.exception.DiagramGenerationException;
import repo.uml.util.PlantUMLUtils;

@Component
public class DiagramGenerationService {
	
	@Autowired
	GitHubRepositoryDAO gitHubRepositoryDAO;

	private static final Logger LOG = LoggerFactory.getLogger(DiagramGenerationService.class);
	
	private static Pattern methodsPattern = Pattern.compile("(((public|private|protected|static|final|native|synchronized|abstract|transient)+\\s)+[\\$_\\w\\<\\>\\[\\] ,&]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*)\\{?[^\\}]*\\}?");
	private static Pattern variablesPattern = Pattern.compile(".*(private|public|protected|static|final|transient)+\\s+([\\$_\\w\\<\\>\\[\\] ,&]*)\\s+([\\$_\\w\\<\\>\\[\\] ,&]*);");
	private static Pattern packagePattern = Pattern.compile("package +([\\w. ]*?) *;");
	
	private static final String ZIP_REPOSITORY_CONTENT_URL_PATTERN = "https://api.github.com/repos/%s/%s/zipball";
	private static final String TEXT_DIAGRAM_FILE_NAME = "diagram.txt";
	
	private static final int BUFFER_SIZE = 4096;

	private byte[] getContentAsZIPArchive(String user, String repository) throws IOException {
		String contentURLString = String.format(ZIP_REPOSITORY_CONTENT_URL_PATTERN, user, repository);
		URL contentURL = new URL(contentURLString);
		byte[] zipContent = IOUtils.toByteArray(contentURL.openStream());
		return zipContent;
	}
	
	/**
     * Extracts a zip file specified by the {@param zipFileContent}
     * @param zipFileContent
     * @throws IOException
     */
	private String unzipFile(byte[] zipFileContent) throws IOException{
		String unzipppedContentPath = null;
		String destDirectory = new ClassPathResource("target/repos", DiagramGenerationService.class).getPath();
		File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipFileContent));
        ZipEntry entry = zipIn.getNextEntry();
        
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
            	
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
            	
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
                if (unzipppedContentPath == null)
                	unzipppedContentPath = dir.getAbsolutePath();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        return unzipppedContentPath;
	}
	
	/**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
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

	private String addAttributesToClass(String fullClassName, String stringContent, Set<String> attributes){
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

	private String getAttributes(String path) throws IOException{
		String generatedUMLContent = IOUtils.toString(Files.newBufferedReader(Paths.get(new ClassPathResource(path + File.separator + TEXT_DIAGRAM_FILE_NAME, DiagramGenerationService.class).getPath()), Charset.defaultCharset()));
		Path startPath = Paths.get(path);
		try (Stream<Path> paths = Files.walk(startPath)) {
			List<Path> pathList = paths.filter(filePath -> filePath.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
			for (Path file : pathList) {
				String classContent = IOUtils.toString(Files.newBufferedReader(file, Charset.defaultCharset()));
				Matcher m = packagePattern.matcher(classContent);
				if (m.find()) {
					String javaCompleteClassName = "class " + m.group(1) + "." + file.getFileName().toString().replaceAll(".java", "");
					String javaCompleteInterfaceName = "interface " + m.group(1) + "." + file.getFileName().toString().replaceAll(".java", "");
					if (generatedUMLContent.contains(javaCompleteClassName)) {
						Set<String> classMethods = getClassAttributes(classContent);
						generatedUMLContent = addAttributesToClass(javaCompleteClassName, generatedUMLContent, classMethods);
					}
					if (generatedUMLContent.contains(javaCompleteInterfaceName)){
						Set<String> classMethods = getClassAttributes(classContent);
						generatedUMLContent = addAttributesToClass(javaCompleteInterfaceName, generatedUMLContent, classMethods);
					}
				}
			}
		}
        return generatedUMLContent;
	}
	
    private byte[] generateDiagram(String repositoryPath, FileFormat fileFormat) throws CommandLineException, ParseException, IOException{
    	final CommandLine commandLineArguments = new CommandLineImpl(new String[]{"-o", new ClassPathResource(repositoryPath + File.separator + TEXT_DIAGRAM_FILE_NAME, DiagramGenerationService.class).getPath(), "-b", repositoryPath, "-dt", "abstract_classes,classes,implementations,interfaces,native_methods,extensions", "-dp", "^(?!java)(.+)$"});
		final JavaProgram plantUmlDependencyProgram = new PlantUMLDependencyProgram();
		final JavaProgramExecution plantUmlDependencyProgramExecution = plantUmlDependencyProgram.parseCommandLine(commandLineArguments);
		
		LOG.info("Started generating text diagram...");
		
		plantUmlDependencyProgramExecution.execute();
		String generatedUMLContent = getAttributes(repositoryPath);
		OutputStream os = new FileOutputStream(new File(new ClassPathResource(repositoryPath + File.separator + TEXT_DIAGRAM_FILE_NAME, DiagramGenerationService.class).getPath()));
		IOUtils.write(generatedUMLContent, os, Charset.defaultCharset());
		IOUtils.closeQuietly(os);
		
		LOG.info("Finished generating text diagram");
		
		File source = new File(new ClassPathResource(repositoryPath + File.separator + TEXT_DIAGRAM_FILE_NAME, DiagramGenerationService.class).getPath());
		SourceFileReader reader = new SourceFileReader(source);
		reader.setFileFormatOption(new FileFormatOption(fileFormat));
		LOG.info("Started generating images...");
		List<GeneratedImage> generatedImages = reader.getGeneratedImages();
		if (generatedImages != null && generatedImages.size() > 0)
		{
			LOG.info("Finished generating images");
			GeneratedImage firstGeneratedImage = generatedImages.get(0);
			return IOUtils.toByteArray(new FileInputStream(firstGeneratedImage.getPngFile()));
		}
		else
			return null;
		
    }
    
	public byte[] generateClassDiagram(Long repositoryId, FileFormat fileFormat) throws DiagramGenerationException, IOException, CommandLineException, ParseException {
		GitHubRepository gitHubRepository = gitHubRepositoryDAO.findOne(repositoryId);
		Assert.notNull(gitHubRepository, "Could not find repository with ID '" + repositoryId + "'");
		String[] pathSegments = gitHubRepository.getRepositoryLink().split("/");
		if (pathSegments.length > 2) {
			String repository = pathSegments[pathSegments.length - 1];
			String user = pathSegments[pathSegments.length - 2];
			byte[] zipContentByteArray = getContentAsZIPArchive(user, repository);
			String repositoryPath = unzipFile(zipContentByteArray);
			return generateDiagram(repositoryPath, fileFormat);
		} else
			throw new DiagramGenerationException("Repository link" + gitHubRepository.getRepositoryLink() + "is not valid.");
	}
	
}
