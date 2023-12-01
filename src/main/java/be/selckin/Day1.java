package be.selckin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Day1 {

    public static void main(String[] args) throws IOException {

        run(Files.readAllLines(Paths.get(args[0]).resolve("day1-example2.txt")));
        run(Files.readAllLines(Paths.get(args[0]).resolve("day1-input.txt")));

    }

    private static void run(List<String> lines) throws IOException {
        int sum = 0;
        for (String line : lines) {

            String first = findFirst(line);
            String last = findLast(line);

            sum += Integer.parseInt(first + last);

        }
        System.out.println(sum);
    }

    private static String findFirst(String line) {
        for (int i = 0; i < line.length(); i++) {
            String num = findNumber(line, i);
            if (num != null)
                return num;
        }
        throw new RuntimeException();
    }

    private static String findLast(String line) {
        for (int i = line.length() - 1; i >= 0; i--) {
            String num = findNumber(line, i);
            if (num != null)
                return num;
        }
        throw new RuntimeException();

    }

    private static String findNumber(String line, int i) {
        if (Character.isDigit(line.charAt(i)))
            return String.valueOf(line.charAt(i));

        return findNumber(line.substring(i));
    }

    private static String findNumber(String rest) {
        List<String> nums = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine");

        for (int j = 0; j < nums.size(); j++) {
            String num = nums.get(j);
            if (rest.startsWith(num)) {
                return String.valueOf(j + 1);
            }
        }
        return null;
    }

}
