package de.bitub.proitbau.core.couchdb.manager;

import java.net.URL;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbInfo;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.ObjectMapperFactory;

public interface iCouchDBConnectionManager {
	
	public CouchDbConnector getCouchDbConnector(final String databaseName);
	
	public CouchDbConnector getCouchDbConnector(final String databaseName, final boolean createIfNotExists);
	
	public DbInfo obtainDatabaseInfo(final String databaseName);
	
	public List<String> getAllDatabases();
	
	public boolean checkIfDbExists(final String databaseName);
	
	public void createDatabase(final String databaseName);
	
	public void deleteDatabase(final String databaseName);
	
	public String getHost();
	
	public String getProtocol();
	
	public int getPort();
	
	public URL getUrl();
	
	public String getUsername();
	
	public String getPassword();
	
	public boolean isCacheEnable();
	
	public int getConnectionTimeout();
	
	public int getMaxCacheEntries();
	
	public int getMaxConnections();
	
	public int getMaxObjectSizeBytes();
	
	public int getSocketTimeout();
	
	public boolean isSslEnable();
	
	public int getRevisionLimit();
	
	public CouchDbConnector getCurrentCouchDbConnector();
	
	public CouchDbInstance getDbInstance();
	
	public ObjectMapperFactory getObjectMapperFactory();
	
	public HttpClient getHttpClient();
	
	public void setHost(final String host);
	
	public void setProtocol(final String protocol);
	
	public void setPort(final int port);
	
	public void setUsername(final String username);
	
	public void setPassword(final String password);
	
	public void setCacheEnable(final boolean cacheEnable);
	
	public void setConnectionTimeout(final int connectionTimeout);
	
	public void setMaxCacheEntries(final int maxCacheEntries);
	
	public void setMaxConnections(final int maxConnections);
	
	public void setMaxObjectSizeBytes(final int maxObjectSizeBytes);
	
	public void setSocketTimeout(final int socketTimeout);
	
	public void setSslEnable(final boolean sslEnable);
	
	public void setRevisionLimit(final int revisonLimit);
	
	public void setObjectMapperFactory(final ObjectMapperFactory objectMapperFactory);
}
