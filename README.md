# Collaborative Plain Text Editor (APT Project)

## Overview

This project is part of the Advanced Programming Techniques course.
We are building a simplified real-time collaborative plain text editor using Java and CRDT (Conflict-free Replicated Data Types).

The system allows multiple users to edit the same document simultaneously while ensuring consistency across all users.

---

# Phase 1 (CRDT Core)

## Objective

Implement the core CRDT engine that guarantees:

* Consistency across users
* Conflict resolution
* Deterministic behavior under concurrent edits

---

## Phase 1 Requirements

* Character-Level CRDT (tree-based)
* Block-Level CRDT
* Insert/Delete operations
* Tombstones (soft deletion)
* Deterministic ordering
* Basic local testing

---

# CRDT Logic (Based on Project Slides)

### Character CRDT

* Each character is a node in a tree
* Each node contains:

    * ID = (UserID + Clock)
    * Value (character)
    * Parent reference
    * Deleted flag (tombstone)

---

### Ordering Rules

If multiple characters share the same parent:

1. Order by timestamp (clock)
2. If equal, order by UserID

---

### Traversal

* Use DFS traversal
* Skip deleted nodes
* Generate final document text

---

### Tombstones

* Characters are not removed
* Instead:

```plaintext
deleted = true
```

---

### Block CRDT

* Document is composed of Blocks
* Each block:

    * Has its own ID
    * Contains a Character CRDT
    * Has deletion flag

---

# Team Workload Division (4 Members)

## Member 1 — Character CRDT

* Implement `CRDTNode`
* Implement `CharacterCRDT`
* Insert character logic
* Delete (tombstones)
* DFS traversal

---

## Member 2 — Block CRDT + Document

* Implement `Block`
* Implement `BlockCRDT`
* Implement `Document`
* Block insert/delete
* Document text reconstruction

---

## Member 3 — Operations + Ordering

* Implement operation classes:

    * InsertCharacterOperation
    * DeleteCharacterOperation
    * InsertBlockOperation
    * DeleteBlockOperation
* Implement ID generation
* Implement ordering rules
* Sorting logic for children

---

## Member 4 — Testing + Integration

* Create test scenarios
* Simulate concurrent operations
* Validate deterministic results
* Handle edge cases
* Integrate all components

---

# Project Structure

```
src/
 ├── crdt/
 │    ├── character/
 │    │     ├── CRDTNode.java
 │    │     ├── CharacterCRDT.java
 │    │
 │    ├── block/
 │          ├── Block.java
 │          ├── BlockCRDT.java
 │
 ├── model/
 │     ├── Document.java
 │     ├── Main.java
 │
 ├── operations/
 │     ├── InsertCharacterOperation.java
 │     ├── DeleteCharacterOperation.java
 │     ├── InsertBlockOperation.java
 │     ├── DeleteBlockOperation.java
 │
 ├── util/
 │     ├── IdGenerator.java
 │     ├── OrderingUtil.java
 │
 ├── test/
```

---

# Workflow (How the System Works)

### Flow:

```
User Action → Operation → CRDT → Tree Update → Traversal → Output Text
```

---

### Example

1. User inserts character 'A'
2. Operation is created
3. CharacterCRDT creates a node
4. Node is attached to parent
5. Tree is updated
6. DFS traversal generates text

---

### Concurrent Example

Two users insert at same position:

```
      A
     / \
    B   C
```

Ordering rule decides final output:

```
ABC  or  ACB
```

The result is the same for all users, ensuring consistency.

---

# Testing Strategy

## Basic Tests

* Insert characters: A → B → C
* Delete character using tombstone
* Verify output

---

## Concurrent Tests

* Insert two characters at same parent
* Apply operations in different orders
* Ensure same final result

---

## Block Tests

* Insert multiple blocks
* Delete block
* Verify document structure

---

## Edge Cases

* Delete non-existing node
* Double deletion
* Insert under deleted parent

---

# How to Run

1. Open the project in IntelliJ
2. Run `Main.java`
3. Check console output
