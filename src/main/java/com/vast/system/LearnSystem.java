package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.network.Properties;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LearnSystem extends IteratingSystem {
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Skill> skillMapper;
	private ComponentMapper<Teach> teachMapper;
	private ComponentMapper<Sync> syncMapper;

	private Set<String> reusableWords = new HashSet<>();
	private Random random = new Random();

	public LearnSystem() {
		super(Aspect.all(Player.class, Active.class, Skill.class));
	}

	@Override
	protected void inserted(int skillEntity) {
		Skill skill = skillMapper.get(skillEntity);
		skill.countdown = skill.interval;
	}

	@Override
	public void removed(IntBag entities) {
		super.removed(entities);
	}

	@Override
	protected void process(int skillEntity) {
		Skill skill = skillMapper.get(skillEntity);

		skill.countdown -= world.getDelta();
		if (skill.countdown <= 0f) {
			reusableWords.clear();

			IntBag knowEntitiesBag = activeMapper.get(skillEntity).knowEntities;
			int[] knowEntities = knowEntitiesBag.getData();
			for (int i = 0, size = knowEntitiesBag.size(); i < size; ++i) {
				int knowEntity = knowEntities[i];
				if (teachMapper.has(knowEntity)) {
					for (String word : teachMapper.get(knowEntity).words) {
						reusableWords.add(word);
					}
				}
			}

			for (String word : reusableWords) {
				skill.increaseWordLevel(word);
			}

			if (reusableWords.size() > 0) {
				syncMapper.create(skillEntity).markPropertyAsDirty(Properties.SKILL);
			}

			skill.countdown = skill.interval;
		}
	}
}
