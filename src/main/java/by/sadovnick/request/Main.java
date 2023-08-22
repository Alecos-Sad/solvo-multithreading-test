package by.sadovnick.request;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of threads: ");
        int numThreads = scanner.nextInt();

        System.out.print("Enter the number of requests: ");
        int numRequests = scanner.nextInt();

        ParallelRequestProcessor processor = new ParallelRequestProcessor(numThreads);

        // Создаем и добавляем запросы в обработку
        for (int i = 0; i < numRequests; i++) {
            Request typeARequest = new Request(0, i);
            Request typeBRequest = new Request(1, i);
            processor.processRequest(typeARequest);
            processor.processRequest(typeBRequest);
        }

        // Завершаем обработку и останавливаем потоки
        processor.shutdown();

        scanner.close();
    }
}
