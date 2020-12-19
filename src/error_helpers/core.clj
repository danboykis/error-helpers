(ns error-helpers.core)

(defmacro err-let [err-fn bindings & body]
  (cond
    (odd? (count bindings))
    (throw (IllegalArgumentException. (str bindings " number of bindings must be even")))

    (zero? (count bindings))
    `(do ~@body)

    (nil? err-fn)
    `(let* ~(destructure bindings) ~@body)

    :else
    `(let [b1# ~(bindings 1)]
       (if (some? (~err-fn b1#))
         b1#
         (let [~(bindings 0) b1#]
           (err-let ~err-fn ~(subvec bindings 2) ~@body))))))

(defmacro if-pred [pred? bindings then else]
  (when (not= 2 (count bindings)) (throw (IllegalArgumentException. (str bindings " number of bindings must be 2"))))
  (let [form (bindings 0) tst (bindings 1)]
    `(let [t# ~tst]
       (if (~pred? t#)
         (let [~form t#]
           ~then)
         ~else))))

(defmacro default-to [operation default-value]
  `(try
     ~operation
     (catch Exception e#
       ~default-value)))

(defn thread-calls [err-key calls init-arg]
  {:pre [(not (empty? calls))]}
  (loop [[c & cs] calls result init-arg]
    (cond
      (some? (get result err-key))
      result

      (nil? c)
      result

      :else
      (recur cs (c result)))))

(defn first-non-error-choice [err-key pick-fn choices]
  (loop [[choice & cs] choices errors []]
    (if (nil? choice)
      {err-key errors}
      (let [result (pick-fn (choice))]
        (if-some [err (get result err-key)]
          (recur cs (conj errors err))
          result)))))
