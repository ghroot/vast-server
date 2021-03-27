package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Skill;
import com.vast.network.Properties;

import java.util.HashMap;
import java.util.Map;

public class SkillPropertyHandler extends AbstractPropertyHandler<Map<String, Integer>, DataObject> {
	private ComponentMapper<Skill> skillMapper;

	private int xpThreshold;

	private DataObject reusableSkillDataObject;

	public SkillPropertyHandler(int xpThreshold) {
		super(Properties.SKILL);

		this.xpThreshold = xpThreshold;

		reusableSkillDataObject = new DataObject();
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return skillMapper.has(entity);
	}

	@Override
	protected Map<String, Integer> getPropertyData(int entity) {
		Skill skill = skillMapper.get(entity);
		Map<String, Integer> skillXp = new HashMap<>();
		for (int i = 0; i < skill.names.length; i++) {
			skillXp.put(skill.names[i], skill.xp[i]);
		}

		return skillXp;
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Map<String, Integer> lastSyncedSkillXp) {
		Skill skill = skillMapper.get(entity);
		for (int i = 0; i < skill.names.length; i++) {
			int lastSyncedLXp = lastSyncedSkillXp.getOrDefault(skill.names[i], 0);
			int newXp = skill.xp[i];
			if (newXp - lastSyncedLXp >= xpThreshold) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected DataObject convertPropertyDataToDataObjectData(Map<String, Integer> skillXp) {
		String[] names = new String[skillXp.size()];
		int[] xp = new int[skillXp.size()];
		int index = 0;
		for (String name : skillXp.keySet()) {
			names[index] = name;
			xp[index] = skillXp.get(name);
			index++;
		}

		reusableSkillDataObject.clear();
		reusableSkillDataObject.set((byte) 0, names);
		reusableSkillDataObject.set((byte) 1, xp);

		return reusableSkillDataObject;
	}
}
