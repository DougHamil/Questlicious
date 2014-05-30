package com.duggernaut.qlicious;

import java.io.File;

import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class WorldLoadHandler
{
	private File sourceFile;
	
	public WorldLoadHandler(File sourceFile)
	{
		this.sourceFile = sourceFile;
	}
	
	@SubscribeEvent
	public void serverStart(WorldEvent.Load event)
	{
		for(Schematic schematic : Schematics.loadFromAssets(sourceFile))
		{
			schematic.generate(event.world);
		}
	}
}
