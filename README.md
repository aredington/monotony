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
let's reason about generating an infinite sequence of periods representing
the hour of 6PM to 7PM on the Wednesday of the third week of every month.

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

    => (take 3 (map #(bounded-cycles-in % :week) months))
    (([#<Date Thu Sep 01 00:00:00 EDT 2011> #<Date Sat Sep 03 23:59:59 EDT
       #2011>] [#<Date Sun Sep 04 00:00:00 EDT 2011> #<Date Sat Sep 10
       #23:59:59 EDT 2011>] [#<Date Sun Sep 11 00:00:00 EDT 2011> #<Date
       #Sat Sep 17 23:59:59 EDT 2011>] [#<Date Sun Sep 18 00:00:00 EDT
       #2011> #<Date Sat Sep 24 23:59:59 EDT 2011>] [#<Date Sun Sep 25
       #00:00:00 EDT 2011> #<Date Fri Sep 30 23:59:59 EDT 2011>]) ([#<Date
       #Sat Oct 01 00:00:00 EDT 2011> #<Date Sat Oct 01 23:59:59 EDT
       #2011>] [#<Date Sun Oct 02 00:00:00 EDT 2011> #<Date Sat Oct 08
       #23:59:59 EDT 2011>] [#<Date Sun Oct 09 00:00:00 EDT 2011> #<Date
       #Sat Oct 15 23:59:59 EDT 2011>] [#<Date Sun Oct 16 00:00:00 EDT
       #2011> #<Date Sat Oct 22 23:59:59 EDT 2011>] [#<Date Sun Oct 23
       #00:00:00 EDT 2011> #<Date Sat Oct 29 23:59:59 EDT 2011>] [#<Date
       #Sun Oct 30 00:00:00 EDT 2011> #<Date Mon Oct 31 23:59:59 EDT
       #2011>]) ([#<Date Tue Nov 01 00:00:00 EDT 2011> #<Date Sat Nov 05
       #23:59:59 EDT 2011>] [#<Date Sun Nov 06 00:00:00 EDT 2011> #<Date
       #Sat Nov 12 23:59:59 EST 2011>] [#<Date Sun Nov 13 00:00:00 EST
       #2011> #<Date Sat Nov 19 23:59:59 EST 2011>] [#<Date Sun Nov 20
       #00:00:00 EST 2011> #<Date Sat Nov 26 23:59:59 EST 2011>] [#<Date
       #Sun Nov 27 00:00:00 EST 2011> #<Date Wed Nov 30 23:59:59 EST
       #2011>]))

One of the seqs in this seq of seqs contains all of the weeks in one month.
The concatenation of these seqs is the weeks in each month. Let's chop that
down to only the third week in each month.

    => (def third-weeks (map #(nth % 2) (map #(bounded-cycles-in % :week) months)))
    => (take 3 third-weeks)
    ([#<Date Sun Sep 11 00:00:00 EDT 2011> #<Date Sat Sep 17 23:59:59 EDT
      #2011>] [#<Date Sun Oct 09 00:00:00 EDT 2011> #<Date Sat Oct 15
      #23:59:59 EDT 2011>] [#<Date Sun Nov 13 00:00:00 EST 2011> #<Date
      #Sat Nov 19 23:59:59 EST 2011>])

We continue the slicing and dicing from weeks to days:

    => (def wednesday-of-third-weeks (map #(nth % 3) (map #(bounded-cycles-in % :day) third-weeks)))
    => (take 3 wednesday-of-third-weeks )
    ([#<Date Wed Sep 14 00:00:00 EDT 2011> #<Date Wed Sep 14 23:59:59 EDT
      #2011>] [#<Date Wed Oct 12 00:00:00 EDT 2011> #<Date Wed Oct 12
      #23:59:59 EDT 2011>] [#<Date Wed Nov 16 00:00:00 EST 2011> #<Date
      #Wed Nov 16 23:59:59 EST 2011>])

And now we slice down to the hour from 6 to 7PM:

    => (def meetings (map #(nth % 18) (map #(cycles-in % :hour) wednesday-of-third-weeks)))
    => (take 3 meetings)
    ([#<Date Wed Sep 14 18:00:00 EDT 2011> #<Date Wed Sep 14 18:59:59 EDT
      #2011>] [#<Date Wed Oct 12 18:00:00 EDT 2011> #<Date Wed Oct 12
      #18:59:59 EDT 2011>] [#<Date Wed Nov 16 18:00:00 EST 2011> #<Date
      #Wed Nov 16 18:59:59 EST 2011>])

This seq is lazy and infinite, so we can grab as much as we want:

    => (take 12 meetings)
    ([#<Date Wed Sep 14 18:00:00 EDT 2011> #<Date Wed Sep 14 18:59:59 EDT
      #2011>] [#<Date Wed Oct 12 18:00:00 EDT 2011> #<Date Wed Oct 12
      #18:59:59 EDT 2011>] [#<Date Wed Nov 16 18:00:00 EST 2011> #<Date
      #Wed Nov 16 18:59:59 EST 2011>] [#<Date Wed Dec 14 18:00:00 EST
      #2011> #<Date Wed Dec 14 18:59:59 EST 2011>] [#<Date Wed Jan 18
      #18:00:00 EST 2012> #<Date Wed Jan 18 18:59:59 EST 2012>] [#<Date
      #Wed Feb 15 18:00:00 EST 2012> #<Date Wed Feb 15 18:59:59 EST 2012>]
      #[#<Date Wed Mar 14 18:00:00 EDT 2012> #<Date Wed Mar 14 18:59:59
      #EDT 2012>] [#<Date Wed Apr 18 18:00:00 EDT 2012> #<Date Wed Apr 18
      #18:59:59 EDT 2012>] [#<Date Wed May 16 18:00:00 EDT 2012> #<Date
      #Wed May 16 18:59:59 EDT 2012>] [#<Date Wed Jun 13 18:00:00 EDT
      #2012> #<Date Wed Jun 13 18:59:59 EDT 2012>] [#<Date Wed Jul 18
      #18:00:00 EDT 2012> #<Date Wed Jul 18 18:59:59 EDT 2012>] [#<Date
      #Wed Aug 15 18:00:00 EDT 2012> #<Date Wed Aug 15 18:59:59 EDT
      #2012>])
