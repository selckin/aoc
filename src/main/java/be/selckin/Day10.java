package be.selckin;

import be.selckin.Day10.Grid.RunForestRun;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Day10 {
    public static final String PART1_EXAMPLE = """
            .....
            .S-7.
            .|.|.
            .L-J.
            .....
            """;
    public static final String PART1_EXAMPLE1 = """
            ..F7.
            .FJ|.
            SJ.L7
            |F--J
            LJ...
            """;

    public static final String PART2_EXAMPLE1 = """
            ...........
            .S-------7.
            .|F-----7|.
            .||.....||.
            .||.....||.
            .|L-7.F-J|.
            .|..|.|..|.
            .L--J.L--J.
            ...........
            """;
    public static final String PART2_EXAMPLE2 = """
            .F----7F7F7F7F-7....
            .|F--7||||||||FJ....
            .||.FJ||||||||L7....
            FJL7L7LJLJ||LJ.L-7..
            L--J.L7...LJS7F-7L7.
            ....F-J..F7FJ|L7L7L7
            ....L7.F7||L7|.L7L7|
            .....|FJLJ|FJ|F7|.LJ
            ....FJL-7.||.||||...
            ....L---J.LJ.LJLJ...
            """;
    public static final String PART2_EXAMPLE3 = """
            FF7FSF7F7F7F7F7F---7
            L|LJ||||||||||||F--J
            FL-7LJLJ||||||LJL-77
            F--JF--7||LJLJ7F7FJ-
            L---JF-JLJ.||-FJLJJ7
            |F|F-JF---7F7-L7L|7|
            |FFJF7L7F-JF7|JL---7
            7-L-JL7||F7|L7F-7F7|
            L.L7LFJ|||||FJL7||LJ
            L7JLJL-JLJLJL--JLJ.L
            """;

    public static void main(String[] args) throws IOException {
        part1(PART1_EXAMPLE, 4);
        part1(PART1_EXAMPLE1, 8);
        part1(PART1_INPUT, 6754);

        part2(PART2_EXAMPLE1, 4);
        part2(PART2_EXAMPLE2, 8);
        part2(PART2_EXAMPLE3, 10);
        part2(PART1_INPUT, 567);
    }

    private static void part1(String input, int expected) {
        Grid grid = parse(input);
        int result = grid.walk().size() / 2;
        if (result != expected)
            throw new RuntimeException();
    }

    private static void part2(String input, int expected) {
        Grid grid = parse(input);
        Deque<RunForestRun> path = grid.walk();

        Map<Point, RunForestRun> pathPoints = path.stream().collect(Collectors.toMap(RunForestRun::point, p -> p));

        int[][] mask = grid.mask();

        int count = 0;
        for (int y = 0; y < mask.length; y++) {

            RunForestRun[] points = new RunForestRun[mask[y].length];
            for (int x = 0; x < mask[y].length; x++) {
                points[x] = pathPoints.get(grid.newPoint(x, y));
            }

            for (int x = 0; x < mask[y].length; x++) {
                if (points[x] == null) {
                    int down = 0;
                    int up = 0;
                    for (int i = x; i < mask[y].length; i++) {
                        RunForestRun hit = points[i];
                        if (hit != null) {
                            // borders
                            switch (hit.point().type()) {
                                case '7' -> down++;
                                case 'F' -> down++;
                                case 'L' -> up++;
                                case 'J' -> up++;
                                case '|' -> {
                                    up++;
                                    down++;
                                }
                            }
                        }
                    }

                    if (Math.min(up, down) % 2 != 0) {
                        mask[y][x] = 1;
                        count++;
                    }
                }
            }

        }

        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[y].length; x++) {
                System.out.print(mask[y][x]);
            }
            System.out.println();

        }
        System.out.println(count);

        if (count != expected)
            throw new RuntimeException();

    }

    private static Grid parse(String input) {
        List<String> lines = input.lines().toList();

        char[][] grid = new char[lines.size()][lines.get(0).length()];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            grid[i] = line.toCharArray();
        }
        return new Grid(grid);
    }


    public static class Grid {
        public final char[][] grid;

        public Grid(char[][] grid) {
            this.grid = grid;
        }

        public int[][] mask() {
            return new int[grid.length][grid[0].length];
        }

        public Deque<RunForestRun> walk() {
            Point start = findStart();

            return Arrays.stream(Direction.values())
                    .map(direction -> run(start, direction))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Deque::size))
                    .findFirst().get();
        }

        private Deque<RunForestRun> run(Point start, Direction direction) {
            Deque<RunForestRun> path = new ArrayDeque<>();

            RunForestRun current = next(start, direction);
            while (current != null) {
                path.add(current);
                current = next(current);
                if (current == null)
                    return null;
                if (current.point().equals(start)) {
                    path.add(new RunForestRun(start, current.exit(), direction));
                    return path;
                }
            }
            return null;
        }


        public RunForestRun next(RunForestRun current) {
            return next(current.point(), current.exit());
        }

        private RunForestRun next(Point start, Direction direction) {
            var next = switch (direction) {
                case NORTH -> newPoint(start.x(), start.y() - 1);
                case EAST -> newPoint(start.x() + 1, start.y());
                case SOUTH -> newPoint(start.x(), start.y() + 1);
                case WEST -> newPoint(start.x() - 1, start.y());
            };

            var nextEntrance = nextEntrance(direction);

            Direction exit = switch (next.type()) {
                case '|' -> exit(nextEntrance, Direction.NORTH, Direction.SOUTH);
                case '-' -> exit(nextEntrance, Direction.EAST, Direction.WEST);
                case 'L' -> exit(nextEntrance, Direction.NORTH, Direction.EAST);
                case 'J' -> exit(nextEntrance, Direction.NORTH, Direction.WEST);
                case '7' -> exit(nextEntrance, Direction.SOUTH, Direction.WEST);
                case 'F' -> exit(nextEntrance, Direction.SOUTH, Direction.EAST);
                case '.' -> null;
                case 'S' -> direction;
                default -> throw new RuntimeException();
            };
            if (exit == null)
                return null;
            else
                return new RunForestRun(next, nextEntrance, exit);
        }

        private static Direction nextEntrance(Direction direction) {
            return switch (direction) {
                case NORTH -> Direction.SOUTH;
                case EAST -> Direction.WEST;
                case SOUTH -> Direction.NORTH;
                case WEST -> Direction.EAST;
            };
        }

        private Direction exit(Direction entrance, Direction a, Direction b) {
            if (entrance == a)
                return b;
            if (entrance == b)
                return a;
            return null;
        }


        public record RunForestRun(Point point, Direction entrance, Direction exit) {

        }

        private Point findStart() {
            for (int y = 0; y < grid.length; y++) {
                char[] chars = grid[y];

                for (int x = 0; x < chars.length; x++) {
                    char point = chars[x];
                    if (point == 'S') {
                        return newPoint(x, y);
                    }
                }
            }
            throw new RuntimeException();
        }


        private Point newPoint(int x, int y) {
            return new Point(x, y, type(x, y));
        }

        private char type(int x, int y) {
            if (y >= grid.length || y < 0 || x >= grid[y].length || x < 0)
                return '.';

            return grid[y][x];
        }
    }

    public enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST;
    }


    public record Point(int x, int y, char type) {

    }


    public static final String PART1_INPUT = """
            LF-|7F-7.F--7---F7-J-F77.FLF7FF|---J-7-L-J7.F7.-.FF|7|7J7-L7.-F7|.|.F|-F-|7.---777L7-.FF77|77F7.|.FF777FJ.-L-|--7FF.F|-|--|--L.L-|F|-FJ7-JF|
            LL7L77-J-J|L7F|FJLJJLFL--7|F--7--L7L-J.||7FJ.|-|.F7LFLJL7..7JLLJLFJF7|7|FF77-L||J-F|.F77--F.7L-7|7FLJF-FJ-||-|7|LLJF-L7|J.L7.|7..|7||LJL-7L7
            .F|-LJ-|---LJ-J--7---|F|.|L.L|J.LFL.LLJ-7-|LL--JJFF7JL77LJ-|.FF|L7FJ.FJ|F|L-7J||J.|L7LJJ77|F|.L|J-FJF7LLF-7JFLJ|.L-J7-L-7F-7-77FLJFJLJL7--J|
            -7|F|J7F7J7-77L|.-FJ-FLJL-..L||F--|7..LJ|L|-|JFL-7||F7|J-|F7.FF7-F7L-FF--JF-JFF7--F777-FJ7.F77-J|FL--|.JLLLJ|.LJ-77LL7|7F77LF7L-|-L.|-F|LJ|L
            LFF-J---J.--JF7L.|F.F--7.JF|J.J..--J7-|F-7|-7.7LFL--J|J||LFJFLLFFJ|J|LL--7L--7||7-|L-7JJ|LF||7.J7FLL|L--L||F7-L|JL-FJ|--.|-JJL7-F7FF7-F-7-FJ
            .|FJ-7J|.F|7-FL-|FF77.F|-||JF-7.77LLL-F-7F|.|7.FF7...|LJ7LF7-.|JL7|F|7|7LL7F-J|L-7|F-JJLFL-J-|J.F-7|F|JF7--|J..L-7FJ7|LJ-|-F-L---J--|J|.F7.7
            F7J.L||J-JLL.JLF-7JFJ--F-F|FL7FFF7L||L|7|.F7JF7-||F7-|J77.L...L7FJL77FF7FL|L-7L7FJ|L-7J7|-L-LJFF7JF7L7.|L||LF-J7|F-.L777L-J.J.FL7|J-L-F7L7F|
            JLL-7LF-F7|LJ-|JLF7FJ.L|-|LL.L--J77||F-JLFJ|-F7.||||7|-77F-FLJLFL-7L7FF-7F|F-JF|L-JF-JFF|7|.|J|7|7J.F|.|FFJ.F-LF7L7.FL-7.L77.FF|--J.L-FL---7
            |-LJFF77L--.F-7.|J|-F7|F-J||7LL7LF.|JL.|JL7||||FJLJ|77F7F7-J.|.F-7L7L7L7|FJL7F7|F--JJ7--7L-J|.LLF77.F-J-L7.FF7J|FLJ.FJLF7.LJ-L||.L|-|LJ.|.|J
            J7.FFJJJ.FJF|-F-77|JJJF7|.7.F7.L-FF-7L77-FJL7|||F--JLF7JFLJFJ-FL7L7L7L7||L-7|||||L|-LF7LL-J7|--F||77..LF7|7JLJ||-F.L--7J|.FJ7FJ-7F7.|.F7--7.
            |F-||FJJF7.F7-7.|7F77FF|7-FF||7-F7L7|7J|JL-7||LJ|F-7FJ|J|--J|||.L7|FL7LJL7FJ||LJL7JF7||-F7FJ7L-FJL-7.F|LJLFJ-L7--F-JLF|7FJ--F|-FLJJLJ.FJF--7
            --|JJ..-||-F-.LFJLL-F-7J||F-JL-7|L-JL-7-F7J|||F7|L7||FJF7|J|-FFF-JL-7L--7||FJL-7FJFJ||L77J-LL-LL7F-J777.FF|L7-LJ-|F|LLF7JJFFJJ-JJ|-F|-|.LF||
            FL|||F7|LJ-|L-J|LJ.L|7|LL-L---7|L-7F--JFJL-J|||LJFJ||L7|L7JF.F7L---7L-7FJ|||F7FJL7|FJL7L7L7.|FFJ|L7.L7.FLJ.|.L|J7F777J||J||7J|FJ-|-FJF|.7|F|
            --77FJ-F|-L|J.-L7|FLJ-L-..F|F-JL-7|L-7JL-7F7LJL7FJFJL7||FJ.F7||F---JF7|L7LJLJ|L7FJ||F7|FJF--77F-JFJ-LF7--F--7FF77|L77.LF-JJL-|J.F-7|LF-7.L||
            JF||-JF7-7F7.7J.JJ7.|J7FJ7|FL--7FJL-7|F7JLJL--7LJFJ7FJLJL7FJLJ||F--7|LJ|L7F--J-|L7||||||FJF-J-L-7|..L||J.|.LF-J|F77JL-L.JJF7.L--7-L7L|FJ7|LJ
            FL-JFJ-|L-||.7.77---|FLJF|-J-F-J|F7FJ|||F7F---JF-JF7L7F--JL7F-JLJF-JL7F7FJ|F7FFJFJ|LJ||LJFJF-7F7|L77F||7FF7JL-7||L777.|F.FJF7-LFJLLF-JL-7FLJ
            L-LLJ-FFFJ|JF--.-7..7-..LF7LFJF7|||L7|||||L-7F7L-7|L7|||F--J|F7F7L--7|||L7|||FJFJ|L7FJ|F-J|L7||LJFJF7|L7FJ|F-7|||FJF77-77-JL-7|.|FFL7F--J-J|
            L7||J.-F-7--F...FL--|JF|7|L-JFJ||||FJ||||L7LLJL7FJL7LJL7|F--J|||||F-J|||FJLJ|L7|JF-J|FJL7F7F||L-7L7|||FJ|FJL7LJ|||||L7F7|F-7FFJ77L-FJL7J...F
            FLF7.F|F.||FJ.-7-|JFJJ||FL7F7|FJ||||FJ|LJFJF7F7|L7FJF7FJ||F7FJLJ|FJF-J|||F--JFJL7L7FJL-7LJ|FJL-7L7LJLJL7||F7L-7|||FJFJ||-7L|-|FLJ7.|F7|.LF-7
            .-7F-J-L--L7FFLJLJF|JF|F7L||LJL7||||L7L-7L7|||||FJ|FJLJFJLJ||F--JL7L--JLJL7F7L7FJFJL-7FJF-J|F--JLL-7F--J|LJ|F-JLJ|L7L-J|-F77-F7F|FFJ|LJJJ|L7
            |J|LFJ77L7LL-7L7.|--7-FF77LJ-F-JLJ|L7L--JFJ|LJLJL-J|F7JL--7LJL7F-7L---7F--J|L7|L7L7F-JL7L--J|F77F--JL7F7|F-JL---7L7L7F7L7||F7||F|7L7L7JJF|.L
            |--L|F-7LF7.LL-L7.LJ|.||L--7JL---7|7L7F-7L7L-7F---7|||F7F7L-7FJL7|F---JL7F7|FJ|FJFJL-7FJF-7FJ|L7L--7FJ|LJL-7F--7L7L7LJ|FJ||||||7F7FJFJ--||-|
            |FFJ||FFJL77FJ7LJ|--L7FL7F-J.||F-JL-7LJJ|FJF-J|FF7LJ|LJ|||F7||F7||L-7F-7|||||FJL7|F--JL7L7LJ7L7L7F7||FJF---J|F-J7L7L7FJL7|||LJL7|||FJJ---JLF
            |L|FJ.7|LFF-|.7.FJ..FF7J|L-7FF7L-7F-JF7FJL7L-7L-J|F7|F-J|LJ||||LJL-7LJFJ||||||F-J||F7F7|FJF-7FJFJ||||L7|F77FJL7F7FJFJ|F-J||L7F-J|LJL7FJ.|.F7
            .FFL.FF|-|JFF-J7-LF-7||FJF-JFJL--J|.FJ||F7L7LL-7FJ||||F7L7FJ|LJF7F-JF7L7||LJLJ|7FJLJ|||||7|FJL7L7|||L7|||L7L-7|||L7L-J|F7||FJL-7L7F-J7FL|FLJ
            --JJ-|-L7L-7JJ|JF|L7LJ|L7|F7L--7F7L7|FJLJL7|F7F|L-J||LJL7|L-JF-JLJFFJ|FJ||F--7|FJF--J||||FJL-7L7||||FJ|LJFJF7||||F|F--J|LJ||F--JFJL-7FF-L|7J
            LJJ.|.|JF.F|J.FFFF-JF7L7||||F-7LJL7|||LF7FJ|||FJF--JL7F-J|F--J7F7F7L7|L7LJL-7||L7|F7-|LJLJF--JFJ||LJL7L-7L7||||||FJL-7FJF-J|L--7|F--JF7-.LJ.
            L|7-7-|7.-J---FFJL-7|L7LJ||||FJFF7|LJL7||L7LJ||FJF7F-JL--JL-7F7||||FJL-JF---JLJFJ||L-JF---J-F7L7||F--JF7L7||||||LJF--JL7L7FJF--J||FF-J||.FJ7
            FL7F--L7FJ.-JFJFF7F||FJF7LJLJL7FJ|L-7FJ||-L7FJ||F||L-7F-----J||||||L7F-7|F7F7F7L7LJF--JF7F7FJ|FJ|||F--J|.||||||L7FJF--7|FJL7|F7FJL-JF-J77J.F
            |-J-|JFJ7.7|FFFFJ|-LJL-JL---7FJL7|F7||||L7FJL7|L-J|F-J|FF77F-J||LJL7LJFJLJLJLJ||L-7|.F-J||||FJ|FJ||L--7|FJ||||L7|L-JF-J||F-JLJLJF7F7L-7J-..7
            |--77-L7|.F7-F7|FJF|LF-7F--7|L-7|||||L-JFJL-7||F--JL-7L7|L7L-7|L--7L7FJF7F7F7FJF-7|L-JF-J|LJ|FJL7|L7F7|LJFJ||L7||F--JF-JLJF-7F--J|||F-J||J7F
            J.L-J7---F7JFJLJ|F7F7|FJL7FJL-7LJLJ|L--7L7F-JLJL--7F-JFJ|FJF7|L-7FJFJL-J||LJ|L7L7LJF-7|F7L-7|L-7||FJ|||F-J.|L-JLJL7F7L-7F7L7|L--7|||L7-FFJF|
            L||LLL-7J|L7L-7FJ|||LJ|F-J|F7LL--7FJ-F-JFJL7F-----JL-7L7|L7|||F-JL7L7F--JL-7L-J|L-7L7LJ||LFJL7FJLJ|FJLJL7|FJF7F7F7LJL--J||FJ|F7FJLJL-J-|-7F7
            FL7||JLF-FJFF-JL7||L7FJL-7|||F---J|F7L-7|F-J|F-7F--7LL7||FJ|LJL-7-L7|L--7F-JF-7F--JFJF7||FJF-JL-7FJL7F--JFJFJ||LJL7F---7||L7||LJF7F-7L7JJJ7J
            --JL7J.|.J||L--7LJ|FJL7F-JLJ|L---7|||F7||L-7|L7||F-JF7||||-L7F--JF7||F7L||F7|FJL--7|-|||||FJF7F7||FL||F7J|FJ|LJ.F7|L--7LJ|FJ|L--JLJFJ..JL||-
            |FF|-7-JJ-F----JF7|L-7|L-7F7L7F7FJLJ||||L7FJ|FJ|||F7|||||L7FJL7F7||||||FJ||LJ|F7F7|L7|||||L7|||||L7FJ||L-JL--7F-J|L--7L-7LJ||F7F-7FJF77JFJ|7
            F|-J7L7J|FL-----J||F7||F-J||FJ||L--7LJ||.||FJL7LJ||LJLJ||FJ|F7||LJLJ|||||||F-J|||||FJ||||L7LJ||||FJ|FJL7F-7F-JL-7L-7|L--JF-7|||L7||J|L7F7F7J
            J..LF|L-J.F----7FJLJLJ|L--J|L-JL7F7L7FJL7LJ|F7L-7|||F7-||L7LJ||L--7FJ|LJFJ||F-JLJ|||FJ|||FJF-J||||FJ|F-JL7|L--7FJF-JF7F77|FJLJL7|LJFJFJ||||7
            .F-.LF7||-L7F-7LJF7F-7L----JF--7LJ|FJL--JF-J||F-J||FJL-JL7L7FJ|F-7|L7L-7|FJ||F7FFJ|||JLJ|L7L7FJ||||FJ|F--JL---JL7L--JLJL-J|F7F7LJF7|FJFJLJL7
            FL7LJLL7-J|LJFJF7|LJFL7F-7F7|F7L-7LJF---7|F-J|L-7LJL7F7F7|F|||LJFJ|FJF-J|L7|||L-JFJLJF--JFJFJL7||LJL-JL----7F---JF7F------J|LJ|F7|LJL-JF7F7|
            |FJJF7L||7FLFJFJ||F---JL7LJ|||L--JF-JF7FJ||F-JF-J7F7LJ||LJFJL--7|FJ|FJF-JFJLJL--7L--7L7F7|FL7FJ||F---------JL--7FJLJ.F7|F7FJF-J||L7F-7FJ||LJ
            LJJJL7.-|J-FJFJ|LJL-7F--JF7LJL7F-7|F-JLJ-LJL7FJF--JL--JL-7L-7F-JLJFJL7L-7L--7F--JF7FJ7LJ|L-7LJFJ||F--------7F7FJL----JL-J||FJF7||FJL7LJFJ|J7
            FJ|-FL|J|.|L7L-7F--7LJF--JL---J||LJL-7F----7||JL7F7F--7F7|F-JL---7L-7|-FJF7FJL7F7|||F7F-JF7|F7L-JLJF-7F7F--J|||F-7F--7F-7LJL-JLJLJF7L-7L-J||
            7-|.L-|7|.F-L--JL-7L--JF--7F7F7|F----J|F-7FJ|L7FJ|LJ-FJ||||F7F7F7|F-J|FJFJ|L7FJ||||LJ|L-7||||L7F-7|L7|||L---JLJL7|L-7||FJF-7F-7F--JL7FJF7-L-
            7||7LFJLF7L-F7|JLFJF-7FJF-J|||LJL-----JL7LJ.L7||FJ7F-JFJ||||||LJLJL--JL7||L-JL7|LJL7FJF7||LJL7|L7|F7|||||F7F----JL--JLJL-JFJL7|L7F7.LJL||||J
            F7-LF--LL7FL|L7F-JFJ-LJLL-7|LJF------7F-JF--7LJLJF7L-7L7|||||L-----7.F7|L---7FJL--7||-|LJ|JF-JL-JLJLJLJL-JLJF7F7F7F7F7F--7|F7|L7LJL----JL-7.
            L7-.77L|FL--L7LJF-JF---7F-J|F7L----7FJ|F7|F-JF7FFJ|F7L7|LJ|||F---7FJFJLJF7F7||F7F7||L7L7FJFJF-7F7F-7F7F7F---JLJLJLJLJ||F-JLJ|L7|F7F-7F-7F7|7
            7L|.J----.-.FL-7|F7|F--JL-7||L--7F-JL7||||L-7|L-JFJ|L-JL7FJ||L--7LJFJF7FJLJLJLJ||||L7|FJL7L7L7|||L7LJLJLJF7F-7F7F-7F7LJL---7|FJ||||FJL7LJ||7
            L-|-|.L-|JLFFLLLJ|LJ|F7F7JLJL--7|L--7LJ|LJF7LJF-7L7L-7F7||FJ|F-7L7L|FJ|L-7F7LLFJ||L7LJL--J7L-JLJL-J7F7F--JLJFJ|LJ-||L7F-7F7||L7||LJL-7|FFLJ7
            .-|L|FFL-.|F----7L-7|||||F-7F--JL---JF-JF-JL-7|-|FJ7FJ|||||FJ|JL7L7LJFJF7LJL7FJFJL7L-77F----7F--7F--JLJF---7L-JF-7||FJL7LJLJL7||L---7||F7F-7
            JLJFFJ7J.F-L---7|F-J|||||L7|L--------JF7|.F7.|L7||F-JFJ||||L7|F-JFJ.FJFJ|F7FJL7|.FJF-JFJF--7|L-7|L7F-7FJ-F-JF--JFJLJL-7L---7.LJL7F-7||LJLJFJ
            |-LL7F-F7F.LF7FJLJF7LJ|||FJL--------7FJLJFJL7|FJ||L7FJFLJLJJLJL7FJF-JFJFJ|LJF-JL7L7L-7L7|-FJL7|||FLJFJ|F-JFFJF7FJLF--7L----JFF77LJJ||L7F--JJ
            7.F||FJL--L7||L---JL-7|||L---------7LJF77|F-J|L7|L7|L---7F-----JL7|F7L7L7L-7L--7|FJF-JFJ|FJF7L-JL7F7L-JL---JFJLJF7|F7|F7F7F--JL-7F7||J|L7J||
            -.FJJ-|.L7LF||LF7F---J|||F7F7F-----JF-JL-JL7||FJL7|L-7F-JL7F7F7F7||||FJF|F-JF-7||L7L-7L7|L-J|F7F7LJ|F7|F7F--J|F7||||LJ|||LJF----J||LJ-L-J7LJ
            |.L7L||JFL7FJL7|LJF7F7LJLJLJLJF----7|F--7F7L7|L-7||F-JL--7||||||||LJ||F-JL--JFJ||FJF-JJLJF7FLJLJL-7|||FJ|L----JLJLJL7-|||F-JF----JL7J77F|-.|
            FJ-F.L.LJF-L-7LJF-J||L7F7F7F7FJLF--J|L-7||L-JL--J||L7F7F7|LJ||||||F-J||F-7F--JJLJL7L-7FF-JL77F----J|||L7|F7F7F-7F--7L-JLJL-7|F7F7F-JFF-FL-77
            ||7F.|.JLL.FFJF7|F7||-LJLJLJ||F7L---JF7|||F--7F-7||FJ|LJLJF-J|LJ||L7FJ||FJL----7F-JF-JFJF-7L-JF7F-7LJL-JLJLJLJ7LJF7L-7F----J||LJLJL|FLJJL..|
            -F|JFFLJ7JF-|FJLJ||||F--7FF7LJ|||F---JLJLJ|F-J|FJ||L7L7JF7L7FJ-|LJFJ|FJ|L7F7F-7|L--JLFJFJ-|F7FJLJJL---7F----7F7F7|L-7|L-----J|F7F7F7L|JFJLLJ
            |.LF.-7F-7L7LJF--J|LJL-7L-J|FFJL-JF---7LF7|L--JL7LJ|L7L7||7||J-F7F|FJ|FJL|||L7LJF---7L-JF7|||L7F------J|F---J|||||F-JL--7F7F-J||||||F7FF-|J.
            |-7LFJLJFJ7LJ|L7F7L7F-7L--7L-JF---JF-7L-JLJF---7L---7L7LJL7LJJ7F--J|FJ|7F||L7L-7L7F7L---J|LJL-J|F------JL-7F7|LJLJL7-F7FJ||L--JLJLJLJL-7-7F7
            LJ7F-7-F7-|LLJ7LJ|FJL7||F7L---JF7F-JJ|F7F--J-F7L----J.L7F-JJL7FL--7|L7|-FJL7L7FJJLJL----7|F7JF7LJF7|F-----J|||F---7L7||L7||F-7F-7F7F7F7|F77F
            FF-J.L7|J||J7LF--JL--JL-JL--7F7||L--7LJLJF---JL----7F-7|L7JFJ-7.|FJL7||LL7FJLLJF7-F7F---JLJL-JL7FJL7L7F----JLJL--7L7LJL7LJLJJLJLLJLJ||LJJ.||
            -7|F7F||FFF77LL------------7LJLJL---JF7F7|F-7F7F7F-J|FJL-JJL..|FFJF-J||FFJ||7LFJL-JLJF--------7LJF7|LLJF--7F7F---JFJF7FJF---7F7F7F7.|L7J77JL
            |FJJ.LLFJFJL7JF----7F--7F-7L-----7F77|LJLJ|JLJLJLJF-JL-7J.FJ7FLFJFJJFLJ-L7|7-.L7F----JJF----77L--J|L---JF7LJ|L---7L7|||FJF--J|||||L7|FJLJJ7|
            7-7|FLF--JF7L7L---7|L-7|L7|F7F7F7LJL-JF7F7L7.F----JF---J|7FLFJ|L7|JFJ.J|.LJ-.LL||F-----JF--7|F----JF----JL-7|F7F7L7LJ|LJFJ|F-J||||FJLJF7J|L7
            |-|-J.L-7FJL7L7FF-JL--J|-|LJLJLJL---7FJLJL7L-JF---7L--7F7777.L||LJ-F-J-F-.||-.FLJL7F----JF-J|L---7FJF---7F-J||LJL7L-7L7FJLFJF7LJLJL---JL7-FJ
            F-JJLJ77LJ|7L7L-JF7F7F7L-JF7F7F7F7F7LJF--7L--7L--7L--7LJL-777LLJ.LJL-L-|JLFJ.L|.F7LJF----JF7L----JL7L7F7LJF7LJF-7|F-JFJL--JFJ|F7F----7F7|-7|
            |L|F---J||-F7L7F7|||||L---JLJLJ||LJL7FJF-JF-7L---JF-7L--7FJ-7LLJ-|.|F|FL--F-L.F-J|L|L----7||F------J.LJL-7|L-7|FJ|L-7|F----JFJ||L--7LLJLJ7LF
            |FFF-J7.L77|L-J|LJLJLJF7F----7FJ|F--J|FJF7L7L-----JFJ7F7LJ|-|F7LLJ-L7F|JLF7-L-|F7L7F----7LJLJF7-F7F7F---7LJF7LJL7L--JLJF---7L7||F7FJ.FL|J|-|
            LL-77LF-LF-JF-7|F---7|||L---7||FJL---JL7|L-JF7F---7L--JL--7FLJ77.|FJ-F.F.|LJ|.LJL7|L---7L----JL-J||||F-7L--JL7F7L------JF--J7LJ||||J-J-|.L77
            L|FJ--JJJL-7|FJ|L7F7L-J|F7.FJ|LJF-----7LJF7FJ|L--7L--7F---J7J.|J-L77LJ7|.JJLFF.LFJL7JF7L7F----7F7|||LJLL----7LJ|F---7F--JF7F-7FLJLJ--|J|L.||
            .J7.L-|JFLFJ|L-JL||L7F7LJL7|FJ|FJF---7|F7|LJFL---JLF7||F7FFF-F|L.|-J7L|7FJ.F-..L|F7L-J||||F--7LJ|LJL7LF-----JF7|L--7LJF7FJLJFJF7F7..|F77-FL7
            L7LF77.7JJL-JF--7LJFJ||F-7LJ|F7L7|F--JLJLJLF7F7|F-7||LJ|L7-J-7L-7JLJJ.L77J-|L7--||L7F7L7LJL-7L--JF-7L7L7F7.F-J||F--JF7||L7F7L-JLJL7-FJL7-|.|
            LL7|-F||.|.JLL-7L--JFJLJFJF7LJL-J|L-7F-----JLJL7L7||L--JFJ7|LFL-7L7.L-JLFF-L----LJ.LJL7|F7F-JF7F7|FL7L7LJL-JF7|LJF7FJ||L7||L-7F---JFJF-J77-|
            F-J|-|LF7FLJ.FLL----JF7FJFJ|F7F-7|F-J|F-7F--7F7L7|||F---JJFJFF7L|.F7.|7LFJFLJFL-JLF---J|||L-7|||LJF7L7L---7FJLJF7||L7||FJ|L7-LJLF--JFJJFLF--
            L-7|-F-|7JJ.FFF7F7FF7||L-JL||||FJ|L--JL7LJF7LJL-J|||L-7F7.FFFFF.|7FL-|-7|FFF777LL7L---7||L7FJ|||F-J|FJF--7||-F7|LJ|FJLJ|||FJF--7L7F-JJ-|JL7|
            |LLJ7|7J|L7FF-JLJL-JLJL-7F7LJLJL7|F---7L7FJL--7F-JLJF7LJ|F7FF|J-LJJ.LFJ|J-7.-J7.LFF---J||FJL-J||L-7LJFJF-JLJFJLJF-JL7F7L-JL-JF7L-JL7FJ7F7LLF
            7|LJJJF.777FL----------7LJL--7F7LJL--7L-JL7F--J|F---JL--J||F7JJ.||7|77.L.L|..F7.F7L-7F7LJL7F-7LJF7L--JJL--7.|F-7L-7FJ|L7F7F--JL--7FJ7JFLLJL|
            LFF|-FF7|.FFF----------JF---7|||7F---JF---JL---JL--7F7.F7|LJL7F|--JJJ|7J-7-L7-F-JL77LJL7F7LJFJF-JL--------JFJL7L-7LJFJLLJ|L7F7F-7LJF7-LL7--L
            .LF|-FJ|F77-L-----------JF--JLJL7L----JF7F7F-7F7F-7LJL-JLJF7FJ7|||-7.7-JJ.L.|.L--7|7F-7LJL7FJJL------------JF7L-7|F7L---7L7LJ||FJF7||7LFJJJ|
            J.|J.L7LJL7-F7F7F-7LF7LF7L---7F7L------JLJLJLLJ|L7|F7F-7F-JLJJ|FF7-7JJ||7F.FJ.FF-JL7L7L---JL7JF7F-----------JL--J||L7F7FJ||F7LJL-J|||F7JL-7J
            |LJ|-LL-7FJ|||||L7|FJL7||F7F7LJ|F7F-----------7L7||||L7|L----7-F-7|J.F7FF7-L|-LL7F7L-JF7F7F7L-J||F7F-7F7F7F-----7|L7LJ|L-7|||F-7F7LJLJ|JJ.-F
            FJ-J-F7|||F7|LJL-J|L-7||LJLJL--J|LJF7F7F-7F7F7|FJ||||FJL7F---JF|FJF--F7FJ|.|F77FLJL7F7|LJLJL--7LJ|LJJLJ||LJF----JL7L-7|F7||||L7||L-7F-J.LJ.|
            7J7LL|L-JLJLJF7F-7L--JLJFSF7F---JF-JLJLJFJ|||||L-JLJLJF-J|JF7F7||F7.F||L7|F7||F-7F7LJLJF------JF7|F7F7|LJF-JF7F7F7|F7||||||||FJ|L-7|L7-F-.7J
            -.F7LL7F7F7F7|||FJF7F7F7|LJLJF--7L-----7L7|LJLJF-7-F7JL-7L7|||LJ|||FF||FJ|||||L7LJ|.F7FL---7F--JLJ||||F-7L--JLJ|||LJLJ||||||||FJF7|L-J-L7FL.
            L.F-F7LJLJ||||LJL7|LJLJLJ.F7FJF7L------J|LJF-7FL7L-JL7F7L-J||L-7LJL7FJ|L7LJ|||-|F-JFJL----7||F7F--JLJ|L7L--7F7FJ|L-7F7LJLJ|||||FJLJFJ.|FL-7.
            .FJJ|L----J|||LL|LJF--7F7FJLJFJL------7F7F7L7|F7|F--7LJL--7|L7-L7F-JL7L7L7FJ||FJL7FJF---7FJLJ|||F---7|FL--7LJLJFJF7LJL--7FJ|LJ||-F|7.F--7.|7
            .FJ|L--7F7FJLJF7F7.L-7LJ||F7FJF77F7F-7LJLJL-J||LJL7FJF----J|FJF-J|F7FJFJFJL7|||F-JL7|F--JL---J|LJF--JL7-F-JF7F7|FJL-----JL-JJ7||-F.|77LL-JJL
            -L7F|-|LJ||F7FJLJL7JFJF7LJ|||FJL-J|L7L7F7F7F7LJF7FJL7|F-7F7|L7L-7LJ||FJFL-7||LJL7F7||L-------7L-7L-7F7L7L--JLJLJL--7F7F-7F7F7-LJL|FF77.LJL||
            LL-L.L|J.||||L-7F7L7L-J|F7||||F7F7L-J-LJLJ||L7FJLJF7LJ|FJ|||FJ|FJF-J|L7F7FJ|L--7|||LJF-7F7F-7|F-JF7LJL7L----------7|||L7LJLJL--7-L7.L|-.LF7J
            F77LJF|J-LJ|L-7LJ|FJF7LLJLJLJ||LJ|F7-F--7JLJFJL-7FJ|F7||FJLJL-7L7L7||FJ||L7|F--J|||F7L7|||L7LJ|F7||7F7L7F------7F7LJ||FJF------JJ-7-F|.J-FFJ
            |F7-L|J|JF-JF7L--JL-JL-------J|F-J|L7|F7L-7FL---JL7LJ||||F----JFJFJFJ|FJ|FJ|L--7||||L7|LJL-JF7LJLJL-JL7LJF7F7F7LJL-7||L7L-7JF7F7-F7F-7F|.||.
            ||JLLJLLJL7FJL----7F----------JL--JFJ||L--JF7.F7F7L-7||LJL--7F-JFJFL7|L7|L7|F7FJLJ||FJL--7F7|L7F-----7L--JLJLJL-7F-J||||F7L-JLJ|-|LJFJ-J.L7|
            FL7L|.7.LFLJF7F7F7||F-------------7L-JL--7FJL-J||L77|||F----JL-7L--7||FJ|FJ|||L-7FJ|L--7FJ|||FJ|F7F7JL-----77F7.LJ-FJL-J|L7F---JJ|F-JJJFJLF|
            F---LFJFFFF-JLJ||LJ|L7F7F7F-7F---7L-7F---J|F---J|FJFJ||L77F7F7.|F--J|||FJL7||L7-|L7L-7FJL-JLJL7||LJL-------JFJL77F-JF-7FJ|||F7.|FJ|F-7L|JJ.|
            L7|F-|-F--JF7F7LJF7|JLJLJLJFJ|LF-JF7LJF7FFJ|F7F7|L7L7|L7|FJLJ|FJ|F7FJ||L7FJ|L7L-JFJFFJL7|F7|F7LJL7F--7F7F---JF7L-JF7L7||.FJLJL-7L7LJFJ.|F77|
            ||F-J|-L---JLJL7FJLJF7F----JFJFJF7|L--JL7L7||LJ||FJFJ|FJ||F--JL7|||L7LJFJ|FJJL--7L7FJF-JFJ|FJL7F7||F-J|LJF---JL---JL-J|L7|F----JFJF-J7F|-JF|
            F7JJF77L-F-----J|F7FJLJF7F-7|FJFJLJF-7F7L-J||F7||L7|FJ|FJ||F7F7||||.L-7L7||F7F7|L7||FJLFJFJL-7LJLJ|L-7|-FJF--7F7F----7|FJLJF7.F7L7|-J7-LJLF|
            |JJFFJ-7LL7F-7F7||LJF--JLJFJ|L7|JF7L7||L7F7|||LJ|FJ|L-J|.||||||||||F7FJFJ||||||F-J||L-7L7L7F7|F-7FJF-JL7L-JF7LJLJF---JLJF-7|L7||FJL7.|F|77..
            JL|..|-|7|||FJ|LJL--JF7F7.L-JFJ|FJ|FJLJ|LJLJ|L7FJL7|F--JFJ||LJLJ|||||L7L-JLJ|||L7FJ|F-J-|FJ||LJFJ|-L---J.F7||F7F7|F7|F7FJFJ|FJ|LJF7L7J.77FFL
            LFFL7FF|J-LJL-J7F77F7|LJL----JFJL7|L----7F7.|FJL7FJ|L--7L7|L-7F-J|LJL-JF----J|L7|L7|L7F7||FJL-7L-JF------JLJLJ||LJ|L-JLJFJ-||-|F-JL-J.L||-|7
            .-JJF7JJ|7F7JF7F||FJLJF-7F----JF-JL--7F-J||FJ|F7||FJF--JJ|L-7||F7L--7F-JF-7F-JFJL7LJFJ|||||F--JF-7L---------7|LJF7|F----JF-JL-JL-7F7JF-J.F77
            J.LJ--F--F||FJL-JLJF-7|FJ|F----JF7F-7LJF7||L7LJLJ|L7|F7F7L-7|||||F7FJ|F7L7|L-7|F7L-7|7|LJ||L7F7L7L7|F7F-----JF7L|||L--7F7|F--7F-7LJL7-7L7J||
            .F.|.|L7-FJLJF--7F7|FJ|L-JL-----JLJFJF7|||L-JF---JFJLJ||L7FJ||LJ|||L7LJL7||F7|LJ|F-J|FJF-J|FJ|L7|FJFJ|L----7J||FJLJF-7LJLJL7LLJFJF-7|F7-J-7J
            FL--L7-JFL-7FJFFJ|||L-J-F7-F7F-----JL||||L-7FJ-F7.L7F-JL7||FJL7FJ||FL7F-J|||||F-JL7FJ|FJF7||LL7|||-L7|F7.F-JFJLJF-7L7L7F-7FJF-7L7L7LJJJ7.FL.
            L|FFJ..LLLJLJF-JFJLJF---JL-JLJF----7FJ|||F7|L7FJL7FJ|F7FJ|||F7|L7|L-7|L7FJLJ|||F7FJ|FJL-J|||F7|||L7FJ||L7L--JF7FJ|L7L7||FJL7|FJFJFJ7.||F-|F7
            F7--7-.F|L|L|L7FJ7F-JF7F7F7F7FJF---J|FJ|LJ||FJL-7LJ-||||FJ|||LJFJ|F-J|FJL-7F||LJ||FJ|F---J|||||LJ|||FJ|FJF7F7||L7F7L-JLJL-7LJL7|FJ|FF--7||LJ
            |L-L-7FF7-|-F-J|F7L7FJLJLJLJLJ.L7F-7|L7L-7||L7F7L-7FJ|LJ|FJ|L-7L7|L-7|L7F-JFJL-7|||FJ|F-7FJLJ|L7F-J|L-JL7|||LJL7LJL------7|F-7||L-77J.|JF|F|
            |L-.L7L|F.JLL7FJ|L-J|F-7-F------JL7||L|F-J||FJ|L-7|L7|F7LJFJF7|FJ|F-J|FJL-7L7F7||||L7||FJL-7|L-JL-7L7F--J|LJF-7|F7F7F----J|L7|||F7L7.|-7LLL7
            F-J7-J|FJ..FLLJFJF-7|L7L7L-7F---7FJ||FJL7-||L7L7JLJFJLJL7||FJLJL7|L7FJL7F-J.||LJ|||FJLJL7F7|F-7F7F|FJL7F7L-7|FJLJLJ|L----7|FJLJLJ|FJ.-JF7-LJ
            LFJ7-|||-|7JJ|JL-JJLJFJFJF7LJF--JL7||L-7|FJL7L7L--7L7F-7L7|L--7LLJ7||F-JL--7|L7L|||L---7||||L7LJL-JL7-LJL-7LJL----7|F7F--J|L-7.|LLJJ.LF|JLJ|
            LJ-FJFJ|.LJ--F-JF--7JL7L-JL7LL7F-7|||F7||L7FJFJF7FJFJL7L7||F--JF---J||F7F-7|L-JFJ||F-7FJ||||.L-7F7F7L----7|F7F7F--J|||L--7|F7|--JJ|F|||L77.7
            |L--..7J-L||LJ.|L-7L--JF--7L--J|FJ||||LJL7||.L7||L7L7FJLLJ|L-77L---7|LJ|L7||F--JFJ||FJ|-LJ||F--J||||F7F7FJ|||||L-77||L--7|||||77JLL-JL7JF7FJ
            |7.|.FJL7JLFJFF7|FJF7F7|F7|F-7FJL7|LJL7F-J|L7FJ|L7|FJL--7FJF7L7F---JL7F|FJLJL7F-JFJ|L7L--7LJL7F7|||||||||FJ||||F7L7|L--7LJ||LJ7-7.|7FLJ.|JF.
            .L-7-JLFJ.F7--|-LL7||||||LJ|FJL7|LJFF7|L-7L7|L7L7|||F7F-JL7||FJ|F--7FJFJ|F-7J|L-7|FJFJF7FJJF7||||LJ||||||L7|||||L-JL7F7L7LLJ7|.L|7J|--J7F-|.
            FL.J.L7.J7|-FJ|7LL||||||L-7|L-7|F---JLJF-JFJL7|FJLJ|||L7F-J||||LJF-JL7L7LJFJFJF-J|L7L7||L-7|LJ||L-7LJLJ|L7LJ||LJF7F7||L7|7F-7J.7JF-J..--F-77
            F-77|.LL-J||J.|LJ7||LJ||F-JL7FJ||F-7F-7L7FJF7||L-7FJ||FJL-7||L--7L7F-JFJF7L7L7L-7|FJ.LJ|F7||F7||F7L7F--JFJF-JL7.||||LJFJL7|FJ7LF.|.7J7.|L7L7
            ||J7|-7.FLF.FFJ7FLLJF-J|L-7FJL7|||F||FL7||FJLJL7FJL7||||F7|||F7FJFJ|F-JFJL7|FJF7|||F---J||||||||||FJL-7FJJL7F7L-JLJL-7|F7LJL-7L--J7|LJ7F-7L7
            FJ-|JF7FL7J7FFFFJ7LL|F7|F-J|F7|||L7LJF-J||L---7|L7JLJ|L7|LJ|LJ|L7L7||F7|F-J|L7|LJLJ|F7F7|LJLJLJ|||L-7FJL--7LJ|F------JLJ|F--7|7J|7FJJLFJ-|7|
            |.FL|JLF.|FF7-LJJLJF||LJ|F7||LJ||FJF-JF7||F7F-JL-JF--JFJ|F7L-7L-JFJ|LJ|||F7L7|L7F7.LJ||||F-----J|L7FJL-7F-JF-JL--------7|L7.LJ7LFF|JF-JF|.|7
            L|7F|7FF-FJ.-7.LF.--||F-J|||L7FJ||FJF7|LJ|||L---7FJF-7|FJ||F7|F7F|FJF-J||||FJL7LJL7F-J|||L---7F7L7LJF--JL-7|F-7F7F7F-7FJ|FJ7LL7-|.F--J-F-FL7
            |L7J.L-L-|JF7|..|7L7LJL7FJ||FJL7LJL7|||JFJ||F7F-JL7||LJL7||||LJL7|L7L--J|||L77|F--JL7FJLJ-F--J|L7L-7L--7F-J|||||LJ|L7||FJL-7J|JFJ7|||.L|-J7|
            |.-77|77|J-LLJ-F7--FJJJLJ-LJL7FJF--J|||FJFJ|||L7F-JL7F--J||||F7FJL-JFF--J|L7|FJL---7LJF--7L--7L7L7FJF--J|F7LJFJL-7L7|LJL7F-J-|L7JL7FLF-J7|.F
            JF|7J7FFL7FJL|L-7-FJ|7|L|||-FJ|J|F-7|LJL7|FJ|L7|L7F7|L-7FJ||LJ||F-7F-JF7FJJ||L-7F-7L7.L-7L---JFJLLJLL--7LJL-7L7F-JFJL77LLJJJL7L7J||J|FJ7L-||
            F|L|JLF--F7-JF7F7J|-F-LJ.-7.L7|FJ|7|L-7|LJL7L7|L7LJLJF-J|FJ|F7|LJFJL7FJLJ--LJF-J|.|FJ-FFJF7F-7L----7F7FJF---JJ|L7JL7FJ7-L|-.||-JFF|-|LF|JF-J
            L-7.FF7|FL-J..FLJ||LFJ|77L|7F||L7|FJF-JF---JFJL-JF--7|F7|L7LJ||F7L7FJL--7|LF-JF7L7LJ77FL-JLJFJF----J||L7L-7F7FJFJ.LLJ-FF7L7F-J.LF---JF--7|.|
            |.777JF7LF7F7-L-JF-7|-J7F|F7FLJFLJL7|F7L-7F7L---7L-7LJ|||-L7FJ|||FJL7F7FJ7FL-7|L7L--7F-7F--7L7L---7FJL-JF7LJ|L-J.F7|7|.|F7|LLF-J|J..7LJ|F7F7
            L.|LF-JLF7J|7-LF|J|L7.FJ-FLF7JLF---J||L--J|L7F7FJF7L7FJ|L-7LJFJ|||7L|||L--7JFJL7|F7FJ|FJL-7L-JF7F7||F7F7||F7L----JL77LF--7J7.FJ.F---|--7J.J.
            .FL-LJ-7LLJL--.LL.7FJFJ77L.J|LFL---7|L7F-7L7LJLJ||L-JL7L7FJF-JFJ||-7|||F--J.L7FJLJLJ|||FF7L--7|||||||||||LJ|F7F-7F-JJFLJJLFF-L-JL-J-L|-F-.L.
            -7F|JF7.JJ7|||FLL-7-.JFFJJFL7.-F---JL7||LL7L7F7F7L7F7FJFJL7L-7L7LJ.LLJLJJ.L7FLJ7JJ.F-JL-JL---J||||||||||L-7LJ||-LJF7L7.|FFF|F|7F-77LFL.--JJ7
            J.FF-J...FLJF.||L7JFLJLFJ.--J-|L-7F7FJ||.FJFJ|LJL-J||||L7FJF-JFJJL.|7-JLL.||-JLJ.|.L7F7F----7FJLJ|||||||F7L7FJL---JL7L---JL|L|FJJ7L-|.FJ7FF7
            .-7||.F-L7|F-7|7L7FL7-||JFJJJ.FLFJ|||-LJ7L7L7L-7F7FJ|L-7||FL-7|L7FF-F-J.|F-||.LL--7L||LJF---JL--7|||||||||FJL--7F-7FJ.|.|-F--JLJLJ..L.F-F-LJ
            LJLJ-FL7.-J7-|JJ-7-||FF|-|J.FFJ|L-JLJL|-F-JFJF7LJ|L7L7FJ|L7J-||7|LL-7.FF-L.77F7.JLF7||LLL7F-7F7FJ||LJLJ||||J-|J|L7LJJ.7.F-.7..|7F|7.|-L7JL|7
            |L-.|F|J.|L|-L7|F7LFF77J.-J-7LJ-LJJ7||JFJF7L-J|-FJFJL|L7L7|L|LJJ77|7J.F--|7J|7.LJ7LFLJJ|.||FLJ|L7|L-7JL||LJ77FFL7|JF7---FJ-F-F7FLJ--7JLF.|L7
            ||LFLJL--F-F-7F7FJ-7FJLJ7.|-JFJF|J.-L|-L7|L7F-J-L-J.FJFJ.LJFJ-J-||J|.L.||LJ-|77FF7.7.LFF-JL-7-|FJ|F-JJJ||LJ7-|LJLJ7LJFJ7L..|FJLF7|7.L-7|F7F|
            FJF7LJ-FF.|.FL-||J.7J|7L|FJ-7|FJJ.7|JL7|LJFJL7J-JJJFJFJ-FJLJF--FJ7FJ7.7-L7L-JLF--L7LL--L--7FJ-LJ.|||LFFJL7J-7-JJF|77FJ7F|L-7L--J7J7|7||J|L|7
            LF7|.F77JF7FJ7LLJ7FL.LJF|-|LL-J.FF77|FJJ-LL7FJJL||.|FJ|LJ.|J|||FL--7-FL7-LJL7|L.FLFJ7.JJFFJL7FJJ-LJ7FL|F-JJJL..FF|JLJ.7FJJ.F.L7||.-JL-J-JJFJ
            L|J|-7J|FLJ-7F..FJ7-J.|L-JFJL.J7LLL-J.FL7LLLJ7|-J-F||.|JLF7F---JL7L7-7.JJF--L|.77L|F7J..-L--J7JJFL|-|-LJF|J-|FFL7J|J.F.LJL7JFL-JF7|7L||.L7|J
            FLF..||LJJ7-F-FF-J|F.FL7L7J-|7LFF7|J|-L-L7J|.L7F7FFLJ-J..||LJJ|.L|-J-L-7F|JJ7|.7J.LL7--|FJJLLF..|F-FJ7.|-JF--J77L.7|F-7.|FLF--7FJ-|-7LJFFJL7
            LL7-LL-L-L-J|--7JL|7-7.J.JJ.L7LFJJ--|-L-JLLLF-LJJ..L.LL-FLFJ.L-JLL-JLJ-|7.LF7-FL-|JJF7.J-JLLLL-JLJJJLF-J.-7.J.7J.-LJ.7..JJL|JLJJJJ|LFJLLJL--
            """;

}


