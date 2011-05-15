package aeminium.gpu.benchmarker;

import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.functions.LambdaMapper;

public class Benchmarker {
	
	int times = 30;
	public int[] sizes = new int[] {
			10, 100, 1000, 10000, 100000, 1000000, 10000000
	};
	
	GPUDevice dev = new DefaultDeviceFactory().getDevice();
	RecordTracker tracker = new RecordTracker();
	
	public static void main(String[] args) {
		Benchmarker b = new Benchmarker();
		b.run();
	}
	
	public void run() {
		System.setProperty("ForceGPU", "yes");
		executeExprMultipleSizes("unit", "input");
		executeExprMultipleSizes("plus", "input + input");
		executeExprMultipleSizes("minus", "input - input");
		executeExprMultipleSizes("mul", "input * input");
		executeExprMultipleSizes("div", "input / input");
		executeExprMultipleSizes("eq", "(input == input) ? 1.0 : 2.0");
		executeExprMultipleSizes("sin", "sin(input)");
		executeExprMultipleSizes("asin", "asin(input)");
		executeExprMultipleSizes("cos", "cos(input)");
		executeExprMultipleSizes("acos", "acos(input)");
		executeExprMultipleSizes("tan", "cos(input)");
		executeExprMultipleSizes("atan", "atan(input)");
		executeExprMultipleSizes("pow", "pow(input, 2)");
		executeExprMultipleSizes("log", "log(input)");
		executeExprMultipleSizes("floor", "floor(input)");
		executeExprMultipleSizes("ceil", "ceil(input)");

		finish();
		System.clearProperty("ForceGPU");
	}
	
	private void finish() {
		tracker.makeAverages();
	}

	public void executeExprMultipleSizes(String name, String expr) {
		PList<Float> input;
		for (int size : sizes) {
			input = new FloatList();
			for (int i = 0; i < size; i++) {
				input.add(new Float(i));
			}
			executeExprMultipleTimes(name, expr, input);
		}
	}
	
	
	public void executeExprMultipleTimes(String name, String expr, PList<Float> input) {
		System.out.println("Op:" + name + ", Size:" + input.size());
		LoggerTimer logger = new LoggerTimer(times, input.size(), name, tracker);
		for (int i = 0; i < times; i++) {
			executeExpr(name, expr, input, logger);
			System.gc();
		}
	}
	
	public void executeExpr(String name, String expr, PList<Float> input, LoggerTimer logger) {
		Map<Float, Float> op = new Map<Float, Float>(getLambda(expr), input, dev);
		op.setLogger(logger);
		dev.execute(op);		
	}
	
	public LambdaMapper<Float, Float> getLambda(final String expr) {
		return new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return input;
			}
			
			@Override
			public String getSource() {
				return "return (" + expr + ");";
			} 

		};
	}
}
