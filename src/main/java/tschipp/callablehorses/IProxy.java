package tschipp.callablehorses;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface IProxy
{
	public World getWorld();
	
	public PlayerEntity getPlayer();
	
	public void displayGui(Object gui);
}
