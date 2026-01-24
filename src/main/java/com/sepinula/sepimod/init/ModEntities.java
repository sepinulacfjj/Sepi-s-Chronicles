package com.sepinula.sepimod.init;

import com.sepinula.sepimod.SepiMod;
import com.sepinula.sepimod.entity.Baby_GoblinEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, SepiMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Baby_GoblinEntity>> BabyGOBLIN =
            ENTITIES.register("baby_goblin", () -> EntityType.Builder.of(Baby_GoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.8f) // Size of the hitbox
                    .build("baby_goblin"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}