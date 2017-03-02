package org.tails.bpm.test;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface ElapsedTimeProcessor {
    void process(Consumer<Callable> callable);
}
