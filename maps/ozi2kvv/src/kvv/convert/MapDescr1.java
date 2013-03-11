package kvv.convert;

public interface MapDescr1 extends MapDescr {
	int getMinDestY();

	int getMinDestX();

	int getMaxDestY();

	int getMaxDestX();

	double getSrcX(int dstX, int dstY);

	double getSrcY(int dstX, int dstY);
}
