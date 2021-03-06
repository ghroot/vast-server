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
import java.util.Map;

import static org.junit.Assert.*;

public class TestSkillPropertyHandler {
    private SkillPropertyHandler skillPropertyHandler;
    private World world;
    private ComponentMapper<Skill> skillMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        skillPropertyHandler = new SkillPropertyHandler(5);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(skillPropertyHandler);

        skillMapper = world.getMapper(Skill.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenHasSkill_decoratesDataObject() {
        skillMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        DataObject skillDataObject = (DataObject) dataObject.get(Properties.SKILL).value;
        String[] names = (String[]) skillDataObject.get((byte) 0).value;
        int[] xp = (int[]) skillDataObject.get((byte) 1).value;
        assertArrayEquals(new String[0], names);
        assertArrayEquals(new int[0], xp);
    }

    @Test
    public void givenNoSkillChange_doesNotDecorateDataObject() {
        skillMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.SKILL, new HashMap<String, Byte>())));

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.SKILL));
    }

    @Test
    public void givenSmallSkillChange_doesNotDecorateDataObject() {
        Skill skill = skillMapper.create(propertyEntity);

        // Changed from [] -> ["test": 1]
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.SKILL, new HashMap<String, Integer>())));
        skill.addXp("test");

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.SKILL));
    }

    @Test
    public void givenLargeSkillChange_decoratesDataObject() {
        Skill skill = skillMapper.create(propertyEntity);

        // Changed from [] -> ["test": 10]
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.SKILL, new HashMap<String, Integer>())));
        skill.addXp("test", 10);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        DataObject skillDataObject = (DataObject) dataObject.get(Properties.SKILL).value;
        String[] names = (String[]) skillDataObject.get((byte) 0).value;
        int[] xp = (int[]) skillDataObject.get((byte) 1).value;
        assertEquals(1, names.length);
        assertEquals("test", names[0]);
        assertEquals(1, xp.length);
        assertEquals(10, xp[0]);
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        skillMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.SKILL));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        skillMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        skillPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.SKILL));
    }
}
