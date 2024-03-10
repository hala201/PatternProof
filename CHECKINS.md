- [Check-in 1: Initial Ideas and Proposals](#check-in-1)
- [Check-in 2: Ideas Refinement](#check-in-2)
- [Planned Features / Tasks](#planned-features--tasks)

# Check-in 1:

## 1. First Idea: Memory visualization

Memory visualization for C program (valgrind visualized, mainly dynamic check)

- Saving a visualization for whenever sth is allocated (keeping history)
- Saving more than what is obvious (NOT only saving that memory is allocated)
- Analysis on desginated chunk of codes to visualize the memory (control flow)
- Support for multiple views including chronological, hierarchical, and statistical representations of memory usage.

## 2. Second Idea: Design Pattern Miner

### Use cases:

- Facilitate the reading and understanding of large source codes.
- Help developers to reverse engineer and collaborate on open source projects.
- Verify if the implementation matches the design intentions which can be helpful for code owners to debug and refine.

### Static Analysis:

- Java: Inspired by a [design pattern recognition algorithm](https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=4015512&tag=1) published in IEEE 2006, we can use similarity scoring to mine and detect design patterns. The approach exploits the fact that patterns reside in one or more Java inheritance hierarchies, reducing the classes on which the analysis needs to be conducted.
- Static: Another [paper uses XMI standard](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=4148953) (a metadata for object management to construct UMLs) to analyse class hierarchies and relations. We can limit our analysis to one of these approaches which seem most feasible within our shared expertise and go from there. The current plan is to choose design patterns that can be detected statically from class hierarchies.
- Control Flow: One significant piece of feedback we have received is that this idea might do all the heavy lifting on class declarations rather than a static analysis of the program execution which might disregard the control flow requirement for this project. A modification to our idea would be to suggest a design pattern given how a program is currently implemented. For example, a program might be hard coding a _double dispatch_ feature, when it could use the visitor pattern instead.

### Possible Components:

We need to make a distinction of the design patterns that influence the control flow of a program versus those that only depend on class hierarchies. Below is our speculation:

#### Structural Design Patterns:

**1. Singleton Pattern**: Look for classes with private constructors that provide a static method to get the instance.

**2. Factory Pattern**: Identify classes that have methods returning instances of other classes, often used in interfaces.

**3. Builder Pattern**: Classes with a fluent interface for constructing complex objects are likely using the builder pattern.

**4. Adapter Pattern**: Classes that convert the interface of one class into another interface that clients expect.

#### Control Flow Design Patterns:

**1. Chain of Responsibility**: This pattern allows a request to be passed through a chain of handlers. Each handler decides either to process the request or to pass it along the chain.

**2. Command Pattern**: This pattern encapsulates a request as an object, thereby parameterizing clients with queues, requests, and operations. It allows for the support of undoable operations, for example.

**3. Interpreter Pattern**: This pattern defines a grammar for interpreting a language and provides a way to evaluate sentences in that language.

**4. Iterator Pattern**: This pattern provides a way to access the elements of an aggregate object sequentially without exposing its underlying representation.

**5. Observer Pattern**: Look for classes that maintain a list of dependents and notify them of state changes.

**6. Visitor Pattern**: Allows you to define a new operation without changing the classes of the elements on which it operates.

# Check-in 2:

## Brief Description

### Objective / Changes

The focus of our project has shifted towards the development of a verification tool that primarily employs static analysis to detect specific design patterns within a codebase. This tool aims to identify the use of certain design patterns accurately, propose potential refactoring opportunities, and enhance code readability and maintainability.

- The tool will perform **static analysis**, eliminating the need for dynamic analysis. This involves parsing the source code to identify structural patterns without executing the program.
- An **over-approximation** approach will be used to analyze the code, identifying not only direct matches but also potential instances of the design patterns that may require further inspection or refactoring.
- **Refactoring suggestions** may be provided as part of the analysis output, offering insights into how the code could be improved to better adhere to the identified design patterns or enhance overall code quality.
- We may or may not support a web UI, depending on the time constraints.

### Scope

The analysis will concentrate on three primary behavioral design patterns:

- **Chain of Responsibility**: To identify sequences where a request passes through a chain of handlers, examining the structuring of handler objects.
- **Visitor**: To detect instances where a new operation is added to a class without changing the class, by identifying elements that accept a visitor.
- **Observer (possible)**: Depending on scope feasibility, to recognize scenarios where objects maintain a list of dependents and notify them of state changes.

These patterns were selected for their relevance in structuring complex interactions within software, facilitating the extension and maintenance of systems.

## Planned Features / Tasks

This list is likely to expand as we progress and finalize the program design.

## Chain of Responsibility

### Task 1: Analysis of Handler Structure (Hala)
#### Goal:
To analyze the structure of handlers within the Chain of Responsibility pattern, identifying the sequence of handlers and their interactions in processing requests.

#### Abstract States σ:
A mapping from each request type to the sequence of handlers responsible for processing the request, including any branching or termination conditions within the chain.

#### Error/Output Information E:
A summary of the chain of responsibility for each request type, highlighting any gaps in handler coverage, redundant handlers, or potential bottlenecks in request processing.

#### Analysis Function:
analyse(σ, s) takes the current abstract state σ and a program statement s (including handler registrations, request processing logic, etc.). It updates σ based on the control flow implications of s for request handling, including the addition or removal of handlers, changes in the processing sequence, and any conditional branching based on request types.

#### Concretisation Function:
Maps the abstract states to concrete sequences of handlers within the program's request processing flow. This includes detailing which handlers are responsible for processing which types of requests, in what order, and under what conditions (e.g., based on request properties or the presence of other handlers in the chain).

#### Termination Strategy:
Implement a fixpoint analysis where the analysis iterates until no new information is discovered in σ. This involves identifying when the addition of new handlers or changes in the processing logic do not change the overall understanding of the handler chain for each request type, indicating a stable state.

### Task 2: Identification of Request Propagation (Dylan)
#### Goal:
To identify instances where a request propagates through the chain of handlers, ensuring that each handler correctly forwards the request to the next handler in the sequence.

#### Abstract States σ:
A mapping from each request type to the sequence of handlers responsible for processing the request, including information about how the request is propagated (e.g., by invoking a method on the next handler in the chain).

#### Error/Output Information E:
Identification of instances where requests are not correctly propagated through the chain, such as handlers failing to pass the request to the next handler, or handlers incorrectly processing requests intended for other handlers.

#### Analysis Function:
analyse(σ, s) examines interactions between handlers and requests within the code. When a handler processes a request, the function checks whether the request is correctly forwarded to the next handler in the sequence. The state σ is updated to reflect these interactions, marking instances where request propagation is incorrect or missing.

#### Concretisation Function:
Maps abstract states σ to concrete instances of request propagation within the handler chain, showing how different types of requests are processed and forwarded through the chain of handlers.

#### Termination Strategy:
Similar to Task 1, a fixpoint analysis is used to identify when the request propagation logic has stabilized, indicating that all requests are correctly processed and forwarded through the chain.

## Visitor

### Task 1: Analysis of Control Flow (Ricky)

#### Goal:

To analyze interaction between visitors and elements within the control flow of the program, ensuring that the Visitor Pattern is correctly and efficiently implemented across various branches of execution.

#### Abstract States σ:

A mapping from each visitor class and element class pair to a set of states representing the visitation status (e.g., "not visited", "visiting", "visited") and the paths taken in the control flow to reach that interaction.

#### Error/Output Information E:

A summary of interactions between visitors and elements, highlighting any inconsistencies or inefficiencies in the visitation process, such as elements never visited, visitors not fully utilized, or potential for optimization in the visitation sequence.

#### Analysis Function:

`analyse(σ, s)` takes the current abstract state σ and a program statement s (including conditional branches, loops, method calls, etc.). It updates σ based on the control flow implications of s for visitor-element interactions. For each visitor and element class pair, it tracks the control flow path leading to their interaction, updating the visitation status accordingly. If a new path introduces a different visitation sequence or an unvisited element is discovered, σ is updated to reflect this new information.

#### Concretisation Function:

Maps the abstract states to concrete sets of visitor-element interactions within the program's control flow. This includes detailing which elements are visited by which visitors, in what order, and under what conditions (e.g., within loops or branches of conditional statements).

#### Termination Strategy:

Implement a fixpoint analysis where the analysis iterates until no new information is discovered in σ. This involves identifying when the addition of new paths or interactions does not change the overall understanding of visitor-element interactions, indicating that a stable state has been reached.

### Task 2: Detection of Double Dispatch (Meng)

#### Goal:

To identify instances of double dispatch within the program's control flow, a key feature of the Visitor Pattern, where an element and a visitor interact in such a way that the appropriate `visit` method is called based on the type of both the visitor and the element.

#### Abstract States σ:

This involves tracking two levels of method calls: the first call from the element to the visitor (via the `accept` method) and the subsequent call back to the element (via the `visit` method). States will record the sequence of these calls, including the types of both the visitors and the elements involved.

- **σ**: A data structure mapping each element type to a visitor type, along with flags indicating whether a double dispatch interaction has occurred.

#### Error/Output Information E:

Identification of instances where double dispatch is correctly or incorrectly implemented. For correct implementations, it would detail the visitor and element types involved in double dispatch. For incorrect implementations, it would highlight missed opportunities for employing double dispatch or misuses of the pattern.

#### Analysis Function:

`analyse(σ, s)` examines interactions between elements and visitors within the code. When an `accept` method is called on an element, the function checks whether the corresponding `visit` method on the visitor correctly realizes a double dispatch by invoking a method specific to the element's type. The state σ is updated to reflect these interactions, marking them as valid double dispatch occurrences or noting potential issues.

#### Concretisation Function:

Maps abstract states σ to concrete instances of double dispatch interactions, showing how different types of elements and visitors engage in two-way method calls. This function helps visualize the dynamic polymorphism achieved through the Visitor Pattern, where the called method variant depends on both the type of the visitor and the element.

#### Termination Strategy:

Given the complexity of tracking double dispatch across potentially recursive visitor patterns or nested element structures, a termination strategy such as limiting the depth of call analysis or employing a heuristic to detect and avoid infinite recursion might be necessary.

## Observer (Kai: 1 of the 2 tasks below)

### Task 1: Analysis of Subject-Observable Relationship 
#### Goal:
To analyze the relationship between subjects and observers in the Observer Pattern, identifying how state changes in subjects are communicated to observers.

#### Abstract States σ:
A mapping from each subject to the set of observers registered with that subject, along with the state of each observer (e.g., active, inactive, notified).

#### Error/Output Information E:
A summary of the subject-observer relationships, highlighting any inconsistencies or inefficiencies in the notification process, such as observers not being notified of state changes, observers being notified multiple times unnecessarily, or potential for optimization in the notification mechanism.

#### Analysis Function:
analyse(σ, s) takes the current abstract state σ and a program statement s (including observer registrations, state change notifications, etc.). It updates σ based on the control flow implications of s for observer notification, including the addition or removal of observers, changes in the notification sequence, and any conditional logic for notifying observers.

#### Concretisation Function:
Maps the abstract states to concrete relationships between subjects and observers within the program's state management flow. This includes detailing which observers are registered with which subjects, how observers are notified of state changes, and under what conditions (e.g., based on specific events or changes in subject state).

#### Termination Strategy:
Implement a fixpoint analysis where the analysis iterates until no new information is discovered in σ. This involves identifying when the addition of new observers or changes in the notification logic do not change the overall understanding of the subject-observer relationships, indicating a stable state.

### Task 2: Detection of Notification Dependencies
#### Goal:
To identify instances where observers depend on the notification order or timing, ensuring that observers are notified in a way that respects their dependencies.

#### Abstract States σ:
A mapping from each observer to the set of observers that it depends on, along with any constraints on the order or timing of notifications.

#### Error/Output Information E:
Identification of instances where notification dependencies are not correctly handled, such as observers not being notified in the correct order, observers being notified prematurely, or observers not being notified at all due to incorrect dependencies.

#### Analysis Function:
analyse(σ, s) examines interactions between observers within the code. When an observer is notified of a state change, the function checks whether the notification respects the dependencies of other observers. The state σ is updated to reflect these interactions, marking instances where notification dependencies are violated.

#### Concretisation Function:
Maps abstract states σ to concrete instances of notification dependencies between observers, showing how different observers are notified and how their dependencies are respected.

#### Termination Strategy:
Similar to Task 1, a fixpoint analysis is used to identify when the notification dependency logic has stabilized, indicating that all observers are correctly notified and that their dependencies are respected.

## Progress Summary

So far, our project has undergone refinements. We have focused in on developing a static analysis tool focused on detecting specific design patterns—primarily the Chain of Responsibility, Visitor, and possibly Observer patterns. This decision was driven by feedback highlighting the importance of incorporating control flow into our analysis. We've initialized a repository utilizing Spring Boot, Gradle, and React, outlining some of the tasks required for our project. We are working on finalizing these tasks and identifying suitable frameworks and tools for our analysis, ensuring our tool can accurately identify design patterns and suggest potential refactorings to enhance code readability and maintainability.

## Division of Responsibilities

NOTE: responsibilities are not concrete yet and may change as we progress.

1. **Meng**: will mainly work on visitor pattern and possibly the frontend.
2. **Hala**: will mainly work on the chain responsibility pattern.
3. **Ricky**: will work with the visitor pattern and the third pattern.
4. **Dylan**: will work with the chain responsibility pattern and the third pattern.
5. **Kai**: will work with the third pattern.

## Potential Roadmap:

- Check-in 3: Design Mockup and User Study Feedback (Week 10, March 11-15)

  - Finalize tasks/features and present the project's intended functionality. (may include UI/UX designs for web components)
  - Conduct the first user study.
  - Incorporate JavaParser or similar parsing library for the analysis of source code.

- Check-in 4: Implementation Status and Final User Study Planning (Week 11, March 18-22)

  - Continue and ideally almost complete with the implementation progress of the tool.
  - Outline the plans for conducting a final user study.
  - Continue to expand supporting design pattern detection.

- Check-in 5: Final User Study Feedback and Video Presentation Planning (Week 12, March 25-29)
  - Discuss the outcomes of the final user study.
  - Finalize implementations.
  - Finalize tests (maybe).
  - Plan for the creation and submission of a final video presentation.
