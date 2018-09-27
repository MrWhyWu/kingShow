package org.jupiter.common.util.internal;

import java.nio.ByteBuffer;
import java.util.Arrays;





























public final class UnsafeUtf8Util
{
  public static final int MAX_BYTES_PER_CHAR = 3;
  
  public static String decodeUtf8(byte[] bytes, int index, int size)
  {
    if ((index | size | bytes.length - index - size) < 0) {
      throw new ArrayIndexOutOfBoundsException("buffer length=" + bytes.length + ", index=" + index + ", size=" + size);
    }
    

    int offset = index;
    int limit = offset + size;
    


    char[] resultArr = new char[size];
    int resultPos = 0;
    


    while (offset < limit) {
      byte b = UnsafeUtil.getByte(bytes, offset);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      offset++;
      DecodeUtil.handleOneByte(b, resultArr, resultPos++);
    }
    
    while (offset < limit) {
      byte byte1 = UnsafeUtil.getByte(bytes, offset++);
      if (DecodeUtil.isOneByte(byte1)) {
        DecodeUtil.handleOneByte(byte1, resultArr, resultPos++);
        

        while (offset < limit) {
          byte b = UnsafeUtil.getByte(bytes, offset);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          offset++;
          DecodeUtil.handleOneByte(b, resultArr, resultPos++);
        } }
      if (DecodeUtil.isTwoBytes(byte1)) {
        if (offset >= limit) {
          throw invalidUtf8();
        }
        DecodeUtil.handleTwoBytes(byte1, UnsafeUtil.getByte(bytes, offset++), resultArr, resultPos++);
      }
      else if (DecodeUtil.isThreeBytes(byte1)) {
        if (offset >= limit - 1) {
          throw invalidUtf8();
        }
        DecodeUtil.handleThreeBytes(byte1, UnsafeUtil.getByte(bytes, offset++), UnsafeUtil.getByte(bytes, offset++), resultArr, resultPos++);


      }
      else
      {

        if (offset >= limit - 2) {
          throw invalidUtf8();
        }
        DecodeUtil.handleFourBytes(byte1, UnsafeUtil.getByte(bytes, offset++), UnsafeUtil.getByte(bytes, offset++), UnsafeUtil.getByte(bytes, offset++), resultArr, resultPos++);
        






        resultPos++;
      }
    }
    
    if (resultPos < resultArr.length) {
      resultArr = Arrays.copyOf(resultArr, resultPos);
    }
    return UnsafeUtil.moveToString(resultArr);
  }
  
  public static String decodeUtf8Direct(ByteBuffer buffer, int index, int size)
  {
    if ((index | size | buffer.limit() - index - size) < 0) {
      throw new ArrayIndexOutOfBoundsException("buffer limit=" + buffer.limit() + ", index=" + index + ", limit=" + size);
    }
    
    long address = UnsafeUtil.addressOffset(buffer) + index;
    long addressLimit = address + size;
    


    char[] resultArr = new char[size];
    int resultPos = 0;
    


    while (address < addressLimit) {
      byte b = UnsafeUtil.getByte(address);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      address += 1L;
      DecodeUtil.handleOneByte(b, resultArr, resultPos++);
    }
    
    while (address < addressLimit) {
      byte byte1 = UnsafeUtil.getByte(address++);
      if (DecodeUtil.isOneByte(byte1)) {
        DecodeUtil.handleOneByte(byte1, resultArr, resultPos++);
        

        while (address < addressLimit) {
          byte b = UnsafeUtil.getByte(address);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          address += 1L;
          DecodeUtil.handleOneByte(b, resultArr, resultPos++);
        } }
      if (DecodeUtil.isTwoBytes(byte1)) {
        if (address >= addressLimit) {
          throw invalidUtf8();
        }
        DecodeUtil.handleTwoBytes(byte1, UnsafeUtil.getByte(address++), resultArr, resultPos++);
      }
      else if (DecodeUtil.isThreeBytes(byte1)) {
        if (address >= addressLimit - 1L) {
          throw invalidUtf8();
        }
        DecodeUtil.handleThreeBytes(byte1, UnsafeUtil.getByte(address++), UnsafeUtil.getByte(address++), resultArr, resultPos++);


      }
      else
      {

        if (address >= addressLimit - 2L) {
          throw invalidUtf8();
        }
        DecodeUtil.handleFourBytes(byte1, UnsafeUtil.getByte(address++), UnsafeUtil.getByte(address++), UnsafeUtil.getByte(address++), resultArr, resultPos++);
        






        resultPos++;
      }
    }
    
    if (resultPos < resultArr.length) {
      resultArr = Arrays.copyOf(resultArr, resultPos);
    }
    return UnsafeUtil.moveToString(resultArr);
  }
  
