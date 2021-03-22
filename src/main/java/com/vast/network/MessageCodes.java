package com.vast.network;

public class MessageCodes {
	// Server -> Client
	public static short ENTITY_CREATED = 0;
	public static byte ENTITY_CREATED_ENTITY_ID = 0;
	public static byte ENTITY_CREATED_TYPE = 1;
	public static byte ENTITY_CREATED_SUB_TYPE = 2;
	public static byte ENTITY_CREATED_OWNER = 3;
	public static byte ENTITY_CREATED_REASON = 4;
	public static byte ENTITY_CREATED_PROPERTIES = 5;

	// Server -> Client
	public static short ENTITY_DESTROYED = 1;
	public static byte ENTITY_DESTROYED_ENTITY_ID = 0;
	public static byte ENTITY_DESTROYED_REASON = 1;

	// Server -> Client
	public static short UPDATE_PROPERTIES = 2;
	public static byte UPDATE_PROPERTIES_ENTITY_ID = 0;
	public static byte UPDATE_PROPERTIES_PROPERTIES = 1;

	// Server -> Client
	public static short EVENT = 3;
	public static byte EVENT_ENTITY_ID = 0;
	public static byte EVENT_TYPE = 1;
	public static byte EVENT_VALUE = 2;

	// Client -> Server
	public static short MOVE = 50;
	public static byte MOVE_POSITION = 0;

	// Client -> Server
	public static short INTERACT = 51;
	public static byte INTERACT_ENTITY_ID = 0;

	// Client -> Server
	public static short BUILD_START = 52;
	public static byte BUILD_START_RECIPE_ID = 0;

	// Client -> Server
	public static short BUILD_MOVE = 53;
	public static byte BUILD_MOVE_DIRECTION = 0;

	// Client -> Server
	public static short BUILD_ROTATE = 54;
	public static byte BUILD_ROTATE_DIRECTION = 0;

	// Client -> Server
	public static short BUILD_CONFIRM = 55;

	// Client -> Server
	public static short BUILD_CANCEL = 56;

	// Client -> Server
	public static short EMOTE = 57;
	public static byte EMOTE_TYPE = 0;

	// Client -> Server
	public static short SET_HOME = 58;

	// Client -> Server
	public static short CRAFT = 59;
	public static byte CRAFT_RECIPE_ID = 0;

	// Client -> Server
	public static short PLANT = 60;

	// Client -> Server
	public static short FOLLOW = 61;
	public static byte FOLLOW_ENTITY_ID = 0;

	// Client -> Server
	public static short CHAT = 62;
	public static byte CHAT_WORD = 0;
}
