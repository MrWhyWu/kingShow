/*    1:     */ package io.netty.handler.codec.compression;
/*    2:     */ 
/*    3:     */ final class Bzip2DivSufSort
/*    4:     */ {
/*    5:     */   private static final int STACK_SIZE = 64;
/*    6:     */   private static final int BUCKET_A_SIZE = 256;
/*    7:     */   private static final int BUCKET_B_SIZE = 65536;
/*    8:     */   private static final int SS_BLOCKSIZE = 1024;
/*    9:     */   private static final int INSERTIONSORT_THRESHOLD = 8;
/*   10:  33 */   private static final int[] LOG_2_TABLE = { -1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
/*   11:     */   private final int[] SA;
/*   12:     */   private final byte[] T;
/*   13:     */   private final int n;
/*   14:     */   
/*   15:     */   Bzip2DivSufSort(byte[] block, int[] bwtBlock, int blockLength)
/*   16:     */   {
/*   17:  54 */     this.T = block;
/*   18:  55 */     this.SA = bwtBlock;
/*   19:  56 */     this.n = blockLength;
/*   20:     */   }
/*   21:     */   
/*   22:     */   private static void swapElements(int[] array1, int idx1, int[] array2, int idx2)
/*   23:     */   {
/*   24:  60 */     int temp = array1[idx1];
/*   25:  61 */     array1[idx1] = array2[idx2];
/*   26:  62 */     array2[idx2] = temp;
/*   27:     */   }
/*   28:     */   
/*   29:     */   private int ssCompare(int p1, int p2, int depth)
/*   30:     */   {
/*   31:  66 */     int[] SA = this.SA;
/*   32:  67 */     byte[] T = this.T;
/*   33:     */     
/*   34:     */ 
/*   35:  70 */     int U1n = SA[(p1 + 1)] + 2;
/*   36:  71 */     int U2n = SA[(p2 + 1)] + 2;
/*   37:     */     
/*   38:  73 */     int U1 = depth + SA[p1];
/*   39:  74 */     int U2 = depth + SA[p2];
/*   40:  76 */     while ((U1 < U1n) && (U2 < U2n) && (T[U1] == T[U2]))
/*   41:     */     {
/*   42:  77 */       U1++;
/*   43:  78 */       U2++;
/*   44:     */     }
/*   45:  81 */     return U2 < U2n ? -1 : U1 < U1n ? 1 : U2 < U2n ? (T[U1] & 0xFF) - (T[U2] & 0xFF) : 0;
/*   46:     */   }
/*   47:     */   
/*   48:     */   private int ssCompareLast(int pa, int p1, int p2, int depth, int size)
/*   49:     */   {
/*   50:  87 */     int[] SA = this.SA;
/*   51:  88 */     byte[] T = this.T;
/*   52:     */     
/*   53:  90 */     int U1 = depth + SA[p1];
/*   54:  91 */     int U2 = depth + SA[p2];
/*   55:  92 */     int U1n = size;
/*   56:  93 */     int U2n = SA[(p2 + 1)] + 2;
/*   57:  95 */     while ((U1 < U1n) && (U2 < U2n) && (T[U1] == T[U2]))
/*   58:     */     {
/*   59:  96 */       U1++;
/*   60:  97 */       U2++;
/*   61:     */     }
/*   62: 100 */     if (U1 < U1n) {
/*   63: 101 */       return U2 < U2n ? (T[U1] & 0xFF) - (T[U2] & 0xFF) : 1;
/*   64:     */     }
/*   65: 103 */     if (U2 == U2n) {
/*   66: 104 */       return 1;
/*   67:     */     }
/*   68: 107 */     U1 %= size;
/*   69: 108 */     U1n = SA[pa] + 2;
/*   70: 109 */     while ((U1 < U1n) && (U2 < U2n) && (T[U1] == T[U2]))
/*   71:     */     {
/*   72: 110 */       U1++;
/*   73: 111 */       U2++;
/*   74:     */     }
/*   75: 114 */     return U2 < U2n ? -1 : U1 < U1n ? 1 : U2 < U2n ? (T[U1] & 0xFF) - (T[U2] & 0xFF) : 0;
/*   76:     */   }
/*   77:     */   
/*   78:     */   private void ssInsertionSort(int pa, int first, int last, int depth)
/*   79:     */   {
/*   80: 120 */     int[] SA = this.SA;
/*   81: 126 */     for (int i = last - 2; first <= i; i--)
/*   82:     */     {
/*   83: 127 */       int t = SA[i];
/*   84:     */       int r;
/*   85: 127 */       for (int j = i + 1; 0 < (r = ssCompare(pa + t, pa + SA[j], depth));)
/*   86:     */       {
/*   87:     */         do
/*   88:     */         {
/*   89: 129 */           SA[(j - 1)] = SA[j];
/*   90: 130 */           j++;
/*   91: 130 */         } while ((j < last) && (SA[j] < 0));
/*   92: 131 */         if (last <= j) {
/*   93:     */           break;
/*   94:     */         }
/*   95:     */       }
/*   96: 135 */       if (r == 0) {
/*   97: 136 */         SA[j] ^= 0xFFFFFFFF;
/*   98:     */       }
/*   99: 138 */       SA[(j - 1)] = t;
/*  100:     */     }
/*  101:     */   }
/*  102:     */   
/*  103:     */   private void ssFixdown(int td, int pa, int sa, int i, int size)
/*  104:     */   {
/*  105: 143 */     int[] SA = this.SA;
/*  106: 144 */     byte[] T = this.T;
/*  107:     */     
/*  108:     */ 
/*  109:     */ 
/*  110:     */ 
/*  111:     */ 
/*  112: 150 */     int v = SA[(sa + i)];
/*  113:     */     int j;
/*  114:     */     int k;
/*  115: 150 */     for (int c = T[(td + SA[(pa + v)])] & 0xFF; (j = 2 * i + 1) < size; i = k)
/*  116:     */     {
/*  117: 151 */       int d = T[(td + SA[(pa + SA[(sa + (k = j++))])])] & 0xFF;
/*  118:     */       int e;
/*  119: 152 */       if (d < (e = T[(td + SA[(pa + SA[(sa + j)])])] & 0xFF))
/*  120:     */       {
/*  121: 153 */         k = j;
/*  122: 154 */         d = e;
/*  123:     */       }
/*  124: 156 */       if (d <= c) {
/*  125:     */         break;
/*  126:     */       }
/*  127: 150 */       SA[(sa + i)] = SA[(sa + k)];
/*  128:     */     }
/*  129: 160 */     SA[(sa + i)] = v;
/*  130:     */   }
/*  131:     */   
/*  132:     */   private void ssHeapSort(int td, int pa, int sa, int size)
/*  133:     */   {
/*  134: 164 */     int[] SA = this.SA;
/*  135: 165 */     byte[] T = this.T;
/*  136:     */     
/*  137:     */ 
/*  138:     */ 
/*  139:     */ 
/*  140: 170 */     int m = size;
/*  141: 171 */     if (size % 2 == 0)
/*  142:     */     {
/*  143: 172 */       m--;
/*  144: 173 */       if ((T[(td + SA[(pa + SA[(sa + m / 2)])])] & 0xFF) < (T[(td + SA[(pa + SA[(sa + m)])])] & 0xFF)) {
/*  145: 174 */         swapElements(SA, sa + m, SA, sa + m / 2);
/*  146:     */       }
/*  147:     */     }
/*  148: 178 */     for (int i = m / 2 - 1; 0 <= i; i--) {
/*  149: 179 */       ssFixdown(td, pa, sa, i, m);
/*  150:     */     }
/*  151: 182 */     if (size % 2 == 0)
/*  152:     */     {
/*  153: 183 */       swapElements(SA, sa, SA, sa + m);
/*  154: 184 */       ssFixdown(td, pa, sa, 0, m);
/*  155:     */     }
/*  156: 187 */     for (i = m - 1; 0 < i; i--)
/*  157:     */     {
/*  158: 188 */       int t = SA[sa];
/*  159: 189 */       SA[sa] = SA[(sa + i)];
/*  160: 190 */       ssFixdown(td, pa, sa, 0, i);
/*  161: 191 */       SA[(sa + i)] = t;
/*  162:     */     }
/*  163:     */   }
/*  164:     */   
/*  165:     */   private int ssMedian3(int td, int pa, int v1, int v2, int v3)
/*  166:     */   {
/*  167: 196 */     int[] SA = this.SA;
/*  168: 197 */     byte[] T = this.T;
/*  169:     */     
/*  170: 199 */     int T_v1 = T[(td + SA[(pa + SA[v1])])] & 0xFF;
/*  171: 200 */     int T_v2 = T[(td + SA[(pa + SA[v2])])] & 0xFF;
/*  172: 201 */     int T_v3 = T[(td + SA[(pa + SA[v3])])] & 0xFF;
/*  173: 203 */     if (T_v1 > T_v2)
/*  174:     */     {
/*  175: 204 */       int temp = v1;
/*  176: 205 */       v1 = v2;
/*  177: 206 */       v2 = temp;
/*  178: 207 */       int T_vtemp = T_v1;
/*  179: 208 */       T_v1 = T_v2;
/*  180: 209 */       T_v2 = T_vtemp;
/*  181:     */     }
/*  182: 211 */     if (T_v2 > T_v3)
/*  183:     */     {
/*  184: 212 */       if (T_v1 > T_v3) {
/*  185: 213 */         return v1;
/*  186:     */       }
/*  187: 215 */       return v3;
/*  188:     */     }
/*  189: 217 */     return v2;
/*  190:     */   }
/*  191:     */   
/*  192:     */   private int ssMedian5(int td, int pa, int v1, int v2, int v3, int v4, int v5)
/*  193:     */   {
/*  194: 221 */     int[] SA = this.SA;
/*  195: 222 */     byte[] T = this.T;
/*  196:     */     
/*  197: 224 */     int T_v1 = T[(td + SA[(pa + SA[v1])])] & 0xFF;
/*  198: 225 */     int T_v2 = T[(td + SA[(pa + SA[v2])])] & 0xFF;
/*  199: 226 */     int T_v3 = T[(td + SA[(pa + SA[v3])])] & 0xFF;
/*  200: 227 */     int T_v4 = T[(td + SA[(pa + SA[v4])])] & 0xFF;
/*  201: 228 */     int T_v5 = T[(td + SA[(pa + SA[v5])])] & 0xFF;
/*  202: 232 */     if (T_v2 > T_v3)
/*  203:     */     {
/*  204: 233 */       int temp = v2;
/*  205: 234 */       v2 = v3;
/*  206: 235 */       v3 = temp;
/*  207: 236 */       int T_vtemp = T_v2;
/*  208: 237 */       T_v2 = T_v3;
/*  209: 238 */       T_v3 = T_vtemp;
/*  210:     */     }
/*  211: 240 */     if (T_v4 > T_v5)
/*  212:     */     {
/*  213: 241 */       int temp = v4;
/*  214: 242 */       v4 = v5;
/*  215: 243 */       v5 = temp;
/*  216: 244 */       int T_vtemp = T_v4;
/*  217: 245 */       T_v4 = T_v5;
/*  218: 246 */       T_v5 = T_vtemp;
/*  219:     */     }
/*  220: 248 */     if (T_v2 > T_v4)
/*  221:     */     {
/*  222: 249 */       int temp = v2;
/*  223: 250 */       v4 = temp;
/*  224: 251 */       int T_vtemp = T_v2;
/*  225: 252 */       T_v4 = T_vtemp;
/*  226: 253 */       temp = v3;
/*  227: 254 */       v3 = v5;
/*  228: 255 */       v5 = temp;
/*  229: 256 */       T_vtemp = T_v3;
/*  230: 257 */       T_v3 = T_v5;
/*  231: 258 */       T_v5 = T_vtemp;
/*  232:     */     }
/*  233: 260 */     if (T_v1 > T_v3)
/*  234:     */     {
/*  235: 261 */       int temp = v1;
/*  236: 262 */       v1 = v3;
/*  237: 263 */       v3 = temp;
/*  238: 264 */       int T_vtemp = T_v1;
/*  239: 265 */       T_v1 = T_v3;
/*  240: 266 */       T_v3 = T_vtemp;
/*  241:     */     }
/*  242: 268 */     if (T_v1 > T_v4)
/*  243:     */     {
/*  244: 269 */       int temp = v1;
/*  245: 270 */       v4 = temp;
/*  246: 271 */       int T_vtemp = T_v1;
/*  247: 272 */       T_v4 = T_vtemp;
/*  248: 273 */       v3 = v5;
/*  249: 274 */       T_v3 = T_v5;
/*  250:     */     }
/*  251: 276 */     if (T_v3 > T_v4) {
/*  252: 277 */       return v4;
/*  253:     */     }
/*  254: 279 */     return v3;
/*  255:     */   }
/*  256:     */   
/*  257:     */   private int ssPivot(int td, int pa, int first, int last)
/*  258:     */   {
/*  259: 286 */     int t = last - first;
/*  260: 287 */     int middle = first + t / 2;
/*  261: 289 */     if (t <= 512)
/*  262:     */     {
/*  263: 290 */       if (t <= 32) {
/*  264: 291 */         return ssMedian3(td, pa, first, middle, last - 1);
/*  265:     */       }
/*  266: 293 */       t >>= 2;
/*  267: 294 */       return ssMedian5(td, pa, first, first + t, middle, last - 1 - t, last - 1);
/*  268:     */     }
/*  269: 296 */     t >>= 3;
/*  270: 297 */     return ssMedian3(td, pa, 
/*  271:     */     
/*  272: 299 */       ssMedian3(td, pa, first, first + t, first + (t << 1)), 
/*  273: 300 */       ssMedian3(td, pa, middle - t, middle, middle + t), 
/*  274: 301 */       ssMedian3(td, pa, last - 1 - (t << 1), last - 1 - t, last - 1));
/*  275:     */   }
/*  276:     */   
/*  277:     */   private static int ssLog(int n)
/*  278:     */   {
/*  279: 306 */     return (n & 0xFF00) != 0 ? 8 + LOG_2_TABLE[(n >> 8 & 0xFF)] : LOG_2_TABLE[(n & 0xFF)];
/*  280:     */   }
/*  281:     */   
/*  282:     */   private int ssSubstringPartition(int pa, int first, int last, int depth)
/*  283:     */   {
/*  284: 312 */     int[] SA = this.SA;
/*  285:     */     
/*  286:     */ 
/*  287:     */ 
/*  288:     */ 
/*  289: 317 */     int a = first - 1;int b = last;
/*  290:     */     for (;;)
/*  291:     */     {
/*  292: 318 */       a++;
/*  293: 318 */       if ((a < b) && (SA[(pa + SA[a])] + depth >= SA[(pa + SA[a] + 1)] + 1))
/*  294:     */       {
/*  295: 319 */         SA[a] ^= 0xFFFFFFFF;
/*  296:     */       }
/*  297:     */       else
/*  298:     */       {
/*  299: 321 */         b--;
/*  300: 322 */         while ((a < b) && (SA[(pa + SA[b])] + depth < SA[(pa + SA[b] + 1)] + 1)) {
/*  301: 323 */           b--;
/*  302:     */         }
/*  303: 326 */         if (b <= a) {
/*  304:     */           break;
/*  305:     */         }
/*  306: 329 */         int t = SA[b] ^ 0xFFFFFFFF;
/*  307: 330 */         SA[b] = SA[a];
/*  308: 331 */         SA[a] = t;
/*  309:     */       }
/*  310:     */     }
/*  311: 333 */     if (first < a) {
/*  312: 334 */       SA[first] ^= 0xFFFFFFFF;
/*  313:     */     }
/*  314: 336 */     return a;
/*  315:     */   }
/*  316:     */   
/*  317:     */   private static class StackEntry
/*  318:     */   {
/*  319:     */     final int a;
/*  320:     */     final int b;
/*  321:     */     final int c;
/*  322:     */     final int d;
/*  323:     */     
/*  324:     */     StackEntry(int a, int b, int c, int d)
/*  325:     */     {
/*  326: 346 */       this.a = a;
/*  327: 347 */       this.b = b;
/*  328: 348 */       this.c = c;
/*  329: 349 */       this.d = d;
/*  330:     */     }
/*  331:     */   }
/*  332:     */   
/*  333:     */   private void ssMultiKeyIntroSort(int pa, int first, int last, int depth)
/*  334:     */   {
/*  335: 354 */     int[] SA = this.SA;
/*  336: 355 */     byte[] T = this.T;
/*  337:     */     
/*  338: 357 */     StackEntry[] stack = new StackEntry[64];
/*  339:     */     
/*  340:     */ 
/*  341:     */ 
/*  342:     */ 
/*  343:     */ 
/*  344:     */ 
/*  345: 364 */     int x = 0;
/*  346:     */     
/*  347: 366 */     int ssize = 0;int limit = ssLog(last - first);
/*  348:     */     for (;;)
/*  349:     */     {
/*  350: 367 */       if (last - first <= 8)
/*  351:     */       {
/*  352: 368 */         if (1 < last - first) {
/*  353: 369 */           ssInsertionSort(pa, first, last, depth);
/*  354:     */         }
/*  355: 371 */         if (ssize == 0) {
/*  356: 372 */           return;
/*  357:     */         }
/*  358: 374 */         StackEntry entry = stack[(--ssize)];
/*  359: 375 */         first = entry.a;
/*  360: 376 */         last = entry.b;
/*  361: 377 */         depth = entry.c;
/*  362: 378 */         limit = entry.d;
/*  363:     */       }
/*  364:     */       else
/*  365:     */       {
/*  366: 382 */         int Td = depth;
/*  367: 383 */         if (limit-- == 0) {
/*  368: 384 */           ssHeapSort(Td, pa, first, last - first);
/*  369:     */         }
/*  370: 386 */         if (limit < 0)
/*  371:     */         {
/*  372: 387 */           int a = first + 1;
/*  373: 387 */           for (int v = T[(Td + SA[(pa + SA[first])])] & 0xFF; a < last; a++) {
/*  374: 388 */             if ((x = T[(Td + SA[(pa + SA[a])])] & 0xFF) != v)
/*  375:     */             {
/*  376: 389 */               if (1 < a - first) {
/*  377:     */                 break;
/*  378:     */               }
/*  379: 392 */               v = x;
/*  380: 393 */               first = a;
/*  381:     */             }
/*  382:     */           }
/*  383: 396 */           if ((T[(Td + SA[(pa + SA[first])] - 1)] & 0xFF) < v) {
/*  384: 397 */             first = ssSubstringPartition(pa, first, a, depth);
/*  385:     */           }
/*  386: 399 */           if (a - first <= last - a)
/*  387:     */           {
/*  388: 400 */             if (1 < a - first)
/*  389:     */             {
/*  390: 401 */               stack[(ssize++)] = new StackEntry(a, last, depth, -1);
/*  391: 402 */               last = a;
/*  392: 403 */               depth++;
/*  393: 404 */               limit = ssLog(a - first);
/*  394:     */             }
/*  395:     */             else
/*  396:     */             {
/*  397: 406 */               first = a;
/*  398: 407 */               limit = -1;
/*  399:     */             }
/*  400:     */           }
/*  401: 410 */           else if (1 < last - a)
/*  402:     */           {
/*  403: 411 */             stack[(ssize++)] = new StackEntry(first, a, depth + 1, ssLog(a - first));
/*  404: 412 */             first = a;
/*  405: 413 */             limit = -1;
/*  406:     */           }
/*  407:     */           else
/*  408:     */           {
/*  409: 415 */             last = a;
/*  410: 416 */             depth++;
/*  411: 417 */             limit = ssLog(a - first);
/*  412:     */           }
/*  413:     */         }
/*  414:     */         else
/*  415:     */         {
/*  416: 423 */           int a = ssPivot(Td, pa, first, last);
/*  417: 424 */           int v = T[(Td + SA[(pa + SA[a])])] & 0xFF;
/*  418: 425 */           swapElements(SA, first, SA, a);
/*  419:     */           
/*  420: 427 */           int b = first + 1;
/*  421: 428 */           while ((b < last) && ((x = T[(Td + SA[(pa + SA[b])])] & 0xFF) == v)) {
/*  422: 429 */             b++;
/*  423:     */           }
/*  424: 431 */           if (((a = b) < last) && (x < v)) {
/*  425:     */             for (;;)
/*  426:     */             {
/*  427: 432 */               b++;
/*  428: 432 */               if ((b >= last) || ((x = T[(Td + SA[(pa + SA[b])])] & 0xFF) > v)) {
/*  429:     */                 break;
/*  430:     */               }
/*  431: 433 */               if (x == v)
/*  432:     */               {
/*  433: 434 */                 swapElements(SA, b, SA, a);
/*  434: 435 */                 a++;
/*  435:     */               }
/*  436:     */             }
/*  437:     */           }
/*  438: 440 */           int c = last - 1;
/*  439: 441 */           while ((b < c) && ((x = T[(Td + SA[(pa + SA[c])])] & 0xFF) == v)) {
/*  440: 442 */             c--;
/*  441:     */           }
/*  442:     */           int d;
/*  443: 444 */           if ((b < (d = c)) && (x > v)) {
/*  444: 445 */             while ((b < --c) && ((x = T[(Td + SA[(pa + SA[c])])] & 0xFF) >= v)) {
/*  445: 446 */               if (x == v)
/*  446:     */               {
/*  447: 447 */                 swapElements(SA, c, SA, d);
/*  448: 448 */                 d--;
/*  449:     */               }
/*  450:     */             }
/*  451:     */           }
/*  452: 452 */           for (; b < c; goto 752)
/*  453:     */           {
/*  454: 453 */             swapElements(SA, b, SA, c);
/*  455:     */             for (;;)
/*  456:     */             {
/*  457: 454 */               b++;
/*  458: 454 */               if ((b >= c) || ((x = T[(Td + SA[(pa + SA[b])])] & 0xFF) > v)) {
/*  459:     */                 break;
/*  460:     */               }
/*  461: 455 */               if (x == v)
/*  462:     */               {
/*  463: 456 */                 swapElements(SA, b, SA, a);
/*  464: 457 */                 a++;
/*  465:     */               }
/*  466:     */             }
/*  467: 460 */             while ((b < --c) && ((x = T[(Td + SA[(pa + SA[c])])] & 0xFF) >= v)) {
/*  468: 461 */               if (x == v)
/*  469:     */               {
/*  470: 462 */                 swapElements(SA, c, SA, d);
/*  471: 463 */                 d--;
/*  472:     */               }
/*  473:     */             }
/*  474:     */           }
/*  475: 468 */           if (a <= d)
/*  476:     */           {
/*  477: 469 */             c = b - 1;
/*  478:     */             int s;
/*  479:     */             int t;
/*  480: 471 */             if ((s = a - first) > (t = b - a)) {
/*  481: 472 */               s = t;
/*  482:     */             }
/*  483: 474 */             int e = first;
/*  484: 474 */             for (int f = b - s; 0 < s; f++)
/*  485:     */             {
/*  486: 475 */               swapElements(SA, e, SA, f);s--;e++;
/*  487:     */             }
/*  488: 477 */             if ((s = d - c) > (t = last - d - 1)) {
/*  489: 478 */               s = t;
/*  490:     */             }
/*  491: 480 */             e = b;
/*  492: 480 */             for (f = last - s; 0 < s; f++)
/*  493:     */             {
/*  494: 481 */               swapElements(SA, e, SA, f);s--;e++;
/*  495:     */             }
/*  496: 484 */             a = first + (b - a);
/*  497: 485 */             c = last - (d - c);
/*  498: 486 */             b = v <= (T[(Td + SA[(pa + SA[a])] - 1)] & 0xFF) ? a : ssSubstringPartition(pa, a, c, depth);
/*  499: 488 */             if (a - first <= last - c)
/*  500:     */             {
/*  501: 489 */               if (last - c <= c - b)
/*  502:     */               {
/*  503: 490 */                 stack[(ssize++)] = new StackEntry(b, c, depth + 1, ssLog(c - b));
/*  504: 491 */                 stack[(ssize++)] = new StackEntry(c, last, depth, limit);
/*  505: 492 */                 last = a;
/*  506:     */               }
/*  507: 493 */               else if (a - first <= c - b)
/*  508:     */               {
/*  509: 494 */                 stack[(ssize++)] = new StackEntry(c, last, depth, limit);
/*  510: 495 */                 stack[(ssize++)] = new StackEntry(b, c, depth + 1, ssLog(c - b));
/*  511: 496 */                 last = a;
/*  512:     */               }
/*  513:     */               else
/*  514:     */               {
/*  515: 498 */                 stack[(ssize++)] = new StackEntry(c, last, depth, limit);
/*  516: 499 */                 stack[(ssize++)] = new StackEntry(first, a, depth, limit);
/*  517: 500 */                 first = b;
/*  518: 501 */                 last = c;
/*  519: 502 */                 depth++;
/*  520: 503 */                 limit = ssLog(c - b);
/*  521:     */               }
/*  522:     */             }
/*  523: 506 */             else if (a - first <= c - b)
/*  524:     */             {
/*  525: 507 */               stack[(ssize++)] = new StackEntry(b, c, depth + 1, ssLog(c - b));
/*  526: 508 */               stack[(ssize++)] = new StackEntry(first, a, depth, limit);
/*  527: 509 */               first = c;
/*  528:     */             }
/*  529: 510 */             else if (last - c <= c - b)
/*  530:     */             {
/*  531: 511 */               stack[(ssize++)] = new StackEntry(first, a, depth, limit);
/*  532: 512 */               stack[(ssize++)] = new StackEntry(b, c, depth + 1, ssLog(c - b));
/*  533: 513 */               first = c;
/*  534:     */             }
/*  535:     */             else
/*  536:     */             {
/*  537: 515 */               stack[(ssize++)] = new StackEntry(first, a, depth, limit);
/*  538: 516 */               stack[(ssize++)] = new StackEntry(c, last, depth, limit);
/*  539: 517 */               first = b;
/*  540: 518 */               last = c;
/*  541: 519 */               depth++;
/*  542: 520 */               limit = ssLog(c - b);
/*  543:     */             }
/*  544:     */           }
/*  545:     */           else
/*  546:     */           {
/*  547: 524 */             limit++;
/*  548: 525 */             if ((T[(Td + SA[(pa + SA[first])] - 1)] & 0xFF) < v)
/*  549:     */             {
/*  550: 526 */               first = ssSubstringPartition(pa, first, last, depth);
/*  551: 527 */               limit = ssLog(last - first);
/*  552:     */             }
/*  553: 529 */             depth++;
/*  554:     */           }
/*  555:     */         }
/*  556:     */       }
/*  557:     */     }
/*  558:     */   }
/*  559:     */   
/*  560:     */   private static void ssBlockSwap(int[] array1, int first1, int[] array2, int first2, int size)
/*  561:     */   {
/*  562: 538 */     int i = size;int a = first1;
/*  563: 538 */     for (int b = first2; 0 < i; b++)
/*  564:     */     {
/*  565: 539 */       swapElements(array1, a, array2, b);i--;a++;
/*  566:     */     }
/*  567:     */   }
/*  568:     */   
/*  569:     */   private void ssMergeForward(int pa, int[] buf, int bufoffset, int first, int middle, int last, int depth)
/*  570:     */   {
/*  571: 545 */     int[] SA = this.SA;
/*  572:     */     
/*  573:     */ 
/*  574:     */ 
/*  575:     */ 
/*  576:     */ 
/*  577:     */ 
/*  578: 552 */     int bufend = bufoffset + (middle - first) - 1;
/*  579: 553 */     ssBlockSwap(buf, bufoffset, SA, first, middle - first);
/*  580:     */     
/*  581: 555 */     int t = SA[first];int i = first;int j = bufoffset;int k = middle;
/*  582:     */     for (;;)
/*  583:     */     {
/*  584: 556 */       int r = ssCompare(pa + buf[j], pa + SA[k], depth);
/*  585: 557 */       if (r < 0)
/*  586:     */       {
/*  587:     */         do
/*  588:     */         {
/*  589: 559 */           SA[(i++)] = buf[j];
/*  590: 560 */           if (bufend <= j)
/*  591:     */           {
/*  592: 561 */             buf[j] = t;
/*  593: 562 */             return;
/*  594:     */           }
/*  595: 564 */           buf[(j++)] = SA[i];
/*  596: 565 */         } while (buf[j] < 0);
/*  597:     */       }
/*  598: 566 */       else if (r > 0)
/*  599:     */       {
/*  600:     */         do
/*  601:     */         {
/*  602: 568 */           SA[(i++)] = SA[k];
/*  603: 569 */           SA[(k++)] = SA[i];
/*  604: 570 */           if (last <= k)
/*  605:     */           {
/*  606: 571 */             for (; j < bufend; buf[(j++)] = SA[i]) {
/*  607: 571 */               SA[(i++)] = buf[j];
/*  608:     */             }
/*  609: 572 */             SA[i] = buf[j];buf[j] = t;
/*  610: 573 */             return;
/*  611:     */           }
/*  612: 575 */         } while (SA[k] < 0);
/*  613:     */       }
/*  614:     */       else
/*  615:     */       {
/*  616: 577 */         SA[k] ^= 0xFFFFFFFF;
/*  617:     */         do
/*  618:     */         {
/*  619: 579 */           SA[(i++)] = buf[j];
/*  620: 580 */           if (bufend <= j)
/*  621:     */           {
/*  622: 581 */             buf[j] = t;
/*  623: 582 */             return;
/*  624:     */           }
/*  625: 584 */           buf[(j++)] = SA[i];
/*  626: 585 */         } while (buf[j] < 0);
/*  627:     */         do
/*  628:     */         {
/*  629: 588 */           SA[(i++)] = SA[k];
/*  630: 589 */           SA[(k++)] = SA[i];
/*  631: 590 */           if (last <= k)
/*  632:     */           {
/*  633: 591 */             while (j < bufend)
/*  634:     */             {
/*  635: 592 */               SA[(i++)] = buf[j];
/*  636: 593 */               buf[(j++)] = SA[i];
/*  637:     */             }
/*  638: 595 */             SA[i] = buf[j];buf[j] = t;
/*  639: 596 */             return;
/*  640:     */           }
/*  641: 598 */         } while (SA[k] < 0);
/*  642:     */       }
/*  643:     */     }
/*  644:     */   }
/*  645:     */   
/*  646:     */   private void ssMergeBackward(int pa, int[] buf, int bufoffset, int first, int middle, int last, int depth)
/*  647:     */   {
/*  648: 605 */     int[] SA = this.SA;
/*  649:     */     
/*  650:     */ 
/*  651:     */ 
/*  652:     */ 
/*  653:     */ 
/*  654:     */ 
/*  655:     */ 
/*  656:     */ 
/*  657: 614 */     int bufend = bufoffset + (last - middle);
/*  658: 615 */     ssBlockSwap(buf, bufoffset, SA, middle, last - middle);
/*  659:     */     
/*  660: 617 */     int x = 0;
/*  661:     */     int p1;
/*  662:     */     int p1;
/*  663: 618 */     if (buf[(bufend - 1)] < 0)
/*  664:     */     {
/*  665: 619 */       x |= 0x1;
/*  666: 620 */       p1 = pa + (buf[(bufend - 1)] ^ 0xFFFFFFFF);
/*  667:     */     }
/*  668:     */     else
/*  669:     */     {
/*  670: 622 */       p1 = pa + buf[(bufend - 1)];
/*  671:     */     }
/*  672:     */     int p2;
/*  673:     */     int p2;
/*  674: 624 */     if (SA[(middle - 1)] < 0)
/*  675:     */     {
/*  676: 625 */       x |= 0x2;
/*  677: 626 */       p2 = pa + (SA[(middle - 1)] ^ 0xFFFFFFFF);
/*  678:     */     }
/*  679:     */     else
/*  680:     */     {
/*  681: 628 */       p2 = pa + SA[(middle - 1)];
/*  682:     */     }
/*  683: 630 */     int t = SA[(last - 1)];int i = last - 1;int j = bufend - 1;int k = middle - 1;
/*  684:     */     for (;;)
/*  685:     */     {
/*  686: 632 */       int r = ssCompare(p1, p2, depth);
/*  687: 633 */       if (r > 0)
/*  688:     */       {
/*  689: 634 */         if ((x & 0x1) != 0)
/*  690:     */         {
/*  691:     */           do
/*  692:     */           {
/*  693: 636 */             SA[(i--)] = buf[j];
/*  694: 637 */             buf[(j--)] = SA[i];
/*  695: 638 */           } while (buf[j] < 0);
/*  696: 639 */           x ^= 0x1;
/*  697:     */         }
/*  698: 641 */         SA[(i--)] = buf[j];
/*  699: 642 */         if (j <= bufoffset)
/*  700:     */         {
/*  701: 643 */           buf[j] = t;
/*  702: 644 */           return;
/*  703:     */         }
/*  704: 646 */         buf[(j--)] = SA[i];
/*  705: 648 */         if (buf[j] < 0)
/*  706:     */         {
/*  707: 649 */           x |= 0x1;
/*  708: 650 */           p1 = pa + (buf[j] ^ 0xFFFFFFFF);
/*  709:     */         }
/*  710:     */         else
/*  711:     */         {
/*  712: 652 */           p1 = pa + buf[j];
/*  713:     */         }
/*  714:     */       }
/*  715: 654 */       else if (r < 0)
/*  716:     */       {
/*  717: 655 */         if ((x & 0x2) != 0)
/*  718:     */         {
/*  719:     */           do
/*  720:     */           {
/*  721: 657 */             SA[(i--)] = SA[k];
/*  722: 658 */             SA[(k--)] = SA[i];
/*  723: 659 */           } while (SA[k] < 0);
/*  724: 660 */           x ^= 0x2;
/*  725:     */         }
/*  726: 662 */         SA[(i--)] = SA[k];
/*  727: 663 */         SA[(k--)] = SA[i];
/*  728: 664 */         if (k < first)
/*  729:     */         {
/*  730: 665 */           while (bufoffset < j)
/*  731:     */           {
/*  732: 666 */             SA[(i--)] = buf[j];
/*  733: 667 */             buf[(j--)] = SA[i];
/*  734:     */           }
/*  735: 669 */           SA[i] = buf[j];
/*  736: 670 */           buf[j] = t;
/*  737: 671 */           return;
/*  738:     */         }
/*  739: 674 */         if (SA[k] < 0)
/*  740:     */         {
/*  741: 675 */           x |= 0x2;
/*  742: 676 */           p2 = pa + (SA[k] ^ 0xFFFFFFFF);
/*  743:     */         }
/*  744:     */         else
/*  745:     */         {
/*  746: 678 */           p2 = pa + SA[k];
/*  747:     */         }
/*  748:     */       }
/*  749:     */       else
/*  750:     */       {
/*  751: 681 */         if ((x & 0x1) != 0)
/*  752:     */         {
/*  753:     */           do
/*  754:     */           {
/*  755: 683 */             SA[(i--)] = buf[j];
/*  756: 684 */             buf[(j--)] = SA[i];
/*  757: 685 */           } while (buf[j] < 0);
/*  758: 686 */           x ^= 0x1;
/*  759:     */         }
/*  760: 688 */         SA[(i--)] = (buf[j] ^ 0xFFFFFFFF);
/*  761: 689 */         if (j <= bufoffset)
/*  762:     */         {
/*  763: 690 */           buf[j] = t;
/*  764: 691 */           return;
/*  765:     */         }
/*  766: 693 */         buf[(j--)] = SA[i];
/*  767: 695 */         if ((x & 0x2) != 0)
/*  768:     */         {
/*  769:     */           do
/*  770:     */           {
/*  771: 697 */             SA[(i--)] = SA[k];
/*  772: 698 */             SA[(k--)] = SA[i];
/*  773: 699 */           } while (SA[k] < 0);
/*  774: 700 */           x ^= 0x2;
/*  775:     */         }
/*  776: 702 */         SA[(i--)] = SA[k];
/*  777: 703 */         SA[(k--)] = SA[i];
/*  778: 704 */         if (k < first)
/*  779:     */         {
/*  780: 705 */           while (bufoffset < j)
/*  781:     */           {
/*  782: 706 */             SA[(i--)] = buf[j];
/*  783: 707 */             buf[(j--)] = SA[i];
/*  784:     */           }
/*  785: 709 */           SA[i] = buf[j];
/*  786: 710 */           buf[j] = t;
/*  787: 711 */           return;
/*  788:     */         }
/*  789: 714 */         if (buf[j] < 0)
/*  790:     */         {
/*  791: 715 */           x |= 0x1;
/*  792: 716 */           p1 = pa + (buf[j] ^ 0xFFFFFFFF);
/*  793:     */         }
/*  794:     */         else
/*  795:     */         {
/*  796: 718 */           p1 = pa + buf[j];
/*  797:     */         }
/*  798: 720 */         if (SA[k] < 0)
/*  799:     */         {
/*  800: 721 */           x |= 0x2;
/*  801: 722 */           p2 = pa + (SA[k] ^ 0xFFFFFFFF);
/*  802:     */         }
/*  803:     */         else
/*  804:     */         {
/*  805: 724 */           p2 = pa + SA[k];
/*  806:     */         }
/*  807:     */       }
/*  808:     */     }
/*  809:     */   }
/*  810:     */   
/*  811:     */   private static int getIDX(int a)
/*  812:     */   {
/*  813: 731 */     return 0 <= a ? a : a ^ 0xFFFFFFFF;
/*  814:     */   }
/*  815:     */   
/*  816:     */   private void ssMergeCheckEqual(int pa, int depth, int a)
/*  817:     */   {
/*  818: 735 */     int[] SA = this.SA;
/*  819: 737 */     if ((0 <= SA[a]) && (ssCompare(pa + getIDX(SA[(a - 1)]), pa + SA[a], depth) == 0)) {
/*  820: 738 */       SA[a] ^= 0xFFFFFFFF;
/*  821:     */     }
/*  822:     */   }
/*  823:     */   
/*  824:     */   private void ssMerge(int pa, int first, int middle, int last, int[] buf, int bufoffset, int bufsize, int depth)
/*  825:     */   {
/*  826: 744 */     int[] SA = this.SA;
/*  827:     */     
/*  828: 746 */     StackEntry[] stack = new StackEntry[64];
/*  829:     */     
/*  830:     */ 
/*  831:     */ 
/*  832:     */ 
/*  833:     */ 
/*  834:     */ 
/*  835: 753 */     int check = 0;int ssize = 0;
/*  836:     */     for (;;)
/*  837:     */     {
/*  838: 755 */       if (last - middle <= bufsize)
/*  839:     */       {
/*  840: 756 */         if ((first < middle) && (middle < last)) {
/*  841: 757 */           ssMergeBackward(pa, buf, bufoffset, first, middle, last, depth);
/*  842:     */         }
/*  843: 760 */         if ((check & 0x1) != 0) {
/*  844: 761 */           ssMergeCheckEqual(pa, depth, first);
/*  845:     */         }
/*  846: 763 */         if ((check & 0x2) != 0) {
/*  847: 764 */           ssMergeCheckEqual(pa, depth, last);
/*  848:     */         }
/*  849: 766 */         if (ssize == 0) {
/*  850: 767 */           return;
/*  851:     */         }
/*  852: 769 */         StackEntry entry = stack[(--ssize)];
/*  853: 770 */         first = entry.a;
/*  854: 771 */         middle = entry.b;
/*  855: 772 */         last = entry.c;
/*  856: 773 */         check = entry.d;
/*  857:     */       }
/*  858: 777 */       else if (middle - first <= bufsize)
/*  859:     */       {
/*  860: 778 */         if (first < middle) {
/*  861: 779 */           ssMergeForward(pa, buf, bufoffset, first, middle, last, depth);
/*  862:     */         }
/*  863: 781 */         if ((check & 0x1) != 0) {
/*  864: 782 */           ssMergeCheckEqual(pa, depth, first);
/*  865:     */         }
/*  866: 784 */         if ((check & 0x2) != 0) {
/*  867: 785 */           ssMergeCheckEqual(pa, depth, last);
/*  868:     */         }
/*  869: 787 */         if (ssize == 0) {
/*  870: 788 */           return;
/*  871:     */         }
/*  872: 790 */         StackEntry entry = stack[(--ssize)];
/*  873: 791 */         first = entry.a;
/*  874: 792 */         middle = entry.b;
/*  875: 793 */         last = entry.c;
/*  876: 794 */         check = entry.d;
/*  877:     */       }
/*  878:     */       else
/*  879:     */       {
/*  880: 798 */         int m = 0;int len = Math.min(middle - first, last - middle);int half = len >> 1;
/*  881: 799 */         for (; 0 < len; half >>= 1)
/*  882:     */         {
/*  883: 802 */           if (ssCompare(pa + getIDX(SA[(middle + m + half)]), pa + 
/*  884: 803 */             getIDX(SA[(middle - m - half - 1)]), depth) < 0)
/*  885:     */           {
/*  886: 804 */             m += half + 1;
/*  887: 805 */             half -= (len & 0x1 ^ 0x1);
/*  888:     */           }
/*  889: 800 */           len = half;
/*  890:     */         }
/*  891: 809 */         if (0 < m)
/*  892:     */         {
/*  893: 810 */           ssBlockSwap(SA, middle - m, SA, middle, m);
/*  894:     */           int j;
/*  895: 811 */           int i = j = middle;
/*  896: 812 */           int next = 0;
/*  897: 813 */           if (middle + m < last)
/*  898:     */           {
/*  899: 814 */             if (SA[(middle + m)] < 0)
/*  900:     */             {
/*  901: 815 */               while (SA[(i - 1)] < 0) {
/*  902: 816 */                 i--;
/*  903:     */               }
/*  904: 818 */               SA[(middle + m)] ^= 0xFFFFFFFF;
/*  905:     */             }
/*  906: 820 */             for (j = middle; SA[j] < 0;) {
/*  907: 821 */               j++;
/*  908:     */             }
/*  909: 823 */             next = 1;
/*  910:     */           }
/*  911: 825 */           if (i - first <= last - j)
/*  912:     */           {
/*  913: 826 */             stack[(ssize++)] = new StackEntry(j, middle + m, last, check & 0x2 | next & 0x1);
/*  914: 827 */             middle -= m;
/*  915: 828 */             last = i;
/*  916: 829 */             check &= 0x1;
/*  917:     */           }
/*  918:     */           else
/*  919:     */           {
/*  920: 831 */             if ((i == middle) && (middle == j)) {
/*  921: 832 */               next <<= 1;
/*  922:     */             }
/*  923: 834 */             stack[(ssize++)] = new StackEntry(first, middle - m, i, check & 0x1 | next & 0x2);
/*  924: 835 */             first = j;
/*  925: 836 */             middle += m;
/*  926: 837 */             check = check & 0x2 | next & 0x1;
/*  927:     */           }
/*  928:     */         }
/*  929:     */         else
/*  930:     */         {
/*  931: 840 */           if ((check & 0x1) != 0) {
/*  932: 841 */             ssMergeCheckEqual(pa, depth, first);
/*  933:     */           }
/*  934: 843 */           ssMergeCheckEqual(pa, depth, middle);
/*  935: 844 */           if ((check & 0x2) != 0) {
/*  936: 845 */             ssMergeCheckEqual(pa, depth, last);
/*  937:     */           }
/*  938: 847 */           if (ssize == 0) {
/*  939: 848 */             return;
/*  940:     */           }
/*  941: 850 */           StackEntry entry = stack[(--ssize)];
/*  942: 851 */           first = entry.a;
/*  943: 852 */           middle = entry.b;
/*  944: 853 */           last = entry.c;
/*  945: 854 */           check = entry.d;
/*  946:     */         }
/*  947:     */       }
/*  948:     */     }
/*  949:     */   }
/*  950:     */   
/*  951:     */   private void subStringSort(int pa, int first, int last, int[] buf, int bufoffset, int bufsize, int depth, boolean lastsuffix, int size)
/*  952:     */   {
/*  953: 862 */     int[] SA = this.SA;
/*  954: 870 */     if (lastsuffix) {
/*  955: 871 */       first++;
/*  956:     */     }
/*  957: 873 */     int a = first;
/*  958: 873 */     for (int i = 0; a + 1024 < last; i++)
/*  959:     */     {
/*  960: 874 */       ssMultiKeyIntroSort(pa, a, a + 1024, depth);
/*  961: 875 */       int[] curbuf = SA;
/*  962: 876 */       int curbufoffset = a + 1024;
/*  963: 877 */       int curbufsize = last - (a + 1024);
/*  964: 878 */       if (curbufsize <= bufsize)
/*  965:     */       {
/*  966: 879 */         curbufsize = bufsize;
/*  967: 880 */         curbuf = buf;
/*  968: 881 */         curbufoffset = bufoffset;
/*  969:     */       }
/*  970: 883 */       int b = a;int k = 1024;
/*  971: 883 */       for (int j = i; (j & 0x1) != 0; j >>>= 1)
/*  972:     */       {
/*  973: 884 */         ssMerge(pa, b - k, b, b + k, curbuf, curbufoffset, curbufsize, depth);b -= k;k <<= 1;
/*  974:     */       }
/*  975: 873 */       a += 1024;
/*  976:     */     }
/*  977: 888 */     ssMultiKeyIntroSort(pa, a, last, depth);
/*  978: 890 */     for (int k = 1024; i != 0; i >>= 1)
/*  979:     */     {
/*  980: 891 */       if ((i & 0x1) != 0)
/*  981:     */       {
/*  982: 892 */         ssMerge(pa, a - k, a, last, buf, bufoffset, bufsize, depth);
/*  983: 893 */         a -= k;
/*  984:     */       }
/*  985: 890 */       k <<= 1;
/*  986:     */     }
/*  987: 897 */     if (lastsuffix)
/*  988:     */     {
/*  989: 899 */       a = first;i = SA[(first - 1)];int r = 1;
/*  990: 900 */       for (; (a < last) && ((SA[a] < 0) || (0 < (r = ssCompareLast(pa, pa + i, pa + SA[a], depth, size)))); a++) {
/*  991: 902 */         SA[(a - 1)] = SA[a];
/*  992:     */       }
/*  993: 904 */       if (r == 0) {
/*  994: 905 */         SA[a] ^= 0xFFFFFFFF;
/*  995:     */       }
/*  996: 907 */       SA[(a - 1)] = i;
/*  997:     */     }
/*  998:     */   }
/*  999:     */   
/* 1000:     */   private int trGetC(int isa, int isaD, int isaN, int p)
/* 1001:     */   {
/* 1002: 914 */     return isaD + p < isaN ? this.SA[(isaD + p)] : this.SA[(isa + (isaD - isa + p) % (isaN - isa))];
/* 1003:     */   }
/* 1004:     */   
/* 1005:     */   private void trFixdown(int isa, int isaD, int isaN, int sa, int i, int size)
/* 1006:     */   {
/* 1007: 920 */     int[] SA = this.SA;
/* 1008:     */     
/* 1009:     */ 
/* 1010:     */ 
/* 1011:     */ 
/* 1012:     */ 
/* 1013: 926 */     int v = SA[(sa + i)];
/* 1014:     */     int j;
/* 1015:     */     int k;
/* 1016: 926 */     for (int c = trGetC(isa, isaD, isaN, v); (j = 2 * i + 1) < size; i = k)
/* 1017:     */     {
/* 1018: 927 */       k = j++;
/* 1019: 928 */       int d = trGetC(isa, isaD, isaN, SA[(sa + k)]);
/* 1020:     */       int e;
/* 1021: 929 */       if (d < (e = trGetC(isa, isaD, isaN, SA[(sa + j)])))
/* 1022:     */       {
/* 1023: 930 */         k = j;
/* 1024: 931 */         d = e;
/* 1025:     */       }
/* 1026: 933 */       if (d <= c) {
/* 1027:     */         break;
/* 1028:     */       }
/* 1029: 926 */       SA[(sa + i)] = SA[(sa + k)];
/* 1030:     */     }
/* 1031: 937 */     SA[(sa + i)] = v;
/* 1032:     */   }
/* 1033:     */   
/* 1034:     */   private void trHeapSort(int isa, int isaD, int isaN, int sa, int size)
/* 1035:     */   {
/* 1036: 941 */     int[] SA = this.SA;
/* 1037:     */     
/* 1038:     */ 
/* 1039:     */ 
/* 1040:     */ 
/* 1041: 946 */     int m = size;
/* 1042: 947 */     if (size % 2 == 0)
/* 1043:     */     {
/* 1044: 948 */       m--;
/* 1045: 949 */       if (trGetC(isa, isaD, isaN, SA[(sa + m / 2)]) < trGetC(isa, isaD, isaN, SA[(sa + m)])) {
/* 1046: 950 */         swapElements(SA, sa + m, SA, sa + m / 2);
/* 1047:     */       }
/* 1048:     */     }
/* 1049: 954 */     for (int i = m / 2 - 1; 0 <= i; i--) {
/* 1050: 955 */       trFixdown(isa, isaD, isaN, sa, i, m);
/* 1051:     */     }
/* 1052: 958 */     if (size % 2 == 0)
/* 1053:     */     {
/* 1054: 959 */       swapElements(SA, sa, SA, sa + m);
/* 1055: 960 */       trFixdown(isa, isaD, isaN, sa, 0, m);
/* 1056:     */     }
/* 1057: 963 */     for (i = m - 1; 0 < i; i--)
/* 1058:     */     {
/* 1059: 964 */       int t = SA[sa];
/* 1060: 965 */       SA[sa] = SA[(sa + i)];
/* 1061: 966 */       trFixdown(isa, isaD, isaN, sa, 0, i);
/* 1062: 967 */       SA[(sa + i)] = t;
/* 1063:     */     }
/* 1064:     */   }
/* 1065:     */   
/* 1066:     */   private void trInsertionSort(int isa, int isaD, int isaN, int first, int last)
/* 1067:     */   {
/* 1068: 972 */     int[] SA = this.SA;
/* 1069: 977 */     for (int a = first + 1; a < last; a++)
/* 1070:     */     {
/* 1071: 978 */       int t = SA[a];
/* 1072:     */       int r;
/* 1073: 978 */       for (int b = a - 1; 0 > (r = trGetC(isa, isaD, isaN, t) - trGetC(isa, isaD, isaN, SA[b]));)
/* 1074:     */       {
/* 1075:     */         do
/* 1076:     */         {
/* 1077: 980 */           SA[(b + 1)] = SA[b];
/* 1078: 981 */         } while ((first <= --b) && (SA[b] < 0));
/* 1079: 982 */         if (b < first) {
/* 1080:     */           break;
/* 1081:     */         }
/* 1082:     */       }
/* 1083: 986 */       if (r == 0) {
/* 1084: 987 */         SA[b] ^= 0xFFFFFFFF;
/* 1085:     */       }
/* 1086: 989 */       SA[(b + 1)] = t;
/* 1087:     */     }
/* 1088:     */   }
/* 1089:     */   
/* 1090:     */   private static int trLog(int n)
/* 1091:     */   {
/* 1092: 994 */     return (n & 0xFF00) != 0 ? 8 + LOG_2_TABLE[(n >> 8 & 0xFF)] : (n & 0xFFFF0000) != 0 ? LOG_2_TABLE[(n >> 16 & 0x10F)] : (n & 0xFF000000) != 0 ? 24 + LOG_2_TABLE[(n >> 24 & 0xFF)] : LOG_2_TABLE[(n & 0xFF)];
/* 1093:     */   }
/* 1094:     */   
/* 1095:     */   private int trMedian3(int isa, int isaD, int isaN, int v1, int v2, int v3)
/* 1096:     */   {
/* 1097:1000 */     int[] SA = this.SA;
/* 1098:     */     
/* 1099:1002 */     int SA_v1 = trGetC(isa, isaD, isaN, SA[v1]);
/* 1100:1003 */     int SA_v2 = trGetC(isa, isaD, isaN, SA[v2]);
/* 1101:1004 */     int SA_v3 = trGetC(isa, isaD, isaN, SA[v3]);
/* 1102:1006 */     if (SA_v1 > SA_v2)
/* 1103:     */     {
/* 1104:1007 */       int temp = v1;
/* 1105:1008 */       v1 = v2;
/* 1106:1009 */       v2 = temp;
/* 1107:1010 */       int SA_vtemp = SA_v1;
/* 1108:1011 */       SA_v1 = SA_v2;
/* 1109:1012 */       SA_v2 = SA_vtemp;
/* 1110:     */     }
/* 1111:1014 */     if (SA_v2 > SA_v3)
/* 1112:     */     {
/* 1113:1015 */       if (SA_v1 > SA_v3) {
/* 1114:1016 */         return v1;
/* 1115:     */       }
/* 1116:1018 */       return v3;
/* 1117:     */     }
/* 1118:1021 */     return v2;
/* 1119:     */   }
/* 1120:     */   
/* 1121:     */   private int trMedian5(int isa, int isaD, int isaN, int v1, int v2, int v3, int v4, int v5)
/* 1122:     */   {
/* 1123:1025 */     int[] SA = this.SA;
/* 1124:     */     
/* 1125:1027 */     int SA_v1 = trGetC(isa, isaD, isaN, SA[v1]);
/* 1126:1028 */     int SA_v2 = trGetC(isa, isaD, isaN, SA[v2]);
/* 1127:1029 */     int SA_v3 = trGetC(isa, isaD, isaN, SA[v3]);
/* 1128:1030 */     int SA_v4 = trGetC(isa, isaD, isaN, SA[v4]);
/* 1129:1031 */     int SA_v5 = trGetC(isa, isaD, isaN, SA[v5]);
/* 1130:1035 */     if (SA_v2 > SA_v3)
/* 1131:     */     {
/* 1132:1036 */       int temp = v2;
/* 1133:1037 */       v2 = v3;
/* 1134:1038 */       v3 = temp;
/* 1135:1039 */       int SA_vtemp = SA_v2;
/* 1136:1040 */       SA_v2 = SA_v3;
/* 1137:1041 */       SA_v3 = SA_vtemp;
/* 1138:     */     }
/* 1139:1043 */     if (SA_v4 > SA_v5)
/* 1140:     */     {
/* 1141:1044 */       int temp = v4;
/* 1142:1045 */       v4 = v5;
/* 1143:1046 */       v5 = temp;
/* 1144:1047 */       int SA_vtemp = SA_v4;
/* 1145:1048 */       SA_v4 = SA_v5;
/* 1146:1049 */       SA_v5 = SA_vtemp;
/* 1147:     */     }
/* 1148:1051 */     if (SA_v2 > SA_v4)
/* 1149:     */     {
/* 1150:1052 */       int temp = v2;
/* 1151:1053 */       v4 = temp;
/* 1152:1054 */       int SA_vtemp = SA_v2;
/* 1153:1055 */       SA_v4 = SA_vtemp;
/* 1154:1056 */       temp = v3;
/* 1155:1057 */       v3 = v5;
/* 1156:1058 */       v5 = temp;
/* 1157:1059 */       SA_vtemp = SA_v3;
/* 1158:1060 */       SA_v3 = SA_v5;
/* 1159:1061 */       SA_v5 = SA_vtemp;
/* 1160:     */     }
/* 1161:1063 */     if (SA_v1 > SA_v3)
/* 1162:     */     {
/* 1163:1064 */       int temp = v1;
/* 1164:1065 */       v1 = v3;
/* 1165:1066 */       v3 = temp;
/* 1166:1067 */       int SA_vtemp = SA_v1;
/* 1167:1068 */       SA_v1 = SA_v3;
/* 1168:1069 */       SA_v3 = SA_vtemp;
/* 1169:     */     }
/* 1170:1071 */     if (SA_v1 > SA_v4)
/* 1171:     */     {
/* 1172:1072 */       int temp = v1;
/* 1173:1073 */       v4 = temp;
/* 1174:1074 */       int SA_vtemp = SA_v1;
/* 1175:1075 */       SA_v4 = SA_vtemp;
/* 1176:1076 */       v3 = v5;
/* 1177:1077 */       SA_v3 = SA_v5;
/* 1178:     */     }
/* 1179:1079 */     if (SA_v3 > SA_v4) {
/* 1180:1080 */       return v4;
/* 1181:     */     }
/* 1182:1082 */     return v3;
/* 1183:     */   }
/* 1184:     */   
/* 1185:     */   private int trPivot(int isa, int isaD, int isaN, int first, int last)
/* 1186:     */   {
/* 1187:1089 */     int t = last - first;
/* 1188:1090 */     int middle = first + t / 2;
/* 1189:1092 */     if (t <= 512)
/* 1190:     */     {
/* 1191:1093 */       if (t <= 32) {
/* 1192:1094 */         return trMedian3(isa, isaD, isaN, first, middle, last - 1);
/* 1193:     */       }
/* 1194:1096 */       t >>= 2;
/* 1195:1097 */       return trMedian5(isa, isaD, isaN, first, first + t, middle, last - 1 - t, last - 1);
/* 1196:     */     }
/* 1197:1104 */     t >>= 3;
/* 1198:1105 */     return trMedian3(isa, isaD, isaN, 
/* 1199:     */     
/* 1200:1107 */       trMedian3(isa, isaD, isaN, first, first + t, first + (t << 1)), 
/* 1201:1108 */       trMedian3(isa, isaD, isaN, middle - t, middle, middle + t), 
/* 1202:1109 */       trMedian3(isa, isaD, isaN, last - 1 - (t << 1), last - 1 - t, last - 1));
/* 1203:     */   }
/* 1204:     */   
/* 1205:     */   private void lsUpdateGroup(int isa, int first, int last)
/* 1206:     */   {
/* 1207:1116 */     int[] SA = this.SA;
/* 1208:1121 */     for (int a = first; a < last; a++)
/* 1209:     */     {
/* 1210:1122 */       if (0 <= SA[a])
/* 1211:     */       {
/* 1212:1123 */         int b = a;
/* 1213:     */         do
/* 1214:     */         {
/* 1215:1125 */           SA[(isa + SA[a])] = a;
/* 1216:1126 */           a++;
/* 1217:1126 */         } while ((a < last) && (0 <= SA[a]));
/* 1218:1127 */         SA[b] = (b - a);
/* 1219:1128 */         if (last <= a) {
/* 1220:     */           break;
/* 1221:     */         }
/* 1222:     */       }
/* 1223:1132 */       int b = a;
/* 1224:     */       do
/* 1225:     */       {
/* 1226:1134 */         SA[a] ^= 0xFFFFFFFF;
/* 1227:1135 */       } while (SA[(++a)] < 0);
/* 1228:1136 */       int t = a;
/* 1229:     */       do
/* 1230:     */       {
/* 1231:1138 */         SA[(isa + SA[b])] = t;
/* 1232:1139 */         b++;
/* 1233:1139 */       } while (b <= a);
/* 1234:     */     }
/* 1235:     */   }
/* 1236:     */   
/* 1237:     */   private void lsIntroSort(int isa, int isaD, int isaN, int first, int last)
/* 1238:     */   {
/* 1239:1144 */     int[] SA = this.SA;
/* 1240:     */     
/* 1241:1146 */     StackEntry[] stack = new StackEntry[64];
/* 1242:     */     
/* 1243:     */ 
/* 1244:     */ 
/* 1245:     */ 
/* 1246:1151 */     int x = 0;
/* 1247:     */     
/* 1248:     */ 
/* 1249:1154 */     int ssize = 0;int limit = trLog(last - first);
/* 1250:     */     for (;;)
/* 1251:     */     {
/* 1252:1155 */       if (last - first <= 8)
/* 1253:     */       {
/* 1254:1156 */         if (1 < last - first)
/* 1255:     */         {
/* 1256:1157 */           trInsertionSort(isa, isaD, isaN, first, last);
/* 1257:1158 */           lsUpdateGroup(isa, first, last);
/* 1258:     */         }
/* 1259:1159 */         else if (last - first == 1)
/* 1260:     */         {
/* 1261:1160 */           SA[first] = -1;
/* 1262:     */         }
/* 1263:1162 */         if (ssize == 0) {
/* 1264:1163 */           return;
/* 1265:     */         }
/* 1266:1165 */         StackEntry entry = stack[(--ssize)];
/* 1267:1166 */         first = entry.a;
/* 1268:1167 */         last = entry.b;
/* 1269:1168 */         limit = entry.c;
/* 1270:     */       }
/* 1271:1172 */       else if (limit-- == 0)
/* 1272:     */       {
/* 1273:1173 */         trHeapSort(isa, isaD, isaN, first, last - first);
/* 1274:     */         int b;
/* 1275:1174 */         for (int a = last - 1; first < a; a = b)
/* 1276:     */         {
/* 1277:1175 */           x = trGetC(isa, isaD, isaN, SA[a]);
/* 1278:1175 */           for (b = a - 1; (first <= b) && (trGetC(isa, isaD, isaN, SA[b]) == x); b--) {
/* 1279:1178 */             SA[b] ^= 0xFFFFFFFF;
/* 1280:     */           }
/* 1281:     */         }
/* 1282:1181 */         lsUpdateGroup(isa, first, last);
/* 1283:1182 */         if (ssize == 0) {
/* 1284:1183 */           return;
/* 1285:     */         }
/* 1286:1185 */         StackEntry entry = stack[(--ssize)];
/* 1287:1186 */         first = entry.a;
/* 1288:1187 */         last = entry.b;
/* 1289:1188 */         limit = entry.c;
/* 1290:     */       }
/* 1291:     */       else
/* 1292:     */       {
/* 1293:1192 */         int a = trPivot(isa, isaD, isaN, first, last);
/* 1294:1193 */         swapElements(SA, first, SA, a);
/* 1295:1194 */         int v = trGetC(isa, isaD, isaN, SA[first]);
/* 1296:     */         
/* 1297:1196 */         int b = first + 1;
/* 1298:1197 */         while ((b < last) && ((x = trGetC(isa, isaD, isaN, SA[b])) == v)) {
/* 1299:1198 */           b++;
/* 1300:     */         }
/* 1301:1200 */         if (((a = b) < last) && (x < v)) {
/* 1302:     */           for (;;)
/* 1303:     */           {
/* 1304:1201 */             b++;
/* 1305:1201 */             if ((b >= last) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1306:     */               break;
/* 1307:     */             }
/* 1308:1202 */             if (x == v)
/* 1309:     */             {
/* 1310:1203 */               swapElements(SA, b, SA, a);
/* 1311:1204 */               a++;
/* 1312:     */             }
/* 1313:     */           }
/* 1314:     */         }
/* 1315:1209 */         int c = last - 1;
/* 1316:1210 */         while ((b < c) && ((x = trGetC(isa, isaD, isaN, SA[c])) == v)) {
/* 1317:1211 */           c--;
/* 1318:     */         }
/* 1319:     */         int d;
/* 1320:1213 */         if ((b < (d = c)) && (x > v)) {
/* 1321:1214 */           while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1322:1215 */             if (x == v)
/* 1323:     */             {
/* 1324:1216 */               swapElements(SA, c, SA, d);
/* 1325:1217 */               d--;
/* 1326:     */             }
/* 1327:     */           }
/* 1328:     */         }
/* 1329:1221 */         for (; b < c; goto 609)
/* 1330:     */         {
/* 1331:1222 */           swapElements(SA, b, SA, c);
/* 1332:     */           for (;;)
/* 1333:     */           {
/* 1334:1223 */             b++;
/* 1335:1223 */             if ((b >= c) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1336:     */               break;
/* 1337:     */             }
/* 1338:1224 */             if (x == v)
/* 1339:     */             {
/* 1340:1225 */               swapElements(SA, b, SA, a);
/* 1341:1226 */               a++;
/* 1342:     */             }
/* 1343:     */           }
/* 1344:1229 */           while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1345:1230 */             if (x == v)
/* 1346:     */             {
/* 1347:1231 */               swapElements(SA, c, SA, d);
/* 1348:1232 */               d--;
/* 1349:     */             }
/* 1350:     */           }
/* 1351:     */         }
/* 1352:1237 */         if (a <= d)
/* 1353:     */         {
/* 1354:1238 */           c = b - 1;
/* 1355:     */           int s;
/* 1356:     */           int t;
/* 1357:1240 */           if ((s = a - first) > (t = b - a)) {
/* 1358:1241 */             s = t;
/* 1359:     */           }
/* 1360:1243 */           int e = first;
/* 1361:1243 */           for (int f = b - s; 0 < s; f++)
/* 1362:     */           {
/* 1363:1244 */             swapElements(SA, e, SA, f);s--;e++;
/* 1364:     */           }
/* 1365:1246 */           if ((s = d - c) > (t = last - d - 1)) {
/* 1366:1247 */             s = t;
/* 1367:     */           }
/* 1368:1249 */           e = b;
/* 1369:1249 */           for (f = last - s; 0 < s; f++)
/* 1370:     */           {
/* 1371:1250 */             swapElements(SA, e, SA, f);s--;e++;
/* 1372:     */           }
/* 1373:1253 */           a = first + (b - a);
/* 1374:1254 */           b = last - (d - c);
/* 1375:     */           
/* 1376:1256 */           c = first;
/* 1377:1256 */           for (v = a - 1; c < a; c++) {
/* 1378:1257 */             SA[(isa + SA[c])] = v;
/* 1379:     */           }
/* 1380:1259 */           if (b < last)
/* 1381:     */           {
/* 1382:1260 */             c = a;
/* 1383:1260 */             for (v = b - 1; c < b; c++) {
/* 1384:1261 */               SA[(isa + SA[c])] = v;
/* 1385:     */             }
/* 1386:     */           }
/* 1387:1264 */           if (b - a == 1) {
/* 1388:1265 */             SA[a] = -1;
/* 1389:     */           }
/* 1390:1268 */           if (a - first <= last - b)
/* 1391:     */           {
/* 1392:1269 */             if (first < a)
/* 1393:     */             {
/* 1394:1270 */               stack[(ssize++)] = new StackEntry(b, last, limit, 0);
/* 1395:1271 */               last = a;
/* 1396:     */             }
/* 1397:     */             else
/* 1398:     */             {
/* 1399:1273 */               first = b;
/* 1400:     */             }
/* 1401:     */           }
/* 1402:1276 */           else if (b < last)
/* 1403:     */           {
/* 1404:1277 */             stack[(ssize++)] = new StackEntry(first, a, limit, 0);
/* 1405:1278 */             first = b;
/* 1406:     */           }
/* 1407:     */           else
/* 1408:     */           {
/* 1409:1280 */             last = a;
/* 1410:     */           }
/* 1411:     */         }
/* 1412:     */         else
/* 1413:     */         {
/* 1414:1284 */           if (ssize == 0) {
/* 1415:1285 */             return;
/* 1416:     */           }
/* 1417:1287 */           StackEntry entry = stack[(--ssize)];
/* 1418:1288 */           first = entry.a;
/* 1419:1289 */           last = entry.b;
/* 1420:1290 */           limit = entry.c;
/* 1421:     */         }
/* 1422:     */       }
/* 1423:     */     }
/* 1424:     */   }
/* 1425:     */   
/* 1426:     */   private void lsSort(int isa, int n, int depth)
/* 1427:     */   {
/* 1428:1296 */     int[] SA = this.SA;
/* 1429:1302 */     for (int isaD = isa + depth; -n < SA[0]; isaD += isaD - isa)
/* 1430:     */     {
/* 1431:1303 */       int first = 0;
/* 1432:1304 */       int skip = 0;
/* 1433:     */       int t;
/* 1434:     */       do
/* 1435:     */       {
/* 1436:1306 */         if ((t = SA[first]) < 0)
/* 1437:     */         {
/* 1438:1307 */           first -= t;
/* 1439:1308 */           skip += t;
/* 1440:     */         }
/* 1441:     */         else
/* 1442:     */         {
/* 1443:1310 */           if (skip != 0)
/* 1444:     */           {
/* 1445:1311 */             SA[(first + skip)] = skip;
/* 1446:1312 */             skip = 0;
/* 1447:     */           }
/* 1448:1314 */           int last = SA[(isa + t)] + 1;
/* 1449:1315 */           lsIntroSort(isa, isaD, isa + n, first, last);
/* 1450:1316 */           first = last;
/* 1451:     */         }
/* 1452:1318 */       } while (first < n);
/* 1453:1319 */       if (skip != 0) {
/* 1454:1320 */         SA[(first + skip)] = skip;
/* 1455:     */       }
/* 1456:1322 */       if (n < isaD - isa)
/* 1457:     */       {
/* 1458:1323 */         first = 0;
/* 1459:     */         do
/* 1460:     */         {
/* 1461:1325 */           if ((t = SA[first]) < 0)
/* 1462:     */           {
/* 1463:1326 */             first -= t;
/* 1464:     */           }
/* 1465:     */           else
/* 1466:     */           {
/* 1467:1328 */             int last = SA[(isa + t)] + 1;
/* 1468:1329 */             for (int i = first; i < last; i++) {
/* 1469:1330 */               SA[(isa + SA[i])] = i;
/* 1470:     */             }
/* 1471:1332 */             first = last;
/* 1472:     */           }
/* 1473:1334 */         } while (first < n);
/* 1474:1335 */         break;
/* 1475:     */       }
/* 1476:     */     }
/* 1477:     */   }
/* 1478:     */   
/* 1479:     */   private static class PartitionResult
/* 1480:     */   {
/* 1481:     */     final int first;
/* 1482:     */     final int last;
/* 1483:     */     
/* 1484:     */     PartitionResult(int first, int last)
/* 1485:     */     {
/* 1486:1347 */       this.first = first;
/* 1487:1348 */       this.last = last;
/* 1488:     */     }
/* 1489:     */   }
/* 1490:     */   
/* 1491:     */   private PartitionResult trPartition(int isa, int isaD, int isaN, int first, int last, int v)
/* 1492:     */   {
/* 1493:1354 */     int[] SA = this.SA;
/* 1494:     */     
/* 1495:     */ 
/* 1496:     */ 
/* 1497:1358 */     int x = 0;
/* 1498:     */     
/* 1499:1360 */     int b = first;
/* 1500:1361 */     while ((b < last) && ((x = trGetC(isa, isaD, isaN, SA[b])) == v)) {
/* 1501:1362 */       b++;
/* 1502:     */     }
/* 1503:     */     int a;
/* 1504:1364 */     if (((a = b) < last) && (x < v)) {
/* 1505:     */       for (;;)
/* 1506:     */       {
/* 1507:1365 */         b++;
/* 1508:1365 */         if ((b >= last) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1509:     */           break;
/* 1510:     */         }
/* 1511:1366 */         if (x == v)
/* 1512:     */         {
/* 1513:1367 */           swapElements(SA, b, SA, a);
/* 1514:1368 */           a++;
/* 1515:     */         }
/* 1516:     */       }
/* 1517:     */     }
/* 1518:1373 */     int c = last - 1;
/* 1519:1374 */     while ((b < c) && ((x = trGetC(isa, isaD, isaN, SA[c])) == v)) {
/* 1520:1375 */       c--;
/* 1521:     */     }
/* 1522:     */     int d;
/* 1523:1377 */     if ((b < (d = c)) && (x > v)) {
/* 1524:1378 */       while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1525:1379 */         if (x == v)
/* 1526:     */         {
/* 1527:1380 */           swapElements(SA, c, SA, d);
/* 1528:1381 */           d--;
/* 1529:     */         }
/* 1530:     */       }
/* 1531:     */     }
/* 1532:1385 */     for (; b < c; goto 299)
/* 1533:     */     {
/* 1534:1386 */       swapElements(SA, b, SA, c);
/* 1535:     */       for (;;)
/* 1536:     */       {
/* 1537:1387 */         b++;
/* 1538:1387 */         if ((b >= c) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1539:     */           break;
/* 1540:     */         }
/* 1541:1388 */         if (x == v)
/* 1542:     */         {
/* 1543:1389 */           swapElements(SA, b, SA, a);
/* 1544:1390 */           a++;
/* 1545:     */         }
/* 1546:     */       }
/* 1547:1393 */       while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1548:1394 */         if (x == v)
/* 1549:     */         {
/* 1550:1395 */           swapElements(SA, c, SA, d);
/* 1551:1396 */           d--;
/* 1552:     */         }
/* 1553:     */       }
/* 1554:     */     }
/* 1555:1401 */     if (a <= d)
/* 1556:     */     {
/* 1557:1402 */       c = b - 1;
/* 1558:     */       int s;
/* 1559:     */       int t;
/* 1560:1403 */       if ((s = a - first) > (t = b - a)) {
/* 1561:1404 */         s = t;
/* 1562:     */       }
/* 1563:1406 */       int e = first;
/* 1564:1406 */       for (int f = b - s; 0 < s; f++)
/* 1565:     */       {
/* 1566:1407 */         swapElements(SA, e, SA, f);s--;e++;
/* 1567:     */       }
/* 1568:1409 */       if ((s = d - c) > (t = last - d - 1)) {
/* 1569:1410 */         s = t;
/* 1570:     */       }
/* 1571:1412 */       e = b;
/* 1572:1412 */       for (f = last - s; 0 < s; f++)
/* 1573:     */       {
/* 1574:1413 */         swapElements(SA, e, SA, f);s--;e++;
/* 1575:     */       }
/* 1576:1415 */       first += b - a;
/* 1577:1416 */       last -= d - c;
/* 1578:     */     }
/* 1579:1418 */     return new PartitionResult(first, last);
/* 1580:     */   }
/* 1581:     */   
/* 1582:     */   private void trCopy(int isa, int isaN, int first, int a, int b, int last, int depth)
/* 1583:     */   {
/* 1584:1423 */     int[] SA = this.SA;
/* 1585:     */     
/* 1586:     */ 
/* 1587:     */ 
/* 1588:     */ 
/* 1589:1428 */     int v = b - 1;
/* 1590:     */     
/* 1591:1430 */     int c = first;
/* 1592:1430 */     for (int d = a - 1; c <= d; c++)
/* 1593:     */     {
/* 1594:     */       int s;
/* 1595:1431 */       if ((s = SA[c] - depth) < 0) {
/* 1596:1432 */         s += isaN - isa;
/* 1597:     */       }
/* 1598:1434 */       if (SA[(isa + s)] == v)
/* 1599:     */       {
/* 1600:1435 */         SA[(++d)] = s;
/* 1601:1436 */         SA[(isa + s)] = d;
/* 1602:     */       }
/* 1603:     */     }
/* 1604:1439 */     c = last - 1;int e = d + 1;
/* 1605:1439 */     for (d = b; e < d; c--)
/* 1606:     */     {
/* 1607:     */       int s;
/* 1608:1440 */       if ((s = SA[c] - depth) < 0) {
/* 1609:1441 */         s += isaN - isa;
/* 1610:     */       }
/* 1611:1443 */       if (SA[(isa + s)] == v)
/* 1612:     */       {
/* 1613:1444 */         SA[(--d)] = s;
/* 1614:1445 */         SA[(isa + s)] = d;
/* 1615:     */       }
/* 1616:     */     }
/* 1617:     */   }
/* 1618:     */   
/* 1619:     */   private void trIntroSort(int isa, int isaD, int isaN, int first, int last, TRBudget budget, int size)
/* 1620:     */   {
/* 1621:1452 */     int[] SA = this.SA;
/* 1622:     */     
/* 1623:1454 */     StackEntry[] stack = new StackEntry[64];
/* 1624:     */     
/* 1625:     */ 
/* 1626:     */ 
/* 1627:1458 */     int x = 0;
/* 1628:     */     
/* 1629:     */ 
/* 1630:     */ 
/* 1631:1462 */     int ssize = 0;int limit = trLog(last - first);
/* 1632:     */     for (;;)
/* 1633:     */     {
/* 1634:1463 */       if (limit < 0)
/* 1635:     */       {
/* 1636:1464 */         if (limit == -1)
/* 1637:     */         {
/* 1638:1465 */           if (!budget.update(size, last - first)) {
/* 1639:     */             break;
/* 1640:     */           }
/* 1641:1468 */           PartitionResult result = trPartition(isa, isaD - 1, isaN, first, last, last - 1);
/* 1642:1469 */           int a = result.first;
/* 1643:1470 */           int b = result.last;
/* 1644:1471 */           if ((first < a) || (b < last))
/* 1645:     */           {
/* 1646:1472 */             if (a < last)
/* 1647:     */             {
/* 1648:1473 */               int c = first;
/* 1649:1473 */               for (int v = a - 1; c < a; c++) {
/* 1650:1474 */                 SA[(isa + SA[c])] = v;
/* 1651:     */               }
/* 1652:     */             }
/* 1653:1477 */             if (b < last)
/* 1654:     */             {
/* 1655:1478 */               int c = a;
/* 1656:1478 */               for (int v = b - 1; c < b; c++) {
/* 1657:1479 */                 SA[(isa + SA[c])] = v;
/* 1658:     */               }
/* 1659:     */             }
/* 1660:1483 */             stack[(ssize++)] = new StackEntry(0, a, b, 0);
/* 1661:1484 */             stack[(ssize++)] = new StackEntry(isaD - 1, first, last, -2);
/* 1662:1485 */             if (a - first <= last - b)
/* 1663:     */             {
/* 1664:1486 */               if (1 < a - first)
/* 1665:     */               {
/* 1666:1487 */                 stack[(ssize++)] = new StackEntry(isaD, b, last, trLog(last - b));
/* 1667:1488 */                 last = a;limit = trLog(a - first);
/* 1668:     */               }
/* 1669:1489 */               else if (1 < last - b)
/* 1670:     */               {
/* 1671:1490 */                 first = b;limit = trLog(last - b);
/* 1672:     */               }
/* 1673:     */               else
/* 1674:     */               {
/* 1675:1492 */                 if (ssize == 0) {
/* 1676:1493 */                   return;
/* 1677:     */                 }
/* 1678:1495 */                 StackEntry entry = stack[(--ssize)];
/* 1679:1496 */                 isaD = entry.a;
/* 1680:1497 */                 first = entry.b;
/* 1681:1498 */                 last = entry.c;
/* 1682:1499 */                 limit = entry.d;
/* 1683:     */               }
/* 1684:     */             }
/* 1685:1502 */             else if (1 < last - b)
/* 1686:     */             {
/* 1687:1503 */               stack[(ssize++)] = new StackEntry(isaD, first, a, trLog(a - first));
/* 1688:1504 */               first = b;
/* 1689:1505 */               limit = trLog(last - b);
/* 1690:     */             }
/* 1691:1506 */             else if (1 < a - first)
/* 1692:     */             {
/* 1693:1507 */               last = a;
/* 1694:1508 */               limit = trLog(a - first);
/* 1695:     */             }
/* 1696:     */             else
/* 1697:     */             {
/* 1698:1510 */               if (ssize == 0) {
/* 1699:1511 */                 return;
/* 1700:     */               }
/* 1701:1513 */               StackEntry entry = stack[(--ssize)];
/* 1702:1514 */               isaD = entry.a;
/* 1703:1515 */               first = entry.b;
/* 1704:1516 */               last = entry.c;
/* 1705:1517 */               limit = entry.d;
/* 1706:     */             }
/* 1707:     */           }
/* 1708:     */           else
/* 1709:     */           {
/* 1710:1521 */             for (int c = first; c < last; c++) {
/* 1711:1522 */               SA[(isa + SA[c])] = c;
/* 1712:     */             }
/* 1713:1524 */             if (ssize == 0) {
/* 1714:1525 */               return;
/* 1715:     */             }
/* 1716:1527 */             StackEntry entry = stack[(--ssize)];
/* 1717:1528 */             isaD = entry.a;
/* 1718:1529 */             first = entry.b;
/* 1719:1530 */             last = entry.c;
/* 1720:1531 */             limit = entry.d;
/* 1721:     */           }
/* 1722:     */         }
/* 1723:1533 */         else if (limit == -2)
/* 1724:     */         {
/* 1725:1534 */           int a = stack[(--ssize)].b;
/* 1726:1535 */           int b = stack[ssize].c;
/* 1727:1536 */           trCopy(isa, isaN, first, a, b, last, isaD - isa);
/* 1728:1537 */           if (ssize == 0) {
/* 1729:1538 */             return;
/* 1730:     */           }
/* 1731:1540 */           StackEntry entry = stack[(--ssize)];
/* 1732:1541 */           isaD = entry.a;
/* 1733:1542 */           first = entry.b;
/* 1734:1543 */           last = entry.c;
/* 1735:1544 */           limit = entry.d;
/* 1736:     */         }
/* 1737:     */         else
/* 1738:     */         {
/* 1739:1546 */           if (0 <= SA[first])
/* 1740:     */           {
/* 1741:1547 */             int a = first;
/* 1742:     */             do
/* 1743:     */             {
/* 1744:1549 */               SA[(isa + SA[a])] = a;
/* 1745:1550 */               a++;
/* 1746:1550 */             } while ((a < last) && (0 <= SA[a]));
/* 1747:1551 */             first = a;
/* 1748:     */           }
/* 1749:1553 */           if (first < last)
/* 1750:     */           {
/* 1751:1554 */             int a = first;
/* 1752:     */             do
/* 1753:     */             {
/* 1754:1556 */               SA[a] ^= 0xFFFFFFFF;
/* 1755:1557 */             } while (SA[(++a)] < 0);
/* 1756:1558 */             int next = SA[(isa + SA[a])] != SA[(isaD + SA[a])] ? trLog(a - first + 1) : -1;
/* 1757:1559 */             a++;
/* 1758:1559 */             if (a < last)
/* 1759:     */             {
/* 1760:1560 */               int b = first;
/* 1761:1560 */               for (int v = a - 1; b < a; b++) {
/* 1762:1561 */                 SA[(isa + SA[b])] = v;
/* 1763:     */               }
/* 1764:     */             }
/* 1765:1565 */             if (a - first <= last - a)
/* 1766:     */             {
/* 1767:1566 */               stack[(ssize++)] = new StackEntry(isaD, a, last, -3);
/* 1768:1567 */               isaD++;last = a;limit = next;
/* 1769:     */             }
/* 1770:1569 */             else if (1 < last - a)
/* 1771:     */             {
/* 1772:1570 */               stack[(ssize++)] = new StackEntry(isaD + 1, first, a, next);
/* 1773:1571 */               first = a;limit = -3;
/* 1774:     */             }
/* 1775:     */             else
/* 1776:     */             {
/* 1777:1573 */               isaD++;last = a;limit = next;
/* 1778:     */             }
/* 1779:     */           }
/* 1780:     */           else
/* 1781:     */           {
/* 1782:1577 */             if (ssize == 0) {
/* 1783:1578 */               return;
/* 1784:     */             }
/* 1785:1580 */             StackEntry entry = stack[(--ssize)];
/* 1786:1581 */             isaD = entry.a;
/* 1787:1582 */             first = entry.b;
/* 1788:1583 */             last = entry.c;
/* 1789:1584 */             limit = entry.d;
/* 1790:     */           }
/* 1791:     */         }
/* 1792:     */       }
/* 1793:1590 */       else if (last - first <= 8)
/* 1794:     */       {
/* 1795:1591 */         if (!budget.update(size, last - first)) {
/* 1796:     */           break;
/* 1797:     */         }
/* 1798:1594 */         trInsertionSort(isa, isaD, isaN, first, last);
/* 1799:1595 */         limit = -3;
/* 1800:     */       }
/* 1801:1599 */       else if (limit-- == 0)
/* 1802:     */       {
/* 1803:1600 */         if (!budget.update(size, last - first)) {
/* 1804:     */           break;
/* 1805:     */         }
/* 1806:1603 */         trHeapSort(isa, isaD, isaN, first, last - first);
/* 1807:     */         int b;
/* 1808:1604 */         for (int a = last - 1; first < a; a = b)
/* 1809:     */         {
/* 1810:1605 */           x = trGetC(isa, isaD, isaN, SA[a]);
/* 1811:1605 */           for (b = a - 1; (first <= b) && (trGetC(isa, isaD, isaN, SA[b]) == x); b--) {
/* 1812:1608 */             SA[b] ^= 0xFFFFFFFF;
/* 1813:     */           }
/* 1814:     */         }
/* 1815:1611 */         limit = -3;
/* 1816:     */       }
/* 1817:     */       else
/* 1818:     */       {
/* 1819:1615 */         int a = trPivot(isa, isaD, isaN, first, last);
/* 1820:     */         
/* 1821:1617 */         swapElements(SA, first, SA, a);
/* 1822:1618 */         int v = trGetC(isa, isaD, isaN, SA[first]);
/* 1823:     */         
/* 1824:1620 */         int b = first + 1;
/* 1825:1621 */         while ((b < last) && ((x = trGetC(isa, isaD, isaN, SA[b])) == v)) {
/* 1826:1622 */           b++;
/* 1827:     */         }
/* 1828:1624 */         if (((a = b) < last) && (x < v)) {
/* 1829:     */           for (;;)
/* 1830:     */           {
/* 1831:1625 */             b++;
/* 1832:1625 */             if ((b >= last) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1833:     */               break;
/* 1834:     */             }
/* 1835:1626 */             if (x == v)
/* 1836:     */             {
/* 1837:1627 */               swapElements(SA, b, SA, a);
/* 1838:1628 */               a++;
/* 1839:     */             }
/* 1840:     */           }
/* 1841:     */         }
/* 1842:1633 */         int c = last - 1;
/* 1843:1634 */         while ((b < c) && ((x = trGetC(isa, isaD, isaN, SA[c])) == v)) {
/* 1844:1635 */           c--;
/* 1845:     */         }
/* 1846:     */         int d;
/* 1847:1637 */         if ((b < (d = c)) && (x > v)) {
/* 1848:1638 */           while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1849:1639 */             if (x == v)
/* 1850:     */             {
/* 1851:1640 */               swapElements(SA, c, SA, d);
/* 1852:1641 */               d--;
/* 1853:     */             }
/* 1854:     */           }
/* 1855:     */         }
/* 1856:1645 */         for (; b < c; goto 1491)
/* 1857:     */         {
/* 1858:1646 */           swapElements(SA, b, SA, c);
/* 1859:     */           for (;;)
/* 1860:     */           {
/* 1861:1647 */             b++;
/* 1862:1647 */             if ((b >= c) || ((x = trGetC(isa, isaD, isaN, SA[b])) > v)) {
/* 1863:     */               break;
/* 1864:     */             }
/* 1865:1648 */             if (x == v)
/* 1866:     */             {
/* 1867:1649 */               swapElements(SA, b, SA, a);
/* 1868:1650 */               a++;
/* 1869:     */             }
/* 1870:     */           }
/* 1871:1653 */           while ((b < --c) && ((x = trGetC(isa, isaD, isaN, SA[c])) >= v)) {
/* 1872:1654 */             if (x == v)
/* 1873:     */             {
/* 1874:1655 */               swapElements(SA, c, SA, d);
/* 1875:1656 */               d--;
/* 1876:     */             }
/* 1877:     */           }
/* 1878:     */         }
/* 1879:1661 */         if (a <= d)
/* 1880:     */         {
/* 1881:1662 */           c = b - 1;
/* 1882:     */           int s;
/* 1883:     */           int t;
/* 1884:1664 */           if ((s = a - first) > (t = b - a)) {
/* 1885:1665 */             s = t;
/* 1886:     */           }
/* 1887:1667 */           int e = first;
/* 1888:1667 */           for (int f = b - s; 0 < s; f++)
/* 1889:     */           {
/* 1890:1668 */             swapElements(SA, e, SA, f);s--;e++;
/* 1891:     */           }
/* 1892:1670 */           if ((s = d - c) > (t = last - d - 1)) {
/* 1893:1671 */             s = t;
/* 1894:     */           }
/* 1895:1673 */           e = b;
/* 1896:1673 */           for (f = last - s; 0 < s; f++)
/* 1897:     */           {
/* 1898:1674 */             swapElements(SA, e, SA, f);s--;e++;
/* 1899:     */           }
/* 1900:1677 */           a = first + (b - a);
/* 1901:1678 */           b = last - (d - c);
/* 1902:1679 */           int next = SA[(isa + SA[a])] != v ? trLog(b - a) : -1;
/* 1903:     */           
/* 1904:1681 */           c = first;
/* 1905:1681 */           for (v = a - 1; c < a; c++) {
/* 1906:1682 */             SA[(isa + SA[c])] = v;
/* 1907:     */           }
/* 1908:1684 */           if (b < last)
/* 1909:     */           {
/* 1910:1685 */             c = a;
/* 1911:1685 */             for (v = b - 1; c < b; c++) {
/* 1912:1686 */               SA[(isa + SA[c])] = v;
/* 1913:     */             }
/* 1914:     */           }
/* 1915:1689 */           if (a - first <= last - b)
/* 1916:     */           {
/* 1917:1690 */             if (last - b <= b - a)
/* 1918:     */             {
/* 1919:1691 */               if (1 < a - first)
/* 1920:     */               {
/* 1921:1692 */                 stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 1922:1693 */                 stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 1923:1694 */                 last = a;
/* 1924:     */               }
/* 1925:1695 */               else if (1 < last - b)
/* 1926:     */               {
/* 1927:1696 */                 stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 1928:1697 */                 first = b;
/* 1929:     */               }
/* 1930:1698 */               else if (1 < b - a)
/* 1931:     */               {
/* 1932:1699 */                 isaD++;
/* 1933:1700 */                 first = a;
/* 1934:1701 */                 last = b;
/* 1935:1702 */                 limit = next;
/* 1936:     */               }
/* 1937:     */               else
/* 1938:     */               {
/* 1939:1704 */                 if (ssize == 0) {
/* 1940:1705 */                   return;
/* 1941:     */                 }
/* 1942:1707 */                 StackEntry entry = stack[(--ssize)];
/* 1943:1708 */                 isaD = entry.a;
/* 1944:1709 */                 first = entry.b;
/* 1945:1710 */                 last = entry.c;
/* 1946:1711 */                 limit = entry.d;
/* 1947:     */               }
/* 1948:     */             }
/* 1949:1713 */             else if (a - first <= b - a)
/* 1950:     */             {
/* 1951:1714 */               if (1 < a - first)
/* 1952:     */               {
/* 1953:1715 */                 stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 1954:1716 */                 stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 1955:1717 */                 last = a;
/* 1956:     */               }
/* 1957:1718 */               else if (1 < b - a)
/* 1958:     */               {
/* 1959:1719 */                 stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 1960:1720 */                 isaD++;
/* 1961:1721 */                 first = a;
/* 1962:1722 */                 last = b;
/* 1963:1723 */                 limit = next;
/* 1964:     */               }
/* 1965:     */               else
/* 1966:     */               {
/* 1967:1725 */                 first = b;
/* 1968:     */               }
/* 1969:     */             }
/* 1970:1728 */             else if (1 < b - a)
/* 1971:     */             {
/* 1972:1729 */               stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 1973:1730 */               stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 1974:1731 */               isaD++;
/* 1975:1732 */               first = a;
/* 1976:1733 */               last = b;
/* 1977:1734 */               limit = next;
/* 1978:     */             }
/* 1979:     */             else
/* 1980:     */             {
/* 1981:1736 */               stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 1982:1737 */               last = a;
/* 1983:     */             }
/* 1984:     */           }
/* 1985:1741 */           else if (a - first <= b - a)
/* 1986:     */           {
/* 1987:1742 */             if (1 < last - b)
/* 1988:     */             {
/* 1989:1743 */               stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 1990:1744 */               stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 1991:1745 */               first = b;
/* 1992:     */             }
/* 1993:1746 */             else if (1 < a - first)
/* 1994:     */             {
/* 1995:1747 */               stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 1996:1748 */               last = a;
/* 1997:     */             }
/* 1998:1749 */             else if (1 < b - a)
/* 1999:     */             {
/* 2000:1750 */               isaD++;
/* 2001:1751 */               first = a;
/* 2002:1752 */               last = b;
/* 2003:1753 */               limit = next;
/* 2004:     */             }
/* 2005:     */             else
/* 2006:     */             {
/* 2007:1755 */               stack[(ssize++)] = new StackEntry(isaD, first, last, limit);
/* 2008:     */             }
/* 2009:     */           }
/* 2010:1757 */           else if (last - b <= b - a)
/* 2011:     */           {
/* 2012:1758 */             if (1 < last - b)
/* 2013:     */             {
/* 2014:1759 */               stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 2015:1760 */               stack[(ssize++)] = new StackEntry(isaD + 1, a, b, next);
/* 2016:1761 */               first = b;
/* 2017:     */             }
/* 2018:1762 */             else if (1 < b - a)
/* 2019:     */             {
/* 2020:1763 */               stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 2021:1764 */               isaD++;
/* 2022:1765 */               first = a;
/* 2023:1766 */               last = b;
/* 2024:1767 */               limit = next;
/* 2025:     */             }
/* 2026:     */             else
/* 2027:     */             {
/* 2028:1769 */               last = a;
/* 2029:     */             }
/* 2030:     */           }
/* 2031:1772 */           else if (1 < b - a)
/* 2032:     */           {
/* 2033:1773 */             stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 2034:1774 */             stack[(ssize++)] = new StackEntry(isaD, b, last, limit);
/* 2035:1775 */             isaD++;
/* 2036:1776 */             first = a;
/* 2037:1777 */             last = b;
/* 2038:1778 */             limit = next;
/* 2039:     */           }
/* 2040:     */           else
/* 2041:     */           {
/* 2042:1780 */             stack[(ssize++)] = new StackEntry(isaD, first, a, limit);
/* 2043:1781 */             first = b;
/* 2044:     */           }
/* 2045:     */         }
/* 2046:     */         else
/* 2047:     */         {
/* 2048:1786 */           if (!budget.update(size, last - first)) {
/* 2049:     */             break;
/* 2050:     */           }
/* 2051:1789 */           limit++;isaD++;
/* 2052:     */         }
/* 2053:     */       }
/* 2054:     */     }
/* 2055:1793 */     for (int s = 0; s < ssize; s++) {
/* 2056:1794 */       if (stack[s].d == -3) {
/* 2057:1795 */         lsUpdateGroup(isa, stack[s].b, stack[s].c);
/* 2058:     */       }
/* 2059:     */     }
/* 2060:     */   }
/* 2061:     */   
/* 2062:     */   private static class TRBudget
/* 2063:     */   {
/* 2064:     */     int budget;
/* 2065:     */     int chance;
/* 2066:     */     
/* 2067:     */     TRBudget(int budget, int chance)
/* 2068:     */     {
/* 2069:1805 */       this.budget = budget;
/* 2070:1806 */       this.chance = chance;
/* 2071:     */     }
/* 2072:     */     
/* 2073:     */     boolean update(int size, int n)
/* 2074:     */     {
/* 2075:1810 */       this.budget -= n;
/* 2076:1811 */       if (this.budget <= 0)
/* 2077:     */       {
/* 2078:1812 */         if (--this.chance == 0) {
/* 2079:1813 */           return false;
/* 2080:     */         }
/* 2081:1815 */         this.budget += size;
/* 2082:     */       }
/* 2083:1817 */       return true;
/* 2084:     */     }
/* 2085:     */   }
/* 2086:     */   
/* 2087:     */   private void trSort(int isa, int n, int depth)
/* 2088:     */   {
/* 2089:1822 */     int[] SA = this.SA;
/* 2090:     */     
/* 2091:1824 */     int first = 0;
/* 2092:1827 */     if (-n < SA[0])
/* 2093:     */     {
/* 2094:1828 */       TRBudget budget = new TRBudget(n, trLog(n) * 2 / 3 + 1);
/* 2095:     */       do
/* 2096:     */       {
/* 2097:     */         int t;
/* 2098:1830 */         if ((t = SA[first]) < 0)
/* 2099:     */         {
/* 2100:1831 */           first -= t;
/* 2101:     */         }
/* 2102:     */         else
/* 2103:     */         {
/* 2104:1833 */           int last = SA[(isa + t)] + 1;
/* 2105:1834 */           if (1 < last - first)
/* 2106:     */           {
/* 2107:1835 */             trIntroSort(isa, isa + depth, isa + n, first, last, budget, n);
/* 2108:1836 */             if (budget.chance == 0)
/* 2109:     */             {
/* 2110:1838 */               if (0 < first) {
/* 2111:1839 */                 SA[0] = (-first);
/* 2112:     */               }
/* 2113:1841 */               lsSort(isa, n, depth);
/* 2114:1842 */               break;
/* 2115:     */             }
/* 2116:     */           }
/* 2117:1845 */           first = last;
/* 2118:     */         }
/* 2119:1847 */       } while (first < n);
/* 2120:     */     }
/* 2121:     */   }
/* 2122:     */   
/* 2123:     */   private static int BUCKET_B(int c0, int c1)
/* 2124:     */   {
/* 2125:1854 */     return c1 << 8 | c0;
/* 2126:     */   }
/* 2127:     */   
/* 2128:     */   private static int BUCKET_BSTAR(int c0, int c1)
/* 2129:     */   {
/* 2130:1858 */     return c0 << 8 | c1;
/* 2131:     */   }
/* 2132:     */   
/* 2133:     */   private int sortTypeBstar(int[] bucketA, int[] bucketB)
/* 2134:     */   {
/* 2135:1862 */     byte[] T = this.T;
/* 2136:1863 */     int[] SA = this.SA;
/* 2137:1864 */     int n = this.n;
/* 2138:1865 */     int[] tempbuf = new int[256];
/* 2139:     */     
/* 2140:     */ 
/* 2141:     */ 
/* 2142:     */ 
/* 2143:     */ 
/* 2144:     */ 
/* 2145:     */ 
/* 2146:1873 */     int i = 1;
/* 2147:1873 */     for (int flag = 1; i < n; i++) {
/* 2148:1874 */       if (T[(i - 1)] != T[i])
/* 2149:     */       {
/* 2150:1875 */         if ((T[(i - 1)] & 0xFF) <= (T[i] & 0xFF)) {
/* 2151:     */           break;
/* 2152:     */         }
/* 2153:1876 */         flag = 0; break;
/* 2154:     */       }
/* 2155:     */     }
/* 2156:1881 */     i = n - 1;
/* 2157:1882 */     int m = n;
/* 2158:     */     int ti;
/* 2159:     */     int t0;
/* 2160:1885 */     if (((ti = T[i] & 0xFF) < (t0 = T[0] & 0xFF)) || ((T[i] == T[0]) && (flag != 0)))
/* 2161:     */     {
/* 2162:1886 */       if (flag == 0)
/* 2163:     */       {
/* 2164:1887 */         bucketB[BUCKET_BSTAR(ti, t0)] += 1;
/* 2165:1888 */         SA[(--m)] = i;
/* 2166:     */       }
/* 2167:     */       else
/* 2168:     */       {
/* 2169:1890 */         bucketB[BUCKET_B(ti, t0)] += 1;
/* 2170:     */       }
/* 2171:     */       int ti1;
/* 2172:1892 */       for (i--; (0 <= i) && ((ti = T[i] & 0xFF) <= (ti1 = T[(i + 1)] & 0xFF)); i--) {
/* 2173:1893 */         bucketB[BUCKET_B(ti, ti1)] += 1;
/* 2174:     */       }
/* 2175:     */     }
/* 2176:1897 */     while (0 <= i)
/* 2177:     */     {
/* 2178:     */       do
/* 2179:     */       {
/* 2180:1899 */         bucketA[(T[i] & 0xFF)] += 1;
/* 2181:1900 */       } while ((0 <= --i) && ((T[i] & 0xFF) >= (T[(i + 1)] & 0xFF)));
/* 2182:1901 */       if (0 <= i)
/* 2183:     */       {
/* 2184:1902 */         bucketB[BUCKET_BSTAR(T[i] & 0xFF, T[(i + 1)] & 0xFF)] += 1;
/* 2185:1903 */         SA[(--m)] = i;
/* 2186:     */         int ti1;
/* 2187:1904 */         for (i--; (0 <= i) && ((ti = T[i] & 0xFF) <= (ti1 = T[(i + 1)] & 0xFF)); i--) {
/* 2188:1905 */           bucketB[BUCKET_B(ti, ti1)] += 1;
/* 2189:     */         }
/* 2190:     */       }
/* 2191:     */     }
/* 2192:1909 */     m = n - m;
/* 2193:1910 */     if (m == 0)
/* 2194:     */     {
/* 2195:1911 */       for (i = 0; i < n; i++) {
/* 2196:1912 */         SA[i] = i;
/* 2197:     */       }
/* 2198:1914 */       return 0;
/* 2199:     */     }
/* 2200:1917 */     int c0 = 0;i = -1;
/* 2201:1917 */     for (int j = 0; c0 < 256; c0++)
/* 2202:     */     {
/* 2203:1918 */       int t = i + bucketA[c0];
/* 2204:1919 */       bucketA[c0] = (i + j);
/* 2205:1920 */       i = t + bucketB[BUCKET_B(c0, c0)];
/* 2206:1921 */       for (int c1 = c0 + 1; c1 < 256; c1++)
/* 2207:     */       {
/* 2208:1922 */         j += bucketB[BUCKET_BSTAR(c0, c1)];
/* 2209:1923 */         bucketB[(c0 << 8 | c1)] = j;
/* 2210:1924 */         i += bucketB[BUCKET_B(c0, c1)];
/* 2211:     */       }
/* 2212:     */     }
/* 2213:1928 */     int PAb = n - m;
/* 2214:1929 */     int ISAb = m;
/* 2215:1930 */     for (i = m - 2; 0 <= i; i--)
/* 2216:     */     {
/* 2217:1931 */       int t = SA[(PAb + i)];
/* 2218:1932 */       c0 = T[t] & 0xFF;
/* 2219:1933 */       int c1 = T[(t + 1)] & 0xFF; int 
/* 2220:1934 */         tmp596_593 = BUCKET_BSTAR(c0, c1); int[] tmp596_588 = bucketB; int tmp600_599 = (tmp596_588[tmp596_593] - 1);tmp596_588[tmp596_593] = tmp600_599;SA[tmp600_599] = i;
/* 2221:     */     }
/* 2222:1936 */     int t = SA[(PAb + m - 1)];
/* 2223:1937 */     c0 = T[t] & 0xFF;
/* 2224:1938 */     int c1 = T[(t + 1)] & 0xFF; int 
/* 2225:1939 */       tmp655_652 = BUCKET_BSTAR(c0, c1); int[] tmp655_647 = bucketB; int tmp659_658 = (tmp655_647[tmp655_652] - 1);tmp655_647[tmp655_652] = tmp659_658;SA[tmp659_658] = (m - 1);
/* 2226:     */     
/* 2227:1941 */     int[] buf = SA;
/* 2228:1942 */     int bufoffset = m;
/* 2229:1943 */     int bufsize = n - 2 * m;
/* 2230:1944 */     if (bufsize <= 256)
/* 2231:     */     {
/* 2232:1945 */       buf = tempbuf;
/* 2233:1946 */       bufoffset = 0;
/* 2234:1947 */       bufsize = 256;
/* 2235:     */     }
/* 2236:1950 */     c0 = 255;
/* 2237:1950 */     for (j = m; 0 < j; c0--) {
/* 2238:1951 */       for (c1 = 255; c0 < c1; c1--)
/* 2239:     */       {
/* 2240:1952 */         i = bucketB[BUCKET_BSTAR(c0, c1)];
/* 2241:1953 */         if (1 < j - i) {
/* 2242:1954 */           subStringSort(PAb, i, j, buf, bufoffset, bufsize, 2, SA[i] == m - 1, n);
/* 2243:     */         }
/* 2244:1951 */         j = i;
/* 2245:     */       }
/* 2246:     */     }
/* 2247:1959 */     for (i = m - 1; 0 <= i; i--)
/* 2248:     */     {
/* 2249:1960 */       if (0 <= SA[i])
/* 2250:     */       {
/* 2251:1961 */         j = i;
/* 2252:     */         do
/* 2253:     */         {
/* 2254:1963 */           SA[(ISAb + SA[i])] = i;
/* 2255:1964 */         } while ((0 <= --i) && (0 <= SA[i]));
/* 2256:1965 */         SA[(i + 1)] = (i - j);
/* 2257:1966 */         if (i <= 0) {
/* 2258:     */           break;
/* 2259:     */         }
/* 2260:     */       }
/* 2261:1970 */       j = i;
/* 2262:     */       do
/* 2263:     */       {
/* 2264:1972 */         int tmp897_896 = (SA[i] ^ 0xFFFFFFFF);SA[i] = tmp897_896;SA[(ISAb + tmp897_896)] = j;
/* 2265:1973 */       } while (SA[(--i)] < 0);
/* 2266:1974 */       SA[(ISAb + SA[i])] = j;
/* 2267:     */     }
/* 2268:1977 */     trSort(ISAb, m, 1);
/* 2269:     */     
/* 2270:1979 */     i = n - 1;j = m;
/* 2271:1980 */     if (((T[i] & 0xFF) < (T[0] & 0xFF)) || ((T[i] == T[0]) && (flag != 0)))
/* 2272:     */     {
/* 2273:1981 */       if (flag == 0) {
/* 2274:1982 */         SA[SA[(ISAb + --j)]] = i;
/* 2275:     */       }
/* 2276:1984 */       for (i--; (0 <= i) && ((T[i] & 0xFF) <= (T[(i + 1)] & 0xFF));) {
/* 2277:1985 */         i--;
/* 2278:     */       }
/* 2279:     */     }
/* 2280:1988 */     while (0 <= i)
/* 2281:     */     {
/* 2282:1989 */       for (i--; (0 <= i) && ((T[i] & 0xFF) >= (T[(i + 1)] & 0xFF));) {
/* 2283:1990 */         i--;
/* 2284:     */       }
/* 2285:1992 */       if (0 <= i)
/* 2286:     */       {
/* 2287:1993 */         SA[SA[(ISAb + --j)]] = i;
/* 2288:1994 */         for (i--; (0 <= i) && ((T[i] & 0xFF) <= (T[(i + 1)] & 0xFF));) {
/* 2289:1995 */           i--;
/* 2290:     */         }
/* 2291:     */       }
/* 2292:     */     }
/* 2293:2000 */     c0 = 255;i = n - 1;
/* 2294:2000 */     for (int k = m - 1; 0 <= c0; c0--)
/* 2295:     */     {
/* 2296:2001 */       for (c1 = 255; c0 < c1; c1--)
/* 2297:     */       {
/* 2298:2002 */         t = i - bucketB[BUCKET_B(c0, c1)];
/* 2299:2003 */         bucketB[BUCKET_B(c0, c1)] = (i + 1);
/* 2300:     */         
/* 2301:2005 */         i = t;
/* 2302:2005 */         for (j = bucketB[BUCKET_BSTAR(c0, c1)]; j <= k; k--)
/* 2303:     */         {
/* 2304:2006 */           SA[i] = SA[k];i--;
/* 2305:     */         }
/* 2306:     */       }
/* 2307:2009 */       t = i - bucketB[BUCKET_B(c0, c0)];
/* 2308:2010 */       bucketB[BUCKET_B(c0, c0)] = (i + 1);
/* 2309:2011 */       if (c0 < 255) {
/* 2310:2012 */         bucketB[BUCKET_BSTAR(c0, c0 + 1)] = (t + 1);
/* 2311:     */       }
/* 2312:2014 */       i = bucketA[c0];
/* 2313:     */     }
/* 2314:2016 */     return m;
/* 2315:     */   }
/* 2316:     */   
/* 2317:     */   private int constructBWT(int[] bucketA, int[] bucketB)
/* 2318:     */   {
/* 2319:2020 */     byte[] T = this.T;
/* 2320:2021 */     int[] SA = this.SA;
/* 2321:2022 */     int n = this.n;
/* 2322:     */     
/* 2323:2024 */     int t = 0;
/* 2324:     */     
/* 2325:2026 */     int c2 = 0;
/* 2326:2027 */     int orig = -1;
/* 2327:2029 */     for (int c1 = 254; 0 <= c1; c1--)
/* 2328:     */     {
/* 2329:2030 */       int i = bucketB[BUCKET_BSTAR(c1, c1 + 1)];int j = bucketA[(c1 + 1)];t = 0;c2 = -1;
/* 2330:2031 */       for (; i <= j; j--)
/* 2331:     */       {
/* 2332:     */         int s;
/* 2333:     */         int s1;
/* 2334:2033 */         if (0 <= (s1 = s = SA[j]))
/* 2335:     */         {
/* 2336:2034 */           s--;
/* 2337:2034 */           if (s < 0) {
/* 2338:2035 */             s = n - 1;
/* 2339:     */           }
/* 2340:     */           int c0;
/* 2341:2037 */           if ((c0 = T[s] & 0xFF) <= c1)
/* 2342:     */           {
/* 2343:2038 */             SA[j] = (s1 ^ 0xFFFFFFFF);
/* 2344:2039 */             if ((0 < s) && ((T[(s - 1)] & 0xFF) > c0)) {
/* 2345:2040 */               s ^= 0xFFFFFFFF;
/* 2346:     */             }
/* 2347:2042 */             if (c2 == c0)
/* 2348:     */             {
/* 2349:2043 */               SA[(--t)] = s;
/* 2350:     */             }
/* 2351:     */             else
/* 2352:     */             {
/* 2353:2045 */               if (0 <= c2) {
/* 2354:2046 */                 bucketB[BUCKET_B(c2, c1)] = t;
/* 2355:     */               }
/* 2356:2048 */               int tmp205_204 = (bucketB[BUCKET_B(c2 = c0, c1)] - 1);t = tmp205_204;SA[tmp205_204] = s;
/* 2357:     */             }
/* 2358:     */           }
/* 2359:     */         }
/* 2360:     */         else
/* 2361:     */         {
/* 2362:2052 */           SA[j] = (s ^ 0xFFFFFFFF);
/* 2363:     */         }
/* 2364:     */       }
/* 2365:     */     }
/* 2366:2057 */     for (int i = 0; i < n; i++)
/* 2367:     */     {
/* 2368:     */       int s;
/* 2369:     */       int s1;
/* 2370:2058 */       if (0 <= (s1 = s = SA[i]))
/* 2371:     */       {
/* 2372:2059 */         s--;
/* 2373:2059 */         if (s < 0) {
/* 2374:2060 */           s = n - 1;
/* 2375:     */         }
/* 2376:     */         int c0;
/* 2377:2062 */         if ((c0 = T[s] & 0xFF) >= (T[(s + 1)] & 0xFF))
/* 2378:     */         {
/* 2379:2063 */           if ((0 < s) && ((T[(s - 1)] & 0xFF) < c0)) {
/* 2380:2064 */             s ^= 0xFFFFFFFF;
/* 2381:     */           }
/* 2382:2066 */           if (c0 == c2)
/* 2383:     */           {
/* 2384:2067 */             SA[(++t)] = s;
/* 2385:     */           }
/* 2386:     */           else
/* 2387:     */           {
/* 2388:2069 */             if (c2 != -1) {
/* 2389:2070 */               bucketA[c2] = t;
/* 2390:     */             }
/* 2391:2072 */             int tmp368_367 = (bucketA[(c2 = c0)] + 1);t = tmp368_367;SA[tmp368_367] = s;
/* 2392:     */           }
/* 2393:     */         }
/* 2394:     */       }
/* 2395:     */       else
/* 2396:     */       {
/* 2397:2076 */         s1 ^= 0xFFFFFFFF;
/* 2398:     */       }
/* 2399:2079 */       if (s1 == 0)
/* 2400:     */       {
/* 2401:2080 */         SA[i] = T[(n - 1)];
/* 2402:2081 */         orig = i;
/* 2403:     */       }
/* 2404:     */       else
/* 2405:     */       {
/* 2406:2083 */         SA[i] = T[(s1 - 1)];
/* 2407:     */       }
/* 2408:     */     }
/* 2409:2086 */     return orig;
/* 2410:     */   }
/* 2411:     */   
/* 2412:     */   public int bwt()
/* 2413:     */   {
/* 2414:2094 */     int[] SA = this.SA;
/* 2415:2095 */     byte[] T = this.T;
/* 2416:2096 */     int n = this.n;
/* 2417:     */     
/* 2418:2098 */     int[] bucketA = new int[256];
/* 2419:2099 */     int[] bucketB = new int[65536];
/* 2420:2101 */     if (n == 0) {
/* 2421:2102 */       return 0;
/* 2422:     */     }
/* 2423:2104 */     if (n == 1)
/* 2424:     */     {
/* 2425:2105 */       SA[0] = T[0];
/* 2426:2106 */       return 0;
/* 2427:     */     }
/* 2428:2109 */     int m = sortTypeBstar(bucketA, bucketB);
/* 2429:2110 */     if (0 < m) {
/* 2430:2111 */       return constructBWT(bucketA, bucketB);
/* 2431:     */     }
/* 2432:2113 */     return 0;
/* 2433:     */   }
/* 2434:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2DivSufSort
 * JD-Core Version:    0.7.0.1
 */