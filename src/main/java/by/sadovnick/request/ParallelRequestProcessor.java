package by.sadovnick.request;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelRequestProcessor {
    private final Queue<Request> typeAQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Request> typeBQueue = new ConcurrentLinkedQueue<>();
    private final Lock typeALock = new ReentrantLock();
    private final Lock typeBLock = new ReentrantLock();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void processRequest(Request request) {
        if (request.getType() == 0) {
            typeAQueue.add(request);
            executorService.submit(this::processTypeARequests);
        } else {
            typeBQueue.add(request);
            executorService.submit(this::processTypeBRequests);
        }
    }

    private void processTypeARequests() {
        typeALock.lock();
        try {
            while (!typeAQueue.isEmpty()) {
                Request request = typeAQueue.poll();
                if (request != null) {
                    // Обработка запроса типа A
                    System.out.println("Processing Type A request with value: " + request.getValue());
                }
            }
        } finally {
            typeALock.unlock();
        }
    }

    private void processTypeBRequests() {
        typeBLock.lock();
        try {
            while (!typeBQueue.isEmpty()) {
                Request request = typeBQueue.poll();
                if (request != null) {
                    // Обработка запроса типа B
                    System.out.println("Processing Type B request with value: " + request.getValue());
                }
            }
        } finally {
            typeBLock.unlock();
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public static void main(String[] args) {
        ParallelRequestProcessor processor = new ParallelRequestProcessor();

        // Создаем и добавляем запросы в обработку
        for (int i = 0; i < 10; i++) {
            Request typeARequest = new Request(0, i);
            Request typeBRequest = new Request(1, i);
            processor.processRequest(typeARequest);
            processor.processRequest(typeBRequest);
        }

        // Завершаем обработку и останавливаем потоки
        processor.shutdown();
    }
}
