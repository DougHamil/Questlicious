package com.duggernaut.qlicious;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.google.common.collect.Lists;
import com.sun.net.ssl.internal.ssl.Provider;

public class Schematics
{
	private static final String SCHEMATICS_PATH = "schematics";
	
	public static List<Schematic> loadFromAssets(File sourceFile)
	{
		List<Schematic> schematics = Lists.newArrayList();
		
		if (sourceFile.isDirectory()) {
			String assetPath = sourceFile.getAbsolutePath()+"/"+SCHEMATICS_PATH;
			File assetFile = new File(assetPath);
			for(File file : assetFile.listFiles())
			{
				if(!file.isDirectory())
				{
					try
					{
						System.out.println("Found schematic: "+file.getName());
						schematics.add(new Schematic(file));
					}
					catch(Exception ex){}
				}
			}
		} 
		else
		{
			try {
				ZipFile sourceZip = new ZipFile(sourceFile);
				ZipEntry schematicsDir = sourceZip.getEntry(SCHEMATICS_PATH);
				Enumeration<? extends ZipEntry> entries = sourceZip.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (!entry.isDirectory()
							&& entry.getName().startsWith(
									schematicsDir.getName())) {
						NBTTagCompound tag = CompressedStreamTools
								.readCompressed(sourceZip.getInputStream(entry));
						schematics.add(new Schematic(tag));
					}
				}
				sourceZip.close();
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
		
		return schematics;
		
	}
	
	
	/**
	 * Generate a schematic object from world bound coordinates
	 */
	public static Schematic fromWorld(World world, int dimension, int[] s, int[] e)
	{
		int[] origin = new int[3];
		for(int i = 0; i < 3; i++)
			origin[i] = s[i] < e[i] ? s[i] : e[i];
		int width = Math.abs(e[0] - s[0]);
		int height = Math.abs(e[1] - s[1]);
		int length = Math.abs(e[2] - s[2]);
		
		byte[] blockBytes = new byte[width * height * length];
		byte[] metaBytes = new byte[width * height * length];
		
		// YXZ order
		for(int y = 0, blockIdx = 0; y < height; y++){
			for(int z = 0; z < length; z++){
				for(int x = 0; x < width; x++, blockIdx++){
					blockBytes[blockIdx] = (byte)Block.getIdFromBlock(world.getBlock(x + origin[0], y + origin[1], z + origin[2]));
					metaBytes[blockIdx] = (byte)world.getBlockMetadata(x + origin[0], y + origin[1], z + origin[2]);
				}
			}
		}
		
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByteArray("Blocks", blockBytes);
		tag.setByteArray("Data", metaBytes);
		tag.setShort("Width", (short)width);
		tag.setShort("Height",  (short)height);
		tag.setShort("Length", (short)length);
		tag.setIntArray("Origin", origin);
		tag.setInteger("Dimension", dimension);
		return new Schematic(tag);
	}
}
