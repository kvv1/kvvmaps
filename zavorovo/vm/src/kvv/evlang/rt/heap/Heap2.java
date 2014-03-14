package kvv.evlang.rt.heap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public abstract class Heap2 implements Heap {
	private static final int REF_VALUE_START = 0x7000;

	private byte[] data;

	private int here;
	private int tempArr;
	private int idxArr;
	private int entries;

	private int firstFree;

	private int cnt;

	private static final int FLAGS_OFF = 0;
	private static final int TYPE_SIZE_OFF = 1;
	private static final int ENTRY_OFF = 2;
	private static final int FIELDS_OFF = 3;

	protected abstract int getTypeSize(int typeIdx);

	protected abstract int getTypeMask(int typeIdx);

	protected abstract void gc();

	private PrintStream out = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});
	
	{
		out = System.out;
	}

	private void draw() {
		short[] entr = new short[entries];
		for (int i = 0; i < entries; i++)
			entr[i] = _getEntry(i);
		//heapFrame.setData(entr);
	}

	public Heap2(int sz) {
		data = new byte[sz];
		this.entries = 0;

		idxArr = data.length - entries * 2;
		tempArr = idxArr - entries;

		extendIdx();
		firstFree = 0;

		draw();
	}

	private void _addToFree(int e) {
		_setEntry(e, firstFree);
		firstFree = e;
	}

	private short _getFree() {
		if (firstFree == 0)
			extendIdx();
		if (firstFree == 0)
			return 0;
		int e = firstFree;
		firstFree = _getEntry(firstFree);
		return (short) e;
	}

	private void extendIdx() {
		if (tempArr - here >= 3) {
			entries++;
			idxArr = data.length - entries * 2;
			tempArr = idxArr - entries;
			_addToFree(entries - 1);
			out.print(" + ");
		}
	}

	private short _get(int off) {
		return (short) ((data[off] << 8) + (data[off + 1] & 0xFF));
	}

	private void _set(int off, short n) {
		data[off] = (byte) (n >> 8);
		data[off + 1] = (byte) n;
	}

	private short _getEntry(int e) {
		if (e < 0 || e >= entries)
			throw new IllegalArgumentException();
		return _get(data.length - (e + 1) * 2);
	}

	private void _setEntry(int e, int n) {
		if (e >= entries)
			throw new IllegalArgumentException();
		_set(data.length - (e + 1) * 2, (short) n);
	}

	private int _getTypeIdxArrSz(int off) {
		return data[off + TYPE_SIZE_OFF] & 0xFF;
	}

	private void _setTypeIdxArrSz(int off, int n) {
		data[off + TYPE_SIZE_OFF] = (byte) n;
	}

	private final static int FLAG_MARKED = 1;
	private final static int FLAG_ARRAY = 2;
	private final static int FLAG_OBJARRAY = 4;

	private int _getBackEntry(int off) {
		return data[off + ENTRY_OFF] & 0xFF;
	}

	private void _setBackEntry(int off, int e) {
		data[off + ENTRY_OFF] = (byte) e;
	}

	private int _getFlags(int off) {
		return data[off + FLAGS_OFF] & 0xFF;
	}

	private void _setFlags(int off, int flags) {
		data[off + FLAGS_OFF] = (byte) flags;
	}

	private short _getField(int off, int idx) {
		return _get(off + FIELDS_OFF + idx * 2);
	}

	private void _setField(int off, int idx, int val) {
		_set(off + FIELDS_OFF + idx * 2, (short) val);
	}

	private boolean isValidRef(int a) {
		if (a == 0)
			return false;

		a -= REF_VALUE_START;
		return a > 0 && a < entries && (_getEntry(a) & 0x8000) != 0;
	}

	@Override
	public int alloc(int typeIdx_arrSize, boolean array, boolean objArray) {
		int e = _getFree();
		if (e == 0)
			return 0;

		int sz = array ? typeIdx_arrSize : getTypeSize(typeIdx_arrSize);

		int newHere = here + FIELDS_OFF + sz * 2;

		if (newHere > tempArr) {
			_addToFree(e);
			return 0;
		}

		int off = here;
		here = newHere;

		Arrays.fill(data, off, here, (byte) 0);

		_setTypeIdxArrSz(off, typeIdx_arrSize);

		int flags = 0;
		if (array)
			flags |= FLAG_ARRAY;
		if (objArray)
			flags |= FLAG_OBJARRAY;

		_setEntry(e, off | 0x8000);
		_setFlags(off, flags);

		_setBackEntry(off, e);

		cnt++;

		out.print(" alloc " + e + " off=" + off + " sz=" + sz
				+ (array ? " [] " : " "));

		draw();

		return e + REF_VALUE_START;
	}

	public int alloc2(int typeIdx_arrSize, boolean array, boolean objArray) {
		int res = alloc(typeIdx_arrSize, array, objArray);
		if (res == 0) {
			gc();
			res = alloc(typeIdx_arrSize, array, objArray);
			if (res == 0)
				throw new RuntimeException("VM out of memory");
		}
		return res;
	}

	@Override
	public short get(int a, int idx) {
		return _getField(_getEntry(a - REF_VALUE_START) & 0x7FFF, idx);
	}

	@Override
	public void set(int a, int idx, int val) {
		_setField(_getEntry(a - REF_VALUE_START) & 0x7FFF, idx, val);
	}

	private int markIdx = 0;
	private int markSz = 0;

	@Override
	public void startMark() {
		markIdx = 0;
		markSz = 0;
	}

	@Override
	public boolean mark(int a) {
		if (!isValidRef(a))
			return false;

		int off = _getEntry(a - REF_VALUE_START) & 0x7FFF;
		int flags = _getFlags(off);

		if ((flags & FLAG_MARKED) != 0)
			return false;

		flags |= FLAG_MARKED;

		_setFlags(off, flags);

		data[tempArr + markSz++] = (byte) (a - REF_VALUE_START);
		return true;
	}

	@Override
	public void markClosure() {
		while (markIdx < markSz) {
			int a = data[tempArr + markIdx];

			int off = _getEntry(a) & 0x7FFF;
			int flags = _getFlags(off);

			if ((flags & FLAG_ARRAY) == 0) {
				int typeIdx = _getTypeIdxArrSz(off);
				int mask = getTypeMask(typeIdx);
				int sz = getTypeSize(typeIdx);
				for (int i = 0; i < sz; i++) {
					if ((mask & 1) != 0)
						mark(_getField(off, i));
					mask >>= 1;
				}
			} else if ((flags & FLAG_OBJARRAY) != 0) {
				int sz = _getTypeIdxArrSz(off);
				for (int i = 0; i < sz; i++)
					mark(_getField(off, i));
			}

			markIdx++;
		}
	}

	@Override
	public void sweep() {
		out.println();
		int dst = 0;
		int src = 0;

		int newCnt = 0;

		for (int i = 0; i < cnt; i++) {
			int flags = _getFlags(src);

			int sz;

			if ((flags & FLAG_ARRAY) != 0)
				sz = _getTypeIdxArrSz(src) * 2 + FIELDS_OFF;
			else
				sz = getTypeSize(_getTypeIdxArrSz(src)) * 2 + FIELDS_OFF;

			int e = _getBackEntry(src);

			if ((flags & FLAG_MARKED) == 0) {
				_addToFree(e);
				out.print(" free " + e + " ");
			} else {
				flags &= ~FLAG_MARKED;
				_setFlags(src, flags);
				if (src != dst) {
					System.arraycopy(data, src, data, dst, sz);
					_setEntry(e, dst | 0x8000);
				}
				dst += sz;
				newCnt++;
			}
			src += sz;
		}
		cnt = newCnt;
		here = dst;
		draw();
		out.println(" here=" + here + " cnt=" + cnt + " entries=" + entries);
	}

	@Override
	public int getArraySize(int a) {
		return _getTypeIdxArrSz(_getEntry(a - REF_VALUE_START) & 0x7FFF);
	}

	@Override
	public int getTypeIdx(int a) {
		return _getTypeIdxArrSz(_getEntry(a - REF_VALUE_START) & 0x7FFF);
	}

	@Override
	public int getRawDataOffset(int a) {
		return _getEntry(a) & 0x7FFF;
	}

	@Override
	public short getRaw(int offset, int idx) {
		return _getField(offset, idx);
	}

	@Override
	public void setRaw(int offset, int idx, int val) {
		_setField(offset, idx, val);
	}
}
