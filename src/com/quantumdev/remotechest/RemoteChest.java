package com.quantumdev.remotechest;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RemoteChest extends JavaPlugin {

	ChestListener chestListener;
	
	static String mainDirectory = "plugins" + File.separator + "RemoteChest";
	private FileConfiguration config = null;
	private File configFile = null;	
	
	public Connection con;
	public Statement st;
	
	private String host;
	private String username;
	private String password;
	
	@Override
	public void onEnable() {
		loadConfig();
		setupMySQL();		
		chestListener  = new ChestListener(this);
		getServer().getPluginManager().registerEvents(chestListener, this);
	}
	
	@Override
	public void onDisable() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadConfig() {
		if (configFile == null)
			configFile = new File(mainDirectory + File.separator + "config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		
		InputStream defaultStream = this.getResource("config.yml");
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultStream);
			config.setDefaults(defaultConfig);
		}
		host = "jdbc:mysql://" + config.getString("host") + "/" + config.getString("database");
		username = config.getString("username");
		password = config.getString("password");
	}
	
	public void setupMySQL() {
			try {
				con = DriverManager.getConnection(host, username, password);
				st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);	
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
}
