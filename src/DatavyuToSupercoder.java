import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class DatavyuToSupercoder {
	
	/**
	 * Open a given file and reads each entry one line at a time, creating Dayavyu objects with the attributes of 
	 * ordinal, onset, offset, and code. Insert each object into a list of Datavyu objects.
	 * @param fileName - the name of the csv file
	 * @return - a list of Datavyu objects which represent a single entry in the csv file
	 */
	public static List<DatavyuObject> readFile(String fileName) {
		List<DatavyuObject> lines = new LinkedList<>();
		
		try {
		    BufferedReader reader = new BufferedReader(new FileReader(fileName));
		    String line;
		    reader.readLine();
            while ((line = reader.readLine()) != null) {    
                String[] parts = line.split(","); // separate the line by comma
                int ordinal = Integer.parseInt(parts[0]); //convert string to integer
                int onset = Integer.parseInt(parts[1]); //convert string to integer
                int offset = Integer.parseInt(parts[2]);//convert string to integer
                String code = parts[3].replace("\"", "");//remove quotation from string
                DatavyuObject newData = new DatavyuObject(ordinal,onset,offset,code); //create object
                lines.add(newData);
            }            
            reader.close();   
		}
		catch (Exception e) {            
            System.err.format("Exception occurred trying to read '%s'.", fileName);            
            e.printStackTrace();        
        }
		return lines;
	}
	
	/**
	 * Given a list of data, list of start times, and file name, this function reformats the data 
	 * and writes it into a new csv file. The csv file will contain the updated onset/offset in frames
	 * and the start times of each trial given in frames, millisecond, and elapsed time. 
	 * @param dataList - a list of Datavyu objects which represents each entry in a csv file
	 * @param startTimes - a list of start times for each trial
	 * @param filename - name of the previous file that was read
	 */
	public static void writeFile(List<DatavyuObject> dataList, List<DatavyuObject> startTimes,String filename) {
		try (PrintWriter writer = new PrintWriter(new File ("Output/"+"OUTPUT_"+filename))) {
		    StringBuilder sb = new StringBuilder();
		    int numTrials = startTimes.size();
		    int trialCount = 0; //keeps track of trial start times
		    
		    sb.append("Reformatted Data (in frames),,,,,Trial Start Times"); // titles 
		    sb.append('\n'); //new line
		    sb.append("Code,Onset,Offset");
		    sb.append(",,,"); // spacing between two data entries 
		    sb.append("Trial Number,");
		    sb.append("Start Time (in frames - Supercoder),");
		    sb.append("Start Time (in milliseconds - Datavyu CSVs),");
		    sb.append("Start Time (in elapsed time - Datavyu coding)");
		    sb.append('\n');
		    
		    for(DatavyuObject d : dataList) { //iterating through all data in datalist
		    	sb.append(d.getCode()+','); //add the code into csv file
		    	double onset = d.getOnset();
		    	double offset = d.getOffset();
		    	int newOnset = (int)Math.round((onset/1000*29.97)); //create new onset in frames
		    	sb.append(Integer.toString(newOnset) +','); //add to csv file
		    	if(offset == 0) {
		    		sb.append(" "); //set offset to blank if it is 0
		    	}
		    	else {
		    		int newOffset = (int)Math.round(offset/1000*29.97); //create new offset in frames
		    		sb.append(Integer.toString(newOffset)); //add to csv file
		    	}
		    	sb.append(",,,"); //spacing between two data entries
		    	if(trialCount < numTrials) { //for inserting start times for each trial
		    		sb.append(Integer.toString(trialCount+1)+','); //add trial number to csv file
		    		DatavyuObject cur = startTimes.get(trialCount);
		    		double onset2 = cur.getOnset();
		    		int newOnset2 = (int)Math.round((onset2/1000*29.97)); //create new start time in frames
		    		int millis = (int)Math.round(((double)newOnset2/29.97*1000)); //create new start time in milliseconds
			    	sb.append(Integer.toString(newOnset2) +',');//add to csv file
			    	sb.append(Integer.toString(millis) +',');//add to csv file
			    	
			    	int minutes = millis /(1000 * 60);//get the number of minutes
			    	int seconds = millis / 1000 % 60;//get the number of seconds
			    	int milli  = millis % 1000;//get the number of milliseconds
			    	String formatted = String.format("%02d:%02d.%03d", minutes, seconds, milli);//reformat as elapsed time
			    	
			    	sb.append(formatted +',');//add elapsed time to csv file
			    	trialCount++;

		    		
		    	}
		    	sb.append('\n');
		    }
		    
		    writer.write(sb.toString());
			System.out.println("File has been converted and is placed in the Output directory.");
			
		}
		catch(FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
	/**
	 * Get the start time of each trial in the csv file
	 * @param dataList - list of Datavyu objects which represent each entry in the csv file
	 * @return - list of Datavyu objects that are the start time
	 */
	public static List<DatavyuObject> getStartTimes(List<DatavyuObject> dataList){
		List<DatavyuObject> startTimes = new LinkedList<>();
		for(DatavyuObject d : dataList) {
			String code = d.getCode();
			if(code.equals("B")) {
				startTimes.add(d);
			}
		}
		return startTimes;
		
	}
	/**
	 * Get all the file names from the Input folder
	 * @param folder - Input folder
	 * @return - a list of all file names within the Input folder
	 */
	public static List<String> getFiles(File folder){
		List<String> fileList = new ArrayList<>();
		for(File fileEntry : folder.listFiles()) {
			fileList.add(fileEntry.getName());
		}
		return fileList;
	}
	
	
	
	public static void main(String[] args) {
		File folder = new File("Input/"); //Input folder
        List<String> fileList = getFiles(folder); //get all file names within the Input folder
        for(String file : fileList) { //for each file name within the Input folder
        	String filePath = String.format("Input/%s",file); 
        	List<DatavyuObject> dataList = readFile(filePath);//read file
            List<DatavyuObject> startTimes = getStartTimes(dataList);//get start time
            writeFile(dataList,startTimes,file);//write new csv file with reformatted data
        }
	}

}
