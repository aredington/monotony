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

    lein repl
    => (require '[monotony.core :as m])

Monotony is dealing with time, and for it to be generally useful to
other Clojure developers it's been designed around an API of pure
functions. While it would be possible to use the local calendar and
current system time by default, this would make all of the API's
return results contextually sensitive to the environment in which it
is run, both in locality and in time. Consequently, much of monotony's
API requires a configuration that is significantly small. It consists
of two elements, :calendar and :seed.  :calendar is a function that
returns a new Calendar instance (which provides time formatting and
time offset information), while :seed is function which returns a new
UNIX time from which all calculations should be based. We will create
a new config with local time and calendar settings.

    => (def conf (m/local-config))
    #'user/conf

This is an event that occurs every month. Monotony can generate infinite
series of periods from some starting point in time with the periods fn.
Passed :month as a keyword, it generates month long periods, including
the period of the present month.

    => (def months (m/periods conf :month))
    #'user/months

Let's peek at the first 12 months of this seq:

    => (take 12 months)
    ([#<Date Tue Nov 01 00:00:00 EDT 2011> #<Date Wed Nov 30 23:59:59
      EST 2011>] [#<Date Thu Dec 01 00:00:00 EST 2011> #<Date Sat Dec
      31 23:59:59 EST 2011>] [#<Date Sun Jan 01 00:00:00 EST 2012>
      #<Date Tue Jan 31 23:59:59 EST 2012>] [#<Date Wed Feb 01
      00:00:00 EST 2012> #<Date Wed Feb 29 23:59:59 EST 2012>]
      [#<Date Thu Mar 01 00:00:00 EST 2012> #<Date Sat Mar 31
      23:59:59 EDT 2012>] [#<Date Sun Apr 01 00:00:00 EDT 2012>
      #<Date Mon Apr 30 23:59:59 EDT 2012>] [#<Date Tue May 01
      00:00:00 EDT 2012> #<Date Thu May 31 23:59:59 EDT 2012>]
      [#<Date Fri Jun 01 00:00:00 EDT 2012> #<Date Sat Jun 30
      23:59:59 EDT 2012>] [#<Date Sun Jul 01 00:00:00 EDT 2012>
      #<Date Tue Jul 31 23:59:59 EDT 2012>] [#<Date Wed Aug 01
      00:00:00 EDT 2012> #<Date Fri Aug 31 23:59:59 EDT 2012>]
      [#<Date Sat Sep 01 00:00:00 EDT 2012> #<Date Sun Sep 30
      23:59:59 EDT 2012>] [#<Date Mon Oct 01 00:00:00 EDT 2012>
      #<Date Wed Oct 31 23:59:59 EDT 2012>])

Now we can start slicing and dicing up these periods into smaller
cycles. We have two tools for this, cycles-in which divides up a
period by the cycle length, and bounded-cycles-in which divides up a
period so that the cuts align with the boundaries of the cycle passed
as an argument, according to the calendar provided by the
configuration.

    => (def nov (first months))
    #'user/nov

    => nov
    [#<Date Tue Nov 01 00:00:00 EDT 2011> #<Date Wed Nov 30 23:59:59 EST 2011>]

    => (m/bounded-cycles-in conf nov :day)
    ([#<Date Tue Nov 01 00:00:00 EDT 2011> #<Date Tue Nov 01 23:59:59
      EDT 2011>] [#<Date Wed Nov 02 00:00:00 EDT 2011> #<Date Wed Nov
      02 23:59:59 EDT 2011>] [#<Date Thu Nov 03 00:00:00 EDT 2011>
      #<Date Thu Nov 03 23:59:59 EDT 2011>] [#<Date Fri Nov 04
      00:00:00 EDT 2011> #<Date Fri Nov 04 23:59:59 EDT 2011>]
      [#<Date Sat Nov 05 00:00:00 EDT 2011> #<Date Sat Nov 05
      23:59:59 EDT 2011>] [#<Date Sun Nov 06 00:00:00 EDT 2011>
      #<Date Sun Nov 06 23:59:59 EST 2011>] [#<Date Mon Nov 07
      00:00:00 EST 2011> #<Date Mon Nov 07 23:59:59 EST 2011>]
      [#<Date Tue Nov 08 00:00:00 EST 2011> #<Date Tue Nov 08
      23:59:59 EST 2011>] [#<Date Wed Nov 09 00:00:00 EST 2011>
      #<Date Wed Nov 09 23:59:59 EST 2011>] [#<Date Thu Nov 10
      00:00:00 EST 2011> #<Date Thu Nov 10 23:59:59 EST 2011>]
      [#<Date Fri Nov 11 00:00:00 EST 2011> #<Date Fri Nov 11
      23:59:59 EST 2011>] [#<Date Sat Nov 12 00:00:00 EST 2011>
      #<Date Sat Nov 12 23:59:59 EST 2011>] [#<Date Sun Nov 13
      00:00:00 EST 2011> #<Date Sun Nov 13 23:59:59 EST 2011>]
      [#<Date Mon Nov 14 00:00:00 EST 2011> #<Date Mon Nov 14
      23:59:59 EST 2011>] [#<Date Tue Nov 15 00:00:00 EST 2011>
      #<Date Tue Nov 15 23:59:59 EST 2011>] [#<Date Wed Nov 16
      00:00:00 EST 2011> #<Date Wed Nov 16 23:59:59 EST 2011>]
      [#<Date Thu Nov 17 00:00:00 EST 2011> #<Date Thu Nov 17
      23:59:59 EST 2011>] [#<Date Fri Nov 18 00:00:00 EST 2011>
      #<Date Fri Nov 18 23:59:59 EST 2011>] [#<Date Sat Nov 19
      00:00:00 EST 2011> #<Date Sat Nov 19 23:59:59 EST 2011>]
      [#<Date Sun Nov 20 00:00:00 EST 2011> #<Date Sun Nov 20
      23:59:59 EST 2011>] [#<Date Mon Nov 21 00:00:00 EST 2011>
      #<Date Mon Nov 21 23:59:59 EST 2011>] [#<Date Tue Nov 22
      00:00:00 EST 2011> #<Date Tue Nov 22 23:59:59 EST 2011>]
      [#<Date Wed Nov 23 00:00:00 EST 2011> #<Date Wed Nov 23
      23:59:59 EST 2011>] [#<Date Thu Nov 24 00:00:00 EST 2011>
      #<Date Thu Nov 24 23:59:59 EST 2011>] [#<Date Fri Nov 25
      00:00:00 EST 2011> #<Date Fri Nov 25 23:59:59 EST 2011>]
      [#<Date Sat Nov 26 00:00:00 EST 2011> #<Date Sat Nov 26
      23:59:59 EST 2011>] [#<Date Sun Nov 27 00:00:00 EST 2011>
      #<Date Sun Nov 27 23:59:59 EST 2011>] [#<Date Mon Nov 28
      00:00:00 EST 2011> #<Date Mon Nov 28 23:59:59 EST 2011>]
      [#<Date Tue Nov 29 00:00:00 EST 2011> #<Date Tue Nov 29
      23:59:59 EST 2011>] [#<Date Wed Nov 30 00:00:00 EST 2011>
      #<Date Wed Nov 30 23:59:59 EST 2011>])

bounded-cycles-in accepts a period and returns us a seq, breaking down
the input period to the resolution passed in. Now we have a day
granularity scale of one month. We can use the period-named? predicate
to find only the wednesdays in one month:

    => (filter #(m/period-named? conf % :wednesday) (m/bounded-cycles-in conf nov :day))
    ([#<Date Wed Nov 02 00:00:00 EDT 2011> #<Date Wed Nov 02 23:59:59
      #EDT 2011>] [#<Date Wed Nov 09 00:00:00 EST 2011> #<Date Wed Nov
      #09 23:59:59 EST 2011>] [#<Date Wed Nov 16 00:00:00 EST 2011>
      ##<Date Wed Nov 16 23:59:59 EST 2011>] [#<Date Wed Nov 23
      #00:00:00 EST 2011> #<Date Wed Nov 23 23:59:59 EST 2011>]
      #[#<Date Wed Nov 30 00:00:00 EST 2011> #<Date Wed Nov 30
      #23:59:59 EST 2011>])

Now we can make some specific functions for plucking sub-periods of
time out of a bigger period, compose them, and generate our desired
time sequence. First let's pull the third wednesday out of a month.

    => (defn third-wednesday [month] (nth (filter #(m/period-named? conf % :wednesday) (m/bounded-cycles-in conf month :day)) 2))
    #'user/third-wednesday

Let's make another function to pluck 6PM out of a day long period:

    => (defn six-pm [day] (nth (m/bounded-cycles-in conf day :hour) 18))
    #'user/six-pm

If we apply this to a month long period, we'll get out the triclojure
meeting for that month:

    => ((comp six-pm third-wednesday) (first months))
    [#<Date Wed Nov 16 18:00:00 EST 2011> #<Date Wed Nov 16 18:59:59
     EST 2011>]

Now that we can chain these together, if we generate a sequence of
months, and apply our function to find the triclojure meeting in one
month, we have the infinite sequence of all triclojure meetings.

    => (def triclojure-meetings (map (comp six-pm third-wednesday) months))
    #'user/triclojure-meetings

    => (take 3 triclojure-meetings)
    ([#<Date Wed Nov 16 18:00:00 EST 2011> #<Date Wed Nov 16 18:59:59
      EST 2011>] [#<Date Wed Dec 21 18:00:00 EST 2011> #<Date Wed Dec
      21 18:59:59 EST 2011>] [#<Date Wed Jan 18 18:00:00 EST 2012>
      #<Date Wed Jan 18 18:59:59 EST 2012>])

## Acknowledgements

Monotony would not be possible without the following people:

* Alex Redington (core maintainer)
* Alan Dipert (co-design)
* David Nolen (for the excellent core.logic library and for contributing to monotony.logic)
