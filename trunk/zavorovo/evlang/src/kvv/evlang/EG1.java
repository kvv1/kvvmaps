/* Generated By:JavaCC: Do not edit this line. EG1.java */
package kvv.evlang;
import java.io.*;
import java.util.*;
import kvv.evlang.impl.*;
import kvv.evlang.rt.*;
import kvv.evlang.impl.Context;
import kvv.controllers.register.*;

public class EG1 extends EG implements EG1Constants {
  public void parse() throws ParseException
  {
    file();
    check();
    checkStack();
  }

  final public void file() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 0:
      jj_consume_token(0);
      break;
    case REG:
    case EEREG:
    case TIMER:
    case ONSET:
    case ONCHANGE:
    case CONST:
    case MAIN:
    case PROC:
    case FUNC:
    case CHECKBOX:
    case TEXT:
      line();
      file();
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void line() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONST:
      constant();
      break;
    case REG:
    case EEREG:
      register();
      break;
    case CHECKBOX:
    case TEXT:
      uiDecl();
      break;
    case TIMER:
      timer();
      break;
    case ONSET:
      onset();
      break;
    case ONCHANGE:
      onchange();
      break;
    case PROC:
      proc();
      break;
    case FUNC:
      func();
      break;
    case MAIN:
      main();
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void proc() throws ParseException {
  Token name;
  Code bytes;
    jj_consume_token(PROC);
    name = jj_consume_token(ID);
    jj_consume_token(45);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      args = argListDef();
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    jj_consume_token(46);
    bytes = stmtBlock();
    if (args.getArgCnt() == 0) bytes.add(BC.RET);
    else bytes.add(BC.RET_N);
    bytes.add(args.getArgCnt());
    Func func = getCreateFunc(name.image, args.getArgCnt(), false);
    func.code = new CodeRef(bytes);
    args.clear();
    System.out.println("proc " + name.image + " " + bytes.size());
  }

  final public void func() throws ParseException {
  Token name;
  Code bytes;
    jj_consume_token(FUNC);
    name = jj_consume_token(ID);
    jj_consume_token(45);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      args = argListDef();
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(46);
    jj_consume_token(47);
    bytes = expr();
    jj_consume_token(48);
    if (args.getArgCnt() == 0) bytes.add(BC.RETI);
    else bytes.add(BC.RETI_N);
    bytes.add(args.getArgCnt());
    Func func = getCreateFunc(name.image, args.getArgCnt(), true);
    func.code = new CodeRef(bytes);
    args.clear();
    System.out.println("func " + name.image + " " + bytes.size());
  }

