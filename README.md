# error-helpers

When calling a series of functions where the result from the previous function needs to be fed into the next,
a regular `let` would work just fine. What if some of these functions could fail returning an error? 
Wouldn't it be nice to have a `let` that can call a predicate checking for an error and short-cicruit the
computation while returning the first encountered error? `ok-let` does exactly this.

# dependency coordinates

```[error-helpers "0.2.0-SNAPSHOT"]```

# example
```clojure
;; test functions mimicing failure
(require '[error-helpers.core :refer [ok-let]])
(defn service-a [n] {:myapp/ok (rand-int n)})
(defn service-b [n] (if (even? n) {:myapp/error (str "n is even: " n)} {:myapp/ok (inc n)}))
(defn service-c [n] {:ok (* 2 n)})

(defn attempt-to-exec [n]
  (ok-let :myapp/ok :myapp/ok #(hash-map :myapp/error %)
    [a (service-a n)
     b (service-b a)
     c (service-c b)]
    {:c c}))
```

```
user=> (attempt-to-exec 10)
{:myapp/ok {:c 8}}
user=> (attempt-to-exec 10)
#:myapp{:error "n is even: 8"}
user=> (attempt-to-exec 10)
#:myapp{:error "n is even: 0"}
user=> (attempt-to-exec 10)
{:myapp/ok {:c 4}}
```

# Limitations

Currently `ok-let` doesn't support destructuring
