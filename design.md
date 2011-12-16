# Core Design

Monotony has at its foundation the following ideas:

* It is useful to model time as a sequence of discrete instances.
* Modeling time to a millisecond resolution allows a useful set of functionality.
* Time is conceived of by humans as being divided into a number of cycles of varying sizes which have containment relationships.
* Humans reason about time in terms of periods of time. Some of these periods of time are fixed periods relative to some larger period, e.g. January is the first month of a year.
* There are interesting ways to describe a pattern of times by decomposing and indexing into these periods, e.g. "At 6:30pm on the 3rd Wednesday of every month"
* Most reasoning about time is contextual to both location and what the reasoner defines to be "now".

## Brute Force Generation and Decomposition

monotony.core features functions for generating successive sequences of periods of time, and for decomposing a period of time into a sequence of higher resolution periods of time. It can, for example, turn a month into a sequence of days in that month, or into weeks of that month. These functions generate a great deal of data and are not very fast. It is possible to compose these operations to create a mapping function which operates against some specificly sized input period, and then apply this mapping function to an infinite sequence of periods of the specified size. This is the approach used in the README example. It is wasteful, but correct for many cases.

## Interval based generation

Another form of generating successive periods of a uniform size in time is to calculate an offset between the start of one period and the start of the period which follows it. As time is cyclic, with a sufficiently large collection of offsets one can generate an infinite series of periods by applying these offsets. Being able to compute this series of offsets given some specification provides a high performance means of doing the same thing that monotony.core does.

This requires the creation of a temporal inference engine, this work is being done in monotony.logic.

## Interval based reasoning

It is useful to express and reason about times in relation to other times. There is work in enumerating the relations between different periods of time (e.g. http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.87.4643), and how this can be used to create logical reasoning about times and events relative to some given initial conditions.
