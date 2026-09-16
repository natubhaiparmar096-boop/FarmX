package com.jelly.farmhelperv2.event;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChunkServerLoadEvent extends Event {
    public int x;
    public int z;
    public Chunk chunk;

    public int getX() { return x; }
    public int getZ() { return z; }
    public Chunk getChunk() { return chunk; }

    public ChunkServerLoadEvent(int x, int z, Chunk c) {
        this.x = x;
        this.z = z;
        this.chunk = c;
    }
}
