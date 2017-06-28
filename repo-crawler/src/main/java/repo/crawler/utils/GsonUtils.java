package repo.crawler.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.gson.Gson;

public class GsonUtils {

	private static Gson gson;

	static{
		if (gson == null)
			gson = new Gson();
	}
	
	private GsonUtils(){}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static <T> T readObjectFromJsonUrl(String stringUrl, Class<T> clazz) throws IOException {
		URL url = new URL(stringUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		
		int code = connection.getResponseCode();
		if (code == 200) {
			InputStream is = connection.getInputStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = readAll(rd);
				return gson.fromJson(jsonText, clazz);
			} finally {
				is.close();
			}
		}
		else
			System.out.println("Code for " + stringUrl + " : " + code);
		return null;
	}
}
