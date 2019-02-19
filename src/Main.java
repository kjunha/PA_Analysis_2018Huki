import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		System.out.println("input file: ");
		String filename = s.nextLine();
		PAData session = new PAData(filename);
		System.out.println("Set start and end time.");
		System.out.print("Start: ");
		String st = s.nextLine();
		System.out.print("End: ");
		String en = s.nextLine();
		session.setTimeframe(Integer.parseInt(st), Integer.parseInt(en));
		session.plotAttendance();
		System.out.println("Under what stdev you will consider unattended?");
		String stdcut = s.nextLine();
		System.out.println("Set your window size: ");
		String winsize = s.nextLine();
		session.cleanUnattended(Double.parseDouble(stdcut), Integer.parseInt(winsize));
		System.out.println("Valid Participant: " + session.getValidDial());
		session.get5Maxmnt(Double.parseDouble(st));
		System.out.println("-----------------------------");
		session.get5Minmnt(Double.parseDouble(st));	
		session.exportSessionReview();
		System.out.println("Export Successful!");
		s.close();
	}
}
