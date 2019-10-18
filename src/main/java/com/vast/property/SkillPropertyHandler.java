package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Skill;
import com.vast.network.Properties;

public class SkillPropertyHandler implements PropertyHandler {
	private ComponentMapper<Skill> skillMapper;

	@Override
	public byte getProperty() {
		return Properties.SKILL;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (skillMapper.has(entity)) {
			Skill skill = skillMapper.get(entity);
			DataObject skillDataObject = new DataObject();
			skillDataObject.set((byte) 0, skill.words);
			skillDataObject.set((byte) 1, skill.wordLevels);
			dataObject.set(Properties.SKILL, skillDataObject);
			return true;
		}
		return false;
	}
}
