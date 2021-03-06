package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class TestMap extends TestCase {

	private static int TEST_SIZE = 10000;
	private static int SMALL_TEST_SIZE = 10;

	public void performTestMapIntToInt(int size) {
		PList<Integer> example = new IntList();
		for (int i = 0; i < size; i++) {
			example.add(i);
		}
		PList<Integer> output = example
				.map(new LambdaMapper<Integer, Integer>() {

					@Override
					public Integer map(Integer input) {
						return 2 * input;
					}

					public String getSource() {
						return "return 2 * input;";
					}

				});

		for (int i=0; i<size; i++) {
			assertEquals(2 * i, output.get(i).intValue());
		}
	}

	public void testMapIntToInt() {
		performTestMapIntToInt(TEST_SIZE);
	}

	public void testSmallMapIntToInt() {
		performTestMapIntToInt(SMALL_TEST_SIZE);
	}

	public void testMapIntToFloat() {
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(i);
		}
		PList<Float> output = example.map(new LambdaMapper<Integer, Float>() {

			@Override
			public Float map(Integer input) {
				return ((float) input) / 2;
			}

			public String getSource() {
				return "return ((float)input)/2;";
			}

		});
		for (int i = 0; i < TEST_SIZE; i++) {
			assertEquals(((float) i) / 2, output.get(i).floatValue());
		}
	}

	public void testMapFloatToFloat() {
		PList<Float> example = new FloatList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(new Float(i) / 2);
		}
		PList<Float> output = example.map(new LambdaMapper<Float, Float>() {

			@Override
			public Float map(Float input) {
				return 2 * (float) input;
			}

			public String getSource() {
				return "return 2 * input;";
			}

		});
		for (int i = 0; i < TEST_SIZE; i++) {
			assertEquals((float) i, output.get(i).floatValue());
		}
	}

	public void testMapDoubleToFloat() {
		PList<Double> example = new DoubleList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(new Double(i) / 2);
		}
		PList<Float> output = example.map(new LambdaMapper<Double, Float>() {

			@Override
			public Float map(Double input) {
				return (float) (2 * input);
			}

			public String getSource() {
				return "return ((float) 2 * input);";
			}

		});
		for (int i = 0; i < TEST_SIZE; i++) {
			assertEquals((float) i, output.get(i).floatValue());
		}
	}

	public void testMapCharToChar() {
		PList<Character> example = new CharList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add('h');
		}
		PList<Character> output = example
				.map(new LambdaMapper<Character, Character>() {

					@Override
					public Character map(Character ch) {
						return 'f';
					}

					public String getSource() {
						return "return 'f';";
					}

				});
		for (int i = 0; i < TEST_SIZE; i++) {
			assertTrue(output.get(i).compareTo('f') == 0);
		}
	}

}
