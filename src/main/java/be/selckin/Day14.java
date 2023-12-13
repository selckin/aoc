package be.selckin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day14 {

    public static final String PART1_EXAMPLE = """
            O....#....
            O.OO#....#
            .....##...
            OO.#O....O
            .O.....O#.
            O.#..O.#.#
            ..O..#O..O
            .......O..
            #....###..
            #OO..#....
            """;

    public static void main(String[] args) throws IOException {
        part1(PART1_EXAMPLE, 136);
        part1(PART1_INPUT, 109654);
        System.out.println("==============");
        part2(PART1_EXAMPLE, 64);
        part2(PART1_INPUT, 94876);
    }


    public static class Grid {
        public static final char rock = 'O';

        private final char[][] grid;
        private final int xLen;
        private final int yLen;

        public Grid(char[][] grid) {
            this.grid = grid;
            yLen = grid.length;
            xLen = grid[0].length;
        }

        private void rollNorth() {
            for (int x = 0; x < xLen; x++)
                for (int y = 0; y < yLen; y++)
                    if (grid[y][x] == rock)
                        move(x, y, 0, -1);
        }

        public void rollWest() {
            for (int y = 0; y < yLen; y++)
                for (int x = 0; x < xLen; x++)
                    if (grid[y][x] == rock)
                        move(x, y, -1, 0);
        }

        public void rollSouth() {
            for (int x = 0; x < xLen; x++)
                for (int y = yLen - 1; y >= 0; y--)
                    if (grid[y][x] == rock)
                        move(x, y, 0, 1);
        }

        public void rollEast() {
            for (int y = 0; y < yLen; y++)
                for (int x = xLen - 1; x >= 0; x--)
                    if (grid[y][x] == rock)
                        move(x, y, 1, 0);
        }

        private void move(int startX, int startY, int xOffset, int yOffset) {
            int x = startX;
            int y = startY;
            while (y + yOffset >= 0 && x + xOffset >= 0 && y + yOffset < yLen && x + xOffset < xLen && grid[y + yOffset][x + xOffset] == '.') {
                x += xOffset;
                y += yOffset;
            }
            grid[startY][startX] = '.';
            grid[y][x] = rock;
        }


        public long load() {
            long sum = 0;
            for (int y = 0; y < yLen; y++)
                for (int x = 0; x < xLen; x++)
                    if (grid[y][x] == rock)
                        sum += yLen - y;

            return sum;

        }

        public Grid copy() {
            char[][] copy = new char[yLen][xLen];
            for (int i = 0; i < grid.length; i++) {
                System.arraycopy(grid[i], 0, copy[i], 0, grid[i].length);
            }
            return new Grid(copy);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Grid other)) return false;
            return Arrays.deepEquals(grid, other.grid);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(grid);
        }
    }

    private static void part1(String input, long expected) {
        Grid grid = parse(input);

        grid.rollNorth();

        long sum = grid.load();


        if (sum != expected)
            throw new RuntimeException("" + sum);
    }

    private static void part2(String input, long expected) {
        Grid grid = parse(input);

        Map<Grid, Integer> seen = new HashMap<>();
        int end = 1_000_000_000;
        for (int i = 0; i < end; i++) {

            grid.rollNorth();
            grid.rollWest();
            grid.rollSouth();
            grid.rollEast();

            Integer previous = seen.get(grid);
            if (previous != null) {
                int loop = i - previous;
                while (i < end - loop) {
                    i += loop;
                }
            } else
                seen.put(grid.copy(), i);
        }
        long sum = grid.load();

        if (sum != expected)
            throw new RuntimeException("" + sum);
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


    public static final String PART1_INPUT = """
            .#..#..O..O.....#O..O#O.O#.#.##O........#O..O...O#.O.O....##..#O..#..O.O#.O..#.O#O.#.#...#...#....O.
            #.O#.##..O.OO#..OO..O...O...#.O..#O...#..O...OO#....O...O#O...OO..##...O.O.O.....OO..#......O..##...
            ..........O..O.OO...O.O....O...O..O..#O#O.O.#........O..O.O.O..O##.#O...O#...O..O....#...OO.#O..#.#.
            ..O.O..O.O#....O...O#.....O.....##O..O#.O...O......#O#...OO...##O....O.O#OO#.....O##.....#O.OO....#O
            OOO...O....##...#OO.O.O.#OO.O#O.O.O..O.OO.##.O...O.........O...#.#O.......O#...O###.#...#O.#.....#..
            O..#..#......O...OO..#....#..O...O#.#...O#...#.##...#.#....O..O#.......O.......O..O.O......##..O....
            .....###..O#........OOO.#...#O.O#O..OO...O#...O..................#....O..##.#.O.O.#.#O...#.##.O...##
            OOOO......#O..O.#...O..O..O...#....#..O.#O..OOO.##....#.O......#.#.#O.O.O.#.O##.OOOO.O.#O.#O###..O.#
            #.....O#..O...O..O.O..#...O..#..#..#.....#..O.#OO...#.O.....#..O.....O.O.#O#......#.O.O#...#.O..#...
            O....O....O....OO.O...#......O.O.#O........O#.....#...........O.#.O.##.....O......#..#....O.....#...
            .........O##.###....#......#....#O.O..#...O.O.....O..OO.O...O.OO..#.O.#OO..#.#...O.##.OO..#.OO.O....
            ......O###....O.#..O..OO#O.......#.......#O..#...O.O......O...O.....###.#O..#O.O...#O.#OO...##O..O.#
            ....#O#.....O...O........#.##O.O..O....#.....O.O#...#.##....##.....O.O.#.O..O#.O..O#...........O.OO.
            #.O......#.##...OO...#.#.....#.###.OO..##O##.O.#.....O#..OOO...O.#O#..O...#..#....##......##.OO..O..
            OOO.##OOOO#.........O.#OOOO...O....OO#O##.O......#.........#.O..#....O..#....#..#....#..#.O....#..OO
            .#O......O#...........O.O....#.O....#...##..O......#.O..#..OOO.#....#...#O.....O....#OO..#.#O..O....
            ...#.....##..O..##...##...O#OO.O..O#..O##..O.....#O#..O...O#...O..##OO...OO#..O...#.O#...##....O.O#O
            .......#.....O...#O..OO.O#O.....O.......O#.O...O.O#...O.#...O...O......O.O.##........#...#.##..O....
            #O....O.O...#.OO....O#.O........O.....#....OO..#O#..O.O#.#.O.##OO...O.#..O......O#.#..O.###.....OO..
            ....O..OO.O..O..O#.........O..O...O.#..#....O.O.#.#O....O....O#.#......O..#.#..#.#..O.#.##.##O.O#.#O
            O......O#.O.....O.#....O......OO....O.#O.O....#...O#.........O..O.O.O..........O....#.....O#...O.OO#
            #OO...O...O...O.......##..O..##O..##......O...O...#O.O.OOO..O........#..##.OO.O.#.......O..#..O...OO
            ...#.#.O#OO.O.#...#.OO.....OO..#.O..#.OO......OO..........OO..O#...........O.#.#O.....#.O..O#..##...
            ..O..O..##.OO...O...O.#......O.#....#.O#..OOOO#.##.O......#.O...O.OO....OOOOO.........#.O.O....O..#.
            .##.#...O#...OO.#.......#.##.O.......O.O.#.#O#..O#O#...O...#.O.............O......OO...O.......OO.##
            ...OO..#.O..O.#...O...........O##OOO...O.O##...O.#...O....#..O.O.OO....##O#.#O...#O.#...OO#.##.#.OO.
            #...#......#.OO..#.#.O.O.....O..O..O#..#.....O.#..#.#.O.#O.#OO......OO.....O.##OO.#...OOOO#OO.....O.
            .....###O##.##.......##O.##.O...#..OO....OO.O.........O#.....OOOO....#OO#...#O.OOO..O#....O.......#.
            ........#O.O..#O#O#O..OOO....O.....O.#..#.......O.....#....O.....#.O##..O#....OO.#..O#.O........#...
            .#O...#..#.......#...O..O.O.#OO..#.O.#O#....O.#O..#....#..OO.#OOO.O#.O.#...O.O..O....#.#.O.......#..
            O.......##.O#...OO.O#O.O.O....O#.....O...O..#.....#O..O..#.#O.##....O...#O...##..O....O...#OO.#.O...
            .O.#..O.OO.O##..OO.##.#O#O..OO..O..........OO...O###..............O.....#.##...O.....#.OO.O#..O#O...
            O.#....O.#.##..#...O....#...#.OO.#.##.#...#.#O.#.##O.....##O.O....O..O..O##.......O#...O##.......OO.
            ...#......#OO.........#..OO.O.O........#..O...O.O....O#O..OO#...#..#O..#.#OO....O.#..O.O.........O.O
            ...O...O..O#.#O#O#O#O.#..##..##.OO##........O.O......##.O.O.#..#.....O#O#..O.O.#....OO...#.O...O#O.O
            ##....#.....O.......#OOOOO..O....#...#........#....O.#....#.#..#.O##.##....O...#.#O...O...O..O......
            ..OO..O...O#.O#..#....##...#........###.O.OO...##.......#..#....O..O..O.#OOO..#..#O........O..OO...O
            ..#...#..........O..O.###..OO..O.O....#..#.OO#....#.#...O#..O..O..OO##....O.#...#.#...#..#.#......O.
            ..OO..#.O.........O#.O.OO.O..O.O#.OOO..O..#.#.O#....O#.OO..O#..##.#..#O#..#..O.#.O#...O...O..#O....O
            ..O#..O..O#O...#..#.OO.....#....OOOO.....O...O..OO.O.#.O...#OO.O##O..O#....##.OO#.......O.#....#.#..
            ......O.#......#....OO.#.O...#OO.#O..#....O#.O.OOOO..OO.O....##O.#....O#..OO#.OO#O##..O.#........O..
            .O...........#O...O..O#..#..O.O..#O#.#..O..OO..O.O.#O...#..OOO.#...OO.....O........O...##..#...O...#
            ......#O...O...#O.O...O..#OO....O..O.#OO..O....O#..#OO...#.#.#.##OOO.O.#OO..OO#........#.##.O..#....
            OOO.O..O.O....O.....O##.......#OO...#O.......O..#O....O....OO..O.#.........O.O.OO.....O#.........OO#
            .#.OO......O...#.#..O....O...O...............O..O...O.OO....O.#O....#........#...#.#......O......O..
            #.......#O.#...###....O.O....#.###.OO..#OO....#....O.....#O.OO##OO..O.O....#O..#......#..#O#...#.O..
            OOOO..#...##O........#....OO.....O.O##..#O..OO..O#.##O..O#...#.OOOO##...........#.#O...#.O#.O....O.O
            O#..O#..#.O..#.O.OO.O#O.O..#......O.#...O#O#..O..OO.#..O.OO#..OOO............O.....OOO#.O###.....##.
            O....##.........O.O.O.O..OOO...O..O.O....O......#..........#...#.....#..#..#.....#.###..#...O#......
            OOOO...#O##.O.O#...#.#..OO.O#...#O.O.....#....OOO....#..#O..##.....OO...O..OO#...O..OOO...#.O.O...OO
            ..#O#...O......O##..OO#..#O#..#O#.O...#.###.O.O....O.....#..OO.O...O.#.......#..#.#.#.....#O..O.O...
            ..#....O.O.......OO..O#..OOOO..#.........#OO......O..O...O..O.###.......#O#...#.O..OOO.......O#.#.#.
            .O.O..#........OOO.O.##..#.O...O.O.OO..#..O#O.##.OOO#.OO...#....OO#O#.......O##.....O....#OOO.O...O.
            #....O.......#..#OO.#.#.O....#..#.#...O..O.O..O....OOO....#.O.#OOOO.#.......#.#.OO...OOO....#..#....
            .OO.O.O......O#.O....#......#O..##.....O.#..##..O...O.O...OO..OO..#..O..O....#......O.O.......#O.#.O
            ....O.#..O....O#.OOO.O..#....O..O...#.OO........O.#OO#O#.##..O..O##.O#...O......O....#O#..#...#O.#..
            #.....#..O#..O..OOO..O.#....#.......OO.O##.#......OO.....OO....O.O...#.#.O#...OO.......OO....#.....#
            ....O.OO#....#O#..O..#O.....#..O...O...O.##...O....#.....#.O....O##O..O#O.....O..##...O....O.O#.....
            ..O.#.#..OO......#..#.#.#.#.....O..#O..O.........O#...O##.#.....#O.O..O....#.##O.#O..OO...##....#..O
            O......#............#.....##O..O..OO.....##..O..O.......O.......#OO...#O.....O.O.O.......OOO.O.#OOO.
            O..OO.#....#...O..O...##..OO...O........OO....O..........#OO.....#..#O...O#.#....#O..O#..#.O......O.
            ..O#.##O..O.#..#.......O...#...O..O.#O....#.#....#O#.O..#O...O..#OO.O#O.....O....#.....#..O.......#.
            .O.O..OO.O...O.#O....OOO..O.O................O..##......OO.O.......OO.O................#.O....OOO#O.
            ......O.O.#O#...O#..#.........#O#.#O......O...#O....#.#O..O...O..#...O.O.#OO.O...#.........OO..OO...
            .#.O..O...#O.##.O.......##....O...##.......OOO..O...#.O#.......#...OO#.#.#......#O..#OO..O...OO...O.
            .O....O........#O.#..#.#...##.#..........O..O.#..#..#.##..#.#.#.#..#......O#........OO.....OO.#..#..
            #O..O.#....O.#..OO#......#.#.O.O..O.##.#.....#O..OO..O......O#...O#..O..O.....OO.#........O......#..
            O.#....O..#....O.O.OO#O..#...O##..OO#......O.#....O...#.O....##..O.O#OO..O......O.O.......#...O.O.#.
            ..O......O..OO.OOO.....#...O...#..#.#...OOO.O.#O...O.#O.....OO.O.....O.O#.O.O..##..............OO...
            ...OO#....O....#.......OOOO....#OO.O.O.O##.#.O..O.O......#.............#......O.O....#O##O.#.O.#O...
            .#..O..#..#O...##O..O#O...#..O#...#.O.........O..O.O......O##O.#O.....O.O..##.#.........#.......O...
            .O.OOOO.#...##.O#O#.#..#...#..OO..O##..##..OO.#...#...#....O..#O#.#...OO..O..O..#O..#.#OO.O#..O.O.##
            ..O...#....O#........O......OO.......O..##.O...O...#O..O.....#...O#...O.O#.#.O.#.#OO.##.....O..O..O.
            .......#.O...O....O.O..#O.O..#O.#....##...................O#OO.....#....OO...O...##.#.O..#O#..#.O#..
            .OO.#...##....#O#.........OOOO.OO.#.#.O.#....#......O.O.O#..###.O#..#..##....OO.#..#....#.#.....O#..
            O.O...O..#....O.#......O##..#.O..#OO#O..........#O.OOO.......O......#OO#.....#..O#.#...##O..O.O.O...
            .....O.....#O###..........O...O...#O.O.#.O.....O..O...O...#O...##...O#.#...OO.........O...#.##O....#
            #OO.OO..##..O.O..OO.OO.O#.##.O.O..O##OO..#...#.O...OO.#.#...OO#.OO.....OO...##O..O...O....#.O...O##O
            #.O...O.#O.##O..##.#.#O.#OO.O.O.O.#..............#...#.O...#.O..#O..O.O.O.#.O..OO#..O#..#..O..#.#...
            .OO.#.O.O.O....O##O..O...O..O.##....#....#.......O...O.OO..#...#.O..#O....O......OO#O........##.O...
            ..#.......#.....##O.###..OO.#....O.O.#..#.O.#O#O##..##.....#.#.........#...O........#..#..#...#O..#.
            ..O#OO.#.OO#.OO...##..O..O.O.O......O.....#.....#O#.#.#O.#......#...O.....#O..O.#O...#.OOOO.OO......
            ....#...#......O#.......#......#O....O.O#O......O..O...O..##..OOO.O.#..#.O.OO..#.#..#...O.#......O..
            O..O....O#....O...##O...#O##.#.....O..#.....#.#......#......O.....#.#.#..O.OO#..O...OO..OOOO.O......
            .O....O..O...OO....O.....O#.......O........O....O....O.......O..O..#..OO..#O...#......##.#....O..OO#
            .....#..OO.O.#.....O.O.O#O.#O#..O.O..O...#.#.O.O...#....O..#..#...##OO..#.#..#O.O...#.O#O......O.#..
            O..##.#.O..#.O#.#O....#O.##..#O.#....O...OOO.O......O....#.O..##..#.O...#........#.#..O..OO.O..###..
            ....#O...O.O......#..O.O..........#.#OO.O#..#O..OO...#.O.O.#.#O...O..#...#..O#.O.OO..O........#OO...
            ..#...O......O.##...#O.#.O...O#O..O..O......O#.......#...#O...O#.#.#........#.....O##O.#..#O#..OO..#
            #O#O#.O#..#.O...OO..........##.O#.....#.O...O.....O....O#OO..#O..O.....OO.#..#..O....#.O..O......O.#
            OO#.#.#......#......###.#.........#..OO.O.O#.....O#O.#.O...O##.....#..##OO..OO#.###.O.O.......#.O.##
            #.#O...#....#..#.......O......O..##....#..##..O...##O#..OO.....O..OOO.OO.O.#OO....O...#.......O..#..
            .O.#O.....#.O......OO..O....###.#...O.O...#....#OO..........O#O....O.........O..##OO.O..#.#..O##.#..
            .#..O..O.OOO.OOO.OO.O........##...OO#.O.O..O...O..O..#O.#.O.....OO...#....OO.#..O#.#.......#.O#....O
            ....O.O#.O#...OO...#..#.##.OOO.....O.O.O.....OO..OO..OOO.#.......O...O...#....#O...O#.O...O##.##.#..
            ##O#...O....O..OO.O..##OO.O#.O.....#.#.O.O.......O#...#..#.OO...#...#..OOO###.......#.O.....O.#O...O
            #.O..#.O.....OO..OO.#....###.#.#O..OO..........#O...O.O....O..##O..#..O..O.#OO...#....#...##..#.....
            #..O..###..#.O.........OO...#O........O..O..O...O......O....O...O...##.....O.......#.#...O#....O.O..
            .#O...O#....#....#...O.#...#O..#...O#...O.OO......O..OOO..#..O.#.O..O..#O.......O####O#.OO#..O.O.O..
            ...#.#O#..#........O.#O....OO....O#..#.#.O..O....O....#.O#.#.#......O.#......O#..OO...##O.#O.....O#O
            """;

}


