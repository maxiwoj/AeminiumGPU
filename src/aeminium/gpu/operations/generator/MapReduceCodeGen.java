package aeminium.gpu.operations.generator;

import java.io.File;
import java.util.HashMap;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.operations.MapReduce;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class MapReduceCodeGen {
	private MapReduce op;
	private String id;

	public MapReduceCodeGen(MapReduce op) {
		this.op = op;
		id = op.getMapFun().getId() + "_" + op.getReduceFun().getId();
	}
	
	public String getMapLambdaSource() {
		HashMap<String,String> mapping = new HashMap<String,String>();
		mapping.put("input_type", BufferHelper.getCLTypeOf(op.getInputType()));
		mapping.put("output_type", BufferHelper.getCLTypeOf(op.getOutputType()));
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("source", op.getMapFun().getSource());
		Template t = new Template(new TemplateWrapper("opencl/MapLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getReduceLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("input_type",
				BufferHelper.getCLTypeOf(op.getOutputType()));
		mapping.put("output_type",
				BufferHelper.getCLTypeOf(op.getOutputType()));
		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("source", op.getReduceFun().getSource());
		Template t = new Template(new File(
				"templates/opencl/ReduceLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getReduceKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();

		mapping.put("input_type",
				BufferHelper.getCLTypeOf(op.getInputType()));
		mapping.put("output_type",
				BufferHelper.getCLTypeOf(op.getOutputType()));
		
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_lambda_def", getMapLambdaSource());
		
		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("reduce_lambda_def", getReduceLambdaSource());
		
		mapping.put("reduce_kernel_name", getReduceKernelName());
		mapping.put("other_sources", op.getOtherSources());

		mapping.put("seed_source", op.getOpenCLSeed());

		Template t = new Template(new File("templates/opencl/MapReduceKernel.clt"));
		return t.apply(mapping);
	}
	
	public String getMapLambdaName() {
		return "map_function_" + op.getMapFun().getId();
	}

	public String getReduceLambdaName() {
		return "reduce_function_" + op.getReduceFun().getId();
	}

	public String getReduceKernelName() {
		return "reduce_kernel_" + id;
	}

}