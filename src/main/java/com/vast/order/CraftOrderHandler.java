package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Craft;
import com.vast.component.Inventory;
import com.vast.component.Message;
import com.vast.data.CraftableItem;
import com.vast.data.Items;
import com.vast.network.MessageCodes;

public class CraftOrderHandler implements OrderHandler {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Message> messageMapper;

	private Items items;

	public CraftOrderHandler(Items items) {
		this.items = items;
	}

	@Override

	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.CRAFT;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !craftMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		craftMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		int itemId = (byte) dataObject.get(MessageCodes.CRAFT_ITEM_TYPE).value;
		CraftableItem itemToCraft = (CraftableItem) items.getItem(itemId);
		Inventory inventory = inventoryMapper.get(orderEntity);
		if (inventory.has(itemToCraft.getCosts())) {
			craftMapper.create(orderEntity).countdown = itemToCraft.getCraftDuration();
			craftMapper.get(orderEntity).itemId = itemId;
			return true;
		} else {
			messageMapper.create(orderEntity).text = "I don't have the required materials...";
			return false;
		}
	}
}
