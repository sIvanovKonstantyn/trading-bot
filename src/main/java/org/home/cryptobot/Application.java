package org.home.cryptobot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.home.cryptobot.openinterest.OpenInterestChecker;

public class Application {
    private static boolean stopped = false;
    private static OpenInterestChecker checker = new OpenInterestChecker();

    public static void main(String[] args) throws IOException {

        runBot();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String command = br.readLine();
            if ("/stop".equals(command)) {
                stopped = true;
                while (!checker.stopped) {
                    sleep();
                }
                break;
            }
        }
        System.out.println("bot stopped...");
    }

    private static void runBot() {
        System.out.println("bot started...");
        new Thread(() -> {
            while (!stopped) {
                System.out.println("bot updates the data");
                checker.check();
                sleep();
            }
            System.out.println("bot starting shut down...");
            checker.shutDown();
        }).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
