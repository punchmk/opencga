package org.opencb.opencga.storage.core;

        import org.opencb.commons.io.DataReader;
        import org.opencb.commons.io.DataWriter;
        import org.opencb.commons.run.Task;

        import java.io.IOException;
        import java.util.*;
        import java.util.concurrent.*;

/**
 * Created by jacobo on 5/02/15.
 */
public class ThreadRunner {
    private ExecutorService executorService;

    private List<ReadNode> readNodes = new LinkedList<>();
    private List<TaskNode> taskNodes = new LinkedList<>();
    private List<WriterNode> writerNodes = new LinkedList<>();
    private List<Node> nodes = new LinkedList<>();
    private final int batchSize;

    private final Object syncObject = new Object();
    private static final List<Object> SINGLETON_LIST = Collections.singletonList(new Object());
    private static final List LAST_BATCH = new LinkedList();

    public ThreadRunner(ExecutorService executorService, int batchSize) {
        this.executorService = executorService;
        this.batchSize = batchSize;
    }

    public <I,O> TaskNode<I,O> newTaskNode(List<Task<I,O>> tasks) {
        TaskNode<I, O> taskNode = new TaskNode<>(tasks, "task-node-" + taskNodes.size());
        taskNodes.add(taskNode);
        return taskNode;
    }

