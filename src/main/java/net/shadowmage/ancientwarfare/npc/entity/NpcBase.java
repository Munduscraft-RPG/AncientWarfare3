package net.shadowmage.ancientwarfare.npc.entity;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import net.shadowmage.ancientwarfare.core.interfaces.IOwnable;
import net.shadowmage.ancientwarfare.core.util.BlockPosition;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand.Command;
import net.shadowmage.ancientwarfare.npc.skin.NpcSkinManager;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class NpcBase extends EntityCreature implements IEntityAdditionalSpawnData, IOwnable
{

private String ownerName = "";//the owner of this NPC, used for checking teams

protected String followingPlayerName;//set/cleared onInteract from player if player.team==this.team

protected NpcLevelingStats levelingStats;

/**
 * a single base texture for ALL npcs to share, used in case other textures were not set
 */
private final ResourceLocation baseDefaultTexture;

private ResourceLocation currentTexture = null;

public ItemStack ordersStack;

public ItemStack upkeepStack;

public NpcBase(World par1World)
  {
  super(par1World);
  baseDefaultTexture = new ResourceLocation("ancientwarfare:textures/entity/npc/npc_default.png");
  levelingStats = new NpcLevelingStats(this);  
  this.getNavigator().setBreakDoors(true);
  this.getNavigator().setAvoidsWater(true);
  this.equipmentDropChances = new float[]{1.f, 1.f, 1.f, 1.f, 1.f};
  this.width = 0.6f;
  }

@Override
public final double getYOffset()
  {
  return (double)(this.yOffset - 0.5F);
  }

@Override
public PathNavigate getNavigator()
  {
  if(this.ridingEntity instanceof EntityLiving)
    {
    return ((EntityLiving)this.ridingEntity).getNavigator();
    }
  return super.getNavigator();
  }

@Override
protected void entityInit()
  {
  super.entityInit();
  this.getDataWatcher().addObject(20, Integer.valueOf(0));//ai tasks
  }

public void setTownHallPosition(BlockPosition pos)
  {
  //NOOP on non-player owned npc
  }

public BlockPosition getTownHallPosition()
  {
  return null;//NOOP on non-player owned npc
  }

public TileTownHall getTownHall()
  {
  return null;//NOOP on non-player owned npc
  }

public void handleTownHallBroadcast(TileTownHall tile, BlockPosition position)
  {
//NOOP on non-player owned npc
  }

/**
 * Used by command baton and town-hall to determine if this NPC is commandable by a player / team
 * @param playerName
 * @return
 */
public boolean canBeCommandedBy(String playerName)
  {
  if(ownerName.isEmpty()){return false;}
  if(playerName==null){return false;}
  Team team = getTeam();
  if(team==null)
    {
    return playerName.equals(ownerName);
    }
  else
    {
    return team==worldObj.getScoreboard().getPlayersTeam(playerName);
    }
  }

@Override
public final boolean attackEntityFrom(DamageSource source, float par2)
  {
  if(source.getEntity() instanceof NpcBase)
    {
    if(!isHostileTowards(source.getEntity()))
      {
      return false;
      }
    }
  return super.attackEntityFrom(source, par2);
  }

@Override
public final void setRevengeTarget(EntityLivingBase par1EntityLivingBase)
  {
  if(par1EntityLivingBase instanceof NpcBase)
    {
    if(!isHostileTowards(par1EntityLivingBase))
      {
      return;
      }
    }
  super.setRevengeTarget(par1EntityLivingBase);
  }

@Override
protected final void dropEquipment(boolean par1, int par2)
  {
  if(!worldObj.isRemote)
    {
    ItemStack stack;
    for(int i = 0; i < 5; i++)
      {
      stack = getEquipmentInSlot(i);
      if(stack!=null){entityDropItem(stack, 0.f);}
      setCurrentItemOrArmor(i, null);
      }
    if(ordersStack!=null){entityDropItem(ordersStack, 0.f);}
    if(upkeepStack!=null){entityDropItem(upkeepStack, 0.f);}
    ordersStack=null;
    upkeepStack=null;
    }
  }

@Override
public final void onKillEntity(EntityLivingBase par1EntityLivingBase)
  {  
  super.onKillEntity(par1EntityLivingBase);
  if(!worldObj.isRemote)
    {
    addExperience(AWNPCStatics.npcXpFromKill);
    if(par1EntityLivingBase==this.getAttackTarget())
      {
      this.setAttackTarget(null);
      }
    }
  }

public Command getCurrentCommand()
  {
  return null;//NOOP on non-player owned npc
  }

public void handlePlayerCommand(Command cmd)
  {
//NOOP on non-player owned npc
  }

/**
 * return the bitfield containing all of the currently executing AI tasks<br>
 * used by player-owned npcs for rendering ai-tasks
 * @return
 */
public final int getAITasks()
  {
  return getDataWatcher().getWatchableObjectInt(20);
  }

/**
 * add a task to the bitfield of currently executing tasks<br>
 * input should be a ^2, or combination of (e.g. 1+2 or 2+4)<br>
 * @param task
 */
public final void addAITask(int task)
  {
  int tasks = getAITasks();
  int tc = tasks;
  tasks = tasks | task;
  if(tc!=tasks)
    {
    setAITasks(tasks);    
    }
  }

/**
 * remove a task from the bitfield of currently executing tasks<br>
 * input should be a ^2, or combination of (e.g. 1+2 or 2+4)<br>
 * @param task
 */
public final void removeAITask(int task)
  {
  int tasks = getAITasks();
  int tc = tasks;
  tasks = tasks & (~task);
  if(tc!=tasks)
    {
    setAITasks(tasks);    
    }
  }

/**
 * set ai tasks -- only used internally
 * @param task
 */
private final void setAITasks(int tasks)
  {
  this.getDataWatcher().updateObject(20, Integer.valueOf(tasks));  
  }

/**
 * add an amount of experience to this npcs leveling stats<br>
 * experience is added for base level, and subtype level(if any)
 * @param amount
 */
public final void addExperience(int amount)
  {
  String type = getNpcFullType();
  getLevelingStats().addExperience(type, amount);
  }

/**
 * implementations should read in any data written during {@link #writeAdditionalItemData(NBTTagCompound)}
 * @param tag
 */
public final void readAdditionalItemData(NBTTagCompound tag)
  {
  NBTTagList equipmentList = tag.getTagList("equipment", Constants.NBT.TAG_COMPOUND);
  ItemStack stack;
  NBTTagCompound equipmentTag;
  for(int i = 0; i < equipmentList.tagCount(); i++)
    {
    equipmentTag = equipmentList.getCompoundTagAt(i);
    stack = InventoryTools.readItemStack(equipmentTag);
    if(equipmentTag.hasKey("slotNum")){setCurrentItemOrArmor(equipmentTag.getInteger("slotNum"), stack);}
    else if(equipmentTag.hasKey("orders")){ordersStack=stack;}
    else if(equipmentTag.hasKey("upkeep")){upkeepStack=stack;}
    }
  if(tag.hasKey("levelingStats")){getLevelingStats().readFromNBT(tag.getCompoundTag("levelingStats"));}
  if(tag.hasKey("maxHealth")){getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(tag.getFloat("maxHealth"));}
  if(tag.hasKey("health")){setHealth(tag.getFloat("health"));}
  if(tag.hasKey("name")){setCustomNameTag(tag.getString("name"));}
  if(tag.hasKey("food")){setFoodRemaining(tag.getInteger("food"));}
  ownerName=tag.getString("owner");
  }

/**
 * Implementations should write out any persistent entity-data needed to restore entity-state from an item-stack.<br>
 * This should include inventory, levels, orders, faction / etc
 * @param tag
 */
public final NBTTagCompound writeAdditionalItemData(NBTTagCompound tag)
  {
  /**
   * write out:
   * equipment (including orders items)
   * leveling stats
   * current health
   */
  NBTTagList equipmentList = new NBTTagList();
  ItemStack stack;
  NBTTagCompound equipmentTag;
  for(int i = 0; i < 5; i++)
    {
    stack = getEquipmentInSlot(i);
    if(stack==null){continue;}
    equipmentTag = InventoryTools.writeItemStack(stack, new NBTTagCompound());
    equipmentTag.setInteger("slotNum", i);
    equipmentList.appendTag(equipmentTag);
    }
  if(ordersStack!=null)
    {
    equipmentTag = InventoryTools.writeItemStack(ordersStack, new NBTTagCompound());
    equipmentTag.setBoolean("orders", true);
    equipmentList.appendTag(equipmentTag);
    }
  if(upkeepStack!=null)
    {
    equipmentTag = InventoryTools.writeItemStack(upkeepStack, new NBTTagCompound());
    equipmentTag.setBoolean("upkeep", true);
    equipmentList.appendTag(equipmentTag);
    }  
  tag.setTag("equipment", equipmentList);
  
  tag.setTag("levelingStats", getLevelingStats().writeToNBT(new NBTTagCompound()));
  tag.setFloat("maxHealth", getMaxHealth());
  tag.setFloat("health", getHealth());
  tag.setInteger("food", getFoodRemaining());
  if(hasCustomNameTag()){tag.setString("name", getCustomNameTag());}
  tag.setString("owner", ownerName);
  return tag;
  }

/**
 * is the input stack a valid orders-item for this npc?<br>
 * used by player-owned NPCs
 * TODO noop this in base, re-abstract in npc-player owned class
 * @param stack
 * @return
 */
public abstract boolean isValidOrdersStack(ItemStack stack);

/**
 * callback for when orders-stack changes.  implemenations should inform any neccessary AI tasks of the
 * change to order-items
 */
public abstract void onOrdersInventoryChanged();

/**
 * callback for when weapon slot has been changed.<br>
 * Implementations should re-set any subtype or inform any AI that need to know when
 * weapon was changed.
 */
public abstract void onWeaponInventoryChanged();

/**
 * return the NPCs subtype.<br>
 * this subtype may vary at runtime.
 * @return
 */
public abstract String getNpcSubType();

/**
 * return the NPCs type.  This type should be unique for the class of entity,
 * or at least unique pertaining to the entity registration.
 * @return
 */
public abstract String getNpcType();

/**
 * handle an incoming attack alert from another NPC.<br>
 * implementations should inform their alert-AI and/or set attack/flee targets appropriately<br>
 * called from alert-AI tasks when npc attack target / attacker changes.
 * @param broadcaster
 * @param target
 */
public abstract void handleAlertBroadcast(NpcBase broadcaster, EntityLivingBase target);

/**
 * return the full NPC type for this npc<br>
 * returns npcType if subtype is empty, else npcType.npcSubtype 
 * @return
 */
public final String getNpcFullType()
  {
  String type = getNpcType();
  String sub = getNpcSubType();
  if(!sub.isEmpty()){type = type+"."+sub;}
  return type;
  }

public final NpcLevelingStats getLevelingStats()
  {
  return levelingStats;
  }

public final ResourceLocation getDefaultTexture()
  {
  return baseDefaultTexture;
  }

public final ItemStack getItemToSpawn()
  {
  return ItemNpcSpawner.getSpawnerItemForNpc(this);
  }

public final long getIDForSkin()
  {
  return this.entityUniqueID.getLeastSignificantBits();
  }

@Override
public final ItemStack getPickedResult(MovingObjectPosition target)
  {
  return getItemToSpawn();
  }

@Override
public void writeSpawnData(ByteBuf buffer)
  {
  buffer.writeLong(getUniqueID().getMostSignificantBits());
  buffer.writeLong(getUniqueID().getLeastSignificantBits());
  ByteBufUtils.writeUTF8String(buffer, ownerName);
  }

@Override
public void readSpawnData(ByteBuf additionalData)
  {
  long l1, l2;
  l1 = additionalData.readLong();
  l2 = additionalData.readLong();
  this.entityUniqueID = new UUID(l1, l2);
  ownerName=ByteBufUtils.readUTF8String(additionalData);
  }

@Override
public void onUpdate()
  {
  worldObj.theProfiler.startSection("AWNpcTick");
  updateArmSwingProgress();
  if(ticksExisted%200==0 && getHealth()<getMaxHealth() && (!requiresUpkeep() || getFoodRemaining()>0))
    {
    setHealth(getHealth()+1);
    }
  super.onUpdate();
  worldObj.theProfiler.endSection();
  }

@Override
protected final boolean canDespawn()
  {
  return false;
  }

@Override
protected final boolean isAIEnabled()
  {
  return true;
  }

@Override
protected void applyEntityAttributes()
  {
  super.applyEntityAttributes();  
  this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.d);//TODO figure out dynamic changing of max-health based on level from levelingStats
  this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);//TODO check what pathfinding range is really needed, perhaps allow config option for longer paths
  this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.325D);//TODO check what entity speed is needed / feels right. perhaps vary depending upon level or type
  this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
  }

