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
	public static short BUILD = 52;
	public static byte BUILD_TYPE = 0;
	public static byte BUILD_POSITION = 1;
	public static byte BUILD_ROTATION = 2;

	// Client -> Server
	public static short EMOTE = 53;
	public static byte EMOTE_TYPE = 0;

	// Client -> Server
	public static short SET_HOME = 54;

	// Client -> Server
	public static short CRAFT = 55;
	public static byte CRAFT_ITEM_TYPE = 0;

	// Client -> Server
	public static short PLANT = 56;

	// Client -> Server
	public static short FOLLOW = 57;
	public static byte FOLLOW_ENTITY_ID = 0;

	// Client -> Server
	public static short CHAT = 58;
	public static byte CHAT_WORD = 0;
}
