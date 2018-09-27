package org.jupiter.serialization.kryo.io;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import java.nio.ByteBuffer;
import org.jupiter.serialization.io.OutputBuf;




















class NioBufOutput
  extends ByteBufferOutput
{
  protected final OutputBuf outputBuf;
  
  NioBufOutput(OutputBuf outputBuf, int minWritableBytes, int maxCapacity)
  {
    this.outputBuf = outputBuf;
    this.maxCapacity = maxCapacity;
    niobuffer = outputBuf.nioByteBuffer(minWritableBytes);
    capacity = niobuffer.remaining();
  }
  
  protected boolean require(int required) throws KryoException
  {
    if (capacity - position >= required) {
      return false;
    }
    if (required > maxCapacity) {
      throw new KryoException("Buffer overflow. Max capacity: " + maxCapacity + ", required: " + required);
    }
    
    flush();
    
    while (capacity - position < required) {
      if (capacity == maxCapacity) {
        throw new KryoException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
      }
      
      if (capacity == 0) {
        capacity = 1;
      }
      capacity = Math.min(capacity << 1, maxCapacity);
      if (capacity < 0) {
        capacity = maxCapacity;
      }
    }
    
    niobuffer = outputBuf.nioByteBuffer(capacity - position);
    capacity = niobuffer.limit();
    return true;
  }
}
