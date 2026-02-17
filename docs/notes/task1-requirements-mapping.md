# Task 1 — Requirements to Implementation Mapping
Dining Philosophers using Semaphores

## Purpose
This document maps each requirement from Task 1 to the design and algorithmic decisions used in the implementation. The goal is to ensure that every part of the implementation directly satisfies a requirement from the project specification.

---

## Requirement: Use only Semaphores for synchronization

### Description
All synchronization must be implemented using semaphores. Other mechanisms such as synchronized blocks, locks, atomics, or built-in barrier constructs are not allowed.

### Implementation Mapping
- Chopsticks are represented as an array of binary semaphores.
- A semaphore mutex protects shared variables such as meal counters.
- Barrier synchronization is implemented using semaphores only.

---

## Requirement: One thread per philosopher

### Description
The program must fork one thread for each philosopher.

### Implementation Mapping
- Each philosopher is represented as a thread instance.
- The number of threads created equals the number of philosophers (P).

---

## Requirement: Chopsticks as shared resources

### Description
Chopsticks must be shared resources that philosophers acquire before eating.

### Implementation Mapping
- Chopsticks are implemented as an array of semaphores.
- Each semaphore is initialized to 1 (binary semaphore).
- Philosopher N accesses:
  - left chopstick = N
  - right chopstick = (N + 1) % P

---

## Requirement: Prevent deadlock

### Description
The system must avoid deadlock situations where philosophers hold one chopstick and wait indefinitely for another.

### Implementation Mapping
- Chopstick acquisition order is modified for at least one philosopher to break circular wait conditions.
- This ensures at least one philosopher can proceed.

---

## Requirement: Barrier at beginning and end

### Description
All philosophers must enter together and leave together.

### Implementation Mapping
- A semaphore-based barrier is used.
- A shared counter tracks arriving threads.
- The last arriving thread releases permits allowing all threads to continue.

---

## Requirement: Protect shared variables

### Description
Any shared variable accessed by multiple threads must be protected.

### Implementation Mapping
- Meal counters and shared state variables are protected using a mutex semaphore to ensure atomic updates.

---

## Requirement: No busy waiting

### Description
Busy waiting loops are not allowed except for simulating eating or thinking for random durations.

### Implementation Mapping
- Philosophers block on semaphores when resources are unavailable.
- Random delays are used only for eating and thinking cycles.

---

## Requirement: Output at specific algorithm steps

### Description
Output must be produced at steps 1, 2, 3, 4, 6, 7, 8, and 11.

### Implementation Mapping
- Output statements are placed directly at the corresponding algorithm steps within the philosopher thread execution.

---

## Requirement: Stop eating after total meals completed

### Description
No additional meals may be eaten once the total number of meals has been reached.

### Implementation Mapping
- A shared meal counter is checked and updated inside a semaphore-protected critical section before eating begins.

---

## Summary
The implementation follows the Dining Philosophers algorithm while ensuring correct synchronization using semaphores only. Each design decision directly corresponds to a requirement in Task 1.
