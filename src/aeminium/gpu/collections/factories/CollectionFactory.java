package aeminium.gpu.collections.factories;

import aeminium.gpu.collections.lazyness.LazyPMatrix;
import aeminium.gpu.collections.lists.BooleanList;
import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.LongList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.collections.matrices.BooleanMatrix;
import aeminium.gpu.collections.matrices.CharMatrix;
import aeminium.gpu.collections.matrices.DoubleMatrix;
import aeminium.gpu.collections.matrices.FloatMatrix;
import aeminium.gpu.collections.matrices.IntMatrix;
import aeminium.gpu.collections.matrices.LongMatrix;
import aeminium.gpu.collections.matrices.PMatrix;
import aeminium.gpu.collections.properties.evaluation.ConcreteCollection;
import aeminium.gpu.collections.properties.evaluation.LazyCollection;

public class CollectionFactory {
	public static PList<?> listFromType(String outputType) {
		if (outputType.equals("Integer") || outputType.equals("java.lang.Integer"))
			return new IntList();
		if (outputType.equals("Float") || outputType.equals("java.lang.Float"))
			return new FloatList();
		if (outputType.equals("Double") || outputType.equals("java.lang.Double"))
			return new DoubleList();
		if (outputType.equals("Long") || outputType.equals("java.lang.Long"))
			return new LongList();
		if (outputType.equals("Character") || outputType.equals("java.lang.Character"))
			return new CharList();
		if (outputType.equals("Boolean") || outputType.equals("java.lang.Boolean"))
			return new BooleanList();

		return null;
	}

	public static PMatrix<?> matrixFromType(String outputType, int m, int n) {
		if (outputType.equals("Integer") || outputType.equals("java.lang.Integer"))
			return new IntMatrix(m, n);
		if (outputType.equals("Float") || outputType.equals("java.lang.Float"))
			return new FloatMatrix(m, n);
		if (outputType.equals("Double") || outputType.equals("java.lang.Double"))
			return new DoubleMatrix(m, n);
		if (outputType.equals("Long") || outputType.equals("java.lang.Long"))
			return new LongMatrix(m, n);
		if (outputType.equals("Character") || outputType.equals("java.lang.Character"))
			return new CharMatrix(m, n);
		if (outputType.equals("Boolean") || outputType.equals("java.lang.Boolean"))
			return new BooleanMatrix(m, n);
		return null;
	}

	public static <T> PMatrix<T> matrixfromPList(PList<T> o, int cols) {
		int rows = o.size() / cols;
		if (o.size() % cols != 0) {
			rows += 1;
		}
		return CollectionFactory.matrixfromPList(o, rows, cols);
	}

	@SuppressWarnings("unchecked")
	public static <T> PMatrix<T> matrixfromPList(PList<T> o, int rows, int cols) {
		if (o instanceof ConcreteCollection) {
			if (o instanceof IntList)
				return (PMatrix<T>) new IntMatrix(((IntList) o).getArray(),
						rows, cols);
			if (o instanceof FloatList)
				return (PMatrix<T>) new FloatMatrix(((FloatList) o).getArray(),
						rows, cols);
			if (o instanceof DoubleList)
				return (PMatrix<T>) new DoubleMatrix(
						((DoubleList) o).getArray(), rows, cols);
			if (o instanceof LongList)
				return (PMatrix<T>) new LongMatrix(((LongList) o).getArray(),
						rows, cols);
			if (o instanceof CharList)
				return (PMatrix<T>) new CharMatrix(((CharList) o).getArray(),
						rows, cols);
			if (o instanceof BooleanList)
				return (PMatrix<T>) new BooleanMatrix(
						((BooleanList) o).getArray(), rows, cols);
		}
		if (o instanceof LazyCollection) {
			return new LazyPMatrix<T>(o, rows, cols);
		}
		return null;
	}
}
