/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ public final class EpollTcpInfo
/*   4:    */ {
/*   5: 64 */   final long[] info = new long[32];
/*   6:    */   
/*   7:    */   public int state()
/*   8:    */   {
/*   9: 67 */     return (int)this.info[0];
/*  10:    */   }
/*  11:    */   
/*  12:    */   public int caState()
/*  13:    */   {
/*  14: 71 */     return (int)this.info[1];
/*  15:    */   }
/*  16:    */   
/*  17:    */   public int retransmits()
/*  18:    */   {
/*  19: 75 */     return (int)this.info[2];
/*  20:    */   }
/*  21:    */   
/*  22:    */   public int probes()
/*  23:    */   {
/*  24: 79 */     return (int)this.info[3];
/*  25:    */   }
/*  26:    */   
/*  27:    */   public int backoff()
/*  28:    */   {
/*  29: 83 */     return (int)this.info[4];
/*  30:    */   }
/*  31:    */   
/*  32:    */   public int options()
/*  33:    */   {
/*  34: 87 */     return (int)this.info[5];
/*  35:    */   }
/*  36:    */   
/*  37:    */   public int sndWscale()
/*  38:    */   {
/*  39: 91 */     return (int)this.info[6];
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int rcvWscale()
/*  43:    */   {
/*  44: 95 */     return (int)this.info[7];
/*  45:    */   }
/*  46:    */   
/*  47:    */   public long rto()
/*  48:    */   {
/*  49: 99 */     return this.info[8];
/*  50:    */   }
/*  51:    */   
/*  52:    */   public long ato()
/*  53:    */   {
/*  54:103 */     return this.info[9];
/*  55:    */   }
/*  56:    */   
/*  57:    */   public long sndMss()
/*  58:    */   {
/*  59:107 */     return this.info[10];
/*  60:    */   }
/*  61:    */   
/*  62:    */   public long rcvMss()
/*  63:    */   {
/*  64:111 */     return this.info[11];
/*  65:    */   }
/*  66:    */   
/*  67:    */   public long unacked()
/*  68:    */   {
/*  69:115 */     return this.info[12];
/*  70:    */   }
/*  71:    */   
/*  72:    */   public long sacked()
/*  73:    */   {
/*  74:119 */     return this.info[13];
/*  75:    */   }
/*  76:    */   
/*  77:    */   public long lost()
/*  78:    */   {
/*  79:123 */     return this.info[14];
/*  80:    */   }
/*  81:    */   
/*  82:    */   public long retrans()
/*  83:    */   {
/*  84:127 */     return this.info[15];
/*  85:    */   }
/*  86:    */   
/*  87:    */   public long fackets()
/*  88:    */   {
/*  89:131 */     return this.info[16];
/*  90:    */   }
/*  91:    */   
/*  92:    */   public long lastDataSent()
/*  93:    */   {
/*  94:135 */     return this.info[17];
/*  95:    */   }
/*  96:    */   
/*  97:    */   public long lastAckSent()
/*  98:    */   {
/*  99:139 */     return this.info[18];
/* 100:    */   }
/* 101:    */   
/* 102:    */   public long lastDataRecv()
/* 103:    */   {
/* 104:143 */     return this.info[19];
/* 105:    */   }
/* 106:    */   
/* 107:    */   public long lastAckRecv()
/* 108:    */   {
/* 109:147 */     return this.info[20];
/* 110:    */   }
/* 111:    */   
/* 112:    */   public long pmtu()
/* 113:    */   {
/* 114:151 */     return this.info[21];
/* 115:    */   }
/* 116:    */   
/* 117:    */   public long rcvSsthresh()
/* 118:    */   {
/* 119:155 */     return this.info[22];
/* 120:    */   }
/* 121:    */   
/* 122:    */   public long rtt()
/* 123:    */   {
/* 124:159 */     return this.info[23];
/* 125:    */   }
/* 126:    */   
/* 127:    */   public long rttvar()
/* 128:    */   {
/* 129:163 */     return this.info[24];
/* 130:    */   }
/* 131:    */   
/* 132:    */   public long sndSsthresh()
/* 133:    */   {
/* 134:167 */     return this.info[25];
/* 135:    */   }
/* 136:    */   
/* 137:    */   public long sndCwnd()
/* 138:    */   {
/* 139:171 */     return this.info[26];
/* 140:    */   }
/* 141:    */   
/* 142:    */   public long advmss()
/* 143:    */   {
/* 144:175 */     return this.info[27];
/* 145:    */   }
/* 146:    */   
/* 147:    */   public long reordering()
/* 148:    */   {
/* 149:179 */     return this.info[28];
/* 150:    */   }
/* 151:    */   
/* 152:    */   public long rcvRtt()
/* 153:    */   {
/* 154:183 */     return this.info[29];
/* 155:    */   }
/* 156:    */   
/* 157:    */   public long rcvSpace()
/* 158:    */   {
/* 159:187 */     return this.info[30];
/* 160:    */   }
/* 161:    */   
/* 162:    */   public long totalRetrans()
/* 163:    */   {
/* 164:191 */     return this.info[31];
/* 165:    */   }
/* 166:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollTcpInfo
 * JD-Core Version:    0.7.0.1
 */