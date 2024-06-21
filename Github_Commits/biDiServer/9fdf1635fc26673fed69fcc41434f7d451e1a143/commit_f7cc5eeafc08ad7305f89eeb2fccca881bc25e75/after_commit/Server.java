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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.gson.JsonElement;

/**
 * The server that can receive requests from the clients and push notifications to the clients.
 * @author spielwitz
 *
 */
public abstract class Server
{
	private static final int SOCKET_TIMEOUT = 30000;
	private static final int SERVER_THREAD_POOL_CORE_SIZE = 2;
	private static final int SERVER_THREAD_POOL_MAX_SIZE = 200;
	/**
	 * The name of the root folder where the server stores all data. The name is "ServerData".
	 */
	public final static String FOLDER_NAME_ROOT = "ServerData";
	private final static String FOLDER_NAME_LOGS = "Logs";
	private final static String FOLDER_NAME_USERS = "Users";
	
	private final static String FOLDER_NAME_NOTIFICATIONS = "Notifications";
	private final static String FOLDER_NAME_DATA_SETS = "DataSets";

	private final static String MESSAGE_PROCESSING_CONTAINER_DATA_KEY_USER = "User";
	private ServerConfiguration config;
	private String homeDir;
	private Log log;
	
	private long startDate;
	
	private boolean shutdown = false;
	private boolean adminCreated;
	private ServerSocket serverSocket;
	
	private NotificationThreadPulseCheckThread pulseCheckThread;
	
	private Hashtable<String, DataSetInfo> dataSetInfos;
	private Hashtable<String,User> users;
	private Hashtable<String, Ciphers> ciphersPerSession = new Hashtable<String, Ciphers>();
	private Hashtable<String, NotificationThread> notificationThreads = new Hashtable<String, NotificationThread>();
	
	private Object dataLock = new Object();

	/**
	 * Instantiate a server object instance.
	 * @param config The server configuration
	 * @param homeDir Home file directory where all server data is stored
	 * @throws ServerException Server exception
	 */
	protected Server(ServerConfiguration config, String homeDir) throws ServerException
	{
		this.config = config;
		this.homeDir = homeDir;
		
		if (config == null)
		{
			throw new ServerException("Server requires a configuration.");
		}
		
		TextProperties.setLocale(config.getLocale());
		
		Path pathLog = this.initCreateDataSetFolders();
		
		this.log = new Log(this, pathLog);
		
		this.initReadAllUsers();
		this.initCreateAdmin();
		this.initReadAllDataSets();
	}

	/**
	 * Get the path to the server data folder
	 * @return
	 */
	public String getPathToServerData()
	{
		return Paths.get(homeDir, FOLDER_NAME_ROOT).toString();
	}
	
	/**
	 * Start the server.
	 */
	public void start()
	{
		try
		{
		    this.serverSocket = new ServerSocket(this.config.getPort());
		    
		    System.out.println(
		    		TextProperties.getMessageText(
		    				TextProperties.ServerStarted(
		    						this.config.getUrl(),
		    						Integer.toString(this.config.getPort()))));
		    
		    this.log.logMessage(
					LogEventId.G1,
					LogLevel.General,
					TextProperties.getMessageText(
							TextProperties.ServerStarted(
									this.config.getUrl(),
									Integer.toString(this.config.getPort()))));
		}
		catch (Exception x)
		{
			System.out.println(
					TextProperties.getMessageText(
							TextProperties.ServerNotStarted(
									this.config.getUrl(),
									Integer.toString(this.config.getPort()))));
			
			this.log.logMessage(
					LogEventId.C1,
					LogLevel.Critical,
					TextProperties.getMessageText(
							TextProperties.ServerNotStarted(
									this.config.getUrl(),
									Integer.toString(this.config.getPort()))));
			return;
		}
		
		this.pulseCheckThread = this.new NotificationThreadPulseCheckThread();
		pulseCheckThread.start();
		
		this.startDate = System.currentTimeMillis();
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(SERVER_THREAD_POOL_CORE_SIZE);
		executor.setMaximumPoolSize(SERVER_THREAD_POOL_MAX_SIZE);
		
		while (true)
		{
			try
			{
			    Socket clientSocket = this.serverSocket.accept();
			    clientSocket.setSoTimeout(SOCKET_TIMEOUT);
			    
			    if (this.shutdown)
			    {
			    	clientSocket.close();
			    	break;
			    }
			    
			    ServerThread serverThread = this.new ServerThread(
			    									clientSocket,
			    									clientSocket.getInetAddress().toString());

			    executor.submit(serverThread);
			}
			catch (SocketException x)
			{
				break;
			}
			catch (Exception x)
			{
				this.log.logMessage(
						LogEventId.E1,
						LogLevel.Error,
						TextProperties.getMessageText(TextProperties.ServerSocketAcceptError(x.getMessage())));
				break;
			}
		}
		
		this.closeServerSocket();
		System.exit(0);
	}
	
	/**
	 * Get the server log level.
	 * @return The log level
	 */
	LogLevel getLogLevel()
	{
		return this.config.getLogLevel();
	}
	
	/**
	 * Check the server and the client build.
	 * @param clientBuild The client build
	 * @return True, if server and client are compatible. Also the minimum compatible build of the server is returned.
	 */
	protected abstract ServerClientBuildCheckResult checkServerClientBuild(String clientBuild);
	
	/**
	 * Check if a data set exists.
	 * @param id The id of the data set
	 * @return True, if a data set exists
	 */
	protected boolean dataSetExists(String id)
	{
		synchronized(this.dataLock)
		{
			return this.dataSetInfos.containsKey(id);
		}
	}
	
