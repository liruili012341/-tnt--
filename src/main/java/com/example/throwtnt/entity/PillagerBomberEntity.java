package com.example.throwtnt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;

public class PillagerBomberEntity extends PillagerEntity {
    private int fuseTimer = -1;
    
    public PillagerBomberEntity(EntityType<? extends PillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createPillagerBomberAttributes() {
        return PillagerEntity.createPillagerAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0D)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0.0D);
    }
    
    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(3, new ThrowTNTGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }
    
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("Fuse", (short)this.fuseTimer);
    }
    
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Fuse")) {
            this.fuseTimer = nbt.getShort("Fuse");
        }
    }
    
    @Override
    public void tick() {
        if (this.fuseTimer > 0) {
            --this.fuseTimer;
            if (this.fuseTimer == 0) {
                this.explode();
            } else if (this.fuseTimer % 5 == 0) {
                // 每5个tick产生一次粒子效果
                if (this.getWorld() instanceof ServerWorld) {
                    ((ServerWorld)this.getWorld()).spawnParticles(
                        new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.TNT)),
                        this.getX(), this.getY() + 1.2D, this.getZ(),
                        3, 0.1D, 0.1D, 0.1D, 0.02D
                    );
                }
            }
        }
        
        super.tick();
    }
    
    public void ignite() {
        if (this.fuseTimer < 0) {
            this.fuseTimer = 40; // 2秒 (40 ticks)
            this.getWorld().playSound(
                null, this.getBlockPos(), 
                SoundEvents.ENTITY_CREEPER_PRIMED, 
                this.getSoundCategory(), 
                1.0F, 1.0F
            );
        }
    }
    
    private void explode() {
        if (!this.getWorld().isClient) {
            this.discard();
            
            // 创建爆炸
            this.getWorld().createExplosion(
                this, 
                this.getX(), 
                this.getY(), 
                this.getZ(), 
                3.0F, 
                World.ExplosionSourceType.MOB
            );
            
            // 产生额外的粒子效果
            if (this.getWorld() instanceof ServerWorld) {
                ((ServerWorld)this.getWorld()).spawnParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0
                );
            }
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PILLAGER_AMBIENT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PILLAGER_DEATH;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PILLAGER_HURT;
    }
    
    // 投掷TNT的目标类
    static class ThrowTNTGoal extends Goal {
        private final PillagerBomberEntity bomber;
        private int attackCooldown = 0;
        private int targetSeeingTicker = 0;
        
        public ThrowTNTGoal(PillagerBomberEntity bomber) {
            this.bomber = bomber;
        }
        
        @Override
        public boolean canStart() {
            if (bomber.getTarget() != null && bomber.getTarget().isAlive()) {
                return bomber.squaredDistanceTo(bomber.getTarget()) > 9.0D; // 距离目标超过3格时才考虑投掷
            }
            
            // 检查附近是否有木门
            return findNearestWoodenDoor() != null;
        }
        
        @Override
        public void start() {
            this.attackCooldown = 0;
            this.targetSeeingTicker = 0;
        }
        
        @Override
        public void stop() {
        }
        
        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }
        
        @Override
        public void tick() {
            if (bomber.getTarget() != null) {
                // 检查视线是否清晰
                boolean canSeeTarget = bomber.getVisibilityCache().canSee(bomber.getTarget());
                if (canSeeTarget) {
                    this.targetSeeingTicker = Math.min(this.targetSeeingTicker + 1, 10);
                } else {
                    this.targetSeeingTicker = Math.max(this.targetSeeingTicker - 1, 0);
                }
                
                // 检查距离
                double distanceSq = bomber.squaredDistanceTo(bomber.getTarget());
                
                // 如果冷却时间到了并且距离合适，则投掷TNT
                if (--this.attackCooldown <= 0 && this.targetSeeingTicker >= 4 && distanceSq < 100.0D) { // 10格内
                    this.throwTNT();
                    this.attackCooldown = 40; // 半秒冷却
                }
            } else {
                // 检查是否有附近的木门
                BlockPos doorPos = findNearestWoodenDoor();
                if (doorPos != null && --this.attackCooldown <= 0) {
                    this.throwTNTAtDoor(doorPos);
                    this.attackCooldown = 60; // 3秒冷却
                }
            }
        }
        
        private BlockPos findNearestWoodenDoor() {
            BlockPos entityPos = bomber.getBlockPos();
            BlockPos nearestDoor = null;
            double nearestDistance = Double.MAX_VALUE;
            
            // 在周围16x16x16范围内搜索木门
            for (int x = -8; x <= 8; x++) {
                for (int y = -8; y <= 8; y++) {
                    for (int z = -8; z <= 8; z++) {
                        BlockPos pos = entityPos.add(x, y, z);
                        BlockState state = bomber.getWorld().getBlockState(pos);
                        
                        // 检查是否是木门
                        if (state.getBlock() instanceof DoorBlock && 
                            (state.isOf(Blocks.OAK_DOOR) || 
                             state.isOf(Blocks.SPRUCE_DOOR) || 
                             state.isOf(Blocks.BIRCH_DOOR) || 
                             state.isOf(Blocks.JUNGLE_DOOR) || 
                             state.isOf(Blocks.ACACIA_DOOR) || 
                             state.isOf(Blocks.DARK_OAK_DOOR) || 
                             state.isOf(Blocks.CRIMSON_DOOR) || 
                             state.isOf(Blocks.WARPED_DOOR))) {
                            
                            // 确保只针对下部门
                            if (state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                double distance = entityPos.getSquaredDistance(pos);
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestDoor = pos;
                                }
                            }
                        }
                    }
                }
            }
            
            return nearestDoor;
        }
        
        private void throwTNT() {
            if (!bomber.getWorld().isClient) {
                Vec3d targetPos = bomber.getTarget().getEyePos();
                Vec3d startPos = bomber.getEyePos();
                Vec3d direction = targetPos.subtract(startPos).normalize();
                
                // 创建投掷的TNT实体
                ThrownTNTEntity tnt = new ThrownTNTEntity(ModEntities.THROWN_TNT, bomber.getWorld(), bomber);
                
                // 设置TNT的位置
                tnt.refreshPositionAndAngles(startPos.x, startPos.y, startPos.z, 0, 0);
                
                // 设置TNT的速度
                tnt.setVelocity(
                    direction.x * 1.5D, 
                    direction.y * 1.5D + 0.2D, 
                    direction.z * 1.5D
                );
                
                // 设置引信时间
                tnt.setFuse(40); // 2秒
                
                // 添加到世界
                bomber.getWorld().spawnEntity(tnt);
                
                // 播放声音
                bomber.getWorld().playSound(
                    null, 
                    BlockPos.ofFloored(startPos), 
                    SoundEvents.ENTITY_SNOWBALL_THROW, 
                    bomber.getSoundCategory(), 
                    1.0F, 
                    0.8F / (bomber.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        }
        
        private void throwTNTAtDoor(BlockPos doorPos) {
            if (!bomber.getWorld().isClient) {
                Vec3d targetPos = new Vec3d(doorPos.getX() + 0.5, doorPos.getY() + 1.0, doorPos.getZ() + 0.5);
                Vec3d startPos = bomber.getEyePos();
                Vec3d direction = targetPos.subtract(startPos).normalize();
                
                // 创建投掷的TNT实体
                ThrownTNTEntity tnt = new ThrownTNTEntity(ModEntities.THROWN_TNT, bomber.getWorld(), bomber);
                
                // 设置TNT的位置
                tnt.refreshPositionAndAngles(startPos.x, startPos.y, startPos.z, 0, 0);
                
                // 设置TNT的速度
                tnt.setVelocity(
                    direction.x * 1.5D, 
                    direction.y * 1.5D + 0.2D, 
                    direction.z * 1.5D
                );
                
                // 设置引信时间
                tnt.setFuse(40); // 2秒
                
                // 添加到世界
                bomber.getWorld().spawnEntity(tnt);
                
                // 播放声音
                bomber.getWorld().playSound(
                    null, 
                    BlockPos.ofFloored(startPos), 
                    SoundEvents.ENTITY_SNOWBALL_THROW, 
                    bomber.getSoundCategory(), 
                    1.0F, 
                    0.8F / (bomber.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        }
    }
}