package be.selckin;

import java.io.IOException;
import java.util.List;

public class Day6 {
    public static final String PART1_EXAMPLE = """
            Time:      7  15   30
            Distance:  9  40  200
            """;

    public static final String PART1_INPUT = """
            Time:        38     67     76     73
            Distance:   234   1027   1157   1236
            """;

    public static void main(String[] args) throws IOException {
        part1(PART1_EXAMPLE);
        part1(PART1_INPUT);
        System.out.println("-----------");
        part2(PART1_EXAMPLE);
        part2(PART1_INPUT);
    }

    private static void part1(String input) {
        List<String> lines = input.lines().toList();

        String[] times = lines.get(0).split(": +")[1].split(" +");
        String[] distances = lines.get(1).split(": +")[1].split(" +");

        long result = 1;
        for (int i = 0; i < times.length; i++) {
            long time = Long.parseLong(times[i]);
            long record = Long.parseLong(distances[i]);

            result *= calcOptions(time, record);

        }
        System.out.println(result);
    }

    private static void part2(String input) {
        List<String> lines = input.lines().toList();

        long time = Long.parseLong(lines.get(0).split(": +")[1].replaceAll(" +", ""));
        long distance = Long.parseLong(lines.get(1).split(": +")[1].replaceAll(" +", ""));

        long options = calcOptions(time, distance);

        System.out.println(options);
    }

    private static int calcOptions(long time, long record) {
        int options = 0;
        for (int holdTime = 0; holdTime < time; holdTime++) {

            long distance = ((time - holdTime) * 1) * holdTime;
            if (distance > record) {
                options++;
            }
        }
        return options;
    }


}


