package abo.pipes.fluids;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import abo.PipeIcons;
import abo.pipes.ABOPipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PipeFluidsInsertion extends ABOPipe<PipeTransportFluids> {

	public PipeFluidsInsertion(Item itemID) {
		super(new PipeTransportFluids(), itemID);
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIcons.PipeLiquidsInsertion.ordinal();
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		if (!super.outputOpen(to)) return false;

		FluidTankInfo[] tanks = transport.getTankInfo(ForgeDirection.UNKNOWN);

		// center tank
		if (tanks == null || tanks[0] == null || tanks[0].fluid == null || tanks[0].fluid.amount == 0) return true;

		Fluid fluidInTank = tanks[0].fluid.getFluid();

		boolean[] validDirections = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

		boolean foundValidTank = false;
		boolean foundEmptyTank = false;
		boolean isToPipe = (this.container.getTile(to) instanceof TileGenericPipe);
		boolean isToValidTank = false;
		boolean isToEmptyTank = false;

		TileEntity tile1 = this.container.getTile(to);
		if (tile1 != null) {
			if (tile1 instanceof IFluidHandler) {
				if (!(tile1 instanceof TileGenericPipe)) {
					IFluidHandler fh = (IFluidHandler) tile1;
					FluidTankInfo[] ti = fh.getTankInfo(to.getOpposite());
					if (ti.length >= 1) {
						if (ti[0].fluid == null) {
							isToEmptyTank = true;
						}
						if (ti[0].fluid != null
								&& fh.fill(to.getOpposite(), new FluidStack(fluidInTank, 1000), false) > 0) {
							isToValidTank = true;
						}
					}
				}
			}
		}

		label0:
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			validDirections[dir.ordinal()] = false;

			TileEntity tile = this.container.getTile(dir);
			if (tile != null) {
				if (tile instanceof IFluidHandler) {
					if (!(tile instanceof TileGenericPipe)) {
						IFluidHandler fh = (IFluidHandler) tile;
						FluidTankInfo[] ti = fh.getTankInfo(dir.getOpposite());
						if (ti.length >= 1) {
							if (ti[0].fluid != null
									&& fh.fill(dir.getOpposite(), new FluidStack(fluidInTank, 1000), false) > 0) {
								foundValidTank = true;
								break label0;
							}
							if (ti[0].fluid == null) {
								foundEmptyTank = true;
								break label0;
							}
						}
					}
				}
			}

		}

		if (isToPipe && !(foundValidTank || foundEmptyTank)) return true;

		if (isToValidTank) return true;
		if (isToEmptyTank && !foundValidTank) return true;

		return false;
	}

}
