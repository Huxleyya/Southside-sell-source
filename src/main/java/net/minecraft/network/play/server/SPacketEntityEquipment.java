package net.minecraft.network.play.server;

import java.io.IOException;

import dev.diona.southside.module.modules.render.OldHitting;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import static dev.diona.southside.Southside.MC.mc;

public class SPacketEntityEquipment implements Packet<INetHandlerPlayClient>
{
    private int entityID;
    private EntityEquipmentSlot equipmentSlot;
    private ItemStack itemStack = ItemStack.EMPTY;

    public SPacketEntityEquipment()
    {
    }

    public SPacketEntityEquipment(int entityIdIn, EntityEquipmentSlot equipmentSlotIn, ItemStack itemStackIn)
    {
        this.entityID = entityIdIn;
        this.equipmentSlot = equipmentSlotIn;
        this.itemStack = itemStackIn.copy();
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityID = buf.readVarInt();
        this.equipmentSlot = (EntityEquipmentSlot)buf.readEnumValue(EntityEquipmentSlot.class);
        this.itemStack = buf.readItemStack();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarInt(this.entityID);
        buf.writeEnumValue(this.equipmentSlot);
        buf.writeItemStack(this.itemStack);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        if (mc.player != null && this.entityID == mc.player.getEntityId() && this.equipmentSlot == EntityEquipmentSlot.OFFHAND && (itemStack.getItem() instanceof ItemShield || (itemStack.isEmpty() && mc.player.getHeldItemOffhand().getItem() instanceof ItemShield)) && OldHitting.giveShield()) {
            return;
        }
        handler.handleEntityEquipment(this);
    }

    public ItemStack getItemStack()
    {
        return this.itemStack;
    }

    public int getEntityID()
    {
        return this.entityID;
    }

    public EntityEquipmentSlot getEquipmentSlot()
    {
        return this.equipmentSlot;
    }
}
