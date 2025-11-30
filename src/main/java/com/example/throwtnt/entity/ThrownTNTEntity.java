package com.example.throwtnt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ThrownTNTEntity extends ThrownItemEntity {
    private int fuseTimer = 80; // 默认4秒引信
    
    public ThrownTNTEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public ThrownTNTEntity(EntityType<? extends ThrownItemEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world);
    }
    
    @Override
    protected Item getDefaultItem() {
        return Items.TNT;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.getWorld().isClient) {
            if (this.fuseTimer > 0) {
                this.fuseTimer--;
                
                // 每10个tick产生一次粒子效果
                if (this.fuseTimer % 10 == 0) {
                    this.getWorld().addParticle(
                        new ItemStackParticleEffect(ParticleTypes.ITEM, this.getDefaultItem().getDefaultStack()),
                        this.getX(), 
                        this.getY(), 
                        this.getZ(), 
                        (this.random.nextFloat() - 0.5) * 0.1,
                        this.random.nextFloat() * 0.1,
                        (this.random.nextFloat() - 0.5) * 0.1
                    );
                }
                
                // 引信结束时爆炸
                if (this.fuseTimer <= 0) {
                    this.explode();
                }
            }
        }
    }
    
    public void setFuse(int fuse) {
        this.fuseTimer = fuse;
    }
    
    private void explode() {
        if (!this.getWorld().isClient) {
            // 创建爆炸
            this.getWorld().createExplosion(
                this, 
                this.getX(), 
                this.getY(), 
                this.getZ(), 
                4.0F, 
                World.ExplosionSourceType.TNT
            );
            
            // 移除实体
            this.discard();
        }
    }
    
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        // 碰撞后立即爆炸
        if (!this.getWorld().isClient) {
            this.explode();
        }
    }
    
    @Override
    public boolean canHit() {
        return false; // 防止与实体相撞时出现问题
    }
}