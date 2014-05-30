package com.duggernaut.qlicious;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Schematic {

	private NBTTagCompound tag;

	public Schematic(File file) throws IOException {
		this(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public Schematic(InputStream stream) throws IOException {
		this(CompressedStreamTools.readCompressed(stream));
	}

	public Schematic(NBTTagCompound tag) {
		this.tag = tag;
	}

	public void save(String filepath)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(filepath);
			CompressedStreamTools.writeCompressed(this.tag, fos);
		}
		catch(Exception ex)
		{
			System.out.println("Failed to write schematic to "+filepath);
		}
	}
	
	public void generate(World world)
	{
		int[] origin = tag.hasKey("Origin") ? tag.getIntArray("Origin") : new int[] {0, 0, 0};
		generate(world, origin[0], origin[1], origin[2], ForgeDirection.UP);
	}
	
	public void generate(World world, int atX, int atY, int atZ,
		ForgeDirection rotationDirection) {
		int dimension = this.tag.hasKey("Dimension") ? this.tag.getInteger("Dimension") : 0;
		if(dimension != world.provider.dimensionId)
			return;
		
		System.out.println(String.format("Placing schematic at: %d, %d, %d", atX, atY, atZ));
		int width = this.tag.getShort("Width");
		int height = this.tag.getShort("Height");
		int length = this.tag.getShort("Length");
		int[][][] blocks = new int[width][height][length];
		int[][][] meta = new int[width][height][length];
		byte[] blockBytes = tag.getByteArray("Blocks");
		byte[] metaBytes = tag.getByteArray("Data");
		byte blockUpperBits[];
		if (tag.hasKey("AddBlocks")) {
			blockUpperBits = tag.getByteArray("AddBlocks");
		} else {
			blockUpperBits = null;
		}

		// YZX order
		for (int y = 0, blockIdx = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++, blockIdx++) {
					blocks[x][y][z] = (blockBytes[blockIdx]) & 0xFF;
					meta[x][y][z] = (metaBytes[blockIdx]) & 0x0F;
					if (blockUpperBits != null) {
						blocks[x][y][z] |= (blockUpperBits[blockIdx >> 1] << ((blockIdx % 2 == 0) ? 4
								: 8)) & 0xF00;
					}
				}
			}
		}

		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Block block = Block.getBlockById(blocks[x][y][z]);
					Vec3 pos = offset.addVector(x, y, z);
					world.setBlock((int) pos.xCoord, (int) pos.yCoord,
							(int) pos.zCoord,
							Block.getBlockById(blocks[x][y][z]), meta[x][y][z],
							0x2);
				}
			}
		}
	}
}
