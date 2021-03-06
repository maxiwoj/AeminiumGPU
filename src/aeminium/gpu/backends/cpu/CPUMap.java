package aeminium.gpu.backends.cpu;

import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class CPUMap<I,O> extends CPUGenericKernel {
	
	protected PList<I> input;
	protected PList<O> output;
	protected LambdaMapper<I, O> mapFun;
	
	protected String outputType;
	
	
	public CPUMap(PList<I> input, LambdaMapper<I, O> mapFun) {
		this.input = input;
		this.mapFun = mapFun;
		outputType = mapFun.getOutputType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		output = (PList<O>) CollectionFactory.listFromType(outputType);
		for (int i=start; i<end; i++) {
			output.add(mapFun.map(input.get(i)));
		}
	}

	@Override
	public void waitForExecution() {
	}
	
	public PList<O> getOutput() {
		return output;
	}
	
	public PList<I> getInput() {
		return input;
	}


	public void setInput(PList<I> input) {
		this.input = input;
	}


	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}


	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}


	public void setOutput(PList<O> output) {
		this.output = output;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

}
