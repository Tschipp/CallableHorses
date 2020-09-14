package tschipp.callablehorses.common.capabilities.storedhorse;

public interface IStoredHorse {

	public String getStorageUUID();
	
	public void setStorageUUID(String uuid);
	
	public String getOwnerUUID();
	
	public void setOwnerUUID(String uuid);
	
	public void setHorseNum(int num);
	
	public int getHorseNum();
	
	public void setOwned(boolean bool);
	
	public boolean isOwned();

}