  public static int encodeUtf8(CharSequence in, byte[] out, int offset, int length) {
    long outIx = offset;
    long outLimit = outIx + length;
    int inLimit = in.length();
    if ((inLimit > length) || (out.length - length < offset))
    {
      throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inLimit - 1) + " at index " + (offset + length));
    }
    

    char c;
    
    for (int inIx = 0; 
        (inIx < inLimit) && ((c = in.charAt(inIx)) < ''); inIx++) {
      UnsafeUtil.putByte(out, outIx++, (byte)c);
    }
    if (inIx == inLimit)
    {
      return (int)outIx;
    }
    for (; 
        inIx < inLimit; inIx++) {
      char c = in.charAt(inIx);
      if ((c < '') && (outIx < outLimit)) {
        UnsafeUtil.putByte(out, outIx++, (byte)c);
      } else if ((c < 'ࠀ') && (outIx <= outLimit - 2L)) {
        UnsafeUtil.putByte(out, outIx++, (byte)(0x3C0 | c >>> '\006'));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & c));
      } else if (((c < 55296) || (57343 < c)) && (outIx <= outLimit - 3L))
      {
        UnsafeUtil.putByte(out, outIx++, (byte)(0x1E0 | c >>> '\f'));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & c >>> '\006'));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & c));
      } else if (outIx <= outLimit - 4L)
      {
        char low;
        
        if ((inIx + 1 == inLimit) || (!Character.isSurrogatePair(c, low = in.charAt(++inIx))))
          throw new IllegalArgumentException("Unpaired surrogate at index " + (inIx - 1) + " of " + inLimit);
        char low;
        int codePoint = Character.toCodePoint(c, low);
        UnsafeUtil.putByte(out, outIx++, (byte)(0xF0 | codePoint >>> 18));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & codePoint >>> 12));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & codePoint >>> 6));
        UnsafeUtil.putByte(out, outIx++, (byte)(0x80 | 0x3F & codePoint));
      } else {
        if ((55296 <= c) && (c <= 57343) && ((inIx + 1 == inLimit) || (!Character.isSurrogatePair(c, in.charAt(inIx + 1)))))
        {

          throw new IllegalArgumentException("Unpaired surrogate at index " + inIx + " of " + inLimit);
        }
        
        throw new ArrayIndexOutOfBoundsException("Failed writing " + c + " at index " + outIx);
      }
    }
    

    return (int)outIx;
  }
  
  public static void encodeUtf8Direct(CharSequence in, ByteBuffer out) {
    long address = UnsafeUtil.addressOffset(out);
    long outIx = address + out.position();
    long outLimit = address + out.limit();
    int inLimit = in.length();
    if (inLimit > outLimit - outIx)
    {
      throw new ArrayIndexOutOfBoundsException("Failed writing " + in.charAt(inLimit - 1) + " at index " + out.limit());
    }
    

    char c;
    
    for (int inIx = 0; 
        (inIx < inLimit) && ((c = in.charAt(inIx)) < ''); inIx++) {
      UnsafeUtil.putByte(outIx++, (byte)c);
    }
    if (inIx == inLimit)
    {
      out.position((int)(outIx - address));
      return;
    }
    for (; 
        inIx < inLimit; inIx++) {
      char c = in.charAt(inIx);
      if ((c < '') && (outIx < outLimit)) {
        UnsafeUtil.putByte(outIx++, (byte)c);
      } else if ((c < 'ࠀ') && (outIx <= outLimit - 2L)) {
        UnsafeUtil.putByte(outIx++, (byte)(0x3C0 | c >>> '\006'));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & c));
      } else if (((c < 55296) || (57343 < c)) && (outIx <= outLimit - 3L))
      {
        UnsafeUtil.putByte(outIx++, (byte)(0x1E0 | c >>> '\f'));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & c >>> '\006'));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & c));
      } else if (outIx <= outLimit - 4L)
      {
        char low;
        
        if ((inIx + 1 == inLimit) || (!Character.isSurrogatePair(c, low = in.charAt(++inIx))))
          throw new IllegalArgumentException("Unpaired surrogate at index " + (inIx - 1) + " of " + inLimit);
        char low;
        int codePoint = Character.toCodePoint(c, low);
        UnsafeUtil.putByte(outIx++, (byte)(0xF0 | codePoint >>> 18));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & codePoint >>> 12));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & codePoint >>> 6));
        UnsafeUtil.putByte(outIx++, (byte)(0x80 | 0x3F & codePoint));
      } else {
        if ((55296 <= c) && (c <= 57343) && ((inIx + 1 == inLimit) || (!Character.isSurrogatePair(c, in.charAt(inIx + 1)))))
        {

          throw new IllegalArgumentException("Unpaired surrogate at index " + inIx + " of " + inLimit);
        }
        
        throw new ArrayIndexOutOfBoundsException("Failed writing " + c + " at index " + outIx);
      }
    }
    

    out.position((int)(outIx - address));
  }
  








  public static int encodedLength(CharSequence sequence)
  {
    int utf16Length = sequence.length();
    int utf8Length = utf16Length;
    int i = 0;
    

    while ((i < utf16Length) && (sequence.charAt(i) < '')) {
      i++;
    }
    for (; 
        
        i < utf16Length; i++) {
      char c = sequence.charAt(i);
      if (c < 'ࠀ') {
        utf8Length += ('' - c >>> 31);
      } else {
        utf8Length += encodedLengthGeneral(sequence, i);
        break;
      }
    }
    
    if (utf8Length < utf16Length)
    {
      throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (utf8Length + 4294967296L));
    }
    
    return utf8Length;
  }
  
  private static int encodedLengthGeneral(CharSequence sequence, int start) {
    int utf16Length = sequence.length();
    int utf8Length = 0;
    for (int i = start; i < utf16Length; i++) {
      char c = sequence.charAt(i);
      if (c < 'ࠀ') {
        utf8Length += ('' - c >>> 31);
      } else {
        utf8Length += 2;
        
        if ((55296 <= c) && (c <= 57343))
        {
          int cp = Character.codePointAt(sequence, i);
          if (cp < 65536) {
            throw new IllegalArgumentException("Unpaired surrogate at index " + i + " of " + utf16Length);
          }
          i++;
        }
      }
    }
    return utf8Length;
  }
  



  private static class DecodeUtil
  {
    private DecodeUtil() {}
    


    private static boolean isOneByte(byte b)
    {
      return b >= 0;
    }
    


    private static boolean isTwoBytes(byte b)
    {
      return b < -32;
    }
    


    private static boolean isThreeBytes(byte b)
    {
      return b < -16;
    }
    
    private static void handleOneByte(byte byte1, char[] resultArr, int resultPos) {
      resultArr[resultPos] = ((char)byte1);
    }
    


    private static void handleTwoBytes(byte byte1, byte byte2, char[] resultArr, int resultPos)
    {
      if ((byte1 < -62) || (isNotTrailingByte(byte2)))
      {
        throw UnsafeUtf8Util.invalidUtf8();
      }
      resultArr[resultPos] = ((char)((byte1 & 0x1F) << 6 | trailingByteValue(byte2)));
    }
    
    private static void handleThreeBytes(byte byte1, byte byte2, byte byte3, char[] resultArr, int resultPos)
    {
      if ((isNotTrailingByte(byte2)) || ((byte1 == -32) && (byte2 < -96)) || ((byte1 == -19) && (byte2 >= -96)) || (isNotTrailingByte(byte3)))
      {




        throw UnsafeUtf8Util.invalidUtf8();
      }
      resultArr[resultPos] = ((char)((byte1 & 0xF) << 12 | trailingByteValue(byte2) << 6 | trailingByteValue(byte3)));
    }
    

    private static void handleFourBytes(byte byte1, byte byte2, byte byte3, byte byte4, char[] resultArr, int resultPos)
    {
      if ((isNotTrailingByte(byte2)) || ((byte1 << 28) + (byte2 - -112) >> 30 != 0) || (isNotTrailingByte(byte3)) || (isNotTrailingByte(byte4)))
      {









        throw UnsafeUtf8Util.invalidUtf8();
      }
      int codePoint = (byte1 & 0x7) << 18 | trailingByteValue(byte2) << 12 | trailingByteValue(byte3) << 6 | trailingByteValue(byte4);
      


      resultArr[resultPos] = highSurrogate(codePoint);
      resultArr[(resultPos + 1)] = lowSurrogate(codePoint);
    }
    


    private static boolean isNotTrailingByte(byte b)
    {
      return b > -65;
    }
    


    private static int trailingByteValue(byte b)
    {
      return b & 0x3F;
    }
    
    private static char highSurrogate(int codePoint) {
      return (char)(55232 + (codePoint >>> 10));
    }
    
    private static char lowSurrogate(int codePoint)
    {
      return (char)(56320 + (codePoint & 0x3FF));
    }
  }
  
  static IllegalStateException invalidUtf8() {
    return new IllegalStateException("Message had invalid UTF-8.");
  }
  
  private UnsafeUtf8Util() {}
}
