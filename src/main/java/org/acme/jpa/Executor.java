package org.acme.jpa;

public class Executor {

    public void execute(Runnable runnable) throws Exception {
        runnable.run();
    }
}
