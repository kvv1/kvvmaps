package forth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Forth {

	private static Input input;

	public static void main(String[] args) {
		try {
			input = new Input(new FileInputStream(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		Init();

		ip = here;
//		compile(colonInterpreterIdx);
//		compile(0);
		compile("step");
		compile("branch");
		compile(-1);

		for(;;) {
			int code = codeFile[codeFile[ip]];
			int param = codeFile[ip];
			ip++;
			interpreters.get(code).interpret(param);
		}
	}

	static List<Interpreter> interpreters = new ArrayList<Interpreter>();

	static Stack astack = new Stack();
	static Stack rstack = new Stack();
	private static final int CODEFILE_SIZE = 1000;
	static int codeFile[] = new int[CODEFILE_SIZE];
	static int here;

	public static void compile(int n) {
		codeFile[here++] = n;
	}

	static int ip;
	static Dictionary dictionary = new Dictionary();

	static void Next() {
	}

	static boolean compilation;

	private static Word lit;

	static int doesInterpreterIdx = addInterpreter(new Interpreter() {
		@Override
		public void interpret(int codeIdx) {
			rstack.push(ip);
			astack.push(codeIdx + 2);
			ip = codeFile[codeIdx + 1];
		}
	});

	static int colonInterpreterIdx = addInterpreter(new Interpreter() {
		@Override
		public void interpret(int codeIdx) {
			rstack.push(ip);
			ip = codeIdx + 2;
		}
	});

	static int createInterpreterIdx = addInterpreter(new Interpreter() {
		@Override
		public void interpret(int codeIdx) {
			astack.push(codeIdx + 2);
		}
	});
	
	private static void Init() {
		
		addWord("s.", new Interpreter() {
			public void interpret(int codeIdx) {
				astack.print();
			}
		}, false);

		addWord("words", new Interpreter() {
			public void interpret(int codeIdx) {
				dictionary.print();
			}
		}, false);

		addWord("here", new Interpreter() {
			public void interpret(int codeIdx) {
				astack.push(here);
			}
		}, false);

		addWord("compile", new Interpreter() {
			public void interpret(int codeIdx) {
				compile(codeFile[ip++]);
			}
		}, false);

		addWord(",", new Interpreter() {
			public void interpret(int codeIdx) {
				compile(astack.pop());
			}
		}, false);

		addWord("immediate", new Interpreter() {
			public void interpret(int codeIdx) {
				dictionary.latest().setImmediate(true);
			}
		}, false);

		addWord("over", new Interpreter() {
			public void interpret(int codeIdx) {
				int b = astack.pop();
				int a = astack.pop();
				astack.push(a);
				astack.push(b);
				astack.push(a);
			}
		}, false);

		addWord("swap", new Interpreter() {
			public void interpret(int codeIdx) {
				int a = astack.pop();
				int b = astack.pop();
				astack.push(a);
				astack.push(b);
			}
		}, false);

		addWord("drop", new Interpreter() {
			public void interpret(int codeIdx) {
				astack.pop();
			}
		}, false);

		addWord("-", new Interpreter() {
			public void interpret(int codeIdx) {
				int a = astack.pop();
				int b = astack.pop();
				astack.push(b - a);
			}
		}, false);

		addWord("+", new Interpreter() {
			public void interpret(int codeIdx) {
				int a = astack.pop();
				int b = astack.pop();
				astack.push(b + a);
			}
		}, false);

		addWord("!", new Interpreter() {
			public void interpret(int codeIdx) {
				int a = astack.pop();
				int n = astack.pop();
				codeFile[a] = n;
			}
		}, false);

		addWord("@", new Interpreter() {
			public void interpret(int codeIdx) {
				int a = astack.pop();
				int n = codeFile[a];
				astack.push(n);
			}
		}, false);

		addWord("branch", new Interpreter() {
			public void interpret(int codeIdx) {
				ip += codeFile[ip] - 1;
			}
		}, false);

		addWord("?branch", new Interpreter() {
			public void interpret(int codeIdx) {
				if (astack.pop() == 0)
					ip += codeFile[ip] - 1;
				else
					ip++;
			}
		}, false);
		/*
		 * addWord("execute", new Interpreter() { public void Interpret(int
		 * params) { execute(astack.pop()); } });
		 */
		addWord("ret", new Interpreter() {
			public void interpret(int codeIdx) {
				ip = rstack.pop();
			}
		}, false);

		addWord(":", new Interpreter() {
			public void interpret(int codeIdx) {
				String name = input.getToken();
				addWord(name, colonInterpreterIdx, false);
				compilation = true;
			}
		}, false);

		addWord("create", new Interpreter() {
			public void interpret(int codeIdx) {
				String name = input.getToken();
				addWord(name, createInterpreterIdx, false);
			}
		}, false);
		
		addWord(";", new Interpreter() {
			public void interpret(int codeIdx) {
				compile("ret");
				compilation = false;
			}
		}, true);

		addWord(";does", new Interpreter() {
			public void interpret(int codeIdx) {
				Word latest = dictionary.latest();
				codeFile[latest.codeIdx] = doesInterpreterIdx;
				codeFile[latest.codeIdx + 1] = codeFile[ip++];
				ip = rstack.pop();
			}
		}, false);
		
		addWord("does>", new Interpreter() {
			public void interpret(int codeIdx) {
				compile(";does");
				compile(here + 1);
			}
		}, true);

		lit = addWord("lit", new Interpreter() {
			public void interpret(int codeIdx) {
				astack.push(codeFile[ip]);
				ip++;
			}
		}, false);

		addWord(".", new Interpreter() {
			public void interpret(int codeIdx) {
				int n = astack.pop();
				System.out.print(n + " ");
			}
		}, false);

		addWord("cr", new Interpreter() {
			public void interpret(int codeIdx) {
				System.out.println();
			}
		}, false);

		addWord("dump", new Interpreter() {
			public void interpret(int codeIdx) {
				int n = astack.pop();
				int a = astack.pop();
				System.out.println();
				System.out.println(a + " " + n);
				for(int i = 0; i < n; i++)
					System.out.print(codeFile[a + i] + " ");
				System.out.println();
			}
		}, false);

		addWord("//", new Interpreter() {
			public void interpret(int codeIdx) {
				input.skipLine();
			}
		}, false);

		addWord("step", new Interpreter() {
			public void interpret(int codeIdx) {
				String token = input.getToken();
				if (token == null)
					System.exit(0);
				Word word = dictionary.find(token);
				if (word != null) {
					if (compilation && !word.immediate)
						compile(word.codeIdx);
					else
						execute(word.codeIdx);
				} else {
					try {
						int num = Integer.parseInt(token);
						if (compilation) {
							compile(lit.codeIdx);
							compile(num);
						} else {
							astack.push(num);
						}

					} catch (NumberFormatException e) {
						System.out.println("\n" + token + " - ?");
						System.exit(0);
					}
				}
			}
		}, false);

	}

//	private static Interpreter colonInterpreter = new Interpreter() {
//		@Override
//		public void interpret(int codeIdx) {
//			Forth.rstack.push(Forth.ip);
//			Forth.ip = codeIdx + 2;
//		}
//	};
	
//	private static Interpreter createInterpreter = new Interpreter() {
//		@Override
//		public void interpret(int codeIdx) {
//			astack.push(codeIdx + 2);
//		}
//	};
	
	private static void compile(String name) {
		compile(dictionary.find(name).codeIdx);
	}

	private static int addInterpreter(Interpreter interpreter) {
		int interIdx = interpreters.size();
		interpreters.add(interpreter);
		return interIdx;
	}
	
	private static Word addWord(String name, int interIdx,
			boolean immed) {
		int h = here;
		compile(interIdx);
		compile(0);
		Word word = new Word(name, h, immed);
		dictionary.add(word);
		return word;
	}

	private static Word addWord(String name, Interpreter interpreter,
			boolean immed) {
		return addWord(name, addInterpreter(interpreter), immed);
	}

	private static void execute(int codeIdx) {
		Interpreter interpreter = interpreters.get(codeFile[codeIdx]);
		interpreter.interpret(codeIdx);
	}
}
