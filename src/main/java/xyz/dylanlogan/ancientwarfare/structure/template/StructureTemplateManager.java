package xyz.dylanlogan.ancientwarfare.structure.template;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import xyz.dylanlogan.ancientwarfare.core.network.NetworkHandler;
import xyz.dylanlogan.ancientwarfare.structure.network.PacketStructure;
import xyz.dylanlogan.ancientwarfare.structure.network.PacketStructureRemove;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class StructureTemplateManager {
    private HashMap<String, StructureTemplateClient> clientTemplates = new HashMap<String, StructureTemplateClient>();//server-side client-templates
    private HashMap<String, BufferedImage> templateImages = new HashMap<String, BufferedImage>();//server-side images
    //private HashMap<String,String> imageMD5s = new HashMap<String,String>();
    private HashMap<String, StructureTemplate> loadedTemplates = new HashMap<String, StructureTemplate>();

    private StructureTemplateManager() {
    }

    public static final StructureTemplateManager INSTANCE = new StructureTemplateManager();

    public void addTemplate(StructureTemplate template) {
        if (template.getValidationSettings() == null) {
            return;
        }
        if (template.getValidationSettings().isWorldGenEnabled()) {
            WorldGenStructureManager.INSTANCE.registerWorldGenStructure(template);
        }
        loadedTemplates.put(template.name, template);
        StructureTemplateClient cl = new StructureTemplateClient(template);
        clientTemplates.put(template.name, cl);

        MinecraftServer server = MinecraftServer.getServer();
        if (server != null && server.isServerRunning() && server.getConfigurationManager() != null) {
            NBTTagCompound tag = new NBTTagCompound();
            cl.writeToNBT(tag);
            PacketStructure pkt = new PacketStructure();
            pkt.packetData.setTag("singleStructure", tag);
            NetworkHandler.sendToAllPlayers(pkt);
        }
    }

    public void onPlayerConnect(EntityPlayerMP player) {
        NBTTagList list = new NBTTagList();
        for (StructureTemplateClient cl : clientTemplates.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            cl.writeToNBT(tag);
            list.appendTag(tag);
        }
        PacketStructure pkt = new PacketStructure();
        pkt.packetData.setTag("structureList", list);
        NetworkHandler.sendToPlayer(player, pkt);

//  PacketStructureImageList pkt2 = new PacketStructureImageList(this.imageMD5s);
//  NetworkHandler.sendToPlayer(player, pkt2);
    }

    public boolean removeTemplate(String name) {
        if (this.loadedTemplates.containsKey(name)) {
            this.loadedTemplates.remove(name);
            this.clientTemplates.remove(name);
            this.templateImages.remove(name);
            NetworkHandler.sendToAllPlayers(new PacketStructureRemove(name));
            return true;
        }
        return false;
    }

    public StructureTemplate getTemplate(String name) {
        return this.loadedTemplates.get(name);
    }

    public void addTemplateImage(String imageName, BufferedImage image) {
        this.templateImages.put(imageName, image);
    }

    public BufferedImage getTemplateImage(String imageName) {
        return templateImages.get(imageName);
    }

}
