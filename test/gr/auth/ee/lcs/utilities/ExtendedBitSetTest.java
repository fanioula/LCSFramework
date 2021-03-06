/*
 *	Copyright (C) 2011 by Allamanis Miltiadis
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in
 *	all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *	THE SOFTWARE.
 */
/**
 * 
 */
package gr.auth.ee.lcs.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>
 * Title: ExtendedBitSetTests
 * </p>
 * <p>
 * Description: A JUnit test case for ExtendedBitSet
 * </p>
 * <p>
 * Aristotle University of Thessaloniki
 * </p>
 * 
 * @author Miltos Allamanis
 * @version 0.2
 * @see java.util.BitSet
 */
public class ExtendedBitSetTest {

	@Test
	public void testAndExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("111111011");
		final ExtendedBitSet testC = testA.and(testB);
		assertTrue(testC.toString().equals("100100011"));
	}

	@Test
	public void testAndIntExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("1010");
		final ExtendedBitSet testC = testA.and(5, testB);
		assertTrue(testC.toString().equals("100000111"));
	}

	@Test
	public void testClearInt() {
		final ExtendedBitSet test = new ExtendedBitSet("10000001");
		test.clear(0);
		assertTrue(test.toString().equals("10000000"));
		test.set(3);
		test.clear(7);
		assertTrue(test.toString().equals("00001000"));

		assertTrue(test.size() == 8);
	}

	@Test
	public void testClearIntInt() {
		final ExtendedBitSet test = new ExtendedBitSet("111111111");
		test.clear(2, 0);
		assertTrue(test.toString().equals("111111111"));
		test.clear(2, 2);
		assertTrue(test.toString().equals("111110011"));
	}

	@Test
	public void testClone() {
		final ExtendedBitSet test = new ExtendedBitSet("0101");
		assertTrue(test.toString().equals("0101"));
		assertTrue(test.getIntAt(0) == 5);
	}

	@Test
	public void testCreepLeft() {

	}

	@Test
	public void testCreepRight() {

	}

	@Test
	public void testDeleteSubSet() {

	}

	@Test
	public void testDoubleValue() {

	}

	@Test
	public void testFloatValue() {

	}

	@Test
	public void testGet() {
		final ExtendedBitSet test = new ExtendedBitSet("10010001");
		assertTrue(test.get(0));
		assertFalse(test.get(1));
		assertTrue(test.get(7));
		assertFalse(test.get(20));
		// TODO: Size has been actually increased to 21, this is a bug...
	}

	@Test
	public void testGetByteAtInt() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		Byte result = testA.getByteAt(3);
		assertTrue(result == 36);
		result = testA.getByteAt(0);
		assertTrue(result == 39);

	}

	@Test
	public void testGetByteAtIntInt() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final Byte result = testA.getByteAt(3, 3);
		assertTrue(result == 4);
	}

	@Test
	public void testGetCharAtIntInt() {
		final ExtendedBitSet testA = new ExtendedBitSet("10000101");
		assertTrue(testA.getCharAt(1, 7) == 'B');
	}

	@Test
	public void testGetDoubleAt() {

	}

	@Test
	public void testGetFloatAt() {

	}

	@Test
	public void testGetIntAtIntInt() {

	}

	@Test
	public void testGetLongAtInt() {

	}

	@Test
	public void testGetLongAtIntInt() {

	}

	@Test
	public void testGetShortAtIntInt() {

	}

	@Test
	public void testGetSubSet() {

	}

	@Test
	public void testInsertSubSet() {

	}

	@Test
	public void testInvertInt() {
		final ExtendedBitSet test = new ExtendedBitSet("111111111");
		test.invert(0);
		test.invert(0);
		assertTrue(test.toString().equals("111111111"));
	}

	@Test
	public void testInvertIntInt() {
		final ExtendedBitSet test = new ExtendedBitSet("111111111");
		test.invert(2, 5);
		test.invert(2, 5);
		assertTrue(test.toString().equals("111111111"));
	}

	@Test
	public void testLongValue() {

	}

	@Test
	public void testOrExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("110111011");
		final ExtendedBitSet testC = testA.or(testB);
		assertTrue(testC.toString().equals("110111111"));
	}

	@Test
	public void testOrIntExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("1010");
		final ExtendedBitSet testC = testA.and(5, testB);
		assertTrue(testC.toString().equals("100000111"));
	}

	@Test
	public void testRotateLeft() {

	}

	@Test
	public void testRotateRight() {

	}

	@Test
	public void testSetByteAtIntByte() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		testA.setByteAt(2, (byte) 2);
		assertTrue(testA.toString().equals("0000001011"));
	}

	@Test
	public void testSetByteAtIntIntByte() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		testA.setByteAt(2, 2, (byte) 2);
		assertTrue(testA.toString().equals("100101011"));
	}

	@Test
	public void testSetCharAtIntIntChar() {
		final ExtendedBitSet testA = new ExtendedBitSet("10000101");
		testA.setCharAt(1, 7, 'C');
		assertTrue(testA.toString().equals("10000111"));
	}

	@Test
	public void testSetDoubleAt() {

	}

	@Test
	public void testSetFloatAt() {

	}

	@Test
	public void testSetInt() {
		final ExtendedBitSet test = new ExtendedBitSet("10010001");
		test.set(0);
		assert (test.toString().equals("10010001"));
		test.set(1);
		assertTrue(test.get(1));
		assert (test.toString().equals("10010011"));
		test.set(10);
		assert (test.toString().equals("10010010011"));
	}

	@Test
	public void testSetIntAtIntInt() {

	}

	@Test
	public void testSetIntAtIntIntInt() {

	}

	@Test
	public void testSetIntInt() {
		final ExtendedBitSet test = new ExtendedBitSet("10000001");
		test.set(2, 3);
		assertTrue(test.toString().equals("10011101"));
		test.set(10, 3);
		assert (test.toString().equals("1110010011101"));
	}

	@Test
	public void testSetLongAtIntIntLong() {

	}

	@Test
	public void testSetLongAtIntLong() {

	}

	@Test
	public void testSetShortAtIntIntShort() {

	}

	@Test
	public void testSetSize() {
		final ExtendedBitSet test = new ExtendedBitSet("10010001");
		test.setSize(15);
		assertTrue(test.toString().equals("000000010010001"));
		test.setSize(5);
		assertTrue(test.toString().equals("10001"));
	}

	@Test
	public void testSetSubSet() {

	}

	@Test
	public void testShiftLeft() {

	}

	@Test
	public void testShiftRightSigned() {

	}

	@Test
	public void testShiftRightUnsigned() {

	}

	@Test
	public void testSize() {
		final ExtendedBitSet test = new ExtendedBitSet("10000001");
		assertTrue(test.size() == 8);
	}

	@Test
	public void testSwapSubSet() {

	}

	@Test
	public void testToString() {
		final ExtendedBitSet test = new ExtendedBitSet("20000001");
		assertTrue(test.toString().equals("10000001"));
	}

	@Test
	public void testXorExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("110111011");
		final ExtendedBitSet testC = testA.xor(testB);
		assertTrue(testC.toString().equals("010011100"));
	}

	@Test
	public void testXorIntExtendedBitSet() {
		final ExtendedBitSet testA = new ExtendedBitSet("100100111");
		final ExtendedBitSet testB = new ExtendedBitSet("1010");
		final ExtendedBitSet testC = testA.xor(5, testB);
		assertTrue(testC.toString().equals("001100111"));
	}

}
