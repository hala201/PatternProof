- [Final Documentation](#final-documentation)
- [Check-in 1: Initial Ideas and Proposals](#check-in-1)
- [Check-in 2: Ideas Refinement](#check-in-2)
- [Planned Features / Tasks](#planned-features--tasks)
- [Check-in 3: User Study and Mock up](#check-in-3)
- [Check-in 4: Implementation Status](#check-in-4)
- [Check-in 5: Final Progress Status](#check-in-5)

# Final Documentation

## Motivation

Our program is a verification tool that aims to verify the usage of three specific design patterns in order to propose refactoring of unhandled cases (that could be bugs or lead to bugs) and propose suggestions on how to achieve a higher quality design patterns (e.g., better adherence to the design pattern, using Java's polymorphism, etc.). The three design patterns we are focusing on are Visitor, Chain of Responsibility, and Observer. Our target users are developers who are at least somewhat familiar with design patterns (either read about it, used it, or learnt about it).

### Some Specific Use Cases

1. Help developers to write more maintainable and readable code.
2. Help developers to find potential subtle bugs.
3. Help developers to understand more about the design patterns.

## Design

Our program is written in Java for backend and JavaScript with React for frontend. It is a static program analysis tool that is value-Agnostic. Since our goal is to understand the structure and the control flow of the code that has design patterns, we do not need to execute the code and we do not need to know the value of any data/variables. We use JavaParser to parse the Java code and collect [relevant states that are used later in analysis stage](#features). We use a fixpoint analysis that makes use of the collected states. Then, the analyzer will make decisions and provide suggestions to the user.

### Features

- _Visitor_: We statically examine the Java code to identify and analyze the use of the Visitor design pattern, focusing on class relationships, method signatures, and interactions that embody the pattern's structure and behavior. It accounts for control flow by analyzing method interactions—particularly the double dispatch mechanism intrinsic to the Visitor pattern—through mappings of candidate classes (class that dispatch/call each other), visitor-element associations, and inheritance hierarchies. By collecting detailed method information and tracking subclass-to-superclass relationships, the analyzer discerns how visitors (very likely) determine which _visit_ method to invoke on an element. The analyzer examines how methods are called and how different classes interact within the pattern. This approach allows us to suggest improvements and identify potential bugs without needing to execute the code.
  - This specific feature fits the use cases such as by finding unhandled Elements that are not visited by any Visitor. It also suggests refactoring to use Java's language features such as polymorphism to handle the Element instead of using repetitive codes.


- _Chain Of Responsibility_: We statically examine the java code to identify and analyze the use of the Chain of Responsibility pattern, focusing on finding overall class hierchy needed for the chain of responsibility, the client, base handlers, concrete handlers, a chain and requests. It accounts for control flow by analyzing variable and chain object creation taking into consideration the order of these calls, along with identifying requests that propagation through a created chain. With the collection of handler responsibility and tracking base and concrete handler relationships, the analyzer determines redundancy in responsibilities, extracts a created chain which is used to follow propagation starting from the head of the chain.
### Trade-offs

- _Visitor_: The Visitor Pattern Analyzer, through its static analysis approach, tends towards _over-approximation_ in evaluating the implementation of the Visitor design pattern within Java codebases. By broadly identifying candidates for pattern components, analyzing method interactions, and considering inheritance relationships without executing the code, the analyzer aims to ensure comprehensive coverage of potential pattern instances. However, this method inherently _risks including false positives_ by identifying non-relevant cases or suggesting refinements where none are needed. The broad capture of candidate classes and assumption-based analysis of interactions and method overloads can lead to the inclusion of more behaviors or paths than are actually present at runtime, necessitating a careful review of its findings against the specific execution context and design intentions. For instance, reflection or dynamically generated code can obscure the actual interactions between Visitors and Elements, leading to cases _where it identifies an Element as a Visitor_.


- _Chain of Responsibility_: The Chain of Responsibility Analyzer, through its static analysis approach, tends towards optimistic detection of propagation validity while evaluating implementations of the Chain of responsibility pattern. The analyzer aims to provide a basic set refactoring suggestions for structure and propagation. However, it heavily requires multiple assumptions about the given code while analyzing structure for redundancy and request calls for propagation without needing to execute the code. 
  - Assumptions:
    - There is a singular chain of responsibility structure
    - There is a singular client which uses the structure
    - The client creates a singular chain

## How-to(s) & Limitations

We are using Spring Boot, Gradle, and React. We have included instructions on how to install, build, and run the application in the README file.

### Limitations

- We rely on the source code to be written in Java, because we rely on Java Parser to generate the AST.
- We rely on the source code to not heavily use external libraries as we cannot generate AST from those nor do we know how those libraries work under the hood. (In this case, the analyzer will only analyze the available codes and ignore external libraries).
- We rely on the source code to have at least the structure of the design patterns to be there.
- We rely on what was taught to us as the "good" way to implement the design patterns.

## User Studies

We conducted user studiea to gather feedback on our design and implementation, specifically focusing on a tool designed to identify and suggest refinements for code implementing the Visitor and Chain of Responsibility design patterns. Our approach included mock-ups and scenarios used in the initial user study, followed by iterative refinements based on the feedback received, and culminating in a final user study with an operational version of our analyzer.

**Initial User Study**: Conducted on March 13th, this study involved two participants familiar with the design patterns in question. They were provided with source code examples containing potential improvements or bugs and mock text-based suggestions similar to what our tool aimed to produce. Feedback highlighted the need for more detailed and clearer suggestions, prompting changes to our design to include additional details about relevant classes and the nature of suggested improvements.

**Feedback and Iterative Refinements**:

- Participants desired hints to foster learning rather than direct solutions.
- Preferences emerged for code suggestions for handlers and descriptive text for visitors, including more context on data and classes involved.
- The necessity for clarity in suggestions was underscored, particularly regarding the location for proposed refactors and the sequence of actions.
- Users faced challenges with specialized terminology and ambiguous suggestions that did not clearly differentiate between identifying issues and offering solutions.
- We updated what we output by returning more details such as information about relevant classes and potential source of bug or unhandled cases. We do this by collecting more states/informations about the relevant classes and methods to provide more detailed suggestions.

**Final User Study**: Conducted on April 2nd, we conducted a study with the same target demographic, utilizing the refined output from our analyzer. This time, users interacted with the actual suggestions generated by our tool. The feedback was positive, indicating that our analyzer successfully assisted users in identifying and fixing code issues. Requests were made for more formatted suggestions. Despite some users not being familiar with certain patterns, the tool was generally found helpful, especially when suggestions were highlighted more clearly.

**Feedback and Iterative Refinements**:

- The UI was found to be simple to understand and use.
- Even when not familiar with the design pattern, the analyzer's output was helpful enough to provide guidance for users to incorporate the refactor suggestions.
- User wanted the suggestion for chain responsibility to be easier to read (it was "hard to tell what’s wrong").
- We refine the output a bit more, making it easier to read. We achieved this simply through adding line breaks, enough spaces, and more importantly using combinations of bullet points and natural languages (instead of only bullet points of fragment sentences).

The balance between providing enough detail for informed action and maintaining simplicity for user comprehension was a challenge. We have based our suggestions on what was taught in CPSC courses which doesn't align with every developer's usage of the same design pattern. Still, we try our best to reccommend what we think are maintainable and readable codes as well as pointed out sources of potential bugs.

<sup><sub><sub>_END; Check-In(s) Below_</sub></sub></sup>

---

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

# Check-in 3:

## Mock Up:

**Original**: This program analysis provides suggestions of how code that implements a design pattern should be refactored.

**Mock**: The mock-up is a text editor that accepts code written in design patterns in Java and we added text-based suggestions to refactor the code.

### One Example Input:

The below code is written in Java using the Chain of Responsibility design pattern.

```java
import java.util.logging.Level;
abstract class Logger {
    private Logger nextLogger;

    public void setNextLogger(Logger nextLogger) {
        this.nextLogger = nextLogger;
    }

    public void logMessage(Level level, String message) {
        if (this.getLevel().intValue() <= level.intValue()) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }

    protected abstract Level getLevel();

    protected abstract void write(String message);
}

class ConsoleLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.INFO;
    }

    @Override
    protected void write(String message) {
        System.out.println("Console Logger: " + message);
    }
}

class FileLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.WARNING;
    }

    @Override
    protected void write(String message) {
        System.out.println("File Logger: " + message);
    }
}


class ErrorLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.WARNING;
    }

    @Override
    protected void write(String message) {
        System.out.println("Error Logger: " + message);
    }
}

public class ChainOfResponsibilityExample {
    private static Logger getLoggerChain() {
        Logger errorLogger = new ErrorLogger();
        Logger fileLogger = new FileLogger();
        Logger consoleLogger = new ConsoleLogger();

        errorLogger.setNextLogger(fileLogger);
        fileLogger.setNextLogger(consoleLogger);

        return errorLogger;
    }

    public static void main(String[] args) {
        Logger loggerChain = getLoggerChain();

        loggerChain.logMessage(Level.INFO, "This is an INFO message.");
        loggerChain.logMessage(Level.WARNING, "This is a DEBUG message.");
        loggerChain.logMessage(Level.ALL, "This is an ERROR message.");
    }
}
```

### One Example Output:

There were 2 handlers FileLogger and ErrorLogger, handling the same case, 'WARNING'. There was one case LEVEL.ALL that was not handled by any handler.

## User Study 1:

### User study idea:

User was given simple source code that implements a design pattern. (Chain of Responsibility & Visitor patterns).
Then, user was given the text suggestions (that our program analysis might give), and was asked to perform refactors based on the suggestions.
The quality of the code that the user produces determines how helpful the suggestions were.

**User 1 (current 310 student):**

- Want an option to give hints before straight up answers/suggestions for users to learn
- Prefer code suggestion for handlers and text suggestion for visitor
- Would help if the text suggestion also includes some more details/logs such as info on the data/classes

**User 2 (completed 210):**

Task1:

- The user was able to refactor task1 successfully using the text suggestions.
- The suggestion saved a lot of time of reading the code and trying to understand what it does. It would be even more straightforward if the suggestions can point out the line where refactors can be done.

Task2:

- If suggestions include specialized software dev terminology (overloading), the user might not know what that means and need to look it up.
- The order of refactors can be confusing since the suggestion included two things that needed refactoring, but it didn’t clarify whether the refactor should be performed in a specific order.
- The wording wasn’t clear whether the suggestion is pointing out the bug or explaining the solution.
- The refactor was not correct on the first try.

## Updated Design post User Study:

- First: detect bug / dead code of handler.
- Second: point out where the bug is, and more precise logging of information about the code.
- Third: give possible suggestions of lines of code without refactoring the code.
  - User study could help answer whether our refactor suggestions will be code modifications or just text-based suggestions that lead the user to the right answer.
  - Maybe the tool can add a quick explanation of the design pattern used.
  - Clearly outline the steps and their order to refactor the code.

# Check-in 4:

## Implementation Status

### Chain of Responsibility

- **Task 1**: Wrote unit tests, no tests are passing yet. Implemented dead handler analysis, needs more work still.
- **Task 2**: No progress so far.

### Visitor Pattern

- **Task 1**: Completed a working version of the double dispatch analysis. The plan is to refine this further and possibly add tests.
- **Task 2**: Integrated with double dispatch analysis and implemented naive control flow analyzer to find all used visitor types. Needs more testcases and possibly apply for more general cases

### Observer Pattern

- **Task 1**: No progress so far.

## Plans for Final User Study

- The approach for the final user study is still under consideration, but it will probably be something similar to the first user study (the output is unsure yet). Depending on how we progress in term of implementation, we may do it the end of check-in 5 or just before the final video.

## Planned Timeline for Remaining Days

- There's an adjustment in the project timeline to roughly complete the implementation by April 1st. Currently, we are a bit behind the goals outlined in Check-in 2.

# Check-in 5:

## Progress Status

- Since our implementataion status was a bit behind early in the week, we decided to push both the final user study and the final video to next week.
- We plan on finishing enough of the implementation to have a working version for the final user study.
- Timeline: we finish implementing by around March 31 - April 2, we will work on the final user study around April 2 - April 3, then we will finish the video by the end of that week and do any final touches.

## Final User Study Plan:

- Similar to our first user study, we will give user a simple source code (from `design_pattern_verifier/src/main/resources/static`) that implements a design pattern. (Chain of Responsibility & Visitor patterns). Then, our program analysis will give text suggestions, and the user will be asked to perform refactors based on the suggestions. The quality of the code that the user produces will determine how helpful the suggestions were.

## Final Video Plan:

- Zoom meeting where we present and share screen. Ideally, we each do a part of the presentation.
- We will try to create a story/presentation that shows:
  - what our analyzer can do and how it helps.
  - target users
