/* Generated By:JavaCC: Do not edit this line. EG1TokenManager.java */
package kvv.evlang;
import java.io.*;
import java.util.*;
import kvv.evlang.impl.*;
import kvv.evlang.impl.Locals.Local;
import kvv.controllers.register.*;

/** Token Manager. */
public class EG1TokenManager implements EG1Constants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x1fffcff00000L) != 0L)
         {
            jjmatchedKind = 45;
            return 1;
         }
         if ((active0 & 0x80000L) != 0L)
            return 7;
         return -1;
      case 1:
         if ((active0 & 0x1fffcdf00000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 1;
            return 1;
         }
         if ((active0 & 0x2000000L) != 0L)
            return 1;
         return -1;
      case 2:
         if ((active0 & 0x1dedcdd00000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 2;
            return 1;
         }
         if ((active0 & 0x21200200000L) != 0L)
            return 1;
         return -1;
      case 3:
         if ((active0 & 0x14e8c9d00000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 3;
            return 1;
         }
         if ((active0 & 0x90504000000L) != 0L)
            return 1;
         return -1;
      case 4:
         if ((active0 & 0x488c0100000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 4;
            return 1;
         }
         if ((active0 & 0x106009c00000L) != 0L)
            return 1;
         return -1;
      case 5:
         if ((active0 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 5;
            return 1;
         }
         if ((active0 & 0x48840100000L) != 0L)
            return 1;
         return -1;
      case 6:
         if ((active0 & 0x80000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 6;
            return 1;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 15;
         return jjMoveStringLiteralDfa1_0(0x400L);
      case 38:
         return jjMoveStringLiteralDfa1_0(0x80L);
      case 40:
         return jjStopAtPos(0, 55);
      case 41:
         return jjStopAtPos(0, 56);
      case 42:
         return jjStopAtPos(0, 18);
      case 43:
         jjmatchedKind = 16;
         return jjMoveStringLiteralDfa1_0(0x20000000L);
      case 44:
         return jjStopAtPos(0, 57);
      case 45:
         jjmatchedKind = 17;
         return jjMoveStringLiteralDfa1_0(0x10000000L);
      case 46:
         return jjStopAtPos(0, 59);
      case 47:
         return jjStartNfaWithStates_0(0, 19, 7);
      case 58:
         return jjStopAtPos(0, 49);
      case 59:
         return jjStopAtPos(0, 51);
      case 60:
         jjmatchedKind = 11;
         return jjMoveStringLiteralDfa1_0(0x1000L);
      case 61:
         jjmatchedKind = 58;
         return jjMoveStringLiteralDfa1_0(0x200L);
      case 62:
         jjmatchedKind = 13;
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 91:
         return jjStopAtPos(0, 53);
      case 93:
         return jjStopAtPos(0, 54);
      case 99:
         return jjMoveStringLiteralDfa1_0(0x2081000000L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x4500000L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x202000000L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x40000000L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x30000000000L);
      case 112:
         return jjMoveStringLiteralDfa1_0(0x8000000L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x800200000L);
      case 115:
         return jjMoveStringLiteralDfa1_0(0x8000000000L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x85100800000L);
      case 118:
         return jjMoveStringLiteralDfa1_0(0x400000000L);
      case 119:
         return jjMoveStringLiteralDfa1_0(0x100000000000L);
      case 120:
         return jjMoveStringLiteralDfa1_0(0x40000000000L);
      case 123:
         return jjStopAtPos(0, 50);
      case 124:
         return jjMoveStringLiteralDfa1_0(0x100L);
      case 125:
         return jjStopAtPos(0, 52);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 38:
         if ((active0 & 0x80L) != 0L)
            return jjStopAtPos(1, 7);
         break;
      case 43:
         if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(1, 29);
         break;
      case 45:
         if ((active0 & 0x10000000L) != 0L)
            return jjStopAtPos(1, 28);
         break;
      case 61:
         if ((active0 & 0x200L) != 0L)
            return jjStopAtPos(1, 9);
         else if ((active0 & 0x400L) != 0L)
            return jjStopAtPos(1, 10);
         else if ((active0 & 0x1000L) != 0L)
            return jjStopAtPos(1, 12);
         else if ((active0 & 0x4000L) != 0L)
            return jjStopAtPos(1, 14);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000000L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x20900700000L);
      case 102:
         if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(1, 25, 1);
         break;
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x104080000000L);
      case 105:
         return jjMoveStringLiteralDfa2_0(active0, 0x800000L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000000L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x401000000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x81008000000L);
      case 116:
         return jjMoveStringLiteralDfa2_0(active0, 0x48000000000L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x10040000000L);
      case 124:
         if ((active0 & 0x100L) != 0L)
            return jjStopAtPos(1, 8);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0x80000000000L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x80000000L);
      case 103:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(2, 21, 1);
         break;
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x140408000000L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x10040000000L);
      case 109:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000L);
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0xc000400000L);
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000000L);
      case 116:
         if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_0(2, 33, 1);
         return jjMoveStringLiteralDfa3_0(active0, 0x2800000000L);
      case 119:
         if ((active0 & 0x20000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 41, 1);
         break;
      case 120:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000L);
      case 121:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 36, 1);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa4_0(active0, 0x2080000000L);
      case 100:
         if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_0(3, 34, 1);
         return jjMoveStringLiteralDfa4_0(active0, 0x40000000L);
      case 101:
         if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(3, 26, 1);
         return jjMoveStringLiteralDfa4_0(active0, 0xc00000L);
      case 108:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 40, 1);
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000L);
      case 109:
         return jjMoveStringLiteralDfa4_0(active0, 0x40000000000L);
      case 110:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000L);
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000000L);
      case 112:
         if ((active0 & 0x80000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 43, 1);
         break;
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
      case 116:
         if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_0(3, 32, 1);
         break;
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x8800000000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000000000L);
      case 101:
         if ((active0 & 0x100000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 44, 1);
         return jjMoveStringLiteralDfa5_0(active0, 0x40000000000L);
      case 103:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(4, 22, 1);
         break;
      case 104:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 37, 1);
         break;
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000000L);
      case 107:
         return jjMoveStringLiteralDfa5_0(active0, 0x80000000L);
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000L);
      case 114:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(4, 23, 1);
         return jjMoveStringLiteralDfa5_0(active0, 0x800000000L);
      case 116:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(4, 24, 1);
         else if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(4, 27, 1);
         break;
      case 119:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 38, 1);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 98:
         return jjMoveStringLiteralDfa6_0(active0, 0x80000000L);
      case 109:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(5, 20, 1);
         break;
      case 110:
         if ((active0 & 0x800000000L) != 0L)
            return jjStartNfaWithStates_0(5, 35, 1);
         break;
      case 114:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 42, 1);
         break;
      case 116:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 39, 1);
         break;
      case 118:
         if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_0(5, 30, 1);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 111:
         return jjMoveStringLiteralDfa7_0(active0, 0x80000000L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 120:
         if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(7, 31, 1);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffff00000000L, 0xffffffffffffffffL
};
static final long[] jjbitVec1 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 18;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(13, 14);
                  else if (curChar == 47)
                     jjCheckNAddStates(0, 2);
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 47)
                        kind = 47;
                     jjCheckNAdd(5);
                  }
                  else if (curChar == 47)
                     jjAddStates(3, 4);
                  else if (curChar == 34)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 45)
                     kind = 45;
                  jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 2:
                  if (curChar == 34)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 3:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 4:
                  if (curChar == 34 && kind > 46)
                     kind = 46;
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAdd(5);
                  break;
               case 6:
                  if (curChar == 47)
                     jjAddStates(3, 4);
                  break;
               case 8:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 9:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 10:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 11:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 12:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 13:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 14:
                  if (curChar == 42)
                     jjCheckNAddStates(5, 7);
                  break;
               case 15:
                  if ((0xffff7bffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(16, 14);
                  break;
               case 16:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(16, 14);
                  break;
               case 17:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 1:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 45)
                     kind = 45;
                  jjCheckNAdd(1);
                  break;
               case 3:
                  jjAddStates(8, 9);
                  break;
               case 8:
                  jjAddStates(0, 2);
                  break;
               case 13:
                  jjCheckNAddTwoStates(13, 14);
                  break;
               case 15:
               case 16:
                  jjCheckNAddTwoStates(16, 14);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 1:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 45)
                     kind = 45;
                  jjCheckNAdd(1);
                  break;
               case 3:
                  if ((jjbitVec1[i2] & l2) != 0L)
                     jjAddStates(8, 9);
                  break;
               case 8:
                  if ((jjbitVec1[i2] & l2) != 0L)
                     jjAddStates(0, 2);
                  break;
               case 13:
                  if ((jjbitVec1[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 15:
               case 16:
                  if ((jjbitVec1[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(16, 14);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 18 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   8, 9, 11, 7, 12, 14, 15, 17, 3, 4, 
};

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\46\46", "\174\174", "\75\75", 
"\41\75", "\74", "\74\75", "\76", "\76\75", "\41", "\53", "\55", "\52", "\57", 
"\145\145\160\162\157\155", "\162\145\147", "\145\145\162\145\147", "\164\151\155\145\162", 
"\143\157\156\163\164", "\151\146", "\145\154\163\145", "\160\162\151\156\164", "\55\55", "\53\53", 
"\155\165\154\144\151\166", "\143\150\145\143\153\142\157\170", "\164\145\170\164", "\151\156\164", 
"\166\157\151\144", "\162\145\164\165\162\156", "\164\162\171", "\143\141\164\143\150", 
"\164\150\162\157\167", "\163\164\162\165\143\164", "\156\165\154\154", "\156\145\167", 
"\170\164\151\155\145\162", "\164\162\141\160", "\167\150\151\154\145", null, null, null, null, "\72", 
"\173", "\73", "\175", "\133", "\135", "\50", "\51", "\54", "\75", "\56", };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
};
static final long[] jjtoToken = {
   0xffeffffffffff81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[18];
private final int[] jjstateSet = new int[36];
protected char curChar;
/** Constructor. */
public EG1TokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public EG1TokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 18; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
