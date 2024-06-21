package spielwitz.biDiServer;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
   * This class was created with the Text Properties Builder from the resource file
   *
   *   TextResources_en_US
   *
   * The resource file is maintained with the Eclipse-Plugin ResourceBundle Editor.
   */
class TextProperties 
{
	private static String languageCode;
	private static ResourceBundle messages;

	static {
		setLocale("en-US");
	}

	static void setLocale(String newLanguageCode){
		languageCode = newLanguageCode;
		String[] language = languageCode.split("-");
		Locale currentLocale = new Locale(language[0], language[1]);
		messages = ResourceBundle.getBundle("TextResources", currentLocale);
	}

	static String getLocale(){
		return languageCode;
	}

	static String getMessageText(TextProperty textProperty){
		if (textProperty != null)
			return MessageFormat.format(messages.getString(textProperty.getKey()), textProperty.getArgs());
		else
			return null;
	}

	/**
	   * Application error on the server: {0}
	   */
	static TextProperty ApplicationError(String arg0) {
		return new TextProperty("ApplicationError", new String[] {arg0});
	}

	/**
	   * Closing server socket.
	   */
	static TextProperty ClosingSocketServer() {
		return new TextProperty("ClosingSocketServer");
	}

	/**
	   * Connection closed.
	   */
	static TextProperty ConnectionClosed() {
		return new TextProperty("ConnectionClosed");
	}

	/**
	   * Creating admin user...
	   */
	static TextProperty CreatingAdmin() {
		return new TextProperty("CreatingAdmin");
	}

	/**
	   * Creating folder {0}...
	   */
	static TextProperty CreatingFolder(String arg0) {
		return new TextProperty("CreatingFolder", new String[] {arg0});
	}

	/**
	   * A data set with the ID {0} already exists.
	   */
	static TextProperty DataSetIdExists(String arg0) {
		return new TextProperty("DataSetIdExists", new String[] {arg0});
	}

	/**
	   * The data set ID contains invalid characters: {0}
	   */
	static TextProperty DataSetIdInvalidCharacters(String arg0) {
		return new TextProperty("DataSetIdInvalidCharacters", new String[] {arg0});
	}

	/**
	   * A data set with the ID {0} does not exist.
	   */
	static TextProperty DataSetIdNotExists(String arg0) {
		return new TextProperty("DataSetIdNotExists", new String[] {arg0});
	}

	/**
	   * The data set ID must not be longer than {0} characters.
	   */
	static TextProperty DataSetIdTooLong(String arg0) {
		return new TextProperty("DataSetIdTooLong", new String[] {arg0});
	}

	/**
	   * The data set ID must be at least {0} characters long.
	   */
	static TextProperty DataSetIdTooShort(String arg0) {
		return new TextProperty("DataSetIdTooShort", new String[] {arg0});
	}

	/**
	   * User {0} is not authorized to read data set {1}.
	   */
	static TextProperty DataSetUserNotAuthorized(String arg0, String arg1) {
		return new TextProperty("DataSetUserNotAuthorized", new String[] {arg0, arg1});
	}

	/**
	   * User {0} is not authorized to delete data set {1}.
	   */
	static TextProperty DataSetUserNotAuthorizedDelete(String arg0, String arg1) {
		return new TextProperty("DataSetUserNotAuthorizedDelete", new String[] {arg0, arg1});
	}

	/**
	   * User {0} is not authorized to update data set {1}.
	   */
	static TextProperty DataSetUserNotAuthorizedUpdate(String arg0, String arg1) {
		return new TextProperty("DataSetUserNotAuthorizedUpdate", new String[] {arg0, arg1});
	}

	/**
	   * {1}/{0}/{2} {3}:{4}:{5}
	   */
	static TextProperty DateFormat(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
		return new TextProperty("DateFormat", new String[] {arg0, arg1, arg2, arg3, arg4, arg5});
	}

	/**
	   * Diffie\u2013Hellman key exchange failed: {0}
	   */
	static TextProperty DiffieHellmanKeyExchangeFailed(String arg0) {
		return new TextProperty("DiffieHellmanKeyExchangeFailed", new String[] {arg0});
	}

	/**
	   * File {0} created.
	   */
	static TextProperty FileCreated(String arg0) {
		return new TextProperty("FileCreated", new String[] {arg0});
	}

	/**
	   * The server requires at least build {0}. The client is using build {1}.
	   */
	static TextProperty IncomptabileBuilds(String arg0, String arg1) {
		return new TextProperty("IncomptabileBuilds", new String[] {arg0, arg1});
	}

	/**
	   * Wrong token. Authorization failed.
	   */
	static TextProperty InvalidToken() {
		return new TextProperty("InvalidToken");
	}

	/**
	   * Date
	   */
	static TextProperty LogDate() {
		return new TextProperty("LogDate");
	}

	/**
	   * Event ID
	   */
	static TextProperty LogEventId() {
		return new TextProperty("LogEventId");
	}

	/**
	   * IP Address
	   */
	static TextProperty LogIpAddress() {
		return new TextProperty("LogIpAddress");
	}

	/**
	   * Level
	   */
	static TextProperty LogLevel() {
		return new TextProperty("LogLevel");
	}

	/**
	   * Message
	   */
	static TextProperty LogMessage() {
		return new TextProperty("LogMessage");
	}

	/**
	   * Logon attempt with inactive user [{0}].
	   */
	static TextProperty LogOnWithInactiveUser(String arg0) {
		return new TextProperty("LogOnWithInactiveUser", new String[] {arg0});
	}

