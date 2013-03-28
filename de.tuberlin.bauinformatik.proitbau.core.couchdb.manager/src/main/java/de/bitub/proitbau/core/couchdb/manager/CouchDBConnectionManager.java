package de.bitub.proitbau.core.couchdb.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbInfo;
import org.ektorp.DbPath;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.ObjectMapperFactory;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;

public class CouchDBConnectionManager implements iCouchDBConnectionManager {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(CouchDBConnectionManager.class);
	{
		CouchDBConnectionManager.logger.setLevel(Level.INFO);
	}
	
	// @formatter:off
	private String host = "localhost";
	private String protocol = "http";
	private int port = 5984;
	private URL url;
	private String username = "";
	private String password = "";
	private boolean cacheEnable = true;
	private int connectionTimeout = 10000;
	private int maxCacheEntries = 1000;
	private int maxConnections = 1;
	private int maxObjectSizeBytes = 8192; 
	private int socketTimeout = 100000;
	private boolean sslEnable = false;
	private int revisonLimit = 2;
	private CouchDbConnector currentCouchDbConnector;
	private CouchDbInstance couchDbInstance;
	private ObjectMapperFactory objectMapperFactory;
	private HttpClient httpClient;
	Map<String, CouchDbConnector> connectorRegistry = new HashMap<String,CouchDbConnector>();
	// @formatter:on
	
	@Override
	public CouchDbConnector getCouchDbConnector(final String databaseName) {
		Preconditions.checkNotNull(databaseName, "Database name is null!");
		Preconditions.checkArgument(databaseName.length() > 0, "Given databse name is empty!");
		return this.getCouchDbConnector(databaseName, true);
	}
	
	@Override
	public CouchDbConnector getCouchDbConnector(final String databaseName, final boolean createIfNotExists) {
		Preconditions.checkNotNull(databaseName, "Database name is null!");
		Preconditions.checkArgument(databaseName.length() > 0, "Given databse name is empty!");
		Preconditions.checkNotNull(this.objectMapperFactory, "Object mapper factory can't be null!");
		
		CouchDbConnector couchDbConnector = this.connectorRegistry.get(databaseName);
		if (couchDbConnector == null) {
			
			// Construct new URL
			try {
				this.url = new URL(this.protocol, this.host, this.port, "");
			}
			catch (final MalformedURLException e) {
				e.printStackTrace();
			}
			
			// @formatter:off
			this.httpClient = new StdHttpClient.Builder()
				.url(this.url)
				.host(this.host)
				.port(this.port)
				.username(this.username)
				.username(this.password)
				.enableSSL(this.sslEnable)
				.caching(this.cacheEnable)
				.connectionTimeout(this.connectionTimeout)
				.socketTimeout(this.socketTimeout)
				.maxConnections(this.maxConnections)
				.maxCacheEntries(this.maxCacheEntries)
				.maxObjectSizeBytes(this.maxObjectSizeBytes)
			.build();
			// @formatter:on
			
			this.couchDbInstance = new StdCouchDbInstance(this.httpClient);
			
			couchDbConnector = new StdCouchDbConnector(databaseName, this.couchDbInstance, this.objectMapperFactory);
			
			if (createIfNotExists) {
				couchDbConnector.createDatabaseIfNotExists();
			}
			
			// Set revision limit only after the database was created
			couchDbConnector.setRevisionLimit(this.revisonLimit);
			
			this.connectorRegistry.put(databaseName, couchDbConnector);
			
		}
		this.currentCouchDbConnector = couchDbConnector;
		return couchDbConnector;
	}
	
	@Override
	public DbInfo obtainDatabaseInfo(final String databaseName) {
		final CouchDbConnector couchDbConnector = this.connectorRegistry.get(databaseName);
		Preconditions.checkNotNull(couchDbConnector, "Database connector is null!");
		return couchDbConnector.getDbInfo();
	}
	
	@Override
	public List<String> getAllDatabases() {
		Preconditions.checkNotNull(this.couchDbInstance, "Database instance is null!");
		return this.couchDbInstance.getAllDatabases();
	}
	
	@Override
	public boolean checkIfDbExists(final String databaseName) {
		Preconditions.checkNotNull(databaseName, "Database name is null!");
		Preconditions.checkArgument(databaseName.length() > 0, "Given databse name is empty!");
		Preconditions.checkNotNull(this.couchDbInstance, "Database instance is null!");
		return this.couchDbInstance.checkIfDbExists(new DbPath(databaseName));
	}
	
	@Override
	public void createDatabase(final String databaseName) {
		Preconditions.checkNotNull(databaseName, "Database name is null!");
		Preconditions.checkArgument(databaseName.length() > 0, "Given databse name is empty!");
		Preconditions.checkNotNull(this.couchDbInstance, "Database instance is null!");
		this.couchDbInstance.createDatabase(new DbPath(databaseName));
	}
	
	@Override
	public void deleteDatabase(final String databaseName) {
		Preconditions.checkNotNull(databaseName, "Database name is null!");
		Preconditions.checkArgument(databaseName.length() > 0, "Given databse name is empty!");
		Preconditions.checkNotNull(this.couchDbInstance, "Database instance is null!");
		if (this.couchDbInstance.checkIfDbExists(DbPath.fromString(databaseName))) {
			this.couchDbInstance.deleteDatabase(new DbPath(databaseName).getPath());
		}
		else {
			CouchDBConnectionManager.logger.info("Database " + databaseName + " doesn't exist!");
		}
	}
	
	@Override
	public String getHost() {
		return this.host;
	}
	
