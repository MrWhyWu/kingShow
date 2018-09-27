package org.jupiter.serialization;

import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.collection.ByteObjectHashMap;
import org.jupiter.common.util.collection.ByteObjectMap;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;

























public final class SerializerFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(SerializerFactory.class);
  
  private static final ByteObjectMap<Serializer> serializers = new ByteObjectHashMap();
  
  static {
    Iterable<Serializer> all = JServiceLoader.load(Serializer.class);
    for (Serializer s : all) {
      serializers.put(s.code(), s);
    }
    logger.info("Supported serializers: {}.", serializers); }
  
  private SerializerFactory() {}
  
  public static Serializer getSerializer(byte code) { Serializer serializer = (Serializer)serializers.get(code);
    
    if (serializer == null) {
      SerializerType type = SerializerType.parse(code);
      if (type != null) {
        throw new IllegalArgumentException("Serializer implementation [" + type.name() + "] not found");
      }
      throw new IllegalArgumentException("Unsupported serializer type with code: " + code);
    }
    

    return serializer;
  }
}