/**
 * called whenever level changes, to update the damage-done stat for the entity
 */
public final void updateDamageFromLevel()
  {
  float dmg = AWNPCStatics.npcAttackDamage;
  float lvl = getLevelingStats().getLevel(getNpcFullType());
  dmg += dmg*lvl*AWNPCStatics.npcLevelDamageMultiplier;
  this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(dmg);  
  }

public int getFoodRemaining()
  {
  return 0;//NOOP in non-player owned
  }

public void setFoodRemaining(int food)
  {
  //NOOP in non-player owned
  }

public int getUpkeepBlockSide()
  {
  return 0;//NOOP in non-player owned
  }

public BlockPosition getUpkeepPoint()
  {
  return null;//NOOP in non-player owned
  }

public int getUpkeepAmount()
  {
  return 0;//NOOP in non-player owned
  }

public int getUpkeepDimensionId()
  {
  return 0;//NOOP in non-player owned
  }

public boolean requiresUpkeep()
  {
  return false;//NOOP in non-player owned
  }

@Override
public void setOwnerName(String name)
  {
  ownerName = name;
  }

@Override
public String getOwnerName()
  {
  return ownerName;
  }

@Override
public Team getTeam()
  {
  return worldObj.getScoreboard().getPlayersTeam(ownerName);
  }

