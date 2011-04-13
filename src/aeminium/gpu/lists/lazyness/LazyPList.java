package aeminium.gpu.lists.lazyness;

import aeminium.gpu.lists.AbstractList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public class LazyPList<T> extends AbstractList<T> implements PList<T> {

	private boolean evaluated = false;
	private PList<T> actual;
	private LazyEvaluator<T> evaluator;
	private int lazynessLevel = 1;


	public LazyPList(LazyEvaluator<T> eval, int size) {
		super();
		this.size = size;
		this.evaluator = eval;
	}
	
	
	@SuppressWarnings("unchecked")
	public PList<T> evaluate() {
		if (!evaluated) {
			actual = (PList<T>) evaluator.evaluate();
			evaluated = true;
		}
		return actual;
	}
	
	@Override
	public void add(int index, T e) {
		evaluate();
		actual.add(index, e);
	}
	
	@Override
	public void remove(T o) {
		evaluate();
		actual.remove(o);
	}

	@Override
	public T get(int index) {
		evaluate();
		return actual.get(index);
	}
	
	@Override
	public void set(int index, T e) {
		evaluate();
		actual.set(index, e);
	}
	
	@Override
	public T remove(int index) {
		evaluate();
		return actual.remove(index);
	}
	
	@Override
	public PList<T> subList(int fromIndex, int toIndex) {
		evaluate();
		return actual.subList(fromIndex,toIndex);
	}
	
	@Override
	public Class<?> getType() {
		return evaluator.getType();
	}


	@Override
	public <O> PList<O> map(LambdaMapper<T, O> mapFun) {
		if (evaluator.canMergeWithMap(mapFun)) {
			Map<T,O> m = new Map<T,O>(mapFun, this, this.getDevice());
			LazyPList<O> r = (LazyPList<O>) evaluator.mergeWithMap(m);
			r.setLazynessLevel(lazynessLevel+1);
			return r;
		} else {
			evaluate();
			return actual.map(mapFun);
		}
	}


	@Override
	public T reduce(LambdaReducer<T> reducer) {
		if (evaluator.canMergeWithReduce(reducer)) {
			Reduce<T> m = new Reduce<T>(reducer, this, this.getDevice());
			return evaluator.mergeWithReducer(m);
		} else {
			evaluate();
			return actual.reduce(reducer);
		}
	}
	
	
	
	
	public int getLazynessLevel() {
		return lazynessLevel;
	}


	public void setLazynessLevel(int lazynessLevel) {
		this.lazynessLevel = lazynessLevel;
	}
	
}
