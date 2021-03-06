package meow.konc.hack.command.commands;

import meow.konc.hack.command.Command;
import meow.konc.hack.command.syntax.SyntaxChunk;
import meow.konc.hack.util.packet.Wrapper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;

/**
 * @author 086
 */
public class PeekCommand extends Command {

    public static TileEntityShulkerBox sb;

    public PeekCommand() {
        super("peek", SyntaxChunk.EMPTY);
        setDescription("Look inside the contents of a shulker box without opening it");
    }

    @Override
    public void call(String[] args) {
        ItemStack is = Wrapper.getPlayer().inventory.getCurrentItem();

        if (is.getItem() instanceof ItemShulkerBox) {
            TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            entityBox.blockType = ((ItemShulkerBox) is.getItem()).getBlock();
            entityBox.setWorld(Wrapper.getWorld());
            entityBox.readFromNBT(is.getTagCompound().getCompoundTag("BlockEntityTag"));
            sb = entityBox;
        } else {
            Command.sendChatMessage("You aren't carrying a shulker box.");
        }
    }
}
