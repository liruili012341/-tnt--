package com.example.throwtnt.entity;

import com.example.throwtnt.ThrowTNTMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static EntityType<PillagerBomberEntity> PILLAGER_BOMBER;
    public static EntityType<ThrownTNTEntity> THROWN_TNT;
    
    public static void registerEntities() {
        PILLAGER_BOMBER = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, PillagerBomberEntity::new)
                .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                .build();
        
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(ThrowTNTMod.MOD_ID, "pillager_bomber"),
            PILLAGER_BOMBER
        );
        
        THROWN_TNT = FabricEntityTypeBuilder.<ThrownTNTEntity>create(SpawnGroup.MISC, ThrownTNTEntity::new)
                .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                .trackRangeChunks(4)
                .trackedUpdateRate(10)
                .build();
        
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(ThrowTNTMod.MOD_ID, "thrown_tnt"),
            THROWN_TNT
        );
    }
    
    public static void registerEntityAttributes() {
        // 这个方法将在客户端和服务端初始化时调用
    }
}