	/**
	 * Delete a data set.
	 * @param id The data set ID.
	 */
	protected void deleteDataSet(String id)
	{
		if (ServerUtils.checkFileName(id) == FileNameCheck.Ok)
		{
			synchronized(this.dataLock)
			{
				File file = Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS, id).toFile();
				file.delete();
				this.dataSetInfos.remove(id);
			}
		}
	}
	
	/**
	 * Get the server build.
	 * @return The server build
	 */
	protected abstract String getBuild();
	
	/**
	 * Get the server configuration.
	 * @return The server configuration
	 */
	protected ServerConfiguration getConfig()
	{
		return config;
	}

	/**
	 * Get a data set.
	 * @param id The ID of the data set
	 * @return The data set
	 */
	protected DataSet getDataSet(String id)
	{
		if (ServerUtils.checkFileName(id) == FileNameCheck.Ok)
		{
			synchronized(this.dataLock)
			{
				return DataSet.readFromFile(Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS, id).toString());
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Get the information related to a data set.
	 * @param id The ID of the data set
	 * @return The data set information
	 */
	protected DataSetInfo getDataSetInfo(String id)
	{
		return this.dataSetInfos.get(id);
	}
	
	/**
	 * Get the data set infos of a specified user.
	 * @param userId User ID
	 * @return All data set infos related to the user
	 */
	protected PayloadResponseGetDataSetInfosOfUser getDataSetInfosOfUser(String userId)
	{
		ArrayList<DataSetInfo> dataSetInfos = new ArrayList<DataSetInfo>();
		
		synchronized(this.dataLock)
		{
			for (DataSetInfo info: this.dataSetInfos.values())
			{
				if (info.getUserIds().contains(userId))
				{
					dataSetInfos.add(info);
				}
			}
		}

		return new PayloadResponseGetDataSetInfosOfUser(dataSetInfos);
	}
	
	/**
	 * Get a user.
	 * @param userId The user ID
	 * @return The user
	 */
	protected User getUser(String userId)
	{
		return this.users.get(userId);
	}
	
	/**
	 * Get all users, except the administrator and the activation users.
	 * @return A list of all users.
	 */
	protected ArrayList<User> getUsers()
	{
		ArrayList<User> users = new ArrayList<User>();
		
		for (User user: this.users.values())
		{
			if (!User.isUserIdReserved(user.getId()))
			{
				users.add(user);
			}
		}
		
		return users;
	}
	
	/**
	 * Log a custom message in the server log.
	 * @param severity Message severity
	 * @param msg The message
	 */
	protected void logCustomMessage(LogLevel severity, String msg)
	{
		if (this.log != null)
		{
			this.log.logMessage(LogEventId.CUSTOM, severity, msg);
		}
	}
	
	/**
	 * Migrate a data set when the server is started.
	 * @param className Name of the class of the data set payload
	 * @param jsonElementBeforeMigration JSON representation of the data set payload before migration
	 * @return Null, is no migration is required. Or else, the new JSON representation of the data set payload after migration
	 */
	protected abstract JsonElement migrateDataSet(String className, JsonElement jsonElementBeforeMigration);

	/**
	 * Update the server configuration file.
	 * @param config Server configuration
	 */
	protected abstract void onConfigurationUpdated(ServerConfiguration config);
	
	/**
	 * A request message of type CUSTOM was received at the server. Process your own logic here.
	 * @param userId User ID
	 * @param payloadRequest Request payload
	 * @return The response message info and the response message payload
	 */
	protected abstract Tuple<ResponseInfo,Object> onCustomRequestMessageReceived(String userId, Object payloadRequest);

	/**
	 * Send a notification
	 * @param sender Sender of the notification
	 * @param payload Notification payload
	 */
	protected void pushNotification(String sender, PayloadRequestMessagePushNotification payload)
	{
		for (String userId: payload.getRecipients())
		{
			if (User.isUserIdReserved(userId))
			{
				continue;
			}
			
			User user = this.getUser(userId);
				
			if (user == null || !user.isActive())
			{
				continue;
			}
			
			this.sendSingleNotification(userId, 
										 new Notification(
														sender,
														payload.getRecipients(),
														payload.getPayloadObject(),
														user.getUserPublicKeyObject()));
		}
	}
	
	/**
	 * Add or update a data set.
	 * @param dataSet The data set
	 * @return The result of the ID check
	 */
	protected FileNameCheck setDataSet(DataSet dataSet)
	{
		if (dataSet == null)
		{
			return FileNameCheck.InvalidCharacters;
		}
		
		FileNameCheck fileNameCheck = ServerUtils.checkFileName(dataSet.getId());
		
		if (fileNameCheck == FileNameCheck.Ok)
		{
			synchronized(this.dataLock)
			{
				this.setDataSetInfoFromDataSet(dataSet);
				dataSet.writeToFile(Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS, dataSet.getId()).toString());
			}
		}
		
		return fileNameCheck;
	}
	
	/**
	 * Set the payload object of a data set info object.
	 * @param dataId Data set ID
	 * @param userIds Users related to the data set
	 * @param dataSetPayloadObject Data set payload object
	 * @param currentDataSetInfoPayloadObject Current data set info payload object
	 * 
	 * @return New data info payload object
	 */
	protected abstract Object setDataSetInfoPayloadObject(String dataId,  HashSet<String> userIds, Object dataSetPayloadObject, Object currentDataSetInfoPayloadObject);
	
	/**
	 * Check if a user exists.
	 * @param userId The user ID
	 * @return True, if the user exists
	 */
	protected boolean userExists(String userId)
	{
		if (userId != null &&
			userId.equals(User.ACTIVATION_USER_ID) ||
			this.users.containsKey(userId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void addNotificationThread(String userId, NotificationThread notificationThread)
	{
		synchronized(this.notificationThreads)
		{
			NotificationThread t = this.notificationThreads.get(userId);
			
			if (t != null)
			{
				t.interrupt();
				this.notificationThreads.remove(userId);
			}
			
			this.notificationThreads.put(userId, notificationThread);
			notificationThread.start();
		}
	}
	
	private void afterResponseMessageSent(MessageProcessingContainer container)
	{
		if (container.getRequestMessage().getType() != RequestMessageType.CUSTOM)
		{
			switch (container.getRequestMessage().getType())
			{
				case CHANGE_USER:
				case ACTIVATE_USER:
					this.afterResponseMessageSentChangeUser(container);
					return;
					
				default:
					return;
			}
		}
	}
	
	private void afterResponseMessageSentChangeUser(MessageProcessingContainer container)
	{
		if (container.getResponseMessage().isSuccess())
		{
			this.updateUser((User)container.getData().get(MESSAGE_PROCESSING_CONTAINER_DATA_KEY_USER));
		}
	}
	
	private void closeServerSocket()
	{
		try
		{
			System.out.println(
					TextProperties.getMessageText(TextProperties.ClosingSocketServer()));
			
			if (serverSocket != null)
				serverSocket.close();
			
			this.log.logMessage(
					LogEventId.G2,
					LogLevel.Information,
					TextProperties.getMessageText(TextProperties.ShutdownDone()));
		}
		catch (Exception e)
		{
			this.log.logMessage(
					LogEventId.E2,
					LogLevel.Error,
					TextProperties.getMessageText(TextProperties.ShutdownError(e.getMessage())));
			
			e.printStackTrace();
		}
	}
	
	private void createFolder(File dir)
	{
		if (!dir.exists())
		{
			System.out.println(TextProperties.getMessageText(TextProperties.CreatingFolder(dir.getAbsolutePath().toString())));
			dir.mkdirs();
		}
	}
	
	private void disconnect(String userId)
	{
		synchronized(this.notificationThreads)
		{
			NotificationThread notificationThread = this.notificationThreads.get(userId);
			
			if (notificationThread != null)
			{
				notificationThread.shutdownServer();
			}
			
			this.notificationThreads.remove(userId);
		}
	}
	
	private Ciphers getCiphers(String sessionId)
	{
		if (sessionId == null || 
			sessionId.equals(CryptoLib.NULL_UUID))
		{
			return null;
		}
		
		synchronized (this.ciphersPerSession)
		{
			Ciphers ciphers = this.ciphersPerSession.get(sessionId);
			
			if (ciphers == null)
				return null;
			
			if (!ciphers.sessionId.equals(sessionId))
				return null;
			
			long timeNow = System.currentTimeMillis();
			
			if (timeNow - ciphers.lastUsed > CryptoLib.CIPHERS_MAX_INACTIVITY_MILLISECONDS)
			{
				return null;
			}				
			
			if (timeNow - ciphers.created > CryptoLib.CIPHERS_MAX_VALIDITY_MILLISECONDS)
			{
				return null;
			}
			
			ciphers.lastUsed = timeNow;
			return ciphers;
		}
	}

	private Log getLog()
	{
		return log;
	}
	
	private String getPathToNotificatiosFolder()
	{
		return Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_NOTIFICATIONS).toString();
	}
	
	private ResponseMessage getResponseMessageNotAuthorized(String userId)
	{
		return new ResponseMessage(
				false,
				null,
				TextProperties.NotAuthorized(userId));
	}
	
	private void initCreateAdmin()
	{
		if (!this.adminCreated)
		{
			System.out.println(TextProperties.getMessageText(TextProperties.CreatingAdmin()));
			
			KeyPair userKeyPair = CryptoLib.getNewKeyPair();
			
			User adminUser = new User(
					User.ADMIN_USER_ID,
					"",
					null,
					true,
					null,
					CryptoLib.encodePublicKeyToBase64(userKeyPair.getPublic()));
			
			this.updateUser(adminUser);
			
			ClientConfiguration clientConfig = new ClientConfiguration(
					adminUser.getId(),
					this.config.getUrl(),
					this.config.getPort(),
					Client.CLIENT_SOCKET_TIMEOUT,
					CryptoLib.encodePrivateKeyToBase64(userKeyPair.getPrivate()),
					this.config.getServerPublicKey(),
					this.config.getAdminEmail());
			
			String adminClientConfigurationFileName = Paths.get(
					homeDir, 
					FOLDER_NAME_ROOT, 
					ClientConfiguration.getFileName(
							adminUser.getId(), 
							this.config.getUrl(), 
							this.config.getPort())).toFile().getAbsolutePath();
			
			if (clientConfig.writeToFile(adminClientConfigurationFileName))
			{
				System.out.println(
						TextProperties.getMessageText(TextProperties.FileCreated(adminClientConfigurationFileName)));
				this.adminCreated = true;
			}
		}
	}
	
	private Path initCreateDataSetFolders()
	{
		this.createFolder(new File(this.homeDir, FOLDER_NAME_ROOT));
		
		Path pathLog = Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_LOGS);
		this.createFolder(pathLog.toFile());
		
		this.createFolder(Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_USERS).toFile());
		this.createFolder(Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS).toFile());
		this.createFolder(Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_NOTIFICATIONS).toFile());
		
		return pathLog;
	}
	
	private void initReadAllDataSets()
	{
		File directoryGames = Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS).toFile();
		this.dataSetInfos = new Hashtable<String, DataSetInfo>();
		
		for (String fileName: directoryGames.list())
		{
			System.out.println(TextProperties.getMessageText(TextProperties.ReadingDataSet(fileName)));
			
			String dataSetFileName = Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_DATA_SETS, fileName).toString();
			DataSet dataSet = DataSet.readFromFile(dataSetFileName);
			
			if (dataSet == null)
				continue;
			
			JsonElement jsonElementAfterMigration = this.migrateDataSet(
									dataSet.getPayload().getClassName(),
									dataSet.getPayload().getJsonElement());
			
			if (jsonElementAfterMigration != null)
			{
				dataSet.getPayload().setJsonElement(jsonElementAfterMigration);
				dataSet.writeToFile(dataSetFileName);
			}
			
			this.setDataSetInfoFromDataSet(dataSet);
		}		
	}
	
	private void initReadAllUsers()
	{
		this.users = new Hashtable<String, User>();
		this.adminCreated = false;
		
		File directoryUsers = Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_USERS).toFile();
		for (String fileName: directoryUsers.list())
		{
			if (fileName.equals(User.ADMIN_USER_ID) || User.isUserIdValid(fileName))
			{
				System.out.println(TextProperties.getMessageText(TextProperties.ReadingUser(fileName)));
				this.readUser(fileName);
			}
		}
	}
	
	private void onRequestMessageReceived(MessageProcessingContainer container) throws ServerException
	{
		switch (container.getRequestMessage().getType())
		{
			case ACTIVATE_USER:
				this.onRequestMessageReceivedActivateUser(container);
				break;
			case CHANGE_USER:
				this.onRequestMessageReceivedChangeUser(container);
				break;
			case SHUTDOWN:
				this.shutdown(container);
				break;
			case PING:
			case ESTABLISH_NOTIFICATION_SOCKET:
				container.setResponseMessage(new ResponseMessage());
				break;
			case GET_USER:
				this.onRequestMessageReceivedGetUser(container);
				break;
			case GET_USERS:
				this.onRequestMessageReceivedGetUsers(container);
				break;
			case DELETE_USER:
				this.onRequestMessageReceivedDeleteUser(container);
				break;
			case GET_SERVER_STATUS:
				this.onRequestMessageReceivedGetServerStatus(container);
				break;
			case GET_LOG:
				this.onRequestMessageReceivedGetLog(container);
				break;
			case SET_LOG_LEVEL:
				this.onRequestMessageReceivedSetLogLevel(container);
				break;
			case GET_DATA_SET_INFOS_OF_USER:
				this.onRequestMessageReceivedGetDataSetInfosOfUser(container);
				break;
			case CREATE_DATA_SET:
				this.onRequestMessageReceivedSetDataSet(container, true);
				break;
			case UPDATE_DATA_SET:
				this.onRequestMessageReceivedSetDataSet(container, false);
				break;
			case DELETE_DATA_SET:
				this.onRequestMessageReceivedDeleteDataSet(container);
				break;
			case GET_DATA_SET:
				this.onRequestMessageReceivedGetDataSet(container);
				break;
			case PUSH_NOTIFICATION:
				this.pushNotification(
						container.getUserId(), 
						(PayloadRequestMessagePushNotification)container.getRequestMessage().getPayloadObject());
				
				container.setResponseMessage(new ResponseMessage(null));
				break;
			case PUSH_NOTIFICATION_RECEIVED:
				this.onRequestMessageReceivedPushNotificationReceived(container);
				break;
			case DISCONNECT:
				this.disconnect(container.getUserId());
				container.setResponseMessage(new ResponseMessage());
				break;
			case CUSTOM:
				Tuple<ResponseInfo,Object> response = 
				this.onCustomRequestMessageReceived(
						container.getUserId(), 
						container.getRequestMessage().getPayloadObject());
		
				ResponseMessage responseMessage = new ResponseMessage(
						new Payload(response.getE2()),
						response.getE1());
				
				container.setResponseMessage(responseMessage);
						break;
		}
	}
	
	private void onRequestMessageReceivedActivateUser(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ACTIVATION_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		PayloadRequestMessageActivateUser payload = (PayloadRequestMessageActivateUser) container.getRequestMessage().getPayloadObject();
		
		User user = this.getUser(payload.getUserId());
		
		if (user == null )
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserActivationError(payload.getUserId())));
			return;
		}
		
		if (user.isActive())
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserAlreadyActive(payload.getUserId())));
			return;
		}
		
		if (!user.getActivationCode().equals(payload.getActivationCode()))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserActivationError(payload.getUserId())));
			return;
		}
		
		user.setActivationCode("");
		user.setActive(true);
		user.setUserPublicKey(payload.getUserPublicKey());
		
		container.getData().put(MESSAGE_PROCESSING_CONTAINER_DATA_KEY_USER, user);
		
		container.setResponseMessage(new ResponseMessage(null));
	}
	
	private void onRequestMessageReceivedChangeUser(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ADMIN_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		PayloadRequestMessageChangeUser payload = (PayloadRequestMessageChangeUser) container.getRequestMessage().getPayloadObject();
		
		String userId = payload.getUserId();
		
		FileNameCheck fileNameCheck = ServerUtils.checkFileName(userId);
		
		if (fileNameCheck != FileNameCheck.Ok)
		{
			TextProperty textPropertyError = null;
			
			switch (fileNameCheck)
			{
			case TooShort:
				textPropertyError = TextProperties.UserIdTooShort(Integer.toString(ServerUtils.FILENAME_MIN_LEN)); 
				break;
				
			case TooLong:
				textPropertyError = TextProperties.UserIdTooLong(Integer.toString(ServerUtils.FILENAME_MAX_LEN));
				break;
				
			case InvalidCharacters:
				textPropertyError = TextProperties.UserIdInvalidCharacters(ServerUtils.getInvalidCharactersAsString());
				break;
				
			default:
				break;
			}
			
			container.setResponseMessage(new ResponseMessage(false, null, textPropertyError));
			return;
		}
		
		if (User.isUserIdReserved(userId))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserIdReserved(userId)));
			return;
		}
		
		if (payload.isCreate() && this.userExists(payload.getUserId()))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserIdExists(payload.getUserId())));
			return;
		}
		
		if (!payload.isCreate() && !this.userExists(payload.getUserId()))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserIdNotExists(payload.getUserId())));
			return;
		}
		
		User user = this.getUser(payload.getUserId());
		
		if (user == null)
		{
			user = new User(
					payload.getUserId(),
					payload.getName(),
					payload.getCustomData());
		}
		
		if (payload.isCreate() || payload.isRenewCredentials())
		{
			user.clearCredentials();
		}
		else
		{
			user.setName(payload.getName());
			user.setCustomData(payload.getCustomData());
		}
		
		container.getData().put(MESSAGE_PROCESSING_CONTAINER_DATA_KEY_USER, user);
		
		PayloadResponseMessageChangeUser payloadResponse = new PayloadResponseMessageChangeUser(
				payload.getUserId(), 
				user.getActivationCode(), 
				this.config.getUrl(), 
				this.config.getPort(),
				this.config.getAdminEmail(), 
				this.config.getServerPublicKey());
		
		container.setResponseMessage(
					new ResponseMessage(new Payload(payloadResponse)));
	}
	
	private void onRequestMessageReceivedDeleteDataSet(MessageProcessingContainer container)
	{
		String dataSetId = (String)container.getRequestMessage().getPayloadObject();
		
		synchronized(this.dataLock)
		{
			if (this.dataSetInfos.containsKey(dataSetId))
			{
				DataSet dataSet = this.getDataSet(dataSetId);
				
				if (!container.getUserId().equals(User.ADMIN_USER_ID) &&
					!dataSet.getUserIds().contains(container.getUserId()))
				{
					container.setResponseMessage(
							new ResponseMessage(
									false,
									null,
									TextProperties.DataSetUserNotAuthorizedDelete(
											container.getUserId(),
											dataSetId)));
					return;
				}
				
				this.deleteDataSet(dataSetId);
			}
		}
		
		container.setResponseMessage(
				new ResponseMessage(
						null));
	}
	
	private void onRequestMessageReceivedDeleteUser(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ADMIN_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		String userId = (String)container.getRequestMessage().getPayloadObject();
		
		if (!this.users.containsKey(userId))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserIdNotExists(userId)));
		}
		else if (User.isUserIdReserved(userId))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserCannotBeDeleted(userId)));
		}
		else
		{
			synchronized (this.users)
			{
				synchronized(this.dataLock)
				{
					File file = Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_USERS, userId).toFile();
					file.delete();
					
					this.users.remove(userId);
					
					for (String dataSetId: this.dataSetInfos.keySet())
					{
						DataSetInfo dataSetInfo = this.dataSetInfos.get(dataSetId);
						
						if (dataSetInfo.getUserIds().contains(userId))
						{
							DataSet dataSet = this.getDataSet(dataSetInfo.getId());
							dataSet.getUserIds().remove(userId);
							
							this.setDataSet(dataSet);
						}
					}
				}
			}
			
			container.setResponseMessage(
					new ResponseMessage(null));
		}
	}
	
	private void onRequestMessageReceivedGetDataSet(MessageProcessingContainer container)
	{
		String dataSetId = (String)container.getRequestMessage().getPayloadObject();
		
		synchronized(this.dataLock)
		{
			if (!this.dataSetInfos.containsKey(dataSetId))
			{
				container.setResponseMessage(
						new ResponseMessage(
								false,
								null,
								TextProperties.DataSetIdNotExists(dataSetId)));
				return;
			}
			
			DataSet dataSet = this.getDataSet(dataSetId);
			
			if (container.getUserId().equals(User.ADMIN_USER_ID) ||
				dataSet.getUserIds().contains(container.getUserId()))
			{
				container.setResponseMessage(
						new ResponseMessage(
								new Payload(dataSet)));
			}
			else
			{
				container.setResponseMessage(
						new ResponseMessage(
								false,
								null,
								TextProperties.DataSetUserNotAuthorized(
										container.getUserId(),
										dataSetId)));
				return;
			}
		}
	}
	
	private void onRequestMessageReceivedGetDataSetInfosOfUser(MessageProcessingContainer container)
	{
		String userId = (String)container.getRequestMessage().getPayloadObject();
		
		container.setResponseMessage(
				new ResponseMessage(
						new Payload(
								this.getDataSetInfosOfUser(userId))));
	}
	
	private void onRequestMessageReceivedGetLog(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ADMIN_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		PayloadResponseMessageGetLog payloadResponse = 
				this.log != null ?
						this.log.getLog() :
						null;
		
		container.setResponseMessage(new ResponseMessage(new Payload(payloadResponse)));
	}
	
	private void onRequestMessageReceivedGetServerStatus(MessageProcessingContainer container)
	{
		PayloadResponseMessageGetServerStatus payloadResponse = 
				new PayloadResponseMessageGetServerStatus(
						this.startDate, 
						this.log != null ? this.log.getSize() : 0, 
						this.config.getLogLevel(), 
						this.getBuild());
		
		container.setResponseMessage(new ResponseMessage(new Payload(payloadResponse)));
	}
	
	private void onRequestMessageReceivedGetUser(MessageProcessingContainer container)
	{
		String userId = (String)container.getRequestMessage().getPayloadObject();
		
		if (User.isUserIdReserved(userId) ||
			!this.users.containsKey(userId))
		{
			container.setResponseMessage(
					new ResponseMessage(
							false, 
							null, 
							TextProperties.UserIdNotExists(userId)));
		}
		else
		{
			container.setResponseMessage(
					new ResponseMessage(
							new Payload(this.users.get(userId))));
		}
	}
	
	private void onRequestMessageReceivedGetUsers(MessageProcessingContainer container)
	{
		ArrayList<User> users = new ArrayList<User>();
		
		for (User user: this.users.values())
		{
			if (!User.isUserIdReserved(user.getId()))
			{
				users.add(user);
			}
		}
		
		container.setResponseMessage(
				new ResponseMessage(
						new Payload(
								new PayloadResponseMessageGetUsers(
										users))));
	}
		
	private void onRequestMessageReceivedPushNotificationReceived(MessageProcessingContainer container)
	{
		String notificationId = (String)container.getRequestMessage().getPayloadObject();
		
		Notification.deleteFile(
				Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_NOTIFICATIONS).toString(),
				container.getUserId(),
				notificationId);
		
		container.setResponseMessage(new ResponseMessage());
	}
	
	private void onRequestMessageReceivedSetDataSet(MessageProcessingContainer container, boolean create)
	{
		DataSet dataSet = (DataSet)container.getRequestMessage().getPayloadObject();
		
		synchronized(this.dataLock)
		{
			if (create && this.dataSetInfos.containsKey(dataSet.getId()))
			{
				container.setResponseMessage(
						new ResponseMessage(
								false,
								null,
								TextProperties.DataSetIdExists(dataSet.getId())));
				return;
			}
			else if (!create)
			{
				if (!this.dataSetInfos.containsKey(dataSet.getId()))
				{
					container.setResponseMessage(
							new ResponseMessage(
									false,
									null,
									TextProperties.DataSetIdNotExists(dataSet.getId())));
					return;
				}
				else if (!container.getUserId().equals(User.ADMIN_USER_ID) &&
						 !dataSet.getUserIds().contains(container.getUserId()))
				{
					container.setResponseMessage(
							new ResponseMessage(
									false,
									null,
									TextProperties.DataSetUserNotAuthorizedUpdate(
											container.getUserId(),
											dataSet.getId())));
					return;
				}
			}
			
			FileNameCheck fileNameCheck = this.setDataSet(dataSet);
			
			if (fileNameCheck != FileNameCheck.Ok)
			{
				TextProperty textPropertyError = null;
				
				switch (fileNameCheck)
				{
				case TooShort:
					textPropertyError = TextProperties.DataSetIdTooShort(Integer.toString(ServerUtils.FILENAME_MIN_LEN)); 
					break;
					
				case TooLong:
					textPropertyError = TextProperties.DataSetIdTooLong(Integer.toString(ServerUtils.FILENAME_MAX_LEN));
					break;
					
				case InvalidCharacters:
					textPropertyError = TextProperties.DataSetIdInvalidCharacters(ServerUtils.getInvalidCharactersAsString());
					break;
					
				default:
					break;
				}
				
				container.setResponseMessage(
						new ResponseMessage(
								false,
								null,
								textPropertyError));
				
				return;
			}
		}
		
		container.setResponseMessage(
				new ResponseMessage());
	}
	
	private void onRequestMessageReceivedSetLogLevel(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ADMIN_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		LogLevel newLogLevel = (LogLevel) container.getRequestMessage().getPayloadObject();
		
		this.config.setLogLevel(newLogLevel);
		this.onConfigurationUpdated(config);
		
		container.setResponseMessage(new ResponseMessage(null));
	}
	
	private void pulseCheckNotificationThreads()
	{
		synchronized(this.notificationThreads)
		{
			for (NotificationThread t: this.notificationThreads.values())
			{
				t.pulseCheck();
			}
		}
	}
	
	private User readUser(String userId)
	{
		synchronized(this.users)
		{
			if (this.users.containsKey(userId))
				return this.users.get(userId);

			User user = User.readFromFile(Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_USERS, userId).toString());
			
			if (user != null)
			{
				this.users.put(user.getId(), user);
				
				if (user.getId().equals(User.ADMIN_USER_ID))
					this.adminCreated = true;
			}
			
			return user;
		}
	}
	
	private void removeNotificationThread(String userId)
	{
		synchronized(this.notificationThreads)
		{
			this.notificationThreads.remove(userId);
		}
	}
	
	private void sendSingleNotification(String userId, Notification notification)
	{
		notification.writeToFile(
				Paths.get(homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_NOTIFICATIONS).toString(),
				userId);
		
		synchronized(this.notificationThreads)
		{
			NotificationThread notificationThread = this.notificationThreads.get(userId);
			
			if (notificationThread != null && notificationThread.isAlive())
			{
				notificationThread.pushNotification(notification);
			}
		}
	}
	
	private void setCiphers(String sessionId, Ciphers ciphers)
	{
		if (sessionId == null || 
			sessionId.equals(CryptoLib.NULL_UUID) ||
			ciphers == null)
		{
			return;
		}
		
		synchronized (this.ciphersPerSession)
		{
			ciphers.lastUsed = System.currentTimeMillis();
			
			if (ciphers.created == 0)
				ciphers.created = ciphers.lastUsed;
			
			this.ciphersPerSession.put(sessionId, ciphers);
		}
	}
	
	private void setDataSetInfoFromDataSet(DataSet dataSet)
	{
		DataSetInfo dataSetInfo = this.getDataSetInfo(dataSet.getId());
		
		Object newPayloadObject = this.setDataSetInfoPayloadObject(
											dataSet.getId(), 
											dataSet.getUserIds(),
											dataSet.getPayloadObject(),
											dataSetInfo != null ?
													dataSetInfo.getPayloadObject() :
													null);
		
		if (dataSetInfo == null)
		{
			dataSetInfo = new DataSetInfo(dataSet.getId(), dataSet.getUserIds(), newPayloadObject);
		}
		else
		{
			dataSetInfo.setUserIds(dataSet.getUserIds());
			dataSetInfo.setPayloadObject(newPayloadObject);
		}
		
		this.dataSetInfos.put(dataSet.getId(), dataSetInfo);
	}
	
	private void shutdown(MessageProcessingContainer container)
	{
		if (!container.getUserId().equals(User.ADMIN_USER_ID))
		{
			container.setResponseMessage(this.getResponseMessageNotAuthorized(container.getUserId()));
			return;
		}
		
		synchronized(this.notificationThreads)
		{
			for (NotificationThread t: this.notificationThreads.values())
			{
				t.shutdownServer();
			}
		}
		
		this.closeServerSocket();
		this.shutdown = true;
	}
	
	private void updateUser(User user)
	{
		synchronized(this.users)
		{
			user.writeToFile(Paths.get(this.homeDir, FOLDER_NAME_ROOT, FOLDER_NAME_USERS, user.getId()).toString());
			
			if (this.users.containsKey(user.getId()))
				this.users.replace(user.getId(), user);
			else
				this.users.put(user.getId(), user);
			
			if (user.getId().equals(User.ADMIN_USER_ID))
				this.adminCreated = true;
		}
	}
	
	// =============================================
	
	private class NotificationThread extends Thread
	{
		private String userId;
		private Socket socket;
		private OutputStream out;
		
		private NotificationSocketCommunicationStructure commStruct;
		
		NotificationThread(String userId, Socket socket, OutputStream out)
		{
			this.userId = userId;
			this.socket = socket;
			this.out = out;
			
			this.commStruct = new NotificationSocketCommunicationStructure();
		}
		
		public void run()
		{
			try
			{
				this.socket.setSoTimeout(0);
			}
			catch (Exception x)
			{
				return;
			}
			
			boolean closeSocket = false;
			
			ArrayList<Notification> notifications = Notification.getAllNotificationsMessagesOfUserFromFile(
																		getPathToNotificatiosFolder(), 
																		userId);
			
			try
			{
				this.executePushNotifications(new Notifications(notifications), out);
			}
			catch (Exception e)
			{
				closeSocket = true;
			}
			
			while (!closeSocket)
			{
				synchronized(this.commStruct)
				{
					try
					{
						this.commStruct.wait();
						
						if (this.commStruct.closeSocket)
						{
							closeSocket = true;
						}
						else
						{
							this.executePushNotifications(
									new Notifications(this.commStruct.notification), 
									out);
						}
					}
					catch (SocketException e)
					{
						closeSocket = true;
					}
					catch (InterruptedException e)
					{
						closeSocket = true;
					}
					catch (Exception e)
					{
						getLog().logMessage(
								LogEventId.E7, 
								LogLevel.Error,
								TextProperties.getMessageText(
									TextProperties.NotificationSocketUnexpectedError(
											this.userId, 
											e.getMessage())));
					}
				}
			}
			
			removeNotificationThread(userId);
			
			try {
				this.socket.close();
			} catch (IOException x)
			{
			}
		}
		
		void pulseCheck()
		{
			this.pushNotification(new Notification());
		}
		
		void pushNotification(Notification notification)
		{
			synchronized(this.commStruct)
			{
				this.commStruct.notification = notification;
				this.commStruct.notify();
			}
		}
		
		void shutdownServer()
		{
			synchronized(this.commStruct)
			{
				this.commStruct.closeSocket = true;
				this.commStruct.notify();
			}
		}
		
		private void executePushNotifications(Notifications notifications, OutputStream out) throws Exception
		{
			CryptoLib.sendStringRsaEncrypted(
					out, 
					notifications.serialize(), 
					getUser(this.userId).getUserPublicKeyObject());
		}
	}

	// ==========================================================
	
	private class NotificationThreadPulseCheckThread extends Thread
	{
		public void run()
		{
			do
			{
				try {
					Thread.sleep(30000);
				} catch (Exception e)
				{
					break;
				}
				
				pulseCheckNotificationThreads();
				
			} while (true);
		}
	}
	
	// ==========================================================
	
	private class ServerThread implements Runnable
	{
		private Socket socket;
		private String ipAddress;
		private String userId;
		
		ServerThread(Socket socket, String ipAddress)
		{
			this.socket = socket;
			this.ipAddress = ipAddress;
		}
		
		public void run()
		{
			String sessionId = null;
			OutputStream out = null;
			DataInputStream in = null;
			ServerClientBuildCheckResult serverClientBuildCheck = null;
			String clientBuild = null;
			
			boolean establishNotificationSocket = false;
			
			try
			{
			    in = new DataInputStream(this.socket.getInputStream());
			    out = this.socket.getOutputStream();
			    
			    RequestMessageBase reqMsgUserId = 
			    		(RequestMessageBase) RequestMessageBase.deserialize(
			    					CryptoLib.receiveStringRsaEncrypted(
			    					in,
			    					getConfig().getServerPrivateKeyObject()));
			    
			    this.userId = (String) reqMsgUserId.getPayloadObject();
			    sessionId = reqMsgUserId.getSessionId();
			    
			    if (!userExists(userId))
			    {
			    	getLog().logMessage(
							LogEventId.W1,
							LogLevel.Warning,
							this.ipAddress,
							userId,
							null,
							null,
							TextProperties.getMessageText(TextProperties.LogOnWithInvalidUserNameLength(Integer.toString(userId.length()))));
			    	
			    	this.closeSocket();
				    return;
			    }
			    
			    if (getBuild() != null && reqMsgUserId.getClientBuild() != null)
			    {
			    	serverClientBuildCheck = checkServerClientBuild(reqMsgUserId.getClientBuild());
			    	clientBuild = reqMsgUserId.getClientBuild(); 
			    }
			}
			catch (Exception x)
			{
				getLog().logMessage(
						LogEventId.E3,
						LogLevel.Error,
						this.ipAddress,
						userId,
						null,
						null,
						TextProperties.getMessageText(TextProperties.RequestReceiveError(x.getMessage())));
				
				this.closeSocket();
				return;
			}
			
			User user = getUser(this.userId);
			
			if (user != null && !user.isActive())
			{
				getLog().logMessage(
						LogEventId.W2,
						LogLevel.Warning,
						this.ipAddress,
						userId,
						null,
						null,
						TextProperties.getMessageText(TextProperties.LogOnWithInactiveUser(user.getId())));
				
				this.closeSocket();
				return;
			}
			
			String token = 
					this.userId.equals(User.ACTIVATION_USER_ID) ?
							CryptoLib.NULL_UUID :
							UUID.randomUUID().toString();
			
			Ciphers ciphers = getCiphers(sessionId);
			
			try
			{
				if (!this.userId.equals(User.ACTIVATION_USER_ID))
				{
					PayloadResponseMessageUserId payload = new PayloadResponseMessageUserId(
																token,
																ciphers != null,
																serverClientBuildCheck);
					
					ResponseMessage respMsg = new ResponseMessage(
													new Payload(payload),
													new ResponseInfo(
															serverClientBuildCheck == null ||
															serverClientBuildCheck.areBuildsCompatible())
													);
					respMsg.setServerBuild(getBuild());

					CryptoLib.sendStringRsaEncrypted(
							out, 
							respMsg.serialize(), 
							user.getUserPublicKeyObject());
				}
				
				if (ciphers == null &&
					!(serverClientBuildCheck != null && !serverClientBuildCheck.areBuildsCompatible()))
				{
					ciphers = CryptoLib.diffieHellmanKeyAgreementServer(in, out);
					sessionId = ciphers.sessionId;
					
					if (!this.userId.equals(User.ACTIVATION_USER_ID))
					{
						setCiphers(sessionId, ciphers);
					}
				}
			}
			catch (Exception x)
			{
				getLog().logMessage(
						LogEventId.E4,
						LogLevel.Error,
						this.ipAddress,
						userId,
						null,
						null,
						TextProperties.getMessageText(TextProperties.DiffieHellmanKeyExchangeFailed(x.getMessage())));
				
				this.closeSocket();
				return;
			}
			
			if (serverClientBuildCheck != null && !serverClientBuildCheck.areBuildsCompatible())
			{
				getLog().logMessage(
						LogEventId.V1,
						LogLevel.Verbose,
						this.ipAddress,
						userId,
						null,
						null,
						TextProperties.getMessageText(
								TextProperties.IncomptabileBuilds(
				    					serverClientBuildCheck.getMinimumComptabileBuild(),
				    					clientBuild)));
				
				this.closeSocket();
				return;
			}
			
			RequestMessage requestMessage = null;
			
			try
			{
				requestMessage = (RequestMessage)RequestMessage.deserialize(
						CryptoLib.receiveStringAesEncrypted(in, ciphers.cipherDecrypt));
			    
			    if (token!= null && !token.equals(requestMessage.getToken()))
			    	throw new Exception (TextProperties.getMessageText(TextProperties.InvalidToken()));
			}
			catch (Exception x)
			{
				getLog().logMessage(
						LogEventId.E5,
						LogLevel.Error,
						this.ipAddress,
						userId,
						null,
						null,
						TextProperties.getMessageText(TextProperties.RequestDecryptionError(x.getMessage())));
				
				this.closeSocket();
				return;
			}
			
			MessageProcessingContainer container = new MessageProcessingContainer(userId, requestMessage); 
		    
	    	try
	    	{
		    	onRequestMessageReceived(container);
	    	}
	    	catch (Exception x)
	    	{
	    		String errorMessage = this.getErrorMessageFromException(x);
	    		
	    		container.setResponseMessage(new ResponseMessage(
	    				false,
	    				null,
	    				TextProperties.ApplicationError(errorMessage)));
	    		
				getLog().logMessage(
						LogEventId.C2,
						LogLevel.Critical,
						this.ipAddress,
						userId,
						requestMessage.getType().toString(),
						requestMessage.serialize(),
						TextProperties.getMessageText(TextProperties.ApplicationError(errorMessage)));

				afterResponseMessageSent(container);
	    	}
		    
		    if (container.getResponseMessage() == null)
		    {
		    	getLog().logMessage(
						LogEventId.C3,
						LogLevel.Critical,
						this.ipAddress,
						userId,
						requestMessage.getType().toString(),
						requestMessage.serialize(),
						TextProperties.getMessageText(TextProperties.ResponseMessageNotSet()));
		    	
		    	this.closeSocket();
		    	return;
		    }
		    
			try
			{
				 container.getResponseMessage().setServerBuild(getBuild());
				
				 CryptoLib.sendStringAesEncrypted(
						 out, 
						 container.getResponseMessage().serialize(), 
						 ciphers.cipherEncrypt);
				 
				 String logMessageText = TextProperties.getMessageText(TextProperties.Ok());
				 LogLevel logLevel =
						 User.isUserIdReserved(userId) ?
								 LogLevel.Information :
								 LogLevel.Verbose;
				 
				 if (!container.getResponseMessage().isSuccess())
				 {
					 logLevel = LogLevel.Warning;
					 
					 if (container.getResponseMessage().getMessage() != null)
					 {
						 logMessageText = container.getResponseMessage().getMessage();
					 }
					 else
					 {
						 if (requestMessage.getType() != RequestMessageType.CUSTOM)
						 {
							 logMessageText = TextProperties.getMessageText(container.getResponseMessage().getTextProperty());
						 }
					 }
				 }
				 
		    	 getLog().logMessage(
			 			 LogEventId.I1,
						 logLevel,
						 this.ipAddress,
						 userId,
						 requestMessage.getType().toString(),
						 requestMessage.getPayload() != null ?
								 requestMessage.getPayload().toString() : "",
								 logMessageText);
		    	 
		    	 if (requestMessage.getType() == RequestMessageType.ESTABLISH_NOTIFICATION_SOCKET)
		    	 {
		    		 establishNotificationSocket = true;
		    	 }
		    	 else
		    	 {
		    		 afterResponseMessageSent(container);
		    	 }
	 		}
			catch (Exception x)
			{
				container.setResponseMessage(new ResponseMessage(
	    				false,
	    				null,
	    				TextProperties.ResponseError(x.getMessage())));
				
				getLog().logMessage(
						LogEventId.E6,
						LogLevel.Error,
						this.ipAddress,
						userId,
						requestMessage.getType().toString(),
						container.getResponseMessage().serialize(),
						TextProperties.getMessageText(TextProperties.ResponseError(x.getMessage())));
				
				afterResponseMessageSent(container);
				
				this.closeSocket();
				return;
			}
			
			if (establishNotificationSocket)
			{
				NotificationThread notificationThread = new NotificationThread(
						this.userId,
						this.socket,
						out);
				addNotificationThread(this.userId, notificationThread);
			}
			else
			{
				this.closeSocket();
			}
		}
		
		private void closeSocket()
		{
		    try {
				this.socket.close();
			} catch (IOException x)
			{
			}
		}
		
		private String getErrorMessageFromException(Exception x)
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(x.toString());
			sb.append('\n');
			
			for (StackTraceElement stackTraceElement: x.getStackTrace())
			{
				sb.append(stackTraceElement.toString());
				sb.append('\n');
			}
			
			return sb.toString();
		}
	}
}
