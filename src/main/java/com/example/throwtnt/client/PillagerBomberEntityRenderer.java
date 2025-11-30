package com.example.throwtnt.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PillagerEntityRenderer;
import net.minecraft.util.Identifier;

public class PillagerBomberEntityRenderer extends PillagerEntityRenderer {
    private static final Identifier TEXTURE = Identifier.of("textures/entity/illager/pillager.png");

    public PillagerBomberEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(net.minecraft.entity.mob.PillagerEntity pillager) {
        return TEXTURE;
    }
}