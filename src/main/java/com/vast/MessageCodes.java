package com.vast;

public class MessageCodes {
	// Server -> Client
	public static short ENTITY_CREATED = 0;
	public static byte ENTITY_CREATED_ENTITY_ID = 0;
	public static byte ENTITY_CREATED_REASON = 1;

	// Server -> Client
	public static short ENTITY_DESTROYED = 1;
	public static byte ENTITY_DESTROYED_ENTITY_ID = 0;
	public static byte ENTITY_DESTROYED_REASON = 1;

	// Server -> Client
	public static short UPDATE_PROPERTIES = 2;
	public static byte UPDATE_PROPERTIES_ENTITY_ID = 0;

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

	// Properties (TODO: should maybe be moved to own enum?)
	public static byte PROPERTY_TYPE = 100;
	public static byte PROPERTY_POSITION = 101;
	public static byte PROPERTY_OWNER = 102;
	public static byte PROPERTY_ACTIVE = 103;
	public static byte PROPERTY_DURABILITY = 104;
	public static byte PROPERTY_PROGRESS = 105;
	public static byte PROPERTY_INTERACTABLE = 106;
}
