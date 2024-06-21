/**	BiDiServer - a library that provides bi-directional communication between
	a server and clients.
	
    Copyright (C) 2022 Michael Schweitzer, spielwitz@icloud.com
	https://github.com/spielwitz/biDiServer
	
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. **/

package spielwitz.biDiServer;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * The configuration of a client.
 * @author spielwitz
 *
 */
public class ClientConfiguration extends FileBasedSerializableEntity
{
	/**
	 * Get the proposed file name of a client configuration file, composed of user ID, server URL, and server port. 
	 * @param userId The user ID
	 * @param url The server URL
	 * @param port The server port
	 * @return The proposed file name
	 */
	public static String getFileName(String userId, String url, int port)
	{
		String userIdTrimmed = userId.replaceAll("[^a-zA-Z0-9.-]", "_");
		String urlTrimmed = url.replaceAll("[^a-zA-Z0-9.-]", "_");
		return userIdTrimmed + "_" + urlTrimmed + "_" + port; 
	}
	/**
	 * Read a client configuration from a file.
	 * @param fileName File name
	 * @return The client configuration
	 */
	public static ClientConfiguration readFromFile(String fileName)
	{
		ClientConfiguration clientConfiguration = (ClientConfiguration) readFromFileInternal(fileName, ClientConfiguration.class);
		
		if (clientConfiguration != null)
		{
			clientConfiguration.serverPublicKeyObject = CryptoLib.decodePublicKeyFromBase64(clientConfiguration.serverPublicKey);
			clientConfiguration.userPrivateKeyObject = CryptoLib.decodePrivateKeyFromBase64(clientConfiguration.userPrivateKey);
		}
		
		return clientConfiguration;
	}
	private String userId;
	private String url;
	private int port;
	private int timeout;
	private String userPrivateKey;
	
	private String serverPublicKey;
	private String adminEmail;
	
	private transient PrivateKey userPrivateKeyObject;
	
	private transient PublicKey serverPublicKeyObject;
	
	/**
	 * Create a client configuration object.
	 * @param userId The user related to the client
	 * @param url Server URL
	 * @param port Server port
	 * @param timeout Connection timeout. If 0, the timeout is defaulted to 10 seconds.
	 * @param userPrivateKey THe private RSA key of the user as a base64-encoded string
	 * @param serverPublicKey The public RSA key of the server as a base64-encoded string
	 * @param adminEmail The e-mail address of the server administrator
	 */
	public ClientConfiguration(
			String userId, 
			String url, 
			int port, 
			int timeout, 
			String userPrivateKey,
			String serverPublicKey, 
			String adminEmail)
	{
		super();
		this.userId = userId;
		this.url = url;
		this.port = port;
		this.timeout = timeout;
		this.userPrivateKey = userPrivateKey;
		this.serverPublicKey = serverPublicKey;
		this.adminEmail = adminEmail;
		
		this.serverPublicKeyObject = CryptoLib.decodePublicKeyFromBase64(this.serverPublicKey);
		this.userPrivateKeyObject = CryptoLib.decodePrivateKeyFromBase64(this.userPrivateKey); 
	}

	/** 
	 * Get the e-mail address of the server administrator.
	 * @return The e-mail address of the server administrator
	 */
	public String getAdminEmail() {
		return adminEmail;
	}
	
	/**
	 * Get the server port.
	 * @return The server port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Get the public RSA key of the server as a base64-encoded string.
	 * @return The public RSA key of the server as a base64-encoded string
	 */
	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	/**
	 * The public RSA key of the server.
	 * @return Public RSA key
	 */
	public PublicKey getServerPublicKeyObject() 
	{
		if (this.serverPublicKeyObject == null)
		{
			this.serverPublicKeyObject = CryptoLib.decodePublicKeyFromBase64(this.serverPublicKey);
		}
		
		return serverPublicKeyObject;
	}

	/**
	 * Get the connection timeout.
	 * @return The connection timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * Get the server URL.
	 * @return The server URL
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Get the user ID.
	 * @return The user ID
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * The private RSA key of the user.
	 * @return Private RSA key
	 */
	public PrivateKey getUserPrivateKeyObject() 
	{
		if (this.userPrivateKeyObject == null)
		{
			this.userPrivateKeyObject = CryptoLib.decodePrivateKeyFromBase64(this.userPrivateKey);
		}
		
		return this.userPrivateKeyObject;
	}
	
	/**
	 * Set the e-mail address of the server administrator.
	 * @param The e-mail address of the server administrator
	 */
	public void setAdminEmail(String adminEmail)
	{
		this.adminEmail = adminEmail;
	}
	
	/**
	 * Set the server port.
	 * @param port The server port.
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
	
	/**
	 * Set the connection timeout.
	 * @param timeout The connection timeout.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Set the server URL.
	 * @param url The server URL
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	/**
	 * Set the user ID.
	 * @param userId The user ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