    public <I,O> TaskNode<I,O> newTaskNode(Task<I,O> task, int n) {
        List<Task<I,O>> tasks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            tasks.add(task);
        }
        TaskNode<I, O> taskNode = new TaskNode<>(tasks, "task-node-" + taskNodes.size());
        taskNodes.add(taskNode);
        return taskNode;
    }

    public <O> ReadNode<O> newReaderNode(List<DataReader<O>> readers) {
        ReadNode<O> readNode = new ReadNode<>(readers, "reader-node-" + readNodes.size());
        readNodes.add(readNode);
        return readNode;
    }

    public <O> ReadNode<O> newReaderNode(DataReader<O> reader, int n) {
        List<DataReader<O>> readers = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            readers.add(reader);
        }
        ReadNode<O> readNode = new ReadNode<>(readers, "reader-node-" + readNodes.size());
        readNodes.add(readNode);
        return readNode;
    }

    public <I> WriterNode<I> newWriterNode(List<DataWriter<I>> writers) {
        WriterNode<I> writerNode = new WriterNode<>(writers, "writer-node-" + writerNodes.size());
        writerNodes.add(writerNode);
        return writerNode;
    }

    public <I> WriterNode<I> newWriterNode(DataWriter<I> writer, int n) {
        List<DataWriter<I>> writers = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            writers.add(writer);
        }
        WriterNode<I> writerNode = new WriterNode<>(writers, "writer-node-" + writerNodes.size());
        writerNodes.add(writerNode);
        return writerNode;
    }

    public void run() {
        start();
        join();
    }

    public void start() {
        nodes.addAll(readNodes);
        nodes.addAll(taskNodes);
        nodes.addAll(writerNodes);

        for (Node node : nodes) {
            node.init();
            node.pre();
        }

        for (ReadNode readNode : readNodes) {
            readNode.start();
        }
    }

    public void join() {
        boolean allFinalized;
        synchronized (syncObject) {
            do {
                allFinalized = true;
                for (Node node : nodes) {
                    if (!node.isFinished()) {
                        System.out.println("Node " + node.name + " is not finished pending:" + node.pendingJobs + " lastBatch:" + node.lastBatch);
                        allFinalized = false;
                        break;
                    } /*else {
                        System.out.println("Node " + node.name + " is finished");
                    }*/
                }
                if (!allFinalized) {
                    try {
                        System.out.println("WAIT");
                        syncObject.wait();
                        System.out.println("NOTIFY");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (!allFinalized);
        }

        for (Node node : nodes) {
            node.post();
        }

        executorService.shutdown();
    }

    public static abstract class Task<I, O> {
        public boolean pre() {return true;}
        public abstract List<O> apply(List<I> batch);
        public boolean post() {return true;}
    }

//    public static abstract class Reader<O> extends Task<Object, O> /*implements DataReader<O>*/ {
//        public boolean open() {return true;}
//        @Override public boolean pre() {return true;}
//        public abstract List<O> read();
//        @Override public boolean post() {return true;}
//        public boolean close() {return true;}
//
//        public final List<O> process(List<Object> batch) {
//            return read();
//        }
//    }
//
//    public static abstract class Writer<I> extends Task<I, Object> /*implements DataWriter<I>*/ {
//        public boolean open() {return true;}
//        @Override public boolean pre() {return true;}
//        public abstract boolean write(List<I> batch);
//        @Override public boolean post() {return true;}
//        public boolean close() {return true;}
//
//        public final List<Object> process(List<I> batch) {
//            write(batch);
//            return Collections.emptyList();
//        }
//    }

    public class ReadNode<O> extends Node<Object, O, DataReader<O>> {
        private ReadNode(List<DataReader<O>> tasks, String name) {
            super(tasks, name);
        }

        public void start() {
            submit(SINGLETON_LIST);
        }

        @Override
        List<O> doJob(List<Object> batch) {
            List<O> oList = super.doJob(batch);
            if (oList != null)
                if (!oList.isEmpty()) {
                    start();
                } else {
                    if (!isLastBatchSent()) {
                        submit(LAST_BATCH);
                    }
//                    lastBatch = true;
//                    System.out.println("Reader '" + name + "' has finished " + isFinished());
//                    synchronized (syncObject) {
//                        syncObject.notify();
//                    }
                }
            return oList;
        }

        @Override
        protected List<O> execute(DataReader<O> reader, List<Object> batch) {
            return reader.read(batchSize);
        }

        @Override
        protected void pre() {
            for (DataReader<O> reader : tasks) {
                reader.open();
                reader.pre();
            }
        }

        @Override
        protected void post() {
            for (DataReader<O> reader : tasks) {
                reader.post();
                reader.close();
            }
        }
    }

    protected class TaskNode<I, O> extends Node<I,O,Task<I,O>> {
        private TaskNode(List<Task<I, O>> tasks, String name) {
            super(tasks, name);
        }

        @Override
        protected List<O> execute(Task<I, O> task, List<I> batch) {
            return task.apply(batch);
        }

        @Override
        protected void pre() {
            for (Task<I, O> task : tasks) {
                task.pre();
            }
        }

        @Override
        protected void post() {
            for (Task<I, O> task : tasks) {
                task.post();
            }
        }
    }

    protected class SimpleTaskNode<I> extends Node<I, I, org.opencb.commons.run.Task<I>> {
        private SimpleTaskNode(List<org.opencb.commons.run.Task<I>> tasks, String name) {
            super(tasks, name);
        }

        @Override
        protected void pre() {
            for (org.opencb.commons.run.Task<I> task : tasks) {
                task.pre();
            }
        }

        @Override
        protected void post() {
            for (org.opencb.commons.run.Task<I> task : tasks) {
                task.post();
            }
        }

        @Override
        protected List<I> execute(org.opencb.commons.run.Task<I> task, List<I> batch) {
            try {
                task.apply(batch);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return batch;
        }
    }

    protected class WriterNode<I> extends Node<I,Object, DataWriter<I>> {
        private WriterNode(List<DataWriter<I>> tasks, String name) {
            super(tasks, name);
        }

        @Override
        protected void pre() {
            for (DataWriter<I> writer : tasks) {
                writer.open();
                writer.pre();
            }
        }

        @Override
        protected void post() {
            for (DataWriter<I> writer : tasks) {
                writer.post();
                writer.close();
            }
        }

        @Override
        protected List<Object> execute(DataWriter<I> writer, List<I> batch) {
            writer.write(batch);
            return SINGLETON_LIST;
        }
    }

    abstract class Node<I, O, EXECUTOR> {
        protected final List<EXECUTOR> tasks;
        protected final String name;
        private final BlockingQueue<EXECUTOR> taskQueue;
        private List<Node<O, ?, ?>> nodes;
        private int pendingJobs;
        private boolean lastBatch;
        private boolean lastBatchSent;

        public Node(List<EXECUTOR> tasks, String name) {
            this.tasks = tasks;
            this.name = name;
            taskQueue = new ArrayBlockingQueue<>(tasks.size(), false, tasks);
            nodes = new LinkedList<>();
        }

        /* package */ void init()  {
            pendingJobs = 0;
            lastBatch = false;
            lastBatchSent = false;
        }

        protected abstract void pre ();

        protected abstract void post ();

        /*package*/ List<O> doJob(List<I> batch) {
            List<O> generatedBatch;
            assert lastBatchSent == false;
            if (batch == LAST_BATCH) {
                lastBatch = true;
                pendingJobs--;
                System.out.println(name + " - lastBatch");
                generatedBatch = Collections.emptyList();
            } else {

                EXECUTOR task = null;
                task = taskQueue.poll();

                boolean nextNodesAvailable = true;
                for (Node<O, ?, ?> node : nodes) {
                    nextNodesAvailable &= node.isAvailable();
                }

                if (task == null) { //No available task
                    resubmit(batch);
                    generatedBatch = null;
                } else if (!nextNodesAvailable) {   //Next nodes have to many batches.
                    try {
                        taskQueue.put(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    resubmit(batch);
                    generatedBatch = null;
                } else {

                    generatedBatch = execute(task, batch);
                    System.out.println(name + " - end job");
                    for (Node<O, ?, ?> node : nodes) {
                        node.submit(generatedBatch);
                    }

                    try {
                        taskQueue.put(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pendingJobs--;  //
//                    System.out.println(name + " - pendingJobs " + pendingJobs);
                }
            }
            if (isFinished()) {
                if (!lastBatchSent) {
                    generatedBatch = LAST_BATCH;
                    for (Node<O, ?, ?> node : nodes) {
                        node.submit(LAST_BATCH);
                    }
                    lastBatchSent = true;
                }
                System.out.println("Node '" + name + "' is finished");
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }
            return generatedBatch;
        }

        protected abstract List<O> execute(EXECUTOR task, List<I> batch);

        private void resubmit(final List<I> batch) {
            executorService.submit(new Runnable() {
                public void run() {
                    doJob(batch);
                }
            });
        }

        /*package*/ void submit(final List<I> batch) {
            System.out.println("Submitting batch: pendingJobs = " + pendingJobs + " - " + "[" + (isAvailable()? " " : "*") + "]" + name + " - " + Thread.currentThread().getName());
            pendingJobs ++;
            resubmit(batch);
        }

        public boolean isAvailable() {
            return pendingJobs < tasks.size();
        }

        public boolean isFinished() {
            return pendingJobs == 0 && lastBatch;
        }

        public boolean isLastBatchSent() {
            return lastBatchSent;
        }

        public void append(Node<O, ?, ?> node) {
            nodes.add(node);
        }

    }

}
