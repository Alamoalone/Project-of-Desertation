package yorickbm.skyblockaddon.islands.permissions;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import yorickbm.skyblockaddon.util.LanguageFile;
import yorickbm.skyblockaddon.util.ServerHelper;

import java.util.ArrayList;
import java.util.List;

public class InteractWithBlocks extends Permission {

    private final List<Item> Allowed_Clickable_Blocks = new ArrayList<>();

    public InteractWithBlocks(boolean state) {
        super(state);

        Allowed_Clickable_Blocks.add(Items.ENDER_CHEST);
    }

    @Override
    public boolean isAllowed(Object ...data) {
        if(state) return true; //User is allowed to Interact with Blocks, we can just skip unnecessary checks

        if(data.length > 0 && data[0] != null) {
            //Go through filters to know if we can just allow it.
            Item itemClickedOn = (Item) data[0];
            return Allowed_Clickable_Blocks.contains(itemClickedOn);
        }
        return false; //It's not allowed
    }

    @Override
    public Item getDisplayItem() {
        return Items.CHEST;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return ServerHelper.formattedText(LanguageFile.getForKey("guis.permissions.OpenBlocks.title"), ChatFormatting.BLUE, ChatFormatting.BOLD);
    }

    @Override
    public Component[] getDescription() {
        return new Component[] {
            ServerHelper.formattedText(LanguageFile.getForKey("guis.permissions.OpenBlocks.desc"), ChatFormatting.GRAY),
            ServerHelper.formattedText("\n\n", ChatFormatting.ITALIC),
            ServerHelper.combineComponents(
                ServerHelper.formattedText("♦ Allowed: ", ChatFormatting.GRAY),
                ServerHelper.formattedText((state ? "TRUE" : "FALSE"), (state ? ChatFormatting.GREEN : ChatFormatting.RED))
            )
        };
    }
}
