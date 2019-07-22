package tschipp.callablehorses;

import tschipp.callablehorses.client.keybinds.KeybindManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		//Item Renders
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		KeybindManager.init();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent e)
	{
		super.postInit(e);
	}
}