public abstract boolean isHostileTowards(Entity e);

public final EntityLivingBase getFollowingEntity()
  {
  if(followingPlayerName==null){return null;}
  return worldObj.getPlayerEntityByName(followingPlayerName);
  }

public final void setFollowingEntity(EntityLivingBase entity)
  {
  if(entity instanceof EntityPlayer && canBeCommandedBy(entity.getCommandSenderName()))
    {
    this.followingPlayerName = entity.getCommandSenderName();        
    }
  }

@Override
public boolean allowLeashing()
  {
  return false;
  }

public final void repackEntity(EntityPlayer player)
  {
  if(!player.worldObj.isRemote)
    {
    ItemStack item = this.getItemToSpawn();
    item = InventoryTools.mergeItemStack(player.inventory, item, -1);
    if(item!=null)
      {
      InventoryTools.dropItemInWorld(player.worldObj, item, player.posX, player.posY, player.posZ);    
      }    
    setDead();
    }
  }

@Override
public void readEntityFromNBT(NBTTagCompound tag)
  {
  super.readEntityFromNBT(tag);
  ownerName = tag.getString("owner");  
  if(tag.hasKey("ordersItem")){ordersStack=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("ordersItem"));}
  if(tag.hasKey("upkeepItem")){upkeepStack=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("upkeepItem"));}
  if(tag.hasKey("home"))
    {
    int[] ccia = tag.getIntArray("home");
    setHomeArea(ccia[0], ccia[1], ccia[2], ccia[3]);
    }
  if(tag.hasKey("levelingStats")){levelingStats.readFromNBT(tag.getCompoundTag("levelingStats"));}
  //TODO
  }

@Override
public void writeEntityToNBT(NBTTagCompound tag)
  {
  super.writeEntityToNBT(tag);
  tag.setString("owner", ownerName);
  if(ordersStack!=null){tag.setTag("ordersItem", ordersStack.writeToNBT(new NBTTagCompound()));}
  if(upkeepStack!=null){tag.setTag("upkeepItem", upkeepStack.writeToNBT(new NBTTagCompound()));}
  if(getHomePosition()!=null)
    {
    ChunkCoordinates cc = getHomePosition();
    int[] ccia = new int[]{cc.posX,cc.posY,cc.posZ, (int)func_110174_bM()};
    tag.setIntArray("home", ccia);
    }
  tag.setTag("levelingStats", levelingStats.writeToNBT(new NBTTagCompound()));
  //TODO
  }

public final ResourceLocation getTexture()
  {  
  if(currentTexture==null)
    {
    updateTexture();
    }  
  return currentTexture==null ? getDefaultTexture() : currentTexture;
  }

public final void updateTexture()
  {
  currentTexture = NpcSkinManager.INSTANCE.getTextureFor(this);
  }

}
