(ns error-helpers.core)

(defmacro err-let [err-fn bindings & body]
  (cond (zero? (count bindings))      `(do ~@body)
        (odd? (count bindings)) (throw (IllegalArgumentException. (str bindings " number of bindings must be even")))
        (nil? err-fn)  `(let* ~(destructure bindings) ~@body)
        (symbol? (bindings 0))        `(let ~(subvec bindings 0 2)
                                         (let [b0#    ~(bindings 0)
                                               err-b0# (~err-fn b0#)]
                                           (if err-b0#
                                             (let bindings body)b0#
                                             (let [~(bindings 0) b0#]
                                               (err-let ~err-fn ~(subvec bindings 2) ~@body)))))
        :else (throw (IllegalArgumentException. "binding must be a symbol"))))

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
      (get result err-key)
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
