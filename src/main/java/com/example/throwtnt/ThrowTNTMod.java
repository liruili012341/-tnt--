package com.example.throwtnt;

import com.example.throwtnt.entity.ModEntities;
import com.example.throwtnt.entity.PillagerBomberEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.entity.TntEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class ThrowTNTMod implements ModInitializer {
	public static final String MOD_ID = "throwtnt";

	@Override
	public void onInitialize() {
		// 注册实体
		ModEntities.registerEntities();
		
		// 注册实体属性
		FabricDefaultAttributeRegistry.register(ModEntities.PILLAGER_BOMBER, PillagerBomberEntity.createPillagerBomberAttributes());
		
		UseItemCallback.EVENT.register(this::onUseItem);
	}

	private TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		
		// 检查是否是 Flint and Steel 并且玩家对着空气使用（没有看向方块）
		if (stack.getItem() instanceof FlintAndSteelItem && player.raycast(4.5, 0.0f, false).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
			// 检查玩家是否有 TNT
			if (hasTNTInInventory(player)) {
				if (!world.isClient) {
					// 创建TNT实体
					TntEntity tntEntity = new TntEntity(world, player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ(), player);
					
					// 设置TNT的运动方向和速度
					Vec3d rotation = player.getRotationVector();
					tntEntity.setVelocity(rotation.multiply(1.5));
					
					// 点燃TNT（设置 fuse timer）
					tntEntity.setFuse(40); // 2秒后爆炸 (20 ticks = 1秒)
					
					// 添加到世界中
					world.spawnEntity(tntEntity);
					
					// 播放声音
					world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
					
					// 消耗TNT
					consumeTNT(player);
				}
				
				return TypedActionResult.success(stack);
			}
		}
		
		return TypedActionResult.pass(stack);
	}
	
	private boolean hasTNTInInventory(PlayerEntity player) {
		if (player.isCreative()) {
			return true;
		}
		
		for (ItemStack stack : player.getInventory().main) {
			if (stack.getItem() == Items.TNT) {
				return true;
			}
		}
		return false;
	}
	
	private void consumeTNT(PlayerEntity player) {
		if (!player.isCreative()) {
			for (int i = 0; i < player.getInventory().main.size(); i++) {
				ItemStack stack = player.getInventory().main.get(i);
				if (stack.getItem() == Items.TNT) {
					stack.decrement(1);
					break;
				}
			}
		}
	}
}