	@Override
	public String getProtocol() {
		return this.protocol;
	}
	
	@Override
	public int getPort() {
		return this.port;
	}
	
	@Override
	public URL getUrl() {
		return this.url;
	}
	
	@Override
	public String getUsername() {
		return this.username;
	}
	
	@Override
	public String getPassword() {
		return this.password;
	}
	
	@Override
	public boolean isCacheEnable() {
		return this.cacheEnable;
	}
	
	@Override
	public int getConnectionTimeout() {
		return this.connectionTimeout;
	}
	
	@Override
	public int getMaxCacheEntries() {
		return this.maxCacheEntries;
	}
	
	@Override
	public int getMaxConnections() {
		return this.maxConnections;
	}
	
	@Override
	public int getMaxObjectSizeBytes() {
		return this.maxObjectSizeBytes;
	}
	
	@Override
	public int getSocketTimeout() {
		return this.socketTimeout;
	}
	
	@Override
	public boolean isSslEnable() {
		return this.sslEnable;
	}
	
	@Override
	public int getRevisionLimit() {
		return this.revisonLimit;
	}
	
	@Override
	public CouchDbConnector getCurrentCouchDbConnector() {
		return this.currentCouchDbConnector;
	}
	
	@Override
	public CouchDbInstance getDbInstance() {
		return this.couchDbInstance;
	}
	
	@Override
	public ObjectMapperFactory getObjectMapperFactory() {
		return this.objectMapperFactory;
	}
	
	@Override
	public HttpClient getHttpClient() {
		return this.httpClient;
	}
	
	@Override
	public void setHost(final String host) {
		Preconditions.checkNotNull(host, "Host name can not contain null value!");
		Preconditions.checkArgument(host.length() > 6, "Host name length must be greater than 6!");
		this.host = host;
	}
	
	@Override
	public void setProtocol(final String protocol) {
		Preconditions.checkNotNull(protocol, "Protocol can not contain null value!");
		Preconditions.checkArgument(protocol.length() > 2, "Protocol length must be greater than 2!");
		Preconditions.checkArgument(protocol.length() < 6, "Protocol length must be less than 6!");
		this.protocol = protocol;
	}
	
	@Override
	public void setPort(final int port) {
		Preconditions.checkArgument(port > 1023, "Port number must be greater than 1023!");
		Preconditions.checkArgument(port < 49152, "Port number must be less than 49152!");
		this.port = port;
	}
	
	@Override
	public void setUsername(final String username) {
		Preconditions.checkNotNull(username, "User name can not contain null value!");
		Preconditions.checkArgument(username.length() > 0, "User name length must be greater than 0!");
		this.username = username;
	}
	
	@Override
	public void setPassword(final String password) {
		Preconditions.checkNotNull(password, "User password can not contain null value!");
		Preconditions.checkArgument(password.length() > 0, "User password length must be greater than 0!");
		this.password = password;
	}
	
	@Override
	public void setCacheEnable(final boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
	}
	
	@Override
	public void setConnectionTimeout(final int connectionTimeout) {
		Preconditions.checkArgument(this.connectionTimeout > 0, "Connection timeout must be greater than 0!");
		Preconditions.checkArgument(this.connectionTimeout < 10001, "Connection timeout must be less than 10001!");
		this.connectionTimeout = connectionTimeout;
	}
	
	@Override
	public void setMaxCacheEntries(final int maxCacheEntries) {
		Preconditions.checkArgument(this.maxCacheEntries > -1, "Maximal number of cache entries must be greater than -1!");
		Preconditions.checkArgument(this.maxCacheEntries < 100000,
			"Maximal number of cache entries must be less than 100000!");
		this.maxCacheEntries = maxCacheEntries;
	}
	
	@Override
	public void setMaxConnections(final int maxConnections) {
		Preconditions.checkArgument(this.maxConnections > 0, "Maximal number of connections must be greater than 1!");
		Preconditions.checkArgument(this.maxConnections < 31, "Maximal number of connections must be less than 31!");
		this.maxConnections = maxConnections;
	}
	
	@Override
	public void setMaxObjectSizeBytes(final int maxObjectSizeBytes) {
		Preconditions.checkArgument(maxObjectSizeBytes > 1023, "Maximal object size in bytes must be greater than 1023!");
		Preconditions.checkArgument(maxObjectSizeBytes < 16385, "Maximal object size in bytes must be less than 16385!");
		this.maxObjectSizeBytes = maxObjectSizeBytes;
	}
	
	@Override
	public void setSocketTimeout(final int socketTimeout) {
		Preconditions.checkArgument(this.port > 0, "Socket timeout must be greater than 0!");
		Preconditions.checkArgument(this.port < 10001, "Socket timeout must be less than 10001!");
		this.socketTimeout = socketTimeout;
	}
	
	@Override
	public void setSslEnable(final boolean sslEnable) {
		this.sslEnable = sslEnable;
	}
	
	@Override
	public void setRevisionLimit(final int revisonLimit) {
		Preconditions.checkArgument(this.revisonLimit > 0, "Revision limit number must be greater than 0!");
		Preconditions.checkArgument(this.revisonLimit < 1001, "Revision limit number must be less than 1001!");
		this.revisonLimit = revisonLimit;
	}
	
	@Override
	public void setObjectMapperFactory(final ObjectMapperFactory objectMapperFactory) {
		Preconditions.checkNotNull(objectMapperFactory, "Object mapper factory can not contain null value!");
		this.objectMapperFactory = objectMapperFactory;
	}
	
}
