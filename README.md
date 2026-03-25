# Collaborative Plain Text Editor (APT Project)

## Overview

This project is part of the Advanced Programming Techniques course.
We are building a simplified real-time collaborative plain text editor using Java and CRDT (Conflict-free Replicated Data Types).

The system allows multiple users to edit the same document simultaneously while ensuring consistency.

---

## Phase 1 (Current Progress)

We are currently implementing the core CRDT logic.

### Completed:

* Project structure setup
* Character-level CRDT class (`CRDTCharacter`)
* Basic testing using a Main class

---

## Key Concepts

* CRDT (Conflict-Free Replicated Data Type)
* Character-level operations
* Unique ID per character
* Deterministic ordering (to be implemented)
* Tombstones for deletion

---

## Project Structure

```
src/
 ├── crdt/
 │    ├── character/
 │    ├── block/
 │
 ├── operations/
 ├── model/
```

---


## How to Run

1. Open the project in IntelliJ
2. Run `Main.java`
3. Check console output

---


