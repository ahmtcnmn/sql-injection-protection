package com.ahmtcnmn.agent.Libs;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class ColorLogger {
  static {
        // Jansi'yi aktif ediyoruz
        AnsiConsole.systemInstall();
    }

    public static void info(String message) {
        System.out.println();
        System.out.println(
                Ansi.ansi()
                        .fg(Ansi.Color.GREEN)
                        .a("[-------------------INFO] ")
                        .a(message)
                        .reset()
        );
        System.out.println();
    }

    public static void warn(String message) {
        System.out.println();
        System.out.println(
                Ansi.ansi()
                        .fg(Ansi.Color.YELLOW)
                        .a("[-------------------WARN] ")
                        .a(message)
                        .reset()
        );
        System.out.println();
    }

    public static void error(String message) {
        System.out.println();
        System.out.println(
                Ansi.ansi()
                        .fg(Ansi.Color.RED)
                        .a("[-------------------ERROR] ")
                        .a(message)
                        .reset()
        );
        System.out.println();
    }

    public static void debug(String message) {
        System.out.println();
        System.out.println(
                Ansi.ansi()
                        .fgBrightBlue()
                        .a("[-------------------DEBUG] ")
                        .a(message)
                        .reset()
        );
        System.out.println();
    }

    public static void success(String message) {
        System.out.println();
        System.out.println(
                Ansi.ansi()
                        .fgBrightGreen()
                        .a("[-------------------SUCCESS] ")
                        .a(message)
                        .reset()
        );
        System.out.println();
    }
}

