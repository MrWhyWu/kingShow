package org.jupiter.flight.exec;

import org.jupiter.common.util.Bytes;






























public class ClassModifier
{
  private static final int CONSTANT_POOL_COUNT_INDEX = 8;
  private static final int CONSTANT_Utf8_info = 1;
  private static final int[] CONSTANT_ITEM_LENGTH = { -1, -1, -1, 5, 5, 9, 9, 3, 3, 5, 5, 5, 5, -1, -1, 4, 3, -1, 5 };
  





  private static final int u1 = 1;
  




  private static final int u2 = 2;
  




  private byte[] classBytes;
  





  public ClassModifier(byte[] classBytes)
  {
    this.classBytes = classBytes;
  }
  


  public byte[] modifyUTF8Constant(String originalString, String replaceString)
  {
    int offset = 8;
    

    int cpCount = Bytes.bytes2Int(classBytes, 8, 2);
    offset += 2;
    for (int i = 0; i < cpCount; i++) {
      int tag = Bytes.bytes2Int(classBytes, offset, 1);
      if (tag == 1) {
        offset++;
        








        int length = Bytes.bytes2Int(classBytes, offset, 2);
        offset += 2;
        
        String str = Bytes.bytes2String(classBytes, offset, length);
        if (str.equalsIgnoreCase(originalString)) {
          byte[] strBytes = Bytes.string2Bytes(replaceString);
          byte[] strLen = Bytes.int2Bytes(replaceString.length(), 2);
          
          classBytes = Bytes.replace(classBytes, offset - 2, 2, strLen);
          classBytes = Bytes.replace(classBytes, offset, length, strBytes);
          
          return classBytes;
        }
        offset += length;
      }
      else {
        offset += CONSTANT_ITEM_LENGTH[tag];
      }
    }
    return classBytes;
  }
}
