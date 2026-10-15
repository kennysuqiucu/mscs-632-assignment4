# Employee Shift Scheduler

The same employee scheduling program written in two languages, Python and Java, to compare how each one handles conditionals, loops, and branching.

## Files

| File | Purpose |
|------|---------|
| `scheduler.py` | Python 3 version |
| `ShiftScheduler.java` | Java 11 or newer version |
| `sample_data.csv` | Sample employees and shift preferences, read by both programs |

## How to run

Run both programs from this folder so they can find `sample_data.csv`.

Python:

```
python scheduler.py
```

Java:

```
javac ShiftScheduler.java
java ShiftScheduler
```

Each program asks `Load employees from sample_data.csv? (y/n)`.

- `y` loads the employees from `sample_data.csv` and prints the schedule.
- `n` lets you type the employees yourself. For each day, type the shifts in order of preference (for example `evening morning`), or `off` for a day off.

## Sample data format

The first line is a header. Each line after it is one employee: the name, then one entry per day from Monday to Sunday.

```
name,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday
Alice,morning,off,morning,off,morning evening,off,morning
```

`morning evening` means morning is the first choice and evening is the second. `off` means no shift wanted that day.

## Scheduling rules

- There are two shifts per day: morning and evening.
- An employee works at most one shift per day.
- An employee works at most 5 days per week.
- Every shift needs exactly 2 employees. If a shift is short, employees who have worked fewer than 5 days are picked at random to fill it.
- If an employee's preferred shift is already full, the program tries the other shift on the same day, and then the next day.

## How it works

1. **Input and storage** - names and ranked shift preferences are stored in a dictionary (Python) or a map of lists (Java).
2. **First pass** - for each day, employees are placed in their preferred shift. A full shift is a conflict, and it is resolved on the same day or the next day.
3. **Second pass** - any shift with fewer than 2 employees is filled at random.
4. **Output** - scheduling notes (conflicts and random fills), the weekly schedule as a table, and the number of days each employee works.

The order of employees is shuffled each day and the fills are random, so the schedule changes from run to run.

If there are not enough employees to cover every shift, the program prints a warning for each shift it could not fill.
