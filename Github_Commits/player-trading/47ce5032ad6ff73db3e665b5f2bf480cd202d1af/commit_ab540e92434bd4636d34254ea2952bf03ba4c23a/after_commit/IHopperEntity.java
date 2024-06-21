package se.leddy231.playertrading.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.shop.Shop;

import java.util.ArrayList;
import java.util.List;

public class IHopperEntity {

    public static void cancelMoveIfConnectedToShop(
            Level level, BlockPos pos, CallbackInfoReturnable<Boolean> callback
    ) {
        var entity = level.getBlockEntity(pos);
        if (entity instanceof BarrelBlockEntity barrelEntity) {
            if (IBarrelEntity.isConnectedToShop(barrelEntity)) {
                callback.setReturnValue(false);
                callback.cancel();
            }
        }
    }

    public static List<Shop> getConnectedShops(HopperBlockEntity hopperEntity) {
        var output = new ArrayList<Shop>();
        var pos = hopperEntity.getBlockPos();
        if (hopperEntity.getLevel() == null) return output;
        var entity = hopperEntity.getLevel().getBlockEntity(pos.relative(Direction.UP));
        if (entity instanceof BarrelBlockEntity barrelEntity) {
            var shop = IBarrelEntity.getConnectedShop(barrelEntity);
            if (shop != null) {
                output.add(shop);
            }
        }
        var facing = hopperEntity.getBlockState().getValue(HopperBlock.FACING);
        entity = hopperEntity.getLevel().getBlockEntity(pos.relative(facing));
        if (entity instanceof BarrelBlockEntity barrelEntity) {
            var shop = IBarrelEntity.getConnectedShop(barrelEntity);
            if (shop != null) {
                output.add(shop);
            }
        }
        return output;
    }
}
