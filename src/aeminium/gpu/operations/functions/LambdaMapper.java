package aeminium.gpu.operations.functions;

import aeminium.gpu.collections.properties.operations.Mapper;
import aeminium.gpu.utils.UniqCounter;

public abstract class LambdaMapper<I, O> implements Mapper<I, O>, GPUFunction {

	private String id = null;

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getSource() {
		return null;
	}

	public String getSourceComplexity() {
		return null;
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getFeatures() {
		return null;
	}
	
	/*  This method should be overridden by the Aeminium GPU Compiler */
	public String[] getParameters() {
		return new String[] { "input" };
	}

	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getId() {
		if (id == null)
			id = UniqCounter.getNewId();
		return id;
	}
	
	/* This method should be overridden by the Aeminium GPU Compiler */
	public String getOutputType() {
		return null;
	}
}
