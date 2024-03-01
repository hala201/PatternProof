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
- Control Flow: One significant piece of feedback we have received is that this idea might do all the heavy lifting on class declarations rather than a static analysis of the program execution which  might disregard the control flow requirement for this project. A modification to our idea would be to suggest a design pattern given how a program is currently implemented. For example, a program might be hard coding a *double dispatch* feature, when it could use the visitor pattern instead. 

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