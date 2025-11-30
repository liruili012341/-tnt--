package com.example.throwtnt.client;

import com.example.throwtnt.entity.ModEntities;
import com.example.throwtnt.entity.PillagerBomberEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ThrowTNTModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.PILLAGER_BOMBER, PillagerBomberEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_TNT, FlyingItemEntityRenderer::new);
        
        // 注册实体属性
        FabricDefaultAttributeRegistry.register(ModEntities.PILLAGER_BOMBER, PillagerBomberEntity.createPillagerBomberAttributes());
    }
}