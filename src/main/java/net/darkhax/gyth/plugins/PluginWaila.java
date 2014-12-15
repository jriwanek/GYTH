package net.darkhax.gyth.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.darkhax.gyth.common.tileentity.TileEntityModularTank;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "Waila")
public class PluginWaila implements IWailaDataProvider {

    @Optional.Method(modid = "Waila")
    public static void callbackRegister(IWailaRegistrar register) {

        PluginWaila instance = new PluginWaila();
        register.registerBodyProvider(instance, Block.class);
    }

    @Override
    @Optional.Method(modid = "Waila")
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {

        return accessor.getStack();
    }

    @Override
    @Optional.Method(modid = "Waila")
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        return currenttip;
    }

    @Override
    @Optional.Method(modid = "Waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        if (accessor.getTileEntity() instanceof TileEntityModularTank) {

            TileEntityModularTank modularTank = (TileEntityModularTank) accessor.getTileEntity();

            if (modularTank.tank.getFluid() != null)
                currenttip.add(StatCollector.translateToLocal("tooltip.gyth.fluidName") + ": " + modularTank.tank.getFluid().getLocalizedName());

            currenttip.add(StatCollector.translateToLocal("tooltip.gyth.fluidAmount") + ": " + modularTank.tank.getFluidAmount() + "/" + modularTank.tank.getCapacity() + " mB");
            currenttip.add(StatCollector.translateToLocal("tooltip.gyth.tankTier") + ": " + modularTank.tierName + " (" + modularTank.tier + ")");

        }

        return currenttip;
    }

    @Override
    @Optional.Method(modid = "Waila")
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        return currenttip;
    }
}