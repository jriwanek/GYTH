package net.darkhax.gyth.common.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.darkhax.bookshelf.lib.BlockStates;
import net.darkhax.gyth.Gyth;
import net.darkhax.gyth.common.tileentity.TileEntityModularTank;
import net.darkhax.gyth.utils.TankData;
import net.darkhax.gyth.utils.TankTier;
import net.darkhax.gyth.utils.Utilities;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class BlockModularTank extends BlockContainer {
    
    public BlockModularTank() {
        
        super(Material.GLASS);
        this.setUnlocalizedName("gyth.modularTank");
        this.setRegistryName(new ResourceLocation("gyth", "modularTank"));
        this.setCreativeTab(Gyth.tabGyth);
        this.setHardness(0.3F);
        this.setSoundType(SoundType.GLASS);
        this.setDefaultState(((IExtendedBlockState) this.blockState.getBaseState()).withProperty(BlockStates.HELD_STATE, null).withProperty(BlockStates.BLOCK_ACCESS, null).withProperty(BlockStates.BLOCKPOS, null));
    }
    
    @Override
    public BlockStateContainer createBlockState () {
        
        return new ExtendedBlockState(this, new IProperty[] {}, new IUnlistedProperty[] { BlockStates.HELD_STATE, BlockStates.BLOCK_ACCESS, BlockStates.BLOCKPOS });
    }
    
    @Override
    public IBlockState getExtendedState (IBlockState state, IBlockAccess world, BlockPos pos) {
        
        state = ((IExtendedBlockState) state).withProperty(BlockStates.BLOCK_ACCESS, world).withProperty(BlockStates.BLOCKPOS, pos);
        
        if (world.getTileEntity(pos) instanceof TileEntityModularTank) {
            
            final TileEntityModularTank tile = (TileEntityModularTank) world.getTileEntity(pos);
            if (tile != null) {
                final TankTier tier = TankData.tiers.get(tile.tierName);
                if (tier != null)
                    return ((IExtendedBlockState) state).withProperty(BlockStates.HELD_STATE, tier.getRenderBlock());
            }
        }
        return state;
    }
    
    @Override
    public boolean hasComparatorInputOverride (IBlockState state) {
        
        return true;
    }
    
    @Override
    public TileEntity createNewTileEntity (World world, int meta) {
        
        return new TileEntityModularTank();
    }
    
    @Override
    public boolean isOpaqueCube (IBlockState state) {
        
        return false;
    }
    
    @Override
    public BlockRenderLayer getBlockLayer () {
        
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    public EnumBlockRenderType getRenderType (IBlockState state) {
        
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    public ItemStack getPickBlock (IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        
        return Utilities.getTankStackFromTile((TileEntityModularTank) world.getTileEntity(pos), true);
    }
    
    @Override
    public int quantityDropped (Random rnd) {
        
        return 0;
    }
    
    @Override
    public int getComparatorInputOverride (IBlockState blockState, World worldIn, BlockPos pos) {
        
        return 0;
    }
    
    @Override
    public boolean onBlockActivated (World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        
        final ItemStack stack = playerIn.inventory.getCurrentItem();
        if (stack != null) {
            
            final FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(stack);
            final TileEntityModularTank tank = (TileEntityModularTank) worldIn.getTileEntity(pos);
            
            if (liquid != null) {
                
                final int amount = tank.fill(EnumFacing.DOWN, liquid, false);
                
                if (amount == liquid.amount) {
                    
                    tank.fill(EnumFacing.DOWN, liquid, true);
                    if (!playerIn.capabilities.isCreativeMode)
                        playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, Utilities.useItemSafely(stack));
                        
                    return true;
                }
                else
                    return true;
            }
            else if (FluidContainerRegistry.isBucket(stack)) {
                
                final FluidTankInfo[] tanks = tank.getTankInfo(EnumFacing.DOWN);
                
                if (tanks[0] != null) {
                    
                    final FluidStack fillFluid = tanks[0].fluid;
                    final ItemStack fillStack = FluidContainerRegistry.fillFluidContainer(fillFluid, stack);
                    
                    if (fillStack != null) {
                        
                        tank.drain(EnumFacing.DOWN, FluidContainerRegistry.getFluidForFilledItem(fillStack).amount, true);
                        
                        if (!playerIn.capabilities.isCreativeMode)
                            if (stack.stackSize == 1)
                                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, fillStack);
                            else
                                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, Utilities.useItemSafely(stack));
                        return true;
                    }
                    else
                        return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean removedByPlayer (IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        
        if (!player.capabilities.isCreativeMode) {
            
            final TileEntityModularTank tank = (TileEntityModularTank) world.getTileEntity(pos);
            Utilities.dropStackInWorld(world, pos, Utilities.getTankStackFromTile(tank, !player.isSneaking()));
        }
        
        return world.setBlockToAir(pos);
    }
    
    @Override
    public void onBlockPlacedBy (World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        
        if (stack.hasTagCompound()) {
            
            final TileEntityModularTank tank = (TileEntityModularTank) worldIn.getTileEntity(pos);
            
            if (tank != null) {
                
                final NBTTagCompound tagFluid = stack.getTagCompound().getCompoundTag("Fluid");
                
                if (tagFluid != null) {
                    
                    final FluidStack liquid = FluidStack.loadFluidStackFromNBT(tagFluid);
                    tank.tank.setFluid(liquid);
                }
                
                tank.tier = stack.getTagCompound().getInteger("Tier");
                tank.tierName = stack.getTagCompound().getString("TierName");
                tank.setTankCapacity(stack.getTagCompound().getInteger("TankCapacity") * FluidContainerRegistry.BUCKET_VOLUME);
            }
        }
    }
    
    @Override
    public void onBlockExploded (World world, BlockPos pos, Explosion explosion) {
        
        Utilities.dropStackInWorld(world, pos, Utilities.getTankStackFromTile((TileEntityModularTank) world.getTileEntity(pos), true));
        world.setBlockToAir(pos);
        this.onBlockDestroyedByExplosion(world, pos, explosion);
    }
}