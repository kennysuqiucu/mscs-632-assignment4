// Employee shift scheduler - Java version
// 2 shifts a day, 2 people per shift, max 5 days per employee

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ShiftScheduler {

    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    static final String[] SHIFTS = {"morning", "evening"};
    static final int MAX_DAYS = 5;
    static final int PER_SHIFT = 2;
    static final String DATA_FILE = "sample_data.csv";

    // name -> list of 7 days, each day is a list of shift numbers in order of preference
    static Map<String, List<List<Integer>>> prefs = new LinkedHashMap<>();
    // schedule[day][shift] = names
    @SuppressWarnings("unchecked")
    static List<String>[][] schedule = new ArrayList[7][2];
    static Map<String, Integer> daysWorked = new HashMap<>();
    static List<String> notes = new ArrayList<>();
    static Random rand = new Random();

    static List<Integer> parseShifts(String text) {
        text = text.trim().toLowerCase();
        if (text.isEmpty()) return null;

        List<Integer> result = new ArrayList<>();
        if (text.equals("off")) return result;

        for (String w : text.split("\\s+")) {
            int s = Arrays.asList(SHIFTS).indexOf(w);
            if (s == -1 || result.contains(s)) return null;
            result.add(s);
        }
        return result;
    }

    static boolean loadFile() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(DATA_FILE));
        } catch (IOException e) {
            System.out.println("Could not find " + DATA_FILE + ", switching to manual entry.");
            return false;
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).trim().split(",");
            if (parts.length != 8) continue;

            List<List<Integer>> week = new ArrayList<>();
            for (int d = 0; d < 7; d++) {
                List<Integer> shifts = parseShifts(parts[d + 1]);
                if (shifts == null) shifts = new ArrayList<>();
                week.add(shifts);
            }
            prefs.put(parts[0].trim(), week);
        }
        return !prefs.isEmpty();
    }

    static void getInput(Scanner sc) {
        System.out.print("Load employees from " + DATA_FILE + "? (y/n): ");
        String ans = sc.nextLine().trim().toLowerCase();
        if (!ans.equals("n") && loadFile()) return;

        System.out.print("How many employees? ");
        String count = sc.nextLine().trim();
        while (!count.matches("\\d+") || Integer.parseInt(count) < 1) {
            System.out.print("Enter a number: ");
            count = sc.nextLine().trim();
        }

        System.out.println("Enter shifts in order of preference (ex: evening morning), or off");
        for (int i = 0; i < Integer.parseInt(count); i++) {
            System.out.print("\nEmployee " + (i + 1) + " name: ");
            String name = sc.nextLine().trim();
            while (name.isEmpty() || prefs.containsKey(name)) {
                System.out.print("Enter a different name: ");
                name = sc.nextLine().trim();
            }

            List<List<Integer>> week = new ArrayList<>();
            for (String day : DAYS) {
                System.out.print("  " + day + ": ");
                List<Integer> shifts = parseShifts(sc.nextLine());
                while (shifts == null) {
                    System.out.println("  Use morning, evening, or off");
                    System.out.print("  " + day + ": ");
                    shifts = parseShifts(sc.nextLine());
                }
                week.add(shifts);
            }
            prefs.put(name, week);
        }
    }

    static boolean isWorking(String name, int day) {
        return schedule[day][0].contains(name) || schedule[day][1].contains(name);
    }

    static boolean hasRoom(int day, int shift) {
        return schedule[day][shift].size() < PER_SHIFT;
    }

    static void add(String name, int day, int shift) {
        schedule[day][shift].add(name);
        daysWorked.put(name, daysWorked.get(name) + 1);
    }

    static void place(String name, int day) {
        List<Integer> wanted = prefs.get(name).get(day);
        if (wanted.isEmpty() || daysWorked.get(name) >= MAX_DAYS || isWorking(name, day)) return;

        // ranked shifts first, then whatever is left
        List<Integer> order = new ArrayList<>(wanted);
        for (int s = 0; s < 2; s++) {
            if (!order.contains(s)) order.add(s);
        }
        int first = order.get(0);

        if (hasRoom(day, first)) {
            add(name, day, first);
            return;
        }

        // first choice is full, try the other shift that day
        for (int i = 1; i < order.size(); i++) {
            int s = order.get(i);
            if (hasRoom(day, s)) {
                add(name, day, s);
                notes.add("Conflict: " + name + " wanted " + DAYS[day] + " " + SHIFTS[first]
                        + " (full) -> moved to " + DAYS[day] + " " + SHIFTS[s]);
                return;
            }
        }

        // whole day is full, try the next day
        if (day < 6 && !isWorking(name, day + 1)) {
            for (int s : order) {
                if (hasRoom(day + 1, s)) {
                    add(name, day + 1, s);
                    notes.add("Conflict: " + name + " wanted " + DAYS[day] + " " + SHIFTS[first]
                            + " (day full) -> moved to " + DAYS[day + 1] + " " + SHIFTS[s]);
                    return;
                }
            }
        }

        notes.add("Conflict: " + name + " could not be placed on " + DAYS[day]);
    }

    static void fillShifts(int day) {
        for (int s = 0; s < 2; s++) {
            while (schedule[day][s].size() < PER_SHIFT) {
                List<String> available = new ArrayList<>();
                for (String name : prefs.keySet()) {
                    if (daysWorked.get(name) < MAX_DAYS && !isWorking(name, day)) {
                        available.add(name);
                    }
                }
                if (available.isEmpty()) {
                    notes.add("WARNING: nobody left to cover " + DAYS[day] + " " + SHIFTS[s]);
                    break;
                }
                String name = available.get(rand.nextInt(available.size()));
                add(name, day, s);
                notes.add("Fill: " + name + " randomly assigned to " + DAYS[day] + " " + SHIFTS[s]);
            }
        }
    }

    static void makeSchedule() {
        for (int d = 0; d < 7; d++) {
            schedule[d][0] = new ArrayList<>();
            schedule[d][1] = new ArrayList<>();
        }
        for (String name : prefs.keySet()) {
            daysWorked.put(name, 0);
        }

        for (int d = 0; d < 7; d++) {
            List<String> names = new ArrayList<>(prefs.keySet());
            Collections.shuffle(names, rand);
            for (String name : names) {
                place(name, d);
            }
        }

        // go back and fill any shift with less than 2 people
        for (int d = 0; d < 7; d++) {
            fillShifts(d);
        }
    }

    static void printSchedule() {
        System.out.println("\nSCHEDULING NOTES (Java)");
        if (notes.isEmpty()) System.out.println("  No conflicts or fills needed");
        for (String note : notes) {
            System.out.println("  " + note);
        }

        String line = "-".repeat(50);
        System.out.println("\nFINAL WEEKLY SCHEDULE (Java)");
        System.out.println(line);
        System.out.printf("%-10s| %-18s| %-18s%n", "Day", "Morning", "Evening");
        System.out.println(line);
        for (int d = 0; d < 7; d++) {
            System.out.printf("%-10s| %-18s| %-18s%n", DAYS[d],
                    String.join(", ", schedule[d][0]), String.join(", ", schedule[d][1]));
        }
        System.out.println(line);

        System.out.println("\nDAYS WORKED (Java)");
        for (String name : prefs.keySet()) {
            System.out.printf("  %-10s%d%n", name, daysWorked.get(name));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Employee Shift Scheduler (Java) ===");
        Scanner sc = new Scanner(System.in);
        getInput(sc);
        makeSchedule();
        printSchedule();
    }
}
