package me.rayentickler.bot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	public static Properties prop;
	static {
        try (InputStream input = PropertyReader.class.getClassLoader().getResourceAsStream("project.properties")) {
        	prop = new Properties();
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
	
	public static String getProperty(String key) {
		return prop.getProperty(key);
	}
	
}
