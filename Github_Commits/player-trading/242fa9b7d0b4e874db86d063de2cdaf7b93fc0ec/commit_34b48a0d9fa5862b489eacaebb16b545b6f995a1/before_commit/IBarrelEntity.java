package se.leddy231.playertrading.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;
import se.leddy231.playertrading.shop.Shop;

public interface IBarrelEntity {

    static boolean isOpen(@Nullable BarrelBlockEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getBlockState().getValue(BarrelBlock.OPEN);
    }

    static @Nullable Shop getConnectedShop(BarrelBlockEntity barrelEntity) {
        var level = barrelEntity.getLevel();
        var pos = barrelEntity.getBlockPos();
        for (var direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            var entity = level.getBlockEntity(pos.relative(direction));
            if (entity instanceof SkullBlockEntity skullEntity) {
                var attachedTo = ISkullEntity.attachedToPosition(skullEntity);
                if (!attachedTo.equals(pos)) {
                    continue;
                }
                var shop = ((ISkullEntity) entity).getShop();
                if (shop != null) {
                    return shop;
                }

            }
        }
        return null;
    }

    static boolean isConnectedToShop(BarrelBlockEntity barrelEntity) {
        return getConnectedShop(barrelEntity) != null;
    }
}
