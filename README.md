# error-helpers

When calling a series of functions where the result from the previous function needs to be fed into the next,
a regular `let` would work just fine. What if some of these functions could fail returning an error? 
Wouldn't it be nice to have a `let` that can call a predicate checking for an error and short-cicruit the
computation while returning the first encountered error? `ok-let` does exactly this.

# dependency coordinates

```[com.danboykis/error-helpers "0.1.0"]```

# example
```clojure
;; test functions mimicing failure
(require '[error-helpers.core :refer [err-let]])
(defn service-a [n] (rand-int n))
(defn service-b [n] (if (even? n) {:myapp/error (str "n is even: " n)} (inc n)))
(defn service-c [n] (* 2 n))

(defn attempt-to-exec [n]
  (err-let :myapp/error
    [a (service-a n)
     b (service-b a)
     c (service-c b)]
    {:c c}))
```

```
user=> (attempt-to-exec 10)
{:c 8}
user=> (attempt-to-exec 10)
#:myapp{:error "n is even: 8"}
user=> (attempt-to-exec 10)
#:myapp{:error "n is even: 0"}
user=> (attempt-to-exec 10)
{:c 4}
```

```clojure
(require '[error-helpers.core :refer [thread-calls]])
```
```
(thread-calls :myapp/error [service-a service-b service-c] 10)
#:myapp{:error "n is even: 2"}
(thread-calls :myapp/error [service-a service-b service-c] 10)
#:myapp{:error "n is even: 0"}
(thread-calls :myapp/error [service-a service-b service-c] 10)
20
```

```clojure
(require '[error-helpers.core :refer [first-non-error-choice]])
```

```
(first-non-error-choice
  :myapp/error
  [#(let [rn (rand-int 100) m {:n rn :position 1}] (if (even? rn) m {:myapp/error m}))
   #(let [rn (rand-int 100) m {:n rn :position 2}] (if (even? rn) m {:myapp/error m}))
   #(let [rn (rand-int 100) m {:n rn :position 3}] (if (even? rn) m {:myapp/error m}))])


=> {:n 72, :position 1}

(first-non-error-choice
  :myapp/error
  [#(let [rn (rand-int 100) m {:n rn :position 1}] (if (even? rn) m {:myapp/error m}))
   #(let [rn (rand-int 100) m {:n rn :position 2}] (if (even? rn) m {:myapp/error m}))
   #(let [rn (rand-int 100) m {:n rn :position 3}] (if (even? rn) m {:myapp/error m}))])

=> #:myapp{:error [{:n 33, :position 1} {:n 93, :position 2} {:n 35, :position 3}]}

```
