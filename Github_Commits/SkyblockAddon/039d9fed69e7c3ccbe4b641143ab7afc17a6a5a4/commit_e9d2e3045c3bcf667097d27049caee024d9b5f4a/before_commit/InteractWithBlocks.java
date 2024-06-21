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
        Item itemClickedOn = (Item) data[0];

        if(state) return true; //User is allowed to Interact with Blocks, we can just skip unnecessary checks

        //Go through filters to know if we can just allow it.
        return Allowed_Clickable_Blocks.contains(itemClickedOn);
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
                ServerHelper.formattedText("\u2666 Allowed: ", ChatFormatting.GRAY),
                ServerHelper.formattedText((state ? "TRUE" : "FALSE"), (state ? ChatFormatting.GREEN : ChatFormatting.RED))
            )
        };
    }
}
