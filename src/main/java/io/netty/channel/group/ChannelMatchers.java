/*   1:    */ package io.netty.channel.group;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ServerChannel;
/*   5:    */ 
/*   6:    */ public final class ChannelMatchers
/*   7:    */ {
/*   8: 26 */   private static final ChannelMatcher ALL_MATCHER = new ChannelMatcher()
/*   9:    */   {
/*  10:    */     public boolean matches(Channel channel)
/*  11:    */     {
/*  12: 29 */       return true;
/*  13:    */     }
/*  14:    */   };
/*  15: 33 */   private static final ChannelMatcher SERVER_CHANNEL_MATCHER = isInstanceOf(ServerChannel.class);
/*  16: 34 */   private static final ChannelMatcher NON_SERVER_CHANNEL_MATCHER = isNotInstanceOf(ServerChannel.class);
/*  17:    */   
/*  18:    */   public static ChannelMatcher all()
/*  19:    */   {
/*  20: 44 */     return ALL_MATCHER;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public static ChannelMatcher isNot(Channel channel)
/*  24:    */   {
/*  25: 51 */     return invert(is(channel));
/*  26:    */   }
/*  27:    */   
/*  28:    */   public static ChannelMatcher is(Channel channel)
/*  29:    */   {
/*  30: 58 */     return new InstanceMatcher(channel);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public static ChannelMatcher isInstanceOf(Class<? extends Channel> clazz)
/*  34:    */   {
/*  35: 66 */     return new ClassMatcher(clazz);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static ChannelMatcher isNotInstanceOf(Class<? extends Channel> clazz)
/*  39:    */   {
/*  40: 74 */     return invert(isInstanceOf(clazz));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static ChannelMatcher isServerChannel()
/*  44:    */   {
/*  45: 81 */     return SERVER_CHANNEL_MATCHER;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static ChannelMatcher isNonServerChannel()
/*  49:    */   {
/*  50: 89 */     return NON_SERVER_CHANNEL_MATCHER;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static ChannelMatcher invert(ChannelMatcher matcher)
/*  54:    */   {
/*  55: 96 */     return new InvertMatcher(matcher);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public static ChannelMatcher compose(ChannelMatcher... matchers)
/*  59:    */   {
/*  60:104 */     if (matchers.length < 1) {
/*  61:105 */       throw new IllegalArgumentException("matchers must at least contain one element");
/*  62:    */     }
/*  63:107 */     if (matchers.length == 1) {
/*  64:108 */       return matchers[0];
/*  65:    */     }
/*  66:110 */     return new CompositeMatcher(matchers);
/*  67:    */   }
/*  68:    */   
/*  69:    */   private static final class CompositeMatcher
/*  70:    */     implements ChannelMatcher
/*  71:    */   {
/*  72:    */     private final ChannelMatcher[] matchers;
/*  73:    */     
/*  74:    */     CompositeMatcher(ChannelMatcher... matchers)
/*  75:    */     {
/*  76:117 */       this.matchers = matchers;
/*  77:    */     }
/*  78:    */     
/*  79:    */     public boolean matches(Channel channel)
/*  80:    */     {
/*  81:122 */       for (ChannelMatcher m : this.matchers) {
/*  82:123 */         if (!m.matches(channel)) {
/*  83:124 */           return false;
/*  84:    */         }
/*  85:    */       }
/*  86:127 */       return true;
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   private static final class InvertMatcher
/*  91:    */     implements ChannelMatcher
/*  92:    */   {
/*  93:    */     private final ChannelMatcher matcher;
/*  94:    */     
/*  95:    */     InvertMatcher(ChannelMatcher matcher)
/*  96:    */     {
/*  97:135 */       this.matcher = matcher;
/*  98:    */     }
/*  99:    */     
/* 100:    */     public boolean matches(Channel channel)
/* 101:    */     {
/* 102:140 */       return !this.matcher.matches(channel);
/* 103:    */     }
/* 104:    */   }
/* 105:    */   
/* 106:    */   private static final class InstanceMatcher
/* 107:    */     implements ChannelMatcher
/* 108:    */   {
/* 109:    */     private final Channel channel;
/* 110:    */     
/* 111:    */     InstanceMatcher(Channel channel)
/* 112:    */     {
/* 113:148 */       this.channel = channel;
/* 114:    */     }
/* 115:    */     
/* 116:    */     public boolean matches(Channel ch)
/* 117:    */     {
/* 118:153 */       return this.channel == ch;
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   private static final class ClassMatcher
/* 123:    */     implements ChannelMatcher
/* 124:    */   {
/* 125:    */     private final Class<? extends Channel> clazz;
/* 126:    */     
/* 127:    */     ClassMatcher(Class<? extends Channel> clazz)
/* 128:    */     {
/* 129:161 */       this.clazz = clazz;
/* 130:    */     }
/* 131:    */     
/* 132:    */     public boolean matches(Channel ch)
/* 133:    */     {
/* 134:166 */       return this.clazz.isInstance(ch);
/* 135:    */     }
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.ChannelMatchers
 * JD-Core Version:    0.7.0.1
 */