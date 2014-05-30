package com.duggernaut.qlicious.editor;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import com.google.common.collect.Lists;

public class SchematicContainerWorldSaveData extends WorldSavedData {
	private final static String key = "QliciousSchematicContainerData";

	private int[] positions;

	public static SchematicContainerWorldSaveData forWorld(World world) {
		MapStorage storage = world.perWorldStorage;
		SchematicContainerWorldSaveData data = (SchematicContainerWorldSaveData) storage
				.loadData(SchematicContainerWorldSaveData.class, key);
		if (data == null) {
			data = new SchematicContainerWorldSaveData();
			storage.setData(key, data);
		}
		return data;
	}

	public SchematicContainerWorldSaveData(String par1Str) {
		super(key);
	}

	public SchematicContainerWorldSaveData() {
		super(key);
	}

	public void addSchematicContainer(World world,
			SchematicContainerTileEntity sc) {
		List<SchematicContainerTileEntity> tes = getList(world);
		tes.add(sc);
		this.positions = buildPositionsFromList(tes);
		this.setDirty(true);
	}

	private int[] buildPositionsFromList(List<SchematicContainerTileEntity> tes)
	{
		int[] pos = new int[tes.size() * 3];
		int i = 0;
		for (SchematicContainerTileEntity te : tes) {
			pos[i] = te.xCoord;
			pos[i + 1] = te.yCoord;
			pos[i + 2] = te.zCoord;
			i += 3;
		}
		return pos;
	}
	
	public List<SchematicContainerTileEntity> getList(World world) {
		List<SchematicContainerTileEntity> tes = Lists.newArrayList();
		if (positions != null) {
			for (int i = 0; i < positions.length; i += 3) {
				int x = positions[i];
				int y = positions[i + 1];
				int z = positions[i + 2];
				TileEntity te = world.getTileEntity(x, y, z);
				System.out.println(String.format("%d, %d, %d", x, y, z));
				if (te != null && te instanceof SchematicContainerTileEntity)
					tes.add((SchematicContainerTileEntity) te);
			}
		}
		return tes;
	}

	@Override
	public void readFromNBT(NBTTagCompound var1) {
		this.positions = var1.getIntArray("positions");
		if (this.positions == null)
			this.positions = new int[0];
	}

	@Override
	public void writeToNBT(NBTTagCompound var1) {
		var1.setIntArray("positions",  this.positions);
	}
}
