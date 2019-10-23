package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Skill;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;

import java.util.HashMap;
import java.util.Map;

public class SkillPropertyHandler implements PropertyHandler {
	private ComponentMapper<Skill> skillMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private final int WORD_LEVEL_THRESHOLD = 1;

	@Override
	public byte getProperty() {
		return Properties.SKILL;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (skillMapper.has(entity)) {
			Skill skill = skillMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			boolean sync = false;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.SKILL)) {
				Map<String, Byte> syncedSkillWords = (Map<String, Byte>) syncHistory.syncedValues.get(Properties.SKILL);
				for (int i = 0; i < skill.words.length; i++) {
					String word = skill.words[i];
					if (syncedSkillWords.containsKey(word)) {
						if (skill.wordLevels[i] - syncedSkillWords.get(word) >= WORD_LEVEL_THRESHOLD ||
							(syncedSkillWords.get(word) < 100 && skill.wordLevels[i] == 100)) {
							sync = true;
							break;
						}
					} else {
						if (skill.wordLevels[i] >= WORD_LEVEL_THRESHOLD) {
							sync = true;
							break;
						}
					}
				}
			}
			if (force || sync) {
				DataObject skillDataObject = new DataObject();
				skillDataObject.set((byte) 0, skill.words);
				skillDataObject.set((byte) 1, skill.wordLevels);
				dataObject.set(Properties.SKILL, skillDataObject);
				if (syncHistory != null) {
					Map<String, Byte> syncedSkillWords = new HashMap<>();
					for (int i = 0; i < skill.words.length; i++) {
						syncedSkillWords.put(skill.words[i], skill.wordLevels[i]);
					}
					syncHistory.syncedValues.put(Properties.SKILL, syncedSkillWords);
				}
				return true;
			}
		}
		return false;
	}
}
