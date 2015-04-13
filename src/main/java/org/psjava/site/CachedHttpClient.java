package org.psjava.site;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CachedHttpClient {

	private static final HashMap<String, String> CACHE = new HashMap<String, String>();

	public synchronized static String getBody(String url) throws IOException {
		if (!CACHE.containsKey(url))
			CACHE.put(url, receiveBody(url));
		return CACHE.get(url);
	}

	private static String receiveBody(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "pure java");
		connection.setDoOutput(false);
		int code = connection.getResponseCode();
		String body = readBody(connection);
		if (code == HttpURLConnection.HTTP_OK) {
			return body;
		} else {
			throw new RuntimeException(code + "\n" + body);
		}
	}

	private static String readBody(HttpURLConnection connection) throws IOException {
		try(InputStream is = connection.getInputStream()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while (true) {
				int read = is.read();
				if (read == -1)
					break;
				bos.write(read);
			}
			return new String(bos.toByteArray(), "UTF-8");
		}
	}
}
