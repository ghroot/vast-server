package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.system.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(AttackInteractionHandler.class);

	private ComponentMapper<Attack> attackMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Death> deathMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

	private Items items;

	public AttackInteractionHandler(Items items) {
		super(Aspect.all(Attack.class, Inventory.class), Aspect.all(Health.class));
		this.items = items;
	}

	@Override
	public boolean canInteract(int attackEntity, int healthEntity) {
		if (healthMapper.get(healthEntity).isDead()) {
			return false;
		}
		if (playerMapper.has(attackEntity) && ownerMapper.has(healthEntity) &&
				playerMapper.get(attackEntity).name.equals(ownerMapper.get(healthEntity).name)) {
			return false;
		}
		return true;
	}

	@Override
	public void start(int attackEntity, int healthEntity) {
	}

	@Override
	public boolean process(int attackEntity, int healthEntity) {
		Attack attack = attackMapper.get(attackEntity);

		if (!attack.implicitWeapon && !hasWeapon(attackEntity)) {
			messageMapper.create(attackEntity).text = "I need a weapon!";
			return true;
		} else {
			float time = world.getSystem(TimeManager.class).getTime();
			if (time - attack.lastAttackTime >= attack.cooldown) {
				Health health = healthMapper.get(healthEntity);
				health.takeDamage(1);
				syncMapper.create(healthEntity).markPropertyAsDirty(Properties.HEALTH);
				eventMapper.create(attackEntity).name = "attacked";
				logger.debug("Entity {} is attacking entity {}, health left: {}", attackEntity, healthEntity, health.health);
				if (health.isDead()) {
					logger.debug("Entity {} killed entity {}", attackEntity, healthEntity);
					deathMapper.create(healthEntity);
					return true;
				}
				attack.lastAttackTime = time;
			}
			return false;
		}
	}

	@Override
	public void stop(int attackEntity, int healthEntity) {
	}

	private boolean hasWeapon(int entity) {
		Inventory inventory = inventoryMapper.get(entity);
		for (int itemId = 0; itemId < inventory.items.length; itemId++) {
			if (items.getItem(itemId).getType().equals("weapon")) {
				return true;
			}
		}
		return false;
	}
}
