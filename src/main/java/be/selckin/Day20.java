package be.selckin;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.*;

import static be.selckin.Day20.Pulse.HIGH;
import static be.selckin.Day20.Pulse.LOW;
import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.attribute.Rank.RankDir.TOP_TO_BOTTOM;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class Day20 {

    public static final String PART1_EXAMPLE = """
            broadcaster -> a, b, c
            %a -> b
            %b -> c
            %c -> inv
            &inv -> a
            """;

    public static final String PART1_EXAMPLE2 = """
            broadcaster -> a
            %a -> inv, con
            &inv -> b
            %b -> con
            &con -> output
            """;

    public static void main(String[] args) throws IOException {
        part1(PART1_EXAMPLE, 32000000);
        part1(PART1_EXAMPLE2, 11687500);
        part1(PART1_INPUT, 812721756);
        System.out.println("==============");
        part2(PART1_INPUT, 233338595643977L);

    }


    public enum Pulse {
        HIGH, LOW
    }

    public static class Module {
        public char type;
        public String name;
        public List<String> outputs;

        // %
        private boolean flipFlop = false;

        // &
        private Map<String, Pulse> memory = new HashMap<>();

        public Module(char type, String name, List<String> outputs) {
            this.type = type;
            this.name = name;
            this.outputs = outputs;
        }

        public List<Signal> on(Signal signal) {
            if (type == '>')
                return output(LOW);
            if (type == '#') {
                return List.of();
            }

            if (type == '%') {
                if (signal.pulse() == LOW) {
                    flipFlop = !flipFlop;
                    return output(flipFlop ? HIGH : LOW);
                } else
                    return List.of();
            }

            if (type == '&') {
                memory.put(signal.source(), signal.pulse());
                return output(outputSignal());
            }

            throw new RuntimeException();
        }

        public Pulse outputSignal() {
            return memory.values().stream().allMatch(pulse -> pulse == HIGH) ? LOW : HIGH;
        }

        private List<Signal> output(Pulse pulse) {
            return outputs.stream().map(output -> new Signal(name, output, pulse)).toList();
        }

        public void connect(String name) {
            if (type == '&') {
                memory.put(name, LOW);
            }
        }
    }

    public record Signal(String source, String target, Pulse pulse) {
    }

    private static void part1(String input, long expected) {
        Map<String, Module> modules = parse(input);

        long low = 0;
        long high = 0;
        Deque<Signal> pending = new ArrayDeque<>();

        for (int i = 0; i < 1000; i++) {
            pending.add(new Signal("button", "broadcaster", LOW));

            while (!pending.isEmpty()) {
                Signal signal = pending.pop();
                switch (signal.pulse) {
                    case HIGH -> high++;
                    case LOW -> low++;
                }

                if (!"rx".equals(signal.target()))
                    pending.addAll(modules.get(signal.target()).on(signal));

            }
        }

        long result = high * low;

        if (expected != result)
            throw new RuntimeException("Expected " + expected + " but got " + result);
    }

    private static void part2(String input, long expected) {

        Map<String, Module> modules = parse(input);


        draw(modules);


        List<List<Module>> numbers = new ArrayList<>();
        for (String output : modules.get("broadcaster").outputs) {
            List<Module> number = new ArrayList<>();
            collect(modules, number, output);
            numbers.add(number);
        }

        List<Module> rollOverModule = new ArrayList<>();
        for (String output : modules.get("broadcaster").outputs) {
            for (String next : modules.get(output).outputs) {
                Module nextMod = modules.get(next);
                if (nextMod.type == '&') {
                    rollOverModule.add(nextMod);
                }
            }
        }


        int count = 0;

        long[] rollOverCount = new long[rollOverModule.size()];

        boolean finished = false;
        Deque<Signal> pending = new ArrayDeque<>();
        while (!finished) {
            count++;
            pending.add(new Signal("button", "broadcaster", LOW));

            while (!pending.isEmpty()) {
                Signal signal = pending.pop();

                if (!"rx".equals(signal.target()))
                    pending.addAll(modules.get(signal.target()).on(signal));
                else if (signal.pulse == LOW) {
                    finished = true;
                }

                for (int i = 0; i < rollOverModule.size(); i++) {
                    if (rollOverCount[i] == 0) {
                        Module module = rollOverModule.get(i);
                        if (signal.source.equals(module.name) && signal.pulse == LOW) {
                            rollOverCount[i] = count;
                            System.out.println("Roll over " + i + " at " + count);
                        }
                    }

                }
            }

            System.out.print(count + ": ");
            for (List<Module> digits : numbers) {
                int num = 0;
                for (int i = 0; i < digits.size(); i++) {
                    if (digits.get(i).flipFlop)
                        num += 1 << i;
                }
                System.out.print(num + " ");

            }
            System.out.println();


            if (count > 4100)
                break;
        }

        long lcm = 0;
        for (long i : rollOverCount) {
            if (lcm == 0)
                lcm = i;
            else
                lcm = Day8.lcm(lcm, i);
        }
        System.out.println(lcm);

        long result = lcm;
        if (expected != result)
            throw new RuntimeException("Expected " + expected + " but got " + result);
    }

    private static void collect(Map<String, Module> modules, List<Module> result, String output) {
        Module module = modules.get(output);
        if (module == null) {
            return;
        }
        if (module.type != '%')
            return;

        result.add(module);

        for (String next : module.outputs) {
            collect(modules, result, next);
        }
    }

    private static void draw(Map<String, Module> modules) {
        List<Node> nodes = new ArrayList<>();
        for (Module module : modules.values()) {

            Node node = node(module.name).with(module.type == '&' ? Color.RED : Color.GREEN);
            for (String output : module.outputs) {
                node = node.link(output);
            }
            nodes.add(node);
        }

        Graph g = graph("aoc").directed()
                .graphAttr().with(Rank.dir(TOP_TO_BOTTOM))

                .linkAttr().with("class", "link-class")
                .with(nodes);
        try {
            Graphviz.fromGraph(g).height(2000).render(Format.PNG).toFile(new File("day20.png"));

        } catch (IOException ex) {
            throw new RuntimeException();
        }
    }

    private static Map<String, Module> parse(String input) {
        Map<String, Module> modules = new HashMap<>();
        modules.put("output", new Module('#', "output", List.of()));
        input.lines().forEach(line -> {
            String[] split = line.split(" -> ");
            List<String> targets = List.of(split[1].split(", "));
            String name = split[0];
            Module module;
            if ("broadcaster".equals(name)) {
                module = new Module('>', "broadcaster", targets);
            } else {
                module = new Module(split[0].charAt(0), name.substring(1), targets);
            }
            modules.put(module.name, module);
        });


        for (Module module : modules.values()) {
            for (String output : module.outputs) {
                Optional.ofNullable(modules.get(output))
                        .ifPresent(m -> m.connect(module.name));
            }
        }
        return modules;
    }

    public static final String PART1_INPUT = """
            %np -> vn
            &lv -> rx
            %rt -> ns
            %th -> bc
            %gt -> rt, db
            %zf -> db, np
            %sg -> fs, gr
            %vn -> db, zj
            %qh -> ms, lz
            %rv -> rj, vc
            %br -> lz, qh
            %pc -> jq, vc
            %dk -> xl
            %qq -> th, gr
            %ns -> xv
            &vc -> gl, tv, pc, qd, tn, dg
            %bd -> lz, vm
            %ms -> lz, bd
            %dg -> rv
            %cf -> vc
            %kc -> cq, db
            %ds -> dk, lz
            %zj -> kc
            %qm -> db, zf
            %gl -> qd
            %hf -> db
            %hx -> px, gr
            %fk -> tv, vc
            %tp -> ld
            %gg -> rq, gr
            %xl -> gj, lz
            %vm -> lz
            %qf -> lz, vr
            %px -> qq
            %fs -> tp
            %bc -> cd, gr
            %vr -> xz, lz
            %xv -> qm, db
            %rq -> gr
            %cq -> hf, db
            &lz -> dt, dk, qf
            &gr -> tp, fs, px, st, th, sg
            &st -> lv
            &tn -> lv
            %xz -> ds, lz
            &hh -> lv
            &db -> np, gt, zj, ns, hh, rt
            %qd -> dg
            %jq -> vc, fk
            %jp -> cf, vc
            %rj -> jp, vc
            %tv -> kz
            %cd -> gg, gr
            &dt -> lv
            %ld -> hx, gr
            %kz -> gl, vc
            broadcaster -> pc, sg, qf, gt
            %gj -> lz, br
            """;
}



