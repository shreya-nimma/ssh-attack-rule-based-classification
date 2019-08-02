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

public class Main2{
	/***
		Program to extract the following new features from packet flows.

			1. Duration of flow (ms)
			2. Milliseconds per packet sent/received (Inverse of packet flow density)
			3. Net Bytes Sent. (+ve: net flow from client to server, -ve: net flow from server to client)
			
			4. Sum of differences between consecutive IATs (Burst Detection). (signed based on direction)

			5. Number of packets with payloads >= 90 Bytes.
			6. Number of packets with payloads >= 90 + 218 = 308 Bytes.

		@author Shreya Nimma 2015A7PS0951H
	*/

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

		/* Labels containing true class for each directory */
		String[] labels = {
			"nonsevere",
			"nonsevere",
			"nonsevere",
			"nonsevere",
			"severe",
			"nonsevere"
		};

		/* Names of the csv output files */
		String[] outputCSV = {
			"bf_sessions",
			"complete",
			"incomplete",
			"portScan",
			"severe_sessions",
			"success_np_sessions"
		};

		/* Iterating through each directory */
		for(int dindex = 0; dindex < directories.length; dindex++){

			/* Obtaining list of pcap files */
			File[] flowFiles = new File(directories[dindex]).listFiles();
			System.out.println(flowFiles.length);

			try{

				/* For writing to the CSV file */
				PrintWriter pw = new PrintWriter(outputCSV[dindex] + ".csv");
				StringBuilder sb = new StringBuilder();

				/* Writing the header of the CSV file */
				// sb.append(",F0");
				sb.append(",F1");							/* 1 */
				sb.append(",F2");							/* 2 */
				sb.append(",F3");							/* 3 */
				sb.append(",F4");							/* 4 */
				sb.append(",F5");							/* 6 */
				sb.append(",F6");							/* 7 */
				sb.append(",Label");
				sb.append("\n");
				pw.write(sb.toString());

				/* Iterating through all pcap files */
				for(int findex = 0; findex < flowFiles.length; findex++){

					if(findex%1000 == 0) System.out.println("..." + (findex*100/flowFiles.length) + "% done!"); 					/* Printing status of program every 1000 files. */

					/* Opening pcap file */
					StringBuilder errbuf = new StringBuilder();							
					Pcap pcap = Pcap.openOffline(flowFiles[findex].toString(), errbuf);
					if (pcap == null) {  												/* Checking for errors in opening the pcap file */		
					  System.err.printf("Error while opening device for capture: "  
					    + errbuf.toString());  
					  return;  
					}
					PcapPacket pkt = new PcapPacket(JMemory.POINTER);					/* For packet decoding */

					/* To calculate F1: duration of flow, F2: Inverse density */
					int first = 1;
					Date startTime = new Date();
					Date endTime = new Date();
					double duration;

					/* To calculate F2: Inverse density */
					long packetCount = 0;							/* Total number of packets. */

					/* To calculate F3: Net Bytes */
					long sumOutgoingPktsLength = 0;					/* Sum of outgoing packet lengths in the flow. */
					long sumIncomingPktsLength = 0;					/* Sum of incoming packet lengths in the flow. */
					String serverIP = "";
					long netBytes = 0;

					long numPayloadsGT90 = 0;
					long numPayloadsGT308 = 0;

					ArrayList<Date> timestamps = new ArrayList<Date>();

					/* Iterating through the packets */
					while (pcap.nextEx(pkt) == Pcap.NEXT_EX_OK) {

						packetCount++;								/* Incrementing total number of packets */
						
						/* Checking for IP header */
						Ip4 ip = new Ip4();
						if(pkt.hasHeader(ip) == false)
							continue;

						/* Obtaining timestamp and server IP of first packet */
						if(first == 1){
							first--;
							startTime = new Date(pkt.getCaptureHeader().timestampInMillis());
							byte[] dIP = new byte[4];
							dIP = pkt.getHeader(ip).destination();
							serverIP = FormatUtils.ip(dIP);
						}

						/* Obtaining length of payload */
						Payload payload = new Payload();
						long payloadSize = 0;
						if(pkt.hasHeader(payload)){
							payloadSize = payload.size();
							if(payloadSize > 90) numPayloadsGT90++;
							if(payloadSize > 308) numPayloadsGT308++;
						}

						/* Obtaining source IP address. */
						byte[] sIP = new byte[4];
						sIP = pkt.getHeader(ip).source();
						String sourceIP = FormatUtils.ip(sIP);

						/* Packet coming from server */
						long packetLength = pkt.getTotalSize();;
						if(sourceIP.equals(serverIP)){
							netBytes += packetLength;
						}
						else{
							netBytes -= packetLength;
						}

						/* Obtaining timestamp of last packet, calculating duration */
						endTime = new Date(pkt.getCaptureHeader().timestampInMillis());

						/* Adding this packet's timestamp. */
						timestamps.add(new Date(pkt.getCaptureHeader().timestampInMillis()));
					}
					duration = endTime.getTime() - startTime.getTime();					/* F1 */

					double msPerPackets = duration/packetCount;							/* F2 */
					double percentPktsGT90 = numPayloadsGT90*100/(double)packetCount;	/* F8 */
					double percentPktsGT308 = numPayloadsGT308*100/(double)packetCount;	/* F9 */

					long[] iats = new long[timestamps.size()-1];
					for(int i = 1; i < timestamps.size(); i++){
						iats[i-1] = timestamps.get(i).getTime() - timestamps.get(i-1).getTime();
					}
					long signedSumOfDiffInIATs = 0;
					long unsignedSumOfDiffInIATs = 0;

					for(int i = 1; i < iats.length; i++){
						long diff = iats[i] - iats[i-1];
						signedSumOfDiffInIATs += (diff);
						unsignedSumOfDiffInIATs += Math.abs(diff);
					}

					/* Forming the row to be written in the CSV file for this pcap file */
					sb.setLength(0);
					sb.append(", " + duration);						/* F1 */
					sb.append(", " + msPerPackets);					/* F2 */
					sb.append(", " + netBytes);						/* F3 */
					sb.append(", " + signedSumOfDiffInIATs);		/* F4 */

					sb.append(", " + numPayloadsGT90);				/* F5 */
					sb.append(", " + numPayloadsGT308);				/* F6 */
					sb.append(", " + labels[dindex]);
					sb.append("\n");
					pw.write(sb.toString());

					/* Closing pcap file */
					pcap.close();
				}

				/* Closing output file */
				pw.close();

			}
			catch(FileNotFoundException e){
				System.out.println("File already open!");
			}
		}
	}
}