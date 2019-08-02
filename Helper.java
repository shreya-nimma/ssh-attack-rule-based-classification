import org.jnetpcap.Pcap;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.format.*;
import org.jnetpcap.packet.Payload;
import org.jnetpcap.protocol.network.Ip4;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.*;

class Helper{
	public static void main(String args[]){

		/* Directories of all SSH Flows */
		String[] directories = {
			"../SSH_Segregated/bf_sessions",
			"../SSH_Segregated/complete",
			"../SSH_Segregated/incomplete",
			"../SSH_Segregated/portScan",
			"../SSH_Segregated/severe_sessions",
			"../SSH_Segregated/success_np_sessions"
		};
		
		long[] sums = new long[6];			/* Calculates sum of payloads in a directory. */
		long[] squaredSums = new long[6];
		long[] numPackets = new long[6];	/* Contains total number of packets in each directory. */
		double[] meanPayload = new double[6];
		double[] variances = new double[6];
		long totalPackets = 0;				/* Total number of flows/pcap files in the entire dataset. */

		/* Iterating through directories */
		for(int i = 0; i < directories.length; i++){

			/* Printing directory name */
			System.out.println("In directory: " + directories[i]);

			sums[i] = 0;
			squaredSums[i] = 0;
			meanPayload[i] = 0;
			numPackets[i] = 0;

			File[] flowFiles = new File(directories[i]).listFiles();

			/* Iterating through all pcap files in a directory */
			for(int j = 0; j < flowFiles.length; j++){

				if(j % 1000 == 0) System.out.println("...");

				/* Opening pcap file */
				StringBuilder errbuf = new StringBuilder();
				Pcap pcap = Pcap.openOffline(flowFiles[i].toString(), errbuf);

				/* Checking for errors in opening the pcap file */
				if (pcap == null) {  				
				  System.err.printf("Error while opening device for capture: "  
				    + errbuf.toString());  
				  return;  
				}

				/* For packet decoding */
				PcapPacket pkt = new PcapPacket(JMemory.POINTER);

				/* Iterating through the packets */
				while (pcap.nextEx(pkt) == Pcap.NEXT_EX_OK) {

					numPackets[i] += 1;					/* Incrementing packet count */
					totalPackets += 1;

					/* If packet has payload */
					Payload payload = new Payload();
					long payloadSize = 0;
					if(pkt.hasHeader(payload)){
						payloadSize = payload.size();
						sums[i] += payloadSize			;				/* Summing payload size */
						squaredSums[i] += (payloadSize * payloadSize);	/* Summing the square of payload size */
					}
				}

				pcap.close();
			}

			meanPayload[i] = sums[i]/(double)numPackets[i];
			variances[i] = (squaredSums[i]/(double)numPackets[i]) - (meanPayload[i] * meanPayload[i]);
			System.out.println("Avg: " + meanPayload[i]);
			System.out.println("SD: " + Math.sqrt(variances[i]));
		}

		/* Calculating average of all payload lengths. */
		double avg = 0;
		for(int i = 0; i < directories.length; i++)
			avg += (double)sums[i]/totalPackets;
		double variance = 0;
		for(int i = 0; i < directories.length; i++)
		 	variance += (double)squaredSums[i]/totalPackets;
		variance -= (avg * avg);

		System.out.println("Total packets: " + totalPackets);
		System.out.println("Average payload length per packet is: " + avg + "\n");
		System.out.println("SD: " + Math.sqrt(variance));
	}
}