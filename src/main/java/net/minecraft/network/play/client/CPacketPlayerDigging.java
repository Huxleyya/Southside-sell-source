package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CPacketPlayerDigging implements Packet<INetHandlerPlayServer>
{
    public final boolean skipProcess;
    private BlockPos position;
    private EnumFacing facing;

    /** Status of the digging (started, ongoing, broken). */
    private Action action;

    public CPacketPlayerDigging()
    {
        this.skipProcess = false;
    }

    public CPacketPlayerDigging(Action actionIn, BlockPos posIn, EnumFacing facingIn)
    {
        this.action = actionIn;
        this.position = posIn;
        this.facing = facingIn;
        this.skipProcess = false;
    }

    public CPacketPlayerDigging(Action actionIn, BlockPos posIn, EnumFacing facingIn, boolean skipProcess)
    {
        this.action = actionIn;
        this.position = posIn;
        this.facing = facingIn;
        this.skipProcess = skipProcess;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.action = (Action)buf.readEnumValue(Action.class);
        this.position = buf.readBlockPos();
        this.facing = EnumFacing.byIndex(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(this.action);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.facing.getIndex());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processPlayerDigging(this);
    }

    public BlockPos getPosition()
    {
        return this.position;
    }

    public EnumFacing getFacing()
    {
        return this.facing;
    }

    public Action getAction()
    {
        return this.action;
    }

    public static enum Action
    {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_HELD_ITEMS;
    }
}
