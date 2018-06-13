# error-helpers

When calling a series of functions where the result from the previous function needs to be fed into the next,
a regular `let` would work just fine. What if some of these functions could fail returning an error? 
Wouldn't it be nice to have a `let` that can call a predicate checking for an error and short-cicruit the
computation while returning the first encountered error? `err-let` does exactly this.

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

# Limitations

Currently `err-let` doesn't support destructuring
