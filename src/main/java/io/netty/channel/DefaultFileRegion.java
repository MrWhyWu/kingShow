/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.AbstractReferenceCounted;
/*   4:    */ import io.netty.util.IllegalReferenceCountException;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.io.File;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.io.RandomAccessFile;
/*  10:    */ import java.nio.channels.FileChannel;
/*  11:    */ import java.nio.channels.WritableByteChannel;
/*  12:    */ 
/*  13:    */ public class DefaultFileRegion
/*  14:    */   extends AbstractReferenceCounted
/*  15:    */   implements FileRegion
/*  16:    */ {
/*  17: 37 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultFileRegion.class);
/*  18:    */   private final File f;
/*  19:    */   private final long position;
/*  20:    */   private final long count;
/*  21:    */   private long transferred;
/*  22:    */   private FileChannel file;
/*  23:    */   
/*  24:    */   public DefaultFileRegion(FileChannel file, long position, long count)
/*  25:    */   {
/*  26: 52 */     if (file == null) {
/*  27: 53 */       throw new NullPointerException("file");
/*  28:    */     }
/*  29: 55 */     if (position < 0L) {
/*  30: 56 */       throw new IllegalArgumentException("position must be >= 0 but was " + position);
/*  31:    */     }
/*  32: 58 */     if (count < 0L) {
/*  33: 59 */       throw new IllegalArgumentException("count must be >= 0 but was " + count);
/*  34:    */     }
/*  35: 61 */     this.file = file;
/*  36: 62 */     this.position = position;
/*  37: 63 */     this.count = count;
/*  38: 64 */     this.f = null;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public DefaultFileRegion(File f, long position, long count)
/*  42:    */   {
/*  43: 76 */     if (f == null) {
/*  44: 77 */       throw new NullPointerException("f");
/*  45:    */     }
/*  46: 79 */     if (position < 0L) {
/*  47: 80 */       throw new IllegalArgumentException("position must be >= 0 but was " + position);
/*  48:    */     }
/*  49: 82 */     if (count < 0L) {
/*  50: 83 */       throw new IllegalArgumentException("count must be >= 0 but was " + count);
/*  51:    */     }
/*  52: 85 */     this.position = position;
/*  53: 86 */     this.count = count;
/*  54: 87 */     this.f = f;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public boolean isOpen()
/*  58:    */   {
/*  59: 94 */     return this.file != null;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void open()
/*  63:    */     throws IOException
/*  64:    */   {
/*  65:101 */     if ((!isOpen()) && (refCnt() > 0)) {
/*  66:103 */       this.file = new RandomAccessFile(this.f, "r").getChannel();
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   public long position()
/*  71:    */   {
/*  72:109 */     return this.position;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public long count()
/*  76:    */   {
/*  77:114 */     return this.count;
/*  78:    */   }
/*  79:    */   
/*  80:    */   @Deprecated
/*  81:    */   public long transfered()
/*  82:    */   {
/*  83:120 */     return this.transferred;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public long transferred()
/*  87:    */   {
/*  88:125 */     return this.transferred;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public long transferTo(WritableByteChannel target, long position)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:130 */     long count = this.count - position;
/*  95:131 */     if ((count < 0L) || (position < 0L)) {
/*  96:132 */       throw new IllegalArgumentException("position out of range: " + position + " (expected: 0 - " + (this.count - 1L) + ')');
/*  97:    */     }
/*  98:136 */     if (count == 0L) {
/*  99:137 */       return 0L;
/* 100:    */     }
/* 101:139 */     if (refCnt() == 0) {
/* 102:140 */       throw new IllegalReferenceCountException(0);
/* 103:    */     }
/* 104:143 */     open();
/* 105:    */     
/* 106:145 */     long written = this.file.transferTo(this.position + position, count, target);
/* 107:146 */     if (written > 0L) {
/* 108:147 */       this.transferred += written;
/* 109:    */     }
/* 110:149 */     return written;
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected void deallocate()
/* 114:    */   {
/* 115:154 */     FileChannel file = this.file;
/* 116:156 */     if (file == null) {
/* 117:157 */       return;
/* 118:    */     }
/* 119:159 */     this.file = null;
/* 120:    */     try
/* 121:    */     {
/* 122:162 */       file.close();
/* 123:    */     }
/* 124:    */     catch (IOException e)
/* 125:    */     {
/* 126:164 */       if (logger.isWarnEnabled()) {
/* 127:165 */         logger.warn("Failed to close a file.", e);
/* 128:    */       }
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public FileRegion retain()
/* 133:    */   {
/* 134:172 */     super.retain();
/* 135:173 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public FileRegion retain(int increment)
/* 139:    */   {
/* 140:178 */     super.retain(increment);
/* 141:179 */     return this;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public FileRegion touch()
/* 145:    */   {
/* 146:184 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public FileRegion touch(Object hint)
/* 150:    */   {
/* 151:189 */     return this;
/* 152:    */   }
/* 153:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultFileRegion
 * JD-Core Version:    0.7.0.1
 */