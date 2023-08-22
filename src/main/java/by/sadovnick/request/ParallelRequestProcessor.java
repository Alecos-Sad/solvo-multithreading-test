package by.sadovnick.request;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelRequestProcessor {
    private final Queue<Request> typeAQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Request> typeBQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, Lock> valueLocks = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final Semaphore typeASemaphore;
    private final Semaphore typeBSemaphore;
    private final Logger logger = Logger.getLogger("ParallelRequestProcessor");

    public ParallelRequestProcessor(int numThreads) {
        executorService = Executors.newFixedThreadPool(numThreads);
        typeASemaphore = new Semaphore(numThreads / 2);
        typeBSemaphore = new Semaphore(numThreads / 2);
    }

    public void processRequest(Request request) {
        if (request.getType() == 0) {
            try {
                typeASemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            typeAQueue.add(request);
            executorService.submit(this::processTypeARequests);
        } else {
            try {
                typeBSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            typeBQueue.add(request);
            executorService.submit(this::processTypeBRequests);
        }
    }

    private void processTypeARequests() {
        while (!typeAQueue.isEmpty()) {
            Request request = typeAQueue.poll();
            if (request != null) {
                // Получаем или создаем блокировщик для данного значения запроса. Если для данного значения блокировщик уже существует в valueLocks,
                // то он будет возвращен. Если нет, то создается новый блокировщик типа ReentrantLock и добавляется в valueLocks для данного значения.
                Lock valueLock = valueLocks.computeIfAbsent(request.getValue(), k -> new ReentrantLock());
                valueLock.lock();
                // Обработка запроса типа A
                logMessage("Processing Type A request with value: " + request.getValue() + " from Queue A" + " Queue A size: "
                        + typeAQueue.size() + ", Queue B size: " + typeBQueue.size());
                valueLock.unlock();
                typeASemaphore.release();
            }
        }
    }

    private void processTypeBRequests() {
        while (!typeBQueue.isEmpty()) {
            Request request = typeBQueue.poll();
            if (request != null) {
                Lock valueLock = valueLocks.computeIfAbsent(request.getValue(), k -> new ReentrantLock());
                valueLock.lock();
                // Обработка запроса типа B
                logMessage("Processing Type B request with value: " + request.getValue() + " from Queue B" + " Queue A size: "
                        + typeAQueue.size() + ", Queue B size: " + typeBQueue.size());
                valueLock.unlock();
                typeBSemaphore.release();
            }
        }
    }

    private void logMessage(String message) {
        logger.log(Level.INFO, message);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}




