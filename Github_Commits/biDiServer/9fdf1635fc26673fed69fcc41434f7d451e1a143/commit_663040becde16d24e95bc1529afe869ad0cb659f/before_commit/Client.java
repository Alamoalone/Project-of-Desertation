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
import java.io.EOFException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;

/**
 * The client that can send requests to the server and can receive notifications from the server.
 * @author spielwitz
 *
 */
public abstract class Client
{
	/**
	 * The default client socket time out (10 seconds)
	 */
	public static final int CLIENT_SOCKET_TIMEOUT = 10;
	/**
	 * Activate a user.
	 * @param userActivationData User activation data
	 * @param locale Language locale
	 * @param clientBuild Client build
	 * @return Client configuration and response message info
	 */
	public static Tuple<ClientConfiguration, ResponseInfo> activateUser(
			PayloadResponseMessageChangeUser userActivationData,
			String locale,
			String clientBuild)
	{
		KeyPair userKeyPair = CryptoLib.getNewKeyPair();
		
		ClientConfiguration clientConfigForActivation = new ClientConfiguration(
				User.ACTIVATION_USER_ID, 
				userActivationData.getServerUrl(), 
				userActivationData.getServerPort(), 
				Client.CLIENT_SOCKET_TIMEOUT, 
				CryptoLib.encodePrivateKeyToBase64(userKeyPair.getPrivate()),
				userActivationData.getServerPublicKey(), 
				userActivationData.getAdminEmail());
		
		ClientForUserActivation clientForActivation = new ClientForUserActivation(clientConfigForActivation, locale, clientBuild);
		clientForActivation.establishNotificationSocket = false;
		
		PayloadRequestMessageActivateUser payload = new PayloadRequestMessageActivateUser(
				userActivationData.getUserId(),
				userActivationData.getActivationCode(),
				CryptoLib.encodePublicKeyToBase64(userKeyPair.getPublic()));
		
		ResponseMessage responseMessage = clientForActivation.sendRequestMessage(RequestMessageType.ACTIVATE_USER, new Payload(payload));
		
		clientConfigForActivation.setUserId(userActivationData.getUserId());
		
		return new Tuple<ClientConfiguration, ResponseInfo>(clientConfigForActivation, responseMessage.getInfo());
	}
	
	private ClientConfiguration config;
	private Ciphers aesCiphers;
	
	boolean establishNotificationSocket;
	private boolean connected;
	
	private ClientNotificationReceiverThread notificationReceiverThread;
	
	private ClientReconnectThread reconnectThread;
	
	private Object lockObject = new Object();
	
	/**
	 * Constructor.
	 * @param config Client configuration
	 * @param establishNotificationSocket True, if a notification socket shall be established. Not possible for administration and activation user 
	 * @param locale Language into which the client converts text properties. "de-DE" or "en-US"
	 */
	protected Client(ClientConfiguration config, boolean establishNotificationSocket, String locale)
	{
		this.config = config;
		this.establishNotificationSocket = 
				establishNotificationSocket && 
				!config.getUserId().equals(User.ACTIVATION_USER_ID) &&
				!config.getUserId().equals(User.ADMIN_USER_ID) ?
						true :
						false;
				
		TextProperties.setLocale(locale);
	}
	
