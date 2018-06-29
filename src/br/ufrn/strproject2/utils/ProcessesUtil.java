package br.ufrn.strproject2.utils;

import br.ufrn.strproject2.models.HostProcess;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dhiogoboza
 */
public class ProcessesUtil {
    
    public static void getProcesses(List<HostProcess> processes, HashMap<String, HostProcess> monitoredProcesses, boolean all) { 
        getProcesses(processes, monitoredProcesses, all, null);
    }
	
	public static void getProcesses(List<HostProcess> processes, HashMap<String, HostProcess> monitoredProcesses, boolean all, String filter) { 
		try {
			String processLine;
			Process p = Runtime.getRuntime().exec(all? "ps -aux" : "ps -ux");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            HostProcess hostProcess;
            HostProcess oldProcess;
            input.readLine();
			
			int j;
			int i;
			String[] hostProcessData;
			StringBuilder data;
			String command, readableName;
			String[] nameSplit;
			
			while ((processLine = input.readLine()) != null) {
                j = 0;
                hostProcessData = new String[10];
                for (i = 0; i < 10; i++) {
                    data = new StringBuilder();
                    while (processLine.length() > j && processLine.charAt(j) != ' ') {
                        data.append(processLine.charAt(j));
                        j++;
                    }
                    hostProcessData[i] = data.toString();
                    
                    while (processLine.length() > j && processLine.charAt(j) == ' ') {
                        j++;
                    }
                }
                
				command = processLine.substring(j - 1, processLine.length());
				nameSplit = command.split("/");
				readableName = nameSplit[nameSplit.length - 1];
				nameSplit = readableName.split(" -");
				readableName = nameSplit[0].trim();
				
				if (filter == null || readableName.contains(filter)) {
                    String key = hostProcessData[1] + command;
                    
                    if (monitoredProcesses.containsKey(key)) {
                        hostProcess = monitoredProcesses.get(key);
                    } else {
                        hostProcess = new HostProcess();
                        hostProcess.setCommand(command);
                        hostProcess.setReadableName(readableName);
                        hostProcess.setUser(hostProcessData[0]);
                        hostProcess.setPID(hostProcessData[1]);
                        hostProcess.setStart(hostProcessData[8]);
                    }
					
					hostProcess.setCPU(hostProcessData[2]);
					hostProcess.setMEM(hostProcessData[3]);
					hostProcess.setTime(hostProcessData[9]);

					processes.add(hostProcess);
				}
			}
            
			input.close();
		} catch (Exception err) {
			Logger.getLogger(ProcessesUtil.class.getName()).log(Level.SEVERE, null, err);
		}
	}
	
	public static void killProcess(HostProcess p) {
		sendSignal(p.getPID(), "SIGKILL");
	}
	
	public static void pauseProcess(HostProcess p) {
		sendSignal(p.getPID(), "SIGSTOP");
	}
	
	public static void continueProcess(HostProcess p) {
		sendSignal(p.getPID(), "SIGCONT");
	}
	
	public static void sendSignal(String processId, String signal) {
		try {
			Runtime rt = Runtime.getRuntime();
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				// TODO
			} else {
			   rt.exec("kill -" + signal + " " + processId);
			}
		} catch (IOException ex) {
			Logger.getLogger(ProcessesUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    public static void addMemoryLog(HostProcess hp) {
        BufferedReader input = null;
        try {
			Runtime rt = Runtime.getRuntime();            
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				// TODO
			} else {
                Process p = Runtime.getRuntime().exec("cat /proc/" + hp.getPID() + "/status");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String processOutput;
                
                while ((processOutput = input.readLine()) != null) {
                    if (processOutput.startsWith("VmRSS:")) {
                        String memoryUsage = processOutput.replace("VmRSS:", "");
                        // TODO: kB, MB, GB ...
                        memoryUsage = memoryUsage.replace("kB", "").trim();

                        hp.addMemoryLog(Long.parseLong(memoryUsage));
                    }
                }
                
			}
		} catch (Throwable ex) {
			Logger.getLogger(ProcessesUtil.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    
                }
            }
        }
    }
    
}
