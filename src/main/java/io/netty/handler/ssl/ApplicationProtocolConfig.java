/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public final class ApplicationProtocolConfig
/*   8:    */ {
/*   9: 34 */   public static final ApplicationProtocolConfig DISABLED = new ApplicationProtocolConfig();
/*  10:    */   private final List<String> supportedProtocols;
/*  11:    */   private final Protocol protocol;
/*  12:    */   private final SelectorFailureBehavior selectorBehavior;
/*  13:    */   private final SelectedListenerFailureBehavior selectedBehavior;
/*  14:    */   
/*  15:    */   public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior, SelectedListenerFailureBehavior selectedBehavior, Iterable<String> supportedProtocols)
/*  16:    */   {
/*  17: 50 */     this(protocol, selectorBehavior, selectedBehavior, ApplicationProtocolUtil.toList(supportedProtocols));
/*  18:    */   }
/*  19:    */   
/*  20:    */   public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior, SelectedListenerFailureBehavior selectedBehavior, String... supportedProtocols)
/*  21:    */   {
/*  22: 62 */     this(protocol, selectorBehavior, selectedBehavior, ApplicationProtocolUtil.toList(supportedProtocols));
/*  23:    */   }
/*  24:    */   
/*  25:    */   private ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior, SelectedListenerFailureBehavior selectedBehavior, List<String> supportedProtocols)
/*  26:    */   {
/*  27: 75 */     this.supportedProtocols = Collections.unmodifiableList((List)ObjectUtil.checkNotNull(supportedProtocols, "supportedProtocols"));
/*  28: 76 */     this.protocol = ((Protocol)ObjectUtil.checkNotNull(protocol, "protocol"));
/*  29: 77 */     this.selectorBehavior = ((SelectorFailureBehavior)ObjectUtil.checkNotNull(selectorBehavior, "selectorBehavior"));
/*  30: 78 */     this.selectedBehavior = ((SelectedListenerFailureBehavior)ObjectUtil.checkNotNull(selectedBehavior, "selectedBehavior"));
/*  31: 80 */     if (protocol == Protocol.NONE) {
/*  32: 81 */       throw new IllegalArgumentException("protocol (" + Protocol.NONE + ") must not be " + Protocol.NONE + '.');
/*  33:    */     }
/*  34: 83 */     if (supportedProtocols.isEmpty()) {
/*  35: 84 */       throw new IllegalArgumentException("supportedProtocols must be not empty");
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   private ApplicationProtocolConfig()
/*  40:    */   {
/*  41: 92 */     this.supportedProtocols = Collections.emptyList();
/*  42: 93 */     this.protocol = Protocol.NONE;
/*  43: 94 */     this.selectorBehavior = SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
/*  44: 95 */     this.selectedBehavior = SelectedListenerFailureBehavior.ACCEPT;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public static enum Protocol
/*  48:    */   {
/*  49:102 */     NONE,  NPN,  ALPN,  NPN_AND_ALPN;
/*  50:    */     
/*  51:    */     private Protocol() {}
/*  52:    */   }
/*  53:    */   
/*  54:    */   public static enum SelectorFailureBehavior
/*  55:    */   {
/*  56:116 */     FATAL_ALERT,  NO_ADVERTISE,  CHOOSE_MY_LAST_PROTOCOL;
/*  57:    */     
/*  58:    */     private SelectorFailureBehavior() {}
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static enum SelectedListenerFailureBehavior
/*  62:    */   {
/*  63:143 */     ACCEPT,  FATAL_ALERT,  CHOOSE_MY_LAST_PROTOCOL;
/*  64:    */     
/*  65:    */     private SelectedListenerFailureBehavior() {}
/*  66:    */   }
/*  67:    */   
/*  68:    */   public List<String> supportedProtocols()
/*  69:    */   {
/*  70:162 */     return this.supportedProtocols;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public Protocol protocol()
/*  74:    */   {
/*  75:169 */     return this.protocol;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public SelectorFailureBehavior selectorFailureBehavior()
/*  79:    */   {
/*  80:176 */     return this.selectorBehavior;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public SelectedListenerFailureBehavior selectedListenerFailureBehavior()
/*  84:    */   {
/*  85:183 */     return this.selectedBehavior;
/*  86:    */   }
/*  87:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ApplicationProtocolConfig
 * JD-Core Version:    0.7.0.1
 */