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
package gr.auth.ee.lcs.implementations.global;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.ArffTrainTestLoader;
import gr.auth.ee.lcs.FoldEvaluator;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.io.IOException;

/**
 * Trains any of the MlTypes of LCS
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class AllMlTypes {

	private static AbstractLearningClassifierSystem getLCS(String name)
			throws IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		return (AbstractLearningClassifierSystem) (Class.forName(name)
				.newInstance());

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		final String lcsType = SettingsLoader.getStringSetting("lcsType", "");
		final AbstractLearningClassifierSystem lcs = getLCS(lcsType);

		final String file = SettingsLoader.getStringSetting("filename", "");
		final String testFile = SettingsLoader.getStringSetting("testFile", "");

		if (testFile.equals("")) {
			final FoldEvaluator loader = new FoldEvaluator(10, lcs, file);
			loader.evaluate();
		} else {
			final ArffTrainTestLoader loader = new ArffTrainTestLoader(lcs);
			loader.loadInstancesWithTest(file, testFile);
			loader.evaluate();

		}

	}
}
