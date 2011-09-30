# Monotony

Monotony is a solution to the problem of how to schedule things in a
way that humans find intuitive.

Cron strings are not very intuitive, but saying something like "The
third friday of every month at 6pm" specifies a predictable and
regular but complex pattern of times.

The core concepts of monotony are periods and cycles. A period is
a stretch of time with a start and an end. We can represent
the year of 2011 as:

[#<Date Sat Jan 01 00:00:00 EST 2011> #<Date Sat Dec 31 23:59:59 EST 2011>]

A cycle is a description of a period as an abstract concept, e.g. an hour,
a day, a week. The cycles monotony comprehends are contained in the cycles
map.

## Computing times

Monotony is based on concepts of regular generation. As an example,
let's reason about generating an infinite sequence of periods
representing the hour of 6PM to 7PM on the third Wednesday of every
month. (Coincidentally, this is when TriClojure meets at Relevance
HQ!)

I like to use cake to develop on monotony, so let's take a test drive with
cake

    cake repl
    => (in-ns 'monotony.core)

This is an event that occurs every month. Monotony can generate infinite
series of periods from some starting point in time with the periods fn.
Passed :month as a keyword, it generates month long periods, including
the period of the present month.

    => (def months (periods :month))

Let's peek at the first 12 months of this seq:

    => (take 12 months)
    ([#<Date Thu Sep 01 00:00:00 EDT 2011> #<Date Fri Sep 30 23:59:59 EDT
      #2011>] [#<Date Sat Oct 01 00:00:00 EDT 2011> #<Date Mon Oct 31
      #23:59:59 EDT 2011>] [#<Date Tue Nov 01 00:00:00 EDT 2011> #<Date
      #Wed Nov 30 23:59:59 EST 2011>] [#<Date Thu Dec 01 00:00:00 EST
      #2011> #<Date Sat Dec 31 23:59:59 EST 2011>] [#<Date Sun Jan 01
      #00:00:00 EST 2012> #<Date Tue Jan 31 23:59:59 EST 2012>] [#<Date
      #Wed Feb 01 00:00:00 EST 2012> #<Date Wed Feb 29 23:59:59 EST 2012>]
      #[#<Date Thu Mar 01 00:00:00 EST 2012> #<Date Sat Mar 31 23:59:59
      #EDT 2012>] [#<Date Sun Apr 01 00:00:00 EDT 2012> #<Date Mon Apr 30
      #23:59:59 EDT 2012>] [#<Date Tue May 01 00:00:00 EDT 2012> #<Date
      #Thu May 31 23:59:59 EDT 2012>] [#<Date Fri Jun 01 00:00:00 EDT
      #2012> #<Date Sat Jun 30 23:59:59 EDT 2012>] [#<Date Sun Jul 01
      #00:00:00 EDT 2012> #<Date Tue Jul 31 23:59:59 EDT 2012>] [#<Date
      #Wed Aug 01 00:00:00 EDT 2012> #<Date Fri Aug 31 23:59:59 EDT
      #2012>])

Now we can start slicing and dicing up these periods into
smaller cycles. We have two tools for this, cycles-in which divides
up a period by the cycle length, and bounded-cycles-in which divides
up a period so that the cuts align with the boundaries of the cycle
passed as an argument.

    => (bounded-cycles-in (first months) :day)
    ([#<Date Thu Sep 01 00:00:00 EDT 2011> #<Date Thu Sep 01 23:59:59
      #EDT 2011>] [#<Date Fri Sep 02 00:00:00 EDT 2011> #<Date Fri Sep
      #02 23:59:59 EDT 2011>] [#<Date Sat Sep 03 00:00:00 EDT 2011>
      ##<Date Sat Sep 03 23:59:59 EDT 2011>] [#<Date Sun Sep 04
      #00:00:00 EDT 2011> #<Date Sun Sep 04 23:59:59 EDT 2011>]
      #[#<Date Mon Sep 05 00:00:00 EDT 2011> #<Date Mon Sep 05
      #23:59:59 EDT 2011>] [#<Date Tue Sep 06 00:00:00 EDT 2011>
      ##<Date Tue Sep 06 23:59:59 EDT 2011>] [#<Date Wed Sep 07
      #00:00:00 EDT 2011> #<Date Wed Sep 07 23:59:59 EDT 2011>]
      #[#<Date Thu Sep 08 00:00:00 EDT 2011> #<Date Thu Sep 08
      #23:59:59 EDT 2011>] [#<Date Fri Sep 09 00:00:00 EDT 2011>
      ##<Date Fri Sep 09 23:59:59 EDT 2011>] [#<Date Sat Sep 10
      #00:00:00 EDT 2011> #<Date Sat Sep 10 23:59:59 EDT 2011>]
      #[#<Date Sun Sep 11 00:00:00 EDT 2011> #<Date Sun Sep 11
      #23:59:59 EDT 2011>] [#<Date Mon Sep 12 00:00:00 EDT 2011>
      ##<Date Mon Sep 12 23:59:59 EDT 2011>] [#<Date Tue Sep 13
      #00:00:00 EDT 2011> #<Date Tue Sep 13 23:59:59 EDT 2011>]
      #[#<Date Wed Sep 14 00:00:00 EDT 2011> #<Date Wed Sep 14
      #23:59:59 EDT 2011>] [#<Date Thu Sep 15 00:00:00 EDT 2011>
      ##<Date Thu Sep 15 23:59:59 EDT 2011>] [#<Date Fri Sep 16
      #00:00:00 EDT 2011> #<Date Fri Sep 16 23:59:59 EDT 2011>]
      #[#<Date Sat Sep 17 00:00:00 EDT 2011> #<Date Sat Sep 17
      #23:59:59 EDT 2011>] [#<Date Sun Sep 18 00:00:00 EDT 2011>
      ##<Date Sun Sep 18 23:59:59 EDT 2011>] [#<Date Mon Sep 19
      #00:00:00 EDT 2011> #<Date Mon Sep 19 23:59:59 EDT 2011>]
      #[#<Date Tue Sep 20 00:00:00 EDT 2011> #<Date Tue Sep 20
      #23:59:59 EDT 2011>] [#<Date Wed Sep 21 00:00:00 EDT 2011>
      ##<Date Wed Sep 21 23:59:59 EDT 2011>] [#<Date Thu Sep 22
      #00:00:00 EDT 2011> #<Date Thu Sep 22 23:59:59 EDT 2011>]
      #[#<Date Fri Sep 23 00:00:00 EDT 2011> #<Date Fri Sep 23
      #23:59:59 EDT 2011>] [#<Date Sat Sep 24 00:00:00 EDT 2011>
      ##<Date Sat Sep 24 23:59:59 EDT 2011>] [#<Date Sun Sep 25
      #00:00:00 EDT 2011> #<Date Sun Sep 25 23:59:59 EDT 2011>]
      #[#<Date Mon Sep 26 00:00:00 EDT 2011> #<Date Mon Sep 26
      #23:59:59 EDT 2011>] [#<Date Tue Sep 27 00:00:00 EDT 2011>
      ##<Date Tue Sep 27 23:59:59 EDT 2011>] [#<Date Wed Sep 28
      #00:00:00 EDT 2011> #<Date Wed Sep 28 23:59:59 EDT 2011>]
      #[#<Date Thu Sep 29 00:00:00 EDT 2011> #<Date Thu Sep 29
      #23:59:59 EDT 2011>] [#<Date Fri Sep 30 00:00:00 EDT 2011>
      ##<Date Fri Sep 30 23:59:59 EDT 2011>])

bounded-cycles-in accepts a period and returns us a seq, breaking down
the input period to the resolution passed in. Now we have a day
granularity scale of one month. We can use the period-named? predicate
to find only the wednesdays in one month:

    => (filter #(period-named? % :wednesday) (bounded-cycles-in (first months) :day))

    ([#<Date Wed Sep 07 00:00:00 EDT 2011> #<Date Wed Sep 07 23:59:59
      #EDT 2011>] [#<Date Wed Sep 14 00:00:00 EDT 2011> #<Date Wed Sep
      #14 23:59:59 EDT 2011>] [#<Date Wed Sep 21 00:00:00 EDT 2011>
      ##<Date Wed Sep 21 23:59:59 EDT 2011>] [#<Date Wed Sep 28
      #00:00:00 EDT 2011> <Date Wed Sep 28 23:59:59 EDT 2011>])

Let's put this all together and make a seq of the third-wednesdays of every month:

    => (def days-in-months (map #(bounded-cycles-in % :day) months))
    => (defn wednesday? [period] (period-named? % :wednesday))
    => (def wednesdays-in-months (map #(filter wednesday? %) days-in-months))
    => (def third-wednesdays (map #(nth % 2) wednesdays-in-months))

Now we can make the TriClojure meetings by chopping up the third wednesday in every month into hours, and taking the 19th hour of the day:

    => (def triclojure-meetings (map #(-> % (bounded-cycles-in :hour)
     (nth 18)) third-wednesdays))

This seq is lazy and infinite, so we can grab as much as we want:

    => (take 3 triclojure-meetings)
    ([#<Date Wed Sep 21 18:00:00 EDT 2011> #<Date Wed Sep 21 18:59:59
      #EDT 2011>] [#<Date Wed Oct 19 18:00:00 EDT 2011> #<Date Wed Oct
      #19 18:59:59 EDT 2011>] [#<Date Wed Nov 16 18:00:00 EST 2011>
      ##<Date Wed Nov 16 18:59:59 EST 2011>])
