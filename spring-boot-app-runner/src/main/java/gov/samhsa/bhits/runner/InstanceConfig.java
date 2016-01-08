package gov.samhsa.bhits.runner;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstanceConfig {

    private int port;
    private Map<String, String> args;
    private Optional<Process> process;

    public InstanceConfig() {
        this.args = new HashMap<>();
        this.process = Optional.empty();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = ArgsUtils.filterPortArgs(args);
    }

    public Optional<Process> getProcess() {
        return process;
    }

    public void setProcess(Optional<Process> process) {
        this.process = process;
    }

    public void stopProcess() {
        this.process.ifPresent(Process::destroy);
        this.process = Optional.empty();
    }
}
