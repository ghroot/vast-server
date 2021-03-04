package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Skill;
import com.vast.network.Properties;

import java.util.HashMap;
import java.util.Map;

public class SkillPropertyHandler extends AbstractPropertyHandler<Map<String, Byte>, DataObject> {
	private ComponentMapper<Skill> skillMapper;

	private int wordLevelThreshold;

	private DataObject reusableSkillDataObject;

	public SkillPropertyHandler(int wordLevelThreshold) {
		super(Properties.SKILL);

		this.wordLevelThreshold = wordLevelThreshold;

		reusableSkillDataObject = new DataObject();
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return skillMapper.has(entity);
	}

	@Override
	protected Map<String, Byte> getPropertyData(int entity) {
		Skill skill = skillMapper.get(entity);
		Map<String, Byte> skillWords = new HashMap<>();
		for (int i = 0; i < skill.words.length; i++) {
			skillWords.put(skill.words[i], skill.wordLevels[i]);
		}

		return skillWords;
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Map<String, Byte> lastSyncedSkillWords) {
		Skill skill = skillMapper.get(entity);
		for (int i = 0; i < skill.words.length; i++) {
			int lastSyncedLevel = (int) lastSyncedSkillWords.getOrDefault(skill.words[i], (byte) 0);
			int newLevel = skill.wordLevels[i];
			if (newLevel - lastSyncedLevel >= wordLevelThreshold) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected DataObject convertPropertyDataToDataObjectData(Map<String, Byte> skillWords) {
		reusableSkillDataObject.clear();
		reusableSkillDataObject.set((byte) 0, skillWords.keySet().toArray());
		reusableSkillDataObject.set((byte) 1, skillWords.values().toArray());

		return reusableSkillDataObject;
	}
}
