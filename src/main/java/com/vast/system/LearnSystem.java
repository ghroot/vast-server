package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Learn;
import com.vast.component.Player;
import com.vast.component.SkillEmission;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LearnSystem extends IteratingSystem {
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Learn> learnMapper;
	private ComponentMapper<SkillEmission> skillEmissionMapper;

	private Set<String> reusableWords = new HashSet<>();
	private Random random = new Random();

	public LearnSystem() {
		super(Aspect.all(Player.class, Active.class, Learn.class));
	}

	@Override
	protected void inserted(int learnEntity) {
		Learn learn = learnMapper.get(learnEntity);
		learn.countdown = learn.interval;
	}

	@Override
	public void removed(IntBag entities) {
		super.removed(entities);
	}

	@Override
	protected void process(int learnEntity) {
		Learn learn = learnMapper.get(learnEntity);

		learn.countdown -= world.getDelta();
		if (learn.countdown <= 0f) {
			reusableWords.clear();

			IntBag knowEntitiesBag = activeMapper.get(learnEntity).knowEntities;
			int[] knowEntities = knowEntitiesBag.getData();
			for (int i = 0, size = knowEntitiesBag.size(); i < size; ++i) {
				int knowEntity = knowEntities[i];
				if (skillEmissionMapper.has(knowEntity)) {
					for (String word : skillEmissionMapper.get(knowEntity).words) {
						reusableWords.add(word);
					}
				}
			}

			if (reusableWords.size() > 0) {
				String randomWord = getRandomWord(reusableWords);
				if (learn.words.containsKey(randomWord)) {
					int currentWordLevel = learn.words.get(randomWord);
					if (currentWordLevel < 100) {
						learn.words.put(randomWord, (byte) (currentWordLevel + 1));
					}
				} else {
					learn.words.put(randomWord, (byte) 1);
				}
			}

			learn.countdown = learn.interval;
		}
	}

	private String getRandomWord(Set<String> words) {
		int size = words.size();
		int randomIndex = random.nextInt(size);
		int i = 0;
		for (String word : words) {
			if (i == randomIndex) {
				return word;
			}
			i++;
		}
		return null;
	}
}
