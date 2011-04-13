package aeminium.gpu.operations.functions;

import aeminium.gpu.lists.properties.operations.Reducer;
import aeminium.gpu.operations.utils.UniqCounter;

public abstract class LambdaReducer<I> implements Reducer<I>, GPUFunction {
	
	private String id = null;
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getSeedSource() {
		return this.getSeed().toString();
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId(); 
		return id;
	}
}