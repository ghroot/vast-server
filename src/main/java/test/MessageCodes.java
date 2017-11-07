package test;

public class MessageCodes {
	// Server -> Client
	public static short PEER_ENTITY_CREATED = 0;
	public static byte PEER_ENTITY_CREATED_ENTITY_ID = 0;
	public static byte PEER_ENTITY_CREATED_OWNER = 1;
	public static byte PEER_ENTITY_CREATED_ACTIVE = 2;
	public static byte PEER_ENTITY_CREATED_POSITION = 3;

	// Server -> Client
	public static short ENTITY_CREATED = 1;
	public static byte ENTITY_CREATED_ENTITY_ID = 0;
	public static byte ENTITY_CREATED_TYPE = 1;
	public static byte ENTITY_CREATED_POSITION = 2;

	// Server -> Client
	public static short ENTITY_DESTROYED = 2;
	public static byte ENTITY_DESTROYED_ENTITY_ID = 0;

	// Server -> Client
	public static short SET_POSITION = 3;
	public static byte SET_POSITION_ENTITY_ID = 0;
	public static byte SET_POSITION_POSITION = 1;

	// Server -> Client
	public static short PEER_ENTITY_ACTIVATED = 4;
	public static byte PEER_ENTITY_ACTIVATED_ENTITY_ID = 0;

	// Server -> Client
	public static short PEER_ENTITY_DEACTIVATED = 5;
	public static byte PEER_ENTITY_DEACTIVATED_ENTITY_ID = 0;

	// Client -> Server
	public static short SET_PATH = 100;
	public static byte SET_PATH_POSITION = 0;
}
