Task 1 — Review and Takeaways

Dining Philosophers with Semaphores

1\. Core Concepts

Dining Philosophers Model



Each philosopher (P) is represented by a thread.



Number of threads = number of philosophers.



Philosophers sit in a circle.



Each philosopher has access to:



the chopstick on their left



the chopstick on their right



Chopsticks are represented as binary semaphores.



Example representation:



Semaphore\[] chopsticks = new Semaphore\[100];



for (int i = 0; i < 100; i++) {

&nbsp;   chopsticks\[i] = new Semaphore(1);

}



Deadlock Risk



Chopsticks are binary semaphores, which can lead to deadlock if:



Every philosopher picks up their left chopstick



No philosopher can obtain the right chopstick



Result:



All threads wait indefinitely.



Shared Variables



static variables are shared among all thread instances.



Shared variables must be protected using a semaphore (mutex).



Examples:



Count of meals eaten



Number of philosophers finished



Shared counters accessed by multiple threads



If multiple threads access a variable, it must be protected.



2\. Barrier Requirement



A barrier is required at the beginning and end:



All philosophers must enter the room together.



No philosopher sits down until all philosophers arrive.



No philosopher leaves until all are ready.



Barrier behavior:



First and last synchronization point.



Barrier idea:



Threads increment a shared counter.



The last arriving thread releases permits.



All waiting threads proceed together.



3\. Synchronization Rules

Allowed



Semaphores



Thread methods



yield() to simulate CPU switching



Disallowed



The following synchronization mechanisms are NOT allowed:



synchronized



ReentrantLock



ReadWriteLock



CountDownLatch



CyclicBarrier



volatile



AtomicInteger



AtomicLong



Any monitor-based synchronization



All synchronization must be done using semaphores.



Busy Waiting Rules



Busy waiting loops are NOT allowed except for:



Simulating philosopher eating time



Simulating philosopher thinking time



These times should be random.



Using busy waiting for synchronization will result in penalties.



4\. Philosopher Behavior Requirements



Philosophers sleep when chopsticks are unavailable.



Threads must not actively spin while waiting.



Output must be produced at specific algorithm steps:



Steps 1, 2, 3, 4, 6, 7, 8, and 11.



Output requirements:



Must specify which philosopher produced the output.



Include relevant identifiers:



philosopher ID



chopsticks involved



number of meals eaten so far.



5\. Yield Usage



yield() simulates that a thread can be switched out of the CPU at any time.



It is unrelated to thinking or eating delays.



Used to test thread safety.



Can represent eating, thinking, reading, or writing.



Yield between chopstick attempts (Task 2 requirement) is used to compare behavior with Task 1.



6\. Meal Definition



A philosopher completes one meal after:



3–6 cycles of eating.



7\. Final Output Requirement



At the end of execution:



Report how many meals each philosopher ate.



Report total meals eaten.



8\. Edge Cases

Case: P = 1, M = 1



Impossible (requires two chopsticks).



Solution: terminate program with proper error message.



Case: P = 2, M = 1



Possible but risk of deadlock.



Possible solution:



Wait some time and force one philosopher to release a chopstick.



Program must handle edge cases appropriately.



9\. Instructor Clarifications (Q\&A)

Q3 — Must meals ≥ philosophers?



No. Program should handle all valid inputs and edge cases.



Q4 — Can busy waiting be used?



No, except for eating/thinking simulation.



Q5 — Should global counters be protected?



Yes. Any shared variable accessed by multiple threads must be protected by a semaphore.



Q6 — What is an eaten meal?



A completed meal equals 3–6 eating cycles.



Q7 — Yield comparison between Task 1 and Task 2



Use the same parameters as Task 1 and compare output differences when yield is added.



Key Takeaways



Semaphores are the only synchronization mechanism allowed.



All shared variables must be protected.



Barriers ensure coordinated start and finish.



Avoid deadlock caused by chopstick acquisition order.



Yield is used to test thread safety, not timing.

