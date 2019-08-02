import org.jnetpcap.Pcap;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.format.*;
import org.jnetpcap.protocol.network.Ip4;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Test{

	public static void main(String args[]){

    	/* Opening up a pcap file as a test */
		StringBuilder errbuf = new StringBuilder();
		String fname = "../ssh_flows/merged_processed.pcap.TCP_1-30-20-148_13908_111-93-5-203_22.pcap";
		Pcap pcap = Pcap.openOffline(fname, errbuf);  

		/* Checking for errors in opening the pcap file */
		if (pcap == null) {  
		  System.err.printf("Error while opening device for capture: "  
		    + errbuf.toString());  
		  return;  
		}

		/* To calculate the duration of the flow */
		Date startTime = new Date();
		Date endTime;

		PcapPacket pkt = new PcapPacket(JMemory.POINTER);
		int first = 1;
		int numOfPackets = 0;
		int numOfOutgoingPackets = 0;

		/* Iterating through the packets */
		while (pcap.nextEx(pkt) == Pcap.NEXT_EX_OK) {

			/* Incrementing count of number of packets */
			numOfPackets++;

			/* Finding flow start time */
			if(first == 1){
				first--;
				startTime = new Date(pkt.getCaptureHeader().timestampInMillis());
			}

			/* Obtaining source IP address. */
			Ip4 ip = new Ip4();
			if(pkt.hasHeader(ip) == false){
				continue;
			}
			byte[] sIP = new byte[4];
			sIP = pkt.getHeader(ip).source();
			String sourceIP = FormatUtils.ip(sIP);
			if(sourceIP.equals("111.93.5.203")){
				numOfOutgoingPackets++;
			}
			System.out.println("Source IP is: " + sourceIP);

			/* Checking length of payload */
			Payload payloadHeader = new Payload();
			if(pkt.hasHeader(payloadHeader)){
				JBuffer payload = payloadHeader;
				System.out.println(payload);
			}
		}

		/* Finding the flow end time and duration */
		endTime = new Date(pkt.getCaptureHeader().timestampInMillis());
		double flowDuration = endTime.getTime() - startTime.getTime();
		System.out.println("Duration is: " + flowDuration);

		/* Finding inverse of density of packets */
		double milSecPerPacket = flowDuration/numOfPackets;
		double packetsPerMilSec = numOfPackets/flowDuration;
		System.out.println("Density: " + packetsPerMilSec + " Inverse: " + milSecPerPacket);

		/* Finding number of outgoing packets */
		System.out.println("Number of outgoing packets: " + numOfOutgoingPackets);

		pcap.close();
	}
}