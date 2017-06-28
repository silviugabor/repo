package repo.crawler.service.observer.token;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import repo.crawler.service.observer.ServiceObserver;


public class TokenChangeServiceObserver implements ServiceObserver{
	
	private static final Logger LOG = LoggerFactory.getLogger(TokenChangeServiceObserver.class);
	
	private List<String> tokenList;
	private int tokenIndex;
	private String tokenFilePath;
	
	public TokenChangeServiceObserver(String tokenFilePath) {
		this.tokenFilePath = tokenFilePath;
	}
	
	@PostConstruct
	private void initTokenFromFileResource(){
		tokenIndex = 0;
		try {
			String rawTokens = IOUtils.toString(new FileInputStream(new ClassPathResource(tokenFilePath).getFile()), Charset.defaultCharset());
			/*tokenList = Arrays.asList(rawProxyList.split("\\r?\\n")).stream().map(entry -> {
				List<String> proxyElementList = Arrays.asList(entry.split(":"));
				return new Proxy(proxyElementList.get(0), proxyElementList.get(1));
			}).collect(Collectors.toList());*/
			tokenList = Arrays.asList(rawTokens.split("\\r?\\n"));
		}catch(IOException e){
			LOG.info("Error opening tokens resource path at " + tokenFilePath, e);
		}
	}
	
	@Override
	public String updateToken() {
		return tokenList.get((++tokenIndex) % tokenList.size());
	}
}
