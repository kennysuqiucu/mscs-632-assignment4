# Employee shift scheduler - Python version
# 2 shifts a day, 2 people per shift, max 5 days per employee

import random

DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
SHIFTS = ["morning", "evening"]
MAX_DAYS = 5
PER_SHIFT = 2
DATA_FILE = "sample_data.csv"

prefs = {}       # name -> {day: [shifts in order of preference]}
schedule = {}    # day -> {shift: [names]}
days_worked = {}
notes = []


def parse_shifts(text):
    words = text.strip().lower().split()
    if not words:
        return None
    if words == ["off"]:
        return []
    for w in words:
        if w not in SHIFTS or words.count(w) > 1:
            return None
    return words


def load_file():
    try:
        f = open(DATA_FILE)
    except FileNotFoundError:
        print("Could not find " + DATA_FILE + ", switching to manual entry.")
        return False

    lines = f.readlines()
    f.close()

    for line in lines[1:]:
        parts = line.strip().split(",")
        if len(parts) != 8:
            continue
        name = parts[0].strip()
        prefs[name] = {}
        for i in range(7):
            shifts = parse_shifts(parts[i + 1])
            if shifts is None:
                shifts = []
            prefs[name][DAYS[i]] = shifts

    return len(prefs) > 0


def get_input():
    ans = input("Load employees from " + DATA_FILE + "? (y/n): ").strip().lower()
    if ans != "n" and load_file():
        return

    count = input("How many employees? ").strip()
    while not count.isdigit() or int(count) < 1:
        count = input("Enter a number: ").strip()

    print("Enter shifts in order of preference (ex: evening morning), or off")
    for i in range(int(count)):
        name = input("\nEmployee " + str(i + 1) + " name: ").strip()
        while name == "" or name in prefs:
            name = input("Enter a different name: ").strip()

        prefs[name] = {}
        for day in DAYS:
            shifts = parse_shifts(input("  " + day + ": "))
            while shifts is None:
                print("  Use morning, evening, or off")
                shifts = parse_shifts(input("  " + day + ": "))
            prefs[name][day] = shifts


def is_working(name, day):
    for shift in SHIFTS:
        if name in schedule[day][shift]:
            return True
    return False


def add(name, day, shift):
    schedule[day][shift].append(name)
    days_worked[name] += 1


def place(name, d):
    day = DAYS[d]
    wanted = prefs[name][day]
    if not wanted or days_worked[name] >= MAX_DAYS or is_working(name, day):
        return

    # try their ranked shifts first, then whatever is left
    order = wanted + [s for s in SHIFTS if s not in wanted]
    first = order[0]

    if len(schedule[day][first]) < PER_SHIFT:
        add(name, day, first)
        return

    # first choice is full, try the other shift that day
    for shift in order[1:]:
        if len(schedule[day][shift]) < PER_SHIFT:
            add(name, day, shift)
            notes.append("Conflict: %s wanted %s %s (full) -> moved to %s %s"
                         % (name, day, first, day, shift))
            return

    # whole day is full, try the next day
    if d < 6:
        next_day = DAYS[d + 1]
        if not is_working(name, next_day):
            for shift in order:
                if len(schedule[next_day][shift]) < PER_SHIFT:
                    add(name, next_day, shift)
                    notes.append("Conflict: %s wanted %s %s (day full) -> moved to %s %s"
                                 % (name, day, first, next_day, shift))
                    return

    notes.append("Conflict: " + name + " could not be placed on " + day)


def fill_shifts(day):
    for shift in SHIFTS:
        while len(schedule[day][shift]) < PER_SHIFT:
            available = [n for n in prefs if days_worked[n] < MAX_DAYS and not is_working(n, day)]
            if not available:
                notes.append("WARNING: nobody left to cover " + day + " " + shift)
                break
            name = random.choice(available)
            add(name, day, shift)
            notes.append("Fill: " + name + " randomly assigned to " + day + " " + shift)


def make_schedule():
    for day in DAYS:
        schedule[day] = {"morning": [], "evening": []}
    for name in prefs:
        days_worked[name] = 0

    for d in range(7):
        names = list(prefs)
        random.shuffle(names)
        for name in names:
            place(name, d)

    # go back and fill any shift with less than 2 people
    for day in DAYS:
        fill_shifts(day)


def print_schedule():
    print("\nSCHEDULING NOTES (Python)")
    if not notes:
        print("  No conflicts or fills needed")
    for note in notes:
        print("  " + note)

    print("\nFINAL WEEKLY SCHEDULE (Python)")
    print("-" * 50)
    print("%-10s| %-18s| %-18s" % ("Day", "Morning", "Evening"))
    print("-" * 50)
    for day in DAYS:
        print("%-10s| %-18s| %-18s" % (day, ", ".join(schedule[day]["morning"]),
                                       ", ".join(schedule[day]["evening"])))
    print("-" * 50)

    print("\nDAYS WORKED (Python)")
    for name in prefs:
        print("  %-10s%d" % (name, days_worked[name]))


print("=== Employee Shift Scheduler (Python) ===")
get_input()
make_schedule()
print_schedule()