  final public ArgListDef argListDef() throws ParseException {
  ArgListDef args = new ArgListDef();
  Token arg;
    arg = jj_consume_token(ID);
    args.add(arg.image);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 49:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_1;
      }
      jj_consume_token(49);
      arg = jj_consume_token(ID);
      args.add(arg.image);
    }
    args.endOfArgs();
    {if (true) return args;}
    throw new Error("Missing return statement in function");
  }

  final public void main() throws ParseException {
  Code code;
    jj_consume_token(MAIN);
    code = stmtBlock();
    code.add(BC.RET);
    Func func = new Func("", 0, 0, false);
    func.code = new CodeRef(code);
    funcValues.set(0, func);
    System.out.println("main " + code.size());
  }

  final public void constant() throws ParseException {
  Token name;
  Token value;
    jj_consume_token(CONST);
    name = jj_consume_token(ID);
    jj_consume_token(47);
    value = jj_consume_token(NUMBER);
    jj_consume_token(48);
    checkName(name.image);
    constants.put(name.image, Integer.parseInt(value.image));
  }

  final public void uiDecl() throws ParseException {
  Token name;
  Token text;
  RegType type;
    type = uitype();
    name = jj_consume_token(ID);
    text = jj_consume_token(STRING);
    jj_consume_token(48);
    setUI(name.image, text.image.replace("\u005c"", ""), type);
  }

  final public RegType uitype() throws ParseException {
  Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CHECKBOX:
      t = jj_consume_token(CHECKBOX);
    {if (true) return RegType.checkbox;}
      break;
    case TEXT:
      t = jj_consume_token(TEXT);
    {if (true) return RegType.textRW;}
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public void register() throws ParseException {
  Token regName;
  Token regNum = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case REG:
      jj_consume_token(REG);
      regName = jj_consume_token(ID);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 45:
        jj_consume_token(45);
        regNum = jj_consume_token(ID);
        jj_consume_token(46);
        break;
      default:
        jj_la1[6] = jj_gen;
        ;
      }
      jj_consume_token(48);
    newRegister(regName, regNum, false);
      break;
    case EEREG:
      jj_consume_token(EEREG);
      regName = jj_consume_token(ID);
      jj_consume_token(48);
    newRegister(regName, null, true);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void timer() throws ParseException {
  Token name;
  Code bytes;
    jj_consume_token(TIMER);
    name = jj_consume_token(ID);
    bytes = stmtBlock();
    bytes.add(BC.RET);
    Timer timer = getCreateTimer(name.image);
    timer.handler = new CodeRef(bytes);
    System.out.println("timer " + name.image + " " + bytes.size());
  }

  final public void onset() throws ParseException {
  Code cond;
  Code bytes;
    jj_consume_token(ONSET);
    jj_consume_token(45);
    cond = expr();
    jj_consume_token(46);
    bytes = stmtBlock();
    cond.add(BC.RETI);
    bytes.add(BC.RET);
    events.add(new Event(cond, bytes, EventType.SET));
    System.out.println("onset " + cond.size() + " " + bytes.size());
  }

  final public void onchange() throws ParseException {
  Code cond;
  Code bytes;
    jj_consume_token(ONCHANGE);
    jj_consume_token(45);
    cond = expr();
    jj_consume_token(46);
    bytes = stmtBlock();
    cond.add(BC.RETI);
    bytes.add(BC.RET);
    events.add(new Event(cond, bytes, EventType.CHANGE));
    System.out.println("onchange " + cond.size() + " " + bytes.size());
  }

  final public Code stmtBlock() throws ParseException {
  Code bytes = new Code();
  Code temp;
    jj_consume_token(50);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case PRINT:
      case ID:
      case 50:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_2;
      }
      temp = stmt();
      bytes.addAll(temp);
    }
    jj_consume_token(51);
    {if (true) return bytes;}
    throw new Error("Missing return statement in function");
  }

  final public Code stmt() throws ParseException {
  Code res;
  Token name;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      res = assign();
    {if (true) return res;}
      break;
    case IF:
      res = ifStmt();
    {if (true) return res;}
      break;
    case 50:
      res = stmtBlock();
    {if (true) return res;}
      break;
    case PRINT:
      jj_consume_token(PRINT);
      res = expr();
      jj_consume_token(48);
    res.add(BC.PRINT);
    {if (true) return res;}
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Code ifStmt() throws ParseException {
  Code res;
  Code stmt;
  Code stmt2 = null;
    jj_consume_token(IF);
    jj_consume_token(45);
    res = expr();
    jj_consume_token(46);
    stmt = stmt();
    if (jj_2_1(2)) {
      jj_consume_token(ELSE);
      stmt2 = stmt();
    } else {
      ;
    }
    if (stmt2 != null)
    {
      res.add(BC.QBRANCH);
      res.add(stmt.size() + 2);
      res.addAll(stmt);
      res.add(BC.BRANCH);
      res.add(stmt2.size());
      res.addAll(stmt2);
    }
    else
    {
      res.add(BC.QBRANCH);
      res.add(stmt.size());
      res.addAll(stmt);
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code assign() throws ParseException {
  Code res;
  Token name;
  List < Code > argList = new ArrayList < Code > ();
  Timer timer;
    name = jj_consume_token(ID);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 47:
      jj_consume_token(47);
      res = expr();
      jj_consume_token(48);
      Integer val = args.get(name.image);
      if (val != null)
      {
        res.compileSetLocal(val - args.getArgCnt());
      }
      else
      {
        RegisterDescr descr = registers.get(name.image);
        if (descr == null) {if (true) throw new ParseException(name.image + " - ?");}
        checkROReg(descr);
        res.compileSetreg(descr.reg);
      }
      {if (true) return res;}
      break;
    case 52:
      jj_consume_token(52);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case START_S:
        jj_consume_token(START_S);
        res = expr();
        jj_consume_token(48);
        timer = getCreateTimer(name.image);
        res.add(BC.SETTIMER_S);
        res.add(timer.n);
        {if (true) return res;}
        break;
      case START_MS:
        jj_consume_token(START_MS);
        res = expr();
        jj_consume_token(48);
        timer = getCreateTimer(name.image);
        res.add(BC.SETTIMER_MS);
        res.add(timer.n);
        {if (true) return res;}
        break;
      case STOP:
        jj_consume_token(STOP);
        jj_consume_token(48);
        timer = getCreateTimer(name.image);
        res = new Code();
        res.add(BC.STOPTIMER);
        res.add(timer.n);
        {if (true) return res;}
        break;
      default:
        jj_la1[10] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case DEC:
      jj_consume_token(DEC);
      jj_consume_token(48);
      RegisterDescr descr = registers.get(name.image);
      if (descr == null) {if (true) throw new ParseException(name.image + " - ?");}
      checkROReg(descr);
      res = new Code();
      res.add(BC.DEC);
      res.add(descr.reg);
      {if (true) return res;}
      break;
    case INC:
      jj_consume_token(INC);
      jj_consume_token(48);
      descr = registers.get(name.image);
      if (descr == null) {if (true) throw new ParseException(name.image + " - ?");}
      checkROReg(descr);
      res = new Code();
      res.add(BC.INC);
      res.add(descr.reg);
      {if (true) return res;}
      break;
    case 45:
      jj_consume_token(45);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
      case MINUS:
      case MULDIV:
      case ID:
      case NUMBER:
      case 45:
        argList = argList();
        break;
      default:
        jj_la1[11] = jj_gen;
        ;
      }
      jj_consume_token(46);
      jj_consume_token(48);
      Func func = getCreateFunc(name.image, argList.size(), false);
      res = new Code();
      for (Code c : argList) res.addAll(c);
      res.add(BC.CALLP);
      res.add(func.n);
      {if (true) return res;}
      break;
    default:
      jj_la1[12] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Code expr() throws ParseException {
  Code res;
  Code temp;
    res = logAndExpr();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_3;
      }
      jj_consume_token(OR);
      temp = logAndExpr();
      res.addAll(temp);
      res.add(BC.OR);
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code logAndExpr() throws ParseException {
  Code res;
  Code temp;
    res = boolExpr();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_4;
      }
      jj_consume_token(AND);
      temp = boolExpr();
      res.addAll(temp);
      res.add(BC.AND);
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code boolExpr() throws ParseException {
  Code res;
  Code temp;
    res = intExpr();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQ:
      case NEQ:
      case LT:
      case LE:
      case GT:
      case GE:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_5;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQ:
        jj_consume_token(EQ);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.EQ);
        break;
      case NEQ:
        jj_consume_token(NEQ);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.NEQ);
        break;
      case LT:
        jj_consume_token(LT);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.LT);
        break;
      case LE:
        jj_consume_token(LE);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.LE);
        break;
      case GT:
        jj_consume_token(GT);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.GT);
        break;
      case GE:
        jj_consume_token(GE);
        temp = intExpr();
        res.addAll(temp);
        res.add(BC.GE);
        break;
      default:
        jj_la1[16] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code intExpr() throws ParseException {
  Code res;
  Code temp;
    res = term();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        ;
        break;
      default:
        jj_la1[17] = jj_gen;
        break label_6;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        temp = term();
        res.addAll(temp);
        res.add(BC.ADD);
        break;
      case MINUS:
        jj_consume_token(MINUS);
        temp = term();
        res.addAll(temp);
        res.add(BC.SUB);
        break;
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code term() throws ParseException {
  Code res;
  Code temp;
    res = unary();
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MULTIPLY:
      case DIVIDE:
        ;
        break;
      default:
        jj_la1[19] = jj_gen;
        break label_7;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MULTIPLY:
        jj_consume_token(MULTIPLY);
        temp = unary();
        res.addAll(temp);
        res.add(BC.MUL);
        break;
      case DIVIDE:
        jj_consume_token(DIVIDE);
        temp = unary();
        res.addAll(temp);
        res.add(BC.DIV);
        break;
      default:
        jj_la1[20] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code unary() throws ParseException {
  Code res;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
      res = element();
    res.add(BC.NOT);
    {if (true) return res;}
      break;
    case MINUS:
      jj_consume_token(MINUS);
      res = element();
    res.add(BC.NEGATE);
    {if (true) return res;}
      break;
    case MULDIV:
    case ID:
    case NUMBER:
    case 45:
      res = element();
    {if (true) return res;}
      break;
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public List < Code > argList() throws ParseException {
  List < Code > res = new ArrayList < Code > ();
  Code arg;
    arg = expr();
    res.add(arg);
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 49:
        ;
        break;
      default:
        jj_la1[22] = jj_gen;
        break label_8;
      }
      jj_consume_token(49);
      arg = expr();
      res.add(arg);
    }
    {if (true) return res;}
    throw new Error("Missing return statement in function");
  }

  final public Code element() throws ParseException {
  Token t;
  Code res = new Code();
  boolean call = false;
  List < Code > argList = new ArrayList < Code > ();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NUMBER:
      t = jj_consume_token(NUMBER);
    res = new Code();
    short s = Short.parseShort(t.image);
    res.compileLit(s);
    {if (true) return res;}
      break;
    case ID:
      t = jj_consume_token(ID);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 45:
        jj_consume_token(45);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
        case MINUS:
        case MULDIV:
        case ID:
        case NUMBER:
        case 45:
          argList = argList();
          break;
        default:
          jj_la1[23] = jj_gen;
          ;
        }
        jj_consume_token(46);
      call = true;
        break;
      default:
        jj_la1[24] = jj_gen;
        ;
      }
    if (call)
    {
      Func func = getCreateFunc(t.image, argList.size(), true);
      res = new Code();
      for (Code c : argList) res.addAll(c);
      res.add(BC.CALLF);
      res.add(func.n);
      {if (true) return res;}
    }
    else
    {
      res = new Code();
      Integer val = args.get(t.image);
      if (val != null)
      {
        res.compileGetLocal(val - args.getArgCnt());
      }
      else
      {
        RegisterDescr descr = registers.get(t.image);
        if (descr != null)
        {
          res.compileGetreg(descr.reg);
        }
        else
        {
          val = constants.get(t.image);
          if (val == null) {if (true) throw new ParseException(t.image + " - ?");}
          res.compileLit((short) (int) val);
        }
      }
      {if (true) return res;}
    }
      break;
    case MULDIV:
    Code temp;
    Code temp1;
      jj_consume_token(MULDIV);
      jj_consume_token(45);
      res = expr();
      jj_consume_token(49);
      temp = expr();
      jj_consume_token(49);
      temp1 = expr();
      jj_consume_token(46);
    res.addAll(temp);
    res.addAll(temp1);
    res.add(BC.MULDIV);
    {if (true) return res;}
      break;
    case 45:
      jj_consume_token(45);
      res = expr();
      jj_consume_token(46);
    {if (true) return res;}
      break;
    default:
      jj_la1[25] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_3R_14() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_scan_token(PRINT)) return true;
    return false;
  }

  private boolean jj_3R_12() {
    if (jj_3R_16()) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_scan_token(ELSE)) return true;
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_scan_token(50)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_3R_15()) return true;
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(IF)) return true;
    return false;
  }

  private boolean jj_3R_9() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_10()) {
    jj_scanpos = xsp;
    if (jj_3R_11()) {
    jj_scanpos = xsp;
    if (jj_3R_12()) {
    jj_scanpos = xsp;
    if (jj_3R_13()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3R_10() {
    if (jj_3R_14()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public EG1TokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[26];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x7e00001,0x7e00000,0x0,0x0,0x0,0x0,0x0,0x600000,0x8000000,0x8000000,0xe0000000,0x28000,0x0,0x100,0x80,0x7e00,0x7e00,0x30000,0x30000,0xc0000,0xc0000,0x28000,0x0,0x28000,0x0,0x0,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x1b1,0x1b1,0x200,0x200,0x20000,0x180,0x2000,0x0,0x40202,0x40202,0x0,0x2a40,0x10a00c,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x2a40,0x20000,0x2a40,0x2000,0x2a40,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public EG1(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public EG1(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new EG1TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
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
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public EG1(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new EG1TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public EG1(EG1TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(EG1TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 26; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
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

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[53];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 26; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 53; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
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

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