	/**
	   * Log-on attempt with invalid user ID length {0}
	   */
	static TextProperty LogOnWithInvalidUserNameLength(String arg0) {
		return new TextProperty("LogOnWithInvalidUserNameLength", new String[] {arg0});
	}

	/**
	   * Payload
	   */
	static TextProperty LogPayload() {
		return new TextProperty("LogPayload");
	}

	/**
	   * Request
	   */
	static TextProperty LogRequestMessageType() {
		return new TextProperty("LogRequestMessageType");
	}

	/**
	   * User
	   */
	static TextProperty LogUser() {
		return new TextProperty("LogUser");
	}

	/**
	   * Connection to server could not be established: {0}
	   */
	static TextProperty NoConnectionToServer(String arg0) {
		return new TextProperty("NoConnectionToServer", new String[] {arg0});
	}

	/**
	   * User {0} is not authorized for this action.
	   */
	static TextProperty NotAuthorized(String arg0) {
		return new TextProperty("NotAuthorized", new String[] {arg0});
	}

	/**
	   * An unexpected error occurred in the notification socket of user {0}. The socket is being closed: {1}
	   */
	static TextProperty NotificationSocketUnexpectedError(String arg0, String arg1) {
		return new TextProperty("NotificationSocketUnexpectedError", new String[] {arg0, arg1});
	}

	/**
	   * OK
	   */
	static TextProperty Ok() {
		return new TextProperty("Ok");
	}

	/**
	   * Reading data set {0}...
	   */
	static TextProperty ReadingDataSet(String arg0) {
		return new TextProperty("ReadingDataSet", new String[] {arg0});
	}

	/**
	   * Reading user {0}...
	   */
	static TextProperty ReadingUser(String arg0) {
		return new TextProperty("ReadingUser", new String[] {arg0});
	}

	/**
	   * Error when decrypting the request message: {0}
	   */
	static TextProperty RequestDecryptionError(String arg0) {
		return new TextProperty("RequestDecryptionError", new String[] {arg0});
	}

	/**
	   * Error when receiving the request: {0}
	   */
	static TextProperty RequestReceiveError(String arg0) {
		return new TextProperty("RequestReceiveError", new String[] {arg0});
	}

	/**
	   * Error when sending the response message: {0}
	   */
	static TextProperty ResponseError(String arg0) {
		return new TextProperty("ResponseError", new String[] {arg0});
	}

	/**
	   * Implementation error: Response message not set in message processing container. Connection closed.
	   */
	static TextProperty ResponseMessageNotSet() {
		return new TextProperty("ResponseMessageNotSet");
	}

	/**
	   * The server is using build {0}. Your client expects a server build of at least {1}.
	   */
	static TextProperty ServerBuildOutdated(String arg0, String arg1) {
		return new TextProperty("ServerBuildOutdated", new String[] {arg0, arg1});
	}

	/**
	   * Server {0}:{1} cannot be started. The port might already be in use. Application terminated.
	   */
	static TextProperty ServerNotStarted(String arg0, String arg1) {
		return new TextProperty("ServerNotStarted", new String[] {arg0, arg1});
	}

	/**
	   * Error when accepting the socket connection: {0}
	   */
	static TextProperty ServerSocketAcceptError(String arg0) {
		return new TextProperty("ServerSocketAcceptError", new String[] {arg0});
	}

	/**
	   * Server {0}:{1} started
	   */
	static TextProperty ServerStarted(String arg0, String arg1) {
		return new TextProperty("ServerStarted", new String[] {arg0, arg1});
	}

	/**
	   * The server was shut down down successfully.
	   */
	static TextProperty ShutdownDone() {
		return new TextProperty("ShutdownDone");
	}

	/**
	   * Error when shutting down the server: {0}
	   */
	static TextProperty ShutdownError(String arg0) {
		return new TextProperty("ShutdownError", new String[] {arg0});
	}

	/**
	   * User {0} could not be activated.
	   */
	static TextProperty UserActivationError(String arg0) {
		return new TextProperty("UserActivationError", new String[] {arg0});
	}

	/**
	   * User {0} is already active.
	   */
	static TextProperty UserAlreadyActive(String arg0) {
		return new TextProperty("UserAlreadyActive", new String[] {arg0});
	}

	/**
	   * User {0} cannot be deleted.
	   */
	static TextProperty UserCannotBeDeleted(String arg0) {
		return new TextProperty("UserCannotBeDeleted", new String[] {arg0});
	}

	/**
	   * The user ID {0} already exists.
	   */
	static TextProperty UserIdExists(String arg0) {
		return new TextProperty("UserIdExists", new String[] {arg0});
	}

	/**
	   * The user ID contains invalid characters: {0}
	   */
	static TextProperty UserIdInvalidCharacters(String arg0) {
		return new TextProperty("UserIdInvalidCharacters", new String[] {arg0});
	}

	/**
	   * The user ID {0} does not exist.
	   */
	static TextProperty UserIdNotExists(String arg0) {
		return new TextProperty("UserIdNotExists", new String[] {arg0});
	}

	/**
	   * The user ID {0} is reserved.
	   */
	static TextProperty UserIdReserved(String arg0) {
		return new TextProperty("UserIdReserved", new String[] {arg0});
	}

	/**
	   * The user ID must not be longer than {0} characters.
	   */
	static TextProperty UserIdTooLong(String arg0) {
		return new TextProperty("UserIdTooLong", new String[] {arg0});
	}

	/**
	   * The user ID must be at least {0} characters long.
	   */
	static TextProperty UserIdTooShort(String arg0) {
		return new TextProperty("UserIdTooShort", new String[] {arg0});
	}
}