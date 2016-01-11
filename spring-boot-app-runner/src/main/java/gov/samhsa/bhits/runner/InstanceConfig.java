package gov.samhsa.bhits.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstanceConfig {

    private int port;
    private Map<String, String> args;
    private Optional<Process> process;
    private Optional<Thread> thread;
    private Optional<RunnableInstance> runnableInstance;

    public InstanceConfig() {
        this.args = new HashMap<>();
        initProcess();
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

    public Optional<Thread> getThread() {
        return thread;
    }

    public void setThread(Optional<Thread> thread) {
        this.thread = thread;
    }

    public Optional<RunnableInstance> getRunnableInstance() {
        return runnableInstance;
    }

    public void setRunnableInstance(Optional<RunnableInstance> runnableInstance) {
        this.runnableInstance = runnableInstance;
    }

    public void stopProcess() {
        this.process.ifPresent(Process::destroy);
        this.runnableInstance.ifPresent(RunnableInstance::terminate);
        this.thread.ifPresent(InstanceConfig::join);
        initProcess();
    }

    private void initProcess() {
        this.process = Optional.empty();
        this.thread = Optional.empty();
        this.runnableInstance = Optional.empty();
    }

    private static final void join(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new ProcessRunnerException(e);
        }
    }
}
