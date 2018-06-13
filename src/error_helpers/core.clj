(ns error-helpers.core)

(defmacro err-let [err-f bindings & body]
  (cond (zero? (count bindings))      `(do ~@body)
        (odd? (count bindings)) (throw (IllegalArgumentException. "number of bindings must be even"))
        (symbol? (bindings 0))        `(let ~(subvec bindings 0 2 )
                                         (cond
                                           (~err-f ~(bindings 0)) ~(bindings 0)
                                           :else (err-let ~err-f ~(subvec bindings 2) ~@body)))
        :else (throw (IllegalArgumentException. "binding must be a symbol"))))
