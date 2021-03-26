package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Skill;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class TestSkillPropertyHandler {
    private SkillPropertyHandler skillPropertyHandler;
    private World world;
    private ComponentMapper<Skill> skillMapper;
    private int entity;

    @Before
    public void setUp() {
        skillPropertyHandler = new SkillPropertyHandler(5);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(skillPropertyHandler);

        skillMapper = world.getMapper(Skill.class);

        entity = world.create();
    }

    @Test
    public void givenHasSkill_decoratesDataObject() {
        skillMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        DataObject skillDataObject = (DataObject) dataObject.get(Properties.SKILL).value;
        String[] words = (String[]) skillDataObject.get((byte) 0).value;
        byte[] wordLevels = (byte[]) skillDataObject.get((byte) 1).value;
        Assert.assertArrayEquals(new String[0], words);
        Assert.assertArrayEquals(new byte[0], wordLevels);
    }

    @Test
    public void givenNoSkillChange_doesNotDecorateDataObject() {
        skillMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        syncHistory.syncedValues.put(Properties.SKILL, new HashMap<String, Byte>());

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.SKILL));
    }

    @Test
    public void givenSmallSkillChange_doesNotDecorateDataObject() {
        Skill skill = skillMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Changed from [] -> ["testWord": 1]
        syncHistory.syncedValues.put(Properties.SKILL, new HashMap<String, Byte>());
        skill.increaseWordLevel("testWord");

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.SKILL));
    }

    @Test
    public void givenLargeSkillChange_decoratesDataObject() {
        Skill skill = skillMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Changed from [] -> ["testWord": 10]
        syncHistory.syncedValues.put(Properties.SKILL, new HashMap<String, Byte>());
        skill.increaseWordLevel("testWord", 10);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        DataObject skillDataObject = (DataObject) dataObject.get(Properties.SKILL).value;
        String[] words = (String[]) skillDataObject.get((byte) 0).value;
        byte[] wordLevels = (byte[]) skillDataObject.get((byte) 1).value;
        Assert.assertEquals(1, words.length);
        Assert.assertEquals("testWord", words[0]);
        Assert.assertEquals(1, wordLevels.length);
        Assert.assertEquals(10, wordLevels[0]);
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        skillMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.SKILL));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        skillMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        skillPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.SKILL));
    }
}