	/**
	 * Create or update a user. The user needs to be activated in another step.
	 * @param payload The user data
	 * @return The information needed to activate the user.
	 */
	public Response<PayloadResponseMessageChangeUser> changeUser(PayloadRequestMessageChangeUser payload)
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.CHANGE_USER, new Payload(payload));
		
		return new Response<PayloadResponseMessageChangeUser>(
				(PayloadResponseMessageChangeUser)responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Create a new data set. There must not be an existing data set with the same ID.
	 * @param dataSet The data set.
	 * @return Response information
	 */
	public ResponseInfo createDataSet(DataSet dataSet)
	{
		return this.sendRequestMessage(RequestMessageType.CREATE_DATA_SET, new Payload(dataSet)).getInfo();
	}
	
	/** 
	 * Delete a data set. No error is returned, if the data set does not exist.
	 * @param id The data set ID.
	 * @return Response information.
	 */
	public ResponseInfo deleteDataSet(String id)
	{
		return this.sendRequestMessage(RequestMessageType.DELETE_DATA_SET, new Payload(id)).getInfo();
	}
	
	/**
	 * Delete a user.
	 * @param userId The user ID.
	 * @return Response information
	 */
	public ResponseInfo deleteUser(String userId)
	{
		return this.sendRequestMessage(RequestMessageType.DELETE_USER, new Payload(userId)).getInfo();
	}
	
	/**
	 * Disconnects the client from the server.
	 */
	public void disconnect()
	{
		if (this.notificationReceiverThread != null)
		{
			this.sendRequestMessage(RequestMessageType.DISCONNECT, null);
			this.notificationReceiverThread.disconnect();
		}
		
		if (this.reconnectThread != null)
		{
			this.reconnectThread.interrupt();
		}
	}
	
	/**
	 * Get the client configuration.
	 * @return The client configuration.
	 */
	public ClientConfiguration getConfig()
	{
		return config;
	}
	
	/**
	 * Get a data set.
	 * @param id The data set ID
	 * @return The data set
	 */
	public Response<DataSet> getDataSet(String id)
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_DATA_SET, new Payload(id));
		
		return new Response<DataSet>(
				(DataSet) responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Get all information about all data sets related to a user.
	 * @param userId The ID of the user.
	 * @return Information about the data sets.
	 */
	public Response<PayloadResponseGetDataSetInfosOfUser> getDataSetInfosOfUser(String userId)
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_DATA_SET_INFOS_OF_USER, new Payload(userId));
		
		return new Response<PayloadResponseGetDataSetInfosOfUser>(
				(PayloadResponseGetDataSetInfosOfUser) responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}

	/**
	 * Get the server log.
	 * @return The server log.
	 */
	public Response<PayloadResponseMessageGetLog> getServerLog()
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_LOG, null);
		
		return new Response<PayloadResponseMessageGetLog>(
				(PayloadResponseMessageGetLog)responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Get the server status.
	 * @return Server status
	 */
	public Response<PayloadResponseMessageGetServerStatus> getServerStatus()
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_SERVER_STATUS, null);
		
		return new Response<PayloadResponseMessageGetServerStatus>(
				(PayloadResponseMessageGetServerStatus)responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Get a user.
	 * @param userId The user ID
	 * @return The user
	 */
	public Response<User> getUser(String userId)
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_USER, new Payload(userId));
		
		return new Response<User>(
				(User)responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Get the ID of the user who is connected through this client.
	 * @return The user ID
	 */
	public String getUserId()
	{
		return this.config.getUserId();
	}
	
	/**
	 * Get all users.
	 * @return All users
	 */
	public Response<PayloadResponseMessageGetUsers> getUsers()
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.GET_USERS, null);
		
		return new Response<PayloadResponseMessageGetUsers>(
				(PayloadResponseMessageGetUsers) responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Checks if the notification socket is connected.
	 * @return True, if connected.
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**
	 * Ping the server.
	 * @return Response information
	 */
	public ResponseInfo pingServer()
	{
		return this.sendRequestMessage(RequestMessageType.PING, null).getInfo();
	}
	
	/**
	 * Push a notification to a number of recipients.
	 * @param recipients The recipients
	 * @param object The object to be pushed
	 * @return Response information
	 */
	public ResponseInfo pushNotification(ArrayList<String> recipients, Object object)
	{
		return this.sendRequestMessage(
				RequestMessageType.PUSH_NOTIFICATION,
				new Payload(new PayloadRequestMessagePushNotification(recipients, object))).getInfo();
	}
	
	/**
	 * Send a custom-defined request message to the server.
	 * @param <T> The class of the response payload
	 * @param payload The request payload
	 * @return The response
	 */
	@SuppressWarnings("unchecked")
	public <T> Response<T> sendCustomRequestMessage(Object payload)
	{
		ResponseMessage responseMessage = this.sendRequestMessage(RequestMessageType.CUSTOM, new Payload(payload));
		
		return new Response<T>(
				(T)responseMessage.getPayloadObject(),
				responseMessage.getInfo());
	}
	
	/**
	 * Set the server log level.
	 * @param newLogLevel The new log level
	 * @return Response information
	 */
	public ResponseInfo setLogLevel(LogLevel newLogLevel)
	{
		return this.sendRequestMessage(RequestMessageType.SET_LOG_LEVEL, new Payload(newLogLevel)).getInfo();
	}
	
	/**
	 * Shut down the server. 
	 * @return Response information
	 */
	public ResponseInfo shutdownServer()
	{
		return this.sendRequestMessage(RequestMessageType.SHUTDOWN, null).getInfo();
	}
	
	/**
	 * Start the client. Also establishes a notification socket, except for user _ADMIN and the activation user.
	 */
	public void start()
	{
		if (this.establishNotificationSocket)
		{
			this.reconnectThread = this.new ClientReconnectThread();
			this.reconnectThread.start();
		}
		else
		{
			this.connected = false;
		}
	}
	
	/**
	 * Update an existing data set.
	 * @param dataSet The data set.
	 * @return Response information
	 */
	public ResponseInfo updateDataSet(DataSet dataSet)
	{
		return this.sendRequestMessage(RequestMessageType.UPDATE_DATA_SET, new Payload(dataSet)).getInfo();
	}
	
	ResponseMessage sendRequestMessage(RequestMessageType type, Payload payload)
	{
		ResponseMessage responseMessage  = null;
		
		synchronized(this.lockObject)
		{
			Socket kkSocket = null;
			OutputStream out = null;
			
			try {
				kkSocket = new Socket();
				kkSocket.connect(
						new InetSocketAddress(this.config.getUrl(), this.config.getPort()), 
						this.config.getTimeout() > 0 ?
								this.config.getTimeout() * 1000 :
								CLIENT_SOCKET_TIMEOUT * 1000);
				
				out = kkSocket.getOutputStream();
				DataInputStream in = new DataInputStream(kkSocket.getInputStream());
				
				String sessionId = null;
				
				if (this.aesCiphers == null || this.config.getUserId().equals(User.ACTIVATION_USER_ID))
				{
					sessionId = CryptoLib.NULL_UUID;
				}
				else
				{
					sessionId = this.aesCiphers.sessionId;
				}
				
				RequestMessageBase requestMessageUserId = 
						new RequestMessageBase(
								sessionId,
								getBuild(),
								new Payload(this.config.getUserId()));
								
				CryptoLib.sendStringRsaEncrypted(
						out, 
						requestMessageUserId.serialize(), 
						this.config.getServerPublicKeyObject());
				
				String token = null;
				
				if (this.config.getUserId().equals(User.ACTIVATION_USER_ID))
				{
					token = CryptoLib.NULL_UUID;
				}
				else
				{
					ResponseMessage responseMessageUserId = 
							(ResponseMessage) ResponseMessage.deserialize(
									CryptoLib.receiveStringRsaEncrypted(
											in, 
											this.config.getUserPrivateKeyObject()));	
					
					PayloadResponseMessageUserId payloadUserId = (PayloadResponseMessageUserId) responseMessageUserId.getPayloadObject();
					
					if (payloadUserId.getServerClientBuildCheck() != null)
					{
						TextProperty textPropertyError = null;
						
						if (!payloadUserId.getServerClientBuildCheck().areBuildsCompatible())
						{
							textPropertyError = TextProperties.IncomptabileBuilds(
									payloadUserId.getServerClientBuildCheck().getMinimumComptabileBuild(),
			    					getBuild());
						}
						else
						{
							ServerClientBuildCheckResult serverClientBuildCheck = 
									this.checkServerClientBuild(responseMessageUserId.getServerBuild());
							
							if (!serverClientBuildCheck.areBuildsCompatible())
							{
								textPropertyError = TextProperties.ServerBuildOutdated(
										responseMessageUserId.getServerBuild(),
		    							serverClientBuildCheck.getMinimumComptabileBuild());
							}
						}
						
					    if (textPropertyError != null)
					    {
					    	responseMessage = new ResponseMessage(
									false,
									TextProperties.getMessageText(textPropertyError),
									null);
					    	responseMessage.setServerBuild(this.getBuild());
					    	
					    	kkSocket.close();
					    	
					    	return responseMessage;
					    }
					}
					
					sessionId = payloadUserId.isSessionValid() ? sessionId : CryptoLib.NULL_UUID;
					token = payloadUserId.getToken();
				}
				
				if (sessionId.equals(CryptoLib.NULL_UUID))
				{
					this.aesCiphers = CryptoLib.diffieHellmanKeyAgreementClient(in, out);
				}
				
				RequestMessage requestMessage = new RequestMessage(
														type,
														sessionId,
														token,
														this.getBuild(),
														payload);
					
				CryptoLib.sendStringAesEncrypted(
						out, 
						requestMessage.serialize(), 
						this.aesCiphers.cipherEncrypt);
				
				responseMessage = 
						(ResponseMessage) ResponseMessage.deserialize(
								CryptoLib.receiveStringAesEncrypted(in, this.aesCiphers.cipherDecrypt));
				
				if (type == RequestMessageType.ESTABLISH_NOTIFICATION_SOCKET &&
						this.establishNotificationSocket &&
						responseMessage != null && 
						responseMessage.isSuccess())
				{
					this.establishNotificationSocket(kkSocket);
				}
				else
				{
					kkSocket.close();
				}
				
				if (responseMessage.getMessage() == null &&
					requestMessage.getType() != RequestMessageType.CUSTOM)
				{
					responseMessage.setMessage(TextProperties.getMessageText(responseMessage.getTextProperty()));
				}
				
				if (this.config.getUserId().equals(User.ACTIVATION_USER_ID))
				{
					this.aesCiphers = null;
				}
			}
		    catch (EOFException e)
			{
		    	responseMessage = new ResponseMessage(
		    								false,
		    								TextProperties.getMessageText(TextProperties.ConnectionClosed()),
		    								null);
		    	responseMessage.setServerBuild(this.getBuild());
		    	
		    	return responseMessage;
			}
		    catch (Exception e)
			{
		    	responseMessage = new ResponseMessage(
						false,
						TextProperties.getMessageText(TextProperties.NoConnectionToServer(e.getMessage())),
						null);
		    	responseMessage.setServerBuild(this.getBuild());
		    	
		    	return responseMessage;
			}
		}
		
		return responseMessage;
	}
	
	/**
	 * Check if the client and server builds are compatible.
	 * @param serverBuild Server build
	 * @return If the builds are compatible plus the minimum required client build
	 */
	protected abstract ServerClientBuildCheckResult checkServerClientBuild(String serverBuild);
	
	/**
	 * Get the client build. Return null, if you want to disable build checks.
	 * @return The client build
	 */
	protected abstract String getBuild();
	
	/**
	 * Called when the connection status of the notification socket changes.
	 * @param connected True, if the notification socket is connected and can receive notifications
	 */
	protected abstract void onConnectionStatusChanged(boolean connected);
	
	/**
	 * Is called when notifications is received through the notification socket.
	 * @param sender Sender of the notification
	 * @param recipients Recipients of the notification
	 * @param dateCreated Date when the notification was created
	 * @param payload Payload of the notification
	 */
	protected abstract void onNotificationReceived(
			String sender,
			ArrayList<String> recipients,
			long dateCreated,
			Object payload);
	
	private void establishNotificationSocket(Socket kkSocket)
	{
		ClientNotificationReceiverThread t = this.new ClientNotificationReceiverThread(kkSocket);
		t.start();
	}
	
	/**
	 * Check if the notification socket is connected. If not, try to reconnect it.
	 */
	private void reconnectCheck()
	{
		if (!this.connected && this.notificationReceiverThread == null)
		{
			this.sendRequestMessage(RequestMessageType.ESTABLISH_NOTIFICATION_SOCKET, null);
		}
	}
	
	private void setConnectionStatus(boolean connected)
	{
		this.connected = connected;
		this.onConnectionStatusChanged(connected);
		
		if (this.notificationReceiverThread != null && !connected)
		{
			this.notificationReceiverThread.interrupt();
			this.notificationReceiverThread = null;
		}
	}
	
	private class ClientNotificationReceiverThread extends Thread
	{
		private Socket socket;
		
		ClientNotificationReceiverThread(Socket socket)
		{
			this.socket = socket;
		}
		
		public void run()
		{
			try
			{
				this.socket.setSoTimeout(0);
				DataInputStream in = new DataInputStream(this.socket.getInputStream());
				
				do
				{
					setConnectionStatus(true);
					
					Notifications notifications = 
							(Notifications) Notifications.deserialize(
								CryptoLib.receiveStringRsaEncrypted(
									in, 
									getConfig().getUserPrivateKeyObject()));
					
					if (notifications != null)
					{
						PushNotificationForwarderThread pushNotificationReceivedThread =
								new PushNotificationForwarderThread(notifications);
						pushNotificationReceivedThread.start();
					}
					
				} while (true);
			}
			catch (Exception x)
			{
				
			}
			
			setConnectionStatus(false);
		}
		
		void disconnect()
		{
			try
			{
				this.socket.close();
			}
			catch (Exception x)
			{
				
			}
		}
	}
	
	private class ClientReconnectThread extends Thread
	{
		public void run()
		{
			do
			{
				reconnectCheck();
				
				try {
					Thread.sleep(10000);
				} catch (Exception e)
				{
					break;
				}
				
			} while (true);
		}
	}
	
	private class PushNotificationForwarderThread extends Thread
	{
		private Notifications notifications;
		
		private PushNotificationForwarderThread(Notifications notifications)
		{
			this.notifications = notifications;
		}
		
		public void run()
		{
			for (Notification notification: this.notifications.getNotifications())
			{
				if (notification.isPing())
					continue;
				
				try
				{
					sendRequestMessage(
							RequestMessageType.PUSH_NOTIFICATION_RECEIVED,
							new Payload(notification.getId()));
					
					onNotificationReceived(
							notification.getSender(),
							notification.getRecipients(),
							notification.getDateCreated(),
							notification.getPayloadObject(getConfig().getUserPrivateKeyObject()));
				}
				catch (Exception x) {}
			}
		}
	}
}
