package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import net.lax1dude.eaglercraft.internal.vfs2.VFile2;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerList {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft mc;
   private final List<ServerData> servers = Lists.newArrayList();

   /** Pinned to the top of the list on every load; see net.slate.Branding. */
   private static final String DEFAULT_NAME = net.slate.Branding.SERVER_NAME;
   private static final String DEFAULT_IP = net.slate.Branding.SERVER_ADDRESS;

   public ServerList(Minecraft mcIn) {
      this.mc = mcIn;
      this.loadServerList();
   }

   public void loadServerList() {
      try {
         this.servers.clear();
         CompoundNBT compoundnbt = CompressedStreamTools.read(new VFile2(this.mc.gameDir, "servers.dat"));

         if (compoundnbt != null) {
            ListNBT listnbt = compoundnbt.getList("servers", 10);
            for(int i = 0; i < listnbt.size(); ++i) {
               ServerData data = ServerData.getServerDataFromNBTCompound(listnbt.getCompound(i));
               if (!data.serverIP.equalsIgnoreCase(DEFAULT_IP)) {
                  this.servers.add(data);
               }
            }
         }

         this.servers.add(0, new ServerData(DEFAULT_NAME, DEFAULT_IP, false));

      } catch (Exception exception) {
         LOGGER.error("Couldn't load server list", (Throwable)exception);
      }
   }

   public void saveServerList() {
      try {
         ListNBT listnbt = new ListNBT();

         for(ServerData serverdata : this.servers) {
            if (!serverdata.serverIP.equalsIgnoreCase(DEFAULT_IP)) {
               listnbt.add(serverdata.getNBTCompound());
            }
         }

         CompoundNBT compoundnbt = new CompoundNBT();
         compoundnbt.put("servers", listnbt);
         CompressedStreamTools.safeWrite(compoundnbt, new VFile2(this.mc.gameDir, "servers.dat"));
      } catch (Exception exception) {
         LOGGER.error("Couldn't save server list", (Throwable)exception);
      }
   }

   public ServerData getServerData(int index) {
      return this.servers.get(index);
   }

   public void func_217506_a(ServerData p_217506_1_) {
      if (p_217506_1_ != null && DEFAULT_IP.equalsIgnoreCase(p_217506_1_.serverIP)) {
         return;
      }
      this.servers.remove(p_217506_1_);
   }

   public void addServerData(ServerData server) {
      this.servers.add(server);
   }

   public int countServers() {
      return this.servers.size();
   }

   public void swapServers(int pos1, int pos2) {
      if (pos1 == 0 || pos2 == 0) {
         return;
      }
      ServerData serverdata = this.getServerData(pos1);
      this.servers.set(pos1, this.getServerData(pos2));
      this.servers.set(pos2, serverdata);
      this.saveServerList();
   }

   public void set(int index, ServerData server) {
      if (index == 0) {
         return;
      }
      this.servers.set(index, server);
   }

   public static void saveSingleServer(ServerData server) {
      ServerList serverlist = new ServerList(Minecraft.getInstance());
      serverlist.loadServerList();

      for(int i = 0; i < serverlist.countServers(); ++i) {
         ServerData serverdata = serverlist.getServerData(i);
         if (serverdata.serverName.equals(server.serverName) && serverdata.serverIP.equals(server.serverIP)) {
            serverlist.set(i, server);
            break;
         }
      }

      serverlist.saveServerList();
   }
}