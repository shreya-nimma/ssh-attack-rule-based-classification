import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import java.util.Random;

import weka.classifiers.rules.OneR;
import weka.classifiers.rules.Ridor;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.ConjunctiveRule;

import weka.classifiers.Evaluation;

/** Class to try out the Weka API.
	@author Shreya Nimma 2015A7PS0951H. 
*/
class MyWekaTest{
	/**	Builds different classifiers and
		cross-validates them.
	*/
	public static void main(String[] args) throws Exception{

		/* Reading in data from Dataset 1 */
		String filename = "test_files/test1.csv";
		DataSource source = new DataSource(filename);
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);

		Evaluation e = new Evaluation(data);

		/**** OneR Classifier ****/
		OneR oneR = new OneR();

		/* Cross-validating the classifier */
		e.crossValidateModel(oneR, data, 10, new Random());
		System.out.println(e.toSummaryString());

		/* Carving a test set out of the data */
		Instances train = data.trainCV(4, 1, new Random());
		Instances test = data.testCV(4, 1);

		/* Evaluating model with training/test datasets */
		oneR.buildClassifier(train);
		System.out.println(oneR.toString());
		e.evaluateModel(oneR, test);
		System.out.println(e.toSummaryString());

		/**** Ridor Classifier ****/
		Ridor ridor = new Ridor();
		ridor.buildClassifier(data);
		System.out.println(ridor.toString());

		/**** PART Classifier ****/
		PART part = new PART();
		part.buildClassifier(data);
		System.out.println(part.toString());

		/**** JRip Classifier ****/
		JRip jrip = new JRip();
		jrip.buildClassifier(data);
		System.out.println(jrip.toString());

		/**** Decision Table Classifier ****/
		DecisionTable dt = new DecisionTable();
		dt.buildClassifier(data);
		System.out.println(dt.toString());

		/**** Conjunctive Rule ****/
		ConjunctiveRule cr = new ConjunctiveRule();
		cr.buildClassifier(data);
		System.out.println(cr.toString());

	}
}