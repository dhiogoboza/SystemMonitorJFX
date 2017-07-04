package br.ufrn.strproject2.models;

/**
 *
 * @author dhiogoboza
 */
public class HostProcess {

    private String user;
    private String pid;
    private String command;
    private String CPU;
    private String MEM;
    private String start;
    private String time;
	private String readableName;

    public HostProcess() {
        
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCPU() {
        return CPU;
    }

    public void setCPU(String CPU) {
        this.CPU = CPU;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMEM() {
        return MEM;
    }

    public void setMEM(String MEM) {
        this.MEM = MEM;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

	public String getReadableName() {
		return readableName;
	}

	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}
}
