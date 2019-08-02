import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.*;

class DatasetMaker{
	public static void main(String args[]){

		try{
			/* Storing the severe dataset in an array of Strings */
			ArrayList<String> severe = new ArrayList<String>();
			String severefile = "results/severe_sessions.csv";

			BufferedReader br = new BufferedReader(new FileReader(severefile));
			String line;
			while((line = br.readLine()) != null){
				severe.add(line);
			}
			severe.remove(0);

			int datasetNum = 0;

			/* Reading the list of non-severe rows */
			ArrayList<String> nonsevere = new ArrayList<String>();
			String nonseverefile = "results/all_non_severe.csv";
			br.close();

			br = new BufferedReader(new FileReader(nonseverefile));
			int lineNum = 0;
			while((line = br.readLine()) != null){
				lineNum++;
				nonsevere.add(line);

				// if(lineNum == 301) break;

				/* Every 300 lines make a new file */
				if(lineNum % 277 == 0){
					ArrayList<String> currDataset = new ArrayList<String>();

					/* Shuffling rows */
					currDataset.addAll(nonsevere);
					currDataset.addAll(severe);
					Collections.shuffle(currDataset);

					/* Writing to file */
					PrintWriter pw = new PrintWriter("data/"+ datasetNum + ".csv");
					datasetNum++;
					pw.write("F1, F2, F3, F4, F5, F6, Label\n");
					for(String str: currDataset){
						pw.write(str);
						pw.write("\n");
					}
					pw.close();

					/* Clearing previous rows */
					nonsevere.clear();
				}
			}

			/* Writing the remaining entries to a dataset */
			PrintWriter pw = new PrintWriter("data/"+ datasetNum + ".csv");
			pw.write("F1, F2, F3, F4, F5, F6, Label\n");
			ArrayList<String> remaining = new ArrayList<String>();
			for(int i = 0; i < nonsevere.size(); i++){
				remaining.add(nonsevere.get(i));
				if(i < severe.size())
					remaining.add(severe.get(i));
			}
			Collections.shuffle(remaining);
			for(String str: remaining){
				pw.write(str);
				pw.write("\n");
			}
			pw.close();
			br.close();
		}
		catch(Exception e){
			System.out.println("Something went wrong!");
		}
	}
}