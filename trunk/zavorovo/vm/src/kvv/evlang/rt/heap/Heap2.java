package kvv.evlang.rt.heap;

import java.util.Arrays;

import kvv.evlang.rt.wnd.HeapFrame;

public abstract class Heap2 implements Heap {

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

	HeapFrame heapFrame = new HeapFrame();

	private void draw() {
		short[] entr = new short[entries];
		for (int i = 0; i < entries; i++)
			entr[i] = _getEntry(i);
		heapFrame.setData(entr);
	}

	public Heap2(int sz, int entries) {
		this.entries = entries;
		data = new byte[sz];
		idxArr = data.length - entries * 2;
		tempArr = idxArr - entries;

		for (int i = 1; i < entries; i++)
			_addToFree(i);

		draw();
	}

	private void _addToFree(int e) {
		_setEntry(e, firstFree);
		firstFree = (short) e;
	}

	private short _getFree() {
		if (firstFree == 0)
			return 0;
		int e = firstFree;
		firstFree = _getEntry(firstFree);
		return (short) e;
	}

	private short _get(int off) {
		return (short) ((data[off] << 8) + (data[off + 1] & 0xFF));
	}

	private void _set(int off, short n) {
		data[off] = (byte) (n >> 8);
		data[off + 1] = (byte) n;
	}

	private short _getEntry(int e) {
		return _get(idxArr + e * 2);
	}

	private void _setEntry(int e, int n) {
		_set(idxArr + e * 2, (short) n);
	}

	private int _getTypeIdxArrSz(int off) {
		return data[off + TYPE_SIZE_OFF] & 0xFF;
	}

	private void _setTypeIdxArrSz(int off, int n) {
		data[off + TYPE_SIZE_OFF] = (byte) n;
	}

	private final static int FLAG_MARKED = 1;
	private final static int FLAG_MARKED_WITH_REFS = 2;
	private final static int FLAG_ARRAY = 4;
	private final static int FLAG_OBJARRAY = 8;

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
		return a > 0 && a < entries && (_getEntry(a) & 0x8000) != 0;
	}

	@Override
	public int alloc(int typeIdx_arrSize, boolean array, boolean objArray) {
		int sz = array ? typeIdx_arrSize : getTypeSize(typeIdx_arrSize);

		if (here + FIELDS_OFF + sz * 2 > tempArr)
			return 0;

		int e = _getFree();
		if (e == 0)
			return 0;

		int off = here;
		here += sz * 2 + FIELDS_OFF;

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

		System.err.println("alloc " + e + " off=" + off + " sz=" + sz
				+ (array ? " []" : ""));

		draw();

		return e;
	}

	@Override
	public short get(int a, int idx) {
		return _getField(_getEntry(a) & 0x7FFF, idx);
	}

	@Override
	public void set(int a, int idx, int val) {
		_setField(_getEntry(a) & 0x7FFF, idx, val);
	}

	int markIdx = 0;
	int markSz = 0;

	void startMark() {
		markIdx = 0;
		markSz = 0;
	}

	@Override
	public boolean mark(int a) {
		if (!isValidRef(a))
			return false;

		int off = _getEntry(a) & 0x7FFF;
		int flags = _getFlags(off);

		if ((flags & FLAG_MARKED) != 0)
			return false;

		flags |= FLAG_MARKED;

		_setFlags(off, flags);

		data[tempArr + markSz++] = (byte) a;
		return true;
	}

	public void closure() {
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

	public boolean _mark(int a) {
		if (!isValidRef(a))
			return false;

		int off = _getEntry(a) & 0x7FFF;
		int flags = _getFlags(off);

		if ((flags & FLAG_MARKED) != 0)
			return false;

		flags |= FLAG_MARKED;

		_setFlags(off, flags);

		return true;
	}

	@Override
	public void markClosure() {
		boolean cont = true;

		while (cont) {
			cont = false;
			for (int a = 1; a < entries; a++) {
				if (!isValidRef(a))
					continue;
				int off = _getEntry(a) & 0x7FFF;
				int flags = _getFlags(off);

				if ((flags & FLAG_MARKED_WITH_REFS) != 0)
					continue;

				if ((flags & FLAG_MARKED) == 0)
					continue;

				if ((flags & FLAG_ARRAY) == 0) {
					int typeIdx = _getTypeIdxArrSz(off);
					int mask = getTypeMask(typeIdx);
					int sz = getTypeSize(typeIdx);
					for (int i = 0; i < sz; i++) {
						if ((mask & 1) != 0)
							cont = mark(_getField(off, i)) || cont;
						mask >>= 1;
					}
				} else if ((flags & FLAG_OBJARRAY) != 0) {
					int sz = _getTypeIdxArrSz(off);
					for (int i = 0; i < sz; i++)
						cont = mark(_getField(off, i)) || cont;
				}

				flags |= FLAG_MARKED_WITH_REFS;
				_setFlags(off, flags);
			}
		}

	}

	@Override
	public void sweep() {
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

			if (src != dst) {
				int e = _getBackEntry(src);
				System.arraycopy(data, src, data, dst, sz);
				_setEntry(e, dst | 0x8000);
			}

			if ((flags & FLAG_MARKED) == 0) {
				int e = _getBackEntry(src);
				_addToFree(e);
				System.err.println("free " + e);
			} else {
				flags &= ~FLAG_MARKED;
				flags &= ~FLAG_MARKED_WITH_REFS;
				_setFlags(src, flags);
				dst += sz;
				newCnt++;
			}
			src += sz;
		}
		cnt = newCnt;
		here = dst;
		draw();
	}

	@Override
	public int getArraySize(int a) {
		return _getTypeIdxArrSz(_getEntry(a) & 0x7FFF);
	}

	@Override
	public int getTypeIdx(int a) {
		return _getTypeIdxArrSz(_getEntry(a) & 0x7FFF);
	}

	public static void main(String[] args) {
		Heap heap = new Heap2(510, 10) {

			@Override
			protected int getTypeSize(int typeIdx) {
				if (typeIdx == 1)
					return 4;
				if (typeIdx == 2)
					return 8;
				return 2;
			}

			@Override
			protected int getTypeMask(int typeIdx) {
				if (typeIdx == 1)
					return 0x01;
				if (typeIdx == 2)
					return 0x03;
				return 0;
			}

		};

		int e1 = heap.alloc(1, false, false);
		int e2 = heap.alloc(1, false, false);

		heap.mark(e2);
		heap.mark(e1);
		heap.markClosure();
		heap.sweep();
	}

}
