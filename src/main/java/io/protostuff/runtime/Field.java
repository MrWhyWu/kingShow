/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Input;
/*  4:   */ import io.protostuff.Output;
/*  5:   */ import io.protostuff.Pipe;
/*  6:   */ import io.protostuff.Tag;
/*  7:   */ import io.protostuff.WireFormat.FieldType;
/*  8:   */ import java.io.IOException;
/*  9:   */ 
/* 10:   */ public abstract class Field<T>
/* 11:   */ {
/* 12:   */   public final WireFormat.FieldType type;
/* 13:   */   public final int number;
/* 14:   */   public final String name;
/* 15:   */   public final boolean repeated;
/* 16:   */   public final int groupFilter;
/* 17:   */   
/* 18:   */   public Field(WireFormat.FieldType type, int number, String name, boolean repeated, Tag tag)
/* 19:   */   {
/* 20:27 */     this.type = type;
/* 21:28 */     this.number = number;
/* 22:29 */     this.name = name;
/* 23:30 */     this.repeated = repeated;
/* 24:31 */     this.groupFilter = (tag == null ? 0 : tag.groupFilter());
/* 25:   */   }
/* 26:   */   
/* 27:   */   public Field(WireFormat.FieldType type, int number, String name, Tag tag)
/* 28:   */   {
/* 29:37 */     this(type, number, name, false, tag);
/* 30:   */   }
/* 31:   */   
/* 32:   */   protected abstract void writeTo(Output paramOutput, T paramT)
/* 33:   */     throws IOException;
/* 34:   */   
/* 35:   */   protected abstract void mergeFrom(Input paramInput, T paramT)
/* 36:   */     throws IOException;
/* 37:   */   
/* 38:   */   protected abstract void transfer(Pipe paramPipe, Input paramInput, Output paramOutput, boolean paramBoolean)
/* 39:   */     throws IOException;
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.Field
 * JD-Core Version:    0.7.0.1
 */