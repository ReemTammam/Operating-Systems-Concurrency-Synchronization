Concurrency Problems in Java: Readers-Writers Agents & Dining Philosophers
📌 Overview

This project implements classic synchronization problems from operating systems using Java multithreading, including the Readers-Writers Agents problem and the Dining Philosophers problem. The focus is on designing safe and efficient concurrent systems while preventing race conditions, deadlocks, and starvation.

Developed as part of CMPS 455 (Operating Systems / Concurrent Systems).

🎯 Objectives

Implement synchronization in multi-threaded systems

Ensure safe access to shared resources

Prevent race conditions, deadlock, and starvation

Apply theoretical OS concepts in practical Java implementations

⚙️ Implemented Problems
🧠 Readers-Writers Agents Problem

Models a shared resource accessed by multiple reader and writer agents (threads).

Key Features:

Multiple readers can access simultaneously

Writers require exclusive access

Ensures:

Data consistency

No race conditions

Fair scheduling

Concepts Used:

Java Threads

Semaphores / synchronization

Critical sections

Mutual exclusion

🍽️ Dining Philosophers Problem

Simulates philosophers competing for shared resources (forks).

Key Features:

Prevents deadlock

Avoids starvation

Ensures fair resource allocation

Concepts Used:

Thread synchronization

Resource allocation

Deadlock prevention

🛠️ Technologies

Language: Java

Concepts: Multithreading, Synchronization, Deadlock Prevention, Mutual Exclusion

📂 Project Structure
.
├── src/
├── readers_writers/
├── philosophers/
└── README.md
