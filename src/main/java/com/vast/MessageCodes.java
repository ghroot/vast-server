package com.vast;

public class MessageCodes {
	// Server -> Client
	public static short ENTITY_CREATED = 0;
	public static byte ENTITY_CREATED_ENTITY_ID = 0;
	public static byte ENTITY_CREATED_TYPE = 1;
	public static byte ENTITY_CREATED_OWNER = 2;
	public static byte ENTITY_CREATED_REASON = 3;

	// Server -> Client
	public static short ENTITY_DESTROYED = 1;
	public static byte ENTITY_DESTROYED_ENTITY_ID = 0;
	public static byte ENTITY_DESTROYED_REASON = 1;

	// Server -> Client
	public static short UPDATE_PROPERTIES = 2;
	public static byte UPDATE_PROPERTIES_ENTITY_ID = 0;

	// Server -> Client
	public static short EVENT = 3;
	public static byte EVENT_ENTITY_ID = 0;
	public static byte EVENT_EVENT = 1;

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

	// Properties
	public static byte PROPERTY_POSITION = 100;
	public static byte PROPERTY_ACTIVE = 101;
	public static byte PROPERTY_DURABILITY = 103;
	public static byte PROPERTY_PROGRESS = 104;
	public static byte PROPERTY_HEALTH = 105;
	public static byte PROPERTY_MAX_HEALTH = 106;
	public static byte PROPERTY_INTERACTABLE = 107;
}
