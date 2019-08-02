import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import java.util.Random;

import weka.classifiers.rules.OneR;
import weka.classifiers.rules.Ridor;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.ConjunctiveRule;

import java.io.*;

import weka.classifiers.Evaluation;

/** Class to print out results of classification for given datasets.
	@author Shreya Nimma 2015A7PS0951H. 
*/
class Classifier{
	/**	Builds different classifiers and
		cross-validates them.
	*/
	public static void main(String[] args) throws Exception{

		String dataDirectory = "data";
		File[] dataFiles = new File(dataDirectory).listFiles();

		/* Picking a random training file to display the rules of. */
		Random r = new Random();
		int random = r.nextInt(dataFiles.length + 1);

		/* Opening the dataset. */
		// System.out.println(dataFiles[trainindex].getName());
		DataSource source = new DataSource("data/" + dataFiles[random].getName());
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);

		/* Initializing models */
		OneR oneR = new OneR();
		Ridor ridor = new Ridor();
		PART part = new PART();
		JRip jrip = new JRip();
		DecisionTable dt = new DecisionTable();
		ConjunctiveRule cr = new ConjunctiveRule();

		/* Building models on training set data */
		oneR.buildClassifier(data);
		ridor.buildClassifier(data);
		part.buildClassifier(data);
		jrip.buildClassifier(data);
		dt.buildClassifier(data);
		cr.buildClassifier(data);

		Evaluation e1 = new Evaluation(data);
		Evaluation e2 = new Evaluation(data);
		Evaluation e3 = new Evaluation(data);
		Evaluation e4 = new Evaluation(data);
		Evaluation e5 = new Evaluation(data);
		Evaluation e6 = new Evaluation(data);

		/* Iterating over all datasets. */
		for(int findex = 1; findex < dataFiles.length; findex++){

			/* Opening the dataset. */
			source = new DataSource("data/" + dataFiles[findex].getName());
			Instances test = source.getDataSet();
			test.setClassIndex(data.numAttributes() - 1);

			/* Classifying test data using models */
			e1.evaluateModel(oneR, test); 
			e2.evaluateModel(ridor, test);
			e5.evaluateModel(part, test);
			e6.evaluateModel(jrip, test);
			e3.evaluateModel(dt, test);
			e4.evaluateModel(cr, test);

		}

		System.out.println(oneR.toString());
		System.out.println(e1.toSummaryString());

		System.out.println(ridor.toString());
		System.out.println(e2.toSummaryString());

		System.out.println(part.toString());
		System.out.println(e3.toSummaryString());

		System.out.println(jrip.toString());
		System.out.println(e4.toSummaryString());

		System.out.println(dt.toString());
		System.out.println(e5.toSummaryString());

		System.out.println(cr.toString());
		System.out.println(e6.toSummaryString());


		System.out.println("Generated for dataset: " + random);
	}
}