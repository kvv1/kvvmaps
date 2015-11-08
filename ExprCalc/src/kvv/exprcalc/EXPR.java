/* EXPR.java */
/* Generated By:JavaCC: Do not edit this line. EXPR.java */
package kvv.exprcalc;
import java.io.*;
import kvv.stdutils.Utils;

@SuppressWarnings("unused")
public abstract class EXPR implements EXPRConstants {
  public EXPR(String text)
  {
        this(new StringReader(Utils.utf2win(text)));
    //this (new JavaCCReader(fileName));
  }

  public void throwExc(String msg) throws ParseException
  {
        throw new ParseException(token, msg);
  }

  public short parse() throws ParseException, IOException
  {
    return expr(false);
  }

  public abstract short getValue(String name) throws ParseException;

  final public short expr(boolean sim) throws ParseException {short res;
    res = condExpr(sim);
{if ("" != null) return res;}
    throw new Error("Missing return statement in function");
  }

  final public short condExpr(boolean sim) throws ParseException {short cond;
  short left;
  short right;
    cond = logOrExpr(sim);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 26:{
      jj_consume_token(26);
      left = expr(sim || cond == 0);
      jj_consume_token(27);
      right = condExpr(sim || cond != 0);
{if ("" != null) return cond != 0 ? left : right;}
      break;
      }
    default:
      jj_la1[0] = jj_gen;
      ;
    }
{if ("" != null) return cond;}
    throw new Error("Missing return statement in function");
  }

  final public short logOrExpr(boolean sim) throws ParseException {short res;
  short temp;
    res = logAndExpr(sim);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case OR:{
        ;
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(OR);
      temp = logAndExpr(sim || res != 0);
if(res == 0)
                        res = temp == 0 ? (short)0 : (short)1;
                else
                        res = res == 0 ? (short)0 : (short)1;
    }
{if ("" != null) return res;}
    throw new Error("Missing return statement in function");
  }

  final public short logAndExpr(boolean sim) throws ParseException {short res;
  short temp;
    res = boolExpr(sim);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case AND:{
        ;
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      jj_consume_token(AND);
      temp = boolExpr(sim || (res == 0));
if(res != 0)
                        res = temp == 0 ? (short)0 : (short)1;
                else
                        res = res == 0 ? (short)0 : (short)1;
    }
{if ("" != null) return res;}
    throw new Error("Missing return statement in function");
  }

  final public short boolExpr(boolean sim) throws ParseException {int res;
  short temp;
    res = intExpr(sim);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case EQ:
      case NEQ:
      case LT:
      case LE:
      case GT:
      case GE:{
        ;
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case EQ:{
        jj_consume_token(EQ);
        temp = intExpr(sim);
res = res == temp ? 1 : 0;
        break;
        }
      case NEQ:{
        jj_consume_token(NEQ);
        temp = intExpr(sim);
res = res != temp ? 1 : 0;
        break;
        }
      case LT:{
        jj_consume_token(LT);
        temp = intExpr(sim);
res = res < temp ? 1 : 0;
        break;
        }
      case LE:{
        jj_consume_token(LE);
        temp = intExpr(sim);
res = res <= temp ? 1 : 0;
        break;
        }
      case GT:{
        jj_consume_token(GT);
        temp = intExpr(sim);
res = res > temp ? 1 : 0;
        break;
        }
      case GE:{
        jj_consume_token(GE);
        temp = intExpr(sim);
res = res >= temp ? 1 : 0;
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
{if ("" != null) return (short)res;}
    throw new Error("Missing return statement in function");
  }

  final public short intExpr(boolean sim) throws ParseException {int res;
  short temp;
    res = term(sim);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PLUS:
      case MINUS:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PLUS:{
        jj_consume_token(PLUS);
        temp = term(sim);
res = res + temp;
        break;
        }
      case MINUS:{
        jj_consume_token(MINUS);
        temp = term(sim);
res = res - temp;
        break;
        }
      default:
        jj_la1[6] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
{if ("" != null) return (short)res;}
    throw new Error("Missing return statement in function");
  }

  final public short term(boolean sim) throws ParseException {int res;
  short temp;
    res = unary(sim);
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case MULTIPLY:
      case DIVIDE:{
        ;
        break;
        }
      default:
        jj_la1[7] = jj_gen;
        break label_5;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case MULTIPLY:{
        jj_consume_token(MULTIPLY);
        temp = unary(sim);
res = res * temp;
        break;
        }
      case DIVIDE:{
        jj_consume_token(DIVIDE);
        temp = unary(sim);
if(sim)
                res = 0;
        else
                res = res / temp;
        break;
        }
      default:
        jj_la1[8] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
{if ("" != null) return (short)res;}
    throw new Error("Missing return statement in function");
  }

  final public short unary(boolean sim) throws ParseException {short res;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case NOT:{
      jj_consume_token(NOT);
      res = simpleElement(sim);
{if ("" != null) return res == 0 ? (short)1 : (short)0;}
      break;
      }
    case MINUS:{
      jj_consume_token(MINUS);
      res = simpleElement(sim);
{if ("" != null) return (short)-res;}
      break;
      }
    case MULDIV:
    case ID:
    case NUMBER:
    case 28:{
      res = simpleElement(sim);
{if ("" != null) return res;}
      break;
      }
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public short simpleElement(boolean sim) throws ParseException {short res;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case ID:{
      res = id(sim);
{if ("" != null) return res;}
      break;
      }
    case NUMBER:{
      res = number();
{if ("" != null) return res;}
      break;
      }
    case MULDIV:{
      res = muldiv(sim);
{if ("" != null) return res;}
      break;
      }
    case 28:{
      jj_consume_token(28);
      res = expr(sim);
      jj_consume_token(29);
{if ("" != null) return res;}
      break;
      }
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public short muldiv(boolean sim) throws ParseException {short res;
  short temp;
  short temp1;
    jj_consume_token(MULDIV);
    jj_consume_token(28);
    res = expr(sim);
    jj_consume_token(30);
    temp = expr(sim);
    jj_consume_token(30);
    temp1 = expr(sim);
    jj_consume_token(29);
if(sim)
                {if ("" != null) return 0;}
    {if ("" != null) return (short)(res * temp / temp1);}
    throw new Error("Missing return statement in function");
  }

  final public short id(boolean sim) throws ParseException {Token t;
    t = jj_consume_token(ID);
if(sim)
                {if ("" != null) return 0;}
    {if ("" != null) return getValue(Utils.win2utf(t.image));}
    throw new Error("Missing return statement in function");
  }

  final public short number() throws ParseException {Token t;
    t = jj_consume_token(NUMBER);
{if ("" != null) return Short.parseShort(t.image);}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public EXPRTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[11];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x4000000,0x100,0x80,0x7e00,0x7e00,0x30000,0x30000,0xc0000,0xc0000,0x11628000,0x11600000,};
   }

  /** Constructor with InputStream. */
  public EXPR(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public EXPR(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new EXPRTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public EXPR(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new EXPRTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public EXPR(EXPRTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(EXPRTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[31];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 11; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 31; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
