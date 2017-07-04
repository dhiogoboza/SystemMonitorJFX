package br.ufrn.strproject2.utils;

import br.ufrn.strproject2.models.CpuInfo;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author dhiogoboza
 */
public class CoresManager {
	//private static long PREV_IDLE;      //CPU Idle time
    //private static long PREV_TOTAL;     //CPU Total time
    //private static final int CONSERVATIVE = 0;
    //private static final int AVERAGE = 1;
    //private static final int OPTIMISTIC = 2;
	
	private final HashMap<String, CpuInfo> cpus = new HashMap<>();

	public CoresManager() {
		//initCores();
	}
	
	/*private void initCores() {
        try (BufferedReader cpuReader = 
				new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")))
				) {
            String line;
			cpuReader.readLine();
			String name;
            while ((line = cpuReader.readLine()) != null) {
                String[] CPU = line.split("\\s+");
                if (CPU[0].startsWith("cpu")) {
					name = String.valueOf(CPU[0]);
					
					cpus.put(name, new CpuInfo(name));
					
                }
            }
        } catch (IOException | NumberFormatException e) {
            //todo
        }
	}*/
	
	public void getCPUProc() {
        try (BufferedReader cpuReader = 
				new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")))
				) {
            String line;
			cpuReader.readLine();
            while ((line = cpuReader.readLine()) != null) {
                String[] CPU = line.split("\\s+");
                if (CPU[0].startsWith("cpu")) {
                    CpuInfo cpuInfo = cpus.get(String.valueOf(CPU[0]));
					
                    long IDLE = Long.parseLong(CPU[4]);//Get the idle CPU time.
                    long TOTAL = Long.parseLong(CPU[1]) + Long.parseLong(CPU[2]) + Long.parseLong(CPU[3]) + Long.parseLong(CPU[4]);
                    // System.out.println("IDLE : " + IDLE);

                    long DIFF_IDLE = IDLE - cpuInfo.getIdle();
                    long DIFF_TOTAL = TOTAL - cpuInfo.getTotal();
                    double DIFF_USAGE = DIFF_TOTAL == 0 ? 0 : (1000.0 * (DIFF_TOTAL - DIFF_IDLE) / DIFF_TOTAL + 5.0) / 10.0;
                    //System.out.println("CPU: " + DIFF_USAGE + "%");

                    cpuInfo.setTotal(TOTAL);
                    cpuInfo.setIdle(IDLE);
					cpuInfo.setUsage(DIFF_USAGE);
                    //HashMap<String, Float> usageData2 = new HashMap<>();
                    //usageData2.put("cpu", (float) DIFF_USAGE);

                    //usageData.put(cpuName, usageData2);
					
					//System.out.println("-> " + cpuInfo.getName() + " = " + cpuInfo.getUsage());
                }
            }
        } catch (IOException | NumberFormatException e) {
            //todo
        }
    }
	
	public HashMap<String, CpuInfo> getCPUsInfo() {
		return cpus;
	}

	public void addCpuInfo(CpuInfo cpuInfo) {
		cpus.put(cpuInfo.getName(), cpuInfo);
	}
	
}
